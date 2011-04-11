/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * TimeseriesMouseMode.java
 *
 * Created on Jul 10, 2008, 8:29:08 AM
 *
 */
package com.asascience.openmap.mousemode;

import java.awt.Point;
import java.awt.event.MouseEvent;

import com.bbn.openmap.MapBean;
import com.bbn.openmap.event.CoordMouseMode;

/**
 * 
 * @author cmueller_mac
 */
public class TimeseriesMouseMode extends CoordMouseMode {

  public final static String TIMESERIES = "timeseries";
  public final static transient String modeID = "Timeseries";
  private MapBean map;
  private Point clickPoint;

  /** Creates a new instance of TimeseriesMouseMode */
  public TimeseriesMouseMode() {
    super(modeID, true);
  }

  @Override
  public void mousePressed(MouseEvent e) {
    if (e.getButton() == MouseEvent.BUTTON1) {
      if (map == null) {
        map = (MapBean) e.getSource();
      }
      clickPoint = e.getPoint();
      firePropertyChange("timeseries", "timeseries", ((clickPoint != null) ? clickPoint : null));
    }
  }
}
