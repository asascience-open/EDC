/*
 * Applied Science Associates, Inc.
 * Copyright 2007. All rights reserved.
 */

/*
 * NonGridReader.java
 *
 * Created on Feb 12, 2008, 10:29:53 AM
 *
 */
package com.asascience.edc.nc;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.apache.log4j.Logger;

import ucar.ma2.Array;
import ucar.ma2.IndexIterator;
import ucar.nc2.Dimension;
import ucar.nc2.Variable;
import ucar.nc2.dataset.NetcdfDataset;
import ucar.nc2.units.DateUnit;
import ucar.unidata.geoloc.LatLonPointImpl;
import ucar.unidata.geoloc.LatLonRect;

/**
 * 
 * @author CBM <cmueller@asascience.com>
 */
public class NonGridReader extends NcReaderBase {

  private List<Dimension> dims = null;
  private List<Variable> vars = null;
  private Dimension unlimDim = null;
  private Variable tVar = null;
  private Variable xVar = null;
  private Variable yVar = null;
  private Variable zVar = null;
  private static Logger logger = Logger.getLogger(NonGridReader.class);
  private static Logger guiLogger = Logger.getLogger("com.asascience.log." + NonGridReader.class.getName());

  /**
   * Creates a new instance of NonGridReader
   *
   * @param ncd
   * @param cons
   * @throws java.io.IOException
   */
  public NonGridReader(NetcdfDataset ncd, NetcdfConstraints cons) throws IOException {
    super(ncd, cons);
  }

  @Override
  public int initialize() {
    try {
      setUnlimDim(ncFile.getUnlimitedDimension());
      setDims(ncFile.getDimensions());
      setVars(ncFile.getVariables());
      return NcReaderBase.INIT_OK;
    } catch (Exception ex) {
      logger.error("Exception", ex);
      guiLogger.error("Exception", ex);
    }
    return NcReaderBase.UNDEFINED_ERROR;
  }

  private double findMinimum(Variable v) {
    try {
      double ret = Double.POSITIVE_INFINITY;
      Array arr = v.read();
      IndexIterator iter = arr.getIndexIterator();
      double val;
      while (iter.hasNext()) {
        val = iter.getDoubleNext();
        ret = (val < ret) ? val : ret;
      }

      return ret;
    } catch (IOException ex) {
      logger.error("IOException", ex);
      guiLogger.error("IOException", ex);
    } catch (Exception ex) {
      logger.error("Exception", ex);
      guiLogger.error("Exception", ex);
    }

    return Double.NaN;
  }

  private double findMaximum(Variable v) {
    try {
      double ret = Double.NEGATIVE_INFINITY;
      Array arr = v.read();
      IndexIterator iter = arr.getIndexIterator();
      double val;
      while (iter.hasNext()) {
        val = iter.getDoubleNext();
        ret = (val > ret) ? val : ret;
      }

      return ret;
    } catch (IOException ex) {
      logger.error("IOException", ex);
      guiLogger.error("IOException", ex);
    } catch (Exception ex) {
      logger.error("Exception", ex);
      guiLogger.error("Exception", ex);
    }
    logger.warn("findMaximum: didn't hit \"ret\"");
    guiLogger.warn("findMaximum: didn't hit \"ret\"");
    return Double.NaN;
  }

  private boolean calculateBounds() {
    try {
      double xMin, xMax, yMin, yMax;
      xMin = findMinimum(xVar);
      xMax = findMaximum(xVar);
      yMin = findMinimum(yVar);
      yMax = findMaximum(yVar);

      bounds = new LatLonRect(new LatLonPointImpl(yMin, xMin), new LatLonPointImpl(yMax, xMax));

      return true;
    } catch (Exception ex) {
      logger.error("Exception", ex);
      guiLogger.error("Exception", ex);
    }
    return false;
  }

  private boolean calculateTimes() {
    try {
      Array arr = tVar.read();
      DateUnit du;
      String uString = tVar.getUnitsString();
      IndexIterator iter = arr.getIndexIterator();
      times = new Date[(int) tVar.getSize()];
      int i = 0;
      double t;
      while (iter.hasNext()) {
        t = iter.getDoubleNext();
        du = new DateUnit(t + " " + uString);
        times[i] = du.getDate();
        i++;
      }

      return true;
    } catch (Exception ex) {
      logger.error("Exception", ex);
      guiLogger.error("Exception", ex);
    }
    return false;
  }

  private void determineTimes() {
    try {
      if (tVar != null) {

        calculateTimes();

        setStartTime(times[0]);
        setEndTime(times[times.length - 1]);
        // if it hasn't errored by this point - assume it has time...
        setHasTime(true);
        // set the rest of the time stuff.
        ncCons.setTVar(tVar.getName());
        ncCons.setTimeDim(getTimeDim());
        ncCons.setTimeUnits(tVar.getUnitsString());
        // calculate the time interval in seconds
        long dt = 0;
        if (times.length > 1) {
          dt = (times[1].getTime() - times[0].getTime()) / 1000;
        }
        ncCons.setTimeInterval(String.valueOf(dt));
      } else {
        setHasTime(false);
        ncCons.setTimeDim("null");
        logger.warn("Time variable is null");
        guiLogger.warn("Time variable is null");
      }
    } catch (Exception ex) {
      logger.error("Exception", ex);
      guiLogger.error("Exception", ex);
    }
  }

  public void applyMap(String time, String x, String y, String z) {
    setTimeDim(time);
    setXDim(x);
    setYDim(y);
    setZDim(z);

    tVar = ncFile.findVariable(time);
    xVar = ncFile.findVariable(x);
    yVar = ncFile.findVariable(y);
    zVar = ncFile.findVariable(z);

    calculateBounds();
    determineTimes();
  }

  public List<String> getDimNames() {
    List<String> dimNames = new ArrayList();
    for (Dimension d : dims) {
      dimNames.add(d.getName());
    }

    return dimNames;
  }

  public List<Dimension> getDims() {
    return dims;
  }

  public void setDims(List<Dimension> dims) {
    this.dims = dims;
  }

  public List<Variable> getVars() {
    return vars;
  }

  public void setVars(List<Variable> vars) {
    this.vars = vars;
  }

  public Dimension getUnlimDim() {
    return unlimDim;
  }

  public void setUnlimDim(Dimension unlimDim) {
    this.unlimDim = unlimDim;
  }
}
