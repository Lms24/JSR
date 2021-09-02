package at.tugraz.ist.stracke.jsr.core.slicing.result;

public record SliceEntry(
  String className,
  int startLine,
  int endLine
) {

  @Override
  public String toString() {
    return "%s:%s".formatted(
      this.className,
      isMultiline() ? "%d-%d".formatted(startLine, endLine) : startLine
    );
  }

  public boolean isMultiline() {
    return this.startLine != this.endLine;
  }
}
