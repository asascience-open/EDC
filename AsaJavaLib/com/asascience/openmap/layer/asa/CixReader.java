/*
 * CixReader.java
 *
 * Created on December 5, 2007, 9:43 AM
 *
 * Applied Science Associates, Inc.
 * Copyright 2007.  All rights reserved.
 */

package com.asascience.openmap.layer.asa;

import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.TimeZone;

import com.asascience.utilities.Cell;
import com.asascience.utilities.Vector3D;
import com.asascience.utilities.io.LERandomAccessFile;

/**
 * 
 * @author CBM
 */
public class CixReader {
	static final int TOP = 0;
	static final int MIDDLE = 1;
	static final int BOTTOM = 2;

	private String cixfile;
	// private int lSeq;
	private int numTimes;
	private long[] timeSteps;
	private List<Cell> dataCells;
	// private List<ArrayList> dataCells;
	// private List<ArrayList> data;

	private double[] lats;
	private double[] lons;
	// private double[] us;
	// private double[] vs;
	// private double[] depths;

	// private List<Double> latList;
	// private List<Double> lonList;

	private GregorianCalendar startCal;

	/**
	 * Creates a new instance of CixReader
	 * 
	 * @param file
	 */
	public CixReader(String file) {
		cixfile = file;

		loadCIXTimes();
		loadCIXGrid();
	}

	public long getStartTime() {
		return timeSteps[0];
	}

	public long getEndTime() {
		return timeSteps[timeSteps.length - 1];
	}

	public long getTimeIncrement() {
		return (timeSteps[1] - timeSteps[0]);
	}

	public double[] getLats() {
		return lats;
	}

	public double[] getLons() {
		return lons;
	}

	private void loadCIXGrid() {
		int nBytes;
		int ndMax;
		int nRecL;
		int nTime;
		float dLon;
		float dLat;
		float oLon;
		float oLat;
		int nDummy;
		float depth;
		short i;
		short j;
		short level;
		short subI;
		short subJ;
		int lSeq;

		LERandomAccessFile raf = null;
		try {
			File f = new File(cixfile);
			if (f.exists()) {
				// System.err.println(f.getAbsolutePath());
				raf = new LERandomAccessFile(f, "r");

				if (raf != null) {

					raf.seek(0);
					nRecL = raf.readInt();
					ndMax = raf.readInt();
					lSeq = raf.readInt();
					raf.seek(42);
					dLon = raf.readFloat();
					dLat = raf.readFloat();
					nDummy = raf.readInt();
					raf.seek(56);
					oLon = raf.readFloat();
					oLat = raf.readFloat();
					nTime = raf.readInt();
					nBytes = nRecL * (6 + nDummy + nTime - 1);

					// System.err.println("nRecL="+nRecL);
					// System.err.println("ndMax="+ndMax);
					// System.err.println("lSeq="+lSeq);
					// System.err.println("dLon="+dLon);
					// System.err.println("dLat="+dLat);
					// System.err.println("oLon="+oLon);
					// System.err.println("oLat="+oLat);
					// System.err.println("nTime="+nTime);
					// System.err.println("nBytes="+nBytes);

					// latList = new ArrayList<Double>();
					// lonList = new ArrayList<Double>();

					lats = new double[lSeq];
					lons = new double[lSeq];

					dataCells = new ArrayList<Cell>();
					// dataCells = new ArrayList<ArrayList>();
					// dataCells.add(0, new ArrayList<Cell>());

					// data = new ArrayList<ArrayList>();
					// data.add(0, new ArrayList<Integer>());//type 1 or 9
					// data.add(1, new ArrayList<Float>());//depth
					// data.add(2, new ArrayList<Float>());//u
					// data.add(3, new ArrayList<Float>());//v

					// data.get(0).clear();

					Cell myCell = null;
					for (int n = 0; n < lSeq; n++) {
						myCell = new Cell();

						nBytes += nRecL;
						raf.seek(nBytes);

						depth = raf.readFloat();
						i = raf.readShort();
						j = raf.readShort();
						level = raf.readShort();
						subI = raf.readShort();
						subJ = raf.readShort();

						// System.err.println("depth="+depth);
						// System.err.println("i="+i);
						// System.err.println("j="+j);
						// System.err.println("level="+level);
						// System.err.println("subI="+subI);
						// System.err.println("subJ="+subJ);

						if ((level != 0) || (subI != 9)) {
							if (level == 0) {

								myCell.p1.setLocation((oLon + ((i - 1) * dLon)), (oLat + ((j - 1) * dLat)));
								myCell.p3.setLocation((myCell.p1.getX() + dLon), (myCell.p1.getY() + dLat));

							} else {
								double dlon_s = dLon / Math.pow(2, level);
								double dlat_s = dLat / Math.pow(2, level);

								myCell.p1.setLocation((oLon + (dLon * (i - 1)) + (dlon_s * (subI - 1))), (oLat
									+ (dLat * (j - 1)) + (dlat_s * (subJ - 1))));
								myCell.p3.setLocation((myCell.p1.getX() + dlon_s), (myCell.p1.getY() + dlat_s));
							}
							myCell.p2.setLocation(myCell.p3.getX(), myCell.p1.getY());
							myCell.p4.setLocation(myCell.p1.getX(), myCell.p3.getY());

							// latList.add(myCell.getCenterY());
							// lonList.add(myCell.getCenterX());

							lats[n] = myCell.getCenterY();
							lons[n] = myCell.getCenterX();

							myCell.setDepth(depth);
							myCell.setType(1);

							// dataCells.get(0).add(myCell);
							dataCells.add(myCell);
							// data.get(0).add(1);
							// data.get(1).add(depth);

							// System.err.println("p1: x= "+myCell.p1.getX()+" y= "+myCell.p1.getY());
							// System.err.println("p2: x= "+myCell.p2.getX()+" y= "+myCell.p2.getY());
							// System.err.println("p3: x= "+myCell.p3.getX()+" y= "+myCell.p3.getY());
							// System.err.println("p4: x= "+myCell.p4.getX()+" y= "+myCell.p4.getY());
							// System.err.println("center x= "+myCell.getCenterX()+" y= "+myCell.getCenterY());

							// System.err.println(myCell.p1.getX()+" "+myCell.p1.getY());
							// System.err.println(myCell.p2.getX()+" "+myCell.p2.getY());
							// System.err.println(myCell.p3.getX()+" "+myCell.p3.getY());
							// System.err.println(myCell.p4.getX()+" "+myCell.p4.getY());
							// System.err.println(myCell.getCenterX()+" "+myCell.getCenterY());
						} else {
							// latList.add(Double.NaN);
							// lonList.add(Double.NaN);
							myCell.setType(9);
							lats[n] = Double.NaN;
							lons[n] = Double.NaN;
							// dataCells.get(0).add(null);
							dataCells.add(myCell);
							// data.get(0).add(9);
							// data.get(1).add((float)-999);
						}
					}
					// lats = (double[])latList.toArray(new
					// double[latList.size()]);

				}
			}
		} catch (EOFException ex) {
			ex.printStackTrace();
		} catch (IOException ex) {
			ex.printStackTrace();
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			if (raf != null)
				try {
					raf.close();
				} catch (IOException ex) {
					ex.printStackTrace();
				}
		}
	}

	private void loadCIXTimes() {
		LERandomAccessFile raf = null;
		try {
			File f = new File(cixfile);
			if (f.exists()) {
				// System.err.println(f.getAbsolutePath());
				raf = new LERandomAccessFile(f, "r");

				if (raf != null) {
					raf.seek(0);

					int nRecL = raf.readInt();
					raf.seek(64);
					numTimes = raf.readInt();

					// System.err.println("numTimes="+numTimes);

					short sy, sm, sd, sh, smin;
					raf.seek(70);
					sy = raf.readShort();
					sm = raf.readShort();
					sd = raf.readShort();
					sh = raf.readShort();
					smin = raf.readShort();

					// System.err.println("sy="+sy+" sm="+sm+" sd="+sd+" sh="+sh+" smin="+smin);

					// set the start calendar in the base class
					this.startCal = new GregorianCalendar(TimeZone.getTimeZone("UTC"));// Calendar.getInstance();
					// TO DO: Fix this calendar issue properly
					// subtracting 1 from month solves the problem. Calendar
					// MONTH is 0 based.
					// i.e. Calendar.DECEMBER = 11;
					// can also set everything but the Millisecond in one call
					this.startCal.set(sy, (sm - 1), sd, sh, smin, 0);
					// this.startCal.set(Calendar.MONTH, sm - 1);
					// this.startCal.set(Calendar.DAY_OF_MONTH, sd);
					// this.startCal.set(Calendar.YEAR, sy);
					// this.startCal.set(Calendar.HOUR_OF_DAY, sh);
					// this.startCal.set(Calendar.MINUTE, smin);
					// this.startCal.set(Calendar.SECOND, 0);
					this.startCal.set(Calendar.MILLISECOND, 0);

					// System.err.println(startCal.getTime());

					// Calendar currCal = Calendar.getInstance();
					long startTime = startCal.getTimeInMillis();
					timeSteps = new long[numTimes];
					float currVal;
					for (int n = 1; n <= numTimes; n++) {
						int nBytes = (nRecL * (6 + n - 1)) + 4;
						raf.seek(nBytes);
						currVal = raf.readFloat();// in hours

						// System.err.println("currVal="+currVal);

						// currCal = startCal;
						// currCal.add(Calendar.MINUTE, (int)(currVal * 60));
						// timeSteps[n-1] = currCal.getTimeInMillis();
						timeSteps[n - 1] = startTime + (long) (currVal * 60 * 60 * 1000);// convert
																							// hours
																							// to
																							// ms
						// System.err.println(timeSteps[n-1]);
					}
				}
			}
		} catch (EOFException ex) {
			ex.printStackTrace();
		} catch (IOException ex) {
			ex.printStackTrace();
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			if (raf != null)
				try {
					raf.close();
				} catch (IOException ex) {
					ex.printStackTrace();
				}
		}
	}

	public double[][] extractTimeSeries(int cellIndex) {
		double[][] uvData = null;
		LERandomAccessFile raf = null;
		try {
			File f = new File(cixfile);
			if (f.exists()) {
				raf = new LERandomAccessFile(f, "r");

				int nRecL, lSeq, nBases, nPotr, nBytes;
				float elev, tHours;
				raf.seek(0);
				nRecL = raf.readInt();
				raf.seek(8);
				lSeq = raf.readInt();

				if (nRecL <= 14) {
					nBases = 1;
				} else {
					nBases = (nRecL - 4) / 8;
				}
				uvData = new double[2][timeSteps.length];

				for (int t = 0; t < timeSteps.length; t++) {
					List<Float> u = new ArrayList<Float>();
					List<Float> v = new ArrayList<Float>();
					nBytes = nRecL * (6 + t);
					raf.seek(nBytes);
					nPotr = raf.readInt();
					tHours = raf.readFloat();
					nBytes = nRecL * (nPotr - 2);

					nBytes += (cellIndex * nRecL);
					raf.seek(nBytes);
					elev = raf.readFloat();
					for (int i = 0; i < nBases; i++) {
						u.add(raf.readFloat());
						v.add(raf.readFloat());
					}
					uvData[0][t] = u.get(0);
					uvData[1][t] = v.get(0);
				}
			}
			return uvData;
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			try {
				raf.close();
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
		return null;
	}

	public boolean loadCIXStep(long queryTime) {
		int tIndex = getTimeIndex(queryTime);
		if (tIndex == -1 || tIndex > timeSteps.length - 1)
			return false;

		LERandomAccessFile raf = null;
		try {
			File f = new File(cixfile);
			if (f.exists()) {
				raf = new LERandomAccessFile(f, "r");

				int nRecL, lSeq, nBases, nPotr, nBytes;
				float elev;

				raf.seek(0);
				nRecL = raf.readInt();
				raf.seek(8);
				lSeq = raf.readInt();

				if (nRecL <= 14) {
					nBases = 1;
				} else {
					nBases = (nRecL - 4) / 8;
				}

				nBytes = nRecL * (6 + tIndex);
				raf.seek(nBytes);
				nPotr = raf.readInt();

				nBytes = nRecL * (nPotr - 2);// byte pointer to beginning of
												// timestep

				Cell c = null;
				for (int i = 0; i < lSeq; i++) {
					// System.err.println(i+" : "+lSeq-1);
					c = dataCells.get(i);
					if (c.getType() != 9) {
						nBytes += nRecL;
						// System.err.println(nBytes);

						raf.seek(nBytes);
						elev = raf.readFloat();
						c.setElev(elev);

						c.resetCurrentVals();
						for (int n = 0; n < nBases; n++) {
							if (n == 0) {
								c.setUTop(raf.readFloat());
								c.setVTop(raf.readFloat());
							}
							if (n == 1) {
								c.setUMid(raf.readFloat());
								c.setVMid(raf.readFloat());
							}
							if (n == 2) {
								c.setUBot(raf.readFloat());
								c.setVBot(raf.readFloat());
							}
						}
					}
				}

				return true;
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			try {
				raf.close();
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}

		return false;
	}

	public Vector3D getCurrentAt(Vector3D position, int tIndex, int cIndex) {
		Vector3D ret = null;
		// int tIndex = getTimeIndex();
		// System.out.println("tIndex="+tIndex);
		// int cIndex = getCellIndex(position);
		// System.out.println("cIndex="+cIndex);

		LERandomAccessFile raf = null;
		File f = null;

		if (tIndex == -1 | cIndex == -1) {
			return ret;
		}

		try {
			f = new File(cixfile);
			if (f.exists()) {
				// System.out.println(f.getAbsolutePath());
				raf = new LERandomAccessFile(f, "r");

				if (raf != null) {
					int nRecL, nBases, nPotr, nBytes;
					float elev;
					float u;
					float v;
					float[] us;
					float[] vs;

					raf.seek(0);
					nRecL = raf.readInt();
					if (nRecL <= 14) {
						nBases = 1;
					} else {
						nBases = (nRecL - 4) / 8;
					}

					nBytes = nRecL * (6 + tIndex - 1);
					raf.seek(nBytes);
					nPotr = raf.readInt();

					us = new float[nBases];
					vs = new float[nBases];

					nBytes = nRecL * (nPotr - 2);// byte pointer to beginning of
													// timestep

					nBytes += nRecL * cIndex;
					raf.seek(nBytes);// the cell of interest
					elev = raf.readFloat();
					// cycle through levels - max of 3, 1=surface
					for (int i = 0; i < nBases; i++) {
						us[i] = raf.readFloat();
						vs[i] = raf.readFloat();
					}
					u = us[0];
					v = vs[0];

					ret = new Vector3D(u, v, 0);
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			try {
				f = null;
				raf.close();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		// System.out.println("curr: u="+ret.getU()+" v="+ret.getV());

		// ret=null;
		return ret;
	}

	public double getU(int timeI, int cellI) {
		return getU(timeI, cellI, CixReader.TOP);
	}

	public double getU(int timeI, int cellI, int level) {
		// loadCIXStep(timeSteps[timeI]);
		switch (level) {
			case 0:
				return dataCells.get(cellI).getUTop();
			case 1:
				return dataCells.get(cellI).getUMid();
			case 2:
				return dataCells.get(cellI).getUBot();
		}

		return Double.NaN;
	}

	public double getV(int timeI, int cellI) {
		return getV(timeI, cellI, CixReader.TOP);
	}

	public double getV(int timeI, int cellI, int level) {
		// loadCIXStep(timeSteps[timeI]);
		switch (level) {
			case 0:
				return dataCells.get(cellI).getVTop();
			case 1:
				return dataCells.get(cellI).getVMid();
			case 2:
				return dataCells.get(cellI).getVBot();
		}

		return Double.NaN;
	}

	public double[] getUVals(int level) {
		double[] ret = new double[dataCells.size()];
		switch (level) {
			case 0:// TOP
				for (int i = 0; i < dataCells.size(); i++) {
					ret[i] = dataCells.get(i).getUTop();
				}
				break;
			case 1:// MIDDLE
				for (int i = 0; i < dataCells.size(); i++) {
					ret[i] = dataCells.get(i).getUMid();
				}
				break;
			case 2:// BOTTOM
				for (int i = 0; i < dataCells.size(); i++) {
					ret[i] = dataCells.get(i).getUBot();
				}
				break;
		}

		return ret;
	}

	public double[] getVVals(int level) {
		double[] ret = new double[dataCells.size()];
		switch (level) {
			case 0:// TOP
				for (int i = 0; i < dataCells.size(); i++) {
					ret[i] = dataCells.get(i).getVTop();
				}
				break;
			case 1:// MIDDLE
				for (int i = 0; i < dataCells.size(); i++) {
					ret[i] = dataCells.get(i).getVMid();
				}
				break;
			case 2:// BOTTOM
				for (int i = 0; i < dataCells.size(); i++) {
					ret[i] = dataCells.get(i).getVBot();
				}
				break;
		}

		return ret;
	}

	public int getTimeIndex(long queryTime) {
		int ret = -1;
		long qt = queryTime;

		for (int i = 0; i < timeSteps.length; i++) {
			if (qt <= timeSteps[timeSteps.length - 1])
				if (qt >= timeSteps[i])
					ret = i;
		}

		return ret;
	}

	public int getCellIndex(Vector3D position) {
		int ret = -1;

		Cell c;
		for (int i = 0; i < dataCells.size(); i++) {
			c = dataCells.get(i);
			if (c.contains(position))// checks the horizontal position
			{
				return i;
			}
		}

		return ret;
	}

	public long[] getTimeSteps() {
		return timeSteps;
	}
}
