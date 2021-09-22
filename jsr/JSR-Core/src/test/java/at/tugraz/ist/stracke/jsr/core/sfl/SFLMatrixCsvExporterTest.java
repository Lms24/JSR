package at.tugraz.ist.stracke.jsr.core.sfl;

import at.tugraz.ist.stracke.jsr.test.TSRData;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

class SFLMatrixCsvExporterTest {

  @Test
  void testCreateCoverageMatrixCsv() {
    SFLMatrixCsvExporter exporter = new SFLMatrixCsvExporter(TSRData.smallCoverageReport);
    String coverageMatrix = exporter.createCoverageMatrixCsv();

    assertThat(coverageMatrix, is(equalTo("TestCase,s1,s2,s3,s4,s5,s6" + System.lineSeparator() +
                                          "t1:t1,1,0,1,0,1,0" + System.lineSeparator() +
                                          "t2:t2,0,1,0,1,1,0" + System.lineSeparator() +
                                          "t3:t3,0,1,0,1,1,0" + System.lineSeparator() +
                                          "t4:t4,0,1,1,0,0,1" + System.lineSeparator() +
                                          "t5:t5,1,0,0,1,1,0" + System.lineSeparator())));
  }

  @Test
  void testCreateOutcomeMatrixCsv() {
    SFLMatrixCsvExporter exporter = new SFLMatrixCsvExporter(TSRData.smallCoverageReport);
    String outcomeMatrix = exporter.createOutcomeMatrixCsv();

    assertThat(outcomeMatrix, is(equalTo("TestCase,pass" + System.lineSeparator() +
                                         "t1:t1,1" + System.lineSeparator() +
                                         "t2:t2,1" + System.lineSeparator() +
                                         "t3:t3,1" + System.lineSeparator() +
                                         "t4:t4,1" + System.lineSeparator() +
                                         "t5:t5,1" + System.lineSeparator())));
  }

  @Test
  void testExportSFLMatrices() {
    final Path outputDir = Path.of("./.matrixOut").toAbsolutePath();
    SFLMatrixExporter exporter = new SFLMatrixCsvExporter(TSRData.smallCoverageReport, outputDir);

    boolean success = exporter.exportSFLMatrices();

    File coverageFile = Path.of(outputDir.toString(), SFLMatrixCsvExporter.COVERAGE_MATRIX_FILENAME).toFile();
    File outcomeFile = Path.of(outputDir.toString(), SFLMatrixCsvExporter.OUTCOME_MATRIX_FILENAME).toFile();

    assertThat(success, is(true));
    assertThat(coverageFile.exists(), is(true));
    assertThat(outcomeFile.exists(), is(true));
  }
}