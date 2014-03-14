/*
 * Applied Science Associates, Inc.
 * Copyright 2009. All Rights Reserved.
 *
 * GenericGridReader.java
 *
 * Created on Feb 12, 2009 @ 8:49:11 AM
 */
package com.asascience.openmap.layer.nc.grid;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import ucar.ma2.Array;
import ucar.nc2.dt.grid.GeoGrid;

/**
 * 
 * @author CBM <cmueller@asascience.com>
 */
public class GenericGridReader extends NcGridReader {

  private static List<String> uNames = new ArrayList<String>();
  private static List<String> vNames = new ArrayList<String>();

  static {
    uNames.add("u");
    uNames.add("water_u");
    uNames.add("taugeo");
    vNames.add("v");
    vNames.add("water_v");
    vNames.add("tavgeo");
  }
  private GeoGrid gU = null;
  private GeoGrid gV = null;
  private List<String> dataNames = new ArrayList<String>();
  private boolean hasUV = false;

  public GenericGridReader(String file) {
    super(file);

    String name;
    for (int i = 0; i < sVars.size(); i++) {
      name = sVars.get(i).getFullName();
      dataNames.add(name);

      if (uNames.contains(name.toLowerCase())) {
        gU = super.getGridByName(name);
        hasUV = true;
        uvUnits = gU.getUnitsString();
      } else if (vNames.contains(name.toLowerCase())) {
        gV = super.getGridByName(name);
      }
    }

    if (gU == null | gV == null) {
      System.err.println("GeneralGridReader:Error Retreiving U & V GeoGrids");
    }
    scalarNames = new ArrayList<String>();
    for (String s : dataNames) {
      if (getGridByName(s) != null) {
        hasScalars = true;
        scalarNames.add(s);
      }
    }
  }

  public double[] getFullLats() {
    return fullLats;
  }

  public double[] getFullLons() {
    return fullLons;
  }

  public String getScalarDescriptionByName(String varName) {
    GeoGrid g = getGridByName(varName);
    if (g != null) {
      return g.getDescription();
    }
    return null;
  }

  public double[] getScalarDataByName(long t, int levelIndex, String varName) {
    GeoGrid v = getGridByName(varName);
    if (v != null) {
      if (t >= getStartTime() & t <= getEndTime()) {
        try {
          int tIndex = getTimeIndex(t);

          return (double[]) v.readDataSlice(tIndex, levelIndex, -1, -1).get1DJavaArray(double.class);
        } catch (IOException ex) {
          Logger.getLogger(NcomReader.class.getName()).log(Level.SEVERE, null, ex);
        }
      }
    }

    return null;
  }

  public double[] getUVals(double depth) {
    return getUVals(this.getStartTime(), depth);
  }

  public double[] getUVals(long time, double depth) {
    int indexT = getTimeIndex(time);
    int indexZ = getZIndexAtValue(depth);

    double[] uVals = new double[]{Double.NaN};
    if (indexT != -1) {// & indexZ != -1){
      try {
        Array a = gU.readYXData(indexT, indexZ).reduce();
        uVals = (double[]) a.get1DJavaArray(double.class);
      } catch (IOException ex) {
        ex.printStackTrace();
      }
    } else {
      System.err.println("GenericGridReader:time or depth index invalid");
    }

    return uVals;
  }

  public double[] getVVals(double depth) {
    return getVVals(this.getStartTime(), depth);
  }

  public double[] getVVals(long time, double depth) {
    int indexT = getTimeIndex(time);
    int indexZ = getZIndexAtValue(depth);

    double[] vVals = new double[]{Double.NaN};
    if (indexT != -1) {// & indexZ != -1){
      try {
        Array a = gV.readYXData(indexT, indexZ).reduce();
        vVals = (double[]) a.get1DJavaArray(double.class);
      } catch (IOException ex) {
        ex.printStackTrace();
      }
    } else {
      System.err.println("NcomReader:time or depth index invalid");
    }

    return vVals;
  }

  public int getYIndexAtValue(double val) {
    return super.getYIndexAtValue(val);
  }

  public int getXIndexAtValue(double val) {
    return super.getXIndexAtValue(val);
  }

  public int getZIndexAtValue(double val) {
    return super.getZIndexAtValue(val);
  }

  public List<String> getScalarNames() {
    return scalarNames;
  }

  public boolean isHasUV() {
    return hasUV;
  }
}
