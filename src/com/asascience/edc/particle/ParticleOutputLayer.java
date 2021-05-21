/*
 * ParticleOutputLayer.java
 *
 * Created on November 1, 2007, 12:36 PM
 *
 * Applied Science Associates, Inc.
 * Copyright 2007.  All rights reserved.
 *///TODO: This can't be done using hashtables to store everything - memory runs out
//will need to read the file for each timestep (boo) - makes a strong case for using
//and NC file to store output
package com.asascience.edc.particle;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.TimeZone;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.apache.commons.math.stat.descriptive.DescriptiveStatistics;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.LogarithmicAxis;
import org.jfree.chart.labels.StandardXYToolTipGenerator;
import org.jfree.chart.plot.Marker;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.ValueMarker;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.time.Hour;
import org.jfree.data.time.Minute;
import org.jfree.data.time.RegularTimePeriod;
import org.jfree.data.time.Second;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.xy.XYDataset;
import org.jfree.ui.RectangleAnchor;
import org.jfree.ui.TextAnchor;

import com.asascience.edc.gui.CustomChartFrame;
import com.asascience.openmap.mousemode.InformationMouseMode;
import com.asascience.openmap.omgraphic.OMParticle;
import com.asascience.utilities.Utils;
import com.asascience.utilities.exception.InitializationFailedException;
import com.bbn.openmap.event.MapMouseListener;
import com.bbn.openmap.omGraphics.OMGraphic;
import com.bbn.openmap.omGraphics.OMGraphicList;
import com.bbn.openmap.proj.Projection;

import net.miginfocom.swing.MigLayout;

/**
 * 
 * @author CBM
 */
public class ParticleOutputLayer extends OutputLayerBase implements MapMouseListener {

  private static final int ANALYSIS_TIMESERIES = 0;
  private static final int HISTOGRAM = 1;
  private OMParticle selParticle;
  private int selPartId;
  private ParticleOutputReader por = null;
  protected transient JPanel box;
  private final JFrame parentFrame;
  private JCheckBox cbColorParts;
  private JComboBox cbPartAtt;
  private CustomChartFrame cf;
  private XYDataset partTimeseriesDataset;
  private DefaultCategoryDataset partHistogramDataset;
  private DecimalFormat numFormat = new DecimalFormat("#0.0000");
  private SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
  private ParticleAnalyzer partAnalyzer = null;
  private HashMap<String, Double> analysisStats = null;
  private int selChartType = 0;

  /**
   * Creates a new instance of ParticleOutputLayer
   *
   * @param incident
   *            <CODE>Incident</CODE>: The Incident object for the scenario.
   * @param pf
   * @throws java.io.FileNotFoundException
   * @throws InitializationFailedException
   */
  public ParticleOutputLayer(File f, JFrame pf) throws FileNotFoundException, InitializationFailedException {
    super(false);
    // super(incident, false);
    this.mouseModeIDs = getMouseModeServiceList();
    this.parentFrame = pf;

    // this.setName(incident.getName() + ": Particle Output");
    if (f.getName().equals(ParticleOutputReader.OUTPUT_NAME)) {
      // this.setName("Deterministic Particles");
      this.setName("Particles");
    } else if (f.getName().contains("agg")) {
      this.setName("Stochastic Particles");
    } else {
      String name = f.getName().replace(".nc", "");
      name = "Particles - " + name.substring(name.lastIndexOf("_") + 1);
      this.setName(name);
    }

    if (f.exists()) {
      this.dataFile = f;
      this.setSourceFilePath(f.getAbsolutePath());

      por = new ParticleOutputReader(this.dataFile);

      this.setStartTime(por.getStartTime());
      this.setEndTime(por.getEndTime());
      this.setTimeIncrement(por.getTimeIncrement());
      this.setTimes(por.getTimes().toArray(new Long[0]));
      this.setLayerExtentRectangle(por.getFullExtent());

      omgraphics = new OMGraphicList();
      this.setList(omgraphics);
    } else {
      throw new FileNotFoundException("File \"" + dataFile + "\" not found");
    }
  }
  
  /**
   *
   * @return
   */
  @Override
  public OMGraphicList prepare() {
    OMGraphicList list = this.getList();
    OMParticle g;
    double ld50conc = por.getLd50();
    for (int i = 0; i < list.size(); i++) {
      g = (OMParticle) list.getOMGraphicAt(i);

      if (true) {
        /**
         * "Standard" coloring method - all black except those on the
         * bottom.
         */
        Color paint = Color.BLACK;
        if (g.getParticleLocationZ() == -9999999) {
          paint = Color.RED;
        }
        g.setLinePaint(paint);
        g.setFillPaint(paint);
        g.setSelectPaint(Color.YELLOW);
      } else {
        /** "New" coloring method - colored by center concentration. */
        Color paint = getColorForParticle(g.getCenterConc());

        g.setLinePaint(paint);
        g.setFillPaint(paint);
        g.setSelectPaint((paint == Color.YELLOW) ? Color.RED : Color.YELLOW);
      }

      /** Highlight the selected particle if necessary. */
      if (selPartId != -1 && g.getParticleID() == selPartId) {
        g.setSelected(true);
        // g.setFillPaint(Color.YELLOW);
        // g.setLinePaint(Color.YELLOW);
      }

      // g.setLinePaint(drawingAttributes.getLinePaint());
      // g.setFillPaint(drawingAttributes.getFillPaint());
      // g.setSelectPaint(drawingAttributes.getSelectPaint());
    }
    list.generate(this.getProjection());
    return list;
  }

  private double[] getDivs() {
    double[] ret = new double[2];
    ret[0] = por.getLd50();
    ret[1] = ret[0] * 0.01d;
    return ret;
  }
  private double[] divs = null;

  private Color getColorForParticle(double val) {
    if (divs == null) {
      divs = getDivs();
    }

    if (Double.isNaN(val)) {
      return new Color(0, 0, 0, 0);
    }

    if (val >= divs[0]) {
      return Color.RED;
    } else if (val >= divs[1]) {
      return new Color(204, 153, 0);
    } else {
      return new Color(0, 153, 153);
    }
  }

  /**
   * Draws the appropriate data for the passed timestep t <CODE>long</CODE>.
   *
   * @param t
   *            The time <CODE>long</CODE> for which the data should be drawn
   */
  @Override
  public void drawDataForTime(long t) {
    OMGraphicList omgl = this.getList();
    if (omgl == null) {
      return;
    }
    omgl.clear();

    Projection proj = this.getProjection();
    // projection will be null on initial call - !null after that
    if (proj != null) {
      List<OMParticle> particles = por.getOMParticlesAtTime(t, proj);
      if (particles != null) {
        currentTime = t;

        if (cf != null && cf.isVisible()) {
          // showChartFrame("Timeseries",
          // buildParticleTimeseriesChart());
          showChartFrame("Timeseries", buildAnalysisTimeseriesChart());
          // selPartId = selParticle.getParticleID();
        } else {
          selPartId = -1;
        }

        for (OMParticle p : particles) {
          omgl.add(p);
        }
      }

      this.doPrepare();
    }
  }

  /**
   *
   * @return
   */
  @Override
  public Component getGUI() {
    // return null;
    if (box == null) {
      box = new JPanel(new MigLayout("", "[left]", ""));

      cbColorParts = new JCheckBox("Color by Attribute");
      cbColorParts.setSelected(false);

      cbPartAtt = new JComboBox();

    }

    // return box;
    return null;
  }

  private XYDataset getDataseries(OMParticle p) {
    TimeSeries ts = new TimeSeries("Particle #" + p.getParticleID(), Second.class);
    List<double[]> data = por.getParticleTimeseries(p.getParticleID());

    if (data == null) {
      return null;
    }

    // determine what RegularTimePeriod to use
    RegularTimePeriod rtp;
    double t0 = data.get(0)[0];
    double t1 = data.get(1)[0];
    long delta = (long) t1 - (long) t0;
    int multiplier = 1;
    GregorianCalendar gc = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
    gc.setTimeInMillis((long) t0);
    if (delta < 60000) {// seconds
      // rtp = new Second(new Date((long)t0));
      rtp = RegularTimePeriod.createInstance(Second.class, gc.getTime(), TimeZone.getTimeZone("UTC"));
      ts = new TimeSeries("Particle # " + p.getParticleID(), Second.class);
      multiplier = ((int) (delta / 1000));
    } else if (delta < 3600000) {// minutes
      // rtp = new Minute(new Date((long)t0));
      rtp = RegularTimePeriod.createInstance(Minute.class, gc.getTime(), TimeZone.getTimeZone("UTC"));
      ts = new TimeSeries("Particle # " + p.getParticleID(), Minute.class);
      multiplier = ((int) (delta / 60000));
    } else {// hours
      // rtp = new Hour(new Date((long)t0));
      rtp = RegularTimePeriod.createInstance(Hour.class, gc.getTime(), TimeZone.getTimeZone("UTC"));
      ts = new TimeSeries("Particle # " + p.getParticleID(), Hour.class);
      multiplier = ((int) (delta / 3600000));
    }

    boolean allNaN = true;// don't return a graph if there is no data for
    // the cell....

    int i = 0;
    for (double[] d : data) {
      if (!Double.isNaN(d[1])) {
        allNaN = false;
      }
      ts.add(rtp, d[1]);
      // rtp = rtp.next();
      while (i < multiplier - 1) {
        rtp = rtp.next();
        // ts.add(rtp, null);
        i++;
      }
      rtp = rtp.next();
      i = 0;
    }
    if (allNaN) {
      return null;
    }

    TimeSeriesCollection dataset = new TimeSeriesCollection();
    dataset.addSeries(ts);

    return dataset;
  }

  /**
   *
   * @return
   */
  @Override
  public MapMouseListener getMapMouseListener() {
    return this;
  }

  public String[] getMouseModeServiceList() {
    String[] ret = new String[1];
    ret[0] = InformationMouseMode.modeID;
    return ret;
  }

  public void initializeParticleAnalyzer() {
    /** Itemize layers in the Data Viewer. */
  }

  public void setAnalysisDataset(String analysisFile) throws Exception {
    partAnalyzer = new ParticleAnalyzer(null, this.getSourceFilePath(), analysisFile);
    Object[] gridNames = partAnalyzer.getAvaliableGridNames().toArray();
    Object ret = null;
    if (gridNames.length > 1) {
      ret = JOptionPane.showInputDialog(parentFrame, "Choose Analysis Grid", "Choose",
              JOptionPane.QUESTION_MESSAGE, null, gridNames, gridNames[0]);
    }
    if (ret == null) {
      ret = gridNames[0];
    }
    partAnalyzer.setAnalysisGridByName(ret.toString());
  }

  private void buildStats(double[] dataSeries) {
    analysisStats = new HashMap<String, Double>();
    DescriptiveStatistics descStats = new DescriptiveStatistics();
    for (double d : dataSeries) {
      if (!Double.isNaN(d)) {
        descStats.addValue(d);
      }
    }
    analysisStats.put("Maximum", descStats.getMax());
    analysisStats.put("Minimum", descStats.getMin());
    analysisStats.put("Mean", descStats.getMean());
  }

  private DefaultCategoryDataset getHistogramDataset() {

    /** Get the dataSeries for the selected particle. */
    double[] dataSeries = partAnalyzer.processParticle(selPartId);
    if (dataSeries == null) {
      return null;
    }

    /**
     * Build the histogram bins (hardcoded to 0.5 degree intervals for now).
     */
    // TODO give user ability to change bin size
    double binSize = 0.5d;
    double[] minMax = Utils.minMaxDouble(dataSeries);
    double min = Utils.roundToNearest(minMax[0], binSize, -1);
    double max = Utils.roundToNearest(minMax[1], binSize, 0);
    int bins = (int) ((max - min) / binSize);

    HashMap<String, Integer> binVals = new HashMap<String, Integer>();
    /** Count the number of values in each bin. */
    for (int i = 0; i < bins; i++) {
      int count = 0;
      max = min + binSize;
      for (double d : dataSeries) {
        if (d > min & d <= max) {
          count++;
        }
      }
      binVals.put(min + "-" + max, count);
      min = max;
    }

    long inc = this.getTimeIncrement();
    String incU;
    inc = inc / 1000;// to seconds
    if (inc < 60) {
      // seconds
      incU = "Seconds";
    } else if (inc < 3600) {
      // minutes
      inc = inc / 60;// inc in minutes
      incU = "Minutes";
    } else {
      inc = inc / 3600;
      incU = "Hours";
    }

    DefaultCategoryDataset ds = new DefaultCategoryDataset();
    String[] keys = binVals.keySet().toArray(new String[0]);
    Arrays.sort(keys);
    int cnt = 0;
    for (String s : keys) {
      cnt = binVals.get(s);
      if (cnt > 0) {
        ds.setValue(binVals.get(s) * inc, incU, s);
      }
    }

    buildStats(dataSeries);

    return ds;
  }

  private XYDataset getAnalysisDataset() {
    try {
      TimeSeries ts = new TimeSeries("Particle #" + selPartId, Second.class);
      double[] dataSeries = partAnalyzer.processParticle(selPartId);
      if (dataSeries == null) {
        return null;
      }

      // determine what RegularTimePeriod to use
      RegularTimePeriod rtp;
      double t0 = por.getStartTime();
      // double t1 = data.get(1)[0];
      long delta = por.getTimeIncrement();
      int multiplier = 1;
      GregorianCalendar gc = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
      gc.setTimeInMillis((long) t0);
      if (delta < 60000) {// seconds
        // rtp = new Second(new Date((long)t0));
        rtp = RegularTimePeriod.createInstance(Second.class, gc.getTime(), TimeZone.getTimeZone("UTC"));
        ts = new TimeSeries("Particle # " + selPartId, Second.class);
        multiplier = ((int) (delta / 1000));
      } else if (delta < 3600000) {// minutes
        // rtp = new Minute(new Date((long)t0));
        rtp = RegularTimePeriod.createInstance(Minute.class, gc.getTime(), TimeZone.getTimeZone("UTC"));
        ts = new TimeSeries("Particle # " + selPartId, Minute.class);
        multiplier = ((int) (delta / 60000));
      } else {// hours
        // rtp = new Hour(new Date((long)t0));
        rtp = RegularTimePeriod.createInstance(Hour.class, gc.getTime(), TimeZone.getTimeZone("UTC"));
        ts = new TimeSeries("Particle # " + selPartId, Hour.class);
        multiplier = ((int) (delta / 3600000));
      }

      boolean allNaN = true;// don't return a graph if there is no data
      // for the cell....

      int i = 0;
      for (double d : dataSeries) {
        if (!Double.isNaN(d)) {
          allNaN = false;
        }
        ts.add(rtp, d);
        // rtp = rtp.next();
        while (i < multiplier - 1) {
          rtp = rtp.next();
          // ts.add(rtp, null);
          i++;
        }
        rtp = rtp.next();
        i = 0;
      }
      if (allNaN) {
        return null;
      }

      TimeSeriesCollection dataset = new TimeSeriesCollection();
      dataset.addSeries(ts);

      buildStats(dataSeries);

      return dataset;
    } catch (Exception e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    return null;
  }

  private JFreeChart buildChart() {
    JFreeChart ret = null;
    switch (selChartType) {
      case ANALYSIS_TIMESERIES:
        ret = buildAnalysisTimeseriesChart();
        break;
      case HISTOGRAM:
        ret = buildHistogramChart();
        break;
      default:
        break;
    }
    return ret;
  }

  private JFreeChart buildHistogramChart() {
    partHistogramDataset = getHistogramDataset();
    if (partHistogramDataset == null) {
      return null;
    }

    JFreeChart chart = ChartFactory.createBarChart("Histogram", "Bin", partHistogramDataset.getRowKeys().get(0).toString(), partHistogramDataset, PlotOrientation.VERTICAL, false, true, false);

    return chart;
  }

  private JFreeChart buildAnalysisTimeseriesChart() {
    partTimeseriesDataset = getAnalysisDataset();
    if (partTimeseriesDataset == null) {
      return null;
    }
    String units = partAnalyzer.getAnalysisGridUnits();
    String description = partAnalyzer.getAnalysisGridDescription();
    description = (description.length() > 20) ? description.substring(0, 20) + "..." : description;
    JFreeChart chart = ChartFactory.createTimeSeriesChart("Particle Analysis Timeseries", "Date", description
            + " (" + units + ")", partTimeseriesDataset, true, true, false);
    // chart.getXYPlot().setRangeAxis(new
    // LogarithmicAxis("Particle Concentration (log)"));

    TimeZone tz = TimeZone.getDefault();// get the "local" timezone
    int offset = tz.getOffset(currentTime);
    final Marker currTime = new ValueMarker(currentTime - offset);
    currTime.setPaint(Color.red);
    currTime.setStroke(new BasicStroke(1.5f));
    currTime.setLabel("Current Time");
    currTime.setLabelAnchor(RectangleAnchor.TOP_RIGHT);
    currTime.setLabelTextAnchor(TextAnchor.TOP_LEFT);
    chart.getXYPlot().addDomainMarker(currTime);

    final XYItemRenderer rend = chart.getXYPlot().getRenderer();
    rend.setBaseToolTipGenerator(new StandardXYToolTipGenerator("{0}: ({1}, {2})", dateFormat, numFormat));
    if (rend instanceof XYLineAndShapeRenderer) {
      final XYLineAndShapeRenderer rr = (XYLineAndShapeRenderer) rend;
      // rr.setSeriesShape(0, new Ellipse2D.Float(0.0f, 0.0f, 5.0f,
      // 5.0f));
      rr.setSeriesPaint(0, Color.black);
      rr.setBaseShapesFilled(true);
      rr.setBaseShapesVisible(true);
    }
    // if(cf == null){
    // cf = new StatsChartFrame("Timeseries", chart);
    // cf.setAlwaysOnTop(true);
    // cf.setResizable(false);
    // }else{
    // cf.getChartPanel().setChart(chart);
    // }
    // // cf.getChartPanel().setHorizontalAxisTrace(true);
    // // cf.getChartPanel().setVerticalAxisTrace(true);
    // cf.getChartPanel().setMouseZoomable(true);
    //
    // cf.pack();
    // if(!cf.isVisible()){
    // cf.setLocationRelativeTo(parentFrame);
    // cf.setVisible(true);
    // }else{
    // // Utils.bringFrameToFront(cf);
    // }
    return chart;
  }

  private JFreeChart buildParticleTimeseriesChart() {
    if (partTimeseriesDataset == null) {
      return null;
    }
    JFreeChart chart = ChartFactory.createTimeSeriesChart("Particle Timeseries", "Date", "Particle Concentration",
            partTimeseriesDataset, true, true, false);
    chart.getXYPlot().setRangeAxis(new LogarithmicAxis("Particle Concentration (log)"));

    TimeZone tz = TimeZone.getDefault();// get the "local" timezone
    int offset = tz.getOffset(currentTime);
    final Marker currTime = new ValueMarker(currentTime - offset);
    currTime.setPaint(Color.red);
    currTime.setStroke(new BasicStroke(1.5f));
    currTime.setLabel("Current Time");
    currTime.setLabelAnchor(RectangleAnchor.TOP_RIGHT);
    currTime.setLabelTextAnchor(TextAnchor.TOP_LEFT);
    chart.getXYPlot().addDomainMarker(currTime);

    final XYItemRenderer rend = chart.getXYPlot().getRenderer();
    rend.setBaseToolTipGenerator(new StandardXYToolTipGenerator("{0}: ({1}, {2})", dateFormat, numFormat));
    if (rend instanceof XYLineAndShapeRenderer) {
      final XYLineAndShapeRenderer rr = (XYLineAndShapeRenderer) rend;
      // rr.setSeriesShape(0, new Ellipse2D.Float(0.0f, 0.0f, 5.0f,
      // 5.0f));
      rr.setSeriesPaint(0, Color.black);
      rr.setBaseShapesFilled(true);
      rr.setBaseShapesVisible(true);
    }
    // if(cf == null){
    // cf = new StatsChartFrame("Timeseries", chart);
    // cf.setAlwaysOnTop(true);
    // cf.setResizable(false);
    // }else{
    // cf.getChartPanel().setChart(chart);
    // }
    // // cf.getChartPanel().setHorizontalAxisTrace(true);
    // // cf.getChartPanel().setVerticalAxisTrace(true);
    // cf.getChartPanel().setMouseZoomable(true);
    //
    // cf.pack();
    // if(!cf.isVisible()){
    // cf.setLocationRelativeTo(parentFrame);
    // cf.setVisible(true);
    // }else{
    // // Utils.bringFrameToFront(cf);
    // }
    return chart;
  }

  private void showChartFrame(String title, JFreeChart chart) {
    if (cf == null) {
      cf = new CustomChartFrame(title, chart, analysisStats, partAnalyzer.getAvaliableGridNames());
      cf.addPropertyChangeListener(new ChartPropListener());
      cf.setAlwaysOnTop(true);
      cf.setResizable(false);
      cf.setDefaultCloseOperation(CustomChartFrame.DISPOSE_ON_CLOSE);
      cf.addWindowListener(new WindowAdapter() {

        @Override
        public void windowClosing(WindowEvent e) {
          partTimeseriesDataset = null;
          analysisStats = null;
          cf = null;
          drawDataForTime(currentTime);
        }
      });
      if (partAnalyzer != null) {
        cf.setSelectedGrid(partAnalyzer.getAnalysisGridName());
      }
      // cf.pack();
    } else {
      cf.getChartPanel().setChart(chart);
      cf.getStatsPanel().setStats(analysisStats);
      cf.setTitle(title);
    }
    // cf.getChartPanel().setHorizontalAxisTrace(true);
    // cf.getChartPanel().setVerticalAxisTrace(true);
    cf.getChartPanel().setMouseZoomable(true);

    cf.pack();
    if (!cf.isVisible()) {
      cf.setLocationRelativeTo(parentFrame);
      cf.setVisible(true);
    } else {
      // Utils.bringFrameToFront(cf);
    }
  }

  /**
   *
   * @param e
   * @return
   */
  public boolean mouseClicked(MouseEvent e) {

    if (selParticle != null) {
      try {
        // setAnalysisDataset("/Users/cmueller_mac/User_Data/EDC/edcdata/SST.nc");
        if (partAnalyzer == null) {
          JOptionPane.showMessageDialog(parentFrame, "Please select a dataset to use for analysis.");
          return false;
        }
      } catch (Exception e1) {
        // TODO Auto-generated catch block
        e1.printStackTrace();
      }
      selPartId = selParticle.getParticleID();
      showChartFrame("Particle Analysis", buildChart());
      // showChartFrame("Particle Analysis",
      // buildAnalysisTimeseriesChart());

      /* Original lines for making a timeseries of concentration. * */
      // partTimeseriesDataset = getDataseries(selParticle);
      // showChartFrame("Timeseries", buildParticleTimeseriesChart());

      // if(partTimeseriesDataset == null){
      // return false;
      // }
      // JFreeChart chart =
      // ChartFactory.createTimeSeriesChart("Particle Timeseries", "Date",
      // "Particle Concentration", partTimeseriesDataset, true, true,
      // false);
      // chart.getXYPlot().setRangeAxis(new
      // LogarithmicAxis("Particle Concentration (log)"));
      //
      // // final Marker currTime = new ValueMarker(currentTime);
      // // currTime.setPaint(Color.red);
      // // currTime.setLabel("Current Time");
      // // currTime.setLabelAnchor(RectangleAnchor.TOP_RIGHT);
      // // currTime.setLabelTextAnchor(TextAnchor.TOP_LEFT);
      // // chart.getXYPlot().addDomainMarker(currTime);
      //
      // final XYItemRenderer rend = chart.getXYPlot().getRenderer();
      // rend.setBaseToolTipGenerator(new
      // StandardXYToolTipGenerator("{0}: ({1}, {2})",
      // dateFormat, numFormat));
      // if(rend instanceof XYLineAndShapeRenderer){
      // final XYLineAndShapeRenderer rr = (XYLineAndShapeRenderer)rend;
      // // rr.setSeriesShape(0, new Ellipse2D.Float(0.0f, 0.0f, 5.0f,
      // 5.0f));
      // rr.setSeriesPaint(0, Color.black);
      // rr.setBaseShapesFilled(true);
      // rr.setBaseShapesVisible(true);
      // }
      // if(cf == null){
      // cf = new StatsChartFrame("Timeseries", chart);
      // cf.setAlwaysOnTop(true);
      // cf.setResizable(false);
      // }else{
      // cf.getChartPanel().setChart(chart);
      // }
      // // cf.getChartPanel().setHorizontalAxisTrace(true);
      // // cf.getChartPanel().setVerticalAxisTrace(true);
      // cf.getChartPanel().setMouseZoomable(true);
      //
      // cf.pack();
      // if(!cf.isVisible()){
      // cf.setLocationRelativeTo(parentFrame);
      // cf.setVisible(true);
      // }else{
      // // Utils.bringFrameToFront(cf);
      // }
    } else {
      selPartId = -1;
      partTimeseriesDataset = null;
      analysisStats = null;
    }
    // DefaultPieDataset data = new DefaultPieDataset();
    // data.setValue("Category 1", 43.2);
    // data.setValue("Category 2", 27.9);
    // data.setValue("Category 3", 79.5);
    // JFreeChart chart = ChartFactory.createPieChart("Sample", data, true,
    // true, false);
    // StatsChartFrame cf = new StatsChartFrame("First", chart);
    // cf.pack();
    // cf.setVisible(true);
    return false;
  }

  /**
   *
   * @param e
   * @return
   */
  public boolean mouseMoved(MouseEvent e) {
    this.getList().deselectAll();
    repaint();

    OMGraphic newSelGraphic = this.getList().selectClosest(e.getX(), e.getY(), 5.0f);
    if (newSelGraphic == null) {
      selectedGraphic = null;
      selParticle = null;
      this.fireRequestInfoLine("");
      repaint();
    } else {// if(newSelPart != selParticle){
      selectedGraphic = newSelGraphic;
      if (newSelGraphic instanceof OMParticle) {
        selParticle = (OMParticle) newSelGraphic;
        this.fireRequestInfoLine("Radius: " + Utils.roundDouble(selParticle.getRealRadius(), 5)
                + " Concentration: " + Utils.roundDouble(selParticle.getCenterConc(), 5));
        repaint();
      }
    }
    return true;
  }

  public boolean mousePressed(MouseEvent e) {
    return false;
  }

  public boolean mouseReleased(MouseEvent e) {
    return false;
  }

  public void mouseEntered(MouseEvent e) {
  }

  public void mouseExited(MouseEvent e) {
  }

  public boolean mouseDragged(MouseEvent e) {
    return false;
  }

  public void mouseMoved() {
    this.getList().deselectAll();
    repaint();
  }

  /**
   * Called when the Layer is removed from the MapBean, giving an opportunity
   * to clean up.
   *
   * @param cont
   */
  @Override
  public void removed(Container cont) {
    if (cf != null) {
      cf.setVisible(false);
      cf.dispose();
    }
    super.removed(cont);
  }

  class ChartPropListener implements PropertyChangeListener {

    public void propertyChange(PropertyChangeEvent evt) {
      String name = evt.getPropertyName();
      if (name.equals("gridChange")) {
        partAnalyzer.setAnalysisGridByName(evt.getOldValue().toString());
        showChartFrame("Particle Analysis", buildChart());
        // showChartFrame("Particle Analysis",
        // buildAnalysisTimeseriesChart());
      } else if (name.equals("chartType")) {
        selChartType = Integer.valueOf(evt.getOldValue().toString());
        showChartFrame("Particle Analysis", buildChart());
      }
    }
  }
}
