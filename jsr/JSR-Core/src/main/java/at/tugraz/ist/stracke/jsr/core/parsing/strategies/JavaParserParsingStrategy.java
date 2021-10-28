package at.tugraz.ist.stracke.jsr.core.parsing.strategies;

import at.tugraz.ist.stracke.jsr.core.parsing.misc.CompilationUnitExtractor;
import at.tugraz.ist.stracke.jsr.core.parsing.statements.AssertionStatement;
import at.tugraz.ist.stracke.jsr.core.parsing.statements.IStatement;
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
import java.util.stream.Collectors;

public class JavaParserParsingStrategy implements ParsingStrategy {

  private static final Logger logger = LogManager.getLogger(ParsingStrategy.class);
  private final CompilationUnitExtractor cuExtractor;
  private Path filePath;

  public JavaParserParsingStrategy(@NonNull String code) {
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
    List<CompilationUnit> compilationUnits = cuExtractor.getCompilationUnits();
    List<TestSuite> partialSuites = compilationUnits.stream()
                                                    .map(this::parseTestSuite)
                                                    .collect(Collectors.toList());

    return mergePartialSuites(partialSuites);
  }

  private TestSuite parseTestSuiteFromString() {
    logger.info("Parsing test suite from String code");

    CompilationUnit cu = cuExtractor.getCompilationUnits().get(0);

    return parseTestSuite(cu);
  }

  private TestSuite parseTestSuite(CompilationUnit cu) {
    var testCaseMethods =
      cu.findAll(MethodDeclaration.class).stream()
        .filter(decl -> decl.getAnnotations()
                            .stream()
                            .anyMatch(a -> a.getNameAsString().equals("Test")))
        /*LS (28.10.21) Adding the filter to exclude ignored test cases. They do not add value to TSR */
        .filter(decl -> decl.getAnnotations()
                             .stream()
                             .noneMatch(a -> a.getNameAsString().equals("Ignore") || a.getNameAsString().equals("Disabled")));

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

    final ClassOrInterfaceDeclaration clazz = (ClassOrInterfaceDeclaration) decl.getParentNode().orElse(null);
    final String className = clazz != null ? clazz.getFullyQualifiedName().orElse("unknown") : "error";

    return new JUnitTestCase(
      decl.getNameAsString(),
      className,
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
