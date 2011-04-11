/*
 * Applied Science Associates, Inc.
 * Copyright 2007. All rights reserved.
 */

/*
 * MassParticle.java
 *
 * Created on Mar 13, 2008, 1:08:35 PM
 *
 */
package com.asascience.edc.particle;

/**
 * 
 * @author CBM <cmueller@asascience.com>
 */
public class MassParticle {

  private double lat;
  private double lon;
  private double depth;
  private double mass;
  private double molecWeight;
  private boolean onBottom = false;
  private boolean onLand = false;

  /**
   * Creates a new instance of MassParticle
   *
   * @param lat
   *            The Latitude position of the particle
   * @param lon
   *            The Longitude position of the particle
   * @param depth
   *            The Vertical position of the particle
   * @param mass
   *            The Mass of the particle
   * @param molecWeight
   *            The Molecular Weight of the chemical
   */
  public MassParticle(double lat, double lon, double depth, double mass, double molecWeight) {
    this.lat = lat;
    this.lon = lon;
    if (lat == -9999999 & lon == -9999999) {
      onLand = true;
    }
    this.depth = depth;
    if (depth == -9999999) {
      onBottom = true;
    }
    this.mass = mass;
    this.molecWeight = molecWeight;
  }

  public double getLat() {
    return lat;
  }

  public void setLat(double lat) {
    this.lat = lat;
  }

  public double getLon() {
    return lon;
  }

  public void setLon(double lon) {
    this.lon = lon;
  }

  public double getDepth() {
    return depth;
  }

  public void setDepth(double depth) {
    this.depth = depth;
  }

  public double getMass() {
    return mass;
  }

  public void setMass(double mass) {
    this.mass = mass;
  }

  public double getMolecWeight() {
    return molecWeight;
  }

  public boolean isOnBottom() {
    return onBottom;
  }

  public void setOnBottom(boolean onBottom) {
    this.onBottom = onBottom;
  }

  public boolean isOnLand() {
    return onLand;
  }

  public void setOnLand(boolean onLand) {
    this.onLand = onLand;
  }
}
