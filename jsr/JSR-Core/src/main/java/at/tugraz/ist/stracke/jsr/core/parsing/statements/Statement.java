package at.tugraz.ist.stracke.jsr.core.parsing.statements;

import org.checkerframework.checker.nullness.qual.NonNull;

public class Statement implements IStatement{
  protected final String text;
  protected final int startLine;
  protected final int endLine;

  public Statement(@NonNull String text, int startLine, int endLine) {
    this.text = text;
    this.startLine = Math.max(startLine, 0);
    this.endLine = Math.max(startLine, endLine);
  }

  @Override
  public int getStartLine() {
    return startLine;
  }

  @Override
  public int getEndLine() {
    return endLine;
  }

  @Override
  public boolean isMultilineStatement() {
    return this.startLine != this.endLine;
  }

  public String getText() {
    return text;
  }

  @Override
  public String toString() {
    return "%s [%d-%d]".formatted(this.text, this.startLine, this.endLine);
  }
}
