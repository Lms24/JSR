package at.tugraz.ist.stracke.jsr.core.tsr.strategies;

import at.tugraz.ist.stracke.jsr.core.coverage.CoverageReport;
import at.tugraz.ist.stracke.jsr.core.shared.TestCase;
import at.tugraz.ist.stracke.jsr.core.shared.TestSuite;
import at.tugraz.ist.stracke.jsr.core.tsr.ReducedTestSuite;
import at.tugraz.ist.stracke.jsr.core.tsr.TSRTestCase;
import io.jenetics.*;
import io.jenetics.engine.Engine;
import io.jenetics.engine.EvolutionStatistics;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.*;
import java.util.stream.Collectors;

import static io.jenetics.engine.EvolutionResult.toBestPhenotype;
import static io.jenetics.engine.Limits.bySteadyFitness;

/**
 * This ReductionStrategy implements a genetic TSR algorithm using the Jenetics library.
 * <p>
 * The idea is to view test cases as a bit vector i.e. a chromosome. The initial population is
 * a set of is a randomly generated bit vectors of where each bit vector denotes a
 * selected sub-set of test cases:
 * If one bit (i.e. a gene) is set to true, it means that the test case is selected,
 * if to 0, it is not selected. Each bit vector has a length of the total number of test cases.
 * <p>
 * To evaluate the fitness of a bit vector, we check if the given TC selection satisfies
 * all satisfiable requirements and if yes, how minimal the selection is. This gives us the
 * evolutionarily best result if all requirements are satisfied but with the fewest test cases
 * possible.
 * <p>
 * Evolution happens over generations with the following strategies:
 * Selection: Roulette Wheel strategy
 * Alteration: Mutation & Single Point Crossover
 *
 * @see <a href="https://jenetics.io/">Jenetics library website</a>
 */
public class GeneticReductionStrategy extends BaseReductionStrategy {

  private static final int POPULATION_SIZE = 500;

  private static final double PROB_BIT_GENE_TRUE_INIT = 0.15;
  private static final double PROB_MUTATOR = 0.55;
  private static final double PROB_ROULETTE = 0.15;

  private static final int LIMIT_STEADY_FITNESS = 7;
  private static final int LIMIT_MAX_GENERATIONS = 100;

  private final Logger logger = LogManager.getLogger(GeneticReductionStrategy.class);

  public GeneticReductionStrategy(@NonNull TestSuite testSuite,
                                  @NonNull CoverageReport coverageReport) {
    super(testSuite, coverageReport);
  }

  public GeneticReductionStrategy() {
  }

  @Override
  public @NonNull ReducedTestSuite reduce() {
    final int chromosomeLength = coverageReport.testCaseCoverageData.keySet().size();

    Engine<BitGene, Integer> engine =
      Engine.builder(this::getFitness,
                     BitChromosome.of(chromosomeLength, PROB_BIT_GENE_TRUE_INIT))
            .populationSize(POPULATION_SIZE)
            .selector(new RouletteWheelSelector<>())
            .alterers(new Mutator<>(PROB_MUTATOR),
                      new SinglePointCrossover<>(PROB_ROULETTE))
        .build();

    final EvolutionStatistics<Integer, ?> statistics = EvolutionStatistics.ofNumber();

    final Phenotype<BitGene, Integer> bestFit =
      engine.stream()
            .limit(bySteadyFitness(LIMIT_STEADY_FITNESS))
            .limit(LIMIT_MAX_GENERATIONS)
            .peek(statistics)
            .collect(toBestPhenotype());

    logger.debug("Jenetics Statistics:");
    statistics.toString().lines().forEach(logger::debug);
    logger.debug("Best Result:");
    logger.debug(bestFit);

    List<TSRTestCase> retainedTsrTCs = this.getTestCaseSelectionFromBitGenotype(bestFit.genotype());
    List<TestCase> retainedTCs =
      this.originalTestsuite.testCases.stream()
                                      .filter(tc -> retainedTsrTCs.stream()
                                                                  .anyMatch(tsrTC -> tc.getFullName()
                                                                                       .equals(tsrTC.getFullName())))
                                      .collect(Collectors.toList());

    retainedTCs.forEach(tc -> logger.debug("Keeping {}", tc.getName()));
    final List<TestCase> removedTCs = super.getRemovedTCs(retainedTCs);

    return new ReducedTestSuite(retainedTCs, removedTCs);
  }

  Integer getFitness(final Genotype<BitGene> geneGenotype) {
    final List<TSRTestCase> tcSelection = this.getTestCaseSelectionFromBitGenotype(geneGenotype);

    if (!tcCollectionSatisfiesAllReqs(tcSelection)) {
      return 0;
    }

    return getFitnessFromSelection(tcSelection);
  }

  int getFitnessFromSelection(List<TSRTestCase> tcSelection) {
    List<CoverageReport.Unit> allCoveredReqs = tcSelection.stream()
                                                          .map(this::getRequirementsSatisfiedByTestCase)
                                                          .flatMap(Collection::stream)
                                                          .collect(Collectors.toList());

    Set<CoverageReport.Unit> uniqueCoveredReqs = new HashSet<>(allCoveredReqs);

    List<Integer> numberOfDuplicatedCoveredReqs = uniqueCoveredReqs.stream()
                                                                   .map(u -> Collections.frequency(allCoveredReqs, u))
                                                                   .filter(f -> f > 1)
                                                                   .map(f -> f - 1)
                                                                   .collect(Collectors.toList());

    int sumDuplicates = numberOfDuplicatedCoveredReqs.stream()
                                                     .mapToInt(i -> i)
                                                     .sum();

    return Math.max(uniqueCoveredReqs.size() - sumDuplicates, 1);
  }

  private List<TSRTestCase> getTestCaseSelectionFromBitGenotype(final Genotype<BitGene> geneGenotype) {
    final List<Boolean> vector = geneGenotype.chromosome()
                                             .as(BitChromosome.class)
                                             .stream()
                                             .map(BitGene::bit)
                                             .collect(Collectors.toList());

    final List<TSRTestCase> sortedTestCases = this.coverageReport.getSortedTestCases();

    List<TSRTestCase> tcSelection = new ArrayList<>();
    for (int i = 0; i < vector.size(); i++) {
      if (vector.get(i)) {
        tcSelection.add(sortedTestCases.get(i));
      }
    }

    return tcSelection;
  }

  private boolean tcCollectionSatisfiesAllReqs(List<TSRTestCase> selection) {
    // deep copy the requirements/units list
    Set<CoverageReport.Unit> unmarkedRequirements = new HashSet<>(this.table.columnKeySet());

    selection.forEach(tc -> {
      List<CoverageReport.Unit> satisfiedReqs = this.getRequirementsSatisfiedByTestCase(tc);
      satisfiedReqs.forEach(unmarkedRequirements::remove);
    });

    return unmarkedRequirements.isEmpty();
  }
}
