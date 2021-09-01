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

  @Test
  public void testTestCaseParsing04() {
    var code = """
      public class TestClass {
        @Test
        public void testCase1() {
          MyObject o = new MyObject(100);
          o.prop1 = 101;
          o.setProp2(o.getProp2() == null ? 100 : o.getProp2());
          assertEquals(101, o.pro1);
          assertEquals(100, o.getProp2());
          assertThat(o, is(not(null)));
        }
      }""";
    var strat = new JavaParserParsingStrategy(code);

    var outcome = """
      Testcase TestClass::testCase1 has 3 assertions:\s
       assertEquals(101, o.pro1); ref {o.pro1, o} [7-7],
       assertEquals(100, o.getProp2()); ref {o} [8-8],
       assertThat(o, is(not(null))); ref {o} [9-9]""";

    TestSuite ts = strat.execute();

    assertThat(ts, is(notNullValue()));
    assertThat(ts.getTestCases(), not(empty()));
    assertThat(ts.getTestCases().get(0).getAssertions(), is(not(empty())));
    assertThat(ts.getTestCases().get(0).getAssertions().size(), is(equalTo(3)));
    assertThat(ts.getTestCases().get(0).toString(), is(equalTo(outcome)));
  }

  @Test
  public void testTestCaseParsing05() {
    var code = """
      public class TestClass {
        @Test
        public void testCase1() {
          MyObject o = new MyObject(100);
          MyOtherObject p = new MyOtherObject(o);
          o.prop1 = 101;
          o.setProp2(o.getProp2() == null ? 100 : o.getProp2());
          assertEquals(101, o.pro1);
          assertEquals(100, o.getProp2());
          assertThat(o, is(not(null)));
          assertTrue(p != null && p.o != null && p.getO() != null);
          assertEquals(p.o.prop1 == 101 && p.getO().prop1 == 101);
        }
      }""";
    var strat = new JavaParserParsingStrategy(code);

    var outcome = """
      Testcase TestClass::testCase1 has 5 assertions:\s
       assertEquals(101, o.pro1); ref {o.pro1, o} [8-8],
       assertEquals(100, o.getProp2()); ref {o} [9-9],
       assertThat(o, is(not(null))); ref {o} [10-10],
       assertTrue(p != null && p.o != null && p.getO() != null); ref {p, p.o} [11-11],
       assertEquals(p.o.prop1 == 101 && p.getO().prop1 == 101); ref {p.o, p, p.getO().prop1, p.o.prop1} [12-12]""";

    TestSuite ts = strat.execute();

    assertThat(ts, is(notNullValue()));
    assertThat(ts.getTestCases(), not(empty()));
    assertThat(ts.getTestCases().get(0).getAssertions(), is(not(empty())));
    assertThat(ts.getTestCases().get(0).getAssertions().size(), is(equalTo(5)));
    assertThat(ts.getTestCases().get(0).toString(), is(equalTo(outcome)));
  }

  @Test
  public void testTestCaseParsing06() {
    var code = """
      public class TestClass {
        @Test
        public void testCase1() {
          MyObject o = new MyObject(100);
          MyOtherObject p = new MyOtherObject(o);
          o.prop1 = 101;
          o.setProp2(o.getProp2() == null ? 100 : o.getProp2());
          assertEquals(101, o.pro1);
          assertEquals(100, o.getProp2());
          assertThat(o, is(not(null)));
          assertTrue(p != null && p.o != null && p.getO() != null);
          assertEquals(p.o.prop1 == 101 && p.getO().prop1 == 101);
        }
        
        @Test
        public void testCase2() {
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
      Testcase TestClass::testCase1 has 5 assertions:\s
       assertEquals(101, o.pro1); ref {o.pro1, o} [8-8],
       assertEquals(100, o.getProp2()); ref {o} [9-9],
       assertThat(o, is(not(null))); ref {o} [10-10],
       assertTrue(p != null && p.o != null && p.getO() != null); ref {p, p.o} [11-11],
       assertEquals(p.o.prop1 == 101 && p.getO().prop1 == 101); ref {p.o, p, p.getO().prop1, p.o.prop1} [12-12]""";

    var outcome2 = """
      Testcase TestClass::testCase2 has 2 assertions:\s
       assertEquals(i, 10); ref {i} [21-21],
       assertTrue(i < 100); ref {i} [22-22]""";

    TestSuite ts = strat.execute();

    assertThat(ts, is(notNullValue()));
    assertThat(ts.getTestCases(), not(empty()));
    assertThat(ts.getTestCases().get(0).getAssertions(), is(not(empty())));
    assertThat(ts.getTestCases().get(0).getAssertions().size(), is(equalTo(5)));
    assertThat(ts.getTestCases().get(0).toString(), is(equalTo(outcome)));
    assertThat(ts.getTestCases().get(1).getAssertions(), is(not(empty())));
    assertThat(ts.getTestCases().get(1).getAssertions().size(), is(equalTo(2)));
    assertThat(ts.getTestCases().get(1).toString(), is(equalTo(outcome2)));
  }


}