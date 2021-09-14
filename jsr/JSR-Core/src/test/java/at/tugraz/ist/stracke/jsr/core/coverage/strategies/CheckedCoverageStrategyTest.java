package at.tugraz.ist.stracke.jsr.core.coverage.strategies;

import at.tugraz.ist.stracke.jsr.core.coverage.CoverageReport;
import at.tugraz.ist.stracke.jsr.test.Mocks;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

class CheckedCoverageStrategyTest {

  @Test
  void testCalculateOverallCoverage() {
    CheckedCoverageStrategy strat = new CheckedCoverageStrategy(new Mocks.MockedTestSuiteParser(),
                                                                new Mocks.MockedTestSuiteSlicer());
    CoverageReport cr = strat.calculateOverallCoverage();

    assertThat(cr, is(notNullValue()));
    assertThat(cr.getCoverageScore(), is(equalTo(0.8)));
    assertThat(cr.allUnits.toArray(), is(arrayWithSize(5)));
    assertThat(cr.coveredUnits.toArray(), is(arrayWithSize(4)));
    assertThat(cr.testCaseCoverageData.size(), is(equalTo(2)));
  }
}