package at.tugraz.ist.stracke.jsr.cli;

import at.tugraz.ist.stracke.jsr.cli.commands.JSRCommand;
import at.tugraz.ist.stracke.jsr.cli.commands.ReductionCommand;
import at.tugraz.ist.stracke.jsr.cli.commands.SFLCommand;
import picocli.CommandLine;

public class Main {

  // this example implements Callable, so parsing, error handling and handling user
  // requests for usage help or version help can be done with one line of code.
  public static void main(String... args) {
    int exitCode = new CommandLine(new JSRCommand())
      .addSubcommand(new ReductionCommand())
      .addSubcommand(new SFLCommand())
      .execute(args);

    System.exit(exitCode);
  }
}
