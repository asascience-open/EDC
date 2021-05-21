/*
 * CheckBoxList.java
 *
 * Created on September 21, 2007, 9:06 AM
 *
 * Applied Science Associates, Inc.
 * Copyright 2007.  All rights reserved.
 */
package com.asascience.edc.sos.ui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JPanel;

import com.asascience.edc.sos.VariableContainer;

import net.miginfocom.swing.MigLayout;

/**
 * VariableCheckBoxList.java
 * 
 * @author Kyle Wilcox <kwilcox@asascience.com>
 */
public class VariableCheckBoxList extends JPanel implements ActionListener {

  private List<VariableCheckbox> variableChecks = new ArrayList();
  private JButton btnToggleAll;
  private JButton btnSelectAll;
  private JButton btnSelectNone;
  private int labelLengthLimit = 20;
  private PropertyChangeSupport pcs;

  public VariableCheckBoxList() {
    setLayout(new MigLayout("fillx, wrap 1"));
    pcs = new PropertyChangeSupport(this);
  }

  public VariableCheckBoxList(boolean showToggleAll, boolean showSelectAll, boolean showSelectNone) {
    this();

    JPanel pnlButtons = new JPanel(new MigLayout("insets 0"));
    if (showToggleAll) {
      btnToggleAll = new JButton("Toggle");
      btnToggleAll.addActionListener(new ActionListener() {

        public void actionPerformed(ActionEvent e) {
          toggleAll();
        }
      });
      pnlButtons.add(btnToggleAll);
    }

    if (showSelectAll) {
      btnSelectAll = new JButton("Select All");
      btnSelectAll.addActionListener(new ActionListener() {

        public void actionPerformed(ActionEvent e) {
          selectAll();
        }
      });
      pnlButtons.add(btnSelectAll);
    }

    if (showSelectNone) {
      btnSelectNone = new JButton("Select None");
      btnSelectNone.addActionListener(new ActionListener() {

        public void actionPerformed(ActionEvent e) {
          deselectAll();
        }
      });
      pnlButtons.add(btnSelectNone);
    }

    if (showToggleAll | showSelectAll | showSelectNone) {
      add(pnlButtons, "align center");
    }
  }

  public void clearCBList() {
    for (VariableCheckbox cb : variableChecks) {
      remove(cb);
    }
    variableChecks = new ArrayList<VariableCheckbox>();
  }

  public void makeCBList(List<VariableContainer> variables, boolean trimName) {
    for (VariableContainer v : variables) {
      addCheckBox(v, trimName);
    }
  }

  class VariableCheckbox extends JCheckBox {
    public VariableContainer variableContainer = null;
    public VariableCheckbox() {
      super();
    }
  }

  public void addCheckBox(VariableContainer variable, boolean trimName) {
    VariableCheckbox cb = new VariableCheckbox();
    String t = variable.getProperty();
    if (!variable.getName().equals("")) {
      t = "[" + variable.getName() + "]" + variable.getProperty();
    }
    // String text = (name.length() > labelLengthLimit) ? name.substring(0,
    // labelLengthLimit - 1) + "..." : name;
    String text;
    if (trimName) {
      text = (t.length() > labelLengthLimit) ? t.substring(0, labelLengthLimit - 1) + "..." : t;
    } else {
      text = t;
    }
    // cb.setText(name);//orig
    cb.setText(text);
    cb.variableContainer = variable;
    cb.setToolTipText(variable.getProperty());
    cb.addActionListener(this);
    cb.setActionCommand(variable.getName());
    variableChecks.add(cb);
    add(cb, "span, wrap");
  }

  public String getFullDescription(String descr) {
    for (VariableCheckbox cb : variableChecks) {
      String text = (cb.getText().length() > labelLengthLimit) ? cb.getText().substring(0, labelLengthLimit - 1)
              + "..." : cb.getText();
      if (text.equals(descr)) {
        return cb.getToolTipText();
        // }else if(text.substring(0, labelLengthLimit -
        // 1).equals(name)){
        // return cb.getToolTipText();
      }
    }
    return null;
  }

  // public List<String> getSelectedItems(){
  // List<String> list = new ArrayList();
  // for(JCheckBox cb : itemCBs){
  // if(cb.isSelected()) list.add(cb.getName());
  // }
  //
  // return list;
  // }
  /**
   * This event is fired each time a checkbox is clicked on/off. The selected
   * checkbox is added/removed from the list and a propertyChangeSupport event
   * is fired indicating which action was performed. If the checkbox was:
   * turned ON, "add" is the name of the fired property. turned OFF, "remove"
   * is the name of the fired property.
   *
   * @param e
   *            The <CODE>ActionEvent</CODE>
   */
  public void actionPerformed(ActionEvent e) {
    VariableCheckbox cb = (VariableCheckbox) e.getSource();
    if (cb.variableContainer != null) {
      pcs.firePropertyChange("variableClicked", null, (VariableContainer) cb.variableContainer);
    }
  }

  public void selectSingleItem(String name) {
    for (VariableCheckbox j : variableChecks) {
      if (j.getText().equals(name)) {
        if (j.isSelected()) {
          j.doClick();
        }
        j.doClick();
      }
    }
  }

  /**
   * Toggles the selection state of all of the checkboxes. If a checkbox was
   * selected, it will be deselected and vice versa.
   */
  public void toggleAll() {
    // selItems.clear();

    for (JCheckBox j : variableChecks) {
      j.doClick();
      // j.setSelected(!j.isSelected());
    }
  }

  /**
   * Uncheckes all of the checkboxes.
   */
  public void deselectAll() {
    for (JCheckBox j : variableChecks) {
      if (j.isSelected()) {
        j.doClick();
      }
    }
  }

  /**
   * Checks all of the checkboxes.
   */
  public void selectAll() {
    for (JCheckBox j : variableChecks) {
      if (!j.isSelected()) {
        j.doClick();
      }
    }
  }

  public ArrayList<VariableContainer> getSelected() {
    ArrayList<VariableContainer> selected = new ArrayList<VariableContainer>(0);
    for (VariableCheckbox s : variableChecks) {
      if (s.isSelected()) {
        selected.add(s.variableContainer);
      }
    }
    return selected;
  }

  public int getSelectedSize() {
    return getSelected().size();
  }

  @Override
  public void addPropertyChangeListener(PropertyChangeListener l) {
    pcs.addPropertyChangeListener(l);
  }

  @Override
  public void removePropertyChangeListener(java.beans.PropertyChangeListener l) {
    pcs.removePropertyChangeListener(l);
  }

  public int getLabelLengthLimit() {
    return labelLengthLimit;
  }

  public void setLabelLengthLimit(int labelLengthLimit) {
    this.labelLengthLimit = labelLengthLimit;
  }
}
