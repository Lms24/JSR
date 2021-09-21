package at.tugraz.ist.stracke.jsr.core.tsr;

import org.checkerframework.checker.nullness.qual.NonNull;

public class TSRReport {
  private final ReducedTestSuite reducedTestSuite;

  private long nrOriginalTestCases;
  private long nrRetainedTestCases;
  private long nrRemovedTestCases;

  public TSRReport(@NonNull ReducedTestSuite reducedTestSuite) {
    this.reducedTestSuite = reducedTestSuite;
    this.nrOriginalTestCases = reducedTestSuite.getNumberOfTestCases() + reducedTestSuite.getNumberOfRemovedTestCases();
    this.nrRetainedTestCases = reducedTestSuite.getNumberOfTestCases();
    this.nrRemovedTestCases = reducedTestSuite.getNumberOfRemovedTestCases();
  }

  public @NonNull String toXMLString() {
    StringBuilder sb = new StringBuilder()
      .append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>").append(System.lineSeparator())
      .append("<report>").append(System.lineSeparator())
      .append("  <meta>").append(System.lineSeparator())
      .append(String.format("    <nrOriginalTestCases>%d</nrOriginalTestCases>", nrOriginalTestCases)).append(
        System.lineSeparator())
      .append(String.format("    <nrRetainedTestCases>%d</nrRetainedTestCases>", nrOriginalTestCases)).append(
        System.lineSeparator())
      .append(String.format("    <nrRemovedTestCases>%d</nrRemovedTestCases>", nrOriginalTestCases)).append(
        System.lineSeparator())
      .append("  </meta>").append(System.lineSeparator());

    sb.append("  <retainedTestCases>").append(System.lineSeparator());
    this.reducedTestSuite.testCases
      .forEach(tc ->
                 sb.append("    <testCase>").append(System.lineSeparator())
                   .append("      <name>").append(tc.getName()).append("</name>").append(System.lineSeparator())
                   .append("      <className>").append(tc.getClassName()).append("</className>").append(System.lineSeparator())
                   .append("    </testCase>").append(System.lineSeparator())
      );
    sb.append("  </retainedTestCases>").append(System.lineSeparator());

    sb.append("  <removedTestCases>").append(System.lineSeparator());
    this.reducedTestSuite.removedTestCases
      .forEach(tc ->
                 sb.append("    <testCase>").append(System.lineSeparator())
                   .append("      <name>").append(tc.getName()).append("</name>").append(System.lineSeparator())
                   .append("      <className>").append(tc.getClassName()).append("</className>").append(System.lineSeparator())
                   .append("    </testCase>").append(System.lineSeparator())
      );
    sb.append("  </removedTestCases>").append(System.lineSeparator());

    sb.append("</report>").append(System.lineSeparator());

    return sb.toString();
  }


}
