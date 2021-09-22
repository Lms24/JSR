package at.tugraz.ist.stracke.jsr.core.facade;

import at.tugraz.ist.stracke.jsr.core.tsr.ReducedTestSuite;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

/**
 * Integration tests via the {@link JSRFacade} interface to test core library functionality
 * for pre-defined strategies in the facade.
 */
class JUnitJSRFacadeTest {

  @Test
  void testReduceTestSuiteWithCheckedCoverage() {
    String srcDir = "./src/test/resources/smallProject/src/main/java";
    String testDir = "./src/test/resources/smallProject/src/test/java";
    String jarDir = "./src/test/resources/smallProject/build/libs/testJar.jar";
    String outDir = "./src/test/resources/smallProject/jsr";
    String slicerDir = "../../slicer/Slicer4J";

    JUnitJSRFacadeBuilder facadeBuilder = new JUnitJSRFacadeBuilder(Path.of(srcDir),
                                                                    Path.of(testDir),
                                                                    Path.of(jarDir),
                                                                    Path.of(outDir),
                                                                    Path.of(slicerDir));
    JSRFacade facade = facadeBuilder.build();

    ReducedTestSuite rts = facade.reduceTestSuite();

    assertThat(rts, is(notNullValue()));
  }
}