package at.tugraz.ist.stracke.jsr.intellij.model;

import at.tugraz.ist.stracke.jsr.intellij.misc.CoverageReportListItem;

import javax.swing.*;
import javax.swing.event.ListDataListener;
import java.util.ArrayList;
import java.util.List;

public class CoverageReportComboboxModel implements ComboBoxModel<CoverageReportListItem> {

  private final List<CoverageReportListItem> items;
  private CoverageReportListItem selItem;
  private final List<ListDataListener> dataListeners;

  public CoverageReportComboboxModel(List<CoverageReportListItem> items) {
    this.items = items;
    this.dataListeners = new ArrayList<>();
  }

  @Override
  public void setSelectedItem(Object o) {
    if (!(o instanceof CoverageReportListItem)) {
      throw new IllegalStateException("Wrong Object Type!");
    }
    this.selItem = (CoverageReportListItem) o;
  }

  @Override
  public Object getSelectedItem() {
    return this.selItem;
  }

  @Override
  public int getSize() {
    return this.items.size();
  }

  @Override
  public CoverageReportListItem getElementAt(int i) {
    return this.items.get(i);
  }

  @Override
  public void addListDataListener(ListDataListener listDataListener) {
    this.dataListeners.add(listDataListener);
  }

  @Override
  public void removeListDataListener(ListDataListener listDataListener) {
    this.dataListeners.remove(listDataListener);
  }
}
