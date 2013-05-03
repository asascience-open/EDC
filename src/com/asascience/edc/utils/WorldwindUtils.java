package com.asascience.edc.utils;

import gov.nasa.worldwind.Configuration;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.globes.Earth;
import java.util.ArrayList;
import java.util.List;

/**
 * WorldwindUtils.java
 * 
 * @author Kyle Wilcox <kwilcox@asascience.com>
 */
public class WorldwindUtils {
  
  public static Position getEyePositionFromPositions(Iterable<? extends LatLon> positions) {
	if(positions == null) return null;
    Sector sector = Sector.boundingSector(positions);
    Angle delta = sector.getDeltaLat();
    if (sector.getDeltaLon().compareTo(delta) > 0) {
      delta = sector.getDeltaLon();
    }
    double arcLength = delta.radians * Earth.WGS84_EQUATORIAL_RADIUS;
    double fieldOfView = Configuration.getDoubleValue(AVKey.FOV, 45.0);
    return new Position(sector.getCentroid(), arcLength / (1.5 * Math.tan(fieldOfView / 2.0)));
  }
  
  public static List<LatLon> normalizeLatLons(List<LatLon> positions) {
    List<LatLon> pos = new ArrayList<LatLon>();
    for (LatLon k : positions) {
      pos.add(WorldwindUtils.normalizeLatLon(k));
    }
    return pos;
  }
  
  public static LatLon normalizeLatLon(LatLon position) {
    return LatLon.fromDegrees(WorldwindUtils.normLat(position.getLatitude().getDegrees()),WorldwindUtils.normLon(position.getLongitude().getDegrees()));
  }
  
  	/**
	 * Normalize the longitude to lie between +/-180
	 * 
	 * @param lon
	 *            east latitude in degrees
	 * @return normalized lon
	 */
	public static double normLon(double lon) {
		if ((lon < -180.0) || (lon > 180.0)) {
			return Math.IEEEremainder(lon, 360.0);
		} else {
			return lon;
		}
	}
    
  	/**
	 * put longitude into the range [0, 360] deg
	 * 
	 * @param lon
	 *            lon to normalize
	 * @return longitude into the range [0, 360] deg
	 */
	static public double normLon360(double lon) {
		return normLon(lon, 180.0);
	}

	/**
	 * put longitude into the range [center +/- 180] deg
	 * 
	 * @param lon
	 *            lon to normalize
	 * @param center
	 *            center point
	 * @return longitude into the range [center +/- 180] deg
	 */
	static public double normLon(double lon, double center) {
		return center + Math.IEEEremainder(lon - center, 360.0);
	}

	/**
	 * Normalize the latitude to lie between +/-90
	 * 
	 * @param lat
	 *            north latitude in degrees
	 * @return normalized lat
	 */
	public static double normLat(double lat) {
		if (lat < -90.0) {
			return -90.0;
		} else if (lat > 90.0) {
			return 90.0;
		} else {
			return lat;
		}
	}
}
