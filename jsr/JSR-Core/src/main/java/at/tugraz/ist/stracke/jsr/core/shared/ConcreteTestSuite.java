package at.tugraz.ist.stracke.jsr.core.shared;

import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.List;

public class ConcreteTestSuite extends TestSuite {

  public String extendedClass;

  public ConcreteTestSuite(@NonNull List<TestCase> testCases) {
    super(testCases);
  }
}
