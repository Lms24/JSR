package at.tugraz.ist.stracke.jsr.core.tsr.strategies;

import at.tugraz.ist.stracke.jsr.core.shared.TestCase;
import at.tugraz.ist.stracke.jsr.core.tsr.ReducedTestSuite;
import at.tugraz.ist.stracke.jsr.test.TSRData;
import io.jenetics.BitChromosome;
import io.jenetics.BitGene;
import io.jenetics.Genotype;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.BitSet;
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
  void testGetFitnessInvalidSelection() {
    GeneticReductionStrategy strategy = new GeneticReductionStrategy(TSRData.smallOriginalTS,
                                                                     TSRData.smallCoverageReport);
    final BitSet bits = new BitSet(5);
    bits.set(0, true);
    bits.set(1, false);
    bits.set(2, false);
    bits.set(3, false);
    bits.set(4, false);
    Genotype<BitGene> geneGenotype = Genotype.of(BitChromosome.of(bits, 5));

    int fitness = strategy.getFitness(geneGenotype);

    assertThat(fitness, is(equalTo(0)));
  }

  @Test
  void testGetFitnessBadSelection() {
    GeneticReductionStrategy strategy = new GeneticReductionStrategy(TSRData.smallOriginalTS,
                                                                     TSRData.smallCoverageReport);
    final BitSet bits = new BitSet(5);
    bits.set(0, true);
    bits.set(1, true);
    bits.set(2, true);
    bits.set(3, true);
    bits.set(4, true);
    Genotype<BitGene> geneGenotype = Genotype.of(BitChromosome.of(bits, 5));

    int fitness = strategy.getFitness(geneGenotype);

    assertThat(fitness, is(equalTo(2))); // LS (26.10.) relaxed duplicate punishment
  }

  @Test
  void testGetFitnessNormalSelection() {
    GeneticReductionStrategy strategy = new GeneticReductionStrategy(TSRData.smallOriginalTS,
                                                                     TSRData.smallCoverageReport);
    final BitSet bits = new BitSet(5);
    bits.set(0, true);
    bits.set(1, true);
    bits.set(2, false);
    bits.set(3, true);
    bits.set(4, false);
    Genotype<BitGene> geneGenotype = Genotype.of(BitChromosome.of(bits, 5));

    int fitness = strategy.getFitness(geneGenotype);

    assertThat(fitness, is(equalTo(5))); // LS (26.10.) relaxed duplicate punishment
  }

  @Test
  void testGetFitnessBestSelection() {
    GeneticReductionStrategy strategy = new GeneticReductionStrategy(TSRData.smallOriginalTS,
                                                                     TSRData.smallCoverageReport);
    final BitSet bits = new BitSet(5);
    bits.set(0, false);
    bits.set(1, false);
    bits.set(2, false);
    bits.set(3, true);
    bits.set(4, true);
    Genotype<BitGene> geneGenotype = Genotype.of(BitChromosome.of(bits, 5));

    int fitness = strategy.getFitness(geneGenotype);

    assertThat(fitness, is(equalTo(6)));
  }
}