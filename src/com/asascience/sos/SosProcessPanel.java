/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.asascience.sos;

import com.asascience.edc.gui.BoundingBoxPanel;
import com.asascience.edc.gui.OpendapInterface;
import com.asascience.edc.nc.NetcdfConstraints;
import gov.noaa.pmel.swing.JSlider2Date;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.border.EtchedBorder;

import net.miginfocom.swing.MigLayout;
import ucar.nc2.ui.widget.FileManager;

import com.asascience.openmap.ui.OMSelectionMapPanel;
import com.asascience.utilities.Utils;
import gov.noaa.pmel.util.GeoDate;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

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
  private SosResponseSelectionPanel responsePanel;
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
              //responsePanel.setAvailableResponseFormats(mapPanel.getSensorLayer().getPickedSensors());
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
            if (name.equals("variableClicked")) {
              variableSelected = !(selPanel.getSelectedVariables().isEmpty());
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

      /*
      responsePanel = new SosResponseSelectionPanel(this, "Responses");
      responsePanel.addPropertyChangeListener(new PropertyChangeListener() {

        public void propertyChange(PropertyChangeEvent e) {
          if (btnProcess != null) {
            String name = e.getPropertyName();
            if (name.equals("responseClicked")) {
              responseSelected = !(responsePanel.getSelectedResponse().isEmpty());
              shouldWeEnableGetObservations();
            }
          }
        }
      });
      pageEndPanel.add(responsePanel, "gap 0, grow");
      */
      
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
      btnProcess.addActionListener(new ActionListener() {

        public void actionPerformed(ActionEvent evt) {
          if (validateAndSetInput()) {
            //sosData.getData().getObservations();
            javax.swing.SwingUtilities.invokeLater(new Runnable() {
              public void run() {
                JFrame frame = new JFrame("Get Observations");
                frame.setLayout(new MigLayout("fill"));
                frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                JComponent newContentPane = new SosGetObsProgressMonitor(sosData.getData());
                newContentPane.setOpaque(true);
                responsePanel = new SosResponseSelectionPanel("Available Responses");
                responsePanel.addPropertyChangeListener(new PropertyChangeListener() {

                  public void propertyChange(PropertyChangeEvent e) {
                    String name = e.getPropertyName();
                    if (name.equals("selected")) {
                      sosData.getData().setResponseFormat(e.getNewValue().toString());
                    }
                  }
                });
                responsePanel.setAvailableResponseFormats(mapPanel.getSensorLayer().getPickedSensors());
                frame.add(responsePanel);
                frame.add(newContentPane, "grow");
                frame.pack();
                frame.setVisible(true);
              }
            });
          }
        }
      });
      processPanel.add(btnProcess);
      pageEndPanel.add(processPanel, "spanx 2, grow");

      add(pageEndPanel, "spanx 3, grow");

    } catch (Exception ex) {
      ex.printStackTrace();
    }
    return true;
  }

  private boolean validateAndSetInput() {

    // SENSORS
    if ((mapPanel.getSensorLayer().getPickedSensors().isEmpty())) {
      JOptionPane.showConfirmDialog(this,
                "No Sensors are selected.", "Invalid Sensor",
                JOptionPane.CANCEL_OPTION, JOptionPane.ERROR_MESSAGE);
      return false;
    }
    sosData.getData().setSelectedSensors((mapPanel.getSensorLayer().getPickedSensors()));

    // VARIABLES
    if (!variableSelected) {
      JOptionPane.showConfirmDialog(this, "You must select at least one variable",
              "Invalid Variable Selection", JOptionPane.CANCEL_OPTION, JOptionPane.ERROR_MESSAGE);
      return false;
    }
    sosData.getData().setSelectedVariables((selPanel.getSelectedVariables()));
    
    // DATES
    Date startDate = null;
    Date endDate = null;
    try {
      startDate = dateSlider.getMinValue().getCalendar().getTime();
      endDate = dateSlider.getMaxValue().getCalendar().getTime();

      SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm Z");
      TimeZone tz = TimeZone.getTimeZone("GMT");
      dateFormatter.setTimeZone(tz);

      if (startDate.compareTo(endDate) >= 0) {
        JOptionPane.showConfirmDialog(this,
                "The start and end date are invalid (Start is after or equal to end?).", "Invalid Time",
                JOptionPane.CANCEL_OPTION, JOptionPane.ERROR_MESSAGE);
        return false;
      }
      sosData.getData().setStartTime(startDate);
      sosData.getData().setEndTime(endDate);
      
      System.out.println("Sensor BeginDate:" + dateFormatter.format(startDate));
      System.out.println("Sensor EndDate:" + dateFormatter.format(endDate));
    } catch (Exception ex) {
      JOptionPane.showConfirmDialog(this, "Date time returned invalid (null) pointer",
              "Invalid Time", JOptionPane.CANCEL_OPTION, JOptionPane.ERROR_MESSAGE);
      ex.printStackTrace();
      return false;
    }

    // TEST THAT SENSORS HAVE THE VARIABLES
    try {
      sosData.getData().validate();
    } catch (Exception e) {
      JOptionPane.showConfirmDialog(this,
              e.getMessage(), "Invalid Query", JOptionPane.CANCEL_OPTION, JOptionPane.ERROR_MESSAGE);
      return false;
    }
    
    System.out.println("Number of selected sensors: " + sosData.getData().getSelectedSensorCount());
    System.out.println("Number of selected variables: " + sosData.getData().getSelectedVariableCount());

    // TODO: Add a summary popup and confirmation
    return true;
  }

  private void shouldWeEnableGetObservations() {
    btnProcess.setEnabled(variableSelected && sensorSelected);
  }
}
