package at.tugraz.ist.stracke.jsr.core.facade;

import at.tugraz.ist.stracke.jsr.core.coverage.CoverageReport;
import at.tugraz.ist.stracke.jsr.core.coverage.strategies.CheckedCoverageStrategy;
import at.tugraz.ist.stracke.jsr.core.coverage.strategies.CoverageStrategy;
import at.tugraz.ist.stracke.jsr.core.parsing.JUnitTestSuiteParser;
import at.tugraz.ist.stracke.jsr.core.parsing.TestSuiteParser;
import at.tugraz.ist.stracke.jsr.core.parsing.strategies.JavaParserParsingStrategy;
import at.tugraz.ist.stracke.jsr.core.parsing.strategies.ParsingStrategy;
import at.tugraz.ist.stracke.jsr.core.sfl.SFLMatrixCsvExporter;
import at.tugraz.ist.stracke.jsr.core.sfl.SFLMatrixExporter;
import at.tugraz.ist.stracke.jsr.core.shared.TestSuite;
import at.tugraz.ist.stracke.jsr.core.slicing.JUnitTestSuiteSlicer;
import at.tugraz.ist.stracke.jsr.core.slicing.TestSuiteSlicer;
import at.tugraz.ist.stracke.jsr.core.slicing.strategies.Slicer4JSlicingStrategy;
import at.tugraz.ist.stracke.jsr.core.slicing.strategies.SlicingStrategy;
import at.tugraz.ist.stracke.jsr.core.tsr.ReducedTestSuite;
import at.tugraz.ist.stracke.jsr.core.tsr.reducer.JUnitTestSuiteReducer;
import at.tugraz.ist.stracke.jsr.core.tsr.reducer.TestSuiteReducer;
import at.tugraz.ist.stracke.jsr.core.tsr.strategies.GreedyIHGSReductionStrategy;
import at.tugraz.ist.stracke.jsr.core.tsr.strategies.ReductionStrategy;
import org.checkerframework.checker.nullness.qual.NonNull;

public class JUnitJSRFacade implements JSRFacade {

  private final JSRConfig config;

  /**
   * Package private ctor s.t. the facade can only be initialized via
   * the {@link JUnitJSRFacadeBuilder}.
   *
   * @param config the config object created by the builder.
   */
  JUnitJSRFacade(@NonNull JSRConfig config) {
    this.config = config;
  }

  @Override
  public ReducedTestSuite reduceTestSuite() {

    // Step 1: Parse the test suite
    TestSuiteParser parser = this.config.testSuiteParser;
    parser.parse();
    TestSuite originalTestSuite = parser.getResult();

    // Step 2: Code instrumentation, TS execution and Slicing per test case, Coverage
    TestSuiteSlicer slicer = this.config.slicer;
    slicer.setTestSuite(originalTestSuite);

    CoverageStrategy coverageStrategy = this.config.coverageStrategy;
    coverageStrategy.setOriginalTestSuite(originalTestSuite);

    CoverageReport report = coverageStrategy.calculateOverallCoverage();

    // Step 3: Perform TSR
    ReductionStrategy reductionStrategy = this.config.reductionStrategy;
    reductionStrategy.setOriginalTestSuite(originalTestSuite);
    reductionStrategy.setCoverageReport(report);

    TestSuiteReducer reducer = this.config.reducer;

    // optional step: SFL export
    if (this.config.exporter != null) {
      SFLMatrixExporter exporter = this.config.exporter;
      exporter.setCoverageReport(report);
      exporter.exportSFLMatrices();
    }

    return reducer.reduce().generateReport(this.config.outputDir).getReducedTestSuite();
  }
}
