package at.tugraz.ist.stracke.jsr.core.parsing;

import at.tugraz.ist.stracke.jsr.core.parsing.strategies.ParsingStrategy;
import at.tugraz.ist.stracke.jsr.core.shared.TestSuite;
import at.tugraz.ist.stracke.jsr.test.Mocks;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

class JUnitTestSuiteParserTest {


  @Test
  public void testInit() {
    ParsingStrategy strategy = new Mocks.MockedParsingStrategy();
    TestSuiteParser tsp = new JUnitTestSuiteParser(strategy);
    assertThat(tsp, is(notNullValue()));
  }

  @Test
  public void testActivity() {
    ParsingStrategy strategy = new Mocks.MockedParsingStrategy();
    TestSuiteParser tsp = new JUnitTestSuiteParser(strategy);

    tsp.parse();
    TestSuite result = tsp.getResult();

    assertThat(result, is(notNullValue()));
    assertThat(result, is(equalTo(Mocks.emptyTestSuite)));
  }

}