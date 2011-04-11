/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * GriddingUtils.java
 *
 * Created on Sep 30, 2008, 10:52:55 AM
 *
 */
package com.asascience.openmap.utilities;

import java.awt.geom.Point2D;
import java.util.Arrays;

import javax.swing.JOptionPane;

import cern.colt.matrix.DoubleMatrix3D;
import cern.colt.matrix.impl.SparseDoubleMatrix3D;

import com.bbn.openmap.proj.Length;

/**
 * 
 * @author cmueller_mac
 */
public class GriddingUtils {

  protected Point2D ulCorner;
  protected Point2D lrCorner;
  protected double[] depthRange;
  protected double startX;
  protected double startY;
  protected double startZ;
  protected double dx;
  protected double dy;
  protected double dz;
  protected int ncellx;
  protected int ncelly;
  protected int ncellz;
  // protected Hashtable<String, Double> gridHash;
  protected double cellVolume;
  protected DoubleMatrix3D dataCube;

  /** Creates a new instance of GriddingUtils */
  public GriddingUtils(Point2D upperLeftCorner, Point2D lowerRightCorner, GriddedOutputInfo gridInfo,
          double[] depthRange) {
    ulCorner = upperLeftCorner;
    lrCorner = lowerRightCorner;
    dx = gridInfo.getDeltaX();
    dy = gridInfo.getDeltaY();
    dz = gridInfo.getDeltaZ();
    ncellx = gridInfo.getNCellsX();
    ncelly = gridInfo.getNCellsY();
    ncellz = gridInfo.getNCellsZ();

    Arrays.sort(depthRange);
    this.depthRange = depthRange;
    //
    // /**Calculate deltas*/
    // dx = Math.abs(lrCorner.getX() - ulCorner.getX()) / ncellx;
    // dy = Math.abs(ulCorner.getY() - lrCorner.getY()) / ncelly;
    // if(depthRange.length == 1 || (depthRange.length == 2 & depthRange[1]
    // == depthRange[0])){
    // dz = depthRange[0] / ncellz;
    // }else{
    // dz = (depthRange[depthRange.length - 1] - depthRange[0]) / ncellz;
    // }
    // dz = Math.abs(dz);

    /** Set the start XYZ values */
    startX = ulCorner.getX();
    startY = lrCorner.getY();
    startZ = Math.abs(this.depthRange[this.depthRange.length - 1]);

    /** Calculate the volume of a cell */
    // double dxM = Geo.distanceKM(latMin, lonMin, latMin, lonMax) * 1000;
    // double dyM = Geo.distanceKM(latMin, lonMin, latMax, lonMin) * 1000;
    // double dxM = Geo.distanceKM(startY, startX, startY, (startX + dx)) *
    // 1000;
    // double dyM = Geo.distanceKM(startY, startX, (startY + dy), startX) *
    // 1000;
    // double dzM = dz;
    double dxM = Length.METER.fromRadians(Length.DECIMAL_DEGREE.toRadians(dx));
    double dyM = Length.METER.fromRadians(Length.DECIMAL_DEGREE.toRadians(dy));
    double dzM = dz;
    cellVolume = dxM * dyM * dzM * 1000;// convert from m^3 to L

    /** Build the hash */
    // buildGridHash();
    buildMatrix();
    // printHashContents();
  }

  public double getCellVolume() {
    return cellVolume;
  }

  private void buildMatrix() {
    try {
      dataCube = new SparseDoubleMatrix3D(ncellz, ncelly, ncellx);
    } catch (IllegalArgumentException ex) {
      JOptionPane.showMessageDialog(null,
              "Number of grid cells too large.\nPlease increase the grid cell size and try again.", "Gridding Error",
              JOptionPane.ERROR_MESSAGE);
    }
  }

  public void clearMatrix() {
    buildMatrix();
  }

  public void assignValue(int depthIndex, int latIndex, int lonIndex, double val) {
    double oVal = dataCube.get(depthIndex, latIndex, lonIndex);
    dataCube.set(depthIndex, latIndex, lonIndex, (oVal + val));
  }

  public void assignValue(double depth, double lat, double lon, double val) {
    depth = Math.abs(depth);
    double id = ((lon - startX) <= 0) ? 0 : (lon - startX) / dx;
    double jd = ((lat - startY) <= 0) ? 0 : (lat - startY) / dy;
    double kd = ((depth - startZ) <= 0) ? 0 : (depth - startZ) / dz;

    int i = ((int) id > (ncellx - 1)) ? ncellx - 1 : (int) id;
    int j = ((int) jd > (ncelly - 1)) ? ncelly - 1 : (int) jd;
    int k = ((int) kd > (ncellz - 1)) ? ncellz - 1 : (int) kd;

    assignValue(k, j, i, val);
  }

  public DoubleMatrix3D getDataCube() {
    return dataCube;
  }

  public double[] getValue(int depthIndex, int latIndex, int lonIndex) {
    double val = dataCube.get(depthIndex, latIndex, lonIndex);
    if (val == 0) {
      return new double[]{GriddedOutputInfo.ND_VALUE, GriddedOutputInfo.ND_VALUE};
    }
    return new double[]{val, (val / cellVolume)};
  }

  public double[] getValue(double depth, double lat, double lon) {
    double id = ((lon - startX) <= 0) ? 0 : (lon - startX) / dx;
    double jd = ((lat - startY) <= 0) ? 0 : (lat - startY) / dy;
    double kd = ((depth - startZ) <= 0) ? 0 : (depth - startZ) / dz;

    int i = (Math.round(id) > (ncellx)) ? (int) ncellx : (int) Math.round(id);
    int j = (Math.round(jd) > (ncelly)) ? (int) ncelly : (int) Math.round(jd);
    int k = (Math.round(kd) > (ncellz)) ? (int) ncellz : (int) Math.round(kd);

    return getValue(k, j, i);
  }

  public void printMatrix() {
    System.out.println(dataCube);
  }
  /** Old Hashtable method of gridding. */
  //
  // public void printHashContents() {
  // Enumeration e = gridHash.keys();
  // while(e.hasMoreElements()){
  // String key = (String)e.nextElement();
  // System.out.println(key + "," + gridHash.get(key).toString());
  // }
  // }
  //
  // public boolean clearGridHash() {
  // try{
  // Enumeration e = gridHash.keys();
  // while(e.hasMoreElements()){
  // String key = (String)e.nextElement();
  // gridHash.put(key, GriddedOutputInfo.ND_VALUE);
  // }
  // return true;
  // }catch(Exception ex){
  // ex.printStackTrace();
  // }
  // return false;
  // }
  //
  // public boolean assignParticleToGrid(String key, double value) {
  // try{
  // double oVal = (Double)gridHash.get(key);
  // gridHash.put(key, (((oVal == GriddedOutputInfo.ND_VALUE) ? 0d : oVal) +
  // value));
  //
  // return true;
  // }catch(Exception ex){
  // ex.printStackTrace();
  // // JOptionPane.showMessageDialog(null,
  // "ERROR: GriddingUtils:assignParticle: " + key);
  // }
  // return false;
  // }
  //
  // /**
  // * Returns a double[] where the first member is the total mass for the
  // grid
  // * cell and the second is the concentration.
  // */
  // public double[] getGridValueAt(String key) {
  // double[] ret = new double[]{GriddedOutputInfo.ND_VALUE,
  // GriddedOutputInfo.ND_VALUE};
  // try{
  // ret[0] = (Double)gridHash.get(key);
  // if(ret[0] == GriddedOutputInfo.ND_VALUE){
  // return new double[]{GriddedOutputInfo.ND_VALUE,
  // GriddedOutputInfo.ND_VALUE};
  // }
  // ret[1] = ret[0] / cellVolume;
  // }catch(Exception ex){
  // ex.printStackTrace();
  // // JOptionPane.showMessageDialog(null, "ERROR: GriddingUtils:getValue: "
  // + key);
  // }
  // return ret;
  // }
  //
  // private boolean buildGridHash() {
  // try{
  // gridHash = new Hashtable<String, Double>();
  //
  // for(int lon = 0; lon < ncellx + 1; lon++){
  // for(int lat = 0; lat < ncelly + 1; lat++){
  // for(int depth = 0; depth < ncellz + 1; depth++){
  // gridHash.put(gridLldToIjk((startX + (lon * dx)), (startY + (lat * dy)),
  // (startZ + (depth * dz))), GriddedOutputInfo.ND_VALUE);
  // }
  // }
  // }
  // return true;
  // }catch(Exception ex){
  // ex.printStackTrace();
  // }
  // return false;
  // }
  //
  // public String gridLldToIjk(double lon, double lat, double depth) {
  // double i = ((lon - startX) <= 0) ? 0 : (lon - startX) / dx;
  // double j = ((lat - startY) <= 0) ? 0 : (lat - startY) / dy;
  // double k = ((depth - startZ) <= 0) ? 0 : (depth - startZ) / dz;
  //
  // // return Math.round(i) + "_" + Math.round(j) + "_" + Math.round(k);
  //
  // i = (Math.round(i) > (ncellx)) ? ncellx : Math.round(i);
  // j = (Math.round(j) > (ncelly)) ? ncelly : Math.round(j);
  // k = (Math.round(k) > (ncellz)) ? ncellz : Math.round(k);
  //
  // return (int)i + "_" + (int)j + "_" + (int)k;
  // }
  //
  // public String partLldToIjk(double lon, double lat, double depth) {
  // depth = Math.abs(depth);
  // double i = ((lon - startX) <= 0) ? 0 : (lon - startX) / dx;
  // double j = ((lat - startY) <= 0) ? 0 : (lat - startY) / dy;
  // double k = ((depth - startZ) <= 0) ? 0 : (depth - startZ) / dz;
  //
  // i = ((int)i > (ncellx - 1)) ? ncellx - 1 : (int)i;
  // j = ((int)j > (ncelly - 1)) ? ncelly - 1 : (int)j;
  // k = ((int)k > (ncellz - 1)) ? ncellz - 1 : (int)k;
  //
  // return (int)i + "_" + (int)j + "_" + (int)k;
  // }
  //
  // public double getCellVolume() {
  // return cellVolume;
  // }
}
