package at.tugraz.ist.stracke.jsr.core.parsing.utils;

import at.tugraz.ist.stracke.jsr.core.parsing.statements.IStatement;

import java.util.ArrayList;
import java.util.List;

public abstract class TestCase {

  private final String name;
  private final String className;

  private final List<IStatement> assertions;

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
}
