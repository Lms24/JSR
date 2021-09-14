package at.tugraz.ist.stracke.jsr.core.tsr.reducer;

import at.tugraz.ist.stracke.jsr.core.tsr.strategies.ReductionStrategy;

import java.nio.file.Path;

public class JUnitTestSuiteReducer implements TestSuiteReducer {

  private final ReductionStrategy tsrStrategy;

  public JUnitTestSuiteReducer(ReductionStrategy tsrStrategy) {
    this.tsrStrategy = tsrStrategy;
  }

  @Override
  public TestSuiteReducer reduce() {
    this.tsrStrategy.reduce();
    return this;
  }

  @Override
  public void generateReport() {

  }

  @Override
  public void serialize(Path srcTestDir) {

  }
}
