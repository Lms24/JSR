package at.tugraz.ist.stracke.jsr.core.coverage.strategies;

import at.tugraz.ist.stracke.jsr.core.coverage.CoverageReport;

public interface CoverageStrategy {

  /**
   * Calculates the {@link CoverageReport} based on the implemented
   * coverage metric (e.g. checked coverage or line coverage).
   *
   * @return a {@link CoverageReport} of the
   */
  CoverageReport calculateOverallCoverage();
}
