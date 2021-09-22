package at.tugraz.ist.stracke.jsr.core.parsing;

import at.tugraz.ist.stracke.jsr.core.parsing.strategies.ParsingStrategy;
import at.tugraz.ist.stracke.jsr.core.shared.TestSuite;

/**
 * Class JUnitTestSuiteParser parses a given JUnit test suite.
 *
 * @see TestSuiteParser
 * @since 1.0
 */
public class JUnitTestSuiteParser implements TestSuiteParser {

  private ParsingStrategy parsingStrategy;

  private TestSuite result;

  public JUnitTestSuiteParser(ParsingStrategy parsingStrategy) {
    this.parsingStrategy = parsingStrategy;
  }

  @Override
  public void parse() {
    this.result = this.parsingStrategy.parseTestSuite();
  }

  @Override
  public TestSuite getResult() {
    return this.result;
  }

  @Override
  public ParsingStrategy getParsingStrategy() {
    return this.parsingStrategy;
  }

  @Override
  public void setParsingStrategy(ParsingStrategy strategy) {
    this.parsingStrategy = strategy;
  }
}
