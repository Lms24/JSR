package at.tugraz.ist.stracke.jsr.core.slicing.result;

public class SliceEntry{
  public final String className;
  public final int startLine;
  public final int endLine;

  public SliceEntry(String className, int startLine, int endLine) {
    this.className = className;
    this.startLine = startLine;
    this.endLine = endLine;
  }

  @Override
  public String toString() {
    return String.format("%s:%s",
      this.className,
      isMultiline() ? String.format("%d-%d", startLine, endLine) : startLine
    );
  }

  public boolean isMultiline() {
    return this.startLine != this.endLine;
  }
}
