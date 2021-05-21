package com.asascience.edc.erddap.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;

import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.apache.commons.lang.ArrayUtils;

import net.miginfocom.swing.MigLayout;

/**
 * ErddapVariableSelector.java
 * 
 * @author Kyle Wilcox <kwilcox@asascience.com>
 */
public class ErddapVariableSelector extends JComponent {

  private JComboBox minOps_;
  private JComboBox maxOps_;
  private JTextField minField_;
  private JTextField maxField_;
  private PropertyChangeSupport pcs = new PropertyChangeSupport(this);

  public ErddapVariableSelector() {
    initComponents(new ArrayList<String>(0));
  }
  
  public ErddapVariableSelector(ArrayList<String> options) {
    super();
    initComponents(options);
  }
  
  private void initComponents(ArrayList<String> options) {
    setLayout(new MigLayout("gap 0, fill"));
    
    JPanel panel = new JPanel(new MigLayout("gap 0, fill"));
    
    String[] operators = {">=",">","=~","","!=","=","<","<="};
    
    /* Min block */
    minOps_ = new JComboBox(operators);
    minOps_.addActionListener(new SelectorListener());
    panel.add(minOps_, "align right");
    minField_ = new JTextField();
    minField_.addActionListener(new SelectorListener());
    panel.add(minField_, "gap 0, width 125");
    
    /* Max block */
    // Reverse operators
    ArrayUtils.reverse(operators);
    maxOps_ = new JComboBox(operators);
    maxOps_.addActionListener(new SelectorListener());
    panel.add(maxOps_, "align right");
    maxField_ = new JTextField();
    maxField_.addActionListener(new SelectorListener());
    panel.add(maxField_, "gap 0, width 125");
    
    if (options.isEmpty()) {
      JLabel singleLabel = new JLabel("No range defined in metadata.");
      panel.add(singleLabel, "align right");
    } else {
      JComboBox cb = new JComboBox(options.toArray());
      cb.addActionListener(new ActionListener() {

        public void actionPerformed(ActionEvent e) {
          if (minField_.getText().isEmpty()) {
            minField_.setText((String)((JComboBox)e.getSource()).getSelectedItem());
            pcs.firePropertyChange("minValue", null, minField_.getText()); 
          } else {
            maxField_.setText((String)((JComboBox)e.getSource()).getSelectedItem());
            pcs.firePropertyChange("maxValue", null, maxField_.getText()); 
          }
        }
      });
      panel.add(cb);
    }
    
    add(panel, "growx");
  }
  
  public String getStartValue() {
    return minField_.getText();
  }
  
  public String getEndValue() {
    return maxField_.getText();
  }
  
  public String getStartConstraint() {
    return (String)minOps_.getSelectedItem();
  }
  
  public String getEndConstraint() {
    return (String)maxOps_.getSelectedItem();
  }
  
  @Override
  public void addPropertyChangeListener(PropertyChangeListener l) {
    pcs.addPropertyChangeListener(l);
  }

  @Override
  public void removePropertyChangeListener(PropertyChangeListener l) {
    pcs.removePropertyChangeListener(l);
  }
  
  class SelectorListener implements ActionListener {
    public void actionPerformed(ActionEvent e) {
      pcs.firePropertyChange("minValue", null, minField_.getText()); 
    }
  }
  
}
