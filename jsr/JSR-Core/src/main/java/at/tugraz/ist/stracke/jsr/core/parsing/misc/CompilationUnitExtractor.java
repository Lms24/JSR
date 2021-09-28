package at.tugraz.ist.stracke.jsr.core.parsing.misc;

import at.tugraz.ist.stracke.jsr.core.parsing.strategies.ParsingStrategy;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.utils.SourceRoot;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class CompilationUnitExtractor {
  private static final Logger logger = LogManager.getLogger(CompilationUnitExtractor.class);


  private Path filePath;
  private String code;

  public CompilationUnitExtractor(Path filePath) {
    this.filePath = filePath;
  }

  public CompilationUnitExtractor(String code) {
    this.code = code;
  }

  public List<CompilationUnit> getCompilationUnits() {
    if (this.filePath != null) {
      return parseCompilationUnitsFromFilePath();
    } else {
      return parseCompilationUnitsFromString();
    }
  }

  private List<CompilationUnit> parseCompilationUnitsFromFilePath() {
    logger.info("Collecting compilation units from File Path {}", this.filePath.toString());
    SourceRoot sourceRoot = new SourceRoot(this.filePath);
    return sourceRoot.tryToParseParallelized()
                     .stream()
                     .filter(pr -> pr.isSuccessful() && pr.getResult().isPresent())
                     .map(pr -> pr.getResult().get())
                     .collect(Collectors.toList());
  }

  private List<CompilationUnit> parseCompilationUnitsFromString() {
    logger.info("Collecting compilation units from String code");
    return Collections.singletonList(StaticJavaParser.parse(code));
  }
}
