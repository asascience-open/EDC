/*
 * NcGridReader.java
 *
 * Created on October 31, 2007, 10:02 AM
 *
 * Applied Science Associates, Inc.
 * Copyright 2007.  All rights reserved.
 */
package com.asascience.openmap.layer.nc.grid;

import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Formatter;

import ucar.nc2.NetcdfFile;
import ucar.nc2.VariableSimpleIF;
import ucar.nc2.dataset.CoordinateAxis;
import ucar.nc2.dataset.CoordinateAxis1D;
import ucar.nc2.dataset.CoordinateAxis1DTime;
import ucar.nc2.dataset.NetcdfDataset;
import ucar.nc2.dt.GridCoordSystem;
import ucar.nc2.ft.FeatureDatasetFactoryManager;
import ucar.nc2.dt.grid.GeoGrid;
import ucar.nc2.dt.grid.GridDataset;
import ucar.unidata.geoloc.LatLonRect;
import ucar.nc2.dt.GridDatatype;
import ucar.nc2.util.CancelTask;
import ucar.nc2.constants.FeatureType;

import com.asascience.utilities.Utils;

/**
 * 
 * @author CBM
 */
public class NcGridReader {

  private NetcdfFile ncfile;
  private GridDataset gridDataset;
  private LatLonRect bounds;
  private Date startDate;
  private Date endDate;
  private Date[] dates;
  private long startTime;
  private long endTime;
  private long[] times;
  private long timeIncrement;
  private double fillValue = Double.NaN;
  protected List<GridDatatype> geoGrids = null;
  protected List<VariableSimpleIF> sVars = null;
  // these next three should/could be doubles - but openmap only accepts
  // floats for it's lat/lon values
  private double[] zValues;
  private double[] xValues;
  private double[] yValues;
  protected double[] fullLats;
  protected double[] fullLons;
  protected boolean hasScalars = false;
  protected List<String> scalarNames;
  protected boolean isRegularSpatial = false;
  protected double gridDx = Double.NaN;
  protected double gridDy = Double.NaN;
  protected String uvUnits = null;

  /**
   * Creates a new instance of NcGridReader
   *
   * @param ncPath
   */
  public NcGridReader(String ncPath) {
    loadFile(ncPath);
  }

  public void reInitialize(String ncPath) {
    loadFile(ncPath);
  }

  private void loadFile(String ncPath) {
    try {
      setNcfile(NetcdfDataset.acquireDataset(ncPath, null));
      Formatter errlog = new Formatter();
      CancelTask cancelTask = null;
      gridDataset = (GridDataset) FeatureDatasetFactoryManager.open(FeatureType.GRID, getNcfile().getLocation(), cancelTask, errlog);
      //gridDataset = GridDataset.open(ncPath);

      if (gridDataset != null) {
        initialize();
      } else {
        System.err.println("NcGridReader:GridDataset = null");
      }

    } catch (IOException ex) {
      ex.printStackTrace();
    }
  }

  private void initialize() {
    bounds = gridDataset.getBoundingBox();

    sVars = gridDataset.getDataVariables();

    geoGrids = gridDataset.getGrids();

    // if there are any geo grids
    if (!geoGrids.isEmpty()) {
      // get the first grid and determine time stuff
      int gi = 0;
      GridDatatype g;
      do {
        g = geoGrids.get(gi++);
        if (gi == geoGrids.size()) {
          break;
        }
      } while (g.getDimensions().size() != gridDataset.getNetcdfDataset().getDimensions().size());

      GridCoordSystem gcs;
      CoordinateAxis1DTime timeAxis;
      gcs = g.getCoordinateSystem();
      isRegularSpatial = gcs.isRegularSpatial();
      // gcs.get
      if (gcs.hasTimeAxis1D()) {
        startDate = gridDataset.getStartDate();
        // System.err.println(startDate.toString());
        startTime = startDate.getTime();
        // System.err.println(startTime);
        endDate = gridDataset.getEndDate();
        endTime = endDate.getTime();
        timeAxis = gcs.getTimeAxis1D();
        dates = timeAxis.getTimeDates();
        times = new long[dates.length];
        // set the times
        for (int i = 0; i < dates.length; i++) {
          times[i] = dates[i].getTime();
        }
        // set the time increment **this is really all that's needed
        // along with the
        // start time and number of timesteps**
        double inc = timeAxis.getIncrement();
        String units = timeAxis.getUnitsString();
        timeIncrement = Utils.getTimeInMilliseconds(inc, units);
        // System.err.println("inc="+inc);
        // System.err.println("units="+units);
        // System.err.println("ti="+timeIncrement);
      } else {
        System.err.println("NcGridReader:Time Axis is null");
        times = new long[1];
        times[0] = 0;
        startTime = 0;
        endTime = 0;
        timeIncrement = 0;
      }
      CoordinateAxis1D vert = gcs.getVerticalAxis();
      if (vert != null) {
        try {
          zValues = (double[]) vert.read().get1DJavaArray(double.class);
        } catch (IOException ex) {
          ex.printStackTrace();
        }
      } else {
        zValues = new double[]{Double.NaN};
        System.err.println("NcGridReader:Vertical Axis is null");
      }
      CoordinateAxis xAxis = gcs.getXHorizAxis();
      if (xAxis != null) {
        try {
          xValues = (double[]) xAxis.read().get1DJavaArray(double.class);
        } catch (IOException ex) {
          ex.printStackTrace();
        }
      } else {
        xValues = new double[]{Float.NaN};
        System.err.println("NcGridReader:X Axis is null");
      }
      CoordinateAxis yAxis = gcs.getYHorizAxis();
      if (yAxis != null) {
        try {
          yValues = (double[]) yAxis.read().get1DJavaArray(double.class);
        } catch (IOException ex) {
          ex.printStackTrace();
        }
      } else {
        yValues = new double[]{Float.NaN};
        System.err.println("NcGridReader:Y Axis is null");
      }
    }

    if (xValues.length != 1 & yValues.length != 1) {
      fullLats = new double[yValues.length * xValues.length];
      fullLons = new double[yValues.length * xValues.length];
      int x = 0;
      for (int i = 0; i < yValues.length; i++) {
        for (int j = 0; j < xValues.length; j++) {
          fullLats[x] = yValues[i];
          fullLons[x] = xValues[j];
          x++;
        }
      }
    }

    initGridCellInfo();

    // //values checks
    // System.err.println("times");
    // for(long t : times){
    // System.err.println(t);
    // }
    // System.err.println("depths");
    // for(double d : zValues){
    // System.err.println(d);
    // }
    // System.err.println("lons");
    // for(double d : xValues){
    // System.err.println(d);
    // }
    // System.err.println("lats");
    // for(double d : yValues){
    // System.err.println(d);
    // }
  }

  public void initGridCellInfo() {
    if (!isRegularSpatial) {
      return;
    }
    if (yValues == null & xValues == null) {
      return;
    }

    gridDx = Math.abs(xValues[1] - xValues[0]);
    gridDy = Math.abs(yValues[1] - yValues[0]);
  }

  public double[][] calculateGridCellAxes(double lat, double lon) {
    if (Double.isNaN(gridDx) | Double.isNaN(gridDy)) {
      return null;
    }

    double[][] ret = new double[2][4];

    try {
      ret[0][0] = lat - gridDy;// left
      ret[0][1] = lat + gridDy;// right
      ret[0][2] = lat + gridDy;// right
      ret[0][3] = lat - gridDy;// left

      ret[1][0] = lon + gridDx;// top
      ret[1][1] = lon + gridDx;// top
      ret[1][2] = lon - gridDx;// bottom
      ret[1][3] = lon - gridDx;// bottom

    } catch (Exception ex) {
      ret = null;
    }

    return ret;
  }

  public int getTimeIndex(long time) {
    if (times.length == 1) {
      return 0;
    }
    for (int i = 0; i < times.length -1; i++) {
      if (time <= times[i + 1]) {
        // if(i == 0) return i;
        //
        // return i-1;
        return i;
      }
    }

    return -1;
  }

  protected int getYIndexAtValue(double val) {
    return getIndexAtValue(val, yValues);
  }

  protected int getXIndexAtValue(double val) {
    return getIndexAtValue(val, xValues);
  }

  protected int getZIndexAtValue(double val) {
    return getIndexAtValue(val, zValues);
  }

  protected int getIndexAtValue(double val, double[] inVals) {
    int ret = -1;

    double[] vals = inVals.clone();
    Arrays.sort(vals);

    double checkVal;
    for (int i = 0; i < vals.length; i++) {
      checkVal = (vals[i] > 180) ? vals[i] - 360 : vals[i];
      if (val <= checkVal) {
        return i;
      }
    }

    return ret;
  }

  // <editor-fold defaultstate="collapsed" desc=" Old Methods ">
  // public int getXIndex(double xVal) {
  // if(xValues.length == 1){
  // return 0;
  // }
  // for(int i = 0; i < xValues.length; i++){
  // if(xVal <= xValues[i + 1]){
  // // if(i == 0) return i;
  // //
  // // return i-1;
  // return i;
  // }
  // }
  //
  // return -1;
  // }
  //
  // public int getYIndex(double yVal) {
  // if(yValues.length == 1){
  // return 0;
  // }
  // for(int i = 0; i < yValues.length; i++){
  // if(yVal <= yValues[i + 1]){
  // // if(i == 0) return i;
  // //
  // // return i-1;
  // return i;
  // }
  // }
  //
  // return -1;
  // }
  //
  // public int getZIndex(double zVal) {
  // if(zValues.length == 1){
  // return 0;
  // }
  // for(int i = 0; i < zValues.length; i++){
  // if(zVal <= zValues[i + 1]){
  // // if(i == 0) return i;
  // //
  // // return i-1;
  // return i;
  // }
  // }
  //
  // return -1;
  // }
  // </editor-fold>
  // public double[] getData(String varName, double depth, long time){
  // double[] retData = new double[]{Double.NaN};
  //
  // GeoGrid dataGrid = getGridByName(varName);
  // if(dataGrid != null){
  //
  // }
  //
  // return retData;
  // }
  public GeoGrid getGridByName(String varName) {
    return gridDataset.findGridByName(varName);
  }

  public NetcdfFile getNcfile() {
    return ncfile;
  }

  public void setNcfile(NetcdfFile ncfile) {
    this.ncfile = ncfile;
  }

  public GridDataset getGridDataset() {
    return gridDataset;
  }

  public LatLonRect getBounds() {
    return bounds;
  }

  public Date getStartDate() {
    return startDate;
  }

  public Date getEndDate() {
    return endDate;
  }

  public Date[] getDates() {
    return dates;
  }

  public long getStartTime() {
    return startTime;
  }

  public long getEndTime() {
    return endTime;
  }

  public long[] getTimes() {
    return times;
  }

  public long getTimeIncrement() {
    return timeIncrement;
  }

  public List<GridDatatype> getGeoGrids() {
    return geoGrids;
  }

  public List<VariableSimpleIF> getSVars() {
    return sVars;
  }

  public double[] getZValues() {
    return zValues;
  }

  public double[] getXValues() {
    return xValues;
  }

  public double[] getYValues() {
    return yValues;
  }

  public boolean isHasScalars() {
    return hasScalars;
  }

  public void setHasScalars(boolean hasScalars) {
    this.hasScalars = hasScalars;
  }

  public double getFillValue() {
    return fillValue;
  }

  public void setFillValue(double fillValue) {
    this.fillValue = fillValue;
  }

  public String getUvUnits() {
    return uvUnits;
  }
}
