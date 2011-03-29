/*
 * CixVectorLayer.java
 *
 * Created on December 5, 2007, 9:44 AM
 *
 * Applied Science Associates, Inc.
 * Copyright 2007.  All rights reserved.
 */
package com.asascience.openmap.layer.asa;

import java.awt.Color;
import java.io.File;

import com.asascience.openmap.layer.VectorLayer;

/**
 * 
 * @author CBM
 */
public class CixVectorLayer extends VectorLayer {

	private CixReader cixReader;

	/**
	 * Creates a new instance of CixVectorLayer
	 * 
	 * @param currFile
	 */
	public CixVectorLayer(String currFile) {
		if (new File(currFile).exists()) {
			cixReader = new CixReader(currFile);
			this.setSourceFilePath(currFile);

			// this sets the time range in the TimeLayer
			this.setTimeRange(cixReader.getStartTime(), cixReader.getEndTime());
			// this sets the timeIncrement in the TimeLayer
			this.setTimeIncrement(cixReader.getTimeIncrement());
			this.setTimes(cixReader.getTimeSteps());

			this.setName(currFile.substring(currFile.lastIndexOf(File.separator) + 1));
			this.setUVFillVal(-30000f);
			// this.setVectorThinning(6);
			// this.setScalingFactor(0.25f);
			this.setVectorColor(Color.DARK_GRAY);

			this.lats = cixReader.getLats();
			this.lons = cixReader.getLons();

			/** Get u/v units */
			uvUnits = "m/s"; // FIXME: Ask Tatsu/Eoin/Matt/Guy to confirm this
		}
	}

	public void drawDataForTime(long t) {
		// System.err.println("drawDataForTime");
		if (cixReader.loadCIXStep(t)) {
			this.setVisible(true);
			// double[] lats = cixReader.getLats();
			// double[] lons = cixReader.getLons();
			double[] us = cixReader.getUVals(CixReader.TOP);
			double[] vs = cixReader.getVVals(CixReader.TOP);

			// System.err.println(lats.length+" "+lons.length+" "+us.length+" "+vs.length);
			this.display(us, vs);
			// this.display(lats, lons, us, vs);
		} else {
			this.setVisible(false);
		}
	}// /**
	// * Called when the Layer is removed from the MapBean, giving an
	// opportunity
	// * to clean up.
	// */
	// @Override
	// public void removed(Container cont){
	// if(cixReader != null){
	// cixReader = null;
	// }
	// }
}
