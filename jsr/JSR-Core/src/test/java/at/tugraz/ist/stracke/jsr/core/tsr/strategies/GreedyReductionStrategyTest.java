package at.tugraz.ist.stracke.jsr.core.tsr.strategies;

import at.tugraz.ist.stracke.jsr.core.shared.TestCase;
import at.tugraz.ist.stracke.jsr.core.tsr.ReducedTestSuite;
import at.tugraz.ist.stracke.jsr.test.TSRData;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

class GreedyReductionStrategyTest {

  @Test
  void testReduce01() {
    ReductionStrategy strategy = new GreedyReductionStrategy(TSRData.smallOriginalTS,
                                                             TSRData.smallCoverageReport);
    ReducedTestSuite rts = strategy.reduce();

    assertThat(rts, is(notNullValue()));

    List<TestCase> union = new ArrayList<>(rts.testCases);
    union.addAll(rts.removedTestCases);

    assertThat(rts.testCases.size(), is(lessThanOrEqualTo(3)));
    assertThat(union.size(), is(equalTo(TSRData.smallOriginalTS.testCases.size())));
    assertThat(TSRData.smallOriginalTS.testCases.containsAll(union), is(true));
  }

  @Test
  void testReduce02() {
    ReductionStrategy strategy = new GreedyReductionStrategy(TSRData.simpleOriginalTS,
                                                             TSRData.simpleCoverageReport);
    ReducedTestSuite rts = strategy.reduce();

    List<TestCase> union = new ArrayList<>(rts.testCases);
    union.addAll(rts.removedTestCases);

    assertThat(union.size(), is(equalTo(TSRData.simpleOriginalTS.testCases.size())));
    assertThat(TSRData.smallOriginalTS.testCases.containsAll(union), is(true));
    assertThat(rts.testCases, either(contains(TSRData.t4)).or(contains(TSRData.t1)));
  }

  @Test
  void testReduce03() {
    ReductionStrategy strategy = new GreedyReductionStrategy(TSRData.simpleOriginalTS,
                                                             TSRData.simpleCoverageReport2);
    ReducedTestSuite rts = strategy.reduce();

    List<TestCase> union = new ArrayList<>(rts.testCases);
    union.addAll(rts.removedTestCases);

    assertThat(union.size(), is(equalTo(TSRData.simpleOriginalTS.testCases.size())));
    assertThat(TSRData.smallOriginalTS.testCases.containsAll(union), is(true));
    assertThat(rts.testCases, contains(TSRData.t4));
  }
}