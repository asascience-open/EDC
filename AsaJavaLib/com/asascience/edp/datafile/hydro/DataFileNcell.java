/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.asascience.edp.datafile.hydro;

import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.TimeZone;

import com.asascience.openmap.layer.nc.ncell.NcellReader;
import com.asascience.utilities.Vector3D;

/**
 * 
 * @author asamac
 */
public class DataFileNcell extends DataFileBase {

  private NcellReader ncellReader;

  /**
   * Creates a new instance of DataFileNcell
   */
  public DataFileNcell() {
  }

  /**
   *
   */
  @Override
  protected void loadDataFile() {
    ncellReader = new NcellReader(getDataFile());

    GregorianCalendar c = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
    c.setTimeInMillis(ncellReader.getStartTime());
    setStartTime((GregorianCalendar) c.clone());
    c.setTimeInMillis(ncellReader.getEndTime());
    setEndTime((GregorianCalendar) c.clone());
    this.timeIncrement = ncellReader.getTimeIncrement();
  }

  @Override
  public Vector3D getCurrentAt(Vector3D position) {
    Vector3D ret = null;
    try {
      int timeI = ncellReader.getTimeIndex(this.getQueryTime());
      int ncellI = ncellReader.getNcellIndex(position.getV(), position.getU());
      // System.out.println("timeI="+timeI+" ncellI="+ncellI);

      if (timeI > -1 & ncellI > -1) {
        double u = ncellReader.getU(timeI, ncellI);
        double v = ncellReader.getV(timeI, ncellI);

        ret = new Vector3D(u, v, 0);
      }

    } catch (Exception ex) {
      System.err.println("DFEds:getCurrentAt:");
      ex.printStackTrace();
    }

    return ret;
  }

  /**
   *
   * @return
   */
  @Override
  public Double[] getLats() {
    // System.out.println("Lats:");
    List<Double> dbls = new ArrayList<Double>();
    for (double d : ncellReader.getLats()) {
      // System.out.println("\t" + d);
      if (!dbls.contains(d)) {
        dbls.add(d);
      }
    }
    return (Double[]) dbls.toArray(new Double[0]);
  }

  /**
   *
   * @return
   */
  @Override
  public Double[] getLons() {
    // System.out.println("Lons:");
    List<Double> dbls = new ArrayList<Double>();
    for (double d : ncellReader.getLons()) {
      // System.out.println("\t" + d);
      if (!dbls.contains(d)) {
        dbls.add(d);
      }
    }
    return (Double[]) dbls.toArray(new Double[0]);
    // return edsReader.getLons();
  }

  /**
   *
   * @param position
   * @return
   */
  @Override
  public double[][] getUVTimeSeries(Vector3D position) {
    return ncellReader.getUVTimeSeries(position);

    // <editor-fold defaultstate="collapsed" desc=" Moved to Reader ">

    // double[][] retVals = null;
    // double lat = position.getV(), lon = position.getU();
    // try{
    // long[] times = ncellReader.getTimeSteps();
    // retVals = new double[2][times.length];
    // int timeI, ncellI, counter = 0;
    // double u, v;
    //
    // // long go = System.currentTimeMillis();
    // ncellI = ncellReader.getNcellIndex(lat, lon);
    // for(int i = 0; i < times.length; i++){
    // timeI = ncellReader.getTimeIndex(times[i]);
    // // ncellI = edsReader.getNcellIndex(lat, lon);//no need for this to
    // be inside the loop....
    // if(timeI > -1 & ncellI > -1){
    // u = ncellReader.getU(timeI, ncellI);
    // v = ncellReader.getV(timeI, ncellI);
    // retVals[0][i] = u;
    // retVals[1][i] = v;
    // // retVals[0][i] = (u > 0) ? u : Double.NaN;
    // // retVals[1][i] = (v > 0) ? v : Double.NaN;
    // // retVals[0][i] = (u != 0) ? u : Double.NaN;
    // // retVals[1][i] = (v != 0) ? v : Double.NaN;
    //
    // if(!Double.isNaN(retVals[0][i]) ||
    // !Double.isNaN(retVals[1][i])){//was &
    // counter++;
    // }
    // }else{
    // retVals[0][i] = Double.NaN;
    // retVals[1][i] = Double.NaN;
    // }
    // }
    //
    // // System.out.println("FOR LOOP Time: " + (System.currentTimeMillis()
    // - go));
    // if(counter < 2){
    // return null;
    // }
    //
    // }catch(Exception ex){
    // ex.printStackTrace();
    // }
    //
    // return retVals;

    // </editor-fold>
  }

  @Override
  public double[] getDataTimeseries(String id, Vector3D position) {
    return ncellReader.getScalarTimeseriesByName(0, id, position);
  }

  @Override
  public void disposeAll() {
  }
}
