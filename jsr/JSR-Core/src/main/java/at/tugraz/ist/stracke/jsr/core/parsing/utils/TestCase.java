package at.tugraz.ist.stracke.jsr.core.parsing.utils;

import at.tugraz.ist.stracke.jsr.core.parsing.statements.IStatement;

import java.util.List;

public abstract class TestCase {

  private final String className;

  private final List<IStatement> assertions;

  public TestCase(List<IStatement> assertions, String className) {
    this.className = className;
    this.assertions = assertions;
  }

  public String getClassName() {
    return className;
  }

  public List<IStatement> getAssertions() {
    return assertions;
  }
}
