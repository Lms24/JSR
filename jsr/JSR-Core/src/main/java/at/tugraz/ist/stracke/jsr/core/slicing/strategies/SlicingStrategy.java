package at.tugraz.ist.stracke.jsr.core.slicing.strategies;

import at.tugraz.ist.stracke.jsr.core.shared.TestCase;
import at.tugraz.ist.stracke.jsr.core.shared.TestSuite;
import at.tugraz.ist.stracke.jsr.core.slicing.result.TestCaseSliceResult;
import at.tugraz.ist.stracke.jsr.core.slicing.result.TestSuiteSliceResult;

import java.nio.file.Path;

public interface SlicingStrategy {

  /**
   * Executes the slicing strategy which has to
   * generate a {@link TestSuiteSliceResult} of a {@link TestSuite}.
   *
   * @return The dynamic slice of the testcase as a {@link TestCaseSliceResult}.
   */
  TestCaseSliceResult execute();

  /**
   * Set the {@link at.tugraz.ist.stracke.jsr.core.shared.TestCase} upon which
   * the slicing strategy should be executed
   *
   * @return A reference to the slicing strategy instance for method chaining
   */
  SlicingStrategy setTestCase(TestCase testCase);

  /**
   * Instrument the jar file containing the compiled test and source classes
   *
   * @return A reference to the slicing strategy instance for method chaining
   */
  SlicingStrategy instrumentJar();

  /**
   * Returns the output directory of the slicing strategy's output files.
   * @return the output directory of the slicing strategy's output files.
   */
  Path getOutputDirectory();
}
