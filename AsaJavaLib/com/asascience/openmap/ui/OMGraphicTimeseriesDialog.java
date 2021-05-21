/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * OMGraphicTimeseriesDialog.java
 *
 * Created on Jul 10, 2008, 1:25:56 PM
 *
 */
package com.asascience.openmap.ui;

import java.awt.Dimension;
import java.util.GregorianCalendar;
import java.util.Hashtable;
import java.util.TimeZone;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.time.Hour;
import org.jfree.data.time.Minute;
import org.jfree.data.time.RegularTimePeriod;
import org.jfree.data.time.Second;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;

import com.asascience.ui.OptionDialogBase;

import net.miginfocom.swing.MigLayout;

/**
 * 
 * @author cmueller_mac
 */
public class OMGraphicTimeseriesDialog extends OptionDialogBase {

  public static final String U_COMP = "U Component";
  public static final String V_COMP = "V Component";
  public static final String W_LEVEL = "Water Level";
  private JFrame parent;
  private GregorianCalendar startCal;
  private long increment;
  private double[] uData, vData;
  private Hashtable<String, double[]> chartData;
  // private ChartPanel cpU, cpV;
  private Hashtable<String, ChartPanel> chartPanels;
  private JPanel pnlMain;
  Dimension cDim = new Dimension(500, 200);

  /**
   * Creates a new instance of OMGraphicTimeseriesDialog
   *
   * @param parent
   * @param cal
   * @param increment
   * @param chartData
   */
  public OMGraphicTimeseriesDialog(JFrame parent, GregorianCalendar cal, long increment, Hashtable chartData) {
    this.setTitle("Timeseries Dialog");
    this.setDefaultCloseOperation(OptionDialogBase.DISPOSE_ON_CLOSE);
    this.setModal(false);
    this.setAlwaysOnTop(true);
    this.parent = parent;
    this.startCal = cal;
    this.increment = increment;
    // this.uData = uvData[0];
    // this.vData = uvData[1];
    this.chartData = chartData;
    initComponents();
  }

  public void redisplay(GregorianCalendar startCal, long increment, Hashtable data) {
    this.startCal = startCal;
    this.increment = increment;
    this.chartData = data;
    ChartPanel cp;
    for (String key : chartData.keySet()) {
      if (chartPanels.containsKey(key)) {// if the panel already exists
        cp = chartPanels.get(key);
        cp.setChart(makeChart(key, chartData.get(key)));
      } else {// if the panel doesn't exist
        cp = new ChartPanel(makeChart(key, chartData.get(key)));
        chartPanels.put(key, cp);
        pnlMain.add(cp, "wrap");
      }
    }
    // cpU.setChart(makeUChart());
    // cpV.setChart(makeVChart());
  }

  private void initComponents() {
    pnlMain = new JPanel(new MigLayout("inset 0, fill"));
    TitledBorder tb = BorderFactory.createTitledBorder("Timeseries:");
    tb.setTitlePosition(TitledBorder.LEFT);
    pnlMain.setBorder(tb);

    // cpU = new ChartPanel(makeUChart());
    // cpU.setPreferredSize(cDim);
    // cpV = new ChartPanel(makeVChart());
    // cpV.setPreferredSize(cDim);

    if (!chartData.isEmpty()) {
      chartPanels = new Hashtable<String, ChartPanel>();
      // Enumeration e = chartData.keys();
      // String key;
      // while(e.hasMoreElements()){
      // key = (String)e.nextElement();
      // chartPanels.put(key, new ChartPanel(makeChart(key,
      // (double[])chartData.get(key))));
      // }

      /** Make sure U and V are first and second... */
      chartPanels.put(U_COMP, new ChartPanel(makeChart(U_COMP, chartData.get(U_COMP))));
      chartPanels.put(V_COMP, new ChartPanel(makeChart(V_COMP, chartData.get(V_COMP))));
      for (String key : chartData.keySet()) {
        if (!key.equals(U_COMP) & !key.equals(V_COMP)) {
          chartPanels.put(key, new ChartPanel(makeChart(key, chartData.get(key))));
        }
      }
    }

    // pnlMain.add(cpU, "wrap");
    // pnlMain.add(cpV, "wrap");
    if (chartPanels != null) {
      for (ChartPanel c : chartPanels.values()) {
        c.setPreferredSize(cDim);
        pnlMain.add(c, "wrap");
      }
    }

    this.add(pnlMain, "wrap, growx");
    this.add(super.buttonPanel("Done"), "center");
    this.pack();
  }

  private JFreeChart makeUChart() {
    return ChartFactory.createTimeSeriesChart("U Component", "Date", "Velocity (m/s)", makeChartSeries("U", vData),
            true, true, false);
  }

  private JFreeChart makeVChart() {
    return ChartFactory.createTimeSeriesChart("V Component", "Date", "Velocity (m/s)", makeChartSeries("V", uData),
            true, true, false);
  }

  private JFreeChart makeChart(String id, double[] data) {
    return ChartFactory.createTimeSeriesChart(id, "Date",
            (id.toLowerCase().contains("component")) ? "Velocity (m/s)" : id, makeChartSeries(id, data), true, true,
            false);
  }

  private TimeSeriesCollection makeChartSeries(String id, double[] data) {
    TimeSeries ts;
    RegularTimePeriod rtp;
    TimeSeriesCollection dataset = new TimeSeriesCollection();

    int multiplier = 1;
    if (increment < 60000) {// seconds
      rtp = RegularTimePeriod.createInstance(Second.class, startCal.getTime(), TimeZone.getTimeZone("UTC"));
      ts = new TimeSeries(id + " Data", Second.class);
      multiplier = ((int) (increment / 1000));
    } else if (increment < 3600000) {// minutes
      rtp = RegularTimePeriod.createInstance(Minute.class, startCal.getTime(), TimeZone.getTimeZone("UTC"));
      ts = new TimeSeries(id + " Data", Minute.class);
      multiplier = ((int) (increment / 60000));
    } else {// hours
      rtp = RegularTimePeriod.createInstance(Hour.class, startCal.getTime(), TimeZone.getTimeZone("UTC"));
      ts = new TimeSeries(id + " Data", Hour.class);
      multiplier = ((int) (increment / 3600000));
    }

    // double[] data;
    // if(id.equals("U")){
    // data = uData;
    // }else{
    // data = vData;
    // }
    int i = 0;
    for (double d : data) {
      ts.add(rtp, d);
      // rtp = rtp.next();
      while (i < multiplier - 1) {
        rtp = rtp.next();
        i++;
      }
      rtp = rtp.next();
      i = 0;
    }

    dataset.addSeries(ts);

    return dataset;
  }
}
