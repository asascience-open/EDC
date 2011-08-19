package com.asascience.edc.sos.requests;

import java.util.ArrayList;
import java.util.List;

/**
 * ResponseFormat.java
 * 
 * @author Kyle Wilcox <kwilcox@asascience.com>
 */
public class ResponseFormat {

  private String name;
  private String value;
  private String className;
  private String fileSuffix;
  private boolean postProcess;
  private List<ResponseFormat> childFormats;
  
  public ResponseFormat(String value) {
    this.value = value;
    this.className = "";
    parseChildrenAndClass();
    parseReadableNames();
    parseFileSuffix();
  }

  public ResponseFormat(String value, String name, String theClass, String suffix, boolean post) {
    this.value = value;
    this.name = name;
    this.className = theClass;
    this.fileSuffix = suffix;
    this.postProcess = post;
  }

  private void parseReadableNames() {
    if (value.contains("0.6.1")) {
      name = "IOOS DIF (0.6.1)";
    } else if (value.contains("swe")) {
      name = "SWE 1.0.0";
    } else if (value.contains("kml")) {
      name = "KML";
    } else if (value.contains("csv")) {
      name = "Comma Seperated";
    } else if (value.contains("tab-separated-values")) {
      name = "Tab Seperated";
    } else if (value.contains("om/1.0.0")) {
      name = "OM (1.0.0)";
    } else {
      name = value;
    }
  }

  private void parseChildrenAndClass() {
    this.className = "GenericRequest";
    childFormats = new ArrayList<ResponseFormat>();
    if (value.contains("0.6.1")) {
      childFormats.add(new ResponseFormat(value, "ARC (post-process from DIF)", "DifToArc", "csv", true));
      childFormats.add(new ResponseFormat(value, "NetCDF (post-process from DIF)", "DifToNetCDF", "nc", true));
      childFormats.add(new ResponseFormat(value, "CSV (post-process from DIF)", "DifToCSV", "csv", true));
    } else if (value.contains("swe")) {
      childFormats.add(new ResponseFormat(value, "ARC (post-process from SWE)", "SweToArc", "csv", true));
      childFormats.add(new ResponseFormat(value, "NetCDF (post-process from SWE)", "SweToNetCDF", "nc", true));
      childFormats.add(new ResponseFormat(value, "CSV (post-process from SWE)", "SweToCSV", "csv", true));
    }
  }

  private void parseFileSuffix() {
    fileSuffix = "sos";
    if (value.contains("0.6.1")) {
      fileSuffix = "xml";
    } else if (value.contains("swe")) {
      fileSuffix = "xml";
    } else if (value.contains("kml")) {
      fileSuffix = "kml";
    } else if (value.contains("csv")) {
      fileSuffix = "csv";
    } else if (value.contains("tab-separated-values")) {
      fileSuffix = "tsv";
    } else if (value.contains("om/1.0.0")) {
      fileSuffix = "xml";
    } else {
      fileSuffix = "sos";
    }
  }

  public String getName() {
    return name;
  }

  public String getValue() {
    return value;
  }

  public List<ResponseFormat> getChildFormats() {
    return childFormats;
  }

  public String getFileSuffix() {
    return fileSuffix;
  }

  public String getClassName() {
    return className;
  }

  public boolean isPostProcess() {
    return postProcess;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null) return false;
    if (!this.getClass().equals(obj.getClass())) return false;

    if (this.hashCode() == obj.hashCode()) {
      return true;
    } else {
      return false;
    }
  }

  @Override
  public int hashCode() {
    int hash = 7;
    hash = 41 * hash + (this.name != null ? this.name.hashCode() : 0);
    hash = 41 * hash + (this.className != null ? this.className.hashCode() : 0);
    return hash;
  }


}
