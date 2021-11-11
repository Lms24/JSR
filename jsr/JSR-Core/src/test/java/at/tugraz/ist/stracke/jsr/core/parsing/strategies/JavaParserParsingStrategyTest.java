package at.tugraz.ist.stracke.jsr.core.parsing.strategies;

import at.tugraz.ist.stracke.jsr.core.parsing.statements.Statement;
import at.tugraz.ist.stracke.jsr.core.shared.TestSuite;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

class JavaParserParsingStrategyTest {

  @Test
  public void testBasicParsing01() {
    var code = "" +
               "public class TestClass {" +
               "}";
    var strat = new JavaParserParsingStrategy(Collections.singletonList(code));

    TestSuite ts = strat.parseTestSuite();

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
    var strat = new JavaParserParsingStrategy(Collections.singletonList(code));

    TestSuite ts = strat.parseTestSuite();

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
    var strat = new JavaParserParsingStrategy(Collections.singletonList(code));

    TestSuite ts = strat.parseTestSuite();

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
    String nl = System.lineSeparator();
    var code =
      "public class TestClass {" + nl +
      "  @Test" + nl +
      "  public void testCase1() {" + nl +
      "    int i = 0;" + nl +
      "    for (; i < 10; i++) {" + nl +
      "      System.out.println();" + nl +
      "    }" + nl +
      "    assertEquals(i, 10);" + nl +
      "    assertTrue(i < 100);" + nl +
      "  }" + nl +
      "}";
    var strat = new JavaParserParsingStrategy(Collections.singletonList(code));

    var outcome =
      "Testcase TestClass::testCase1 has 2 assertions: " + nl +
      " assertEquals(i, 10); ref {i} [8-8]," + nl +
      " assertTrue(i < 100); ref {i} [9-9]";

    TestSuite ts = strat.parseTestSuite();

    assertThat(ts, is(notNullValue()));
    assertThat(ts.getTestCases(), not(empty()));
    assertThat(ts.getTestCases().get(0).getAssertions(), is(not(empty())));
    assertThat(ts.getTestCases().get(0).getAssertions().size(), is(equalTo(2)));
    assertThat(ts.getTestCases().get(0).toString(), is(equalTo(outcome)));
  }

  @Test
  public void testTestCaseParsing04() {
    String nl = System.lineSeparator();
    var code = "public class TestClass {" + nl +
               "  @Test" + nl +
               "  public void testCase1() {" + nl +
               "    MyObject o = new MyObject(100);" + nl +
               "    o.prop1 = 101;" + nl +
               "    o.setProp2(o.getProp2() == null ? 100 : o.getProp2());" + nl +
               "    assertEquals(101, o.pro1);" + nl +
               "    assertEquals(100, o.getProp2());" + nl +
               "    assertThat(o, is(not(null)));" + nl +
               "  }" + nl +
               "}";
    var strat = new JavaParserParsingStrategy(Collections.singletonList(code));

    var outcome =
      "Testcase TestClass::testCase1 has 3 assertions: " + nl +
      " assertEquals(101, o.pro1); ref {o.pro1, o} [7-7]," + nl +
      " assertEquals(100, o.getProp2()); ref {o} [8-8]," + nl +
      " assertThat(o, is(not(null))); ref {o} [9-9]";

    TestSuite ts = strat.parseTestSuite();

    assertThat(ts, is(notNullValue()));
    assertThat(ts.getTestCases(), not(empty()));
    assertThat(ts.getTestCases().get(0).getAssertions(), is(not(empty())));
    assertThat(ts.getTestCases().get(0).getAssertions().size(), is(equalTo(3)));
    assertThat(ts.getTestCases().get(0).toString(), is(equalTo(outcome)));
  }

  @Test
  public void testTestCaseParsing05() {
    String nl = System.lineSeparator();

    var code =
      "public class TestClass {" + nl +
      "  @Test" + nl +
      "  public void testCase1() {" + nl +
      "    MyObject o = new MyObject(100);" + nl +
      "    MyOtherObject p = new MyOtherObject(o);" + nl +
      "    o.prop1 = 101;" + nl +
      "    o.setProp2(o.getProp2() == null ? 100 : o.getProp2());" + nl +
      "    assertEquals(101, o.pro1);" + nl +
      "    assertEquals(100, o.getProp2());" + nl +
      "    assertThat(o, is(not(null)));" + nl +
      "    assertTrue(p != null && p.o != null && p.getO() != null);" + nl +
      "    assertEquals(p.o.prop1 == 101 && p.getO().prop1 == 101);" + nl +
      "  }" + nl +
      "}";
    var strat = new JavaParserParsingStrategy(Collections.singletonList(code));

    var outcome =
      "Testcase TestClass::testCase1 has 5 assertions: " + nl +
      " assertEquals(101, o.pro1); ref {o.pro1, o} [8-8]," + nl +
      " assertEquals(100, o.getProp2()); ref {o} [9-9]," + nl +
      " assertThat(o, is(not(null))); ref {o} [10-10]," + nl +
      " assertTrue(p != null && p.o != null && p.getO() != null); ref {p, p.o} [11-11]," + nl +
      " assertEquals(p.o.prop1 == 101 && p.getO().prop1 == 101); ref {p.o, p, p.getO().prop1, p.o.prop1} [12-12]";

    TestSuite ts = strat.parseTestSuite();

    assertThat(ts, is(notNullValue()));
    assertThat(ts.getTestCases(), not(empty()));
    assertThat(ts.getTestCases().get(0).getAssertions(), is(not(empty())));
    assertThat(ts.getTestCases().get(0).getAssertions().size(), is(equalTo(5)));
    assertThat(ts.getTestCases().get(0).toString(), is(equalTo(outcome)));
  }

  @Test
  public void testTestCaseParsing06() {
    String nl = System.lineSeparator();

    var code =
      "public class TestClass {" + nl +
      "  @Test" + nl +
      "  public void testCase1() {" + nl +
      "    MyObject o = new MyObject(100);" + nl +
      "    MyOtherObject p = new MyOtherObject(o);" + nl +
      "    o.prop1 = 101;" + nl +
      "    o.setProp2(o.getProp2() == null ? 100 : o.getProp2());" + nl +
      "    assertEquals(101, o.pro1);" + nl +
      "    assertEquals(100, o.getProp2());" + nl +
      "    assertThat(o, is(not(null)));" + nl +
      "    assertTrue(p != null && p.o != null && p.getO() != null);" + nl +
      "    assertEquals(p.o.prop1 == 101 && p.getO().prop1 == 101);" + nl +
      "  }" + nl +
      "" + nl +
      "  @Test" + nl +
      "  public void testCase2() {" + nl +
      "    int i = 0;" + nl +
      "    for (; i < 10; i++) {" + nl +
      "      System.out.println();" + nl +
      "    }" + nl +
      "    assertEquals(i, 10);" + nl +
      "    assertTrue(i < 100);" + nl +
      "  }" + nl +
      "}";
    var strat = new JavaParserParsingStrategy(Collections.singletonList(code));

    var outcome =
      "Testcase TestClass::testCase1 has 5 assertions: " + nl +
      " assertEquals(101, o.pro1); ref {o.pro1, o} [8-8]," + nl +
      " assertEquals(100, o.getProp2()); ref {o} [9-9]," + nl +
      " assertThat(o, is(not(null))); ref {o} [10-10]," + nl +
      " assertTrue(p != null && p.o != null && p.getO() != null); ref {p, p.o} [11-11]," + nl +
      " assertEquals(p.o.prop1 == 101 && p.getO().prop1 == 101); ref {p.o, p, p.getO().prop1, p.o.prop1} [12-12]";

    var outcome2 =
      "Testcase TestClass::testCase2 has 2 assertions: " + nl +
      " assertEquals(i, 10); ref {i} [21-21]," + nl +
      " assertTrue(i < 100); ref {i} [22-22]";

    TestSuite ts = strat.parseTestSuite();

    assertThat(ts, is(notNullValue()));
    assertThat(ts.getTestCases(), not(empty()));
    assertThat(ts.getTestCases().get(0).getAssertions(), is(not(empty())));
    assertThat(ts.getTestCases().get(0).getAssertions().size(), is(equalTo(5)));
    assertThat(ts.getTestCases().get(0).toString(), is(equalTo(outcome)));
    assertThat(ts.getTestCases().get(1).getAssertions(), is(not(empty())));
    assertThat(ts.getTestCases().get(1).getAssertions().size(), is(equalTo(2)));
    assertThat(ts.getTestCases().get(1).toString(), is(equalTo(outcome2)));
  }

  @Test
  public void testStatementParsing01() {
    String nl = System.lineSeparator();
    var code = "package at.ist.test;" + nl +
               "public class TestClass {" + nl +
               "  int i = 666;" + nl +
               "  public void function1() {" + nl +
               "    MyObject o = new MyObject(100);" + nl +
               "    o.prop1 = 101;" + nl +
               "    o.setProp2(o.getProp2() == null ? 100 : o.getProp2());" + nl +
               "  }" + nl +
               "}";
    var strat = new JavaParserParsingStrategy(Collections.singletonList(code));

    List<Statement> stmts = strat.parseStatements();

    assertThat(stmts.size(), is(equalTo(3)));
  }

  @Test
  public void testStatementParsing02() {
    String nl = System.lineSeparator();
    var code = "package at.ist.test;" + nl +
               "public class TestClass {" + nl +
               "  int i = 666;" + nl +
               "  public void function1() {" + nl +
               "    MyObject o = new MyObject(100);" + nl +
               "    o.prop1 = 101;" + nl +
               "    this.executeFunction();" + nl +
               "    executeFunction2();" + nl +
               "    if (a == b) {" + nl +
               "      executeFunction3();" + nl +
               "    } else {" + nl +
               "      this.x = 999;" + nl +
               "    }" + nl +
               "    for(String s : strings) {" + nl +
               "      log(s);" + nl +
               "    }" + nl +
               "    collection.stream().foreach(i -> i.get());" + nl +
               "    " + nl +
               "    o.setProp2(o.getProp2() == null ? 100 : o.getProp2());" + nl +
               "  }" + nl +
               "}";
    var strat = new JavaParserParsingStrategy(Collections.singletonList(code));

    List<Statement> stmts = strat.parseStatements();

    assertThat(stmts.size(), is(equalTo(12)));
  }

  @Test
  public void testTestCaseParsing07() {
    var code = "" +
               "public class TestClass {" +
               "  @Test(expected = NullPointerException.class) public void testCaseWithExAnnotation() {} " +
               "  @Test public void testCase2() {} " +
               "  public void notATestCase() {} " +
               "  @OtherAnnotation public void testCase2() {} " +
               "}";
    var strat = new JavaParserParsingStrategy(Collections.singletonList(code));

    TestSuite ts = strat.parseTestSuite();

    assertThat(ts, is(notNullValue()));
    assertThat(ts.getTestCases(), not(empty()));
    assertThat(ts.getNumberOfTestCases(), is(equalTo(2)));
  }

  @Test
  public void testTestCaseParsing08() {
    var code = "" +
               "public class TestClass {" +
               "  @Test(expected = NullPointerException.class) public void testCaseWithExAnnotation() {} " +
               "  @Test @Ignore public void ignoredTestCase() {} " +
               "  @Test @Ignore(\"Reason\") public void ignoredTestCase1() {} " +
               "  @Test @Disabled public void ignoredTestCase2() {} " +
               "  @Test @Disabled(\"Reason\") public void ignoredTestCase3() {} " +
               "  public void notATestCase() {} " +
               "  @OtherAnnotation public void testCase2() {} " +
               "}";
    var strat = new JavaParserParsingStrategy(Collections.singletonList(code));

    TestSuite ts = strat.parseTestSuite();

    assertThat(ts, is(notNullValue()));
    assertThat(ts.getTestCases(), not(empty()));
    assertThat(ts.getNumberOfTestCases(), is(equalTo(1)));
  }

  @Test
  public void testTestCaseParsing09() {
    var code1 = "" +
                "public abstract class BaseTestClass {" +
                "  @Test public void abstractTC1() {int i;} " +
                "  @Test public void abstractTC2() {int a;} " +
                "}";
    var code2 = "" +
                "public class ConcreteTestClass extends BaseTestClass {" +
                "  @Test public void concreteTC1() {} " +
                "  @Test public void concreteTC2() {} " +
                "  @Override @Test public void abstractTC1() {} " +
                "}";
    var strat = new JavaParserParsingStrategy(Arrays.asList(code1, code2));

    TestSuite ts = strat.parseTestSuite();

    assertThat(ts, is(notNullValue()));
    assertThat(ts.getTestCases(), not(empty()));
    assertThat(ts.getNumberOfTestCases(), is(equalTo(4)));
    assertThat(ts.getTestCases().stream().allMatch(tc -> tc.getClassName().equals("ConcreteTestClass")), is(true));
  }

  @Test
  public void testTestCaseParsing10() {
    var code0 = "" +
                "public abstract class UnusedBaseClass {" +
                "  @Test public void unusedAbstractTC1() {int i;} " +
                "  @Test public void unusedAbstractTC2() {int a;} " +
                "}";
    var code1 = "" +
                "public abstract class BaseTestClass {" +
                "  @Test public void abstractTC1() {int i;} " +
                "  @Test public void abstractTC2() {int a;} " +
                "}";
    var code2 = "" +
                "public class ConcreteTestClass extends BaseTestClass {" +
                "  @Test public void concreteTC1() {} " +
                "  @Test public void concreteTC2() {} " +
                "  @Override @Test public void abstractTC1() {} " +
                "}";
    var code3 = "" +
                "public class AnotherConcreteTestClass extends BaseTestClass {" +
                "  @Test public void concreteTC1() {} " +
                "  @Test public void concreteTC2() {} " +
                "  @Test public void concreteTC3() {} " +
                "}";
    var code4 = "" +
                "public class YetAnotherConcreteTestClass extends BaseTestClass {" +
                "}";
    var code5 = "" +
                "public class FinalConcreteTestClass extends BaseTestClass {" +
                "  @Override @Test public void abstractTC1() {} " +
                "  @Override @Test public void abstractTC2() {} " +
                "}";
    var strat = new JavaParserParsingStrategy(Arrays.asList(code0, code1, code2, code3, code4, code5));

    TestSuite ts = strat.parseTestSuite();

    assertThat(ts, is(notNullValue()));
    assertThat(ts.getTestCases(), not(empty()));
    assertThat(ts.getNumberOfTestCases(), is(equalTo(13)));
    assertThat(ts.getTestCases().stream().filter(tc -> tc.getClassName().equals("ConcreteTestClass")).count(),
               is(equalTo(4L)));
    assertThat(ts.getTestCases().stream().filter(tc -> tc.getClassName().equals("AnotherConcreteTestClass")).count(),
               is(equalTo(5L)));
    assertThat(ts.getTestCases().stream().filter(tc -> tc.getClassName().equals("YetAnotherConcreteTestClass")).count(),
               is(equalTo(2L)));
    assertThat(ts.getTestCases().stream().filter(tc -> tc.getClassName().equals("FinalConcreteTestClass")).count(),
               is(equalTo(2L)));
  }

  @Test
  public void testTestCaseParsing11() {
    var code0 = "" +
                "public abstract class UnusedBaseClass {" +
                "  @Test public void unusedAbstractTC1() {int i;} " +
                "  @Test public void unusedAbstractTC2() {int a;} " +
                "}";
    var code1 = "" +
                "public abstract class BaseTestClass {" +
                "  @Test public void abstractTC1() {int i;} " +
                "  @Test public void abstractTC2() {int a;} " +
                "}";
    var code2 = "" +
                "public class ConcreteTestClass extends BaseTestClass {" +
                "  @Test public void concreteTC1() {} " +
                "  @Test public void concreteTC2() {} " +
                "  @Override @Test @Ignore public void abstractTC1() {} " +
                "  @Override @Test @Ignore public void abstractTC2() {} " +
                "}";
    var strat = new JavaParserParsingStrategy(Arrays.asList(code0, code1, code2));

    TestSuite ts = strat.parseTestSuite();

    assertThat(ts, is(notNullValue()));
    assertThat(ts.getTestCases(), not(empty()));
    assertThat(ts.getNumberOfTestCases(), is(equalTo(2)));
    assertThat(ts.getTestCases().stream().filter(tc -> tc.getClassName().equals("ConcreteTestClass")).count(),
               is(equalTo(2L)));
  }

  @Test
  public void testTestCaseParsing12() {
    var code1 = "" +
                "public abstract class BaseTestClass {" +
                "  @Test public void abstractTC1() {int i;} " +
                "  @Test @Ignore public void abstractTC2() {int a;} " +
                "}";
    var code2 = "" +
                "public class ConcreteTestClass extends BaseTestClass {" +
                "  @Test public void concreteTC1() {} " +
                "  @Test @Ignore public void concreteTC2() {} " +
                "  @Override @Test @Ignore public void abstractTC1() {} " +
                "  @Override @Test public void abstractTC2() {} " +
                "}";
    var strat = new JavaParserParsingStrategy(Arrays.asList(code1, code2));

    TestSuite ts = strat.parseTestSuite();

    assertThat(ts, is(notNullValue()));
    assertThat(ts.getTestCases(), not(empty()));
    assertThat(ts.getNumberOfTestCases(), is(equalTo(2)));
    assertThat(ts.getTestCases().stream().filter(tc -> tc.getClassName().equals("ConcreteTestClass")).count(),
               is(equalTo(2L)));
  }

  @Test
  public void testTestCaseParsing13() {
    var code1 = "" +
                "public abstract class BaseTestClass {" +
                "  @Test public void abstractTC1() {int i;} " +
                "  @Test @Ignore public void abstractTC2() {int a;} " +
                "}";
    var code2 = "" +
                "public class ConcreteTestClass extends BaseTestClass {" +
                "  @Test public void concreteTC1() {} " +
                "  @Test @Ignore public void concreteTC2() {} " +
                "  @Override @Test public void abstractTC2() {} " +
                "}";
    var strat = new JavaParserParsingStrategy(Arrays.asList(code1, code2));

    TestSuite ts = strat.parseTestSuite();

    assertThat(ts, is(notNullValue()));
    assertThat(ts.getTestCases(), not(empty()));
    assertThat(ts.getNumberOfTestCases(), is(equalTo(3)));
    assertThat(ts.getTestCases().stream().filter(tc -> tc.getClassName().equals("ConcreteTestClass")).count(),
               is(equalTo(3L)));
  }

  @Test
  public void testTestCaseParsing14() {
    var code1 = "" +
                "public abstract class BaseTestClass {" +
                "  @Test public void abstractTC1() {int i;} " +
                "  @Test public void abstractTC2() {int a;} " +
                "}";
    var code2 = "" +
                "public class ConcreteTestClass extends BaseTestClass {" +
                "  @Override @Test @Ignore public void abstractTC2() {} " +
                "  @Override @Test @Ignore public void abstractTC1() {} " +
                "}";
    var strat = new JavaParserParsingStrategy(Arrays.asList(code1, code2));

    TestSuite ts = strat.parseTestSuite();

    assertThat(ts, is(notNullValue()));
    assertThat(ts.getTestCases(), is(empty()));
    assertThat(ts.getNumberOfTestCases(), is(equalTo(0)));
  }

  @Test
  public void testTestCaseParsing15() {
    var code1 = "" +
                "package at.jsr.test;" +
                "public class ParentTestClass {" +
                "  @Test public void parentTC1() {int i;} " +
                "  @Test public void parentTC2() {int a;} " +
                "}";
    var code2 = "" +
                "package at.jsr.test;" +
                "public class ChildTestClass extends ParentTestClass {" +
                "}";
    var strat = new JavaParserParsingStrategy(Arrays.asList(code1, code2));

    TestSuite ts = strat.parseTestSuite();

    assertThat(ts, is(notNullValue()));
    assertThat(ts.getTestCases(), is(not(empty())));
    assertThat(ts.getNumberOfTestCases(), is(equalTo(4)));
  }

  @Test
  public void testTestCaseParsing16() {
    var code1 = "" +
                "package at.jsr.test;" +
                "public class ParentTestClass {" +
                "  @Test public void parentTC1() {int i;} " +
                "  @Test public void parentTC2() {int a;} " +
                "}";
    var code2 = "" +
                "package at.jsr.test;" +
                "public class ChildTestClass extends ParentTestClass {" +
                "  @Override @Test public void parentTC1() {int i;} " +
                "  @Override @Test public void parentTC2() {int a;} " +
                "}";
    var strat = new JavaParserParsingStrategy(Arrays.asList(code1, code2));

    TestSuite ts = strat.parseTestSuite();

    assertThat(ts, is(notNullValue()));
    assertThat(ts.getTestCases(), is(not(empty())));
    assertThat(ts.getNumberOfTestCases(), is(equalTo(4)));
  }

  @Test
  public void testTestCaseParsing17() {
    var code1 = "" +
                "package at.jsr.test;" +
                "public class ParentTestClass {" +
                "  @Test public void parentTC1() {int i;} " +
                "  @Test public void parentTC2() {int a;} " +
                "}";
    var code2 = "" +
                "package at.jsr.test;" +
                "public class ChildTestClass extends ParentTestClass {" +
                "  @Test public void childTC1() {int i;} " +
                "  @Test public void childTC2() {int a;} " +
                "}";
    var strat = new JavaParserParsingStrategy(Arrays.asList(code1, code2));

    TestSuite ts = strat.parseTestSuite();

    assertThat(ts, is(notNullValue()));
    assertThat(ts.getTestCases(), is(not(empty())));
    assertThat(ts.getNumberOfTestCases(), is(equalTo(6)));
  }

  @Test
  public void testTestCaseParsing18() {
    var code1 = "" +
                "package at.jsr.test;" +
                "public class ParentTestClass {" +
                "  public void parentTC1() {int i;} " +
                "  public void parentTC2() {int a;} " +
                "}";
    var code2 = "" +
                "package at.jsr.test;" +
                "public class ChildTestClass extends ParentTestClass {" +
                "  @Test public void childTC1() {int i;} " +
                "  @Test public void childTC2() {int a;} " +
                "}";
    var strat = new JavaParserParsingStrategy(Arrays.asList(code1, code2));

    TestSuite ts = strat.parseTestSuite();

    assertThat(ts, is(notNullValue()));
    assertThat(ts.getTestCases(), is(not(empty())));
    assertThat(ts.getNumberOfTestCases(), is(equalTo(2)));
  }

  @Test
  public void testTestCaseParsing19() {
    var code1 = "" +
                "package at.jsr.test;" +
                "public class ParentTestClass {" +
                "  @Test public void parentTC1() {int i;} " +
                "  @Test public void parentTC2() {int a;} " +
                "}";
    var code2 = "" +
                "package at.jsr.test;" +
                "public class ChildTestClass extends ParentTestClass {" +
                "  @Test public void childTC1() {int i;} " +
                "  @Test public void childTC2() {int a;} " +
                "}";
    var code3 = "" +
                "package at.jsr.test;" +
                "public class GrandChildTestClass extends ChildTestClass {" +
                "  @Test public void grandChildTC1() {int i;} " +
                "  @Test public void grandChildTC2() {int a;} " +
                "}";
    var strat = new JavaParserParsingStrategy(Arrays.asList(code1, code2, code3));

    TestSuite ts = strat.parseTestSuite();

    assertThat(ts, is(notNullValue()));
    assertThat(ts.getTestCases(), is(not(empty())));
    assertThat(ts.getNumberOfTestCases(), is(equalTo(12)));
  }

  @Test
  public void testTestCaseParsing20() {
    var code1 = "" +
                "package at.jsr.test;" +
                "public class ParentTestClass {" +
                "  @Test public void parentTC1() {int i;} " +
                "  @Test public void parentTC2() {int a;} " +
                "}";
    var code2 = "" +
                "package at.jsr.test;" +
                "public class ChildTestClass extends ParentTestClass {" +
                "  @Test public void childTC1() {int i;} " +
                "  @Test public void childTC2() {int a;} " +
                "}";
    var code3 = "" +
                "package at.jsr.test;" +
                "public class GrandChildTestClass extends ChildTestClass {" +
                "  @Test public void grandChildTC1() {int i;} " +
                "  @Test public void grandChildTC2() {int a;} " +
                "}";
    var strat = new JavaParserParsingStrategy(Arrays.asList(code3, code2, code1));

    TestSuite ts = strat.parseTestSuite();

    assertThat(ts, is(notNullValue()));
    assertThat(ts.getTestCases(), is(not(empty())));
    assertThat(ts.getNumberOfTestCases(), is(equalTo(12)));
  }

  @Test
  public void testTestCaseParsing21() {
    var code1 = "" +
                "package at.jsr.test;" +
                "public class ParentTestClass {" +
                "  @Test public void parentTC1() {int i;} " +
                "  @Test public void parentTC2() {int a;} " +
                "}";
    var code2 = "" +
                "package at.jsr.test;" +
                "public class ChildTestClass extends ParentTestClass {" +
                "  @Test public void childTC1() {int i;} " +
                "  @Test public void childTC2() {int a;} " +
                "}";
    var code3 = "" +
                "package at.jsr.test;" +
                "public class GrandChildTestClass extends ChildTestClass {" +
                "  @Test public void grandChildTC1() {int i;} " +
                "  @Test public void grandChildTC2() {int a;} " +
                "}";
    var strat = new JavaParserParsingStrategy(Arrays.asList(code2, code3, code1));

    TestSuite ts = strat.parseTestSuite();

    assertThat(ts, is(notNullValue()));
    assertThat(ts.getTestCases(), is(not(empty())));
    assertThat(ts.getNumberOfTestCases(), is(equalTo(12)));
  }

  @Test
  public void testTestCaseParsing22() {
    var code1 = "" +
                "package at.jsr.test;" +
                "public class ParentTestClass {" +
                "  @Test public void parentTC1() {int i;} " +
                "  @Test public void parentTC2() {int a;} " +
                "}";
    var code2 = "" +
                "package at.jsr.test;" +
                "public class ChildTestClass extends ParentTestClass {" +
                "  @Test public void childTC1() {int i;} " +
                "  @Test public void childTC2() {int a;} " +
                "}";
    var code3 = "" +
                "package at.jsr.test;" +
                "public class GrandChildTestClass extends ChildTestClass {" +
                "  @Test public void grandChildTC1() {int i;} " +
                "  @Test public void grandChildTC2() {int a;} " +
                "}";
    var code4 = "" +
                "package at.jsr.test;" +
                "public class GrandChildTestClass2 extends ChildTestClass {" +
                "  @Test public void grandChildTC3() {int i;} " +
                "  @Test public void grandChildTC4() {int a;} " +
                "}";
    var strat = new JavaParserParsingStrategy(Arrays.asList(code2, code3, code1, code4));

    TestSuite ts = strat.parseTestSuite();

    assertThat(ts, is(notNullValue()));
    assertThat(ts.getTestCases(), is(not(empty())));
    assertThat(ts.getNumberOfTestCases(), is(equalTo(18)));
  }

  @Test
  public void testTestCaseParsing23() {
    var code1 = "" +
                "package at.jsr.test;" +
                "public class ParentTestClass {" +
                "  @Test public void parentTC1() {int i;} " +
                "  @Test public void parentTC2() {int a;} " +
                "}";
    var code2 = "" +
                "package at.jsr.test;" +
                "public class ChildTestClass extends ParentTestClass {" +
                "  @Test public void childTC1() {int i;} " +
                "  @Test public void childTC2() {int a;} " +
                "}";
    var code3 = "" +
                "package at.jsr.test;" +
                "public class GrandChildTestClass extends ChildTestClass {" +
                "  @Test public void grandChildTC1() {int i;} " +
                "  @Test public void grandChildTC2() {int a;} " +
                "}";
    var code4 = "" +
                "package at.jsr.test;" +
                "public class GrandChildTestClass2 extends SomethingWeDontKnow {" +
                "  @Test public void grandChildTC3() {int i;} " +
                "  public void grandChildNoTC4() {int a;} " +
                "}";
    var strat = new JavaParserParsingStrategy(Arrays.asList(code2, code3, code1, code4));

    TestSuite ts = strat.parseTestSuite();

    assertThat(ts, is(notNullValue()));
    assertThat(ts.getTestCases(), is(not(empty())));
    assertThat(ts.getNumberOfTestCases(), is(equalTo(13)));
  }
}