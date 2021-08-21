package at.tugraz.ist.stracke.jsr.core.parsing.statements;

import org.checkerframework.checker.nullness.qual.NonNull;

import javax.annotation.Nonnegative;
import java.util.HashSet;
import java.util.Set;

public class AssertionStatement extends Statement {

  private final Set<String> ref;

  public AssertionStatement(@Nonnegative int startLine,
                            @Nonnegative int endLine) {
    super(startLine, endLine);
    this.ref = new HashSet<>();
  }

  public AssertionStatement(@Nonnegative int startLine,
                            @Nonnegative int endLine,
                            @NonNull Set<String> ref) {
    super(startLine, endLine);
    this.ref = ref;
  }

  public Set<String> getRef() {
    return ref;
  }
}
