/*
 * ParticleOutputReader.java
 *
 * Created on November 7, 2007, 9:03 AM
 *
 * Applied Science Associates, Inc.
 * Copyright 2007.  All rights reserved.
 */
package com.asascience.edc.particle;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import ucar.ma2.Array;
import ucar.ma2.Index;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;
import ucar.nc2.dataset.NetcdfDataset;
import ucar.unidata.geoloc.LatLonPointImpl;
import ucar.unidata.geoloc.LatLonRect;

import com.asascience.openmap.layer.nc.NetcdfKeysBase;
import com.asascience.openmap.omgraphic.OMParticle;
import com.asascience.utilities.exception.InitializationFailedException;
import com.bbn.openmap.proj.Projection;

/**
 * 
 * @author CBM
 */
public class ParticleOutputReader extends OutputReaderBase {

  // variable tags
  public static final String PH = "ph";
  public static final String RAD = "radius";
  public static final String RADZ = "radius_z";
  public static final String CCONC = "centerconc";
  public static final String ECONC = "edgeconc";
  public static final String MASS = "mass";
  public static final String RUNID = "runid";
  public static final String CONCENTRATION = "concentration";
  public static final String HIGH_LIKELIHOOD = "high_likelihood";
  public static final String LOW_LIKELIHOOD = "low_likelihood";
  // global attribute tags
  public static final String CHEMICAL = "chemical";
  public static final String MOLECULAR_WEIGHT = "molecular_weight";
  public static final String LD50CONC = "ld50conc";
  public static final String INITMASS = "initmass";
  public static final String GRID_TYPE = "grid_type";
  public static final String DELTA_X = "delta_x";
  public static final String DELTA_Y = "delta_y";
  public static final String DELTA_Z = "delta_z";
  public static final String NUMRUNS = "numruns";
  public static final String OUTPUT_NAME = "modelOutParticle.nc";
  private NetcdfFile ncFile;
  private Variable timeVar;
  private Variable partVar;
  private Variable latVar;
  private Variable lonVar;
  private Variable depthVar;
  private Variable pHVar;
  private Variable radVar;
  private Variable radZVar;
  private Variable centerConcVar;
  private Variable edgeConcVar;
  private Variable massVar;
  private Array timeArray;
  private Index timeIndex;
  private Array partArray;
  private int numParticles;

  /**
   * Creates a new instance of ParticleOutputReader
   *
   * @param dataFile
   * @throws com.asascience.utilities.exception.InitializationFailedException
   */
  public ParticleOutputReader(File dataFile) throws InitializationFailedException {
    try {
      this.dataFile = dataFile;
      // ncDataset =
      // NetcdfDataset.acquireDataset(dataFile.getAbsolutePath(), null);
      ncFile = NetcdfDataset.acquireFile(this.dataFile.getAbsolutePath(), null);
      if (ncFile != null) {
        if (!initializeDataset()) {
          throw new InitializationFailedException(
                  "The dataset is malformed or not an ASA Particle Netcdf file.");
        }
      }

    } catch (IOException ex) {
      ex.printStackTrace();
    }
  }

  public ParticleOutputReader(String dataFile) throws InitializationFailedException {
    this(new File(dataFile));
  }

  private boolean initializeDataset() {
    try {
      // System.out.println("initDataset: \"" + dataFile.getAbsolutePath()
      // + "\"");

      // get the variables
      timeVar = ncFile.findVariable(NetcdfKeysBase.TIME);
      partVar = ncFile.findVariable(NetcdfKeysBase.PARTICLE);
      latVar = ncFile.findVariable(NetcdfKeysBase.LAT);
      lonVar = ncFile.findVariable(NetcdfKeysBase.LON);
      depthVar = ncFile.findVariable(NetcdfKeysBase.DEPTH);
      pHVar = ncFile.findVariable(ParticleOutputReader.PH);
      radVar = ncFile.findVariable(ParticleOutputReader.RAD);
      radZVar = ncFile.findVariable(ParticleOutputReader.RADZ);
      centerConcVar = ncFile.findVariable(ParticleOutputReader.CCONC);
      edgeConcVar = ncFile.findVariable(ParticleOutputReader.ECONC);
      massVar = ncFile.findVariable(ParticleOutputReader.MASS);

      // get the time & particle data
      timeArray = timeVar.read();
      timeIndex = timeArray.getIndex();
      times = new ArrayList<Long>();
      for (int i = 0; i < timeArray.getSize(); i++) {
        timeIndex.set(i);
        times.add(timeArray.getLong(timeIndex) * 1000);// convert to
        // milliseconds
      }
      partArray = partVar.read();
      numParticles = partArray.getShape()[0];

      this.startTime = times.get(0);
      this.endTime = times.get(times.size() - 1);
      this.timeIncrement = times.get(1) - times.get(0);

      // //set the start & end time for use by the TimeLayer
      // timeIndex.set(0);
      // this.startTime = timeArray.getLong(timeIndex) * 1000;//convert to
      // milliseconds
      // timeIndex.set(timeArray.getShape()[0] - 1);
      // this.endTime = timeArray.getLong(timeIndex) * 1000;//convert to
      // milliseconds
      //
      // //set the increment for use by the TimeLayer
      // timeIndex.set(1);
      // this.timeIncrement = (timeArray.getLong(timeIndex) * 1000 -
      // this.startTime);//convert to milliseconds
      // // timeIncrement =
      // ncFile.findGlobalAttribute(this.INCREMENT).getNumericValue().longValue();

      chemical = ncFile.findGlobalAttribute(ParticleOutputReader.CHEMICAL).getStringValue();
      molecWeight = ncFile.findGlobalAttribute(ParticleOutputReader.MOLECULAR_WEIGHT).getNumericValue().doubleValue();
      ld50 = ncFile.findGlobalAttribute(ParticleOutputReader.LD50CONC).getNumericValue().doubleValue();
      initMass = ncFile.findGlobalAttribute(ParticleOutputReader.INITMASS).getNumericValue().doubleValue();
      return true;
    } catch (IOException ex) {
      // TODO: if it's a bad nc file - don't use it...
      System.out.println("POR:initialize: - Error initializing dataset");
      ex.printStackTrace();
    } catch (IndexOutOfBoundsException ex) {
    }
    return false;
  }

  public List<SimpleParticle> getSimpleParticlesAtTime(long queryTime) {
    try {
      int tIndex = getTimeIndex(queryTime);
      if (tIndex == -1) {
        return null;
      }

      List<SimpleParticle> particles = new ArrayList<SimpleParticle>();

      int[] origin = new int[]{tIndex, 0};
      int[] size = new int[]{1, numParticles};// to get 1 timestep of
      // data
      SimpleParticle particle;

      double[] lats = (double[]) latVar.read(origin, size).get1DJavaArray(double.class);
      double[] lons = (double[]) lonVar.read(origin, size).get1DJavaArray(double.class);
      double[] depths = (double[]) depthVar.read(origin, size).get1DJavaArray(double.class);
      double[] phs = (double[]) pHVar.read(origin, size).get1DJavaArray(double.class);
      double[] rads = (double[]) radVar.read(origin, size).get1DJavaArray(double.class);
      double[] radzs = (double[]) radZVar.read(origin, size).get1DJavaArray(double.class);
      double[] ccs = (double[]) centerConcVar.read(origin, size).get1DJavaArray(double.class);
      double[] ecs = (double[]) edgeConcVar.read(origin, size).get1DJavaArray(double.class);
      double[] masses = (double[]) massVar.read(origin, size).get1DJavaArray(double.class);

      for (int i = 0; i < numParticles; i++) {
        particle = new SimpleParticle(lats[i], lons[i], depths[i], phs[i], rads[i], radzs[i], ccs[i], ecs[i],
                masses[i]);
        particles.add(particle);
      }

      return particles;
    } catch (InvalidRangeException ex) {
      ex.printStackTrace();
    } catch (Exception ex) {
      ex.printStackTrace();
    }
    return null;
  }

  public List<MassParticle> getMassParticlesAtTime(long queryTime) {
    try {
      int tIndex = getTimeIndex(queryTime);
      if (tIndex == -1) {
        return null;
      }

      List<MassParticle> particles = new ArrayList<MassParticle>();

      int[] origin = new int[]{tIndex, 0};
      int[] size = new int[]{1, numParticles};// to get 1 timestep of
      // data
      MassParticle particle;

      double[] lats = (double[]) latVar.read(origin, size).get1DJavaArray(double.class);
      double[] lons = (double[]) lonVar.read(origin, size).get1DJavaArray(double.class);
      double[] depths = (double[]) depthVar.read(origin, size).get1DJavaArray(double.class);
      double[] masses = (double[]) massVar.read(origin, size).get1DJavaArray(double.class);

      for (int i = 0; i < numParticles; i++) {
        particle = new MassParticle(lats[i], lons[i], depths[i], masses[i], molecWeight);
        if (!particle.isOnBottom() & !particle.isOnLand()) {
          particles.add(particle);
        }
      }

      // <editor-fold defaultstate="collapsed" desc=" Old Method ">

      // double lat, lon, depth, mass;
      // Array data;
      // // System.out.println("partArray size:"+partArray.getSize());
      // for(int i = 0; i < numParticles; i++){
      // origin[1] = i;//set the origin index to the appropriate particle
      // data = latVar.read(origin, size);
      // lat = data.getDouble(data.getIndex());
      //
      // data = lonVar.read(origin, size);
      // lon = data.getDouble(data.getIndex());
      //
      // data = depthVar.read(origin, size);
      // depth = data.getDouble(data.getIndex());
      //
      // data = massVar.read(origin, size);
      // mass = data.getDouble(data.getIndex());
      //
      //
      // particle = new MassParticle(lat, lon, depth, mass, molecWeight);
      //
      // //if the particle is on the bottom, or on land, don't return
      // it...can't because the # of particles changes...
      // if(!particle.isOnBottom() & !particle.isOnLand()){
      // particles.add(particle);
      // }
      // }

      // </editor-fold>

      return particles;
    } catch (InvalidRangeException ex) {
      ex.printStackTrace();
    } catch (Exception ex) {
      ex.printStackTrace();
    }
    return null;
  }

  public int getNumberOfParticles() {
    return numParticles;
  }

  private int getParticleIndex(int particleID) {
    Index ind = partArray.getIndex();
    for (int i = 0; i < numParticles; i++) {
      if (particleID == partArray.getInt(ind.set(i))) {
        return i;
      }
    }
    return -1;
  }

  /**
   * @param particleID
   * @return double[][] where the array in the first position (i.e. [0][])
   *         contains the latitude positions, the second ([1][]) contains the
   *         longitude positions, and the last ([2][]) contains the depth
   *         positions.
   */
  public double[][] getParticleLocations(int particleID) {
    try {

      int partIndex = getParticleIndex(particleID);
      if (partIndex == -1) {
        return null;
      }

      double[][] ret = new double[3][];

      int[] origin = new int[]{0, partIndex};
      int[] size = new int[]{times.size(), 1};

      ret[0] = (double[]) latVar.read(origin, size).get1DJavaArray(double.class);
      ret[1] = (double[]) lonVar.read(origin, size).get1DJavaArray(double.class);
      ret[2] = (double[]) depthVar.read(origin, size).get1DJavaArray(double.class);

      return ret;
    } catch (IOException ex) {
      ex.printStackTrace();
    } catch (InvalidRangeException ex) {
      ex.printStackTrace();
    }
    return null;
  }

  public List<double[]> getParticleTimeseries(int particleID) {
    try {
      int partIndex = getParticleIndex(particleID);
      if (partIndex == -1) {
        return null;
      }

      List<double[]> dataList = new ArrayList<double[]>();

      int[] origin = new int[]{0, partIndex};
      int[] size = new int[]{times.size(), 1};

      double[] cConcs = (double[]) centerConcVar.read(origin, size).get1DJavaArray(double.class);
      // double[] eConcs = (double[])edgeConcVar.read(origin,
      // size).get1DJavaArray(double.class);

      for (int i = 1; i < times.size(); i++) {
        if (cConcs[i] > 0d) {
          dataList.add(new double[]{times.get(i), cConcs[i]});
        }
      }

      // <editor-fold defaultstate="collapsed" desc=" Old Method ">

      // double time, data1, data2;
      // Array arr;
      // for(int i = 1; i < times.size(); i++){
      // origin[0] = i;//set the origin index to the appropriate timestep
      // arr = centerConcVar.read(origin, size);
      // data1 = arr.getDouble(arr.getIndex());
      //
      // arr = edgeConcVar.read(origin, size);
      // data2 = arr.getDouble(arr.getIndex());
      //
      // time = times.get(i);
      // dataList.add(new double[]{time, data1});
      // }

      // </editor-fold>

      return dataList;
    } catch (IOException ex) {
      ex.printStackTrace();
    } catch (InvalidRangeException ex) {
      ex.printStackTrace();
    }
    return null;
  }

  public List<OMParticle> getOMParticlesAtTime(long queryTime, Projection proj) {
    try {
      int tIndex = getTimeIndex(queryTime);
      // System.out.println(tIndex);
      if (tIndex == -1) {
        return null;
      }

      List<OMParticle> particles = new ArrayList();

      int[] origin = new int[]{tIndex, 0};
      int[] size = new int[]{1, numParticles};// to get 1 timestep of
      // data

      // int[] parts = (int[])partVar.read().get1DJavaArray(int.class);
      double[] lats = (double[]) latVar.read(origin, size).get1DJavaArray(double.class);
      double[] lons = (double[]) lonVar.read(origin, size).get1DJavaArray(double.class);
      double[] depths = (double[]) depthVar.read(origin, size).get1DJavaArray(double.class);
      double[] pHs = (double[]) pHVar.read(origin, size).get1DJavaArray(double.class);
      double[] rads = (double[]) radVar.read(origin, size).get1DJavaArray(double.class);
      double[] cConcs = (double[]) centerConcVar.read(origin, size).get1DJavaArray(double.class);
      double[] eConcs = (double[]) edgeConcVar.read(origin, size).get1DJavaArray(double.class);

      OMParticle particle;
      for (int i = 0; i < numParticles; i++) {
        particle = new OMParticle(proj, (i + 1), lons[i], lats[i], depths[i], pHs[i], rads[i], cConcs[i],
                eConcs[i]);
        particles.add(particle);
      }

      // <editor-fold defaultstate="collapsed" desc=" Old Method ">

      // int id;
      // double lat, lon, depth, pH, rad, cconc, econc;
      // Array data;
      //
      // // System.out.println("partArray size:"+partArray.getSize());
      // for(int i = 0; i < numParticles; i++){
      // origin[1] = i;//set the origin index to the appropriate particle
      //
      // data = partVar.read(new int[]{i}, new int[]{1});
      // id = data.getInt(data.getIndex());
      //
      // data = latVar.read(origin, size);
      // lat = data.getDouble(data.getIndex());
      //
      // data = lonVar.read(origin, size);
      // lon = data.getDouble(data.getIndex());
      //
      // data = depthVar.read(origin, size);
      // depth = data.getDouble(data.getIndex());
      //
      // data = pHVar.read(origin, size);
      // pH = data.getDouble(data.getIndex());
      //
      // data = radVar.read(origin, size);
      // rad = data.getDouble(data.getIndex());
      //
      // data = centerConcVar.read(origin, size);
      // cconc = data.getDouble(data.getIndex());
      //
      // data = edgeConcVar.read(origin, size);
      // econc = data.getDouble(data.getIndex());
      //
      // // System.out.println("proj:"+proj);
      // // System.out.println("particle:"+i);
      // // System.out.println("lat:"+lat);
      // // System.out.println("lon:"+lon);
      // // System.out.println("ph:"+pH);
      // // System.out.println("rad:"+rad);
      // // System.out.println("cconc:"+cconc);
      // // System.out.println("econc:"+econc);
      //
      // particle = new OMParticle(proj, id, lon, lat, depth, pH, rad,
      // cconc, econc);
      // particles.add(particle);
      // }

      // </editor-fold>

      return particles;
    } catch (InvalidRangeException ex) {
      ex.printStackTrace();
    } catch (IOException ex) {
      ex.printStackTrace();
    }

    return null;
  }

  public double[] getDepthRangeAtTime(long queryTime) {
    // TODO: get the min/max depths from the file
    try {
      int tIndex = getTimeIndex(queryTime);

      if (tIndex == -1) {
        return null;
      }

      int[] origin = new int[]{tIndex, 0};
      int[] size = new int[]{1, numParticles};
      double top = Double.NEGATIVE_INFINITY;
      double bot = Double.POSITIVE_INFINITY;

      double[] depths = (double[]) depthVar.read(origin, size).get1DJavaArray(double.class);

      for (double d : depths) {
        if (d == -9999999) {
          continue;
        }
        top = (d > top) ? d : top;
        bot = (d < bot) ? d : bot;
      }

      // <editor-fold defaultstate="collapsed" desc=" Old Method ">

      // double temp;
      // Array data;
      // for(int i = 0; i < partArray.getSize(); i++){
      // origin[1] = i;
      //
      // data = depthVar.read(origin, size);
      // temp = data.getDouble(data.getIndex());
      // if(temp == -9999999){
      // continue;
      // }
      // top = (top < temp) ? temp : top;
      // bot = (bot > temp) ? temp : bot;
      // }

      // </editor-fold>

      return new double[]{top, bot};
    } catch (IOException ex) {
      Logger.getLogger(ParticleOutputReader.class.getName()).log(Level.SEVERE, null, ex);
    } catch (InvalidRangeException ex) {
      Logger.getLogger(ParticleOutputReader.class.getName()).log(Level.SEVERE, null, ex);
    }

    return null;
  }

  public double[] getDepthRange() {
    try {
      double top = Double.NEGATIVE_INFINITY;
      double bot = Double.POSITIVE_INFINITY;

      int[] origin = new int[]{0, 0};
      int[] size = new int[]{times.size(), numParticles};

      double[] depths = (double[]) depthVar.read(origin, size).get1DJavaArray(double.class);

      for (double d : depths) {
        if (d == -9999999) {
          continue;
        }
        top = (d > top) ? d : top;
        bot = (d < bot) ? d : bot;
      }

      // <editor-fold defaultstate="collapsed" desc=" Old Method ">

      // double[] temp;
      // for(long t : times){
      // temp = getDepthRangeAtTime(t);
      // top = (top < temp[0]) ? temp[0] : top;
      // bot = (bot > temp[1]) ? temp[1] : bot;
      // }

      // </editor-fold>

      return new double[]{top, bot};
    } catch (IOException ex) {
      Logger.getLogger(ParticleOutputReader.class.getName()).log(Level.SEVERE, null, ex);
    } catch (InvalidRangeException ex) {
      Logger.getLogger(ParticleOutputReader.class.getName()).log(Level.SEVERE, null, ex);
    }
    return null;
  }

  public LatLonRect getExtentAtTime(long queryTime) {
    try {
      int tIndex = getTimeIndex(queryTime);

      if (tIndex == -1) {
        return null;
      }

      int[] origin = new int[]{tIndex, 0};
      int[] size = new int[]{1, numParticles};
      double latMin = Double.POSITIVE_INFINITY, lonMin = Double.POSITIVE_INFINITY, latMax = Double.NEGATIVE_INFINITY, lonMax = Double.NEGATIVE_INFINITY;

      double[] lats = (double[]) latVar.read(origin, size).get1DJavaArray(double.class);
      double[] lons = (double[]) lonVar.read(origin, size).get1DJavaArray(double.class);

      for (double d : lats) {
        latMin = (d < latMin) ? d : latMin;
        latMax = (d > latMax) ? d : latMax;
      }
      for (double d : lons) {
        lonMin = (d < lonMin) ? d : lonMin;
        lonMax = (d > lonMax) ? d : lonMax;
      }

      // <editor-fold defaultstate="collapsed" desc=" Old Method ">

      // double tempVal;
      // Array data;
      // for(int i = 0; i < partArray.getSize(); i++){
      // origin[1] = i;
      //
      // data = latVar.read(origin, size);
      // tempVal = data.getDouble(data.getIndex());
      // if(tempVal != -9999999){
      // latMin = (latMin <= tempVal) ? latMin : tempVal;
      // latMax = (latMax > tempVal) ? latMax : tempVal;
      // }
      //
      // data = lonVar.read(origin, size);
      // tempVal = data.getDouble(data.getIndex());
      // if(tempVal != -9999999){
      // lonMin = (lonMin <= tempVal) ? lonMin : tempVal;
      // lonMax = (lonMax > tempVal) ? lonMax : tempVal;
      // }
      //
      // // System.out.println("Particle: " + i);
      // // System.out.println("  Lat Min: " + latMin);
      // // System.out.println("  Lat Max: " + latMax);
      // // System.out.println("  Lon Min: " + lonMin);
      // // System.out.println("  Lon Max: " + lonMax);
      // }

      // </editor-fold>

      return new LatLonRect(new LatLonPointImpl(latMin, lonMin), new LatLonPointImpl(latMax, lonMax));
    } catch (Exception ex) {
      ex.printStackTrace();
    }

    return null;
  }

  public LatLonRect getFullExtent() {
    if (fullExtent != null) {
      return fullExtent;
    }
    try {

      double latMin = Double.POSITIVE_INFINITY, lonMin = Double.POSITIVE_INFINITY, latMax = Double.NEGATIVE_INFINITY, lonMax = Double.NEGATIVE_INFINITY;

      int[] origin = new int[]{0, 0};
      int[] size = new int[]{times.size(), numParticles};

      double[] lats = (double[]) latVar.read(origin, size).get1DJavaArray(double.class);
      double[] lons = (double[]) lonVar.read(origin, size).get1DJavaArray(double.class);

      for (double d : lats) {
        latMin = (d < latMin) ? d : latMin;
        latMax = (d > latMax) ? d : latMax;
      }
      for (double d : lons) {
        lonMin = (d < lonMin) ? d : lonMin;
        lonMax = (d > lonMax) ? d : lonMax;
      }

      // <editor-fold defaultstate="collapsed" desc=" Old Method ">

      // LatLonRect tempLlr;
      // for(long t : times){
      // tempLlr = getExtentAtTime(t);
      //
      // latMin = (latMin <= tempLlr.getLatMin()) ? latMin :
      // tempLlr.getLatMin();
      // latMax = (latMax > tempLlr.getLatMax()) ? latMax :
      // tempLlr.getLatMax();
      // lonMin = (lonMin <= tempLlr.getLonMin()) ? lonMin :
      // tempLlr.getLonMin();
      // lonMax = (lonMax > tempLlr.getLonMax()) ? lonMax :
      // tempLlr.getLonMax();
      // }

      // </editor-fold>

      fullExtent = new LatLonRect(new LatLonPointImpl(latMin, lonMin), new LatLonPointImpl(latMax, lonMax));
      return fullExtent;
    } catch (Exception ex) {
      ex.printStackTrace();
    }

    return null;
  }

  public double[] getMaximumExtentDimensions() {
    try {

      double maxDx = Double.NEGATIVE_INFINITY, maxDy = Double.NEGATIVE_INFINITY, maxDz = Double.NEGATIVE_INFINITY, temp;

      LatLonRect tempLlr;
      for (long t : times) {
        tempLlr = getExtentAtTime(t);

        temp = tempLlr.getLatMax() - tempLlr.getLatMin();
        maxDy = (temp > maxDy) ? temp : maxDy;

        temp = tempLlr.getLonMax() - tempLlr.getLonMin();
        maxDx = (temp > maxDx) ? temp : maxDx;

        // double[] d;
        // d = getDepthRangeAtTime(t);
        // Arrays.sort(d);
        // temp = Math.abs(d[1] - d[0]);
        // maxDz = (temp > maxDz) ? temp : maxDz;
      }

      // return new double[]{maxDx, maxDy, maxDz};
      return new double[]{maxDx, maxDy};
    } catch (Exception ex) {
      ex.printStackTrace();
    }

    return null;
  }

  // <editor-fold defaultstate="collapsed" desc=" Property Get/Set ">
  public Variable getTimeVar() {
    return timeVar;
  }

  public Variable getPartVar() {
    return partVar;
  }

  public Variable getLatVar() {
    return latVar;
  }

  public Variable getLonVar() {
    return lonVar;
  }

  public Variable getPHVar() {
    return pHVar;
  }

  public Variable getRadVar() {
    return radVar;
  }

  public Variable getCenterConcVar() {
    return centerConcVar;
  }

  public Variable getEdgeConcVar() {
    return edgeConcVar;
  } // </editor-fold>
}
