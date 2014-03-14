/*
 * Applied Science Associates, Inc.
 * Copyright 2007. All rights reserved.
 */

/*
 * OutputReaderBase.java
 *
 * Created on Mar 17, 2008, 12:42:34 PM
 *
 */
package com.asascience.edc.particle;

import java.io.File;
import java.util.List;

import ucar.nc2.util.CancelTask;
import ucar.unidata.geoloc.LatLonRect;

import com.asascience.utilities.BinarySearch;

/**
 * 
 * @author CBM <cmueller@asascience.com>
 */
public class OutputReaderBase implements CancelTask {

  protected File dataFile;
  protected String chemical;
  protected double molecWeight;
  protected double ld50;
  protected double initMass;
  protected long startTime;
  protected long endTime;
  protected long timeIncrement;
  protected List<Long> times;
  protected LatLonRect fullExtent;
  protected BinarySearch binarySearch;

  /** Creates a new instance of OutputReaderBase */
  public OutputReaderBase() {
    binarySearch = new BinarySearch();
  }

  public int getTimeIndex(long query) {
    if (query == 0) {
      query = startTime;
    }
    return binarySearch.longSearch(times.toArray(new Long[0]), 0, times.size(), query);

    // for(int i = 0; i < times.size(); i++){
    // if(times.get(i) >= query){
    // return i;
    // }
    // }
    // return -1;
  }

  public String getFileLocation() {
    return this.dataFile.getParent();
  }

  public List<Long> getTimes() {
    return times;
  }

  public String getChemical() {
    return chemical;
  }

  public double getMolecWeight() {
    return molecWeight;
  }

  public double getLd50() {
    return ld50;
  }

  public double getInitMass() {
    return initMass;
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

  public boolean isCancel() {
    return true;
  }

  public void setError(String arg0) {
  }


public void setProgress(String arg0, int arg1) {
	// TODO Auto-generated method stub
	
}
}
