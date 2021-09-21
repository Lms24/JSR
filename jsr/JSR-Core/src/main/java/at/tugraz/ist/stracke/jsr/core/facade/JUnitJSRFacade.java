package at.tugraz.ist.stracke.jsr.core.facade;

import at.tugraz.ist.stracke.jsr.core.coverage.CoverageReport;
import at.tugraz.ist.stracke.jsr.core.coverage.strategies.CheckedCoverageStrategy;
import at.tugraz.ist.stracke.jsr.core.coverage.strategies.CoverageStrategy;
import at.tugraz.ist.stracke.jsr.core.parsing.JUnitTestSuiteParser;
import at.tugraz.ist.stracke.jsr.core.parsing.TestSuiteParser;
import at.tugraz.ist.stracke.jsr.core.parsing.strategies.JavaParserParsingStrategy;
import at.tugraz.ist.stracke.jsr.core.parsing.strategies.ParsingStrategy;
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

import java.nio.file.Path;

public class JUnitJSRFacade implements JSRFacade {
  private final Path sourceDir;
  private final Path testDir;
  private final Path jarFile;
  private final Path outputDir;
  private final Path slicerDir;

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
  public JUnitJSRFacade(@NonNull Path sourceDir,
                        @NonNull Path testDir,
                        @NonNull Path jarFile,
                        @NonNull Path outputDir,
                        @NonNull Path slicerDir) {
    this.sourceDir = sourceDir.toAbsolutePath();
    this.testDir = testDir.toAbsolutePath();
    this.jarFile = jarFile.toAbsolutePath();
    this.outputDir = outputDir.toAbsolutePath();
    this.slicerDir = slicerDir.toAbsolutePath();
  }

  @Override
  public ReducedTestSuite reduceTestSuiteWithCheckedCoverage() {

    // Step 1: Parse the test suite
    ParsingStrategy parsingStrategy = new JavaParserParsingStrategy(this.testDir);
    TestSuiteParser parser = new JUnitTestSuiteParser(parsingStrategy);
    parser.parse();
    TestSuite originalTestSuite = parser.getResult();

    // Step 2: Code instrumentation, TS execution and Slicing per test case, Coverage
    SlicingStrategy slicingStrategy = new Slicer4JSlicingStrategy(this.jarFile.toString(),
                                                                  this.slicerDir.toString(),
                                                                  this.outputDir.toString());
    TestSuiteSlicer slicer = new JUnitTestSuiteSlicer(slicingStrategy, originalTestSuite);

    parsingStrategy = new JavaParserParsingStrategy(this.sourceDir);
    parser = new JUnitTestSuiteParser(parsingStrategy);

    CoverageStrategy coverageStrategy = new CheckedCoverageStrategy(originalTestSuite,
                                                                    parser,
                                                                    slicer);
    CoverageReport report = coverageStrategy.calculateOverallCoverage();

    // Step 3: Perform TSR
    ReductionStrategy reductionStrategy = new GreedyIHGSReductionStrategy(originalTestSuite, report);
    TestSuiteReducer reducer = new JUnitTestSuiteReducer(reductionStrategy);

    return reducer.reduce().generateReport(this.outputDir).getReducedTestSuite();
  }
}
