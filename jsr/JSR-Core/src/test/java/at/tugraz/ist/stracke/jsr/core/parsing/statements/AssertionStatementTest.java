package at.tugraz.ist.stracke.jsr.core.parsing.statements;

import com.google.common.collect.Sets;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

class AssertionStatementTest {

  @Test
  public void testInterfaceFunctionality01() {
    IStatement stmt = new AssertionStatement("int i", 5, 5);

    assertThat(stmt, is(notNullValue()));
    assertThat(stmt.getStartLine(), is(equalTo(5)));
    assertThat(stmt.getEndLine(), is(equalTo(5)));
    assertThat(stmt.isMultilineStatement(), is(false));
  }

  @Test
  public void testInterfaceFunctionality02() {
    IStatement stmt = new AssertionStatement("int i", 5, 7);

    assertThat(stmt, is(notNullValue()));
    assertThat(stmt.getStartLine(), is(equalTo(5)));
    assertThat(stmt.getEndLine(), is(equalTo(7)));
    assertThat(stmt.isMultilineStatement(), is(true));
  }

  @Test
  public void testInvalidInitParams01() {
    // The case of negative numbers is handled via
    // the @Nonnegative ctor param Annotation
    IStatement stmt = new AssertionStatement("int i", 5, 3);

    assertThat(stmt, is(notNullValue()));
    assertThat(stmt.getStartLine(), is(equalTo(5)));
    assertThat(stmt.getEndLine(), is(equalTo(5)));
    assertThat(stmt.isMultilineStatement(), is(false));
  }

  @Test
  public void testAssertionStatementFunctionality01() {
    AssertionStatement stmt = new AssertionStatement("int i", 5, 5);

    assertThat(stmt.getRef(), is(empty()));
  }

  @Test
  public void testAssertionStatementFunctionality02() {
    Set<String> ref = Sets.newHashSet("var1", "var2");
    AssertionStatement stmt = new AssertionStatement("int i", 5, 5, ref);

    assertThat(stmt.getRef(), is(not(empty())));
    assertThat(stmt.getRef().size(), is(equalTo(2)));
    assertThat(stmt.getRef(), hasItems("var1", "var2"));
  }

}