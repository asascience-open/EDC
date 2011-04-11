/*
 * Applied Science Associates, Inc.
 * Copyright 2007. All rights reserved.
 */

/*
 * DimMapDialog.java
 *
 * Created on Feb 12, 2008, 11:22:09 AM
 *
 */
package com.asascience.edc.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;

import net.miginfocom.swing.MigLayout;

/**
 * 
 * @author CBM <cmueller@asascience.com>
 */
public class DimMapDialog extends JDialog {

  private List<String> dimNames;
  private JComboBox cbTime;
  private JComboBox cbLat;
  private JComboBox cbLon;
  private JComboBox cbLevel;
  private JButton btnAccept;
  private String timeDim;
  private String yDim;
  private String xDim;
  private String zDim;

  /**
   * Creates a new instance of DimMapDialog
   *
   * @param parent
   * @param dimNames
   */
  public DimMapDialog(JFrame parent, List<String> dimNames) {
    super(parent, "Dimension Map", true);
    this.dimNames = dimNames;

    this.setLayout(new MigLayout("center"));
    this.setLocationRelativeTo(parent);
    this.setResizable(false);
    this.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);

    initComponents();
    fillCombos();

    this.pack();// accounts for growth due to items in comboboxes
  }

  private void initComponents() {
    cbTime = new JComboBox();
    cbLat = new JComboBox();
    cbLon = new JComboBox();
    cbLevel = new JComboBox();
    btnAccept = new JButton("OK");
    btnAccept.addActionListener(new ActionListener() {

      public void actionPerformed(ActionEvent e) {
        timeDim = (String) cbTime.getSelectedItem();
        yDim = (String) cbLat.getSelectedItem();
        xDim = (String) cbLon.getSelectedItem();
        zDim = (String) cbLevel.getSelectedItem();

        setVisible(false);
      }
    });

    this.add(new JLabel("Select the dimensions to use for:"), "center, wrap");
    this.add(new JLabel("Time:"), "split 2, center");
    this.add(cbTime, "wrap");
    this.add(new JLabel("Lat:"), "split 2, center");
    this.add(cbLat, "wrap");
    this.add(new JLabel("Lon:"), "split 2, center");
    this.add(cbLon, "wrap");
    this.add(new JLabel("Level:"), "split 2, center");
    this.add(cbLevel, "wrap");

    this.add(btnAccept, "center");
  }

  private void fillCombos() {
    cbTime.addItem("null");
    cbLat.addItem("null");
    cbLon.addItem("null");
    cbLevel.addItem("null");

    for (String s : dimNames) {
      cbTime.addItem(s);
      cbLat.addItem(s);
      cbLon.addItem(s);
      cbLevel.addItem(s);
    }
  }

  public String getTimeDim() {
    return timeDim;
  }

  public void setTimeDim(String timeDim) {
    this.timeDim = timeDim;
  }

  public String getYDim() {
    return yDim;
  }

  public void setYDim(String yDim) {
    this.yDim = yDim;
  }

  public String getXDim() {
    return xDim;
  }

  public void setXDim(String xDim) {
    this.xDim = xDim;
  }

  public String getZDim() {
    return zDim;
  }

  public void setZDim(String zDim) {
    this.zDim = zDim;
  }

  public static void main(String[] args) {
    javax.swing.SwingUtilities.invokeLater(new Runnable() {

      public void run() {
        List<String> names = new ArrayList();
        names.add("null");
        names.add("zlevel");
        names.add("lat");
        names.add("lon");
        names.add("time");
        DimMapDialog dimMap = new DimMapDialog(null, names);

        dimMap.setVisible(true);

        System.err.println("time=" + dimMap.getTimeDim());
        System.err.println("lat=" + dimMap.getYDim());
        System.err.println("lon=" + dimMap.getXDim());
        System.err.println("level=" + dimMap.getZDim());

        dimMap.dispose();
      }
    });
  }
}
