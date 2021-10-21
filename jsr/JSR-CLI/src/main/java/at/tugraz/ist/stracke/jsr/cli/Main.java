package at.tugraz.ist.stracke.jsr.cli;

import picocli.CommandLine;

public class Main {

  // this example implements Callable, so parsing, error handling and handling user
  // requests for usage help or version help can be done with one line of code.
  public static void main(String... args) {
    int exitCode = new CommandLine(new ReductionCommand()).execute(args);
    System.exit(exitCode);
  }
}
