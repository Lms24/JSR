package at.tugraz.ist.stracke.jsr.core.coverage.strategies;

import at.tugraz.ist.stracke.jsr.core.coverage.CoverageReport;
import at.tugraz.ist.stracke.jsr.core.parsing.TestSuiteParser;
import at.tugraz.ist.stracke.jsr.core.shared.TestCase;
import at.tugraz.ist.stracke.jsr.core.shared.TestSuite;
import at.tugraz.ist.stracke.jsr.core.slicing.TestSuiteSlicer;
import at.tugraz.ist.stracke.jsr.core.slicing.strategies.Slicer4JCLI;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Objects;

import static at.tugraz.ist.stracke.jsr.core.coverage.strategies.JaCoCoCLI.Args.*;
import static at.tugraz.ist.stracke.jsr.core.coverage.strategies.JaCoCoCLI.FileNames.EXEC_LOG;
import static at.tugraz.ist.stracke.jsr.core.coverage.strategies.JaCoCoCLI.FileNames.REPORT_LOG;
import static at.tugraz.ist.stracke.jsr.core.coverage.strategies.JaCoCoCLI.Paths.AGENT_JAR;
import static at.tugraz.ist.stracke.jsr.core.coverage.strategies.JaCoCoCLI.Paths.CLI_JAR;
import static at.tugraz.ist.stracke.jsr.core.slicing.strategies.Slicer4JCLI.JUNIT4_RUNNER_MAIN_CLASS;

abstract class JaCoCoCoverageStrategy implements CoverageStrategy {

  protected final Logger logger;
  private final String classPathSeparator;
  private Path pathToJar;
  private Path pathToClasses;
  private Path pathToSources;
  private Path pathToSlicer;
  protected Path pathToOutDir;
  protected TestSuite originalTestSuite;

  public JaCoCoCoverageStrategy(Path pathToJar,
                                Path pathToClasses,
                                Path pathToSources,
                                Path pathToSlicer,
                                Path pathToOutDir,
                                Logger concreteLogger) {
    this.logger = concreteLogger;
    this.classPathSeparator = System.getProperty("path.separator");

    try {
      this.pathToJar = pathToJar.toAbsolutePath().toRealPath();
      this.pathToClasses = pathToClasses.toAbsolutePath().toRealPath();
      this.pathToSources = pathToSources.toAbsolutePath().toRealPath();
      this.pathToSlicer = pathToSlicer.toAbsolutePath().toRealPath();
      this.pathToOutDir = convertOutPutPath(pathToOutDir);
    } catch (IOException e) {
      logger.error("Error while setting up directories for JaCoCo");
      e.printStackTrace();
    }
  }

  /**
   * Template method to be implemented by the concrete class.
   * All relevant data was collected before this method is called.
   * It is now up to the concrete class to assemble it into a coverage
   * report w/ the respective coverage metric.
   *
   * @return the final coverage report or <code>null</code> on error.
   */
  abstract CoverageReport createTestSuiteCoverageReport();

  private Path convertOutPutPath(Path outDir) throws IOException {
    outDir = Path.of(outDir.toString(), "coverage");
    File outFile = outDir.toFile();

    if (!outFile.exists()) {
      boolean success = outFile.mkdirs();
      if (!success) {
        logger.error("Could not create output directory");
      }
    }

    return outDir.toAbsolutePath().toRealPath();
  }

  @Override
  public CoverageReport calculateOverallCoverage() {
    boolean collectedAllTCData = originalTestSuite.testCases.stream().allMatch(this::runTestCase);

    if (!collectedAllTCData) {
      logger.error("Could not collect all individual test case data. Aborting...");
      return null;
    }

    CoverageReport report = createTestSuiteCoverageReport();

    if (report != null) {
      this.cleanup();
    }

    return report;
  }

  private boolean runTestCase(TestCase tc) {
    String tcId = String.format("%s#%s", tc.getClassName(), tc.getName());
    String tcExecFileName = String.format("%s.exec", tcId);

    boolean success = this.instrumentAndExecuteTestCase(tcId, tcExecFileName);
    if (!success) return false;

    success = this.createReport(tcId, tcExecFileName);
    return success;
  }

  private boolean createReport(String tcId, String tcExecFileName) {
    Path tcOutDir = Path.of(this.pathToOutDir.toString(), tcId);

    boolean success = true;
    if (!tcOutDir.toFile().exists()) {
      success = tcOutDir.toFile().mkdirs();
    }

    if (!success) {
      logger.error("Could not create directory {}", tcOutDir.toString());
      return false;
    }

    ProcessBuilder pb = new ProcessBuilder()
      .command("java",
               "-jar", CLI_JAR,
               CLI_REPORT, this.pathToOutDir.toString() + "/" + tcExecFileName,
               CLI_CLASS_FILES, this.pathToClasses.toString(),
               CLI_SOURCE_FILES, this.pathToSources.toString(),
               CLI_XML, String.format("%s/report.xml", tcOutDir),
        /*CLI_CSV, String.format("%s/report.csv", tcOutDir),*/
               CLI_HTML, tcOutDir.toString())
      .redirectOutput(ProcessBuilder.Redirect.to(new File(
        String.format("%s/%s", this.pathToOutDir.toString(), REPORT_LOG))))
      .redirectError(ProcessBuilder.Redirect.to(new File(
        String.format("%s/%s", this.pathToOutDir.toString(), REPORT_LOG))));

    try {
      logger.debug("Report generation command: {}", String.join(" ", pb.command()));
      Process p = pb.start();
      p.waitFor();
      if (p.exitValue() == 0) {
        logger.info("Report for {} was generated successfully", tcId);
      } else {
        logger.error("Error while generating report for {}. See {} for details", tcId, REPORT_LOG);
        return false;
      }
    } catch (IOException | InterruptedException e) {
      logger.error("Error during report generation for {}:", tcId);
      e.printStackTrace();
      return false;
    }

    return true;
  }

  private boolean instrumentAndExecuteTestCase(String tcId, String tcExecFileName) {
    logger.info("Executing testcase {}", tcId);

    ProcessBuilder pb = new ProcessBuilder()
      .command("java",
               String.format("-javaagent:%s=%s",
                             AGENT_JAR,
                             String.format("%s=%s/%s", AGENT_DEST_FILE, this.pathToOutDir.toString(), tcExecFileName)),
               "-cp", String.format("%s/%s%s%s/%s%s%s",
                                    this.pathToSlicer.toString(), Slicer4JCLI.Paths.PATH_JUNIT4_RUNNER_JAR,
                                    this.classPathSeparator,
                                    this.pathToSlicer.toString(), Slicer4JCLI.Paths.PATH_JUNIT4_LIB_JAR,
                                    this.classPathSeparator,
                                    this.pathToJar.toString()),
               JUNIT4_RUNNER_MAIN_CLASS,
               tcId)
      .redirectOutput(ProcessBuilder.Redirect.to(new File(
        String.format("%s/%s", this.pathToOutDir.toString(), EXEC_LOG))))
      .redirectError(ProcessBuilder.Redirect.to(new File(
        String.format("%s/%s", this.pathToOutDir.toString(), EXEC_LOG))));

    try {
      logger.debug("Instrumentation and execution command: {}", String.join(" ", pb.command()));
      Process p = pb.start();
      p.waitFor();

      // 0.. test pass, 1.. test fail, rest.. Error
      if (p.exitValue() == 0 || p.exitValue() == 1) {
        logger.info("Test case was instrumented and executed successfully.");
      } else {
        logger.error("Error while instrumenting and executing {}, see {} for details", tcId, EXEC_LOG);
      }
    } catch (IOException | InterruptedException e) {
      logger.error("Error during test case execution, Caught exception:");
      e.printStackTrace();
      return false;
    }

    return true;
  }

  private void cleanup() {
    final File[] outDirFiles = Objects.requireNonNull(this.pathToOutDir.toFile().listFiles());

    boolean deletedAllExecFiles = Arrays.stream(outDirFiles)
                                        .filter(file -> file.getName().endsWith(".exec"))
                                        .allMatch(File::delete);

    boolean deletedAllLogFiles = Arrays.stream(outDirFiles)
                                       .filter(file -> file.getName().endsWith(".log"))
                                       .allMatch(File::delete);

    boolean deletedIndividualReports = Arrays.stream(outDirFiles)
                                             .filter(File::isDirectory)
                                             .map(File::toPath)
                                             .allMatch(p -> {
                                               try {
                                                 return Files.walk(p)
                                                             .sorted(Comparator.reverseOrder())
                                                             .map(Path::toFile)
                                                             .allMatch(File::delete);
                                               } catch (IOException e) {
                                                 e.printStackTrace();
                                                 logger.error("Could not delete all individual reports.");
                                                 return false;
                                               }
                                             });

    if (!deletedAllExecFiles) {
      logger.error("Could not delete all exec files.");
    }

    if (!deletedAllLogFiles) {
      logger.error("Could not delete all log files.");
    }

    if (deletedAllExecFiles && deletedAllLogFiles && deletedIndividualReports) {
      logger.debug("Cleanup successful!");
    }
  }

  @Override
  public void setOriginalTestSuite(TestSuite testSuite) {
    this.originalTestSuite = testSuite;
  }

  @Override
  public void setSlicer(TestSuiteSlicer slicer) {
    throw new IllegalThreadStateException("Should not be called for this class");
  }

  @Override
  public void setStatementParser(TestSuiteParser parser) {
    throw new IllegalThreadStateException("Should not be called for this class");
  }
}
