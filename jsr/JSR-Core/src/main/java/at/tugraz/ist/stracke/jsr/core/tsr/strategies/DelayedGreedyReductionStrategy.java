package at.tugraz.ist.stracke.jsr.core.tsr.strategies;

import at.tugraz.ist.stracke.jsr.core.coverage.CoverageReport;
import at.tugraz.ist.stracke.jsr.core.shared.TestCase;
import at.tugraz.ist.stracke.jsr.core.shared.TestSuite;
import at.tugraz.ist.stracke.jsr.core.tsr.ReducedTestSuite;
import at.tugraz.ist.stracke.jsr.core.tsr.TSRTestCase;
import com.google.common.collect.ArrayTable;
import com.google.common.collect.Table;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

/**
 * The DelayedGreedyReductionStrategy is a {@link ReductionStrategy} that
 * uses the "Delayed Greedy" algorithm proposed by Tallam and Gupta 2005.
 * <p>
 * It iteratively applies a set of reduction heuristics inferred from
 * the "concept analysis framework" followed by a greedy test case selection
 * if necessary. The authors report results that are at least as good
 * as the conventional greedy and the greedy HGS algorithm. If no
 * greedy interference is necessary, the algorithm is guaranteed
 * to deliver an optimal solution.
 */
@SuppressWarnings("UnstableApiUsage") // Guava ArrayTable
public class DelayedGreedyReductionStrategy extends BaseReductionStrategy {

  private final boolean MULTIPLE_REDUCTIONS_PER_STEP = false;

  private final List<TestCase> retainedTCs = new ArrayList<>();

  boolean optimalResult = false;

  public DelayedGreedyReductionStrategy() {
    super(LogManager.getLogger(DelayedGreedyReductionStrategy.class));
  }

  public DelayedGreedyReductionStrategy(@NonNull TestSuite originalTestsuite,
                                        @NonNull CoverageReport coverageReport) {
    super(originalTestsuite,
          coverageReport,
          LogManager.getLogger(DelayedGreedyReductionStrategy.class));
  }

  @Override
  public @NonNull ReducedTestSuite reduce() {
    int nrInterferences = 0;

    while (isNonEmptyContextTable(table)) {
      boolean interferenceNecessary = false;

      while (!interferenceNecessary && isNonEmptyContextTable(table)) {
        // Step 1: Object Reduction
        interferenceNecessary = !this.performObjectReduction();
        // Step 2: Attribute Reduction
        interferenceNecessary &= !this.performAttributeReduction();
        // Step 3: Owner Reduction
        interferenceNecessary &= !this.performOwnerReduction();
      }

      // Step 4: Greedy Heuristic
      if (isNonEmptyContextTable(table)) {
        this.performGreedySelection();
        ++nrInterferences;
      }
    }

    if (nrInterferences > 0) {
      logger.info("The greedy heuristic was used {} times --> reduction result might not be optimal.",
                  nrInterferences);
      this.optimalResult = false;
    } else {
      logger.info("The greedy heuristic was not used during TSR --> reduction result is optimal!");
      this.optimalResult = true;
    }

    List<TestCase> removedTCs = super.getRemovedTCs(retainedTCs);
    return new ReducedTestSuite(retainedTCs, removedTCs);
  }

  /**
   * Step 1 - Object reduction:
   * In this step, a row r_i is removed from the table iff there is another
   * row r_j that covers all requirements of r_i and possibly even other requirements.
   * <p>
   * In other words, this means that a test case t_i is excluded from the retained TCs,
   * iff another testcase t_j covers at least all of t_i's requirements and possibly
   * additional ones.
   *
   * @return true if at least one object reduction was performed.
   */
  private boolean performObjectReduction() {
    //logger.debug("Trying Object Reduction with table size {}", table.size());
    boolean performedOR = false;

    List<Map.Entry<TSRTestCase,Map<CoverageReport.Unit, Boolean>>> entries = new ArrayList<>(table.rowMap().entrySet());

    for (var entry_i : entries) {
      if (performedOR) break;

      TSRTestCase tc_i = entry_i.getKey();
      List<CoverageReport.Unit> coveredUnits_i = getRequirementsSatisfiedByTestCase(tc_i);

      for (var entry_j : entries) {
        TSRTestCase tc_j = entry_j.getKey();
        if (tc_i == tc_j) {
          break;
        }

        List<CoverageReport.Unit> coveredUnits_j = getRequirementsSatisfiedByTestCase(tc_j);

        TestCase tcToKeep = null, tcToRemove = null;
        List<CoverageReport.Unit> coveredUnitsToRemove = null;
        if (coveredUnits_j.containsAll(coveredUnits_i)) {
          tcToRemove = tc_i;
          tcToKeep = tc_j;
          coveredUnitsToRemove = coveredUnits_i;
        } else if (coveredUnits_i.containsAll(coveredUnits_j)) {
          tcToRemove = tc_j;
          tcToKeep = tc_i;
          coveredUnitsToRemove = coveredUnits_j;
        }

        if (tcToKeep != null && tcToRemove != null) {
          logger.debug("OR because {} => {}", tcToKeep.getName(), tcToRemove.getName());
          final TestCase finalTcToRemove = tcToRemove;
          coveredUnitsToRemove.forEach(u -> ((ArrayTable<?, ?, ?>) table).erase(finalTcToRemove, u));
          performedOR = true;
          this.updateTable();
          if (MULTIPLE_REDUCTIONS_PER_STEP) {
            this.performObjectReduction();
          }
          break;
        }
      }
    }

    return performedOR;
  }

  /**
   * Step 2: Attribute Reduction
   * In this step a column (=requirement/unit) c_j compared against the other columns. In case
   * there is a column c_i whose set of TCs covering it is a superset of the testcases covering c_j,
   * the column c_i is removed.
   *
   * @return true if at least one attribute reduction was performed
   */
  private boolean performAttributeReduction() {
    //logger.debug("Trying Attribute Reduction with table size {}", table.size());
    boolean performedAR = false;

    for (var entry : table.columnMap().entrySet()) {
      if (performedAR) break;

      CoverageReport.Unit req_j = entry.getKey();
      List<TSRTestCase> coveringTCs_j = getTestCasesSatisfyingRequirement(req_j);

      for (var e : table.columnMap().entrySet()) {
        CoverageReport.Unit req_i = e.getKey();
        if (req_i == req_j) {
          break;
        }

        List<TSRTestCase> coveringTCs_i = getTestCasesSatisfyingRequirement(req_i);

        CoverageReport.Unit reqToKeep = null, reqToRemove = null;
        List<TSRTestCase> coveringTCsToRemove = null;
        if (coveringTCs_i.containsAll(coveringTCs_j)) {
          reqToKeep = req_j;
          reqToRemove = req_i;
          coveringTCsToRemove = coveringTCs_i;
        } else if (coveringTCs_j.containsAll(coveringTCs_i)) {
          reqToKeep = req_i;
          reqToRemove = req_j;
          coveringTCsToRemove = coveringTCs_j;
        }

        if (reqToKeep != null && reqToRemove != null) {
          logger.debug("AR because {} => {}", reqToKeep.name, reqToRemove.name);
          final CoverageReport.Unit finalReqToRemove = reqToRemove;
          coveringTCsToRemove.forEach(tc -> ((ArrayTable<?, ?, ?>) table).erase(tc, finalReqToRemove));
          performedAR = true;
          this.updateTable();
          if (MULTIPLE_REDUCTIONS_PER_STEP) {
            performAttributeReduction();
          }
          break;
        }
      }
    }

    return performedAR;
  }

  /**
   * Step 3: Owner Reduction
   * In this step we try to find "owners", i.e. test cases that are the only ones that
   * satisfy a requirement. These TCs thus "own" the requirement and have to be selected
   * for the reduced test suite.
   *
   * @return true if at least one owner reduction was performed
   */
  private boolean performOwnerReduction() {
    //logger.debug("Trying Owner Reduction with table size {}", table.size());
    boolean performedOwnerReduction = false;

    for (var entry : this.table.columnMap().entrySet()) {
      CoverageReport.Unit req_i = entry.getKey();
      List<TSRTestCase> coveringTCs = getTestCasesSatisfyingRequirement(req_i);

      if (coveringTCs.size() == 1) {
        TSRTestCase owner = coveringTCs.get(0);
        logger.debug("Owner found: {} owns {}", owner.getName(), req_i.name);

        selectTC(owner);

        performedOwnerReduction = true;
        if (MULTIPLE_REDUCTIONS_PER_STEP) {
          performOwnerReduction();
        } else {
          break;
        }
      }
    }

    return performedOwnerReduction;
  }


  /**
   * Step 4: The greedy heuristic
   * <p>
   * In case steps 1-3 could not be applied further to the concept table,
   * we have to resort to the greedy heuristic to select a test case that will
   * be in the set of retained test cases.
   * <p>
   * The applied heuristic is as follows: Select the TC(s) that cover the most
   * requirements. In case there are >1 TCs with the max. number of covered
   * requirements, select the one that covers a requirement that is least covered by
   * all other test cases.
   * <p>
   * Once this heuristic was applied, the final set of retained test cases might not
   * be optimal anymore. There is no way around this (due to the NP-completeness of
   * the Minimum Set Cover problem).
   */
  private void performGreedySelection() {
    logger.debug("Applying greedy heuristic on table with size {}", table.size());

    if (this.isEmptyContextTable(table)) {
      throw new IllegalStateException("You shouldn't be here! " +
                                      "The greedy heuristic must not be called w/ an empty table!");
    }

    Map<TestCase, Integer> tcMap =
      this.table.rowMap()
                .entrySet()
                .stream()
                .collect(Collectors.toMap(Map.Entry::getKey,
                                          e -> this.getRequirementsSatisfiedByTestCase(e.getKey()).size()));
    int mostCoveringTCs = tcMap.values().stream().mapToInt(x -> x).max().orElse(-1);

    if (mostCoveringTCs == -1) {
      logger.error("Some weird error during greedy heuristic application");
    }

    List<TestCase> selectionCandidates = tcMap.entrySet()
                                              .stream()
                                              .filter(e -> e.getValue() == mostCoveringTCs)
                                              .map(Map.Entry::getKey)
                                              .collect(Collectors.toList());

    logger.debug("Identified {} selection candidates", selectionCandidates.size());

    if (selectionCandidates.size() == 1) {
      this.selectTC(selectionCandidates.get(0));
      return;
    }

    AtomicReference<TestCase> bestCandidate = new AtomicReference<>(selectionCandidates.get(0));
    int leastCoverings = getRequirementsSatisfiedByTestCase((TSRTestCase) bestCandidate.get())
      .stream()
      .mapToInt(u -> getTestCasesSatisfyingRequirement(u).size())
      .min()
      .orElse(999999999);

    selectionCandidates.forEach(candidate -> {
      List<CoverageReport.Unit> coveredUnits = getRequirementsSatisfiedByTestCase((TSRTestCase) candidate);
      CoverageReport.Unit leastCoveredUnit =
        coveredUnits.stream()
                    .min(Comparator.comparing(u -> getTestCasesSatisfyingRequirement(u).size()))
                    .orElse(null);
      if (getTestCasesSatisfyingRequirement(leastCoveredUnit).size() < leastCoverings) {
        bestCandidate.set(candidate);
      }
    });

    this.selectTC(bestCandidate.get());
  }


  /* Helpers */

  private void selectTC(TestCase testCase) {
    logger.debug("Selecting {}", testCase.getName());
    this.addSelectedTCToRTS(testCase);
    List<CoverageReport.Unit> ownersCoveredReqs = this.getRequirementsSatisfiedByTestCase((TSRTestCase) testCase);
    final ArrayTable<?, ?, ?> arrayTable = (ArrayTable<?, ?, ?>) this.table;
    ownersCoveredReqs.forEach(r -> getTestCasesSatisfyingRequirement(r).forEach(t -> arrayTable.erase(t, r)));

    this.updateTable();
  }

  private void addSelectedTCToRTS(TestCase owner) {
    Set<CoverageReport.Unit> rtsCoveredReqs =
      this.retainedTCs.stream()
                      .map(tc -> coverageReport.testCaseCoverageData.get(tc))
                      .flatMap(Set::stream)
                      .collect(Collectors.toSet());
    Set<CoverageReport.Unit> allReqsCoveredByOwner = this.coverageReport.testCaseCoverageData.get(owner);
    if (!rtsCoveredReqs.containsAll(allReqsCoveredByOwner)) {
      logger.debug("Adding TC {} to set of retained TCs", owner.getName());
      this.retainedTCs.add(owner);
    }
  }

  private void updateTable() {
    List<TSRTestCase> remainingTCs = this.table.rowMap()
                                               .entrySet()
                                               .stream()
                                               .filter(e -> isNonEmptyRowOrColumn(e.getValue().values()))
                                               .map(Map.Entry::getKey)
                                               .collect(Collectors.toList());
    List<CoverageReport.Unit> remainingUnits = this.table.columnMap()
                                                         .entrySet()
                                                         .stream()
                                                         .filter(e -> isNonEmptyRowOrColumn(e.getValue().values()))
                                                         .map(Map.Entry::getKey)
                                                         .collect(Collectors.toList());

    Table<TSRTestCase, CoverageReport.Unit, Boolean> newTable = ArrayTable.create(remainingTCs, remainingUnits);

    remainingTCs.forEach(rtc -> {
      remainingUnits.forEach(ru -> {
        Boolean oldTableVal = this.table.get(rtc, ru);
        if (oldTableVal != null && oldTableVal) {
          newTable.put(rtc, ru, true);
        }
      });
    });

    this.table = newTable;
  }

  private boolean isNonEmptyRowOrColumn(Collection<Boolean> values) {
    return !values.stream().allMatch(Objects::isNull);
  }

  private boolean isEmptyContextTable(Table<TSRTestCase, CoverageReport.Unit, Boolean> table) {
    return table.values().stream().allMatch(Objects::isNull);
  }

  private boolean isNonEmptyContextTable(Table<TSRTestCase, CoverageReport.Unit, Boolean> table) {
    return !isEmptyContextTable(table);
  }
}
