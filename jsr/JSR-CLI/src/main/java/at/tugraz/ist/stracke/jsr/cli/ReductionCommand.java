package at.tugraz.ist.stracke.jsr.cli;

import at.tugraz.ist.stracke.jsr.cli.candidates.AlgorithmCandidates;
import at.tugraz.ist.stracke.jsr.cli.candidates.CoverageCandidates;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.Option;
import picocli.CommandLine.ArgGroup;

import java.nio.file.Path;
import java.util.concurrent.Callable;

@Command(
  name = "reduce",
  mixinStandardHelpOptions = true,
  usageHelpWidth = 100,
  footer = "\r\nJSR - The Java Test Suite Reduction Framework",
  version = "JSR CLI 1.0",
  description = "\r\nReduces a test suite based on the given options and parameters.\r\n"
)
public class ReductionCommand implements Callable<Integer> {

  @Parameters(index = "0",
              arity = "1",
              paramLabel = "<testSourceDir>",
              description = "The root directory of the test suite sources\r\n")
  private Path pathTestSources;

  @ArgGroup(heading = "\r\nRequired Flags:\r\n", order = 1, validate = false, exclusive = false)
  private RequiredFlags requiredFlags;

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
            description = "The path to the root directory containing the compiled classes")
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
            description = "The path to the directory where all reports and output files are saved")
    private Path pathOut;
  }

  @ArgGroup(heading = "\r\nOptional Flags:\r\n", order = 2, validate = false, exclusive = false)
  private OptionalFlags optionalFlags;

  static class OptionalFlags {
    @Option(order = 5,
            arity = "1",
            names = {"--gen"},
            description = "The path to the directory where the modified test classes are generated and saved")
    private Path pathGenOut;

    @Option(order = 6,
            arity = "1",
            names = {"--package"},
            description = "When specified, only classes under this package are instrumented for line and method coverage calculation")
    private String basePackage;

    @Option(order = 7,
            arity = "1",
            showDefaultValue = CommandLine.Help.Visibility.ALWAYS,
            defaultValue = CoverageCandidates.COV_CHECKED,
            completionCandidates = CoverageCandidates.class,
            names = {"--coverage"},
            description = "The coverage metric calculated before the reduction when no coverage report is specified. " +
                          "This option only has an effect if --report is not specified" +
                          "\r\n  Available options: " + CoverageCandidates.COV_CHECKED + ", " +
                          CoverageCandidates.COV_LINE + ", " + CoverageCandidates.COV_METHOD)
    private CoverageCandidates coverageMetric;

    @Option(order = 8,
            arity = "1",
            names = {"--report"},
            description = "Path to the coverage report which is used for the reduction")
    private Path pathCoverageReport;

    @Option(order = 9,
            arity = "1",
            showDefaultValue = CommandLine.Help.Visibility.ALWAYS,
            defaultValue = AlgorithmCandidates.ALG_GREEDY_HGS,
            completionCandidates = AlgorithmCandidates.class,
            names = {"--algorithm"},
            description = "The reduction algorithm used to reduce the test suite." +
                          "\r\n  Available options: " + AlgorithmCandidates.ALG_GREEDY_HGS + ", " +
                          AlgorithmCandidates.ALG_GENETIC)
    private CoverageCandidates algorithm;
  }

  @Override
  public Integer call() throws Exception {
    return null;
  }
}
