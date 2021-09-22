package at.tugraz.ist.stracke.jsr.core.coverage;

import at.tugraz.ist.stracke.jsr.core.tsr.TSRTestCase;
import at.tugraz.ist.stracke.jsr.test.TSRData;
import com.google.common.collect.Table;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

class CoverageReportTest {

  @Test
  void testGetCoverageScore() {
    final Set<CoverageReport.Unit> allUnits = new HashSet<>(Arrays.asList(
      new CoverageReport.Unit("u1", 1, 1),
      new CoverageReport.Unit("u2", 2, 2),
      new CoverageReport.Unit("u3", 3, 3),
      new CoverageReport.Unit("u4", 4, 4),
      new CoverageReport.Unit("u5", 5, 5)
    ));
    final Set<CoverageReport.Unit> coveredUnits = new HashSet<>(Arrays.asList(
      new CoverageReport.Unit("u1", 1, 1),
      new CoverageReport.Unit("u4", 4, 4),
      new CoverageReport.Unit("u5", 5, 5)
    ));

    CoverageReport r = new CoverageReport(allUnits, coveredUnits, Collections.emptyMap());

    assertThat(r.allUnits.toArray(), is(arrayWithSize(allUnits.size())));
    assertThat(r.coveredUnits.toArray(), is(arrayWithSize(coveredUnits.size())));
    assertThat(r.getCoverageScore(), is(equalTo(0.6)));
  }

  @Test
  void testToTable() {
    CoverageReport r = TSRData.simpleCoverageReport;

    Table<TSRTestCase, CoverageReport.Unit, Boolean> table = r.toTable(true);

    assertThat(table.rowKeySet(), hasSize(4));
    assertThat(table.columnKeySet(), hasSize(6));
    assertThat(table.get(new TSRTestCase(TSRData.t1), TSRData.s1), is(true));
    assertThat(table.get(new TSRTestCase(TSRData.t3), TSRData.s3), anyOf(nullValue(), equalTo(true)));
  }

  @Test
  void testToTable2() {
    CoverageReport r = TSRData.simpleCoverageReport;

    Table<TSRTestCase, CoverageReport.Unit, Boolean> table = r.toTable(false);

    assertThat(table.rowKeySet(), hasSize(4));
    assertThat(table.columnKeySet(), hasSize(3));
    assertThat(table.get(new TSRTestCase(TSRData.t1), TSRData.s1), is(true));
    assertThat(table.get(new TSRTestCase(TSRData.t3), TSRData.s3), anyOf(nullValue(), equalTo(true)));
  }

  @Test
  void testUnitHashCode() {
    CoverageReport.Unit u = new CoverageReport.Unit("unit1", 1, 1);

    assertThat(u.hashCode(), is(equalTo(Objects.hash("unit1", 1, 1))));
  }

  @Test
  void testUnitEquals() {
    CoverageReport.Unit u1 = new CoverageReport.Unit("unit1", 1, 1);
    CoverageReport.Unit u2 = new CoverageReport.Unit("unit1", 1, 1);
    CoverageReport.Unit u3 = new CoverageReport.Unit("unit2", 1, 1);
    CoverageReport.Unit u4 = new CoverageReport.Unit("unit2", 1, 2);

    assertThat(u1.equals(u2), is(true));
    assertThat(u1.equals(u3), is(false));
    assertThat(u2.equals(u3), is(false));
    assertThat(u3.equals(u4), is(false));
  }

  @Test
  void testUnitToString() {
    CoverageReport.Unit u = new CoverageReport.Unit("unit1", 1, 1);

    assertThat(u.toString(), is(stringContainsInOrder("unit1")));
  }
}