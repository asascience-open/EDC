package com.asascience.edc.map;

import java.util.Date;

import ucar.unidata.geoloc.LatLonPointImpl;
import ucar.unidata.geoloc.LatLonRect;

import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.render.SurfaceSquare;

public class TrackLineVertex {
	Position trackPt;
	Date startTime;
	Date endTime;
	LatLonRect boundingBox;
	SurfaceSquare square;
	public TrackLineVertex(Position pos, BoundingBox bbox, 
						   Date startTime, Date endTime,
						   SurfaceSquare square){
		this.trackPt = pos;
		this.square = square;
		this.startTime = startTime;
		this.endTime = endTime;
		if(startTime != null && endTime != null)
		System.out.println("Track line time start " + startTime.toString() + " end " + endTime.toString());
		initLatLonRect(bbox);
	}

	public void initLatLonRect(BoundingBox bbox){
		LatLonPointImpl right = new LatLonPointImpl(bbox.getLowerRight().latitude.degrees,
				bbox.getLowerRight().longitude.degrees);
		LatLonPointImpl left = new LatLonPointImpl(bbox.getUpperLeft().latitude.degrees,
				bbox.getUpperLeft().longitude.degrees);
		boundingBox = new LatLonRect(left, right);
	}
	
	public Position getTrackPt() {
		return trackPt;
	}

	public void setTrackPt(Position trackPt) {
		this.trackPt = trackPt;
	}

	public Date getStartTime() {
		return startTime;
	}

	public void setStartTime(Date startTime) {
		this.startTime = startTime;
	}

	public Date getEndTime() {
		return endTime;
	}

	public SurfaceSquare getSquare() {
		return square;
	}

	public void setSquare(SurfaceSquare square) {
		this.square = square;
	}

	public void setEndTime(Date endTime) {
		this.endTime = endTime;
	}

	public LatLonRect getBoundingBox() {
		return boundingBox;
	}


	
}
