// Copyright 2000-2020 JetBrains s.r.o. and other contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

package at.tugraz.ist.stracke.jsr.intellij;

import at.tugraz.ist.stracke.jsr.core.coverage.CoverageReport;
import at.tugraz.ist.stracke.jsr.core.shared.TestCase;
import at.tugraz.ist.stracke.jsr.core.tsr.ReducedTestSuite;
import at.tugraz.ist.stracke.jsr.intellij.misc.CoverageMetric;
import at.tugraz.ist.stracke.jsr.intellij.misc.CoverageReportListItem;
import at.tugraz.ist.stracke.jsr.intellij.misc.ReductionAlgorithm;
import at.tugraz.ist.stracke.jsr.intellij.model.CoverageReportComboboxModel;
import at.tugraz.ist.stracke.jsr.intellij.services.ReductionService;
import at.tugraz.ist.stracke.jsr.intellij.state.StateService;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.ui.TextBrowseFolderListener;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.impl.JavaPsiFacadeEx;
import com.intellij.psi.impl.JavaPsiFacadeImpl;
import com.intellij.ui.AnimatedIcon;
import com.intellij.ui.CollectionListModel;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import java.awt.event.*;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

public class JSRToolWindow {

  private final Project project;
  private JPanel jsrToolWindowContent;
  private JPanel pnlSettings;
  private TextFieldWithBrowseButton tfTestPath;
  private JLabel lblTestSources;
  private JLabel lblMainSources;
  private TextFieldWithBrowseButton tfSourcePath;
  private JLabel lblJarFile;
  private TextFieldWithBrowseButton tfJarPath;
  private JLabel lblClassesPath;
  private TextFieldWithBrowseButton tfClassesPath;
  private JLabel lblOutPath;
  private TextFieldWithBrowseButton tfOutputPath;
  private JLabel lblSlicerPath;
  private TextFieldWithBrowseButton tfSlicerPath;
  private JLabel lblSerialDir;
  private TextFieldWithBrowseButton tfSerialPath;
  private JLabel lblBasePackage;
  private JButton btnStartTSR;
  private JTextField tfBasePackage;
  private JPanel pnlSettingsWrapper;
  private JLabel lblSettingsWrapper;
  private JPanel pnlParamsWrapper;
  private JLabel lblParamsWrapper;
  private ComboBox<String> cbCovMetric;
  private JLabel lblCovMetric;
  private JLabel lblReductionAlg;
  private ComboBox<String> cbReductionAlg;
  private JPanel pnlParams;
  private JLabel lblStatus;
  private JCheckBox chkDeactivate;
  private JPanel pnlResultsWrapper;
  private JLabel lblResultsWrapper;
  private JList<String> lstRetained;
  private JList<String> lstRemoved;
  private JLabel lblRetained;
  private JLabel lblRemoved;
  private JPanel pnlResults;
  private JScrollBar scrollBar1;
  private JRadioButton rbNewRep;
  private JRadioButton rbOldRep;
  private JPanel pnlReportSel;
  private ComboBox<CoverageReportListItem> cbOldCovReport;
  private JLabel lblSelOld;
  private JPanel pnlStart;
  private StateService.State state;

  private CoverageReportListItem selItem;

  public JSRToolWindow(ToolWindow toolWindow, Project project) {
    this.project = project;
    listenToPanelClicks(lblSettingsWrapper, pnlSettings, "settings");
    listenToPanelClicks(lblParamsWrapper, pnlParams, "params");
    listenToPanelClicks(lblResultsWrapper, pnlResults, "results");
    initStatePersistence();
    initComponentStateFromPersistedState();
    listenToInputChanges();
    initStartTsrButton();
    initRadioButtons();
    loadCoverageReports();
  }

  private void listenToInputChanges() {
    this.listenToTextFieldChanges(tfTestPath.getTextField());
    this.listenToTextFieldChanges(tfSourcePath.getTextField());
    this.listenToTextFieldChanges(tfJarPath.getTextField());
    this.listenToTextFieldChanges(tfClassesPath.getTextField());
    this.listenToTextFieldChanges(tfSlicerPath.getTextField());
    this.listenToTextFieldChanges(tfBasePackage);

    tfTestPath.addActionListener(e -> persistState());
    tfSourcePath.addActionListener(e -> persistState());
    tfJarPath.addActionListener(e -> persistState());
    tfClassesPath.addActionListener(e -> persistState());
    tfSlicerPath.addActionListener(e -> persistState());
    tfSerialPath.addActionListener(e -> persistState());

    /* tfOutputPath is special as we need to look for
       coverage reports inside the changed directory */
    tfOutputPath.getTextField().addFocusListener(new FocusListener() {
      @Override
      public void focusGained(FocusEvent focusEvent) {
      }

      @Override
      public void focusLost(FocusEvent focusEvent) {
        persistState();
        loadCoverageReports();
      }
    });

    tfOutputPath.addActionListener(e -> {
      persistState();
      this.loadCoverageReports();
    });

    rbNewRep.addActionListener(e -> persistState());
    rbOldRep.addActionListener(e -> persistState());

    cbReductionAlg.addActionListener(e -> persistState());
    cbCovMetric.addActionListener(e -> persistState());
    cbOldCovReport.addActionListener(e -> persistState());
  }

  private void listenToTextFieldChanges(JTextField textField) {
    textField.addFocusListener(new FocusListener() {
      @Override
      public void focusGained(FocusEvent focusEvent) {
      }

      @Override
      public void focusLost(FocusEvent focusEvent) {
        persistState();
      }
    });
  }

  public JPanel getContent() {
    return jsrToolWindowContent;
  }

  private void initStartTsrButton() {
    this.btnStartTSR.addActionListener((actionEvent) -> {
      persistState();
      ResourceBundle rb = ResourceBundle.getBundle("i18n/en");

      lblStatus.setVisible(true);
      lblStatus.setText(rb.getString("status.loading"));
      lblStatus.setIcon(new AnimatedIcon.Default());

      pnlResultsWrapper.setVisible(false);
      togglePanel(pnlResults, lblResultsWrapper, false, "results");

      this.selItem = state.useLastCoverageReport ? cbOldCovReport.getItem() : null;
      CoverageReport oldReport = state.useLastCoverageReport ? cbOldCovReport.getItem().coverageReport : null;
      new Thread(() -> startTSR(oldReport)).start();
    });
  }

  private void startTSR(CoverageReport oldReport) {
    ReductionService reductionService = project.getService(ReductionService.class);
    ReducedTestSuite rts;

    try {
      rts = reductionService.startTSReduction(state.pathTestSources,
                                              state.pathSources,
                                              state.pathJar,
                                              state.pathClasses,
                                              state.pathSlicer,
                                              state.pathOutput,
                                              state.pathSerialOut,
                                              state.basePackage,
                                              state.coverageMetric,
                                              state.reductionAlgorithm,
                                              state.deactivateTCs,
                                              oldReport);
    } catch (Exception ex) {
      ex.printStackTrace();
      rts = null;
    }

    ReducedTestSuite finalRts = rts;
    ApplicationManager.getApplication().invokeLater(() -> onReductionPerformed(finalRts));
  }

  public void onReductionPerformed(ReducedTestSuite rts) {
    ResourceBundle rb = ResourceBundle.getBundle("i18n/en");
    boolean success = (rts != null);

    if (success) {
      lblStatus.setText(rb.getString("status.success"));
      lblStatus.setIcon(AllIcons.Actions.Checked);
      this.togglePanel(pnlSettings, lblSettingsWrapper, false, "settings");
      this.togglePanel(pnlParams, lblParamsWrapper, false, "params");
      this.showTSRResults(rts);
      this.loadCoverageReports();
    } else {
      lblStatus.setText(rb.getString("status.error"));
      lblStatus.setIcon(AllIcons.General.BalloonError);
    }
  }

  private void showTSRResults(ReducedTestSuite rts) {
    this.pnlResultsWrapper.setVisible(true);
    this.togglePanel(pnlResults, lblResultsWrapper, true, "results");

    List<TestCase> sortedRetainedTCs = rts.testCases.stream()
                                                    .sorted(Comparator.comparing(TestCase::getFullName))
                                                    .collect(Collectors.toList());
    List<TestCase> sortedRemovedTCs = rts.removedTestCases.stream()
                                                          .sorted(Comparator.comparing(TestCase::getFullName))
                                                          .collect(Collectors.toList());

    List<String> retainedTCs = sortedRetainedTCs.stream()
                                                .map(tc -> {
                                                  String[] tmp = tc.getFullName().split("\\.");
                                                  String[] classAndMethod = tmp[tmp.length - 1].split(":");
                                                  return String.format("%s::%s", classAndMethod[0], classAndMethod[1]);
                                                })
                                                .collect(Collectors.toList());

    List<String> removedTCs = sortedRemovedTCs.stream()
                                              .map(tc -> {
                                                String[] tmp = tc.getFullName().split("\\.");
                                                String[] classAndMethod = tmp[tmp.length - 1].split(":");
                                                return String.format("%s::%s",
                                                                     classAndMethod[0],
                                                                     classAndMethod[1]);
                                              })
                                              .sorted()
                                              .collect(Collectors.toList());

    ListModel<String> retainedModel = new CollectionListModel<>(retainedTCs);
    ListModel<String> removedModel = new CollectionListModel<>(removedTCs);

    this.lstRetained.setModel(retainedModel);
    this.lstRemoved.setModel(removedModel);

    AtomicLong lastEvent = new AtomicLong();
    if (lstRetained.getListSelectionListeners().length == 0) {
      lstRetained.addListSelectionListener(listSelectionEvent -> onTestCaseSelect(sortedRetainedTCs,
                                                                                  lastEvent,
                                                                                  listSelectionEvent));
    }

    if (lstRemoved.getListSelectionListeners().length == 0) {
      lstRemoved.addListSelectionListener(listSelectionEvent -> onTestCaseSelect(sortedRemovedTCs,
                                                                                 lastEvent,
                                                                                 listSelectionEvent));
    }
  }

  private void onTestCaseSelect(List<TestCase> sortedRetainedTCs,
                                AtomicLong lastEvent,
                                ListSelectionEvent listSelectionEvent) {
    long eventFiredAt = System.currentTimeMillis();
    if (eventFiredAt - lastEvent.get() <= 500) {
      return;
    }

    // Since the ListSelectionEvent is designed for multiple
    // selections it behaves weirdly with single selections
    // Thus we have to check which index was actually selected
    final int firstIndex = listSelectionEvent.getFirstIndex();
    final int lastIndex = listSelectionEvent.getLastIndex();
    final JList<?> lsm = (JList<?>) listSelectionEvent.getSource();
    int index = firstIndex;
    for (int i = firstIndex; i <= lastIndex; i++) {
      if (lsm.isSelectedIndex(i)) {
        index = i;
        break;
      }
    }

    TestCase selTc = sortedRetainedTCs.get(index);
    navigateToTestCaseInEditor(selTc);
    lastEvent.set(eventFiredAt);
  }

  private void navigateToTestCaseInEditor(TestCase selTc) {
    JavaPsiFacadeEx facade = new JavaPsiFacadeImpl(project);
    PsiClass tcClass = facade.findClass(selTc.getClassName());
    VirtualFile tcFile = tcClass.getContainingFile().getVirtualFile();
    PsiMethod tcMethod = tcClass.findMethodsByName(selTc.getName(), true)[0];

    OpenFileDescriptor open = new OpenFileDescriptor(project, tcFile, tcMethod.getTextOffset());
    open.navigate(true);
  }

  private void persistState() {
    ResourceBundle rb = ResourceBundle.getBundle("i18n/en");

    this.state.pathTestSources = tfTestPath.getText();
    this.state.pathSources = tfSourcePath.getText();
    this.state.pathJar = tfJarPath.getText();
    this.state.pathClasses = tfClassesPath.getText();
    this.state.pathSlicer = tfSlicerPath.getText();
    this.state.pathOutput = tfOutputPath.getText();
    this.state.pathSerialOut = tfSerialPath.getText();

    this.state.basePackage = tfBasePackage.getText();

    this.state.useLastCoverageReport = this.rbOldRep.isSelected();
    this.state.deactivateTCs = this.chkDeactivate.isSelected();

    this.state.settingsExpanded = this.pnlSettings.isVisible();
    this.state.paramsExpanded = this.pnlParams.isVisible();
    this.state.paramsExpanded = this.pnlResults.isVisible();

    final String selCovItemTxt = this.cbCovMetric.getItem();
    if (rb.getString("cov.line").equals(selCovItemTxt)) {
      this.state.coverageMetric = CoverageMetric.LINE_COVERAGE;
    } else if (rb.getString("cov.method").equals(selCovItemTxt)) {
      this.state.coverageMetric = CoverageMetric.METHOD_COVERAGE;
    } else {
      this.state.coverageMetric = CoverageMetric.CHECKED_COVERAGE;
    }

    final String selAlgoItemTxt = this.cbReductionAlg.getItem();
    if (rb.getString("alg.gen").equals(selAlgoItemTxt)) {
      this.state.reductionAlgorithm = ReductionAlgorithm.GENETIC;
    } else {
      this.state.reductionAlgorithm = ReductionAlgorithm.GREEDY_HGS;
    }
  }

  private void initComponentStateFromPersistedState() {
    ResourceBundle rb = ResourceBundle.getBundle("i18n/en");

    this.togglePanel(pnlSettings, lblSettingsWrapper, state.settingsExpanded, "settings");
    this.togglePanel(pnlParams, lblParamsWrapper, state.paramsExpanded, "params");
    // results panel should always be closed initially, as long as we do not
    // persist the lists from last run (which I really don't want to bother with atm)
    this.togglePanel(pnlResults, lblResultsWrapper, false, "results");
    this.pnlResultsWrapper.setVisible(false);

    this.tfTestPath.setText(state.pathTestSources);
    this.tfSourcePath.setText(state.pathSources);
    this.tfJarPath.setText(state.pathJar);
    this.tfClassesPath.setText(state.pathClasses);
    this.tfSlicerPath.setText(state.pathSlicer);
    this.tfOutputPath.setText(state.pathOutput);
    this.tfSerialPath.setText(state.pathSerialOut);

    this.tfBasePackage.setText(state.basePackage);

    String covMetricItem;
    switch (state.coverageMetric) {
      case LINE_COVERAGE:
        covMetricItem = rb.getString("cov.line");
        break;
      case METHOD_COVERAGE:
        covMetricItem = rb.getString("cov.method");
        break;
      default:
        covMetricItem = rb.getString("cov.checked");
    }
    this.cbCovMetric.setItem(covMetricItem);

    String redAlgoItem;
    switch (state.reductionAlgorithm) {
      case GENETIC:
        redAlgoItem = rb.getString("alg.gen");
        break;
      case GREEDY_HGS:
      default:
        redAlgoItem = rb.getString("alg.hgs");
    }
    this.cbReductionAlg.setItem(redAlgoItem);

    this.chkDeactivate.setSelected(state.deactivateTCs);

    this.toggleReportSelection(state.useLastCoverageReport);

    this.lblStatus.setVisible(false);
  }

  private void initStatePersistence() {
    this.state = project.getService(StateService.class).getState();
  }

  private void listenToPanelClicks(JLabel wrapperLabel, JPanel panel, String panelName) {
    wrapperLabel.addMouseListener(new MouseAdapter() {
      @Override
      public void mouseClicked(MouseEvent e) {
        togglePanel(panel, wrapperLabel, panelName);
        super.mouseClicked(e);
      }
    });
  }

  private void togglePanel(JPanel panel, JLabel wrapperLabel, String panelName) {
    boolean nowVisible = !panel.isVisible();
    togglePanel(panel, wrapperLabel, nowVisible, panelName);
  }

  private void togglePanel(JPanel panel, JLabel wrapperLabel, boolean nowVisible, String panelName) {
    panel.setVisible(nowVisible);
    final Icon newIcon = nowVisible ? AllIcons.Ide.Notification.Expand : AllIcons.Actions.ArrowExpand;
    wrapperLabel.setIcon(newIcon);

    if (state != null) {
      switch (panelName) {
        case "settings":
          state.settingsExpanded = nowVisible;
          break;
        case "params":
          state.paramsExpanded = nowVisible;
          break;
        case "results":
          state.resultsExpanded = nowVisible;
          break;
      }
    }
  }

  /**
   * Entry point for component creation
   */
  private void createUIComponents() {
    initFileBrowsers();
    initComboBoxes();
  }

  private void initFileBrowsers() {
    tfTestPath = new TextFieldWithBrowseButton();
    tfSourcePath = new TextFieldWithBrowseButton();
    tfJarPath = new TextFieldWithBrowseButton();
    tfClassesPath = new TextFieldWithBrowseButton();
    tfSlicerPath = new TextFieldWithBrowseButton();
    tfSerialPath = new TextFieldWithBrowseButton();
    tfOutputPath = new TextFieldWithBrowseButton(actionEvent -> {
      this.persistState();
      this.loadCoverageReports();
    });

    FileChooserDescriptor chooseFolderDescriptor = new FileChooserDescriptor(false,
                                                                             true,
                                                                             false,
                                                                             false,
                                                                             false,
                                                                             false).withShowHiddenFiles(true);
    FileChooserDescriptor chooseJarDescriptor = new FileChooserDescriptor(false,
                                                                          false,
                                                                          true,
                                                                          false,
                                                                          false,
                                                                          false).withShowHiddenFiles(true);

    tfTestPath.addBrowseFolderListener(new TextBrowseFolderListener(chooseFolderDescriptor));
    tfSourcePath.addBrowseFolderListener(new TextBrowseFolderListener(chooseFolderDescriptor));
    tfJarPath.addBrowseFolderListener(new TextBrowseFolderListener(chooseJarDescriptor));
    tfClassesPath.addBrowseFolderListener(new TextBrowseFolderListener(chooseFolderDescriptor));
    tfSlicerPath.addBrowseFolderListener(new TextBrowseFolderListener(chooseFolderDescriptor));
    tfOutputPath.addBrowseFolderListener(new TextBrowseFolderListener(chooseFolderDescriptor));
    tfSerialPath.addBrowseFolderListener(new TextBrowseFolderListener(chooseFolderDescriptor));
  }

  private void initComboBoxes() {
    ResourceBundle rb = ResourceBundle.getBundle("i18n/en");
    List<String> covMetricItems = Arrays.asList(
      rb.getString("cov.checked"),
      rb.getString("cov.line"),
      rb.getString("cov.method")
    );

    List<String> algorithmItems = Arrays.asList(
      rb.getString("alg.hgs"),
      rb.getString("alg.gen")
    );

    cbCovMetric = new ComboBox<>(covMetricItems.toArray(new String[0]));
    cbReductionAlg = new ComboBox<>(algorithmItems.toArray(new String[0]));
  }

  private void initRadioButtons() {
    rbOldRep.addActionListener(actionEvent -> {
      if (rbOldRep.isSelected()) {
        this.toggleReportSelection(true);
      } else {
        this.rbOldRep.setSelected(true);
      }
    });

    rbNewRep.addActionListener(actionEvent -> {
      if (rbNewRep.isSelected()) {
        this.toggleReportSelection(false);
      } else {
        this.rbNewRep.setSelected(true);
      }
    });
  }

  private void toggleReportSelection(boolean useOldReport) {
    this.rbOldRep.setSelected(useOldReport);
    this.rbNewRep.setSelected(!useOldReport);

    this.lblCovMetric.setVisible(!useOldReport);
    this.cbCovMetric.setVisible(!useOldReport);
    this.lblSelOld.setVisible(useOldReport);
    this.cbOldCovReport.setVisible(useOldReport);
  }

  private void loadCoverageReports() {
    ReductionService reductionService = project.getService(ReductionService.class);
    Path reportsPath = Path.of(this.state.pathOutput, "coverage");
    System.out.println("loading crs " + reportsPath);
    List<CoverageReport> reports = reductionService.loadCoverageReportsFromDisk(reportsPath)
                                                   .stream()
                                                   .sorted(Comparator.comparing(coverageReport -> coverageReport.createdAt))
                                                   .collect(Collectors.toList());
    Collections.reverse(reports);
    System.out.println("got " + reports.size() + " reports");
    if (reports.isEmpty()) {
      this.toggleReportSelection(false);
      this.rbOldRep.setEnabled(false);
      return;
    }

    this.rbOldRep.setEnabled(true);

    List<CoverageReportListItem> reportItems = reports.stream()
                                                      .map(CoverageReportListItem::new)
                                                      .collect(Collectors.toList());

    ComboBoxModel<CoverageReportListItem> reportItemsModel = new CoverageReportComboboxModel(reportItems);

    this.cbOldCovReport.setModel(reportItemsModel);
    this.cbOldCovReport.setItem(this.selItem != null ? this.selItem : reportItems.get(0));
  }
}
