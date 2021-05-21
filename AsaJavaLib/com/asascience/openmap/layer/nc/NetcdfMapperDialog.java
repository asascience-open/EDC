/*
 * Applied Science Associates, Inc.
 * Copyright 2009. All Rights Reserved.
 *
 * NetcdfMapper.java
 *
 * Created on Feb 10, 2009 @ 1:42:24 PM
 */
package com.asascience.openmap.layer.nc;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;

import org.jdom.JDOMException;

import com.asascience.ui.OptionDialogBase;

import net.miginfocom.swing.MigLayout;

/**
 * 
 * @author CBM <cmueller@asascience.com>
 */
public class NetcdfMapperDialog extends OptionDialogBase {

  private HashMap<String, String> varInfo;
  private List<JComboBox> comboList;
  private JComboBox cbT;
  private JComboBox cbX;
  private JComboBox cbY;
  private JComboBox cbZ;
  private JComboBox cbU;
  private JComboBox cbV;
  private JCheckBox cbIsNcell;

  public NetcdfMapperDialog(HashMap<String, String> varInfo) {
    super("Variable Mapping Dialog");
    this.varInfo = varInfo;

    comboList = new ArrayList<JComboBox>();
    initComponents();
    fillCombos();
  }

  public void buildSupportFile(String fileLoc) throws FileNotFoundException, IOException, JDOMException {
    NetcdfSupport ncMap = new NetcdfSupport(fileLoc);
    ncMap.setMapping(this.getMapping());
    ncMap.setIsNcell(cbIsNcell.isSelected());
    ncMap.writeXml();
  }

  public HashMap<String, String> getMapping() {
    HashMap<String, String> ret = new HashMap<String, String>();
    ret.put(GenericNetcdfReader.T, cbT.getSelectedItem().toString());
    ret.put(GenericNetcdfReader.X, cbX.getSelectedItem().toString());
    ret.put(GenericNetcdfReader.Y, cbY.getSelectedItem().toString());
    ret.put(GenericNetcdfReader.Z, cbZ.getSelectedItem().toString());
    ret.put(GenericNetcdfReader.U, cbU.getSelectedItem().toString());
    ret.put(GenericNetcdfReader.V, cbV.getSelectedItem().toString());

    return ret;
  }

  private void initComponents() {
    this.add(coordinatePanel(), "growx, wrap");
    this.add(dataPanel(), "growx, wrap");

    this.add(this.buttonPanel("OK"));
    this.pack();
  }

  private void fillCombos() {
    for (JComboBox cb : comboList) {
      cb.addItem("None");
      for (String s : varInfo.keySet()) {
        cb.addItem(s);
      }
    }
  }

  private JPanel coordinatePanel() {
    JPanel pnl = new JPanel(new MigLayout());
    TitledBorder tb = BorderFactory.createTitledBorder("Coordinate Information:");
    pnl.setBorder(tb);

    cbT = new JComboBox();
    cbX = new JComboBox();
    cbY = new JComboBox();
    cbZ = new JComboBox();
    cbIsNcell = new JCheckBox("Cell Based Coordinates");
    comboList.add(cbT);
    comboList.add(cbX);
    comboList.add(cbY);
    comboList.add(cbZ);

    pnl.add(new JLabel("Time"));
    pnl.add(cbT, "wrap");
    pnl.add(cbIsNcell, "span, wrap");
    pnl.add(new JLabel("X"));
    pnl.add(cbX, "wrap");
    pnl.add(new JLabel("Y"));
    pnl.add(cbY, "wrap");
    pnl.add(new JLabel("Z"));
    pnl.add(cbZ, "wrap");

    return pnl;
  }

  private JPanel dataPanel() {
    JPanel pnl = new JPanel(new MigLayout());
    TitledBorder tb = BorderFactory.createTitledBorder("Data Information:");
    pnl.setBorder(tb);

    cbU = new JComboBox();
    cbV = new JComboBox();
    comboList.add(cbU);
    comboList.add(cbV);

    pnl.add(new JLabel("U"));
    pnl.add(cbU, "wrap");
    pnl.add(new JLabel("V"));
    pnl.add(cbV, "wrap");

    return pnl;
  }
}
