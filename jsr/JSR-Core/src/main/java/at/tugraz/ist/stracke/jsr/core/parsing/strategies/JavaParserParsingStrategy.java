package at.tugraz.ist.stracke.jsr.core.parsing.strategies;

import at.tugraz.ist.stracke.jsr.core.parsing.misc.CompilationUnitExtractor;
import at.tugraz.ist.stracke.jsr.core.parsing.statements.AssertionStatement;
import at.tugraz.ist.stracke.jsr.core.parsing.statements.IStatement;
import at.tugraz.ist.stracke.jsr.core.shared.ConcreteTestSuite;
import at.tugraz.ist.stracke.jsr.core.shared.JUnitTestCase;
import at.tugraz.ist.stracke.jsr.core.shared.TestCase;
import at.tugraz.ist.stracke.jsr.core.shared.TestSuite;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.Statement;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

public class JavaParserParsingStrategy implements ParsingStrategy {

  private static final Logger logger = LogManager.getLogger(ParsingStrategy.class);
  private final CompilationUnitExtractor cuExtractor;
  private Path filePath;
  private List<TestSuite> abstractPartialSuites;

  public JavaParserParsingStrategy(@NonNull List<String> code) {
    this.cuExtractor = new CompilationUnitExtractor(code);
  }

  public JavaParserParsingStrategy(@NonNull Path filePath) {
    this.filePath = filePath;
    this.cuExtractor = new CompilationUnitExtractor(filePath);
  }


  @Override
  public List<at.tugraz.ist.stracke.jsr.core.parsing.statements.Statement> parseStatements() {
    if (this.filePath != null) {
      return parseStatementsFromFilePath();
    } else {
      return parseStatementsFromString();
    }
  }

  @Override
  public TestSuite parseTestSuite() {
    if (this.filePath != null) {
      return parseTestSuiteFromFilePath();
    } else {
      return parseTestSuiteFromString();
    }
  }

  private TestSuite parseTestSuiteFromFilePath() {
    logger.info("Parsing test suite from File Path");
    return parseTestSuiteFromMultipleCompilationUnits();
  }

  private TestSuite parseTestSuiteFromString() {
    logger.info("Parsing test suite from String code");
    return parseTestSuiteFromMultipleCompilationUnits();
  }

  private TestSuite parseTestSuiteFromMultipleCompilationUnits() {
    List<CompilationUnit> compilationUnits = cuExtractor.getCompilationUnits();
    List<CompilationUnit> filteredCUs = preFilterCUs(compilationUnits);
    List<CompilationUnit> abstractClassCUs = this.findAbstractClasses(filteredCUs);
    List<CompilationUnit> concreteClassCUs = this.findConcreteClasses(filteredCUs, abstractClassCUs);
    List<CompilationUnit> extendingClassCUs = this.findExtendingClasses(concreteClassCUs);

    abstractPartialSuites = abstractClassCUs.stream()
                                            .map(cu -> parseTestSuite(cu, false))
                                            .filter(Objects::nonNull)
                                            .collect(Collectors.toList());
    List<ConcreteTestSuite> concretePartialSuites = concreteClassCUs.stream()
                                                                    .map(cu -> parseTestSuite(cu, true))
                                                                    .filter(Objects::nonNull)
                                                                    .collect(Collectors.toList());
    this.findFullExtendedClasses(concretePartialSuites, abstractPartialSuites);

    boolean needsAnotherRound = true;
    while (needsAnotherRound) {
      needsAnotherRound = this.assignParentTestsToConcreteSuites(abstractPartialSuites, concretePartialSuites);
    }

    List<TestSuite> finalTestSuites = concretePartialSuites.stream()
                                                           .map(cts -> new TestSuite(cts.testCases))
                                                           .collect(Collectors.toList());
    return mergePartialSuites(finalTestSuites);
  }

  private List<CompilationUnit> preFilterCUs(List<CompilationUnit> compilationUnits) {
    return compilationUnits.stream()
                           .filter(cu -> {
                             final List<ClassOrInterfaceDeclaration> all =
                               cu.findAll(ClassOrInterfaceDeclaration.class);
                             if (all.isEmpty()) {
                               return false;
                             }
                             return all.get(0)
                                       .getAnnotations()
                                       .stream()
                                       .noneMatch(a -> a.getNameAsString().equals("Ignore") ||
                                                       a.getNameAsString().equals("Disabled"));
                           })
                           .collect(Collectors.toList());
  }

  private boolean assignParentTestsToConcreteSuites(List<TestSuite> abstractPartialSuites,
                                                 List<ConcreteTestSuite> concretePartialSuites) {
    AtomicBoolean modifiedSomething = new AtomicBoolean(false);
    abstractPartialSuites.forEach(ats -> concretePartialSuites.forEach(cts -> {
      final String abstractClassName = ats.testClasses.toArray()[0].toString();
      if (abstractClassName.equals(cts.extendedClass)) {
        final String concreteClassName = cts.testClasses.toArray()[0].toString();
        ats.testCases.forEach(atc -> {
          TestCase newTC = new JUnitTestCase(atc.getName(), concreteClassName, atc.getAssertions());
          newTC.parentClassName = abstractClassName;
          if (cts.testCases.stream().noneMatch(ctc -> ctc.getName().equals(newTC.getName()))) {
            cts.testCases.add(newTC);
            modifiedSomething.set(true);
          }
        });
        cts.extendedClass = null;
      }
    }));

    List<ConcreteTestSuite> extendingSuites = concretePartialSuites.stream()
                                                                   .filter(cps -> cps.extendedClass != null)
                                                                   .collect(Collectors.toList());
    extendingSuites.forEach(ets -> concretePartialSuites.forEach(cps -> {
      final String parentClassName = ets.extendedClass;
      final String childClassName = ets.testClasses.toArray()[0].toString();
      final String concreteClassName = cps.testClasses.toArray()[0].toString();

      if (parentClassName.equals(concreteClassName)) {
        cps.testCases.forEach(cts -> {
          TestCase newTC = new JUnitTestCase(cts.getName(), childClassName, cts.getAssertions());
          if (ets.testCases.stream().noneMatch(etc -> etc.getName().equals(newTC.getName()))) {
            ets.testCases.add(newTC);
            modifiedSomething.set(true);
          }
        });
      }
    }));
    return modifiedSomething.get();
  }

  private void findFullExtendedClasses(List<ConcreteTestSuite> concretePartialSuites, List<TestSuite> abstractSuites) {
    concretePartialSuites.stream()
                         .filter(cps -> cps.extendedClass != null)
                         .forEach(cps -> {
                           concretePartialSuites.stream()
                                                .map(x -> x.testClasses.toArray()[0].toString())
                                                .filter(x -> x.endsWith("." + cps.extendedClass))
                                                .findFirst()
                                                .ifPresent((x) -> cps.extendedClass = x);
                           abstractSuites.stream()
                                         .map(x -> x.testClasses.toArray()[0].toString())
                                         .filter(x -> x.endsWith("." + cps.extendedClass))
                                         .findFirst()
                                         .ifPresent((x) -> cps.extendedClass = x);
                         });
  }

  private List<CompilationUnit> findExtendingClasses(List<CompilationUnit> compilationUnits) {
    return compilationUnits.stream()
                           .filter(cu -> {
                             List<ClassOrInterfaceDeclaration> classes = cu.findAll(ClassOrInterfaceDeclaration.class);
                             if (classes == null || classes.isEmpty()) {
                               return false;
                             }
                             ClassOrInterfaceDeclaration clazz = classes.get(0);
                             return clazz.getExtendedTypes().isNonEmpty();
                           })
                           .collect(Collectors.toList());
  }

  private List<CompilationUnit> findConcreteClasses(List<CompilationUnit> compilationUnits, List<CompilationUnit> abstractClassCUs) {
    return compilationUnits.stream()
                           .filter(cu -> !abstractClassCUs.contains(cu))
                           .collect(Collectors.toList());
  }

  private List<CompilationUnit> findAbstractClasses(List<CompilationUnit> compilationUnits) {
    return compilationUnits.stream()
                           .filter(cu -> cu.findAll(ClassOrInterfaceDeclaration.class)
                                           .get(0).getModifiers()
                                           .stream()
                                           .anyMatch(m -> m.getKeyword().toString().equals("ABSTRACT")))
                           .collect(Collectors.toList());
  }


  private ConcreteTestSuite parseTestSuite(CompilationUnit cu, boolean concrete) {
    var testCaseMethods =
      cu.findAll(MethodDeclaration.class).stream()
        .filter(decl -> decl.getAnnotations()
                            .stream()
                            .anyMatch(a -> a.getNameAsString().equals("Test")));

    List<TestCase> tcs = testCaseMethods
      .map(this::mapDeclarationToTestCase)
      .collect(Collectors.toList());
    logger.info("Found {} test case{}", tcs.size(), tcs.size() != 1 ? "s" : "");

    if (!concrete && tcs.isEmpty()) {
      return null;
    }

    tcs.forEach(tc -> logger.info(tc.toString()));

    final ConcreteTestSuite cts = new ConcreteTestSuite(tcs);
    if (concrete) {
      final ClassOrInterfaceDeclaration clazz = cu.findAll(ClassOrInterfaceDeclaration.class)
                                                  .get(0);
      if (clazz != null && clazz.getExtendedTypes().isNonEmpty()) {
        //if (this.abstractPartialSuites != null && !this.abstractPartialSuites.isEmpty()) {
          /*
          cts.extendedClass = this.abstractPartialSuites.stream()
                                                        .filter(s -> s.testClasses.toArray()[0].toString()
                                                                                               .contains(partialName))
                                                        .map(s -> s.testClasses.toArray()[0].toString())
                                                        .findFirst().orElse("Error");
           */
          cts.extendedClass = clazz.getExtendedTypes().get(0).getNameAsString();
        //}
      }
      if (clazz != null && cts.testClasses.isEmpty()) {
        cts.testClasses.add(clazz.getFullyQualifiedName().orElse("Error"));
      }
    }
    return cts;
  }

  private JUnitTestCase mapDeclarationToTestCase(MethodDeclaration decl) {
    List<IStatement> aStmts = new ArrayList<>();

    if (decl.getBody().isPresent()) {
      aStmts = decl.getBody().get().findAll(Statement.class).stream()
                   .filter(Statement::isExpressionStmt)
                   .filter(stmt -> stmt.toString().toLowerCase().contains("assert"))
                   .map(this::mapStatementToAssertionStatement)
                   .collect(Collectors.toList());
    }

    final ClassOrInterfaceDeclaration clazz = (ClassOrInterfaceDeclaration) decl.getParentNode().orElse(null);
    final String className = clazz != null ? clazz.getFullyQualifiedName().orElse("unknown") : "error";

    final JUnitTestCase jUnitTestCase = new JUnitTestCase(
      decl.getNameAsString(),
      className,
      aStmts);

    /*LS (28.10.21) Adding the filter to exclude ignored test cases. They do not add value to TSR */
    jUnitTestCase.disabled = decl.getAnnotations()
                                 .stream()
                                 .anyMatch(a -> a.getNameAsString().equals("Ignore") ||
                                                a.getNameAsString().equals("Disabled"));

    return jUnitTestCase;
  }

  private AssertionStatement mapStatementToAssertionStatement(Statement stmt) {
    Set<String> refs = stmt.findAll(Expression.class).stream()
                           .filter(e -> e.isNameExpr() || e.isFieldAccessExpr())
                           .map(Node::toString)
                           .collect(Collectors.toSet());

    return new AssertionStatement(
      stmt.toString(),
      stmt.getBegin().isPresent() ? stmt.getBegin().get().line : 0,
      stmt.getEnd().isPresent() ? stmt.getEnd().get().line : 0,
      refs
    );
  }

  private List<at.tugraz.ist.stracke.jsr.core.parsing.statements.Statement> parseStatementsFromFilePath() {
    logger.info("Parsing executable lines from File Path");
    List<CompilationUnit> compilationUnits = cuExtractor.getCompilationUnits();
    List<List<at.tugraz.ist.stracke.jsr.core.parsing.statements.Statement>> partialStatementLists =
      compilationUnits.stream()
                      .map(this::parseStatements)
                      .collect(Collectors.toList());
    return this.mergePartialStatementLists(partialStatementLists);
  }

  private List<at.tugraz.ist.stracke.jsr.core.parsing.statements.Statement> parseStatementsFromString() {
    logger.info("Parsing test suite from String code");

    CompilationUnit cu = cuExtractor.getCompilationUnits().get(0);

    return parseStatements(cu);
  }

  private List<at.tugraz.ist.stracke.jsr.core.parsing.statements.Statement> parseStatements(CompilationUnit cu) {
    List<at.tugraz.ist.stracke.jsr.core.parsing.statements.Statement> executableStatements = new ArrayList<>();

    cu.findAll(ClassOrInterfaceDeclaration.class).forEach(clazz -> {
      var stmts = clazz.findAll(Statement.class).stream()
                       .filter(s -> !(s instanceof BlockStmt))
                       .map(s -> mapToJSRStatement(clazz, s))
                       .collect(Collectors.toList());
      executableStatements.addAll(stmts);
    });

    return executableStatements;
  }

  private at.tugraz.ist.stracke.jsr.core.parsing.statements.Statement mapToJSRStatement(ClassOrInterfaceDeclaration clazz,
                                                                                        Statement s) {
    final int beg = s.getBegin().isPresent() ? s.getBegin().get().line : -1;
    final int end = s.getEnd().isPresent() ? s.getEnd().get().line : -1;
    var fullClassName =
      clazz.getFullyQualifiedName().isPresent() ?
      clazz.getFullyQualifiedName().get() : clazz.getName().asString();
    var jsrStmt = new at.tugraz.ist.stracke.jsr.core.parsing.statements.Statement(s.toString(), beg, end);
    jsrStmt.setClassName(fullClassName);
    return jsrStmt;
  }

  private TestSuite mergePartialSuites(List<TestSuite> partialSuites) {
    if (partialSuites.isEmpty()) {
      return new TestSuite(Collections.emptyList());
    }

    List<TestCase> allTCs = partialSuites.stream()
                                         .map(ts -> ts.testCases)
                                         .collect(Collectors.toList())
                                         .stream()
                                         .flatMap(Collection::stream)
                                         .filter(tc -> !tc.disabled)
                                         .collect(Collectors.toList());

    final TestSuite testSuite = new TestSuite(allTCs);

    logger.info("====================================================================");
    logger.info("Finished parsing the test suite.");
    logger.info("It contains {} test cases and {} assertions.",
                testSuite.getNumberOfTestCases(),
                testSuite.getNumberOfAssertions());

    return testSuite;
  }

  private List<at.tugraz.ist.stracke.jsr.core.parsing.statements.Statement> mergePartialStatementLists(
    List<List<at.tugraz.ist.stracke.jsr.core.parsing.statements.Statement>> partialStatementLists) {

    return partialStatementLists.stream()
                                .flatMap(Collection::stream)
                                .collect(Collectors.toList());
  }

}
