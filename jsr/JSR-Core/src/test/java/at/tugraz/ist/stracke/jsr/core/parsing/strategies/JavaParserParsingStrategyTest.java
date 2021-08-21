package at.tugraz.ist.stracke.jsr.core.parsing.strategies;

import at.tugraz.ist.stracke.jsr.core.parsing.utils.TestSuite;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

class JavaParserParsingStrategyTest {

  @Test
  public void testBasicParsing01() {
    var code = "" +
      "public class TestClass {" +
      "}";
    var strat = new JavaParserParsingStrategy(code);

    TestSuite ts = strat.execute();

    assertThat(ts, is(notNullValue()));
    assertThat(ts.getTestCases(), is(empty()));
    assertThat(ts.getNumberOfTestCases(), is(equalTo(0)));
  }

  @Test
  public void testTestCaseParsing01() {
    var code = "" +
      "public class TestClass {" +
      "  @Test public void testCase1() {} " +
      "  @Test public void testCase2() {} " +
      "  public void notATestCase() {} " +
      "  @OtherAnnotation public void testCase2() {} " +
      "}";
    var strat = new JavaParserParsingStrategy(code);

    TestSuite ts = strat.execute();

    assertThat(ts, is(notNullValue()));
    assertThat(ts.getTestCases(), not(empty()));
    assertThat(ts.getNumberOfTestCases(), is(equalTo(2)));
  }

  @Test
  public void testTestCaseParsing02() {
    var code = "" +
      "public class TestClass {" +
      "  @Test \n" +
      "  public void testCase1() {} " +
      "  @Test public void testCase2() {} " +
      "  public void notATestCase() {} " +
      "  @OtherAnnotation public void testCase2() {} " +
      "}";
    var strat = new JavaParserParsingStrategy(code);

    TestSuite ts = strat.execute();

    assertThat(ts, is(notNullValue()));
    assertThat(ts.getTestCases(), not(empty()));
    assertThat(ts.getNumberOfTestCases(), is(equalTo(2)));
    assertThat(ts.getTestCases().get(0).getName(), is(equalTo("testCase1")));
    assertThat(ts.getTestCases().get(1).getName(), is(equalTo("testCase2")));
    assertThat(ts.getTestCases().get(0).getAssertions(), is(empty()));
    assertThat(ts.getTestCases().get(1).getAssertions(), is(empty()));
  }

  @Test
  public void testTestCaseParsing03() {
    var code = """
      public class TestClass {
        @Test
        public void testCase1() {
          int i = 0;
          for (; i < 10; i++) {
            System.out.println();
          }
          assertEquals(i, 10);
          assertTrue(i < 100);
        }
      }""";
    var strat = new JavaParserParsingStrategy(code);

    var outcome = """
      Testcase TestClass::testCase1 has 2 assertions:\s
       assertEquals(i, 10); ref {i} [8-8],
       assertTrue(i < 100); ref {i} [9-9]""";

    TestSuite ts = strat.execute();

    assertThat(ts, is(notNullValue()));
    assertThat(ts.getTestCases(), not(empty()));
    assertThat(ts.getTestCases().get(0).getAssertions(), is(not(empty())));
    assertThat(ts.getTestCases().get(0).getAssertions().size(), is(equalTo(2)));
    assertThat(ts.getTestCases().get(0).toString(), is(equalTo(outcome)));
  }

}