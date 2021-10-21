package at.tugraz.ist.stracke.jsr.core.sfl.exporter;

import at.tugraz.ist.stracke.jsr.core.coverage.CoverageReport;

public interface SFLMatrixExporter {

  /**
   * Exports Spectrum-Based Fault Localization Matrices for further
   * research and development purposes.
   * Classes implementing this interface must export the two matrices
   * in a data format of their choice (e.g. csv, json, etc.)
   *
   * The outcome must be two matrices: A and e:
   * A is a MxN matrix with M rows, where each row denotes a test case
   * and N columns where each column denotes a unit (e.g. line) that was
   * covered by the test case. a_ij is \in {0, 1} depending on if the
   * TC covered the unit (1) or not (0).
   *
   * e is a Tx1 Matrix/column-vector, where T denotes the number of
   * test cases. e_i1 also is \in {0, 1} depending on if the
   * TC passed (1) or failed (0).
   *
   * @return a boolean set to true, when the export was successful
   *         and false otherwise.
   */
  boolean exportSFLMatrices();

  void setCoverageReport(CoverageReport report);
}
