/*
 * Applied Science Associates, Inc.
 * Copyright 2007. All rights reserved.
 */

/*
 * NetcdfKeysBase.java
 *
 * Created on Apr 21, 2008, 10:00:44 AM
 *
 */
package com.asascience.openmap.layer.nc;

/**
 * 
 * @author CBM <cmueller@asascience.com>
 */
public class NetcdfKeysBase {

  // variable tags
  public static final String TIME = "record";
  public static final String PARTICLE = "particle";
  public static final String LAT = "lat";
  public static final String LON = "lon";
  public static final String DEPTH = "depth";
  // variable attribute tags
  public static final String UNITS = "units";
  public static final String LONG_NAME = "long_name";
  public static final String MIN_VAL = "minimum_value";
  public static final String MAX_VAL = "maximum_value";
  public static final String FILL_VAL = "_FillValue";
  public static final String COORD_AXIS_TYPE = "_CoordinateAxisType";
  public static final String COORD_AXIS_ZPOS = "_CoordinateZisPositive";
}
