/*
 * Applied Science Associates, Inc.
 * Copyright 2007. All rights reserved.
 */

/*
 * NcReaderBase.java
 *
 * Created on Jan 1, 2008, 12:00:00 AM
 *
 */
package com.asascience.edc.nc;

import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.util.Date;
import java.util.List;

import ucar.nc2.NetcdfFile;
import ucar.nc2.VariableSimpleIF;
import ucar.nc2.dataset.NetcdfDataset;
import ucar.nc2.dt.grid.GridDataset;
import ucar.nc2.util.CancelTask;
import ucar.unidata.geoloc.LatLonRect;

/**
 * 
 * @author CBM <cmueller@asascience.com>
 */
public class NcReaderBase implements CancelTask {

  public final static int UNDEFINED_ERROR = -1;
  public final static int INIT_OK = 0;
  public final static int NO_GRIDDATASET = 1;
  public final static int NO_GEOGRIDS = 2;
  public final static int INVALID_BOUNDS = 3;
  protected NetcdfFile ncFile;
  protected NetcdfConstraints ncCons;
  protected LatLonRect bounds;
  protected Date startTime;
  protected Date endTime;
  protected Date[] times;
  protected List<VariableSimpleIF> sVars = null;
  protected String timeDim = "";
  protected String xDim = "";
  protected String yDim = "";
  protected String zDim = "";
  protected boolean hasTime = false;
  protected boolean hasZ = false;

  public NcReaderBase(NetcdfDataset ncd, NetcdfConstraints cons) throws IOException {
    setNcFile(ncd);
    ncCons = cons;
  }

  public int initialize() {
    return NcReaderBase.UNDEFINED_ERROR;
  }

  public boolean extractData2(NetcdfConstraints cons, String outnc) {
    return false;
  }

  // public int extractData2(NetcdfConstraints cons, String outnc,
  // List<GridDataset> gdsList){
  // return -1;
  // }
  public boolean extractData2(NetcdfConstraints cons, String outnc, List<GridDataset> gdsList) {
    return false;
  }

  public int extractData2(NetcdfConstraints cons, String outnc, List<GridDataset> gdsList, PropertyChangeSupport pcs) {
    return -1;
  }

  // public boolean extractData2(NetcdfConstraints cons, String outnc,
  // List<GridDataset> gdsList, PropertyChangeSupport pcs){
  // return false;
  // }
  public int getTimeIndex(Date date) {
    return getTimeIndex(date, times);
  }

  public int getTimeIndex(Date date, Date[] dates) {
    for (int i = 0; i < dates.length; i++) {
      if (((Date) dates[i]).compareTo(date) == 0) {
        return i;
      }
    }
    return -1;
  }

  public boolean isCancel() {
    return false;
  }

  public void setError(String msg) {
  }

  public NetcdfFile getNcFile() {
    return ncFile;
  }

  public void setNcFile(NetcdfFile ncds) {
    this.ncFile = ncds;
  }

  public NetcdfConstraints getNcConstraints() {
    return ncCons;
  }

  public void setNcConstraints(NetcdfConstraints constraints) {
    this.ncCons = constraints;
  }

  public NetcdfConstraints getNcCons() {
    return ncCons;
  }

  public void setNcCons(NetcdfConstraints ncCons) {
    this.ncCons = ncCons;
  }

  public LatLonRect getBounds() {
    return bounds;
  }

  public void setBounds(LatLonRect bounds) {
    this.bounds = bounds;
  }

  public Date getStartTime() {
    return startTime;
  }

  public void setStartTime(Date startTime) {
    this.startTime = startTime;
  }

  public Date getEndTime() {
    return endTime;
  }

  public void setEndTime(Date endTime) {
    this.endTime = endTime;
  }

  public Date[] getTimes() {
    return times;
  }

  public void setTimes(Date[] times) {
    this.times = times;
  }

  // public List<VariableSimpleIF> getSVars() {
  // return sVars;
  // }
  //
  // public void setSVars(List<VariableSimpleIF> sVars) {
  // this.sVars = sVars;
  // }
  public String getTimeDim() {
    return timeDim;
  }

  public void setTimeDim(String timeDim) {
    this.timeDim = timeDim;
  }

  public String getXDim() {
    return xDim;
  }

  public void setXDim(String xDim) {
    this.xDim = xDim;
  }

  public String getYDim() {
    return yDim;
  }

  public void setYDim(String yDim) {
    this.yDim = yDim;
  }

  public String getZDim() {
    return zDim;
  }

  public void setZDim(String zDim) {
    this.zDim = zDim;
  }

  public boolean isHasTime() {
    return hasTime;
  }

  public void setHasTime(boolean hasTime) {
    this.hasTime = hasTime;
  }

  public boolean isHasZ() {
    return hasZ;
  }

  public void setHasZ(boolean hasZ) {
    this.hasZ = hasZ;
  }

@Override
public void setProgress(String arg0, int arg1) {
	// TODO Auto-generated method stub
	
}
}
