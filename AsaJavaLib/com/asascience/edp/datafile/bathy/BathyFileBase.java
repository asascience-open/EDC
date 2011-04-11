/*
 * Applied Science Associates, Inc.
 * Copyright 2007. All rights reserved.
 */

/*
 * BathyFileBase.java
 *
 * Created on Mar 21, 2008, 11:26:04 AM
 *
 */
package com.asascience.edp.datafile.bathy;

import com.asascience.utilities.Vector3D;

/**
 * 
 * @author CBM <cmueller@asascience.com>
 */
public class BathyFileBase {

  protected String dataFile;

  /** Creates a new instance of BathyFileBase */
  public BathyFileBase() {
  }

  protected void loadDataFile() {
  }

  ;

  protected void releaseBathyFile() {
  }

  ;

  public void cleanup() {
    releaseBathyFile();
  }

  /**
   *
   * @param queryPos
   * @return
   */
  public double getDepthAtLoc(Vector3D queryPos) {
    return Double.NaN;
  }

  public String getDataFile() {
    return dataFile;
  }

  public void setDataFile(String dataFile) {
    this.dataFile = dataFile;
    loadDataFile();
  }
}
