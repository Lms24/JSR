package at.tugraz.ist.stracke.jsr.intellij.services;

import at.tugraz.ist.stracke.jsr.core.coverage.strategies.CoverageStrategy;
import at.tugraz.ist.stracke.jsr.core.coverage.strategies.LineCoverageStrategy;
import at.tugraz.ist.stracke.jsr.core.coverage.strategies.MethodCoverageStrategy;
import at.tugraz.ist.stracke.jsr.core.facade.JSRFacade;
import at.tugraz.ist.stracke.jsr.core.facade.JUnitJSRFacadeBuilder;
import at.tugraz.ist.stracke.jsr.core.tsr.ReducedTestSuite;
import at.tugraz.ist.stracke.jsr.core.tsr.strategies.GeneticReductionStrategy;
import at.tugraz.ist.stracke.jsr.intellij.misc.CoverageMetric;
import at.tugraz.ist.stracke.jsr.intellij.misc.ReductionAlgorithm;
import com.intellij.openapi.components.Service;

import java.nio.file.Path;

@Service()
public class ReductionService {
  public ReducedTestSuite startTSReduction(String strPathTestSources,
                                  String strPathSources,
                                  String strPathJar,
                                  String strPathClasses,
                                  String strPathSlicer,
                                  String strPathOutput,
                                  String strPathSerialOut,
                                  String basePackage,
                                  CoverageMetric coverageMetric,
                                  ReductionAlgorithm reductionAlgorithm,
                                  boolean deactivateTCs,
                                  boolean useLastCoverageReport) {

    final Path pathSources = Path.of(strPathSources);
    final Path pathTestSources = Path.of(strPathTestSources);
    final Path pathJar = Path.of(strPathJar);
    final Path pathSlicer = Path.of(strPathSlicer);
    final Path pathClasses = Path.of(strPathClasses);
    final Path pathOutput = Path.of(strPathOutput);
    final Path pathSerialOutput = Path.of(strPathSerialOut);

    JUnitJSRFacadeBuilder builder = new JUnitJSRFacadeBuilder(pathSources,
                                                              pathTestSources,
                                                              pathJar,
                                                              pathOutput,
                                                              pathSlicer);

    CoverageStrategy coverageStrategy = null;

    switch (coverageMetric) {
      case LINE_COVERAGE:
        coverageStrategy = new LineCoverageStrategy(pathJar,
                                                    pathClasses,
                                                    pathSources,
                                                    pathSlicer,
                                                    pathOutput,
                                                    basePackage);
        break;
      case METHOD_COVERAGE:
        coverageStrategy = new MethodCoverageStrategy(pathJar,
                                                      pathClasses,
                                                      pathSources,
                                                      pathSlicer,
                                                      pathOutput,
                                                      basePackage);
        break;
    }

    if (coverageStrategy != null) {
      builder.coverageStrategy(coverageStrategy);
    }

    if (deactivateTCs && strPathOutput != null && !strPathSerialOut.isEmpty() && !strPathSerialOut.isBlank()) {
      builder.applyModificationsAsCopy(pathSerialOutput);
    }

    if (reductionAlgorithm == ReductionAlgorithm.GENETIC) {
      builder.reductionStrategy(new GeneticReductionStrategy());
    }

    JSRFacade facade = builder.build();

    return facade.reduceTestSuite();
  }
}
