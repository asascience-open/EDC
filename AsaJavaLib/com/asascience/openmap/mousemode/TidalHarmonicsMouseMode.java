/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * TidalHarmonicsMouseMode.java
 *
 * Created on May 6, 2008, 9:59:22 AM
 *
 */

package com.asascience.openmap.mousemode;

import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import com.bbn.openmap.LatLonPoint;
import com.bbn.openmap.MapBean;
import com.bbn.openmap.event.CoordMouseMode;

/**
 * 
 * @author cmueller_mac
 */
public class TidalHarmonicsMouseMode extends CoordMouseMode {

	public final static String TIDE_HARM = "tideHarm";
	public final static String VIEW_DATA = "viewData";
	public final static String TIMESERIES = "timeseries";
	public final static transient String modeID = "TideHarm";
	private JPopupMenu popup;
	private MapBean map;
	private Point clickPoint;
	private LatLonPoint clickLoc;

	/** Creates a new instance of TidalHarmonicsMouseMode */
	public TidalHarmonicsMouseMode() {
		super(modeID, true);
		popup = new JPopupMenu();
		HarmonicsActions ha = new HarmonicsActions();

		JMenuItem tideHarm = new JMenuItem("Run Tidal Harmonics on Vector");
		tideHarm.setActionCommand(TidalHarmonicsMouseMode.TIDE_HARM);
		tideHarm.addActionListener(ha);

		JMenuItem viewData = new JMenuItem("View Current Data");
		viewData.setActionCommand(TidalHarmonicsMouseMode.VIEW_DATA);
		viewData.addActionListener(ha);

		JMenuItem timeseries = new JMenuItem("View Timeseries");
		timeseries.setActionCommand(TidalHarmonicsMouseMode.TIMESERIES);
		timeseries.addActionListener(ha);

		popup.add(viewData);
		popup.add(timeseries);
		popup.add(tideHarm);
	}

	@Override
	public void mousePressed(MouseEvent e) {
		if (e.getButton() == MouseEvent.BUTTON3) {
			if (map == null) {
				map = (MapBean) e.getSource();
			}
			clickLoc = map.getProjection().inverse(e.getPoint());
			clickPoint = e.getPoint();

			showPopup(e);
		}
	}

	private void showPopup(MouseEvent e) {
		if (e.getButton() == MouseEvent.BUTTON3) {
			popup.show(e.getComponent(), e.getX(), e.getY());
		}
	}

	class HarmonicsActions implements ActionListener {

		public void actionPerformed(ActionEvent e) {
			String name = e.getActionCommand();
			// System.out.println("thmm: " + name);
			if (name.equals(TidalHarmonicsMouseMode.TIDE_HARM)) {
				firePropertyChange("harmonics", "tideharm", ((clickPoint != null) ? clickPoint : null));
			} else if (name.equals(TidalHarmonicsMouseMode.VIEW_DATA)) {
				firePropertyChange("harmonics", "viewdata", ((clickPoint != null) ? clickPoint : null));
			} else if (name.equals(TidalHarmonicsMouseMode.TIMESERIES)) {
				firePropertyChange("harmonics", "timeseries", ((clickPoint != null) ? clickPoint : null));
			}
		}
	}
}
