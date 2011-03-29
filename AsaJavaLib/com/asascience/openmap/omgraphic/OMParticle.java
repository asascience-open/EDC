/*
 * OMParticle.java
 *
 * Created on November 5, 2007, 8:44 AM
 *
 * Applied Science Associates, Inc.
 * Copyright 2007.  All rights reserved.
 */

package com.asascience.openmap.omgraphic;

import com.asascience.utilities.Vector3D;
import com.bbn.openmap.omGraphics.OMPoint;
import com.bbn.openmap.proj.Projection;

/**
 * 
 * @author CBM
 */
public class OMParticle extends OMPoint {
	private int particleID;
	private Vector3D particleLocation;
	private double pH;
	private double pixelRadius;
	private double realRadius;
	private double centerConc;
	private double edgeConc;

	// private

	/**
	 * Creates a new instance of OMParticle
	 * 
	 * @param proj
	 * @param id
	 * @param loc
	 * @param pH
	 * @param rad
	 * @param cconc
	 * @param econc
	 */
	public OMParticle(Projection proj, int id, Vector3D loc, double pH, double rad, double cconc, double econc) {
		// TODO: Remove float casts when OM updated to double precision
		super((float) loc.getV(), (float) loc.getU());
		this.setOval(true);

		setParticleID(id);
		setParticleLocation(loc);
		setPH(pH);
		setRealRadius(rad);

		// //calculate the radial distance in pixels...
		// Point centerPixel = proj.forward((float)loc.getV(),
		// (float)loc.getU());
		// // Mercator merc = new Mercator();
		// // ProjectionPoint projP = merc.latLonToProj(loc.getV(), loc.getU());
		// UTMPoint utmPt = UTMPoint.LLtoUTM(new LatLonPoint(loc.getV(),
		// loc.getU()));
		// // double x0 = projP.getX();
		// // double y0 = projP.getY();
		// double x0 = utmPt.easting;
		// double y0 = utmPt.northing;
		// x0 += rad;
		// y0 += rad;
		// // LatLonPoint llp = merc.projToLatLon(x0, y0);
		// Point edgePixel = proj.forward((float)llp.getLatitude(),
		// (float)llp.getLongitude());
		//        
		// int pixelRadius = (int)Math.abs((edgePixel.getX() -
		// centerPixel.getX()));
		// // System.out.println("Pixel Radius = "+pixelRadius);
		// if(pixelRadius == 0) pixelRadius = 1;
		// // System.out.println("Adjusted Pixel Radius = "+pixelRadius);
		//        
		// //override radius calculations - set to constant
		// pixelRadius = 1;

		this.setPixelRadius(1);
		this.setRadius(1);
		setCenterConc(cconc);
		setEdgeConc(econc);
	}

	public OMParticle(Projection proj, int id, double x, double y, double z, double pH, double rad, double cconc,
		double econc) {

		this(proj, id, new Vector3D(x, y, z), pH, rad, cconc, econc);
	}

	public Vector3D getParticleLocation() {
		return particleLocation;
	}

	public double getParticleLocationX() {
		return particleLocation.getU();
	}

	public double getParticleLocationY() {
		return particleLocation.getV();
	}

	public double getParticleLocationZ() {
		return particleLocation.getW();
	}

	public void setParticleLocation(Vector3D particleLocation) {
		this.particleLocation = particleLocation;
	}

	public void setParticleLocation(double x, double y, double z) {
		this.particleLocation = new Vector3D(x, y, z);
	}

	public double getPH() {
		return pH;
	}

	public void setPH(double pH) {
		this.pH = pH;
	}

	public double getPixelRadius() {
		return pixelRadius;
	}

	public void setPixelRadius(double pixelRadius) {
		this.pixelRadius = pixelRadius;
	}

	public double getCenterConc() {
		return centerConc;
	}

	public void setCenterConc(double centerConc) {
		this.centerConc = centerConc;
	}

	public double getEdgeConc() {
		return edgeConc;
	}

	public void setEdgeConc(double edgeConc) {
		this.edgeConc = edgeConc;
	}

	public double getRealRadius() {
		return realRadius;
	}

	public void setRealRadius(double realRadius) {
		this.realRadius = realRadius;
	}

	public int getParticleID() {
		return particleID;
	}

	public void setParticleID(int particleID) {
		this.particleID = particleID;
	}

}
