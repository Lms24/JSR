package at.tugraz.ist.stracke.jsr.core.parsing.strategies;

import at.tugraz.ist.stracke.jsr.core.parsing.utils.JUnitTestCase;
import at.tugraz.ist.stracke.jsr.core.parsing.utils.TestCase;
import at.tugraz.ist.stracke.jsr.core.parsing.utils.TestSuite;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import org.apache.logging.log4j.LogManager;

import java.io.FileNotFoundException;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.logging.log4j.Logger;

public class JavaParserParsingStrategy implements ParsingStrategy {

  private Path filePath;
  private String code;

  private static final Logger logger = LogManager.getLogger(ParsingStrategy.class);

  public JavaParserParsingStrategy(String code) {
    this.code = code;
  }

  public JavaParserParsingStrategy(Path filePath) {
    this.filePath = filePath;
  }

  @Override
  public TestSuite execute() {
    if (this.filePath != null) {
      logger.info("Parsing from File Path");
      try {
        //TODO
        CompilationUnit cu = StaticJavaParser.parse(this.filePath.toFile());
      } catch (FileNotFoundException e) {
        e.printStackTrace();
      }
      return null;
    } else {
      logger.info("Parsing from String code");

      CompilationUnit cu = StaticJavaParser.parse(code);

      var testCaseMethods = cu.findAll(MethodDeclaration.class).stream().filter(decl ->
        decl.getAnnotations().stream().anyMatch(a -> a.getNameAsString().equals("Test")));

      List<TestCase> tcs = testCaseMethods.map(decl ->
        new JUnitTestCase(
          decl.getNameAsString(),
          decl.getParentNode().isPresent() ?
          ((ClassOrInterfaceDeclaration) decl.getParentNode().get()).getNameAsString() : null,
          null)).collect(Collectors.toList());

      logger.info("Found {} test cases", tcs.size());

      return new TestSuite(tcs);
    }
  }
}
