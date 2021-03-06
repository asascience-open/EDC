package com.asascience.edc.erddap.gui;

import com.asascience.edc.erddap.ErddapVariable;
import com.asascience.edc.gui.jslider.ErddapJSlider2Double;
import gov.noaa.pmel.swing.JSlider2Date;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import net.miginfocom.swing.MigLayout;

/**
 * ErddapVariableSubset.java
 * 
 * @author Kyle Wilcox <kwilcox@asascience.com>
 */
public class ErddapVariableSubset extends JPanel {

  private ErddapVariable variable;
  private String min;
  private String max;
  private String minConstraint = "";
  private String maxConstraint = "";
  private JCheckBox check;
  private PropertyChangeSupport pcs = new PropertyChangeSupport(this);
  
  public ErddapVariableSubset(ErddapVariable variable) {
    this.variable = variable;
    this.min = variable.getMin();
    this.max = variable.getMax();
    initComponents();
  }
  
  private void initComponents() {
    setLayout(new MigLayout("insets 0, gap 0, fill"));
    
    String title = variable.getTitle();
    if (variable.getUnits() != null) {
      title += " (" + variable.getUnits() + ")";
    }
    check = new JCheckBox(title);
    check.addActionListener(new JCheckboxListener());
    add(check, "gap 0, grow");
             
    if (variable.isTime()) {
      check.setSelected(true);
      check.setEnabled(false);
      JLabel singleLabel = new JLabel("Time variable selected by default.");
      add(singleLabel, "gap 0, align right");
    } else if (variable.isX()) {
      check.setSelected(true);
      check.setEnabled(false);
      JLabel singleLabel = new JLabel("Geographic variable selected by default.");
      add(singleLabel, "gap 0, align right");
    } else if (variable.isY()) {
      check.setSelected(true);
      check.setEnabled(false);
      JLabel singleLabel = new JLabel("Geographic variable selected by default.");
      add(singleLabel, "gap 0, align right");
    } else if (variable.isSingleValue()) {
      JLabel singleLabel = new JLabel("Only one value present in dataset: " + variable.getMin());
      add(singleLabel, "gap 0, align right");
    } else if (variable.hasNoRange()) {
      ErddapVariableSelector evs = new ErddapVariableSelector();
      evs.addPropertyChangeListener(new JSelectorListener());
      add(evs, "gap 0, wmax 400, align right");
    } else {
      ErddapJSlider2Double slider = new ErddapJSlider2Double();
      slider.addPropertyChangeListener(new JSliderListener());
      slider.setShowBorder(false);
      slider.setAlwaysPost(true);
      slider.setHandleSize(6);
      if (!variable.getValues().isEmpty()) {
        if (variable.isDouble()) {
        	double minVal = Double.parseDouble(variable.getMin());
        	double maxVal =  Double.parseDouble(variable.getMax());
        	if((int)minVal!=(int)maxVal){
        		slider.setRange(minVal, maxVal);
        		add(slider, "gap 0, wmax 400, align right");
        	}
        	else{
        		 JLabel singleLabel = new JLabel("Only one value present in dataset: " + variable.getMin());
        	      add(singleLabel, "gap 0, align right");
        	}
        } else {
          if (variable.isCdm()) {
            check.setSelected(true);
            check.setEnabled(false);
          }
          ErddapVariableSelector evs = new ErddapVariableSelector(variable.getValues());
          evs.addPropertyChangeListener(new JSelectorListener());
          add(evs, "gap 0, align right");
        }
      } else if (variable.isZ()) {
        slider.setRange(Double.parseDouble(variable.getMin()), Double.parseDouble(variable.getMax()));
        add(slider, "gap 0, wmax 400, align right");
      } else if (variable.isDouble()) {
        slider.setRange(Double.parseDouble(variable.getMin()), Double.parseDouble(variable.getMax()));
        add(slider, "gap 0, wmax 400, align right");
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
  
    
    public List<String> toConstraints() {
  	    ArrayList<String> z = new ArrayList<String>();
  	    String minValue = getMin();
  	    if(variable.isString()){
  	        minValue =    "\"" + minValue + "\""; 	
  	    }
  	    if (getMin().equals(getMax()) && !getMin().isEmpty()) {
  	    	
  	    		z.add(variable.getName() + "=" + minValue);
  	    		
  	    } else {
  	      // Add constraints to URL
  	      if (!minConstraint.isEmpty() && !getMin().isEmpty()) {
  	        z.add(variable.getName() + minConstraint + minValue);
  	      }
  
  	      if (!maxConstraint.isEmpty() && !getMax().isEmpty()) {
  	    	  String maxValue = getMax();
  	    	  if(variable.isString())
  	    		  maxValue = "\"" + maxValue + "\"";
  	        z.add(variable.getName() + maxConstraint + maxValue);
  	      }
  	    }    	
  
  	    return z;
  	  }

  
  @Override
  public void addPropertyChangeListener(PropertyChangeListener l) {
    pcs.addPropertyChangeListener(l);
  }

  @Override
  public void removePropertyChangeListener(PropertyChangeListener l) {
    pcs.removePropertyChangeListener(l);
  }
  
  class JSliderListener implements PropertyChangeListener {
    public void propertyChange(PropertyChangeEvent evt) {
      min = Double.toString(((ErddapJSlider2Double)evt.getSource()).getStartValue());
      minConstraint = ((ErddapJSlider2Double)evt.getSource()).getStartConstraint();
      max = Double.toString(((ErddapJSlider2Double)evt.getSource()).getEndValue());
      maxConstraint = ((ErddapJSlider2Double)evt.getSource()).getEndConstraint();
      pcs.firePropertyChange(evt);
    }
  }
  
  class JSelectorListener implements PropertyChangeListener {
    public void propertyChange(PropertyChangeEvent evt) {
      min = ((ErddapVariableSelector)evt.getSource()).getStartValue();
      minConstraint = ((ErddapVariableSelector)evt.getSource()).getStartConstraint();
      max = ((ErddapVariableSelector)evt.getSource()).getEndValue();
      maxConstraint = ((ErddapVariableSelector)evt.getSource()).getEndConstraint();
      pcs.firePropertyChange(evt);
    }
  }
  
  class JCheckboxListener implements ActionListener {
    public void actionPerformed(ActionEvent evt) {
      pcs.firePropertyChange("update", 0, 1);
    }
  }
  
  class JDateSliderListener implements PropertyChangeListener {
    public void propertyChange(PropertyChangeEvent evt) {
      min = ((JSlider2Date)evt.getSource()).getStartValue().toString("yyyy-MM-dd");
      max = ((JSlider2Date)evt.getSource()).getEndValue().toString("yyyy-MM-dd");
      pcs.firePropertyChange(evt);
    }
  }
  
}


