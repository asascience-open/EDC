/*
 * Conditional.java
 *
 * Created on December 11, 2007, 8:53 AM
 *
 * Applied Science Associates, Inc.
 * Copyright 2007.  All rights reserved.
 */
package com.asascience.edc;

/**
 * The static members of this class provide "flags" which enable conditional
 * compilation. After changing the values of any members of this class, a full
 * "Clean & Build" must be performed for the changes to take effect.
 * 
 * @author CBM
 */
public class Conditional {

  /**
   * <CODE>boolean</CODE> Determines the location where System.err print calls
   * are sent. If <CODE>true</CODE>, output is sent to a file. If
   * <CODE>false</CODE>, output is printed in the output pane.
   */
  public static final boolean OUTPUT_TO_FILE = true;
  // public static final boolean OUTPUT_TO_FILE = false;
  /**
   * <CODE>boolean</CODE> Determines the type of SubsetProcessPanel to
   * display. ESRI restricts user selections to conform to ArcGIS file types
   * (Raster & Feature. OILMAP provides additional options necessary for using
   * the dataset in ASA's Oilmap software. GENERAL provides no restrictions.
   */
  public static final int DISPLAY_TYPE = DisplayType.GENERAL;
  // public static final int DISPLAY_TYPE = DisplayType.ESRI;
  // public static final int DISPLAY_TYPE = DisplayType.OILMAP;
  /**
   * <CODE>boolean</CODE> Determines if the interface should be closed after
   * the user processes the desired dataset. For the ESRI implementation the
   * interface should close to return the user seamlessly to ArcMap. For stand
   * alone use, the interface can be left open after processing a dataset.
   */
  // public static final boolean CLOSE_AFTER_PROCESSING = true;
  public static final boolean CLOSE_AFTER_PROCESSING = false;

  public static class DisplayType {

    public static final int GENERAL = 0;
    public static final int ESRI = 1;
    public static final int OILMAP = 2;
  }
}
