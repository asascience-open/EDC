/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * LocationSelectMouseMode.java
 *
 * Created on Jun 3, 2008, 1:20:04 PM
 *
 */

package com.asascience.openmap.mousemode;

import java.awt.Cursor;
import java.awt.Point;
import java.awt.event.MouseEvent;

import com.asascience.utilities.Utils;
import com.bbn.openmap.LatLonPoint;
import com.bbn.openmap.MapBean;
import com.bbn.openmap.event.CoordMouseMode;

/**
 * 
 * @author cmueller_mac
 */
public class LocationSelectMouseMode extends CoordMouseMode {

	public final static transient String modeID = "LocSelect";

	private MapBean map;
	private LatLonPoint selLoc;

	private boolean useCursor;

	/** Creates a new instance of LocationSelectMouseMode */
	public LocationSelectMouseMode() {
		super(modeID, true);
		setUseCursor(true);
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		e.getComponent().requestFocus();
		if (e.getSource() instanceof MapBean) {
			if (map == null) {
				map = (MapBean) e.getSource();
			}
			Point p = e.getPoint();

			// p.translate(10, 6);//shift the point to match the "center" of the
			// mouse cursor graphic...

			selLoc = map.getProjection().inverse(p);

			firePropertyChange("locSelect", true, selLoc);
		}
	}

	/**
	 * @param useCursor
	 *            The useCursor to set.
	 */
	public void setUseCursor(boolean useCursor) {
		this.useCursor = useCursor;
		if (useCursor) {
			Cursor c = Utils.createCustomCursor("LocSelect.gif", LocationSelectMouseMode.class, true);
			setModeCursor(c);
		}
	}
}
