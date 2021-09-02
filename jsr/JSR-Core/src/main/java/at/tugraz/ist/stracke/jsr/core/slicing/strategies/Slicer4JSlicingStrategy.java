package at.tugraz.ist.stracke.jsr.core.slicing.strategies;

import at.tugraz.ist.stracke.jsr.core.shared.TestCase;
import at.tugraz.ist.stracke.jsr.core.slicing.result.TestCaseSliceResult;
import org.checkerframework.checker.nullness.qual.NonNull;

public class Slicer4JSlicingStrategy implements SlicingStrategy {

  private TestCase testCase;

  public Slicer4JSlicingStrategy() {
  }

  @Override
  public TestCaseSliceResult execute() {
    return null;
  }

  @Override
  public SlicingStrategy setTestCase(@NonNull TestCase testCase) {
    this.testCase = testCase;
    return this;
  }
}
