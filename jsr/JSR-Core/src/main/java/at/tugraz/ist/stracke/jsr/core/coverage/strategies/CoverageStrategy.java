package at.tugraz.ist.stracke.jsr.core.coverage.strategies;

import at.tugraz.ist.stracke.jsr.core.coverage.CoverageReport;
import at.tugraz.ist.stracke.jsr.core.parsing.TestSuiteParser;
import at.tugraz.ist.stracke.jsr.core.shared.TestSuite;
import at.tugraz.ist.stracke.jsr.core.slicing.TestSuiteSlicer;

public interface CoverageStrategy {

  /**
   * Calculates the {@link CoverageReport} based on the implemented
   * coverage metric (e.g. checked coverage or line coverage).
   *
   * @return a {@link CoverageReport} of the
   */
  CoverageReport calculateOverallCoverage();

  void setOriginalTestSuite(TestSuite testSuite);
  void setSlicer(TestSuiteSlicer slicer);
  void setStatementParser(TestSuiteParser parser);
}
