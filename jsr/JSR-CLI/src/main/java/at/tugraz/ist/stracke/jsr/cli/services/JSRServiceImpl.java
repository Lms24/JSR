package at.tugraz.ist.stracke.jsr.cli.services;

import at.tugraz.ist.stracke.jsr.cli.candidates.AlgorithmCandidates;
import at.tugraz.ist.stracke.jsr.cli.candidates.CoverageCandidates;
import at.tugraz.ist.stracke.jsr.core.coverage.CoverageReport;
import at.tugraz.ist.stracke.jsr.core.coverage.export.CoverageReportExporter;
import at.tugraz.ist.stracke.jsr.core.coverage.strategies.CheckedCoverageStrategy;
import at.tugraz.ist.stracke.jsr.core.coverage.strategies.CoverageStrategy;
import at.tugraz.ist.stracke.jsr.core.coverage.strategies.LineCoverageStrategy;
import at.tugraz.ist.stracke.jsr.core.coverage.strategies.MethodCoverageStrategy;
import at.tugraz.ist.stracke.jsr.core.facade.JSRFacade;
import at.tugraz.ist.stracke.jsr.core.facade.JUnitJSRFacadeBuilder;
import at.tugraz.ist.stracke.jsr.core.parsing.JUnitTestSuiteParser;
import at.tugraz.ist.stracke.jsr.core.parsing.TestSuiteParser;
import at.tugraz.ist.stracke.jsr.core.parsing.strategies.JavaParserParsingStrategy;
import at.tugraz.ist.stracke.jsr.core.parsing.strategies.ParsingStrategy;
import at.tugraz.ist.stracke.jsr.core.sfl.SFLFacade;
import at.tugraz.ist.stracke.jsr.core.sfl.SFLFacadeImpl;
import at.tugraz.ist.stracke.jsr.core.shared.JSRParams;
import at.tugraz.ist.stracke.jsr.core.shared.TestSuite;
import at.tugraz.ist.stracke.jsr.core.slicing.JUnitTestSuiteSlicer;
import at.tugraz.ist.stracke.jsr.core.slicing.TestSuiteSlicer;
import at.tugraz.ist.stracke.jsr.core.slicing.strategies.Slicer4JSlicingStrategy;
import at.tugraz.ist.stracke.jsr.core.slicing.strategies.SlicingStrategy;
import at.tugraz.ist.stracke.jsr.core.tsr.ReducedTestSuite;
import at.tugraz.ist.stracke.jsr.core.tsr.strategies.GeneticReductionStrategy;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.nio.file.Path;

public class JSRServiceImpl implements TSRService, SFLService, CoverageService {

  @Override
  public ReducedTestSuite reduceTestSuite(JSRParams params) {

    JUnitJSRFacadeBuilder builder = new JUnitJSRFacadeBuilder(
      params.pathSources,
      params.pathTestSources,
      params.pathJar,
      params.pathOut,
      params.pathSlicer
    );

    if (params.pathGenOut != null) {
      builder.applyModificationsAsCopy(params.pathGenOut);
    }

    CoverageStrategy coverageStrategy = getCoverageStrategy(params);
    if (coverageStrategy != null) {
      builder.coverageStrategy(coverageStrategy);
    }

    if (AlgorithmCandidates.ALG_GENETIC.equals(params.algorithm)) {
      builder.reductionStrategy(new GeneticReductionStrategy());
    }

    JSRFacade facade = builder.build();

    if (params.pathCoverageReport != null) {
      CoverageReport report = this.readCoverageReport(params.pathCoverageReport);
      if (report == null) {
        return null;
      }
      return facade.reduceTestSuiteFromCoverageReport(report);
    }
    return facade.reduceTestSuite();
  }


  @Override
  public boolean createAndExportSFLMatrices(JSRParams params) {
    SFLFacade facade = new SFLFacadeImpl();
    return facade.createAndExportSFLMatrices(params);
  }

  private CoverageReport readCoverageReport(Path pathCoverageReport) {
    File reportFile = pathCoverageReport.toFile();
    CoverageReport report;
    try {
      FileInputStream fis = new FileInputStream(reportFile);
      ObjectInputStream ois = new ObjectInputStream(fis);
      report = (CoverageReport) ois.readObject();
      ois.close();
    } catch (IOException | ClassNotFoundException e) {
      e.printStackTrace();
      return null;
    }
    return report;
  }

  private CoverageStrategy getCoverageStrategy(JSRParams params) {
    if (params.coverageMetric == null) {
      return null;
    }

    CoverageStrategy coverageStrategy = null;
    switch (params.coverageMetric) {
      case CoverageCandidates.COV_LINE:
        coverageStrategy = new LineCoverageStrategy(params.pathJar,
                                                    params.pathClasses,
                                                    params.pathSources,
                                                    params.pathSlicer,
                                                    params.pathOut,
                                                    params.basePackage);
        break;
      case CoverageCandidates.COV_METHOD:
        coverageStrategy = new MethodCoverageStrategy(params.pathJar,
                                                      params.pathClasses,
                                                      params.pathSources,
                                                      params.pathSlicer,
                                                      params.pathOut,
                                                      params.basePackage);
        break;
    }
    return coverageStrategy;
  }

  @Override
  public CoverageReport calculateCoverage(JSRParams params) {
    CoverageStrategy coverageStrategy;
    ParsingStrategy parsingStrategy = new JavaParserParsingStrategy(params.pathTestSources);
    TestSuiteParser parser = new JUnitTestSuiteParser(parsingStrategy);
    parser.parse();
    TestSuite originalTestSuite = parser.getResult();

    switch (params.coverageMetric) {
      case CoverageCandidates.COV_LINE:
        coverageStrategy = new LineCoverageStrategy(params.pathJar,
                                                      params.pathClasses,
                                                      params.pathSources,
                                                      params.pathSlicer,
                                                      params.pathOut,
                                                      params.basePackage);
        coverageStrategy.setOriginalTestSuite(originalTestSuite);
        break;
      case CoverageCandidates.COV_METHOD:
        coverageStrategy = new MethodCoverageStrategy(params.pathJar,
                                                      params.pathClasses,
                                                      params.pathSources,
                                                      params.pathSlicer,
                                                      params.pathOut,
                                                      params.basePackage);
        coverageStrategy.setOriginalTestSuite(originalTestSuite);
        break;
      default:
        SlicingStrategy slicingStrategy = new Slicer4JSlicingStrategy(params.pathJar.toString(),
                                                                      params.pathSlicer.toString(),
                                                                      params.pathOut.toString());
        TestSuiteSlicer slicer = new JUnitTestSuiteSlicer(slicingStrategy, originalTestSuite);

        coverageStrategy = new CheckedCoverageStrategy(originalTestSuite, parser, slicer);
    }

    CoverageReport report = coverageStrategy.calculateOverallCoverage();

    CoverageReportExporter exporter = new CoverageReportExporter(report);
    exporter.exportToFile(Path.of(params.pathOut.toString(), "coverage"),
                          report.coverageType + "CoverageReport.cvg");

    return report;
  }
}
