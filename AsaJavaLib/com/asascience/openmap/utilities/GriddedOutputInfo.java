/*
 * Applied Science Associates, Inc.
 * Copyright 2007. All rights reserved.
 */

/*
 * GriddedOutputInfo.java
 *
 * Created on Mar 12, 2008, 4:30:00 PM
 *
 */

package com.asascience.openmap.utilities;

import java.awt.geom.Point2D;
import java.util.Arrays;

import ucar.unidata.geoloc.LatLonPointImpl;
import ucar.unidata.geoloc.LatLonRect;

import com.bbn.openmap.proj.Length;

/**
 * 
 * @author CBM <cmueller@asascience.com>
 */
public class GriddedOutputInfo {

	public static final double ND_VALUE = -9999999.0d;

	private static double oDx = 0;
	private static double oDy = 0;
	private static double oDz = 0;
	private double deltaX = 0;
	private double deltaY = 0;
	private double deltaZ = 0;
	private int nCellsX = 0;
	private int nCellsY = 0;
	private int nCellsZ = 1;

	private int nTimes;

	private boolean useFloatingGrid;
	private static boolean useNumCells;

	// private double[] latVals;
	// private double[] lonVals;
	// private double[] depthVals;

	/**
	 * Creates a new instance of GriddedOutputInfo
	 * 
	 * @param args
	 */
	public GriddedOutputInfo(double[] args) {
		setGridDimensions(args);
	}

	/**
	 * Creates a new instance of GriddedOutputInfo
	 * 
	 * @param deltaArgs
	 * @param ncellArgs
	 */
	public GriddedOutputInfo(double[] deltaArgs, int[] ncellArgs) {
		setGridDimensions(deltaArgs, ncellArgs);
	}

	public double[] calculateLats(LatLonRect llr) {
		double[] lats = new double[nCellsY];

		double lat = llr.getLatMin();
		for (int i = 0; i < nCellsY; i++) {
			lats[i] = lat + (deltaY * 0.5);
			lat = lat + deltaY;
		}

		return lats;
	}

	public double[] calculateLons(LatLonRect llr) {
		double[] lons = new double[nCellsX];

		double lon = llr.getLonMin();
		for (int i = 0; i < nCellsX; i++) {
			lons[i] = lon + (deltaX * 0.5);
			lon = lon + deltaX;
		}

		return lons;
	}

	public double[] calculateDepths(double[] depthRange) {
		double[] depths = new double[nCellsZ];

		double depth = depthRange[0];// depthRange[0] == surface(top)
		for (int i = 0; i < nCellsZ; i++) {
			depths[i] = depth - (deltaZ * 0.5);
			depth = depth - deltaZ;
		}

		return depths;
	}

	public String toString2() {
		return "DeltaXY [" + getDeltaX() + ", " + getDeltaY() + "] " + "#CellsXY [" + getNCellsX() + ", "
			+ getNCellsY() + "]";
	}

	public void setGridDimensions(double[] args) {
		if (args.length != 6) {
			throw new ArrayIndexOutOfBoundsException(
				"There must be 6 members in the array in the order {dx, dy, dz, numcellsx, numcellsy, numcellsz}");
		}
		setDeltaX(args[0]);
		setDeltaY(args[1]);
		setDeltaZ(args[2]);
		setNCellsX((int) args[3]);
		setNCellsY((int) args[4]);
		setNCellsZ((int) args[5]);
	}

	public void setGridDimensions(double[] deltaArgs, int[] ncellArgs) {
		if (deltaArgs.length != 3) {
			throw new ArrayIndexOutOfBoundsException("There must be 3 members in the array in the order {dx, dy, dz}");
		}
		if (ncellArgs.length != 3) {
			throw new ArrayIndexOutOfBoundsException(
				"There must be 3 members in the array in the order {numcellsx, numcellsy, numcellsz}");
		}
		setDeltaX(deltaArgs[0]);
		setDeltaY(deltaArgs[1]);
		setDeltaZ(deltaArgs[2]);
		setNCellsX(ncellArgs[0]);
		setNCellsY(ncellArgs[1]);
		setNCellsZ(ncellArgs[2]);
	}

	public double[] getDeltas() {
		return new double[] { getDeltaX(), getDeltaY(), getDeltaZ() };
	}

	public void setDeltas(double[] args) {
		if (args.length != 3) {
			throw new ArrayIndexOutOfBoundsException("There must be 3 members in the array in the order {dx, dy, dz}");
		}
		setDeltaX(args[0]);
		setDeltaY(args[1]);
		setDeltaZ(args[2]);
	}

	public int[] getNCells() {
		return new int[] { getNCellsX(), getNCellsY() };
	}

	public void setNCells(int[] args) {
		if (args.length != 3) {
			throw new ArrayIndexOutOfBoundsException(
				"There must be 3 members in the array in the order {numcellsx, numcellsy, numcellsz}");
		}
		setNCellsX(args[0]);
		setNCellsY(args[1]);
		setNCellsZ(args[2]);
	}

	// <editor-fold defaultstate="collapsed" desc=" Static Methods ">

	/**
	 * 
	 * @param llr
	 *            - The <CODE>LatLonRect</CODE> object defining the maximum
	 *            extent.
	 * @param gridX
	 *            - The X parameter (# cells or deltaX).
	 * @param gridY
	 *            - The Y parameter (# cells or deltaX).
	 * @param gridZ
	 *            - The Z parameter (# cells or deltaX). otherwise,
	 *            <CODE>false</CODE>.
	 * @param depthRange
	 * @param isUseNumCells
	 * @return
	 */
	public static double[] calculateGrid(LatLonRect llr, double gridX, double gridY, double gridZ, double[] depthRange,
		boolean isNumCells) {
		GriddedOutputInfo.useNumCells = isNumCells;
		if (isNumCells) {
			return calculateGrid_dxdy(llr, (int) gridX, (int) gridY, (int) gridZ, depthRange);
		} else {
			return calculateGrid_numCells(llr, gridX, gridY, gridZ, depthRange);
		}
	}

	public static double[] calculateGrid(Point2D startLoc, double[] maxExtent, double gridx, double gridy,
		double gridz, double[] depthRange, boolean isNumCells) {
		try {
			LatLonRect llr = new LatLonRect(new LatLonPointImpl(startLoc.getY(), startLoc.getX()), maxExtent[1],
				maxExtent[0]);

			return calculateGrid(llr, gridx, gridy, gridz, depthRange, isNumCells);
		} catch (Exception ex) {

		}
		return null;
	}

	private static double[] calculateGrid_numCells(LatLonRect llr, double dx, double dy, double dz, double[] depthRange) {
		/** Convert dx dy dz in meters to decimal degrees */
		oDx = new Double(dx);
		oDy = new Double(dy);
		oDz = new Double(dz);

		Arrays.sort(depthRange);

		dx = Length.DECIMAL_DEGREE.fromRadians(Length.METER.toRadians(dx));
		dy = Length.DECIMAL_DEGREE.fromRadians(Length.METER.toRadians(dy));

		double ncellsX = (llr.getLonMax() - llr.getLonMin()) / dx;
		double ncellsY = (llr.getLatMax() - llr.getLatMin()) / dy;
		double ncellsZ = (Math.abs(depthRange[0]) - Math.abs(depthRange[1])) / dz;
		// ncellsX = ((int)ncellsX <= 0) ? 1 : ncellsX;
		// ncellsY = ((int)ncellsY <= 0) ? 1 : ncellsY;
		// ncellsZ = ((int)ncellsZ <= 0) ? 1 : ncellsZ;

		// return new double[]{dx, dy, dz, (int)ncellsX, (int)ncellsY,
		// (int)ncellsZ};

		ncellsX = (Math.round(ncellsX) <= 0) ? 1 : ncellsX;
		ncellsY = (Math.round(ncellsY) <= 0) ? 1 : ncellsY;
		ncellsZ = (Math.round(ncellsZ) <= 0) ? 1 : ncellsZ;

		return new double[] { dx, dy, dz, Math.round(ncellsX), Math.round(ncellsY), Math.round(ncellsZ) };
	}

	private static double[] calculateGrid_dxdy(LatLonRect llr, int numX, int numY, int numZ, double[] depthRange) {
		double dx = (llr.getLonMax() - llr.getLonMin()) / numX;
		double dy = (llr.getLatMax() - llr.getLatMin()) / numY;
		double dz = (Math.abs(depthRange[1]) - Math.abs(depthRange[0])) / numZ;

		return new double[] { dx, dy, dz, numX, numY, numZ };
	}

	// </editor-fold>

	// <editor-fold defaultstate="collapsed" desc=" Property Get/Set ">

	public double getDeltaX() {
		return deltaX;
	}

	public void setDeltaX(double deltaX) {
		this.deltaX = deltaX;
	}

	public double getDeltaY() {
		return deltaY;
	}

	public void setDeltaY(double deltaY) {
		this.deltaY = deltaY;
	}

	public double getDeltaZ() {
		return deltaZ;
	}

	public void setDeltaZ(double deltaZ) {
		this.deltaZ = deltaZ;
	}

	public int getNCellsX() {
		return nCellsX;
	}

	public void setNCellsX(int nCellsX) {
		this.nCellsX = nCellsX;
	}

	public int getNCellsY() {
		return nCellsY;
	}

	public void setNCellsY(int nCellsY) {
		this.nCellsY = nCellsY;
	}

	public int getNCellsZ() {
		return nCellsZ;
	}

	public void setNCellsZ(int nCellsZ) {
		this.nCellsZ = nCellsZ;
	}

	public int getNTimes() {
		return nTimes;
	}

	public void setNTimes(int nTimes) {
		this.nTimes = nTimes;
	}

	public boolean isUseFloatingGrid() {
		return useFloatingGrid;
	}

	public void setUseFloatingGrid(boolean useFloatingGrid) {
		this.useFloatingGrid = useFloatingGrid;
	}

	public boolean isUseNumCells() {
		return useNumCells;
	}

	public void setUseNumCells(boolean useNumCells) {
		this.useNumCells = useNumCells;
	}

	public static double getODx() {
		return oDx;
	}

	public static double getODy() {
		return oDy;
	}

	public static double getODz() {
		return oDz;
	}

	// </editor-fold>

}
