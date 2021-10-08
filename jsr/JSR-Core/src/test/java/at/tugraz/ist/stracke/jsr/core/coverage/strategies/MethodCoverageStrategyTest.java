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

class MethodCoverageStrategyTest {
  @Test
  void testCalcOverallCoverage() {
    TestSuiteParser parser = new JUnitTestSuiteParser(
      new JavaParserParsingStrategy(Path.of("src/test/resources/smallProject/src/test/java/at/tugraz/ist/stracke/jsr"))
    );
    parser.parse();
    TestSuite ts = parser.getResult();

    final Path pathToJar = Path.of("src/test/resources/smallProject/build/libs/testJar.jar");
    final Path pathTOClasses = Path.of("src/test/resources/smallProject/build/classes/java/main");
    final Path pathToSources = Path.of("src/test/resources/smallProject/src/main/java");
    final Path pathToSlicer = Path.of("../../slicer/Slicer4J");
    final Path pathToOutDir = Path.of("build/jsr/jacocoTest2method");


    /* Test data sanity checks */
    assertThat("This path must exist", pathToJar.toFile().exists());
    assertThat("This path must exist", pathTOClasses.toFile().exists());
    assertThat("This path must exist", pathToSources.toFile().exists());
    assertThat("This path must exist", pathToSlicer.toFile().exists());

    MethodCoverageStrategy strat = new MethodCoverageStrategy(pathToJar,
                                                          pathTOClasses,
                                                          pathToSources,
                                                          pathToSlicer,
                                                          pathToOutDir,
                                                          "at.tugraz.ist.stracke.jsr");

    strat.setOriginalTestSuite(ts);

    // disable cleanup to check for presence of all files
    strat.performCleanup = false;

    CoverageReport report = strat.calculateOverallCoverage();

    assertThat(report, is(not(nullValue())));
    assertThat(report.allUnits, hasSize(11));
    assertThat(report.coveredUnits, hasSize(10));
    assertThat(report.testCaseCoverageData, is(aMapWithSize(14)));
    assertThat(report.testCaseCoverageData.size(), is(equalTo(ts.testCases.size())));
    assertThat(report.testCaseCoverageData.get(ts.testCases.get(0)), hasSize(2)); // CalculatorTest::testAdd()
    assertThat(report.testCaseCoverageData.get(ts.testCases.get(13)), hasSize(4)); // MessageTest::testGetters()

    assertThat("Cleanup is successful", strat.cleanup());
  }
}