package at.tugraz.ist.stracke.jsr.core.slicing.result;

import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class TestSuiteSliceResult {

  public TestSuiteSliceResult(@NonNull List<TestCaseSliceResult> testCaseSlices) {
    this.testCaseSlices = testCaseSlices;
  }

  public final List<TestCaseSliceResult> testCaseSlices;

  public Set<SliceEntry> getTestCaseSliceIntersection() {
    if (this.testCaseSlices.size() > 1) {
      return this.testCaseSlices.stream()
        .map(tcs -> tcs.slice)
        .skip(1)
        .collect(() -> new HashSet<>(this.testCaseSlices.get(0).slice),
                 Set::retainAll,
                 Set::retainAll);
    }
    return this.testCaseSlices.isEmpty() ?
      Collections.emptySet() : this.testCaseSlices.get(0).slice;
  }

  public Set<SliceEntry> getTestCaseSliceUnion() {
    return this.testCaseSlices.stream()
      .map(tcs -> tcs.slice)
      .collect(Collectors.toList())
      .stream()
      .flatMap(Set::stream)
      .collect(Collectors.toSet());
  }
}
