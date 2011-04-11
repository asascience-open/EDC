/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * OMVectorLine.java
 *
 * Created on Jul 1, 2008, 2:48:02 PM
 *
 */
package com.asascience.openmap.omgraphic;

import java.awt.Color;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.asascience.utilities.Utils;
import com.bbn.openmap.omGraphics.OMArrowHead;
import com.bbn.openmap.omGraphics.OMLine;

/**
 * 
 * @author cmueller_mac
 */
public class OMVectorLine extends OMLine {

  private double u;
  private double v;
  private double cLat;
  private double cLon;
  private double dir;
  private double speed;
  private double scalingFactor;
  private boolean fromDir;

  /** Creates a new instance of OMVectorLine */
  public OMVectorLine(double centerLat, double centerLon, double uu, double vv, Color vectorColor,
          float scalingFactor, boolean useArrowhead, boolean fromDir) {
    cLat = centerLat;
    cLon = centerLon;
    u = uu;
    v = vv;
    this.scalingFactor = scalingFactor;
    this.fromDir = fromDir;
    // this.setLL(calculateLine());
    this.setLL(Utils.doubleArrayToFloatArray(calculateLineD()));
    this.setLinePaint(vectorColor);
    this.setRenderType(OMLine.RENDERTYPE_LATLON);
    this.setLineType(OMLine.LINETYPE_RHUMB);
    if (useArrowhead) {
      this.setArrowHead(new OMArrowHead(OMArrowHead.ARROWHEAD_DIRECTION_FORWARD, 100, 5, 15));
    }
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

      double[] lls = new double[]{lat1, lon1, lat2, lon2};
      return lls;
    } catch (Exception ex) {
      Logger.getLogger(OMVectorLine.class.getName()).log(Level.SEVERE, null, ex);
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

      float[] lls = new float[]{lat1, lon1, lat2, lon2};
      return lls;
    } catch (Exception ex) {
      Logger.getLogger(OMVectorLine.class.getName()).log(Level.SEVERE, null, ex);
    }
    return null;
  }

  public String getPropertyDescription(String separator) {
    return getPropertyDescription(separator, "");
  }

  public String getPropertyDescription(String separator, String vectUnits) {
    return getUDescription() + separator + vectUnits + separator + getVDescription() + separator + vectUnits
            + separator + getDirectionDescription() + separator + getSpeedDescription() + vectUnits;
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
}
