/*
 * GeoConstraints.java
 *
 * Created on December 11, 2007, 1:17 PM
 *
 * Applied Science Associates, Inc.
 * Copyright 2007.  All rights reserved.
 */
package com.asascience.openmap.utilities;

import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import ucar.unidata.geoloc.LatLonPointImpl;
import ucar.unidata.geoloc.LatLonRect;

/**
 * 
 * @author CBM
 */
public class GeoConstraints {

  private String extentName = "";
  protected double northernExtent = 0;
  protected double southernExtent = 0;
  protected double easternExtent = 0;
  protected double westernExtent = 0;
  protected GregorianCalendar startTime = new GregorianCalendar(TimeZone.getTimeZone("UTC"));// Calendar.getInstance();
  protected GregorianCalendar endTime = new GregorianCalendar(TimeZone.getTimeZone("UTC"));// Calendar.getInstance();

  /** Creates a new instance of GeoConstraints */
  public GeoConstraints() {
  }

  public void setBoundingBox(double north, double east, double south, double west) {
    northernExtent = north;
    easternExtent = east;
    southernExtent = south;
    westernExtent = west;
  }

  public void setBoundingBox(LatLonRect llr) {
    northernExtent = (float) llr.getLatMax();
    easternExtent = (float) llr.getLonMax();
    southernExtent = (float) llr.getLatMin();
    westernExtent = (float) llr.getLonMin();
  }

  public LatLonRect getBoundingBox() {
    LatLonPointImpl uL = new LatLonPointImpl(northernExtent, westernExtent);
    LatLonPointImpl lR = new LatLonPointImpl(southernExtent, easternExtent);
    LatLonRect llr = new LatLonRect(uL, lR);
    return llr;
  }

  public double getNorthernExtent() {
    return northernExtent;
  }

  public void setNorthernExtent(double northernExtent) {
    this.northernExtent = northernExtent;
  }

  public double getSouthernExtent() {
    return southernExtent;
  }

  public void setSouthernExtent(double southernExtent) {
    this.southernExtent = southernExtent;
  }

  public double getEasternExtent() {
    return easternExtent;
  }

  public void setEasternExtent(double easternExtent) {
    this.easternExtent = easternExtent;
  }

  public double getWesternExtent() {
    return westernExtent;
  }

  public void setWesternExtent(double westernExtent) {
    this.westernExtent = westernExtent;
  }

  // public Calendar getStartTime() {
  // return startTime;
  // }
  public Date getStartTime() {
    return startTime.getTime();
  }

  // public void setStartTime(Calendar startTime) {
  // this.startTime = startTime;
  // }
  public void setStartTime(Date startTime) {
    this.startTime.setTime(startTime);
  }

  // public Calendar getEndTime() {
  // return endTime;
  // }
  public Date getEndTime() {
    return startTime.getTime();
  }

  // public void setEndTime(Calendar endTime) {
  // this.endTime = endTime;
  // }
  public void setEndTime(Date endTime) {
    this.endTime.setTime(endTime);
  }

  public String getExtentName() {
    return extentName;
  }

  public void setExtentName(String extentName) {
    this.extentName = extentName;
  }
}
