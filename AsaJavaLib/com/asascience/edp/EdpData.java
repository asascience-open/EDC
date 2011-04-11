/*
 * EdpData.java
 *
 * Created on October 10, 2007, 8:09 AM
 *
 * Applied Science Associates, Inc.
 * Copyright 2007.  All rights reserved.
 */
package com.asascience.edp;

import com.asascience.utilities.Vector3D;

/**
 * Provides an object for shuttling data between the EDP and the transport &
 * fates models.
 * 
 * @author CBM
 */
public class EdpData {

  private long queryTime;
  private Vector3D queryPos;
  private double waterTemp;
  private double airTemp;
  private double sal;
  // private Vector3D position;
  private Vector3D current;
  private Vector3D wind;

  /** Creates a new instance of EdpData */
  public EdpData() {
  }

  public double getWaterTemp() {
    return waterTemp;
  }

  public void setWaterTemp(double temp) {
    this.waterTemp = temp;
  }

  public double getAirTemp() {
    return airTemp;
  }

  public void setAirTemp(double airTemp) {
    this.airTemp = airTemp;
  }

  public double getSal() {
    return sal;
  }

  public void setSal(double sal) {
    this.sal = sal;
  }

  //
  // public Vector3D getPosition() {
  // return position;
  // }
  // public void setPosition(Vector3D position) {
  // this.position = position;
  // }
  // public void setPosition(double x, double y, double z){
  // this.position = new Vector3D(x, y, z);
  // }
  public Vector3D getCurrent() {
    return current;
  }

  public void setCurrent(Vector3D current) {
    this.current = current;
  }

  public void setCurrent(double x, double y, double z) {
    this.current = new Vector3D(x, y, z);
  }

  public Vector3D getWind() {
    return wind;
  }

  public void setWind(Vector3D wind) {
    this.wind = wind;
  }

  public void setWind(double x, double y, double z) {
    this.wind = new Vector3D(x, y, z);
  }

  public long getQueryTime() {
    return queryTime;
  }

  public void setQueryTime(long queryTime) {
    this.queryTime = queryTime;
  }

  public Vector3D getQueryPos() {
    return queryPos;
  }

  public void setQueryPos(Vector3D queryPos) {
    this.queryPos = queryPos;
  }
}
