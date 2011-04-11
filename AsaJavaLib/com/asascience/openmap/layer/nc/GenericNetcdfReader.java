/*
 * Applied Science Associates, Inc.
 * Copyright 2009. All Rights Reserved.
 *
 * GenericNetcdfReader.java
 *
 * Created on Feb 10, 2009 @ 1:48:49 PM
 */
package com.asascience.openmap.layer.nc;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.TimeZone;

import ucar.nc2.Dimension;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;
import ucar.nc2.units.DateUnit;
import ucar.nc2.units.TimeUnit;

/**
 * 
 * @author CBM <cmueller@asascience.com>
 */
public class GenericNetcdfReader {

  public static final String T = "t";
  public static final String T_DIM = "tdim";
  public static final String X = "x";
  public static final String X_DIM = "xdim";
  public static final String Y = "y";
  public static final String Y_DIM = "ydim";
  public static final String Z = "z";
  public static final String Z_DIM = "zdim";
  public static final String U = "u";
  public static final String V = "v";
  private NetcdfFile ncFile;
  private HashMap<String, String> varInfo;
  private HashMap<String, Variable> varList;
  private HashMap<String, String> varMap;
  private NetcdfSupport ncMap;
  private Variable tVar;
  private List<Dimension> tDim;
  private Variable xVar;
  private List<Dimension> xDim;
  private Variable yVar;
  private List<Dimension> yDim;
  private Variable zVar;
  private List<Dimension> zDim;
  private Variable uVar;
  private List<Dimension> uDim;
  private Variable vVar;
  private List<Dimension> vDim;
  private long[] times;
  private long startTime;
  private long endTime;
  private long timeIncrement;

  @SuppressWarnings("unchecked")
  public GenericNetcdfReader(String fileLoc) throws Exception {
    File f = new File(fileLoc);
    if (!f.exists()) {
      throw new Exception("File Does Not Exist");
    }
    ncFile = NetcdfFile.open(fileLoc);
    varInfo = new HashMap<String, String>();
    varList = new HashMap<String, Variable>();
    for (Variable v : ncFile.getVariables()) {
      varInfo.put(v.getName(), v.getDescription());
      varList.put(v.getName(), v);
    }
    /** Check for a map xml file. */
    String xf = fileLoc.replace(".nc", ".xml");
    if (!NetcdfSupport.isNetcdfSupportFile(xf)) {
      /**
       * TODO add logic to attempt to build the map file "automatically"
       * and then bypass the form.
       */
      /** Show the dialog so the map can be built */
      NetcdfMapperDialog nmd = new NetcdfMapperDialog(varInfo);
      nmd.setVisible(true);
      if (nmd.acceptChanges()) {
        nmd.buildSupportFile(xf);
      } else {
        throw new Exception("Mapping cancelled.");
      }
    }
    /** Use the file to build the map. */
    ncMap = new NetcdfSupport(xf);

    varMap = ncMap.getMapping();
    /** TODO Remove these lines - used for testing. */
    for (String s : varMap.keySet()) {
      System.out.println(s + " -> " + varMap.get(s));
    }

    if (!varMap.get(T).toLowerCase().equals("none")) {
      tVar = ncFile.findVariable(varMap.get(T));
      extractTimes();
      tDim = tVar.getDimensions();
    }

    xVar = ncFile.findVariable(varMap.get(X));
    xDim = xVar.getDimensions();
    yVar = ncFile.findVariable(varMap.get(Y));
    yDim = yVar.getDimensions();
    if (!varMap.get(Z).toLowerCase().equals("none")) {
      zVar = ncFile.findVariable(varMap.get(Z));
      zDim = zVar.getDimensions();
    }

    uVar = ncFile.findVariable(varMap.get(U));
    uDim = uVar.getDimensions();
    vVar = ncFile.findVariable(varMap.get(V));
    vDim = vVar.getDimensions();

    double[] data = getVariableData(uVar.getName(), 0, 0, 0, 0);
    System.out.println();
  }

  private void extractTimes() throws Exception {
    double[] ts = (double[]) tVar.read().get1DJavaArray(double.class);
    times = new long[ts.length];
    SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss.sss Z");
    sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
    GregorianCalendar gc = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
    DateUnit du = new DateUnit(tVar.getUnitsString());
    gc.setTime(du.getDate());
    TimeUnit tu = du.getTimeUnit();
    long origin = gc.getTimeInMillis();
    long timestep = (long) (tu.getValueInSeconds() * 1000);// timestep in ms
    for (int i = 0; i < ts.length; i++) {
      times[i] = (long) (origin + (ts[i] * timestep));
      gc.setTimeInMillis(times[i]);
      System.out.println(sdf.format(gc.getTime()));
    }

    startTime = times[0];
    gc.setTimeInMillis(startTime);
    System.out.println("ST: " + sdf.format(gc.getTime()));
    endTime = times[times.length - 1];
    gc.setTimeInMillis(endTime);
    System.out.println("ET: " + sdf.format(gc.getTime()));

    /**
     * SimpleDateFormat sdf = new
     * SimpleDateFormat("MM/dd/yyyy HH:mm:ss.sss Z");
     * sdf.setTimeZone(TimeZone.getTimeZone("UTC")); GregorianCalendar gc =
     * new GregorianCalendar(TimeZone.getTimeZone("UTC")); DateUnit du = new
     * DateUnit(tVar.getUnitsString()); gc.setTime(du.getDate());
     * System.out.println(sdf.format(gc.getTime())); TimeUnit tu =
     * du.getTimeUnit(); double sec = tu.getValueInSeconds();
     * System.out.println(sec);
     */
  }

  // public int getIndex(double[] )
  public double[] getVariableData(String varName, double time, double x, double y, double z) throws Exception {
    int tIndex, xIndex, yIndex, zIndex;

    if (tVar != null) {
    }
    return null;
  }

  public double[] getVariableData(String varName, int timeIndex, int xIndex, int yIndex, int zIndex) throws Exception {
    double[] ret = null;
    Variable var = null;
    if (varMap.containsValue(varName)) {
      for (String s : varMap.keySet()) {
        if (varMap.get(s).equals(varName)) {
          var = ncFile.findVariable(s);
          break;
        }
      }
    } else {
      // for non-mapped (i.e. scalar) variables
      var = ncFile.findVariable(varName);
    }

    if (var == null) {
      throw new Exception("Variable with name " + varName + " cannot be found.");
    }
    int dims = var.getDimensions().size();
    switch (dims) {
      case 1:
        getData(var, timeIndex);
        break;
      case 2:

        break;
      case 3:
        getData(var, timeIndex, xIndex, yIndex);
        break;
      case 4:
        getData(var, timeIndex, xIndex, yIndex, zIndex);
        break;
      default:
        // don't know what to do...
        break;
    }

    return ret;
  }

  private double[] getData(Variable var, int timeIndex, int xIndex, int yIndex, int zIndex) {
    double[] ret = null;

    return ret;
  }

  private double[] getData(Variable var, int timeIndex, int xIndex, int yIndex) {
    double[] ret = null;

    return ret;
  }

  private double[] getData(Variable var, int timeIndex) {
    double[] ret = null;

    return ret;
  }

  public long getStartTime() {
    return startTime;
  }

  public long getEndTime() {
    return endTime;
  }

  public long getTimeIncrement() {
    return timeIncrement;
  }

  public long[] getTimes() {
    return times;
  }
}
