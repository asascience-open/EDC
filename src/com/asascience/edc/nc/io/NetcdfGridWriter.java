/*
 * NetcdfGridWriter.java
 *
 * Created on September 19, 2007, 9:44 AM
 *
 * Applied Science Associates, Inc.
 * Copyright 2007.  All rights reserved.
 */
package com.asascience.edc.nc.io;

import java.awt.geom.Path2D;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.asascience.edc.ArcType;
import com.asascience.edc.nc.NetcdfConstraints;
import com.asascience.edc.utils.PolygonUtils;

import ucar.ma2.Array;
import ucar.ma2.DataType;
import ucar.ma2.Index;
import ucar.ma2.InvalidRangeException;
import ucar.ma2.Range;
import ucar.nc2.Attribute;
import ucar.nc2.Dimension;
import ucar.nc2.Group;
import ucar.nc2.NetcdfFileWriter;
import ucar.nc2.Variable;
import ucar.nc2.constants.AxisType;
import ucar.nc2.dataset.CoordinateAxis;
import ucar.nc2.dataset.CoordinateAxis1DTime;
import ucar.nc2.dt.GridCoordSystem;
import ucar.nc2.dt.GridDatatype;
import ucar.nc2.dt.grid.GridDataset;
import ucar.nc2.units.DateRange;
import ucar.unidata.geoloc.LatLonPoint;
import ucar.unidata.geoloc.LatLonPointImpl;
import ucar.unidata.geoloc.LatLonRect;

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
  private static final String FILL_VAL_ATTRIUBTE = "_FillValue";
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
  public int  prepareToWriteFile(String outLoc, List<GridDataset> gdsL, NetcdfConstraints cons, 
		  			   ArcType type, 
		  			 List<Variable> varList, List<LatLonPointImpl> polygonVertices,
		  			List<GridDatatype> trimmedGrids ) {

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
    int stride_h = 1;

    try {

  

      pcs.firePropertyChange("note", null, "Gathering Attributes...");
      Thread.sleep(250);

	  List<String> varNames = new ArrayList<String>();
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
            int startIndex = timeAxis.findTimeIndexFromCalendarDate(range.getStart().getCalendarDate());
            int endIndex = timeAxis.findTimeIndexFromCalendarDate(range.getEnd().getCalendarDate());

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
          //  System.out.println("Grid " + rect.getLowerLeftPoint().getX() + rect.getLowerLeftPoint().getY());
          //  System.out.println("llb " + llbb.getLatMin() + " " + llbb.getLatMax()+","+llbb.getLonMin()+" " +llbb.getLonMax());
            grid = grid.makeSubset(timeRange, levelRange, llbb, 1, stride_h, stride_h);
            trimmedGrids.add(grid);
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
                if (!dim.getShortName().equals(axis.getShortName())) {
                  axis.setName(dim.getShortName());
                  cons.setTVar(axis.getShortName());
                }
              }

              if (!varNames.contains(axis.getShortName())) {
                varNames.add(axis.getShortName());
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
  
  
 
  public void writeDimensions(NetcdfFileWriter ncWriter, Map<Integer, List<Variable>> varMap, 
		  					  Map<Integer, ArrayList<Variable>> vertexVarMap
		  					  ){
	
      List<Attribute> attsList = new ArrayList<Attribute>();
      
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
    

//     ncWriter.addGroupAttribute(null, new Attribute("History",
//              "Translated by ASA (NetcdfGridWriter.java) using the Netcdf-Java CDM; " + "Original Dataset = "
//              + gdsLoc + "; Translation date = " + new Date() + ";"));
      
	  // create all of the dimensions
	  for(Integer vertex : varMap.keySet()){
		  Group vertexGroup;
		  if(varMap.size() == 1)
			  vertexGroup = null;
		  else {
		        Group root = ncWriter.addGroup(null, ""); //Gets the root group

			  vertexGroup =  ncWriter.addGroup(root, "Track_Pt_"+vertex);
			 
		       // ncWriter.addGroupAttribute(vertexGroup, new Attribute("vertex", 100));

		  }
		  List<String> dimensionsAdded = new ArrayList<String>();
		  ArrayList<Variable> vertexArrayList = new ArrayList<Variable>();
		  Map<String, Dimension> dimensionMap = new HashMap<String, Dimension>();
		  for(Variable var : varMap.get(vertex)){
	
			  List<Dimension> dims = new ArrayList<Dimension>();

			  for(Dimension dim : var.getDimensions()){
				  if(!dimensionsAdded.contains(dim.getName())) {
					  Dimension addedDim = ncWriter.addDimension(vertexGroup, dim.getName(), dim.getLength());
							  
					  dimensionMap.put(dim.getName(), addedDim);
					  dimensionsAdded.add(dim.getName());
				  }
				  dims.add(dimensionMap.get(dim.getName()));
			  }
			  Variable vertexVar =  ncWriter.addVariable(vertexGroup, var.getShortName(), var.getDataType(), dims);
		
			  vertexArrayList.add(vertexVar);
			  
			//  for(Attribute att : attsList){
			//	  ncWriter.addGroupAttribute(vertexGroup, att);
			 // }
		  }		 		 
		  vertexVarMap.put(vertex, vertexArrayList);

	  }
//
//	  
//	  

		try {
			ncWriter.create();
		

		
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	
		
	  
  }
  
	 

  
  
  
  
  
  
  
  
  
  
  
  
  
  

  
  
  
  
  
  public Map<Integer, Map<Variable, Variable>>  writeDimensions(NetcdfFileWriter ncWriter, 
		  								Map<Integer, List<Variable>> varList) throws IOException{

	  List<Attribute> attsList = new ArrayList<Attribute>();

	  String gdsLoc = "";
	  // gather all the global attributes from each of the GridDatasets
	  for (GridDataset gds : gdsList) {
		  for (Attribute a : gds.getGlobalAttributes()) {
			  if (!attsList.contains(a)) {
				  attsList.add(a);
				//  System.out.println("added attribute " + a.getName());
			  }
		  }
		  gdsLoc += gds.getLocationURI() + " ; ";
	  }

	  Group rootGroup = ncWriter.addGroup(null, "");
	  //for(Attribute att : attsList){
	  //if(att.getDataType().isNumeric())
	  //rootGroup.addAttribute(att.getShortName(), geVarType(att.getDataType()), att.getNumericValue());
	  //else
	  //rootGroup.addAttribute(att.getShortName(), geVarType(att.getDataType()), att.getStringValue());
	  //}
	  ncWriter.addGroupAttribute(rootGroup, new Attribute("History",  
			  "Translated by ASA (NetcdfGridWriter.java) using the Netcdf-Java CDM; " + "Original Dataset = "
					  + gdsLoc + "; Translation date = " + new Date() + ";"));

	  // create all of the dimensions
	  List<String> dimensionsAdded = new ArrayList<String>();
	  Map<String, Dimension> dimensionMap = new HashMap<String, Dimension>();
	  Map<Integer,  Map<Variable, Variable>> vertexVarMap = new HashMap<Integer, Map<Variable, Variable>>();
	  boolean multGroups = varList.keySet().size()  > 1;
	  for(Integer vertexId : varList.keySet()){
		  Map<Variable, Variable> vertexVarList = new HashMap<Variable, Variable>();
		  //if(varList.size() > 1){
		  //// create new group 
		  //currGroup = rootGroup.addGroup("Vertex_"+vertexId);
		  //currGroup.addAttribute("description", NhVariable.TP_STRING_VAR, "Data for vertex " + vertexId+ " of the track line");
		  //}
		  dimensionsAdded.clear();
		  for(	 Variable var : varList.get(vertexId)){
			  List<Dimension> dims = new ArrayList<Dimension>();
			  for(Dimension dim : var.getDimensions()){
				  if(!dimensionsAdded.contains(dim.getShortName())) {
					  Dimension addedDim = null;
					  String dimName = dim.getShortName();
					  if(multGroups)
						  dimName += vertexId;
					  addedDim = ncWriter.addDimension(rootGroup, dimName, dim.getLength());
					  dimensionMap.put(dim.getShortName(), addedDim);
					  dimensionsAdded.add(dim.getShortName());
				  }
				  dims.add(dimensionMap.get(dim.getShortName()));
			  }
			  String varName = var.getFullName();
			  if(multGroups)
				  varName += vertexId; 
			  Variable vertexVar =  ncWriter.addVariable(rootGroup, varName, var.getDataType(), dims);
			  //if(vertexVar.getRank() )
			  for(Attribute a : var.getAttributes()){
				  ncWriter.addVariableAttribute(vertexVar, a);

			  }
			  vertexVarList.put(vertexVar, var);


		  }	
		  vertexVarMap.put(vertexId, vertexVarList);
	  }
	  for(Attribute att : attsList){
		  ncWriter.addGroupAttribute(rootGroup, att);
	  }
	  //
	  //
	  //

	  ncWriter.create();

	  return vertexVarMap;

}

  
  
  
  
  
  
  
  
  

  
  public Boolean[][] getIndiciestoInclude( List<LatLonPointImpl> polygonVertices,  GridDatatype grid) {
	  if(polygonVertices == null || grid == null)
		  return null;
	  GridCoordSystem coord = grid.getCoordinateSystem();
	  Path2D.Double polygon = PolygonUtils.getPolygonFromVertices(polygonVertices);
	  
	  Dimension yDim = grid.getYDimension();
	  Dimension xDim = grid.getXDimension();
	  Boolean[][] includeYLoc = new Boolean[xDim.getLength()][yDim.getLength()];
	  for(int y = 0; y < yDim.getLength(); y++){
		  for(int x = 0; x < xDim.getLength(); x++){
			  LatLonPoint pt = coord.getLatLon(x, y);
				  includeYLoc[x][y] = polygon.contains(pt.getLongitude(), pt.getLatitude());
		
		  }
	  }
	  return includeYLoc;
  }
  
  
  public void writerFile(Map<Integer, List<Variable>> varList, String outLoc, 
		  				 boolean trimToPolygon,
		  				 Map<Integer, Boolean[][]>includeLoc,
		  				 Map<Integer, String> xDimNameMap,
		  				 Map<Integer, String> yDimNameMap,
		  				 Map<Integer, String> zDimNameMap) throws InvalidRangeException{
	  try {
//		  NhFileWriter ncWriter = new NhFileWriter(outLoc, NhFileWriter.OPT_OVERWRITE,
//				  0,0,            // nhBugs, hdfBugs
//				  0,            // utcModTime: use current time.
//				  null,                  // logDir
//				  null);
		  NetcdfFileWriter ncWriter = NetcdfFileWriter.createNew(NetcdfFileWriter.Version.netcdf3, outLoc, null);

		  // Go through each vertex and combine the necessary  dimension variables first

		  //  Integer numXDimIncluded = 0;
		  //  Integer numYDimIncluded = 0;

	
		  //  boolean[] includeYLoc = null;
//		  if(polygonVertices != null){
//			  Path2D.Double polygon = getPolygonFromVertices(polygonVertices);
//			  includeLoc = getIndiciestoInclude(polygon, grid);
//			  //  includeYLoc = getYIndiciestoInclude(polygon, grid);
//		  }
		 Map<Integer, Map<Variable, Variable>> vertexVarMap =  writeDimensions(ncWriter, varList);
		
		  pcs.firePropertyChange("note", null, "Writing File...");

		  Thread.sleep(250);
		 
		  for(Integer vertexID : vertexVarMap.keySet()){
			  Boolean[][]vertexIncludeLoc = includeLoc.get(vertexID);
			//  for(NhVariable nhVar : vertexVarMap.get(vertexID).keySet()){
			  
			  for(Variable var : vertexVarMap.get(vertexID).keySet()){
				 // Variable var = vertexVarMap.get(vertexID).get(nhVar);
				  Variable origVar =  vertexVarMap.get(vertexID).get(var);
				  Array outputArray = origVar.read();

				  if(!trimToPolygon || var.getRank() < 3)
					  ncWriter.write(var, outputArray);
					  //  nhVar.writeData(null,  outputArray, true);


				  else {
					  // Filter out the points that are not within the polygon
					  double fillVal = 999.9;
					  Attribute fillValue = var.findAttribute(FILL_VAL_ATTRIUBTE);
					  if(fillValue != null){
						  fillVal =  fillValue.getNumericValue().doubleValue();
					  }
					  int xDimIndex = -1;
					  int yDimIndex = -1;
					  int zDimIndex = -1;
					  int kDimIndex = -1;
					  int currIndex = 0;
					  List<Dimension> varDimList = var.getDimensions();
					
					  for(Dimension d:varDimList){
						  if(d.getFullName().equals(xDimNameMap.get(vertexID))){
							  xDimIndex = currIndex;
						  }
						  else if(d.getFullName().equals(yDimNameMap.get(vertexID))){
							  yDimIndex = currIndex;
						  }
						  else if(d.getFullName().equals(zDimNameMap.get(vertexID))){
							  zDimIndex = currIndex;
						  }
						  else
							  kDimIndex = currIndex;
						  currIndex++;
					  }
					  
					  Index varIndex = outputArray.getIndex();
					  int zIndexMax = 1;
					  if(zDimIndex != -1) {
						  // Grid has a vertical dimension
						  zIndexMax = varDimList.get(zDimIndex).getLength();
					  }
						  
					  for(int zIndex = 0; zIndex < zIndexMax; zIndex++){
						  for(int xIndex = 0; xIndex< varDimList.get(xDimIndex).getLength(); xIndex++){
							  for(int yIndex = 0; yIndex < varDimList.get(yDimIndex).getLength(); yIndex++){
								  for(int k = 0; k < varDimList.get(kDimIndex).getLength(); k++){

									  if(!vertexIncludeLoc[xIndex][yIndex]){
										 // System.out.println(xIndex+" " + xIndex+" "+yIndex);
										  int [] setIndices = new int[var.getRank()];
										 // System.out.println("rank " + setIndices.length);
										  
										  if(xDimIndex >= 0 && xDimIndex < setIndices.length)
										  	setIndices[xDimIndex] = xIndex;
										  if(yDimIndex >= 0 && yDimIndex < setIndices.length)
											  setIndices[yDimIndex] = yIndex;
										  if(kDimIndex >= 0 && kDimIndex < setIndices.length)
											  setIndices[kDimIndex] = k;
										  if(zDimIndex >= 0 && zDimIndex < setIndices.length)
											  setIndices[zDimIndex] = zIndex;
										//  System.out.println("xDim " + xDimIndex + " yDim "+ yDimIndex+ " zDim " +
										//	  zDimIndex + " k " + kDimIndex);	
										  varIndex.set(setIndices);
										
										  outputArray.setDouble(varIndex, fillVal);
									  }
								
								  }
							  }
						  }
					  }
					//  nhVar.writeData(null,  outputArray, true);
					  ncWriter.write(var, outputArray);
				  }

			  }
		  }

		  //
		  ncWriter.close();



	  } catch (InterruptedException e) {
		  // TODO Auto-generated catch block
		  e.printStackTrace();
	  }
	  // write the ncfile to disk
	  catch (IOException e) {
		  // TODO Auto-generated catch block
		  e.printStackTrace();
	  }

  }
}
