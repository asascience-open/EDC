package com.asascience.edc.map;

import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.avlist.AVListImpl;
import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.render.BasicShapeAttributes;
import gov.nasa.worldwind.render.Material;
import gov.nasa.worldwind.render.ShapeAttributes;


public class WorldWindMapControl extends AVListImpl {

	  protected final WorldWindow wwd;
	  protected boolean armed = false;
	
	  protected final RenderableLayer layer;
	  protected boolean active = false;
	  protected   ShapeAttributes atts;
	  /**
	   * Construct a new line builder using the specified polygon and layer and drawing events from the specified world
	   * window. Either or both the polygon and the layer may be null, in which case the necessary object is created.
	   *
	   * @param wwd       the world window to draw events from.
	   * @param lineLayer the layer holding the polygon. May be null, in which case a new layer is created.
	   * @param polyline  the polygon object to build. May be null, in which case a new polygon is created.
	   */
	  public WorldWindMapControl(final WorldWindow wwd, 
			  					 RenderableLayer lineLayer) {
	    this.wwd = wwd;
	     atts = new BasicShapeAttributes();
	      atts.setInteriorMaterial(Material.WHITE);
	      atts.setOutlineOpacity(0.8);
	      atts.setInteriorOpacity(0.3);
	      atts.setOutlineMaterial(Material.GREEN);
	      atts.setOutlineWidth(1);
	      atts.setDrawOutline(true);
	      atts.setDrawInterior(true);

	    this.layer = lineLayer != null ? lineLayer : new RenderableLayer();
	    this.layer.setPickEnabled(false);
	    this.wwd.getModel().getLayers().add(this.layer);
	
	    
	    
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
	   * Identifies whether the line builder is armed.
	   *
	   * @return true if armed, false if not armed.
	   */
	  public boolean isArmed() {
	    return this.armed;
	  }


	public void setArmed(boolean armed) {
		this.armed = armed;
	}

	

	 
	
}
