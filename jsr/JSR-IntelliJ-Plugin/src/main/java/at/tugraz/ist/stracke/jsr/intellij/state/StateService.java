package at.tugraz.ist.stracke.jsr.intellij.state;

import at.tugraz.ist.stracke.jsr.intellij.misc.CoverageMetric;
import at.tugraz.ist.stracke.jsr.intellij.misc.ReductionAlgorithm;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.components.State;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

@Service({Service.Level.PROJECT})
@State(name = "jsrConfig")
public class StateService implements PersistentStateComponent<StateService.State> {
  private static StateService instance;

  private final Project myProject;
  private State myState = new State();

  private StateService(Project project) {
    myProject = project;
  }

  public static StateService getInstance(Project project) {
    if (instance == null) {
      instance = new StateService(project);
    }
    return instance;
  }

  @Override
  public @Nullable State getState() {
    return myState;
  }

  @Override
  public void loadState(@NotNull State state) {
    myState = state;
  }

  public static class State {

    public String pathTestSources;
    public String pathSources;
    public String pathJar;
    public String pathClasses;
    public String pathSlicer;
    public String pathOutput;
    public String pathSerialOut;
    public String basePackage;
    public CoverageMetric coverageMetric;
    public ReductionAlgorithm reductionAlgorithm;
    public boolean useLastCoverageReport;
    public boolean settingsExpanded;
    public boolean paramsExpanded;
    public boolean resultsExpanded;
    public boolean deactivateTCs;
    public boolean keepZeroCoverageTCs;

    public State() {
      this.pathTestSources = "";
      this.pathSources = "";
      this.pathJar = "";
      this.pathClasses = "";
      this.pathSlicer = "";
      this.pathOutput = "";
      this.pathSerialOut = "";
      this.basePackage = "";
      this.coverageMetric = CoverageMetric.CHECKED_COVERAGE;
      this.reductionAlgorithm = ReductionAlgorithm.GREEDY_HGS;
      this.useLastCoverageReport = false;
      this.settingsExpanded = false;
      this.paramsExpanded = false;
      this.resultsExpanded = false;
      this.deactivateTCs = true;
      this.keepZeroCoverageTCs = false;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;
      State state = (State) o;
      return Objects.equals(pathTestSources, state.pathTestSources) &&
             Objects.equals(pathSources, state.pathSources) &&
             Objects.equals(pathJar, state.pathJar) &&
             Objects.equals(pathClasses, state.pathClasses) &&
             Objects.equals(pathSlicer, state.pathSlicer) &&
             Objects.equals(pathOutput, state.pathOutput) &&
             Objects.equals(pathSerialOut, state.pathSerialOut) &&
             Objects.equals(basePackage, state.basePackage) &&
             Objects.equals(coverageMetric, state.coverageMetric) &&
             Objects.equals(reductionAlgorithm, state.reductionAlgorithm) &&
             Objects.equals(useLastCoverageReport, state.useLastCoverageReport) &&
             Objects.equals(settingsExpanded, state.settingsExpanded) &&
             Objects.equals(paramsExpanded, state.paramsExpanded) &&
             Objects.equals(resultsExpanded, state.resultsExpanded) &&
             Objects.equals(deactivateTCs, state.deactivateTCs) &&
             Objects.equals(keepZeroCoverageTCs, state.keepZeroCoverageTCs);
    }

    @Override
    public int hashCode() {
      return Objects.hash(pathTestSources,
                          pathSources,
                          pathJar,
                          pathClasses,
                          pathSlicer,
                          pathOutput,
                          pathSerialOut,
                          basePackage,
                          coverageMetric,
                          reductionAlgorithm,
                          useLastCoverageReport,
                          settingsExpanded,
                          paramsExpanded,
                          resultsExpanded,
                          deactivateTCs,
                          keepZeroCoverageTCs);
    }
  }
}
