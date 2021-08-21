package at.tugraz.ist.stracke.jsr.core.parsing.utils;

import at.tugraz.ist.stracke.jsr.core.parsing.statements.AssertionStatement;
import at.tugraz.ist.stracke.jsr.core.parsing.statements.IStatement;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

class JUnitTestCaseTest {

  @Test
  public void testEmptyInitialization() {
    TestCase tc = new JUnitTestCase("tc1", "NullClass", null);

    assertThat(tc.getClassName(), equalTo("NullClass"));
    assertThat(tc.getName(), is(equalTo("tc1")));
    assertThat(tc.getAssertions(), is(empty()));
  }

  @Test
  public void testLoadedInitialization() {
    IStatement ass1 = new AssertionStatement(1, 1);
    IStatement ass2 = new AssertionStatement(4, 5);
    IStatement ass3 = new AssertionStatement(10, 12);
    List<IStatement> stmts = Arrays.asList(ass1, ass2, ass3);

    TestCase tc = new JUnitTestCase("tc1", "LoadedClass", stmts);

    assertThat(tc.getClassName(), equalTo("LoadedClass"));
    assertThat(tc.getAssertions(), notNullValue());
    assertThat(tc.getAssertions(), hasItems(ass1, ass2, ass3));
    assertThat(tc.getAssertions().size(), is(equalTo(3)));
  }
}