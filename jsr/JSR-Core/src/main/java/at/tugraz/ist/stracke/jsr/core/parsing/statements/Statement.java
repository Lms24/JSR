package at.tugraz.ist.stracke.jsr.core.parsing.statements;

import at.tugraz.ist.stracke.jsr.core.coverage.CoverageReport;
import org.checkerframework.checker.nullness.qual.NonNull;

public class Statement implements IStatement{
  protected final String text;
  protected final int startLine;
  protected final int endLine;

  /**
   * The name of the class, the statement belongs to
   */
  protected String className;

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

  public String getClassName() {
    return className;
  }

  public void setClassName(String className) {
    this.className = className;
  }

  @Override
  public String toString() {
    return String.format("%s [%d-%d]", this.text, this.startLine, this.endLine);
  }

  public CoverageReport.Unit toUnit() {
    return new CoverageReport.Unit(String.format("%s:%d", this.className,
                                                 this.startLine),
                                   this.startLine,
                                   this.endLine);
  }
}
