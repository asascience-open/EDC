/*
 * Applied Science Associates, Inc.
 * Copyright 2007. All rights reserved.
 */

/*
 * BathyFileEtopo.java
 *
 * Created on Mar 21, 2008, 11:47:33 AM
 *
 */

package com.asascience.edp.datafile.bathy;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.asascience.utilities.Vector3D;
import com.bbn.openmap.io.BinaryBufferedFile;
import com.bbn.openmap.io.FormatException;
import com.bbn.openmap.omGraphics.OMRaster;

/**
 * 
 * @author CBM <cmueller@asascience.com>
 */
public class BathyFileEtopo extends BathyFileBase {
	protected OMRaster etopoRas;

	/** ETOPO elevation files */
	protected final static String[] etopoFileNames = { "ETOPO2", "ETOPO5", "ETOPO10", "ETOPO15" };
	/** The current resolution (in minutes) */
	protected int minuteSpacing = 5;

	/** The etopo elevation data */
	protected short[] dataBuffer = null;
	protected int bufferWidth;
	protected int bufferHeight;

	/**
	 * Number of pixel spacers that should be added to a data file, per line, to
	 * adjust for skewing.
	 */
	protected int spacer = 0;

	/** dimensions of the ETOPO files (don't mess with these!) */
	protected final static int[] etopoWidths = { 10800, 4320, 2160, 1440 };// ep-g
	protected final static int[] etopoHeights = { 5400, 2160, 1080, 720 }; // ep-g

	/**
	 * Spacings (in meters) between adjacent lon points at the equater. The
	 * values here were aesthetically defined (they are not the actual spacings)
	 */
	protected final static double[] etopoSpacings = { 1800., 3500., 7000., 10500. }; // ep-g

	/** Creates a new instance of BathyFileEtopo */
	public BathyFileEtopo() {

	}

	boolean init = true;

	/**
     * 
     */
	@Override
	protected void loadDataFile() {
		loadEtopo();
	}

	protected void releaseDataFile() {
		dataBuffer = null;
	}

	/**
	 * 
	 * @param queryPos
	 * @return
	 */
	@Override
	public double getDepthAtLoc(Vector3D queryPos) {
		try {
			// compute scalers for lat/lon indicies
			float scy = (float) bufferHeight / 180f;
			float scx = (float) bufferWidth / 360f;

			// LatLonPoint llp = new LatLonPoint((float)queryPos.getV(),
			// (float)queryPos.
			// getU());
			// float lat = llp.getLatitude();
			// float lon = llp.getLongitude();

			// get point values
			float lat = (float) queryPos.getV();
			float lon = (float) queryPos.getU();

			// check... dfd
			if (minuteSpacing == 2) {
				lon += 180.;
			} else {
				if (lon < 0.) {
					lon += 360.;
				}
			}

			// find indicies
			int lat_idx = (int) ((90. - lat) * scy);
			int lon_idx = (int) (lon * scx);

			// offset
			int ofs = lon_idx + lat_idx * bufferWidth;

			// get elevation
			short el = dataBuffer[ofs];

			return el;
		} catch (Exception ex) {
			Logger.getLogger(BathyFileEtopo.class.getName()).log(Level.SEVERE, null, ex);
		}
		return Double.NaN;
	}

	private void loadEtopo() {
		loadBuffer();
	}

	protected void loadBuffer() {

		// get the resolution index
		int resIdx = minuteSpacing / 5; // ep-g
		if (resIdx < 0) {
			resIdx = 0;
		} else if (resIdx > 3) {
			resIdx = 3;
		}

		// // build file name
		dataFile += File.separator + etopoFileNames[resIdx];

		// Clean this out...dfd
		dataBuffer = null;

		try {

			// treat as buffered binary
			BinaryBufferedFile binFile = new BinaryBufferedFile(getDataFile());
			binFile.byteOrder(true);

			// set width/height
			bufferWidth = etopoWidths[resIdx];
			bufferHeight = etopoHeights[resIdx];

			int lSpacer = 1;

			// don't know why I have to do this, but there seems to be
			// a wrapping thing going on with different data sets.
			switch (minuteSpacing) {
				case (2):
					lSpacer = 1 + this.spacer;
					break;
				case (5):
					lSpacer = 0 + this.spacer;
					break;
				default:
					lSpacer = 1 + this.spacer;
			}

			// allocate storage
			dataBuffer = new short[(bufferWidth + lSpacer) * bufferHeight];

			// read data
			for (int i = 0; i < bufferWidth * bufferHeight; i++)
				dataBuffer[i] = binFile.readShort();

			// done
			binFile.close();

			// This is important for image creation.
			bufferWidth += lSpacer;

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (FormatException e) {
			e.printStackTrace();
		}
	}
}
