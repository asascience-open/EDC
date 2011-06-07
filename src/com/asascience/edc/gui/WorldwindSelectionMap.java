/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.asascience.edc.gui;

import com.asascience.edc.sos.SensorContainer;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.Properties;

import javax.swing.JButton;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;

import net.miginfocom.swing.MigLayout;

import com.asascience.openmap.utilities.GeoConstraints;
import com.asascience.edc.sos.map.WorldwindSosLayer;
import com.asascience.edc.sos.ui.SosWorldwindBoundingBoxTool;
import com.asascience.utilities.Utils;
import com.bbn.openmap.MapBean;
import com.bbn.openmap.MapHandler;
import com.bbn.openmap.MouseDelegator;
import com.bbn.openmap.gui.OMToolSet;
import com.bbn.openmap.gui.ToolPanel;
import gov.nasa.worldwind.BasicModel;
import gov.nasa.worldwind.awt.WorldWindowGLCanvas;
import gov.nasa.worldwind.event.SelectEvent;
import gov.nasa.worldwind.event.SelectListener;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.globes.Earth;
import gov.nasa.worldwind.globes.EarthFlat;
import gov.nasa.worldwind.layers.LayerList;
import gov.nasa.worldwind.layers.SkyColorLayer;
import gov.nasa.worldwind.layers.SkyGradientLayer;
import gov.nasa.worldwind.render.PointPlacemark;
import gov.nasa.worldwind.view.orbit.BasicOrbitView;
import gov.nasa.worldwind.view.orbit.BasicOrbitViewLimits;
import gov.nasa.worldwind.view.orbit.FlatOrbitView;
import gov.nasa.worldwind.view.orbit.OrbitViewLimits;
import java.util.List;
import javax.swing.JPanel;
import javax.swing.border.EtchedBorder;
import ucar.unidata.geoloc.LatLonRect;

/**
 *
 * @author Kyle
 */
public class WorldwindSelectionMap extends JPanel implements PropertyChangeListener {

  public static final String AOI_CLEAR = "aoiclear";
  public static final String AOI_SAVE = "aoisave";
  public static final String AOI_APPLY = "aoiapply";
  public static final String AOI_REMALL = "aoiremall";
  public static final String AOI_MANUAL = "aoimanual";
  protected JPopupMenu aoiMenu;
  protected JButton aoiButton;
  protected JMenuItem addAoi;
  protected JMenuItem useAoi;
  protected JMenuItem clearAoi;
  protected JMenuItem remAllAoi;
  protected JMenuItem manEntryAoi;
  protected PropertyChangeSupport pcs;
  protected GeoConstraints geoCons;
  protected String dataDir;
  protected MapHandler mHandler;
  private WorldwindSosLayer sensorLayer;
  protected MapBean mBean;
  protected Properties lyrProps;
  protected OMToolSet tools;
  protected ToolPanel toolPanel;
  protected MouseDelegator mouseDelegator;
  protected boolean showAOIButton = false;
  private WorldWindowGLCanvas mapCanvas;
  private JButton toggleViewButton;
  private String LABEL_2D = "2D";
  private String LABEL_3D = "3D";
  private SosWorldwindBoundingBoxTool polygonTool;

  /**
   * Creates a new instance of OMSelectionMapPanel
   *
   * @param cons
   * @param dataDir
   * @param showAOIButton
   */
  public WorldwindSelectionMap(GeoConstraints cons, String dataDir, boolean showAOIButton) {
    this.dataDir = Utils.appendSeparator(dataDir);
    this.showAOIButton = showAOIButton;
    geoCons = cons;
    if (geoCons == null) {
      System.err.println("geoCons == null");
    }

    pcs = new PropertyChangeSupport(this);

    initComponents();
  }

  public WorldwindSelectionMap(GeoConstraints cons, String dataDir) {
    this(cons, dataDir, false);
  }

  private void initComponents() {
    setLayout(new MigLayout("gap 0, fill"));
    setBorder(new EtchedBorder());

    mapCanvas = new WorldWindowGLCanvas();
    mapCanvas.setModel(new BasicModel());
    // Select the stations by clicking
    mapCanvas.addSelectListener(new SelectListener(){
      public void selected(SelectEvent event){
        if (event.getEventAction().equals(SelectEvent.LEFT_CLICK)) {
          if (event.getTopObject() instanceof PointPlacemark) {
            pcs.firePropertyChange("clicked",null,event.getTopObject());
          }
        }
      }}
    );

    // Default view is Globe
    makeGlobe();

    add(mapCanvas, "gap 0, grow, wrap");

    // Toolbar
    JPanel toolbar = new JPanel(new MigLayout("gap 0, fill"));

    toggleViewButton = new JButton();
    toggleViewButton.setLabel(getButtonLabel());
    toggleViewButton.addActionListener(new ActionListener() {

      public void actionPerformed(ActionEvent e) {
        toggleView();
      }
    });

    toolbar.add(toggleViewButton);

    polygonTool = new SosWorldwindBoundingBoxTool(mapCanvas);
    polygonTool.addPropertyChangeListener("boundsStored",new PropertyChangeListener() {

      public void propertyChange(PropertyChangeEvent evt) {
        sensorLayer.setPickedByBBOX(polygonTool.getBBOX());
        pcs.firePropertyChange(evt);
      }
    });
    toolbar.add(polygonTool);

    add(toolbar, "gap 0, growx");

    // <editor-fold defaultstate="collapsed" desc="AOI Menu">
    // if desired, add the AOI Menu Button to the toolbar
    if (showAOIButton) {
      // create an action listener for the AOI menu items
      ActionListener menuActionListener = new ActionListener() {

        public void actionPerformed(ActionEvent e) {
          if (e.getActionCommand().equals(AOI_SAVE)) {
            pcs.firePropertyChange(AOI_SAVE, false, true);
          } else if (e.getActionCommand().equals(AOI_APPLY)) {
            pcs.firePropertyChange(AOI_APPLY, false, true);
          } else if (e.getActionCommand().equals(AOI_CLEAR)) {
            pcs.firePropertyChange(AOI_CLEAR, false, true);
          } else if (e.getActionCommand().equals(AOI_REMALL)) {
            pcs.firePropertyChange(AOI_REMALL, false, true);
          } else if (e.getActionCommand().equals(AOI_MANUAL)) {
            pcs.firePropertyChange(AOI_MANUAL, false, true);
          }
        }
      };

      // make the AOI menu items
      aoiMenu = new JPopupMenu("AOIs");
      addAoi = new JMenuItem("Save Current AOI...");
      addAoi.setActionCommand(AOI_SAVE);
      addAoi.addActionListener(menuActionListener);

      useAoi = new JMenuItem("Apply Existing AOI...");
      useAoi.setActionCommand(AOI_APPLY);
      useAoi.addActionListener(menuActionListener);

      clearAoi = new JMenuItem("Clear Current AOI");
      clearAoi.setActionCommand(AOI_CLEAR);
      clearAoi.addActionListener(menuActionListener);

      remAllAoi = new JMenuItem("Clear AOI List");
      remAllAoi.setActionCommand(AOI_REMALL);
      remAllAoi.addActionListener(menuActionListener);

      manEntryAoi = new JMenuItem("Enter an AOI Maually...");
      manEntryAoi.setActionCommand(AOI_MANUAL);
      manEntryAoi.addActionListener(menuActionListener);

      // construct the AOI menu
      aoiMenu.add(addAoi);
      aoiMenu.add(useAoi);
      aoiMenu.add(manEntryAoi);
      aoiMenu.add(new JSeparator());
      aoiMenu.add(clearAoi);
      aoiMenu.add(new JSeparator());
      aoiMenu.add(remAllAoi);

      // attach the AOI menu to a button on the toolbar
      aoiButton = new JButton("AOIs");
      aoiButton.addActionListener(new ActionListener() {

        public void actionPerformed(ActionEvent ae) {
          if (polygonTool.getBBOX() != null) {
            addAoi.setEnabled(true);
            clearAoi.setEnabled(true);
          } else {
            addAoi.setEnabled(false);
            clearAoi.setEnabled(false);
          }

          aoiMenu.show(aoiButton, 0, aoiButton.getHeight());
        }
      });

      toolPanel.add(aoiButton);
    }

    // </editor-fold>

  }

  private String getButtonLabel() {
    if (mapCanvas.getModel().getGlobe() instanceof Earth) {
      return LABEL_2D;
    } else {
      return LABEL_3D;
    }
  }

  private void toggleView() {
    if (getButtonLabel().equals(LABEL_2D)) {
      makeFlat();
    } else {
      makeGlobe();
    }
    toggleViewButton.setLabel(getButtonLabel());
  }

  public WorldwindSosLayer getSensorLayer() {
    return sensorLayer;
  }

  public void addSensors(List<SensorContainer> sensorList) {
    sensorLayer = new WorldwindSosLayer();
    sensorLayer.addPropertyChangeListener(new PropertyChangeListener() {

      public void propertyChange(PropertyChangeEvent evt) {
        pcs.firePropertyChange("sensorsloaded", false, true);
      }
    });
    mapCanvas.getModel().getLayers().add(sensorLayer);
    sensorLayer.setSensors(sensorList);
    mapCanvas.getView().setEyePosition(sensorLayer.getEyePosition());
  }

  public void toggleSensor(PointPlacemark sensor) {
    sensorLayer.toggleSensor(sensor);
    mapCanvas.repaint();
  }

  private void makeFlat() {
    FlatOrbitView fov = new FlatOrbitView();
    OrbitViewLimits ovl = fov.getOrbitViewLimits();
    ovl.setZoomLimits(0, 20e6);
    ovl.setCenterLocationLimits(Sector.FULL_SPHERE);
    ovl.setPitchLimits(Angle.ZERO, Angle.ZERO);
    BasicOrbitViewLimits.applyLimits(fov, ovl);
    fov.setEyePosition(mapCanvas.getView().getCurrentEyePosition());

    mapCanvas.getModel().setGlobe(new EarthFlat());
    mapCanvas.setView(fov);

    LayerList layers = mapCanvas.getModel().getLayers();
    for (int i = 0; i < layers.size(); i++) {
      if (layers.get(i) instanceof SkyGradientLayer) {
        layers.set(i, new SkyColorLayer());
      }
    }
  }

  public void makeGlobe() {
    BasicOrbitView bov = new BasicOrbitView();
    bov.setEyePosition(mapCanvas.getView().getCurrentEyePosition());

    mapCanvas.getModel().setGlobe(new Earth());
    mapCanvas.setView(bov);

    LayerList layers = mapCanvas.getModel().getLayers();
    for (int i = 0; i < layers.size(); i++) {
      if (layers.get(i) instanceof SkyColorLayer) {
        layers.set(i, new SkyGradientLayer());
      }
    }
  }

  public LatLonRect getSelectedExtent() {
    if (polygonTool.getBBOX() != null) {
      return polygonTool.getBBOX();
    }
    return null;
  }
  
  public void makeSelectedExtentLayer(LatLonRect llr) {
    polygonTool.setBBOX(llr);
  }

  public void propertyChange(PropertyChangeEvent evt) {
    pcs.firePropertyChange(evt);// pass the event along to the calling class
  }

  @Override
  public void addPropertyChangeListener(PropertyChangeListener l) {
    pcs.addPropertyChangeListener(l);
  }

  @Override
  public void removePropertyChangeListener(PropertyChangeListener l) {
    pcs.removePropertyChangeListener(l);
  }
}
