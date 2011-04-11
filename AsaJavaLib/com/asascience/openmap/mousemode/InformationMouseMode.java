/*
 * InformationMouseMode.java
 *
 * Created on November 28, 2007, 1:40 PM
 *
 * Applied Science Associates, Inc.
 * Copyright 2007.  All rights reserved.
 */
package com.asascience.openmap.mousemode;

import com.bbn.openmap.event.SelectMouseMode;

/**
 * 
 * @author CBM
 */
public class InformationMouseMode extends SelectMouseMode {

  /**
   *
   */
  public final static transient String modeID = "Information";

  /** Creates a new instance of InformationMouseMode */
  public InformationMouseMode() {
    super("Information", true);
  }
}
