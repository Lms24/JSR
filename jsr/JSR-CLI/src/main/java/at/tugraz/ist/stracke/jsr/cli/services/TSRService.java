package at.tugraz.ist.stracke.jsr.cli.services;

import at.tugraz.ist.stracke.jsr.core.shared.JSRParams;
import at.tugraz.ist.stracke.jsr.core.tsr.ReducedTestSuite;

public interface TSRService {
  ReducedTestSuite reduceTestSuite(JSRParams params);
}
