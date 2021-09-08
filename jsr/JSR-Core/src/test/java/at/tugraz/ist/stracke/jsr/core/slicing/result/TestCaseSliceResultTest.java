package at.tugraz.ist.stracke.jsr.core.slicing.result;

import at.tugraz.ist.stracke.jsr.core.shared.TestCase;
import at.tugraz.ist.stracke.jsr.test.Mocks;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

class TestCaseSliceResultTest {

  @Test
  void testGetSliceCount() {
    TestCase tc = new Mocks.MockedTestCase();
    Set<SliceEntry> entries = new HashSet<>(Arrays.asList(
      new SliceEntry("MyClass", 1, 1),
      new SliceEntry("MyClass", 2, 2),
      new SliceEntry("MyClass", 3, 5)
    ));

    TestCaseSliceResult tsr = new TestCaseSliceResult(tc, entries);

    assertThat(tsr.slice.size(), is(equalTo(tsr.getSliceCount())));
    assertThat(tsr.getSliceCount(), is(equalTo(3)));
  }
}