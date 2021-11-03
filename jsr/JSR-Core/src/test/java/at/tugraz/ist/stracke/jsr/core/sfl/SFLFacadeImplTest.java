package at.tugraz.ist.stracke.jsr.core.sfl;

import at.tugraz.ist.stracke.jsr.core.sfl.exporter.SFLMatrixCsvExporter;
import at.tugraz.ist.stracke.jsr.core.shared.JSRParams;
import at.tugraz.ist.stracke.jsr.test.Mocks;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

class SFLFacadeImplTest {

  @Test
  void testCreateAndExportSFLMatricesMocked() {
    Path srcDir = Path.of("tmp");
    Path testDir = Path.of("tmp");
    Path jarDir = Path.of("tmp");
    Path outDir = Path.of("./build/jsr/sflMockedFacadeTest");
    Path slicerDir = Path.of("tmp");
    Path classDir = Path.of("tmp");

    JSRParams params = new JSRParams(testDir,
                                     srcDir,
                                     jarDir,
                                     classDir,
                                     slicerDir,
                                     outDir,
                                     null,
                                     null,
                                     null,
                                     null,
                                     "at.tugraz",
                                     false);

    SFLFacadeImpl facade = new SFLFacadeImpl();

    // Disable default behaviour by injecting stubs
    facade.setParser(new Mocks.SFLTestSuiteParser());
    facade.setSlicer(new Mocks.MockedTestSuiteSlicer());
    facade.setSlicingStrategy(new Mocks.MockedSlicingStrategy());
    facade.setFailCoverageStrategy(new Mocks.SFLFailCoverageStrategy());
    facade.setPassCoverageStrategy(new Mocks.SFLPassCoverageStrategy());

    boolean success = facade.createAndExportSFLMatrices(params);
    SFLMatrixCsvExporter exporter = (SFLMatrixCsvExporter) facade.getExporter();

    String coverageOut = exporter.createCoverageMatrixCsv();
    String passFailOut = exporter.createOutcomeMatrixCsv();

    String nl = System.lineSeparator();

    assertThat(success, is(true));
    assertThat(exporter, is(notNullValue()));
    assertThat(coverageOut, is(equalTo("TestCase,s1,s2,s3,s4,s5,s6" + nl  +
                                       "t1:t1,1,1,1,0,0,0" + nl +
                                       "t2:t2,1,1,0,0,0,0" + nl +
                                       "t3:t3,1,0,0,0,1,0" + nl +
                                       "t4:t4,1,1,1,0,0,0" + nl +
                                       "t5:t5,0,0,0,0,1,0" + nl +
                                       "tf1:tf1,0,1,0,0,1,1" + nl +
                                       "tf2:tf2,0,1,0,1,0,1" + nl +
                                       "tf3:tf3,1,0,0,1,0,1" + nl)));
    assertThat(passFailOut, is(equalTo("TestCase,pass" + nl  +
                                       "t1:t1,1" + nl +
                                       "t2:t2,1" + nl +
                                       "t3:t3,1" + nl +
                                       "t4:t4,1" + nl +
                                       "t5:t5,1" + nl +
                                       "tf1:tf1,0" + nl +
                                       "tf2:tf2,0" + nl +
                                       "tf3:tf3,0" + nl)));
  }

  @Test
  @Disabled("To decrease test time (mocked test above tests added behaviour)")
  void testIntegrateCreateAndExportSFLMatrices() {

    Path srcDir = Path.of("./src/test/resources/smallProject/src/main/java");
    Path testDir = Path.of("./src/test/resources/smallProject/src/test/java");
    Path jarDir = Path.of("./src/test/resources/smallProject/build/libs/testJar.jar");
    Path outDir = Path.of("./build/jsr/sflFacadeTest");
    Path slicerDir = Path.of("../../slicer/Slicer4J");
    Path classDir = Path.of("./src/test/resources/smallProject/build/classes/java/main");

    JSRParams params = new JSRParams(testDir,
                                     srcDir,
                                     jarDir,
                                     classDir,
                                     slicerDir,
                                     outDir,
                                     null,
                                     null,
                                     null,
                                     null,
                                     "at.tugraz",
                                     false);

    SFLFacade facade = new SFLFacadeImpl();

    boolean success = facade.createAndExportSFLMatrices(params);

    assertThat(success, is(true));
  }
}