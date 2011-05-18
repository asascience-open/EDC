/*
 * Applied Science Associates, Inc.
 * Copyright 2009. All Rights Reserved.
 *
 * SensorContainer.java
 *
 * Created on Aug 27, 2009 @ 10:41:39 AM
 */
package com.asascience.edc.sos;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

/**
 * 
 * @author DAS <dstuebe@asascience.com>
 */
public class SensorContainer {

  private double[] NESW;
  private String name;
  private String gmlName;
  private String description;
  public List<VariableContainer> varList = null;
  private Date startTime;
  private Date endTime;
  private SimpleDateFormat dateFormatter = null;
  private boolean selected = false;
  private List<String> responseFormats = null;

  public SensorContainer() {
    varList = new ArrayList<VariableContainer>();
    responseFormats = new ArrayList<String>();
    dateFormatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm Z");
    TimeZone tz = TimeZone.getTimeZone("GMT");
    dateFormatter.setTimeZone(tz);
  }

  public List<VariableContainer> getVarList() {
    return varList;
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

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public void addVariable(VariableContainer Var) {

    varList.add(Var);

  }

  public int getNumVars() {
    return varList.size();
  }

  public double[] getNESW() {
    // if (NESW != null) {
    return NESW;
    // }

  }

  public void setNESW(double[] NESW) {
    this.NESW = new double[NESW.length];
    System.arraycopy(NESW, 0, this.NESW, 0, NESW.length);

  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public void printSensor() {
    // TODO Auto-generated method stub

    System.out.println("Sensor Name: " + name);
    System.out.println("Sensor Description:" + description);
    if (NESW != null) {
      System.out.println("NESW=" + NESW[0] + ";" + NESW[1] + ";" + NESW[2] + ";" + NESW[3] + ";");
      System.out.println("Sensor BeginDate:" + dateFormatter.format(startTime));
      System.out.println("Sensor EndDate:" + dateFormatter.format(endTime));

    } else {
      System.out.println("Bounds are Null!");
    }
    System.out.println("Number of Vars=" + getNumVars());
    for (VariableContainer v : varList) {
      v.printVariable();
    }

  }

  public void setSelected(boolean selected) {
    this.selected = selected;
  }

  public boolean isSelected() {
    return selected;
  }

  public void setGmlName(String gmlName) {
    this.gmlName = gmlName;
  }

  public String getGmlName() {
    return gmlName;
  }

  public void setResponseFormats(List<String> formats) {
    responseFormats = formats;
  }

  public List<String> getResponseFormats() {
    return responseFormats;
  }
}
