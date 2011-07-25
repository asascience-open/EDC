/**
 * This is taken from NOAA SGT
 * http://www.epic.noaa.gov/java/sgt/
 * 
 * Original information:
 * @author Donald Denbo
 * @version $Revision: 1.8 $, $Date: 2000/10/06 23:16:20 $
 * @see JSlider2
**/
package com.asascience.edc.gui.jslider;

import gov.noaa.pmel.util.Range2D;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.Serializable;
import java.text.DecimalFormat;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import net.miginfocom.swing.MigLayout;
import org.apache.commons.lang.ArrayUtils;
import org.softsmithy.lib.swing.JDoubleField;

/**
 *
 * @author Kyle
 */
public class ErddapJSlider2Double extends JComponent implements Serializable
{
  private boolean twoHandles_;
  private Range2D range_;
  private double minValue_, oldMinValue_;
  private double maxValue_, oldMaxValue_;
  private double scale_;
  private String format_ = "";
  private DecimalFormat form_;
  private boolean indexed_ = false;
  private double[] values_;
  private double[] scaled_;
  private JSlider2 slider_;
  private JComboBox minOps_;
  private JComboBox maxOps_;
  private JDoubleField minField_;
  private JDoubleField maxField_;

  private PropertyChangeSupport pcs = new PropertyChangeSupport(this);
  
  /**
   * Default constructor. The default creates two handles.
   **/
  public ErddapJSlider2Double() {
    this(true);
  }
  /**
   * Constructs a one or two handled slider.
   *
   * @param twoHandles if true create two handles
   */
  public ErddapJSlider2Double(boolean twoHandles) {
    super();
    twoHandles_ = twoHandles;
    
    // Format of the Doubles being displayed
    form_ = new DecimalFormat("###.###");
    form_.setGroupingUsed(false);
      
    setLayout(new MigLayout("gap 0, fill"));
    
    JPanel top_panel = new JPanel(new MigLayout("gap 0, fill"));
    
    slider_ = new JSlider2(twoHandles_);
    
    String[] operators = {">=",">","=~","","!=","=","<","<="};
    
    /* Min block */
    minOps_ = new JComboBox(operators);
    minOps_.addActionListener(new OperatorListener());
    minField_ = new JDoubleField(form_);
    minField_.addActionListener(new ActionListener() {

      public void actionPerformed(ActionEvent e) {
        minValue_ = Math.max(minField_.getDoubleValue(), range_.start);
        minValue_ = Math.min(minValue_, maxValue_);
        double min = (minValue_ - range_.start)/scale_;
        slider_.setMinValue(min);
      }
    });
       
    /* Max block */
    // Reverse operators
    ArrayUtils.reverse(operators);
    maxOps_ = new JComboBox(operators);
    maxOps_.addActionListener(new OperatorListener());
    maxField_ = new JDoubleField(form_);
    maxField_.addActionListener(new ActionListener() {

      public void actionPerformed(ActionEvent e) {
        maxValue_ = Math.min(maxField_.getDoubleValue(), range_.end);
        maxValue_ = Math.max(maxValue_, minValue_);
        double max = (maxValue_ - range_.start)/scale_;
        slider_.setMaxValue(max);
      }
    });
		
    slider_.addPropertyChangeListener(new PropertyChangeListener() {
      public void propertyChange(PropertyChangeEvent evt) {
        if(evt.getPropertyName().equals("minValue")) {
          minValue_ = range_.start + slider_.getMinValue()*scale_;
          minField_.setDoubleValue(minValue_);
          testMin();
          validate();
        } else if(evt.getPropertyName().equals("maxValue")) {
          maxValue_ = range_.start + slider_.getMaxValue()*scale_;
          maxField_.setDoubleValue(maxValue_);
          testMax();
          validate();
        }
      }
    });
    
    top_panel.add(minOps_);
    top_panel.add(minField_, "gap 0, width 80");
    top_panel.add(slider_, "gap 0, growx");
    if(twoHandles_) {
      top_panel.add(maxOps_);
      top_panel.add(maxField_, "gap 0, width 80");
    }
    
    add(top_panel,"growx, wrap");
    //add(bottom_panel,"growx");
  }
  /**
   * Set the range for the slider. The minimum value must be
   * less than the maximum value.
   *
   * @param min minimum value
   * @param max maximum value
   **/
  public void setRange(double min, double max) {
    setRange(new Range2D(min, max));
  }
  /**
   * Set the range for the slider.
   *
   * @param range slider total range
   **/
  public void setRange(Range2D range) {
    double min, max;
    range_ = range;
    scale_ = (double)(range_.end - range_.start);
    slider_.setMinLabel(String.valueOf(range_.start));
    slider_.setMaxLabel(String.valueOf(range_.end));
    
    minValue_ = range_.start;
    oldMinValue_ = minValue_;
    min = (minValue_ - range_.start)/scale_;
    slider_.setMinValue(min); 
    minField_.setDoubleValue(minValue_);
    minField_.setMinimumDoubleValue(minValue_);
    
    maxValue_ = range_.end;
    oldMaxValue_ = maxValue_;
    max = (maxValue_ - range_.start)/scale_;
    slider_.setMaxValue(max);
    maxField_.setDoubleValue(maxValue_);
    maxField_.setMaximumDoubleValue(maxValue_);
    
    if (indexed_) {
      for(int i=0; i < values_.length; i++) {
        scaled_[i] = (values_[i] - range_.start)/scale_;
      }
      slider_.setIndexValues(scaled_);
    }
  }
  /**
   * Get the slider range.
   *
   * @return slider range
   **/
  public Range2D getRange() {
    return range_;
  }
  /**
   * Set the minimum for the range.
   *
   * @param min minimum range value
   */
  public void setMinRange(double min) {
    range_.start = min;
    setRange(range_);
  }
  /**
   * Get the minimum for the range.
   *
   * @return minimum range value
   */
  public double getMinRange() {
    return (double)range_.start;
  }
  /**
   * Set the maximum for the range.
   *
   * @param max maximum range value
   */
  public void setMaxRange(double max) {
    range_.end = max;
    setRange(range_);
  }
  /**
   * Get the maximum for the range.
   *
   * @return maximum range value
   */
  public double getMaxRange() {
    return (double)range_.end;
  }
  /**
   * Reset the slider handles
   **/
  public void reset()
  {
    slider_.reset();
  }
  
  public void setIndexValues(double[] array) {
     values_ = array;
     indexed_ = true;
     scaled_ = new double[values_.length];
     setRange(range_);
  }

  /**
   * Get the two handle flag.
   *
   * @return true if two handles
   */
  public boolean getTwoHandles() {
    return twoHandles_;
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
   * Set the format for the slider range label and display.
   *
   * @param frmt format in Format syntax
   */
  public void setFormat(String frmt) {
    format_ = frmt;
    form_ = new DecimalFormat(format_);
    form_.setGroupingUsed(false);
    setRange(range_);
  }
  /**
   * Get the format for the slider range label.
   *
   * @return the format in Format syntax
   */
  public String getFormat() {
    return format_;
  }
  
  public double getStartValue() {
    if(range_.start > range_.end) {
      minValue_ = Math.min(minField_.getDoubleValue(), range_.start);
    } else {
      minValue_ = Math.max(minField_.getDoubleValue(), range_.start);
    }
    return minValue_;
  }
  
  public double getEndValue() {
    if(range_.start > range_.end) {
      maxValue_ = Math.max(maxField_.getDoubleValue(), range_.end);
    } else {
      maxValue_ = Math.min(maxField_.getDoubleValue(), range_.end);
    }
    return maxValue_;
  }
  
  public String getStartConstraint() {
    return (String)minOps_.getSelectedItem();
  }
  
  public String getEndConstraint() {
    return (String)maxOps_.getSelectedItem();
  }
  
  public void setStartValue(double min) {
    if(range_.start > range_.end) {
      minValue_ = Math.min(min, range_.start);
    } else {
      minValue_ = Math.max(min, range_.start);
    }
    min = (minValue_ - range_.start)/scale_;
    slider_.setMinValue(min);
    minField_.setMinimumDoubleValue(min);
  }
  
  public void setEndValue(double max) {
    if(range_.start > range_.end) {
      maxValue_ = Math.max(max, range_.end);
    } else {
      maxValue_ = Math.min(max, range_.end);
    }
    max = (maxValue_ - range_.start)/scale_;
    slider_.setMaxValue(max);
    maxField_.setMaximumDoubleValue(max);
  }
  
  /**
   * Get the minimum handle value.
   *
   * @return minimum handle value.
   **/
  public double getMinValue() {
    minValue_ = Math.max(minField_.getDoubleValue(), range_.start);
    minValue_ = Math.min(minValue_, maxValue_);
    return minValue_;
  }
  /**
   * Set the minimum handle value.
   *
   * @param min minimum handle value.
   **/
  public void setMinValue(double min) {
    minValue_ = min;
    minValue_ = Math.min(minValue_, maxValue_);
    min = (minValue_ - range_.start)/scale_;
    slider_.setMinValue(min); 
    minField_.setMinimumDoubleValue(min);
  }
  /**
   * Get the maximum handle value.
   *
   * @return maximum handle value
   **/
  public double getMaxValue() {
    maxValue_ = Math.min(maxField_.getDoubleValue(), range_.end);
    maxValue_ = Math.max(maxValue_, minValue_);
    return maxValue_;
  }
  /**
   * Set the maximum handle value.
   *
   * @param max maximum handle value
   **/
  public void setMaxValue(double max) {
    maxValue_ = max;
    maxValue_ = Math.max(maxValue_, minValue_);
    max = (maxValue_ - range_.start)/scale_;
    slider_.setMaxValue(max);
    maxField_.setMaximumDoubleValue(max);
  }

   /**
   * Show a border around the slider.
   *
   * @param sb if true show the border
   */
  public void setShowBorder(boolean sb) {
    slider_.setShowBorder(sb);
  }
  
  /**
   * Set the handle size for the slider.
   *
   * @param sz handle size in pixels
   */
  public void setHandleSize(int sz) {
    slider_.setHandleSize(sz);
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
  /**
   * Add a property change listener. The properties that fire a property
   * change are "minValue" and "maxValue". The old and new Double objects
   * values are set.
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

  public boolean isIndexed() {
     return indexed_;
  }
  
  public void setIndexed(boolean ind) {
      indexed_ = ind;
  }
  
  public void setSize(Dimension dim) {
    super.setSize(dim);
    validate();
  }
  public void setSize(int w, int h) {
    super.setSize(w, h);
    validate();
  }
    
  public Dimension getMaximumSize() {
    return new Dimension(Short.MAX_VALUE,Short.MAX_VALUE);
  }
 
  void testMax() {
    if(oldMaxValue_ != maxValue_) {
      Double tempOldValue = new Double(oldMaxValue_);
      oldMaxValue_ = maxValue_;
      pcs.firePropertyChange("maxValue", tempOldValue, new Double(maxValue_));
    }
  }
  void testMin() {
    if(oldMinValue_ != minValue_) {
      Double tempOldValue = new Double(oldMinValue_);
      oldMinValue_ = minValue_;
      pcs.firePropertyChange("minValue", tempOldValue, new Double(minValue_)); 
    }
  }

  public static void main(String[] args) {
    JFrame jf = new JFrame("JSlider2Double Test");
    jf.setSize(300,200);
    jf.getContentPane().setLayout(new MigLayout("gap 0, fill"));
    ErddapJSlider2Double js2db = new ErddapJSlider2Double();
    js2db.setRange(new Range2D(-20.0, 20.0));
    js2db.setShowBorder(false);
    //js2db.setSize(400, 150);
    //js2db.setStartValue(-10.0);
    //js2db.setEndValue(15.0);
    //double[] values = {-20.0, -10.0, -5.0, -2.5, 0., 2.5, 5.0, 10., 15., 20.0};
    //js2db.setIndexValues(values);
    js2db.setAlwaysPost(true);
    jf.getContentPane().add(js2db, "gap 0, growx");
    jf.setVisible(true);
  }

  class OperatorListener implements ActionListener {
    public void actionPerformed(ActionEvent e) {
      pcs.firePropertyChange("minValue", null, minField_.getDoubleValue()); 
    }
  }
  
}
