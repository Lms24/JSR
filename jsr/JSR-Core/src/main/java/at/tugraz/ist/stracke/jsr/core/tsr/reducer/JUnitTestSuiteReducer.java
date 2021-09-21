package at.tugraz.ist.stracke.jsr.core.tsr.reducer;

import at.tugraz.ist.stracke.jsr.core.tsr.ReducedTestSuite;
import at.tugraz.ist.stracke.jsr.core.tsr.TSRReport;
import at.tugraz.ist.stracke.jsr.core.tsr.strategies.ReductionStrategy;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class JUnitTestSuiteReducer implements TestSuiteReducer {

  public static final String REPORT_DEFAULT_FILENAME = "tsr-report.log";
  private final ReductionStrategy tsrStrategy;

  private ReducedTestSuite reducedTestSuite;

  public JUnitTestSuiteReducer(ReductionStrategy tsrStrategy) {
    this.tsrStrategy = tsrStrategy;
  }

  @Override
  public TestSuiteReducer reduce() {
    this.reducedTestSuite = this.tsrStrategy.reduce();
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
    }

    return this;
  }

  @Override
  public TestSuiteReducer serialize(Path srcTestDir) {
    // TODO actually reduce the program's test suite
    //  (i.e. delete unnecessary test cases)
    return this;
  }

  @Override
  public ReducedTestSuite getReducedTestSuite() {
    return this.reducedTestSuite;
  }
}
