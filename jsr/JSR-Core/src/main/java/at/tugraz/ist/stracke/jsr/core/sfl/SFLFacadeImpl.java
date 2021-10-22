package at.tugraz.ist.stracke.jsr.core.sfl;

import at.tugraz.ist.stracke.jsr.core.coverage.CoverageReport;
import at.tugraz.ist.stracke.jsr.core.coverage.strategies.CheckedCoverageStrategy;
import at.tugraz.ist.stracke.jsr.core.coverage.strategies.CoverageStrategy;
import at.tugraz.ist.stracke.jsr.core.coverage.strategies.LineCoverageStrategy;
import at.tugraz.ist.stracke.jsr.core.parsing.JUnitTestSuiteParser;
import at.tugraz.ist.stracke.jsr.core.parsing.TestSuiteParser;
import at.tugraz.ist.stracke.jsr.core.parsing.strategies.JavaParserParsingStrategy;
import at.tugraz.ist.stracke.jsr.core.parsing.strategies.ParsingStrategy;
import at.tugraz.ist.stracke.jsr.core.sfl.exporter.SFLMatrixCsvExporter;
import at.tugraz.ist.stracke.jsr.core.sfl.exporter.SFLMatrixExporter;
import at.tugraz.ist.stracke.jsr.core.shared.JSRParams;
import at.tugraz.ist.stracke.jsr.core.shared.TestCase;
import at.tugraz.ist.stracke.jsr.core.shared.TestSuite;
import at.tugraz.ist.stracke.jsr.core.slicing.JUnitTestSuiteSlicer;
import at.tugraz.ist.stracke.jsr.core.slicing.TestSuiteSlicer;
import at.tugraz.ist.stracke.jsr.core.slicing.strategies.Slicer4JSlicingStrategy;
import at.tugraz.ist.stracke.jsr.core.slicing.strategies.SlicingStrategy;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class SFLFacadeImpl implements SFLFacade {

  private final Logger logger = LogManager.getLogger(SFLFacadeImpl.class);

  private ParsingStrategy parsingStrategy;
  private TestSuiteParser parser;

  private SlicingStrategy slicingStrategy;
  private TestSuiteSlicer slicer;

  private CoverageStrategy passCoverageStrategy;
  private CoverageStrategy failCoverageStrategy;

  private SFLMatrixExporter exporter;

  @Override
  public boolean createAndExportSFLMatrices(JSRParams params) {

    if (!checkParams(params)) {
      return false;
    }

    configureParsing(params);

    parser.parse();
    TestSuite testSuite = parser.getResult();

    if (testSuite == null) {
      return false;
    }

    configureSlicing(params, testSuite);

    CoverageReport finalReport = createSFLCoverageReport(params, testSuite);

    configureSFLExporter(params, finalReport);

    logger.info("Calling exporter");
    return exporter.exportSFLMatrices();
  }

  private CoverageReport createSFLCoverageReport(JSRParams params, TestSuite testSuite) {

    if (failCoverageStrategy == null) {
      failCoverageStrategy = new CheckedCoverageStrategy(testSuite, parser, slicer);
    }
    if (passCoverageStrategy == null) {
      passCoverageStrategy = new LineCoverageStrategy(params.pathJar,
                                                      params.pathClasses,
                                                      params.pathSources,
                                                      params.pathSlicer,
                                                      params.pathOut,
                                                      params.basePackage);
    }

    passCoverageStrategy.setOriginalTestSuite(testSuite);

    logger.info("Calculating coverage for failing test cases");
    CoverageReport failCoverage = failCoverageStrategy.calculateOverallCoverage();
    logger.info("Calculating coverage for passing test cases");
    CoverageReport passCoverage = passCoverageStrategy.calculateOverallCoverage();

    Set<CoverageReport.Unit> allUnits = passCoverage.allUnits;
    Set<CoverageReport.Unit> coveredUnits =
      passCoverage.testCaseCoverageData.entrySet()
                                       .stream()
                                       .filter(e -> e.getKey().isPassed())
                                       .map(Map.Entry::getValue)
                                       .flatMap(Collection::stream)
                                       .collect(Collectors.toSet());

    coveredUnits.addAll(failCoverage.testCaseCoverageData.entrySet()
                                                         .stream()
                                                         .filter(e -> !e.getKey().isPassed())
                                                         .map(Map.Entry::getValue)
                                                         .flatMap(Set::stream)
                                                         .collect(Collectors.toSet()));

    Map<TestCase, Set<CoverageReport.Unit>> coverageData = new HashMap<>();
    passCoverage.testCaseCoverageData.entrySet()
                                     .stream()
                                     .filter(e -> e.getKey().isPassed())
                                     .forEach(e -> coverageData.put(e.getKey(), e.getValue()));
    failCoverage.testCaseCoverageData.entrySet()
                                     .stream()
                                     .filter(e -> !e.getKey().isPassed())
                                     .forEach(e -> coverageData.put(e.getKey(), e.getValue()));


    CoverageReport finalReport = new CoverageReport("mixed", allUnits, coveredUnits, coverageData);
    logger.info("Assembled combined coverage report");
    return finalReport;
  }

  private void configureSFLExporter(JSRParams params, CoverageReport finalReport) {
    if (exporter == null) {
      exporter = new SFLMatrixCsvExporter(finalReport, params.pathOut);
    }
  }

  private void configureSlicing(JSRParams params, TestSuite testSuite) {
    if (slicingStrategy == null) {
      slicingStrategy = new Slicer4JSlicingStrategy(params.pathJar.toString(),
                                                    params.pathSlicer.toString(),
                                                    params.pathOut.toString());
    }

    if (slicer == null) {
      slicer = new JUnitTestSuiteSlicer(slicingStrategy, testSuite);
    }
  }

  private void configureParsing(JSRParams params) {
    if (parsingStrategy == null) {
      parsingStrategy = new JavaParserParsingStrategy(params.pathTestSources);
    }
    if (parser == null) {
      parser = new JUnitTestSuiteParser(parsingStrategy);
    }
  }

  private boolean checkParams(JSRParams params) {
    if (params == null) {
      logger.error("The passed params object must not be null!");
      return false;
    }

    if (params.pathTestSources == null ||
        params.pathJar == null ||
        params.pathSlicer == null ||
        params.pathOut == null ||
        params.pathClasses == null) {
      logger.error("Required parameters are not set (null)");
      return false;
    }

    return true;
  }

  /* Setter Dependency Injection */

  public void setParsingStrategy(ParsingStrategy parsingStrategy) {
    this.parsingStrategy = parsingStrategy;
  }

  public void setParser(TestSuiteParser parser) {
    this.parser = parser;
  }

  public void setSlicingStrategy(SlicingStrategy slicingStrategy) {
    this.slicingStrategy = slicingStrategy;
  }

  public void setSlicer(TestSuiteSlicer slicer) {
    this.slicer = slicer;
  }

  public void setExporter(SFLMatrixExporter exporter) {
    this.exporter = exporter;
  }

  public void setPassCoverageStrategy(CoverageStrategy passCoverageStrategy) {
    this.passCoverageStrategy = passCoverageStrategy;
  }

  public void setFailCoverageStrategy(CoverageStrategy failCoverageStrategy) {
    this.failCoverageStrategy = failCoverageStrategy;
  }

  SFLMatrixExporter getExporter() {
    return exporter;
  }
}
