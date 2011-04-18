/*
 * CheckBoxList.java
 *
 * Created on September 21, 2007, 9:06 AM
 *
 * Applied Science Associates, Inc.
 * Copyright 2007.  All rights reserved.
 */
package com.asascience.ui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JPanel;

import net.miginfocom.swing.MigLayout;

/**
 * This class provides a control consisting of a series of checkbox controls
 * stacked on top of each other in a "list". The selection action of boxes is
 * accessible from outside the class, as are the items which are currently
 * selected. Tool tips for each checkbox allow for attachment of descriptive
 * text.
 * 
 * @author CBM
 */
public class CheckBoxList extends JPanel implements ActionListener {

  public static final String ADDED = "added";
  public static final String REMOVED = "removed";
  private List<String> items;
  private List<String> descrips;
  private List<JCheckBox> itemCBs = new ArrayList();
  private List<String> selItems = new ArrayList();
  private JButton btnToggleAll;
  private JButton btnSelectAll;
  private JButton btnSelectNone;
  private int labelLengthLimit = 20;

  /** Creates a new instance of CheckBoxList */
  public CheckBoxList() {
    setLayout(new MigLayout("fillx, wrap 1"));

    // selectedItems = new ArrayList();
  }

  public CheckBoxList(boolean showToggleAll, boolean showSelectAll, boolean showSelectNone) {
    setLayout(new MigLayout("fillx, wrap 1"));

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
    // if(showToggleAll) {
    // if(showSelectAll) {
    // if(showSelectNone) {
    // add(btnToggleAll);
    // add(btnSelectAll);
    // add(btnSelectNone, "wrap");
    // } else {
    // add(btnToggleAll);
    // add(btnSelectAll, "wrap");
    // }
    // }else {
    // add(btnToggleAll, "wrap");
    // }
    // }

    if (showToggleAll | showSelectAll | showSelectNone) {
      add(pnlButtons);
    }

  }

  public void clearCBList() {
    for (JCheckBox cb : itemCBs) {
      remove(cb);
    }
    this.itemCBs = new ArrayList<JCheckBox>();
    this.selItems = new ArrayList<String>();
  }

  /**
   * The list constructor. Dynamically creates a checkbox for each item in the
   * list.
   *
   * @param items
   *            The names to use for the checkboxes. One checkbox is created
   *            for each item in the list.
   * @param descrips
   *            Descriptions for each item added to the list. These strings
   *            will be used as tool tips for the corresponding checkbox
   * @param trimName
   */
  public void makeCBList(List<String> items, List<String> descrips, boolean trimName) {
    this.items = items;
    this.descrips = descrips;
    String s = "";
    String d = "";
    for (int i = 0; i < items.size(); i++) {
      s = items.get(i);
      d = descrips.get(i);
      addCheckBox(s, d, trimName);
    }
  }

  public void makeCBList(List<String> items, boolean trimName) {
    String s = "";
    for (int i = 0; i < items.size(); i++) {
      s = items.get(i);
      addCheckBox(s);
    }
  }

  /**
   * Appends a single checkbox to the end of the existing list.
   *
   * @param name
   *            The name to use for the checkbox
   */
  public void addCheckBox(String name) {
    addCheckBox("", name, false);
  }

  /**
   * Appends a single checkbox to the end of the existing list.
   *
   * @param name
   *            The name to use for the checkbox
   * @param descr
   *            The description for the checkbox. Will be applied as a tool
   *            tip.
   * @param trimName
   */
  public void addCheckBox(String name, String descr, boolean trimName) {
    JCheckBox cb = new JCheckBox();
    String t = descr;
    if (!name.equals("")) {
      t = "[" + name + "]" + descr;
    }
    // String text = (name.length() > labelLengthLimit) ? name.substring(0,
    // labelLengthLimit - 1) + "..." : name;
    String text;
    if (trimName) {
      text = (t.length() > labelLengthLimit) ? t.substring(0, labelLengthLimit - 1) + "..." : t;
    } else {
      text = t;
    }
    cb.setText(text);
    cb.setToolTipText(descr);
    cb.addActionListener(this);
    cb.setActionCommand(name);
    itemCBs.add(cb);
    add(cb, "span, wrap");
  }

  public String getFullDescription(String descr) {
    for (JCheckBox cb : itemCBs) {
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
    JCheckBox cb = (JCheckBox) e.getSource();
    if (cb.isSelected()) {
      selItems.add(cb.getText());
      propertyChangeSupport.firePropertyChange(CheckBoxList.ADDED, cb.getText(), cb);
    } else {
      if (selItems.contains(cb.getText())) {
        selItems.remove(cb.getText());
        propertyChangeSupport.firePropertyChange(CheckBoxList.REMOVED, cb.getText(), cb);
      } else {
        System.err.println("CB not in list");
      }
    }
  }

  public void selectSingleItem(String name) {
    for (JCheckBox j : itemCBs) {
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

    for (JCheckBox j : itemCBs) {
      j.doClick();
      // j.setSelected(!j.isSelected());
    }
  }

  /**
   * Uncheckes all of the checkboxes.
   */
  public void deselectAll() {
    for (JCheckBox j : itemCBs) {
      // if the checkbox is selected
      if (j.isSelected()) {
        // perform a click event to deselect them
        j.doClick();
      }
      // j.setSelected(false);//uncheck them
    }
    selItems.clear();// clear the selItems list
  }

  /**
   * Checks all of the checkboxes.
   */
  public void selectAll() {
    for (JCheckBox j : itemCBs) {
      if (!j.isSelected()) {
        j.doClick();
      }
    }
  }

  /**
   * Unchecks all of the checkboxes except for the one specified
   *
   * @param cb
   *            The <CODE>JCheckBox</CODE> that is NOT to be unchecked. After
   *            calling this method, this will be the only selected checkbox.
   */
  public void deselectAllButOne(JCheckBox cb) {
    for (JCheckBox j : itemCBs) {
      if (j != cb) {// if it's not the passed box
        j.setSelected(false);// uncheck the box
        if (selItems.contains(j.getText())) {
          // if in list
          selItems.remove(j.getText()); // remove from selItems list
        }// remove from selItems list
      }
    }
    // this.setSelectedItems(selItems);
  }
  /**
   * Holds value of property selectedItems.
   */
  // private List<String> selectedItems;
  /**
   * Utility field used by bound properties.
   */
  private java.beans.PropertyChangeSupport propertyChangeSupport = new java.beans.PropertyChangeSupport(this);

  /**
   * Adds a PropertyChangeListener to the listener list.
   *
   * @param l
   *            The listener to add.
   */
  @Override
  public void addPropertyChangeListener(java.beans.PropertyChangeListener l) {
    propertyChangeSupport.addPropertyChangeListener(l);
  }

  /**
   * Removes a PropertyChangeListener from the listener list.
   *
   * @param l
   *            The listener to remove.
   */
  @Override
  public void removePropertyChangeListener(java.beans.PropertyChangeListener l) {
    propertyChangeSupport.removePropertyChangeListener(l);
  }

  /**
   * Get the value of the list at the specified index.
   *
   * @param index
   *            The list index to retrieve
   * @return The <CODE>String</CODE> of the list item at the specified index.
   */
  public String getSelectedItems(int index) {
    // return this.selectedItems.get(index);
    return this.selItems.get(index);
  }

  /**
   * Retrieves the names of the selected checkboxes.
   *
   * @return The <CODE>List</CODE> of strings representing the selected
   *         checkboxes.
   */
  public List<String> getSelectedItems() {
    // return this.selectedItems;
    return this.selItems;
  }

  // /**
  // * Indexed setter for property selectedItems.
  // * @param index Index of the property.
  // * @param selectedItems New value of the property at <CODE>index</CODE>.
  // */
  // public void setSelectedItems(int index, String selectedItems) {
  // this.selectedItems.set(index, selectedItems);
  // // propertyChangeSupport.firePropertyChange ("selectedItems", null, null
  // );
  // }
  // public void setSelectedItems(List<String> items){
  // this.selectedItems = items;
  // // propertyChangeSupport.firePropertyChange("selectedItems", null, null);
  // }
  public List<String> getAllItems() {
    List<String> ret = new ArrayList<String>();
    for (JCheckBox cb : itemCBs) {
      ret.add(cb.getText());
    }
    return ret;
  }

  public int getAllItemsSize() {
    return this.itemCBs.size();
  }

  /**
   * Getter for property listSize.
   *
   * @return Value of property listSize.
   */
  public int getSelItemsSize() {
    return this.selItems.size();
  }

  public int getLabelLengthLimit() {
    return labelLengthLimit;
  }

  public void setLabelLengthLimit(int labelLengthLimit) {
    this.labelLengthLimit = labelLengthLimit;
  }
}
