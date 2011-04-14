/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.asascience.sos;

import com.asascience.edc.gui.BoundingBoxPanel;
import com.asascience.edc.gui.OpendapInterface;
import com.asascience.edc.nc.NetcdfConstraints;
import gov.noaa.pmel.swing.JSlider2Date;

import java.awt.BorderLayout;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EtchedBorder;

import net.miginfocom.swing.MigLayout;
import ucar.nc2.ui.widget.FileManager;

import com.asascience.openmap.ui.OMSelectionMapPanel;
import com.asascience.utilities.Utils;
import gov.noaa.pmel.util.GeoDate;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

/**
 *
 * @author Kyle
 */
public class SosProcessPanel extends JPanel {

  private SosData sosData;
  private String sysDir;
  private OMSelectionMapPanel mapPanel;
  private SosVariableSelectionPanel selPanel;
  private SosSensorSelectionPanel sensorPanel;
  private JSlider2Date dateSlider;
  private JButton btnProcess;
  private BoundingBoxPanel bboxGui;
  private boolean variableSelected;
  private boolean sensorSelected;

  public SosProcessPanel(ucar.util.prefs.PreferencesExt prefs,
          FileManager fileChooser, OpendapInterface caller,
          SosData sosd, String homeDir, String sysDir) {
    this.sosData = sosd;
    this.sysDir = Utils.appendSeparator(sysDir);
    initComponents();
  }

  public boolean initData() {
    // Set the timeslider values
    // Set start time to 10 days ago
    dateSlider.setRange(sosData.getData().getStartTime(), sosData.getData().getEndTime());
    GeoDate tempDate = new GeoDate();
    tempDate.setTime(System.currentTimeMillis() - 1000*60*60*24*10);
    dateSlider.setStartValue(tempDate);

    // Load variables into the variable pane
    selPanel.addVariables(sosData.getData().getVariables());

    // Show station locations on the map
    mapPanel.addSensors(sosData.getData().getSensors());

    // Load the stations into the sensors pane
    // sensorPanel.addSensors(sosData.getData().getSensors());

    return true;
  }

  public void setData(SosData data) {
    this.sosData = data;
  }

  private boolean initComponents() {
    try {
      setLayout(new MigLayout("gap 0, fill"));
      setBorder(BorderFactory.createRaisedBevelBorder());

      // Sensor selection panel with checkbox event
      sensorPanel = new SosSensorSelectionPanel(this, "Sensors");
      sensorPanel.addPropertyChangeListener(new PropertyChangeListener() {

        public void propertyChange(PropertyChangeEvent e) {
          if (btnProcess != null) {
            String name = e.getPropertyName();
            if (name.equals("processEnabled")) {
              sensorSelected = !(mapPanel.getSensorLayer().getPickedSensors().isEmpty());
              shouldWeEnableGetObservations();
            } else if (name.equals("sensorClicked")) {
              ((SensorPoint)e.getNewValue()).togglePicked();
              //mapPanel.getSensorLayer().repaint();
              sensorSelected = !(mapPanel.getSensorLayer().getPickedSensors().isEmpty());
              shouldWeEnableGetObservations();
            }
          }
        }
      });
      add(sensorPanel, "gap 0");


      // Map panel with BBOX change event
      String gisDataDir = sysDir + "data";
      NetcdfConstraints constraints = new NetcdfConstraints();
      mapPanel = new OMSelectionMapPanel(constraints, gisDataDir, false);
      mapPanel.zoomToDataExtent(sosData.getData().getBBOX());
      mapPanel.setBorder(new EtchedBorder());
      mapPanel.addPropertyChangeListener(new PropertyChangeListener() {

        public void propertyChange(PropertyChangeEvent e) {
          String name = e.getPropertyName();
          // Bounding box was drawn
          if (name.equals("boundsStored")) {
            if (selPanel != null) {
              bboxGui.setBoundingBox(mapPanel.getSelectedExtent());
              sensorPanel.setSensors(mapPanel.getSensorLayer().getSensors());
              sensorSelected = !(mapPanel.getSensorLayer().getPickedSensors().isEmpty());
              shouldWeEnableGetObservations();
            }
          } else if (name.equals("clicked") || name.equals("loaded")) {
            sensorPanel.setSensors(mapPanel.getSensorLayer().getSensors());
            sensorSelected = !(mapPanel.getSensorLayer().getPickedSensors().isEmpty());
            shouldWeEnableGetObservations();
          }
        }
      });
      add(mapPanel, "gap 0, grow");

      // Variable selection panel with checkbox event
      selPanel = new SosVariableSelectionPanel(this, "Variables");
      selPanel.addPropertyChangeListener(new PropertyChangeListener() {

        public void propertyChange(PropertyChangeEvent e) {
          if (btnProcess != null) {
            String name = e.getPropertyName();
            if (name.equals("processEnabled")) {
              variableSelected = Boolean.valueOf(e.getNewValue().toString());
              shouldWeEnableGetObservations();
            }
          }
        }
      });
      add(selPanel, "gap 0, wrap");

      
      // PARENT panel
      JPanel pageEndPanel = new JPanel(new MigLayout("gap 0, fill"));
      //pageEndPanel.setBorder(new EtchedBorder());

      // BBOX panel with bboxchange event
      bboxGui = new BoundingBoxPanel();
      bboxGui.addPropertyChangeListener(new PropertyChangeListener() {

        public void propertyChange(PropertyChangeEvent evt) {
          if (evt.getPropertyName().equals("bboxchange")) {
            mapPanel.makeSelectedExtentLayer(bboxGui.getBoundingBox());
          }
        }
      });
      pageEndPanel.add(bboxGui, "gap 0, growy");

      // TIME panel
      JPanel timePanel = new JPanel(new MigLayout("gap 0, fill"));
      timePanel.setBorder(new EtchedBorder());
      dateSlider = new JSlider2Date();
      dateSlider.setAlwaysPost(true);
      dateSlider.setHandleSize(7);
      timePanel.add(dateSlider, "gap 0, grow, center");
      pageEndPanel.add(timePanel, "gap 0, grow, wrap");

      // PROCESS BUTTON panel
      JPanel processPanel = new JPanel();
      processPanel.setBorder(new EtchedBorder());
      btnProcess = new JButton("Get Observations");
      btnProcess.setToolTipText("Apply the specified spatial & temporal constraints\n"
              + "and export the selected variables to the desired output format.");
      btnProcess.setEnabled(false);
      processPanel.add(btnProcess);
      pageEndPanel.add(processPanel, "spanx 2, grow");

      add(pageEndPanel, "spanx 3, grow");

    } catch (Exception ex) {
      ex.printStackTrace();
    }
    return true;
  }

  private void shouldWeEnableGetObservations() {
    btnProcess.setEnabled(variableSelected && sensorSelected);
  }
}
