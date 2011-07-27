package com.asascience.edc.sos.map;

import com.asascience.edc.sos.SensorContainer;
import com.bbn.openmap.omGraphics.OMPoint;
import java.awt.Color;
import java.awt.Paint;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

/**
 * SensorPoint.java
 * 
 * @author Kyle Wilcox <kwilcox@asascience.com>
 */
public class SensorPoint extends OMPoint implements PropertyChangeListener {

  private SensorContainer sensor;
  private final static Paint UNPICKED_COLOR = Color.RED;
  private final static Paint UNPICKED_OUTLINE = Color.BLACK;
  private final static Paint PICKED_COLOR = Color.GREEN;
  private final static Paint SELECTED_COLOR = Color.ORANGE;
  private final static Paint SELECTED_OUTLINE = Color.BLACK;
  private boolean picked;
  protected PropertyChangeSupport pcs;

  public SensorPoint(float lat, float lon, int radius) {
    super(lat, lon, radius);
    pcs = new PropertyChangeSupport(this);
    resetStyle();
    picked = false;
  }

  public void setSensor(SensorContainer sensor) {
    this.sensor = sensor;
  }

  public SensorContainer getSensor() {
    return sensor;
  }

  private void resetStyle() {
    this.setFillPaint(UNPICKED_COLOR);
    this.setLinePaint(UNPICKED_OUTLINE);
    this.setSelectPaint(SELECTED_OUTLINE);
  }

  public void pick() {
    this.setFillPaint(PICKED_COLOR);
    pcs.firePropertyChange("pick", null, this);
    picked = true;
  }

  public void depick() {
    resetStyle();
    pcs.firePropertyChange("depick", null, this);
    picked = false;
  }

  @Override
  public void select() {
    resetStyle();
    this.setFillPaint(SELECTED_COLOR);
    super.select();
  }

  @Override
  public void deselect() {
    resetStyle();
    super.deselect();
    if (picked) {
      pick();
    }
  }

  public boolean isPicked() {
    return picked;
  }

  public void togglePicked() {
    if (picked) {
      depick();
    } else {
      pick();
    }
  }

  public void addPropertyChangeListener(java.beans.PropertyChangeListener l) {
    pcs.addPropertyChangeListener(l);
  }

  public void removePropertyChangeListener(java.beans.PropertyChangeListener l) {
    pcs.removePropertyChangeListener(l);
  }

  public void propertyChange(PropertyChangeEvent evt) {
    pcs.firePropertyChange(evt);
  }
}
