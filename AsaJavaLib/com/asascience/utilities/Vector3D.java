/*
 * Vector3D.java
 *
 * Created on September 25, 2007, 1:34 PM
 *
 * Applied Science Associates, Inc.
 * Copyright 2007.  All rights reserved.
 */

package com.asascience.utilities;

/**
 * Holds the u, v and w components of a vector.
 * 
 * @author CBM
 */
public class Vector3D {
	private double u;
	private double v;
	private double w;

	/**
	 * Creates a new instance of Vector3D
	 */
	public Vector3D() {
	}

	/**
	 * Creates a new instance of Vector3D.
	 * 
	 * @param u
	 *            <CODE>double</CODE> The u-component of the vector.
	 * @param v
	 *            <CODE>double</CODE> The v-component of the vector.
	 * @param w
	 *            <CODE>double</CODE> The w-component of the vector.
	 */
	public Vector3D(double u, double v, double w) {
		this.u = u;
		this.v = v;
		this.w = w;
	}

	/**
	 * Returns the u component of the vector.
	 * 
	 * @return <CODE>double</CODE> The u component.
	 */
	public double getU() {
		return u;
	}

	/**
	 * Sets the u component of the vector.
	 * 
	 * @param u
	 *            <CODE>double</CODE> The u component.
	 */
	public void setU(double u) {
		this.u = u;
	}

	/**
	 * Returns the v component of the vector.
	 * 
	 * @return <CODE>double</CODE> The v component.
	 */
	public double getV() {
		return v;
	}

	/**
	 * Sets the v component of the vector.
	 * 
	 * @param v
	 *            <CODE>double</CODE> The v component.
	 */
	public void setV(double v) {
		this.v = v;
	}

	/**
	 * Returns the w component of the vector.
	 * 
	 * @return <CODE>double</CODE> The w component.
	 */
	public double getW() {
		return w;
	}

	/**
	 * Sets the w component of the vector.
	 * 
	 * @param w
	 *            <CODE>double</CODE> The w component.
	 */
	public void setW(double w) {
		this.w = w;
	}

	public String toString2() {
		return "Vector[u,v,w]: [" + getU() + "," + getV() + "," + getW() + "]";
	}

	// <editor-fold defaultstate="collapsed" desc=" Operators ">
	public static Vector3D add(Vector3D v1, Vector3D v2) {
		return (new Vector3D(v1.getU() + v2.getU(), v1.getV() + v2.getV(), v1.getW() + v2.getW()));
	}

	public static Vector3D sub(Vector3D v1, Vector3D v2) {
		return (new Vector3D(v1.getU() - v2.getU(), v1.getV() - v2.getV(), v1.getW() - v2.getW()));
	}

	public static Vector3D mult(Vector3D v, double d) {
		return (new Vector3D(v.getU() * d, v.getV() * d, v.getW() * d));
	}

	public static Vector3D div(Vector3D v, double d) {
		return (new Vector3D(v.getU() / d, v.getV() / d, v.getW() / d));
	}

	public static Vector3D cross(Vector3D v1, Vector3D v2) {
		return (new Vector3D(v1.getV() * v2.getW() - v1.getW() * v2.getV(), v1.getW() * v2.getU() - v1.getU()
			* v2.getW(), v1.getU() * v2.getV() - v1.getV() * v2.getU()));
	}

	public Vector3D cross(Vector3D other) {
		return cross(this, other);
	}

	public static double dot(Vector3D v1, Vector3D v2) {
		return (v1.getU() * v2.getU() + v1.getV() * v2.getV() + v1.getW() * v2.getW());
	}

	public double dot(Vector3D other) {
		return dot(this, other);
	}

	// </editor-fold>
}
