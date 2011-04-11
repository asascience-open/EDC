/*
 * DetermineNcType.java
 *
 * Created on August 2, 2007, 12:57 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package com.asascience.utilities.io;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import ucar.nc2.Attribute;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;
import ucar.nc2.dt.grid.GridDataset;

/**
 * 
 * @author cmueller
 */
public class DetermineNcType {

  /**
   * Creates a new instance of DetermineNcType
   */
  public DetermineNcType() {
  }

  public static NcFileType determineFileType(String file) {
    NetcdfFile ncfile = null;
    try {
      ncfile = NetcdfFile.open(file);
      if (isSWAFS(ncfile)) {
        return NcFileType.SWAFS;
      }
      if (isNCOM(ncfile)) {
        return NcFileType.NCOM;
      }
      if (isNcell(ncfile)) {
        if (isAsset(ncfile)) {
          return NcFileType.ASSET;
        }
        return NcFileType.NCELL;
      }
      if (isGenericGrid(ncfile)) {
        return NcFileType.GENERIC_GRID;
      }
    } catch (IOException ex) {
      Logger.getLogger(DetermineNcType.class.getName()).log(Level.SEVERE, null, ex);
    } finally {
      if (ncfile != null) {
        try {
          ncfile.close();
        } catch (IOException ex) {
          Logger.getLogger(DetermineNcType.class.getName()).log(Level.SEVERE, null, ex);
        }
      }
    }

    return NcFileType.UNKNOWN;
  }

  private static ArrayList<String> getVarNames(NetcdfFile ncfile) {
    List<Variable> vars = ncfile.getVariables();
    ArrayList<String> varNames = new ArrayList<String>();
    Variable v = null;
    Iterator<Variable> it = vars.iterator();
    while (it.hasNext()) {
      v = (Variable) it.next();
      varNames.add(v.getName());
    }
    return varNames;
  }

  public static boolean isSWAFS(String fileLoc) {
    try {
      NetcdfFile ncfile = NetcdfFile.open(fileLoc);
      return isSWAFS(ncfile);
    } catch (IOException ex) {
      Logger.getLogger(DetermineNcType.class.getName()).log(Level.SEVERE, null, ex);
    }
    return false;
  }

  public static boolean isSWAFS(NetcdfFile ncfile) {
    Attribute a = ncfile.findGlobalAttributeIgnoreCase("generating_model");
    if (a != null) {
      if (a.getStringValue().toLowerCase().contains("swafs")) {
        return true;
      }
    }
    ArrayList varNames = getVarNames(ncfile);
    /** Do not check depth - the file may not have it... */
    // if(!varNames.contains("depth")){
    // return false;
    // }
    if (!varNames.contains("tau")) {
      return false;
    }
    if (!varNames.contains("time")) {
      return false;
    }
    if (!varNames.contains("lon")) {
      return false;
    }
    if (!varNames.contains("lat")) {
      return false;
    }
    if (!varNames.contains("water_v")) {
      return false;
    }
    if (!varNames.contains("water_u")) {
      return false;
    }
    if (!varNames.contains("water_temp")) {
      return false;
    }
    if (!varNames.contains("salinity")) {
      return false;
    }
    if (!varNames.contains("botdep")) {
      return false;// the distinguishing feature of SWAFS
    }
    if (!varNames.contains("surf_el")) {
      return false; // added assurance that it is indeed a SWAFS file...
    }

    // TODO: this was failing on the new Swafs files from Matt - they don't
    // have this attribute.
    // Attribute a =
    // ncfile.findGlobalAttributeIgnoreCase("generating_model");
    // if(a == null || !a.getStringValue().contains("SWAFS")){
    // return false;
    // }
    return true;
  }

  public static boolean isNCOM(String fileLoc) {
    try {
      NetcdfFile ncfile = NetcdfFile.open(fileLoc);
      return isNCOM(ncfile);
    } catch (IOException ex) {
      Logger.getLogger(DetermineNcType.class.getName()).log(Level.SEVERE, null, ex);
    }
    return false;
  }

  public static boolean isNCOM(NetcdfFile ncfile) {
    Attribute a = ncfile.findGlobalAttributeIgnoreCase("generating_model");
    ArrayList varNames = getVarNames(ncfile);
    if (a != null) {
      if (a.getStringValue().toLowerCase().contains("ncom")) {
        // Only return true if the NCOM datasaet has vectors (u and v)
        ArrayList checkNames = new ArrayList(2);
        checkNames.add("water_u");
        checkNames.add("water_v");
        if (varNames.containsAll(checkNames)) {
          return true;
        }
      }
    }
    /** Do not check depth - the file may not have it... */
    // if(!varNames.contains("depth")){
    // return false;
    // }
    if (!varNames.contains("lat")) {
      return false;
    }
    if (!varNames.contains("lon")) {
      return false;
    }
    if (!varNames.contains("salinity")) {
      return false;
    }
    if (!varNames.contains("surf_el")) {
      return false;
    }
    if (!varNames.contains("tau")) {
      return false;
    }
    if (!varNames.contains("time")) {
      return false;
    }
    if (!varNames.contains("water_temp")) {
      return false;
    }
    if (!varNames.contains("water_u")) {
      return false;
    }
    if (!varNames.contains("water_v")) {
      return false; // added assurance that it is indeed an NCOM file...
    }

    /** Do not check depth - the file may not have it... */
    // Attribute a =
    // ncfile.findGlobalAttributeIgnoreCase("generating_model");
    // if(!a.getStringValue().contains("NCOM")){
    // return false;
    // }
    return true;
  }

  public static boolean isNcell(NetcdfFile ncfile) {
    ArrayList varNames = getVarNames(ncfile);
    if (!varNames.contains("ncells")) {
      return false;
    }
    if (!varNames.contains("lat")) {
      return false;
    }
    if (!varNames.contains("lon")) {
      return false;
    }
    if (!varNames.contains("time")) {
      return false;
      // if(!varNames.contains("U") || !varNames.contains("wind_u"))
      // return false;
      // if(!varNames.contains("V") || !varNames.contains("wind_v"))
      // return false;
    }
    return true;
  }

  public static boolean isAsset(NetcdfFile ncfile) {
    ArrayList<String> varNames = getVarNames(ncfile);
    if (!varNames.contains("asset")) {
      return false;
    }
    if (!varNames.contains("GoNoGo")) {
      return false;
    }
    return true;
  }

  public static boolean isGenericGrid(NetcdfFile ncfile) {
    try {
      GridDataset gds = GridDataset.open(ncfile.getLocation());
      if (gds == null) {
        return false;
      }
      return true;
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    return false;
  }
}
