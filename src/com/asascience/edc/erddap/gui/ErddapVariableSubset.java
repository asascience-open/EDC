/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.asascience.edc.erddap.gui;

import com.asascience.edc.erddap.ErddapVariable;
import com.asascience.edc.gui.jslider.ErddapJSlider2Double;
import gov.noaa.pmel.swing.JSlider2Date;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import net.miginfocom.swing.MigLayout;

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
    add(check, "spany");
             
    if (variable.isTime()) {
      // Just show the checkbox and Label
    } else if (variable.isX()) {
      // Just show the checkbox and Label
    } else if (variable.isY()) {
      // Just show the checkbox and Label
    } else if (variable.isSingleValue()) {
      JLabel singleLabel = new JLabel("Only one value present in dataset: " + variable.getMin());
      add(singleLabel, "align right");
    } else {
      ErddapJSlider2Double slider = new ErddapJSlider2Double();
      slider.addPropertyChangeListener(new JSliderListener());
      slider.setShowBorder(false);
      slider.setAlwaysPost(true);
      slider.setHandleSize(6);
      if (!variable.getValues().isEmpty()) {
        if (variable.isDouble()) {
          slider.setRange(Double.parseDouble(variable.getMin()), Double.parseDouble(variable.getMax()));
          add(slider, "width 300, align right");
        }
      } else if (variable.isZ()) {
        slider.setRange(Double.parseDouble(variable.getMin()), Double.parseDouble(variable.getMax()));
        add(slider, "width 300, align right");
      } else if (variable.isDouble()) {
        slider.setRange(Double.parseDouble(variable.getMin()), Double.parseDouble(variable.getMax()));
        add(slider, "width 300, align right");
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
      min = Double.toString(((ErddapJSlider2Double)evt.getSource()).getStartValue());
      max = Double.toString(((ErddapJSlider2Double)evt.getSource()).getEndValue());
    }
  }
  
  class JDateSliderListener implements PropertyChangeListener {
    public void propertyChange(PropertyChangeEvent evt) {
      min = ((JSlider2Date)evt.getSource()).getStartValue().toString("yyyy-MM-dd");
      max = ((JSlider2Date)evt.getSource()).getEndValue().toString("yyyy-MM-dd");
    }
  }
  
}


