package at.tugraz.ist.stracke.jsr.core.slicing.result;

import at.tugraz.ist.stracke.jsr.core.coverage.CoverageReport;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

class SliceEntryTest {

  @Test
  void testToString() {
    SliceEntry e = new SliceEntry("MyClass", 5, 5);

    assertThat(e.toString(), stringContainsInOrder("MyClass", "5"));
  }

  @Test
  void testToStringMultiline() {
    SliceEntry e = new SliceEntry("MyClass", 5, 6);

    assertThat(e.toString(), stringContainsInOrder("MyClass", "5", "6"));
  }

  @Test
  void testIsMultiline() {
    SliceEntry e1 = new SliceEntry("MyClass", 5, 5);
    SliceEntry e2 = new SliceEntry("MyClass", 5, 10);

    assertThat(e1.isMultiline(), is(false));
    assertThat(e2.isMultiline(), is(true));
  }

  @Test
  void testHashCode() {
    SliceEntry e = new SliceEntry("entry1", 1, 1);

    assertThat(e.hashCode(), is(equalTo("entry1".hashCode() + 1 + 1)));
  }

  @Test
  void testUnitEquals() {
    SliceEntry e1 = new SliceEntry("entry1", 1, 1);
    SliceEntry e2 = new SliceEntry("entry1", 1, 1);
    SliceEntry e3 = new SliceEntry("entry2", 1, 1);
    SliceEntry e4 = new SliceEntry("entry2", 1, 2);

    assertThat(e1.equals(e2), is(true));
    assertThat(e1.equals(e3), is(false));
    assertThat(e2.equals(e3), is(false));
    assertThat(e3.equals(e4), is(false));
  }

  @Test
  void testUnitToString() {
    SliceEntry e = new SliceEntry("entry1", 1, 1);

    assertThat(e.toString(), is(stringContainsInOrder("entry1")));
  }
}