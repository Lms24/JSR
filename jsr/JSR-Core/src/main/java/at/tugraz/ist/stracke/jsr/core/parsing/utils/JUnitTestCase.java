package at.tugraz.ist.stracke.jsr.core.parsing.utils;

import at.tugraz.ist.stracke.jsr.core.parsing.statements.IStatement;

import java.util.List;

public class JUnitTestCase extends TestCase {

  public JUnitTestCase(String name, String className, List<IStatement> assertions) {
    super(name, className, assertions);
  }
}
