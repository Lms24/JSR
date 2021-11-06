package at.tugraz.ist.stracke.jsr.core.coverage.strategies;

import at.tugraz.ist.stracke.jsr.core.coverage.CoverageReport;
import at.tugraz.ist.stracke.jsr.core.parsing.TestSuiteParser;
import at.tugraz.ist.stracke.jsr.core.parsing.statements.Statement;
import at.tugraz.ist.stracke.jsr.core.shared.TestCase;
import at.tugraz.ist.stracke.jsr.core.shared.TestSuite;
import at.tugraz.ist.stracke.jsr.core.slicing.TestSuiteSlicer;
import at.tugraz.ist.stracke.jsr.core.slicing.result.SliceEntry;
import at.tugraz.ist.stracke.jsr.core.slicing.result.TestSuiteSliceResult;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

public class CheckedCoverageStrategy implements CoverageStrategy {

  private static final Logger logger = LogManager.getLogger(CheckedCoverageStrategy.class);

  private TestSuite originalTestSuite;
  private TestSuiteParser statementParser;
  private TestSuiteSlicer slicer;

  public boolean shouldCleanup = true;

  public CheckedCoverageStrategy(TestSuite originalTestSuite,
                                 TestSuiteParser statementParser,
                                 TestSuiteSlicer slicer) {
    this.originalTestSuite = originalTestSuite;
    this.statementParser = statementParser;
    this.slicer = slicer;
  }

  public CheckedCoverageStrategy(TestSuiteParser statementParser,
                                 TestSuiteSlicer slicer) {
    this.statementParser = statementParser;
    this.slicer = slicer;
  }

  @Override
  public CoverageReport calculateOverallCoverage() {
    logger.info("Parsing Source code to find executable statements");
    List<Statement> executableStatements = this.statementParser.getParsingStrategy().parseStatements();
    TestSuiteSliceResult res = this.slicer.slice();

    Set<CoverageReport.Unit> allUnits = executableStatements.stream()
                                                            .filter(s -> !originalTestSuite.testClasses.contains(s.getClassName()))
                                                            .map(Statement::toUnit)
                                                            .collect(Collectors.toSet());

    Set<CoverageReport.Unit> coveredUnits = res.getTestCaseSliceUnion()
                                               .stream()
                                               .filter(s -> !originalTestSuite.testClasses.contains(s.className))
                                               .map(SliceEntry::toStatement)
                                               .map(Statement::toUnit)
                                               .filter(u -> allUnits.stream()
                                                                    .anyMatch(u2 -> u.name.equals(u2.name) &&
                                                                                    u.positionStart ==
                                                                                    u2.positionStart)) // only units that we marked as executable should be in here
                                               .collect(Collectors.toSet());


    Map<TestCase, Set<CoverageReport.Unit>> testCaseData =
      res.testCaseSlices.stream()
                        .collect(Collectors.toMap(tcs -> tcs.testCase,
                                                  tcs -> tcs.slice.stream()
                                                                  .filter(s -> !originalTestSuite.testClasses.contains(s.className))
                                                                  .map(se -> se.toStatement().toUnit())
                                                                  .filter(u -> allUnits.stream()
                                                                                       .anyMatch(u2 ->
                                                                                                   u.name.equals(u2.name) &&
                                                                                                   u.positionStart ==
                                                                                                   u2.positionStart)) // only units that we marked as executable should be in here
                                                                  .collect(Collectors.toSet())));

    CoverageReport report = new CoverageReport("Checked",
                                               allUnits,
                                               coveredUnits,
                                               testCaseData);

    logger.info("Generated Checked Coverage Report:");
    logger.info("Found {} coverable lines, {} covered lines. Coverage Score: {}",
                allUnits.size(), coveredUnits.size(), report.getCoverageScore());

    this.cleanup();

    return report;
  }

  @Override
  public void setOriginalTestSuite(TestSuite testSuite) {
    this.originalTestSuite = testSuite;
  }

  @Override
  public void setSlicer(TestSuiteSlicer slicer) {
    this.slicer = slicer;
  }

  @Override
  public void setStatementParser(TestSuiteParser parser) {
    this.statementParser = parser;
  }

  boolean cleanup() {
    if (!this.shouldCleanup) {
      return true;
    }

    final File[] outDirFiles = Objects.requireNonNull(this.slicer.getSlicingStrategy()
                                                                 .getOutputDirectory()
                                                                 .toFile()
                                                                 .listFiles());

    final File jimple_code_dir = Arrays.stream(outDirFiles)
                                   .filter(file -> file.isDirectory() && file.getName().equals("jimple_code"))
                                   .findFirst().orElse(null);
    if (jimple_code_dir == null) {
      logger.warn("Could not delete jimple directory.");
      return false;
    }
    final File[] jimple_files = jimple_code_dir.listFiles();
    boolean deletedJImplDir = false;
    if (jimple_files != null && jimple_files.length > 0) {
      deletedJImplDir = Arrays.stream(jimple_files).allMatch(File::delete);
    }
    deletedJImplDir &= jimple_code_dir.delete();

    boolean deletedLogFiles = Arrays.stream(outDirFiles)
                                    .filter(file -> file.getName().endsWith(".log"))
                                    .allMatch(File::delete);
    boolean deletedTxtFiles = Arrays.stream(outDirFiles)
                                    .filter(file -> file.getName().endsWith(".txt") &&
                                                    !file.getName().equals("terminalLog.txt"))
                                    .allMatch(File::delete);
    boolean deletedCsvFiles = Arrays.stream(outDirFiles)
                                    .filter(file -> file.getName().endsWith(".csv") &&
                                                    file.getName().contains("result_s"))
                                    .allMatch(File::delete);
    boolean deletedPdfFiles = Arrays.stream(outDirFiles)
                                    .filter(file -> file.getName().endsWith(".pdf"))
                                    .allMatch(File::delete);
    boolean deletedJarFiles = Arrays.stream(outDirFiles)
                                    .filter(file -> file.getName().endsWith(".jar"))
                                    .allMatch(File::delete);

    if (!deletedJImplDir) {
      logger.warn("Could not delete jimple directory.");
    } else if (!deletedLogFiles) {
      logger.warn("Could not delete all log files.");
    } else if (!deletedTxtFiles) {
      logger.warn("Could not delete all txt files.");
    } else if (!deletedCsvFiles) {
      logger.warn("Could not delete all csv files.");
    } else if (!deletedPdfFiles) {
      logger.warn("Could not delete all pdf files.");
    } else if (!deletedJarFiles) {
      logger.warn("Could not delete all jar files.");
    }

    if (deletedJImplDir && deletedLogFiles && deletedTxtFiles &&
        deletedCsvFiles && deletedPdfFiles && deletedJarFiles) {
      logger.info("Cleanup successful!");
      return true;
    }
    return false;
  }
}
