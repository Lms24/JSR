package at.tugraz.ist.stracke.jsr.core.coverage.strategies;

import at.tugraz.ist.stracke.jsr.core.coverage.CoverageReport;
import at.tugraz.ist.stracke.jsr.core.parsing.TestSuiteParser;
import at.tugraz.ist.stracke.jsr.core.shared.TestCase;
import at.tugraz.ist.stracke.jsr.core.shared.TestSuite;
import at.tugraz.ist.stracke.jsr.core.slicing.TestSuiteSlicer;
import at.tugraz.ist.stracke.jsr.core.slicing.strategies.Slicer4JCLI;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

import static at.tugraz.ist.stracke.jsr.core.coverage.strategies.JaCoCoCLI.Args.*;
import static at.tugraz.ist.stracke.jsr.core.coverage.strategies.JaCoCoCLI.FileNames.EXEC_LOG;
import static at.tugraz.ist.stracke.jsr.core.coverage.strategies.JaCoCoCLI.FileNames.REPORT_LOG;
import static at.tugraz.ist.stracke.jsr.core.coverage.strategies.JaCoCoCLI.Paths.AGENT_JAR;
import static at.tugraz.ist.stracke.jsr.core.coverage.strategies.JaCoCoCLI.Paths.CLI_JAR;
import static at.tugraz.ist.stracke.jsr.core.slicing.strategies.Slicer4JCLI.JUNIT4_RUNNER_MAIN_CLASS;

public class JaCoCoCoverageStrategy implements CoverageStrategy {

  private static final Logger logger = LogManager.getLogger(JaCoCoCoverageStrategy.class);
  private final String classPathSeparator;
  private Path pathToJar;
  private Path pathToClasses;
  private Path pathToSources;
  private Path pathToSlicer;
  private Path pathToOutDir;
  private TestSuite originalTestSuite;

  public JaCoCoCoverageStrategy(Path pathToJar,
                                Path pathToClasses,
                                Path pathToSources,
                                Path pathToSlicer,
                                Path pathToOutDir) {

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

  private Path convertOutPutPath(Path outDir) throws IOException {
    outDir = Path.of(outDir.toString(), "jacoco");
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
    originalTestSuite.testCases.forEach(this::runTestCase);
    return null;
  }

  private void runTestCase(TestCase tc) {
    String tcId = String.format("%s#%s", tc.getClassName(), tc.getName());
    String tcExecFileName = String.format("%s.exec", tcId);

    boolean success = this.instrumentAndExecuteTestCase(tcId, tcExecFileName);
    if (!success) return;

    Path tcOutDir = Path.of(this.pathToOutDir.toString(), tcId);
    if (!tcOutDir.toFile().exists()) {
      success = tcOutDir.toFile().mkdirs();
    }
    if (!success) {
      logger.error("Could not create directory{}", tcOutDir.toString());
      return;
    }

    ProcessBuilder pb = new ProcessBuilder()
      .command("java",
               "-jar", CLI_JAR,
               CLI_REPORT, this.pathToOutDir.toString() + tcExecFileName,
               CLI_CLASS_FILES, this.pathToClasses.toString(),
               CLI_SOURCE_FILES, this.pathToSources.toString(),
               CLI_XML, String.format("%s/report.xml", tcOutDir),
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
      }
    } catch (IOException | InterruptedException e) {
      logger.error("Error during report generation for {}:", tcId);
      e.printStackTrace();
    }
  }

  private boolean instrumentAndExecuteTestCase(String tcId, String tcExecFileName) {
    logger.info("Executing testcase {}", tcId);

    ProcessBuilder pb = new ProcessBuilder()
      .command("java",
               String.format("-javaagent:%s=%s",
                             AGENT_JAR,
                             String.format("%s=%s", AGENT_DEST_FILE, this.pathToOutDir.toString() + tcExecFileName)),
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
      if (p.exitValue() == 0) {
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
