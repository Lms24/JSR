package at.tugraz.ist.stracke.jsr.core.coverage.strategies;

import at.tugraz.ist.stracke.jsr.core.coverage.CoverageReport;
import at.tugraz.ist.stracke.jsr.core.shared.TestCase;
import org.apache.logging.log4j.LogManager;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class LineCoverageStrategy extends JaCoCoCoverageStrategy {

  private final Set<CoverageReport.Unit> allUnits = new HashSet<>();
  private final Set<CoverageReport.Unit> coveredUnits = new HashSet<>();
  private final Map<TestCase, Set<CoverageReport.Unit>> coverageData = new HashMap<>();

  public LineCoverageStrategy(Path pathToJar,
                              Path pathToClasses,
                              Path pathToSources,
                              Path pathToSlicer,
                              Path pathToOutDir) {
    super(pathToJar,
          pathToClasses,
          pathToSources,
          pathToSlicer,
          pathToOutDir,
          LogManager.getLogger(LineCoverageStrategy.class));
  }

  @Override
  CoverageReport createTestSuiteCoverageReport() {
    boolean collectedIndividualData = originalTestSuite.testCases.stream()
                                                                 .allMatch(this::processTestCaseCoverageReportData);

    if (!collectedIndividualData) {
      return null;
    }

    return new CoverageReport(this.allUnits, this.coveredUnits, this.coverageData);
  }

  private boolean firstIteration = true;
  private boolean processTestCaseCoverageReportData(TestCase tc) {
    String tcId = String.format("%s#%s", tc.getClassName(), tc.getName());

    logger.info("Reading report of {}", tcId);
    Path xmlReportPath = Path.of(this.pathToOutDir.toString(), tcId, "report.xml");
    try {
      Document document = parseXmlDocument(xmlReportPath);

      NodeList classes = document.getElementsByTagName("class");
      Set<CoverageReport.Unit> tmpCoveredUnits = new HashSet<>();
      for (int i = 0; i < classes.getLength(); i++) {
        Node clazz = classes.item(i);
        String className = clazz.getAttributes().getNamedItem("name").getTextContent().replace("/", ".");
        String sourceFileName = clazz.getAttributes().getNamedItem("sourcefilename").getTextContent();
        Node sourceFile = this.getSourceFileByName(sourceFileName, document);

        assert sourceFile != null;
        NodeList lineNodes = sourceFile.getChildNodes();


        for (int j = 0; j < lineNodes.getLength(); j++) {
          Node line = lineNodes.item(j);
          if (!line.getNodeName().equals("line")) {
            continue;
          }
          int lineNr = Integer.parseInt(line.getAttributes().getNamedItem("nr").getTextContent());
          int coveredInstructions = Integer.parseInt(line.getAttributes().getNamedItem("ci").getTextContent());

          final CoverageReport.Unit lineUnit = new CoverageReport.Unit(className, lineNr, lineNr);
          if (firstIteration) {
            this.allUnits.add(lineUnit);
          }
          if (coveredInstructions > 0) {
            tmpCoveredUnits.add(lineUnit);
          }
        }
      }
      this.coveredUnits.addAll(tmpCoveredUnits);
      this.coverageData.put(tc, tmpCoveredUnits);
    } catch (ParserConfigurationException | IOException | SAXException e) {
      e.printStackTrace();
      return false;
    }
    firstIteration = false;
    return true;
  }

  private Document parseXmlDocument(Path xmlReportPath) throws
                                                        ParserConfigurationException,
                                                        IOException,
                                                        SAXException {

    DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
    DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
    final String xmlString = Files.readString(xmlReportPath).replace(
      "<!DOCTYPE report PUBLIC \"-//JACOCO//DTD Report 1.1//EN\" \"report.dtd\">",
      "");
    return documentBuilder.parse(new InputSource(new StringReader(xmlString)));
  }

  private Node getSourceFileByName(String sourceFileName, Document document) {
    NodeList sourceFiles = document.getElementsByTagName("sourcefile");
    for (int i = 0; i < sourceFiles.getLength(); i++) {
      Node sf = sourceFiles.item(i);
      String n = sf.getAttributes().getNamedItem("name").getTextContent();
      if (sourceFileName.equals(n)) {
        return sf;
      }
    }
    return null;
  }
}
