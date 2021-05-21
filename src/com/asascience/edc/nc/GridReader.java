/*
 * GridReader.java
 *
 * Created on September 12, 2007, 9:52 AM
 *
 * Applied Science Associates, Inc.
 * Copyright 2007.  All rights reserved.
 */
package com.asascience.edc.nc;

import java.io.IOException;
import java.util.Date;
import java.util.Formatter;
import java.util.List;

import org.apache.log4j.Logger;

import ucar.nc2.NetcdfFile;
import ucar.nc2.constants.FeatureType;
import ucar.nc2.dataset.CoordinateAxis;
import ucar.nc2.dataset.CoordinateAxis1D;
import ucar.nc2.dataset.CoordinateAxis1DTime;
import ucar.nc2.dataset.NetcdfDataset;
import ucar.nc2.dt.GridCoordSystem;
import ucar.nc2.dt.GridDatatype;
import ucar.nc2.dt.grid.GeoGrid;
import ucar.nc2.dt.grid.GridDataset;
import ucar.nc2.ft.FeatureDatasetFactoryManager;
import ucar.nc2.util.CancelTask;
import ucar.unidata.util.Parameter;

/**
 * 
 * @author CBM
 */
public class GridReader extends NcReaderBase {

  private GridDataset gds;
  private boolean regularSpatial;
  private List<GridDatatype> geoGrids = null;
  private static Logger logger = Logger.getLogger(GridReader.class);
  private static Logger guiLogger = Logger.getLogger("com.asascience.log." + GridReader.class.getName());

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

    if (errlog.toString().length() > 0) {
      logger.error(errlog.toString());
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
    String ncLoc = ncFile.getLocation();
    // netCDf java will change dods to http -- which won't work for secure sites, changing it here to https
    if (ncLoc.startsWith("dods:")) { 
    	   ncLoc = "https:" + ncLoc.substring(5); 
    	 }
    System.out.println("grid reader "+ ncLoc);
    
    gds = (GridDataset) FeatureDatasetFactoryManager.open(FeatureType.GRID, ncLoc, cancelTask, errlog);
    //gds = GridDataset.open(ncd.getLocation());

    // String loc = ncd.getLocation().replace("dods", "http");
    // RandomAccessFile raf = new RandomAccessFile(loc, "r");
    // long len = raf.length();
    // gds = GridDataset.open(ncd.getLocation());
    // initialize();

    if (errlog.toString().length() > 0) {
      logger.error(errlog.toString());
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
        logger.error(errString);

        return NcReaderBase.INVALID_BOUNDS;
      }
      startTime = gds.getCalendarDateStart().toDate();
      endTime = gds.getCalendarDateEnd().toDate();
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
    	  if(!p.toString().trim().startsWith("earth_radius")){
    		  sbuff.append(p.toString());
    		  sbuff.append(" ; ");
    	  }
      }
      // String proj = geoGrid.getProjection().getClassName();
      String proj = sbuff.toString();

      ncCons.setProjection(proj);
      ncCons.setIsZPositive(coordSys.isZPositive());// increasing z values
      // mean "up" in
      // altitude

      if (coordSys.hasTimeAxis1D()) {
        hasTime = true;
        timeAxis = coordSys.getTimeAxis1D();
        times = timeAxis.getTimeDates();
        ncCons.setStartTime(times[0]);
        ncCons.setTimeUnits(timeAxis.getUnitsString());
        // //calculate the time interval in seconds
        // long dt = 0;
        // if(times.length > 1){
        // dt = (times[1].getTime() - times[0].getTime()) / 1000;
        // }
        // ncCons.setTimeInterval(String.valueOf(dt));
        ncCons.setTimeInterval(String.valueOf(timeAxis.getTimeResolution().getValueInSeconds()));
        ncCons.setTimeDim(timeAxis.getDimensionsString());
        ncCons.setTVar(timeAxis.getName());
      } else {
        hasTime = false;
        ncCons.setTimeDim("null");
        logger.error("Time Axis is null");
        guiLogger.error("Time axis is null");
      }
      CoordinateAxis1D vert = coordSys.getVerticalAxis();
      if (vert != null) {
        ncCons.setZDim(vert.getDimensionsString());
      } else {
        ncCons.setZDim("null");
        logger.error("Vertical Axis is null");
        guiLogger.error("Vertical Axis is null");
      }
      CoordinateAxis xAxis = coordSys.getXHorizAxis();
      if (xAxis != null) {
        ncCons.setXDim(xAxis.getDimensionsString());
      } else {
        ncCons.setXDim("null");
        logger.error("X Axis is null");
        guiLogger.error("X Axis is null");
      }
      CoordinateAxis yAxis = coordSys.getYHorizAxis();
      if (yAxis != null) {
        ncCons.setYDim(yAxis.getDimensionsString());
      } else {
        ncCons.setYDim("null");
        logger.error("Y Axis is null");
        guiLogger.error("Y Axis is null");
      }
      return NcReaderBase.INIT_OK;
    } catch (Exception ex) {
      logger.error("GR:initialize:", ex);
      guiLogger.error("GR:initialize:", ex);
    }
    return NcReaderBase.UNDEFINED_ERROR;
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
