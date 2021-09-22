package at.tugraz.ist.stracke.jsr.core.slicing;

import at.tugraz.ist.stracke.jsr.core.shared.TestSuite;
import at.tugraz.ist.stracke.jsr.core.slicing.result.TestSuiteSliceResult;
import at.tugraz.ist.stracke.jsr.core.slicing.strategies.SlicingStrategy;

public interface TestSuiteSlicer {

  /**
   * Starts the slicing procedure of the supplied {@link TestSuite}.
   *
   * The dynamic slices of all test cases packed as a {@link TestSuiteSliceResult}
   */
  TestSuiteSliceResult slice();

  /**
   * Returns a {@link TestSuiteSliceResult} after {@link #slice()}
   * was executed.
   *
   * @return a {@link TestSuiteSliceResult}
   */
  TestSuiteSliceResult getResult();

  void setSlicingStrategy(SlicingStrategy strategy);

  void setTestSuite(TestSuite testSuite);
}
