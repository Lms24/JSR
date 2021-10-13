// Copyright 2000-2020 JetBrains s.r.o. and other contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

package at.tugraz.ist.stracke.jsr.intellij;

import at.tugraz.ist.stracke.jsr.intellij.misc.CoverageMetric;
import at.tugraz.ist.stracke.jsr.intellij.misc.ReductionAlgorithm;
import at.tugraz.ist.stracke.jsr.intellij.state.StateService;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.ui.TextBrowseFolderListener;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.openapi.wm.ToolWindow;

import javax.swing.*;
import java.awt.*;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;

public class JSRToolWindow {

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
  private JCheckBox chkLastReport;
  private JPanel pnlParams;

  private Project project;
  private StateService.State state;

  public JSRToolWindow(ToolWindow toolWindow, Project project) {
    this.project = project;
    listenToPanelClicks(lblSettingsWrapper, pnlSettings, "settings");
    listenToPanelClicks(lblParamsWrapper, pnlParams, "params");
    initStatePersistence();
    initComponentStateFromPersistedState();
    listenToInputChanges();
    initStartTsrButton();
  }

  private void listenToInputChanges() {
    this.listenToTextFieldChanges(tfTestPath.getTextField());
    this.listenToTextFieldChanges(tfSourcePath.getTextField());
    this.listenToTextFieldChanges(tfJarPath.getTextField());
    this.listenToTextFieldChanges(tfClassesPath.getTextField());
    this.listenToTextFieldChanges(tfSlicerPath.getTextField());
    this.listenToTextFieldChanges(tfOutputPath.getTextField());
    this.listenToTextFieldChanges(tfBasePackage);

    tfTestPath.addActionListener(actionEvent -> persistState());
    tfSourcePath.addActionListener(actionEvent -> persistState());
    tfJarPath.addActionListener(actionEvent -> persistState());
    tfClassesPath.addActionListener(actionEvent -> persistState());
    tfSlicerPath.addActionListener(actionEvent -> persistState());
    tfOutputPath.addActionListener(actionEvent -> persistState());
    tfSerialPath.addActionListener(actionEvent -> persistState());

    this.chkLastReport.addActionListener(actionEvent -> persistState());
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
      System.out.println("Clicked Button");
      persistState();
    });
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

    this.state.useLastCoverageReport = this.chkLastReport.isSelected();

    this.state.settingsExpanded = this.pnlSettingsWrapper.isVisible();
    this.state.paramsExpanded = this.pnlParamsWrapper.isVisible();

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
      default:
        redAlgoItem = rb.getString("alg.hgs");
    }
    this.cbReductionAlg.setItem(redAlgoItem);

    this.chkLastReport.setSelected(state.useLastCoverageReport);
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
    tfOutputPath = new TextFieldWithBrowseButton();
    tfSerialPath = new TextFieldWithBrowseButton();

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
}
