package at.tugraz.ist.stracke.jsr.core.tsr.reducer;

import at.tugraz.ist.stracke.jsr.core.tsr.ReducedTestSuite;
import at.tugraz.ist.stracke.jsr.core.tsr.TSRReport;
import at.tugraz.ist.stracke.jsr.core.tsr.serializer.Serializer;
import at.tugraz.ist.stracke.jsr.core.tsr.strategies.ReductionStrategy;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.stream.Collectors;

public class JUnitTestSuiteReducer implements TestSuiteReducer {

  public static final String REPORT_DEFAULT_FILENAME = "tsr-report.xml";

  private static final Logger logger = LogManager.getLogger(JUnitTestSuiteReducer.class);

  private ReductionStrategy tsrStrategy;

  private ReducedTestSuite reducedTestSuite;

  public JUnitTestSuiteReducer(ReductionStrategy tsrStrategy) {
    this.tsrStrategy = tsrStrategy;
  }

  @Override
  public TestSuiteReducer reduce() {
    logger.info("Starting test suite reduction");

    this.reducedTestSuite = this.tsrStrategy.reduce();

    logger.info("Finished test suite reduction");
    logger.info("Reduced test suite from {} to {} test cases: {} redundant test cases found",
                this.reducedTestSuite.testCases.size() + this.reducedTestSuite.removedTestCases.size(),
                this.reducedTestSuite.testCases.size(),
                this.reducedTestSuite.removedTestCases.size());

    return this;
  }

  @Override
  public TestSuiteReducer generateReport(Path reportDir) {
    return generateReport(reportDir, REPORT_DEFAULT_FILENAME);
  }

  @Override
  public TestSuiteReducer generateReport(Path reportDir, String reportName) {
    TSRReport report = new TSRReport(this.reducedTestSuite);
    String xml = report.toXMLString();
    Path destPath = Path.of(reportDir.toString(), reportName);

    try {
      if (!Files.exists(destPath)) {
        Files.createFile(destPath);
      }
      Files.write(destPath, xml.getBytes(StandardCharsets.UTF_8));
    } catch (IOException e) {
      e.printStackTrace();

      logger.error("Error while writing report: {}", e.getMessage());
      logger.error(Arrays.stream(e.getStackTrace())
                         .map(StackTraceElement::toString)
                         .collect(Collectors.joining("\n")));
    }

    logger.info("Generated report and wrote it to {}",
                reportDir + "/" + reportName);

    return this;
  }

  @Override
  public ReducedTestSuite getReducedTestSuite() {
    return this.reducedTestSuite;
  }

  @Override
  public void setReductionStrategy(ReductionStrategy strategy) {
    this.tsrStrategy = strategy;
  }
}
