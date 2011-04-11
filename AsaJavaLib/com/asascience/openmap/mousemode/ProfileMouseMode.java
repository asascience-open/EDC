/*
 * Applied Science Associates, Inc.
 * Copyright 2007. All rights reserved.
 */

/*
 * ProfileMouseMode.java
 *
 * Created on Mar 26, 2008, 12:03:22 PM
 *
 */
package com.asascience.openmap.mousemode;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import com.bbn.openmap.LatLonPoint;
import com.bbn.openmap.MapBean;
import com.bbn.openmap.event.CoordMouseMode;
import com.bbn.openmap.omGraphics.OMGraphic;
import com.bbn.openmap.omGraphics.OMLine;
import com.bbn.openmap.proj.Projection;

/**
 * 
 * @author CBM <cmueller@asascience.com>
 */
public class ProfileMouseMode extends CoordMouseMode {

  public final static String CREATE = "create";
  public final static String ERASE = "erase";
  public final static transient String modeID = "Profile";
  private MapBean map;
  private LatLonPoint sPoint, ePoint;
  private JPopupMenu popup;
  /**
   * Flag, true if the mouse has already been pressed
   */
  public boolean mousePressed = false;
  /**
   * Vector to store all distance segments, first point and last point pairs
   */
  public List<LatLonPoint> segments = new ArrayList<LatLonPoint>();
  /**
   * The line type to be displayed, see OMGraphic. LINETYPE_GREATCIRCLE,
   * LINETYPE_RHUMB, LINETYPE_STRAIGHT default LINETYPE_GREATCIRCLE
   */
  public static int lineType = OMGraphic.LINETYPE_RHUMB;

  /** Creates a new instance of ProfileMouseMode */
  public ProfileMouseMode() {
    super(modeID, true);
    popup = new JPopupMenu();
    ProfileActions pa = new ProfileActions();

    JMenuItem create = new JMenuItem("Create Profile");
    create.setActionCommand(ProfileMouseMode.CREATE);
    create.addActionListener(pa);

    JMenuItem erase = new JMenuItem("Erase Line");
    erase.setActionCommand(ProfileMouseMode.ERASE);
    erase.addActionListener(pa);

    popup.add(create);
    popup.add(erase);
  }

  @Override
  public void mouseClicked(MouseEvent e) {
    // if(e.getSource() instanceof MapBean){
    // // if double (or more) mouse clicked
    // if (e.getClickCount() >= 2) {
    // // end of distance path
    // mousePressed = false;
    // // add the last point to the line segments
    // segments.add(ePoint);
    //
    // firePropertyChange("profile", null, segments);
    //
    // // erase all line segments
    // eraseLines();
    //
    // // cleanup
    // cleanUp();
    // }
    // }
  }

  @Override
  public void mousePressed(MouseEvent e) {
    e.getComponent().requestFocus();
    if (e.getSource() instanceof MapBean) {
      // mouse has now been pressed
      mousePressed = true;
      // erase the old circle if any

      if (map == null) {
        map = (MapBean) e.getSource();
      }

      // anchor the new first point of the line
      sPoint = map.getProjection().inverse(e.getPoint());
      // ensure the second point is not yet set.
      ePoint = null;
      // add the anchor point to the list of line segments
      if (segments.size() == 0) {
        segments.add(sPoint);
      } else {
        LatLonPoint last = segments.get(segments.size() - 1);
        if ((last.getLatitude() == sPoint.getLatitude()) & (last.getLongitude() == sPoint.getLongitude())) {
          // System.out.println("LatLonPoint already in list.");
        } else {
          segments.add(sPoint);
        }
      }
    }

    if (e.getButton() == MouseEvent.BUTTON3) {
      if (segments != null && segments.size() > 1) {
        showPopup(e);
      }
      // // end of distance path
      // mousePressed = false;
      // // add the last point to the line segments
      // segments.add(ePoint);
      //
      // firePropertyChange("profile", null, segments);
      //
      // // erase all line segments
      // eraseLines();
      //
      // // cleanup
      // cleanUp();
    }
  }

  @Override
  public void mouseMoved(MouseEvent e) {
    if (e.getSource() instanceof MapBean) {
      // only when the mouse has already been pressed
      if (mousePressed) {
        // set the map bean
        map = (MapBean) (e.getSource());
        // erase the old line and circle first
        paintRubberband(sPoint, ePoint);
        // get the current mouse location in latlon
        ePoint = map.getProjection().inverse(e.getPoint());
        // paint the new line and circle up to the current
        // mouse location
        paintRubberband(sPoint, ePoint);

      } else {
        fireMouseLocation(e);
      }
    }
  }

  /**
   * Draw a rubberband line and circle between two points
   *
   * @param pt1
   *            the anchor point.
   * @param pt2
   *            the current (mouse) position.
   */
  public void paintRubberband(LatLonPoint pt1, LatLonPoint pt2) {
    if (map != null) {
      paintRubberband(pt1, pt2, map.getGraphics());
    }
  }

  /**
   * Draw a rubberband line and circle between two points
   *
   * @param pt1
   *            the anchor point.
   * @param pt2
   *            the current (mouse) position.
   * @param g
   *            a java.awt.Graphics object to render into.
   */
  public void paintRubberband(LatLonPoint pt1, LatLonPoint pt2, Graphics g) {
    paintLine(pt1, pt2, g);
  }

  /**
   * Draw a rubberband line between two points
   *
   * @param pt1
   *            the anchor point.
   * @param pt2
   *            the current (mouse) position.
   */
  public void paintLine(LatLonPoint pt1, LatLonPoint pt2) {
    if (map != null) {
      paintLine(pt1, pt2, map.getGraphics());
    }
  }

  /**
   * Draw a rubberband line between two points into the Graphics object.
   *
   * @param pt1
   *            the anchor point.
   * @param pt2
   *            the current (mouse) position.
   * @param graphics
   *            a java.awt.Graphics object to render into.
   */
  public void paintLine(LatLonPoint pt1, LatLonPoint pt2, Graphics graphics) {
    Graphics2D g = (Graphics2D) graphics;
    g.setXORMode(java.awt.Color.cyan);
    g.setColor(java.awt.Color.red);
    if (pt1 != null && pt2 != null) {
      // the line connecting the segments
      OMLine cLine = new OMLine(pt1.getLatitude(), pt1.getLongitude(), pt2.getLatitude(), pt2.getLongitude(),
              lineType);
      // get the map projection
      Projection proj = map.getProjection();
      // prepare the line for rendering
      cLine.generate(proj);
      // render the line graphic
      cLine.render(g);
    }
  }

  /**
   * Erase all line segments.
   */
  public void eraseLines() {
    for (int i = 0; i < segments.size() - 1; i++) {
      paintLine((LatLonPoint) (segments.get(i)), (LatLonPoint) (segments.get(i + 1)));
    }
  }

  /**
   * Reset the segments
   */
  public void cleanUp() {
    // a quick way to clean the vector
    segments = new Vector();
  }

  private void showPopup(MouseEvent e) {
    if (e.getButton() == MouseEvent.BUTTON3) {
      popup.show(e.getComponent(), e.getX(), e.getY());
    }
  }

  class ProfileActions implements ActionListener {

    public void actionPerformed(ActionEvent e) {
      String name = e.getActionCommand();
      if (name.equals(ProfileMouseMode.CREATE)) {
        // end of distance path
        mousePressed = false;

        // // add the last point to the line segments
        // segments.add(ePoint);

        firePropertyChange("profile", null, segments);

        // erase all line segments
        eraseLines();

        // cleanup
        cleanUp();
      } else if (name.equals(ProfileMouseMode.ERASE)) {
        mousePressed = false;
        eraseLines();
        cleanUp();
      }
    }
  }
  // class PopupListener extends MouseAdapter{
  // @Override
  // public void mousePressed(MouseEvent e){
  // showPopup(e);
  // }
  // @Override
  // public void mouseReleased(MouseEvent e){
  // showPopup(e);
  // }
  //
  //
  // }
}
