/*
 * Applied Science Associates, Inc.
 * Copyright 2009. All Rights Reserved.
 *
 * AssetReader.java
 *
 * Created on Jan 9, 2009 @ 11:37:40 AM
 */
package com.asascience.openmap.layer.nc.ncell;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import com.asascience.utilities.BinarySearch;

import ucar.nc2.Attribute;
import ucar.nc2.Dimension;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;

/**
 * 
 * @author CBM <cmueller@asascience.com>
 */
public class AssetReader {

  private static final String GONOGO = "GoNoGo";
  private NetcdfFile ncfile = null;
  private int nCells;
  private Variable goNoGoVar = null;
  private Variable assetVar = null;
  private Variable latVar = null;
  private Variable lonVar = null;
  private Variable timeVar = null;
  private double[] lats;
  private double[] lons;
  private double[][] latGrid;
  private double[][] lonGrid;
  private boolean timeDimFirst = true;
  private double[] times;
  private long[] timeSteps;
  private int fillValue;
  private double latCellSize;
  private double lonCellSize;
  private int[] goNoGos;
  private NcellLookup ncellLookup;
  private BinarySearch binSearch;

  public AssetReader(String ncFile) {
    try {
      File f = new File(ncFile);
      if (!f.exists()) {
        throw new Exception("File Does Not Exist");
      }

      binSearch = new BinarySearch();

      ncfile = NetcdfFile.open(ncFile);
      if (ncfile != null) {
        nCells = ncfile.findVariable("ncells").getShape()[0];
        latVar = ncfile.findVariable("lat");
        lonVar = ncfile.findVariable("lon");
        timeVar = ncfile.findVariable("time");// minutes from 2000-01-01
        // 00:00 - integer

        goNoGoVar = ncfile.findVariable("GoNoGo");
        assetVar = ncfile.findVariable("asset");

        Attribute fillAtt = goNoGoVar.findAttribute("_FillValue");
        if (fillAtt != null) {
          fillValue = fillAtt.getNumericValue().intValue();
        }

        Dimension timeDim = ncfile.findDimension("time");
        if (timeDim == null) {
          timeDim = ncfile.findDimension("record");// nefis-type ncell
        }
        if (goNoGoVar.getDimension(0) != timeDim) {
          timeDimFirst = false;
        }

        if ((latVar == null) || (lonVar == null) || (timeVar == null) || (goNoGoVar == null)
                || (assetVar == null)) {
          throw new Exception("Error reading nc file");
        } else {
          // get the times, lats and lons
          extractTimes();
          lats = (double[]) latVar.read().get1DJavaArray(double.class);
          // latCellSize = lats[1] - lats[0];
          lons = (double[]) lonVar.read().get1DJavaArray(double.class);
          // lonCellSize = lons[1] - lons[0];
          /** Get the x & y cell sizes. */
          getCellSizes();
          /** Build the grid. */
          buildGrid();

          ncellLookup = new NcellLookup(lats, lons, nCells);
          // float[] flats =
          // (float[])latVar.read().copyTo1DJavaArray();
          // float[] flons =
          // (float[])lonVar.read().copyTo1DJavaArray();
          //
          // lats = new double[flats.length];
          // for(int i = 0; i < flats.length; i++)
          // lats[i] = flats[i];
          // lons = new double[flons.length];
          // for(int i = 0; i < flons.length; i++)
          // lons[i] = flons[i];
        }

        // //if there, get grid vars
        // xGridVar = ncfile.findVariable("xgrid");
        // if(xGridVar != null){
        // xGrid =
        // (double[])xGridVar.read().get1DJavaArray(double.class);
        // }else{
        // xGrid = null;
        // }
        // yGridVar = ncfile.findVariable("ygrid");
        // if(yGridVar != null){
        // yGrid =
        // (double[])yGridVar.read().get1DJavaArray(double.class);
        // }else{
        // yGrid = null;
        // }
        //
        // if(xGrid != null & yGrid != null){
        // hasScalars = true;
        // scalarVars = new ArrayList<Variable>();
        // for(Variable v : ncfile.getVariables()){
        // if((latVar != v) &
        // (lonVar != v) &
        // (timeVar != v) &
        // (levelVar != v) &
        // ((xGridVar == null) ? true : xGridVar != v) &
        // ((yGridVar == null) ? true : yGridVar != v) &
        // (ncfile.findVariable("ncells") != v)){
        // scalarVars.add(v);
        // }
        // }
        // }else{
        // hasScalars = false;
        // }
      } else {
        throw new Exception("ncFile is null");
      }
    } catch (IOException ex) {
      ex.printStackTrace();
    } catch (Exception ex) {
      ex.printStackTrace();
    } finally {
      // if(ncfile != null){
      // try {
      // ncfile.close();
      // } catch (IOException ex) {
      // ex.printStackTrace();
      // }
      // }
    }
  }

  private void getCellSizes() {
    double val;
    for (int i = 0; i < nCells - 1; i++) {
      val = lats[i + 1] - lats[i];
      if (val != 0) {
        latCellSize = val;
        break;
      }
    }
    for (int i = 0; i < nCells - 1; i++) {
      val = lons[i + 1] - lons[i];
      if (val != 0) {
        lonCellSize = val;
        break;
      }
    }
  }

  private void extractTimes() {
    try {
      times = (double[]) timeVar.read().copyTo1DJavaArray();
      timeSteps = new long[times.length];

      GregorianCalendar c = new GregorianCalendar(TimeZone.getTimeZone("UTC"));// Calendar.getInstance();
      for (int i = 0; i < times.length; i++) {
        // set the calendar = the asa reference time
        // remember that the month is a 0-11 value!!!
        c.set(2000, 0, 1, 0, 0, 0);// = 2000-01-01 00:00:00.000
        c.set(Calendar.MILLISECOND, 0);

        c.add(Calendar.MINUTE, (int) times[i]);
        timeSteps[i] = c.getTimeInMillis();
      }
    } catch (IOException ex) {
      ex.printStackTrace();
    }
  }

  /** New Method */
  public int getNcellIndex(double latPos, double lonPos) {
    return ncellLookup.getNcellIndexFromLatLon(latPos, lonPos);
  }

  public int getTimeIndex(long queryTime) {
    long increment = (timeSteps.length > 1) ? (long) ((timeSteps[1] - timeSteps[0]) * 0.5) : 0;
    int index = binSearch.longSearch(timeSteps, 0, timeSteps.length, queryTime, increment);
    return index;
  }

  public int getTimeIndex_(long queryTime) {
    int ret = -1;
    long qt = queryTime;

    for (int i = 0; i < timeSteps.length; i++) {
      if (qt <= timeSteps[timeSteps.length - 1]) {
        if (qt >= timeSteps[i]) {
          ret = i;
        }
      }
    }

    return ret;
  }

  public int[] getGoNoGoValues(long t, String asset) {
    try {
      if (t >= getStartTime() & t <= getEndTime()) {
        int tIndex = getTimeIndex(t);
        if (tIndex != -1) {
          int[] origin = new int[]{tIndex, 1, 0};
          int[] shape = new int[]{1, 1, nCells};

          // ensure the origin dimension order is correct
          if (!timeDimFirst) {
            origin = new int[]{0, 0, tIndex};
            shape = new int[]{nCells, 0, 1};
          }

          goNoGos = (int[]) goNoGoVar.read(origin, shape).get1DJavaArray(int.class);
          return goNoGos;
        }
      }
    } catch (Exception ex) {
      ex.printStackTrace();
    }
    return null;
  }

  public boolean buildGrid() {
    try {
      double dx = lonCellSize * 0.5;
      double dy = latCellSize * 0.5;

      latGrid = new double[nCells][4];
      lonGrid = new double[nCells][4];
      for (int i = 0; i < nCells; i++) {
        /** Lats */
        latGrid[i][0] = lats[i] - dy;
        latGrid[i][1] = lats[i] + dy;
        latGrid[i][2] = lats[i] + dy;
        latGrid[i][3] = lats[i] - dy;

        /** Lons */
        lonGrid[i][0] = lons[i] - dx;
        lonGrid[i][1] = lons[i] - dx;
        lonGrid[i][2] = lons[i] + dx;
        lonGrid[i][3] = lons[i] + dx;
      }
      return true;
    } catch (Exception ex) {
      ex.printStackTrace();
    }
    return false;
  }

  public long getStartTime() {
    return timeSteps[0];
  }

  public long getEndTime() {
    return timeSteps[timeSteps.length - 1];
  }

  public long getTimeIncrement() {
    if (timeSteps.length == 1) {
      return 0;
    }
    return (timeSteps[1] - timeSteps[0]);
  }

  public double[] getXGridForNcell(int index) {
    return lonGrid[index];
  }

  public double[] getYGridForNcell(int index) {
    return latGrid[index];
  }

  public double[] getLats() {
    return lats;
  }

  public double[] getLons() {
    return lons;
  }

  public long[] getTimeSteps() {
    return timeSteps;
  }

  public int getNCells() {
    return nCells;
  }

  public void setNCells(int nCells) {
    this.nCells = nCells;
  }

  public int getFillValue() {
    return fillValue;
  }

  public void dispose() {
    if (ncfile != null) {
      try {
        ncfile.close();
      } catch (IOException ex) {
        ex.printStackTrace();
      }
    }
  }
}
