/** Applied Science Associates, Inc.
 * Copyright 2009. All Rights Reserved.
 *
 * ParticleTracker.java
 *
 * Created on Feb 19, 2009 @ 12:19:11 PM
 */

package com.asascience.edc.particle;

import java.awt.Component;
import java.io.IOException;
import java.util.List;

import javax.swing.JOptionPane;

import ucar.nc2.dt.grid.GeoGrid;

import com.asascience.openmap.layer.nc.grid.GenericGridReader;

/**
 * 
 * @author CBM <cmueller@asascience.com>
 */

public class ParticleAnalyzer {

	private ParticleOutputReader por;
	private GenericGridReader ggr;
	private List<Long> partTimes;
	private Component parent;
	private String analysisGridName = "";
	private GeoGrid analysisGrid = null;

	public ParticleAnalyzer(Component parent, String particleFile, String dataFile) throws Exception {
		// VectorLayer l = MapUtils.obtainVectorLayer(dataFile);
		// if (l == null) {
		// throw new Exception("The data file is not a recognized format.");
		// }

		// ParticleOutputLayer pol = new ParticleOutputLayer(new
		// File(particleFile), null);
		// if(pol == null) {
		// throw new Exception("The particle file is not a recognized format.");
		// }
		this.parent = parent;
		ggr = new GenericGridReader(dataFile);
		por = new ParticleOutputReader(particleFile);

		partTimes = por.getTimes();

		// test();
	}

	private void test() {
		// for (long t : partTimes) {
		// processTimestep(t);
		// }

		int numPart = por.getNumberOfParticles();
		double[][] values = new double[numPart][];
		for (int i = 0; i < numPart; i++) {
			values[i] = processParticle(i + 1);// particle id's are 1 based
		}

		printValues("out.txt", values);

		JOptionPane.showMessageDialog(parent, "Finished");
	}

	public List<String> getAvaliableGridNames() {
		return ggr.getScalarNames();
	}

	public void setAnalysisGridByName(String gridName) {
		GeoGrid grid = ggr.getGridByName(gridName);
		if (grid != null) {
			analysisGrid = grid;
			analysisGridName = gridName;
		} else {
			analysisGrid = null;
			analysisGridName = "";
		}
	}

	public String getAnalysisGridName() {
		return analysisGridName;
	}

	public String getAnalysisGridUnits() {
		String ret = "";
		if (analysisGrid != null) {
			ret = analysisGrid.getUnitsString();
		}
		return ret;
	}

	public String getAnalysisGridDescription() {
		String ret = "";
		if (analysisGrid != null) {
			ret = analysisGrid.getDescription();
		}
		return ret;
	}

	public String getAnalysisGridAxisTitle() {
		StringBuilder ret = new StringBuilder();
		String desc = getAnalysisGridDescription();
		ret.append((desc.length() > 20) ? desc.substring(0, 20) + "..." : desc);
		ret.append(" (");
		ret.append(getAnalysisGridUnits());
		ret.append(")");

		return ret.toString();
	}

	public double[] processParticle(int particleID) {
		double[] ret = new double[partTimes.size()];
		double[][] locs = por.getParticleLocations(particleID);
		if (analysisGrid == null) {
			return null;
		}
		long time;
		int t, x, y, z;
		for (int i = 0; i < partTimes.size(); i++) {
			t = ggr.getTimeIndex(partTimes.get(i));
			if (t != -1) {
				y = ggr.getYIndexAtValue(locs[0][i]);
				x = ggr.getXIndexAtValue(locs[1][i]);
				z = ggr.getZIndexAtValue(locs[2][i]);
				if (x != -1 & y != -1) {
					try {
						double[] vals = (double[]) analysisGrid.readDataSlice(t, z, y, x).get1DJavaArray(double.class);
						ret[i] = vals[0];
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}
		return ret;
	}

	public void processTimestep(long time) {
		if (time >= por.getStartTime() & time <= por.getEndTime()) {
			List<SimpleParticle> particles = por.getSimpleParticlesAtTime(time);
			if (analysisGrid == null) {
				return;
			}
			int t = ggr.getTimeIndex(time);
			int y, x, z;
			if (t != -1) {
				SimpleParticle p;
				for (int i = 0; i < particles.size(); i++) {
					p = particles.get(i);
					y = ggr.getYIndexAtValue(p.getPosY());
					x = ggr.getXIndexAtValue(p.getPosX());
					z = ggr.getZIndexAtValue(p.getPosZ());
					if (x != -1 & y != -1) {
						try {
							double[] vals = (double[]) analysisGrid.readDataSlice(t, z, y, x).get1DJavaArray(
								double.class);
							for (double d : vals) {
								System.out.println(i + " " + d);
							}
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}
			}
		}
	}

	private void printValues(String outPath, double[][] values) {
		java.io.FileWriter writer = null;
		try {
			writer = new java.io.FileWriter(outPath);
			for (int i = 0; i < values.length; i++) {
				for (int j = 0; j < values[i].length; j++) {
					writer.write(i + " " + j + " " + values[i][j] + "\n");
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				if (writer != null) {
					writer.flush();
					writer.close();
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

}
