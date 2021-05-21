/*
 * CsvProperties.java
 *
 * Created on September 17, 2007, 4:13 PM
 *
 * Applied Science Associates, Inc.
 * Copyright 2007.  All rights reserved.
 */
package com.asascience.edc;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.output.XMLOutputter;

import com.asascience.edc.utils.FileSaveUtils;

/**
 * 
 * @author Kyle
 */
public class CsvProperties {

  private ArrayList<String> timesteps;
  private ArrayList<String> variableHeaders;
  private String latHeader;
  private String lonHeader;
  private String zHeader;
  private String idHeader;
  private String timeHeader;
  private String path;
  private String cdmFeatureType;
  private String suffix;
  private Document outDoc;
  private static Logger logger = Logger.getLogger(Configuration.class);

  public CsvProperties() {
    timesteps = new ArrayList<String>();
    variableHeaders = new ArrayList<String>();
  }

  public void createXml() {
    outDoc = new Document(new Element("asa_csvdata"));
    Element root = outDoc.getRootElement();
    root.addContent(new Element("cdm_feature_type"));
    root.addContent(new Element("z_header"));
    root.addContent(new Element("lat_header"));
    root.addContent(new Element("lon_header"));
    root.addContent(new Element("id_header"));
    root.addContent(new Element("time_header"));
    root.addContent(new Element("time_start"));
    root.addContent(new Element("time_end"));
    root.addContent(new Element("variables"));
    root.addContent(new Element("timesteps"));
    root.addContent(new Element("output_loc"));
  }

  public void writeFile() {
    try {

      XMLOutputter out = new XMLOutputter();
      File xmlFile = new File(FileSaveUtils.getFilePathNoSuffix(path) + ".xml");
      if (xmlFile.exists()) {
        if (!xmlFile.delete()) {
          logger.warn("CsvProperties.writeFile: Could not delete file \"" + xmlFile.getAbsolutePath() + "\"");
        }
      }
      createXml();
      Element root = outDoc.getRootElement();

      // Populate template
      for (String s : timesteps) {
        root.getChild("timesteps").addContent(new Element("timestep").setText(s));
      }
      if (!timesteps.isEmpty()) {
        root.getChild("time_start").addContent(new Element("value").setText(timesteps.get(0)));
        root.getChild("time_end").addContent(new Element("value").setText(timesteps.get(timesteps.size() - 1)));
      }

      for (String s : variableHeaders) {
        root.getChild("variables").addContent(new Element("variable").setText(s));
      }

      root.getChild("lat_header").addContent(new Element("value").setText(latHeader));
      root.getChild("lon_header").addContent(new Element("value").setText(lonHeader));
      root.getChild("id_header").addContent(new Element("value").setText(idHeader));
      root.getChild("time_header").addContent(new Element("value").setText(timeHeader));
      root.getChild("cdm_feature_type").addContent(new Element("value").setText(cdmFeatureType));
      root.getChild("z_header").addContent(new Element("value").setText(zHeader));

      root.getChild("output_loc").setAttribute("filepath", FileSaveUtils.getFilePathNoSuffix(path) + "." + suffix);

      try {
        FileOutputStream fos = new FileOutputStream(xmlFile);
        out.output(outDoc, fos);
        fos.close();
      } catch (IOException ex) {
        logger.error("IOException", ex);
      }
    } catch (Exception ex) {
      logger.error("Exception", ex);
    }
  }

  public void setPath(String path) {
    this.path = path;
  }

  public void setSuffix(String suffix) {
    this.suffix = suffix;
  }

  public void setIdHeader(String id) {
    this.idHeader = id;
  }

  public void setLatHeader(String lat) {
    this.latHeader = lat;
  }

  public void setLonHeader(String lon) {
    this.lonHeader = lon;
  }

  public void setZHeader(String z) {
    this.zHeader = z;
  }
  
  public void setTimeHeader(String time) {
    this.timeHeader = time;
  }

  public void setTimesteps(ArrayList<String> timesteps) {
    this.timesteps = timesteps;
  }

  public void setVariableHeaders(ArrayList<String> variables) {
    this.variableHeaders = variables;
  }
  
  public void setCdmFeatureType(String type) {
    this.cdmFeatureType = type;
  }
  
  public ArrayList<String> getHeaders() {
    ArrayList<String> headers = new ArrayList<String>();
    headers.addAll(variableHeaders);
    headers.add(latHeader);
    headers.add(lonHeader);
    headers.add(idHeader);
    headers.add(zHeader);
    headers.add(timeHeader);
    return headers;
  }
  
}
