package at.tugraz.ist.stracke.jsr.core.tsr.reducer;

import at.tugraz.ist.stracke.jsr.core.tsr.ReducedTestSuite;
import at.tugraz.ist.stracke.jsr.core.tsr.strategies.ReductionStrategy;

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
   *
   * @param reportDir the directory where the report shall be saved
   *
   * @return a reference to self for optional method chaining
   */
  TestSuiteReducer generateReport(Path reportDir);

  /**
   * Generates a report as a non-invasive TSR result. This report
   * can be read by humans or programs alike.
   *
   * @param reportDir the directory where the report shall be saved
   * @param reportName the name of the report file
   *
   * @return a reference to self for optional method chaining
   */
  TestSuiteReducer generateReport(Path reportDir, String reportName);

  /**
   * Serializes the reduced test suite to a file structure,
   * resembling the original test suite's file and code structure
   * as closely as possible. Obviously, the removed TCs are no longer
   * present in the serialization.
   *
   * @param srcTestDir is the root directory of the test directory
   *                   (e.g. myProject/src/test in a typical Java project)
   *
   * @return a reference to self for optional method chaining
   */
  TestSuiteReducer serialize(Path srcTestDir);

  ReducedTestSuite getReducedTestSuite();

  void setReductionStrategy(ReductionStrategy strategy);
}
