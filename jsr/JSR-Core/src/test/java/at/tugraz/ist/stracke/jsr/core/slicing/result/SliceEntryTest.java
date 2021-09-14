package at.tugraz.ist.stracke.jsr.core.slicing.result;

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
}