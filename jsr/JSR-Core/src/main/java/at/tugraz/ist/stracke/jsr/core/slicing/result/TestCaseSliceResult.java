package at.tugraz.ist.stracke.jsr.core.slicing.result;

import at.tugraz.ist.stracke.jsr.core.shared.TestCase;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.Set;

public class TestCaseSliceResult extends SliceEntry {

  public final TestCase testCase;
  public final Set<SliceEntry> slice;

  public TestCaseSliceResult(@NonNull TestCase testCase,
                             @NonNull Set<SliceEntry> slice) {
    this.testCase = testCase;
    this.slice = slice;
  }

  public int getSliceLength() {
    return this.slice.size();
  }
}
