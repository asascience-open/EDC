/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.asascience.sos;

import com.asascience.ui.CheckBoxList;
import java.awt.Dimension;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
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

  private CheckBoxList cblVars;
  private List<VariableContainer> localVariables;
  private ArrayList varNames;
  private ArrayList varDescr;
  private boolean getObsEnabled;
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

    varNames = new ArrayList();
    varDescr = new ArrayList();

    for (VariableContainer v : localVariables) {
      varNames.add(v.getName());
      varDescr.add(v.getProperty());
    }
    cblVars.clearCBList();
    cblVars.makeCBList(varNames, varDescr, true);

    // remove any existing pcl's
    PropertyChangeListener[] pcls = getCblVars().getPropertyChangeListeners();
    for (int i = pcls.length - 1; i >= 0; i--) {
      getCblVars().removePropertyChangeListener(pcls[i]);
    }
  }

  public CheckBoxList getCblVars() {
    return cblVars;
  }

  public void initComponents() {
    setLayout(new MigLayout("gap 0, fill"));
    
    cblVars = new CheckBoxList(true, true, true);
    cblVars.setLabelLengthLimit(40);
   
    JScrollPane sp = new JScrollPane();
    sp.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
    sp.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
    sp.setBorder(BorderFactory.createTitledBorder(panelTitle + ": "));
    sp.setMinimumSize(new Dimension(330, 200));
    sp.setViewportView(cblVars);
    add(sp, "grow");
  }

  private void enableGetObs() {
    boolean shouldEnableGetObs = (getCblVars().getSelItemsSize() == 0) ? false : true;
    boolean oldShouldEnableGetObs = this.getObsEnabled;
    this.getObsEnabled = shouldEnableGetObs;
    propertyChangeSupport.firePropertyChange("processEnabled",
                                              oldShouldEnableGetObs,
                                              this.getObsEnabled);
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
      enableGetObs();
    }
  }
}
