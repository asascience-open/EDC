/*
 * Applied Science Associates, Inc.
 * Copyright 2009. All Rights Reserved.
 *
 * AssetLayer.java
 *
 * Created on Jan 9, 2009 @ 11:36:09 AM
 */
package com.asascience.openmap.layer.nc.ncell;

import java.awt.Color;
import java.io.File;
import java.util.ArrayList;

import com.asascience.openmap.layer.VectorLayer;
import com.asascience.openmap.omgraphic.OMGridCell;
import com.asascience.openmap.utilities.MapUtils;
import com.bbn.openmap.util.DataBounds;

/**
 * 
 * @author CBM <cmueller@asascience.com>
 */
public class AssetLayer extends VectorLayer {

  private AssetReader assetReader = null;

  public AssetLayer(String dataFile) {
    super.drawVectors = false;
    super.drawGridCells = true;
    if (new File(dataFile).exists()) {
      assetReader = new AssetReader(dataFile);
      this.setSourceFilePath(dataFile);

      this.setTimeRange(assetReader.getStartTime(), assetReader.getEndTime());
      // this sets the timeIncrement in the TimeLayer
      this.setTimeIncrement(assetReader.getTimeIncrement());
      this.setTimes(assetReader.getTimeSteps());

      this.setName(dataFile.substring(dataFile.lastIndexOf(File.separator) + 1));
      this.setUVFillVal(assetReader.getFillValue());

      this.setVectorColor(Color.RED);

      this.setShowDisplayType(false);

      this.lats = assetReader.getLats();
      this.lons = assetReader.getLons();
    }
  }

  @Override
  public DataBounds getLayerExtent() {
    // this.setLats(assetReader.getLats());
    // this.setLons(assetReader.getLons());
    return super.getLayerExtent();
  }

  public void drawDataForTime() {
    drawDataForTime(this.getCurrentTime());
  }

  @Override
  public void drawDataForTime(long t) {
    // TODO Auto-generated method stub

    if (t == -1) {
      t = assetReader.getStartTime();
    }

    int[] goNoGoData = assetReader.getGoNoGoValues(t, "BathTub");

    buildGridCells(goNoGoData);

    this.display();
  }

  /** FIXME Add an overrideable method to VectorLayer. */
  protected void buildGridCells(int[] dataList) {
    gridCells = new ArrayList<OMGridCell>();
    double[] grdx;
    double[] grdy;
    int data;
    int fillVal = assetReader.getFillValue();
    boolean add;
    for (int i = 0; i < assetReader.getNCells(); i++) {
      try {
        if (dataList != null) {
          data = dataList[i];
        } else {
          data = fillVal;
        }
      } catch (Exception ex) {
        data = fillVal;
      }

      // if(data == fillVal){
      // continue;
      // }

      grdx = assetReader.getXGridForNcell(i);
      grdy = assetReader.getYGridForNcell(i);

      // make sure the grid is valid
      add = true;

      for (double d : grdx) {
        if (d == fillVal) {
          add = false;
          break;
        }
      }
      if (add) {
        for (double d : grdy) {
          if (d == fillVal) {
            add = false;
            break;
          }
        }
      }

      if (add) {
        Color cellColor = Color.BLACK;
        if (!Double.isNaN(data)) {
          switch (data) {
            case 1:// pass
              cellColor = Color.GREEN;
              break;
            case 2:// fail
              cellColor = Color.RED;
              break;
            default:
              break;
          }
          gridCells.add(new OMGridCell(null, MapUtils.buildFloatPolygonArray(grdy, grdx), data, cellColor,
                  false));
        }
      }
    }
  }
}
