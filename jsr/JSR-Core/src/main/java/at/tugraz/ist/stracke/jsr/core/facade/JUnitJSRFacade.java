package at.tugraz.ist.stracke.jsr.core.facade;

import at.tugraz.ist.stracke.jsr.core.coverage.CoverageReport;
import at.tugraz.ist.stracke.jsr.core.coverage.export.CoverageReportExporter;
import at.tugraz.ist.stracke.jsr.core.coverage.strategies.CoverageStrategy;
import at.tugraz.ist.stracke.jsr.core.parsing.TestSuiteParser;
import at.tugraz.ist.stracke.jsr.core.shared.TestSuite;
import at.tugraz.ist.stracke.jsr.core.slicing.TestSuiteSlicer;
import at.tugraz.ist.stracke.jsr.core.tsr.ReducedTestSuite;
import at.tugraz.ist.stracke.jsr.core.tsr.reducer.TestSuiteReducer;
import at.tugraz.ist.stracke.jsr.core.tsr.serializer.Serializer;
import at.tugraz.ist.stracke.jsr.core.tsr.strategies.ReductionStrategy;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.nio.file.Path;

public class JUnitJSRFacade implements JSRFacade {

  private final Logger logger = LogManager.getLogger(JUnitJSRFacade.class);
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
    long startTimeParsing = System.currentTimeMillis();
    TestSuite originalTestSuite = parseTestSuite();

    // Step 2: Code instrumentation, TS execution and Slicing per test case, Coverage
    long startTimeCoverage = System.currentTimeMillis();
    CoverageReport report = calculateCoverage(originalTestSuite);
    exportCoverageReport(report);

    // Step 3: Perform TSR
    long startTimeTSR = System.currentTimeMillis();
    final ReducedTestSuite reducedTestSuite = reduceTestSuite(originalTestSuite, report);

    // Optional step: RTS serialization
    long startTimeSerialization = -1;
    if (this.config.serialize) {
      startTimeSerialization = System.currentTimeMillis();
      serializeReducedTestSuite(reducedTestSuite);
    }

    long endTime = System.currentTimeMillis();
    this.logTime(startTimeParsing, startTimeCoverage, startTimeTSR, startTimeSerialization, endTime);

    return reducedTestSuite;
  }

  @Override
  public ReducedTestSuite reduceTestSuiteFromCoverageReport(CoverageReport report) {
    // Step 1: Parse the test suite
    long startTimeParsing = System.currentTimeMillis();
    TestSuite originalTestSuite = parseTestSuite();

    // Step 2: Perform TSR
    long startTimeTSR = System.currentTimeMillis();
    final ReducedTestSuite reducedTestSuite = reduceTestSuite(originalTestSuite, report);

    // Optional step: RTS serialization
    long startTimeSerialization = -1;
    if (this.config.serialize) {
      startTimeSerialization = System.currentTimeMillis();
      serializeReducedTestSuite(reducedTestSuite);
    }

    long endTime = System.currentTimeMillis();
    this.logTime(startTimeParsing, -1, startTimeTSR, startTimeSerialization, endTime);

    return reducedTestSuite;
  }

  private void exportCoverageReport(CoverageReport report) {
    CoverageReportExporter exporter = new CoverageReportExporter(report);
    final Path coverageOutputPath = Path.of(this.config.outputDir.toString(), "coverage");
    exporter.exportToFile(coverageOutputPath);
  }

  private void serializeReducedTestSuite(ReducedTestSuite reducedTestSuite) {
    final Serializer serializer = this.config.serializer;

    if (this.config.serializationDirectory != null) {
      serializer.setOutputDirectory(this.config.serializationDirectory);
    }

    serializer.setReducedTestSuite(reducedTestSuite)
              .serialize(true);
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

  private void logTime(long startTimeParsing, long startTimeCoverage, long startTimeTSR, long startTimeSerialization, long endTime) {
    boolean coverage = startTimeCoverage > 0;
    boolean serial = startTimeSerialization > 0;

    final float parsingTime = ((coverage ? startTimeCoverage : startTimeTSR) - startTimeParsing) / 1000f;
    final float tsrTime = ((serial ? startTimeSerialization : endTime) - startTimeTSR) / 1000f;
    final float overallTime = (endTime - startTimeParsing) / 1000f;

    logger.info("******************************************************************");
    logger.info("* Facade Statistics:");
    logger.info("* Parsing took: {} seconds", parsingTime);
    if (coverage) {
      final float coverageTime = (startTimeTSR - startTimeCoverage) / 1000f;
      logger.info("* Coverage took: {} seconds", coverageTime);
    }
    logger.info("* Reduction took: {} seconds", tsrTime);
    if (serial) {
      final float serialTime = (endTime - startTimeSerialization) / 1000f;
      logger.info("* Serialization took: {} seconds", serialTime);
    }
    logger.info("* Overall: {} seconds", overallTime);
    logger.info("******************************************************************");
  }
}
