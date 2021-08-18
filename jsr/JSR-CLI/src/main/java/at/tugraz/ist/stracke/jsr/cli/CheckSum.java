package at.tugraz.ist.stracke.jsr.cli;

import at.tugraz.ist.stracke.jsr.core.CoreTest;
import picocli.CommandLine;
import picocli.CommandLine.Command;

import java.util.concurrent.Callable;


@Command(
  name = "checksum",
  mixinStandardHelpOptions = true,
  version = "checksum 4.0",
  description = "Prints the checksum (MD5 by default) of a file to STDOUT."
)
class CheckSum implements Callable<Integer> {

  @Override
  public Integer call() throws Exception {
    System.out.printf("Hello There from CLI" + System.lineSeparator());
    CoreTest t = new CoreTest();
    t.hello();
    return 0;
  }

  // this example implements Callable, so parsing, error handling and handling user
  // requests for usage help or version help can be done with one line of code.
  public static void main(String... args) {
    int exitCode = new CommandLine(new CheckSum()).execute(args);
    System.exit(exitCode);
  }
}