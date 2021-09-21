package at.tugraz.ist.stracke.jsr.test;

import at.tugraz.ist.stracke.jsr.core.coverage.CoverageReport;
import at.tugraz.ist.stracke.jsr.core.shared.TestCase;
import at.tugraz.ist.stracke.jsr.core.shared.TestSuite;
import at.tugraz.ist.stracke.jsr.core.tsr.ReducedTestSuite;
import at.tugraz.ist.stracke.jsr.core.tsr.TSRTestCase;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class TSRData {

  public static final TestCase t1 = new TSRTestCase("t1", "t1");
  public static final TestCase t2 = new TSRTestCase("t2", "t2");
  public static final TestCase t3 = new TSRTestCase("t3", "t3");
  public static final TestCase t4 = new TSRTestCase("t4", "t4");
  public static final TestCase t5 = new TSRTestCase("t5", "t5");

  static final CoverageReport.Unit s1 = new CoverageReport.Unit("s1", 1, 1);
  static final CoverageReport.Unit s2 = new CoverageReport.Unit("s2", 2, 2);
  static final CoverageReport.Unit s3 = new CoverageReport.Unit("s3", 3, 3);
  static final CoverageReport.Unit s4 = new CoverageReport.Unit("s4", 4, 4);
  static final CoverageReport.Unit s5 = new CoverageReport.Unit("s5", 5, 5);
  static final CoverageReport.Unit s6 = new CoverageReport.Unit("s6", 6, 6);

  public static final TestSuite smallOriginalTS = new TestSuite(List.of(t1, t2, t3, t4, t5));
  public static final CoverageReport smallCoverageReport = new CoverageReport(
    Set.of(s1, s2, s3 ,s4, s5, s6),
    Set.of(s1, s2, s3, s4, s5, s6),
    Map.of(t1, Set.of(s1, s3, s5),
           t2, Set.of(s2, s4, s5),
           t3, Set.of(s2, s4, s5),
           t4, Set.of(s2, s3, s6),
           t5, Set.of(s1, s4, s5))
  );

  public static final TestSuite simpleOriginalTS = new TestSuite(List.of(t1, t2, t3, t4));
  public static final CoverageReport simpleCoverageReport = new CoverageReport(
    Set.of(s1, s2, s3 ,s4, s5, s6),
    Set.of(s1, s2, s3),
    Map.of(t1, Set.of(s1, s3, s2),
           t2, Set.of(s2, s1),
           t3, Set.of(s1),
           t4, Set.of(s1, s3, s2))
  );

  public static final CoverageReport simpleCoverageReport2 = new CoverageReport(
    Set.of(s1, s2, s3 ,s4, s5, s6),
    Set.of(s1, s2, s3, s4),
    Map.of(t1, Set.of( s1,  s2,  s3/**/),
           t2, Set.of( s1,  s2 /**//**/),
           t3, Set.of(/**//**/  s3,  s4),
           t4, Set.of( s1,  s2, s3,  s4))
  );

  public static final ReducedTestSuite simpleReducedTesSuite = new ReducedTestSuite(
    List.of(t1, t3, t4),
    List.of(t2, t5)
  );
}
