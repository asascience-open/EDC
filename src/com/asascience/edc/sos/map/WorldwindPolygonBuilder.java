/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.asascience.edc.sos.map;

import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.avlist.AVListImpl;
import gov.nasa.worldwind.event.PositionEvent;
import gov.nasa.worldwind.event.PositionListener;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.render.SurfacePolygon;

import java.awt.event.*;
import java.util.*;

public class WorldwindPolygonBuilder extends AVListImpl {

  private final WorldWindow wwd;
  private boolean armed = false;
  private ArrayList<Position> positions = new ArrayList<Position>();
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
  public WorldwindPolygonBuilder(final WorldWindow wwd, RenderableLayer lineLayer, SurfacePolygon polygon) {
    this.wwd = wwd;

    if (polygon != null) {
      this.polygon = polygon;
    } else {
      this.polygon = new SurfacePolygon();
    }
    this.layer = lineLayer != null ? lineLayer : new RenderableLayer();
    this.layer.addRenderable(this.polygon);
    this.wwd.getModel().getLayers().add(this.layer);
    this.wwd.getInputHandler().addMouseListener(new MouseAdapter() {
      
      @Override
      public void mousePressed(MouseEvent mouseEvent) {
        if (armed && mouseEvent.getButton() == MouseEvent.BUTTON1) {
          if (armed && (mouseEvent.getModifiersEx() & MouseEvent.BUTTON1_DOWN_MASK) != 0) {
            if (!mouseEvent.isControlDown()) {
              active = true;
              addPosition();
            }
          }
          mouseEvent.consume();
        }
      }

      @Override
      public void mouseReleased(MouseEvent mouseEvent) {
        if (armed && mouseEvent.getButton() == MouseEvent.BUTTON1) {
          if (positions.size() == 1) {
            removePosition();
          }
          active = false;
          mouseEvent.consume();
        }
      }

      @Override
      public void mouseClicked(MouseEvent mouseEvent) {
        if (armed && mouseEvent.getButton() == MouseEvent.BUTTON1) {
          if (mouseEvent.isControlDown()) {
            removePosition();
          }
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

        if (positions.size() == 1) {
          addPosition();
        } else {
          replacePosition();
        }
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
    while (this.positions.size() > 0) {
      this.removePosition();
    }
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

  public void addPosition(Position pos) {
    this.positions.add(pos);
    this.polygon.setLocations(this.positions);
    //this.firePropertyChange("WorldwindPolygonBuilder.AddPosition", null, pos);
    this.wwd.redraw();
  }
  
  private void addPosition() {
    Position curPos = this.wwd.getCurrentPosition();
    if (curPos == null) {
      return;
    }

    this.positions.add(curPos);
    this.polygon.setLocations(this.positions);
    this.firePropertyChange("WorldwindPolygonBuilder.AddPosition", null, curPos);
    this.wwd.redraw();
  }

  private void replacePosition() {
    Position curPos = this.wwd.getCurrentPosition();
    if (curPos == null) {
      return;
    }

    int index = this.positions.size() - 1;
    if (index < 0) {
      index = 0;
    }

    Position currentLastPosition = this.positions.get(index);
    this.positions.set(index, curPos);
    this.polygon.setLocations(this.positions);
    this.firePropertyChange("WorldwindPolygonBuilder.ReplacePosition", currentLastPosition, curPos);
    this.wwd.redraw();
  }

  private void removePosition() {
    if (this.positions.isEmpty()) {
      return;
    }

    Position currentLastPosition = this.positions.get(this.positions.size() - 1);
    this.positions.remove(this.positions.size() - 1);
    this.polygon.setLocations(this.positions);
    this.firePropertyChange("WorldwindPolygonBuilder.RemovePosition", currentLastPosition, null);
    this.wwd.redraw();
  }
}