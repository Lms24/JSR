package at.tugraz.ist.stracke.jsr.cli.candidates;

import java.util.ArrayList;
import java.util.Arrays;

public class AlgorithmCandidates extends ArrayList<String> {
  public static final String ALG_GREEDY_HGS = "greedyHGS";
  public static final  String ALG_GENETIC = "genetic";

  AlgorithmCandidates() {
    super(Arrays.asList(ALG_GREEDY_HGS, ALG_GENETIC));
  }

}
