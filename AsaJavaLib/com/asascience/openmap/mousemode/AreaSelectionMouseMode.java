/*
 * AreaSelectionMouseMode.java
 *
 * Created on September 11, 2007, 10:58 AM
 *
 * Applied Science Associates, Inc.
 * Copyright 2007.  All rights reserved.
 */
package com.asascience.openmap.mousemode;

import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import com.asascience.openmap.utilities.GeoConstraints;
import com.bbn.openmap.LatLonPoint;
import com.bbn.openmap.MapBean;
import com.bbn.openmap.event.CoordMouseMode;
import com.bbn.openmap.proj.Proj;
import com.bbn.openmap.proj.Projection;

/**
 * 
 * @author CBM
 */
public class AreaSelectionMouseMode extends CoordMouseMode implements PropertyChangeListener {

	public final static transient String modeID = "AreaSelection";
	protected Point pointS, pointE;
	private LatLonPoint startLL, endLL;
	private GeoConstraints geoCons;
	private PropertyChangeSupport pcs;

	/**
	 * Creates a new instance of AreaSelectionMouseMode
	 * 
	 * @param cons
	 */
	public AreaSelectionMouseMode(GeoConstraints cons) {
		this(true, cons);
	}

	public AreaSelectionMouseMode(boolean shouldConsumeEvents, GeoConstraints cons) {
		super(modeID, shouldConsumeEvents);
		geoCons = cons;

		pcs = new PropertyChangeSupport(this);

		// override the default cursor
		setModeCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
	}

	@Override
	public void mousePressed(MouseEvent e) {
		e.getComponent().requestFocus();
		if (!mouseSupport.fireMapMousePressed(e) && e.getSource() instanceof MapBean) {
			pointS = e.getPoint();
			pointE = null;
		}
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		// System.err.println(e.getButton());
		if (e.getButton() == MouseEvent.BUTTON1) {
			Object obj = e.getSource();
			mouseSupport.fireMapMouseClicked(e);
			if (!(obj instanceof MapBean) || pointS == null) {
				return;
			}

			MapBean map = (MapBean) obj;
			Projection prj = map.getProjection();
			Proj p = (Proj) prj;

			LatLonPoint llp = prj.inverse(e.getPoint());

			boolean shift = e.isShiftDown();
			boolean control = e.isControlDown();

			if (control) {
				if (shift) {
					p.setScale(p.getScale() * 2.0f);
				} else {
					p.setScale(p.getScale() / 2.0f);
				}

			}

			pointS = null;
			pointE = null;

			p.setCenter(llp);
			map.setProjection(p);
		} else if (e.getButton() == MouseEvent.BUTTON3) {
			pcs.firePropertyChange("rightclick", null, null);
		}
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		Object obj = e.getSource();

		mouseSupport.fireMapMouseReleased(e);

		if (!(obj instanceof MapBean) || pointS == null || pointE == null) {
			return;
		}

		MapBean map = (MapBean) obj;
		Projection projection = map.getProjection();
		Proj p = (Proj) projection;

		synchronized (this) {
			pointE = e.getPoint();
			// pointE = getRatioPoint((MapBean) e.getSource(),
			// pointS,
			// e.getPoint());
			int dx = Math.abs(pointS.x - pointE.x);
			int dy = Math.abs(pointS.y - pointE.y);

			// Don't bother redrawing if the rectangle is too small
			if ((dx < 5) || (dy < 5)) {
				// clean up the rectangle, since point2 has the old
				// value.
				paintRectangle(map, pointS, pointE);

				// If rectangle is too small in both x and y then
				// recenter the map
				if ((dx < 5) && (dy < 5)) {
					LatLonPoint llp = projection.inverse(e.getPoint());

					boolean shift = e.isShiftDown();
					boolean control = e.isControlDown();

					if (control) {
						if (shift) {
							p.setScale(p.getScale() * 2.0f);
						} else {
							p.setScale(p.getScale() / 2.0f);
						}
					}

					// reset the points here so the point doesn't get
					// rendered on the repaint.
					pointS = null;
					pointE = null;

					p.setCenter(llp);
					map.setProjection(p);
				}
				return;
			}

			// Figure out the new scale
			float newScale = com.bbn.openmap.proj.ProjMath.getScale(pointS, pointE, projection);

			// Figure out the center of the rectangle
			int centerx = Math.min(pointS.x, pointE.x) + dx / 2;
			int centery = Math.min(pointS.y, pointE.y) + dy / 2;
			LatLonPoint center = projection.inverse(centerx, centery);

			// Get the bounding points
			startLL = projection.inverse(pointS);
			endLL = projection.inverse(pointE);

			storeLatLon();
			// Set the parameters of the projection and then set
			// the projection of the map. This way we save having
			// the MapBean fire two ProjectionEvents.
			// p.setScale(newScale);
			// p.setCenter(center);

			// reset the points here so the point doesn't get rendered
			// on the repaint.
			pointS = null;
			pointE = null;

			map.setProjection(p);
		}
	}

	public void storeLatLon() {
		float latS = startLL.getLatitude();
		float latE = endLL.getLatitude();
		float lonS = startLL.getLongitude();
		float lonE = endLL.getLongitude();

		// System.err.println("Point1:\nLat:" + latS + "\nLong:" + lonS);
		// System.err.println("Point2:\nLat:" + latE + "\nLong:" + lonE);
		// if(Math.abs(lonS) > 90 & Math.abs(lonE) > 90){
		// if((lonS > 0 & lonE < 0) || (lonS < 0 & lonE > 0)){
		// System.err.println("CROSS!!");
		// }
		// }
		boolean crosses180 = false;
		// geoCons.setCrosses180(false);

		if ((Math.abs(lonS) + Math.abs(lonE)) > 180) {
			if ((lonS > 0 & lonE < 0) || (lonS < 0 & lonE > 0)) {
				// geoCons.setCrosses180(true);
				crosses180 = true;
			}
		}

		if (geoCons != null) {
			// set lats
			if (latS > latE) {
				geoCons.setNorthernExtent(latS);
				geoCons.setSouthernExtent(latE);
			} else {
				geoCons.setNorthernExtent(latE);
				geoCons.setSouthernExtent(latS);
			}

			// set lons
			if (!crosses180) {// it doesn't cross...treat normally....
				if (lonS < lonE) {
					geoCons.setWesternExtent(lonS);
					geoCons.setEasternExtent(lonE);
				} else {
					geoCons.setWesternExtent(lonE);
					geoCons.setEasternExtent(lonS);
				}
			} else {// it crosses...invert
				if (lonS < lonE) {
					geoCons.setWesternExtent(lonE);
					geoCons.setEasternExtent(lonS);
				} else {
					geoCons.setWesternExtent(lonS);
					geoCons.setEasternExtent(lonE);
				}
			}

			// System.err.println("asmm:cross="+geoCons.isCrosses180());
			// notify about the change
			pcs.firePropertyChange("boundsStored", false, true);

			// System.err.println("Stored Coordinates: (AreaSelectionMouseMode)");
			// System.err.println("  N: " + geoCons.getNorthernExtent());
			// System.err.println("  S: " + geoCons.getSouthernExtent());
			// System.err.println("  W: " + geoCons.getWesternExtent());
			// System.err.println("  E: " + geoCons.getEasternExtent());
		} else {
			System.err.println("ncBounds null");
		}
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		super.mouseEntered(e);
	}

	@Override
	public void mouseExited(MouseEvent e) {
		super.mouseExited(e);

		if (e.getSource() instanceof MapBean) {
			// clean up the last box drawn
			paintRectangle((MapBean) e.getSource(), pointS, pointE);
			// set the second point to null so that a new box will be
			// drawn if the mouse comes back, and the box will use the
			// old
			// starting point, if the mouse button is still down.
			pointE = null;
		}
	}

	@Override
	public void mouseDragged(MouseEvent e) {
		super.mouseDragged(e);

		if (e.getSource() instanceof MapBean) {

			// clean up the old rectangle, since point2 has the old
			// value.
			paintRectangle((MapBean) e.getSource(), pointS, pointE);
			// paint new rectangle
			// point2 = e.getPoint();
			pointE = e.getPoint();
			// pointE = getRatioPoint((MapBean) e.getSource(),
			// pointS,
			// e.getPoint());
			paintRectangle((MapBean) e.getSource(), pointS, pointE);
		}
	}

	protected Point getRatioPoint(MapBean map, Point pt1, Point pt2) {
		Projection proj = map.getProjection();
		float mapRatio = (float) proj.getHeight() / (float) proj.getWidth();

		float boxHeight = (float) (pt1.y - pt2.y);
		float boxWidth = (float) (pt1.x - pt2.x);
		float boxRatio = Math.abs(boxHeight / boxWidth);
		int isNegative = -1;
		if (boxRatio > mapRatio) {
			// box is too tall, adjust boxHeight
			if (boxHeight < 0) {
				isNegative = 1;
			}
			boxHeight = Math.abs(mapRatio * boxWidth);
			pt2.y = pt1.y + (isNegative * (int) boxHeight);

		} else if (boxRatio < mapRatio) {
			// box is too wide, adjust boxWidth
			if (boxWidth < 0) {
				isNegative = 1;
			}
			boxWidth = Math.abs(boxHeight / mapRatio);
			pt2.x = pt1.x + (isNegative * (int) boxWidth);
		}
		return pt2;
	}

	protected void paintRectangle(MapBean map, Point pt1, Point pt2) {
		if (map != null) {
			paintRectangle(map.getGraphics(), pt1, pt2);
		}
	}

	protected void paintRectangle(Graphics g, Point pt1, Point pt2) {
		g.setXORMode(java.awt.Color.lightGray);
		g.setColor(new java.awt.Color(0, 135, 255, 150));

		if (pt1 != null && pt2 != null) {
			int width = Math.abs(pt2.x - pt1.x);
			int height = Math.abs(pt2.y - pt1.y);

			if (width == 0) {
				width++;
			}
			if (height == 0) {
				height++;
			}

			g.fillRect(pt1.x < pt2.x ? pt1.x : pt2.x, pt1.y < pt2.y ? pt1.y : pt2.y, width, height);
			g.drawRect(pt1.x < pt2.x ? pt1.x + (pt2.x - pt1.x) / 2 - 1 : pt2.x + (pt1.x - pt2.x) / 2 - 1,
				pt1.y < pt2.y ? pt1.y + (pt2.y - pt1.y) / 2 - 1 : pt2.y + (pt1.y - pt2.y) / 2 - 1, 2, 2);
		}
	}

	/**
	 * 
	 * @param g
	 */
	@Override
	public void listenerPaint(java.awt.Graphics g) {
		// will be properly rejected of point1, point2 == null
		paintRectangle(g, pointS, pointE);
	}

	public void propertyChange(PropertyChangeEvent evt) {
	}

	public void addPropertyChangeListener(java.beans.PropertyChangeListener l) {
		pcs.addPropertyChangeListener(l);
	}

	public void removePropertyChangeListener(java.beans.PropertyChangeListener l) {
		pcs.removePropertyChangeListener(l);
	}

	public GeoConstraints getGeoCons() {
		return geoCons;
	}

	public void setGeoCons(GeoConstraints geoCons) {
		this.geoCons = geoCons;
	}
}
