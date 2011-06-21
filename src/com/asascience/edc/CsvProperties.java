/*
 * CsvProperties.java
 *
 * Created on September 17, 2007, 4:13 PM
 *
 * Applied Science Associates, Inc.
 * Copyright 2007.  All rights reserved.
 */
package com.asascience.edc;

import com.asascience.edc.utils.FileSaveUtils;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.output.XMLOutputter;


/**
 * 
 * @author Kyle
 */
public class CsvProperties {
  
  private ArrayList<String> timesteps;
  private ArrayList<String> variableHeaders;
  private String latHeader;
  private String lonHeader;
  private String idHeader;
  private String timeHeader;
  private String path;
  private String suffix;
  private Document outDoc;

  public CsvProperties() {
    timesteps = new ArrayList<String>();
    variableHeaders = new ArrayList<String>();
  }

  public void createXml() {
    outDoc = new Document(new Element("asa_csvdata"));
    Element root = outDoc.getRootElement();
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
          System.err.println("CsvProperties.writeFile: Could not delete file \"" + xmlFile.getAbsolutePath()
                  + "\"");
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

      root.getChild("output_loc").setAttribute("filepath", FileSaveUtils.getFilePathNoSuffix(path) + "." + suffix);

      try {
        out.output(outDoc, new FileOutputStream(xmlFile));
      } catch (IOException ex) {
        ex.printStackTrace();
      }
    } catch (Exception ex) {
      ex.printStackTrace();
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

  public void setTimeHeader(String time) {
    this.timeHeader = time;
  }
  
  public void setTimesteps(ArrayList<String> timesteps) {
    this.timesteps = timesteps;
  }

  public void setVariableHeaders(ArrayList<String> variables) {
    this.variableHeaders = variables;
  }
}
