/*
 * DataFileNcGrid.java
 *
 * Created on November 12, 2007, 10:47 AM
 *
 * Applied Science Associates, Inc.
 * Copyright 2007.  All rights reserved.
 */
package com.asascience.edp.datafile.hydro;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.TimeZone;

import ucar.ma2.Array;
import ucar.ma2.Index;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;
import ucar.nc2.VariableSimpleIF;
import ucar.nc2.dataset.CoordinateAxis1D;
import ucar.nc2.dataset.CoordinateAxis1DTime;
import ucar.nc2.dt.grid.GeoGrid;

import com.asascience.utilities.Vector3D;

/**
 * 
 * @author CBM
 */
public class DataFileNcGrid extends DataFileBase {

  protected NetcdfFile ncfile;
  protected double[] xVals, yVals, zVals;
  protected long[] tVals;
  protected List<VariableSimpleIF> sVars = null;
  protected List<GeoGrid> geoGrids = null;
  protected CoordinateAxis1D xAxis = null, yAxis = null, zAxis = null;
  protected CoordinateAxis1DTime timeAxis = null;
  protected GeoGrid uGrid = null;
  protected GeoGrid vGrid = null;
  protected GeoGrid tGrid = null;
  protected GeoGrid sGrid = null;
  protected GeoGrid seGrid = null;
  protected GeoGrid bdGrid = null;

  /**
   * Creates a new instance of DataFileNcGrid
   */
  public DataFileNcGrid() {
  }

  protected long[] collectTimes(Date[] dateTimes) {

    long[] ret = new long[dateTimes.length];

    GregorianCalendar cal = new GregorianCalendar(TimeZone.getTimeZone("UTC"));// Calendar.getInstance();
    for (int i = 0; i < dateTimes.length; i++) {
      cal.setTime(dateTimes[i]);
      ret[i] = cal.getTimeInMillis();
      // System.out.println("Time in Millis:"+ret[i]);
      // if(i > 0)
      // System.out.println(ret[i] - ret[i - 1]);
    }
    return ret;
  }

  protected int getTimeIndexAtValue() {
    return getTimeIndexAtValue(this.getQueryTime());
  }

  protected int getTimeIndexAtValue(long val) {
    int ret = -1;
    // if val is > startTime and < endTime then
    // cycle through and find the highest index < val...
    if (val >= tVals[0]) {
      if (val <= tVals[tVals.length - 1]) {
        for (int i = 0; i < tVals.length; i++) {
          // System.out.println(i);
          // System.out.println(val + " | " + tVals[i]);
          if (val == tVals[i]) {
            return i;
          } else if (val < tVals[i]) {
            return i - 1;
          }
        }
      }
    }

    return ret;
  }

  protected int getYIndexAtValue(double val) {
    return getIndexAtValue(val, yVals);
  }

  protected int getXIndexAtValue(double val) {
    if (xVals[1] > 180 & Math.abs(val) < 180) {
      val = val + 360;
    }
    return getIndexAtValue(val, xVals);
  }

  protected int getZIndexAtValue(double val) {
    return getIndexAtValue(val, zVals);
  }

  protected int getIndexAtValue(double val, double[] vals) {
    int ret = -1;

    for (int i = 0; i < vals.length; i++) {
      if (val <= vals[i]) {
        return i;
      }
    }

    return ret;
  }

  @Override
  public Double[] getLons() {
    List<Double> dbls = new ArrayList<Double>();
    for (double d : xVals) {
      if (!dbls.contains(d)) {
        dbls.add(d);
      }
    }
    return (Double[]) dbls.toArray(new Double[0]);
  }

  @Override
  public Double[] getLats() {
    List<Double> dbls = new ArrayList<Double>();
    for (double d : yVals) {
      if (!dbls.contains(d)) {
        dbls.add(d);
      }
    }
    return (Double[]) dbls.toArray(new Double[0]);
  }

  @Override
  public double[][] getUVTimeSeries(Vector3D position) {
    double[][] retVals = new double[2][tVals.length];
    try {
      int latIndex = getYIndexAtValue(position.getV());
      int lonIndex = getXIndexAtValue(position.getU());
      int depthIndex = getZIndexAtValue(position.getW());
      int tIndex;
      double[] uvVals;
      for (int i = 0; i < tVals.length; i++) {
        tIndex = getTimeIndexAtValue(tVals[i]);
        uvVals = getUVComponentAt(tIndex, depthIndex, latIndex, lonIndex);
        retVals[0][i] = uvVals[0];
        retVals[1][i] = uvVals[1];
      }

      return retVals;
    } catch (Exception ex) {
      ex.printStackTrace();
    }
    return null;
  }

  public double[] getUVComponentAt(int tIndex, int depthIndex, int latIndex, int lonIndex) {
    double[] ret = new double[2];
    int[] origin = new int[]{tIndex, depthIndex, latIndex, lonIndex};
    int[] size = new int[]{1, 1, 1, 1};
    try {
      if (originOk(origin)) {
        Variable uvar = (Variable) uGrid.getVariable();
        Variable vvar = (Variable) vGrid.getVariable();

        Array au = uvar.read(origin, size);
        Array av = vvar.read(origin, size);

        Index ima = au.getIndex();
        ima.set(0, 0, 0, 0);

        ret[0] = au.getDouble(ima);
        ret[1] = av.getDouble(ima);
      }

      return ret;
    } catch (Exception ex) {
      ex.printStackTrace();
    }
    return null;
  }

  protected int[] getOrigin(Vector3D position) {
    int time = this.getTimeIndexAtValue();// timeAxis.findTimeIndexFromDate(getQueryTime());
    int z = this.getZIndexAtValue(position.getW());
    int y = this.getYIndexAtValue(position.getV());
    int x = this.getXIndexAtValue(position.getU());

    // System.out.println("Time: " + getQueryTime() +
    // " X:"+position.getU()+" Y:"+position.getV());
    // System.out.println("Indexes: t="+time+" x=" + x + " y=" + y);
    return new int[]{time, z, y, x};
  }

  protected int[] getBottomDepthOrigin(Vector3D position) {
    int x = this.getXIndexAtValue(position.getU());
    int y = this.getYIndexAtValue(position.getV());
    return new int[]{y, x};
  }

  protected boolean originOk(int[] ori) {
    // boolean ret = true;

    for (int i : ori) {
      if (i == -1) {
        return false;
      }
    }
    return true;

    // if(ori[0] == -1){
    // ret = false;
    // }
    // if(ori[1] == -1){
    // ret = false;
    // }
    // if(ori[2] == -1){
    // ret = false;
    // }
    // if(ori[3] == -1){
    // ret = false;
    // }
    // return ret;
  }

  // no longer abstract - simply overridden in file-specific classes
  // abstract method to load file-specific data
  // protected abstract void loadDataFile();
  public Vector3D getCurrentAt(Vector3D position) {
    Vector3D v = null;
    try {
      int[] origin = getOrigin(position);
      if (originOk(origin)) {
        int[] size = new int[]{1, 1, 1, 1};

        Variable uvar = (Variable) uGrid.getVariable();
        Variable vvar = (Variable) vGrid.getVariable();

        Array au = uvar.read(origin, size);
        Array av = vvar.read(origin, size);

        Index ima = au.getIndex();
        ima.set(0, 0, 0, 0);

        double du = au.getDouble(ima);
        double dv = av.getDouble(ima);
        // System.out.println(au.toString());
        v = new Vector3D(du, dv, 0);
      }
    } catch (IOException ex) {
      ex.printStackTrace();
    } catch (InvalidRangeException ex) {
      ex.printStackTrace();
    }

    return v;
  }

  public double getSalinityAt(Vector3D position) {
    double ds = -999;
    try {
      int[] origin = getOrigin(position);
      if (originOk(origin)) {
        int[] size = new int[]{1, 1, 1, 1};

        Variable svar = (Variable) sGrid.getVariable();
        Array as = svar.read(origin, size);

        Index ima = as.getIndex();
        ima.set(0, 0, 0, 0);

        ds = as.getDouble(ima);
      }
    } catch (IOException ex) {
      ex.printStackTrace();
    } catch (InvalidRangeException ex) {
      ex.printStackTrace();
    }

    return ds;
  }

  public double getTempAt(Vector3D position) {
    double dt = -999;
    try {
      int[] origin = getOrigin(position);
      if (originOk(origin)) {
        int[] size = new int[]{1, 1, 1, 1};

        Variable tvar = (Variable) tGrid.getVariable();
        Array at = tvar.read(origin, size);

        Index ima = at.getIndex();
        ima.set(0, 0, 0, 0);

        dt = at.getDouble(ima);
      }
      return dt;
    } catch (IOException ex) {
      ex.printStackTrace();
    } catch (InvalidRangeException ex) {
      ex.printStackTrace();
    }

    return -999;
  }

  public double getBottomDepthAt(Vector3D position) {
    double dt = -999;
    try {
      int[] origin = getBottomDepthOrigin(position);
      if (originOk(origin)) {
        int[] size = new int[]{1, 1};

        Variable tvar = (Variable) bdGrid.getVariable();

        dt = ((double[]) tvar.read(origin, size).get1DJavaArray(double.class))[0];
        // Array at = tvar.read(origin, size);
        //
        // Index ima = at.getIndex();
        // ima.set(0, 0, 0, 0);
        //
        // dt = at.getDouble(ima);
      }
      return dt;
    } catch (Exception ex) {
      ex.printStackTrace();
    }
    return -999;
  }

  public void disposeAll() {
    try {
      if (ncfile != null) {
        ncfile.close();
      }
    } catch (IOException ex) {
      ex.printStackTrace();
    }
  }
}
