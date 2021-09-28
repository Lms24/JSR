package at.tugraz.ist.stracke.jsr.core.tsr.serializer;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import org.apache.logging.log4j.LogManager;

import java.nio.file.Path;

public class JUnit4Serializer extends JUnitSerializer {

  public JUnit4Serializer(Path testDir) {
    super(testDir, LogManager.getLogger(JUnit4Serializer.class));
  }

  @Override
  void addAnnotation(MethodDeclaration methodDeclaration, String reason) {
    methodDeclaration.addSingleMemberAnnotation("Ignore", String.format("\"%s\"", reason));
  }

  @Override
  void importAnnotation(CompilationUnit cu) {
    cu.addImport("org.junit.Ignore");
  }
}
