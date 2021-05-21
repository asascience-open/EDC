/*
 * Applied Science Associates, Inc.
 * Copyright 2007. All rights reserved.
 */

/*
 * DcpsysLayerPanel.java
 *
 * Created on Apr 28, 2008, 2:12:36 PM
 *
 */
package com.asascience.openmap.ui;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.bbn.openmap.Layer;
import com.bbn.openmap.LayerHandler;
import com.bbn.openmap.event.LayerEvent;
import com.bbn.openmap.event.LayerListener;
import com.bbn.openmap.gui.BasicMapPanel;

import net.miginfocom.swing.MigLayout;

/**
 * 
 * @author CBM <cmueller@asascience.com>
 */
public class OMLayerPanel extends JPanel implements LayerListener {

  protected transient LayerHandler layerHandler = null;
  protected OMTimeSlider tlh;
  protected List<OMLayerPane> paneList;
  protected List<String> layerNames = new ArrayList<String>();

  /** Creates a new instance of DcpsysLayerPanel */
  public OMLayerPanel() {
    super(new MigLayout("inset 0"));
    this.setBorder(BorderFactory.createEtchedBorder());
    paneList = new ArrayList<OMLayerPane>();
  }

  public OMLayerPanel(BasicMapPanel omp, OMTimeSlider tlh) {
    this();
    setLayerHandler((LayerHandler) omp.getMapHandler().get(LayerHandler.class));
    setTimeLayerHandler(tlh);
    tlh.addPropertyChangeListener(new PropertyChangeListener() {

      public void propertyChange(PropertyChangeEvent evt) {
        String propName = evt.getPropertyName();
        if (propName.equals("layeronoff")) {
          String name = (String) evt.getOldValue();
          String cmd = (String) evt.getNewValue();

          for (OMLayerPane p : paneList) {
            if (p.getLayer().getName().equals(name)) {
              if (cmd.equals("on")) {
                p.layerVisibility(true);
              } else if (cmd.equals("off")) {
                p.layerVisibility(false);
              }
            }
          }
        }
      }
    });
  }

  public void setLayerHandler(LayerHandler lh) {
    if (layerHandler != null) {
      layerHandler.removeLayerListener(this);
    }

    layerHandler = lh;
    if (layerHandler != null) {
      layerHandler.addLayerListener(this);
    } else {
      setLayers(new Layer[0]);
    }

    // update layer panes
  }

  private void createPanes(Layer[] layers) {
    disposePanes();

    List<Layer> background = new ArrayList<Layer>();
    List<Layer> foreground = new ArrayList<Layer>();
    for (Layer l : layers) {
      if (l.getAddAsBackground()) {
        background.add(l);
      }
    }
    for (Layer l : layers) {
      if (!l.getAddAsBackground()) {
        foreground.add(l);
      }
    }

    for (Layer l : foreground) {
      this.add(createLayerPane(l), "growx, wrap");
    }

    if (foreground.size() > 0) {
      JLabel lblBgLrs = new JLabel("---Background Layers---");
      lblBgLrs.setHorizontalAlignment(JLabel.CENTER);
      this.add(lblBgLrs, "growx, wrap");
    }

    for (Layer l : background) {
      this.add(createLayerPane(l), "growx, wrap");
    }

    // for(Layer l : layers){
    // this.add(createLayerPane(l), "growx, wrap");
    // }
  }

  private void disposePanes() {
    // for(OMLayerPane p : paneList){
    // p.setCollapsed(true);
    // }
    paneList = new ArrayList<OMLayerPane>();
    this.removeAll();
    this.update(this.getGraphics());// This fixed the refresh issue of the
    // JScrollPane...
  }

  // abstract JPanel createLayerPane(Layer layer);
  protected JPanel createLayerPane(Layer layer) {
    OMLayerPane omp = new OMLayerPane(layer, layerHandler, tlh);
    // omp.addPropertyChangeListener(this);
    paneList.add(omp);
    return omp;
  }

  public void setLayers(Layer[] inLayers) {
    Layer[] layers = inLayers;
    if (inLayers == null) {
      layers = new Layer[0];
    }

    layerNames = new ArrayList<String>();
    for (Layer l : layers) {
      layerNames.add(l.getName());
    }
    // System.out.println("Create");
    createPanes(layers);

    if (this.getParent() != null) {
      this.getParent().validate();
    }
    // if(this.getTopLevelAncestor() != null){
    // Container c = this.getTopLevelAncestor();
    // c.repaint();
    // }
  }

  private void setTimeLayerHandler(OMTimeSlider tlh) {
    this.tlh = tlh;
  }

  public void setLayers(LayerEvent evt) {
    Layer[] layers = evt.getLayers();
    int type = evt.getType();
    if (type == LayerEvent.ALL) {

      boolean cont = false;
      if (layers.length != layerNames.size()) {
        cont = true;
      } else {
        for (int i = 0; i < layers.length; i++) {// maintains order of
          // layers
          if (!layerNames.get(i).equals(layers[i].getName())) {
            cont = true;
            break;
          }
        }

        // for(Layer l : layers){
        // if(!layerNames.contains(l.getName())){
        // cont = true;
        // break;
        // }
        // }
      }

      if (cont) {
        setLayers(layers);
      }
    }
  }
}
