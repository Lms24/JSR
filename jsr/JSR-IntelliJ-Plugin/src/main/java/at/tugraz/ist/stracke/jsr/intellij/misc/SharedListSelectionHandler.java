package at.tugraz.ist.stracke.jsr.intellij.misc;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.plaf.basic.BasicListUI;

/*
public class SharedListSelectionHandler implements ListSelectionListener {

    public void valueChanged(ListSelectionEvent e) {
      ListSelectionModel lsm = (ListSelectionModel)e.getSource();

      int firstIndex = e.getFirstIndex();
      int lastIndex = e.getLastIndex();
      boolean isAdjusting = e.getValueIsAdjusting();
      output.append("Event for indexes "
                    + firstIndex + " - " + lastIndex
                    + "; isAdjusting is " + isAdjusting
                    + "; selected indexes:");

      if (lsm.isSelectionEmpty()) {
        output.append(" <none>");
      } else {
        // Find out which indexes are selected.
        int minIndex = lsm.getMinSelectionIndex();
        int maxIndex = lsm.getMaxSelectionIndex();
        for (int i = minIndex; i <= maxIndex; i++) {
          if (lsm.isSelectedIndex(i)) {
            output.append(" " + i);
          }
        }
      }
      output.append(newline);
    }
}
*/