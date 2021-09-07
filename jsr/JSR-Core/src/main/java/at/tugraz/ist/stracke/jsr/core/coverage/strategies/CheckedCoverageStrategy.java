package at.tugraz.ist.stracke.jsr.core.coverage.strategies;

import at.tugraz.ist.stracke.jsr.core.coverage.CoverageReport;
import at.tugraz.ist.stracke.jsr.core.shared.TestSuite;
import at.tugraz.ist.stracke.jsr.core.slicing.TestSuiteSlicer;
import at.tugraz.ist.stracke.jsr.core.slicing.strategies.SlicingStrategy;

public class CheckedCoverageStrategy implements CoverageStrategy {

  private TestSuite testSuite;
  private TestSuiteSlicer slicer;
  private SlicingStrategy slicingStrategy;

  public CheckedCoverageStrategy(TestSuite testSuite,
                                 TestSuiteSlicer slicer,
                                 SlicingStrategy slicingStrategy) {
    this.testSuite = testSuite;
    this.slicer = slicer;
    this.slicingStrategy = slicingStrategy;
  }

  @Override
  public CoverageReport calculate() {
    this.slicer.slice();
    //TODO
    return new CoverageReport(0.8F);
  }

  public CheckedCoverageStrategy setSlicer(TestSuiteSlicer slicer) {
    this.slicer = slicer;
    return this;
  }

  public CheckedCoverageStrategy setSlicingStrategy(SlicingStrategy strategy) {
    this.slicingStrategy = strategy;
    return this;
  }
}
