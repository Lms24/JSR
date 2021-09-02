package at.tugraz.ist.stracke.jsr.core.slicing.result;

import at.tugraz.ist.stracke.jsr.core.shared.TestCase;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.Set;

public record TestCaseSliceResult(
  @NonNull TestCase testCase,
  @NonNull Set<SliceEntry> slice
) {

  public int getSliceCount() {
    return this.slice.size();
  }
}
