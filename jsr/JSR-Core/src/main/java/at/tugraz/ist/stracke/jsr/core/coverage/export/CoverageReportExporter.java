package at.tugraz.ist.stracke.jsr.core.coverage.export;

import at.tugraz.ist.stracke.jsr.core.coverage.CoverageReport;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.Date;

public class CoverageReportExporter {

  private static Logger logger = LogManager.getLogger(CoverageReportExporter.class);

  CoverageReport report;

  public CoverageReportExporter(CoverageReport report) {
    this.report = report;
  }

  public boolean exportToFile(Path outputPath) {
    Date now = new Date();
    SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
    final String coverageType = String.format("%sCovRep", report.coverageType);
    final String fileName = String.format("%s#%s.cvg", coverageType, df.format(now));

    if (!outputPath.toFile().exists()) {
      boolean success = outputPath.toFile().mkdirs();
      if (!success) {
        return false;
      }
    }

    File f = Path.of(outputPath.toString(), fileName).toFile();
    try {
      FileOutputStream fos = new FileOutputStream(f);
      ObjectOutputStream oos = new ObjectOutputStream(fos);
      oos.writeObject(report);
      oos.close();
      logger.info("Wrote coverage report to file {}", f.getAbsolutePath());
      return true;
    } catch (IOException e) {
      e.printStackTrace();
      logger.error("Error while writing coverage report to file {}", f.getAbsolutePath());
      return false;
    }
  }
}
