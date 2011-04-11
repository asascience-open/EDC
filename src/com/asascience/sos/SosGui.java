/*
 * Applied Science Associates, Inc.
 * Copyright 2009. All Rights Reserved.
 *
 * SosData.java
 *
 * Created on Aug 25, 2009 @ 11:51:16 AM
 */
package com.asascience.sos;

import gov.noaa.pmel.swing.JSlider2Date;
import gov.noaa.pmel.util.GeoDate;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;

import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

import net.miginfocom.swing.MigLayout;

import org.softsmithy.lib.swing.JDoubleField;

import com.asascience.components.JCheckBoxList;

/**
 * 
 * @author DAS <dstuebe@asascience.com>
 */
public class SosGui {

  // Program plan:
  // 0) Parse GetCap
  // 1) Show GUI with dots for stations
  // 2) Enter AOI (draw with mouse)
  // 3) Enter Time
  // 4) Call GetObservation to get Data
  // 4) Output in desired format
  /**
   * GetCapabilities - This function returns metadata about this service. (Not
   * fully implemented.) Parameters include: o request = GetCapabilities o
   * service = SOS DescribeSensor - This function returns detailed sensor
   * charactertistics. (Not fully implemented. Only returns position at this
   * time.) Parameters include: o request = DescribeSensor o OutputFormat =
   * text/xml;subtype="sensorML/1.0.0" o procedure = urn of sensor to describe
   * (see procedure in GetCapabilities response) o service = SOS o version =
   * 1.0.0 GetObservation - This function returns observation data of the
   * specified type for the stations within the specified start and stop
   * dates. (Not fully implemented - Collections are not available for
   * currents.) Parameters include: o request = GetObservation o service = SOS
   * o version = 1.0.0 o offering =
   * urn:x-noaa:def:station:noaa.nws.ndbc::<station ID> for singe station or
   * urn:x-noaa:def:network:noaa.nws.ndbc::all for use with collections. Other
   * networks may be added in the future. o observedproperty = one of the
   * following: + Currents + Salinity + WaterLevel + WaterTemperature + Waves
   * + Winds o responseformat = one of the following: +
   * text/xml;schema="ioos/0.6.1" + application/ioos+xml;version=0.6.1 o
   * eventtime (optional parameter) = <DateTime or DateTime1/DateTime2> +
   * DateTime is formatted as 2008-06-04-T00:00:00Z or 2008-06-04-T00:00Z. At
   * this time, only the seconds are optional. + When this parameter is
   * omitted, the most recent observation is returned. o featureofinterest
   * (optional parameter) = BBOX:<min_lon>,<min_lat>,<max_lon>,<max_lat>
   *
   * @return
   **/
  private JPanel dataPanel;
  private JTextField txtInput;
  private JDoubleField nEdge;
  private JDoubleField sEdge;
  private JDoubleField eEdge;
  private JDoubleField wEdge;
  private JCheckBoxList varBoxList;
  List<JCheckBox> boxList = null;
  JSlider2Date dateSlider = null;
  private double[] bndsNESW;
  private SosData myData;
  JDialog mainPane;
  private Component parent;

  public SosGui(Component parent) {
    this.parent = parent;
  }

  // This Kicks off the GUI and waits for user action:
  public void sosAction(SosData myData) {

    this.myData = myData;

    bndsNESW = myData.getData().getNESW();

    mainPane = new JDialog();
    mainPane.setTitle("SOS Data Connector");
    JPanel mainPanel = new JPanel(new MigLayout("fillx, insets 0"));

    // create a panel to hold all the time related components
    JPanel timePanel = new JPanel(new MigLayout("insets 0, fill"));
    // timePanel.setBorder(new EtchedBorder());
    timePanel.setBorder(BorderFactory.createLineBorder(Color.black));

    dateSlider = new JSlider2Date();
    dateSlider.setAlwaysPost(true);
    dateSlider.setHandleSize(7);// default is 6

    // dateSlider.addPropertyChangeListener(new PropertyChangeListener() {
    //
    // public void propertyChange(PropertyChangeEvent evt) {
    // String name = evt.getPropertyName();
    // if (name.equals("minValue")) {
    // //
    // constraints.setStartTime(dateSlider.getMinValue().getCalendar().getTime());
    // } else if (name.equals("maxValue")) {
    // //
    // constraints.setEndTime(dateSlider.getMaxValue().getCalendar().getTime());
    // }
    // }
    // });

    // JLabel lblDateIncrement = new JLabel("Time Interval (sec): ");
    // JLabel lblNumDatesSelected = new JLabel("# Timesteps Selected: ");
    // timePanel.add(lblDateIncrement,
    // "gap 0, gapright 20, center, split 2");
    // timePanel.add(lblNumDatesSelected,
    // "gap 0, gapleft 20, center, wrap");
    timePanel.add(dateSlider, "gap 0, grow, center");

    dateSlider.setRange(myData.getData().getStartTime(), myData.getData().getEndTime());
    GeoDate tempDate = new GeoDate();
    tempDate.setDate(tempDate.getDate() - 10);
    dateSlider.setStartValue(tempDate);


    // Make AOI panel
    JPanel aoi = new JPanel(new MigLayout("insets 1, fill"));

    aoi.setBorder(BorderFactory.createLineBorder(Color.black));

    nEdge = new JDoubleField();
    nEdge.setValue(BigDecimal.valueOf(bndsNESW[0]));

    sEdge = new JDoubleField();
    sEdge.setValue(BigDecimal.valueOf(bndsNESW[2]));

    eEdge = new JDoubleField();
    eEdge.setValue(BigDecimal.valueOf(bndsNESW[1]));

    wEdge = new JDoubleField();
    wEdge.setValue(BigDecimal.valueOf(bndsNESW[3]));

    aoi.add(new JLabel("North"), "cell 2 1, align center");
    aoi.add(nEdge, "cell 2 2, width 60, align center");
    aoi.add(new JLabel("West"), "cell 1 2, align center");
    aoi.add(wEdge, "cell 1 3, width 60, align center");
    aoi.add(new JLabel("East"), "cell 3 2, align center");
    aoi.add(eEdge, "cell 3 3, width 60, align center");
    aoi.add(new JLabel("South"), "cell 2 4, align center");
    aoi.add(sEdge, "cell 2 5, width 60, align center");

    JLabel aoiLabel = new JLabel("SET Area of Interest");
    aoi.add(aoiLabel, "cell 2 3, align center");

    // JLabel aoiLabel= new JLabel("SET Area of Interest");
    // timePanel.add(aoiLabel,"align center, growy");
    timePanel.add(aoi);

    varBoxList = new JCheckBoxList();
    varBoxList.setBorder(BorderFactory.createLineBorder(Color.black));

    String[] myVars = myData.getData().getvarNames();

    // List<ItemListener> itemListenerList = new ArrayList<ItemListener>();
    boxList = new ArrayList<JCheckBox>();
    for (int ctr = 0; ctr < myVars.length; ctr++) {
      JCheckBox myBox = new JCheckBox();
      myBox.setText(myVars[ctr]);
      myBox.setMnemonic(ctr + 1);
      myBox.setSelected(true);

      // itemListenerList.add(myListener);
      boxList.add(myBox);
    }
    varBoxList.setListData(boxList.toArray());

    JScrollPane sp = new JScrollPane();
    sp.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
    sp.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

    sp.setPreferredSize(new Dimension(250, 200));

    sp.setViewportView(varBoxList);
    timePanel.add(sp, "wrap");

    // mainPanel.add(aoi,"split 2, w 50%");

    JButton getData = new JButton("Get Data");
    getData.addActionListener(new ActionListener() {

      public void actionPerformed(ActionEvent evt) {
        // Validate inputs
        // sosAction.this.Cntr
        // Call function

        if (validateInput()) {

          System.out.println("Go Get The Data!");
          SosGui.this.myData.getData().getObservations();
        }

        // dataPanel.setEnabled(true);
        // txtInput.setEnabled(true);
        // dataPanel.getParent().invalidate();

      }
    });
    timePanel.add(getData, "wrap");

    mainPanel.add(timePanel, "wrap");

    // dataPanel = new JPanel();
    // txtInput = new JTextField("Stuff Goes Here!");
    // txtInput.setEnabled(false);
    // dataPanel.add(txtInput);
    // dataPanel.setEnabled(false);
    // mainPanel.add(dataPanel, "wrap");

    mainPane.add(mainPanel);
    mainPane.setModal(true);

    mainPane.pack();
    mainPane.setLocationRelativeTo(parent);
    mainPane.setVisible(true);

  }

  // if(ncReader.isHasTime()){
  // dateSlider.setRange(ncReader.getStartTime(), ncReader.getEndTime());
  // lblDateIncrement.setText(lblDateIncrement.getText() +
  // constraints.getTimeInterval());
  // lblNumDatesSelected.setText("# Timesteps Selected: " +
  // Math.round(calcNumTimesteps()));
  // }else{
  // dateSlider.reset();
  // dateSlider.setVisible(false);
  // lblDateIncrement.setText(lblDateIncrement.getText() + "NA");
  // lblNumDatesSelected.setText("# Timesteps Selected: " + "NA");
  // }
  // Make sure AOI is reasonable.
  public boolean validateInput() {

    // Check North South Bounds
    if (nEdge.getDoubleValue() <= sEdge.getDoubleValue()) {
      JOptionPane.showConfirmDialog((Component) mainPane,
              "The selected North and South Boundary are invalid (N latidtude < S latitude).", "Invalid Region",
              JOptionPane.CANCEL_OPTION, JOptionPane.ERROR_MESSAGE);
      return false;
    }

    // Check EAST - WEST BOUNDS
    if (wEdge.getDoubleValue() < -180.0 || 180.0 < wEdge.getDoubleValue()) {
      JOptionPane.showConfirmDialog((Component) mainPane,
              "The Western boundary selected is not between -180 and 180", "ASA", JOptionPane.CANCEL_OPTION,
              JOptionPane.ERROR_MESSAGE);
      return false;
    }

    if (eEdge.getDoubleValue() < -180.0 || 180.0 < eEdge.getDoubleValue()) {
      JOptionPane.showConfirmDialog((Component) mainPane,
              "The Eastern boundary selected is not between -180 and 180", "ASA", JOptionPane.CANCEL_OPTION,
              JOptionPane.ERROR_MESSAGE);
      return false;
    }

    double[] NESW = new double[4];

    NESW[0] = nEdge.getDoubleValue();
    NESW[1] = eEdge.getDoubleValue();
    NESW[2] = sEdge.getDoubleValue();
    NESW[3] = wEdge.getDoubleValue();

    Date startDate = null;
    Date endDate = null;
    try {

      startDate = dateSlider.getMinValue().getCalendar().getTime();
      endDate = dateSlider.getMaxValue().getCalendar().getTime();

      SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm Z");
      TimeZone tz = TimeZone.getTimeZone("GMT");
      dateFormatter.setTimeZone(tz);

//			System.out.println("Sensor BeginDate:" + dateFormatter.format(startDate));
//			System.out.println("Sensor EndDate:" + dateFormatter.format(endDate));

      // System.out.println("My SDate is: " +
      // SosData.getStartTime().toString());

      // System.out.println("My EDate is: " +
      // SosData.getEndTime().toString());

      if (startDate.compareTo(endDate) >= 0) {
        JOptionPane.showConfirmDialog((Component) mainPane,
                "The start and end date are invalid (Start is after or equal to end?).", "Invlaid Time",
                JOptionPane.CANCEL_OPTION, JOptionPane.ERROR_MESSAGE);
        return false;
      }
    } catch (Exception ex) {
      JOptionPane.showConfirmDialog((Component) mainPane, "Date time returned invalid (null) pointer",
              "Invlaid Time", JOptionPane.CANCEL_OPTION, JOptionPane.ERROR_MESSAGE);
      ex.printStackTrace();
      return false;
    }

    // Object[] oArray = varBoxList.getComponents();

    List<String> selectedVars = new ArrayList<String>();

    System.out.println("======= Variable Selected ========");
    for (JCheckBox box : boxList) {
      if (box.isSelected()) {
        System.out.println("Variable: " + box.getText());
        selectedVars.add(box.getText());
      }
    }

    if (selectedVars.size() < 1) {
      JOptionPane.showConfirmDialog((Component) mainPane, "You must select at least one variable",
              "Invlaid Variable Selection", JOptionPane.CANCEL_OPTION, JOptionPane.ERROR_MESSAGE);
      return false;
    }

    // Determine which sensors to select!
    myData.getData().setSelectedSensors(NESW, startDate, endDate, selectedVars);

    System.out.println("Number of selected sensors: " + myData.getData().getSelectedSensorCnt());

    int[] numVars = myData.getSelectedVarCnt();

    int cnt = Math.min(selectedVars.size(), numVars.length);
    for (int ind = 0; ind < cnt; ind++) {

      System.out.println("Number of " + selectedVars.get(ind) + " sensors found: " + numVars[ind]);
    }

    if (myData.getData().getSelectedSensorCnt() < 1) {
      JOptionPane.showConfirmDialog((Component) mainPane,
              "The selected area did not contain any sensors with the selected varaibles",
              "Invlaid Variable Selection", JOptionPane.CANCEL_OPTION, JOptionPane.ERROR_MESSAGE);
      return false;
    }

    // Generate message for joption pane
    String[] message = new String[cnt + 2];
    message[0] = "Number of selected sensors: " + myData.getData().getSelectedSensorCnt();
    for (int ind = 0; ind < cnt; ind++) {

      message[ind + 1] = "Number of " + selectedVars.get(ind) + " sensors found: " + numVars[ind];
    }
    message[cnt + 1] = "To refine your region hit Cancel, To get the Data press Ok!";

    int fire = JOptionPane.showConfirmDialog((Component) mainPane, message, "Confirm Data Request",
            JOptionPane.CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);

    if (fire == JOptionPane.CANCEL_OPTION) {
      return false;
    }

    return true;
  }
}
