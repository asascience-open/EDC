/*
 * Applied Science Associates, Inc.
 * Copyright 2007. All rights reserved.
 */

/*
 * MapUtils.java
 *
 * Created on Apr 2, 2008, 12:11:54 PM
 *
 */
package com.asascience.openmap.utilities;

import java.awt.Color;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import ucar.unidata.geoloc.LatLonPointImpl;

import com.asascience.openmap.layer.VectorLayer;
import com.asascience.openmap.layer.asa.CixVectorLayer;
import com.asascience.openmap.layer.nc.grid.GenericGridLayer;
import com.asascience.openmap.layer.nc.grid.NcomVectorLayer;
import com.asascience.openmap.layer.nc.grid.SwafsVectorLayer;
import com.asascience.openmap.layer.nc.ncell.AssetLayer;
import com.asascience.openmap.layer.nc.ncell.NcellVectorLayer;
import com.asascience.utilities.Utils;
import com.asascience.utilities.io.DetermineNcType;
import com.asascience.utilities.io.NcFileType;
import com.bbn.openmap.LatLonPoint;
import com.bbn.openmap.Layer;

/**
 * 
 * @author CBM <cmueller@asascience.com>
 */
public class MapUtils {

	public static int UNITS_KNOTS = 0;
	public static int UNITS_MPS = 1;
	public static int UNITS_CMPS = 2;

	// /** Creates a new instance of MapUtils */
	// public MapUtils(){
	//        
	// }
	public static LatLonPoint ucarPointToBbnPoint(LatLonPointImpl ucarP) {
		return new LatLonPoint(ucarP.getLatitude(), ucarP.getLongitude());
	}

	public static LatLonPointImpl bbnPointToUcarPoint(LatLonPoint bbnP) {
		return new LatLonPointImpl(bbnP.getLatitude(), bbnP.getLongitude());
	}

	public static String getUniqueLayerName(String layerName, Layer[] layers) {
		List<String> lnames = MapUtils.getLayerNames(layers);
		if (lnames.contains(layerName)) {
			int i = 0;
			for (String s : lnames) {
				if (s.equals(layerName)) {
					i++;
				}
			}
			// l.setName(l.getName() + "_" + i);
			layerName += "_" + i;
		}
		return layerName;
	}

	public static List<String> getLayerNames(Layer[] layers) {
		List<String> ret = new ArrayList<String>();
		for (Layer l : layers) {
			ret.add(l.getName());
		}

		return ret;
	}

	public static VectorLayer obtainVectorLayer(String layerLoc) {
		return MapUtils.obtainVectorLayer(layerLoc, null);
	}

	public static VectorLayer obtainVectorLayer(String layerLoc, Layer[] layers) {
		try {
			File f = new File(layerLoc);
			if (!f.exists()) {
				return null;
			}
			VectorLayer lyr = null;
			String ext = Utils.getExtension(f);
			if (ext.toLowerCase().equals("nc")) {
				NcFileType type = DetermineNcType.determineFileType(layerLoc);
				if (type == NcFileType.NCELL) {
					lyr = new NcellVectorLayer(layerLoc);
				} else if (type == NcFileType.NCOM) {
					lyr = new NcomVectorLayer(layerLoc);
				} else if (type == NcFileType.SWAFS) {
					lyr = new SwafsVectorLayer(layerLoc);
				} else if (type == NcFileType.ASSET) {
					lyr = new AssetLayer(layerLoc);
				} else if (type == NcFileType.GENERIC_GRID) {
					lyr = new GenericGridLayer(layerLoc);
				} else {
					/**
					 * If the type cannot be determined, show a dialog which
					 * will create a "map" file for the netcdf layer.
					 */

				}
			} else if (ext.toLowerCase().equals("cix")) {
				lyr = new CixVectorLayer(layerLoc);
			}

			if (lyr == null) {
				return lyr;
			}
			if (layers != null) {
				lyr.setName(MapUtils.getUniqueLayerName(lyr.getName(), layers));
			}

			return lyr;
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		return null;
	}

	public static float[] buildFloatPolygonArray(double[] lats, double[] lons) {
		if (lats.length != lons.length) {
			return null;
		}
		try {
			double[] ret = new double[(lats.length * 2) + 2];
			int latI = 0, lonI = 1;
			// add the lats and lons alternating
			for (int i = 0; i < lats.length; i++) {
				ret[latI] = lats[i];
				ret[lonI] = lons[i];
				latI = latI + 2;
				lonI = lonI + 2;
			}
			// close the polygon
			ret[ret.length - 2] = lats[0];
			ret[ret.length - 1] = lons[0];

			return Utils.doubleArrayToFloatArray(ret);
		} catch (Exception ex) {
		}

		return null;
	}

	// TODO: change from float to double once openmap supports doubles
	public static float[] buildFloatPolygonArray(double lat1, double lon1, double lat2, double lon2) {
		double[] ret = new double[10];

		ret[0] = lat1;
		ret[1] = lon1;

		ret[2] = lat2;
		ret[3] = lon1;

		ret[4] = lat2;
		ret[5] = lon2;

		ret[6] = lat1;
		ret[7] = lon2;

		ret[8] = lat1;
		ret[9] = lon1;

		return Utils.doubleArrayToFloatArray(ret);
	}

	public static Color[] buildColorRamp(Color startColor, Color endColor, int numColors, int alpha) {
		Color[] colors = new Color[numColors];

		for (int j = 0; j < colors.length; j++) {
			float ratio = (float) j / (float) colors.length;
			int red = (int) (endColor.getRed() * ratio + startColor.getRed() * (1 - ratio));
			int green = (int) (endColor.getGreen() * ratio + startColor.getGreen() * (1 - ratio));
			int blue = (int) (endColor.getBlue() * ratio + startColor.getBlue() * (1 - ratio));
			colors[j] = new Color(red, green, blue, alpha);
		}

		return colors;
	}

	public static double[] buildEqualDivisionVals(int numVals, double maxVal, double minVal) {
		double[] ret = new double[numVals];
		double inc = (maxVal - minVal) / numVals;

		ret[0] = maxVal - inc;
		for (int i = 1; i < numVals; i++) {
			ret[i] = ret[i - 1] - inc;
		}

		return ret;
	}

	public static double[] buildUnequalDivisionVals(int numVals, double maxVal, double minVal) {
		double[] ret = new double[numVals];

		ret[0] = maxVal * 0.75d;
		for (int i = 1; i < numVals; i++) {
			ret[i] = ret[i - 1] * 0.5;
		}

		return ret;
	}

	public static class Conversion {

		public static double[] toMPS(String fromUnit, double[] fromValues) {
			double[] ret = null;
			int fu = getUnitFromString(fromUnit);
			if (fu == -1) {
				return ret;
			}

			if (fu == 1) {// Already in m/s
				return fromValues;
			}

			try {
				ret = new double[fromValues.length];
				for (int i = 0; i < fromValues.length; i++) {
					ret[i] = convertUnits(fu, 1, fromValues[i]);
				}
				return ret;
			} catch (Exception e) {
				e.printStackTrace();
			}

			return null;
		}

		public static double toMPS(String fromUnit, double fromValue) {
			int fu = getUnitFromString(fromUnit);
			if (fu != -1) {
				return convertUnits(fu, 1, fromValue);
			}

			return Double.NaN;
		}

		public static double[] toCMPS(String fromUnit, double[] fromValues) {
			double[] ret = null;
			int fu = getUnitFromString(fromUnit);
			if (fu == -1) {
				return ret;
			}

			if (fu == 2) {// Already in cm/s
				return fromValues;
			}

			try {
				ret = new double[fromValues.length];
				for (int i = 0; i < fromValues.length; i++) {
					ret[i] = convertUnits(fu, 2, fromValues[i]);
				}
				return ret;
			} catch (Exception e) {
				e.printStackTrace();
			}

			return null;
		}

		public static double toCMPS(String fromUnit, double fromValue) {
			int fu = getUnitFromString(fromUnit);
			if (fu != -1) {
				return convertUnits(fu, 2, fromValue);
			}

			return Double.NaN;
		}

		public static double[] toKNOTS(String fromUnit, double[] fromValues) {
			double[] ret = null;
			int fu = getUnitFromString(fromUnit);
			if (fu == -1) {
				return ret;
			}

			if (fu == 0) {// Already in knots
				return fromValues;
			}

			try {
				ret = new double[fromValues.length];
				for (int i = 0; i < fromValues.length; i++) {
					ret[i] = convertUnits(fu, 0, fromValues[i]);
				}
				return ret;
			} catch (Exception e) {
				e.printStackTrace();
			}

			return null;
		}

		public static double toKNOTS(String fromUnit, double fromValue) {
			int fu = getUnitFromString(fromUnit);
			if (fu != -1) {
				return convertUnits(fu, 0, fromValue);
			}

			return Double.NaN;
		}

		public static int getUnitFromString(String unitString) {
			if (unitString == null) {
				return -1;
			}
			if (unitString.toLowerCase().contains("knots") || unitString.toLowerCase().contains("kts")
				|| unitString.toLowerCase().contains("kt")) {
				return 0;
			}
			if (unitString.toLowerCase().contains("meters/second") || unitString.toLowerCase().contains("meters/sec")
				|| unitString.toLowerCase().contains("meters/s") || unitString.toLowerCase().contains("meter/second")
				|| unitString.toLowerCase().contains("meter/sec") || unitString.toLowerCase().contains("meter/s")
				|| unitString.toLowerCase().contains("m/second") || unitString.toLowerCase().contains("m/sec")
				|| unitString.toLowerCase().contains("m/s") || unitString.toLowerCase().contains("meters per second")
				|| unitString.toLowerCase().contains("meters per sec")
				|| unitString.toLowerCase().contains("meters per s")
				|| unitString.toLowerCase().contains("meter per second")
				|| unitString.toLowerCase().contains("meter per sec")
				|| unitString.toLowerCase().contains("meter per s") || unitString.toLowerCase().contains("m per s")
				|| unitString.toLowerCase().contains("mps")) {
				return 1;
			}
			if (unitString.toLowerCase().contains("centimeters/second")
				|| unitString.toLowerCase().contains("centimeters/sec")
				|| unitString.toLowerCase().contains("centimeters/s")
				|| unitString.toLowerCase().contains("centimeter/second")
				|| unitString.toLowerCase().contains("centimeter/sec")
				|| unitString.toLowerCase().contains("centimeter/s") || unitString.toLowerCase().contains("cm/second")
				|| unitString.toLowerCase().contains("cm/sec") || unitString.toLowerCase().contains("cm/s")
				|| unitString.toLowerCase().contains("centimeters per second")
				|| unitString.toLowerCase().contains("centimeters per sec")
				|| unitString.toLowerCase().contains("centimeters per s")
				|| unitString.toLowerCase().contains("centimeter per second")
				|| unitString.toLowerCase().contains("centimeter per sec")
				|| unitString.toLowerCase().contains("centimeter per s")
				|| unitString.toLowerCase().contains("cm per s") || unitString.toLowerCase().contains("cmps")) {
				return 2;
			}

			return -1;
		}

		public static double convertUnits(String fromUnit, String toUnit, double fromValue) {
			int fu = getUnitFromString(fromUnit);
			int tu = getUnitFromString(toUnit);
			if (fu != -1 & tu != -1) {
				return convertUnits(fu, tu, fromValue);
			}

			return Double.NaN;
		}

		public static double convertUnits(int fromUnit, int toUnit, double fromValue) {
			double ret = Double.NaN;

			switch (fromUnit) {
				case 0:// knots
					switch (toUnit) {
						case 0:// knots
							ret = fromValue;
							break;
						case 1:// m/s
							ret = fromValue * 0.514444444d;
							break;
						case 2:// cm/s
							ret = fromValue * 51.44444444d;
							break;
						default:// unknown - return the input value
							ret = fromValue;
							break;
					}
					break;
				case 1:// m/s
					switch (toUnit) {
						case 0:// knots
							ret = fromValue * 1.94384449d;
							break;
						case 1:// m/s
							ret = fromValue;
							break;
						case 2:// cm/s
							ret = fromValue * 100d;
							break;
						default:// unknown - return the input value
							ret = fromValue;
							break;
					}
					break;
				case 2:// cm/s
					switch (toUnit) {
						case 0:// knots
							ret = fromValue * 0.0194384449d;
							break;
						case 1:// m/s
							ret = fromValue * 0.01d;
							break;
						case 2:// cm/s
							ret = fromValue;
							break;
						default:// unknown - return the input value
							ret = fromValue;
							break;
					}
					break;
				default:// unknown - return the input value
					ret = fromValue;
					break;
			}

			return ret;
		}
	}
}
