/*
 * NetcdfGridWriter.java
 *
 * Created on September 19, 2007, 9:44 AM
 *
 * Applied Science Associates, Inc.
 * Copyright 2007.  All rights reserved.
 */
package com.asascience.edc.nc.io;

import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import ucar.nc2.units.DateRange;
import ucar.ma2.Array;
import ucar.ma2.DataType;
import ucar.ma2.InvalidRangeException;
import ucar.ma2.Range;
import ucar.nc2.Attribute;
import ucar.nc2.Dimension;
import ucar.nc2.FileWriter;
import ucar.nc2.Variable;
import ucar.nc2.dataset.CoordinateAxis;
import ucar.nc2.dataset.CoordinateAxis1DTime;
import ucar.nc2.dt.GridCoordSystem;
import ucar.nc2.dt.GridDatatype;
import ucar.nc2.dt.grid.GridDataset;
import ucar.unidata.geoloc.LatLonRect;

import com.asascience.edc.ArcType;
import com.asascience.edc.nc.NetcdfConstraints;
import org.apache.log4j.Logger;
import ucar.nc2.constants.AxisType;

/**
 * 
 * @author CBM
 */
public class NetcdfGridWriter {

  public final static int UNDEFINED_ERROR = -1;
  public final static int SUCCESSFUL_PROCESS = 0;
  public final static int CANCELLED_PROCESS = 1;
  private List<GridDataset> gdsList;
  private PropertyChangeSupport pcs;
  private static Logger logger = Logger.getLogger(NetcdfGridWriter.class);
  private static Logger guiLogger = Logger.getLogger("com.asascience.log." + NetcdfGridWriter.class.getName());

  /**
   * Creates a new instance of NetcdfGridWriter
   *
   * @param pcs
   */
  public NetcdfGridWriter(PropertyChangeSupport pcs) {
    this.pcs = pcs;
  }

  // TODO: Expand the reporting of errors to provide more in-depth feedback to
  // the user
  public int writeFile(String outLoc, List<GridDataset> gdsL, NetcdfConstraints cons, ArcType type) {

    gdsList = gdsL;
    List<String> gridList = cons.getSelVars();
    LatLonRect llbb = cons.getBoundingBox();
    boolean trimByZ = cons.isTrimByZ();
    String trimByName = cons.getTrimByDim();
    int trimByIndex = cons.getTrimByIndex();

    DateRange range = null;
    if ((cons.getStartTime() != null) && (cons.getEndTime() != null)) {
      range = new DateRange(cons.getStartTime(), cons.getEndTime());
    }
    int stride_h = 1, stride_z = 1, stride_time = 1;

    try {
      FileWriter writer = new FileWriter(outLoc, false);

      // Set up array lists to hold the variables, variable names, and
      // coordinate axis
      List<Variable> varList = new ArrayList<Variable>();
      List<String> varNames = new ArrayList<String>();
      List<Attribute> attsList = new ArrayList<Attribute>();

      pcs.firePropertyChange("note", null, "Gathering Attributes...");
      Thread.sleep(250);

      String gdsLoc = "";
      // gather all the global attributes from each of the GridDatasets
      for (GridDataset gds : gdsList) {
        for (Attribute a : gds.getGlobalAttributes()) {
          if (!attsList.contains(a)) {
            attsList.add(a);
          }
        }
        gdsLoc += gds.getLocationURI() + " ; ";
      }

      // add CF convention attribute
      attsList.add(new Attribute("History",
              "Translated by ASA (NetcdfGridWriter.java) using the Netcdf-Java CDM; " + "Original Dataset = "
              + gdsLoc + "; Translation date = " + new Date() + ";"));

      // write the global attributes to the file
      for (Attribute a : attsList) {
        writer.writeGlobalAttribute(a);
      }

      // for each of the datasets
      for (GridDataset gds : gdsList) {
        // add the desired grids
        for (String gridName : gridList) {
          if (Thread.currentThread().isInterrupted()) {
            return NetcdfGridWriter.CANCELLED_PROCESS;
          }
          pcs.firePropertyChange("note", null, "Preparing: " + gridName);        
          
          // get the grid and it's coordinate system
          //GridDatatype grid = gds.findGridDatatype(gridName);
          GridDatatype grid = gds.findGridDatatype(gridName);
          if (grid == null) {
            continue;
          }
          
          if (varNames.contains(gridName)) {
            continue;
          }
          varNames.add(gridName);

          GridCoordSystem gcsOrg = grid.getCoordinateSystem();

          // create a subset if needed
          Range timeRange = null;
          if (range != null & gcsOrg.hasTimeAxis1D()) {
            CoordinateAxis1DTime timeAxis = gcsOrg.getTimeAxis1D();
            int startIndex = timeAxis.findTimeIndexFromDate(range.getStart().getDate());
            int endIndex = timeAxis.findTimeIndexFromDate(range.getEnd().getDate());

            if (startIndex > -1 & endIndex > -1) {
              timeRange = new Range(startIndex, endIndex);
            }
          }

          Range levelRange = null;
          if (trimByZ) {// if trimming by level
            if (trimByIndex == -1) {
              levelRange = null;
            } else {
              levelRange = new Range(trimByIndex, trimByIndex);
            }
          } else {// if trimming by time
            if (trimByIndex != -1) {// false positive possible
              // use only the time indicated by the trimByIndex
              timeRange = new Range(trimByIndex, trimByIndex);
            }
            levelRange = null; // use all levels
          }

          if (cons.isUseAllValues()) {// if use all levels == true
            levelRange = null; // use all levels
          }

          if ((llbb != null) || (timeRange != null) || (stride_h >= 1)) {
            pcs.firePropertyChange("note", null, "Subsetting: " + gridName);
            grid = grid.makeSubset(timeRange, levelRange, llbb, 1, stride_h, stride_h);
          }

          // reduce the variable to 3D then add the variable
          Variable gridV = (Variable) grid.getVariable();

          // if(cons.getPanelType() ==
          // com.asascience.edc.gui.SelectionPanelBase.ESRI){//ESRI
          // panel
          if (!cons.isUseAllValues()) {// only if "all levels" is not
            // selected
            pcs.firePropertyChange("note", null, "Trimming: " + gridName);
            // get the trimBy Dimension & Dimension Index
            int trimDimIndex = gridV.findDimensionIndex(trimByName);
            Dimension trimDim = gridV.getDimension(trimDimIndex);
            int len = (trimDim == null) ? -1 : trimDim.getLength();

            if (trimDimIndex != -1 & len == 1) {

              if (Thread.currentThread().isInterrupted()) {
                return NetcdfGridWriter.CANCELLED_PROCESS;
              }
              pcs.firePropertyChange("note", null, "Trimming: " + gridName + ": reading data");
              // get the data
              Array arrV = gridV.read();

              pcs.firePropertyChange("note", null, "Trimming: " + gridName + ": reducing data");
              // reduce the data by trimming the trimDim
              arrV = arrV.reduce(trimDimIndex);

              pcs.firePropertyChange("note", null, "Trimming: " + gridName + ": reconstructing data");
              // create the new variable
              Variable newGridV = new Variable(gds.getNetcdfFile(), null, null, gridV.getShortName());
              newGridV.setDataType(gridV.getDataType());

              List<Dimension> dims = gridV.getDimensions();
              if (trimDim != null) {
                dims.remove(trimDim);
              }
              newGridV.setDimensions(dims);

              // Carry over all of the attributes, excluding those that may
              // reference an old coordinate system.  This may have changed due
              // to an altitude variable being removed.
              // Also make sure the Time variable is correct.
              for (Attribute a : (List<Attribute>) gridV.getAttributes()) {
                if ((!a.getName().equalsIgnoreCase("coordinates")) && (!a.getName().equalsIgnoreCase("_CoordinateAxes"))) {
                  newGridV.addAttribute(a);
                }
              }
              Attribute correctCoords = new Attribute("coordinates", newGridV.getDimensionsString());
              newGridV.addAttribute(correctCoords);

              newGridV.setCachedData(arrV, false);
              gridV = new Variable(newGridV);
            }
          }
          // }

          /**
           * Temporarily downgrade any doubles to singles for
           * compatibility with Arc
           */
          if (type == ArcType.RASTER) {
            if (gridV.getDataType() == DataType.DOUBLE) {
              gridV.setDataType(DataType.FLOAT);
            }
          }

          /**
           * THIS COULD BE WHERE THE FUNKYNESS IS COMING IN... IT'S
           * NOT = WITHOUT THIS THERE ARE NO VALUES FOR X, Y, Z OR
           * TIME
           */
          // add the coordinate axes for each variable if it's not
          // already in the list
          GridCoordSystem gcs = grid.getCoordinateSystem();
          List<CoordinateAxis> axes = gcs.getCoordinateAxes();
          for (int j = 0; j < axes.size(); j++) {
            CoordinateAxis axis = axes.get(j);
            // Don't carry over RunTime dimension!
            if (!axis.getAxisType().equals(AxisType.RunTime)) {
              // make sure the varname and dim name match
              // ONLY if a single dimension...prevents Lat(ny nx) vars
              // from being buggered....
              if (axis.getDimensions().size() == 1) {
                Dimension dim = axis.getDimension(0);
                if (dim == null) {
                  dim = axis.getDimension(0);
                }
                if (!dim.getName().equals(axis.getName())) {
                  axis.setName(dim.getName());
                  cons.setTVar(axis.getName());
                }
              }

              if (!varNames.contains(axis.getName())) {
                varNames.add(axis.getName());
                varList.add(axis);
              }
            }
          }

          // Structure parentStructure = gridV.getParentStructure();
          // if(parentStructure != null){
          // String var =
          // gridV.getName().substring(gridV.getName().lastIndexOf(".")
          // + 1);
          // Variable v = parentStructure.findVariable(var);
          // if(v != null){
          // gridV = v
          // }
          // }

          varList.add(gridV);
        }
      }

      writer.writeVariables(varList);

      pcs.firePropertyChange("note", null, "Writing File...");
      Thread.sleep(250);
      // write the ncfile to disk
      writer.finish();

      return NetcdfGridWriter.SUCCESSFUL_PROCESS;
    } catch (IOException ex) {
      logger.error("IOException", ex);
      guiLogger.error("IOException", ex);
    } catch (InvalidRangeException ex) {
      logger.error("InvalidRangeException", ex);
      guiLogger.error("InvalidRangeException", ex);
    } catch (InterruptedException ex) {
      // this catches the "cancel" button
      return NetcdfGridWriter.CANCELLED_PROCESS;
    } catch (Exception ex) {
      logger.error("Exception", ex);
      guiLogger.error("Exception", ex);
    }

    return NetcdfGridWriter.UNDEFINED_ERROR;
  }
}
