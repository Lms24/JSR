package at.tugraz.ist.stracke.jsr.core.tsr.strategies;

import at.tugraz.ist.stracke.jsr.core.tsr.ReducedTestSuite;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * This interface must be implemented by all
 * test suite reduction strategies. Such strategies can
 * e.g. differ in optimization algorithm, employed library, etc.
 */
public interface ReductionStrategy {

  /**
   * Executes the implemented test suite reduction strategy
   * and returns the reduced test suite
   *
   * @return a {@link ReducedTestSuite} instance containing the retained
   * test cases in {@link ReducedTestSuite#testCases} and the removed
   * test cases in {@link ReducedTestSuite#removedTestCases}.
   */
  @NonNull ReducedTestSuite reduce();
}
