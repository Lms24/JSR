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

  private Logger logger = LogManager.getLogger(SFLFacadeImpl.class);

  private ParsingStrategy parsingStrategy;
  private TestSuiteParser parser;

  private SlicingStrategy slicingStrategy;
  private TestSuiteSlicer slicer;

  private SFLMatrixExporter exporter;

  @Override
  public boolean createAndExportSFLMatrices(JSRParams params) {

    if (params == null) {
      return false;
    }

    if (parsingStrategy == null) {
      parsingStrategy = new JavaParserParsingStrategy(params.pathTestSources);
    }
    if (parser == null) {
      parser = new JUnitTestSuiteParser(parsingStrategy);
    }

    parser.parse();
    TestSuite testSuite = parser.getResult();

    if (slicingStrategy == null) {
      slicingStrategy = new Slicer4JSlicingStrategy(params.pathJar.toString(),
                                                    params.pathSlicer.toString(),
                                                    params.pathOut.toString());
    }

    if (slicer == null) {
      slicer = new JUnitTestSuiteSlicer(slicingStrategy, testSuite);
    }


    CoverageStrategy failCoverageStrategy = new CheckedCoverageStrategy(testSuite, parser, slicer);
    CoverageStrategy passCoverageStrategy = new LineCoverageStrategy(params.pathJar,
                                                                     params.pathClasses,
                                                                     params.pathSources,
                                                                     params.pathSlicer,
                                                                     params.pathOut,
                                                                     params.basePackage);

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

    if (exporter == null) {
      exporter = new SFLMatrixCsvExporter(finalReport, params.pathOut);
    }

    logger.info("Calling exporter");
    return exporter.exportSFLMatrices();
  }
}
