/*
 * NcProperties.java
 *
 * Created on September 17, 2007, 4:13 PM
 *
 * Applied Science Associates, Inc.
 * Copyright 2007.  All rights reserved.
 */
package com.asascience.edc.nc.io;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.output.XMLOutputter;

/**
 * 
 * @author CBM
 */
public class NcProperties {

  private Document outDoc;
  private String outPath = "";
  private NcGridObjectProperties[] gridObjectProperties;
  
  private int surfLayer = 1;
  private Vector dims;
  private boolean vectorType = false;
  private boolean cancelTask;
  private static Logger logger = Logger.getLogger(NcProperties.class);
  private static Logger guiLogger = Logger.getLogger("com.asascience.log." + NcProperties.class.getName());
  private String xmlPath;
  private final String GRID_DATA = "grid_data";
  private final String NC_INFO = "nc_info";
  private final String TIME_INFO = "timeinfo";
  private final String DIMENSIONS = "dimensions";
  private final String VARIABLES = "variables";
  /**
   * Creates a new instance of NcProperties
   */
  public NcProperties(int gridObjects) {
	  initGridProperties(gridObjects, null);

  }

  public NcProperties(String ncpath, int gridObjects) {
	  initGridProperties(gridObjects, ncpath);
      xmlPath = ncpath.replace(".nc", ".xml");

  }

  private void initGridProperties(int gridObjects, String ncpath){
	  gridObjectProperties = new NcGridObjectProperties[gridObjects];
	  for(int i = 0; i < gridObjectProperties.length; i++){
		  gridObjectProperties[i] = new NcGridObjectProperties();
		  gridObjectProperties[i].setNcPath(ncpath);
	  }
  }
  public void createPointerXml(String xmlPath, String sysPath) {
    Document d = new Document(new Element("asa_ncdata"));
    Element root = d.getRootElement();
    root.addContent(new Element("nc_info"));

    root.getChild("nc_info").setAttribute("last", xmlPath.replace(".nc", ".xml"));
    XMLOutputter out = new XMLOutputter();

    sysPath = (sysPath.endsWith(File.separator) ? sysPath : sysPath + File.separator);

    File outFile = new File(sysPath + "NcPointer.xml");

    // //TODO: FIXED: remove this in favor of the above line - this is to
    // maintain compatibility with
    // //Kelly's last build.
    // File outFile = new File(sysPath);
    // outFile = new File(outFile.getParent() + File.separator +
    // "NcPointer.xml");

    try {
      out.output(d, new FileOutputStream(outFile));
    } catch (FileNotFoundException ex) {
      logger.error("FileNotFoundException", ex);
      guiLogger.error("FileNotFoundException", ex);
    } catch (IOException ex) {
      logger.error("IOException", ex);
      guiLogger.error("IOException", ex);
    }
  }

  // no longer used
  public void createPointerXml(String xmlPath) {
    File xmlFile = new File(xmlPath);
    Document d = new Document(new Element("asa_ncdata"));
    Element root = d.getRootElement();
    root.addContent(new Element("nc_info"));

    String lastFile = xmlFile.getAbsolutePath();
    if (xmlFile.getName().equals("edcstore.xml")) {
      lastFile = "null";
    }

    root.getChild("nc_info").setAttribute("last", lastFile);
    // root.getChild("nc_info").setAttribute("last",
    // xmlFile.getAbsolutePath());
    XMLOutputter out = new XMLOutputter();

    String outTo = outTo = xmlFile.getParent();

    // if(lastFile.equals("null")) outTo = xmlFile.getParent();

    File outFile = new File(outTo + File.separator + "NcPointer.xml");
    try {
      out.output(d, new FileOutputStream(outFile));
    } catch (FileNotFoundException ex) {
      logger.error("Exception", ex);
      guiLogger.error("Exception", ex);
    } catch (IOException ex) {
      logger.error("Exception", ex);
      guiLogger.error("Exception", ex);
    }
  }

  public void createXml() {
    outDoc = new Document(new Element("asa_ncdata"));
    Element root = outDoc.getRootElement();
    root.addContent(new Element("version"));
    for(int gridI = 0; gridI < this.gridObjectProperties.length; gridI++){
    	Element gridData = new Element(GRID_DATA);
    	gridData.addContent(new Element(NC_INFO));
    	gridData.addContent(new Element(TIME_INFO));
    	gridData.addContent(new Element(DIMENSIONS));
    	gridData.addContent(new Element(VARIABLES));
        root.addContent(gridData);
      
    }
    root.addContent(new Element("output_loc"));
    root.addContent(new Element("vectortype"));
  }

  public void writeFile() {
    try {

      XMLOutputter out = new XMLOutputter();
      File xmlFile = new File(xmlPath);
      if (xmlFile.exists()) {
        if (!xmlFile.delete()) {
          logger.warn("NcProperties.writeFile: Could not delete file \"" + xmlFile.getAbsolutePath() + "\"");
          guiLogger.warn("NcProperties.writeFile: Could not delete file \"" + xmlFile.getAbsolutePath() + "\"");
        }
      }
      createXml();
      Element root = outDoc.getRootElement();
      root.getChild("version").setText("7.0");
      List<Element> grids = root.getChildren(GRID_DATA);

      int gridI = 0;
      for(Element gridData : grids){
    	  Element ncInfo = gridData.getChild(NC_INFO);
    	  NcGridObjectProperties gridProps = this.gridObjectProperties[gridI];
    	  ncInfo.setAttribute("filepath", gridProps.ncPath);
    	  ncInfo.setAttribute("type", gridProps.type.toString());
    	  ncInfo.setAttribute("starttime", gridProps.startTime);
    	  ncInfo.setAttribute("timeinterval", gridProps.timeInterval);
    	  ncInfo.setAttribute("timeunit", gridProps.timeUnits);
    	  ncInfo.setAttribute("projection", gridProps.projection);
    	  
    	  Element timeInfo = gridData.getChild(TIME_INFO);
    	  for (String s : gridProps.times) {
    		  timeInfo.addContent(new Element("timestep").setText(s));
    	  }
    	  Element dimElem = gridData.getChild("dimensions");
    	  dimElem.setAttribute("band_dim", gridProps.bandDim);
    	  dimElem.setAttribute("trim_dim", gridProps.trimByDim);
    	  dimElem.setAttribute("trim_val", gridProps.trimByValue);
    	  dimElem.addContent(new Element("time").setText(gridProps.time)).addContent(
    			  			 new Element("x").setText(gridProps.xCoord)).addContent(
    			  			 new Element("y").setText(gridProps.yCoord)).addContent(
    			  			 new Element("z").setAttribute("surfaceLayer", 
    			  					 String.valueOf(surfLayer)).setText(gridProps.zCoord));
    	  // .addContent(new Element("trimDim").setText(trimByDim))
    	  // .addContent(new Element("trimVal").setText(trimByValue));
    	  Element varElem = gridData.getChild(VARIABLES);
    	  varElem.setAttribute("u_var", gridProps.uVar);
    	  varElem.setAttribute("v_var", gridProps.vVar);
    	  varElem.setAttribute("t_var", gridProps.tVar);

    	  // write other variables
    	  int num = 1;
    	  for (Iterator i = gridProps.vars.iterator(); i.hasNext();) {
    		  gridData.getChild("variables").addContent(new Element("variable").setText((String) i.next()));
    		  num++;
    	  }
    	  // write time variable
    	  gridData.getChild("variables").addContent(new Element("variableT").setText(gridProps.getTVar()));
    	  gridI++;
      }
      root.getChild("output_loc").setAttribute("filepath", outPath);

      root.getChild("vectortype").setText(String.valueOf(vectorType));

      // createPointerXml(xmlFile);

      try {
        out.output(outDoc, new FileOutputStream(xmlFile));
      } catch (IOException ex) {
        logger.error("IOException", ex);
        guiLogger.error("IOException", ex);
      }
    } catch (Exception ex) {
      logger.error("NP:writeFile:", ex);
      guiLogger.error("NP:writeFile:", ex);
    }

  }

  public Document getOutDoc() {
    return outDoc;
  }

  public void setOutDoc(Document outDoc) {
    this.outDoc = outDoc;
  }

  public String getOutPath() {
    return outPath;
  }

  public void setOutPath(String outPath) {
    this.outPath = outPath;
  }

 
  public Vector getDims() {
    return dims;
  }

  public void setDims(Vector dims) {
    this.dims = dims;
  }



  public NcGridObjectProperties getGridProperties(int propIndex){
	  NcGridObjectProperties props = null;
	  if(propIndex >= 0 && propIndex < this.gridObjectProperties.length)
		  props = this.gridObjectProperties[propIndex];
	  return props;
				  
  }

  public boolean isCancelTask() {
    return cancelTask;
  }

  public void setCancelTask(boolean cancelTask) {
    this.cancelTask = cancelTask;
  }

 
  public boolean isVectorType() {
    return vectorType;
  }

  public void setVectorType(boolean vectorType) {
    this.vectorType = vectorType;
  }

  public int getSurfLayer() {
    return surfLayer;
  }

  public void setSurfLayer(int surfLayer) {
    this.surfLayer = surfLayer;
  }


}
