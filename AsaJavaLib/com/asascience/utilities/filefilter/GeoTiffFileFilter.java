/*
 * Applied Science Associates, Inc.
 * Copyright 2007. All rights reserved.
 */

/*
 * GeoTiffFileFilter.java
 *
 * Created on Mar 25, 2008, 2:14:45 PM
 *
 */
package com.asascience.utilities.filefilter;

import java.io.File;

import javax.swing.filechooser.FileFilter;

import com.asascience.utilities.Utils;

/**
 * 
 * @author CBM <cmueller@asascience.com>
 */
public class GeoTiffFileFilter extends FileFilter {

  /** Creates a new instance of GeoTiffFileFilter */
  public GeoTiffFileFilter() {
  }

  public boolean accept(File f) {
    if (f.isDirectory()) {
      return true;
    }

    String ext = Utils.getExtension(f);
    if (ext != null) {
      if (ext.equalsIgnoreCase("tif")) {
        return true;
      }
    }
    return false;
  }

  public String getDescription() {
    return "GeoTIFF Files";
  }
}
