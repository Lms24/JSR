package at.tugraz.ist.stracke.jsr.core.facade;

import at.tugraz.ist.stracke.jsr.core.coverage.CoverageReport;
import at.tugraz.ist.stracke.jsr.core.tsr.ReducedTestSuite;

/**
 * JSRFacade follows the facade pattern to simplify access to the
 * core JSR library with a simple API. The idea is to provide one
 * interface with which library users can interact directly.
 */
public interface JSRFacade {

  /**
   * Performs the whole TSR process from start to finish. This includes:
   *
   * * Parsing the test suite
   * * Calculate coverage
   * * Performing TS reduction with the HGS greedy algorithm
   *
   * @return a {@link ReducedTestSuite} instance with the final outcome
   */
  ReducedTestSuite reduceTestSuite();

  /**
   * Calculates a reduced test suite from a passed coverage report
   *
   * Performing TS reduction with the HGS greedy algorithm
   *
   * @param report the {@link CoverageReport} on which the reduction shall be based
   *
   * @return a {@link ReducedTestSuite} instance with the final outcome
   */
  ReducedTestSuite reduceTestSuiteFromCoverageReport(CoverageReport report);
}
