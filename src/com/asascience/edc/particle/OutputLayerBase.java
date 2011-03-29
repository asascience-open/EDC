/*
 * Applied Science Associates, Inc.
 * Copyright 2007. All rights reserved.
 */

/*
 * OutputLayerBase.java
 *
 * Created on Mar 17, 2008, 11:32:51 AM
 *
 */

package com.asascience.edc.particle;

import java.awt.Container;
import java.io.File;

import com.asascience.openmap.layer.TimeLayer;
import com.bbn.openmap.omGraphics.OMGraphic;
import com.bbn.openmap.omGraphics.OMGraphicList;

/**
 * 
 * @author CBM <cmueller@asascience.com>
 */
public class OutputLayerBase extends TimeLayer {

	protected File dataFile = null;

	protected OMGraphicList omgraphics = null;
	protected OMGraphic selectedGraphic = null;

	// protected Incident incident = null;

	protected long currentTime;

	/**
	 * Creates a new instance of OutputLayerBase
	 * 
	 * @param incident
	 * @param consumeEvents
	 */
	public OutputLayerBase(boolean consumeEvents) {
		// public OutputLayerBase(Incident incident, boolean consumeEvents){
		// this.incident = incident;
		this.consumeEvents = false;
	}

	/**
	 * 
	 * @return
	 */
	@Override
	public OMGraphicList prepare() {
		return null;
	}

	/**
	 * Draws the appropriate data for the passed timestep t <CODE>long</CODE>.
	 * 
	 * @param t
	 *            The time <CODE>long</CODE> for which the data should be drawn
	 */
	public void drawDataForTime(long t) {
	}

	/**
	 * Called when the Layer is removed from the MapBean, giving an opportunity
	 * to clean up.
	 * 
	 * @param cont
	 */
	@Override
	public void removed(Container cont) {
		OMGraphicList list = this.getList();
		if (list != null) {
			list.clear();
			list = null;
		}
	}
}
