package at.tugraz.ist.stracke.jsr.core.shared;

import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.List;

public class TestSuite {

  public final List<TestCase> testCases;

  public TestSuite(@NonNull List<TestCase> testCases) {
    this.testCases = testCases;
  }

  public List<TestCase> getTestCases() {
    return testCases;
  }

  public void addTestCase(TestCase tc) {
    if (tc != null) {
      this.testCases.add(tc);
    }
  }

  public int getNumberOfTestCases() {
    return this.testCases.size();
  }
}
