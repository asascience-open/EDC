package com.asascience.edc.utils;

import java.awt.geom.Path2D;
import java.util.List;

import ucar.unidata.geoloc.LatLonPointImpl;

public class PolygonUtils {
	public static Path2D.Double getPolygonFromVertices( List<LatLonPointImpl> verticeList){
		Path2D.Double polygon = new Path2D.Double();
		if(verticeList != null && verticeList.size() > 0){
			polygon.moveTo(verticeList.get(0).getLongitude(), verticeList.get(0).getLatitude());
			for(int i = 1; i < verticeList.size(); i++){
				polygon.lineTo(verticeList.get(i).getLongitude(), verticeList.get(i).getLatitude());
			}
		}
		return polygon;
	}
	  
	
}
