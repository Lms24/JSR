package at.tugraz.ist.stracke.jsr.cli.commands;

import picocli.CommandLine.*;

@Command(
  name = "jsr",
  mixinStandardHelpOptions = true,
  usageHelpWidth = 100,
  footer = "JSR - The Java Test Suite Reduction Framework",
  version = "JSR CLI 1.0"
)
public class JSRCommand {
}
