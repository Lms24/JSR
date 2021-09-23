package at.tugraz.ist.stracke.jsr.core.tsr;

import at.tugraz.ist.stracke.jsr.core.shared.TestCase;

import java.util.Collections;

public class TSRTestCase extends TestCase {
  public TSRTestCase(TestCase tc) {
    super(tc.getName(), tc.getClassName(), tc.getAssertions());
  }

  /**
   * Ctor to instantiate a TSR TC w/o assertions
   */
  public TSRTestCase(String name, String className) {
    super(name, className, Collections.emptyList());
  }

  public TSRTestCase(String name, String className, boolean passed) {
    super(name, className, Collections.emptyList());
    super.setPassed(passed);
  }

  @Override
  public String toString() {
    return super.getFullName();
  }
}
