package at.tugraz.ist.stracke.jsr.core.facade;

import at.tugraz.ist.stracke.jsr.core.tsr.ReducedTestSuite;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

/**
 * Integration tests via the {@link JSRFacade} interface to test core library functionality
 * for pre-defined strategies in the facade.
 */
class JUnitJSRFacadeTest {

  @Test
  void testReduceTestSuiteWithCheckedCoverage() {
    String srcDir = "./src/test/resources/smallProject/src/main/java";
    String testDir = "./src/test/resources/smallProject/src/test/java";
    String jarDir = "./src/test/resources/smallProject/build/libs/testJar.jar";
    String outDir = "./src/test/resources/smallProject/jsr";
    String slicerDir = "../../slicer/Slicer4J";

    JUnitJSRFacadeBuilder facadeBuilder = new JUnitJSRFacadeBuilder(Path.of(srcDir),
                                                                    Path.of(testDir),
                                                                    Path.of(jarDir),
                                                                    Path.of(outDir),
                                                                    Path.of(slicerDir));
    JSRFacade facade = facadeBuilder.build();

    ReducedTestSuite rts = facade.reduceTestSuite();

    assertThat(rts, is(notNullValue()));
    assertThat(rts.testCases, hasSize(6));
    assertThat(rts.removedTestCases, hasSize(8));

    Path covMatrix = Path.of(outDir, "coverageMatrix.csv");
    Path passMatrix = Path.of(outDir, "outcomeMatrix.csv");
    Path tsrReport = Path.of(outDir, "tsr-report.xml");
    Path instrumentedJar = Path.of(outDir, "testJar_i.jar");

    assertThat(covMatrix.toFile().exists(), is(true));
    assertThat(passMatrix.toFile().exists(), is(true));
    assertThat(tsrReport.toFile().exists(), is(true));
    assertThat(instrumentedJar.toFile().exists(), is(true));

    try {
      List<String> covMatrixLines = Files.readAllLines(covMatrix);
      List<String> passMatrixLines = Files.readAllLines(passMatrix);
      List<String> tsrReportLines = Files.readAllLines(tsrReport);

      assertThat(covMatrixLines, hasSize(15));
      assertThat(passMatrixLines, hasSize(15));
      assertThat(tsrReportLines, hasSize(68));

      assertThat(tsrReportLines.stream().anyMatch(l -> l.contains("<nrOriginalTestCases>14</nrOriginalTestCases>")),
                 is(true));
      assertThat(tsrReportLines.stream().anyMatch(l -> l.contains("<nrRetainedTestCases>6</nrRetainedTestCases>")),
                 is(true));
      assertThat(tsrReportLines.stream().anyMatch(l -> l.contains("<nrRemovedTestCases>8</nrRemovedTestCases>")),
                 is(true));

      assertThat(passMatrixLines.stream().anyMatch(
        l -> l.contains("at.tugraz.ist.stracke.jsr.CalculatorTest:divideZeroByNotZero,1")),
                 is(true));
      assertThat(
        passMatrixLines.stream().anyMatch(l -> l.contains("at.tugraz.ist.stracke.jsr.CalculatorTest:shouldFail,0")),
        is(true));

      assertThat(covMatrixLines.stream().anyMatch(
        l -> l.contains("at.tugraz.ist.stracke.jsr.MessageTest:testEquals,0,0,0,1,1,1,0,0,0,0,0,1,0,0,0,0,0,0,0")),
                 is(true));
      assertThat(covMatrixLines.stream().anyMatch(
        l -> l.contains("at.tugraz.ist.stracke.jsr.MessageTest:testEquals2,0,0,0,1,1,1,0,0,0,0,0,1,1,0,1,0,0,0,0")),
                 is(true));
      assertThat(covMatrixLines.stream().anyMatch(
        l -> l.contains("at.tugraz.ist.stracke.jsr.MessageTest:testGetters,0,0,0,1,1,1,0,0,0,0,0,0,0,0,0,0,1,1,1")),
                 is(true));

    } catch (IOException e) {
      e.printStackTrace();
      // If we get an exception, we fail
      assertThat(true, is(false));
    }
  }

  @Test
  void testReduceTestSuiteWithCheckedCoverageWithSerialization() {
    String srcDir = "./src/test/resources/smallProject/src/main/java";
    String testDir = "./src/test/resources/smallProject/src/test/java";
    String jarDir = "./src/test/resources/smallProject/build/libs/testJar.jar";
    String outDir = "./src/test/resources/smallProject/jsr";
    String slicerDir = "../../slicer/Slicer4J";
    String serialDir = "./src/test/resources/smallProject/jsr/serializedFiles";


    JUnitJSRFacadeBuilder facadeBuilder = new JUnitJSRFacadeBuilder(Path.of(srcDir),
                                                                    Path.of(testDir),
                                                                    Path.of(jarDir),
                                                                    Path.of(outDir),
                                                                    Path.of(slicerDir));

    JSRFacade facade = facadeBuilder.applyModificationsAsCopy(Path.of(serialDir))
                                    .build();

    ReducedTestSuite rts = facade.reduceTestSuite();

    assertThat(rts, is(notNullValue()));
    assertThat(rts.testCases, hasSize(6));
    assertThat(rts.removedTestCases, hasSize(8));

    Path covMatrix = Path.of(outDir, "coverageMatrix.csv");
    Path passMatrix = Path.of(outDir, "outcomeMatrix.csv");
    Path tsrReport = Path.of(outDir, "tsr-report.xml");
    Path instrumentedJar = Path.of(outDir, "testJar_i.jar");

    assertThat(covMatrix.toFile().exists(), is(true));
    assertThat(passMatrix.toFile().exists(), is(true));
    assertThat(tsrReport.toFile().exists(), is(true));
    assertThat(instrumentedJar.toFile().exists(), is(true));

    try {
      List<String> covMatrixLines = Files.readAllLines(covMatrix);
      List<String> passMatrixLines = Files.readAllLines(passMatrix);
      List<String> tsrReportLines = Files.readAllLines(tsrReport);

      assertThat(covMatrixLines, hasSize(15));
      assertThat(passMatrixLines, hasSize(15));
      assertThat(tsrReportLines, hasSize(68));

      assertThat(tsrReportLines.stream().anyMatch(l -> l.contains("<nrOriginalTestCases>14</nrOriginalTestCases>")),
                 is(true));
      assertThat(tsrReportLines.stream().anyMatch(l -> l.contains("<nrRetainedTestCases>6</nrRetainedTestCases>")),
                 is(true));
      assertThat(tsrReportLines.stream().anyMatch(l -> l.contains("<nrRemovedTestCases>8</nrRemovedTestCases>")),
                 is(true));

      assertThat(passMatrixLines.stream().anyMatch(
        l -> l.contains("at.tugraz.ist.stracke.jsr.CalculatorTest:divideZeroByNotZero,1")),
                 is(true));
      assertThat(
        passMatrixLines.stream().anyMatch(l -> l.contains("at.tugraz.ist.stracke.jsr.CalculatorTest:shouldFail,0")),
        is(true));

      assertThat(covMatrixLines.stream().anyMatch(
        l -> l.contains("at.tugraz.ist.stracke.jsr.MessageTest:testEquals,0,0,0,1,1,1,0,0,0,0,0,1,0,0,0,0,0,0,0")),
                 is(true));
      assertThat(covMatrixLines.stream().anyMatch(
        l -> l.contains("at.tugraz.ist.stracke.jsr.MessageTest:testEquals2,0,0,0,1,1,1,0,0,0,0,0,1,1,0,1,0,0,0,0")),
                 is(true));
      assertThat(covMatrixLines.stream().anyMatch(
        l -> l.contains("at.tugraz.ist.stracke.jsr.MessageTest:testGetters,0,0,0,1,1,1,0,0,0,0,0,0,0,0,0,0,1,1,1")),
                 is(true));

    } catch (IOException e) {
      e.printStackTrace();
      // If we get an exception, we fail
      assertThat(true, is(false));
    }
  }
}