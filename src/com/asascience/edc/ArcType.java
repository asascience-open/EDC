/*
 * ArcType.java
 *
 * Created on November 26, 2007, 3:56 PM
 *
 * Applied Science Associates, Inc.
 * Copyright 2007.  All rights reserved.
 */

package com.asascience.edc;

/**
 * Enumeration for the type of file that the nc file will become. Only relevant
 * when used in conjunction with ArcGIS. Null is used otherwise.
 * 
 * @author CBM
 */
public enum ArcType {
	/**
	 * The dataset should become a raster in ArcGIS.
	 */
	RASTER,
	/**
	 * The dataset should become a feature in ArcGIS.
	 */
	FEATURE,
	/**
	 * The dataset should become a feature using vectors in ArcGIS.
	 */
	VECTOR,
	/**
	 * The dataset will not be automatically ingested into ArcGIS.
	 */
	NULL
}
