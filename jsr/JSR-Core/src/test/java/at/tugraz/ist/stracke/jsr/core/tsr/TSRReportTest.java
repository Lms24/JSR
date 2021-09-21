package at.tugraz.ist.stracke.jsr.core.tsr;

import at.tugraz.ist.stracke.jsr.test.TSRData;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

class TSRReportTest {

  @Test
  void testToXMLString() {
    TSRReport rep = new TSRReport(TSRData.simpleReducedTesSuite);
    String xml = rep.toXMLString();

    assertThat(xml, is(equalTo("<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + System.lineSeparator()  +
                               "<report>" + System.lineSeparator()  +
                               "  <meta>" + System.lineSeparator()  +
                               "    <nrOriginalTestCases>5</nrOriginalTestCases>" + System.lineSeparator()  +
                               "    <nrRetainedTestCases>3</nrRetainedTestCases>" + System.lineSeparator()  +
                               "    <nrRemovedTestCases>2</nrRemovedTestCases>" + System.lineSeparator()  +
                               "  </meta>" + System.lineSeparator()  +
                               "  <retainedTestCases>" + System.lineSeparator()  +
                               "    <testCase>" + System.lineSeparator()  +
                               "      <name>t1</name>" + System.lineSeparator()  +
                               "      <className>t1</className>" + System.lineSeparator()  +
                               "    </testCase>" + System.lineSeparator()  +
                               "    <testCase>" + System.lineSeparator()  +
                               "      <name>t3</name>" + System.lineSeparator()  +
                               "      <className>t3</className>" + System.lineSeparator()  +
                               "    </testCase>" + System.lineSeparator()  +
                               "    <testCase>" + System.lineSeparator()  +
                               "      <name>t4</name>" + System.lineSeparator()  +
                               "      <className>t4</className>" + System.lineSeparator()  +
                               "    </testCase>" + System.lineSeparator()  +
                               "  </retainedTestCases>" + System.lineSeparator()  +
                               "  <removedTestCases>" + System.lineSeparator()  +
                               "    <testCase>" + System.lineSeparator()  +
                               "      <name>t2</name>" + System.lineSeparator()  +
                               "      <className>t2</className>" + System.lineSeparator()  +
                               "    </testCase>" + System.lineSeparator()  +
                               "    <testCase>" + System.lineSeparator()  +
                               "      <name>t5</name>" + System.lineSeparator()  +
                               "      <className>t5</className>" + System.lineSeparator()  +
                               "    </testCase>" + System.lineSeparator()  +
                               "  </removedTestCases>" + System.lineSeparator()  +
                               "</report>" + System.lineSeparator() )));
  }
}