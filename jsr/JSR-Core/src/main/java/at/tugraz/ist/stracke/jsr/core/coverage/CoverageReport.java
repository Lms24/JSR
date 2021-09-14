package at.tugraz.ist.stracke.jsr.core.coverage;

import java.util.Set;

public class CoverageReport {

  final Set<Unit> allUnits;
  final Set<Unit> coveredUnits;

  public CoverageReport(Set<Unit> allUnits, Set<Unit> coveredUnits) {
    this.allUnits = allUnits;
    this.coveredUnits = coveredUnits;
  }

  public double getCoverageScore() {
    return this.allUnits.size() != 0 ?
           this.coveredUnits.size() / (double) this.allUnits.size() : 0;
  }

  public static class Unit {
    final public String name;
    final public int positionStart;
    final public int positionEnd;

    public Unit(String name, int positionStart, int positionEnd) {
      this.name = name;
      this.positionStart = positionStart;
      this.positionEnd = positionEnd;
    }
  }
}
