package com.asascience.edc.map.view;

import com.asascience.edc.dap.map.DataExtentLayer;
import com.asascience.edc.dap.ui.DapWorldwindProcessPanel;
import com.asascience.edc.map.BoundingBox;
import com.asascience.edc.map.WwPolygonSelection;
import com.asascience.edc.map.WwTrackLineSelection;
import com.asascience.edc.map.view.SelectionMethodsPanel.ActiveSelectionSource;
import com.asascience.edc.sos.SensorContainer;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.Properties;

import javax.swing.JButton;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import net.miginfocom.swing.MigLayout;

import com.asascience.edc.sos.map.WorldwindSosLayer;
import com.asascience.utilities.Utils;
import com.bbn.openmap.LatLonPoint;
import com.bbn.openmap.MapBean;
import com.bbn.openmap.MapHandler;
import com.bbn.openmap.MouseDelegator;
import com.bbn.openmap.gui.OMToolSet;
import com.bbn.openmap.gui.ToolPanel;
import gov.nasa.worldwind.BasicModel;
import gov.nasa.worldwind.awt.WorldWindowGLCanvas;
import gov.nasa.worldwind.event.RenderingExceptionListener;
import gov.nasa.worldwind.event.SelectEvent;
import gov.nasa.worldwind.event.SelectListener;
import gov.nasa.worldwind.exception.WWAbsentRequirementException;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.globes.Earth;
import gov.nasa.worldwind.globes.EarthFlat;
import gov.nasa.worldwind.layers.CompassLayer;
import gov.nasa.worldwind.layers.Layer;
import gov.nasa.worldwind.layers.LayerList;
import gov.nasa.worldwind.layers.SkyColorLayer;
import gov.nasa.worldwind.layers.SkyGradientLayer;
import gov.nasa.worldwind.layers.ViewControlsLayer;
import gov.nasa.worldwind.layers.ViewControlsSelectListener;
import gov.nasa.worldwind.render.PointPlacemark;
import gov.nasa.worldwind.render.SurfaceSquare;
import gov.nasa.worldwind.view.orbit.BasicOrbitView;
import gov.nasa.worldwind.view.orbit.BasicOrbitViewLimits;
import gov.nasa.worldwind.view.orbit.FlatOrbitView;
import gov.nasa.worldwind.view.orbit.OrbitViewLimits;
import java.util.List;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.border.EtchedBorder;

import ucar.unidata.geoloc.LatLonPointImpl;
import ucar.unidata.geoloc.LatLonRect;

/**
 * WorldwindSelectionMap.java
 * 
 * @author Kyle Wilcox <kwilcox@asascience.com>
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
  protected String dataDir;
  protected MapHandler mHandler;
  private WorldwindSosLayer sensorLayer;
  private DataExtentLayer dataExtentLayer;
  protected MapBean mBean;
  protected Properties lyrProps;
  protected OMToolSet tools;
  protected ToolPanel toolPanel;
  protected MouseDelegator mouseDelegator;
  protected boolean showTrackLineOptions = false;
  private WorldWindowGLCanvas mapCanvas;
  private JButton toggleViewButton;
  private String LABEL_2D = "2D";
  private String LABEL_3D = "3D";
  private  JPanel toolbar; 
  private SelectionMethodsPanel locationSelectionTool;
  
  /**
   * Creates a new instance of WorldwindSelectionMap
   *
   * @param dataDir
   * @param showAOIButton
   */
  public WorldwindSelectionMap(String dataDir, boolean showTrackLineOptions) {
    this.dataDir = Utils.appendSeparator(dataDir);
    this.showTrackLineOptions = showTrackLineOptions;
    pcs = new PropertyChangeSupport(this);

    initComponents();
  }

  public WorldwindSelectionMap(String dataDir) {
    this(dataDir, false);
  }
  public void reInitComponents(){
	  //  add(mapCanvas, "gap 0, grow, wrap");
	  //  add(toolbar, "gap 0, growx");
	  dataExtentLayer = null;
	  initComponents();
  }
  private void initComponents() {
    setLayout(new MigLayout("gap 0, fill"));
    setBorder(new EtchedBorder());

    mapCanvas = new WorldWindowGLCanvas();
    mapCanvas.setModel(new BasicModel());
    // Select the stations by clicking
    mapCanvas.addSelectListener(new SelectListener() {

      public void selected(SelectEvent event) {
        if (event.getEventAction().equals(SelectEvent.LEFT_CLICK)) {
          if (event.getTopObject() instanceof PointPlacemark) {
            pcs.firePropertyChange("clicked", null, event.getTopObject());
            event.consume();
          }
        }
      }
    });

    // Create and install the view controls layer and register a controller for it with the World Window.
    ViewControlsLayer viewControlsLayer = new ViewControlsLayer();
    LayerList layers = mapCanvas.getModel().getLayers();
    int compassPosition = 0;
    for (Layer l : layers) {
      if (l instanceof CompassLayer) {
        compassPosition = layers.indexOf(l);
      }
    }
    layers.add(compassPosition, viewControlsLayer);
    mapCanvas.addSelectListener(new ViewControlsSelectListener(mapCanvas, viewControlsLayer));

    // Register a rendering exception listener that's notified when exceptions occur during rendering.
    mapCanvas.addRenderingExceptionListener(new RenderingExceptionListener() {

      public void exceptionThrown(Throwable t) {
        if (t instanceof WWAbsentRequirementException) {
          String message = "Computer does not meet minimum graphics requirements.\n";
          message += "Please install up-to-date graphics driver and try again.\n";
          message += "Reason: " + t.getMessage() + "\n";
          message += "This program will end when you press OK.";

          JOptionPane.showMessageDialog(WorldwindSelectionMap.this, message, "Unable to Start Program",
                  JOptionPane.ERROR_MESSAGE);
          System.exit(-1);
        }
      }
    });

    // Default view is Globe
    makeGlobe();
    //makeFlat();

    add(mapCanvas, "gap 0, grow, wrap");

    // Toolbar
    toolbar = new JPanel(new MigLayout("gap 0, fill"));

    toggleViewButton = new JButton();
    toggleViewButton.setText(getButtonLabel());
    toggleViewButton.addActionListener(new ActionListener() {

      public void actionPerformed(ActionEvent e) {
        toggleView();
      }
    });

    toolbar.add(toggleViewButton);

    locationSelectionTool = new SelectionMethodsPanel(mapCanvas, this.showTrackLineOptions);
    locationSelectionTool.addPropertyChangeListener(SelectionMethodsPanel.BOUNDS_STORED,
    		new PropertyChangeListener() {

      public void propertyChange(PropertyChangeEvent evt) {
        if (sensorLayer != null){
        	if(locationSelectionTool.getActiveSelectionSource() == ActiveSelectionSource.BBOX) 
        		sensorLayer.setPickedByBBOX(locationSelectionTool.getBBOX());
   
        }
        pcs.firePropertyChange(evt);
      }
    });
    locationSelectionTool.addPropertyChangeListener(DapWorldwindProcessPanel.DISABLE_BBOX,
    		new PropertyChangeListener(){

		@Override
		public void propertyChange(PropertyChangeEvent evt) {
			  pcs.firePropertyChange(evt);
		}

    });
    locationSelectionTool.addPropertyChangeListener(DapWorldwindProcessPanel.DISABLE_SLIDER,
    		new PropertyChangeListener(){

		@Override
		public void propertyChange(PropertyChangeEvent evt) {
			System.out.println("Passing on slider event");
			  pcs.firePropertyChange(evt);
		}

    });
    locationSelectionTool.addPropertyChangeListener(WwPolygonSelection.SELECTION_ADDED,
    		new PropertyChangeListener(){

		@Override
		public void propertyChange(PropertyChangeEvent evt) {
				updateSelectedSosFromTrack();
			  pcs.firePropertyChange(evt);
		}

    });
    locationSelectionTool.addPropertyChangeListener(WwTrackLineSelection.TRACK_PT_ADDED, 
    		new PropertyChangeListener(){

				@Override
				public void propertyChange(PropertyChangeEvent event) {
					pcs.firePropertyChange(event);
				}
    	
    });
 
    toolbar.add(locationSelectionTool);

    add(toolbar, "gap 0, growx");

  }

  private void updateSelectedSosFromTrack(){
	  if (sensorLayer != null) {
		  List<LatLon> selectedLocs;
		  if(locationSelectionTool.getActiveSelectionSource() == ActiveSelectionSource.TRACK_LINE)
			  selectedLocs = sensorLayer.setPickedByTrackLine(
					  locationSelectionTool.getTrackLinePositions(), 
					  locationSelectionTool.getTrackSel().getTrackLineWidth());
		  else
			  selectedLocs = sensorLayer.setPickedByPolygon(locationSelectionTool.getTrackLinePositions());
		  locationSelectionTool.setSelectedLocsFromTrackLine(selectedLocs);
	  }
	  
  }
  
  public void makeGlobeVisible(boolean visible){
	  mapCanvas.setVisible(visible);
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
    mapCanvas.redraw();
    toggleViewButton.setText(getButtonLabel());
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
    mapCanvas.getModel().getLayers().add(1, sensorLayer);
    sensorLayer.setSensors(sensorList);
    Position eyePosition = sensorLayer.getEyePosition();
    if(eyePosition != null)
    	mapCanvas.getView().setEyePosition(eyePosition);
    else
    	mapCanvas.getView().setEyePosition(Position.fromDegrees(0,0));
    
  }

  public void toggleSensor(PointPlacemark sensor) {
    sensorLayer.toggleSensor(sensor);
    mapCanvas.repaint();
  }

  private void makeFlat() {
    FlatOrbitView fov = new FlatOrbitView();
    OrbitViewLimits ovl = fov.getOrbitViewLimits();
    ovl.setZoomLimits(0, 11e7);
    ovl.setCenterLocationLimits(Sector.FULL_SPHERE);
    ovl.setPitchLimits(Angle.ZERO, Angle.ZERO);
    BasicOrbitViewLimits.applyLimits(fov, ovl);
    Position eyePosition = mapCanvas.getView().getCurrentEyePosition();
    if(eyePosition != null)
    	fov.setEyePosition(eyePosition);
    else
    	mapCanvas.getView().setEyePosition(Position.fromDegrees(0,0));
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

  public List<LatLonRect> getSelectedExtent() {
	  List<LatLonRect> latLonBboxList = new ArrayList<LatLonRect>();
	  
	  switch(locationSelectionTool.getActiveSelectionSource()){
	  case BBOX:
	  case POLYGON:
		  if (locationSelectionTool.getBBOX() != null) {
			  latLonBboxList.add(locationSelectionTool.getBBOX());
		  }
		  break;
	  case TRACK_LINE:
		  for(BoundingBox bb : locationSelectionTool.getTrackSel().getTrackPointVertices()){
			 LatLonPointImpl right = new LatLonPointImpl(bb.getLowerRight().latitude.degrees,
					 								 bb.getLowerRight().longitude.degrees);
			 LatLonPointImpl left = new LatLonPointImpl(bb.getUpperLeft().latitude.degrees,
						 bb.getUpperLeft().longitude.degrees);
			 LatLonRect rect = new LatLonRect(left, right);
			 latLonBboxList.add(rect);
		  }
		  
		  break;
	  }
    return latLonBboxList;
  }


  public List<LatLonPointImpl> getSelectedVertices(){
	  List<LatLonPointImpl> polygonVertices = new ArrayList<LatLonPointImpl>();
	  if(locationSelectionTool.getActiveSelectionSource() == ActiveSelectionSource.POLYGON){
		 List<Position> polygonPositions =  locationSelectionTool.getPolygonSel().getPolygonPositions();
		 for(Position pos1 : polygonPositions){
			 LatLonPointImpl ll1 = new LatLonPointImpl(pos1.getLatitude().degrees, pos1.getLongitude().degrees);
			 polygonVertices.add(ll1);
		 }
	  }
		  
	  
	  return polygonVertices;
  }
  
  public void makeDataExtentLayer(LatLonRect llr){
	  makeDataExtentLayer(llr, true, false);
  }
  
  
  public void makeDataExtentLayer(LatLonRect llr, boolean dataExtentFound, boolean is360) {
    if (dataExtentLayer == null) {
      dataExtentLayer = new DataExtentLayer(mapCanvas.getModel().getGlobe());
      mapCanvas.getModel().getLayers().add(1, dataExtentLayer);
    }
    if(dataExtentFound)
    	dataExtentLayer.setDataExtent(llr, is360);
    Position eyePosition = dataExtentLayer.getEyePosition();
    if(eyePosition != null) 
    	mapCanvas.getView().setEyePosition(eyePosition);
    else
    	mapCanvas.getView().setEyePosition(Position.fromDegrees(0,0));
    mapCanvas.redraw();
    
    
  }

  public void makeSelectedExtentLayer(LatLonRect llr) {
    locationSelectionTool.setBBOX(llr);
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

public SelectionMethodsPanel getLocationSelectionTool() {
	return locationSelectionTool;
}

public WorldWindowGLCanvas getMapCanvas() {
	return mapCanvas;
}
}
