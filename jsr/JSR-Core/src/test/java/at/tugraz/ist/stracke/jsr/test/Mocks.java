package at.tugraz.ist.stracke.jsr.test;

import at.tugraz.ist.stracke.jsr.core.coverage.CoverageReport;
import at.tugraz.ist.stracke.jsr.core.parsing.TestSuiteParser;
import at.tugraz.ist.stracke.jsr.core.parsing.statements.AssertionStatement;
import at.tugraz.ist.stracke.jsr.core.parsing.statements.Statement;
import at.tugraz.ist.stracke.jsr.core.parsing.strategies.ParsingStrategy;
import at.tugraz.ist.stracke.jsr.core.shared.TestCase;
import at.tugraz.ist.stracke.jsr.core.shared.TestSuite;
import at.tugraz.ist.stracke.jsr.core.slicing.TestSuiteSlicer;
import at.tugraz.ist.stracke.jsr.core.slicing.result.SliceEntry;
import at.tugraz.ist.stracke.jsr.core.slicing.result.TestCaseSliceResult;
import at.tugraz.ist.stracke.jsr.core.slicing.result.TestSuiteSliceResult;
import at.tugraz.ist.stracke.jsr.core.slicing.strategies.SlicingStrategy;
import com.google.common.collect.Maps;

import java.util.*;

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

  public static final TestSuite tsrOriginalTS = new TestSuite(Arrays.asList(
    new MockedTestCase("t1"),
    new MockedTestCase("t2"),
    new MockedTestCase("t3"),
    new MockedTestCase("t4"),
    new MockedTestCase("t5")
  ));

  public static final CoverageReport smallCoverageReport = new CoverageReport(
    Set.of(
      new CoverageReport.Unit("s1", 1, 1),
      new CoverageReport.Unit("s2", 2, 2),
      new CoverageReport.Unit("s3", 3, 3),
      new CoverageReport.Unit("s4", 4, 4),
      new CoverageReport.Unit("s5", 5, 5),
      new CoverageReport.Unit("s6", 6, 6)
    ),
    Set.of(
      new CoverageReport.Unit("s1", 1, 1),
      new CoverageReport.Unit("s2", 2, 2),
      new CoverageReport.Unit("s3", 3, 3),
      new CoverageReport.Unit("s4", 4, 4),
      new CoverageReport.Unit("s5", 5, 5),
      new CoverageReport.Unit("s6", 6, 6)
    ),
    Map.of(new MockedTestCase("t1"), Set.of(new CoverageReport.Unit("s1", 1,1),
                                            new CoverageReport.Unit("s3", 3, 3),
                                            new CoverageReport.Unit("s5", 5, 5)),
           new MockedTestCase("t2"), Set.of(new CoverageReport.Unit("s2", 2,2),
                                            new CoverageReport.Unit("s4", 4, 4),
                                            new CoverageReport.Unit("s5", 5, 5)),
           new MockedTestCase("t3"), Set.of(new CoverageReport.Unit("s2", 2,2),
                                            new CoverageReport.Unit("s4", 4, 4),
                                            new CoverageReport.Unit("s5", 5, 5)),
           new MockedTestCase("t4"), Set.of(new CoverageReport.Unit("s2", 2,2),
                                            new CoverageReport.Unit("s3", 3, 3),
                                            new CoverageReport.Unit("s6", 6, 6)),
           new MockedTestCase("t5"), Set.of(new CoverageReport.Unit("s1", 1,1),
                                            new CoverageReport.Unit("s4", 4, 4),
                                            new CoverageReport.Unit("s5", 5, 5))
    )
  );

  public static class MockedTestCase extends TestCase {
    public MockedTestCase() {
      super("MockedTestCase", "MockedTestClass", Collections.singletonList(
        new AssertionStatement("int i", 1, 1)
      ));
    }
    public MockedTestCase(String name) {
      super(name, name, Collections.emptyList());
    }
  }

  public static class MockedParsingStrategy implements ParsingStrategy {
    @Override
    public TestSuite parseTestSuite() {
      return emptyTestSuite;
    }

    @Override
    public List<Statement> parseStatements() {
      return Collections.emptyList();
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

  public static class MockedTestSuiteParser implements TestSuiteParser {

    private TestSuite result;

    public MockedTestSuiteParser() {
      this.result = nonEmptyTestSuite;
    }

    @Override
    public void parse() {
    }

    @Override
    public TestSuite getResult() {
      return nonEmptyTestSuite;
    }

    @Override
    public ParsingStrategy getParsingStrategy() {
      return new ParsingStrategy() {
        @Override
        public TestSuite parseTestSuite() {
          return nonEmptyTestSuite;
        }

        @Override
        public List<Statement> parseStatements() {
          return Arrays.asList(
            new Statement("s1", 1, 1, "TestClass"),
            new Statement("s2", 2, 2, "TestClass"),
            new Statement("s3", 3, 3, "TestClass"),
            new Statement("s4", 4, 4, "TestClass"),
            new Statement("s5", 5, 5, "TestClass")
          );
        }
      };
    }
  }

  public static class MockedTestSuiteSlicer implements TestSuiteSlicer {

    private TestSuiteSliceResult res;

    public MockedTestSuiteSlicer() {
      this.res = new TestSuiteSliceResult(Arrays.asList(
        new TestCaseSliceResult(new MockedTestCase(), new HashSet<>(Arrays.asList(
          new SliceEntry("TestClass", 1, 1),
          new SliceEntry("TestClass", 2, 2),
          new SliceEntry("TestClass", 3, 3)
        ))),
        new TestCaseSliceResult(new MockedTestCase(), new HashSet<>(Arrays.asList(
          new SliceEntry("TestClass", 1, 1),
          new SliceEntry("TestClass", 4, 4)
        )))
      ));
    }

    @Override
    public TestSuiteSliceResult slice() {
      return this.res;
    }

    @Override
    public TestSuiteSliceResult getResult() {
      return this.res;
    }
  }
}
