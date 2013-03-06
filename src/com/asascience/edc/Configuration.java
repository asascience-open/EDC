/*
 * Applied Science Associates, Inc.
 * Copyright 2007. All rights reserved.
 */

/*
 * Configuration.java
 *
 * Created on Feb 15, 2008, 10:22:26 AM
 *
 */
package com.asascience.edc;

import java.io.File;
import org.apache.log4j.Logger;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;

/**
 * 
 * @author CBM <cmueller@asascience.com>
 */
public class Configuration {

  public static int DISPLAY_TYPE = DisplayType.GENERAL;
  public static boolean CLOSE_AFTER_PROCESSING = true;
  public static boolean MAKE_POINTER = true;
  public static String OUTPUT_LOCATION = "";
  public static boolean ALLOW_FILE_REPLACEMENT = false;
  public static boolean USE_VARIABLE_NAME_FOR_OUTPUT = true;
  private static Logger logger = Logger.getLogger(Configuration.class);

  public static boolean initialize(String xmlfile) {
    try {
      File f = new File(xmlfile);
      if (!f.exists()) {
        return false;
      }
      SAXBuilder in = new SAXBuilder();
      Document xmlDoc = in.build(f);

      Element root = xmlDoc.getRootElement();

      if (root.getChild("DISPLAY_TYPE") != null) {
        DISPLAY_TYPE = Integer.parseInt(root.getChildText("DISPLAY_TYPE"));
      }
      if (root.getChild("CLOSE_AFTER_PROCESSING") != null) {
        CLOSE_AFTER_PROCESSING = Boolean.parseBoolean(root.getChildText("CLOSE_AFTER_PROCESSING"));
      }
      if (root.getChild("MAKE_POINTER") != null) {
        MAKE_POINTER = Boolean.parseBoolean(root.getChildText("MAKE_POINTER"));
      }
      if (root.getChild("OUTPUT_LOCATION") != null) {
        OUTPUT_LOCATION = root.getChildTextTrim("OUTPUT_LOCATION");
      }
      if (root.getChild("ALLOW_FILE_REPLACEMENT") != null) {
        ALLOW_FILE_REPLACEMENT = Boolean.parseBoolean(root.getChildTextTrim("ALLOW_FILE_REPLACEMENT"));
      }
      if (root.getChild("USE_VARIABLE_NAME_FOR_OUTPUT") != null) {
        USE_VARIABLE_NAME_FOR_OUTPUT = Boolean.parseBoolean(root.getChildTextTrim("USE_VARIABLE_NAME_FOR_OUTPUT"));
      }

      // check to see if the DISPLAY_TYPE is ESRI and override values if
      // it is
      Configuration.checkDisplayType();

      logger.info("Configuration initialization:");
      logger.info("  DISPLAY_TYPE: " + DISPLAY_TYPE);
      logger.info("  CLOSE_AFTER_PROCESSING: " + CLOSE_AFTER_PROCESSING);
      logger.info("  MAKE_POINTER: " + MAKE_POINTER);
      logger.info("  OUTPUT_LOCATION: " + OUTPUT_LOCATION);
      logger.info("  ALLOW_FILE_REPLACEMENT: " + ALLOW_FILE_REPLACEMENT);
      logger.info("");

      return true;
    } catch (Exception ex) {
      ex.printStackTrace();
    }
    return false;
  }

  private static void checkDisplayType() {
    if (DISPLAY_TYPE == DisplayType.ESRI) {
      ALLOW_FILE_REPLACEMENT = false;
    } else {
      ALLOW_FILE_REPLACEMENT = true;
    }
  }

  public static void setDisplayType(int val) {
    if (val == 0 || val == 1 || val == 2) {
      DISPLAY_TYPE = val;
      Configuration.checkDisplayType();
    }
  }

  public static class DisplayType {

    public static final int GENERAL = 0;
    public static final int ESRI = 1;
    public static final int OILMAP = 2;
    public static final int MATLAB = 3;
    public static final int R = 4;
  }
}
