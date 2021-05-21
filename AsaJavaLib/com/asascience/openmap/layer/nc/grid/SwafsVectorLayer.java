/*
 * SwafsVectorLayer.java
 *
 * Created on August 3, 2007, 10:36 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package com.asascience.openmap.layer.nc.grid;

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

import com.asascience.openmap.layer.VectorLayer;
import com.asascience.openmap.omgraphic.OMGridCell;
import com.asascience.openmap.utilities.MapUtils;
import com.asascience.utilities.Utils;

import net.miginfocom.swing.MigLayout;

/**
 * 
 * @author cmueller
 */
public class SwafsVectorLayer extends VectorLayer {

  private SwafsReader sReader;
  private JComboBox jcbVars;

  // private String gridVar = null;
  // private int step = 0;
  // private double[] us;
  // private double[] vs;
  // private double[] fullLats;
  // private double[] fullLons;
  // private long currTime = -1;
  /**
   * Creates a new instance of SwafsVectorLayer
   *
   * @param ncFile
   */
  public SwafsVectorLayer(String ncFile) {
    if (new File(ncFile).exists()) {
      sReader = new SwafsReader(ncFile);
      this.setSourceFilePath(ncFile);
      // this sets the time range in the TimeLayer
      this.setTimeRange(sReader.getStartTime(), sReader.getEndTime());
      // this sets the timeIncrement in the TimeLayer
      this.setTimeIncrement(sReader.getTimeIncrement());
      this.setTimes(sReader.getTimes());

      this.setName(ncFile.substring(ncFile.lastIndexOf(File.separator) + 1));
      this.setUVFillVal(-10000f);
      // this.setVectorThinning(30);
      // this.setScalingFactor(0.25f);
      this.setVectorColor(Color.GRAY);
      // float[] us = sReader.getUs(step, 0);
      // float[] vs = sReader.getVs(step, 0);

      // fullLats = sReader.getFullLats();
      // fullLons = sReader.getFullLons();

      this.lats = sReader.getFullLats();
      this.lons = sReader.getFullLons();

      uvUnits = sReader.getUvUnits();

      // attempt at speed calculation - WAY slow...
      // float speed;
      // us = sReader.getUs(step, 0);
      // vs = sReader.getVs(step, 0);
      // for(int i = 0; i < us.length; i++){
      // for(int j = 0; j < vs.length; j++){
      // speed = (float)Math.sqrt((Math.pow(us[i], 2)) + Math.pow(vs[j],
      // 2));
      // if(speed > getMaxSpeed()) setMaxSpeed(speed);
      // if(speed < getMinSpeed()) setMinSpeed(speed);
      // }
      // }
      // this.fireRequestMessage(String.valueOf(getMaxSpeed()) + "  " +
      // String.valueOf(getMinSpeed()));
      // this.display(fullLats, fullLons, us, vs);
    }
  }

  public void drawDataForTime() {
    drawDataForTime(this.getCurrentTime());
  }

  public void drawDataForTime(long t) {
    // if(time > currTime){
    // step++;
    // }
    // else if(time < currTime){
    // step--;
    // }
    // currTime = time;

    boolean display = false;
    if (drawGridCells) {
      double[] varDat = sReader.getScalarDataByName(t, -1, selGridVar);
      double[] minMax = Utils.minMaxDouble(varDat, sReader.getFillValue());
      Color[] colors = MapUtils.buildColorRamp(Color.RED, Color.GREEN, 10, 240);
      double[] divs = MapUtils.buildEqualDivisionVals(10, minMax[1], minMax[0]);

      buildGridCells(varDat, colors, divs);
      // buildGridCells(null, null, null);
      display = true;
    }
    if (drawVectors) {
      us = sReader.getUVals(t, 0d);
      vs = sReader.getVVals(t, 0d);

      // us = sReader.getUs(step, 0);
      // vs = sReader.getVs(step, 0);
      if (us.length == 1 && Double.isNaN(us[0])) {
        // this.setVisible(false);
      } else {
        // draw the layer
        display = true;
        // this.setVisible(true);
        // this.display();
        // this.display(us, vs);
        // this.display(fullLats, fullLons, us, vs);
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
    double[][] grdData;
    double[] grdx;
    double[] grdy;
    double data;
    boolean add;
    double[] uLats = sReader.getYValues();
    double[] uLons = sReader.getXValues();

    int d = 0;
    for (int i = 0; i < uLats.length; i++) {
      for (int j = 0; j < uLons.length; j++) {
        // make sure the data is valid
        try {
          if (dataList != null) {
            data = dataList[d];
          } else {
            data = Double.NaN;
          }
        } catch (Exception ex) {
          data = Double.NaN;
        }

        add = true;

        grdData = sReader.calculateGridCellAxes(uLats[i], uLons[j]);
        if (grdData == null) {
          add = false;
        }

        if (add) {
          grdy = grdData[0];
          grdx = grdData[1];
          Color cellColor = null;
          if (!Double.isNaN(data)) {
            for (int h = 0; h < divVals.length; h++) {
              if (data > divVals[h]) {
                cellColor = colors[h];
                break;
              }
            }
          }
          if (!Double.isNaN(data)) {
            gridCells.add(new OMGridCell(null, MapUtils.buildFloatPolygonArray(grdy, grdx), data,
                    cellColor, false));
          }
        }

        d++;
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
    if (sReader.isHasScalars()) {
      if (jcbVars == null) {
        // add the appropriate components for visualization of grid
        JPanel pnlGrid = new JPanel(new MigLayout("fill"));
        jcbVars = new JComboBox();
        jcbVars.setRenderer(new ComboToolTipRenderer(jcbVars));
        jcbVars.setActionCommand("jcbVars");
        jcbVars.addItem("Current Vectors");
        for (String s : sReader.getScalarNames()) {
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
  // if(sReader != null){
  // sReader.closeSwafs();
  // sReader = null;
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
                : sReader.getScalarDescriptionByName(cmd.toString());
        jcbVars.setToolTipText(s);

        drawDataForTime();

        // refreshDisplay();
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
                : sReader.getScalarDescriptionByName(value.toString());
        list.setToolTipText(s);
      }
      return this;
    }
  }
}
