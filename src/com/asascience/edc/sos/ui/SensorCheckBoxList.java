/*
 * CheckBoxList.java
 *
 * Created on September 21, 2007, 9:06 AM
 *
 * Applied Science Associates, Inc.
 * Copyright 2007.  All rights reserved.
 */
package com.asascience.edc.sos.ui;

import com.asascience.edc.sos.map.SensorPoint;
import gov.nasa.worldwind.render.PointPlacemark;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JPanel;

import net.miginfocom.swing.MigLayout;

/**I
 *
 * @author Kyle
 */
public class SensorCheckBoxList extends JPanel implements ActionListener {

  private List<SensorCheckbox> sensorChecks = new ArrayList();
  private JButton btnToggleAll;
  private JButton btnSelectAll;
  private JButton btnSelectNone;
  private int labelLengthLimit = 20;
  private PropertyChangeSupport pcs;

  public SensorCheckBoxList() {
    setLayout(new MigLayout("fillx, wrap 1"));
    pcs = new PropertyChangeSupport(this);
  }

  public SensorCheckBoxList(boolean showToggleAll, boolean showSelectAll, boolean showSelectNone) {
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
    for (SensorCheckbox cb : sensorChecks) {
      remove(cb);
    }
    sensorChecks = new ArrayList<SensorCheckbox>();
  }

  public void makeCBList(List<String> items, List<String> descrips, List<Boolean> checks, List<? extends Object> sensors, boolean trimName) {
    String s = "";
    String d = "";
    boolean c = false;
    for (int i = 0; i < items.size(); i++) {
      s = items.get(i);
      d = descrips.get(i);
      c = checks.get(i).booleanValue();
      addCheckBox(s, d, c, sensors.get(i), trimName);
    }
  }
  
  class SensorCheckbox extends JCheckBox {
    public SensorPoint sensor = null;
    public PointPlacemark worldwindSensor = null;
    public SensorCheckbox() {
      super();
    }
    public Object getSensor() {
      if (sensor != null) {
        return sensor;
      }
      if (worldwindSensor != null) {
        return worldwindSensor;
      }
      return null;
    }
  }

  public void addCheckBox(String name, String descr, boolean checked, Object sensor, boolean trimName) {
    SensorCheckbox cb = new SensorCheckbox();
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
    // cb.setText(name);//orig
    cb.setText(text);
    if (sensor.getClass().equals(SensorPoint.class)) {
      cb.sensor = (SensorPoint)sensor;
    } else {
      cb.worldwindSensor = (PointPlacemark)sensor;
    }
    cb.setSelected(checked);
    cb.setToolTipText(descr);
    cb.addActionListener(this);
    cb.setActionCommand(name);
    sensorChecks.add(cb);
    add(cb, "span, wrap");
  }

  public String getFullDescription(String descr) {
    for (SensorCheckbox cb : sensorChecks) {
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
    SensorCheckbox cb = (SensorCheckbox) e.getSource();
    if (cb.getSensor() != null) {
      pcs.firePropertyChange("sensorClicked", null, cb.getSensor());
    }
  }

  public void selectSingleItem(String name) {
    for (SensorCheckbox j : sensorChecks) {
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

    for (JCheckBox j : sensorChecks) {
      j.doClick();
      // j.setSelected(!j.isSelected());
    }
  }

  /**
   * Uncheckes all of the checkboxes.
   */
  public void deselectAll() {
    for (JCheckBox j : sensorChecks) {
      if (j.isSelected()) {
        j.doClick();
      }
    }
  }

  /**
   * Checks all of the checkboxes.
   */
  public void selectAll() {
    for (JCheckBox j : sensorChecks) {
      if (!j.isSelected()) {
        j.doClick();
      }
    }
  }

  public ArrayList<SensorCheckbox> getSelected() {
    ArrayList<SensorCheckbox> selected = new ArrayList<SensorCheckbox>(0);
    for (SensorCheckbox s : sensorChecks) {
      if (s.isSelected()) {
        selected.add(s);
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
