package at.tugraz.ist.stracke.jsr.core.slicing;

import at.tugraz.ist.stracke.jsr.core.slicing.result.TestSuiteSliceResult;
import at.tugraz.ist.stracke.jsr.test.Mocks;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

class JUnitTestSuiteSlicerTest {

  @Test
  void testSlice() {
    TestSuiteSlicer slicer = new JUnitTestSuiteSlicer(new Mocks.MockedSlicingStrategy(),
                                                           Mocks.nonEmptyTestSuite);
    final TestSuiteSliceResult slice = slicer.slice();

    assertThat(slice, is(notNullValue()));
    assertThat(slice.testCaseSlices.toArray(), is(arrayWithSize(3)));
    assertThat(slice.testCaseSlices.stream().allMatch(tcs -> tcs.getSliceCount() == 0), is(true));
  }

  @Test
  void testGetResult() {
    TestSuiteSlicer slicer = new JUnitTestSuiteSlicer(new Mocks.MockedSlicingStrategy(),
                                                           Mocks.nonEmptyTestSuite);
    assertThat(slicer.getResult(), is(nullValue()));

    final TestSuiteSliceResult slice = slicer.slice();

    assertThat(slicer.getResult(), is(notNullValue()));
    assertThat(slicer.getResult(), is(equalTo(slice)));
  }
}