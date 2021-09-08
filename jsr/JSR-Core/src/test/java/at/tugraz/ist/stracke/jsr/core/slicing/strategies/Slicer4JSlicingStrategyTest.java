package at.tugraz.ist.stracke.jsr.core.slicing.strategies;

import org.junit.jupiter.api.Test;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
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
    assertThat((int) reader.lines().count(), is(greaterThan(0)));
    assertThat((int) reader.lines().filter(l -> l.contains("Instrumentation done")).count(), is(greaterThan(0)));
  }

  @Test
  void testSetTestCase() {
    SlicingStrategy strat = new Slicer4JSlicingStrategy("", "", "");

    // returned instance must be the same after the method chain
    assertThat(strat, is(equalTo(strat.setTestCase(null))));
  }
}