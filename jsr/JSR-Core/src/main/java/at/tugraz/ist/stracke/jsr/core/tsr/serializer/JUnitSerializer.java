package at.tugraz.ist.stracke.jsr.core.tsr.serializer;

import at.tugraz.ist.stracke.jsr.core.parsing.misc.CompilationUnitExtractor;
import at.tugraz.ist.stracke.jsr.core.shared.TestCase;
import at.tugraz.ist.stracke.jsr.core.tsr.ReducedTestSuite;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import org.apache.logging.log4j.Logger;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

public abstract class JUnitSerializer implements Serializer {

  protected final Logger logger;

  private final Path testDir;
  private Path outDir;

  private ReducedTestSuite rts;

  private List<String> modifiedCode;

  public JUnitSerializer(@NonNull Path testDir,
                         @NonNull Logger logger) {
    this.logger = logger;
    this.testDir = testDir;
  }

  /**
   * Template method for concrete classes to be implemented.
   * <p>
   * Implementors must add an annotation to the test methods that shall
   * no longer be executed so that these methods are skipped by the test
   * runner.
   *
   * @param methodDeclaration the {@link MethodDeclaration} of the method to be modified
   * @param reason            the reason/message given alongside the annotation
   */
  abstract void addAnnotation(MethodDeclaration methodDeclaration, String reason);

  abstract void importAnnotation(CompilationUnit cu);

  @Override
  public void serialize(boolean writeToFile) {
    // 1. Parse CUs
    List<CompilationUnit> compilationUnits = new CompilationUnitExtractor(testDir).getCompilationUnits();

    // 2. Modify ASTs
    AtomicBoolean modified = new AtomicBoolean(false);
    compilationUnits.forEach(cu -> modified.set(this.modifyCompilationUnit(cu) || modified.get()));

    if (modified.get()) {
      this.modifiedCode = compilationUnits.stream().map(Node::toString).collect(Collectors.toList());;
    }

    // 3. Write to file
    if (writeToFile && modified.get()) {
      compilationUnits.forEach(this::writeCompilationUnit);
    }
  }

  @Override
  public Serializer setOutputDirectory(Path outDir) {
    this.outDir = outDir;
    return this;
  }

  @Override
  public Serializer setReducedTestSuite(ReducedTestSuite rts) {
    this.rts = rts;
    return this;
  }

  @Override
  public List<String> getModifiedCode() {
    return this.modifiedCode;
  }

  private boolean modifyCompilationUnit(CompilationUnit cu) {
    final List<TestCase> removedTestCases = rts.removedTestCases;

    final Set<String> classNames = removedTestCases.stream()
                                                   .map(TestCase::getClassName)
                                                   .collect(Collectors.toSet());

    AtomicBoolean modifiedSomething = new AtomicBoolean(false);

    cu.findAll(ClassOrInterfaceDeclaration.class).forEach(clazz -> {
      String fullClassName = clazz.getFullyQualifiedName().orElse("");
      if (classNames.contains(fullClassName)) {
        final List<TestCase> testCasesToModify = removedTestCases.stream()
                                                                 .filter(tc -> tc.getClassName().equals(fullClassName))
                                                                 .collect(Collectors.toList());

        final List<MethodDeclaration> allMethods = clazz.findAll(MethodDeclaration.class);

        modifiedSomething.set(this.modifyTestMethods(allMethods, testCasesToModify));
      }
    });

    if (modifiedSomething.get()) {
      this.importAnnotation(cu);
    }

    return modifiedSomething.get();
  }

  private boolean modifyTestMethods(List<MethodDeclaration> methods, List<TestCase> testCasesToModify) {
    AtomicBoolean changedSomething = new AtomicBoolean(false);
    methods.forEach(m -> {
      if (testCasesToModify.stream().anyMatch(tc -> tc.getName().equals(m.getNameAsString()))) {
        this.addAnnotation(m, "Redundant Test Case (identified by JSR)");
        changedSomething.set(true);
      }
    });
    return changedSomething.get();
  }

  private void writeCompilationUnit(CompilationUnit cu) {
    final CompilationUnit.Storage cuStorage = cu.getStorage().orElseThrow(
      () -> new IllegalStateException("No Storage Present"));

    /*
     * Here we re-create the directory sub structure (which is mostly based on the package structure)
     * based on the directory information of the original compilation unit
     */
    String packageDirs = cuStorage.getPath().toString().replace(cuStorage.getSourceRoot().toString(), "");
    packageDirs = packageDirs.replace("\\", "/")
                             .replace(cuStorage.getFileName(), "")
                             .substring(1);
    packageDirs = packageDirs.substring(0, packageDirs.length() - 1);

    // outputPath == serialization root dir + package structure + file name
    Path outputPath = (this.outDir == null) ? cuStorage.getPath() : Path.of(this.outDir.toString(),
                                                                            packageDirs,
                                                                            cuStorage.getFileName());
    if (!outputPath.getParent().toFile().exists()) {
      boolean success = outputPath.getParent().toFile().mkdirs();
      if (!success) {
        logger.error("Error while creating directory {}", outputPath.getParent().toString());
      }
    }

    try {
      Files.write(outputPath, cu.toString().getBytes(StandardCharsets.UTF_8));
    } catch (IOException e) {
      logger.error("Error while writing File: {}", e.getMessage());
      Arrays.stream(e.getStackTrace()).forEach(logger::error);
      e.printStackTrace();
      return;
    }

    logger.info("Wrote {} to {}", cuStorage.getFileName(), outputPath.toString());
  }
}