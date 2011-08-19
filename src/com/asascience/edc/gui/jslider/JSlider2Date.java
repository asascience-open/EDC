/*
 * $Id: JSlider2Date.java,v 1.8 2000/11/12 19:22:09 dwd Exp $
 */
package com.asascience.edc.gui.jslider;

import com.toedter.calendar.JDateChooser;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.Serializable;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerDateModel;
import javax.swing.SpinnerModel;
import net.miginfocom.swing.MigLayout;

/**
 * Class provides graphical and textual input of a time range.
 * Time calculations are performed using GeoDate. Minimum value
 * are required to be less than or equal to the maximum value.
 * Support has been added for java.util.Date, for both setting
 * the range and getting and setting the slider values.
 *
 * @author Donald Denbo
 * @version $Revision: 1.8 $, $Date: 2000/11/12 19:22:09 $
 * @see JSlider2
 **/
public class JSlider2Date extends JComponent implements Serializable {

  private boolean twoHandles_;
  private DateFormat format_, timeFormat_;
  private JLabel labelStart_, labelStop_;
  private JDateChooser s_cal, e_cal;
  private JSpinner s_time, e_time;
  private JSlider2 slider_;
  private double scale_;
  private PropertyChangeSupport pcs = new PropertyChangeSupport(this);
  private String dateFormatString = "yyyy-MM-dd";
  private String timeFormatString = "HH:mm:ss";

  /**
   * Default constructor. The default creates two handles.
   **/
  public JSlider2Date() {
    this(true);
  }

  /**
   * Constructs a single or double time slider.
   *
   * @param twoHandles if true create two handles
   */
  public JSlider2Date(boolean twoHandles) {
    super();
    twoHandles_ = twoHandles;
    format_ = new SimpleDateFormat(dateFormatString);
    timeFormat_ = new SimpleDateFormat(timeFormatString);

    setLayout(new MigLayout("insets 0, gap 0, fillx, align center"));
    
    /* 
     * Slider
     */
    slider_ = new JSlider2(twoHandles_);
    slider_.setAlwaysPost(true);
    slider_.setDoubleBuffered(true);
    slider_.addPropertyChangeListener(new PropertyChangeListener() {
      public void propertyChange(PropertyChangeEvent evt) {
        double m;
        Date d;
        if(evt.getPropertyName().equals("minValue")) {
          m = ((Double)evt.getNewValue()).doubleValue();
          d = (Date)s_cal.getMinSelectableDate().clone();
          d.setTime(d.getTime() + ((long)((m*scale_) * 86400000)));
          setStartDate(d);
        } else if(evt.getPropertyName().equals("maxValue")) {
          m = ((Double)evt.getNewValue()).doubleValue();
          d = (Date)e_cal.getMinSelectableDate().clone();
          d.setTime(d.getTime() + ((long)((m*scale_) * 86400000)));
          setEndDate(d);
        }
      }
    });
    add(slider_, "gap 0, growx, wrap, spanx 2");
    
    SpinnerModel model;
    JComponent editor;
    
    /*
     * Start
     */
    JPanel s_panel = new JPanel(new MigLayout("align left, gapx 2"));
    labelStart_ = new JLabel("Start:");
    s_panel.add(labelStart_);
    s_cal = new JDateChooser();
    s_cal.setDateFormatString(dateFormatString);
    s_cal.addPropertyChangeListener(new PropertyChangeListener() {

      public void propertyChange(PropertyChangeEvent evt) {
        Date d = (Date)s_cal.getMinSelectableDate().clone();
        d.setTime(d.getTime() + ((long)((slider_.getMinValue()*scale_) * 86400000)));
        if (s_cal.getDate() != null && !s_cal.getDate().equals(d)) {
          setStartDate(s_cal.getDate());
        }
      }
    });
    s_panel.add(s_cal, "wmin 92");
    model = new SpinnerDateModel();
    s_time = new JSpinner(model);
    editor = new JSpinner.DateEditor(s_time, timeFormatString);
    s_time.setEditor(editor);
    s_panel.add(s_time);
    
    /*
     * Stop
     */
    JPanel e_panel = new JPanel(new MigLayout("align right, gapx 2"));
    labelStop_ = new JLabel("Stop:");
    e_panel.add(labelStop_);
    e_cal = new JDateChooser();
    e_cal.setDateFormatString(dateFormatString);
    e_cal.addPropertyChangeListener(new PropertyChangeListener() {

      public void propertyChange(PropertyChangeEvent evt) {
        Date d = (Date)s_cal.getMinSelectableDate().clone();
        d.setTime(d.getTime() + ((long)((slider_.getMaxValue()*scale_) * 86400000)));
        if (e_cal.getDate() != null && !e_cal.getDate().equals(d)) {
          setEndDate(e_cal.getDate());
        }
      }
    });
    e_panel.add(e_cal, "wmin 92");
    model = new SpinnerDateModel();
    e_time = new JSpinner(model);
    editor = new JSpinner.DateEditor(e_time, "HH:mm:ss");
    e_time.setEditor(editor);
    e_panel.add(e_time);
    
    add(s_panel, "width 50%");
    if (twoHandles_) {
      add(e_panel, "width 50%");
    } else {
      labelStart_.setText("Date:");
    }
  }

  public void setRange(Date s, Date e) {
    s_cal.setSelectableDateRange(s, e);
    e_cal.setSelectableDateRange(s, e);
    slider_.setMinLabel(format_.format(s_cal.getMinSelectableDate()));
    slider_.setMaxLabel(format_.format(e_cal.getMaxSelectableDate()));
    scale_ = JSlider2Date.offsetInDays(e, s);
    setStartDate(s);
    setEndDate(e);
  }
  
  public void setStartDate(Date d) {
    Date oldDate = s_cal.getDate();
    if (oldDate == null || !s_cal.getDate().equals(d)) {
      s_cal.setDate(d);
      s_time.setValue(d);
    }
    double min = (JSlider2Date.offsetInDays(d, s_cal.getMinSelectableDate())) / scale_;
    if (min != slider_.getMinValue()) {
      slider_.setMinValue(min);
    }
    pcs.fireIndexedPropertyChange("date", 0, oldDate, d);
  }
  
  public void setEndDate(Date d) {
    Date oldDate = e_cal.getDate();
    if (oldDate == null || !e_cal.getDate().equals(d)) {
      e_cal.setDate(d);
      e_time.setValue(d);
    }
    double max = (JSlider2Date.offsetInDays(d, s_cal.getMinSelectableDate())) / scale_;
    if (max != slider_.getMaxValue()) {
      slider_.setMaxValue(max);
    }
    pcs.fireIndexedPropertyChange("date", 0, oldDate, d);
  }

  /**
   * Set the two handle mode.
   *
   * @param th if true set two handles
   */
  public void setTwoHandles(boolean th) {
    twoHandles_ = th;
    slider_.setTwoHandles(twoHandles_);
    if (twoHandles_) {
      labelStart_.setText("Start:");
      add(labelStart_);
      add(s_cal);
    } else {
      labelStart_.setText("Date:");
      remove(labelStop_);
      remove(e_cal);
    }
    validate();
  }

  /**
   * Get the two handle flag.
   *
   * @return true if two handles
   */
  public boolean isTwoHandles() {
    return twoHandles_;
  }

  /**
   * Get the minimum for the range.
   *
   * @param minimum range value
   */
  public Date getStartDate() {
    try {
      return new SimpleDateFormat(dateFormatString + " " + timeFormatString).parse(format_.format(s_cal.getDate()) + " " + (timeFormat_.format((Date)s_time.getValue())));
    } catch (Exception e) {
      return null;
    }
  }

  /**
   * Get the maximum for the range.
   *
   * @param maximum range value
   */
  public Date getEndDate() {
    try {
      return new SimpleDateFormat(dateFormatString + " " + timeFormatString).parse(format_.format(e_cal.getDate()) + " " + (timeFormat_.format((Date)e_time.getValue())));
    } catch (Exception e) {
      return null;
    }
  }

  /**
   * Set the format for the slider range label.
   *
   * @param frmt DateFormat
   */
  public void setFormat(DateFormat frmt) {
    this.format_ = frmt;
    slider_.setMinLabel(format_.format(s_cal.getMinSelectableDate()));
    slider_.setMaxLabel(format_.format(e_cal.getMaxSelectableDate()));
    validate();
  }

  /**
   * Show a border around the slider.
   *
   * @param sb if true show the border
   */
  public void setShowBorder(boolean sb) {
    slider_.setShowBorder(sb);
    validate();
  }

  /**
   * Get border status for the slider.
   *
   * @return true if border is showing
   */
  public boolean getShowBorder() {
    return slider_.getShowBorder();
  }

  /**
   * Set the handle size for the slider.
   *
   * @param sz handle size in pixels
   */
  public void setHandleSize(int sz) {
    slider_.setHandleSize(sz);
    validate();
  }

  /**
   * Get the current slider handle size.
   *
   * @return handle size in pixels
   */
  public int getHandleSize() {
    return slider_.getHandleSize();
  }

  /**
   * Set the always post flag for the slider. If true any motion of the
   * slider will fire a property change, if false a property change is
   * only caused when the mouse button is released.
   *
   * @param ap if true always post
   */
  public void setAlwaysPost(boolean ap) {
    slider_.setAlwaysPost(ap);
  }

  /**
   * Get the always post flag for the slider.
   *
   * @return true if the slider will always post
   */
  public boolean getAlwaysPost() {
    return slider_.getAlwaysPost();
  }
  
  public void reset() {
    slider_.reset();
  }
  
  private static double offsetInDays(Date s, Date e) {
    return ((double)(s.getTime() - e.getTime())) / 86400000.0;
  }

  /**
   * Add a property change listener. The properties that fire a property
   * change are "minValue" and "maxValue". The old and new GeoDate objects
   * are set.
   *
   * @param l property change listener
   */
  @Override
  public void addPropertyChangeListener(PropertyChangeListener l) {
    pcs.addPropertyChangeListener(l);
  }

  /**
   * Remove a property change listener.
   *
   * @param l property change listener
   */
  @Override
  public void removePropertyChangeListener(PropertyChangeListener l) {
    pcs.removePropertyChangeListener(l);
  }

  public static void main(String[] args) {
    Date start,end;
    JSlider2Date js2dt = new JSlider2Date();
    try {
      DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm");
      start = df.parse("0000-00-00 00:00");
      end = df.parse("2010-01-01 00:00");
      js2dt.setRange(start, end);
      
      start = df.parse("2003-01-01 00:00");
      end = df.parse("2003-02-01 00:00");
      js2dt.setStartDate(start);
      js2dt.setEndDate(end);
      
      js2dt.setAlwaysPost(true);
    } catch (ParseException pe) {
    }
    JFrame jf = new JFrame("JSlider2Date Test");
    jf.setSize(800, 500);
    jf.getContentPane().setLayout(new MigLayout("fill"));
    jf.getContentPane().add(js2dt, "grow");
    jf.pack();
    jf.setVisible(true);
  }
}
