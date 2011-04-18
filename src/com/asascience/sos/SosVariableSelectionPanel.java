/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.asascience.sos;

import java.awt.Dimension;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import net.miginfocom.swing.MigLayout;

/**
 *
 * @author Kyle
 */
public final class SosVariableSelectionPanel extends JPanel {

  private VariableCheckBoxList cblVars;
  private List<VariableContainer> localVariables;
  private PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);
  private String panelTitle;

  public SosVariableSelectionPanel(SosProcessPanel parent, String title) {
    this.panelTitle = title;
    initComponents();
    getCblVars().addPropertyChangeListener(new CheckBoxPropertyListener());
  }

  public void addVariables(List<VariableContainer> vars) {
    localVariables = vars;
    setVariables();
  }

  public void setVariables() {
    if (localVariables == null) {
      return;
    }

    cblVars.clearCBList();
    cblVars.makeCBList(localVariables, true);

    // remove any existing pcl's
    PropertyChangeListener[] pcls = getCblVars().getPropertyChangeListeners();
    for (int i = pcls.length - 1; i >= 0; i--) {
      getCblVars().removePropertyChangeListener(pcls[i]);
    }
  }

  public VariableCheckBoxList getCblVars() {
    return cblVars;
  }

  public List<VariableContainer> getSelectedVariables() {
    return cblVars.getSelected();
  }

  public void initComponents() {
    setLayout(new MigLayout("gap 0, fill"));
    
    cblVars = new VariableCheckBoxList(true, true, true);
    cblVars.setLabelLengthLimit(40);
   
    JScrollPane sp = new JScrollPane();
    sp.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
    sp.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
    sp.setBorder(BorderFactory.createTitledBorder(panelTitle + ": "));
    sp.setMinimumSize(new Dimension(330, 200));
    sp.setViewportView(cblVars);
    add(sp, "grow");
  }

  @Override
  public void addPropertyChangeListener(java.beans.PropertyChangeListener l) {
    propertyChangeSupport.addPropertyChangeListener(l);
  }
  @Override
  public void removePropertyChangeListener(java.beans.PropertyChangeListener l) {
    propertyChangeSupport.removePropertyChangeListener(l);
  }
  
  class CheckBoxPropertyListener implements PropertyChangeListener {

    public void propertyChange(PropertyChangeEvent e) {
      if (e.getPropertyName().equals("variableClicked")) {
        propertyChangeSupport.firePropertyChange(e);
      }
    }
  }
}
