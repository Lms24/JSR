package at.tugraz.ist.stracke.jsr.core.tsr.strategies;

import at.tugraz.ist.stracke.jsr.core.shared.TestCase;
import at.tugraz.ist.stracke.jsr.core.tsr.ReducedTestSuite;
import at.tugraz.ist.stracke.jsr.core.tsr.TSRTestCase;
import at.tugraz.ist.stracke.jsr.test.Mocks;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

class GreedyHGSReductionStrategyTest {

  @Test
  void testReduce() {
    ReductionStrategy strategy = new GreedyHGSReductionStrategy(Mocks.tsrOriginalTS,
                                                                Mocks.smallCoverageReport);
    ReducedTestSuite rts = strategy.reduce();

    List<TestCase> union = new ArrayList<>(rts.testCases);
    union.addAll(rts.removedTestCases);

    assertThat(union.size(), is(equalTo(Mocks.tsrOriginalTS.testCases.size())));
    assertThat(Mocks.tsrOriginalTS.testCases.containsAll(union), is(true));
    assertThat(rts.testCases, contains(new TSRTestCase("t4", "t4"),
                                       new TSRTestCase("t1", "t1"),
                                       new TSRTestCase("t5", "t5")));
  }
}