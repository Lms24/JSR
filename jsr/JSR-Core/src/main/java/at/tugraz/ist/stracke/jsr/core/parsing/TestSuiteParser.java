package at.tugraz.ist.stracke.jsr.core.parsing;

import at.tugraz.ist.stracke.jsr.core.parsing.strategies.ParsingStrategy;
import at.tugraz.ist.stracke.jsr.core.shared.TestSuite;

/**
 * A {@link TestSuiteParser} parses a given test suite and returns the
 * internal representation of the test suite - an object of type {@link TestSuite}
 *
 * @since 1.0
 */
public interface TestSuiteParser {

  /**
   * Commences parsing a given set of files (to be passed via the ctor)
   */
  void parse();

  /**
   * Returns the resulting {@link TestSuite} created while parsing the set of files
   *
   * @return the resulting {@link TestSuite} created while parsing the set of files
   * {@code null} if called before {@link #parse()} or an undefined error
   * occured while parsing or retrieving the result
   */
  TestSuite getResult();

  ParsingStrategy getParsingStrategy();
}
