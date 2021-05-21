/*
 * Applied Science Associates, Inc.
 * Copyright 2008. All Rights Reserved.
 *
 * GroupLayer.java
 *
 * Created on Dec 8, 2008 @ 12:43:18 PM
 */
package com.asascience.openmap.layer;

import java.awt.Component;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.swing.JCheckBox;
import javax.swing.JPanel;

import com.asascience.openmap.ui.OMLayerPane;
import com.bbn.openmap.Layer;

import net.miginfocom.swing.MigLayout;

/**
 * 
 * @author CBM <cmueller@asascience.com>
 */
public class GroupLayer extends TimeLayer {

  private HashMap<String, OMLayerPane> layerMap;
  protected transient JPanel box;
  protected JCheckBox cbVis;

  public GroupLayer() {
    layerMap = new HashMap<String, OMLayerPane>();
  }

  public GroupLayer(OMLayerPane[] layerPanes) {
    super();
    for (OMLayerPane l : layerPanes) {
      addLayerPane(l);
    }
  }

  @Override
  public void drawDataForTime(long t) {
    Iterator<String> iter = layerMap.keySet().iterator();
    TimeLayer timeLayer;
    Layer l;
    while (iter.hasNext()) {
      l = layerMap.get(iter.next()).getLayer();
      if (l instanceof TimeLayer) {
        timeLayer = (TimeLayer) l;
        timeLayer.drawDataForTime(t);
      }
    }
  }

  // public synchronized OMGraphicList prepare() {
  // // OMGraphicList list = this.getList();
  // // list.clear();
  //
  // Iterator<String> iter = layerMap.keySet().iterator();
  // OMLayerPane pane;
  // OMGraphicHandlerLayer omghLayer;
  // Iterator<OMGraphic> listIter;
  // while(iter.hasNext()) {
  // pane = layerMap.get(iter.next());
  // if(pane.getLayer() instanceof OMGraphicHandlerLayer) {
  // omghLayer = (OMGraphicHandlerLayer) pane.getLayer();
  // if (omghLayer.isVisible()) {
  // omghLayer.doPrepare();
  // // listIter = omghLayer.getList().iterator();
  // // while (listIter.hasNext()) {
  // // list.add(listIter.next());
  // // }
  // }
  // }
  // }
  // // list.generate(this.getProjection());
  //
  // return null;
  // }
  public void addLayerPane(OMLayerPane layerPane) {
    if (layerPane != null && layerPane.getLayer() != null) {
      Layer l = layerPane.getLayer();
      if (l != null && l.getName() != null && !l.getName().equals("")) {
        if (!layerMap.containsKey(l.getName())) {
          if (l instanceof TimeLayer) {
            TimeLayer tl = (TimeLayer) l;
            updateLayerTimeProps(tl.getTimeIncrement(), tl.getStartTime(), tl.getEndTime(), tl.getUniqueTimes());
          }
          layerMap.put(l.getName(), layerPane);
        }
      }
    }
  }

  public void updateLayerTimeProps(long timeInc, long startTime, long endTime, List<Long> uniqueTimes) {
    if (this.getTimeIncrement() == 0 || this.getTimeIncrement() > timeInc) {
      this.setTimeIncrement(timeInc);
    }

    if (startTime < this.getStartTime()) {
      this.setStartTime(startTime);
    }

    if (endTime > this.getEndTime()) {
      this.setEndTime(endTime);
    }

    List<Long> ut = this.getUniqueTimes();
    for (long t : uniqueTimes) {
      if (!ut.contains(t)) {
        ut.add(t);
      }
    }
  }

  public HashMap<String, OMLayerPane> getLayerMap() {
    return layerMap;
  }

  public OMLayerPane[] getLayerPanes() {
    return layerMap.values().toArray(new OMLayerPane[0]);
  }

  public Layer[] getLayers() {
    Layer[] ret = new Layer[layerMap.size()];
    Iterator<String> iter = layerMap.keySet().iterator();
    OMLayerPane pane;
    int i = 0;
    while (iter.hasNext()) {
      pane = layerMap.get((iter.next()));
      ret[i] = pane.getLayer();
      i++;
    }
    return ret;
  }

  public void setVisibility(boolean bool) {
    Iterator<String> iter = layerMap.keySet().iterator();
    OMLayerPane pane;
    while (iter.hasNext()) {
      pane = layerMap.get((iter.next()));
      pane.layerVisibility(bool);
    }
  }

  @Override
  public Component getGUI() {
    if (box == null) {
      box = new JPanel(new MigLayout("", "[left]", ""));
      // cbVis = new JCheckBox("Visibility");
      // cbVis.addActionListener(new ActionListener() {
      //
      // public void actionPerformed(ActionEvent e) {
      // setVisibility(cbVis.isSelected());
      // }
      //
      // });
      // cbVis.setSelected(true);
      // box.add(cbVis, "wrap");

      Iterator<String> iter = layerMap.keySet().iterator();
      OMLayerPane pane;
      while (iter.hasNext()) {
        pane = layerMap.get((iter.next()));
        box.add(pane, "wrap");
      }
    }

    return box;
  }
}
