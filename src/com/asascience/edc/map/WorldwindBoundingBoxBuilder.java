/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.asascience.edc.map;

import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.avlist.AVListImpl;
import gov.nasa.worldwind.event.PositionEvent;
import gov.nasa.worldwind.event.PositionListener;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.render.BasicShapeAttributes;
import gov.nasa.worldwind.render.Material;
import gov.nasa.worldwind.render.ShapeAttributes;
import gov.nasa.worldwind.render.SurfacePolygon;

import java.awt.event.*;
import java.util.*;

public class WorldwindBoundingBoxBuilder extends AVListImpl {

  private final WorldWindow wwd;
  private boolean armed = false;
  private Position ul;
  private Position lr;
  private final RenderableLayer layer;
  private final SurfacePolygon polygon;
  private boolean active = false;

  /**
   * Construct a new line builder using the specified polygon and layer and drawing events from the specified world
   * window. Either or both the polygon and the layer may be null, in which case the necessary object is created.
   *
   * @param wwd       the world window to draw events from.
   * @param lineLayer the layer holding the polygon. May be null, in which case a new layer is created.
   * @param polyline  the polygon object to build. May be null, in which case a new polygon is created.
   */
  public WorldwindBoundingBoxBuilder(final WorldWindow wwd, RenderableLayer lineLayer, SurfacePolygon polygon) {
    this.wwd = wwd;

    if (polygon != null) {
      this.polygon = polygon;
    } else {
      ShapeAttributes atts = new BasicShapeAttributes();
      atts.setInteriorMaterial(Material.WHITE);
      atts.setOutlineOpacity(0.8);
      atts.setInteriorOpacity(0.3);
      atts.setOutlineMaterial(Material.GREEN);
      atts.setOutlineWidth(1);
      atts.setDrawOutline(true);
      atts.setDrawInterior(true);
      this.polygon = new SurfacePolygon();
      this.polygon.setAttributes(atts);
    }
    this.layer = lineLayer != null ? lineLayer : new RenderableLayer();
    this.layer.addRenderable(this.polygon);
    this.wwd.getModel().getLayers().add(this.layer);
    this.wwd.getInputHandler().addMouseListener(new MouseAdapter() {
      
      @Override
      public void mousePressed(MouseEvent mouseEvent) {
        if (armed && mouseEvent.getButton() == MouseEvent.BUTTON1) {
          active = true;
          clear();
          addPosition();
          mouseEvent.consume();
        }
      }

      @Override
      public void mouseReleased(MouseEvent mouseEvent) {
        if (armed && mouseEvent.getButton() == MouseEvent.BUTTON1) {
          if (ul != null) {
            addPosition();
            completeBBOX();
          }
          active = false;
          mouseEvent.consume();
        }
      }
    });

    this.wwd.getInputHandler().addMouseMotionListener(new MouseMotionAdapter() {

      @Override
      public void mouseDragged(MouseEvent mouseEvent) {
        if (armed && (mouseEvent.getModifiersEx() & MouseEvent.BUTTON1_DOWN_MASK) != 0) {
          // Don't update the polygon here because the wwd current cursor position will not
          // have been updated to reflect the current mouse position. Wait to update in the
          // position listener, but consume the event so the view doesn't respond to it.
          if (active) {
            mouseEvent.consume();
          }
        }
      }
    });

    this.wwd.addPositionListener(new PositionListener() {

      public void moved(PositionEvent event) {
        if (!active) {
          return;
        }

        addPosition();
      }
    });
  }

  /**
   * Returns the layer holding the polygon being created.
   *
   * @return the layer containing the polygon.
   */
  public RenderableLayer getLayer() {
    return this.layer;
  }

  /**
   * Returns the layer currently used to display the polygon.
   *
   * @return the layer holding the polygon.
   */
  public SurfacePolygon getPolygon() {
    return this.polygon;
  }

  /**
   * Removes all positions from the polygon.
   */
  public void clear() {
    this.polygon.clearList();
    this.ul = null;
    this.lr = null;
    drawBBOX();
  }

  /**
   * Identifies whether the line builder is armed.
   *
   * @return true if armed, false if not armed.
   */
  public boolean isArmed() {
    return this.armed;
  }

  /**
   * Arms and disarms the line builder. When armed, the line builder monitors user input and builds the polygon in
   * response to the actions mentioned in the overview above. When disarmed, the line builder ignores all user input.
   *
   * @param armed true to arm the line builder, false to disarm it.
   */
  public void setArmed(boolean armed) {
    this.armed = armed;
  }
  
  private void drawBBOX() {
    
    if (ul != null && lr != null) {
      // Set this.positions to the bounding box, computed from the corners.
      ArrayList<Position> positions = new ArrayList<Position>();
      
      Position ur = new Position(ul.getLatitude(), lr.getLongitude(), 0);
      Position ll = new Position(lr.getLatitude(), ul.getLongitude(), 0);

      positions.add(ul);
      positions.add(ur);
      positions.add(lr);
      positions.add(ll);

      this.polygon.setLocations(positions);
      //this.wwd.redraw();
      this.firePropertyChange("WorldwindBoundingBoxBuilder.BBOXDrawn", null, true);
    }
  }
  
  public void setUpperLeftPoint(Position ul) {
    this.ul = ul;
  }
  
  public void setLowerRightPoint(Position lr) {
    this.lr = lr;
  }
  
  public void redraw() {
    drawBBOX();
  }
  
  private void addPosition() {
    Position curPos = this.wwd.getCurrentPosition();
    if (curPos == null) {
      return;
    }
    if (ul == null) {
      this.ul = curPos;
    } else {
      this.lr = curPos;
    }
    drawBBOX();
  }
  
  private void completeBBOX() {
    this.firePropertyChange("WorldwindBoundingBoxBuilder.BBOXComplete", null, true);
  }
}