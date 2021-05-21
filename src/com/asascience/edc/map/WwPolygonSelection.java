package com.asascience.edc.map;


import java.awt.Color;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.event.PositionEvent;
import gov.nasa.worldwind.event.PositionListener;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.render.Polyline;
import gov.nasa.worldwind.render.SurfacePolygon;
import ucar.unidata.geoloc.LatLonPointImpl;
import ucar.unidata.geoloc.LatLonRect;

public class WwPolygonSelection extends WorldWindMapControl {
	List<Position> positions;
	public static final String SELECTION_STARTED ="WwPolygon.Started";
	public static final String SELECTION_ADDED ="WwPolygon.PointAdded";
	public static final String SELECTION_FINISHED ="WwPolygon.Finished";
	private Polyline polygonLine;
	private SurfacePolygon polygon;
	private Position lastMousePos;
	private boolean finished;
	private List<LatLon> selectedLocs;
	private LatLonRect bbox;
	public WwPolygonSelection(WorldWindow wwd,
			RenderableLayer lineLayer) {
		super(wwd, lineLayer);
		polygonLine = new Polyline();
		polygonLine.setColor(Color.GREEN);
		polygonLine.setLineWidth(2);
		selectedLocs = new ArrayList<LatLon>();
		polygonLine.setFollowTerrain(true);
		positions = new ArrayList<Position>();
		polygon = new SurfacePolygon();
        polygon.setAttributes(atts);
	    this.layer.addRenderable(this.polygon);
	    this.layer.addRenderable(this.polygonLine);
	    finished = false;
		initPolygonListeners();
	}
	
	
	private void addCurrPosToPolygon(){
		 Position curPos = this.wwd.getCurrentPosition();
		    if (curPos == null) {
		      return;
		    }
		  positions.add(curPos);
		  addListToPositions(positions, null);
	}
	
	public void redrawWwMap(){
		this.wwd.redraw();
	}

	
	public LatLonRect getPolygonBBox(){
			  Path2D.Double polygon = new Path2D.Double();
			 Iterable<? extends LatLon> polygonLocs = this.polygon.getLocations();
			  Iterator polygonI = polygonLocs.iterator();
			  if(polygonI.hasNext()){
				  LatLon loc = (LatLon) polygonI.next();
				  polygon.moveTo(loc.latitude.degrees, loc.longitude.degrees);
				 while(polygonI.hasNext()){
					 loc = (LatLon) polygonI.next();
					  polygon.lineTo(loc.latitude.degrees, loc.longitude.degrees);

				  }
			  }
		Rectangle2D bounds =	  polygon.getBounds2D();
		LatLonPointImpl llpt = new LatLonPointImpl(bounds.getMinX(), bounds.getMinY());
		LatLonPointImpl urpt = new LatLonPointImpl(bounds.getMaxX(), bounds.getMaxY());

		
		this.bbox = new LatLonRect(llpt, urpt);
		return this.bbox;
	}
	private void addListToPositions(List<Position> posList, Position currMousePos){
		
		  if(currMousePos != null)
			  posList.add(currMousePos);
		  this.polygonLine.setPositions(posList);
		

		  this.wwd.redraw();
	      this.firePropertyChange(SELECTION_ADDED, null, true);
	      if(currMousePos != null)
			  posList.remove(currMousePos);
	}
	
	private void polylgonFinished(){
		active = false;
		lastMousePos = null;
		finished = true;
		this.polygon.setLocations(this.polygonLine.getPositions());;
		this.polygonLine.setPositions(new ArrayList<Position>());



		firePropertyChange(SELECTION_FINISHED, null, true);
	}
	
	
	private void initPolygonListeners(){


		this.wwd.getInputHandler().addMouseListener(new MouseAdapter() {

			@Override
			public void mousePressed(MouseEvent mouseEvent) {
				if (armed && mouseEvent.getButton() == MouseEvent.BUTTON1) {
					mouseEvent.consume();	
					if(mouseEvent.getClickCount() == 1) {
						active = true;
						lastMousePos =  null;
						if(finished && positions.size() > 0) {
							positions.clear();
							polygon.setLocations(new ArrayList<Position>());

							finished = false;
						}
						if(positions.size() == 0)
						      firePropertyChange(SELECTION_STARTED, null, true);

						addCurrPosToPolygon();
					}
					
				}
			}

			@Override
			public void mouseClicked(MouseEvent mouseEvent){
				if (armed && mouseEvent.getButton() == MouseEvent.BUTTON1) {

					mouseEvent.consume();
					if(mouseEvent.getClickCount() == 2) {
						polylgonFinished();

					}
				}
			}
			@Override
			public void mouseReleased(MouseEvent mouseEvent) {
				if (armed && mouseEvent.getButton() == MouseEvent.BUTTON1) {
					mouseEvent.consume();
				}
			}
		});

		this.wwd.addPositionListener(new PositionListener() {

			public void moved(PositionEvent event) {
				if (!active) {
					return;
				}
				if(armed && positions.size() > 0){
					lastMousePos = wwd.getCurrentPosition();
					addListToPositions(positions, lastMousePos);
					event.consume();

				}

			}
		});


	}

	public void clear(){
		positions.clear();
		polygonLine.setPositions(new ArrayList<Position>());
		polygon.setLocations(new ArrayList<Position>());
		lastMousePos = null;
		finished = false;
		this.wwd.redraw();
	}


	public List<Position> getPolygonPositions() {
		return positions;
	}


	public List<LatLon> getSelectedLocs() {
		return selectedLocs;
	}


	public void setSelectedLocs(List<LatLon> selectedLocs) {
		this.selectedLocs = selectedLocs;
	}




	
}
