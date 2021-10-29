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
import java.util.stream.Collectors;

public class JavaParserParsingStrategy implements ParsingStrategy {

  private static final Logger logger = LogManager.getLogger(ParsingStrategy.class);
  private final CompilationUnitExtractor cuExtractor;
  private Path filePath;

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
    List<CompilationUnit> abstractClassCUs = this.findAbstractClasses(compilationUnits);
    List<CompilationUnit> concreteClassCUs = this.findConcreteClasses(compilationUnits, abstractClassCUs);

    List<TestSuite> abstractPartialSuites = abstractClassCUs.stream()
                                                            .map(cu -> parseTestSuite(cu, false))
                                                            .collect(Collectors.toList());
    List<ConcreteTestSuite> concretePartialSuites = concreteClassCUs.stream()
                                                                    .map(cu -> parseTestSuite(cu, true))
                                                                    .collect(Collectors.toList());

    this.assignAbstractTestsToConcreteSuites(abstractPartialSuites, concretePartialSuites);

    List<TestSuite> finalTestSuites = concretePartialSuites.stream()
                                                           .map(cts -> new TestSuite(cts.testCases))
                                                           .collect(Collectors.toList());
    return mergePartialSuites(finalTestSuites);
  }

  private void assignAbstractTestsToConcreteSuites(List<TestSuite> abstractPartialSuites,
                                                   List<ConcreteTestSuite> concretePartialSuites) {
    abstractPartialSuites.forEach(ats -> concretePartialSuites.forEach(cts -> {
      final String abstractClassName = ats.testClasses.toArray()[0].toString();
      if (abstractClassName.equals(cts.extendedClass)) {
        ats.testCases.forEach(atc -> {
          final String concreteClassName = cts.testClasses.toArray()[0].toString();
          TestCase newTC = new JUnitTestCase(atc.getName(), concreteClassName, atc.getAssertions());
          if (!cts.testCases.contains(newTC)) {
            cts.testCases.add(newTC);
          }
        });
      }
    }));
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

    tcs.forEach(tc -> logger.info(tc.toString()));

    final ConcreteTestSuite cts = new ConcreteTestSuite(tcs);
    if (concrete) {
      final ClassOrInterfaceDeclaration clazz = cu.findAll(ClassOrInterfaceDeclaration.class).get(
        0);
      if (clazz != null && clazz.getExtendedTypes().isNonEmpty()) {
        cts.extendedClass = clazz.getExtendedTypes().get(0).getNameAsString();
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
