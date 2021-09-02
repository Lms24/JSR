package at.tugraz.ist.stracke.jsr.core.slicing;

import at.tugraz.ist.stracke.jsr.core.shared.TestSuite;
import at.tugraz.ist.stracke.jsr.core.slicing.result.TestCaseSliceResult;
import at.tugraz.ist.stracke.jsr.core.slicing.result.TestSuiteSliceResult;
import at.tugraz.ist.stracke.jsr.core.slicing.strategies.SlicingStrategy;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.List;
import java.util.stream.Collectors;

public class JUnitTestSuiteSlicer implements TestSuiteSlicer {

  private final SlicingStrategy slicingStrategy;
  private final TestSuite testSuite;

  private TestSuiteSliceResult result;

  public JUnitTestSuiteSlicer(@NonNull SlicingStrategy slicingStrategy,
                              @NonNull TestSuite testSuite) {
    this.slicingStrategy = slicingStrategy;
    this.testSuite = testSuite;
  }

  @Override
  public TestSuiteSliceResult slice() {
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
}
