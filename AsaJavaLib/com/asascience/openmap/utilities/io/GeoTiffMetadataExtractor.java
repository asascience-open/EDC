/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * GeoTiffMetadataExtractor.java
 *
 * Created on May 12, 2008, 1:58:26 PM
 *
 */
package com.asascience.openmap.utilities.io;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DecimalFormat;

import org.geotiff.image.jai.GeoTIFFDirectory;

import com.bbn.openmap.dataAccess.image.geotiff.GeoTIFFFile;
import com.bbn.openmap.util.PropUtils;

/**
 * 
 * @author cmueller_mac
 */
public class GeoTiffMetadataExtractor {

  private String inTiff = null;
  private double[] tfwData = null;

  /**
   * Creates a new instance of GeoTiffMetadataExtractor
   *
   * @param inTiff
   */
  public GeoTiffMetadataExtractor(String inTiff) {
    // if(!new File(inTiff.replaceAll("(?i).tif", ".tfw")).exists()){
    // if(!new File(inTiff.replaceAll("(?i).tif", ".tifw")).exists()){
    if (!new File(inTiff.replace(".tif", ".tfw")).exists()) {
      if (!new File(inTiff.replace(".tif", ".tifw")).exists()) {
        this.inTiff = inTiff;
      } else {
        return;
      }
    } else {
      return;
    }

    if (accessMetadata()) {
      writeTFWFile();
    }
  }

  public GeoTiffMetadataExtractor() {
  }

  public boolean extractTFW(String inTiff) {
    if (!new File(inTiff.replace(".tif", ".tfw")).exists()) {
      if (!new File(inTiff.replace(".tif", ".tifw")).exists()) {
        this.inTiff = inTiff;
      } else {
        return true;
      }
    } else {
      return true;
    }

    if (accessMetadata()) {
      if (writeTFWFile()) {
        return true;
      }
    }
    return false;
  }

  private boolean accessMetadata() {
    try {
      URL fileURL = PropUtils.getResourceOrFileOrURL(inTiff);
      if (fileURL != null) {
        GeoTIFFFile gtfFile = new GeoTIFFFile(fileURL);
        GeoTIFFDirectory gtfd = gtfFile.getGtfDirectory();
        if (gtfd == null) {
          return false;
        }

        double latTie = Double.NaN, lonTie = Double.NaN, latScale = Double.NaN, lonScale = Double.NaN;

        double[] tiePoints = gtfd.getTiepoints();
        if (tiePoints == null) {
          return false;
        }

        if (tiePoints.length == 6) {
          latTie = tiePoints[4];
          lonTie = tiePoints[3];
        }

        double[] scaleMatrix = gtfd.getPixelScale();
        if (scaleMatrix == null) {
          return false;
        }

        if (scaleMatrix.length == 3) {
          latScale = scaleMatrix[1];
          lonScale = scaleMatrix[0];
        }

        if (!Double.isNaN(latTie) & !Double.isNaN(lonTie) & !Double.isNaN(latScale) & !Double.isNaN(lonScale)) {
          tfwData = new double[6];
          tfwData[0] = lonScale;
          tfwData[1] = 0.0d;
          tfwData[2] = 0.0d;
          tfwData[3] = -latScale;
          tfwData[4] = lonTie;
          tfwData[5] = latTie;
          return true;
        }

        // System.out.println("------ Tie Point Values ------");
        // for (int i = 0; i < tiePoints.length; i++) {
        // System.out.println(tiePoints[i]);
        // }
        //
        // double[] scaleMatrix = gtfd.getPixelScale();
        // System.out.println("------ Pixel Scale Values ------");
        // for (int i = 0; i < scaleMatrix.length; i++) {
        // System.out.println(scaleMatrix[i]);
        // }
      }
    } catch (MalformedURLException ex) {
      ex.printStackTrace();
    } catch (IOException ex) {
      ex.printStackTrace();
    }
    return false;
  }

  private boolean writeTFWFile() {
    BufferedWriter outWrite = null;
    try {
      // String out = inTiff.replaceAll("(?i).tif", ".tfw");
      String out = inTiff.replace(".tif", ".tfw");
      outWrite = new BufferedWriter(new FileWriter(out));
      DecimalFormat df = new DecimalFormat("#0.000000000000000");
      for (double d : tfwData) {
        outWrite.write(df.format(d));
        outWrite.newLine();
      }
      return true;
    } catch (IOException ex) {
      ex.printStackTrace();
    } finally {
      try {
        outWrite.close();
      } catch (IOException ex) {
        ex.printStackTrace();
      }
    }
    return false;
  }
}
