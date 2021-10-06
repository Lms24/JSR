package at.tugraz.ist.stracke.jsr.core.coverage.strategies;

class JaCoCoCLI {
  static class Paths {
    public static final String JACOCO_DIR = "../../jacoco/";
    public static final String AGENT_JAR = JACOCO_DIR + "jacocoagent.jar";
    public static final String CLI_JAR = JACOCO_DIR + "jacococli.jar";
  }

  static class Args {
    static final String AGENT_DEST_FILE = "destfile";

    static final String CLI_REPORT = "report";
    static final String CLI_CLASS_FILES = "--classfiles";
    static final String CLI_SOURCE_FILES = "--sourcefiles";
    static final String CLI_XML = "--xml";
    static final String CLI_CSV = "--csv";
    static final String CLI_HTML = "--html";
  }

  static class FileNames {
    static final String EXEC_LOG = "execution.log";
    static final String REPORT_LOG = "report.log";
  }
}
