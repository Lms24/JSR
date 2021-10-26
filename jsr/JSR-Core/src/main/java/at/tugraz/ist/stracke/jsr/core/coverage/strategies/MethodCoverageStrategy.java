package at.tugraz.ist.stracke.jsr.core.coverage.strategies;

import at.tugraz.ist.stracke.jsr.core.coverage.CoverageReport;
import at.tugraz.ist.stracke.jsr.core.shared.TestCase;
import org.apache.logging.log4j.LogManager;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.nio.file.Path;

import java.util.HashSet;
import java.util.Set;

public class MethodCoverageStrategy extends JaCoCoCoverageStrategy {

  public MethodCoverageStrategy(Path pathToJar,
                                Path pathToClasses,
                                Path pathToSources,
                                Path pathToSlicer,
                                Path pathToOutDir,
                                String basePackage) {
    super(pathToJar,
          pathToClasses,
          pathToSources,
          pathToSlicer,
          pathToOutDir,
          basePackage,
          LogManager.getLogger(MethodCoverageStrategy.class));
  }


  @Override
  boolean processTestCaseCoverageReportData(TestCase tc) {
    final Set<CoverageReport.Unit> tmpCoveredUnits = new HashSet<>();
    String tcId = String.format("%s#%s", tc.getClassName(), tc.getName());

    Document document = parseXmlReport(tcId);

    NodeList classes = document.getElementsByTagName("class");
    for (int i = 0; i < classes.getLength(); i++) {
      Node clazz = classes.item(i);
      collectMethodData(tmpCoveredUnits, clazz);
    }

    this.coveredUnits.addAll(tmpCoveredUnits);
    this.coverageData.put(tc, tmpCoveredUnits);

    firstIteration = false;
    return true;
  }

  private void collectMethodData(Set<CoverageReport.Unit> tmpCoveredUnits, Node clazz) {
    String className = clazz.getAttributes().getNamedItem("name").getTextContent().replace("/", ".");
    NodeList methods = clazz.getChildNodes();
    for (int i = 0; i < methods.getLength(); i++) {
      Node method = methods.item(i);

      if (!method.getNodeName().equals("method")) {
        continue;
      }

      String methodSignature = method.getAttributes().getNamedItem("name").getTextContent();
      methodSignature += method.getAttributes().getNamedItem("desc").getTextContent();

      int methodStartLine = Integer.parseInt(method.getAttributes().getNamedItem("line").getTextContent());

      NodeList methodCounters = method.getChildNodes();
      boolean covered = isMethodCovered(methodCounters);

      final CoverageReport.Unit methodUnit = new CoverageReport.Unit(String.format("%s#%s", className, methodSignature),
                                                                     methodStartLine,
                                                                     methodStartLine);

      if (firstIteration) {
        allUnits.add(methodUnit);
      }
      if (covered) {
        tmpCoveredUnits.add(methodUnit);
      }
    }
  }

  private boolean isMethodCovered(NodeList methodCounters) {
    for (int j = 0; j < methodCounters.getLength(); j++) {
      Node counter = methodCounters.item(j);
      if (counter.getAttributes().getNamedItem("type").getTextContent().equals("INSTRUCTION")) {
        if (Integer.parseInt(counter.getAttributes().getNamedItem("covered").getTextContent()) > 0) {
          return true;
        }
        break;
      }
    }
    return false;
  }

  @Override
  CoverageReport assembleReport() {
    final CoverageReport report = new CoverageReport("Method",
                                                     this.allUnits,
                                                     this.coveredUnits,
                                                     this.coverageData);

    logger.info("Successfully created coverage report: {} of {} methods covered. Coverage score: {}",
                report.coveredUnits.size(),
                report.allUnits.size(),
                report.getCoverageScore());

    return report;
  }
}
