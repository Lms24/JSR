package at.tugraz.ist.stracke.jsr.core.coverage.strategies;

import at.tugraz.ist.stracke.jsr.core.coverage.CoverageReport;
import at.tugraz.ist.stracke.jsr.core.parsing.TestSuiteParser;
import at.tugraz.ist.stracke.jsr.core.parsing.statements.Statement;
import at.tugraz.ist.stracke.jsr.core.shared.TestCase;
import at.tugraz.ist.stracke.jsr.core.shared.TestSuite;
import at.tugraz.ist.stracke.jsr.core.slicing.TestSuiteSlicer;
import at.tugraz.ist.stracke.jsr.core.slicing.result.SliceEntry;
import at.tugraz.ist.stracke.jsr.core.slicing.result.TestSuiteSliceResult;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class CheckedCoverageStrategy implements CoverageStrategy {

  private static final Logger logger = LogManager.getLogger(CheckedCoverageStrategy.class);

  private TestSuite originalTestSuite;
  private TestSuiteParser parser;
  private TestSuiteSlicer slicer;

  public CheckedCoverageStrategy(TestSuite originalTestSuite,
                                 TestSuiteParser parser,
                                 TestSuiteSlicer slicer) {
    this.originalTestSuite = originalTestSuite;
    this.parser = parser;
    this.slicer = slicer;
  }

  public CheckedCoverageStrategy(TestSuiteParser parser,
                                 TestSuiteSlicer slicer) {
    this.parser = parser;
    this.slicer = slicer;
  }

  @Override
  public CoverageReport calculateOverallCoverage() {
    List<Statement> executableStatements = this.parser.getParsingStrategy().parseStatements();
    TestSuiteSliceResult res = this.slicer.slice();

    Set<CoverageReport.Unit> allUnits = executableStatements.stream()
                                                            .filter(s -> !originalTestSuite.testClasses.contains(s.getClassName()))
                                                            .map(Statement::toUnit)
                                                            .collect(Collectors.toSet());

    Set<CoverageReport.Unit> coveredUnits = res.getTestCaseSliceUnion()
                                               .stream()
                                               .filter(s -> !originalTestSuite.testClasses.contains(s.className))
                                               .map(SliceEntry::toStatement)
                                               .map(Statement::toUnit)
                                               .filter(u -> allUnits.stream().anyMatch(u2 -> u.name.equals(u2.name) && u.positionStart == u2.positionStart)) // only units that we marked as executable should be in here
                                               .collect(Collectors.toSet());


    Map<TestCase, Set<CoverageReport.Unit>> testCaseData =
      res.testCaseSlices.stream()
                        .collect(Collectors.toMap(tcs -> tcs.testCase,
                                                  tcs -> tcs.slice.stream()
                                                                  .filter(s -> !originalTestSuite.testClasses.contains(s.className))
                                                                  .map(se -> se.toStatement().toUnit())
                                                                  .filter(u -> allUnits.stream().anyMatch(u2 -> u.name.equals(u2.name) && u.positionStart == u2.positionStart)) // only units that we marked as executable should be in here
                                                                  .collect(Collectors.toSet())));

    CoverageReport report = new CoverageReport(allUnits, coveredUnits, testCaseData);

    logger.info("Generated Checked Coverage Report:");
    logger.info("Found {} coverable lines, {} covered lines. Coverage Score: {}",
                allUnits.size(), coveredUnits.size(), report.getCoverageScore());

    return report;
  }

  @Override
  public void setOriginalTestSuite(TestSuite testSuite) {
    this.originalTestSuite = testSuite;
  }

  @Override
  public void setSlicer(TestSuiteSlicer slicer) {
    this.slicer = slicer;
  }

  @Override
  public void setStatementParser(TestSuiteParser parser) {
    this.parser = parser;
  }
}
