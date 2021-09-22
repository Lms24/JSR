package at.tugraz.ist.stracke.jsr.core.sfl;

import at.tugraz.ist.stracke.jsr.core.coverage.CoverageReport;
import at.tugraz.ist.stracke.jsr.core.shared.TestCase;
import at.tugraz.ist.stracke.jsr.core.tsr.TSRTestCase;
import com.google.common.collect.Table;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Comparator;
import java.util.stream.Collectors;

public class SFLMatrixCsvExporter implements SFLMatrixExporter {

  public static final String COVERAGE_MATRIX_FILENAME = "coverageMatrix.csv";
  public static final String OUTCOME_MATRIX_FILENAME = "outcomeMatrix.csv";

  private static Logger logger = LogManager.getLogger(SFLMatrixCsvExporter.class);

  private CoverageReport coverageReport;
  private final Path outputDir;
  private final String coverageMatrixFilename;
  private final String passMatrixFilename;

  public SFLMatrixCsvExporter(@NonNull CoverageReport coverageReport,
                              @NonNull Path outputDir,
                              @NonNull String coverageMatrixFilename,
                              @NonNull String passMatrixFilename) {

    this.coverageReport = coverageReport;
    this.outputDir = outputDir;
    this.coverageMatrixFilename = coverageMatrixFilename;
    this.passMatrixFilename = passMatrixFilename;
  }

  public SFLMatrixCsvExporter(@NonNull CoverageReport coverageReport,
                              @NonNull Path outputDir) {
    this.coverageReport = coverageReport;
    this.outputDir = outputDir;
    this.coverageMatrixFilename = COVERAGE_MATRIX_FILENAME;
    this.passMatrixFilename = OUTCOME_MATRIX_FILENAME;
  }

  SFLMatrixCsvExporter(@NonNull CoverageReport coverageReport) {
    this.coverageReport = coverageReport;
    this.outputDir = null;
    this.coverageMatrixFilename = null;
    this.passMatrixFilename = null;
  }

  public SFLMatrixCsvExporter() {
    this.outputDir = null;
    this.coverageMatrixFilename = null;
    this.passMatrixFilename = null;
  }

  @Override
  public boolean exportSFLMatrices() {
    logger.info("Starting SFL Matrix export");
    String coverageCsv = this.createCoverageMatrixCsv();
    String outcomeCsv = this.createOutcomeMatrixCsv();

    final boolean success = writeFiles(coverageCsv, outcomeCsv);

    logger.info("SFL Matrix export completed");

    return success;
  }

  public String createCoverageMatrixCsv() {
    StringBuilder sb = new StringBuilder();
    Table<TSRTestCase, CoverageReport.Unit, Boolean> table = this.coverageReport.toTable(true);

    // create header row
    sb.append("TestCase,").append(table.columnKeySet()
                                       .stream()
                                       .map(CoverageReport.Unit::toString)
                                       .sorted(Comparator.comparing(s -> s))
                                       .collect(Collectors.joining(",")));
    sb.append(System.lineSeparator());

    // write coverage rows
    table.rowMap().forEach((tc, map) -> {
      sb.append(tc.getFullName())
        .append(",")
        .append(map.entrySet()
                   .stream()
                   .sorted(Comparator.comparing(e -> e.getKey().toString()))
                   .collect(Collectors.toList())
                   .stream()
                   .map(b -> b.getValue() != null && b.getValue() ? "1" : "0").collect(
            Collectors.joining(",")))
        .append(System.lineSeparator());
    });

    return sb.toString();
  }

  public String createOutcomeMatrixCsv() {
    StringBuilder sb = new StringBuilder();

    // create header row
    sb.append("TestCase,").append("pass").append(System.lineSeparator());

    // write outcome rows
    this.coverageReport.testCaseCoverageData.keySet()
                                            .stream()
                                            .sorted(Comparator.comparing(TestCase::getFullName))
                                            .forEach(tc -> sb.append(tc.getFullName())
                                                             .append(",")
                                                             .append("1") //TODO add real verdict here
                                                             .append(System.lineSeparator()));

    return sb.toString();
  }

  private boolean writeFiles(String coverageCsv, String outcomeCsv) {
    boolean success = true;
    if (this.outputDir != null) {

      if (!Files.exists(outputDir)) {
        try {
          Files.createDirectory(outputDir);
        } catch (IOException e) {
          logger.error("Error while creating directory {}", outputDir);
          logger.error(e.toString(), e.getMessage());
          Arrays.stream(e.getStackTrace())
                .map(StackTraceElement::toString)
                .forEach(s -> logger.error(s));
          e.printStackTrace();
        }
      }

      final Path coverageFilePath = Path.of(outputDir + "/" + coverageMatrixFilename);
      success = this.writeFile(coverageFilePath, coverageCsv);
      if (success) {
        logger.info("Exported coverage matrix to {}", coverageFilePath);
      }

      final Path outcomeFilePath = Path.of(outputDir + "/" + passMatrixFilename);
      success &= this.writeFile(outcomeFilePath, outcomeCsv);
      if (success) {
        logger.info("Exported test outcome matrix to {}", outcomeFilePath);
      }
    }
    return success;
  }

  private boolean writeFile(Path filePath, String content) {
    try {
      Files.write(filePath, content.getBytes(StandardCharsets.UTF_8));
      return true;
    } catch (IOException e) {
      logger.error("Error while writing file {}", filePath.toString());
      logger.error(e.toString(), e.getMessage());
      Arrays.stream(e.getStackTrace())
            .map(StackTraceElement::toString)
            .forEach(s -> logger.error(s));
      e.printStackTrace();
      return false;
    }
  }

  @Override
  public void setCoverageReport(CoverageReport report) {
    this.coverageReport = report;
  }
}
