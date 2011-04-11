/*
 * TestNcellReader.java
 *
 * Created on December 12, 2007, 1:08 PM
 *
 * Applied Science Associates, Inc.
 * Copyright 2007.  All rights reserved.
 */
package com.asascience.openmap.layer.nc.ncell;

/**
 * 
 * @author CBM
 */
public class TestNcellReader {

  /** Creates a new instance of TestNcellReader */
  public TestNcellReader() {
    String file = "/Users/asamac/asascience/EdsBrowser/UAE7T8W_20071212145830169_12599_36375.NC";
    NcellReader eds = new NcellReader(file);
    System.err.println(eds.getUVs(eds.getStartTime()));

  }

  public static void main(String args[]) {
    java.awt.EventQueue.invokeLater(new Runnable() {

      public void run() {
        new TestNcellReader();
      }
    });
  }
}
