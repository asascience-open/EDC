package com.asascience.edc.dap.map;

import com.asascience.edc.utils.WorldwindUtils;

import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.geom.Position;
import ucar.unidata.geoloc.LatLonRect;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.globes.Globe;
import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.render.BasicShapeAttributes;
import gov.nasa.worldwind.render.Box;
import gov.nasa.worldwind.render.Material;
import gov.nasa.worldwind.render.ShapeAttributes;
import gov.nasa.worldwind.render.SurfacePolygon;
import gov.nasa.worldwind.render.SurfaceSector;
import java.util.ArrayList;
import java.util.List;

/**
 * DataExtentLayer.java
 * 
 * @author Kyle Wilcox <kwilcox@asascience.com>
 */
public class DataExtentLayer extends RenderableLayer {

	private SurfacePolygon polygon;
	private SurfaceSector sector;
	private SurfaceSector sector2;
	private SurfaceSector initialZoomSector;
	private Box box;
	private Globe globe;

	public DataExtentLayer(Globe e) {
		this.globe = e;
		this.setPickEnabled(false);

		ShapeAttributes satts = new BasicShapeAttributes();
		satts.setOutlineMaterial(Material.RED);
		satts.setInteriorMaterial(Material.RED);
		satts.setOutlineOpacity(0.4);
		satts.setInteriorOpacity(0.4);
	

		this.polygon = new SurfacePolygon();
		this.polygon.setVisible(true);
		this.polygon.setAttributes(satts);

		this.sector = new SurfaceSector();
		this.sector.setVisible(false);
		this.sector.setAttributes(satts);
		// add an initial sector to set the initial fov
		// make the box inivisible 
		 sector.setSector(Sector.boundingSector(Position.fromDegrees(90,-180), 
				 								Position.fromDegrees(-90, 0)));
		this.sector.setPathType(AVKey.LINEAR);

		this.sector2 = new SurfaceSector();
		this.sector2.setVisible(true);
		this.sector2.setAttributes(satts);
		this.sector2.setPathType(AVKey.LINEAR);


		this.box = new Box();
		this.box.setVisible(true);
		this.box.setAttributes(satts);
		this.box.setCenterPosition(Position.fromDegrees(0,0));
		

		addRenderable(polygon);
		addRenderable(sector2);
		addRenderable(sector);
		addRenderable(box);
	}

  public void setDataExtent(LatLonRect bbox, boolean is360) {
	sector.setVisible(true);
	Double leftLon = bbox.getUpperLeftPoint().getLongitude();
	Double rightLon =  bbox.getLowerRightPoint().getLongitude();

	// fix to deal with bbox that goes from 0 to 360
	// when normalized both values would be 0 and the
	// bounding box would be a single line
	if(leftLon == 0 && rightLon == 0 && is360)
		leftLon = 359.9999;
	
    LatLon ul = WorldwindUtils.normalizeLatLon(LatLon.fromDegrees(bbox.getUpperLeftPoint().getLatitude(), leftLon));
    LatLon lr = WorldwindUtils.normalizeLatLon(LatLon.fromDegrees(bbox.getLowerRightPoint().getLatitude(), rightLon));


 	if (bbox.crossDateline() || is360){
		LatLon eL;
    	LatLon wL;
	   if(lr.longitude.degrees < 0){
    	 eL= LatLon.fromDegrees(ul.latitude.degrees, -179.999);
    	 wL= LatLon.fromDegrees(lr.latitude.degrees, 180);
	   }
	   else {
		    eL= LatLon.fromDegrees(ul.latitude.degrees, 180);
	    	 wL= LatLon.fromDegrees(lr.latitude.degrees, -179.999);
	   }
    	 sector2.setSector(Sector.boundingSector(wL, ul));
    	 sector.setSector(Sector.boundingSector(eL, lr));

    }
   else if (bbox.getWidth() > 180){
	   LatLon eL;
	   LatLon wL;
	   if(lr.longitude.degrees < 0){
		   eL= LatLon.fromDegrees(ul.latitude.degrees, -0.001);
		   wL= LatLon.fromDegrees(lr.latitude.degrees, 0);
	   }
	   else {
		   eL= LatLon.fromDegrees(ul.latitude.degrees, 0);
		   wL= LatLon.fromDegrees(lr.latitude.degrees, -0.001);
	   }
	   sector2.setSector(Sector.boundingSector(wL, ul));
	   sector.setSector(Sector.boundingSector(eL, lr));

   }
   else {
	   sector.setSector(Sector.boundingSector(ul, lr));
	   sector2.clearList();
   }
  }

  public Position getEyePosition() {
	  
    return WorldwindUtils.getEyePositionFromPositions(sector.getLocations(globe));
  }
}
