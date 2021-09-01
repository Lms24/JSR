package at.tugraz.ist.stracke.jsr.core.parsing;

import at.tugraz.ist.stracke.jsr.core.parsing.strategies.ParsingStrategy;
import at.tugraz.ist.stracke.jsr.core.parsing.utils.TestSuite;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

class JUnitTestSuiteParserTest {

  private static final TestSuite resultingTestSuite = new TestSuite(new ArrayList<>());

  private static class MockedParsingStrategy implements ParsingStrategy {
    @Override
    public TestSuite execute() {
      return resultingTestSuite;
    }
  }

  @Test
  public void testInit() {
    ParsingStrategy strategy = new MockedParsingStrategy();
    TestSuiteParser tsp = new JUnitTestSuiteParser(strategy);
    assertThat(tsp, is(notNullValue()));
  }

  @Test
  public void testActivity() {
    ParsingStrategy strategy = new MockedParsingStrategy();
    TestSuiteParser tsp = new JUnitTestSuiteParser(strategy);

    tsp.parse();
    TestSuite result = tsp.getResult();

    assertThat(result, is(notNullValue()));
    assertThat(result, is(equalTo(resultingTestSuite)));
  }

}