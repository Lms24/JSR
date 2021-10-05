package at.tugraz.ist.stracke.jsr.core.coverage.strategies;

import at.tugraz.ist.stracke.jsr.core.coverage.CoverageReport;
import at.tugraz.ist.stracke.jsr.core.parsing.JUnitTestSuiteParser;
import at.tugraz.ist.stracke.jsr.core.parsing.TestSuiteParser;
import at.tugraz.ist.stracke.jsr.core.parsing.strategies.JavaParserParsingStrategy;
import at.tugraz.ist.stracke.jsr.core.shared.TestSuite;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

class JaCoCoCoverageStrategyTest {

  @Test
  void testCalcOverallCoverage() {
    TestSuiteParser parser = new JUnitTestSuiteParser(
      new JavaParserParsingStrategy(Path.of("src/test/resources/smallProject/src/test/java/at/tugraz/ist/stracke/jsr"))
    );
    parser.parse();
    TestSuite ts = parser.getResult();

    CoverageStrategy strat = new JaCoCoCoverageStrategy(
      Path.of("src/test/resources/smallProject/build/libs/testJar.jar"),
      Path.of("src/test/resources/smallProject/build/classes/java/main"),
      Path.of("src/test/resources/smallProject/src/main/java/at/tugraz/ist/stracke/jsr"),
      Path.of("../../slicer/Slicer4J"),
      Path.of("build/jsr/jacocoTest1")
    );

    strat.setOriginalTestSuite(ts);

    CoverageReport report = strat.calculateOverallCoverage();

    assertThat(report, is(not(nullValue())));
  }
}