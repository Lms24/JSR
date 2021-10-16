package at.tugraz.ist.stracke.jsr.core.coverage;

import at.tugraz.ist.stracke.jsr.core.shared.TestCase;
import at.tugraz.ist.stracke.jsr.core.tsr.TSRTestCase;
import com.google.common.collect.ArrayTable;
import com.google.common.collect.Table;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

public class CoverageReport implements Serializable {

  public final String coverageType;
  public final Date createdAt;

  public final Set<Unit> allUnits;
  public final Set<Unit> coveredUnits;
  public final Map<TestCase, Set<Unit>> testCaseCoverageData;

  private List<TSRTestCase> sortedTestCases;

  public CoverageReport(String coverageType,
                        Set<Unit> allUnits,
                        Set<Unit> coveredUnits,
                        Map<TestCase, Set<Unit>> testCaseCoverageData) {
    this.coverageType = coverageType;
    this.allUnits = allUnits;
    this.coveredUnits = coveredUnits;
    this.testCaseCoverageData = testCaseCoverageData;
    this.createdAt = new Date();
  }

  public double getCoverageScore() {
    return this.allUnits.size() != 0 ?
           this.coveredUnits.size() / (double) this.allUnits.size() : 0;
  }

  public List<TSRTestCase> getSortedTestCases() {
    if (this.sortedTestCases == null) {
      this.sortedTestCases = testCaseCoverageData.keySet()
                                                 .stream()
                                                 .sorted(Comparator.comparing(TestCase::getFullName))
                                                 .map(TSRTestCase::new)
                                                 .collect(Collectors.toList());
    }
    return this.sortedTestCases;
  }

  public Table<TSRTestCase, Unit, Boolean> toTable(boolean includeUncoveredUnits) {
    List<TSRTestCase> rows = this.getSortedTestCases();

    Set<Unit> columnUnits = includeUncoveredUnits ? this.allUnits : this.coveredUnits;

    List<CoverageReport.Unit> columns = columnUnits.stream()
                                                   .sorted(Comparator.comparing(CoverageReport.Unit::toString))
                                                   .collect(Collectors.toList());

    Table<TSRTestCase, Unit, Boolean> table = ArrayTable.create(rows, columns);

    testCaseCoverageData.forEach((tc, units) -> units.forEach(u -> table.put(new TSRTestCase(tc), u, true)));

    return table;
  }

  public static class Unit implements Serializable{
    final public String name;
    final public int positionStart;
    final public int positionEnd;

    public Unit(@NonNull String name,
                int positionStart,
                int positionEnd) {
      this.name = name;
      this.positionStart = positionStart;
      this.positionEnd = positionEnd;
    }

    @Override
    public int hashCode() {
      return Objects.hash(this.name, this.positionStart/*, this.positionEnd*/);
    }

    @Override
    public boolean equals(Object obj) {
      if (!(obj instanceof Unit)) {
        return false;
      }
      Unit u = (Unit) obj;
      return this.name.equals(u.name) &&
             this.positionStart == u.positionStart;
      // Note: relaxing the equality criterion here, as sliced statements do not have the end line information
      /*&& this.positionEnd == u.positionEnd;*/
    }

    @Override
    public String toString() {
      return name;
    }
  }
}
