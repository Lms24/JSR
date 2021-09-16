package at.tugraz.ist.stracke.jsr.core.parsing.statements;

import org.checkerframework.checker.nullness.qual.NonNull;

import javax.annotation.Nonnegative;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class AssertionStatement extends Statement {

  private final Set<String> ref;

  public AssertionStatement(@NonNull String text,
                            @Nonnegative int startLine,
                            @Nonnegative int endLine) {
    super(text, startLine, endLine);
    this.ref = new HashSet<>();
  }

  public AssertionStatement(@NonNull String text,
                            @Nonnegative int startLine,
                            @Nonnegative int endLine,
                            @NonNull Set<String> ref) {
    super(text, startLine, endLine);
    this.ref = ref;
  }

  public Set<String> getRef() {
    return ref;
  }

  @Override
  public String toString() {
    return String.format("%s ref {%s} [%d-%d]",
      this.text,
      String.join(", ", this.ref),
      this.startLine,
      this.endLine
    );
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    if (!super.equals(o)) return false;
    AssertionStatement that = (AssertionStatement) o;
    return ref.equals(that.ref);
  }

  @Override
  public int hashCode() {
    return Objects.hash(ref) + super.hashCode();
  }
}
