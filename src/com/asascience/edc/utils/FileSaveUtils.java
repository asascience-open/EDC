/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.asascience.edc.utils;

import java.awt.FileDialog;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.swing.JFrame;

/**
 *
 * @author Kyle
 */
public class FileSaveUtils {
  
  public static String getNameFromURL(String url) {
    try {
      URL target = new URL(url);
      return target.getHost().replace('.','_') + File.separator + getNameFromDate();
    } catch (MalformedURLException e) {
      return "";
    }
  }
  
  public static String getNameFromDate() {
    Date dateNow = new Date ();
    SimpleDateFormat formatted = new SimpleDateFormat("yyyy-MM-dd_HHmma");
    return new StringBuilder( formatted.format( dateNow ) ).toString();
  }

  public static File chooseSavePath(JFrame parentFrame, String homeDir, String domain) {
    FileDialog outputPath = new FileDialog(parentFrame, "Create directory and save output files here...", FileDialog.SAVE);
    File containingFolder = new File(homeDir + File.separator + getNameFromURL(domain) + File.separator);
    if (!containingFolder.exists()) {
      containingFolder.mkdirs();
    }
    outputPath.setDirectory(containingFolder.getAbsolutePath());
    outputPath.setFile("Choose Output Directory (ignore this filename)");
    outputPath.setVisible(true);
    
    File newHomeDir = new File(outputPath.getDirectory());
    // Did the user use the new directory we created for them?
    if (!newHomeDir.getAbsolutePath().contains(containingFolder.getAbsolutePath())) {
      if (containingFolder.length() == 0) {
        containingFolder.delete();
        // Create a timestamped directory where the user has chosen
        newHomeDir = new File(outputPath.getDirectory() + File.separator + getNameFromDate());
        newHomeDir.mkdirs();
      }
    }
    return newHomeDir;
  }
  
}
