package at.tugraz.ist.stracke.jsr.core.coverage;

import at.tugraz.ist.stracke.jsr.core.shared.TestCase;

import java.util.Map;
import java.util.Set;

public class CoverageReport {

  public final Set<Unit> allUnits;
  public final Set<Unit> coveredUnits;
  public final Map<TestCase, Set<Unit>> testCaseCoverageData;

  public CoverageReport(Set<Unit> allUnits,
                        Set<Unit> coveredUnits,
                        Map<TestCase, Set<Unit>> testCaseCoverageData) {
    this.allUnits = allUnits;
    this.coveredUnits = coveredUnits;
    this.testCaseCoverageData = testCaseCoverageData;
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
