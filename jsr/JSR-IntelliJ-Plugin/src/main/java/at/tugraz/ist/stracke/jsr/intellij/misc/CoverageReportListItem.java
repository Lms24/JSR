package at.tugraz.ist.stracke.jsr.intellij.misc;

import at.tugraz.ist.stracke.jsr.core.coverage.CoverageReport;

import java.text.SimpleDateFormat;
import java.util.Objects;

public class CoverageReportListItem {
  public final CoverageReport coverageReport;

  public CoverageReportListItem(CoverageReport coverageReport) {
    this.coverageReport = coverageReport;
  }

  @Override
  public String toString() {
    SimpleDateFormat df = new SimpleDateFormat("dd.MM.yy HH:mm");
    String strCreatedAt = df.format(coverageReport.createdAt);
    return String.format("%s Coverage, %s", coverageReport.coverageType, strCreatedAt);
  }

  /* Simplifying equals and hashcode here as the date should be unique enough */
  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    CoverageReportListItem that = (CoverageReportListItem) o;
    return Objects.equals(coverageReport.createdAt, that.coverageReport.createdAt);
  }

  @Override
  public int hashCode() {
    return Objects.hash(coverageReport.createdAt);
  }
}
