package at.tugraz.ist.stracke.jsr.cli.candidates;

import java.util.ArrayList;
import java.util.Arrays;

public class CoverageCandidates extends ArrayList<String> {
  public static final String COV_CHECKED = "checked";
  public static final  String COV_LINE = "line";
  public static final String COV_METHOD = "method";

  CoverageCandidates() {
    super(Arrays.asList(COV_CHECKED, COV_LINE, COV_METHOD));
  }
}
