package com.asascience.edc.sos.map;

import java.awt.Color;
import java.awt.Font;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.asascience.edc.sos.SensorContainer;
import com.asascience.edc.utils.WorldwindUtils;
import com.asascience.utilities.Utils;

import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.event.SelectEvent;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.render.PointPlacemark;
import gov.nasa.worldwind.render.PointPlacemarkAttributes;
import ucar.unidata.geoloc.LatLonRect;

/**
 * WorldwindSosLayer.java
 * 
 * @author Kyle Wilcox <kwilcox@asascience.com>
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
    attrs.setLabelColor(Utils.getABGRFromColor("00", Color.WHITE));
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
    LatLon minPt = LatLon.fromDegrees(WorldwindUtils.normLat(bbox.getLatMin()), 
  		  WorldwindUtils.normLon360(bbox.getLonMin()));
    LatLon maxPt = LatLon.fromDegrees(WorldwindUtils.normLat(bbox.getLatMax()), 
  		  WorldwindUtils.normLon360(bbox.getLonMax()));
    for (PointPlacemark sp : sensorPoints) {
      sp.setHighlighted(false);
      LatLon norm360Pt = LatLon.fromDegrees(WorldwindUtils.normLat( sp.getPosition().getLatitude().degrees),
    		  WorldwindUtils.normLon360(  sp.getPosition().getLongitude().degrees));
   

      // normalize longitudes from 0-360 and determine if the box contains each point
	  if(norm360Pt.latitude.degrees >= minPt.latitude.degrees && norm360Pt.latitude.degrees <= maxPt.latitude.degrees &&
		 (norm360Pt.longitude.degrees >= minPt.longitude.degrees && norm360Pt.longitude.degrees <= maxPt.longitude.degrees ||
		 (minPt.longitude.degrees > maxPt.longitude.degrees && 
				 (norm360Pt.longitude.degrees >= minPt.longitude.degrees || norm360Pt.longitude.degrees <= maxPt.longitude.degrees ))
		 )){
        pickedSensors.add(sp);
        sp.setHighlighted(true);
      }
    }
  }

  public List<LatLon> setPickedByPolygon(List<Position> posList){
	  List<LatLon> selectedSosLocs = new ArrayList<LatLon>();
	  Path2D polyPath = new Path2D.Double();

	  boolean first = true;
	  for(Position pos : posList){
		  if(first)
			  polyPath.moveTo(pos.latitude.degrees, pos.longitude.degrees);
		  else
			  polyPath.lineTo(pos.latitude.degrees,  pos.longitude.degrees);
		  first = false;
	  }
	  polyPath.closePath();
	  for (PointPlacemark sp : sensorPoints) {
	      sp.setHighlighted(false);
	      if(polyPath.contains(sp.getPosition().getLatitude().degrees, sp.getPosition().getLongitude().degrees)){
	    	  sp.setHighlighted(true);
	    	 
	    	  selectedSosLocs.add(sp.getPosition());
	      }
	  }
	  return selectedSosLocs;
  }
  public List<LatLon> setPickedByTrackLine(List<Position> posList, Double trackLineWidth){
	  List<LatLon> selectedSosLocs = new ArrayList<LatLon>();
	   for (PointPlacemark sp : sensorPoints) {
		      sp.setHighlighted(false);
		      LatLon normPt = LatLon.fromDegrees(WorldwindUtils.normLat( sp.getPosition().getLatitude().degrees),
		    		  WorldwindUtils.normLon(  sp.getPosition().getLongitude().degrees));
		      Iterator<Position> posIter = posList.iterator();
		      boolean intersects = false;
		      Position previous = null;
		      while(posIter.hasNext()){
		    	  Position firstSeg;
		    	  if(previous !=  null)
		    		  firstSeg = previous;
		    	  else
		    		  firstSeg = posIter.next();
		    	  Position secSeg = null;
		    	  if(posIter.hasNext())
		    		  secSeg = posIter.next();
		    	  if(secSeg == null)
		    		  break;
		    	  previous = secSeg;
		    	  Line2D trackLineSeg = new Line2D.Double(firstSeg.latitude.degrees, firstSeg.longitude.degrees,
		    			  							secSeg.latitude.degrees, secSeg.longitude.degrees);
		    

		    	 Double dist = trackLineSeg.ptSegDist(normPt.latitude.degrees, normPt.longitude.degrees);
		    	  if(dist <= trackLineWidth) {
		    			  
		    		  intersects = true;
		    		  break;
		    	  }
		      }
		      if(intersects){
		    	  sp.setHighlighted(true);
		    	  selectedSosLocs.add(normPt);
		      }
	   }
	   return selectedSosLocs;
  }
  public Position getEyePosition() {
    return WorldwindUtils.getEyePositionFromPositions(sensorLatLons);
  }

  public void toggleSensor(PointPlacemark sensor) {
    sensor.setHighlighted(!sensor.isHighlighted());
  }

  public void setSensors(List<SensorContainer> sensors) {
    try {
      sensorPoints.clear();
      sensorLatLons.clear();
      for (SensorContainer sensor : sensors) {
    	  double[] bBox = sensor.getNESW();
    	  if(bBox != null && bBox.length >= 2)
    		  addRenderable(createPoint(sensor.getNESW()[0], WorldwindUtils.normLon(sensor.getNESW()[1]), sensor));
      }
      pcs.firePropertyChange("sensorsloaded", false, true);
    } catch (Exception ex) {
      ex.printStackTrace();
    }
  }

  public PointPlacemark createPoint(double lat, double lon, SensorContainer sensor) {
    PointPlacemark pp = new PointPlacemark(Position.fromDegrees(lat, lon));
    if (!sensor.getName().isEmpty()) {
      pp.setLabelText(sensor.getName());
    }
    pp.setValue("sensor", sensor);
    pp.setAltitudeMode(WorldWind.CLAMP_TO_GROUND);
    pp.setAttributes(attrs);
    pp.setHighlightAttributes(selected_attrs);
    sensorPoints.add(pp);
    sensorLatLons.add(WorldwindUtils.normalizeLatLon(LatLon.fromDegrees(lat, lon)));
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

  public void selected(SelectEvent event) {
    if (event.getEventAction().equals(SelectEvent.LEFT_CLICK)) {
      // This is a left click
      if (event.hasObjects() && event.getTopPickedObject().hasPosition()) {
        // There is a picked object with a position
        if (event.getTopObject().getClass().equals(PointPlacemark.class)) {
          // This object class we handle and we have an orbit view
          PointPlacemark sen = (PointPlacemark)event.getTopPickedObject().getObject();
          sen.setHighlighted(!sen.isHighlighted());
        }
      }
    }
  }


}
