/*
 * Applied Science Associates, Inc.
 * Copyright 2009. All Rights Reserved.
 *
 * TriangulationLookup.java
 *
 * Created on Feb 25, 2009 @ 10:56:03 AM
 */

package com.asascience.openmap.layer.nc.ncell;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import com.asascience.utilities.BinarySearch;
import com.asascience.utilities.Vector3D;

/**
 * 
 * @author CBM <cmueller@asascience.com>
 */

public class TriangulationLookup {

	private final Vector3D ref1 = new Vector3D(0, 0, 0);
	private final Vector3D ref2 = new Vector3D(45, 0, 0);
	private final Vector3D ref3 = new Vector3D(0, 45, 0);

	// private HashMap<Double, Integer> htLookup;
	private HashMap<TrigDistance, Integer> htLookup;
	// private HashMap<String, Integer> htLookup;

	// private List<Double> keys;
	private List<TrigDistance> keys;
	// private List<String> keys;

	private BinarySearch binSearch;

	public TriangulationLookup(double[] lats, double[] lons) {
		// htLookup = new HashMap<Double, Integer>();
		htLookup = new HashMap<TrigDistance, Integer>();
		// htLookup = new HashMap<String, Integer>();

		Vector3D pos;
		// double totDist;
		TrigDistance totDist;
		// String totDist;
		for (int i = 0; i < lats.length; i++) {
			pos = new Vector3D(lons[i], lats[i], 0);
			// totDist = calculateTotalDistance(pos);
			totDist = calculateTrigDistance(pos);
			// totDist = calculateStringDistance(pos);
			htLookup.put(totDist, i);
		}
		// Double[] ks = htLookup.keySet().toArray(new Double[0]);
		TrigDistance[] ks = htLookup.keySet().toArray(new TrigDistance[0]);
		// String[] ks = htLookup.keySet().toArray(new String[0]);
		Arrays.sort(ks);
		keys = Arrays.asList(ks);
		// printKeyValuePairs();
	}

	// public void printKeyValuePairs() {
	// double d;
	// for(int i = 0; i < keys.size(); i++) {
	// d = keys.get(i);
	// System.out.println(i + "  -->  " + d + ": " + htLookup.get(d));
	// }
	// }

	public int getNcellIndexFromLatLon(double lat, double lon) {
		Vector3D pos = new Vector3D(lon, lat, 0);
		// double totDist = calculateTotalDistance(pos);
		TrigDistance queryDist = calculateTrigDistance(pos);
		// String totDist = calculateStringDistance(pos);
		TrigDistance td;
		int index = -1;
		// /** Works - but pretty slow (but still faster than the "original"
		// method!!) */
		double sum, minu, minv, minw, minsum = -1;
		for (int i = 0; i < keys.size(); i++) {
			td = keys.get(i);
			minu = Math.abs(td.getDu() - queryDist.getDu());
			minv = Math.abs(td.getDv() - queryDist.getDv());
			minw = Math.abs(td.getDw() - queryDist.getDw());
			sum = minu + minv + minw;
			if (sum == 0) {
				index = i;
				break;
			}
			if (i == 0) {
				minsum = sum;
			}
			if (sum < minsum) {
				minsum = sum;
				index = i;
			}
		}
		/** Doesn't work... */
		// for(int i = 0; i < keys.size(); i++) {
		// if(keys.get(i).compareTo(totDist) <= 0) {
		// index = i;
		// }
		// }
		// /**Doesn't work...*/
		// index = Collections.binarySearch(keys, totDist);
		// if(index < 0) {
		// System.out.println("neg");
		// index = Math.abs(index) - 1;
		// }
		return htLookup.get(keys.get(index));
	}

	public double calculateTotalDistance(Vector3D pos) {
		/** Distance from ref1. */
		double d1 = calculateDistance(pos, ref1);
		/** Distance from ref2. */
		double d2 = calculateDistance(pos, ref2);
		/** Distance from ref3. */
		double d3 = calculateDistance(pos, ref3);

		/** Combine the values. */
		// double ret = (d1*10000) + (d2*100) + d3;
		// double ret = d1 + d2 + d3;
		double ret = ((int) d1 * 1000000d) + ((int) d2 * 1000d) + ((int) d3);
		return ret;
	}

	public TrigDistance calculateTrigDistance(Vector3D pos) {
		/** Distance from ref1. */
		double d1 = calculateDistance(pos, ref1);
		/** Distance from ref2. */
		double d2 = calculateDistance(pos, ref2);
		/** Distance from ref3. */
		double d3 = calculateDistance(pos, ref3);

		/** Combine the values. */
		// double ret = (d1*10000) + (d2*100) + d3;
		// double ret = d1 + d2 + d3;

		TrigDistance td = new TrigDistance(d1, d2, d3);
		return td;
	}

	public String calculateStringDistance(Vector3D pos) {
		/** Distance from ref1. */
		double d1 = calculateDistance(pos, ref1);
		/** Distance from ref2. */
		double d2 = calculateDistance(pos, ref2);
		/** Distance from ref3. */
		double d3 = calculateDistance(pos, ref3);

		/** Combine the values. */
		// String ret = d1 + "-" + d2 + "-" + d3;
		StringBuilder ret = new StringBuilder();
		ret.append(Math.round(d1 * 1000000d));
		ret.append(Math.round(d2 * 1000000d));
		ret.append(Math.round(d3 * 1000000d));
		// double ret = d1 + d2 + d3;
		// return ret;
		return ret.toString();
	}

	public double calculateDistance(Vector3D pos, Vector3D ref) {
		double u = (ref.getU() - pos.getU());
		double v = (ref.getV() - pos.getV());
		double w = (ref.getW() - pos.getW());

		return Math.sqrt((u * u) + (v * v) + (w * w));
	}

	class TrigDistance implements Comparable<TrigDistance> {

		double du;
		double dv;
		double dw;

		public TrigDistance(double u, double v, double w) {
			du = u;
			dv = v;
			dw = w;
		}

		public double getDu() {
			return du;
		}

		public double getDv() {
			return dv;
		}

		public double getDw() {
			return dw;
		}

		public int compareTo(TrigDistance t) {
			int ret;
			double diffu = (this.getDu() - t.getDu());
			double diffv = (this.getDv() - t.getDv());
			double diffw = (this.getDw() - t.getDw());
			double sum = diffu + diffv + diffw;
			if (sum == 0) {
				ret = 0;
			} else if (sum > 0) {
				ret = 1;
			} else {
				ret = -1;
			}

			return ret;
		}

	}

	class TDComparator implements Comparator<TrigDistance> {

		public int compare(TrigDistance t1, TrigDistance t2) {
			int ret;
			double diffu = (t1.getDu() - t2.getDu());
			double diffv = (t1.getDv() - t2.getDv());
			double diffw = (t1.getDw() - t2.getDw());
			double sum = diffu + diffv + diffw;
			if (sum == 0) {
				ret = 0;
			} else if (sum > 0) {
				ret = 1;
			} else {
				ret = -1;
			}

			return ret;
		}

	}
}
