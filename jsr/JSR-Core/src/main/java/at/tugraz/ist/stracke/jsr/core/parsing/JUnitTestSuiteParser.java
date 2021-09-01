package at.tugraz.ist.stracke.jsr.core.parsing;

import at.tugraz.ist.stracke.jsr.core.parsing.strategies.ParsingStrategy;
import at.tugraz.ist.stracke.jsr.core.parsing.utils.TestSuite;

/**
 * Class JUnitTestSuiteParser parses a given JUnit test suite.
 *
 * @see TestSuiteParser
 * @since 1.0
 */
public class JUnitTestSuiteParser implements TestSuiteParser {

  private final ParsingStrategy parsingStrategy;

  private TestSuite result;

  public JUnitTestSuiteParser(ParsingStrategy parsingStrategy) {
    this.parsingStrategy = parsingStrategy;
  }

  @Override
  public void parse() {
    this.result = this.parsingStrategy.execute();
  }

  @Override
  public TestSuite getResult() {
    return this.result;
  }
}
