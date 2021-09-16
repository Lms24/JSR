package at.tugraz.ist.stracke.jsr.core.tsr.strategies;

import at.tugraz.ist.stracke.jsr.core.coverage.CoverageReport;
import at.tugraz.ist.stracke.jsr.core.shared.TestCase;
import at.tugraz.ist.stracke.jsr.core.shared.TestSuite;
import at.tugraz.ist.stracke.jsr.core.tsr.ReducedTestSuite;
import at.tugraz.ist.stracke.jsr.core.tsr.TSRTestCase;
import com.google.common.collect.*;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * This Reduction Strategy implements a greedy TSR algorithm with the
 * HGS heuristic as originally proposed by Harrold, Gupta and Soffa (HGS) in 1993.
 * <p>
 * It is often used as a baseline for TSR comparisons.
 * <p>
 * The main idea is to select test cases first that satisfy requirements which are
 * "hard" to satisfy (i.e. that only one or few TCs actually cover). The first step
 * is to determine an order of "hard" requirements.
 * Once a hard requirement is chosen, the next step is to select a TC that satisfies it.
 * The trivial case is, if only one TC satisfies it (then this one is chosen). In case of
 * multiple candidate TCs, it is determined which candidate TC satisfies the most next-"hard"
 * requirements. In case of a tie (i.e. >1 TCs satisfying the same number of hard requirement)
 * the hardness is reduced and ultimately a random selection is made.
 * Additionally, the table containing the TC<->Requirement mapping is updated after
 * a TC was selected: All requirements satisfied by the TC are marked in the testcase.
 * This procedure is repeated with decreasing hardness as long as there are unsatisfied
 * requirements.
 * <p>
 * The outcome is a reduced test suite. However, the solution is only guaranteed to be
 * locally optimal. A better global optimum might exist that is not found by the
 * algorithm/heuristic.
 */
public class GreedyHGSReductionStrategy implements ReductionStrategy {
  private final TestSuite originalTestsuite;
  protected final Table<TSRTestCase, CoverageReport.Unit, Boolean> table;
  protected final Deque<CoverageReport.Unit> unmarkedRequirements;

  public GreedyHGSReductionStrategy(@NonNull TestSuite originalTestsuite,
                                    @NonNull CoverageReport coverageReport) {
    this.originalTestsuite = originalTestsuite;

    List<TSRTestCase> rows =
      originalTestsuite.testCases.stream()
                                 .sorted(Comparator.comparing(TestCase::getFullName))
                                 .map(TSRTestCase::new)
                                 .collect(Collectors.toList());
    List<CoverageReport.Unit> columns =
      coverageReport.coveredUnits.stream()
                                 .sorted(Comparator.comparing(CoverageReport.Unit::toString))
                                 .collect(Collectors.toList());
    this.table = ArrayTable.create(rows, columns);

    this.populateTableFromCoverageReport(coverageReport);

    this.unmarkedRequirements =
      coverageReport.coveredUnits.stream()
                                 .sorted(Comparator.comparing(u -> filterSatisfyingTestCases(u).count()))
                                 .collect(Collectors.toCollection(ArrayDeque::new));
  }

  private Stream<Boolean> filterSatisfyingTestCases(CoverageReport.Unit r) {
    return this.table.column(r)
                     .values()
                     .stream().filter(b -> b != null && b);
  }

  @Override
  public @NonNull ReducedTestSuite reduce() {
    List<TestCase> retainedTCs = new ArrayList<>();
    boolean allReqsMarked = unmarkedRequirements.isEmpty();
    while (!allReqsMarked) {
      CoverageReport.Unit req = this.unmarkedRequirements.pop();
      final List<TSRTestCase> column = this.getTestCasesSatisfyingRequirement(req);

      AtomicLong nrOfSatisfyingTCs = new AtomicLong(column.size());

      TSRTestCase tcToKeep = column.size() > 1 ? getTestCaseToKeep(column, nrOfSatisfyingTCs) : column.get(0);

      retainedTCs.add(tcToKeep);

      updateTable(tcToKeep);

      allReqsMarked = this.unmarkedRequirements.isEmpty();
    }

    final List<TestCase> removedTCs = this.originalTestsuite.testCases.stream().filter(
      t -> !retainedTCs.contains(t)).collect(Collectors.toList());

    return new ReducedTestSuite(retainedTCs, removedTCs);
  }

  protected void updateTable(TSRTestCase tcToKeep) {
    this.getRequirementsSatisfiedByTestCase(tcToKeep).forEach(r -> {
      this.unmarkedRequirements.remove(r);
      this.table.put(tcToKeep, r, false);
    });
  }

  protected TSRTestCase getTestCaseToKeep(List<TSRTestCase> column, AtomicLong nrOfSatisfyingTCs) {
    return column.stream().reduce(column.get(0), (curr, next) -> findNextBestTestCase(curr, next, new AtomicLong(1)));
  }

  protected TSRTestCase findNextBestTestCase(TSRTestCase curr, TSRTestCase next, AtomicLong target) {
    var currReqs = getRequirementsSatisfiedByTestCase(curr);
    var nextReqs = getRequirementsSatisfiedByTestCase(next);

    long targetN = target.get();

    TSRTestCase result = curr;
    boolean tie = curr != next;
    while (tie) {
      // number of testCases that satisfy the current requirement
      long finalTargetN = targetN;
      long currentN =
        currReqs.stream()
                .filter(r -> getTestCasesSatisfyingRequirement(r).size() == finalTargetN)
                .count();
      // number of testCases that satisfy the next requirement
      long finalTargetN1 = targetN;
      long nextN =
        nextReqs.stream()
                .filter(r -> getTestCasesSatisfyingRequirement(r).size() == finalTargetN1)
                .count();

      tie = currentN == nextN && currentN > 0;
      if (tie) {
        targetN++;
      } else if (nextN > currentN) {
        result = next;
      }
    }

    return result;
  }


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

  private void populateTableFromCoverageReport(CoverageReport rep) {
    rep.testCaseCoverageData.forEach((tc, units) ->
                                       units.forEach(u -> this.table.put(new TSRTestCase(tc), u, true)));
  }
}
