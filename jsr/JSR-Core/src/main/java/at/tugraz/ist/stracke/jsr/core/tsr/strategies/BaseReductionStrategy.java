package at.tugraz.ist.stracke.jsr.core.tsr.strategies;

import at.tugraz.ist.stracke.jsr.core.coverage.CoverageReport;
import at.tugraz.ist.stracke.jsr.core.shared.TestCase;
import at.tugraz.ist.stracke.jsr.core.shared.TestSuite;
import at.tugraz.ist.stracke.jsr.core.tsr.ReducedTestSuite;
import at.tugraz.ist.stracke.jsr.core.tsr.TSRTestCase;
import com.google.common.collect.Table;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Class BaseReductionStrategy
 */
abstract class BaseReductionStrategy implements ReductionStrategy {

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
   * @return
   */
  abstract @NonNull public ReducedTestSuite reduce();

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
    return this.originalTestsuite.testCases.stream().filter(
      t -> !retainedTCs.contains(t)).collect(Collectors.toList());
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
