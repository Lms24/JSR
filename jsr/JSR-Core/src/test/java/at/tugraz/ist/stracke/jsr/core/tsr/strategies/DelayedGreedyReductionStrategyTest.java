package at.tugraz.ist.stracke.jsr.core.tsr.strategies;

import at.tugraz.ist.stracke.jsr.core.shared.TestCase;
import at.tugraz.ist.stracke.jsr.core.tsr.ReducedTestSuite;
import at.tugraz.ist.stracke.jsr.test.TSRData;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

/**
 * Class DelayedGreedyReductionStrategyTest represents
 *
 * @author Lukas Stracke, 31.10.2021
 */
class DelayedGreedyReductionStrategyTest {

  @Test
  void testReduceToOptimalSolution01() {
    /*
     * This TC must produce an optimal solution as described in
     * Tallam and Gupta 2005
     */
    DelayedGreedyReductionStrategy delayedGreedy = new DelayedGreedyReductionStrategy(TSRData.smallOriginalTS,
                                                                                      TSRData.delGreedyCoverageReport);
    ReducedTestSuite rts = delayedGreedy.reduce();

    List<TestCase> union = new ArrayList<>(rts.testCases);
    union.addAll(rts.removedTestCases);

    assertThat(rts, is(notNullValue()));
    assertThat(union.size(), is(equalTo(TSRData.smallOriginalTS.testCases.size())));
    assertThat(rts.removedTestCases.toArray(), is(arrayWithSize(2)));
    assertThat(rts.testCases.toArray(), is(arrayWithSize(3)));
    assertThat(rts.removedTestCases.toArray(), is(arrayContainingInAnyOrder(TSRData.t1,
                                                                            TSRData.t5)));
    assertThat(rts.testCases.toArray(), is(arrayContainingInAnyOrder(TSRData.t2,
                                                                     TSRData.t3,
                                                                     TSRData.t4)));
    assertThat(delayedGreedy.optimalResult, is(true));
  }


  @Test
  void testReduceToOptimalSolution02() {
    DelayedGreedyReductionStrategy strategy = new DelayedGreedyReductionStrategy(TSRData.smallOriginalTS,
                                                                                 TSRData.smallCoverageReport);
    ReducedTestSuite rts = strategy.reduce();

    List<TestCase> union = new ArrayList<>(rts.testCases);
    union.addAll(rts.removedTestCases);

    assertThat(union.size(), is(equalTo(TSRData.smallOriginalTS.testCases.size())));
    assertThat(TSRData.smallOriginalTS.testCases.containsAll(union), is(true));
    assertThat(rts.testCases, contains(TSRData.t4, TSRData.t5));
    assertThat(strategy.optimalResult, is(true));
  }

  @Test
  void testReduceToOptimalSolution03() {
    DelayedGreedyReductionStrategy strategy = new DelayedGreedyReductionStrategy(TSRData.simpleOriginalTS,
                                                                                 TSRData.simpleCoverageReport);
    ReducedTestSuite rts = strategy.reduce();

    List<TestCase> union = new ArrayList<>(rts.testCases);
    union.addAll(rts.removedTestCases);

    assertThat(union.size(), is(equalTo(TSRData.simpleOriginalTS.testCases.size())));
    assertThat(TSRData.smallOriginalTS.testCases.containsAll(union), is(true));
    assertThat(rts.testCases, (contains(TSRData.t4)));
    assertThat(strategy.optimalResult, is(true));
  }

  @Test
  void testReduceToOptimalSolution04() {
    DelayedGreedyReductionStrategy strategy = new DelayedGreedyReductionStrategy(TSRData.simpleOriginalTS,
                                                                                 TSRData.simpleCoverageReport2);
    ReducedTestSuite rts = strategy.reduce();

    List<TestCase> union = new ArrayList<>(rts.testCases);
    union.addAll(rts.removedTestCases);

    assertThat(union.size(), is(equalTo(TSRData.simpleOriginalTS.testCases.size())));
    assertThat(TSRData.smallOriginalTS.testCases.containsAll(union), is(true));
    assertThat(rts.testCases, contains(TSRData.t4));
    assertThat(strategy.optimalResult, is(true));
  }

  @Test
  void testForceGreedySolution01() {
    DelayedGreedyReductionStrategy strategy = new DelayedGreedyReductionStrategy(TSRData.forceGreedyOriginalTS,
                                                                                 TSRData.forceGreedyCoverageReport);
    ReducedTestSuite rts = strategy.reduce();

    List<TestCase> union = new ArrayList<>(rts.testCases);
    union.addAll(rts.removedTestCases);

    assertThat(union.size(), is(equalTo(TSRData.forceGreedyOriginalTS.testCases.size())));
    assertThat(TSRData.forceGreedyOriginalTS.testCases.containsAll(union), is(true));
    assertThat(rts.removedTestCases, containsInAnyOrder(TSRData.t2,
                                                        TSRData.t3,
                                                        TSRData.t5,
                                                        TSRData.t6,
                                                        TSRData.t8));
    assertThat(rts.testCases, containsInAnyOrder(TSRData.t4,
                                                 TSRData.t9,
                                                 TSRData.t7));
    assertThat(strategy.optimalResult, is(false));
  }
}