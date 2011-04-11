/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * NefisVectorLayer.java
 *
 * Created on Jun 18, 2008, 10:24:02 AM
 *
 */
package com.asascience.openmap.layer.nefis;

import java.awt.Color;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;

import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;

import net.miginfocom.swing.MigLayout;

import com.asascience.openmap.layer.VectorLayer;
import com.asascience.ui.OptionDialogBase;
import com.asascience.utilities.Utils;
import com.bbn.openmap.LatLonPoint;
import com.bbn.openmap.proj.coords.UTMPoint;

/**
 * 
 * @author cmueller_mac
 */
public class NefisVectorLayer extends VectorLayer {

  private NefisReader nefisReader = null;
  private int selectedLevel = 0;// default to the surface
  private int utmZone = 52;// default for "pohang" files...
  private char utmHemis = 'N';
  private CoordSysDialog coordDialog;

  /**
   * Creates a new instance of NefisVectorLayer
   *
   * @param dataFile
   */
  public NefisVectorLayer(String dataFile) {
    if (new File(dataFile).exists()) {
      nefisReader = new NefisReader(dataFile);
      this.setSourceFilePath(dataFile);

      // this sets the time range in the TimeLayer
      this.setTimeRange(nefisReader.getStartTime(), nefisReader.getEndTime());
      // this sets the timeIncrement in the TimeLayer
      this.setTimeIncrement(nefisReader.getTimeIncrement());

      this.setName(dataFile.substring(dataFile.lastIndexOf(File.separator) + 1));
      this.setUVFillVal(-99999f);// this should be 0f
      // this.setVectorThinning(6);
      this.setScalingFactor(0.05f);
      // this.setScalingFactor(1f);
      this.setVectorColor(Color.ORANGE);

      this.setShowDisplayType(false);

      // have the user select a coordinate system
      if (coordDialog == null) {
        coordDialog = new CoordSysDialog();
      }
      while (!coordDialog.acceptChanges()) {
        coordDialog.setVisible(true);
      }

      // this.lats = nefisReader.getYVals();
      // this.lons = nefisReader.getXGrid();
      this.lats = Utils.floatArrayToDoubleArray(nefisReader.getYVals());
      this.lons = Utils.floatArrayToDoubleArray(nefisReader.getXGrid());

    }
  }

  public void drawDataForTime(long t) {
    if (nefisReader == null) {
      return;
    }
    if (t == -1) {
      t = nefisReader.getStartTime();
    }

    if (nefisReader.isValidTime(t)) {
      int tI = nefisReader.getTimeIndex(t);
      if (tI == -1) {
        return;
      }

      // //TODO: need to convert the lat/lon values from UTM (or whatever
      // coordinate system) to DecimalDegrees
      // double[] lats = nefisReader.getYVals();
      // double[] lons = nefisReader.getXVals();

      UTMPoint utm;
      LatLonPoint llp;
      for (int i = 0; i < lats.length; i++) {
        if (lats[i] != 0 | lons[i] != 0) {
          utm = new UTMPoint((float) lats[i], (float) lons[i], utmZone, utmHemis);
          llp = utm.toLatLonPoint();
          lats[i] = (double) llp.getLatitude();
          lons[i] = (double) llp.getLongitude();
        }
      }

      // double[] us = nefisReader.getUVals().get(tI)[selectedLevel];
      // double[] vs = nefisReader.getVVals().get(tI)[selectedLevel];
      // us = nefisReader.getUVals().get(tI)[selectedLevel];
      // vs = nefisReader.getVVals().get(tI)[selectedLevel];
      us = Utils.floatArrayToDoubleArray(nefisReader.getUVals().get(tI)[selectedLevel]);
      vs = Utils.floatArrayToDoubleArray(nefisReader.getVVals().get(tI)[selectedLevel]);
      // if(nefisReader.getUVals().get(tI) instanceof float[][]){
      // us =
      // Utils.floatArrayToDoubleArray(((float[][])nefisReader.getUVals().get(tI))[selectedLevel]);
      // vs =
      // Utils.floatArrayToDoubleArray(((float[][])nefisReader.getVVals().get(tI))[selectedLevel]);
      // }else if(nefisReader.getUVals().get(tI) instanceof double[][]){
      // us = ((double[][])nefisReader.getUVals().get(tI))[selectedLevel];
      // vs = ((double[][])nefisReader.getVVals().get(tI))[selectedLevel];
      // }

      this.display();
      // this.display(us, vs);
      // this.display(lats, lons, us, vs);
    }

  }

  // @Override
  // public Component getGUI() {
  // return null;
  // }
  // /**
  // * Called when the Layer is removed from the MapBean, giving an
  // opportunity
  // * to clean up.
  // */
  // @Override
  // public void removed(Container cont){
  // if(nefisReader != null){
  // // nefisReader.dispose();
  // nefisReader = null;
  // }
  // }
  class CoordSysDialog extends OptionDialogBase {

    public CoordSysDialog() {
      super("Select Coordinate System for Layer");

      initComponents();
    }

    private void initComponents() {
      JPanel pnlMain = new JPanel(new MigLayout("inset 0, fill"));
      TitledBorder tb = BorderFactory.createTitledBorder("Coordinate System:");
      tb.setTitlePosition(TitledBorder.LEFT);
      pnlMain.setBorder(tb);

      JComboBox cbZones = new JComboBox();
      cbZones.setActionCommand("zone");
      for (int i = 0; i < 60; i++) {
        cbZones.addItem(i + 1);
      }
      cbZones.setSelectedItem(utmZone);// apply the default zone
      JComboBox cbHemis = new JComboBox();
      cbHemis.setActionCommand("hemis");
      cbHemis.addItem("Northern");
      cbHemis.addItem("Southern");

      cbZones.addItemListener(new ItemListener() {

        public void itemStateChanged(ItemEvent e) {
          if (e.getStateChange() == ItemEvent.SELECTED) {
            JComboBox cb = (JComboBox) e.getSource();
            if (cb.getActionCommand().equals("zone")) {
              utmZone = cb.getSelectedIndex() + 1;
            }
          }
        }
      });
      cbHemis.addItemListener(new ItemListener() {

        public void itemStateChanged(ItemEvent e) {
          if (e.getStateChange() == ItemEvent.SELECTED) {
            JComboBox cb = (JComboBox) e.getSource();
            if (cb.getActionCommand().equals("hemis")) {
              if (cb.getSelectedIndex() == 0) {
                utmHemis = 'N';
              } else {
                utmHemis = 'S';
              }
            }
          }
        }
      });

      pnlMain.add(new JLabel("Select UTM Zone:"));
      pnlMain.add(cbZones, "wrap");
      pnlMain.add(new JLabel("Select Hemisphere:"));
      pnlMain.add(cbHemis, "wrap");

      this.add(pnlMain, "wrap, growx");
      this.add(super.buttonPanel("Accept"), "center");
      this.pack();
    }
  }
}
