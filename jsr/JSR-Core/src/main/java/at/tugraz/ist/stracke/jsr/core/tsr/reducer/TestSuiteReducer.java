package at.tugraz.ist.stracke.jsr.core.tsr.reducer;

import java.nio.file.Path;

/**
 * The wrapper Interface tasked with starting the test suite reduction
 * process, by employing a TSR {@link at.tugraz.ist.stracke.jsr.core.tsr.strategies.ReductionStrategy}.
 * and with serializing the resulting test suite to a new file (i.e. to actually remove the
 * test cases, if this is desired).
 */
public interface TestSuiteReducer {

  /**
   * Carries out the TSR process, employing the given
   * {@link at.tugraz.ist.stracke.jsr.core.tsr.strategies.ReductionStrategy}.
   *
   * @return a reference to self for optional method chaining
   */
  TestSuiteReducer reduce();

  /**
   * Generates a report as a non-invasive TSR result. This report
   * can be read by humans or programs alike.
   */
  void generateReport();

  /**
   * Serializes the reduced test suite to a file structure,
   * resembling the original test suite's file and code structure
   * as closely as possible. Obviously, the removed TCs are no longer
   * present in the serialization.
   *
   * @param srcTestDir is the root directory of the test directory
   *                   (e.g. myProject/src/test in a typical Java project)
   */
  void serialize(Path srcTestDir);
}
