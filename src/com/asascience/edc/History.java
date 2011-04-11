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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;

/**
 *
 * @author CBM <cmueller@asascience.com>
 */
public class History {

  private static File f;

  public static boolean initialize(String historyFile) {
    try {
      f = new File(historyFile);
      if (!f.exists()) {
        BufferedWriter output = new BufferedWriter(new FileWriter(f));
        output.newLine();
        output.close();
      }
      if (f.canWrite()) {
        return true;
      } else {
        throw new Exception("Can't write to the history file");
      }
    } catch (Exception ex) {
      ex.printStackTrace();
    }
    return false;
  }

  public static void addEntry(String name, String path) throws FileNotFoundException, IOException {
    BufferedWriter output = new BufferedWriter(new FileWriter(f, true));
    StringBuilder s = new StringBuilder();
    s.append(name);
    s.append(":");
    s.append(path);
    try {
      output.write(s.toString());
      output.newLine();
      output.flush();
    } catch (IOException ioe) {
      ioe.printStackTrace();
    } finally {
      output.close();
    }
  }
}
