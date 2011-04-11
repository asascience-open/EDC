/*
 * Cell.java
 *
 * Created on December 4, 2007, 12:54 PM
 *
 * Applied Science Associates, Inc.
 * Copyright 2007.  All rights reserved.
 */
package com.asascience.utilities;

import java.awt.geom.Point2D;

/**
 * 
 * @author CBM
 */
public class Cell {

  /**
   * The lower-left corner of the cell
   */
  public Point2D p1;
  /**
   * The lower-right corner of the cell
   */
  public Point2D p2;
  /**
   * The upper-right corner of the cell
   */
  public Point2D p3;
  /**
   * The upper-left corner of the cell
   */
  public Point2D p4;
  private int type;
  private int level;
  private double depth;
  private double elev;
  private double uTop;
  private double vTop;
  private double uMid;
  private double vMid;
  private double uBot;
  private double vBot;

  /**
   * Creates a new instance of Cell
   */
  public Cell() {
    p1 = new Point2D.Float();
    p2 = new Point2D.Float();
    p3 = new Point2D.Float();
    p4 = new Point2D.Float();

    resetCurrentVals();
  }

  public boolean contains(Vector3D position) {
    double qLat = position.getV();
    double qLon = position.getU();

    if (qLat >= p1.getY() & qLat <= p3.getY())// in the cell in y-dir
    {
      if (qLon >= p1.getX() & qLon <= p3.getX())// in the cell in x-dir
      {
        return true;
      }
    }

    return false;
  }

  public void resetAllVals() {
    type = -1;
    level = -1;
    depth = Double.NaN;
    elev = Double.NaN;

    resetCurrentVals();
  }

  public void resetCurrentVals() {
    uTop = Double.NaN;
    vTop = Double.NaN;
    uMid = Double.NaN;
    vMid = Double.NaN;
    uBot = Double.NaN;
    vBot = Double.NaN;
  }

  public double getCenterX() {
    return (p1.getX() + ((p3.getX() - p1.getX()) * 0.5));
  }

  public double getCenterY() {
    return (p1.getY() + ((p3.getY() - p1.getY()) * 0.5));
  }

  public int getType() {
    return type;
  }

  public void setType(int type) {
    this.type = type;
  }

  public int getLevel() {
    return level;
  }

  public void setLevel(int level) {
    this.level = level;
  }

  public double getDepth() {
    return depth;
  }

  public void setDepth(double depth) {
    this.depth = depth;
  }

  public double getUTop() {
    return uTop;
  }

  public void setUTop(double uTop) {
    this.uTop = uTop;
  }

  public double getVTop() {
    return vTop;
  }

  public void setVTop(double vTop) {
    this.vTop = vTop;
  }

  public double getUMid() {
    return uMid;
  }

  public void setUMid(double uMid) {
    this.uMid = uMid;
  }

  public double getVMid() {
    return vMid;
  }

  public void setVMid(double vMid) {
    this.vMid = vMid;
  }

  public double getUBot() {
    return uBot;
  }

  public void setUBot(double uBot) {
    this.uBot = uBot;
  }

  public double getVBot() {
    return vBot;
  }

  public void setVBot(double vBot) {
    this.vBot = vBot;
  }

  public double getElev() {
    return elev;
  }

  public void setElev(double elev) {
    this.elev = elev;
  }
}
