/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * XmlFileFilter.java
 *
 * Created on Aug 22, 2008, 4:32:56 PM
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
public class XmlFileFilter extends FileFilter {

  /** Creates a new instance of GeoTiffFileFilter */
  public XmlFileFilter() {
  }

  public boolean accept(File f) {
    if (f.isDirectory()) {
      return true;
    }

    String ext = Utils.getExtension(f);
    if (ext != null) {
      if (ext.equalsIgnoreCase("xml")) {
        return true;
      }
    }
    return false;
  }

  public String getDescription() {
    return "XML Files";
  }
}
