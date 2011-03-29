/*
 * Applied Science Associates, Inc.
 * Copyright 2007. All rights reserved.
 */

/*
 * ASALayer.java
 *
 * Created on Apr 2, 2008, 11:21:13 AM
 *
 */

package com.asascience.openmap.layer;

import ucar.unidata.geoloc.LatLonRect;

import com.bbn.openmap.layer.OMGraphicHandlerLayer;

/**
 * 
 * @author CBM <cmueller@asascience.com>
 */
public class ASALayer extends OMGraphicHandlerLayer {
	private LatLonRect layerExtentRectangle = null;
	private String sourceFilePath = null;

	// /** Creates a new instance of ASALayer */
	// public ASALayer(){
	// }

	public LatLonRect getLayerExtentRectangle() {
		return layerExtentRectangle;
	}

	public void setLayerExtentRectangle(LatLonRect rect) {
		layerExtentRectangle = rect;
	}

	public String getSourceFilePath() {
		return sourceFilePath;
	}

	public void setSourceFilePath(String sourceFilePath) {
		this.sourceFilePath = sourceFilePath;
	}
}
