/*
 * SelectionPanelBase.java
 *
 * Created on November 26, 2007, 11:39 AM
 *
 * Applied Science Associates, Inc.
 * Copyright 2007.  All rights reserved.
 */
package com.asascience.edc.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.TitledBorder;

import net.miginfocom.swing.MigLayout;
import ucar.nc2.Variable;
import ucar.nc2.dt.grid.GeoGrid;

import com.asascience.edc.nc.NetcdfConstraints;
import com.asascience.edc.sos.SosProcessPanel;
import com.asascience.ui.CheckBoxList;

/**
 * 
 * @author CBM
 */
public class SelectionPanelBase extends JPanel {

  protected boolean hasGeoGrids;
  protected JPanel parentSpp;
  protected NetcdfConstraints constraints;
  private List<GeoGrid> localGeoGrids;
  private List<Variable> localVariables;
  private boolean makeRaster = true;
  private boolean makeVector = false;
  private boolean hasGeoSub = false;
  private boolean hasTimeSub = false;
  private int trimByIndex = -1;
  private String trimByValue = "";
  private String uVar = "";
  private String vVar = "";
  private int surfaceLevel = 0;
  private boolean vectorType = false;
  private CheckBoxList cblVars;
  private boolean useAllLevels;
  private List<String> varNames = null;
  private List<String> varDescr = null;
  public static final int GENERAL = 0;
  public static final int ESRI = 1;
  public static final int OILMAP = 2;
  private int panelType;

  public int getPanelType() {
    return panelType;
  }

  protected void setPanelType(int pt) {
    panelType = pt;
  }

  /**
   * Creates a new instance of SelectionPanelBase
   *
   * @param borderTitle
   * @param cons
   * @param parent
   */
  public SelectionPanelBase(String borderTitle, NetcdfConstraints cons, SubsetProcessPanel parent) {
    super(new MigLayout("fill"));

    TitledBorder tb = BorderFactory.createTitledBorder(borderTitle);
    tb.setTitleJustification(TitledBorder.CENTER);
    this.setBorder(tb);
    this.constraints = cons;
    this.parentSpp = parent;
  }

  public SelectionPanelBase(String borderTitle, NetcdfConstraints cons, SosProcessPanel parent) {
    super(new MigLayout("fill"));

    TitledBorder tb = BorderFactory.createTitledBorder(borderTitle);
    tb.setTitleJustification(TitledBorder.CENTER);
    this.setBorder(tb);
    this.constraints = cons;
    this.parentSpp = parent;
  }

  public void createPanel() {
    this.add(variablePanel(), "grow, center");
  }

  public void gridRegularity(boolean isRegular) {
  }

  public void addVariables(List<Variable> vars) {
    for (Variable v : vars) {
      localVariables.add(v);
    }
    setVariables();
  }

  public String getFullDescriptionFromShortDescription(String desc) {
    return cblVars.getFullDescription(desc);
  }

  public String getVarNameFromDescription(String desc) {
    if (varDescr != null) {
      String checker;
      for (int i = 0; i < varDescr.size(); i++) {
        checker = cblVars.getFullDescription(desc);
        if (checker != null) {
          if (checker.equals(varDescr.get(i))) {
            return varNames.get(i);
          }
        }
      }
    }
    return null;
  }

  public void setVariables(List<Variable> vars) {
    localVariables = vars;
    setVariables();
  }

  public void setVariables() {
    if (localVariables == null) {
      return;
    }

    hasGeoGrids = false;

    varNames = new ArrayList();
    varDescr = new ArrayList();

    for (Variable v : localVariables) {
      varNames.add(v.getName());
      varDescr.add((v.getDescription().equals("")) ? v.getName() : v.getDescription());
    }
    // cblVars.removeAll();
    cblVars.clearCBList();

    cblVars.makeCBList(varNames, varDescr, true);// orig
    // cblVars.makeCBList(varDescr, varDescr, true);

    // remove any existing pcl's
    PropertyChangeListener[] pcls = getCblVars().getPropertyChangeListeners();
    for (int i = pcls.length - 1; i >= 0; i--) {
      getCblVars().removePropertyChangeListener(pcls[i]);
    }
  }

  public void addGeoGridVars(List<GeoGrid> geoGrids) {
    for (GeoGrid g : geoGrids) {
      localGeoGrids.add(g);
    }
    setGeoGridVars();
  }

  public void setGeoGridVars(List<GeoGrid> geoGrids) {
    localGeoGrids = geoGrids;
    setGeoGridVars();
  }

  public void setGeoGridVars() {
    if (localGeoGrids == null) {
      return;
    }

    hasGeoGrids = true;

    varNames = new ArrayList();
    varDescr = new ArrayList();

    for (GeoGrid grid : localGeoGrids) {
      varNames.add(grid.getName());
      varDescr.add((grid.getDescription().equals("")) ? grid.getName() : grid.getDescription());
    }
    // cblVars.removeAll();
    cblVars.clearCBList();

    cblVars.makeCBList(varNames, varDescr, true);// orig
    // cblVars.makeCBList(varDescr, varDescr);

    // remove any existing pcl's
    PropertyChangeListener[] pcls = getCblVars().getPropertyChangeListeners();
    for (int i = pcls.length - 1; i >= 0; i--) {
      getCblVars().removePropertyChangeListener(pcls[i]);
    }
  }

  protected JPanel variablePanel() {
    cblVars = new CheckBoxList(true, true, true);
    cblVars.setLabelLengthLimit(30);

    JPanel variablePanel = new JPanel(new MigLayout("fill, insets 0"));
    variablePanel.setBorder(BorderFactory.createTitledBorder("Variables:"));

    JButton toggleAll = new JButton("Toggle All");
    toggleAll.addActionListener(new ActionListener() {

      public void actionPerformed(ActionEvent e) {
        cblVars.toggleAll();
      }
    });

    // variablePanel.add(toggleAll, "wrap");

    JScrollPane varScroll = new JScrollPane();
    // varScroll.setBorder(BorderFactory.createTitledBorder("Variables:"));
    varScroll.getVerticalScrollBar().setUnitIncrement(5);
    varScroll.setViewportView(cblVars);
    variablePanel.add(varScroll, "grow");

    // return varScroll;
    return variablePanel;
  }

  public CheckBoxList getCblVars() {
    return cblVars;
  }

  public void setCblVars(CheckBoxList cblVars) {
    this.cblVars = cblVars;
  }

  public boolean isMakeRaster() {
    // System.err.println("getting makeRaster: " + this.makeRaster);
    return this.makeRaster;
  }

  public void setMakeRaster(boolean makeRaster) {
    // System.err.println("setting makeRaster: " + this.makeRaster);
    this.makeRaster = makeRaster;
  }
  // <editor-fold defaultstate="collapsed" desc=" Bound Properties ">
  /**
   * Holds value of property processEnabled.
   */
  private boolean processEnabled;
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
   * Getter for property processEnabled.
   *
   * @return Value of property processEnabled.
   */
  public boolean isProcessEnabled() {
    return this.processEnabled;
  }

  /**
   * Setter for property processEnabled.
   *
   * @param processEnabled
   *            New value of property processEnabled.
   */
  public void setProcessEnabled(boolean processEnabled) {
    boolean oldProcessEnabled = this.processEnabled;
    this.processEnabled = processEnabled;
    propertyChangeSupport.firePropertyChange("processEnabled", new Boolean(oldProcessEnabled), new Boolean(
            processEnabled));
  }

  // </editor-fold>
  public List<GeoGrid> getLocalGeoGrids() {
    return localGeoGrids;
  }

  protected void setLocalGeoGrids(List<GeoGrid> localGeoGrids) {
    this.localGeoGrids = localGeoGrids;
  }

  public int getTrimByIndex() {
    return trimByIndex;
  }

  protected void setTrimByIndex(int trimByIndex) {
    this.trimByIndex = trimByIndex;
  }

  public String getTrimByValue() {
    return trimByValue;
  }

  protected void setTrimByValue(String trimByValue) {
    this.trimByValue = trimByValue;
  }

  public String getUVar() {
    return uVar;
  }

  protected void setUVar(String uVar) {
    this.uVar = uVar;
  }

  public String getVVar() {
    return vVar;
  }

  protected void setVVar(String vVar) {
    this.vVar = vVar;
  }

  public boolean isMakeVector() {
    return makeVector;
  }

  public void setMakeVector(boolean makeVector) {
    this.makeVector = makeVector;
  }

  public boolean isHasGeoSub() {
    return hasGeoSub;
  }

  public void setHasGeoSub(boolean hasGeoSub) {
    this.hasGeoSub = hasGeoSub;
  }

  public boolean isHasTimeSub() {
    return hasTimeSub;
  }

  public void setHasTimeSub(boolean hasTimeSub) {
    this.hasTimeSub = hasTimeSub;
  }

  public int getSurfaceLevel() {
    return surfaceLevel;
  }

  public void setSurfaceLevel(int surfaceLevel) {
    this.surfaceLevel = surfaceLevel;
  }

  public boolean isVectorType() {
    return vectorType;
  }

  public void setVectorType(boolean vectorType) {
    this.vectorType = vectorType;
  }

  public List<Variable> getLocalVariables() {
    return localVariables;
  }

  public void setLocalVariables(List<Variable> localVariables) {
    this.localVariables = localVariables;
  }

  public boolean isUseAllLevels() {
    return useAllLevels;
  }

  public void setUseAllLevels(boolean useAllLevels) {
    this.useAllLevels = useAllLevels;
  }
}
