package at.tugraz.ist.stracke.jsr.core.shared;

import at.tugraz.ist.stracke.jsr.core.parsing.statements.IStatement;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public abstract class TestCase {

  private final String name;
  private final String className;

  private final List<IStatement> assertions;

  private boolean passed;

  public TestCase(String name, String className, List<IStatement> assertions) {
    this.name = name;
    this.className = className;
    this.assertions = assertions != null ? assertions : new ArrayList<>();
  }

  public String getName() {
    return name;
  }

  public String getClassName() {
    return className;
  }

  public List<IStatement> getAssertions() {
    return assertions;
  }

  public String getFullName() {
    return String.format("%s:%s", this.className, this.name);
  }

  @Override
  public String toString() {
    return String.format("Testcase %s::%s has %d assertions: %s %s",
      this.className,
      this.name,
      this.assertions.size(),
      System.lineSeparator(),
      this.assertions.stream().map(Object::toString).collect(Collectors.joining(String.format(",%s ", System.lineSeparator()))));
  }

  @Override
  public int hashCode() {
    return (this.name != null ? this.name.hashCode() : 0) +
           (this.className != null ? this.className.hashCode() : 0) +
           this.assertions.hashCode() * this.assertions.size();
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof TestCase)) {
      return false;
    }
    TestCase t = (TestCase) obj;
    return this.name.equals(t.name) &&
           this.className.equals(t.className) &&
           this.assertions.equals(t.assertions);
  }

  public boolean isPassed() {
    return passed;
  }

  public void setPassed(boolean passed) {
    this.passed = passed;
  }
}
