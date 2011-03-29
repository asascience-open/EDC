/*
 * Applied Science Associates, Inc.
 * Copyright 2008. All Rights Reserved.
 *
 * SimpleParticle.java
 *
 * Created on Dec 11, 2008 @ 2:37:52 PM
 */

package com.asascience.edc.particle;

/**
 * 
 * @author CBM <cmueller@asascience.com>
 */

public class SimpleParticle {

	private double posX;
	private double posY;
	private double posZ;
	private double pH;
	private double rad;
	private double radZ;
	private double centerConc;
	private double edgeConc;
	private double mass;
	private int runId;

	public SimpleParticle(double posY, double posX, double posZ, double pH, double rad, double radZ, double centerConc,
		double edgeConc, double mass) {
		this.posX = posX;
		this.posY = posY;
		this.posZ = posZ;
		this.pH = pH;
		this.rad = rad;
		this.radZ = radZ;
		this.centerConc = centerConc;
		this.edgeConc = edgeConc;
		this.mass = mass;
	}

	public double getPosX() {
		return posX;
	}

	public void setPosX(double posX) {
		this.posX = posX;
	}

	public double getPosY() {
		return posY;
	}

	public void setPosY(double posY) {
		this.posY = posY;
	}

	public double getPosZ() {
		return posZ;
	}

	public void setPosZ(double posZ) {
		this.posZ = posZ;
	}

	public double getPH() {
		return pH;
	}

	public void setPH(double ph) {
		pH = ph;
	}

	public double getRad() {
		return rad;
	}

	public void setRad(double rad) {
		this.rad = rad;
	}

	public double getRadZ() {
		return radZ;
	}

	public void setRadZ(double radZ) {
		this.radZ = radZ;
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

	public double getMass() {
		return mass;
	}

	public void setMass(double mass) {
		this.mass = mass;
	}

	public int getRunId() {
		return runId;
	}

	public void setRunId(int runId) {
		this.runId = runId;
	}
}
