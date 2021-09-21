package at.tugraz.ist.stracke.jsr.core.facade;

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
   * * Parsing the code for all statements
   * * Performing Dynamic Slicing (jar instrumentation, TS execution, slicing)
   * * Calculating Checked coverage
   * * Performing TS reduction with the HGS greedy algorithm
   *
   * @return a {@link ReducedTestSuite} instance with the final outcome
   */
  ReducedTestSuite reduceTestSuiteWithCheckedCoverage();
}
