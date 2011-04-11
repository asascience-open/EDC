/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * ShapefileFileFilter.java
 *
 * Created on Jul 7, 2008, 12:51:01 PM
 *
 */
package com.asascience.utilities.filefilter;

import java.io.File;

import javax.swing.filechooser.FileFilter;

import com.asascience.utilities.Utils;

/**
 * 
 * @author cmueller_mac
 */
public class ShapefileFileFilter extends FileFilter {

  /** Creates a new instance of GeoTiffFileFilter */
  public ShapefileFileFilter() {
  }

  public boolean accept(File f) {
    if (f.isDirectory()) {
      return true;
    }

    String ext = Utils.getExtension(f);
    if (ext != null) {
      if (ext.equalsIgnoreCase("shp")) {
        return true;
      }
    }
    return false;
  }

  public String getDescription() {
    return "Shapefiles";
  }
}
