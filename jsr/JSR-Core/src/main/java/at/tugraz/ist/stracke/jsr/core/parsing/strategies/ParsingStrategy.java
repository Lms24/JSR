package at.tugraz.ist.stracke.jsr.core.parsing.strategies;

import at.tugraz.ist.stracke.jsr.core.parsing.statements.Statement;
import at.tugraz.ist.stracke.jsr.core.shared.TestSuite;

import java.util.List;

/**
 * A {@link ParsingStrategy} encapsulates the employed parsing logic. It is used to
 * potentially use different parsing algorithms or approaches depending on e.g. the
 * test framework, the language, user preferences, etc.
 */
public interface ParsingStrategy {

  /**
   * Executes the parsing strategy and returns the internal representation of the
   * test suite - an object of type {@link TestSuite}
   *
   * @return the internal representation of the test suite or {@code null}
   * in case of an error
   */
  TestSuite parseTestSuite();

  /**
   * Executes the parsing strategy and returns a collection of executable
   * {@link Statement}. This can be executed to e.g. get executable statements for
   * checked coverage calculation
   *
   * @return a collection of executable {@link Statement}s.
   */
  List<Statement> parseStatements();

}
