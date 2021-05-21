/*
 * DataFileNcom.java
 *
 * Created on October 9, 2007, 3:43 PM
 *
 * Applied Science Associates, Inc.
 * Copyright 2007.  All rights reserved.
 */
package com.asascience.edp.datafile.hydro;

import java.io.IOException;
import java.util.Formatter;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import ucar.nc2.constants.FeatureType;
import ucar.nc2.dataset.CoordinateAxis1D;
import ucar.nc2.dt.GridCoordSystem;
import ucar.nc2.dt.GridDatatype;
import ucar.nc2.dt.grid.GeoGrid;
import ucar.nc2.dt.grid.GridDataset;
import ucar.nc2.ft.FeatureDatasetFactoryManager;
import ucar.nc2.util.CancelTask;

/**
 * 
 * @author CBM
 */
public class DataFileNcom extends DataFileNcGrid {

  private String m_z = "depth";
  private String m_tau = "tau";
  private String m_time = "time";
  private String m_x = "lon";
  private String m_y = "lat";
  private String m_v = "water_v";
  private String m_u = "water_u";
  private String m_temp = "water_temp";
  private String m_sal = "salinity";
  private String m_surfele = "surf_el";

  /**
   * Creates a new instance of DataFileNcom
   */
  public DataFileNcom() {
  }

  @Override
  protected void loadDataFile() {
    try {
      // ncfile = NetcdfFile.open(this.getDataFile());
      Formatter errlog = new Formatter();
      CancelTask cancelTask = null;
      GridDataset gds = (GridDataset) FeatureDatasetFactoryManager.open(FeatureType.GRID, getDataFile(), cancelTask, errlog);

      if (gds == null) {
        System.out.println("Couldn't open ncfile:\n" + errlog); // get
        // the
        // grids
      }
      for (GridDatatype geo : gds.getGrids()) {
        geoGrids.add((GeoGrid) geo);
      }

      for (GeoGrid g : geoGrids) {
        String n = g.getName();
        // System.out.println(n + " vs " +g.getName());
        if (n.equals(m_u)) {
          uGrid = g;
          // System.out.println("uGrid");
        } else if (n.equals(m_v)) {
          vGrid = g;
          // System.out.println("vGrid");
        } else if (n.equals(m_temp)) {
          tGrid = g;
          // System.out.println("tempGrid");
        } else if (n.equals(m_sal)) {
          sGrid = g;
          // System.out.println("salGrid");
        } else if (n.equals(m_surfele)) {
          seGrid = g;
          // System.out.println("seGrid");
        } else {
          // System.out.println("Grid \"" + g.getName() +
          // "\" not assigned.");
        }
      }

      // get the x, y, z & time axes

      /**
       * Ensures that a grid that uses all dimensions is selected for the
       * initialization.
       */
      int gi = 0;
      GeoGrid g;
      do {
        g = geoGrids.get(gi++);
        if (gi == geoGrids.size()) {
          break;
        }
      } while (g.getDimensions().size() != gds.getNetcdfDataset().getDimensions().size());

      GridCoordSystem coordSys = g.getCoordinateSystem();
      // GridCoordSystem coordSys = geoGrids.get(0).getCoordinateSystem();
      if (coordSys.hasTimeAxis1D()) {
        timeAxis = coordSys.getTimeAxis1D();
        tVals = collectTimes(timeAxis.getTimeDates());

        GregorianCalendar c = new GregorianCalendar(TimeZone.getTimeZone("UTC"));// Calendar.getInstance();
        c.setTime(timeAxis.getTimeDate(0));
        setStartTime((GregorianCalendar) c.clone());
        c.setTime(timeAxis.getTimeDate(tVals.length - 1));
        setEndTime((GregorianCalendar) c.clone());
        this.setTimeIncrement(tVals[1] - tVals[0]);
      } else {
        System.err.println("Time Axis is null");
      }
      zAxis = coordSys.getVerticalAxis();
      if (zAxis != null) {
        zVals = zAxis.getCoordValues();
      } else {
        System.err.println("Vertical Axis is null");
      }

      xAxis = (CoordinateAxis1D) coordSys.getXHorizAxis();
      if (xAxis != null) {
        xVals = xAxis.getCoordValues();
      } else {
        System.err.println("X Axis is null");
      }

      yAxis = (CoordinateAxis1D) coordSys.getYHorizAxis();
      if (yAxis != null) {
        yVals = yAxis.getCoordValues();
      } else {
        System.err.println("Y Axis is null");
      }

      // System.out.println("x=" + xVals[xVals.length - 1] + " y=" +
      // yVals[yVals.length - 1] + " z=" + zVals[zVals.length - 1]);

    } catch (IOException ex) {
      System.out.println("DFN:loadDataFile:");
      ex.printStackTrace();
    }
  }
}
