package at.tugraz.ist.stracke.jsr.core.sfl;

import at.tugraz.ist.stracke.jsr.core.shared.JSRParams;

/**
 * SFLFacade follows the facade pattern to provide
 * a high-level, easy to use interface to create and export
 * spectrum-based fault localisation matrices.
 *
 * @see at.tugraz.ist.stracke.jsr.core.sfl.exporter.SFLMatrixExporter for further details.
 */
public interface SFLFacade {

  /**
   * Creates and exports the two SFL matrices.
   *
   * @param params the {@link JSRParams} object containing the necessary paths.
   *
   * @return a boolean set to <code>true</code> on SFL export success, <code>false</code> otherwise.
   */
  boolean createAndExportSFLMatrices(JSRParams params);
}
