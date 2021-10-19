package at.tugraz.ist.stracke.jsr.core.slicing.strategies;

import at.tugraz.ist.stracke.jsr.core.parsing.statements.AssertionStatement;
import at.tugraz.ist.stracke.jsr.core.shared.JUnitTestCase;
import at.tugraz.ist.stracke.jsr.core.slicing.result.TestCaseSliceResult;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

class Slicer4JSlicingStrategyTest {

  @Test
  void testInstrument01() throws FileNotFoundException {

    final String outDir = ".testOut/testJar-out";
    SlicingStrategy strat =
      new Slicer4JSlicingStrategy("src/test/resources/testJars/testJar.jar",
                                  "../../slicer/Slicer4J",
                                  outDir);

    strat.instrumentJar();

    final File logFile = new File(outDir + "/instr-debug.log");
    final BufferedReader reader = new BufferedReader(new FileReader(logFile));

    assertThat(strat, is(not(nullValue())));

    // Interesting observation about these assertions:
    // although they contribute to this programs FDC, they would not contribute
    // anything in itself to our checked coverage implementation other than side effects
    assertThat(logFile.exists(), is(true));

    List<String> lines = reader.lines().collect(Collectors.toList());
    assertThat(lines.size(), is(greaterThan(0)));
    assertThat((int) lines.stream().filter(l -> l.contains("Instrumentation done")).count(), is(greaterThan(0)));
  }

  @Test
  void testExecuteAndTrace01() throws FileNotFoundException {

    final String outDir = ".testOut/testJar-out";
    SlicingStrategy strat =
      new Slicer4JSlicingStrategy("src/test/resources/testJars/testJar.jar",
                                  "../../slicer/Slicer4J",
                                  outDir);
    strat.instrumentJar();

    strat.setTestCase(new JUnitTestCase("divideZeroByNotZero",
                                        "at.tugraz.ist.stracke.jsr.CalculatorTest",
                                        Collections.singletonList(new AssertionStatement("assertEquals(1,1)", 5, 5))));

    // ignoring the return value here, as it does not have an influence on execution
    strat.execute();

    final File fullLogFile = new File(outDir + "/trace_full.log");
    final File logFile = new File(outDir + "/trace.log");
    final BufferedReader fullLogReader = new BufferedReader(new FileReader(fullLogFile));
    final BufferedReader logReader = new BufferedReader(new FileReader(logFile));

    List<String> fullLogLines = fullLogReader.lines().collect(Collectors.toList());
    List<String> logLines = logReader.lines().collect(Collectors.toList());

    assertThat(fullLogLines, is(not(empty())));
    assertThat(fullLogLines, hasItem("Test pass"));
    assertThat((int) fullLogLines.stream()
                                 .filter(l -> l.contains("SLICING") && l.contains("ZLIB"))
                                 .count(), is(equalTo(1)));

    assertThat(logLines, is(not(empty())));
    assertThat(logLines, hasSize(1));
    assertThat(logLines.get(0), containsString("SLICING"));
  }

  @Test
  void testSlice01() throws FileNotFoundException {

    final String outDir = ".testOut/testJar-out";
    SlicingStrategy strat =
      new Slicer4JSlicingStrategy("src/test/resources/testJars/testJar.jar",
                                  "../../slicer/Slicer4J",
                                  outDir);
    strat.instrumentJar();

    final JUnitTestCase testcase =
      new JUnitTestCase("divideZeroByNotZero",
                        "at.tugraz.ist.stracke.jsr.CalculatorTest",
                        Collections.singletonList(new AssertionStatement(
                          "Assert.assertEquals(0, calc.divide(0, 999))", 39,
                          39
                        )));

    strat.setTestCase(testcase);

    TestCaseSliceResult slice = strat.execute();

    final File fullLogFile = new File(outDir + "/slice.log");

    assertThat(fullLogFile.exists(), is(true));
    assertThat(slice, is(notNullValue()));
    assertThat(slice.testCase, is(equalTo(testcase)));
    assertThat(slice.getSliceLength(), is(equalTo(5)));

  }

  @Test
  void testSetTestCase() {
    final String outDir = ".testOut/testJar-out";
    SlicingStrategy strat =
      new Slicer4JSlicingStrategy("src/test/resources/testJars/testJar.jar",
                                  "../../slicer/Slicer4J",
                                  outDir);
    // returned instance must be the same after the method chain
    assertThat(strat, is(equalTo(strat.setTestCase(null))));
  }
}