/*
 * Applied Science Associates, Inc.
 * Copyright 2007. All rights reserved.
 */

/*
 * HarmonicsFileFilter.java
 *
 * Created on Apr 23, 2008, 4:04:38 PM
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
public class HarmonicsFileFilter extends FileFilter {

  /** Creates a new instance of HarmonicsFileFilter */
  public HarmonicsFileFilter() {
  }

  public boolean accept(File f) {
    if (f.isDirectory()) {
      return true;
    }

    String ext = Utils.getExtension(f);
    if (ext != null) {
      if (ext.equalsIgnoreCase("thnc")) {
        return true;
      }
    }
    return false;
  }

  public String getDescription() {
    return "Harmonics Files";
  }
}
