package at.tugraz.ist.stracke.jsr.cli.services;

import java.nio.file.Path;

public class JSRParams {

  public Path pathSources;
  public Path pathTestSources;
  public Path pathJar;
  public Path pathClasses;
  public Path pathSlicer;
  public Path pathOut;
  public Path pathGenOut;
  public Path pathCoverageReport;
  public String coverageMetric;
  public String algorithm;
  public String basePackage;

  public JSRParams(Path pathTestSources,
                   Path pathSources,
                   Path pathJar,
                   Path pathClasses,
                   Path pathSlicer,
                   Path pathOut,
                   Path pathGenOut,
                   Path pathCoverageReport,
                   String coverageMetric,
                   String algorithm,
                   String basePackage) {
    this.pathSources = pathSources;
    this.pathTestSources = pathTestSources;
    this.pathJar = pathJar;
    this.pathClasses = pathClasses;
    this.pathSlicer = pathSlicer;
    this.pathOut = pathOut;
    this.pathGenOut = pathGenOut;
    this.pathCoverageReport = pathCoverageReport;
    this.coverageMetric = coverageMetric;
    this.algorithm = algorithm;
    this.basePackage = basePackage;
  }
}
