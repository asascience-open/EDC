/*
 * Applied Science Associates, Inc.
 * Copyright 2007. All rights reserved.
 */

/*
 * OMGridCell.java
 *
 * Created on Mar 17, 2008, 1:03:43 PM
 *
 */

package com.asascience.openmap.omgraphic;

import java.awt.Color;

import com.asascience.openmap.utilities.MapUtils;
import com.asascience.utilities.Utils;
import com.bbn.openmap.omGraphics.OMGraphic;
import com.bbn.openmap.omGraphics.OMPoly;
import com.bbn.openmap.proj.Projection;

/**
 * 
 * @author CBM <cmueller@asascience.com>
 */
public class OMGridCell extends OMPoly {// OMRect{

	private double data;
	private double centerLat;
	private double centerLon;
	private Color lineColor = Color.BLACK;
	private Color selectColor = Color.YELLOW;
	private float[] tlats;
	private float[] tlons;

	// private float[] llArray;

	// public OMGridCell(double[] llPairs){
	// super(Utils.doubleArrayToFloatArray(llPairs), OMGraphic.DECIMAL_DEGREES,
	// OMGraphic.LINETYPE_RHUMB);
	// }

	public OMGridCell(Projection proj, float[] llPairs) {
		super((float[]) llPairs.clone(), OMGraphic.DECIMAL_DEGREES, OMGraphic.LINETYPE_RHUMB);
		super.setNumSegs(0);

		tlats = Utils.getSubsetArrayFloat(llPairs, 0, 2);
		tlons = Utils.getSubsetArrayFloat(llPairs, 1, 2);
		centerLat = Utils.averageDouble(Utils.floatArrayToDoubleArray(tlats));
		centerLon = Utils.averageDouble(Utils.floatArrayToDoubleArray(tlons));
		// centerLat = Utils.averageDouble(new double[]{lat1,lat2});
		// centerLon = Utils.averageDouble(new double[]{lon1,lon2});
	}

	/**
	 * Creates a new instance of OMGridCell Currently only called from
	 * FixedGridOutputReader
	 * 
	 * @param proj
	 * @param lat1
	 * @param lon1
	 * @param lat2
	 * @param lon2
	 * @param data
	 */
	public OMGridCell(Projection proj, double lat1, double lon1, double lat2, double lon2, double data) {
		// TODO: remove float casts once openmap supports doubles
		// super((float)lat1, (float)lon1, (float)lat2, (float)lon2,
		// LineType.Rhumb);
		this(proj, MapUtils.buildFloatPolygonArray(lat1, lon1, lat2, lon2));
		// llArray = MapUtils.buildFloatPolygonArray(lat1, lon1, lat2, lon2);
		// centerLat = Utils.averageDouble(new double[]{lat1,lat2});
		// centerLon = Utils.averageDouble(new double[]{lon1,lon2});
		// super.setNumSegs(0);
		this.data = data;
	}

	// public OMGridCell(Projection proj, double[] llPairs, double data){
	// this(proj, Utils.doubleArrayToFloatArray(llPairs));
	// super.setNumSegs(0);
	// this.data = data;
	// }
	public OMGridCell(Projection proj, float[] llPairs, double data, Color fillColor) {
		this(proj, llPairs, data, fillColor, true);
	}

	/**
	 * Creates a new instance of OMGridCell This is the method that is called
	 * most often.
	 * 
	 * @param proj
	 * @param llPairs
	 * @param data
	 * @param fillColor
	 * @param showOutline
	 */
	public OMGridCell(Projection proj, float[] llPairs, double data, Color fillColor, boolean showOutline) {
		this(proj, llPairs);
		super.setNumSegs(0);
		if (fillColor != null) {
			super.setFillPaint(fillColor);
		}
		if (showOutline) {
			super.setLinePaint(lineColor);
		} else {
			super.setLinePaint(fillColor);

		}
		super.setSelectPaint(selectColor);
		this.data = data;
	}

	// public String toString2(){
	// return "LowerLeft: [" + super.getSouthLat() + ", " + super.getEastLon() +
	// "] UpperRight: [" +
	// super.getNorthLat() + ", " + super.getWestLon() + "]";
	// }

	public double getData() {
		return data;
	}

	public double getCenterLat() {
		return centerLat;

		// float[] lla = llArray.clone();
		// int len = (lla.length / 2) - 1;
		// float[] pts = new float[len];
		// int ind;
		// for(int i = 0; i < len; i++){
		// ind = i*2;
		// pts[i] = lla[i*2];
		// }
		//        
		// return Utils.averageFloat(pts);

		// return this.getSouthLat() + ((this.getNorthLat() -
		// this.getSouthLat()) * 0.5f);
	}

	public double getCenterLon() {
		return centerLon;

		// float[] lla = llArray.clone();
		// int len = (lla.length / 2) - 1;
		// float[] pts = new float[len];
		// int ind;
		// for(int i = 0; i < len; i++){
		// ind = i*2+1;
		// pts[i] = lla[i*2+1];
		// }
		//        
		// return Utils.averageFloat(pts);

		// return this.getWestLon() + ((this.getEastLon() - this.getWestLon()) *
		// 0.5f);
	}

	public float[] getLats() {
		return tlats;
	}

	public float[] getLons() {
		return tlons;
	}
}
