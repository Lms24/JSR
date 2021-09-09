package at.tugraz.ist.stracke.jsr.core.slicing.strategies;

import at.tugraz.ist.stracke.jsr.core.shared.JUnitTestCase;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
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
  void testExecuteAndTrace01()  {

    final String outDir = ".testOut/testJar-out";
    SlicingStrategy strat =
      new Slicer4JSlicingStrategy("src/test/resources/testJars/testJar.jar",
                                  "../../slicer/Slicer4J",
                                  outDir);

    strat.setTestCase(new JUnitTestCase("divideZeroByNotZero",
                                        "at.tugraz.ist.stracke.jsr.CalculatorTest",
                                        Collections.emptyList()));
    strat.execute();
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