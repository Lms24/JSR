package at.tugraz.ist.stracke.jsr.core.facade;

import at.tugraz.ist.stracke.jsr.core.coverage.strategies.CoverageStrategy;
import at.tugraz.ist.stracke.jsr.core.parsing.TestSuiteParser;
import at.tugraz.ist.stracke.jsr.core.parsing.strategies.ParsingStrategy;
import at.tugraz.ist.stracke.jsr.core.slicing.TestSuiteSlicer;
import at.tugraz.ist.stracke.jsr.core.slicing.strategies.SlicingStrategy;
import at.tugraz.ist.stracke.jsr.core.tsr.reducer.TestSuiteReducer;
import at.tugraz.ist.stracke.jsr.core.tsr.serializer.Serializer;
import at.tugraz.ist.stracke.jsr.core.tsr.strategies.ReductionStrategy;

import java.nio.file.Path;

class JSRConfig {

  public Path sourceDir;
  public Path testDir;
  public Path jarFile;
  public Path outputDir;
  public Path slicerDir;

  public TestSuiteParser testSuiteParser;
  public ParsingStrategy testSuiteParsingStrategy;

  public TestSuiteParser statementParser;
  public ParsingStrategy statementParsingStrategy;

  public TestSuiteSlicer slicer;
  public SlicingStrategy slicingStrategy;

  public CoverageStrategy coverageStrategy;

  public TestSuiteReducer reducer;
  public ReductionStrategy reductionStrategy;

  public boolean serialize;
  public Path serializationDirectory;
  public Serializer serializer;

  public String staticTSRReportName;

  public boolean keepZeroCoverageTCs;

  public JSRConfig(Path sourceDir, Path testDir, Path jarFile, Path outputDir, Path slicerDir) {
    this.sourceDir = sourceDir;
    this.testDir = testDir;
    this.jarFile = jarFile;
    this.outputDir = outputDir;
    this.slicerDir = slicerDir;
  }
}
