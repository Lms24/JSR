package at.tugraz.ist.stracke.jsr.core.tsr.strategies;

import at.tugraz.ist.stracke.jsr.core.coverage.CoverageReport;
import at.tugraz.ist.stracke.jsr.core.shared.TestSuite;
import at.tugraz.ist.stracke.jsr.core.tsr.ReducedTestSuite;
import at.tugraz.ist.stracke.jsr.core.tsr.TSRTestCase;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

/**
 * This Reduction Strategy implements a slightly improved version of the initial
 * {@link GreedyHGSReductionStrategy}. It is unclear if Harrold, Gupta and Soffa (2015)
 * described this or the latter algorithm, thus we are keeping both for the moment.
 *
 * The improvement over the original implementation is two-fold:
 * 1. After each TC selection round, the table is updated completely, meaning that all
 *    items in all columns of requirements satisfied by the selected TC are marked.
 *    This removes already satisfied requirements and potential TC candidates for further
 *    iterations that would be pointless to investigate further and might lead to suboptimal
 *    results.
 * 2. When selecting a requirement r_b for which a TC is selected, only TCs that satisfy
 *    at least the same number of requirements as TCs r_B are considered. This comes naturally
 *    as a consequence of 1.), as there cannot be columns with less entries than the one currently
 *    under investigation.
 *
 * Since this algorithm is very closely related to {@link GreedyHGSReductionStrategy},
 * we are extending the class and overriding behaviour in the style of a slightly
 * modified "template method" pattern (namely, not using an abstract class as a
 * base class).
 */
public class GreedyIHGSReductionStrategy extends GreedyHGSReductionStrategy {

  public GreedyIHGSReductionStrategy() {
    super();
  }

  public GreedyIHGSReductionStrategy(@NonNull TestSuite originalTestsuite,
                                     @NonNull CoverageReport coverageReport) {
    super(originalTestsuite, coverageReport);
  }

  @Override
  public @NonNull ReducedTestSuite reduce() {
    return super.reduce();
  }

  @Override
  protected void updateTable(TSRTestCase tcToKeep) {
    super.getRequirementsSatisfiedByTestCase(tcToKeep).forEach(r -> {
      this.unmarkedRequirements.remove(r);
      this.table.put(tcToKeep, r, false);
      this.getTestCasesSatisfyingRequirement(r).forEach(tc -> this.table.put(tc, r, false));
    });
  }

  @Override
  protected TSRTestCase getTestCaseToKeep(List<TSRTestCase> column, AtomicLong nrOfSatisfyingTCs) {
    return column.stream().reduce(column.get(0), (curr, next) -> findNextBestTestCase(curr, next, nrOfSatisfyingTCs));
  }
}
