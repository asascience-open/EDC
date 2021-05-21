/*
 * OMTimeSlider.java
 *
 * Created on August 1, 2007, 4:16 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package com.asascience.openmap.ui;

import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;
import java.util.TimeZone;
import java.util.logging.Logger;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.JToolBar;
import javax.swing.SpinnerNumberModel;
import javax.swing.Timer;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.asascience.openmap.layer.TimeLayer;
import com.asascience.ui.CheckBoxList;
import com.asascience.ui.OptionDialogBase;
import com.asascience.utilities.Utils;
import com.bbn.openmap.Layer;
import com.bbn.openmap.gui.OMComponentPanel;

import net.miginfocom.swing.MigLayout;

/**
 * 
 * @author cmueller
 */
public class OMTimeSlider extends OMComponentPanel implements ActionListener, ChangeListener {

  private Logger logger = Logger.getLogger("TimerLayerHandler");
  private SimpleDateFormat sdformat;
  private final static String TIMER_RESET = "reset";
  private final static String TIMER_TO_END = "endset";
  private final static String TIMER_STOP = "stop";
  private final static String TIMER_FORWARD = "forward";
  private final static String TIMER_BACKWARD = "backward";
  private final static String TIMER_STEP_FORWARD = "stepforward";
  private final static String TIMER_STEP_BACKWARD = "stepbackward";
  private final static String SELECT_LAYERS = "selectlayers";
  private final static String UNIQUE_TIMES_HELP = "uthelp";
  // TO DO: check to see which of these are being used...
  protected Timer timer;
  protected double updateInterval = 0.1;
  protected long time = 0;
  protected int timeIncrement = 1;
  protected int clockDirection = 1;
  protected Date timeDate = null;
  protected boolean restart = false;
  protected long minIncrement = Long.MAX_VALUE;
  protected long incrementBy = (long) ((60 * 60 * 1000));// ) * 0.5);//default
  // 1 hr increment in
  // ms
  // private Calendar gblStartCal;
  // private Calendar gblEndCal;
  // private Calendar gblCurrCal;
  protected long gblStartTime = Long.MAX_VALUE;
  protected long gblEndTime = Long.MIN_VALUE;
  private long gblCurrTime;
  protected ImageIcon backwardStepIcon;
  protected ImageIcon backwardIcon;
  protected ImageIcon forwardIcon;
  protected ImageIcon forwardStepIcon;
  protected ImageIcon pauseIcon;
  protected ImageIcon stopIcon;
  protected ImageIcon resetIcon;
  protected ImageIcon endsetIcon;
  protected ImageIcon helpIcon;
  protected JButton btnBackwardStep;
  protected JButton btnBack;
  protected JButton btnForward;
  protected JButton btnForwardStep;
  protected JButton pause;
  protected JButton btnStop;
  protected JButton btnReset;
  protected JButton btnEndset;
  protected JButton btnUtHelp;
  protected JLabel lblStartTime;
  protected JLabel lblEndTime;
  protected JLabel lblCurrTime;
  protected JSlider timeSlider;
  protected JSpinner intervalSpinner;
  protected SpinnerNumberModel spinnerNumModel;
  // protected String drawLayer = ALL_LAYERS;
  private DrawLayerDialog dldLayers;
  protected ImageIcon layersIcon;
  protected JButton btnTLayers;
  protected JCheckBox cbUseUniqueTimes;
  protected List<TimeLayer> layers;
  private List<String> drawLayers;
  private boolean useUniqueTimes;
  private Long[] uniqueTimes;
  private List<Long> uTimesList;

  /** Creates a new instance of OMTimeSlider */
  public OMTimeSlider() {
    layers = new ArrayList<TimeLayer>();
    drawLayers = new ArrayList<String>();
    sdformat = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss.SSS");
    sdformat.setTimeZone(TimeZone.getTimeZone("UTC"));

    // timer = new Timer(100, this);
    ActionListener timerListener = new ActionListener() {

      public void actionPerformed(ActionEvent evt) {
        if (getClockDirection() == 1) {
          if (timeSlider.getValue() == timeSlider.getMaximum()) {
            timeSlider.setValue(timeSlider.getMinimum());
          } else {
            timeSlider.setValue(timeSlider.getValue() + 1);
            // setTime(getTime() + timeIncrement);
          }
        } else if (getClockDirection() == -1) {
          if (timeSlider.getValue() == timeSlider.getMinimum()) {
            timeSlider.setValue(timeSlider.getMaximum());
          } else {
            timeSlider.setValue(timeSlider.getValue() - 1);
            // setTime(getTime() - timeIncrement);
          }
        }
      }
    };
    timer = new Timer((int) (updateInterval * 1000), timerListener);
    initComponents();
    /** Default "Use Unique Times" to on */
    useUniqueTimes = true;
  }

  public void initComponents() {
    this.setLayout(new MigLayout("insets 0, fill"));
    removeAll();
    try {
      forwardIcon = new ImageIcon(Utils.getImageResource("playforward.png", OMTimeSlider.class));
      forwardStepIcon = new ImageIcon(Utils.getImageResource("stepforward.png", OMTimeSlider.class));
      backwardIcon = new ImageIcon(Utils.getImageResource("playbackward.png", OMTimeSlider.class));
      backwardStepIcon = new ImageIcon(Utils.getImageResource("stepbackward.png", OMTimeSlider.class));
      pauseIcon = new ImageIcon(Utils.getImageResource("pause.png", OMTimeSlider.class));
      stopIcon = new ImageIcon(Utils.getImageResource("stop.png", OMTimeSlider.class));
      resetIcon = new ImageIcon(Utils.getImageResource("reset.png", OMTimeSlider.class));
      endsetIcon = new ImageIcon(Utils.getImageResource("endset.png", OMTimeSlider.class));
      layersIcon = new ImageIcon(Utils.getImageResource("layers.png", OMTimeSlider.class));
      helpIcon = new ImageIcon(Utils.getImageResource("helpicon.png", OMTimeSlider.class));
    } catch (NullPointerException npe) {
      npe.printStackTrace();
    }

    JToolBar jtb = new JToolBar();
    jtb.setBorder(new EtchedBorder());
    jtb.setLayout(new MigLayout("insets 0, fillx"));
    jtb.setFloatable(true);
    jtb.setRollover(true);

    jtb.add(sliderPanel(), "center, growx, wrap");
    jtb.add(controlPanel(), "center");

    add(jtb, "growx");
    enableControls();
  }

  protected JPanel controlPanel() {
    JPanel controlPanel = new JPanel(new MigLayout("insets 1"));

    btnTLayers = new JButton(layersIcon);
    btnTLayers.setToolTipText("Select layers to include in timeline");
    btnTLayers.setActionCommand(SELECT_LAYERS);
    btnTLayers.addActionListener(this);
    // controlPanel.add(new JLabel("Layers:"));
    controlPanel.add(btnTLayers);

    btnReset = new JButton(resetIcon);
    btnReset.setToolTipText("Go To Start");
    btnReset.setActionCommand(TIMER_RESET);
    btnReset.addActionListener(this);
    controlPanel.add(btnReset, "split 7, gapright 0");

    btnBack = new JButton(backwardIcon);
    btnBack.setToolTipText("Play Backwards");
    btnBack.setActionCommand(TIMER_BACKWARD);
    btnBack.addActionListener(this);
    controlPanel.add(btnBack, "gapright 0");

    btnBackwardStep = new JButton(backwardStepIcon);
    btnBackwardStep.setToolTipText("Step Backward");
    btnBackwardStep.setActionCommand(TIMER_STEP_BACKWARD);
    btnBackwardStep.addActionListener(this);
    controlPanel.add(btnBackwardStep, "gapright 0");

    btnStop = new JButton(stopIcon);
    btnStop.setToolTipText("Stop");
    btnStop.setActionCommand(TIMER_STOP);
    btnStop.addActionListener(this);
    controlPanel.add(btnStop, "gapright 0");

    btnForwardStep = new JButton(forwardStepIcon);
    btnForwardStep.setToolTipText("Step Forward");
    btnForwardStep.setActionCommand(TIMER_STEP_FORWARD);
    btnForwardStep.addActionListener(this);
    controlPanel.add(btnForwardStep, "gapright 0");

    btnForward = new JButton(forwardIcon);
    btnForward.setToolTipText("Play Forward");
    btnForward.setActionCommand(TIMER_FORWARD);
    btnForward.addActionListener(this);
    controlPanel.add(btnForward, "gapright 0");

    btnEndset = new JButton(endsetIcon);
    btnEndset.setToolTipText("Go To End");
    btnEndset.setActionCommand(TIMER_TO_END);
    btnEndset.addActionListener(this);
    controlPanel.add(btnEndset);

    spinnerNumModel = new SpinnerNumberModel(0.1, 0.1, 2, 0.1);
    intervalSpinner = new JSpinner(spinnerNumModel);
    intervalSpinner.addChangeListener(new ChangeListener() {

      public void stateChanged(ChangeEvent e) {
        updateInterval = spinnerNumModel.getNumber().doubleValue();
        timer.setDelay((int) (updateInterval * 1000));
      }
    });
    controlPanel.add(new JLabel("Refresh Rate (sec):"), "gapleft 10, gapright 0");
    controlPanel.add(intervalSpinner, "gapright 0");

    cbUseUniqueTimes = new JCheckBox("Use Unique Times");
    // cbUseUniqueTimes.setToolTipText("When selected, the unique times of all loaded datasets will be used as tick marks.  "
    // +
    // "Otherwise, the smallest interval is used.");
    cbUseUniqueTimes.setSelected(true);
    cbUseUniqueTimes.addActionListener(new ActionListener() {

      public void actionPerformed(ActionEvent e) {
        useUniqueTimes = cbUseUniqueTimes.isSelected();
        updateUniqueTimes();
        updateTimeSliderRange();
      }
    });
    controlPanel.add(cbUseUniqueTimes);

    btnUtHelp = new JButton(helpIcon);
    btnUtHelp.setBorderPainted(false);
    btnUtHelp.setToolTipText("Display help for the \"Use Unique Times\" control.");
    btnUtHelp.setActionCommand(UNIQUE_TIMES_HELP);
    btnUtHelp.addActionListener(this);
    controlPanel.add(btnUtHelp);

    return controlPanel;
  }

  protected JPanel sliderPanel() {
    JPanel sliderPanel = new JPanel(new MigLayout("insets 0, fill"));
    // sliderPanel.setBorder(new EtchedBorder());
    lblCurrTime = new JLabel("current");
    lblCurrTime.setAlignmentX(JLabel.CENTER_ALIGNMENT);
    lblCurrTime.setFont(new Font("Microsoft Sans Sarif", Font.PLAIN, 10));
    lblStartTime = new JLabel("start");
    lblStartTime.setFont(new Font("Microsoft Sans Sarif", Font.PLAIN, 10));
    lblEndTime = new JLabel("end");
    lblEndTime.setFont(new Font("Microsoft Sans Sarif", Font.PLAIN, 10));
    timeSlider = new JSlider(JSlider.HORIZONTAL);
    timeSlider.setSnapToTicks(true);
    timeSlider.setValue(0);

    sliderPanel.add(lblCurrTime, "cell 1 0, align center, wrap");
    sliderPanel.add(lblStartTime);
    sliderPanel.add(timeSlider, "growx");
    sliderPanel.add(lblEndTime);

    timeSlider.addChangeListener(this);

    return sliderPanel;
  }

  protected void enableControls() {
    boolean enable = false;
    if (layers.size() > 0) {
      enable = true;
    }

    timeSlider.setEnabled(enable);

    btnTLayers.setEnabled(enable);

    btnBackwardStep.setEnabled(enable);
    btnBack.setEnabled(enable);
    btnForward.setEnabled(enable);
    btnForwardStep.setEnabled(enable);
    btnStop.setEnabled(enable);
    btnReset.setEnabled(enable);
    btnEndset.setEnabled(enable);
    intervalSpinner.setEnabled(enable);
    cbUseUniqueTimes.setEnabled(enable);
    btnUtHelp.setEnabled(enable);
  }

  public void resetSlider() {
    this.lblStartTime.setText("start");
    this.lblCurrTime.setText("current");
    this.lblEndTime.setText("end");

    timeSlider.setMaximum(0);
    timeSlider.setMinimum(0);
    timeSlider.setSnapToTicks(true);
    timeSlider.setPaintTicks(false);
  }

  protected long getTimeFromCal(GregorianCalendar c) {
    return c.getTimeInMillis();
  }

  protected GregorianCalendar getCalFromTime(long time) {
    GregorianCalendar c = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
    c.setTimeInMillis(time);
    return c;
  }

  protected String getTimeString(long time) {
    return sdformat.format(getCalFromTime(time).getTime());
  }

  protected void updateGlobalTimeRange() {
    // btnReset the gblStart & gblEnd times to clear everything...
    this.gblStartTime = Long.MAX_VALUE;
    this.gblEndTime = Long.MIN_VALUE;
    this.minIncrement = Long.MAX_VALUE;

    // System.err.println("numLayers="+layers.size());
    Iterator<TimeLayer> it = layers.iterator();
    TimeLayer tl;

    while (it.hasNext()) {
      tl = (TimeLayer) it.next();
      if (drawLayers.contains(tl.getName())) {
        // System.err.println(this.gblStartTime);
        if (this.gblStartTime == Long.MAX_VALUE) {
          this.gblStartTime = tl.getStartTime();
        } else {
          // if the gblStartTime is later (greater) than the layers
          // starttime - fix it
          if (this.gblStartTime > tl.getStartTime()) {
            this.gblStartTime = tl.getStartTime();
          }
        }

        if (this.gblEndTime == Long.MIN_VALUE) {
          this.gblEndTime = tl.getEndTime();
        } else {
          if (this.gblEndTime < tl.getEndTime()) {
            this.gblEndTime = tl.getEndTime();
          }
        }

        // System.err.println(tl.getTimeIncrement());
        if (this.minIncrement > tl.getTimeIncrement()) {
          this.minIncrement = tl.getTimeIncrement();
        }

      }
    }

    // <editor-fold defaultstate="collapsed" desc=" Old & Unused ">

    // if(drawLayer.equals(ALL_LAYERS)){//calculate for all layers
    // //if there's only one layer, use that layers info
    //
    // if(layers.size() == 1){
    // tl = layers.get(0);
    // this.gblStartTime = tl.getStartTime();
    // this.gblEndTime = tl.getEndTime();
    // this.minIncrement = tl.getTimeIncrement();
    // }else{//there's more than one layer
    //
    // while(it.hasNext()){
    // tl = (TimeLayer)it.next();
    // // System.err.println(this.gblStartTime);
    // if(this.gblStartTime == Long.MAX_VALUE){
    // this.gblStartTime = tl.getStartTime();
    // }else{
    // //if the gblStartTime is later (greater) than the layers starttime -
    // fix it
    // if(this.gblStartTime > tl.getStartTime()){
    // this.gblStartTime = tl.getStartTime();
    // }
    // }
    //
    // if(this.gblEndTime == Long.MIN_VALUE){
    // this.gblEndTime = tl.getEndTime();
    // }else{
    // if(this.gblEndTime < tl.getEndTime()){
    // this.gblEndTime = tl.getEndTime();
    // }
    // }
    //
    // // System.err.println(tl.getTimeIncrement());
    // if(this.minIncrement > tl.getTimeIncrement()){
    // this.minIncrement = tl.getTimeIncrement();
    // }
    //
    // }
    // }
    // }else{//calculate for the particular layer of interest
    //
    // while(it.hasNext()){
    // tl = (TimeLayer)it.next();
    // if(tl.getName().equals(drawLayer)){
    // this.gblStartTime = tl.getStartTime();
    // this.gblEndTime = tl.getEndTime();
    // this.minIncrement = tl.getTimeIncrement();
    // break;
    // }
    // }
    // }

    // </editor-fold>

    // set the incrementBy value to the minimum increment of the layers
    if (this.minIncrement != Long.MAX_VALUE) {
      this.incrementBy = this.minIncrement;
    }

    // <editor-fold defaultstate="collapsed" desc=" Old & Unused ">

    //
    // /**For each layer, if not null - compareTo returns 0 if the calendars
    // are equal,
    // <0 if the arguement is AFTER the calling calendar,
    // and >0 if the arguement is BEFORE the calling calendar*/
    // while(it.hasNext()){
    // tl = (TimeLayer)it.next();
    // // System.err.println(tl.getName());
    // if(this.gblStartCal == null){
    // // System.err.println("sc is null");
    // this.gblStartCal = tl.getStartCal();
    // // System.err.println(this.gblStartCal.getTimeInMillis());
    // }else{
    // //For StartCal: if the lyrCal is before the current gblCal, update
    // the gblCal
    // int i = tl.getStartCal().compareTo(this.gblStartCal);
    // if(i > 0) this.gblStartCal = tl.getStartCal();
    // }
    //
    // if(this.gblEndCal == null){
    // // System.err.println("ec is null");
    // this.gblEndCal = tl.getEndCal();
    // // System.err.println(this.gblEndCal.getTimeInMillis());
    // }else{
    // //For EndCal: if the lyrCal is after the current gblCal, update the
    // gblCal
    // int i = tl.getEndCal().compareTo(this.gblEndCal);
    // if(i < 0) this.gblEndCal = tl.getEndCal();
    // }
    // }

    // set the gblCurrCal = gblStartCal
    // this.gblCurrCal = this.gblStartCal;

    // this.gblStartTime = this.gblStartCal.getTimeInMillis();
    // this.gblEndTime = this.gblEndCal.getTimeInMillis();

    // </editor-fold>
  }

  protected void updateTimeSliderRange() {
    int steps;
    if (useUniqueTimes) {
      steps = uniqueTimes.length - 1;
    } else {
      // long sT = this.gblStartCal.getTimeInMillis();
      // long eT = this.gblEndCal.getTimeInMillis();
      // int steps = (int)((eT - sT) / incrementBy);
      if (this.gblStartTime == this.gblEndTime) {
        steps = 1;
      } else {
        steps = (int) ((this.gblEndTime - this.gblStartTime) / this.incrementBy);
      }
      // System.err.println("sT=" + sT + " eT=" + eT);
      // System.err.println("steps=" + steps);
    }
    // fix the labels
    // this.lblStartTime.setText(this.gblStartCal.getTime().toString());
    // this.lblEndTime.setText(this.gblEndCal.getTime().toString());
    this.lblStartTime.setText(this.getTimeString(this.gblStartTime));
    this.lblEndTime.setText(this.getTimeString(this.gblEndTime));

    // fix the slider steps
    timeSlider.setMinimum(0);
    timeSlider.setMaximum(steps);
    timeSlider.setMinorTickSpacing(1);
    timeSlider.setMajorTickSpacing((int) (steps / 5));// major tick every 5
    // steps
    // updateSliderLabels(steps);

    timeSlider.setPaintTrack(true);
    // Only draw tick marks if the number of steps is less than:
    // - 15 days of 1 second interval
    // - 900 days of 1 minute interval
    // - 54000 days of 1 hour interval
    if (steps <= 1296000) {
      timeSlider.setPaintTicks(true);
    } else {
      timeSlider.setPaintTicks(false);
    }

    // timeSlider.setValue(0);
  }

  protected void updateUniqueTimes() {
    uTimesList = new ArrayList<Long>();
    List<Long> layerTimes = null;
    for (TimeLayer tl : layers) {
      if (drawLayers.contains(tl.getName())) {
        // layerTimes = tl.getUniqueTimes();
        layerTimes = Arrays.asList(tl.getTimes());
        for (Long l : layerTimes) {
          if (!uTimesList.contains(l)) {
            uTimesList.add(l);
          }
        }
      }
    }
    uniqueTimes = new Long[uTimesList.size()];
    uTimesList.toArray(uniqueTimes);
    Arrays.sort(uniqueTimes);
  }

  public boolean addLayer(Layer lyr) {
    try {
      if (lyr != null) {
        if (lyr instanceof TimeLayer) {
          TimeLayer tl = (TimeLayer) lyr;
          // resetSlider();
          if (!tl.isHasTimes()) {
            return true;
          }
          if (!layers.contains(tl)) {
            layers.add(tl);

            if (!drawLayers.contains(tl.getName())) {
              drawLayers.add(tl.getName());
            }

            updateUniqueTimes();
            updateGlobalTimeRange();
            updateTimeSliderRange();

            enableControls();

            redrawTimestep();

            return true;
          }
        }
      }
    } catch (Exception ex) {
      logger.warning("TLH:addLayer - Problem Adding Layer:\n");
      ex.printStackTrace();
    }
    return false;
  }

  public void redrawTimestep() {
    if (this.gblCurrTime == 0) {
      this.gblCurrTime = this.gblStartTime;
    }
    for (int i = 0; i <= timeSlider.getMaximum(); i++) {
      long add = i * this.incrementBy;
      if (this.gblCurrTime == (this.gblStartTime + add)) {
        if (timeSlider.getValue() == i) {// if the slider position
          // hasn't changed

          setTime(this.gblCurrTime);
        } else {
          timeSlider.setValue(i);
        }
        break;
      }
    }
  }

  private void checkLayerCount() {
    if (layers.size() == 0) {
      this.resetSlider();
    }
  }

  public boolean removeLayer(TimeLayer layer) {
    boolean ret = false;
    try {
      for (int i = 0; i < layers.size(); i++) {
        if (layer == layers.get(i)) {
          layers.remove(i);
          drawLayers.remove(layer.getName());

          updateUniqueTimes();
          updateGlobalTimeRange();
          updateTimeSliderRange();

          enableControls();
          ret = true;
        }
      }
    } catch (Exception ex) {
      ex.printStackTrace();
    }

    checkLayerCount();
    return ret;
  }

  public boolean removeLayer(String layerName) {
    boolean ret = false;
    try {
      TimeLayer itLyr = null;
      for (int i = 0; i < layers.size(); i++) {
        itLyr = (TimeLayer) layers.get(i);
        if (itLyr.getName().equalsIgnoreCase(layerName)) {
          layers.remove(i);

          updateUniqueTimes();
          updateGlobalTimeRange();
          updateTimeSliderRange();

          enableControls();
          ret = true;
        }
      }
    } catch (Exception ex) {
      logger.warning("TLH:removeLayer - Problem Removing Layer:\n");
      ex.printStackTrace();

    }
    checkLayerCount();
    return ret;
  }

  public boolean removeAllLayers() {
    try {
      for (int i = 0; i < layers.size(); i++) {
        removeLayer(layers.get(i));
        return true;
      }
    } catch (Exception ex) {
      logger.warning("TLH:removeAllLayers - Problem Removing Layers:\n");
      ex.printStackTrace();
    }

    return false;
  }

  public void setUpdateInterval(double interval) {
    updateInterval = interval;
  }

  public double getUpdateInterval() {
    return updateInterval;
  }

  public void setPace(int pace) {
    timeIncrement = pace;
  }

  public int getPace() {
    return timeIncrement;
  }

  public void setTime(long t) {
    time = t;
    // System.err.println(t);
    // if(timeDate != null){// && timeLabel != null){
    // timeDate.setTime(time);
    // }

    // method to update the graphics based on time
    Iterator<TimeLayer> it = layers.iterator();
    TimeLayer tl = null;
    while (it.hasNext()) {
      tl = (TimeLayer) it.next();
      if (drawLayers.contains(tl.getName())) {
        // if(tl.timeIsValid(t)){
        tl.advanceTime(t);
        // }
      }
    }

    // if(drawLayer.equals(ALL_LAYERS)){//draw all of the layers
    //
    // while(it.hasNext()){
    // tl = (TimeLayer)it.next();
    //
    // if(tl.isVisible()){
    // tl.advanceTime(t);
    // }
    // }
    // }else{
    // while(it.hasNext()){//only draw the selected layer
    //
    // tl = (TimeLayer)it.next();
    // if(tl.getName().equals(drawLayer)){
    // if(tl.isVisible()){
    // tl.advanceTime(t);
    // break;
    // }
    // }
    // }
    // }
    // Iterator it = layers.iterator();
    // while(it.hasNext()){
    // Object o = it.next();
    // if(o instanceof TimeLayer){
    // TimeLayer tl = (TimeLayer)o;
    // //don't redraw if the layer is not visible - can't have this because
    // a layer is turned off
    // //when a time is invalid....accounted for in tl.advanceTime(t)
    // anyhow....
    // if(tl.isVisible()){
    // tl.advanceTime(t);
    // }
    // }
    // VectorLayer vl = (VectorLayer) it.next();
    // if(vl instanceof SwafsVectorLayer){
    // SwafsVectorLayer tl = (SwafsVectorLayer) vl;
    // tl.drawDataForTime(time);
    // }else if(vl instanceof NcomVectorLayer){
    // NcomVectorLayer tl = (NcomVectorLayer) vl;
    // tl.drawDataForTime(time);
    // }
    // }
  }

  public long getTime() {
    return time;
  }

  public void startClock() {
    if (restart) {
      restart = false;
      timer.restart();
    }

    timer.setDelay((int) (updateInterval * 1000));
    timer.start();
  }

  public void stopClock() {
    timer.stop();
  }

  public void setClockDirection(int direction) {
    if (direction >= 0) {
      clockDirection = 1;
    } else {
      clockDirection = -1;
    }
  }

  public int getClockDirection() {
    return clockDirection;
  }

  public void stepForward() {
    if (timer.isRunning()) {
      stopClock();
    }
    /**
     * If the current value is equal to the maximum, wrap back to the
     * beginning.
     */
    if (timeSlider.getValue() == timeSlider.getMaximum()) {
      timeSlider.setValue(timeSlider.getMinimum());
    } else {
      timeSlider.setValue(timeSlider.getValue() + 1);
    }
  }

  public void stepBackward() {
    if (timer.isRunning()) {
      stopClock();
    }
    /** If the current value is equal to the minimum, wrap back to the end. */
    if (timeSlider.getValue() == timeSlider.getMinimum()) {
      timeSlider.setValue(timeSlider.getMaximum());
    } else {
      timeSlider.setValue(timeSlider.getValue() - 1);
    }
  }

  public void actionPerformed(ActionEvent ae) {
    String cmd = ae.getActionCommand();
    // System.err.println(cmd);
    if (cmd.equals(TIMER_FORWARD)) {
      setClockDirection(1);
      startClock();
    } else if (cmd.equals(TIMER_BACKWARD)) {
      setClockDirection(-1);
      startClock();
    } else if (cmd.equals(TIMER_STEP_BACKWARD)) {
      stepBackward();
    } else if (cmd.equals(TIMER_STEP_FORWARD)) {
      stepForward();
    } else if (cmd.equals(TIMER_STOP)) {
      stopClock();
    } else if (cmd.equals(TIMER_RESET)) {
      restart = true;
      timeSlider.setValue(timeSlider.getMinimum());
    } else if (cmd.equals(TIMER_TO_END)) {
      // restart = true;
      timeSlider.setValue(timeSlider.getMaximum());
    } else if (cmd.equals(SELECT_LAYERS)) {
      if (dldLayers == null) {
        dldLayers = new DrawLayerDialog();
      }
      dldLayers.setDrawLayers();
      dldLayers.setVisible(true);
      if (dldLayers.acceptChanges()) {
        drawLayers = dldLayers.getDrawLayers();
        for (TimeLayer l : layers) {
          if (drawLayers.contains(l.getName())) {
            l.setLayerManuallyOn(true);
            firePropertyChange("layeronoff", l.getName(), "on");
          } else {
            l.setLayerManuallyOn(false);
            firePropertyChange("layeronoff", l.getName(), "off");
          }
        }
        updateUniqueTimes();
        updateGlobalTimeRange();
        updateTimeSliderRange();

        redrawTimestep();
      }
    } else if (cmd.equals(UNIQUE_TIMES_HELP)) {
      JOptionPane.showMessageDialog(
              this,
              "When the \"Use Unique Times\" checkbox is selected (default), the tick marks on the\n"
              + "Timeslider are based on the the unique times of all layers loaded in the Timeslider.\n\n"
              + "When deselected, the minimum time interval of all layers loaded in the Timeslider is used to draw the tick marks.\n\n"
              + "When datasets have very different time intervals or time ranges, using the unique times option\n"
              + "prevents having \"extra\" tick marks and is recommended.",
              "\"Use Unique Times\" Help Dialog", JOptionPane.PLAIN_MESSAGE);
    }
  }

  public long getIncrementBy() {
    return incrementBy;
  }

  public void setIncrementBy(long incrementBy) {
    this.incrementBy = incrementBy;
  }

  public void stateChanged(ChangeEvent e) {
    // if there are no layers - ignore changes
    if (layers.size() == 0) {
      return;
    }
    Object o = e.getSource();
    if (o instanceof JSlider) {
      JSlider js = (JSlider) o;

      if (js.getValue() > -1) {
        if (useUniqueTimes) {
          // uses unique times to determine the time to draw
          long uTime = uniqueTimes[js.getValue()];
          this.gblCurrTime = uTime;
          lblCurrTime.setText(this.getTimeString(uTime));
        } else {
          // calls the setTime method passing the time based on the
          // index number of the slider
          // update the lblCurrTime regardless of if the slider has
          // been released
          long add = js.getValue() * this.incrementBy;
          this.gblCurrTime = this.gblStartTime + add;
          lblCurrTime.setText(this.getTimeString(this.gblCurrTime));
        }
        // only setTime (i.e. redraw layer) if the value has been set
        // (i.e. slider released)
        if (!js.getValueIsAdjusting()) {
          setTime(this.gblCurrTime);
        }
      }
    }
  }

  public List<TimeLayer> getLayers() {
    return layers;
  }

  public long getGblCurrTime() {
    return gblCurrTime;
  }

  public void setGblCurrTime(long gblCurrTime) {
    this.gblCurrTime = gblCurrTime;
  }

  // public void itemStateChanged(ItemEvent e) {
  // if(e.getStateChange() == ItemEvent.SELECTED){
  // String cmd = e.getItem().toString();
  // if(cmd.equals(ALL_LAYERS)){//include all available layers
  //
  // drawLayer = ALL_LAYERS;
  // }else{//include only the particular layer
  //
  // drawLayer = cmd;
  // }
  //
  // updateGlobalTimeRange();
  // updateTimeSliderRange();
  //
  // redrawTimestep();
  // }
  // }
  class DrawLayerDialog extends OptionDialogBase {

    private CheckBoxList cblLayers;

    public DrawLayerDialog() {
      super("Active Time Layers");

      initComponents();
    }

    private void initComponents() {
      JPanel pnlMain = new JPanel(new MigLayout("inset 0, fill"));
      TitledBorder tb = BorderFactory.createTitledBorder("Layers:");
      tb.setTitlePosition(TitledBorder.LEFT);
      pnlMain.setBorder(tb);

      cblLayers = new CheckBoxList();

      pnlMain.add(cblLayers);

      this.setMinimumSize(new java.awt.Dimension(250, 200));
      this.add(pnlMain, "wrap, growx");
      this.add(super.buttonPanel("Accept"), "center");
      this.pack();
    }

    public void setDrawLayers() {
      cblLayers.clearCBList();
      for (TimeLayer l : layers) {
        cblLayers.addCheckBox(l.getName());
        if (drawLayers.contains(l.getName())) {
          cblLayers.selectSingleItem(l.getName());
        }
      }
      this.pack();
    }

    public void setDrawLayers(List<String> layers) {
      cblLayers.clearCBList();
      for (String s : layers) {
        cblLayers.addCheckBox(s);
      }
    }

    public List<String> getDrawLayers() {
      return cblLayers.getSelectedItems();
    }
  }
}
