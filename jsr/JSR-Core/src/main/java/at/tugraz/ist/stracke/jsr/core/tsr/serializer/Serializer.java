package at.tugraz.ist.stracke.jsr.core.tsr.serializer;

import at.tugraz.ist.stracke.jsr.core.tsr.ReducedTestSuite;

import java.nio.file.Path;
import java.util.List;

public interface Serializer {

  void serialize(boolean writeToFile);

  List<String> getModifiedCode();

  Serializer setOutputDirectory(Path outDir);
  Serializer setReducedTestSuite(ReducedTestSuite rts);
}
