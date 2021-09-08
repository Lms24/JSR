package at.tugraz.ist.stracke.jsr.core.slicing.result;

import at.tugraz.ist.stracke.jsr.test.Mocks;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

class TestSuiteSliceResultTest {

  private TestSuiteSliceResult result;

  private final SliceEntry se1 = new SliceEntry("Class1", 1, 1);
  private final SliceEntry se2 = new SliceEntry("Class1", 2, 2);
  private final SliceEntry se3 = new SliceEntry("Class1", 3, 4);
  private final SliceEntry se20 = new SliceEntry("Class1", 20, 20);
  private final SliceEntry se22 = new SliceEntry("Class1", 22, 22);
  private final SliceEntry se23 = new SliceEntry("Class1", 23, 24);
  private final SliceEntry se50 = new SliceEntry("Class1", 50, 50);

  @BeforeEach
  void setUp() {

    TestCaseSliceResult tsr1 =
      new TestCaseSliceResult(new Mocks.MockedTestCase(),
                              new HashSet<>(Arrays.asList(se1, se2, se3)));

    TestCaseSliceResult tsr2 =
      new TestCaseSliceResult(new Mocks.MockedTestCase(),
                              new HashSet<>(Arrays.asList(se1, se20, se22, se23)));

    TestCaseSliceResult tsr3 =
      new TestCaseSliceResult(new Mocks.MockedTestCase(),
                              new HashSet<>(Arrays.asList(se1, se3, se50)));

    this.result = new TestSuiteSliceResult(Arrays.asList(tsr1, tsr2, tsr3));
  }

  @Test
  void testGetTestCaseSliceUnion() {
    assertThat(this.result.testCaseSlices.size(), is(equalTo(3)));
    assertThat(this.result.getTestCaseSliceUnion().size(), is(equalTo(7)));
    assertThat(this.result.getTestCaseSliceUnion().toArray(),
               is(arrayContainingInAnyOrder(se1, se2, se3, se20, se22, se23, se50)));
  }

  @Test
  void testGetTestCaseSliceIntersection() {
    assertThat(this.result.testCaseSlices.size(), is(equalTo(3)));
    assertThat(this.result.getTestCaseSliceIntersection().size(), is(equalTo(1)));
    assertThat(this.result.getTestCaseSliceIntersection().toArray(),
               is(arrayContainingInAnyOrder(se1)));
  }

  @Test
  void testEmptySliceSetOperations() {
    TestSuiteSliceResult tsr = new TestSuiteSliceResult(Collections.emptyList());

    assertThat(tsr.testCaseSlices.toArray(), is(emptyArray()));
    assertThat(tsr.getTestCaseSliceUnion().toArray(), is(emptyArray()));
    assertThat(tsr.getTestCaseSliceIntersection().toArray(), is(emptyArray()));
  }

  @Test
  void testSizeOneSliceSetOperations() {
    TestSuiteSliceResult tsr = new TestSuiteSliceResult(Collections.singletonList(
      new TestCaseSliceResult(new Mocks.MockedTestCase(), new HashSet<>(Arrays.asList(
        se1, se2, se3
      )))
    ));

    assertThat(tsr.testCaseSlices.toArray(), is(arrayWithSize(1)));
    assertThat(tsr.getTestCaseSliceUnion().toArray(), is(arrayWithSize(3)));
    assertThat(tsr.getTestCaseSliceIntersection().toArray(), is(arrayWithSize(3)));
  }
}