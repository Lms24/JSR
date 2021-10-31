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

  public static final TestCase t1 = new TSRTestCase("t1", "t1", true);
  public static final TestCase t2 = new TSRTestCase("t2", "t2", true);
  public static final TestCase t3 = new TSRTestCase("t3", "t3", true);
  public static final TestCase t4 = new TSRTestCase("t4", "t4", true);
  public static final TestCase t5 = new TSRTestCase("t5", "t5", true);
  public static final TestCase t6 = new TSRTestCase("t6", "t6", true);
  public static final TestCase t7 = new TSRTestCase("t7", "t7", true);
  public static final TestCase t8 = new TSRTestCase("t8", "t8", true);
  public static final TestCase t9 = new TSRTestCase("t9", "t9", true);
  public static final TestCase t10 = new TSRTestCase("t10", "t10", true);
  public static final TestCase tf1 = new TSRTestCase("tf1", "tf1", false);
  public static final TestCase tf2 = new TSRTestCase("tf2", "tf2", false);
  public static final TestCase tf3 = new TSRTestCase("tf3", "tf3", false);

  public static final CoverageReport.Unit s1 = new CoverageReport.Unit("s1", 1, 1);
  public static final CoverageReport.Unit s2 = new CoverageReport.Unit("s2", 2, 2);
  public static final CoverageReport.Unit s3 = new CoverageReport.Unit("s3", 3, 3);
  public static final CoverageReport.Unit s4 = new CoverageReport.Unit("s4", 4, 4);
  public static final CoverageReport.Unit s5 = new CoverageReport.Unit("s5", 5, 5);
  public static final CoverageReport.Unit s6 = new CoverageReport.Unit("s6", 6, 6);
  public static final CoverageReport.Unit s7 = new CoverageReport.Unit("s7", 6, 6);
  public static final CoverageReport.Unit s8 = new CoverageReport.Unit("s8", 6, 6);
  public static final CoverageReport.Unit s9 = new CoverageReport.Unit("s9", 6, 6);
  public static final CoverageReport.Unit s10 = new CoverageReport.Unit("s10", 6, 6);

  public static final TestSuite smallOriginalTS = new TestSuite(List.of(t1, t2, t3, t4, t5));
  public static final CoverageReport smallCoverageReport = new CoverageReport(
    "Abstract",
    Set.of(s1, s2, s3 ,s4, s5, s6),
    Set.of(s1, s2, s3, s4, s5, s6),
    Map.of(t1, Set.of(s1, s3, s5),
           t2, Set.of(s2, s4, s5),
           t3, Set.of(s2, s4, s5),
           t4, Set.of(s2, s3, s6),
           t5, Set.of(s1, s4, s5))
  );
  public static final CoverageReport delGreedyCoverageReport = new CoverageReport(
    "Abstract",
    Set.of(s1, s2, s3 ,s4, s5, s6),
    Set.of(s1, s2, s3, s4, s5, s6),
    Map.of(t1, Set.of(s1, s2, s3),
           t2, Set.of(s1, s4),
           t3, Set.of(s2, s5),
           t4, Set.of(s3, s6),
           t5, Set.of(s5))
  );

  public static final TestSuite simpleOriginalTS = new TestSuite(List.of(t1, t2, t3, t4));
  public static final CoverageReport simpleCoverageReport = new CoverageReport(
    "Abstract",
    Set.of(s1, s2, s3 ,s4, s5, s6),
    Set.of(s1, s2, s3),
    Map.of(t1, Set.of(s1, s3, s2),
           t2, Set.of(s2, s1),
           t3, Set.of(s1),
           t4, Set.of(s1, s3, s2))
  );

  public static final CoverageReport simpleCoverageReport2 = new CoverageReport(
    "Abstract",
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

  public static final TestSuite sflTestSuite = new TestSuite(List.of(t1, t2, t3, t4, t5, tf1, tf2, tf3));

  public static final CoverageReport sflPassCoverageReport = new CoverageReport(
    "Abstract",
    Set.of(s1, s2, s3 ,s4, s5, s6),
    Set.of(s1, s2, s3, s5),
    Map.of(t1, Set.of(s1, s3, s2),
           t2, Set.of(s2, s1),
           t3, Set.of(s1, s5),
           t4, Set.of(s1, s3, s2),
           t5, Set.of(s5),
           tf1, Set.of(),
           tf2, Set.of(),
           tf3, Set.of())
  );

  public static final CoverageReport sflFailCoverageReport = new CoverageReport(
    "Abstract",
    Set.of(s1, s2, s3 ,s4, s5, s6),
    Set.of(s1, s2, s4, s5, s6),
    Map.of(t1, Set.of(),
           t2, Set.of(),
           t3, Set.of(),
           t4, Set.of(),
           t5, Set.of(),
           tf1, Set.of(s6, s5, s2),
           tf2, Set.of(s6, s4, s2),
           tf3, Set.of(s6, s4, s1))
  );

  public static final TestSuite forceGreedyOriginalTS =
    new TestSuite(List.of(t2, t3, t4, t5, t6, t7, t8, t9));
  public static final CoverageReport forceGreedyCoverageReport = new CoverageReport(
    "Abstract",
    Set.of(s1, s2, s3, s4, s5, s6, s7, s8, s9),
    Set.of(s1, s2, s3, s4, s5, s6, s7, s8, s9),
    Map.of(t2, Set.of(s5, s8, s9),
           t3, Set.of(s1, s2, s4, s5, s7),
           t4, Set.of(s1, s3, s4, s5, s6),
           t5, Set.of(s1, s2, s5, s8),
           t6, Set.of(s2, s6, s8),
           t7, Set.of(s3, s4, s7, s8),
           t8, Set.of(s1, s9),
           t9, Set.of(s2, s3, s7, s9))
  );
}
