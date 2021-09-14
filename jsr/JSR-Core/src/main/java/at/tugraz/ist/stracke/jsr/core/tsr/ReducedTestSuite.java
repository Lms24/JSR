package at.tugraz.ist.stracke.jsr.core.tsr;

import at.tugraz.ist.stracke.jsr.core.shared.TestCase;
import at.tugraz.ist.stracke.jsr.core.shared.TestSuite;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.List;

public class ReducedTestSuite extends TestSuite {

  public final List<TestCase> removedTestCases;

  public ReducedTestSuite(@NonNull List<TestCase> testCases,
                          @NonNull List<TestCase> removedTestCases) {
    super(testCases);
    this.removedTestCases = removedTestCases;
  }

  public int getNumberOfRemovedTestCases() {
    return  removedTestCases.size();
  }
}
