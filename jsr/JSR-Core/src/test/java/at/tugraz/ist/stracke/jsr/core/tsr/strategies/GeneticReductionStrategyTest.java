package at.tugraz.ist.stracke.jsr.core.tsr.strategies;

import at.tugraz.ist.stracke.jsr.core.shared.TestCase;
import at.tugraz.ist.stracke.jsr.core.tsr.ReducedTestSuite;
import at.tugraz.ist.stracke.jsr.test.TSRData;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

class GeneticReductionStrategyTest {

  @Test
  void testReduce01() {
    ReductionStrategy strategy = new GeneticReductionStrategy(TSRData.smallOriginalTS,
                                                              TSRData.smallCoverageReport);
    ReducedTestSuite rts = strategy.reduce();

    List<TestCase> union = new ArrayList<>(rts.testCases);
    union.addAll(rts.removedTestCases);

    assertThat(union.size(), is(equalTo(TSRData.smallOriginalTS.testCases.size())));
    assertThat(TSRData.smallOriginalTS.testCases.containsAll(union), is(true));
    assertThat(rts.testCases, contains(TSRData.t4, TSRData.t5));
  }

  @Test
  void testGetFitness() {
    GeneticReductionStrategy strategy = new GeneticReductionStrategy(TSRData.smallOriginalTS,
                                                                     TSRData.smallCoverageReport);
    //Genotype<BitGene> geneGenotype = Genotype.of(BitChromosome.of(BitSet.valueOf([0,1,1,0,0,1])));

    strategy.maxNumberOfSatisfyingTestCasesPerUnit = 10;

    int fitness = strategy.getFitness(null);

    assertThat(fitness, is(equalTo(10)));
  }
}