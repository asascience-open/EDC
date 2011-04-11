/*
 * GridReader.java
 *
 * Created on September 12, 2007, 9:52 AM
 *
 * Applied Science Associates, Inc.
 * Copyright 2007.  All rights reserved.
 */
package com.asascience.edc.nc;

import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Formatter;

import ucar.ma2.InvalidRangeException;
import ucar.ma2.Range;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;
import ucar.nc2.dataset.CoordinateAxis;
import ucar.nc2.dataset.CoordinateAxis1D;
import ucar.nc2.dataset.CoordinateAxis1DTime;
import ucar.nc2.dataset.NetcdfDataset;
import ucar.nc2.dt.GridCoordSystem;
import ucar.nc2.ft.FeatureDatasetFactoryManager;
import ucar.nc2.dt.grid.GeoGrid;
import ucar.nc2.dt.grid.GridDataset;
import ucar.unidata.util.Parameter;

import com.asascience.edc.nc.io.NetcdfGridWriter;
import ucar.nc2.constants.FeatureType;
import ucar.nc2.dt.GridDatatype;
import ucar.nc2.util.CancelTask;

/**
 * 
 * @author CBM
 */
public class GridReader extends NcReaderBase {

  private GridDataset gds;
  private boolean regularSpatial;
  private List<GridDatatype> geoGrids = null;
  private List<Variable> supplementalVars = new ArrayList();

  /**
   * Creates a new instance of GridReader
   *
   * @param filename
   * @param cons
   * @throws java.io.IOException
   */
  // not used
  public GridReader(String filename, NetcdfConstraints cons) throws IOException {
    super(NetcdfDataset.acquireDataset(filename, null), cons);
    Formatter errlog = new Formatter();
    CancelTask cancelTask = null;
    gds = (GridDataset) FeatureDatasetFactoryManager.open(FeatureType.GRID, ncFile.getLocation(), cancelTask, errlog);
    // RandomAccessFile raf = new RandomAccessFile(filename, "r");
    // long len = raf.length();
    // initialize();

    if (errlog.toString().length() > 0) {
      System.err.println(errlog.toString());
    }
  }

  /**
   * Creates a new instance of GridReader
   *
   * @param ncd
   * @param cons
   * @throws java.io.IOException
   */
  // used
  public GridReader(NetcdfDataset ncd, NetcdfConstraints cons) throws IOException {
    super(ncd, cons);
    // setNcfile(ncd);
    // constraints = cons;
    Formatter errlog = new Formatter();
    CancelTask cancelTask = null;
    //gds = (GridDataset) TypedDatasetFactory.open(thredds.catalog.DataType.GRID, ncd, this, errlog);
    gds = (GridDataset) FeatureDatasetFactoryManager.open(FeatureType.GRID, ncFile.getLocation(), cancelTask, errlog);
    //gds = GridDataset.open(ncd.getLocation());

    // String loc = ncd.getLocation().replace("dods", "http");
    // RandomAccessFile raf = new RandomAccessFile(loc, "r");
    // long len = raf.length();
    // gds = GridDataset.open(ncd.getLocation());
    // initialize();

    if (errlog.toString().length() > 0) {
      System.err.println(errlog.toString());
    }
  }

  @Override
  public int initialize() {
    try {
      if (gds == null) {
        return NcReaderBase.NO_GRIDDATASET;
      }

      bounds = gds.getBoundingBox();

      if (bounds == null || (bounds.getLatMax() == bounds.getLatMin())
              || bounds.getLonMax() == bounds.getLonMin()) {
        String errString = "Invalid Dataset Bounds: ";
        errString += (bounds != null) ? bounds.toString2() : "Null Bounds";
        System.err.println(errString);

        return NcReaderBase.INVALID_BOUNDS;
      }
      startTime = gds.getStartDate();
      endTime = gds.getEndDate();
      // sVars = gds.getDataVariables();

      //gds.getGrids();
      geoGrids = gds.getGrids();
      //for (GridDatatype geo : ) {
      //  geoGrids.add((GeoGrid) geo.);
      //}

      // need to check to ensure there ARE grids before grabbing one...
      if (geoGrids == null || geoGrids.isEmpty()) {
        return NcReaderBase.NO_GEOGRIDS;
      }

      // get the geoGrid with the most dimensions
      GridDatatype geoGrid = null;
      int size = 0;
      for (GridDatatype g : geoGrids) {
        int i = g.getDimensions().size();
        if (i > size) {
          geoGrid = g;
        }
        size = i;
      }
      if (geoGrid == null) {
        return NcReaderBase.NO_GEOGRIDS;
      }

      GridCoordSystem coordSys;
      CoordinateAxis1DTime timeAxis;

      coordSys = geoGrid.getCoordinateSystem();
      setRegularSpatial(coordSys.isRegularSpatial());

      List<Parameter> params = geoGrid.getProjection().getProjectionParameters();
      StringBuilder sbuff = new StringBuilder();
      for (Parameter p : params) {
        sbuff.append(p.toString());
        sbuff.append(" ; ");
      }
      // System.err.println(sbuff.toString());
      // String proj = geoGrid.getProjection().getClassName();
      String proj = sbuff.toString();
      // System.err.println("Dataset Projection: "+proj);

      ncCons.setProjection(proj);
      ncCons.setIsZPositive(coordSys.isZPositive());// increasing z values
      // mean "up" in
      // altitude

      if (coordSys.hasTimeAxis1D()) {
        hasTime = true;
        timeAxis = coordSys.getTimeAxis1D();
        times = timeAxis.getTimeDates();
        // System.err.println("timeRes: " +
        // timeAxis.getTimeResolution().getValueInSeconds());
        // System.err.println("increment: " + timeAxis.getIncrement());
        ncCons.setStartTime(times[0]);
        ncCons.setTimeUnits(timeAxis.getUnitsString());
        // //calculate the time interval in seconds
        // long dt = 0;
        // if(times.length > 1){
        // dt = (times[1].getTime() - times[0].getTime()) / 1000;
        // }
        // System.err.println(String.valueOf(timeAxis.getTimeResolution().getValueInSeconds()));
        // System.err.println(String.valueOf(dt));
        // ncCons.setTimeInterval(String.valueOf(dt));
        ncCons.setTimeInterval(String.valueOf(timeAxis.getTimeResolution().getValueInSeconds()));
        ncCons.setTimeDim(timeAxis.getDimensionsString());
        // System.err.println("TimeName="+timeAxis.getName());
        ncCons.setTVar(timeAxis.getName());
      } else {
        hasTime = false;
        ncCons.setTimeDim("null");
        System.err.println("Time Axis is null");
      }
      CoordinateAxis1D vert = coordSys.getVerticalAxis();
      if (vert != null) {
        ncCons.setZDim(vert.getDimensionsString());
      } else {
        ncCons.setZDim("null");
        System.err.println("Vertical Axis is null");
      }
      CoordinateAxis xAxis = coordSys.getXHorizAxis();
      if (xAxis != null) {
        ncCons.setXDim(xAxis.getDimensionsString());
      } else {
        ncCons.setXDim("null");
        System.err.println("X Axis is null");
      }
      CoordinateAxis yAxis = coordSys.getYHorizAxis();
      if (yAxis != null) {
        ncCons.setYDim(yAxis.getDimensionsString());
      } else {
        ncCons.setYDim("null");
        System.err.println("Y Axis is null");
      }
      return NcReaderBase.INIT_OK;
    } catch (Exception ex) {
      System.err.println("GR:initialize:");
      ex.printStackTrace();
    }
    return NcReaderBase.UNDEFINED_ERROR;
    // System.err.println(bounds.getCenterLon());
    // System.err.println(bounds.getLatMax() + "\n" + bounds.getLonMax() +
    // "\n" + bounds.getLatMin() + "\n" + bounds.getLonMin());
    // System.err.println("Start Time: " + startTime.toString());
    // System.err.println("End Time: " + endTime.toString());

  }

  //
  // public int getTimeIndex(Date date){
  // return getTimeIndex(date, times);
  // }
  // public int getTimeIndex(Date date, Date[] dates){
  // for(int i = 0; i < dates.length; i++){
  // if(((Date)dates[i]).compareTo(date) == 0){
  // return i;
  // }
  // }
  // return -1;
  // }
  // used
  @Override
  public int extractData2(NetcdfConstraints cons, String outnc, List<GridDataset> gdsList, PropertyChangeSupport pcs) {
    try {
      NetcdfGridWriter write = new NetcdfGridWriter(pcs);

      return write.writeFile(outnc, gdsList, cons, null);

    } catch (Exception ex) {
      ex.printStackTrace();
    }
    return -1;
  }

  // not used
  public void extractData(NetcdfConstraints cons) {
    try {
      Range timeRange = new Range(0, 10);
      Range zRange = null;
      Range yRange = new Range(1, 10);
      Range xRange = new Range(1, 10);

      List<String> varNames = cons.getSelVars();
      String name;
      GridCoordSystem coordSys;
      CoordinateAxis1DTime timeAxis = null;
      List<Range> yxRanges;
      CoordinateAxis1D xaxis;
      CoordinateAxis1D yaxis;
      GeoGrid subgrid;
      GeoGrid outgrid;
      List<GeoGrid> gridsOut = new ArrayList<GeoGrid>();
      for (Iterator i = varNames.iterator(); i.hasNext();) {
        name = (String) i.next();
        GeoGrid grd = getGeoGrid(name);
        coordSys = grd.getCoordinateSystem();

        // get y & x ranges from the drawn box
        yxRanges = coordSys.getRangesFromLatLonRect(cons.getBoundingBox());
        yRange = yxRanges.get(0);
        System.err.println("y = " + yRange.toString());
        xRange = yxRanges.get(1);
        System.err.println("x = " + xRange.toString());

        // see what the values for those ranges are
        xaxis = (CoordinateAxis1D) coordSys.getXHorizAxis();
        yaxis = (CoordinateAxis1D) coordSys.getYHorizAxis();

        System.err.println("First y: " + yaxis.getCoordValue(yRange.first()));
        System.err.println("Last y: " + yaxis.getCoordValue(yRange.last()));
        System.err.println("First x: " + xaxis.getCoordValue(xRange.first()));
        System.err.println("Last y: " + xaxis.getCoordValue(xRange.last()));

        // get the timeRange axis and determine the range selected
        if (coordSys.hasTimeAxis1D()) {
          timeAxis = coordSys.getTimeAxis1D();
          int s = timeAxis.findTimeIndexFromDate(cons.getStartTime());
          int e = timeAxis.findTimeIndexFromDate(cons.getEndTime());
          System.err.println("t = " + s + ":" + e + ":1");
          timeRange = new Range(s, e);
        }

        subgrid = grd.subset(timeRange, zRange, yRange, xRange);
        if (subgrid == null) {
          continue;
        }
        gridsOut.add(subgrid);
        // System.err.println(grd.getName());
      }

      // write the grids to ncfiles
      for (Iterator i = gridsOut.iterator(); i.hasNext();) {
        try {
          outgrid = (GeoGrid) i.next();
          outgrid.writeFile(ncFile.getTitle() + "_" + outgrid.getName() + ".nc");
        } catch (IOException ex) {
          ex.printStackTrace();
        }
      }
    } catch (InvalidRangeException ex) {
      ex.printStackTrace();
    }
  }

  public NetcdfFile getNcfile() {
    return ncFile;
  }

  public void setNcfile(NetcdfFile ncfile) {
    this.ncFile = ncfile;
  }

  public List<GridDatatype> getGeoGrids() {
    return geoGrids;
  }

  public GeoGrid getGeoGrid(String gdsName) {
    GeoGrid geoGrid;
    for (int i = 0; i < geoGrids.size(); i++) {
      geoGrid = (GeoGrid) geoGrids.get(i);
      if (gdsName.equals(geoGrid.getName())) {
        return geoGrid;
      }
    }

    return null;
  }

  public GridDataset getGridDataset() {
    return gds;
  }

  public void setGridDataset(GridDataset gds) {
    this.gds = gds;
  }

  public boolean isRegularSpatial() {
    return regularSpatial;
  }

  public void setRegularSpatial(boolean regularSpatial) {
    this.regularSpatial = regularSpatial;
  }
}
