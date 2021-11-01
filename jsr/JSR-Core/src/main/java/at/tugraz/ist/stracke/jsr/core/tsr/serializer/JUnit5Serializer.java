package at.tugraz.ist.stracke.jsr.core.tsr.serializer;

import at.tugraz.ist.stracke.jsr.core.tsr.TSRReport;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import org.apache.logging.log4j.LogManager;

import java.nio.file.Path;

public class JUnit5Serializer extends JUnitSerializer {

  public JUnit5Serializer(Path testDir) {
    super(testDir, LogManager.getLogger(JUnit5Serializer.class));
  }

  @Override
  void addAnnotation(MethodDeclaration methodDeclaration, String reason) {
    methodDeclaration.addAndGetAnnotation("Disabled").addPair("", reason);
  }

  @Override
  void importAnnotation(CompilationUnit cu) {
    cu.addImport("org.junit.jupiter.api.Disabled");
  }

  @Override
  void importTestMarkerAnnotation(CompilationUnit cu) {
    if (cu.getImports().stream().noneMatch(i -> i.getNameAsString().equals("org.junit.jupiter.api.Test"))) {
      cu.addImport("org.junit.jupiter.api.Test");
    }
  }
}
