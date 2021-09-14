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
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.Statement;
import org.apache.logging.log4j.LogManager;

import java.io.FileNotFoundException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.logging.log4j.Logger;
import org.checkerframework.checker.nullness.qual.NonNull;

public class JavaParserParsingStrategy implements ParsingStrategy {

  private static final Logger logger = LogManager.getLogger(ParsingStrategy.class);
  private Path filePath;
  private String code;

  public JavaParserParsingStrategy(@NonNull String code) {
    this.code = code;
  }

  public JavaParserParsingStrategy(@NonNull Path filePath) {
    this.filePath = filePath;
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
    try {
      CompilationUnit cu = StaticJavaParser.parse(this.filePath.toFile());
      return parseTestSuite(cu);
    } catch (FileNotFoundException e) {
      logger.error("Error while parsing test suite from file path");
      e.printStackTrace();
      return null;
    }
  }

  private TestSuite parseTestSuiteFromString() {
    logger.info("Parsing test suite from String code");

    CompilationUnit cu = StaticJavaParser.parse(code);

    return parseTestSuite(cu);
  }

  private TestSuite parseTestSuite(CompilationUnit cu) {
    var testCaseMethods = cu.findAll(MethodDeclaration.class).stream().filter(decl ->
                                                                                decl.getAnnotations().stream().anyMatch(
                                                                                  a -> a.getNameAsString()
                                                                                        .equals("Test")));

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

  private List<at.tugraz.ist.stracke.jsr.core.parsing.statements.Statement> parseStatementsFromFilePath() {
    logger.info("Parsing executable lines from File Path");
    try {
      CompilationUnit cu = StaticJavaParser.parse(this.filePath.toFile());
      return parseStatements(cu);
    } catch (FileNotFoundException e) {
      logger.error("Error while parsing statements from file path");
      e.printStackTrace();
      return null;
    }
  }

  private List<at.tugraz.ist.stracke.jsr.core.parsing.statements.Statement> parseStatementsFromString() {
    logger.info("Parsing test suite from String code");

    CompilationUnit cu = StaticJavaParser.parse(code);

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
}
