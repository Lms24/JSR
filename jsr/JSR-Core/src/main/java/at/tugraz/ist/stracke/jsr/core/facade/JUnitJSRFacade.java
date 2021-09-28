package at.tugraz.ist.stracke.jsr.core.facade;

import at.tugraz.ist.stracke.jsr.core.coverage.CoverageReport;
import at.tugraz.ist.stracke.jsr.core.coverage.strategies.CoverageStrategy;
import at.tugraz.ist.stracke.jsr.core.parsing.TestSuiteParser;
import at.tugraz.ist.stracke.jsr.core.sfl.SFLMatrixExporter;
import at.tugraz.ist.stracke.jsr.core.shared.TestSuite;
import at.tugraz.ist.stracke.jsr.core.slicing.TestSuiteSlicer;
import at.tugraz.ist.stracke.jsr.core.tsr.ReducedTestSuite;
import at.tugraz.ist.stracke.jsr.core.tsr.reducer.TestSuiteReducer;
import at.tugraz.ist.stracke.jsr.core.tsr.serializer.Serializer;
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
    TestSuite originalTestSuite = parseTestSuite();

    // Step 2: Code instrumentation, TS execution and Slicing per test case, Coverage
    CoverageReport report = calculateCoverage(originalTestSuite);

    // Step 3: Perform TSR
    final ReducedTestSuite reducedTestSuite = reduceTestSuite(originalTestSuite, report);

    // optional step: SFL export
    if (this.config.exporter != null) {
      exportSFLMatrices(report);
    }

    // Optional step: RTS serialization
    if (this.config.serialize) {
      serializeReducedTestSuite(reducedTestSuite);
    }

    return reducedTestSuite;
  }

  private void serializeReducedTestSuite(ReducedTestSuite reducedTestSuite) {
    final Serializer serializer = this.config.serializer;

    if (this.config.serializationDirectory != null) {
      serializer.setOutputDirectory(this.config.serializationDirectory);
    }

    serializer.setReducedTestSuite(reducedTestSuite)
              .serialize(true);
  }

  private void exportSFLMatrices(CoverageReport report) {
    SFLMatrixExporter exporter = this.config.exporter;
    exporter.setCoverageReport(report);
    exporter.exportSFLMatrices();
  }

  private ReducedTestSuite reduceTestSuite(TestSuite originalTestSuite, CoverageReport report) {
    ReductionStrategy reductionStrategy = this.config.reductionStrategy;
    reductionStrategy.setOriginalTestSuite(originalTestSuite);
    reductionStrategy.setCoverageReport(report);

    TestSuiteReducer reducer = this.config.reducer;

    return reducer.reduce()
                  .generateReport(this.config.outputDir)
                  .getReducedTestSuite();
  }

  private CoverageReport calculateCoverage(TestSuite originalTestSuite) {
    TestSuiteSlicer slicer = this.config.slicer;
    slicer.setTestSuite(originalTestSuite);

    CoverageStrategy coverageStrategy = this.config.coverageStrategy;
    coverageStrategy.setOriginalTestSuite(originalTestSuite);

    return coverageStrategy.calculateOverallCoverage();
  }

  private TestSuite parseTestSuite() {
    TestSuiteParser parser = this.config.testSuiteParser;
    parser.parse();
    return parser.getResult();
  }
}
