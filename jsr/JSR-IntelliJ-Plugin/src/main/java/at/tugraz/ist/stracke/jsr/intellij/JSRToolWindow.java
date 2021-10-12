// Copyright 2000-2020 JetBrains s.r.o. and other contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

package at.tugraz.ist.stracke.jsr.intellij;

import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.ui.TextBrowseFolderListener;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.openapi.wm.ToolWindow;

import javax.swing.*;
import java.awt.*;

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
  private JComboBox cbCovMetric;
  private JLabel lblCovMetric;
  private JLabel lblReductionAlg;
  private JComboBox cbReductionAlg;
  private JCheckBox chkLastReport;

  public JSRToolWindow(ToolWindow toolWindow) {
  }

  public JPanel getContent() {
    return jsrToolWindowContent;
  }

  private void createUIComponents() {
    tfTestPath = new TextFieldWithBrowseButton();
    tfSourcePath = new TextFieldWithBrowseButton();
    tfJarPath = new TextFieldWithBrowseButton();
    tfClassesPath = new TextFieldWithBrowseButton();
    tfSlicerPath = new TextFieldWithBrowseButton();
    tfOutputPath = new TextFieldWithBrowseButton();
    tfSerialPath = new TextFieldWithBrowseButton();
    //tfBasePackage = new JTextField();

    FileChooserDescriptor chooseFolderDescriptor = new FileChooserDescriptor(false,
                                                                             true,
                                                                             false,
                                                                             false,
                                                                             false,
                                                                             false);
    FileChooserDescriptor chooseJarDescriptor = new FileChooserDescriptor(false,
                                                                           false,
                                                                           true,
                                                                           false,
                                                                           false,
                                                                           false);

    tfTestPath.addBrowseFolderListener(new TextBrowseFolderListener(chooseFolderDescriptor));
    tfSourcePath.addBrowseFolderListener(new TextBrowseFolderListener(chooseFolderDescriptor));
    tfJarPath.addBrowseFolderListener(new TextBrowseFolderListener(chooseJarDescriptor));
    tfClassesPath.addBrowseFolderListener(new TextBrowseFolderListener(chooseFolderDescriptor));
    tfSlicerPath.addBrowseFolderListener(new TextBrowseFolderListener(chooseFolderDescriptor));
    tfOutputPath.addBrowseFolderListener(new TextBrowseFolderListener(chooseFolderDescriptor));
    tfSerialPath.addBrowseFolderListener(new TextBrowseFolderListener(chooseFolderDescriptor));
  }
}
