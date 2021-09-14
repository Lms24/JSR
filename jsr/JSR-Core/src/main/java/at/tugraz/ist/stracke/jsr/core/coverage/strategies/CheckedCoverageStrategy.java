package at.tugraz.ist.stracke.jsr.core.coverage.strategies;

import at.tugraz.ist.stracke.jsr.core.coverage.CoverageReport;
import at.tugraz.ist.stracke.jsr.core.parsing.TestSuiteParser;
import at.tugraz.ist.stracke.jsr.core.parsing.statements.Statement;
import at.tugraz.ist.stracke.jsr.core.shared.TestCase;
import at.tugraz.ist.stracke.jsr.core.slicing.TestSuiteSlicer;
import at.tugraz.ist.stracke.jsr.core.slicing.result.SliceEntry;
import at.tugraz.ist.stracke.jsr.core.slicing.result.TestSuiteSliceResult;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class CheckedCoverageStrategy implements CoverageStrategy {

  private final TestSuiteParser parser;
  private final TestSuiteSlicer slicer;

  public CheckedCoverageStrategy(TestSuiteParser parser,
                                 TestSuiteSlicer slicer) {
    this.parser = parser;
    this.slicer = slicer;
  }

  @Override
  public CoverageReport calculateOverallCoverage() {
    List<Statement> executableStatements = this.parser.getParsingStrategy().parseStatements();
    TestSuiteSliceResult res = this.slicer.slice();


    Set<CoverageReport.Unit> coveredUnits = res.getTestCaseSliceUnion()
                                               .stream()
                                               .map(SliceEntry::toStatement)
                                               .map(Statement::toUnit)
                                               .collect(Collectors.toSet());
    Set<CoverageReport.Unit> allUnits = executableStatements.stream()
                                                            .map(Statement::toUnit)
                                                            .collect(Collectors.toSet());

    Map<TestCase, Set<CoverageReport.Unit>> testCaseData =
      res.testCaseSlices.stream()
                        .collect(Collectors.toMap(tcs -> tcs.testCase,
                                                  tcs -> tcs.slice.stream()
                                                                  .map(se -> se.toStatement().toUnit())
                                                                  .collect(Collectors.toSet())));

    return new CoverageReport(allUnits, coveredUnits, testCaseData);
  }
}
