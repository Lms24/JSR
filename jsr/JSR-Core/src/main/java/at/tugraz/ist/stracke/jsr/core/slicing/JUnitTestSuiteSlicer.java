package at.tugraz.ist.stracke.jsr.core.slicing;

import at.tugraz.ist.stracke.jsr.core.shared.TestSuite;
import at.tugraz.ist.stracke.jsr.core.slicing.result.TestCaseSliceResult;
import at.tugraz.ist.stracke.jsr.core.slicing.result.TestSuiteSliceResult;
import at.tugraz.ist.stracke.jsr.core.slicing.strategies.SlicingStrategy;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class JUnitTestSuiteSlicer implements TestSuiteSlicer {

  private Logger logger = LogManager.getLogger(JUnitTestSuiteSlicer.class);

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
    AtomicInteger tcCounter = new AtomicInteger();
    List<TestCaseSliceResult> tcSlices =
      this.testSuite.getTestCases().stream()
                    .map(tc -> {
                      logger.info("Executing and Slicing test case {}/{}",
                                  tcCounter.incrementAndGet(),
                                  testSuite.getTestCases().size());
                      this.slicingStrategy.setTestCase(tc);
                      return this.slicingStrategy.execute();
                    })
                    .filter(Objects::nonNull)
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

  @Override
  public SlicingStrategy getSlicingStrategy() {
    return this.slicingStrategy;
  }
}
