package at.tugraz.ist.stracke.jsr.core.slicing.strategies;

import at.tugraz.ist.stracke.jsr.core.shared.TestCase;
import at.tugraz.ist.stracke.jsr.core.slicing.result.SliceEntry;
import at.tugraz.ist.stracke.jsr.core.slicing.result.TestCaseSliceResult;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static at.tugraz.ist.stracke.jsr.core.slicing.strategies.Slicer4JCLI.*;

/**
 * A {@link SlicingStrategy} that uses the tool Slicer4J to create a dynamic slice
 * of the passed Testcase.
 * <p>
 * Initialize this strategy once for the whole test suite to instrument the jar of the
 * project. Then iterate through the suite's {@link TestCase}s and use
 * {@link #setTestCase(TestCase)} to set the test case to be sliced and use {@link #execute()}
 * to produce the slice.
 *
 * @see <a href="https://github.com/resess/Slicer4J">Slicer4J Repository</a>
 */
public class Slicer4JSlicingStrategy implements SlicingStrategy {

  private static final Logger logger = LogManager.getLogger(Slicer4JSlicingStrategy.class);

  private Path pathToJar;
  private Path pathToSlicer;
  private Path pathToOutDir;

  private Path pathToLoggerJar;

  private TestCase testCase;

  private String jarFileName;
  private String instrumentedJarFileName;

  /**
   * Initializes a new Slicer4JSlicingStrategy instance
   *
   * @param pathToJar    the path to the fat jar which shall be sliced
   * @param pathToSlicer the path to the slicer (main Slicer4J directory)
   */
  public Slicer4JSlicingStrategy(String pathToJar,
                                 String pathToSlicer,
                                 String pathToOutDir) {

    try {
      this.convertPaths(pathToJar, pathToSlicer, pathToOutDir);
      this.createDirectoriesIfNecessary();
    } catch (IOException e) {
      e.printStackTrace();
    }

    this.logPaths();

    this.instrumentJar();
  }

  @Override
  public TestCaseSliceResult execute() {
    if (this.testCase == null) {
      throw new IllegalStateException("Test case was not set before calling execute()");
    }
    if (this.testCase.getAssertions().isEmpty()) {
      logger.warn("Got TestCase w/o assertions, skipping execution and slicing ({}:{})",
                  this.testCase.getClassName(),
                  this.testCase.getName());
      return null;
    }
    this.executeTestCase();
    return this.calculateSlice();
  }

  @Override
  public SlicingStrategy setTestCase(@NonNull TestCase testCase) {
    this.testCase = testCase;
    return this;
  }

  private void convertPaths(String pathToJar, String pathToSlicer, String pathToOutDir) throws IOException {
    this.pathToJar = Path.of(pathToJar).toAbsolutePath().toRealPath();
    this.pathToSlicer = Path.of(pathToSlicer).toAbsolutePath().toRealPath();

    Path out = Path.of(pathToOutDir).toAbsolutePath();
    this.pathToOutDir = out.toFile().exists() ? out.toRealPath() : out;

    this.pathToLoggerJar = Path.of("../../slicer/" + Slicer4JCLI.Paths.PATH_LOGGER_JAR)
                               .toAbsolutePath()
                               .toRealPath();
    final String[] tmp = this.pathToJar.toString().split("/");
    this.jarFileName = tmp[tmp.length - 1];
    this.instrumentedJarFileName = this.jarFileName
      .replace(".jar", FileNames.INSTRUMENTED_JAR_SUFFIX + ".jar");
  }

  private void instrumentJar() throws IllegalStateException {
    logger.info("Starting to instrument Jar...");

    ProcessBuilder pb = new ProcessBuilder()
      .command("java", "-Xmx8g",
               "-cp", String.format("%s/%s:%s/%s",
                                    this.pathToSlicer.toString(), Slicer4JCLI.Paths.PATH_SLICER4J_JAR_WITH_DEPENDENCIES,
                                    this.pathToSlicer.toString(), Slicer4JCLI.Paths.PATH_SLICER4J_ALL_LIBS),
               SL4C_MAIN_CLASS,
               Args.ARG_MODE, Args.MODE_INSTRUMENT,
               Args.ARG_JAR, pathToJar.toString(),
               Args.ARG_OUT_DIR, pathToOutDir.toString(),
               Args.ARG_STATIC_LOG, String.format("%s/%s", this.pathToOutDir, FileNames.STATIC_LOG),
               Args.ARG_LOGGING_CLASS, pathToLoggerJar.toString())
      .redirectOutput(ProcessBuilder.Redirect.to(new File(
        String.format("%s/%s", this.pathToOutDir.toString(), FileNames.INSTR_DEBUG_LOG))))
      .redirectError(ProcessBuilder.Redirect.to(new File(
        String.format("%s/%s", this.pathToOutDir.toString(), FileNames.INSTR_DEBUG_LOG))));

    try {
      logger.debug("Instrumentation command: {}", String.join(" ", pb.command()));

      Process p = pb.start();
      p.waitFor();

      if (p.exitValue() == 0) {
        logger.info("Jar was instrumented successfully.");
      } else {
        handleInstrumentationError(p);
      }
    } catch (IOException | InterruptedException e) {
      logger.error("Error during Instrumentation, Caught exception:");
      e.printStackTrace();
    }
  }

  private void executeTestCase() {
    logger.info("Executing testcase {}#{}", this.testCase.getClassName(), this.testCase.getName());

    ProcessBuilder pb = new ProcessBuilder()
      .command("java", "-Xmx8g",
               "-cp", String.format("%s/%s:%s/%s:%s",
                                    this.pathToSlicer.toString(), Slicer4JCLI.Paths.PATH_JUNIT4_RUNNER_JAR,
                                    this.pathToSlicer.toString(), Slicer4JCLI.Paths.PATH_JUNIT4_LIB_JAR,
                                    String.format("%s/%s", this.pathToOutDir, this.instrumentedJarFileName)),
               JUNIT4_RUNNER_MAIN_CLASS,
               String.format("%s#%s", this.testCase.getClassName(), this.testCase.getName()))
      .redirectOutput(ProcessBuilder.Redirect.to(new File(
        String.format("%s/%s", this.pathToOutDir.toString(), FileNames.TRACE_FULL_LOG))))
      .redirectError(ProcessBuilder.Redirect.to(new File(
        String.format("%s/%s", this.pathToOutDir.toString(), FileNames.TRACE_FULL_LOG))));

    try {
      logger.debug("Test case runner command: {}", String.join(" ", pb.command()));

      Process p = pb.start();
      p.waitFor();

      if (p.exitValue() == 0) {
        logger.info("Test case was executed successfully.");
        this.writeTraceLog();
      } else {
        boolean actualError = handleTCExecutionError(p);
        if (!actualError) {
          writeTraceLog();
        }
      }
    } catch (IOException | InterruptedException e) {
      logger.error("Error during test case execution, Caught exception:");
      e.printStackTrace();
    }
  }

  private TestCaseSliceResult calculateSlice() {
    this.createGraph();

    String criterion = getICDGSlicingCriterion();
    if (criterion == null) {
      return null;
    }

    this.slice(criterion);

    Set<SliceEntry> slice = collectSliceResult();
    if (slice == null) {
      return null;
    }

    return new TestCaseSliceResult(this.testCase, slice);
  }


  private void createGraph() {
    logger.info("Creating ICDG Graph for {}#{}", this.testCase.getClassName(), this.testCase.getName());

    ProcessBuilder pb = new ProcessBuilder()
      .command("java", "-Xmx8g",
               "-cp", String.format("%s/%s:%s/%s",
                                    this.pathToSlicer.toString(), Slicer4JCLI.Paths.PATH_SLICER4J_JAR_WITH_DEPENDENCIES,
                                    this.pathToSlicer.toString(), Slicer4JCLI.Paths.PATH_SLICER4J_ALL_LIBS),
               SL4C_MAIN_CLASS,
               Args.ARG_MODE, Args.MODE_GRAPH,
               Args.ARG_JAR, pathToJar.toString(),
               Args.ARG_TRACE_LOG, String.format("%s/%s", this.pathToOutDir, FileNames.TRACE_LOG),
               Args.ARG_OUT_DIR, pathToOutDir.toString(),
               Args.ARG_STATIC_LOG, String.format("%s/%s", this.pathToOutDir, FileNames.STATIC_LOG),
               Args.ARG_STUBDROID, String.format("%s/%s", this.pathToSlicer, Slicer4JCLI.Paths.PATH_SUMMARIES_MANUAL),
               Args.ARG_TAINT_WRAPPER,
               String.format("%s/%s", this.pathToSlicer, Slicer4JCLI.Paths.PATH_TAINT_WRAPPER_SOURCE))
      .redirectOutput(ProcessBuilder.Redirect.to(new File(
        String.format("%s/%s", this.pathToOutDir.toString(), FileNames.GRAPH_DEBUG_LOG))))
      .redirectError(ProcessBuilder.Redirect.to(new File(
        String.format("%s/%s", this.pathToOutDir.toString(), FileNames.GRAPH_DEBUG_LOG))));

    try {
      logger.debug("Graph creation command: {}", String.join(" ", pb.command()));

      Process p = pb.start();
      p.waitFor();

      if (p.exitValue() == 0) {
        logger.info("Graph was created successfully.");
      } else {
        handleGraphCreationError(p);
      }
    } catch (IOException | InterruptedException e) {
      logger.error("Error graph creation, Caught exception:");
      e.printStackTrace();
    }
  }

  private String getICDGSlicingCriterion() {
    List<String> icdgLogLines;
    try {
      icdgLogLines = Files.readAllLines(
        Path.of(String.format("%s/%s", this.pathToOutDir, FileNames.TRACE_ICDG)));
    } catch (IOException e) {
      logger.error("Error while reading {}, aborting.", FileNames.TRACE_ICDG);
      e.printStackTrace();
      return null;
    }

    List<String> finalIcdgLogLines = icdgLogLines;
    String sl4jSlicingCriteriaString =
      this.testCase.getAssertions()
                   .stream()
                   .map(ass -> {
                     String substr = String.format("LINENO:%s:FILE:%s",
                                                   ass.getStartLine(),
                                                   this.testCase.getClassName());
                     return finalIcdgLogLines.stream()
                                             .filter(l -> l.contains(substr))
                                             .map(l -> l.split(", ")[0])
                                             .collect(Collectors.joining("-"));
                   })
                   .collect(Collectors.joining("-"));

    logger.debug("SL4J Slicing Criterion: [{}]", sl4jSlicingCriteriaString);

    return sl4jSlicingCriteriaString;
  }

  private void slice(String criterion) {
    logger.info("Slicing {}/{}", this.testCase.getClassName(), this.testCase.getName());

    ProcessBuilder pb = new ProcessBuilder()
      .command("java", "-Xmx8g",
               "-cp", String.format("%s/%s:%s/%s",
                                    this.pathToSlicer.toString(), Slicer4JCLI.Paths.PATH_SLICER4J_JAR_WITH_DEPENDENCIES,
                                    this.pathToSlicer.toString(), Slicer4JCLI.Paths.PATH_SLICER4J_ALL_LIBS),
               SL4C_MAIN_CLASS,
               Args.ARG_MODE, Args.MODE_SLICE,
               Args.ARG_JAR, pathToJar.toString(),
               Args.ARG_TRACE_LOG, String.format("%s/%s", this.pathToOutDir, FileNames.TRACE_LOG),
               Args.ARG_OUT_DIR, pathToOutDir.toString(),
               Args.ARG_STATIC_LOG, String.format("%s/%s", this.pathToOutDir, FileNames.STATIC_LOG),
               Args.ARG_STUBDROID, String.format("%s/%s", this.pathToSlicer, Slicer4JCLI.Paths.PATH_SUMMARIES_MANUAL),
               Args.ARG_TAINT_WRAPPER,
               String.format("%s/%s", this.pathToSlicer, Slicer4JCLI.Paths.PATH_TAINT_WRAPPER_SOURCE),
               Args.ARG_SLICE_POSITIONS, criterion)
      .redirectOutput(ProcessBuilder.Redirect.to(new File(
        String.format("%s/%s", this.pathToOutDir.toString(), FileNames.SLICE_FILE))))
      .redirectError(ProcessBuilder.Redirect.to(new File(
        String.format("%s/%s", this.pathToOutDir.toString(), FileNames.SLICE_FILE))));

    try {
      logger.debug("Slicing command: {}", String.join(" ", pb.command()));

      Process p = pb.start();
      p.waitFor();

      if (p.exitValue() == 0) {
        logger.info("Slice was calculated successfully");
      } else {
        handleSlicingError(p);
      }
    } catch (IOException | InterruptedException e) {
      logger.error("Error during Instrumentation, Caught exception:");
      e.printStackTrace();
    }
  }

  private Set<SliceEntry> collectSliceResult() {
    List<String> sliceLines;
    try {
      sliceLines = Files.readAllLines(Path.of(String.format("%s/%s", this.pathToOutDir, FileNames.SLICE_LOG)));
    } catch (IOException e) {
      logger.error("Error while collecting slice result, see slice.log and slice-file.log for more details.");
      e.printStackTrace();
      return null;
    }

    final Set<SliceEntry> slice = sliceLines.stream()
                                            .map(line -> line.split(":"))
                                            .map(l -> new SliceEntry(l[0], Integer.parseInt(l[1])))
                                            .collect(Collectors.toSet());

    logger.info("Collected slice result");

    return slice;
  }

  /**
   * Writes the {@link Slicer4JCLI.FileNames#TRACE_LOG} file
   *
   * @throws IOException to be caught by parent calling function
   */
  private void writeTraceLog() throws IOException {
    final File fullLogFile = new File(this.pathToOutDir.toString() + "/" + FileNames.TRACE_FULL_LOG);
    final BufferedReader reader = new BufferedReader(new FileReader(fullLogFile));

    List<String> lines = reader.lines().collect(Collectors.toList());
    List<String> slicing = lines.stream().filter(l -> l.contains("SLICING")).collect(Collectors.toList());

    // the trace_full.log file contains fail/pass information, which we save here
    this.testCase.setPassed(lines.stream().anyMatch(l -> l.toLowerCase().contains("test pass")));

    final File logFile = new File(this.pathToOutDir.toString() + "/" + FileNames.TRACE_LOG);

    Files.write(logFile.toPath(), String.join(System.lineSeparator(), slicing).getBytes(StandardCharsets.UTF_8));
  }

  private void handleInstrumentationError(@NonNull Process p) {
    logger.error("Error during Instrumentation, See instr-debug.log for more info");

    new BufferedReader(new InputStreamReader(p.getErrorStream()))
      .lines().forEach(logger::error);
  }

  private boolean handleTCExecutionError(@NonNull Process p) {
    List<String> lines = null;
    try {
      lines = Files.readAllLines(Path.of(this.pathToOutDir.toString(), FileNames.TRACE_FULL_LOG));
    } catch (IOException e) {
      e.printStackTrace();
    }

    boolean testJustFailed = lines != null && lines.stream().anyMatch(l -> l.toLowerCase().contains("test fail"));

    if (testJustFailed) {
      return false;
    } else {

      logger.error("Error during test case execution, See trace_full.log for more info");
      new BufferedReader(new InputStreamReader(p.getErrorStream()))
        .lines().forEach(logger::error);
      return true;
    }
  }

  private void handleGraphCreationError(@NonNull Process p) {
    logger.error("Error during graph creation, See graph-debug.log for more info");

    new BufferedReader(new InputStreamReader(p.getErrorStream()))
      .lines().forEach(logger::error);
  }

  private void handleSlicingError(@NonNull Process p) {
    logger.error("Error during slicing, See slice-file.log for more info");

    new BufferedReader(new InputStreamReader(p.getErrorStream()))
      .lines().forEach(logger::error);
  }

  private void createDirectoriesIfNecessary() throws IOException {
    if (!this.pathToOutDir.toFile().exists()) {
      logger.info("Output directory does not yet exist, creating it");

      boolean success = this.pathToOutDir.toFile().mkdirs();
      if (success) {
        this.pathToOutDir = this.pathToOutDir.toRealPath();
      } else {
        logger.error("Could not create directory");
      }
    }
  }

  private void logPaths() {
    Path currentRelativePath = Paths.get("");
    String s = currentRelativePath.toAbsolutePath().toString();
    logger.debug("Current absolute path is: " + s);
    logger.debug("Path to jar: {}", this.pathToJar);
    logger.debug("Path to slicer: {}", this.pathToSlicer);
    logger.debug("Path to out dir: {}", this.pathToOutDir);
    logger.debug("Path to out logging jar: {}", this.pathToLoggerJar);
  }
}
