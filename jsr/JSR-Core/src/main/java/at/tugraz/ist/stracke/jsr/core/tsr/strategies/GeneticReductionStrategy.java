package at.tugraz.ist.stracke.jsr.core.tsr.strategies;

import at.tugraz.ist.stracke.jsr.core.coverage.CoverageReport;
import at.tugraz.ist.stracke.jsr.core.shared.TestSuite;
import at.tugraz.ist.stracke.jsr.core.tsr.ReducedTestSuite;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.Collections;

public class GeneticReductionStrategy implements ReductionStrategy {
  @Override
  public @NonNull ReducedTestSuite reduce() {
    return new ReducedTestSuite(Collections.emptyList(), Collections.emptyList());
  }

  @Override
  public void setCoverageReport(CoverageReport report) {

  }

  @Override
  public void setOriginalTestSuite(TestSuite testSuite) {

  }
}
