/*
 * MeasureMouseMode.java
 *
 * Created on December 4, 2007, 8:51 AM
 *
 * Applied Science Associates, Inc.
 * Copyright 2007.  All rights reserved.
 */
package com.asascience.openmap.mousemode;

import java.awt.Cursor;

import com.bbn.openmap.event.DistanceMouseMode;
import com.bbn.openmap.proj.Length;

/**
 * 
 * @author CBM
 */
public class MeasureMouseMode extends DistanceMouseMode {

  public final static transient String modeID = "Measure";

  /**
   * Creates a new instance of MeasureMouseMode
   */
  public MeasureMouseMode() {
    super(modeID, true);
    super.setUnit(Length.METER);
    super.setModeCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
  }
}
