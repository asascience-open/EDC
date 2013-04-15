package com.asascience.edc.sos.ui;

import com.asascience.edc.Configuration;
import com.asascience.edc.sos.SosServer;
import com.asascience.edc.map.BoundingBoxPanel;
import com.asascience.edc.gui.OpendapInterface;
import com.asascience.edc.gui.jslider.JSlider2Date;
import com.asascience.edc.map.WorldwindSelectionMap;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.border.EtchedBorder;

import net.miginfocom.swing.MigLayout;

import com.asascience.edc.sos.requests.ResponseFormat;
import com.asascience.utilities.Utils;
import gov.nasa.worldwind.render.PointPlacemark;
import java.awt.Dimension;
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
 * SosWorldwindProcessPanel.java
 * 
 * @author Kyle Wilcox <kwilcox@asascience.com>
 */
public class SosWorldwindProcessPanel extends JPanel {

  private SosServer sosServer;
  private String sysDir;
  private WorldwindSelectionMap mapPanel;
  private OpendapInterface parent;
  private SosVariableSelectionPanel selPanel;
  private SosSensorSelectionPanel sensorPanel;
  private SosResponseSelectionPanel responsePanel;
  private JSlider2Date dateSlider;
  private JButton btnProcess;
  private BoundingBoxPanel bboxGui;
  private boolean variableSelected;
  private boolean sensorSelected;

  public SosWorldwindProcessPanel(OpendapInterface caller, SosServer sosd, String homeDir, String sysDir) {
    this.sosServer = sosd;
    this.sysDir = Utils.appendSeparator(sysDir);
    this.parent = caller;
    initComponents();
  }

  public boolean initData() {
    // Set the timeslider values
    // Set start time to 10 days ago
    dateSlider.setRange(sosServer.getParser().getStartTime(), sosServer.getParser().getEndTime());
    Date tempDate = new Date();
    tempDate.setTime(sosServer.getParser().getEndTime().getTime() - 1000*60*60*24*10);
    dateSlider.setStartDate(tempDate);

    // Load variables into the variable pane
    selPanel.addVariables(sosServer.getParser().getVariables());

    // Show station locations on the map
    mapPanel.addSensors(sosServer.getParser().getSensors());

    return true;
  }

  public WorldwindSelectionMap getMapPanel(){
	  return mapPanel;
  }
  
  
  public void setData(SosServer data) {
    this.sosServer = data;
  }

  private boolean initComponents() {
    try {
      setLayout(new MigLayout("gap 0, fill"));
      setBorder(new EtchedBorder());

      JPanel pageTopPanel = new JPanel(new MigLayout("gap 0, fill"));
      
      // Sensor selection panel with checkbox event
      sensorPanel = new SosSensorSelectionPanel("Sensors");
      sensorPanel.addPropertyChangeListener(new PropertyChangeListener() {

        public void propertyChange(PropertyChangeEvent e) {
          if (btnProcess != null) {
            String name = e.getPropertyName();
            if (name.equals("processEnabled")) {
              sensorSelected = !(mapPanel.getSensorLayer().getPickedSensors().isEmpty());
              shouldWeEnableGetObservations();
            } else if (name.equals("sensorClicked")) {
              mapPanel.toggleSensor((PointPlacemark)e.getNewValue());
              sensorSelected = !(mapPanel.getSensorLayer().getPickedSensors().isEmpty());
              shouldWeEnableGetObservations();
            }
          }
        }
      });
      pageTopPanel.add(sensorPanel, "gap 0, growy");

      
      // Map panel with BBOX change event
      String gisDataDir = sysDir + "data";
      
      mapPanel = new WorldwindSelectionMap(gisDataDir);
      mapPanel.addPropertyChangeListener(new PropertyChangeListener() {

        public void propertyChange(PropertyChangeEvent e) {
          String name = e.getPropertyName();
          
          // Bounding box was drawn
          if (name.equals("boundsStored")) {
            if (selPanel != null) {
              bboxGui.setBoundingBox(mapPanel.getSelectedExtent());
              sensorPanel.setWorldwindSensors(mapPanel.getSensorLayer().getSensors());
              sensorSelected = !(mapPanel.getSensorLayer().getPickedSensors().isEmpty());
              shouldWeEnableGetObservations();
            }
          } else if (name.equals("sensorsloaded")) {
            sensorPanel.setWorldwindSensors(mapPanel.getSensorLayer().getSensors());
            sensorSelected = !(mapPanel.getSensorLayer().getPickedSensors().isEmpty());
            shouldWeEnableGetObservations();
          } else if (name.equals("clicked")) {
            sensorPanel.toggleOneSensor((PointPlacemark)e.getNewValue());
            sensorSelected = !(mapPanel.getSensorLayer().getPickedSensors().isEmpty());
            shouldWeEnableGetObservations();
          }
        }
      });
      pageTopPanel.add(mapPanel, "gap 0, grow");

      // Variable selection panel with checkbox event
      selPanel = new SosVariableSelectionPanel("Variables");
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
      pageTopPanel.add(selPanel, "gap 0, growy, wrap");

      
      // Bottom panel
      JPanel pageEndPanel = new JPanel(new MigLayout("gap 0, fill"));

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
      dateSlider.setHandleSize(6);
      timePanel.add(dateSlider, "gap 0, growx, center");
      pageEndPanel.add(timePanel, "gap 0, grow");

      // PROCESS BUTTON panel
      JPanel processPanel = new JPanel();
      btnProcess = new JButton("Get Observations");
      btnProcess.setToolTipText("Apply the specified spatial & temporal constraints\n"
              + "and export the selected variables to the desired output format.");
      btnProcess.setEnabled(false);
      btnProcess.addActionListener(new ActionListener() {

        public void actionPerformed(ActionEvent evt) {
          if (validateAndSetInput()) {
            javax.swing.SwingUtilities.invokeLater(new Runnable() {
              public void run() {
                final JFrame popup = new JFrame("Get Observations");
                popup.setLayout(new MigLayout("fill"));
                popup.setPreferredSize(new Dimension(980, 400));
                popup.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                JComponent newContentPane = new SosGetObsProgressMonitor(sosServer);
                sosServer.getRequest().setParentFrame(popup);
                newContentPane.addPropertyChangeListener(new PropertyChangeListener() {

                  public void propertyChange(PropertyChangeEvent evt) {
                    if (evt.getPropertyName().equals("done")) {
                      if (Configuration.CLOSE_AFTER_PROCESSING) {
                        parent.formWindowClose(evt.getNewValue().toString());
                      }
                    }
                  }
                });
                newContentPane.setOpaque(true);
                
                responsePanel = new SosResponseSelectionPanel("Available Responses");
                responsePanel.addPropertyChangeListener(new PropertyChangeListener() {

                  public void propertyChange(PropertyChangeEvent e) {
                    String name = e.getPropertyName();
                    if (name.equals("selected")) {
                      sosServer.setResponseFormat((ResponseFormat)e.getNewValue());
                    }
                  }
                });
                sosServer.getParser().setPanelType(Configuration.DISPLAY_TYPE);
                sosServer.getParser().parseWorldwindResponseFormats(mapPanel.getSensorLayer().getPickedSensors());
                responsePanel.setResponseFormats(sosServer.getParser().getResponseFormats());
                popup.add(responsePanel);
                popup.add(newContentPane, "grow");
                popup.pack();
                popup.setVisible(true);
              }
            });
          }
        }
      });
      processPanel.add(btnProcess);
      
      add(pageTopPanel, "spanx 3, grow, wrap");
      add(pageEndPanel, "spanx 3, growx, wrap, hmax 200");
      add(processPanel, "spanx 3, growx");

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
    sosServer.getRequest().setWorldwindSelectedSensors((mapPanel.getSensorLayer().getPickedSensors()));
    
    
    // VARIABLES
    if (!variableSelected) {
      JOptionPane.showConfirmDialog(this, "You must select at least one variable",
              "Invalid Variable Selection", JOptionPane.CANCEL_OPTION, JOptionPane.ERROR_MESSAGE);
      return false;
    }
    sosServer.getRequest().setSelectedVariables((selPanel.getSelectedVariables()));
    
    // DATES
    Date startDate = null;
    Date endDate = null;
    try {
      startDate = dateSlider.getStartDate();
      endDate = dateSlider.getEndDate();

      SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm Z");
      TimeZone tz = TimeZone.getTimeZone("GMT");
      dateFormatter.setTimeZone(tz);

      if (startDate.compareTo(endDate) >= 0) {
        JOptionPane.showConfirmDialog(this,
                "The start and end date are invalid (Start is after or equal to end?).", "Invalid Time",
                JOptionPane.CANCEL_OPTION, JOptionPane.ERROR_MESSAGE);
        return false;
      }
      sosServer.getRequest().setStartTime(startDate);
      sosServer.getRequest().setEndTime(endDate);
      
      //System.out.println("Sensor BeginDate:" + dateFormatter.format(startDate));
      //System.out.println("Sensor EndDate:" + dateFormatter.format(endDate));
    } catch (Exception ex) {
      JOptionPane.showConfirmDialog(this, "Date time returned invalid (null) pointer",
              "Invalid Time", JOptionPane.CANCEL_OPTION, JOptionPane.ERROR_MESSAGE);
      ex.printStackTrace();
      return false;
    }

    // TEST THAT SENSORS HAVE THE VARIABLES
    try {
      sosServer.getRequest().validate();
    } catch (Exception e) {
      JOptionPane.showConfirmDialog(this,
              e.getMessage(), "Invalid Query", JOptionPane.CANCEL_OPTION, JOptionPane.ERROR_MESSAGE);
      return false;
    }
    
    //System.out.println("Number of selected sensors: " + sosServer.getData().getSelectedSensorCount());
    //System.out.println("Number of selected variables: " + sosServer.getData().getSelectedVariableCount());

    // TODO: Add a summary popup and confirmation
    return true;
  }

  private void shouldWeEnableGetObservations() {
    btnProcess.setEnabled(variableSelected && sensorSelected);
  }
}
