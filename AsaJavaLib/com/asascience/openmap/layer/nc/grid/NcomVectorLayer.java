/*
 * NcomVectorLayer.java
 *
 * Created on August 6, 2007, 5:18 PM
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
public class NcomVectorLayer extends VectorLayer {

  private NcomReader nReader;
  private JComboBox jcbVars;

  // private String selGridVar = null;
  // private int step = 0;
  // private double[] us;
  // private double[] vs;
  // private double[] fullLats;
  // private double[] fullLons;
  // private long currTime = -1;
  /**
   * Creates a new instance of NcomVectorLayer
   *
   * @param ncFile
   */
  public NcomVectorLayer(String ncFile) {
    if (new File(ncFile).exists()) {
      nReader = new NcomReader(ncFile);
      this.setSourceFilePath(ncFile);

      // NcGridReader gr = new NcGridReader(ncFile);

      // this sets the time range in the TimeLayer
      this.setTimeRange(nReader.getStartTime(), nReader.getEndTime());
      // this sets the timeIncrement in the TimeLayer
      this.setTimeIncrement(nReader.getTimeIncrement());
      this.setTimes(nReader.getTimes());

      this.setName(ncFile.substring(ncFile.lastIndexOf(File.separator) + 1));
      this.setUVFillVal(-30000f);
      // this.setUVFillVal(nReader.getFillValue());
      // this.setVectorThinning(6);
      // this.setScalingFactor(0.25f);
      this.setVectorColor(Color.BLUE);
      // float[] lats = nReader.getLats();
      // float[] lons = nReader.getLons();

      // fullLats = nReader.getFullLats();
      // fullLons = nReader.getFullLons();

      this.lats = nReader.getFullLats();
      this.lons = nReader.getFullLons();

      uvUnits = nReader.getUvUnits();

      // us = nReader.getUVals(0d);
      // vs = nReader.getVVals(0d);
      // us = nReader.getUs(step, 0);
      // vs = nReader.getVs(step, 0);
      // System.err.println("lengths u=" + us.length + " v=" + vs.length +
      // " x=" + nReader.getXValues().length + " y=" +
      // nReader.getYValues().length);
      // System.err.println("lat lon u v");
      // for(int i = 0; i < us.length; i++){
      // System.err.println(fullLats[i] + " " + fullLons[i] + " " + us[i]
      // + " " + vs[i]);
      // }

      // this.display(fullLats, fullLons, us, vs);

      // uses the StartTime from TimeLayer
      // drawDataForTime(this.getStartTime());
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
      double[] varDat = nReader.getScalarDataByName(t, 0, selGridVar);
      double[] minMax = Utils.minMaxDouble(varDat, nReader.getFillValue());
      Color[] colors = MapUtils.buildColorRamp(Color.RED, Color.GREEN, 10, 240);
      double[] divs = MapUtils.buildEqualDivisionVals(10, minMax[1], minMax[0]);
      //
      buildGridCells(varDat, colors, divs);
      // buildGridCells(null, null, null);
      display = true;
    }
    if (drawVectors) {
      us = nReader.getUVals(t, 0d);
      vs = nReader.getVVals(t, 0d);

      // us = nReader.getUVals(step, 0);
      // vs = nReader.getVVals(step, 0);
      if (us.length == 1 && Double.isNaN(us[0])) {
        // The layer has no data for the specified time
        // this.setVisible(false);
      } else {
        // draw the layer
        // this.setVisible(true);

        // this.display(us, vs);
        // this.display(fullLats, fullLons, us, vs);
        display = true;
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
    double[] uLats = nReader.getYValues();
    double[] uLons = nReader.getXValues();

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

        grdData = nReader.calculateGridCellAxes(uLats[i], uLons[j]);
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
    if (nReader.isHasScalars()) {
      if (jcbVars == null) {
        // add the appropriate components for visualization of grid
        JPanel pnlGrid = new JPanel(new MigLayout("fill"));
        jcbVars = new JComboBox();
        jcbVars.setRenderer(new ComboToolTipRenderer(jcbVars));
        jcbVars.setActionCommand("jcbVars");
        jcbVars.addItem("Current Vectors");
        for (String s : nReader.getScalarNames()) {
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
  // if(nReader != null){
  // nReader.closeNcom();
  // nReader = null;
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
          /** Set the Tool Tip */
          jcbVars.setToolTipText("Vectors representing the U & V current components");
        } else {
          // //just draw the grids first...
          // buildGridCells(null);//works
          selGridVar = cmd;
          // buildGridCells(ncellReader.getScalarDataByName(cmd));
          drawGridCells = true;
          drawVectors = false;
          /** Set the Tool Tip */
          jcbVars.setToolTipText("Vectors representing the U & V current components");
        }
        /** Set the Tool Tip */
        String s = (cmd.equals("Current Vectors")) ? "Vectors representing the U & V current components"
                : nReader.getScalarDescriptionByName(cmd.toString());
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
                : nReader.getScalarDescriptionByName(value.toString());
        list.setToolTipText(s);
      }
      return this;
    }
  }
}
