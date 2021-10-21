package at.tugraz.ist.stracke.jsr.cli;

import at.tugraz.ist.stracke.jsr.cli.candidates.AlgorithmCandidates;
import at.tugraz.ist.stracke.jsr.cli.candidates.CoverageCandidates;
import at.tugraz.ist.stracke.jsr.cli.services.JSRParams;
import at.tugraz.ist.stracke.jsr.cli.services.JSRService;
import at.tugraz.ist.stracke.jsr.cli.services.JSRServiceImpl;
import at.tugraz.ist.stracke.jsr.core.tsr.ReducedTestSuite;
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

  @ArgGroup(heading = "\r\nRequired Parameters:\r\n", order = 1, validate = false, exclusive = false)
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
            description = "The path to the directory where all reports and output files are saved")
    private Path pathOut;
  }

  @ArgGroup(heading = "\r\nOptional Parameters:\r\n", order = 2, validate = false, exclusive = false)
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
    private String coverageMetric;

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
    private String algorithm;
  }

  @Override
  public Integer call() throws Exception {

    final boolean options = this.optionalFlags != null;

    JSRParams params = new JSRParams(
      this.pathTestSources,
      this.requiredFlags.pathSources,
      this.requiredFlags.pathJar,
      this.requiredFlags.pathClasses,
      this.requiredFlags.pathSlicer,
      this.requiredFlags.pathOut,
      options ? this.optionalFlags.pathGenOut : null,
      options ? this.optionalFlags.pathCoverageReport : null,
      options ? this.optionalFlags.coverageMetric : null,
      options ? this.optionalFlags.algorithm : null,
      options ? this.optionalFlags.basePackage : null
    );

    JSRService jsrService = new JSRServiceImpl();
    ReducedTestSuite rts = jsrService.reduceTestSuite(params);

    if (rts == null) {
      System.err.println("Error during Reduction, check your logs in the output directory");
      return 1;
    }

    printResult(rts);

    return 0;
  }

  private void printResult(ReducedTestSuite rts) {
    System.out.println("Successfully reduced your test suite!");
    System.out.println("+----------------------------------- Summary -----------------------------------+");
    System.out.printf("| Test suite size: %d test case%s%n", rts.getTestSuiteSize(), rts.getTestSuiteSize() == 1 ? "" : "s");
    System.out.printf("+-------------------------------------------------------------------------------+%n");
    System.out.printf("| Found %d relevant test cases:%n", rts.testCases.size());
    rts.testCases.forEach(x -> System.out.printf("|    %s%n", x.getFullName()));
    System.out.printf("+-------------------------------------------------------------------------------+%n");
    System.out.printf("| Found %d redundant test cases:%n", rts.removedTestCases.size());
    rts.removedTestCases.forEach(x -> System.out.printf("|    %s%n", x.getFullName()));
    if (this.optionalFlags.pathGenOut != null) {
      System.out.printf("+-------------------------------------------------------------------------------+%n");
      System.out.printf("| Wrote reduced test suite code to: %s%n", this.optionalFlags.pathGenOut);
    }
    System.out.println("+-------------------------------------------------------------------------------+");
  }
}
