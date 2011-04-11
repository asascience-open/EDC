/*
 * NcellReader.java
 *
 * Created on December 12, 2007, 10:04 AM
 *
 * Applied Science Associates, Inc.
 * Copyright 2007.  All rights reserved.
 */
package com.asascience.openmap.layer.nc.ncell;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;

import ucar.ma2.InvalidRangeException;
import ucar.nc2.Attribute;
import ucar.nc2.Dimension;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;
import ucar.nc2.units.DateUnit;
import ucar.nc2.units.TimeUnit;

import com.asascience.utilities.BinarySearch;
import com.asascience.utilities.Vector3D;

/**
 * 
 * @author CBM
 */
public class NcellReader {

  private NetcdfFile ncfile = null;
  private int nCells;
  private Variable uVar = null;
  private Variable vVar = null;
  private Variable latVar = null;
  private Variable lonVar = null;
  private Variable timeVar = null;
  private Variable levelVar = null;
  private Variable xGridVar = null;
  private Variable yGridVar = null;
  private List<Variable> scalarVars = null;
  private double[] lats;
  private double[] lons;
  private int numTimes;
  private long[] times;
  private double[] us;
  private double[] vs;
  private double[] xGrid;
  private double[] yGrid;
  private boolean timeDimFirst = true;
  private boolean hasScalars = false;
  private double fillValue;
  private String uvUnits = null;
  private NcellLookup ncellLookup;
  private BinarySearch binSearch;
  private TriangulationLookup trigLookup;

  /**
   * Creates a new instance of NcellReader
   *
   * @param ncFile
   */
  public NcellReader(String ncFile) {
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
        levelVar = ncfile.findVariable("level");

        uVar = ncfile.findVariable("U");
        if (uVar == null) {
          uVar = ncfile.findVariable("wind_u");
        }
        vVar = ncfile.findVariable("V");
        if (vVar == null) {
          vVar = ncfile.findVariable("wind_v");
        }

        Attribute fillAtt = vVar.findAttribute("_FillValue");
        if (fillAtt != null) {
          fillValue = fillAtt.getNumericValue().doubleValue();
        }

        Attribute unitAtt = vVar.findAttribute("units");
        if (unitAtt != null) {
          uvUnits = unitAtt.getStringValue();
        }
        // fillValue =
        // vVar.findAttribute("_FillValue").getNumericValue().
        // doubleValue();

        Dimension timeDim = ncfile.findDimension("time");
        if (timeDim == null) {
          timeDim = ncfile.findDimension("record");// nefis-type ncell
        }
        if (uVar.getDimension(0) != timeDim) {
          timeDimFirst = false;
        }

        if ((latVar == null) || (lonVar == null) || (timeVar == null) || (uVar == null) || (vVar == null)) {
          throw new Exception("Error reading nc file");
        } else {
          // get the times, lats and lons
          extractTimes();
          lats = (double[]) latVar.read().get1DJavaArray(double.class);
          lons = (double[]) lonVar.read().get1DJavaArray(double.class);

          // ncellLookup = new NcellLookup(lats, lons, nCells);
          trigLookup = new TriangulationLookup(lats, lons);
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

        // if there, get grid vars
        xGridVar = ncfile.findVariable("xgrid");
        if (xGridVar != null) {
          xGrid = (double[]) xGridVar.read().get1DJavaArray(double.class);
        } else {
          xGrid = null;
        }
        yGridVar = ncfile.findVariable("ygrid");
        if (yGridVar != null) {
          yGrid = (double[]) yGridVar.read().get1DJavaArray(double.class);
        } else {
          yGrid = null;
        }

        if (xGrid != null & yGrid != null) {
          hasScalars = true;
          scalarVars = new ArrayList<Variable>();
          for (Variable v : ncfile.getVariables()) {
            if ((latVar != v) & (lonVar != v) & (timeVar != v) & (levelVar != v)
                    & ((xGridVar == null) ? true : xGridVar != v) & ((yGridVar == null) ? true : yGridVar != v)
                    & (ncfile.findVariable("ncells") != v)) {
              scalarVars.add(v);
            }
          }
        } else {
          hasScalars = false;
        }
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

  public void dispose() {
    if (ncfile != null) {
      try {
        ncfile.close();
      } catch (IOException ex) {
        ex.printStackTrace();
      }
    }
  }

  private void extractTimes() {
    try {
      /** New Method - does not rely on a "fixed" time origin. */
      double[] ts = (double[]) timeVar.read().get1DJavaArray(double.class);
      numTimes = ts.length;
      times = new long[ts.length];
      SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss.sss Z");
      sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
      GregorianCalendar gc = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
      DateUnit du = new DateUnit(timeVar.getUnitsString());
      gc.setTime(du.getDate());
      TimeUnit tu = du.getTimeUnit();
      long origin = gc.getTimeInMillis();
      long timestep = (long) (tu.getValueInSeconds() * 1000);// timestep
      // in ms
      for (int i = 0; i < ts.length; i++) {
        times[i] = (long) (origin + (ts[i] * timestep));
        // gc.setTimeInMillis(times[i]);
        // System.out.println(sdf.format(gc.getTime()));
      }

      // times = (int[])timeVar.read().copyTo1DJavaArray();
      // timeSteps = new long[times.length];
      //
      // GregorianCalendar c = new
      // GregorianCalendar(TimeZone.getTimeZone("UTC"));//Calendar.getInstance();
      // for(int i = 0; i < times.length; i++){
      // //set the calendar = the asa reference time
      // //remember that the month is a 0-11 value!!!
      // c.set(2000, 0, 1, 0, 0, 0);// = 2000-01-01 00:00:00.000
      // c.set(Calendar.MILLISECOND, 0);
      //
      // c.add(Calendar.MINUTE, times[i]);
      // timeSteps[i] = c.getTimeInMillis();
      // }
    } catch (IOException ex) {
      ex.printStackTrace();
    } catch (Exception e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  /** Newest Method */
  public int getNcellIndex(double latPos, double lonPos) {
    return trigLookup.getNcellIndexFromLatLon(latPos, lonPos);
  }

  /** New Method */
  public int newOld_getNcellIndex(double latPos, double lonPos) {
    return ncellLookup.getNcellIndexFromLatLon(latPos, lonPos);
  }

  /** Old Method */
  public int getNcellIndex_old(double latPos, double lonPos) {
    int ret = -1;
    List<Integer> latList = latIndexes(latPos);
    List<Integer> lonList = lonIndexes(lonPos);
    if (latList == null | lonList == null) {
      return -1;
    }

    // System.out.println("Lats: (" + latPos + ")");
    // for(int i : latList){
    // System.out.println("  " + i);
    // }
    // System.out.println("Lons: (" + lonPos + ")");
    // for(int i : lonList){
    // System.out.println("  " + i);
    // }

    for (int i : latList) {
      for (int j : lonList) {
        if (i == j) {
          ret = j;
          break;
        }
      }
    }

    return ret;
  }

  private List<Integer> lonIndexes(double lonPos) {
    List<Integer> ret = new ArrayList<Integer>();

    double[] lons2 = (double[]) lons.clone();
    java.util.Arrays.sort(lons2);

    double target = Double.NaN;

    for (int i = 0; i < lons2.length; i++) {
      if (lonPos >= lons2[i]) {
        // if i is not the last lat
        if (i != lons2.length - 1) {
          if (lonPos == lons2[i + 1]) {
            target = lons2[i + 1];
            break;
          } else if (lonPos <= lons2[i + 1]) {
            target = lons2[i];
            break;
          }
        } else {// i is the last lat
          if (lonPos <= (lons2[i] + (lons2[i] - lons2[i - 1]))) {
            target = lons2[i];
            break;
          }
        }
      }
    }

    // gather all the indexes that match the target value
    for (int i = 0; i < lons.length; i++) {
      if (lons[i] == target) {
        ret.add(i);
        // System.err.println(i+" ="+lats[i]);
      }
    }

    return ret;
  }

  private List<Integer> latIndexes(double latPos) {
    List<Integer> ret = new ArrayList<Integer>();

    double[] lats2 = (double[]) lats.clone();
    java.util.Arrays.sort(lats2);

    double target = Double.NaN;

    for (int i = 0; i < lats2.length; i++) {
      if (latPos >= lats2[i]) {
        // if i is not the last lat
        if (i != lats2.length - 1) {
          if (latPos == lats2[i + 1]) {
            target = lats2[i + 1];
            break;
          } else if (latPos <= lats2[i + 1]) {
            target = lats2[i];
            break;
          }
        } else {// i is the last lat
          if (latPos <= (lats2[i] + (lats2[i] - lats2[i - 1]))) {
            target = lats2[i];
            break;
          }
        }
      }
    }

    // gather all the indexes that match the target value
    for (int i = 0; i < lats.length; i++) {
      if (lats[i] == target) {
        ret.add(i);
        // System.err.println(i+" ="+lats[i]);
      }
    }

    return ret;
  }

  private List<Integer> lonIndexes2(double lonPos) {
    List<Integer> lonList = new ArrayList<Integer>();

    double[] lons2 = (double[]) lons.clone();
    java.util.Arrays.sort(lons2);
    double delta = lons2[1] - lons2[0];
    int del = 1;
    while (delta == 0 & del < lons2.length - 1) {
      delta = lons2[del + 1] - lons2[del];
      del++;
    }

    if (delta == 0) {
      return null;
    }

    double target = Double.NaN;

    // cycle through to find the target value
    for (int i = 0; i < lons2.length - 1; i++) {
      if (lonPos >= lons2[i] & lonPos <= (lons2[i] + delta)) {
        target = lons2[i];
        break;
      }
    }

    if (Double.isNaN(target)) {
      return null;
    }

    // gather all the indexes that match the target value
    for (int i = 0; i < lons.length - 1; i++) {
      if (lons[i] == target) {
        lonList.add(i);
        // System.err.println(i+" ="+lons[i]);
      }

    }
    return lonList;
  }

  private List<Integer> latIndexes2(double latPos) {
    List<Integer> latList = new ArrayList<Integer>();

    double[] lats2 = (double[]) lats.clone();
    java.util.Arrays.sort(lats2);
    double delta = lats2[1] - lats2[0];// assumes that the lat lon increment
    // is even!!!
    int del = 1;
    while (delta == 0 & del < lats2.length - 1) {
      delta = lats2[del + 1] - lats2[del];
      del++;
    }

    if (delta == 0) {
      return null;
    }

    // System.err.println("latlist");
    double target = Double.NaN;

    // cycle through to find the target value
    for (int i = 0; i < lats2.length - 1; i++) {// !!This ALWAYS skips the
      // last one....
      if (latPos >= lats2[i] & latPos <= (lats2[i] + delta)) {
        target = lats2[i];
        break;
      }
    }

    if (Double.isNaN(target)) {
      return null;
    }

    // gather all the indexes that match the target value
    for (int i = 0; i < lats.length; i++) {
      if (lats[i] == target) {
        latList.add(i);
        // System.err.println(i+" ="+lats[i]);
      }
    }

    return latList;
  }

  public int getTimeIndex(long queryTime) {
    long increment = (times.length > 1) ? (long) ((times[1] - times[0]) * 0.5) : 0;
    int index = binSearch.longSearch(times, 0, times.length, queryTime, increment);
    return index;
  }

  public int getTimeIndex_(long queryTime) {
    int ret = -1;
    long qt = queryTime;

    for (int i = 0; i < times.length; i++) {
      if (qt <= times[times.length - 1]) {
        if (qt >= times[i]) {
          ret = i;
        }
      }
    }

    return ret;
  }

  public double getU(int timeIndex, int nCellIndex) {
    double ret = Double.NaN;
    try {

      int[] origin = new int[]{timeIndex, nCellIndex};
      // ensure the origin dimension order is correct
      if (!timeDimFirst) {
        origin = new int[]{nCellIndex, timeIndex};
      }
      int[] shape = new int[]{1, 1};

      if (levelVar != null) {// has level - nefis style
        // int lvl = 0;
        // if(level != -1){
        // if(level < levelVar.getShape()[0]){
        // }
        // }

        origin = new int[]{origin[0], 0, origin[1]};
        shape = new int[]{shape[0], 1, shape[1]};
      }

      double[] vals = (double[]) uVar.read(origin, shape).get1DJavaArray(double.class);

      if (vals.length == 1) {
        ret = vals[0];
      }
    } catch (IOException ex) {
      Logger.getLogger(NcellReader.class.getName()).log(Level.SEVERE, null, ex);
    } catch (InvalidRangeException ex) {
      Logger.getLogger(NcellReader.class.getName()).log(Level.SEVERE, null, ex);
    }

    return ret;
  }

  public double getV(int timeIndex, int nCellIndex) {
    double ret = Double.NaN;
    try {
      int[] origin = new int[]{timeIndex, nCellIndex};
      // ensure the origin dimension order is correct
      if (!timeDimFirst) {
        origin = new int[]{nCellIndex, timeIndex};
      }
      int[] shape = new int[]{1, 1};

      if (levelVar != null) {// has level - nefis style
        // int lvl = 0;
        // if(level != -1){
        // if(level < levelVar.getShape()[0]){
        // }
        // }

        origin = new int[]{origin[0], 0, origin[1]};
        shape = new int[]{shape[0], 1, shape[1]};
      }

      double[] vals = (double[]) vVar.read(origin, shape).get1DJavaArray(double.class);

      if (vals.length == 1) {
        ret = vals[0];
      }
    } catch (IOException ex) {
      Logger.getLogger(NcellReader.class.getName()).log(Level.SEVERE, null, ex);
    } catch (InvalidRangeException ex) {
      Logger.getLogger(NcellReader.class.getName()).log(Level.SEVERE, null, ex);
    }

    return ret;
  }

  public boolean getUVs(long t) {
    return getUVs(t, -1);
  }

  public boolean getUVs(long t, int level) {
    // make sure the time is valid for the dataset
    if (t >= getStartTime() & t <= getEndTime()) {
      int tIndex = getTimeIndex(t);
      // System.err.println("tIndex = "+tIndex);
      if (tIndex != -1) {
        try {
          // System.err.println("nCells = "+nCells);
          // System.err.println(uVar.toString());
          // System.err.println(vVar.toString());
          int[] origin = new int[]{tIndex, 0};
          int[] shape = new int[]{1, nCells};

          // ensure the origin dimension order is correct
          if (!timeDimFirst) {
            origin = new int[]{0, tIndex};
            shape = new int[]{nCells, 1};
          }

          if (levelVar != null) {// has level - nefis style
            // int lvl = 0;
            // if(level != -1){
            // if(level < levelVar.getShape()[0]){
            // }
            // }

            origin = new int[]{origin[0], 0, origin[1]};
            shape = new int[]{shape[0], 1, shape[1]};
          }

          // System.err.println(origin.length);
          // System.err.println(shape.length);

          us = (double[]) uVar.read(origin, shape).get1DJavaArray(double.class);
          vs = (double[]) vVar.read(origin, shape).get1DJavaArray(double.class);

          // for(int i = 0; i < us.length; i++){
          // System.err.println(us[i] + " " + vs[i]);
          // }
          return true;
        } catch (Exception ex) {
          ex.printStackTrace();
        }
      }
    }

    return false;
  }

  public long getStartTime() {
    return times[0];
  }

  public long getEndTime() {
    return times[times.length - 1];
  }

  public long getTimeIncrement() {
    if (times.length == 1) {
      return 0;
    }
    return (times[1] - times[0]);
  }

  public double[] getLats() {
    return lats;
  }

  public double[] getLons() {
    return lons;
  }

  public long[] getTimeSteps() {
    return times;
  }

  public double[] getUs() {
    return us;
  }

  public double[] getVs() {
    return vs;
  }

  public double[] getXGridForNcell(int ncell) {
    try {
      if (ncell >= 0 & ncell < nCells) {
        return (double[]) xGridVar.read(new int[]{ncell, 0}, new int[]{1, 4}).get1DJavaArray(double.class);
      }
    } catch (IOException ex) {
      Logger.getLogger(NcellReader.class.getName()).log(Level.SEVERE, null, ex);
    } catch (InvalidRangeException ex) {
      Logger.getLogger(NcellReader.class.getName()).log(Level.SEVERE, null, ex);
    }
    return null;
  }

  public double[] getYGridForNcell(int ncell) {
    try {
      if (ncell >= 0 & ncell < nCells) {
        return (double[]) yGridVar.read(new int[]{ncell, 0}, new int[]{1, 4}).get1DJavaArray(double.class);
      }
    } catch (IOException ex) {
      Logger.getLogger(NcellReader.class.getName()).log(Level.SEVERE, null, ex);
    } catch (InvalidRangeException ex) {
      Logger.getLogger(NcellReader.class.getName()).log(Level.SEVERE, null, ex);
    }
    return null;
  }

  public boolean isHasScalars() {
    return hasScalars;
  }

  public void setHasScalars(boolean hasGridInfo) {
    this.hasScalars = hasGridInfo;
  }

  public List<Variable> getScalarVars() {
    return scalarVars;
  }

  public double[][] getUVTimeSeries_old(Vector3D position) {
    double[][] retVals = null;
    try {
      retVals = new double[2][numTimes];
      int timeI, ncellI, counter = 0;
      double u, v;

      ncellI = getNcellIndex(position.getV(), position.getU());
      for (int i = 0; i < numTimes; i++) {
        timeI = getTimeIndex(times[i]);
        // ncellI = edsReader.getNcellIndex(lat, lon);//no need for this
        // to be inside the loop....
        if (timeI > -1 & ncellI > -1) {
          u = getU(timeI, ncellI);
          v = getV(timeI, ncellI);
          retVals[0][i] = u;
          retVals[1][i] = v;

          if (!Double.isNaN(retVals[0][i]) || !Double.isNaN(retVals[1][i])) {// was
            // &
            counter++;
          }
        } else {
          retVals[0][i] = Double.NaN;
          retVals[1][i] = Double.NaN;
        }
      }

      if (counter < 2) {
        return null;
      }

    } catch (Exception ex) {
      ex.printStackTrace();
    }

    return retVals;
  }

  public double[][] getUVTimeSeries(Vector3D position) {
    double[][] ret = new double[2][numTimes];
    int ncellI = getNcellIndex(position.getV(), position.getU());
    if (ncellI > -1) {
      try {
        int[] origin = new int[]{0, ncellI};
        int[] shape = new int[]{numTimes, 1};

        // has level - nefis style
        if (!timeDimFirst) {
          origin = new int[]{0, 0};
          shape = new int[]{1, numTimes};
        }

        if (levelVar != null) {// has level - nefis style
          // int lvl = 0;
          // if(level != -1){
          // if(level < levelVar.getShape()[0]){
          // }
          // }
          origin = new int[]{origin[0], 0, origin[1]};
          shape = new int[]{shape[0], 1, shape[1]};
        }

        ret[0] = (double[]) uVar.read(origin, shape).get1DJavaArray(double.class);
        ret[1] = (double[]) vVar.read(origin, shape).get1DJavaArray(double.class);

        return ret;
      } catch (IOException ex) {
        Logger.getLogger(NcellReader.class.getName()).log(Level.SEVERE, null, ex);
      } catch (InvalidRangeException ex) {
        Logger.getLogger(NcellReader.class.getName()).log(Level.SEVERE, null, ex);
      }
    }
    return null;
  }

  public double[] getScalarTimeseriesByName(int level, String varName, Vector3D position) {
    if (!hasScalars) {
      return null;
    }
    Variable v = getScalarVarByName(varName);
    if (v != null) {
      int ncellI = getNcellIndex(position.getV(), position.getU());
      if (ncellI > -1) {
        try {
          int[] origin = new int[]{0, ncellI};
          int[] shape = new int[]{numTimes, 1};

          // has level - nefis style
          if (!timeDimFirst) {
            origin = new int[]{0, 0};
            shape = new int[]{1, numTimes};
          }

          if (levelVar != null) {// has level - nefis style
            // int lvl = 0;
            // if(level != -1){
            // if(level < levelVar.getShape()[0]){
            // }
            // }
            origin = new int[]{origin[0], 0, origin[1]};
            shape = new int[]{shape[0], 1, shape[1]};
          }
          return (double[]) v.read(origin, shape).get1DJavaArray(double.class);
        } catch (IOException ex) {
          Logger.getLogger(NcellReader.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InvalidRangeException ex) {
          Logger.getLogger(NcellReader.class.getName()).log(Level.SEVERE, null, ex);
        }
      }
    }

    return null;
  }

  public double[] getScalarDataByName(long t, int level, String varName) {
    Variable v = getScalarVarByName(varName);
    if (v != null) {
      if (t >= getStartTime() & t <= getEndTime()) {
        int tIndex = getTimeIndex(t);
        // System.err.println("tIndex = "+tIndex);
        if (tIndex != -1) {
          try {
            // System.err.println("nCells = "+nCells);
            // System.err.println(uVar.toString());
            // System.err.println(vVar.toString());
            int[] origin = new int[]{tIndex, 0};
            int[] shape = new int[]{1, nCells};

            // ensure the origin dimension order is correct
            if (!timeDimFirst) {
              origin = new int[]{0, tIndex};
              shape = new int[]{nCells, 1};
            }

            if (levelVar != null) {// has level - nefis style
              int lvl = 0;
              if (level != -1) {
                if (level < levelVar.getShape()[0]) {
                }
              }

              origin = new int[]{origin[0], 0, origin[1]};
              shape = new int[]{shape[0], 1, shape[1]};
            }

            return (double[]) v.read(origin, shape).get1DJavaArray(double.class);

          } catch (IOException ex) {
            Logger.getLogger(NcellReader.class.getName()).log(Level.SEVERE, null, ex);
          } catch (InvalidRangeException ex) {
            Logger.getLogger(NcellReader.class.getName()).log(Level.SEVERE, null, ex);
          }
        }
      }
    }
    return null;
  }

  public String getScalarDescriptionByName(String varName) {
    for (Variable v : scalarVars) {
      if (v.getName().equals(varName)) {
        return v.getDescription();
      }
    }
    return null;
  }

  public Variable getScalarVarByName(String varName) {
    for (Variable v : scalarVars) {
      if (v.getName().equals(varName)) {
        return v;
      }
    }
    return null;
  }

  public List<String> getScalarNames() {
    List<String> ret = new ArrayList<String>();
    for (Variable v : scalarVars) {
      ret.add(v.getName());
    }
    return ret;
  }

  public int getNCells() {
    return nCells;
  }

  public void setNCells(int nCells) {
    this.nCells = nCells;
  }

  public double getFillValue() {
    return fillValue;
  }

  public String getUvUnits() {
    return uvUnits;
  }
}
