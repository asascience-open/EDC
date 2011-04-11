/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * CustomFileFilter.java
 *
 * Created on Aug 22, 2008, 4:36:00 PM
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
public class CustomFileFilter extends FileFilter {

  private String[] extensions;
  private String description;

  /** Creates a new instance of GeoTiffFileFilter */
  public CustomFileFilter(String[] extensions, String description) {
    this.extensions = extensions;
    this.description = description;
  }

  public boolean accept(File f) {
    if (f.isDirectory()) {
      return true;
    }
    String ext = Utils.getExtension(f);
    if (ext != null) {
      for (String s : extensions) {
        if (s.replace(".", "").equalsIgnoreCase(ext)) {
          return true;
        }
      }
    }
    return false;
  }

  public String getDescription() {
    return description;
  }
}
