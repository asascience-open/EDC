/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.asascience.edc.erddap.gui;

import com.asascience.edc.erddap.ErddapVariable;
import gov.noaa.pmel.swing.JSlider2Date;
import gov.noaa.pmel.swing.JSlider2Double;
import gov.noaa.pmel.util.GeoDate;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Date;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import net.miginfocom.swing.MigLayout;
import ucar.nc2.units.DateUnit;

/**
 *
 * @author Kyle
 */
public class ErddapVariableSubset extends JPanel {

  private ErddapVariable variable;
  private String min;
  private String max;
  private JCheckBox check;
  
  public ErddapVariableSubset(ErddapVariable variable) {
    this.variable = variable;
    this.min = variable.getMin();
    this.max = variable.getMax();
    initComponents();
  }
  
  private void initComponents() {
    setLayout(new MigLayout("gap 0, fill"));
    
    String title = variable.getLongname();
    if (variable.getUnits() != null) {
      title += " (" + variable.getUnits() + ")";
    }
    check = new JCheckBox(title);
    add(check);
             
    if (variable.isTime()) {
      JSlider2Date slider = new JSlider2Date();
      slider.addPropertyChangeListener(new JDateSliderListener());
      slider.setShowBorder(false);
      slider.setHandleSize(6);
      slider.setAlwaysPost(true);
      Date st = DateUnit.getStandardDate(variable.getMin() + " " + variable.getUnits());
      Date et = DateUnit.getStandardDate(variable.getMax() + " " + variable.getUnits());
      slider.setRange(st,et);
      slider.setStartValue(new GeoDate(st));
      slider.setFormat("yyyy-MM-dd");
      add(slider, "growx");
    } else {
      JSlider2Double slider = new JSlider2Double();
      slider.addPropertyChangeListener(new JSliderListener());
      slider.setShowBorder(false);
      slider.setHandleSize(6);
      if (!variable.getValues().isEmpty()) {
        if (variable.isDouble()) {
          slider.setRange(Double.parseDouble(variable.getMin()), Double.parseDouble(variable.getMax()));
          add(slider, "growx");
        }
      } else if (variable.isZ()) {
        slider.setRange(Double.parseDouble(variable.getMin()), Double.parseDouble(variable.getMax()));
        add(slider, "growx");
      } else if (variable.isDouble()) {
        slider.setRange(Double.parseDouble(variable.getMin()), Double.parseDouble(variable.getMax()));
        add(slider, "growx");
      }
    }
  }

  public String getMax() {
    return max;
  }
  
  public String getMin() {
    return min;
  }
  
  public boolean isSelected() {
    return check.isSelected();
  }
  
  class JSliderListener implements PropertyChangeListener {
    public void propertyChange(PropertyChangeEvent evt) {     
      min = Double.toString(((JSlider2Double)evt.getSource()).getStartValue());
      max = Double.toString(((JSlider2Double)evt.getSource()).getEndValue());
    }
  }
  
  class JDateSliderListener implements PropertyChangeListener {
    public void propertyChange(PropertyChangeEvent evt) {
      min = ((JSlider2Date)evt.getSource()).getStartValue().toString("yyyy-MM-dd");
      max = ((JSlider2Date)evt.getSource()).getEndValue().toString("yyyy-MM-dd");
    }
  }
  
}


