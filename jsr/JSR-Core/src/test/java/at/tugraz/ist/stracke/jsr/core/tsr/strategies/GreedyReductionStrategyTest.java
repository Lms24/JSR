package at.tugraz.ist.stracke.jsr.core.tsr.strategies;

import at.tugraz.ist.stracke.jsr.core.shared.TestCase;
import at.tugraz.ist.stracke.jsr.core.tsr.ReducedTestSuite;
import at.tugraz.ist.stracke.jsr.test.Mocks;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

class GreedyReductionStrategyTest {

  @Test
  void testReduce() {
    ReductionStrategy strategy = new GreedyReductionStrategy(Mocks.tsrOriginalTS,
                                                          Mocks.smallCoverageReport);
    ReducedTestSuite rts = strategy.reduce();

    assertThat(rts, is(notNullValue()));

    List<TestCase> union = new ArrayList<>(rts.testCases);
    union.addAll(rts.removedTestCases);

    assertThat(rts.testCases.size(), is(lessThanOrEqualTo(Mocks.tsrOriginalTS.testCases.size())));
    assertThat(union.size(), is(equalTo(Mocks.tsrOriginalTS.testCases.size())));
    assertThat(Mocks.tsrOriginalTS.testCases.containsAll(union), is(true));
  }
}