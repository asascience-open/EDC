/*
 * WindsFileFilter.java
 *
 * Created on October 23, 2007, 10:22 AM
 *
 * Applied Science Associates, Inc.
 * Copyright 2007.  All rights reserved.
 */
package com.asascience.utilities.filefilter;

import java.io.File;

import javax.swing.filechooser.FileFilter;

import com.asascience.utilities.Utils;

/**
 * 
 * @author CBM
 */
public class WindsFileFilter extends FileFilter {

  /**
   * Creates a new instance of WindsFileFilter
   */
  public WindsFileFilter() {
  }

  public boolean accept(File f) {
    if (f.isDirectory()) {
      return true;
    }

    String ext = Utils.getExtension(f);
    if (ext != null) {
      if (ext.equalsIgnoreCase("nc") || ext.equalsIgnoreCase("wix") || ext.equalsIgnoreCase("bdp")) {
        return true;
      }
    }
    return false;
  }

  public String getDescription() {
    return "Supported Wind Files";
  }
}
