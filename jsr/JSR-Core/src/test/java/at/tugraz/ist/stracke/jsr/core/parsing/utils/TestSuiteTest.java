package at.tugraz.ist.stracke.jsr.core.parsing.utils;

import at.tugraz.ist.stracke.jsr.core.parsing.statements.AssertionStatement;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

class TestSuiteTest {

  private class MockedTestCase extends TestCase {

    public MockedTestCase() {
      super(Collections.singletonList(
        new AssertionStatement(1, 1)
      ), "MockedTestClass");
    }
  }

  @Test
  public void testInitializeNull() {
    TestSuite ts = new TestSuite(null);

    assertThat(ts, is(notNullValue()));
    assertThat(ts.getTestCases(), is(nullValue()));
    assertThat(ts.getNumberOfTestCases(), is(equalTo(0)));
  }

  @Test
  public void testInitializeNotNull() {
    List<TestCase> tcs = Arrays.asList(new MockedTestCase(), new MockedTestCase());
    TestSuite ts = new TestSuite(tcs);

    assertThat(ts, is(notNullValue()));
    assertThat(ts.getTestCases(), is(notNullValue()));
    assertThat(ts.getNumberOfTestCases(), is(equalTo(2)));
  }

  @Test
  public void testAddTestCase() {
    List<TestCase> tcs = new ArrayList<>();
    TestSuite ts = new TestSuite(tcs);

    assertThat(ts, is(notNullValue()));
    assertThat(ts.getTestCases(), is(notNullValue()));
    assertThat(ts.getNumberOfTestCases(), is(equalTo(0)));

    ts.addTestCase(new MockedTestCase());
    ts.addTestCase(new MockedTestCase());

    assertThat(ts.getNumberOfTestCases(), is(equalTo(2)));
  }

}