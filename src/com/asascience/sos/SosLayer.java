/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.asascience.sos;

import com.asascience.openmap.mousemode.InformationMouseMode;
import com.bbn.openmap.event.MapMouseListener;
import com.bbn.openmap.layer.OMGraphicHandlerLayer;
import com.bbn.openmap.omGraphics.OMGraphicList;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.List;
import ucar.unidata.geoloc.LatLonRect;

/**
 *
 * @author Kyle
 */
public class SosLayer extends OMGraphicHandlerLayer implements MapMouseListener, PropertyChangeListener {

  private ArrayList<SensorPoint> sensorPoints;
  private OMGraphicList omgraphics;
  private SensorPoint selectedGraphic;
  private ArrayList<SensorPoint> pickedSensors;
  private PropertyChangeSupport pcs;

  public SosLayer() {
    pickedSensors = new ArrayList();
    sensorPoints = new ArrayList();
    this.consumeEvents = true;
    this.mouseModeIDs = getMouseModeServiceList();
    omgraphics = new OMGraphicList();
    this.setList(omgraphics);
    pcs = new PropertyChangeSupport(this);
  }

  public void setPickedByBBOX(LatLonRect bbox) {
    pickedSensors.clear();
    for (SensorPoint sp : sensorPoints) {
      sp.depick();
      if (bbox.contains(sp.getLat(), sp.getLon())) {
        pickedSensors.add(sp);
        sp.pick();
      }
    }
  }

  public void setSensors(List<SensorContainer> sensors) {
    try {
      OMGraphicList omgl = this.getList();
      omgl.clear();
      for (SensorContainer sensor : sensors) {
        omgl.add(createPoint(sensor.getNESW()[0], sensor.getNESW()[1], 5, sensor));
        omgl.setVisible(true);
        this.doPrepare();
        pcs.firePropertyChange("loaded", false, true);
      }
    } catch (Exception ex) {
      ex.printStackTrace();
    }
  }

  public SensorPoint createPoint(double lat, double lon, int radius, SensorContainer sensor) {
    SensorPoint pt = new SensorPoint(Float.parseFloat(Double.toString(lat)), Float.parseFloat(Double.toString(lon)), radius);
    pt.setSensor(sensor);
    pt.setOval(true);
    pt.addPropertyChangeListener(new PropertyChangeListener() {

      public void propertyChange(PropertyChangeEvent evt) {
        repaint();
        setPickedSensors();
      }
    });
    if (!sensorPoints.contains(pt)) {
      sensorPoints.add(pt);
    }
/*
    LabeledOMPoly p2 = new LabeledOMPoly();
    p2.setLat(pt.getLat());
    p2.setLon(pt.getLon());
    p2.setRenderType(LabeledOMPoly.RENDERTYPE_LATLON);
    p2.setText(name);
    p2.setTextPaint(Color.BLUE);
    p2.setVisible(true);
*/
    return pt;
  }

  @Override
  public OMGraphicList prepare() {
    OMGraphicList list = this.getList();
    /*
    OMGraphic g;
    for (int i = 0; i < list.size(); i++) {
      g = list.getOMGraphicAt(i);
      g.setLinePaint(Color.BLACK);
      g.setFillPaint(Color.BLUE);
      g.setSelectPaint(Color.RED);
    }
    */
    list.generate(this.getProjection());
    return list;
  }

  public boolean mousePressed(MouseEvent me) {
    return false;
  }

  public boolean mouseReleased(MouseEvent me) {
    return false;
  }

  private void setPickedSensors() {
    pickedSensors.clear();
    for (SensorPoint p : sensorPoints) {
      if (p.isPicked()) {
        pickedSensors.add(p);
      }
    }
  }

  public boolean mouseClicked(MouseEvent me) {
    SensorPoint newSelGraphic = (SensorPoint)this.getList().findClosest(me.getX(), me.getY(), 5.0f);
    if (newSelGraphic == null) {
      this.fireRequestInfoLine("");
      repaint();
      return true;
    } else {
      if (pickedSensors.contains(newSelGraphic)) {
        newSelGraphic.depick();
      } else {
        newSelGraphic.pick();
      }
      pcs.firePropertyChange("clicked", null, newSelGraphic);
    }
    
    return true;
  }

  public ArrayList<SensorPoint> getPickedSensors() {
    setPickedSensors();
    return pickedSensors;
  }

  public ArrayList<SensorPoint> getSensors() {
    return sensorPoints;
  }

  public void mouseEntered(MouseEvent me) {
  }

  public void mouseExited(MouseEvent me) {
  }

  public boolean mouseDragged(MouseEvent me) {
    return false;
  }

  public void mouseMoved() {
  }

  @Override
  public MapMouseListener getMapMouseListener() {
    return this;
  }

  public String[] getMouseModeServiceList() {
    String[] ret = new String[1];
    ret[0] = InformationMouseMode.modeID;
    return ret;
  }

  public boolean mouseMoved(MouseEvent me) {
    SensorPoint newSelGraphic = (SensorPoint)this.getList().findClosest(me.getX(), me.getY(), 5.0f);
    if (selectedGraphic != null) {
      selectedGraphic.deselect();
    }
    if (newSelGraphic == null) {
      this.fireRequestInfoLine("");
      repaint();
      return true;
    } else {
      selectedGraphic = newSelGraphic;
      newSelGraphic.select();
      this.fireRequestInfoLine("Sensor: " + selectedGraphic.getSensor().getName());
      repaint();
    }
    return true;
  }

  public void propertyChange(PropertyChangeEvent evt) {
    pcs.firePropertyChange(evt);
  }

  @Override
  public void addPropertyChangeListener(java.beans.PropertyChangeListener l) {
    pcs.addPropertyChangeListener(l);
  }

  @Override
  public void removePropertyChangeListener(java.beans.PropertyChangeListener l) {
    pcs.removePropertyChangeListener(l);
  }
}
