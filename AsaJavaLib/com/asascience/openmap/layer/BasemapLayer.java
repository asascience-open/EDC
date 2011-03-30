/*
 * Applied Science Associates, Inc.
 * Copyright 2009. All Rights Reserved.
 *
 * BasemapLayer.java
 *
 * Created on Feb 17, 2009 @ 8:28:35 AM
 */

package com.asascience.openmap.layer;

import java.util.Properties;

import com.bbn.openmap.event.ProjectionEvent;
import com.bbn.openmap.layer.shape.ShapeLayer;
import com.bbn.openmap.omGraphics.DrawingAttributes;
import com.bbn.openmap.proj.Projection;
import java.io.File;

/**
 * 
 * @author CBM <cmueller@asascience.com>
 */

public class BasemapLayer extends ShapeLayer {

	private String dataDir;
	private String projID = "";

	public BasemapLayer(String dataDir) {
		super();
		this.dataDir = dataDir;
		Properties lyrProps = new Properties();
		lyrProps.put("prettyName", "Basemap");
		lyrProps.put("lineColor", "000000");
		lyrProps.put("fillColor", "666666");// BDDE83
		lyrProps.put("shapeFile", dataDir + "lowResCoast.shp");
		this.setProperties(lyrProps);
		this.setAddAsBackground(true);
		this.setVisible(true);
    this.setRemovable(false);
	}

  @Override
	public void projectionChanged(ProjectionEvent e) {
		Projection p = e.getProjection();
		if (this.projID.equals(p.getProjectionID())) {
			return;
		} else {
			this.projID = p.getProjectionID();
		}
		float scale = p.getScale();
		DrawingAttributes da = this.getDrawingAttributes();
		Properties lyrProps = new Properties();
		lyrProps = this.getProperties(lyrProps);
    // lowResCoast.shp should always be there.  Replace with different
    // coastlines if the files exist.
    lyrProps.put("shapeFile", dataDir + "lowResCoast.shp");
    if (scale < 10000000f) {
      if (new File(dataDir + "medResCoast.shp").exists()) {
        lyrProps.put("shapeFile", dataDir + "medResCoast.shp");
      }
      if (scale < 2000000f) {
        if (new File(dataDir + "highResCoast.shp").exists()) {
          lyrProps.put("shapeFile", dataDir + "highResCoast.shp");
        }
      }
		}
		this.setProperties(lyrProps);
		this.setDrawingAttributes(da);
		super.projectionChanged(e);
	}
}
