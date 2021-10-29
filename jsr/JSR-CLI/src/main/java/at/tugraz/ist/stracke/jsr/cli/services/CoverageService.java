package at.tugraz.ist.stracke.jsr.cli.services;

import at.tugraz.ist.stracke.jsr.core.coverage.CoverageReport;
import at.tugraz.ist.stracke.jsr.core.shared.JSRParams;

public interface CoverageService {
  CoverageReport calculateCoverage(JSRParams params);
}
