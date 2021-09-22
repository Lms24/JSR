package at.tugraz.ist.stracke.jsr.core.tsr.strategies;

import at.tugraz.ist.stracke.jsr.core.coverage.CoverageReport;
import at.tugraz.ist.stracke.jsr.core.shared.TestCase;
import at.tugraz.ist.stracke.jsr.core.shared.TestSuite;
import at.tugraz.ist.stracke.jsr.core.tsr.ReducedTestSuite;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.*;
import java.util.stream.Collectors;

/**
 * This Reduction strategy is a simple greedy reduction algorithm that does
 * not employ any heuristic. It simply iterates through the requirements and takes the
 * first testcase that satisfies the requirement.
 *
 * This greedy algorithm is based on the description of a traditional greedy algorithm
 * by El-Deeb et al. (EHGSA, 2015).
 */
public class GreedyReductionStrategy implements ReductionStrategy {

  private TestSuite originalTestsuite;
  private Table<TestCase, CoverageReport.Unit, Boolean> table;
  private Deque<CoverageReport.Unit> unmarkedRequirements;

  public GreedyReductionStrategy() {
  }

  public GreedyReductionStrategy(@NonNull TestSuite originalTestsuite,
                                 @NonNull CoverageReport coverageReport) {
    this.originalTestsuite = originalTestsuite;
    this.table = HashBasedTable.create();
    this.unmarkedRequirements = new ArrayDeque<>();
    this.populateTableFromCoverageReport(coverageReport);
  }

  @Override
  public @NonNull ReducedTestSuite reduce() {
    List<TestCase> retainedTCs = new ArrayList<>();
    boolean allReqsMarked = unmarkedRequirements.isEmpty();
    while (!allReqsMarked) {
      CoverageReport.Unit req = this.unmarkedRequirements.pop();
      final List<Map.Entry<TestCase, Boolean>> column = new ArrayList<>(this.table.column(req).entrySet());

      // choose the testcase satisfying the most requirements
      TestCase tcToKeep = column.stream().reduce(column.get(0), (ret, curr) -> {
        var currReqs = this.table.row(curr.getKey()).size();
        var retReqs = this.table.row(ret.getKey()).size();
        return (currReqs > retReqs) ? curr : ret;
      }).getKey();

      retainedTCs.add(tcToKeep);

      this.table.row(tcToKeep).forEach((r, in) -> this.unmarkedRequirements.remove(r));
      allReqsMarked = this.unmarkedRequirements.isEmpty();
    }

    final List<TestCase> removedTCs = this.originalTestsuite.testCases.stream().filter(
      t -> !retainedTCs.contains(t)).collect(Collectors.toList());

    return new ReducedTestSuite(retainedTCs, removedTCs);
  }

  private void populateTableFromCoverageReport(CoverageReport rep) {
    rep.testCaseCoverageData.forEach((tc, units) ->
                                       units.forEach(u -> this.table.put(tc, u, true)));
    this.unmarkedRequirements.addAll(rep.coveredUnits);
    //this.unmarkedRequirements.sort(Comparator.comparing(a -> this.table.column(a).size()));
  }

  @Override
  public void setCoverageReport(CoverageReport report) {
    this.table = HashBasedTable.create();
    this.unmarkedRequirements = new ArrayDeque<>();
    this.populateTableFromCoverageReport(report);
  }

  @Override
  public void setOriginalTestSuite(TestSuite testSuite) {
    this.originalTestsuite = testSuite;
  }
}
