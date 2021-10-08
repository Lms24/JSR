package at.tugraz.ist.stracke.jsr.core.slicing;

import at.tugraz.ist.stracke.jsr.core.shared.TestSuite;
import at.tugraz.ist.stracke.jsr.core.slicing.result.TestCaseSliceResult;
import at.tugraz.ist.stracke.jsr.core.slicing.result.TestSuiteSliceResult;
import at.tugraz.ist.stracke.jsr.core.slicing.strategies.SlicingStrategy;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.List;
import java.util.stream.Collectors;

public class JUnitTestSuiteSlicer implements TestSuiteSlicer {

  private SlicingStrategy slicingStrategy;
  private TestSuite testSuite;

  private TestSuiteSliceResult result;

  public JUnitTestSuiteSlicer(@NonNull SlicingStrategy slicingStrategy,
                              @NonNull TestSuite testSuite) {
    this.slicingStrategy = slicingStrategy;
    this.testSuite = testSuite;
  }

  public JUnitTestSuiteSlicer(@NonNull SlicingStrategy slicingStrategy) {
    this.slicingStrategy = slicingStrategy;
  }

  @Override
  public TestSuiteSliceResult slice() {
    this.slicingStrategy.instrumentJar();

    List<TestCaseSliceResult> tcSlices =
      this.testSuite.getTestCases().stream()
                    .map(tc -> this.slicingStrategy.setTestCase(tc).execute())
                    .collect(Collectors.toList());

    this.result = new TestSuiteSliceResult(tcSlices);

    return this.result;
  }

  @Override
  public TestSuiteSliceResult getResult() {
    return this.result;
  }

  @Override
  public void setSlicingStrategy(SlicingStrategy strategy) {
    this.slicingStrategy = strategy;
  }

  @Override
  public void setTestSuite(TestSuite testSuite) {
    this.testSuite = testSuite;
  }
}
