package at.tugraz.ist.stracke.jsr.cli.commands;

import at.tugraz.ist.stracke.jsr.cli.candidates.CoverageCandidates;
import at.tugraz.ist.stracke.jsr.cli.services.CoverageService;
import at.tugraz.ist.stracke.jsr.cli.services.JSRServiceImpl;
import at.tugraz.ist.stracke.jsr.core.shared.JSRParams;
import picocli.CommandLine;
import picocli.CommandLine.*;

import java.nio.file.Path;
import java.util.concurrent.Callable;

@Command(
  name = "coverage",
  mixinStandardHelpOptions = true,
  usageHelpWidth = 100,
  footer = "\r\nJSR - The Java Test Suite Reduction Framework",
  version = "JSR CLI 1.0",
  description = "Creates a coverage report of the specified type to be used for TSR\r\n"
)
public class CoverageCommand implements Callable<Integer> {
  @Parameters(index = "0",
              arity = "1",
              paramLabel = "<testSourceDir>",
              description = "The root directory of the test suite sources\r\n")
  private Path pathTestSources;

  @ArgGroup(heading = "\r\nRequired Parameters:\r\n", order = 1, validate = false, exclusive = false)
  private CoverageCommand.RequiredFlags requiredFlags;

  static class RequiredFlags {

    @Option(required = true,
            order = 0,
            arity = "1",
            names = {"-s", "--sources"},
            description = "The root directory of the main source code")
    private Path pathSources;

    @Option(required = true,
            order = 1,
            arity = "1",
            names = {"-j", "--jar"},
            description = "The path to the jar file containing the source and test classes")
    private Path pathJar;

    @Option(required = true,
            order = 2,
            arity = "1",
            names = {"-c", "--classes"},
            description = "The path to the root directory containing the compiled source code classes")
    private Path pathClasses;

    @Option(required = true,
            order = 3,
            arity = "1",
            names = {"-l", "--slicer"},
            description = "The path to the Slicer4J directory")
    private Path pathSlicer;

    @Option(required = true,
            order = 4,
            arity = "1",
            names = {"-o", "--out"},
            description = "The path to the directory where the coverage report is written to")
    private Path pathOut;
  }

  @ArgGroup(heading = "\r\nOptional Parameters:\r\n", order = 2, validate = false, exclusive = false)
  private CoverageCommand.OptionalFlags optionalFlags;

  static class OptionalFlags {

    @Option(order = 1,
            arity = "1",
            names = {"--package"},
            description = "When specified, only classes under this package are instrumented for line and method coverage calculation")
    private String basePackage;

    @Option(arity = "1",
            names = {"--type"},
            showDefaultValue = CommandLine.Help.Visibility.ALWAYS,
            defaultValue = CoverageCandidates.COV_CHECKED,
            completionCandidates = CoverageCandidates.class,
            description = "The coverage type to be calculated" +
                          "\r\n  Available options: " + CoverageCandidates.COV_CHECKED + ", " +
                          CoverageCandidates.COV_LINE + ", " + CoverageCandidates.COV_METHOD)
    private String coverageMetric;
  }

  @Override
  public Integer call() throws Exception {
    long timeStart = System.currentTimeMillis();
    boolean opts = this.optionalFlags != null;

    JSRParams params = new JSRParams(
      pathTestSources,
      requiredFlags.pathSources,
      requiredFlags.pathJar,
      requiredFlags.pathClasses,
      requiredFlags.pathSlicer,
      requiredFlags.pathOut,
      null,
      null,
      opts ? optionalFlags.coverageMetric : CoverageCandidates.COV_CHECKED,
      null,
      opts ? optionalFlags.basePackage : null,
      false);

    CoverageService coverageService = new JSRServiceImpl();
    coverageService.calculateCoverage(params);
    long timeEnd = System.currentTimeMillis();

    double durationSeconds = (timeEnd - timeStart) / 1000.0;

    System.out.println("* Coverage Calculation took " + durationSeconds + " seconds");

    return 0;
  }
}
