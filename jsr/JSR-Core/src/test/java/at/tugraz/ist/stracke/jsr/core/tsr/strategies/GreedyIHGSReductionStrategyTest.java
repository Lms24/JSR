package at.tugraz.ist.stracke.jsr.core.tsr.strategies;

import at.tugraz.ist.stracke.jsr.core.shared.TestCase;
import at.tugraz.ist.stracke.jsr.core.tsr.ReducedTestSuite;
import at.tugraz.ist.stracke.jsr.core.tsr.TSRTestCase;
import at.tugraz.ist.stracke.jsr.test.TSRData;
import org.junit.jupiter.api.Test;

import javax.management.Query;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

class GreedyIHGSReductionStrategyTest {

  @Test
  void testReduce01() {
    ReductionStrategy strategy = new GreedyIHGSReductionStrategy(TSRData.smallOriginalTS,
                                                                 TSRData.smallCoverageReport);
    ReducedTestSuite rts = strategy.reduce();

    List<TestCase> union = new ArrayList<>(rts.testCases);
    union.addAll(rts.removedTestCases);

    assertThat(union.size(), is(equalTo(TSRData.smallOriginalTS.testCases.size())));
    assertThat(TSRData.smallOriginalTS.testCases.containsAll(union), is(true));
    assertThat(rts.testCases, contains(TSRData.t4, TSRData.t5));
  }

  @Test
  void testReduce02() {
    ReductionStrategy strategy = new GreedyIHGSReductionStrategy(TSRData.simpleOriginalTS,
                                                                 TSRData.simpleCoverageReport);
    ReducedTestSuite rts = strategy.reduce();

    List<TestCase> union = new ArrayList<>(rts.testCases);
    union.addAll(rts.removedTestCases);

    assertThat(union.size(), is(equalTo(TSRData.simpleOriginalTS.testCases.size())));
    assertThat(TSRData.smallOriginalTS.testCases.containsAll(union), is(true));
    assertThat(rts.testCases, (contains(TSRData.t1)));
  }

  @Test
  void testReduce03() {
    ReductionStrategy strategy = new GreedyIHGSReductionStrategy(TSRData.simpleOriginalTS,
                                                                 TSRData.simpleCoverageReport2);
    ReducedTestSuite rts = strategy.reduce();

    List<TestCase> union = new ArrayList<>(rts.testCases);
    union.addAll(rts.removedTestCases);

    assertThat(union.size(), is(equalTo(TSRData.simpleOriginalTS.testCases.size())));
    assertThat(TSRData.smallOriginalTS.testCases.containsAll(union), is(true));
    assertThat(rts.testCases, contains(TSRData.t4));
  }

  @Test
  void testReduce04() {
    GreedyIHGSReductionStrategy strategy = new GreedyIHGSReductionStrategy(TSRData.forceGreedyOriginalTS,
                                                                              TSRData.forceGreedyCoverageReport);
    ReducedTestSuite rts = strategy.reduce();

    List<TestCase> union = new ArrayList<>(rts.testCases);
    union.addAll(rts.removedTestCases);

    assertThat(union.size(), is(equalTo(TSRData.forceGreedyOriginalTS.testCases.size())));
    assertThat(TSRData.forceGreedyOriginalTS.testCases.containsAll(union), is(true));
    assertThat(rts.removedTestCases, containsInAnyOrder(TSRData.t5, TSRData.t9));
    assertThat(rts.testCases, containsInAnyOrder(TSRData.t2,
                                                 TSRData.t3,
                                                 TSRData.t4,
                                                 TSRData.t6,
                                                 TSRData.t7,
                                                 TSRData.t8));
  }
}