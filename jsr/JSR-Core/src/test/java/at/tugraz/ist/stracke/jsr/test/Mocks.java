package at.tugraz.ist.stracke.jsr.test;

import at.tugraz.ist.stracke.jsr.core.parsing.statements.AssertionStatement;
import at.tugraz.ist.stracke.jsr.core.parsing.strategies.ParsingStrategy;
import at.tugraz.ist.stracke.jsr.core.shared.TestCase;
import at.tugraz.ist.stracke.jsr.core.shared.TestSuite;
import at.tugraz.ist.stracke.jsr.core.slicing.result.TestCaseSliceResult;
import at.tugraz.ist.stracke.jsr.core.slicing.strategies.SlicingStrategy;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

public class Mocks {
  public static final TestSuite emptyTestSuite = new TestSuite(new ArrayList<>());

  /**
   * {@link TestSuite} with 3 {@link TestCase}s.
   */
  public static final TestSuite nonEmptyTestSuite = new TestSuite(Arrays.asList(
    new MockedTestCase(),
    new MockedTestCase(),
    new MockedTestCase()
  ));

  public static class MockedTestCase extends TestCase {
    public MockedTestCase() {
      super("MockedTestCase", "MockedTestClass", Collections.singletonList(
        new AssertionStatement("int i", 1, 1)
      ));
    }
  }

  public static class MockedParsingStrategy implements ParsingStrategy {
    @Override
    public TestSuite execute() {
      return emptyTestSuite;
    }
  }

  public static class MockedSlicingStrategy implements SlicingStrategy {

    private TestCase tc;

    @Override
    public TestCaseSliceResult execute() {
      return new TestCaseSliceResult(tc, Collections.emptySet());
    }

    @Override
    public SlicingStrategy setTestCase(TestCase testCase) {
      this.tc = testCase;
      return this;
    }
  }
}
