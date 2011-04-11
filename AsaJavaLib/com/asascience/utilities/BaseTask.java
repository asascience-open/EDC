/*
 * Applied Science Associates, Inc.
 * Copyright 2007. All rights reserved.
 */

/*
 * BaseTask.java
 *
 * Created on Jan 1, 2008, 12:00:00 AM
 *
 */
package com.asascience.utilities;

import org.jdesktop.swingworker.SwingWorker;

/**
 * 
 * @author CBM <cmueller@asascience.com>
 */
public class BaseTask extends SwingWorker<Void, Void> {

  private String name;

  public BaseTask() {
    super();
  }

  @Override
  public Void doInBackground() {
    return null;
  }

  @Override
  public void done() {
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }
}
