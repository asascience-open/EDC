/*
 * NcellVectorLayer.java
 *
 * Created on December 12, 2007, 10:04 AM
 *
 * Applied Science Associates, Inc.
 * Copyright 2007.  All rights reserved.
 */
package com.asascience.openmap.layer.nc.ncell;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.ArrayList;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ToolTipManager;
import javax.swing.plaf.basic.BasicComboBoxRenderer;

import net.miginfocom.swing.MigLayout;

import com.asascience.openmap.layer.VectorLayer;
import com.asascience.openmap.omgraphic.OMGridCell;
import com.asascience.openmap.utilities.MapUtils;
import com.asascience.utilities.Utils;
import com.bbn.openmap.util.DataBounds;

/**
 * 
 * @author CBM
 */
public class NcellVectorLayer extends VectorLayer {

  private NcellReader ncellReader = null;
  private JComboBox jcbVars;

  // private String selGridVar = null;
  // private double[] varDat;
  // private double[] minMax;
  // private double[] divs;
  // private Color[] colors;
  /**
   * Creates a new instance of NcellVectorLayer
   *
   * @param dataFile
   */
  public NcellVectorLayer(String dataFile) {
    if (new File(dataFile).exists()) {
      ncellReader = new NcellReader(dataFile);
      this.setSourceFilePath(dataFile);

      // this sets the time range in the TimeLayer
      this.setTimeRange(ncellReader.getStartTime(), ncellReader.getEndTime());
      // this sets the timeIncrement in the TimeLayer
      this.setTimeIncrement(ncellReader.getTimeIncrement());
      this.setTimes(ncellReader.getTimeSteps());

      this.setName(dataFile.substring(dataFile.lastIndexOf(File.separator) + 1));
      this.setUVFillVal(ncellReader.getFillValue());
      // this.setVectorThinning(6);
      // this.setScalingFactor(0.25f);
      // this.setScalingFactor(1f);
      this.setVectorColor(Color.RED);

      this.setShowDisplayType(false);

      this.lats = ncellReader.getLats();
      this.lons = ncellReader.getLons();

      uvUnits = ncellReader.getUvUnits();

      // buildGridCells(null);

      // if(ncellReader.isHasScalars()){
      // List<OMGridCell> grids = new ArrayList<OMGridCell>();
      // double[] xs, ys;
      // OMGridCell gc;
      // for(int i = 0; i < ncellReader.getNCells(); i++){
      // xs = ncellReader.getXGridForNcell(i);
      // ys = ncellReader.getYGridForNcell(i);
      //
      // }
      // }
    }
  }

  /**
   *
   * @return
   */
  @Override
  public DataBounds getLayerExtent() {
    this.setLats(ncellReader.getLats());
    this.setLons(ncellReader.getLons());
    return super.getLayerExtent();
  }

  public void drawDataForTime() {
    drawDataForTime(this.getCurrentTime());
  }

  /**
   *
   * @param t
   */
  public void drawDataForTime(long t) {
    // defaults to draw the first timestep
    if (t == -1) {
      t = ncellReader.getStartTime();
    }

    boolean display = false;
    if (drawGridCells) {
      double[] varDat = ncellReader.getScalarDataByName(t, -1, selGridVar);
      double[] minMax = Utils.minMaxDouble(varDat, ncellReader.getFillValue());
      Color[] colors = MapUtils.buildColorRamp(Color.RED, Color.GREEN, 10, 240);
      double[] divs = MapUtils.buildEqualDivisionVals(10, minMax[1], minMax[0]);

      buildGridCells(varDat, colors, divs);
      // buildGridCells(ncellReader.getScalarDataByName(t, -1,
      // selGridVar));
      // buildGridCells(null);

      display = true;
    }

    if (drawVectors) {
      if (ncellReader.getUVs(t)) {
        this.setVisible(true);
        // double[] lats = ncellReader.getLats();
        // double[] lons = ncellReader.getLons();
        // double[] us = ncellReader.getUs();
        // double[] vs = ncellReader.getVs();
        us = ncellReader.getUs();
        vs = ncellReader.getVs();

        // this.display(lats, lons, us, vs);
        // this.display(us, vs);
        // this.display();
        display = true;
      } else {
        // this.setVisible(false);
      }
    }
    if (display) {
      this.display();
    } else {
      this.clearDisplay();
    }
  }

  @Override
  protected void buildGridCells(double[] dataList, Color[] colors, double[] divVals) {
    gridCells = new ArrayList<OMGridCell>();
    double[] grdx;
    double[] grdy;
    double data;
    boolean add;
    for (int i = 0; i < ncellReader.getNCells(); i++) {
      // make sure the data is valid
      try {
        if (dataList != null) {
          data = dataList[i];
        } else {
          data = Double.NaN;
        }
      } catch (Exception ex) {
        data = Double.NaN;
      }

      if (data == ncellReader.getFillValue()) {
        continue;
      }

      grdx = ncellReader.getXGridForNcell(i);
      grdy = ncellReader.getYGridForNcell(i);

      // make sure the grid is valid
      add = true;

      for (double d : grdx) {
        if (d == ncellReader.getFillValue()) {
          add = false;
          break;
        }
      }
      for (double d : grdy) {
        if (d == ncellReader.getFillValue()) {
          add = false;
          break;
        }
      }

      if (add) {
        Color cellColor = null;
        if (!Double.isNaN(data)) {
          for (int j = 0; j < divVals.length; j++) {
            if (data > divVals[j]) {
              cellColor = colors[j];
              break;
            }
          }
        }
        // float[] llpairs = MapUtils.buildFloatPolygonArray(grdy,
        // grdx);
        gridCells.add(new OMGridCell(null, MapUtils.buildFloatPolygonArray(grdy, grdx), data, cellColor, false));
      }
    }
  }

  /**
   *
   * @return
   */
  @Override
  public Component getGUI() {
    JPanel pnl = (JPanel) super.getGUI();
    if (ncellReader.isHasScalars()) {
      if (jcbVars == null) {
        // add the appropriate components for visualization of grid
        JPanel pnlGrid = new JPanel(new MigLayout("fill"));
        jcbVars = new JComboBox();
        jcbVars.setRenderer(new ComboToolTipRenderer(jcbVars));
        jcbVars.setActionCommand("jcbVars");
        jcbVars.addItem("Current Vectors");
        jcbVars.setToolTipText("Vectors representing the U & V current components");
        for (String s : ncellReader.getScalarNames()) {
          jcbVars.addItem(s);
        }
        jcbVars.addActionListener(new GridActionListener());
        pnlGrid.add(new JLabel("Variables:"), "wrap");
        pnlGrid.add(jcbVars);

        pnl.add(pnlGrid);
      }
    }
    return pnl;
  }

  // /**
  // * Called when the Layer is removed from the MapBean, giving an
  // opportunity
  // * to clean up.
  // */
  // @Override
  // public void removed(Container cont){
  // if(edsReader != null){
  // edsReader.dispose();
  // edsReader = null;
  // }
  // }
  class GridActionListener implements ActionListener {

    public void actionPerformed(ActionEvent e) {
      String name = e.getActionCommand();
      if (name.equals("jcbVars")) {
        String cmd = jcbVars.getSelectedItem().toString();
        /** get the appropriate data and draw the grid using that data */
        if (cmd.equals("Current Vectors")) {
          selGridVar = null;
          drawVectors = true;
          drawGridCells = false;
        } else {
          // //just draw the grids first...
          // buildGridCells(null);//works
          selGridVar = cmd;
          // buildGridCells(ncellReader.getScalarDataByName(cmd));
          drawGridCells = true;
          drawVectors = false;
        }
        /** Set the Tool Tip */
        String s = (cmd.equals("Current Vectors")) ? "Vectors representing the U & V current components"
                : ncellReader.getScalarDescriptionByName(cmd.toString());
        jcbVars.setToolTipText(s);

        drawDataForTime();
      }
    }
  }

  class ComboToolTipRenderer extends BasicComboBoxRenderer {

    JComboBox combo;
    JList comboList;

    public ComboToolTipRenderer(JComboBox combo) {
      this.combo = combo;
    }

    public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected,
            boolean cellHasFocus) {
      super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
      if (comboList == null) {
        comboList = list;
        combo.addKeyListener(new KeyAdapter() {

          public void keyReleased(KeyEvent e) {
            if (e.getKeyCode() == KeyEvent.VK_DOWN || e.getKeyCode() == KeyEvent.VK_UP) {
              int x = 5;
              int y = comboList.indexToLocation(comboList.getSelectedIndex()).y;
              ToolTipManager.sharedInstance().mouseEntered(
                      new MouseEvent(comboList, MouseEvent.MOUSE_ENTERED, 0, 0, x, y, 0, false));
              ToolTipManager.sharedInstance().mouseMoved(
                      new MouseEvent(comboList, MouseEvent.MOUSE_MOVED, 0, 0, x, y, 0, false));
            }
          }
        });
      }
      if (isSelected) {
        String s = (value.toString().equals("Current Vectors")) ? "Vectors representing the U & V current components"
                : ncellReader.getScalarDescriptionByName(value.toString());
        list.setToolTipText(s);
      }
      return this;
    }
  }
}
