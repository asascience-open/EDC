/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * NefisReader.java
 *
 * Created on Jun 11, 2008, 1:11:46 PM
 *
 */
package com.asascience.openmap.layer.nefis;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.lang.ArrayUtils;

import com.asascience.openmap.layer.nefis.NefisFile.CellDef;
import com.asascience.openmap.layer.nefis.NefisFile.ElementDef;
import com.asascience.openmap.layer.nefis.NefisFile.GroupData;
import com.asascience.openmap.layer.nefis.NefisFile.GroupDef;
import com.asascience.utilities.Utils;

/**
 * 
 * @author cmueller_mac
 */
public class NefisReader {

  private boolean debug = false;
  private static long nil = -1;// in matlab it is 4294967295, but in java it
  // is -1
  private static ByteOrder byteOrder = ByteOrder.BIG_ENDIAN;// default
  private static ByteBuffer byteBuffer = null;
  private static byte[] byteArray;
  private NefisFile nefisFile;
  // private double[] xVals = null;
  // private double[] yVals = null;
  // private double[] zVals = null;
  // private double[] xGrid = null;
  // private double[] yGrid = null;
  // private double[] depthVals = null;
  private float[] xVals = null;
  private float[] yVals = null;
  private float[] zVals = null;
  private float[] xGrid = null;
  private float[] yGrid = null;
  private float[] depthVals = null;
  private float[] rotCorrVals = null;
  private int nMax = -1;
  private int mMax = -1;
  private String[] xMeta = new String[3];
  private String[] yMeta = new String[3];
  private String[] zMeta = new String[3];
  private String[] xgMeta = new String[3];
  private String[] ygMeta = new String[3];
  private String[] depthMeta = new String[3];
  private String[] uMeta = new String[3];
  private String[] vMeta = new String[3];
  // private List<double[][]> uVals = null;
  // private List<double[][]> vVals = null;
  private List uVals = null;
  private List vVals = null;
  private long[] times = null;
  private GregorianCalendar startCal = null;
  private SimpleDateFormat sdf = null;
  private long timeIncrement;
  private String subtype;
  private int numLevels;
  private boolean hasGridVals = false;
  private long fileSize;

  public NefisReader(String fileLoc) {
    startCal = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
    sdf = new SimpleDateFormat("yyyyMMdd hhmmss");
    sdf.setTimeZone(TimeZone.getTimeZone("UTC"));

    fileSize = new File(fileLoc).length();

    nefisFile = initializeNefisFile(fileLoc);

    subtype = nefisFile.getSubType();

    // these are not necessary here - adds to startup time of Nefis2Netcdf
    // gui

    // // //get the times
    // //// initTimes();
    // // initTimes2();
    // // //get the x&y locations
    // // initXYLocs();
    // // //get the depth and z locations
    // // initDepthZLocs();
    // // //get the grid-node locations
    // // hasGridVals = initGrid();
    // // //get the u&v data
    // // initUVData();

    boolean writeout = false;
    // writeout = true;
    if (writeout) {
      initTimes2();
      initDepthZLocs();
      initXYLocs();
      initUVData();
      // write currents to a file
      System.out.println("Writing output to disk...");
      writeCurrentDataToFile(nefisFile.getFilename() + ".out");
      System.out.println("Finished writing output.");
    }
  }

  public void initializeCommonVariables() {
    // get the times
    // initTimes();
    initTimes2();
    // get the x&y locations
    initXYLocs();
    // get the depth and z locations
    initDepthZLocs();
    // get the grid-node locations
    hasGridVals = initGrid();
    // get the u&v data
    initUVData();
    // get the rotation correction
    initRotCorr();
    // // fix the u/v using the rotation corrections
    // fixUVRotation();
  }

  public void writeCurrentDataToFile(String fileLoc) {
    FileWriter fw = null;
    try {
      fw = new FileWriter(fileLoc, false);

      for (int t = 0; t < times.length; t++) {
        for (int l = 0; l < numLevels; l++) {
          for (int d = 0; d < xVals.length; d++) {
            fw.write(String.valueOf(times[t]) + "," + l + "," + xVals[d] + "," + yVals[d] + ","
                    + ((float[][]) uVals.get(t))[l][d] + "," + ((float[][]) vVals.get(t))[l][d]);
            fw.write(System.getProperty("line.separator"));
          }
        }
      }

    } catch (IOException ex) {
      Logger.getLogger(NefisReader.class.getName()).log(Level.SEVERE, null, ex);
    } finally {
      if (fw != null) {
        try {
          fw.close();
        } catch (IOException ex) {
          Logger.getLogger(NefisReader.class.getName()).log(Level.SEVERE, null, ex);
        }
      }
    }
  }

  public int getTimeIndex(long t) {
    try {
      long time;
      for (int i = 0; i < times.length; i++) {
        time = times[i];
        if (t < time) {
          return i - 1;
        }
      }
    } catch (Exception ex) {
      Logger.getLogger(NefisReader.class.getName()).log(Level.SEVERE, null, ex);
    }
    return -1;
  }

  public boolean isValidTime(long t) {
    try {
      if (t >= this.getStartTime()) {
        if (t <= this.getEndTime()) {
          return true;
        }
      }

    } catch (Exception ex) {
      Logger.getLogger(NefisReader.class.getName()).log(Level.SEVERE, null, ex);
    }
    return false;
  }

  public List<String> getExclusions() {
    List<String> ret = new ArrayList<String>();

    /**
     * This no longer works because the dataset is not loaded prior to the
     * gui, so these are all null
     */
    // ret.add(xMeta[1]);
    // ret.add(yMeta[1]);
    // if(zMeta != null){
    // ret.add(zMeta[1]);
    // }
    // ret.add(uMeta[1]);
    // ret.add(vMeta[1]);
    if (this.getNefisFile().getSubType().equals(NefisUtils.D3D_TRIM)) {
      ret.add("U1");
      ret.add("V1");
      ret.add("XZ");
      ret.add("YZ");
      ret.add("ZK");
    } else if (this.getNefisFile().getSubType().equals(NefisUtils.D3D_HWGXY)) {
      ret.add("VELOC-X");
      ret.add("VELOC-Y");
      ret.add("XP");
      ret.add("YP");
    }

    return ret;
  }

  private void initTimes2() {
    List t = null, tu = null, tdt = null, ts = null, t1 = null;

    int numsteps = -1;
    StringBuilder sStartTime = new StringBuilder();
    if (subtype.equals(NefisUtils.D3D_TRIM)) {
      t = getData("map-const", "ITDATE");
      tu = getData("map-const", "TUNIT");
      tdt = getData("map-const", "DT");
      ts = getData("map-info-series", "ITMAPC");

      // put the ITDATE into the StringBuilder
      sStartTime.append(String.valueOf(((int[]) t.get(0))[0]));
      sStartTime.append(" ");
      sStartTime.append(String.valueOf(((int[]) t.get(0))[1]));

      // load the date-time string into the start calendar
      if ((sStartTime.length() != 0) & (sdf != null)) {
        try {
          // ensure the date-string is the right length of characters
          // by appending 0's
          while (sStartTime.length() < 15) {
            sStartTime.append("0");
          }
          startCal.setTime(sdf.parse(sStartTime.toString()));
          startCal.set(GregorianCalendar.MILLISECOND, 0);
        } catch (ParseException parseException) {
          Logger.getLogger(NefisReader.class.getName()).log(Level.SEVERE, null, parseException);
        }
      }

      // fill the times array
      // double tUnit = ((double[])tu.get(0))[0];
      // double tDT = ((double[])tdt.get(0))[0];
      float tUnit = ((float[]) tu.get(0))[0];
      float tDT = ((float[]) tdt.get(0))[0];
      int tt;
      times = new long[ts.size()];
      /** Old Method - attempt to calculate */
      // for(int i = 0; i < ts.size(); i++){
      // tt = ((int[])ts.get(i))[0];
      // times[i] = (long)((tt * tUnit * ((tDT > 0) ? tDT : 1) * 1000) +
      // startCal.
      // getTimeInMillis());
      // }
      /** New Method - use provided series (IMAPC) */
      for (int i = 0; i < ts.size(); i++) {
        tt = ((int[]) ts.get(i))[0];
        times[i] = ((long) (((tt * tUnit * ((tDT > 0) ? tDT : 1)) * 1000))) + startCal.getTimeInMillis();
      }

      // set the time increment
      if (times.length > 1) {
        timeIncrement = times[1] - times[0];
      } else {
        timeIncrement = -1;
      }
    } else if (subtype.equals(NefisUtils.D3D_HWGXY)) {
      t = getData("PARAMS", "IT01");
      t1 = getData("PARAMS", "IT02");
      tu = getData("PARAMS", "TSCALE");
      tdt = getData("PARAMS", "DT");
      ts = getData("map-series", "TIME");
      // get the number of timesteps
      numsteps = ts.size();

      // get the start date and time (two elements) - comes back as
      // integers....
      sStartTime.append(String.valueOf(((int[]) t.get(0))[0]));
      sStartTime.append(" ");
      sStartTime.append(String.valueOf(((int[]) t1.get(0))[0]));
      // load the date-time string into the start calendar
      if ((sStartTime.length() != 0) & (sdf != null)) {
        try {
          // ensure the date-string is the right length of characters
          // by appending 0's
          while (sStartTime.length() < 15) {
            sStartTime.append("0");
          }
          startCal.setTime(sdf.parse(sStartTime.toString()));
          startCal.set(GregorianCalendar.MILLISECOND, 0);
        } catch (ParseException parseException) {
          Logger.getLogger(NefisReader.class.getName()).log(Level.SEVERE, null, parseException);
        }
      }

      // fill the times array
      // double tUnit = ((double[])tu.get(0))[0];
      // double tDT = ((double[])tdt.get(0))[0];
      float tUnit = ((float[]) tu.get(0))[0];
      float tDT = ((float[]) tdt.get(0))[0];
      int tt;
      times = new long[ts.size()];
      /** Old Method - attempt to calculate */
      // for(int i = 0; i < ts.size(); i++){
      // tt = ((int[])ts.get(i))[0];
      // times[i] = (long)((tt * tUnit * ((tDT > 0) ? tDT : 1) * 1000) +
      // startCal.
      // getTimeInMillis());
      // }
      /** New Method - use provided series (IMAPC) */
      for (int i = 0; i < ts.size(); i++) {
        tt = ((int[]) ts.get(i))[0];
        times[i] = ((long) (((tt * tUnit * ((tDT > 0) ? tDT : 1)) * 1000))) + startCal.getTimeInMillis();
      }

      // set the time increment
      if (times.length > 1) {
        timeIncrement = times[1] - times[0];
      } else {
        timeIncrement = -1;
      }
    }
  }

  private void initTimes() {
    List t = null, t1 = null, tu = null, tdt = null, ts = null;

    int numsteps = -1;
    // get the times
    StringBuilder sStartTime = new StringBuilder();
    if (subtype.equals(NefisUtils.D3D_TRIM)) {// "trim" type file
      t = getData("map-const", "SIMDAT");
      tu = getData("map-const", "TUNIT");
      tdt = getData("map-const", "DT");
      ts = getData("map-info-series", "ITMAPC");

      // get the number of timesteps
      numsteps = ts.size();

      // get the start date and time (one element) - comes back as a
      // string...
      sStartTime.append((String) t.get(0));

      // get the time increment - returned as float value - assume
      // seconds...
      double tUnit = ((double[]) tu.get(0))[0];
      double tDT = ((double[]) tdt.get(0))[0];

      timeIncrement = (long) (tUnit * tDT);
    } else if (subtype.equals(NefisUtils.D3D_HWGXY)) {// "wavm" type file
      t = getData("PARAMS", "IT01");
      t1 = getData("PARAMS", "IT02");
      tu = getData("PARAMS", "TSCALE");
      // tdt = getData("PARAMS", "DT");
      ts = getData("map-series", "TIME");

      // get the number of timesteps
      numsteps = ts.size();

      // get the start date and time (two elements) - comes back as
      // integers....
      sStartTime.append(String.valueOf(((int[]) t.get(0))[0]));
      sStartTime.append(" ");
      sStartTime.append(String.valueOf(((int[]) t1.get(0))[0]));

      // float tDt = ((float[])tdt.get(0))[0];
      double tUnit = ((double[]) tu.get(0))[0];
      // get the time increment - returned as float value - assume
      // seconds...
      timeIncrement = (long) (tUnit);
    }

    // convert the time increment from seconds(assumed) to milliseconds
    timeIncrement = timeIncrement * 1000;

    // load the date-time string into the start calendar
    if ((sStartTime.length() != 0) & (sdf != null)) {
      try {
        // ensure the date-string is the right length of characters by
        // appending 0's
        while (sStartTime.length() < 15) {
          sStartTime.append("0");
        }
        startCal.setTime(sdf.parse(sStartTime.toString()));
      } catch (ParseException parseException) {
        Logger.getLogger(NefisReader.class.getName()).log(Level.SEVERE, null, parseException);
      }
    }

    times = new long[numsteps];
    // build the times array
    for (int i = 0; i < numsteps; i++) {
      times[i] = startCal.getTimeInMillis() + (timeIncrement * i);
    }

  }

  private void initDepthZLocs() {
    try {
      boolean calcLevels = false;
      List z = null, d = null, l = null, t = null;
      ElementDef edz = null, edd = null, edl = null, edt = null;
      if (subtype.equals(NefisUtils.D3D_TRIM)) {// "trim" type file
        d = getData("map-const", "DP0");
        edd = nefisFile.getElementDefByName("DP0");
        z = getData("map-const", "ZK");// is this used??
        edz = nefisFile.getElementDefByName("ZK");
        // if(z == null){
        t = getData("map-const", "THICK");
        edt = nefisFile.getElementDefByName("THICK");
        // calcLevels = true;
        // }
        l = getData("map-const", "KMAX");
        edl = nefisFile.getElementDefByName("KMAX");
      } else if (subtype.equals(NefisUtils.D3D_HWGXY)) {// "wavm" type
        // file - not 3D
      }

      if (l != null) {
        numLevels = ((int[]) l.get(0))[0];
      } else {
        numLevels = 1;
      }

      // if(z != null & edz != null){
      // zMeta[0] = edz.getName();
      // zMeta[1] = edz.getDescription();
      // zMeta[2] = edz.getUnits();
      //
      // zVals = ((float[])z.get(0));
      // }

      if (edz != null) {
        zMeta[0] = edz.getName();
        zMeta[1] = edz.getDescription();
        zMeta[2] = edz.getUnits();
        //
        // zVals = ((float[])z.get(0));
      } else if (edd != null) {
        zMeta[0] = "depth";
        zMeta[1] = "Vertical coordinates";
        zMeta[2] = edd.getUnits();
      } else {
        zMeta[0] = "depth";
        zMeta[1] = "Vertical coordinates";
        zMeta[2] = "unknown";
      }

      // double[] tVals = null;
      // if(t != null){
      // tVals = ((double[])t.get(0));
      // }
      float[] tVals = null;
      if (t != null) {
        tVals = ((float[]) t.get(0));
      }

      if (d != null & edd != null) {
        depthMeta[0] = edd.getName();
        depthMeta[1] = edd.getDescription();
        depthMeta[2] = edd.getUnits();

        // depthVals = ((double[][])d.get(0))[0];
        depthVals = ((float[][]) d.get(0))[0];
      }

      // zVals = new double[numLevels];
      zVals = new float[numLevels];
      if (numLevels == 1) {
        zVals[0] = 0;
      } else {
        if (depthVals != null & tVals != null) {
          // if(d != null & z != null){
          // double avgDepth =
          // Utils.averageDouble((double[])depthVals);
          float avgDepth = Utils.averageFloat((float[]) depthVals, -999f);
          float[] modes = Utils.calculateModes(depthVals, -999f);
          // float perc = 0;
          // //TODO: deal with depths properly, then write to file
          // for(int i = 0; i < numLevels; i++){
          // if(i == 0){
          // perc = tVals[i] * 0.5f;
          // }else{
          // perc = perc + tVals[i];
          // }
          // zVals[i] = -(Math.abs(avgDepth * perc));//bottom
          // }
          float lyrDepth = avgDepth * tVals[0];// all values of tVals
          // are the same
          for (int i = 0; i < numLevels; i++) {
            // zVals[i]=-(Math.abs((lyrDepth * i) - (lyrDepth *
            // 0.5f)));
            zVals[i] = i + 1;
          }

          // }
        }
      }

    } catch (Exception ex) {
      Logger.getLogger(NefisReader.class.getName()).log(Level.SEVERE, null, ex);
    }
  }

  private float[] findCentroids(float[] grid) {
    List<Float> lst = new ArrayList<Float>();
    float[] cell = new float[4];
    float mid1, mid2, cent;
    List<Float> toAvg = new ArrayList<Float>();
    for (int i = nMax + 1; i < grid.length; i++) {
      cell[0] = grid[i - nMax];
      cell[1] = grid[i - nMax - 1];
      cell[2] = grid[i];
      cell[3] = grid[i - 1];
      int zerocount = 0;
      toAvg.removeAll(toAvg);
      for (int c = 0; c < cell.length; c++) {
        if (cell[c] != 0) {
          toAvg.add(cell[c]);
        }
        // zerocount = (cell[c] == 0) ? zerocount + 1 : zerocount;
      }

      if (toAvg.size() == 4) {
        lst.add(Utils.averageFloat(ArrayUtils.toPrimitive(toAvg.toArray(new Float[0]))));
        // lst.add(Utils.averageFloat(Utils
        // .floatListArrayToFloatPrimArray(toAvg)));
      } else {
        lst.add(0f);
      }

      // if(zerocount == 0){
      // mid1 = Utils.averageDouble(new double[]{cell[0], cell[1]});
      // mid2 = Utils.averageDouble(new double[]{cell[2], cell[3]});
      // cent = Utils.averageDouble(new double[]{mid1, mid2});
      // // System.out.println(cell[0] + "," + cell[1] + "," + cell[2] +
      // "," + cell[3] + "," +
      // // mid1 + "," + mid2 + "," + cent);
      // lst.add(cent);
      // }else{
      // lst.add(0d);
      // }
    }

    return ArrayUtils.toPrimitive(lst.toArray(new Float[0]));
    // return Utils.floatListArrayToFloatPrimArray(lst);
  }

  private double[] findCentroids(double[] grid) {
    List<Double> lst = new ArrayList<Double>();
    double[] cell = new double[4];
    double mid1, mid2, cent;
    List<Double> toAvg = new ArrayList<Double>();
    for (int i = nMax + 1; i < grid.length; i++) {
      cell[0] = grid[i - nMax];
      cell[1] = grid[i - nMax - 1];
      cell[2] = grid[i];
      cell[3] = grid[i - 1];
      int zerocount = 0;
      toAvg.removeAll(toAvg);
      for (int c = 0; c < cell.length; c++) {
        if (cell[c] != 0) {
          toAvg.add(cell[c]);
        }
        // zerocount = (cell[c] == 0) ? zerocount + 1 : zerocount;
      }

      if (toAvg.size() == 4) {
        lst.add(Utils.averageDouble(Utils.doubleListArrayToDoublePrimArray(toAvg)));
      } else {
        lst.add(0d);
      }

      // if(zerocount == 0){
      // mid1 = Utils.averageDouble(new double[]{cell[0], cell[1]});
      // mid2 = Utils.averageDouble(new double[]{cell[2], cell[3]});
      // cent = Utils.averageDouble(new double[]{mid1, mid2});
      // // System.out.println(cell[0] + "," + cell[1] + "," + cell[2] +
      // "," + cell[3] + "," +
      // // mid1 + "," + mid2 + "," + cent);
      // lst.add(cent);
      // }else{
      // lst.add(0d);
      // }
    }

    return Utils.doubleListArrayToDoublePrimArray(lst);
  }

  private boolean initXYLocs() {
    try {
      List x = null, y = null, d = null;
      ElementDef edx = null, edy = null;
      xVals = null;
      yVals = null;
      if (subtype.equals(NefisUtils.D3D_TRIM)) {// "trim" type file
        // XCOR/YCOR is the "grid" location,
        // XZ/YZ is the "grid centroid"
        x = getData("map-const", "XZ");
        edx = nefisFile.getElementDefByName("XZ");
        y = getData("map-const", "YZ");
        edy = nefisFile.getElementDefByName("YZ");

      } else if (subtype.equals(NefisUtils.D3D_HWGXY)) {// "wavm" type
        // file
        // these are actually the grid locations!!!
        x = getData("map-series", "XP");
        edx = nefisFile.getElementDefByName("XP");
        y = getData("map-series", "YP");
        edy = nefisFile.getElementDefByName("YP");
        // use the first size dimension of "XP" as nMax and the second
        // for mMax
        List<Integer> s = nefisFile.getElementDefByName("XP").getSize();
        nMax = s.get(0);
        mMax = s.get(1);
        // now find the centroids for the "non-outer" cells...
        // xVals = findCentroids(((double[][])x.get(0))[0]);
        // yVals = findCentroids(((double[][])y.get(0))[0]);
        xVals = findCentroids(((float[][]) x.get(0))[0]);
        yVals = findCentroids(((float[][]) y.get(0))[0]);
        // z = getData("map-const", "ZK");
        // edz = nefisFile.getElementDefByName("ZK");
      } else {
        return false;
      }

      if (edx != null & edy != null & x != null & y != null) {
        xMeta[0] = edx.getName();
        xMeta[1] = edx.getDescription();
        xMeta[2] = edx.getUnits();
        yMeta[0] = edy.getName();
        yMeta[1] = edy.getDescription();
        yMeta[2] = edy.getUnits();

        if (xVals == null & yVals == null) {
          // xVals = ((double[][])x.get(0))[0];
          // yVals = ((double[][])y.get(0))[0];
          xVals = ((float[][]) x.get(0))[0];
          yVals = ((float[][]) y.get(0))[0];
        }
      } else {
        // bail without these...
        throw new Exception("File missing X/Y information.");
      }
      return true;
    } catch (Exception ex) {
      Logger.getLogger(NefisReader.class.getName()).log(Level.SEVERE, null, ex);
    }
    return false;
  }

  private boolean initGrid() {
    try {
      List x = null, y = null, m = null, n = null;
      ElementDef edx = null, edy = null;
      if (subtype.equals(NefisUtils.D3D_TRIM)) {// "trim" type file
        x = getData("map-const", "XCOR");
        edx = nefisFile.getElementDefByName("XCOR");
        y = getData("map-const", "YCOR");
        edy = nefisFile.getElementDefByName("YCOR");
        n = getData("map-const", "NMAX");// number of rows
        m = getData("map-const", "MMAX");// number of columns
      } else if (subtype.equals(NefisUtils.D3D_HWGXY)) {// "wavm" type
        // file
        // //TODO: figure out how to deal with this....
        // //TODO: must open the accompanying "trim" file and get the
        // data
        // out....

        // does not work --> the arrays are not of equal length and
        // "nmax" is not the same in each file...
        // String trimFile = nefisFile.getFilename().replace("wavm",
        // "trim") + nefisFile.
        // getDatExt();
        // if(!new File(trimFile).exists()){
        // return false;
        // }
        // try{
        // NefisReader trimRead = new NefisReader(trimFile);
        // x = trimRead.getData("map-const", "XCOR");
        // edx = trimRead.getNefisFile().getElementDefByName("XCOR");
        // y = trimRead.getData("map-const", "YCOR");
        // edy = trimRead.getNefisFile().getElementDefByName("YCOR");
        // n = trimRead.getData("map-const", "NMAX");//number of rows
        // m = trimRead.getData("map-const", "MMAX");//number of columns
        // //make sure the lengths agree
        // int l1 = ((double[][])x.get(0))[0].length;
        // int l2 = getXVals().length;
        // if(((double[][])x.get(0))[0].length < getXVals().length){
        // return false;
        // }

        // }catch(Exception ex){
        // Logger.getLogger(NefisReader.class.getName()).
        // log(Level.SEVERE, null, ex);
        // return false;
        // }

        // these are actually the grid locations!!!
        x = getData("map-series", "XP");
        edx = nefisFile.getElementDefByName("XP");
        y = getData("map-series", "YP");
        edy = nefisFile.getElementDefByName("YP");
        // use the first size dimension of "XP" as nMax and the second
        // for mMax
        List<Integer> s = nefisFile.getElementDefByName("XP").getSize();
        nMax = s.get(0);
        mMax = s.get(1);
      } else {
        return false;
      }

      if (edx != null & edy != null & x != null & y != null) {
        xgMeta[0] = edx.getName();
        xgMeta[1] = edx.getDescription();
        xgMeta[2] = edx.getUnits();
        ygMeta[0] = edy.getName();
        ygMeta[1] = edy.getDescription();
        ygMeta[2] = edy.getUnits();

        // xGrid = ((double[][])x.get(0))[0];
        xGrid = ((float[][]) x.get(0))[0];
        // for(double d:xGrid){
        // System.out.println(d);
        // }
        // yGrid = ((double[][])y.get(0))[0];
        yGrid = ((float[][]) y.get(0))[0];
        // for(double d:yGrid){
        // System.out.println(d);
        // }
      } else {
        throw new Exception("File missing grid information.");
      }

      if (n != null & m != null) {
        nMax = ((int[]) n.get(0))[0];
        mMax = ((int[]) m.get(0))[0];
      }

      return true;
    } catch (Exception ex) {
      Logger.getLogger(NefisReader.class.getName()).log(Level.SEVERE, null, ex);
    }
    return false;
  }

  private void initUVData() {
    try {
      List u = null, v = null;
      ElementDef edu = null, edv = null;
      if (subtype.equals(NefisUtils.D3D_TRIM)) {// "trim" type file
        u = getData("map-series", "U1");
        edu = nefisFile.getElementDefByName("U1");
        v = getData("map-series", "V1");
        edv = nefisFile.getElementDefByName("V1");
      } else if (subtype.equals(NefisUtils.D3D_HWGXY)) {// "wavm" type
        // file
        u = getData("map-series", "VELOC-X");
        edu = nefisFile.getElementDefByName("VELOC-X");
        v = getData("map-series", "VELOC-Y");
        edv = nefisFile.getElementDefByName("VELOC-Y");
      } else {
        return;
      }

      if (edu != null & edv != null & u != null & v != null) {
        uMeta[0] = edu.getName();
        uMeta[1] = edu.getDescription();
        uMeta[2] = edu.getUnits();
        vMeta[0] = edv.getName();
        vMeta[1] = edv.getDescription();
        vMeta[2] = edv.getUnits();

        // uVals = (List<double[][]>)u;
        // vVals = (List<double[][]>)v;
        uVals = u;
        vVals = v;

      } else {
        throw new Exception("File missing U/V information.");
      }
    } catch (Exception ex) {
      Logger.getLogger(NefisReader.class.getName()).log(Level.SEVERE, null, ex);
    }
  }

  private void initRotCorr() {
    List alfas = null;
    if (subtype.equals(NefisUtils.D3D_TRIM)) {
      alfas = getData("map-const", "ALFAS");
      rotCorrVals = ((float[][]) alfas.get(0))[0];
    } else if (subtype.equals(NefisUtils.D3D_HWGXY)) {
      alfas = getData("map-const", "ALFAS");
      rotCorrVals = null;
    }
  }

  /**
   *
   * @param groupName
   *            The name of the group containing the desired data.
   * @param elementName
   *            The name of the element containing the desired data.
   * @return A <code>List</code> containing a <code>List(object)</code> which
   *         holds the data.
   */
  public List getData(String groupName, String elementName) throws OutOfMemoryError {
    if (debug) {
      System.out.println("getData: " + groupName + " " + elementName);
    }
    RandomAccessFile rafDat = null;
    try {
      rafDat = new RandomAccessFile(nefisFile.getFilename() + nefisFile.getDatExt(), "r");

      GroupData grpDat = nefisFile.getGroupDataByName(groupName);
      if (grpDat == null) {
        // group does not exist
        System.out.println("Group \"" + groupName + "\" does not exist.");
        return null;
      }

      List<List<Integer>> gIndex = new ArrayList<List<Integer>>();
      List<List<Integer>> eIndex = new ArrayList<List<Integer>>();

      int id = grpDat.getDefIndex();
      int ic = nefisFile.getGrpDef().get(id).getCelIndex();
      List<Integer> elmsInCell = nefisFile.getCelDef().get(ic).getElm();
      // ElementDef elmDef;
      // for(int k = 0; k < elmsInCell.size(); k++){
      // elmDef = nf.getElmDef().get(elmsInCell.get(k));
      // if(elementName.equals(elmDef.getName())){
      // // eIndex = new int[elmDef.getSize().size()];
      // break;
      // }
      // }

      int i = 0;
      int j = 0;

      // check parameters (vs_let, 498)
      int i_new = nefisFile.getGroupDataIndex(groupName);
      if (i_new == -1) {
        // group does not exist
        System.out.println("Group \"" + groupName + "\" does not exist.");
        return null;
      }
      int id_new = nefisFile.getGrpDat().get(i_new).getDefIndex();
      int ic_new = nefisFile.getGrpDef().get(id_new).getCelIndex();
      elmsInCell = nefisFile.getCelDef().get(ic_new).getElm();
      int j_new = -1;
      for (int k = 0; k < elmsInCell.size(); k++) {
        if (elementName.equals(nefisFile.getElmDef().get(elmsInCell.get(k)).getName())) {
          j_new = elmsInCell.get(k);
        }
      }

      // (vs_let, 565) "gIndex{k}=1:VS.GrpDat(i_new).SizeDim(k);"
      for (int k = 0; k < nefisFile.getGrpDat().get(i_new).getSizeDim().size(); k++) {
        List<Integer> toAdd = new ArrayList<Integer>();
        for (int g = 0; g < nefisFile.getGrpDat().get(i_new).getSizeDim().get(k); g++) {
          toAdd.add(g);
        }
        gIndex.add(toAdd);
      }

      // (vs_let, 627) "eIndex{k}=1:VS.ElmDef(j_new).Size(k);"
      for (int k = 0; k < nefisFile.getElmDef().get(j_new).getSize().size(); k++) {
        List<Integer> toAdd = new ArrayList<Integer>();
        for (int g = 0; g < nefisFile.getElmDef().get(j_new).getSize().get(k); g++) {
          toAdd.add(g);
        }
        eIndex.add(toAdd);
      }

      // (vs_let, 659)
      i = i_new;
      id = nefisFile.getGrpDat().get(i).getDefIndex();
      ic = nefisFile.getGrpDef().get(id).getCelIndex();
      elmsInCell = nefisFile.getCelDef().get(ic).getElm();
      j = j_new;

      // everything should now be correct....(vs_let, 664)

      // (vs_let, 730)
      int jn = 0;
      int jnm = 1;

      // count the # of group indices to be returned (vs_let, 737)
      int readNGrp = 1;
      int[] gNIndex = new int[gIndex.size()];
      for (int k = gIndex.size() - 1; k > -1; k--) {
        // remove any replicate values and sort -> ignored for now
        gNIndex[k] = gIndex.get(k).size();
        readNGrp = gIndex.get(k).size() * readNGrp;
      }

      // count the # of element entries to be returned (vs_let, 745)
      List<Integer> eDimen = new ArrayList<Integer>();
      List<Integer> eSize = new ArrayList<Integer>();
      int[] eNIndex = new int[eIndex.size()];
      for (int j1 = 0; j1 < jnm; j1++) {
        for (int k = 0; k < eIndex.size(); k++) {
          eNIndex[k] = eIndex.get(k).size();
        }
        for (int k = eIndex.get(j1).size() - 1; k > -1; k--) {
          // remove any replicate values and sort -> ignored for now
        }
        eDimen.add(nefisFile.getElmDef().get(j).getSize().size());
        int es = 1;
        for (int k = 0; k < eDimen.get(j1); k++) {
          es = es * nefisFile.getElmDef().get(j).getSize().get(k);
        }
        eSize.add(es);
        // determine scalar index for element selection (vs_let, 754)
        // **wait to see if necessary**
      }

      int[] P256 = new int[]{1, 256, (int) Math.pow(256, 2), (int) Math.pow(256, 3)};

      /** START READING DATA */
      // determine whether there is a variable dimension (vs_let, 812)
      List<Integer> gDimen = new ArrayList<Integer>(nefisFile.getGrpDat().get(i).getSizeDim());
      int vd = nefisFile.getGrpDat().get(i).getVarDim();

      // determine the number of variable dimension (vs_let, 814)
      int vdOffset = 1;
      int varDimCntMax;
      List<Integer> vdIndex;
      if (vd == 1) {
        vdIndex = gIndex.get(vd - 1);
        gDimen.set(vd - 1, 1);
        for (int k = 0; k > vd - 1; k++) {
          vdOffset = vdOffset * gIndex.get(k).size();
        }
        gIndex.set(vd - 1, new ArrayList<Integer>());
        gIndex.get(vd - 1).add(1);

        varDimCntMax = vdIndex.size();
      } else {
        vdOffset = 0;
        varDimCntMax = 1;
        vdIndex = null;
      }

      int gOrder = nefisFile.getGrpDat().get(i).getOrderDim();

      // //reorder the dimensions for data access (vs_let, 841)
      // //**wait on implementing these lines (842-851) - see if
      // necessary**
      // if(vd == 1){
      // if(gOrder != 1){
      // gOrder = 1;
      // }
      // }

      // precompute element offset within cell
      int elmOffset = 0;
      if (jn == 0) {
        for (int k : elmsInCell) {
          if (k == j) {
            break;
          }
          elmOffset = elmOffset + nefisFile.getElmDef().get(k).getSizeElm();
        }
      }

      // preload nBytesPerCell, dataType, elmBytes, & valBytes
      int nBytesPerCell = 0;
      for (int k = 0; k < elmsInCell.size(); k++) {
        nBytesPerCell = nBytesPerCell + nefisFile.getElmDef().get(elmsInCell.get(k)).getSizeElm();
      }
      int dataType = nefisFile.getElmDef().get(j).getType();
      int valBytes = nefisFile.getElmDef().get(j).getSizeVal();
      int elmBytes = nefisFile.getElmDef().get(j).getSizeElm();
      String rdType = "";
      switch (dataType) {
        case 1:// CHARACTE
          rdType = "uchar";
          break;
        case 2:// COMPLEX
          switch (valBytes) {
            case 8:// number of bytes == 8 (4+4)
              rdType = "float32";
              break;
            case 16:// number of bytes == 16 (8+8)
              rdType = "float64";
              break;
            default:
              // throw an error
              System.out.println("Unable to read COMPLEX data.");
              break;
          }
          break;
        case 3:// INTEGER
          switch (valBytes) {
            case 4:// number of bytes == 4
              rdType = "int32";
              break;
            case 2:// number of bytes == 2
              rdType = "int16";
              break;
            default:
              // throw an error
              System.out.println("Unable to read INTEGER data.");
              break;
          }
          break;
        case 4:// LOGICAL
          switch (valBytes) {
            case 4:// number of bytes == 4
              rdType = "int32";
              break;
            case 2:// number of bytes == 2
              rdType = "int16";
              break;
            default:
              // throw an error
              System.out.println("Unable to read LOGICAL data.");
              break;
          }
          break;
        case 5:// REAL
          switch (valBytes) {
            case 4:// number of bytes == 4
              rdType = "float32";
              break;
            case 8:// number of bytes == 8
              rdType = "float64";
              break;
            default:
              // throw an error
              System.out.println("Unable to read REAL data.");
              break;
          }
          break;
        default:
          // throw an error
          System.err.println("Unexpected number type \"" + dataType + "\" for \""
                  + nefisFile.getElmDef().get(j).getName() + "\"");
          break;
      }

      // initialize dimension for reading - (vs_let, 940)
      int[] rDimen = new int[2];
      switch (dataType) {
        case 1:// CHARACTE
          rDimen[0] = valBytes;
          rDimen[1] = eSize.get(0);
          break;
        case 2:// COMPLEX
          rDimen[0] = 2;
          rDimen[1] = eSize.get(0);
          break;
        case 3:// INTEGER
        case 4:// LOGICAL
        case 5:// FLOAT
          rDimen[0] = 1;// originally 1 - should this be 4 for the
          // byte size??
          rDimen[1] = eSize.get(0);
          break;
        default:
          rDimen[0] = -1;
          rDimen[1] = -1;
          break;
      }

      // //init the arrayList to hold the data - (time(x,y)) (vs_let 968);
      // List<List<Object>> data = new ArrayList<List<Object>>();
      // //don't need to initialize the internal lists because it's a
      // mutable ArrayList....do you??
      // for(int k = 0; k < readNGrp; k++){
      // data.add(new ArrayList<Object>());
      // }

      // Had an unnecessary sub-list in the "data" object
      List<Object> data = new ArrayList<Object>();

      // (vs_let, 1016)
      int cellOffset = 0;

      // Determine wether all group dimensions related to cell are
      // completely read. (vs_let, 1001)
      /**
       * Used to determine if GroupOptimization is possible - not
       * necessary for reading one var at a time skip to (vs_let, 1040)
       */
      // "SkipCellMask=[1 cumprod(gDimen(1:(end-1)))]*NBytesPerCell;"
      // (vs_let, 1047)
      int skipCellMask = 1;
      for (int k = 0; k < gDimen.size(); k++) {
        skipCellMask = skipCellMask * gDimen.get(k);
      }
      skipCellMask = skipCellMask * nBytesPerCell;

      /**
       * (vs_let, 1044) for k=1:length(gIndex),
       * CelOffset(:,k)=repmat(reshape
       * (repmat(gIndex{k},CPfw(k),1),SZgIndex(k)*CPfw(k),1),CPbw(k+1),1);
       * end;
       */
      cellOffset = (cellOffset - 1) * (skipCellMask);
      cellOffset = (cellOffset < 0) ? 0 : cellOffset;

      // (vs_let, 1049)
      int nrCells = 1;// count the number of cells that will be read (per
      // VD)
      int nrCellsPerRead = 1;// one cell per fread statement

      // determine the number of bytes to skip between fread statements.
      // (vs_let, 1066)
      // always one element
      int cellSkip = 0;// "CelSkip=[0; diff(CelOffset)-ElmBytes(1)];"
      // where "diff(CelOffset)-ElmBytes(1)" returns
      // []

      // determine the appropriate offset where the cell data is stored
      // (vs_let, 1086)
      // default the data record follows GROUP record in data file
      int offset = nefisFile.getGrpDat().get(i).getOffset() + 404;

      int[] lastVd = new int[]{-1, -1, -1, -1};
      int[][] pointerList = new int[4][0];// PointerList is a 4x256
      // multidimensional array
      if (vd == 1) {
        rafDat.seek(offset + 4);
        pointerList[0] = new int[256];
        pointerList[1] = new int[256];
        pointerList[2] = new int[256];
        pointerList[3] = new int[256];
        byteArray = new byte[4 * 256];
        rafDat.read(byteArray);
        byteBuffer = ByteBuffer.wrap(byteArray);
        byteBuffer.order(byteOrder).asIntBuffer().get(pointerList[3]);
      }

      // (vs_let, 1099)
      for (int varDimCnt = 0; varDimCnt < varDimCntMax; varDimCnt++) {
        if (debug) {
          System.out.println("varDim: " + varDimCnt + " of " + varDimCntMax);
        }
        if (vd == 1) {
          int temp = vdIndex.get(varDimCnt) + 1;
          int[] vdByte = new int[4];
          for (int k = 0; k < vdByte.length; k++) {
            vdByte[k] = (int) Math.floor((temp / P256[k]));
            vdByte[k] = (int) (vdByte[k] - 256 * Math.floor((vdByte[k] / 256)));
          }

          // (vs_let, 1111)
          int bt1;
          byteArray = new byte[4 * 256];
          if (debug) {
            System.out.println("byteArray built");
          }
          for (int bt = 3; bt > 0; bt--) {
            if (lastVd[bt] != vdByte[bt]) {
              bt1 = bt - 1;
              rafDat.seek(pointerList[bt][vdByte[bt]]);
              rafDat.read(byteArray);
              byteBuffer = ByteBuffer.wrap(byteArray);
              byteBuffer.order(byteOrder).asIntBuffer().get(pointerList[bt1]);
              lastVd[bt] = vdByte[bt];
              lastVd[bt1] = (int) nil;
            }
          }

          // (vs_let, 1125)
          offset = pointerList[0][vdByte[0]];

          // (vs_let, 1133)
          int vdCellOffset = (varDimCnt - 1) * vdOffset;
          // (vs_let, 1135)
          int alpha1 = (varDimCnt - 1) * nrCells;

          // (vs_let, 1138)
          if (vd == 1 & offset == nil) {// TODO: should this & be a
            // |???
            // reading from a pointer that has not been set is not
            // possible
            // zeros and blanks returned by default.
          } else {
            // (vs_let, 1145)
            // need to use try/catch instead of the "-1" returned in
            // matlab if the read is unsuccessful
            long status = 0;
            try {
              if (jn > 0) {
                // one large jump to start of first element
                rafDat.seek(offset + cellOffset);
                status = rafDat.getFilePointer();
              } else {
                // one large jump directly to start of element
                rafDat.seek(offset + cellOffset + elmOffset);
                status = rafDat.getFilePointer();
              }
            } catch (IOException ex) {
              status = -1;
            }

            // (vs_let, 1159) - can eliminate this and simply throw
            // the IOError....
            if (status < 0) {
            } else {
              if (debug) {
                System.out.println("status good");
              }
              // (vs_let, 1165) - not necessary, if group to cell
              // is 1 to 1....??
              for (int cell = 0; cell < cellOffset + 1; cell++) {
                if (debug) {
                  System.out.println("cell: " + cell + " of " + cellOffset);
                }
                int idxCell = cell + 1;
                // (vs_let, 1180) - cellSkip is always 0....
                // go to the appropriate offset for reading
                if (cellSkip > 0) {
                  rafDat.readShort();// is this right? - needs
                  // an "int8"...never
                  // gets here anyhow...
                }

                // (vs_let, 1184) - don't need outer loop
                // "for j1=jnm,"
                // read total dataset into temp
                if (debug) {
                  System.out.println("read total dataset into temp");
                }
                for (int j1 = 0; j1 < jnm; j1++) {
                  if (debug) {
                    System.out.println("j1: " + j1 + " of " + jnm);
                  }
                  int p = eSize.get(j1);
                  // read the data!!
                  // (vs_let, 1211)
                  if ((rdType.equals("float64"))) {// ||
                    // (rdType.equals("float32"))){
                    double[] tempDatD = new double[rDimen[1]];
                    byteArray = new byte[4 * rDimen[1]];
                    rafDat.read(byteArray);
                    byteBuffer = ByteBuffer.wrap(byteArray);

                    // if(rdType.equals("float32")){
                    // float[] tempDatF = new
                    // float[rDimen[1]];
                    // byteBuffer.order(byteOrder).
                    // asFloatBuffer().get(tempDatF);
                    // tempDatD = Utils.
                    // floatArrayToDoubleArray(tempDatF);
                    // }else{
                    // byteBuffer.order(byteOrder).
                    // asDoubleBuffer().get(tempDatD);
                    // }

                    if (eNIndex.length == 3) {// has
                      // "levels"...
                      double[][] tempDatF2 = new double[eNIndex[2]][eNIndex[0] * eNIndex[1]];
                      int ind = 0;
                      for (int r = 0; r < eNIndex[2]; r++) {
                        for (int d = 0; d < (eNIndex[0] * eNIndex[1]); d++) {
                          tempDatF2[r][d] = tempDatD[ind++];
                        }
                      }
                      data.add(tempDatF2);
                    } else if (eNIndex.length == 2) {// single
                      // number
                      // or
                      // normal
                      // array
                      double[][] tempDatF2 = new double[1][eNIndex[0] * eNIndex[1]];
                      tempDatF2[0] = tempDatD;
                      data.add(tempDatF2);
                    } else {// single number or normal array
                      data.add(tempDatD);
                    }
                  } else if (rdType.equals("float32")) {
                    float[] tempDatF = new float[rDimen[1]];
                    byteArray = new byte[4 * rDimen[1]];
                    rafDat.read(byteArray);
                    byteBuffer = ByteBuffer.wrap(byteArray);
                    byteBuffer.order(byteOrder).asFloatBuffer().get(tempDatF);

                    if (eNIndex.length == 3) {// has
                      // "levels"...
                      float[][] tempDatF2 = new float[eNIndex[2]][eNIndex[0] * eNIndex[1]];
                      int ind = 0;
                      for (int r = 0; r < eNIndex[2]; r++) {
                        for (int d = 0; d < (eNIndex[0] * eNIndex[1]); d++) {
                          tempDatF2[r][d] = tempDatF[ind++];
                        }
                      }
                      data.add(tempDatF2);
                    } else if (eNIndex.length == 2) {// 2D
                      // array
                      float[][] tempDatF2 = new float[1][eNIndex[0] * eNIndex[1]];
                      tempDatF2[0] = tempDatF;
                      data.add(tempDatF2);
                    } else {// single number or normal array
                      data.add(tempDatF);
                    }
                  } else if (rdType.equals("uchar")) {
                    byteArray = new byte[rDimen[0] * rDimen[1]];
                    rafDat.read(byteArray);
                    String tempString = new String(byteArray);
                    data.add(tempString);
                  } else if (rdType.equals("int32")) {
                    int[] tempDatI = new int[rDimen[1]];
                    byteArray = new byte[4 * rDimen[1]];
                    rafDat.read(byteArray);
                    byteBuffer = ByteBuffer.wrap(byteArray);
                    byteBuffer.order(byteOrder).asIntBuffer().get(tempDatI);
                    data.add(tempDatI);
                  }
                }
              }
            }
          }
        }
      }

      if (debug) {
        System.out.println("Return data");
        System.out.println();
      }
      return data;

    } catch (ArrayIndexOutOfBoundsException ex) {
      Logger.getLogger(NefisReader.class.getName()).log(Level.SEVERE, null, ex);
    } catch (FileNotFoundException ex) {
      Logger.getLogger(NefisReader.class.getName()).log(Level.SEVERE, null, ex);
    } catch (IOException ex) {
      Logger.getLogger(NefisReader.class.getName()).log(Level.SEVERE, null, ex);
      // }catch(OutOfMemoryError ex){
      // Logger.getLogger(NefisReader.class.getName()).
      // log(Level.SEVERE, null, ex);
    } catch (Exception ex) {
      Logger.getLogger(NefisReader.class.getName()).log(Level.SEVERE, null, ex);
    } finally {
      if (rafDat != null) {
        try {
          rafDat.close();
        } catch (IOException ex) {
          Logger.getLogger(NefisReader.class.getName()).log(Level.SEVERE, null, ex);
        }
      }
    }
    if (debug) {
      System.out.println("Return null");
      System.out.println();
    }
    return null;
  }

  /**
   *
   * This method opens the Nefis file and reads the group, cell and element
   * information into a NefisFile. The method is meant to replicate the
   * functionality of the Matlab vs_use function.
   *
   * @param fileLoc
   *            The location of the nefis file on the system.
   * @return The <code>NefisFile</code> object containing the information
   *         about the file.
   */
  public NefisFile initializeNefisFile(String fileLoc) {
    if (!new File(fileLoc).exists()) {
      System.out.println("No file called: " + fileLoc);
      return null;
    }
    RandomAccessFile rafDat = null;
    RandomAccessFile rafDef = null;

    NefisFile nf = new NefisFile();

    try {
      // determine if one file or many
      boolean oneFile = false;
      String fileExt = fileLoc.substring(fileLoc.lastIndexOf(".")).toLowerCase();
      if (fileExt.equals(".daf") | fileExt.equals(".dtf")) {
        oneFile = true;
        nf.setDatExt(fileExt);
        nf.setDefExt(fileExt);
      } else if (fileExt.equals(".dat") | fileExt.equals(".def")) {
        oneFile = false;
        nf.setDatExt(".dat");
        nf.setDefExt(".def");
      }
      // set the filename with the extention trimmed off
      nf.setFilename(fileLoc.replace(fileExt, ""));

      // this ensures that the data file is loaded
      rafDat = new RandomAccessFile(nf.getFilename() + nf.getDatExt(), "r");

      // get header and format
      byteArray = new byte[60];
      rafDat.read(byteArray);
      String header = new String(byteArray, "UTF-8");
      String hEnd = header.substring(header.length() - 1);
      header = header.substring(0, header.length() - 1).trim();

      // System.out.println("Header:\n" + header);
      // System.out.println("File Format:\n" + hEnd);

      nf.setFormat(hEnd);
      // if 'l' switch to little-endian, otherwise stick with big-endian
      if (hEnd.equals("l")) {
        byteOrder = ByteOrder.LITTLE_ENDIAN;
      }

      // get File Length
      int len = rafDat.readInt();
      // System.out.println("Length of datafile:\n" + len);

      // go to the location after the header and file length
      rafDat.seek((byteArray.length + 4));

      // Retrieve the non-empty Group Dat Buckets
      List<Integer> nonEmptyBuckets = getNonEmptyBuckets(rafDat);

      // System.out.println("Non-empty Group Dat \"buckets\":" +
      // nonEmptyBuckets.
      // size());

      // Read and build the GroupDat array
      boolean varDim = false;
      String x, name, def, ian, ran, san, sav;

      int offset, link, size, cellSize;
      int[] iaValues = new int[5];
      float[] raValues = new float[5];

      long fp;
      GroupData grpData = null;
      for (int i = 0; i < nonEmptyBuckets.size(); i++) {
        rafDat.seek(nonEmptyBuckets.get(i));
        offset = nonEmptyBuckets.get(i);
        link = rafDat.readInt();
        size = rafDat.readInt();

        byteArray = new byte[4];
        rafDat.read(byteArray);
        x = new String(byteArray).trim().toLowerCase();
        if (x.equals("5")) {
          varDim = true;
        }

        byteArray = new byte[1 * 16];
        rafDat.read(byteArray);
        name = new String(byteArray).trim();

        byteArray = new byte[1 * 16];
        rafDat.read(byteArray);
        def = new String(byteArray).trim();

        byteArray = new byte[5 * 16];
        rafDat.read(byteArray);
        ian = new String(byteArray).trim();

        byteArray = new byte[5 * 4];
        rafDat.read(byteArray);
        byteBuffer = ByteBuffer.wrap(byteArray);
        byteBuffer.order(byteOrder).asIntBuffer().get(iaValues);

        byteArray = new byte[5 * 16];
        rafDat.read(byteArray);
        ran = new String(byteArray).trim();

        byteArray = new byte[5 * 4];
        rafDat.read(byteArray);
        byteBuffer = ByteBuffer.wrap(byteArray);
        byteBuffer.order(byteOrder).asFloatBuffer().get(raValues);

        byteArray = new byte[5 * 16];
        rafDat.read(byteArray);
        san = new String(byteArray).trim();

        byteArray = new byte[5 * 16];
        rafDat.read(byteArray);
        sav = new String(byteArray).trim();

        if (varDim) {
          cellSize = rafDat.readInt();
        } else {
          cellSize = size - 404;
        }

        grpData = new GroupData();
        grpData.setName(name);
        grpData.setDefName(def);
        grpData.setOffset(offset);
        grpData.setVarDim((varDim) ? 1 : 0);
        grpData.setIANames(ian);
        grpData.setIAValues(iaValues);
        grpData.setRANames(ran);
        grpData.setRAValues(raValues);
        grpData.setSANames(san);
        grpData.setSAValues(sav);

        nf.addGroupData(grpData);
      }

      // read the def file
      rafDef = new RandomAccessFile(nf.getFilename() + nf.getDefExt(), "r");
      rafDef.seek(64 + 2 * 4 * 997);
      fp = rafDef.getFilePointer();

      // retrieve the non-empty Group Def buckets
      nonEmptyBuckets = getNonEmptyBuckets(rafDef);
      // System.out.println("Non-empty Group Def \"buckets\":" +
      // nonEmptyBuckets.
      // size());
      Collections.sort(nonEmptyBuckets);

      int cellIndex, nDims, sDim, oDim;

      int[] dims;
      Integer[] vDim;
      String code, cellName;
      GroupDef grpDef;
      for (int i = 0; i < nonEmptyBuckets.size(); i++) {
        rafDef.seek(nonEmptyBuckets.get(i));
        offset = nonEmptyBuckets.get(i);
        link = rafDef.readInt();
        size = rafDef.readInt();

        byteArray = new byte[1 * 4];
        rafDef.read(byteArray);
        code = new String(byteArray).trim();

        byteArray = new byte[1 * 16];
        rafDef.read(byteArray);
        name = new String(byteArray).trim();

        byteArray = new byte[1 * 16];
        rafDef.read(byteArray);
        cellName = new String(byteArray).trim();
        cellIndex = 0;

        byteArray = new byte[11 * 4];
        dims = new int[11];
        rafDef.read(byteArray);
        byteBuffer = ByteBuffer.wrap(byteArray);
        byteBuffer.order(byteOrder).asIntBuffer().get(dims);

        // TODO: figure out what the hell they're doing with vdim....
        List<Integer> vDimList = new ArrayList<Integer>();
        for (int j = 0; j < dims.length; j++) {
          if (dims[j] == 0) {
            vDimList.add(j);
          }
        }
        vDim = new Integer[vDimList.size()];
        vDimList.toArray(vDim);

        nDims = dims[0];
        sDim = dims[1];
        oDim = dims[6];

        // BAIL IF MULTIPLE DIMENSIONS
        if (nDims != 1 & vDim.length != 1) {
          System.out.println("AHHH....Multiple Dimensions!!: " + nDims + " " + vDim.length);
          return null;
        }

        grpDef = new GroupDef();
        grpDef.setName(name);
        grpDef.setCelName(cellName);
        grpDef.setCelIndex(cellIndex);
        grpDef.setOffset(offset);
        grpDef.addSizeDim(sDim);
        grpDef.setOrderDim(oDim);

        GroupData gd;
        for (int j = 0; j < nf.getGrpDat().size(); j++) {
          gd = nf.getGrpDat().get(j);
          if (grpDef.getName().equals(gd.getDefName())) {
            gd.setDefIndex(i);
            gd.addSizeDim(sDim);
            gd.setOrderDim(oDim);

            // TODO: do something with the vDim....
            gd.setVarDim(vDim[0]);
            // determine variable dimension size by scanning
            // the pointer list in the data file
            rafDat.seek(gd.getOffset() + 404 + 4);
            fp = rafDat.getFilePointer();

            int nByte = 3, dim = 0;
            int k = -1;
            int[] pList;
            while (nByte >= 0) {
              byteArray = new byte[256 * 4];
              pList = new int[256];
              rafDat.read(byteArray);
              byteBuffer = ByteBuffer.wrap(byteArray);
              byteBuffer.order(byteOrder).asIntBuffer().get(pList);
              for (int s = 0; s < pList.length; s++) {
                if (pList[s] != nil) {
                  if (s > k) {
                    k = s;
                  }
                }
              }
              if (k == -1) {
                break;
              }

              if (nByte > 0) {
                dim = dim + (k) * (int) Math.pow(256, nByte);// 256
                // ^
                // nByte;
                rafDat.seek(pList[k]);
              } else {
                dim = dim + k;
              }
              nByte = nByte - 1;
            }
            gd.setSizeDim(vDim[0] - 1, dim);
          }
        }

        nf.addGroupDef(grpDef);
      }

      // retrieve the CELL definition
      // rafDef.seek(132 + 4 * 997);//one file
      rafDef.seek(64 + 4 * 997);

      // retrieve the non-empty Cell Def buckets
      nonEmptyBuckets = getNonEmptyBuckets(rafDef);
      // System.out.println("Non-empty Cell Def \"buckets\":" +
      // nonEmptyBuckets.
      // size());

      Collections.sort(nonEmptyBuckets);

      int totalNumElm = 0, numElm;
      CellDef cellDef;
      GroupDef gd;
      for (int i = 0; i < nonEmptyBuckets.size(); i++) {
        rafDef.seek(nonEmptyBuckets.get(i));

        link = rafDef.readInt();
        size = rafDef.readInt();

        byteArray = new byte[1 * 4];
        rafDef.read(byteArray);
        code = new String(byteArray).trim();

        byteArray = new byte[1 * 16];
        rafDef.read(byteArray);
        name = new String(byteArray).trim();

        cellSize = rafDef.readInt();
        numElm = rafDef.readInt();

        // add info to a CellDef object
        cellDef = new CellDef();
        cellDef.setOffset(nonEmptyBuckets.get(i));
        cellDef.setName(name);

        // fill the elementList with 0s
        for (int j = 0; j < numElm; j++) {
          cellDef.addElement(0);
        }

        // populate the celIndex property of the appropriate GroupDef
        for (int j = 0; j < nf.getGrpDef().size(); j++) {
          gd = nf.getGrpDef().get(j);
          if (cellDef.getName().equals(gd.getCelName())) {
            gd.setCelIndex(i);
            totalNumElm = totalNumElm + numElm;
          }
        }

        nf.addCellDef(cellDef);
      }

      // retrieve the ELEMENT definition
      // rafDef.seek(132);//one file
      rafDef.seek(64);

      // retrieve the non-empty Element Def buckets
      nonEmptyBuckets = getNonEmptyBuckets(rafDef);
      // System.out.println("Non-empty Element Def \"buckets\":" +
      // nonEmptyBuckets.
      // size());

      Collections.sort(nonEmptyBuckets);

      String sType, desc;

      int type, sizeElm, sizeVal, nDim;
      int[] elmDims;
      ElementDef elmDef = null;
      for (int i = 0; i < nonEmptyBuckets.size(); i++) {
        rafDef.seek(nonEmptyBuckets.get(i));

        link = rafDef.readInt();
        size = rafDef.readInt();

        byteArray = new byte[1 * 4];
        rafDef.read(byteArray);
        code = new String(byteArray).trim();

        byteArray = new byte[1 * 16];
        rafDef.read(byteArray);
        name = new String(byteArray).trim();

        byteArray = new byte[1 * 8];
        rafDef.read(byteArray);
        sType = new String(byteArray).trim();
        if (sType.equals("CHARACTE")) {
          type = 1;
        } else if (sType.equals("COMPLEX")) {
          type = 2;
        } else if (sType.equals("INTEGER")) {
          type = 3;
        } else if (sType.equals("LOGICAL")) {
          type = 4;
        } else if (sType.equals("REAL")) {
          type = 5;
        } else {
          type = 0;// unknown
        }

        sizeElm = rafDef.readInt();
        sizeVal = rafDef.readInt();

        byteArray = new byte[1 * 96];
        rafDef.read(byteArray);
        desc = new String(byteArray);

        byteArray = new byte[6 * 4];
        elmDims = new int[6];
        rafDef.read(byteArray);
        byteBuffer = ByteBuffer.wrap(byteArray);
        byteBuffer.order(byteOrder).asIntBuffer().get(elmDims);

        // add info to an ElementDef object
        elmDef = new ElementDef();
        elmDef.setOffset(nonEmptyBuckets.get(i));
        elmDef.setName(name);
        elmDef.setType(type);
        elmDef.setSizeVal(sizeVal);
        elmDef.setSizeElm(sizeElm);
        elmDef.setQuantity(desc.substring(0, 15).trim());
        elmDef.setUnits(desc.substring(16, 31).trim());
        elmDef.setDescription(desc.substring(32, 95).trim());
        nDim = elmDims[0];

        // perform the "VS.ElmDef(i).Size=Dimens(2:(1+NDim));"
        // functionality (vs_use, 916)
        List<Integer> sList = new ArrayList<Integer>();
        for (int j = 1; j < 1 + nDim; j++) {
          sList.add(elmDims[j]);
        }
        elmDef.setSize(sList);

        nf.addElementDef(elmDef);
      }

      // reading elements from cell definition
      List<String> elmNames = nf.getAllElementDefNames();
      List<Integer> matches;
      CellDef cd;
      for (int i = 0; i < nf.getCelDef().size(); i++) {
        cd = nf.getCelDef().get(i);
        rafDef.seek(cd.getOffset() + 36);

        for (int j = 0; j < cd.getElm().size(); j++) {
          matches = new ArrayList<Integer>();
          byteArray = new byte[1 * 16];
          rafDef.read(byteArray);
          name = new String(byteArray).trim();
          // perform the "j=strmatch(Name,ElmNames,'exact');" function
          // (vs_use, 939)
          for (int k = 0; k < elmNames.size(); k++) {
            if (name.equals(elmNames.get(k))) {
              matches.add(k);
            }
          }
          if (matches.size() > 0) {
            if (matches.size() > 1) {
              matches = new ArrayList<Integer>();
              matches.add(0);// TODO: Should this be the first
              // element of 'matches' rather than
              // just 0??
            }
            cd.setElm(j, matches.get(0));
          }
        }
      }

      // TODO: make vs_type functionality
      nf.setSubType(NefisUtils.determineSubType(nf));

      // DONE READING FILE

      return nf;
    } catch (FileNotFoundException ex) {
      Logger.getLogger(NefisReader.class.getName()).log(Level.SEVERE, null, ex);
    } catch (IOException ex) {
      Logger.getLogger(NefisReader.class.getName()).log(Level.SEVERE, null, ex);
    } finally {
      if (rafDat != null) {
        try {
          rafDat.close();
        } catch (IOException ex) {
          Logger.getLogger(NefisReader.class.getName()).log(Level.SEVERE, null, ex);
        }
      }
      if (rafDef != null) {
        try {
          rafDef.close();
        } catch (IOException ex) {
          Logger.getLogger(NefisReader.class.getName()).log(Level.SEVERE, null, ex);
        }
      }
    }

    return null;
  }

  private static List<Integer> getNonEmptyBuckets(RandomAccessFile raf) throws IOException {
    // ReadHashTableGetNonEmptyBuckets
    byte[] hashBytes = new byte[997 * 4];
    raf.read(hashBytes);
    int[] hashTable = new int[997];
    byteBuffer = ByteBuffer.wrap(hashBytes);
    byteBuffer.order(byteOrder).asIntBuffer().get(hashTable);

    // perform "Buck=find(HashTable~=Nil);" & "NonEmptyBuck=HashTable(Buck)"
    // - (vs_use, 1032)
    List<Integer> nonEmptyBuck = new ArrayList<Integer>();
    for (int i = 0; i < hashTable.length; i++) {
      if (hashTable[i] != nil) {// & hashTable[i] != 0){
        nonEmptyBuck.add(hashTable[i]);
      }
    }
    int link;
    // int[] dummy;
    // String label;
    for (int i = 0; i < nonEmptyBuck.size(); i++) {
      raf.seek(nonEmptyBuck.get(i));
      link = raf.readInt();

      // from vs_use - but unnecessary if not checking bucket
      // (complicated)
      // byteArray = new byte[1 * 2];
      // dummy = new int[2];
      // raf.read(byteArray);
      // byteBuffer = ByteBuffer.wrap(byteArray);
      // byteBuffer.order(byteOrder).asIntBuffer().get(dummy);
      //
      // byteArray = new byte[1 * 16];
      // raf.read(byteArray);
      // label = new String(byteArray).trim();

      if (link != -1) {
        nonEmptyBuck.add(link);
      }
    }

    return nonEmptyBuck;
  }

  public NefisFile getNefisFile() {
    return nefisFile;
  }

  public void setNefisFile(NefisFile nefisFile) {
    this.nefisFile = nefisFile;
  }

  public List<Integer> getNonNullRecordIndexes() {
    List<Integer> ret = new ArrayList<Integer>();
    if (uVals.get(0) instanceof double[][]) {
      double[][] us = (double[][]) uVals.get(0);
      double[][] vs = (double[][]) vVals.get(0);
      for (int i = 0; i < xVals.length; i++) {
        /**
         * TODO: Make this more robust so that it deals with all times &
         * all levels
         */
        if (xVals[i] == 0 & yVals[i] == 0 & us[0][i] == 0 & vs[0][i] == 0) {
          // do not add the index
        } else {
          ret.add(i);
        }
      }
    } else if (uVals.get(0) instanceof float[][]) {
      float[][] us = (float[][]) uVals.get(0);
      float[][] vs = (float[][]) vVals.get(0);
      for (int i = 0; i < xVals.length; i++) {
        /**
         * TODO: Make this more robust so that it deals with all times &
         * all levels
         */
        if (xVals[i] == 0 & yVals[i] == 0 & us[0][i] == 0 & vs[0][i] == 0) {
          // do not add the index
        } else {
          ret.add(i);
        }
      }
    }

    if (ret.size() == 0) {
      ret = null;
    }
    return ret;
  }

  public float[] getXVals() {
    return xVals;
  }

  public float[] getYVals() {
    return yVals;
  }

  public float[] getZVals() {
    return zVals;
  }

  // public double[] getXVals() {
  // return xVals;
  // }
  //
  // public double[] getYVals() {
  // return yVals;
  // }
  //
  // public double[] getZVals() {
  // return zVals;
  // }
  public List<float[][]> getUVals() {
    return uVals;
  }

  public List<float[][]> getVVals() {
    return vVals;
  }

  // public List<double[][]> getUVals() {
  // return uVals;
  // }
  //
  // public List<double[][]> getVVals() {
  // return vVals;
  // }
  public String[] getXMeta() {
    return xMeta;
  }

  public String[] getYMeta() {
    return yMeta;
  }

  public String[] getZMeta() {
    return zMeta;
  }

  public String[] getUMeta() {
    return uMeta;
  }

  public String[] getVMeta() {
    return vMeta;
  }

  public long[] getTimes() {
    return times;
  }

  public long getStartTime() {
    return times[0];
  }

  public long getEndTime() {
    return times[times.length - 1];
  }

  public GregorianCalendar getStartCal() {
    return startCal;
  }

  public long getTimeIncrement() {
    return timeIncrement;
  }

  public int getNumLevels() {
    return numLevels;
  }

  public float[] getRotCorrVals() {
    return rotCorrVals;
  }

  // public double[] getXGrid() {
  // return xGrid;
  // }
  public float[] getXGrid() {
    return xGrid;
  }

  // public double[] getYGrid() {
  // return yGrid;
  // }
  public float[] getYGrid() {
    return yGrid;
  }

  public int getNMax() {
    return nMax;
  }

  public int getMMax() {
    return mMax;
  }

  public String[] getXgMeta() {
    return xgMeta;
  }

  public String[] getYgMeta() {
    return ygMeta;
  }

  public long getFileSize() {
    return fileSize;
  }
}
