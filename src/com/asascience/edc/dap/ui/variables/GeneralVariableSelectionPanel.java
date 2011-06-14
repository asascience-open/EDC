/*
 * GeneralSelectionPanel.java
 *
 * Created on November 26, 2007, 1:55 PM
 *
 * Applied Science Associates, Inc.
 * Copyright 2007.  All rights reserved.
 */
package com.asascience.edc.dap.ui.variables;

import com.asascience.edc.dap.ui.DapWorldwindProcessPanel;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import net.miginfocom.swing.MigLayout;
import ucar.nc2.dataset.CoordinateAxis1D;
import ucar.nc2.dt.GridCoordSystem;

import com.asascience.edc.nc.NetcdfConstraints;
import com.asascience.ui.CheckBoxList;

/**
 * 
 * @author CBM
 */
public class GeneralVariableSelectionPanel extends VariableSelectionPanel {

  private JComboBox cbTrimBy;
  private JLabel lblTrimBy;
  private JCheckBox ckNoTrim;

  /**
   * Creates a new instance of GeneralSelectionPanel
   *
   * @param cons
   * @param parent
   */
  public GeneralVariableSelectionPanel(NetcdfConstraints cons, DapWorldwindProcessPanel parent) {
    this("", cons, parent);
  }

  public GeneralVariableSelectionPanel(String borderTitle, NetcdfConstraints cons, DapWorldwindProcessPanel parent) {
    super(borderTitle, cons, parent);
    setPanelType(VariableSelectionPanel.GENERAL);
    createPanel();

    getCblVars().addPropertyChangeListener(new CheckBoxPropertyListener());
    // getCblVars().addPropertyChangeListener(new PropertyChangeListener(){
    // public void propertyChange(PropertyChangeEvent e){
    // System.err.println("propName="+e.getPropertyName());
    // if(getCblVars().getSelItemsSize() > 0){
    // setProcessEnabled(true);
    // }else{
    // setProcessEnabled(false);
    // }
    // }
    // });

    constraints.setTrimByIndex(-1);
    constraints.setTrimByDim("null");
  }

  /**
   * Builds the panel and initializes the various components.
   */
  @Override
  public void createPanel() {
    super.createPanel();
    this.add(optionsPanel(), BorderLayout.SOUTH);
  }

  private JPanel optionsPanel() {
    JPanel optPnl = new JPanel(new MigLayout());
    optPnl.setBorder(BorderFactory.createTitledBorder("Options"));
    cbTrimBy = new JComboBox();
    cbTrimBy.setEnabled(false);
    cbTrimBy.addActionListener(new ActionListener() {

      public void actionPerformed(ActionEvent e) {
        int index = cbTrimBy.getSelectedIndex();
        if (index > -1) {
          setTrimByIndex(index);
          setTrimByValue(cbTrimBy.getSelectedItem().toString());
        }
      }
    });

    lblTrimBy = new JLabel("Select Trim Level:");

    ckNoTrim = new JCheckBox("All");
    ckNoTrim.setEnabled(false);
    ckNoTrim.addActionListener(new ActionListener() {

      public void actionPerformed(ActionEvent e) {
        if (getCblVars().getSelItemsSize() > 0) {
          boolean useAll = (ckNoTrim.isSelected()) ? true : false;
          // if(ckNoTrim.isSelected()){
          // useAll = true;
          // }
          cbTrimBy.setEnabled(!useAll);
          setUseAllLevels(useAll);
        }
      }
    });

    optPnl.add(lblTrimBy);
    optPnl.add(cbTrimBy);
    optPnl.add(ckNoTrim);

    return optPnl;
  }

  class CheckBoxPropertyListener implements PropertyChangeListener {

    public void propertyChange(PropertyChangeEvent e) {
      String propName = e.getPropertyName();
      String vName = (String) e.getOldValue();

      GridCoordSystem coordSys = ((DapWorldwindProcessPanel) parentSpp).getGridByName(vName, true).getCoordinateSystem();
      CoordinateAxis1D vert = coordSys.getVerticalAxis();

      if (propName.equals(CheckBoxList.ADDED)) {
        double[] zVals = null;
        String zDesc = null;

        // add items to the trim combobox
        if (vert != null) {
          zVals = vert.getCoordValues();
          zDesc = vert.getDescription();
          if (zDesc == null) {
            zDesc = "No Description Available";
          }

          cbTrimBy.removeAllItems();
          for (int i = 0; i < zVals.length; i++) {
            cbTrimBy.addItem(zVals[i]);
          }
          if (cbTrimBy.getItemCount() != 0) {
            cbTrimBy.setEnabled(true);
            constraints.setTrimByZ(true);
            constraints.setTrimByDim(vert.getName());
          } else {
            cbTrimBy.setEnabled(false);
            constraints.setTrimByZ(false);
            constraints.setTrimByDim("null");
          }
        }

      } else if (propName.equals(CheckBoxList.REMOVED)) {
        boolean keepVerts = false;
        for (String s : getCblVars().getSelectedItems()) {
          vert = ((DapWorldwindProcessPanel) parentSpp).getGridByName(s, true).getCoordinateSystem().getVerticalAxis();
          if (vert != null) {
            keepVerts = true;
            constraints.setTrimByDim(vert.getName());
          }
        }

        if (!keepVerts) {
          cbTrimBy.removeAllItems();
          constraints.setTrimByDim("null");
        }

        if (cbTrimBy.getItemCount() == 0) {
          cbTrimBy.setEnabled(false);
        }
      }

      // ensure that the "noTrim" checkboxe is available if there are
      // variable(s) selected
      if (getCblVars().getSelItemsSize() > 0) {
        ckNoTrim.setEnabled(true);
      } else {
        ckNoTrim.setSelected(false);
        ckNoTrim.setEnabled(false);
      }

      boolean isEnabled = (getCblVars().getSelItemsSize() == 0) ? false : true;
      setProcessEnabled(isEnabled);
    }
  }
}
