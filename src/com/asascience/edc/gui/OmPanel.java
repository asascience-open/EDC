/*
 * Applied Science Associates, Inc.
 * Copyright 2007. All rights reserved.
 */

/*
 * OmPanel.java
 *
 * Created on Apr 30, 2008, 10:14:53 AM
 *
 */
package com.asascience.edc.gui;

import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asascience.openmap.mousemode.InformationMouseMode;
import com.asascience.openmap.mousemode.MeasureMouseMode;
import com.asascience.openmap.mousemode.NavMouseMode3;
import com.asascience.openmap.mousemode.PanMouseMode2;
import com.asascience.openmap.mousemode.TimeseriesMouseMode;
import com.asascience.openmap.mousemode.VectorInterrogationMouseMode;
import com.asascience.utilities.Utils;
import com.bbn.openmap.InformationDelegator;
import com.bbn.openmap.LayerHandler;
import com.bbn.openmap.MouseDelegator;
import com.bbn.openmap.gui.BasicMapPanel;
import com.bbn.openmap.gui.MouseModeButtonPanel;
import com.bbn.openmap.gui.OMToolSet;
import com.bbn.openmap.gui.ProjectionStackTool;
import com.bbn.openmap.gui.ToolPanel;
import com.bbn.openmap.layer.shape.ShapeLayer;
import com.bbn.openmap.omGraphics.event.StandardMapMouseInterpreter;
import com.bbn.openmap.proj.ProjectionStack;

/**
 * 
 * @author CBM <cmueller@asascience.com>
 */
public class OmPanel extends BasicMapPanel {

  final Logger logger = LoggerFactory.getLogger(OmPanel.class);
  private String userDir;
  private LayerHandler layerHandler;
  private OMToolSet omTools;
  private MouseDelegator mouseDelegator;
  private ToolPanel toolBar;
  private TimeseriesMouseMode tsmm;
  private VectorInterrogationMouseMode vimm;

  /**
   * Creates a new instance of DcpsysOmPanel
   *
   * @param userDir
   */
  public OmPanel(String userDir) {
    this.userDir = userDir;
    String dataDir = Utils.appendSeparator(userDir);

    layerHandler = new LayerHandler();
    mapHandler.add(layerHandler);

    omTools = new OMToolSet();
    toolBar = new ToolPanel();

    mapHandler.add(omTools);
    mapHandler.add(toolBar);

    mouseDelegator = new MouseDelegator(mapBean);
    mapHandler.add(mouseDelegator);

    mapHandler.add(new NavMouseMode3());
    mapHandler.add(new PanMouseMode2());
    mapHandler.add(new MeasureMouseMode());
    mapHandler.add(new InformationMouseMode());
    tsmm = new TimeseriesMouseMode();
    // mapHandler.add(tsmm);
    vimm = new VectorInterrogationMouseMode(true, true, false);
    mapHandler.add(vimm);

    mapHandler.add(new StandardMapMouseInterpreter());
    mapHandler.add(new MouseModeButtonPanel());
    mapHandler.add(new InformationDelegator());
    mapHandler.add(new ProjectionStack());
    mapHandler.add(new ProjectionStackTool());

    // BasemapLayer basemapLayer = new BasemapLayer(dataDir);

    ShapeLayer basemapLayer = new ShapeLayer();
    Properties lyrProps = new Properties();
    lyrProps.put("prettyName", "Basemap");
    lyrProps.put("lineColor", "000000");
    lyrProps.put("fillColor", "666666");// BDDE83
    lyrProps.put("shapeFile", dataDir + "lowResCoast.shp");
    basemapLayer.setProperties(lyrProps);
    basemapLayer.setAddAsBackground(true);
    basemapLayer.setVisible(true);

    layerHandler.addLayer(basemapLayer);
  }

  public LayerHandler getLayerHandler() {
    return layerHandler;
  }

  public TimeseriesMouseMode getTsmm() {
    return tsmm;
  }

  public VectorInterrogationMouseMode getVimm() {
    return vimm;
  }
}
