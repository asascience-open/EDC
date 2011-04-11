/*
 * DataFileBase.java
 *
 * Created on October 9, 2007, 3:43 PM
 *
 * Applied Science Associates, Inc.
 * Copyright 2007.  All rights reserved.
 */
package com.asascience.edp.datafile.hydro;

import java.util.GregorianCalendar;

import com.asascience.utilities.Vector3D;

/**
 * The base class for all current file types. This class contians generic
 * overridable methods for retrieving data at a specific time and location
 * (x/y). Any methods contained in extended classes MUST be defined in this
 * class or the method will not be available to the application.
 * 
 * @author CBM
 */
public abstract class DataFileBase {

  protected long queryTime = 0;
  protected String dataFile;
  protected GregorianCalendar startCal = null;
  protected GregorianCalendar endCal = null;
  protected long timeIncrement = 0l;
  protected boolean hasBottomDepth = false;

  /**
   * Creates a new instance of DataFileBase
   *
   * @param dataFile
   *            <CODE>String</CODE> The path to the data file
   */
  public DataFileBase() {// String dataFile) {
    // this.dataFile = dataFile;
  }

  protected void loadDataFile() {
  }

  ;

  /**
   * <CODE>String</CODE> Get the path to the data file
   *
   * @return <CODE>String</CODE> The path of the data file
   */
  public String getDataFile() {
    return dataFile;
  }

  /**
   * This method ingests the data file string, sets the base property
   * <CODE>dataFile</CODE> and calls the loadDataFile() method to read the
   * data.
   *
   * @param dataFile
   *            <CODE>String</CODE> path to the data file.
   */
  public void setDataFile(String dataFile) {
    this.dataFile = dataFile;
    loadDataFile();
  }

  public void setStartTime(GregorianCalendar startTime) {
    this.startCal = startTime;
  }

  public GregorianCalendar getStartTime() {
    return startCal;
  }

  public void setEndTime(GregorianCalendar endTime) {
    this.endCal = endTime;
  }

  public GregorianCalendar getEndTime() {
    return endCal;
  }

  public void setTimeIncrement(long timeIncrement) {
    this.timeIncrement = timeIncrement;
  }

  public long getTimeIncrement() {
    return timeIncrement;
  }

  public Double[] getLons() {
    return null;
  }

  public Double[] getLats() {
    return null;
  }

  // public double[][] getUVTimeSeries(double lat, double lon){
  // return getUVTimeSeries(new Vector3D(lon, lat, 0));
  // }
  public double[][] getUVTimeSeries(Vector3D position) {
    return null;
  }

  public double[] getDataTimeseries(String id, Vector3D position) {
    return null;
  }

  /**
   * This method sets the queryTime variable. It should be called PRIOR to
   * each time iteration and applies to all the particles for that time step.
   *
   * @param queryTime
   *            <CODE>long</CODE> The current time for data retrieval.
   */
  public void setQueryTime(long queryTime) {
    this.queryTime = queryTime;
  }

  public long getQueryTime() {
    return this.queryTime;
  }

  /**
   * <CODE>double</CODE> The data value at the specified x, y, z position for
   * the <CODE>queryTime</CODE>
   * <P>
   * Override this method in child classes to provide access to file-type
   * specific data retrieval methodology.
   *
   * @param position
   *            <CODE>Vector3D</CODE> The position at which data is being
   *            queried
   * @return <CODE>double</CODE> The data value at the specified x/y
   *         coordinate for the <CODE>queryTime</CODE>
   */
  public Vector3D getCurrentAt(Vector3D position) {
    // process the data in the file-specific class
    return getCurrentAt(this.getQueryTime(), position);
  }

  /**
   * <CODE>double</CODE> The data value at the specified x, y, z position and
   * time.
   * <P>
   * Override this method in child classes to provide access to file-type
   * specific data retrieval methodology.
   *
   * @param position
   *            <CODE>Vector3D</CODE> The position at which data is being
   *            queried
   * @param time
   *            <CODE>long</CODE> The time constraint
   * @return <CODE>double</CODE> The data value at the specified x/y
   *         coordinate and time
   */
  public Vector3D getCurrentAt(long time, Vector3D position) {
    // process the data in the file-specific class
    return null;
  }

  public double getSalinityAt(Vector3D position) {
    return getSalinityAt(this.getQueryTime(), position);
  }

  public double getSalinityAt(long time, Vector3D position) {
    return Double.NaN;
  }

  public double getTempAt(Vector3D position) {
    return getTempAt(this.getQueryTime(), position);
  }

  public double getTempAt(long time, Vector3D position) {
    return Double.NaN;
  }

  public double getBottomDepthAt(Vector3D position) {
    return getBottomDepthAt(this.getQueryTime(), position);
  }

  public double getBottomDepthAt(long time, Vector3D position) {
    return Double.NaN;
  }

  public void setHasBottomDepth(boolean hasBottomDepth) {
    this.hasBottomDepth = hasBottomDepth;
  }

  public boolean isHasBottomDepth() {
    return hasBottomDepth;
  }

  /**
   * Close and/or dispose of any objects to free up space after model run
   * completes.
   */
  public void disposeAll() {
  }
}
