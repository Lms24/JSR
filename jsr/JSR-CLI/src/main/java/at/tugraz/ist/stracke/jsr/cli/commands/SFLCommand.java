package at.tugraz.ist.stracke.jsr.cli.commands;

import at.tugraz.ist.stracke.jsr.cli.services.JSRServiceImpl;
import at.tugraz.ist.stracke.jsr.cli.services.SFLService;
import at.tugraz.ist.stracke.jsr.core.shared.JSRParams;
import picocli.CommandLine.*;

import java.nio.file.Path;
import java.util.concurrent.Callable;

@Command(
  name = "sfl",
  mixinStandardHelpOptions = true,
  usageHelpWidth = 100,
  description = "Creates Spectrum-based Fault Localization matrices.\r\n"
)
public class SFLCommand implements Callable<Integer> {
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
            description = "The path to the directory where the SFL matrices are written to")
    private Path pathOut;
  }

  @ArgGroup(heading = "\r\nOptional Parameters:\r\n", order = 2, validate = false, exclusive = false)
  private OptionalFlags optionalFlags;

  static class OptionalFlags {

    @Option(order = 6,
            arity = "1",
            names = {"--package"},
            description = "When specified, only classes under this package are instrumented for line and method coverage calculation")
    private String basePackage;
  }

  @Override
  public Integer call() throws Exception {
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
      null,
      null,
      opts ? optionalFlags.basePackage : null);

    SFLService sflService = new JSRServiceImpl();
    sflService.createAndExportSFLMatrices(params);
    return 0;
  }
}
