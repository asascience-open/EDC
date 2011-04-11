/*
 * Applied Science Associates, Inc.
 * Copyright 2007. All rights reserved.
 */

/*
 * FileMonitor.java
 *
 * Created on Feb 14, 2008, 12:59:15 PM
 *
 */
package com.asascience.utilities;

import java.util.Timer;
import java.util.TimerTask;

import org.jdesktop.swingworker.SwingWorker;

/**
 * 
 * @author CBM <cmueller@asascience.com>
 */
public class FileMonitor extends SwingWorker {

  private java.io.File file;
  private Timer t;
  private long length = 0;

  /** Creates a new instance of FileMonitor */
  public FileMonitor(String file) {
    this.file = new java.io.File(file);
    t = new Timer();
  }

  public void startMonitor() {
    try {
      doInBackground();
    } catch (Exception ex) {
      ex.printStackTrace();
    }
  }

  public void stopMonitor() {
    t.cancel();
  }

  @Override
  protected Void doInBackground() throws Exception {
    if (t != null) {
      t.scheduleAtFixedRate(task, 0, 5);
    }
    return null;
  }
  private TimerTask task = new TimerTask() {

    @Override
    public void run() {
      if (file.exists()) {
        if (file.length() != length) {
          long oLen = length;
          length = file.length();
          firePropertyChange("filelength", oLen, length);
        }
      }
    }
  };
}
