package com.asascience.edc.map;

import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.avlist.AVListImpl;
import gov.nasa.worldwind.event.PositionEvent;
import gov.nasa.worldwind.event.PositionListener;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.render.BasicShapeAttributes;
import gov.nasa.worldwind.render.Material;
import gov.nasa.worldwind.render.ShapeAttributes;
import gov.nasa.worldwind.render.SurfacePolygon;

import java.awt.event.*;
import java.util.*;

import com.asascience.edc.utils.WorldwindUtils;

/**
 * WorldwindBoundingBoxBuilder.java
 * 
 * @author Kyle Wilcox <kwilcox@asascience.com>
 */
public class WorldwindBoundingBoxBuilder extends WorldWindMapControl {

  private BoundingBox bbox;
  protected final SurfacePolygon polygon;

public final static String BBOX_DRAWN = "WorldwindBoundingBoxBuilder.BBOXDrawn";
public final static String BBOX_COMPLETE = "WorldwindBoundingBoxBuilder.BBOXComplete";
public final static String BBOX_STARTED = "WorldwindBoundingBoxBuilder.BBOXStarted";

  /**
   * Construct a new line builder using the specified polygon and layer and drawing events from the specified world
   * window. Either or both the polygon and the layer may be null, in which case the necessary object is created.
   *
   * @param wwd       the world window to draw events from.
   * @param lineLayer the layer holding the polygon. May be null, in which case a new layer is created.
   * @param polyline  the polygon object to build. May be null, in which case a new polygon is created.
   */
  public WorldwindBoundingBoxBuilder(final WorldWindow wwd, RenderableLayer lineLayer, SurfacePolygon polygon) {
	  super(wwd, lineLayer);
	  if (polygon != null) {
	      this.polygon = polygon;
	    } else {
	 
	      this.polygon = new SurfacePolygon();
	      this.polygon.setAttributes(atts);
	    }
	    this.layer.addRenderable(this.polygon);
	    bbox = new BoundingBox();
	  initListeners();
    
  }


  
  private void initListeners(){
	  this.wwd.getInputHandler().addMouseListener(new MouseAdapter() {

	      @Override
	      public void mousePressed(MouseEvent mouseEvent) {
	        if (armed && mouseEvent.getButton() == MouseEvent.BUTTON1) {
	          active = true;
	          clear();
	          firePropertyChange(BBOX_STARTED, null, true);

	          addPosition();
	          mouseEvent.consume();
	        }
	      }

	      @Override
	      public void mouseReleased(MouseEvent mouseEvent) {
	        if (armed && mouseEvent.getButton() == MouseEvent.BUTTON1) {
	          if (bbox.upperLeft != null) {
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
    this.bbox.upperLeft = null;
    this.bbox.lowerRight = null;
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
	    // Set this.positions to the bounding box, computed from the corners.
      ArrayList<Position> positions = new ArrayList<Position>();
    if (bbox.upperLeft != null && bbox.lowerRight != null) {
    	Position realUl = bbox.upperLeft;
    	Position realLr = bbox.lowerRight;
       

    	if(realUl.latitude.degrees < realLr.latitude.degrees) {
    		realUl = bbox.upperLeft;
    		realLr = bbox.lowerRight;
    	}

      

    	
  
  
      Position ur = new Position(realUl.getLatitude(), realLr.getLongitude(), 0);
      Position ll = new Position(realLr.getLatitude(), realUl.getLongitude(), 0);
      double leftLonNorm360 =  WorldwindUtils.normLon360(realUl.getLongitude().degrees);
      double rightLonNorm360 =  WorldwindUtils.normLon360(realLr.getLongitude().degrees);
      // add all degrees that should
      // be included in the bounding box. This is necessary
      // so that the .extend function call in calcultateBoundingBox 
      // extends the polygon in the correct direction
      if(rightLonNorm360 < leftLonNorm360) {
    	  // crosses the prime meridian 
    	  
    	  // add top left
    	 for(double lonI = leftLonNorm360; lonI <= 360; lonI=lonI+1.0 ) {
    	  positions.add(Position.fromDegrees(realUl.getLatitude().degrees, WorldwindUtils.normLon(lonI)));
    	 }
    	 // add top right
    	 for(double lonI = 0; lonI <= rightLonNorm360; lonI=lonI+1.0){
    		 positions.add(Position.fromDegrees(realUl.getLatitude().degrees, WorldwindUtils.normLon(lonI)));
    	 }
    	 // add lower right
    	 for(double lonI = rightLonNorm360; lonI >= 0;  lonI=lonI-1.0 ) {
       	  positions.add(Position.fromDegrees(realLr.getLatitude().degrees, WorldwindUtils.normLon(lonI)));
       	 }
       	 // add lower left
       	 for(double lonI = 360; lonI >= leftLonNorm360; lonI=lonI-1.0){
       		 positions.add(Position.fromDegrees(realLr.getLatitude().degrees, WorldwindUtils.normLon(lonI)));

       	 }

    	
      }
      else {
    	  double midPoint = (leftLonNorm360 + rightLonNorm360) / 2.0;
       	  // add top right
    	  for(double lonI = midPoint; lonI <= rightLonNorm360; lonI=lonI+1.0 ) {
    		  positions.add(Position.fromDegrees(ur.getLatitude().degrees, WorldwindUtils.normLon(lonI)));
    	  }
    	  positions.add(ur);
    	  positions.add(realLr);
    	  // add bottom 
    	  for(double lonI = rightLonNorm360; lonI >= leftLonNorm360; lonI=lonI-1.0 ) {
    		  positions.add(Position.fromDegrees(realLr.getLatitude().degrees, WorldwindUtils.normLon(lonI)));
    	  }
    
    	  positions.add(ll);
    	  positions.add(realUl);
    	  
    	  // add top left
    	  for(double lonI = leftLonNorm360; lonI <=  midPoint; lonI=lonI+1.0){
    		  positions.add(Position.fromDegrees(realUl.getLatitude().degrees, WorldwindUtils.normLon(lonI)));


    	  }

      }
      
    }  
      this.polygon.setLocations(positions);
      this.wwd.redraw();
      this.firePropertyChange(BBOX_DRAWN, null, true);
    
  }

  public void setUpperLeftPoint(Position ul) {
    this.bbox.setUpperLeft(ul);
  }

  public void setLowerRightPoint(Position lr) {
	  this.bbox.setLowerRight(lr);
  }

  public void redraw() {
    drawBBOX();
  }


  private void addPosition() {
    Position curPos = this.wwd.getCurrentPosition();
    if (curPos == null) {
      return;
    }
    if (bbox.upperLeft == null) {
      this.bbox.upperLeft = curPos;
    } else {
      this.bbox.lowerRight = curPos;
    }

    drawBBOX();
  }

  private void completeBBOX() {
    this.firePropertyChange(BBOX_COMPLETE, null, true);
  }
}