package at.tugraz.ist.stracke.jsr.core.parsing.statements;

public abstract class Statement implements IStatement{
  private int startLine;
  private int endLine;

  public Statement(int startLine, int endLine) {
    this.startLine = startLine;
    this.endLine = endLine;
  }

  @Override
  public int getStartLine() {
    return startLine;
  }

  @Override
  public int getEndLine() {
    return endLine;
  }
}
