package at.tugraz.ist.stracke.jsr.core.parsing.utils;

import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.List;

public record TestSuite(@NonNull List<TestCase> testCases) {

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
