package com.asascience.edc.map;

import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.event.PositionEvent;
import gov.nasa.worldwind.event.PositionListener;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.render.PointPlacemark;
import gov.nasa.worldwind.render.PointPlacemarkAttributes;
import gov.nasa.worldwind.render.SurfaceCircle;
import gov.nasa.worldwind.render.SurfacePolyline;
import gov.nasa.worldwind.render.SurfaceSquare;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.swing.JFileChooser;

import com.asascience.edc.dap.ui.DapWorldwindProcessPanel;
import com.asascience.edc.utils.EdcDateUtils;



public class WwTrackLineSelection extends WorldWindMapControl {
	List<SurfaceSquare> trackPointList;
	List<BoundingBox> trackPointVertices;
	List<TrackLineVertex> trackConnectorModel;
	List<Position> trackConnectorLocs;
	SurfacePolyline trackLineConnector;
	boolean trackPtsFromImport;
	boolean lineFinished;
	public static final String TRACK_PT_ADDED = "WwTrackLineSelection.trackPointAdded";
	public static final String TRACK_PT_FINISHED = "WwTrackLineSelection.trackPointFinished";
	public static final String TRACK_PT_STARTED = "WwTrackLineSelection.trackPointStarted";
	public static final String TRACK_RADIUS_UPDATED ="WwTrackLineSelection.trackRadiusUpdated";
	private Double trackLineRadius;
	public WwTrackLineSelection(WorldWindow wwd,
			RenderableLayer lineLayer) {
		super(wwd, lineLayer);
		trackLineRadius = 10.0;
		lineFinished = false;
		trackPtsFromImport = false;
		trackPointList = new ArrayList<SurfaceSquare>();
		trackPointVertices = new ArrayList<BoundingBox>();
		trackLineConnector = new SurfacePolyline();
		trackConnectorLocs = new ArrayList<Position>();
		trackConnectorModel = new ArrayList<TrackLineVertex>();
		trackLineConnector.setClosed(false);
		initTrackListeners();

	}
	
	public void clearTrackPoints(){
		for(SurfaceSquare trackPt : trackPointList){
			this.layer.removeRenderable(trackPt);
			this.layer.removeRenderable(trackLineConnector);
		}
		trackPointList.clear();
		trackPointVertices.clear();
		trackConnectorLocs.clear();
		trackConnectorModel.clear();
		
	}
	
	

	public void addCurrPosToTrackPt(){
		Position curPos = this.wwd.getCurrentPosition();
		if (curPos == null) {
			return;
		}
		addPosToTrackPt(curPos, null, null);
		this.firePropertyChange(TRACK_PT_ADDED, false, true);


	}
	
	private void addPosToTrackPt(Position currPos, Date startDate, Date endDate){
		// convert track radius from km to m
				SurfaceSquare trackPoint = new SurfaceSquare(currPos, this.trackLineRadius*1000);

				BoundingBox trackPtBbox = getTrackPtBBox(trackPoint);
				if(trackPtBbox != null)
					trackPointVertices.add(trackPtBbox);
				
				trackPointList.add(trackPoint);
				TrackLineVertex trackVertex = new TrackLineVertex(currPos, trackPtBbox,
																  startDate, endDate,
																  trackPoint);
				this.trackConnectorModel.add(trackVertex);
				this.trackConnectorLocs.add(trackVertex.getTrackPt());
				this.trackLineConnector.setLocations(trackConnectorLocs);
				this.layer.addRenderable(trackPoint);
				if(trackConnectorLocs.size() == 2){
					this.layer.addRenderable(trackLineConnector);
				}
	}
	
	private BoundingBox getTrackPtBBox(SurfaceSquare trackPt){
		BoundingBox trackPtBBox = null;
	
		Iterable<? extends LatLon> bboxLocs = trackPt.getLocations(wwd.getModel().getGlobe());
	
		if(bboxLocs != null){
			trackPtBBox = new BoundingBox();
		Double minLat = null;
		Double minLon = null;
		Double maxLat = null;
		Double maxLon = null;
		for(LatLon pt: bboxLocs){
			Double latDeg = pt.getLatitude().degrees;
			Double lonDeg = pt.getLongitude().degrees;
			if(minLat == null || latDeg < minLat )
				minLat = pt.getLatitude().degrees;
			if(maxLat == null || latDeg > maxLat)
				maxLat = pt.getLatitude().degrees;
			if(minLon == null || lonDeg < minLon)
				minLon = lonDeg;
			if(maxLon == null || lonDeg > maxLon)
				maxLon = lonDeg;				
		}
		if(minLon != null && maxLon != null &&
		   minLat != null && maxLat != null){
			LatLon ul = LatLon.fromDegrees(maxLat, minLon);
			LatLon lr = LatLon.fromDegrees(minLat, maxLon);
			trackPtBBox.lowerRight = new Position(lr, 0);
			trackPtBBox.upperLeft = new Position(ul, 0);
			
			
		}
		   
		}
		return trackPtBBox;
	}

	
	public Double getTrackLineWidth() {
		return trackLineRadius;
	}


	public void setTrackLineWidth(Double trackLineWidth) {
		if(this.trackLineRadius != trackLineWidth){
			this.trackLineRadius = trackLineWidth;
			updateTrackRadius();
			this.firePropertyChange(TRACK_RADIUS_UPDATED, false, true);
		}
	}
	
	public TrackLineVertex getTrackLineVertexFromSquare(SurfaceSquare square){
		TrackLineVertex vertex = null;
		for(TrackLineVertex currVertex: this.trackConnectorModel){
			if(currVertex.square == square){
				vertex = currVertex;
				break;
			}
		}
		return vertex;
	}
	
	public void updateTrackRadius(){
		trackPointVertices.clear();
		
		
		
		double newRadius = this.trackLineRadius * 1000;
		for(SurfaceSquare surfaceSquare : this.trackPointList){
			TrackLineVertex trackVertex = this.getTrackLineVertexFromSquare(surfaceSquare);
			surfaceSquare.setHeight(newRadius);
			surfaceSquare.setWidth(newRadius);
			
			BoundingBox trackPtBbox = getTrackPtBBox(surfaceSquare);
			if(trackPtBbox != null)
				trackPointVertices.add(trackPtBbox);
			if(trackVertex != null)
				trackVertex. initLatLonRect(trackPtBbox);
		}


		this.wwd.redraw();

	}	

	public void trackLineFinished(){
		lineFinished = true;
		this.firePropertyChange(TRACK_PT_FINISHED, false, true);
	}
	
	
	public List<SurfaceSquare> getTrackPoints(){
		return trackPointList;
	}
	
	private void initTrackListeners(){


		this.wwd.getInputHandler().addMouseListener(new MouseAdapter() {

			@Override
			public void mousePressed(MouseEvent mouseEvent) {
				if (armed && mouseEvent.getButton() == MouseEvent.BUTTON1) {
					mouseEvent.consume();	
					if(mouseEvent.getClickCount() == 1) {
						
						if(trackPointVertices.isEmpty() || trackPtsFromImport || lineFinished)
							firePropertyChange(TRACK_PT_STARTED, false, true);
						if(lineFinished)
							trackPointList.clear();
						lineFinished = false;
						trackPtsFromImport = false;
						
						addCurrPosToTrackPt();
					}
					
				}
			}

			@Override
			public void mouseClicked(MouseEvent mouseEvent){
				if (armed && mouseEvent.getButton() == MouseEvent.BUTTON1) {

					mouseEvent.consume();
					if(mouseEvent.getClickCount() == 2) {
						//addCurrPosToTrackPt();
						 trackLineFinished();
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
				if(armed){
					
					event.consume();

				}

			}
		});


	}
	
	
	// Return true if date data was contained in the imported file
	public boolean importTrackLineFromFile(Component comp){
		String workingDir = System.getProperty("user.dir");
		File currFile = null;
		if(workingDir != null)
			currFile = new File(workingDir);
		boolean containsDateData = false;
		JFileChooser fileChooser = new JFileChooser(currFile);
		int returnVal = fileChooser.showOpenDialog(comp);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			
			File file = fileChooser.getSelectedFile();
			if(file != null){
				//read the file and add the points to the track line
				try {
					BufferedReader br = new BufferedReader(new FileReader(file));
					EdcDateUtils dateUtils = new EdcDateUtils();
					String line;
					clearTrackPoints();
					trackPtsFromImport = true;
					while ((line = br.readLine()) != null) {
						String tok[] = line.split(",");
						if(tok.length == 3 || tok.length == 5){
							Double lat, lon, elevation;
							Date startDate = null;
							Date endDate = null;
							try{
								lat = Double.valueOf(tok[0]);
								lon = Double.valueOf(tok[1]);
								elevation = Double.valueOf(tok[2]);
								LatLon trackPt = LatLon.fromDegrees(lat, lon);
								if(tok.length == 5){
									startDate = dateUtils.parseDate(tok[3]);
									endDate = dateUtils.parseDate(tok[4]);
						          
									containsDateData = true;
								}
								addPosToTrackPt(new Position(trackPt, elevation),
												startDate, endDate);


							}
							catch(NumberFormatException e){
								e.printStackTrace();
							}
						}
					}
					br.close();

				

				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		return containsDateData;
	}
	
	

	public void exportTrackLineToFile(Component comp){
		JFileChooser fileChooser = new JFileChooser();
		int returnVal = fileChooser.showOpenDialog(comp);
		DateFormat dateFormat = DateFormat.getDateInstance();
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			File file = fileChooser.getSelectedFile();
			if(file != null){
				//read the file and add the points to the track line
				try {
					BufferedWriter wr = new BufferedWriter(new FileWriter(file));
					for(TrackLineVertex model : this.trackConnectorModel){
						Position pos = model.getTrackPt();
						wr.write(pos.latitude.degrees + ","+pos.longitude.degrees+","+pos.getElevation());
						if(model.getStartTime() != null && model.getEndTime() != null)
							wr.write(dateFormat.format(model.getStartTime())+","+
									dateFormat.format(model.getEndTime())+"\n");
					}
					wr.close();
				}
					catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				}
	}
	
	
	public List<BoundingBox> getTrackPointVertices() {
		return trackPointVertices;
	}

	public void setTrackPointVertices(List<BoundingBox> trackPointVertices) {
		this.trackPointVertices = trackPointVertices;
	}

	public List<TrackLineVertex> getTrackConnectorModel() {
		return trackConnectorModel;
	}

}
