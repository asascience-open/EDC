/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.asascience.edc.sos.map;

import com.asascience.edc.sos.SensorContainer;
import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.render.PointPlacemark;
import gov.nasa.worldwind.render.PointPlacemarkAttributes;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.List;
import ucar.unidata.geoloc.LatLonRect;
import com.asascience.utilities.Utils;
import gov.nasa.worldwind.Configuration;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.globes.Earth;

/**
 *
 * @author Kyle
 */
public class WorldwindSosLayer extends RenderableLayer {
  
  private ArrayList<PointPlacemark> sensorPoints;
  private ArrayList<LatLon> sensorLatLons;
  private ArrayList<PointPlacemark> pickedSensors;
  private PropertyChangeSupport pcs;
  private PointPlacemarkAttributes attrs;
  private PointPlacemarkAttributes selected_attrs;

  public WorldwindSosLayer() {
    pickedSensors = new ArrayList();
    sensorPoints = new ArrayList();
    sensorLatLons = new ArrayList();
    pcs = new PropertyChangeSupport(this);
    
    // Default attributes
    attrs = new PointPlacemarkAttributes();
    attrs.setLineColor(Utils.getABGRFromColor(Color.RED));
    attrs.setLabelColor(Utils.getABGRFromColor("00",Color.WHITE));
    attrs.setUsePointAsDefaultImage(true);
    attrs.setScale(4d);
    
    selected_attrs = new PointPlacemarkAttributes(attrs);
    selected_attrs.setLineColor(Utils.getABGRFromColor(Color.GREEN));
    selected_attrs.setLabelFont(Font.decode("Arial-BOLD-14"));
    selected_attrs.setLabelColor(Utils.getABGRFromColor(Color.WHITE));
    selected_attrs.setScale(6d);
  }

  public void setPickedByBBOX(LatLonRect bbox) {
    pickedSensors.clear();
    for (PointPlacemark sp : sensorPoints) {
      sp.setHighlighted(false);
      if (bbox.contains(sp.getPosition().getLatitude().getDegrees(), sp.getPosition().getLongitude().getDegrees())) {
        pickedSensors.add(sp);
        sp.setHighlighted(true);
      }
    }
  }
  
  public Position getEyePosition() { 
    Sector sector = Sector.boundingSector(sensorLatLons);
    Angle delta = sector.getDeltaLat();
    if (sector.getDeltaLon().compareTo(delta) > 0) {
        delta = sector.getDeltaLon();
    }
    double arcLength = delta.radians * Earth.WGS84_EQUATORIAL_RADIUS;
    double fieldOfView = Configuration.getDoubleValue(AVKey.FOV, 45.0);
    return new Position(sector.getCentroid(), arcLength / (1.5 * Math.tan(fieldOfView / 2.0)));
  }

  public void toggleSensor(PointPlacemark sensor) {
    sensor.setHighlighted(!sensor.isHighlighted());
  }
  
  public void setSensors(List<SensorContainer> sensors) {
    try {
      sensorPoints.clear();
      sensorLatLons.clear();
      for (SensorContainer sensor : sensors) {
        addRenderable(createPoint(sensor.getNESW()[0], sensor.getNESW()[1], sensor));
      }
      pcs.firePropertyChange("sensorsloaded", false, true);
    } catch (Exception ex) {
      ex.printStackTrace();
    }
  }

  public PointPlacemark createPoint(double lat, double lon, SensorContainer sensor) {
    PointPlacemark pp = new PointPlacemark(Position.fromDegrees(lat, lon));
    pp.setLabelText(sensor.getName());
    pp.setValue("sensor", sensor);
    pp.setAltitudeMode(WorldWind.CLAMP_TO_GROUND);
    pp.setAttributes(attrs);
    pp.setHighlightAttributes(selected_attrs);
    sensorPoints.add(pp);
    sensorLatLons.add(LatLon.fromDegrees(lat, lon));
    return pp;
  }

  private void setPickedSensors() {
    pickedSensors.clear();
    for (PointPlacemark p : sensorPoints) {
      if (p.isHighlighted()) {
        pickedSensors.add(p);
      }
    }
  }

  public ArrayList<PointPlacemark> getPickedSensors() {
    setPickedSensors();
    return pickedSensors;
  }

  public ArrayList<PointPlacemark> getSensors() {
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
