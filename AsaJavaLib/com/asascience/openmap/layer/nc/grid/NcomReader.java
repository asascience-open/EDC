/*
 * NcomReader.java
 *
 * Created on August 2, 2007, 12:26 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.asascience.openmap.layer.nc.grid;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import ucar.ma2.Array;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.Attribute;
import ucar.nc2.Dimension;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;
import ucar.nc2.dt.grid.GeoGrid;

/**
 * 
 * @author cmueller
 */
public class NcomReader extends NcGridReader {

	private NetcdfFile ncfile = null;
	private List<Dimension> dims;
	private List<Variable> vars;
	private List<Attribute> atts;

	private int m_latLength;
	private int m_lonLength;
	// private double[] fullLats;
	// private double[] fullLons;

	private String m_depth = "depth";
	private String m_tau = "tau";
	private String m_time = "time";
	private String m_lon = "lon";
	private String m_lat = "lat";
	private String m_v = "water_v";
	private String m_u = "water_u";
	private String m_temp = "water_temp";
	private String m_sal = "salinity";
	private String m_surfel = "surf_el";

	private GeoGrid gU = null;
	private GeoGrid gV = null;
	private GeoGrid gTemp = null;
	private GeoGrid gSal = null;
	private GeoGrid gSel = null;

	// private boolean hasScalars = false;
	// public List<String> scalarNames;

	/** Creates a new instance of NcomReader */
	public NcomReader(String file) {
		super(file);// construct the NcGridReader

		// assign the appropriate geogrids
		gU = getGridByName(m_u);
		gV = getGridByName(m_v);
		// gTemp = getGridByName(m_temp);
		// gSal = getGridByName(m_sal);
		// gSel = getGridByName(m_surfel);

		if (gU == null | gV == null) {
			System.err.println("NcomReader:Error Retreiving U & V GeoGrids");
		}
		// if(gTemp == null | gSal == null | gSel == null){
		// System.err.println("NcomReader:Error Retreiving Temp, Salinity & Surface Elevation GeoGrids");
		// }
		scalarNames = new ArrayList<String>();
		if (getGridByName(m_u) != null) {
			hasScalars = true;
			scalarNames.add(m_u);
			/** Get u/v units */
			uvUnits = getGridByName(m_u).getUnitsString();
		}
		if (getGridByName(m_v) != null) {
			hasScalars = true;
			scalarNames.add(m_v);
		}
		if (getGridByName(m_temp) != null) {
			hasScalars = true;
			scalarNames.add(m_temp);
		}
		if (getGridByName(m_sal) != null) {
			hasScalars = true;
			scalarNames.add(m_sal);
		}
		if (getGridByName(m_surfel) != null) {
			hasScalars = true;
			scalarNames.add(m_surfel);
		}

		// try {
		// ncfile = NetcdfFile.open(file);
		// dims = ncfile.getDimensions();
		// vars = ncfile.getVariables();
		// atts = ncfile.getGlobalAttributes();
		//            
		// //determine the length of the lat & lon variables
		// Variable v = ncfile.findVariable(m_lat);
		// int[] shp = v.getShape();
		// m_latLength = shp[0];
		// v = ncfile.findVariable(m_lon);
		// shp = v.getShape();
		// m_lonLength = shp[0];

		// <editor-fold defaultstate="collapsed" desc=" Moved to NcGridReader ">
		// float[] lats = this.getYValues();
		// float[] lons = this.getXValues();
		// fullLats = new double[lats.length * lons.length];
		// fullLons = new double[lats.length * lons.length];
		// int x = 0;
		// for(int i = 0; i < lats.length; i++){
		// for(int j = 0; j < lons.length; j++){
		// fullLats[x] = lats[i];
		// fullLons[x] = lons[j];
		// x++;
		// }
		// }

		// </editor-fold>

		//            
		// } catch (IOException ex) {
		// ex.printStackTrace();
		// }
	}

	// public float[] getLats(){
	// Variable v = ncfile.findVariable(m_lat);
	// float[] darr = new float[m_latLength];
	// try {
	// Array a = v.read();
	// darr = (float[]) a.get1DJavaArray(float.class);
	// } catch (IOException ex) {
	// ex.printStackTrace();
	// }
	// return darr;
	// }
	// public float[] getLons(){
	// Variable v = ncfile.findVariable(m_lon);
	// float[] darr = new float[m_lonLength];
	// try {
	// Array a = v.read();
	// darr = (float[]) a.get1DJavaArray(float.class);
	// } catch (IOException ex) {
	// ex.printStackTrace();
	// }
	// return darr;
	// }

	public double[] getFullLats() {
		return fullLats;
	}

	public double[] getFullLons() {
		return fullLons;
	}

	public String getScalarDescriptionByName(String varName) {
		GeoGrid g = getGridByName(varName);
		if (g != null) {
			return g.getDescription();
		}
		return null;
	}

	public double[] getScalarDataByName(long t, int levelIndex, String varName) {
		GeoGrid v = getGridByName(varName);
		if (v != null) {
			if (t >= getStartTime() & t <= getEndTime()) {
				try {
					int tIndex = getTimeIndex(t);

					return (double[]) v.readDataSlice(tIndex, levelIndex, -1, -1).get1DJavaArray(double.class);
				} catch (IOException ex) {
					Logger.getLogger(NcomReader.class.getName()).log(Level.SEVERE, null, ex);
				}
			}
		}

		return null;
	}

	public double[] getUVals(double depth) {
		return getUVals(this.getStartTime(), depth);
	}

	public double[] getUVals(long time, double depth) {
		int indexT = getTimeIndex(time);
		int indexZ = getZIndexAtValue(depth);

		double[] uVals = new double[] { Double.NaN };
		if (indexT != -1) {// & indexZ != -1){
			try {
				Array a = gU.readYXData(indexT, indexZ).reduce();
				uVals = (double[]) a.get1DJavaArray(double.class);
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		} else {
			System.err.println("NcomReader:time or depth index invalid");
		}

		return uVals;
	}

	public double[] getVVals(double depth) {
		return getVVals(this.getStartTime(), depth);
	}

	public double[] getVVals(long time, double depth) {
		int indexT = getTimeIndex(time);
		int indexZ = getZIndexAtValue(depth);

		double[] vVals = new double[] { Double.NaN };
		if (indexT != -1) {// & indexZ != -1){
			try {
				Array a = gV.readYXData(indexT, indexZ).reduce();
				vVals = (double[]) a.get1DJavaArray(double.class);
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		} else {
			System.err.println("NcomReader:time or depth index invalid");
		}

		return vVals;
	}

	public double[] getUs(int timestep, int depth) {
		int[] origin = new int[] { timestep, depth, 0, 0 };
		int[] size = new int[] { 1, 1, m_latLength, m_lonLength };
		double[] uVals = new double[m_latLength * m_lonLength];

		Variable v = this.getNcfile().findVariable(m_u);
		try {
			// read the array
			Array a = v.read(origin, size).reduce();

			// get all of the values into a single float[]
			uVals = (double[]) a.get1DJavaArray(double.class);

			// alternate way to do the same thing...
			// int x = 0;
			// Index index = a.getIndex();
			// for(int i = 0; i < m_latLength; i++){
			// for(int j = 0; j < m_lonLength; j++){
			// uVals[x] = a.getShort(index.set(i, j));
			// x++;
			// }
			// }
		} catch (IOException ex) {
			ex.printStackTrace();
		} catch (InvalidRangeException ex) {
			ex.printStackTrace();
		}

		return uVals;
	}

	public double[] getVs(int timestep, int depth) {
		int[] origin = new int[] { timestep, depth, 0, 0 };
		int[] size = new int[] { 1, 1, m_latLength, m_lonLength };
		double[] vVals = new double[m_latLength * m_lonLength];

		Variable v = this.getNcfile().findVariable(m_v);
		try {
			Array a = v.read(origin, size);
			vVals = (double[]) a.get1DJavaArray(double.class);
		} catch (IOException ex) {
			ex.printStackTrace();
		} catch (InvalidRangeException ex) {
			ex.printStackTrace();
		}

		return vVals;
	}

	public ArrayList<String> getVariableNames() {
		ArrayList<String> vNames = new ArrayList<String>();
		Iterator it = vars.iterator();
		Variable v = null;
		while (it.hasNext()) {
			v = (Variable) it.next();
			vNames.add(v.getName());
		}
		return vNames;
	}

	public List<Variable> getVariables() {
		return ncfile.getVariables();
	}

	public List<String> getScalarNames() {
		return scalarNames;
	}

	public boolean closeNcom() {
		try {
			if (ncfile != null) {
				ncfile.close();
			}
			return true;
		} catch (IOException ex) {
			ex.printStackTrace();
		}
		return false;
	}
}
