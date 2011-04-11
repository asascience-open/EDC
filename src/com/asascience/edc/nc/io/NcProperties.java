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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.output.XMLOutputter;

import com.asascience.edc.ArcType;
import com.asascience.edc.Configuration;

/**
 * 
 * @author CBM
 */
public class NcProperties {

  private Document outDoc;
  private String outPath = "";
  private String ncPath = "";
  private String startTime = "";
  private String timeInterval = "";
  private String timeUnits = "";
  private String time = "";
  private List<String> times = new ArrayList<String>();
  private String xCoord = "";
  private String yCoord = "";
  private String zCoord = "";
  private String trimByValue = "";
  private String trimByDim = "";
  private String bandDim = "";
  private String uVar = "";
  private String vVar = "";
  private String tVar = "";
  private String projection = "";
  private String isZPositive = "";
  private int surfLayer = 1;
  private ArcType type;
  private Vector dims;
  private List<String> vars;
  private boolean vectorType = false;
  private boolean cancelTask;

  /**
   * Creates a new instance of NcProperties
   */
  public NcProperties() {
  }

  public NcProperties(String ncpath) {
    ncPath = ncpath;
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
      ex.printStackTrace();
    } catch (IOException ex) {
      ex.printStackTrace();
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
      // System.err.println(xmlFile.getName());
      lastFile = "null";
    }

    // System.err.println("lastFile="+lastFile);
    root.getChild("nc_info").setAttribute("last", lastFile);
    // root.getChild("nc_info").setAttribute("last",
    // xmlFile.getAbsolutePath());
    XMLOutputter out = new XMLOutputter();

    String outTo = outTo = xmlFile.getParent();

    // if(lastFile.equals("null")) outTo = xmlFile.getParent();

    File outFile = new File(outTo + File.separator + "NcPointer.xml");
    // File outFile = new File("ncpointer.xml");

    // File outFile = new File(xmlFile.getParentFile().getParent() +
    // File.separator + "NcPointer.xml");
    // System.err.println(outFile.getAbsolutePath());
    // String outpath = ncPath.substring(0,
    // ncPath.lastIndexOf(File.separator));
    // outpath = outpath.substring(0, outpath.lastIndexOf(File.separator));
    // File outFile = new File(outpath + File.separator + "NcPointer.xml");
    // System.err.println("xml pointer: " + outpath);
    try {
      out.output(d, new FileOutputStream(outFile));
    } catch (FileNotFoundException ex) {
      ex.printStackTrace();
    } catch (IOException ex) {
      ex.printStackTrace();
    }
  }

  public void createXml() {
    outDoc = new Document(new Element("asa_ncdata"));
    Element root = outDoc.getRootElement();
    root.addContent(new Element("version"));
    root.addContent(new Element("nc_info"));
    root.addContent(new Element("timeinfo"));
    root.addContent(new Element("dimensions"));
    root.addContent(new Element("variables"));
    root.addContent(new Element("output_loc"));
    root.addContent(new Element("vectortype"));
  }

  public void writeFile() {
    try {

      XMLOutputter out = new XMLOutputter();
      String xmlPath = ncPath.replace(".nc", ".xml");
      // System.err.println(xmlPath + "\n"+ ncPath);
      File xmlFile = new File(xmlPath);
      if (xmlFile.exists()) {
        if (!xmlFile.delete()) {
          System.err.println("NcProperties.writeFile: Could not delete file \"" + xmlFile.getAbsolutePath()
                  + "\"");
        }
      }
      createXml();
      Element root = outDoc.getRootElement();
      root.getChild("version").setText("6.0");
      root.getChild("nc_info").setAttribute("filepath", ncPath).setAttribute("type", type.toString()).setAttribute("starttime", startTime).setAttribute("timeinterval", timeInterval).setAttribute(
              "timeunit", timeUnits).setAttribute("projection", projection);

      for (String s : times) {
        root.getChild("timeinfo").addContent(new Element("timestep").setText(s));
      }

      root.getChild("dimensions").setAttribute("band_dim", bandDim).setAttribute("trim_dim", trimByDim).setAttribute("trim_val", trimByValue).addContent(new Element("time").setText(time)).addContent(
              new Element("x").setText(xCoord)).addContent(new Element("y").setText(yCoord)).addContent(
              new Element("z").setAttribute("surfaceLayer", String.valueOf(surfLayer)).setText(zCoord));
      // .addContent(new Element("trimDim").setText(trimByDim))
      // .addContent(new Element("trimVal").setText(trimByValue));

      root.getChild("variables").setAttribute("u_var", uVar).setAttribute("v_var", vVar).setAttribute("t_var",
              tVar);

      // write other variables
      int num = 1;
      for (Iterator i = vars.iterator(); i.hasNext();) {
        root.getChild("variables").addContent(new Element("variable").setText((String) i.next()));
        num++;
      }
      // write time variable
      root.getChild("variables").addContent(new Element("variableT").setText(getTVar()));

      root.getChild("output_loc").setAttribute("filepath", outPath);

      root.getChild("vectortype").setText(String.valueOf(vectorType));

      // createPointerXml(xmlFile);

      try {
        out.output(outDoc, new FileOutputStream(xmlFile));
      } catch (IOException ex) {
        ex.printStackTrace();
      }
    } catch (Exception ex) {
      System.err.println("NP:writeFile:");
      ex.printStackTrace();
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

  public String getNcPath() {
    return ncPath;
  }

  public void setNcPath(String path) {
    this.ncPath = path;
  }

  public String getStartTime() {
    return startTime;
  }

  public void setStartTime(String startTime) {
    this.startTime = startTime;
  }

  public String getTimeInterval() {
    return timeInterval;
  }

  public void setTimeInterval(String timeInterval) {
    this.timeInterval = timeInterval;
  }

  public String getTimeUnits() {
    return timeUnits;
  }

  public void setTimeUnits(String timeUnits) {
    this.timeUnits = timeUnits;
  }

  public String getTime() {
    return time;
  }

  public void setTime(String time) {
    this.time = time;
  }

  public String getXCoord() {
    return xCoord;
  }

  public void setXCoord(String xCoord) {
    this.xCoord = xCoord;
  }

  public String getYCoord() {
    return yCoord;
  }

  public void setYCoord(String yCoord) {
    this.yCoord = yCoord;
  }

  public String getBandDim() {
    return bandDim;
  }

  public void setBandDim(String bandDim) {
    this.bandDim = bandDim;
  }

  public ArcType getType() {
    return type;
  }

  public void setType(ArcType type) {
    this.type = type;
  }

  public Vector getDims() {
    return dims;
  }

  public void setDims(Vector dims) {
    this.dims = dims;
  }

  public List<String> getVars() {
    return vars;
  }

  public void setVars(List<String> vars) {
    this.vars = vars;
  }

  public String getUVar() {
    return uVar;
  }

  public void setUVar(String uVar) {
    this.uVar = uVar;
  }

  public String getVVar() {
    return vVar;
  }

  public void setVVar(String vVar) {
    this.vVar = vVar;
  }

  public boolean isCancelTask() {
    return cancelTask;
  }

  public void setCancelTask(boolean cancelTask) {
    this.cancelTask = cancelTask;
  }

  public String getTVar() {
    return tVar;
  }

  public void setTVar(String tVar) {
    this.tVar = tVar;
  }

  public String getZCoord() {
    return zCoord;
  }

  public void setZCoord(String zCoord) {
    this.zCoord = zCoord;
  }

  public String getProjection() {
    return projection;
  }

  public void setProjection(String projection) {
    this.projection = projection;
  }

  public String getIsZPositive() {
    return isZPositive;
  }

  public void setIsZPositive(String isZPositive) {
    this.isZPositive = isZPositive;
  }

  public String getTrimByValue() {
    return trimByValue;
  }

  public void setTrimByValue(String trimByValue) {
    this.trimByValue = trimByValue;
  }

  public String getTrimByDim() {
    return trimByDim;
  }

  public void setTrimByDim(String trimByDim) {
    this.trimByDim = trimByDim;
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

  public List<String> getTimes() {
    return times;
  }

  public void setTimes(List<String> times) {
    this.times = times;
  }
}
