/*
 * NetcdfConstraints.java
 *
 * Created on September 11, 2007, 1:51 PM
 *
 * Applied Science Associates, Inc.
 * Copyright 2007.  All rights reserved.
 */
package com.asascience.edc.nc;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import ucar.unidata.geoloc.LatLonPointImpl;
import ucar.unidata.geoloc.LatLonRect;

import com.asascience.openmap.utilities.GeoConstraints;
import org.apache.log4j.Logger;

/**
 * 
 * @author CBM
 */
public class NetcdfConstraints extends GeoConstraints implements PropertyChangeListener {

  /**
   * Utility field used by bound properties.
   */
  private PropertyChangeSupport propertyChangeSupport;
  private String yDim = "";
  private String xDim = "";
  private String zDim = "";
  private String timeDim = "";
  private String bandDim = "";
  private String projection = "";
  private boolean isZPositive = true;
  private String trimByDim = "";
  private int trimByIndex = 0;
  private boolean trimByZ = true;
  private boolean useAllValues = false;
  private int startTimeIndex = 0;
  private int endTimeIndex = 0;
  private Date sTime = new Date();
  private Date eTime = new Date();
  private String timeInterval = "";
  private String timeUnits = "";
  private int panelType = 0;
  // //striding integers - default value is 1
  private int stride_h = 1;
  private int stride_z = 1;
  private int stride_t = 1;
  private List<String> selVars = new ArrayList<String>();
  private String tVar = "";
  private static Logger logger = Logger.getLogger(NetcdfConstraints.class);
  private static Logger guiLogger = Logger.getLogger("com.asascience.log." + NetcdfConstraints.class.getName());

  /** Creates a new instance of NetcdfConstraints */
  public NetcdfConstraints() {
    propertyChangeSupport = new PropertyChangeSupport(this);
  }

  public NetcdfConstraints(NetcdfConstraints copy){
	  this();
	  yDim = copy.yDim;
	  xDim = copy.xDim;
	  zDim = copy.zDim;
	  timeDim = copy.timeDim;
	  bandDim = copy.bandDim;
	  projection = copy.projection;
	  isZPositive = copy.isZPositive;
	  trimByDim = copy.trimByDim;
	  trimByIndex = copy.trimByIndex;
	  trimByZ = copy.trimByZ;
	  useAllValues = copy.useAllValues;
	  startTimeIndex = copy.startTimeIndex;
	  endTimeIndex = copy.endTimeIndex;
	  sTime = new Date(copy.sTime.getTime());
	  eTime = new Date(copy.eTime.getTime());
	  timeInterval = copy.timeInterval;
	  timeUnits = copy.timeUnits;
	  stride_h = copy.stride_h;
	  stride_z = copy.stride_z;
	  stride_t = copy.stride_t;
	  selVars .addAll(copy.selVars);
	  tVar = copy.tVar;
  }
  
  public void propertyChange(PropertyChangeEvent evt) {
  }

  /**
   * Adds a variable name to the list of selected variables.
   *
   * @param var
   */
  public void addVariable(String var) {
    try {
      selVars.add(var);
    } catch (Exception ex) {
      logger.error("Exception", ex);
      guiLogger.error("Exception", ex);
    }
  }

  /**
   * Removes a variable name to the list of selected variables.
   *
   * @param var
   */
  public void removeVariable(String var) {
    try {
      selVars.remove(var);
    } catch (Exception ex) {
      logger.error("Exception", ex);
      guiLogger.error("Exception", ex);
    }
  }

  /**
   * Clears the list of variable names.
   */
  public void resetVariables() {
    selVars = new ArrayList<String>();
  }

  /**
   * Retrieves the list of selected variable names.
   *
   * @return A vector of variable names.
   */
  public List<String> getSelVars() {
    return this.selVars;
  }

  /**
   * Sets the list of selected variable names.
   *
   * @param selVars
   */
  public void setSelVars(List<String> selVars) {
    List<String> oldSelVars = this.selVars;
    this.selVars = selVars;
    propertyChangeSupport.firePropertyChange("selVars", new ArrayList<String>(oldSelVars), new ArrayList<String>(
            selVars));
  }

  public int getStartTimeIndex() {
    return this.startTimeIndex;
  }

  public void setStartTimeIndex(int i) {
    this.startTimeIndex = i;
  }

  public int getEndTimeIndex() {
    return this.endTimeIndex;
  }

  public void setEndTimeIndex(int i) {
    this.endTimeIndex = i;
  }

  @Override
  public Date getStartTime() {
    return this.sTime;
  }

  @Override
  public void setStartTime(Date startTime) {
    // Date oldStartTime = this.startTime;
    this.sTime = startTime;
    // propertyChangeSupport.firePropertyChange("startTime", new
    // Date(oldStartTime.toString()), new Date(startTime.toString()));
  }

  @Override
  public Date getEndTime() {
    return this.eTime;
  }

  @Override
  public void setEndTime(Date endTime) {
    // Date oldEndTime = this.endTime;
    this.eTime = endTime;
    // propertyChangeSupport.firePropertyChange("startTime", new
    // Date(oldEndTime.toString()), new Date(endTime.toString()));
  }

  @Override
  public void setBoundingBox(LatLonRect llr) {
    northernExtent = (float) llr.getLatMax();
    easternExtent = (float) llr.getLonMax();
    southernExtent = (float) llr.getLatMin();
    westernExtent = (float) llr.getLonMin();
  }

  @Override
  public LatLonRect getBoundingBox() {
    LatLonPointImpl uL = new LatLonPointImpl(northernExtent, westernExtent);
    LatLonPointImpl lR = new LatLonPointImpl(southernExtent, easternExtent);
    LatLonRect llr = new LatLonRect(uL, lR);
    return llr;
  }

  public String getTimeDim() {
    return timeDim;
  }

  public void setTimeDim(String timeDim) {
    this.timeDim = timeDim;
  }

  public String getYDim() {
    return yDim;
  }

  public void setYDim(String yDim) {
    this.yDim = yDim;
  }

  public String getXDim() {
    return xDim;
  }

  public void setXDim(String xDim) {
    this.xDim = xDim;
  }

  public String getZDim() {
    return zDim;
  }

  public void setZDim(String zDim) {
    this.zDim = zDim;
  }

  public String getBandDim() {
    return bandDim;
  }

  public void setBandDim(String bandDim) {
    this.bandDim = bandDim;
  }

  public int getStride_h() {
    return stride_h;
  }

  public void setStride_h(int stride_h) {
    this.stride_h = stride_h;
  }

  public int getStride_z() {
    return stride_z;
  }

  public void setStride_z(int stride_z) {
    this.stride_z = stride_z;
  }

  public int getStride_t() {
    return stride_t;
  }

  public void setStride_t(int stride_t) {
    this.stride_t = stride_t;
  }

  public String getTVar() {
    return tVar;
  }

  public void setTVar(String tVar) {
    this.tVar = tVar;
  }

  public String getTimeInterval() {
    return timeInterval;
  }

  public void setTimeInterval(String timeInterval) {
    this.timeInterval = timeInterval;
  }

  public String getTimeUnits() {
    return timeUnits;
  }

  public void setTimeUnits(String timeUnits) {
    this.timeUnits = timeUnits;
  }

  public int getTrimByIndex() {
    return trimByIndex;
  }

  public void setTrimByIndex(int trimByIndex) {
    this.trimByIndex = trimByIndex;
  }

  public String getProjection() {
    return projection;
  }

  public void setProjection(String projection) {
    this.projection = projection;
  }

  public boolean isIsZPositive() {
    return isZPositive;
  }

  public void setIsZPositive(boolean isZPositive) {
    this.isZPositive = isZPositive;
  }

  public String getTrimByDim() {
    return trimByDim;
  }

  public void setTrimByDim(String trimByDim) {
    this.trimByDim = trimByDim;
  }

  public boolean isTrimByZ() {
    return trimByZ;
  }

  public void setTrimByZ(boolean trimByZ) {
    this.trimByZ = trimByZ;
  }

  public int getPanelType() {
    return panelType;
  }

  public void setPanelType(int panelType) {
    this.panelType = panelType;
  }

  public boolean isUseAllValues() {
    return useAllValues;
  }

  public void setUseAllValues(boolean useAllValues) {
    this.useAllValues = useAllValues;
  }
}
