package at.tugraz.ist.stracke.jsr.core.shared;

import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class TestSuite {

  public final List<TestCase> testCases;
  public final Set<String> testClasses;

  public TestSuite(@NonNull List<TestCase> testCases) {
    this.testCases = testCases;
    this.testClasses = this.testCases.stream()
                                     .map(TestCase::getClassName)
                                     .collect(Collectors.toSet());
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

  public int getNumberOfTestClasses() {
    return this.testClasses.size();
  }

  public int getNumberOfAssertions() {
    return this.testCases.stream()
                         .mapToInt(tc -> tc.getAssertions().size())
                         .sum();
  }
}
