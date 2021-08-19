package at.tugraz.ist.stracke.jsr.core.parsing.utils;

import java.util.List;

public record TestSuite(List<TestCase> testCases) {

  public List<TestCase> getTestCases() {
    return testCases;
  }

  public void addTestCase(TestCase tc) {
    if (tc != null) {
      this.testCases.add(tc);
    }
  }

  public int getNumberOfTestCases() {
    return this.testCases != null ? this.testCases.size() : 0;
  }
}
