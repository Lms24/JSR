package at.tugraz.ist.stracke.jsr.core.tsr.strategies;

import at.tugraz.ist.stracke.jsr.core.coverage.CoverageReport;
import at.tugraz.ist.stracke.jsr.core.shared.TestCase;
import at.tugraz.ist.stracke.jsr.core.shared.TestSuite;
import at.tugraz.ist.stracke.jsr.core.tsr.ReducedTestSuite;
import at.tugraz.ist.stracke.jsr.core.tsr.TSRTestCase;
import com.google.common.collect.Table;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Class BaseReductionStrategy
 */
abstract class BaseReductionStrategy implements ReductionStrategy {

  public static final boolean KEEP_IRRELEVANT_TCS = false;

  protected TestSuite originalTestsuite;
  protected CoverageReport coverageReport;
  protected Table<TSRTestCase, CoverageReport.Unit, Boolean> table;

  public BaseReductionStrategy() {
  }

  public BaseReductionStrategy(@NonNull TestSuite originalTestsuite,
                               @NonNull CoverageReport coverageReport) {
    this.originalTestsuite = originalTestsuite;
    this.coverageReport = coverageReport;
    this.table = coverageReport.toTable(false);
  }

  /**
   * This obviously needs to be implemented by the concrete classes.
   *
   * @return
   */
  abstract public @NonNull ReducedTestSuite reduce();

  protected List<TSRTestCase> getTestCasesSatisfyingRequirement(CoverageReport.Unit req) {
    return this.table.column(req)
                     .entrySet()
                     .stream()
                     .filter(e -> e.getValue() != null && e.getValue())
                     .map(Map.Entry::getKey)
                     .collect(Collectors.toList());
  }


  protected List<CoverageReport.Unit> getRequirementsSatisfiedByTestCase(TSRTestCase req) {
    return this.table.row(req).entrySet()
                     .stream()
                     .filter(e -> e.getValue() != null && e.getValue())
                     .map(Map.Entry::getKey)
                     .collect(Collectors.toList());
  }

  protected List<TestCase> getRemovedTCs(List<TestCase> retainedTCs) {
    final List<TestCase> allTCs = KEEP_IRRELEVANT_TCS ?
                                  new ArrayList<>(this.coverageReport.testCaseCoverageData.keySet()) :
                                  this.originalTestsuite.testCases;

    final List<TestCase> removedTCs = allTCs.stream()
                                            .filter(t -> !retainedTCs.contains(t))
                                            .collect(Collectors.toList());

    if (KEEP_IRRELEVANT_TCS) {
      retainedTCs.addAll(this.originalTestsuite.testCases.stream()
                                                         .filter(tc -> !removedTCs.contains(tc))
                                                         .filter(tc -> !retainedTCs.contains(tc))
                                                         .collect(Collectors.toList()));
    }

    return removedTCs;
  }

  @Override
  public void setCoverageReport(CoverageReport report) {
    this.coverageReport = report;
    this.table = report.toTable(false);
  }

  @Override
  public void setOriginalTestSuite(TestSuite testSuite) {
    this.originalTestsuite = testSuite;
  }

}
