/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * OMArrow.java
 *
 * Created on Jun 30, 2008, 12:52:56 PM
 *
 */
package com.asascience.openmap.omgraphic;

import java.awt.Color;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.asascience.utilities.Utils;
import com.bbn.openmap.LatLonPoint;
import com.bbn.openmap.geo.Geo;
import com.bbn.openmap.omGraphics.OMLine;
import com.bbn.openmap.omGraphics.OMPoly;

/**
 * 
 * @author cmueller_mac
 */
public class OMArrow extends OMPoly {

	private double u;
	private double v;
	private double cLat;
	private double cLon;
	private double dir;
	private double speed;
	private String name;
	private Color lineColor = Color.BLACK;
	private Color selectColor = Color.YELLOW;
	private float scalingFactor = 1f;// default is no scaling
	private boolean fromDir = false;
	private boolean scaleBySpeed = true;
	private double[] arrowShapeLocs;

	/**
	 * Creates a new instance of GLVector
	 * 
	 *@param centerLat
	 *            is the latitude of the center of the vector
	 * 
	 *@param centerLon
	 *            is the longitude of the center of the vector
	 * 
	 *@param uu
	 *            is the u(x) component of the vector
	 * 
	 *@param vv
	 *            is the v(y) component of the vector
	 * @param vectorColor
	 * @param scalingFactor
	 */
	public OMArrow(double centerLat, double centerLon, double uu, double vv, Color vectorColor, float scalingFactor,
		boolean fromDir, boolean scaleBySpeed, boolean useBlackOutline) {
		setScalingFactor(scalingFactor);
		cLat = centerLat;
		cLon = centerLon;
		u = uu;
		v = vv;
		this.fromDir = fromDir;
		this.scaleBySpeed = scaleBySpeed;

		// double[] da = constructArrow(calculateLineD());
		// float[] fa = Utils.doubleArrayToFloatArray(da);
		// for(int i = 0; i < da.length - 1; i = i + 2){
		// System.out.println(da[i+1] + "," + da[i]);
		// }
		// for(int i = 0; i < da.length - 1; i = i + 2){
		// System.out.println(fa[i+1] + "," + fa[i]);
		// }

		// double[] da = constructArrowM(calculateLineD());
		// for(int i = 0; i < da.length - 1; i = i + 2){
		// System.out.println(da[i+1] + "," + da[i]);
		// }

		this.setLocation(Utils.doubleArrayToFloatArray(constructArrowM(calculateLineD())), OMPoly.DECIMAL_DEGREES);

		this.setLineType(OMLine.LINETYPE_RHUMB);
		this.setLinePaint(lineColor);
		if (!useBlackOutline) {
			this.setLinePaint(vectorColor);
		}
		this.setFillPaint(vectorColor);
		this.setSelectPaint(selectColor);
	}

	public double[] constructArrowM(double[] startEnd) {
		double[] ret = new double[16];
		try {
			double sy = startEnd[0];
			double sx = startEnd[1];
			double ey = startEnd[2];
			double ex = startEnd[3];
			double dx = ex - sx;
			double dy = ey - sy;

			// Geo gS = new Geo(startEnd[0], startEnd[1]);
			// Geo gE = new Geo(startEnd[2], startEnd[3]);

			double dist;
			if (scaleBySpeed) {
				dist = Math.sqrt((dx * dx) + (dy * dy));
			} else {
				dist = scalingFactor * 0.1d;
				double[] newEnds = pointFromDistAngle(sx, sy, dist, dir);
				ey = newEnds[0];
				ex = newEnds[1];
			}
			// System.out.println(dist);
			// if((dir > 90 & dir < 180)){
			// System.out.println("in 90 -> 180");
			// }
			// if((dir > 270 & dir < 360)){
			// System.out.println("in 270 -> 360");
			// }

			double ang = dir;
			double pang = ((ang + 90) > 360) ? (ang + 90 - 360) : ang + 90;
			double opang = ((pang + 180) > 360) ? (pang + 180 - 360) : pang + 180;

			// double ang = dir * (Math.PI/180);
			// double pang = ang + (Math.PI / 2);
			// double opang = ang + Math.PI;

			double pitDist = dist * 0.75d;
			double sDist = dist * 0.1d;
			double wDist = dist * 0.05d;

			// calculate the 'bottom right' point
			double[] pts = pointFromDistAngle(sx, sy, wDist, pang);
			double bry = pts[0];
			double brx = pts[1];

			// calculate and assign the 'bottom left' point
			pts = pointFromDistAngle(sx, sy, wDist, opang);
			double bly = pts[0];
			double blx = pts[1];

			// calculate and assign the 'right pit' point
			pts = pointFromDistAngle(brx, bry, pitDist, ang);
			double pry = pts[0];
			double prx = pts[1];

			// calculate and assign the 'left pit' point
			pts = pointFromDistAngle(blx, bly, pitDist, ang);
			double ply = pts[0];
			double plx = pts[1];

			// calculate and assign the 'right shoulder' point
			pts = pointFromDistAngle(prx, pry, sDist, pang);
			double sry = pts[0];
			double srx = pts[1];

			// calculate and assign the 'left shoulder' point
			pts = pointFromDistAngle(plx, ply, sDist, opang);
			double sly = pts[0];
			double slx = pts[1];

			// fill the arrow array in "lat, lon, lat, lon, ..." order
			ret[0] = bry;
			ret[1] = brx;
			ret[2] = pry;
			ret[3] = prx;
			ret[4] = sry;
			ret[5] = srx;
			ret[6] = ey;
			ret[7] = ex;
			ret[8] = sly;
			ret[9] = slx;
			ret[10] = ply;
			ret[11] = plx;
			ret[12] = bly;
			ret[13] = blx;
			ret[14] = bry;
			ret[15] = brx;

			arrowShapeLocs = ret;

			return ret;
		} catch (Exception ex) {
			Logger.getLogger(OMArrow.class.getName()).log(Level.SEVERE, null, ex);
		}

		return null;
	}

	public double[] pointFromDistAngle(double inx, double iny, double dist, double ang) {
		double[] ret = new double[2];
		try {
			double nang = ang;
			// move the angle to the "upper right" quadrant
			// while(nang > 90){
			// nang = nang - 90;
			// }
			nang = nang * (Math.PI / 180);
			double cosres = Math.cos(nang);// * (180/Math.PI);
			double sinres = Math.sin(nang);
			double outy = dist * cosres;
			double outx = dist * sinres;
			if (fromDir) {
				outy = dist * sinres;
				outx = dist * cosres;
			}

			// if((dir > 90 & dir < 180)){
			// outy = dist * sinres;
			// outx = dist * cosres;
			// }
			// if((dir > 270 & dir < 360)){
			// outy = dist * sinres;
			// outx = dist * cosres;
			// }
			// double pp = (Math.pow(dist, 2) - Math.pow(outy, 2));
			// double outx = Math.sqrt(Math.abs(pp));

			// if(ang >= 270){// -x & +y
			// outx = -Math.abs(outx);
			// outy = Math.abs(outy);
			// }else if(ang >= 180){// -x & -y
			// outx = -Math.abs(outx);
			// outy = -Math.abs(outy);
			// }else if(ang >= 90){// +x & -y
			// outx = Math.abs(outx);
			// outy = -Math.abs(outy);
			// }else{
			// outx = Math.abs(outx);
			// outy = Math.abs(outy);
			// }

			ret[0] = iny + outy;
			ret[1] = inx + outx;

			return ret;
		} catch (Exception ex) {
			Logger.getLogger(OMArrow.class.getName()).log(Level.SEVERE, null, ex);
		}

		return null;
	}

	public double[] calculateLineD() {
		try {
			double lat1 = 0f, lat2 = 0f, lon1 = 0f, lon2 = 0f;

			/** caluclate the raw direction */
			// double at = Math.atan(u/v);
			// double absat = Math.atan((Math.abs(u) / Math.abs(v)));
			// double dat = at * (180 / Math.PI);
			// double dabsat = absat * (180 / Math.PI);
			if (fromDir) {
				dir = (Math.atan(Math.abs(v) / Math.abs(u))) * (180 / Math.PI);
				if (u < 0) {// -u
					if (v < 0) {// -v --> SW quad
						dir += 180;// correct
					} else {// +v
						dir += 2 * (90 - dir);// correct
					}
				} else {// +u
					if (v < 0) {// -v
						dir += (2 * (90 - dir)) + 180;// correct
					} else {// +v
						// do nothing
					}
				}
			} else {
				dir = (Math.atan(Math.abs(u) / Math.abs(v))) * (180 / Math.PI);
				if (v < 0) {// -v
					if (u < 0) {// -u --> SW quad
						dir += 180;// correct
					} else {// +u
						dir += 2 * (90 - dir);// correct
					}
				} else {// +v
					if (u < 0) {// -u
						dir += (2 * (90 - dir)) + 180;// correct
					} else {// +u
						// do nothing
					}
				}
			}
			// double odir = dir;

			// /**adjust the direction based on mathematical quadrant*/
			// if(u < 0){//quadrant II
			// dir += 180;
			// if(v < 0){//quadrant III
			// }
			// }else if(u > 0){//quadrant I
			// if(v < 0){//quadrant IV
			// dir += 360;
			// }
			// }else{//special cases -> 270 & 90
			// if(v < 0){
			// dir = 270;
			// }else{
			// dir = 90;
			// }
			// }

			// if(v < 0){//-v
			// if(u < 0){//-u --> SW quad
			// dir += 180;//correct
			// }else{//+u
			// dir += 2 * (90 - dir);//correct
			// }
			// }else{//+v
			// if(u < 0){//-u
			// dir += (2 * (90 - dir)) + 180;//correct
			// }else{//+u
			// //do nothing
			// }
			// }

			// /**change direction from mathematical to geographical*/
			// dir = 450 - dir;
			// if(dir > 360){
			// dir -= 360;
			// }

			// System.out.println(u + "," + v + "," + odir + "," + dir);

			speed = Math.sqrt((Math.pow(u, 2)) + Math.pow(v, 2));

			// logger.info("Direction = " + String.valueOf(dir));
			// logger.info("Speed = " + String.valueOf(speed));

			double uFactor = u * scalingFactor;
			double vFactor = v * scalingFactor;

			// TODO: Remove these float casts when OM is updated to double
			// precision.
			lat1 = cLat;
			lon1 = cLon;
			// lat2 = (float)(cLat + v);
			// lon2 = (float)(cLon + u);

			lat2 = (cLat + vFactor);
			lon2 = (cLon + uFactor);

			// System.err.println("sLa=" + lat1 + " eLa=" + lat2 +
			// " sLo="+lon1+" eLo="+lon2);
			// for vectors who's center is in the middle of the grid
			// lat1 = cLat - (vFactor/2);
			// lon1 = cLon - (uFactor/2);
			// lat2 = cLat + (vFactor/2);
			// lon2 = cLon + (uFactor/2);

			double[] lls = new double[] { lat1, lon1, lat2, lon2 };
			return lls;
		} catch (Exception ex) {
			Logger.getLogger(OMArrow.class.getName()).log(Level.SEVERE, null, ex);
		}
		return null;
	}

	// <editor-fold defaultstate="collapsed" desc=" Old Arrow Construction ">

	/** Build the arrow shape */
	private double[] constructArrow(double[] startEndLocs) {
		try {
			// this.startEndLocs = startEndLocs;
			double[] ret = new double[16];

			// init a start Geo(startLat, startLon)
			Geo gS = new Geo(startEndLocs[0], startEndLocs[1]);
			// init an end Geo(endLat, endLon)
			Geo gE = new Geo(startEndLocs[2], startEndLocs[3]);

			double az = gS.azimuth(gE);
			double naz = az + Math.PI;
			double paz = az + (Math.PI * 0.5d);
			double npaz = paz + Math.PI;
			double dist = gS.distance(gE);
			double pitDist = dist * 0.75d;
			double sDist = dist * 0.1d;
			double wDist = dist * 0.05d;

			Geo gBr = gS.offset(wDist, paz);
			Geo gBl = gS.offset(wDist, npaz);

			Geo gPr = gBr.offset(pitDist, az);
			Geo gPl = gBl.offset(pitDist, az);

			Geo gSr = gPr.offset(sDist, paz);
			Geo gSl = gPl.offset(sDist, npaz);

			// double az = Geo.degrees(gS.azimuth(gE));
			// double naz = az + 180;
			// naz = (naz > 360) ? naz - 360 : naz;
			// double paz = az + 90;
			// paz = (paz > 360) ? paz - 360 : paz;
			// double npaz = paz + 180;
			// npaz = (paz > 360) ? npaz - 360 : npaz;

			// double dist = gS.distance(gE);
			// double pitDist = dist * 0.75d;
			// double sDist = dist * 0.1d;
			// double wDist = dist * 0.05d;

			// Geo gBr = gS.offset(wDist, Geo.radians(paz));
			// Geo gBl = gS.offset(wDist, Geo.radians(npaz));
			//            
			// Geo gPr = gBr.offset(pitDist, Geo.radians(az));
			// Geo gPl = gBl.offset(pitDist, Geo.radians(az));
			//            
			// Geo gSr = gPr.offset(sDist, Geo.radians(paz));
			// Geo gSl = gPl.offset(sDist, Geo.radians(npaz));

			ret[0] = gBr.getLatitude();
			ret[1] = gBr.getLongitude();
			ret[2] = gPr.getLatitude();
			ret[3] = gPr.getLongitude();
			ret[4] = gSr.getLatitude();
			ret[5] = gSr.getLongitude();
			ret[6] = gE.getLatitude();
			ret[7] = gE.getLongitude();
			ret[8] = gSl.getLatitude();
			ret[9] = gSl.getLongitude();
			ret[10] = gPl.getLatitude();
			ret[11] = gPl.getLongitude();
			ret[12] = gBl.getLatitude();
			ret[13] = gBl.getLongitude();
			ret[14] = gBr.getLatitude();
			ret[15] = gBr.getLongitude();

			return ret;
		} catch (Exception ex) {
			Logger.getLogger(OMArrow.class.getName()).log(Level.SEVERE, null, ex);
		}
		return null;
	}

	public float[] calculateLine() {
		try {
			float lat1 = 0f, lat2 = 0f, lon1 = 0f, lon2 = 0f;

			// caluclate the raw direction
			dir = (Math.atan(v / u)) * (180 / Math.PI);
			// adjust the direction based on mathematical quadrant

			if (u < 0) {// quadrant II
				dir += 180;
				if (v < 0) {// quadrant III
				}
			} else if (u > 0) {// quadrant I
				if (v < 0) {// quadrant IV
					dir += 360;
				}
			} else {// special cases -> 270 & 90
				if (v < 0) {
					dir = 270;
				} else {
					dir = 90;
				}
			}

			// change direction from mathematical to geographical
			dir = 450 - dir;
			if (dir > 360) {
				dir -= 360;
			}

			speed = Math.sqrt((Math.pow(u, 2)) + Math.pow(v, 2));

			// logger.info("Direction = " + String.valueOf(dir));
			// logger.info("Speed = " + String.valueOf(speed));

			double uFactor = u * scalingFactor;
			double vFactor = v * scalingFactor;

			// TODO: Remove these float casts when OM is updated to double
			// precision.
			lat1 = (float) cLat;
			lon1 = (float) cLon;
			// lat2 = (float)(cLat + v);
			// lon2 = (float)(cLon + u);

			lat2 = (float) (cLat + vFactor);
			lon2 = (float) (cLon + uFactor);

			// System.err.println("sLa=" + lat1 + " eLa=" + lat2 +
			// " sLo="+lon1+" eLo="+lon2);
			// for vectors who's center is in the middle of the grid
			// lat1 = cLat - (vFactor/2);
			// lon1 = cLon - (uFactor/2);
			// lat2 = cLat + (vFactor/2);
			// lon2 = cLon + (uFactor/2);

			float[] lls = new float[] { lat1, lon1, lat2, lon2 };
			return lls;
		} catch (Exception ex) {
			Logger.getLogger(OMArrow.class.getName()).log(Level.SEVERE, null, ex);
		}
		return null;
	}

	// </editor-fold>

	// <editor-fold defaultstate="collapsed" desc="Get/Set Region">
	public LatLonPoint getVectorPosition() {
		return new LatLonPoint(cLat, cLon);
	}

	public double getVectorStartLat() {
		return cLat;
	}

	public double getVectorStartLon() {
		return cLon;
	}

	public double getU() {
		return u;
	}

	public double getV() {
		return v;
	}

	public double getDir() {
		return dir;
	}

	public double getSpeed() {
		return speed;
	}

	public float getScalingFactor() {
		return scalingFactor;
	}

	public void setU(double set) {
		u = set;
	}

	public void setV(double set) {
		v = set;
	}

	public void setDir(double set) {
		dir = set;
	}

	public void setSpeed(double set) {
		speed = set;
	}

	public void setScalingFactor(float set) {
		scalingFactor = set;
	}

	public String getName() {
		return name;
	}

	public String getPropertyDescription(String separator) {
		return getPropertyDescription(separator, "");
	}

	public String getPropertyDescription(String separator, String vectUnits) {
		return getUDescription() + vectUnits + separator + getVDescription() + vectUnits + separator
			+ getDirectionDescription() + separator + getSpeedDescription() + vectUnits;
	}

	public String getUDescription() {
		return "u = " + String.valueOf(Utils.roundDouble(u, 5));// + " m/s";
	}

	public String getVDescription() {
		return "v = " + String.valueOf(Utils.roundDouble(v, 5));// + " m/s";
	}

	public String getDirectionDescription() {
		return "Direction = " + String.valueOf(Utils.roundDouble(dir, 2));
	}

	public String getSpeedDescription() {
		return "Speed = " + String.valueOf(Utils.roundDouble(speed, 3));
	}

	public void setName(String set) {
		name = set;
	}

	public double[] getArrowShapeLocs() {
		return arrowShapeLocs;
	}
	// </editor-fold>
}
