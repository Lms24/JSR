package at.tugraz.ist.stracke.jsr.core.facade;

import at.tugraz.ist.stracke.jsr.core.coverage.strategies.CheckedCoverageStrategy;
import at.tugraz.ist.stracke.jsr.core.coverage.strategies.CoverageStrategy;
import at.tugraz.ist.stracke.jsr.core.parsing.JUnitTestSuiteParser;
import at.tugraz.ist.stracke.jsr.core.parsing.TestSuiteParser;
import at.tugraz.ist.stracke.jsr.core.parsing.strategies.JavaParserParsingStrategy;
import at.tugraz.ist.stracke.jsr.core.parsing.strategies.ParsingStrategy;
import at.tugraz.ist.stracke.jsr.core.sfl.SFLMatrixCsvExporter;
import at.tugraz.ist.stracke.jsr.core.sfl.SFLMatrixExporter;
import at.tugraz.ist.stracke.jsr.core.slicing.JUnitTestSuiteSlicer;
import at.tugraz.ist.stracke.jsr.core.slicing.TestSuiteSlicer;
import at.tugraz.ist.stracke.jsr.core.slicing.strategies.Slicer4JSlicingStrategy;
import at.tugraz.ist.stracke.jsr.core.slicing.strategies.SlicingStrategy;
import at.tugraz.ist.stracke.jsr.core.tsr.reducer.JUnitTestSuiteReducer;
import at.tugraz.ist.stracke.jsr.core.tsr.reducer.TestSuiteReducer;
import at.tugraz.ist.stracke.jsr.core.tsr.strategies.GreedyIHGSReductionStrategy;
import at.tugraz.ist.stracke.jsr.core.tsr.strategies.ReductionStrategy;

import java.nio.file.Path;

public class JUnitJSRFacadeBuilder {

  private final JSRConfig config;

  /**
   * Constructor containing all necessary extrinsic information the core library requires
   * to perform test suite reduction
   *
   * @param sourceDir Path to the directory containing all main source files.
   *                  Usually, in a Java project this directory is similar to "projectRoot/src/main/java".
   * @param testDir   Path to the directory containing all test source files.
   *                  Usually, in a Java project, this directory is similar to "projectRoot/src/test/java.
   * @param jarFile   Path to the fat jar file containing test, as well as source classes.
   * @param outputDir Path to the directory to which all output files (slicing logs, TSR report, etc)
   *                  are written.
   * @param slicerDir Path to the directory of the slicer as described in {@link Slicer4JSlicingStrategy}.
   */
  public JUnitJSRFacadeBuilder(Path sourceDir,
                               Path testDir,
                               Path jarFile,
                               Path outputDir,
                               Path slicerDir) {
    this.config = new JSRConfig(sourceDir, testDir, jarFile, outputDir, slicerDir);
    this.setDefaultOptions();
  }

  private void setDefaultOptions() {
    this.config.testSuiteParsingStrategy = new JavaParserParsingStrategy(this.config.testDir);
    this.config.testSuiteParser = new JUnitTestSuiteParser(this.config.testSuiteParsingStrategy);


    this.config.statementParsingStrategy = new JavaParserParsingStrategy(this.config.sourceDir);
    this.config.statementParser = new JUnitTestSuiteParser(this.config.statementParsingStrategy);

    this.config.slicingStrategy = new Slicer4JSlicingStrategy(this.config.jarFile.toString(),
                                                              this.config.slicerDir.toString(),
                                                              this.config.outputDir.toString());
    this.config.slicer = new JUnitTestSuiteSlicer(this.config.slicingStrategy);

    this.config.coverageStrategy = new CheckedCoverageStrategy(this.config.statementParser,
                                                               this.config.slicer);

    this.config.reductionStrategy = new GreedyIHGSReductionStrategy();
    this.config.reducer = new JUnitTestSuiteReducer(this.config.reductionStrategy);

    this.config.exporter = new SFLMatrixCsvExporter(this.config.outputDir);
  }

  public JUnitJSRFacadeBuilder skipSFLMatrix() {
    this.config.exporter = null;
    return this;
  }

  public JUnitJSRFacadeBuilder testSuiteParser(TestSuiteParser parser) {
    this.config.testSuiteParser = parser;
    return this;
  }

  public JUnitJSRFacadeBuilder statementParser(TestSuiteParser parser) {
    if (config.coverageStrategy == null) {
      throw new IllegalStateException("Cannot set a Stmt Parser without a coverage strategy");
    }

    this.config.statementParser = parser;
    this.config.coverageStrategy.setStatementParser(parser);

    return this;
  }

  public JUnitJSRFacadeBuilder testSuiteParsingStrategy(ParsingStrategy parsingStrategy) {
    if (config.testSuiteParser == null) {
      throw new IllegalStateException("Cannot set a TS ParsingStrategy without a parser!");
    }

    this.config.testSuiteParsingStrategy = parsingStrategy;
    this.config.testSuiteParser.setParsingStrategy(parsingStrategy);

    return this;
  }

  public JUnitJSRFacadeBuilder statementParsingStrategy(ParsingStrategy parsingStrategy) {
    if (config.statementParser == null) {
      throw new IllegalStateException("Cannot set a Stmt ParsingStrategy without a parser!");
    }

    this.config.statementParsingStrategy = parsingStrategy;
    this.config.statementParser.setParsingStrategy(parsingStrategy);

    return this;
  }

  public JUnitJSRFacadeBuilder coverageStrategy(CoverageStrategy coverageStrategy) {
    this.config.coverageStrategy = coverageStrategy;
    return this;
  }

  public JUnitJSRFacadeBuilder slicer(TestSuiteSlicer slicer) {
    if (config.coverageStrategy == null) {
      throw new IllegalStateException("Cannot set a slicer without a coverage strategy");
    }
    this.config.slicer = slicer;
    this.config.coverageStrategy.setSlicer(slicer);
    return this;
  }

  public JUnitJSRFacadeBuilder slicingStrategy(SlicingStrategy slicingStrategy) {
    if (config.slicer == null) {
      throw new IllegalStateException("Cannot set a SlicingStrategy without a slicer!");
    }

    this.config.slicingStrategy = slicingStrategy;
    this.config.slicer.setSlicingStrategy(slicingStrategy);
    return this;
  }

  public JUnitJSRFacadeBuilder reducer(TestSuiteReducer reducer) {
    this.config.reducer = reducer;
    return this;
  }

  public JUnitJSRFacadeBuilder reductionStrategy(ReductionStrategy reductionStrategy) {
    if (config.reducer == null) {
      throw new IllegalStateException("Cannot set a SlicingStrategy without a reducer!");
    }

    this.config.reductionStrategy = reductionStrategy;
    this.config.reducer.setReductionStrategy(reductionStrategy);

    return this;
  }

  public JUnitJSRFacadeBuilder sflExporter(SFLMatrixExporter exporter) {
    this.config.exporter = exporter;
    return this;
  }

  public JUnitJSRFacade build() {
    return new JUnitJSRFacade(this.config);
  }
}
