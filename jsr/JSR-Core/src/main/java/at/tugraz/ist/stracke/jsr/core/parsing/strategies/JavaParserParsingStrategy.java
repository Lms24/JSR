package at.tugraz.ist.stracke.jsr.core.parsing.strategies;

import at.tugraz.ist.stracke.jsr.core.parsing.statements.AssertionStatement;
import at.tugraz.ist.stracke.jsr.core.parsing.statements.IStatement;
import at.tugraz.ist.stracke.jsr.core.shared.JUnitTestCase;
import at.tugraz.ist.stracke.jsr.core.shared.TestCase;
import at.tugraz.ist.stracke.jsr.core.shared.TestSuite;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.stmt.Statement;
import org.apache.logging.log4j.LogManager;

import java.io.FileNotFoundException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
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
      return parseFromString();
    }
  }

  private TestSuite parseFromString() {
    logger.info("Parsing from String code");

    CompilationUnit cu = StaticJavaParser.parse(code);

    var testCaseMethods = cu.findAll(MethodDeclaration.class).stream().filter(decl ->
      decl.getAnnotations().stream().anyMatch(a -> a.getNameAsString().equals("Test")));

    List<TestCase> tcs = testCaseMethods
      .map(this::mapDeclarationToTestCase)
      .collect(Collectors.toList());

    logger.info("Found {} test case{}", tcs.size(), tcs.size() != 1 ? "s" : "");

    tcs.forEach(tc -> logger.info(tc.toString()));

    return new TestSuite(tcs);
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

    return new JUnitTestCase(
      decl.getNameAsString(),
      decl.getParentNode().isPresent() ?
        ((ClassOrInterfaceDeclaration) decl.getParentNode().get()).getNameAsString() : null,
      aStmts);
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
}
