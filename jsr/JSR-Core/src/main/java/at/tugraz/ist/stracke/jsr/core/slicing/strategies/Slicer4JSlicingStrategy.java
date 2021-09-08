package at.tugraz.ist.stracke.jsr.core.slicing.strategies;

import at.tugraz.ist.stracke.jsr.core.shared.TestCase;
import at.tugraz.ist.stracke.jsr.core.slicing.result.TestCaseSliceResult;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.nio.file.Paths;

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

    this.pathToLoggerJar = Path.of(
      "../../slicer/DynamicSlicingCore/DynamicSlicingLoggingClasses/DynamicSlicingLogger.jar")
                               .toAbsolutePath()
                               .toRealPath();
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

  private void instrumentJar() throws IllegalStateException {
    logger.info("Starting to instrument Jar...");

    ProcessBuilder pb = new ProcessBuilder()
      .command("java", "-Xmx8g",
               "-cp", String.format("%s/Slicer4J/target/slicer4j-jar-with-dependencies.jar:%s/Slicer4J/target/lib/*",
                                    this.pathToSlicer.toString(), this.pathToSlicer.toString()),
               "ca.ubc.ece.resess.slicer.dynamic.slicer4j.Slicer",
               "-m", "i", "-j", pathToJar.toString(),
               "-o", pathToOutDir.toString(),
               "-sl", String.format("%s/static_log.log", this.pathToOutDir),
               "-lc", pathToLoggerJar.toString())
      .redirectOutput(ProcessBuilder.Redirect.to(new File(this.pathToOutDir.toString() + "/instr-debug.log")))
      .redirectError(ProcessBuilder.Redirect.to(new File(this.pathToOutDir.toString() + "/instr-debug.log")));

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

  private void handleInstrumentationError(Process p) {
    logger.error("Error during Instrumentation, See instr-debug.log for more info");

    new BufferedReader(new InputStreamReader(p.getErrorStream()))
      .lines().forEach(logger::error);
  }

  private void executeTestCase() {
    // TODO use a test runner to execute the passed testCase in the instrumented jar
    // TODO produce execution trace
  }

  private TestCaseSliceResult calculateSlice() {
    // TODO call slicer in "s" mode to calculate the slice
    // TODO produce a TestCaseSliceResult
    return null;
  }
}
