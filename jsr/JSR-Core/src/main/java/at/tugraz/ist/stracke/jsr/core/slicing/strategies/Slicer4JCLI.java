package at.tugraz.ist.stracke.jsr.core.slicing.strategies;

class Slicer4JCLI {

  static final String SL4C_MAIN_CLASS = "ca.ubc.ece.resess.slicer.dynamic.slicer4j.Slicer";
  static final String JUNIT4_RUNNER_MAIN_CLASS = "SingleJUnitTestRunner";

  static class Paths {
    static final String PATH_LOGGER_JAR = "DynamicSlicingCore/DynamicSlicingLoggingClasses/DynamicSlicingLogger.jar";

    static final String PATH_SLICER4J_JAR_WITH_DEPENDENCIES = "Slicer4J/target/slicer4j-jar-with-dependencies.jar";
    static final String PATH_SLICER4J_ALL_LIBS = "Slicer4J/target/lib/*";

    static final String PATH_JUNIT4_RUNNER_JAR = "scripts/SingleJUnitTestRunner.jar";
    static final String PATH_JUNIT4_LIB_JAR = "scripts/junit-4.8.2.jar";

    static final String PATH_SUMMARIES_MANUAL = "models/summariesManual";
    static final String PATH_TAINT_WRAPPER_SOURCE = "models/EasyTaintWrapperSource.txt";
  }

  static class Args {
    static final String ARG_MODE = "-m";
    static final String MODE_INSTRUMENT = "i";
    static final String MODE_GRAPH = "g";
    static final String MODE_SLICE = "s";

    static final String ARG_JAR = "-j";

    static final String ARG_OUT_DIR = "-o";
    static final String ARG_STATIC_LOG = "-sl";
    static final String ARG_LOGGING_CLASS = "-lc";

    static final String ARG_TRACE_LOG = "-t";
    static final String ARG_STUBDROID = "-sd";
    static final String ARG_TAINT_WRAPPER = "-tw";

    static final String ARG_SLICE_POSITIONS = "-sp";
  }

  static class FileNames {
    static final String STATIC_LOG = "static_log.log";
    static final String INSTR_DEBUG_LOG = "instr-debug.log";

    static final String TRACE_FULL_LOG = "trace_full.log";
    static final String TRACE_LOG = "trace.log";
    static final String TRACE_ICDG = "trace.log_icdg.log";

    static final String INSTRUMENTED_JAR_SUFFIX = "_i";

    static final String SLICE_FILE = "slice-file.log";
    static final String GRAPH_DEBUG_LOG = "graph-debug.log";
    static final String SLICE_LOG = "slice.log";

  }
}
