// Copyright 2000-2020 JetBrains s.r.o. and other contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

package at.tugraz.ist.stracke.jsr.intellij;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.ui.TextBrowseFolderListener;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.openapi.util.IconLoader;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.impl.status.widget.StatusBarWidgetWrapper;
import com.intellij.util.PlatformIcons;
import org.apache.xmlbeans.impl.xb.xsdschema.All;

import javax.swing.*;
import javax.swing.plaf.IconUIResource;
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

  public JSRToolWindow(ToolWindow toolWindow) {
    initIcons();
    listenToPanelClicks(lblSettingsWrapper, pnlSettings);
    listenToPanelClicks(lblParamsWrapper, pnlParams);
  }

  public JPanel getContent() {
    return jsrToolWindowContent;
  }

  private void initIcons() {
    lblSettingsWrapper.setIcon(AllIcons.Ide.Notification.Expand);
    lblParamsWrapper.setIcon(AllIcons.Ide.Notification.Expand);
  }

  private void listenToPanelClicks(JLabel wrapperLabel, JPanel panel) {
    wrapperLabel.addMouseListener(new MouseAdapter() {
      @Override
      public void mouseClicked(MouseEvent e) {
        boolean nowVisible = !panel.isVisible();
        panel.setVisible(nowVisible);
        final Icon newIcon = nowVisible ? AllIcons.Ide.Notification.Expand : AllIcons.Actions.ArrowExpand;
        wrapperLabel.setIcon(newIcon);
        super.mouseClicked(e);
      }
    });
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
