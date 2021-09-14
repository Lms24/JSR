package at.tugraz.ist.stracke.jsr.core.slicing.result;

import at.tugraz.ist.stracke.jsr.core.parsing.statements.Statement;

public class SliceEntry{
  public final String className;
  public final int startLine;
  public final int endLine;

  public SliceEntry(String className, int startLine, int endLine) {
    this.className = className;
    this.startLine = startLine;
    this.endLine = endLine;
  }

  public SliceEntry(String className, int startLine) {
    this.className = className;
    this.startLine = startLine;
    this.endLine = startLine;
  }

  @Override
  public String toString() {
    return String.format("%s:%s",
      this.className,
      isMultiline() ? String.format("%d-%d", startLine, endLine) : startLine
    );
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof SliceEntry)) {
      return false;
    }
    SliceEntry se = (SliceEntry) obj;
    return this.className.equals(se.className) &&
           this.startLine == se.startLine &&
           this.endLine == se.endLine;
  }

  @Override
  public int hashCode() {
    return this.className.hashCode() + this.startLine + this.endLine;
  }

  public boolean isMultiline() {
    return this.startLine != this.endLine;
  }

  public Statement toStatement() {
    Statement s = new Statement("Slice", this.startLine, this.endLine);
    s.setClassName(this.className);
    return s;
  }
}
