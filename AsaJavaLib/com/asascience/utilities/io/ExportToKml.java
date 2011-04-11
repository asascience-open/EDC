/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * ExportToKml.java
 *
 * Created on Sep 25, 2008, 1:19:23 PM
 *
 */
package com.asascience.utilities.io;

import java.awt.Color;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.asascience.utilities.Utils;

/**
 * 
 * @author cmueller_mac
 */
public class ExportToKml {

  private StringBuilder objectDoc = new StringBuilder();
  private StringBuilder kmlDoc = new StringBuilder();
  private HashMap<String, String> styles = new HashMap<String, String>();
  private BufferedWriter writer = null;

  /**
   * Creates a new instance of ExportToKml
   *
   * @throws IOException
   */
  public ExportToKml(String outPath, String layerName, String desc) throws IOException {
    outPath = Utils.appendExtension(outPath, "kml");
    writer = new BufferedWriter(new FileWriter(outPath));

    writer.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
    writer.write("<kml xmlns=\"http://www.opengis.net/kml/2.2\">");
    writer.write("<Document>");
    writer.write("<name>");
    writer.write(layerName);
    writer.write("</name><description>");
    writer.write(desc);
    writer.write("</description>");

    // kmlDoc.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
    // kmlDoc.append("<kml xmlns=\"http://www.opengis.net/kml/2.2\">");
    // kmlDoc.append("<Document>");
    // kmlDoc.append("<name>");
    // kmlDoc.append(layerName);
    // kmlDoc.append("</name><description>");
    // kmlDoc.append(desc);
    // kmlDoc.append("</description>");
    // kmlDoc.append(blackLineYellowFill);
    // kmlDoc.append(blackLineRedFill);
    // kmlDoc.append(blackLineBlueFill);
    // kmlDoc.append(redLineRedFill);
  }

  public String addStyle(Color fCol, Color lCol) {
    StringBuilder styleID = new StringBuilder();
    StringBuilder style = new StringBuilder();
    StringBuilder fill = new StringBuilder();
    StringBuilder line = new StringBuilder();

    /** Build the kml color strings. kml uses an "aabbggrr" hex string. */
    fill.append(getHex(fCol.getAlpha()));
    fill.append(getHex(fCol.getBlue()));
    fill.append(getHex(fCol.getGreen()));
    fill.append(getHex(fCol.getRed()));

    line.append(getHex(lCol.getAlpha()));
    line.append(getHex(lCol.getBlue()));
    line.append(getHex(lCol.getGreen()));
    line.append(getHex(lCol.getRed()));

    /** Build the style id string. */
    styleID.append(line.toString());
    styleID.append("Line");
    styleID.append(fill.toString());
    styleID.append("Fill");
    if (styles.containsKey(styleID.toString())) {
      return styleID.toString();
    }

    /** Build the style string. */
    style.append("<Style id=\"");
    style.append(styleID.toString());
    style.append("\"><LineStyle><color>");
    style.append(line.toString());
    style.append("</color><width>4</width></LineStyle><PolyStyle><color>");
    style.append(fill.toString());
    style.append("</color></PolyStyle></Style>");

    styles.put(styleID.toString(), style.toString());

    return styleID.toString();
  }

  private String[] getTimeSpan(GregorianCalendar dateTime, int increment) {
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
    sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
    String sTime = sdf.format(dateTime.getTime());
    String eTime = null;
    if (increment != -1) {
      /**
       * Add the increment and then subtract 1 second so that the
       * timespans do not overlap.
       */
      dateTime.add(GregorianCalendar.MILLISECOND, increment);
      dateTime.add(GregorianCalendar.SECOND, -1);
      eTime = sdf.format(dateTime.getTime());
    }
    return new String[]{sTime, eTime};
  }

  private String getHex(int in) {
    StringBuilder out = new StringBuilder(Integer.toHexString(in));
    while (out.length() < 2) {
      out.append("0");
    }
    return out.toString();
  }

  public boolean addPoint(String name, double lat, double lon, GregorianCalendar dateTime, int increment,
          String style, double data) {
    try {
      String[] tSpan = getTimeSpan(dateTime, increment);

      // objectDoc.append(buildPoint(name, lat, lon, tSpan[0], tSpan[1],
      // style, data));
      writer.write(buildPoint(name, lat, lon, tSpan[0], tSpan[1], style, data));
      return true;
    } catch (Exception ex) {
      Logger.getLogger(ExportToKml.class.getName()).log(Level.SEVERE, null, ex);
    }
    return false;
  }

  private String buildPoint(String name, double lat, double lon, String sTime, String eTime, String style, double data) {
    StringBuilder point = new StringBuilder();
    point.append("<Placemark><name>");
    point.append(name);
    point.append("</name><description/>");
    if (sTime != null) {// has start time
      if (eTime != null) {// has end time - use TimeSpan
        point.append("<TimeSpan><begin>");
        point.append(sTime);
        point.append("</begin><end>");
        point.append(eTime);
        point.append("</end></TimeSpan>");
      } else {// no end time - use TimeStamp
        point.append("<TimeStamp><when>");
        point.append(sTime);
        point.append("</when></TimeStamp>");
      }
    }
    // /** Add style. */
    // point.append("<styleUrl>#");
    // point.append(style);
    // point.append("</styleUrl>");
    /** If not NaN, add data information. */
    if (!Double.isNaN(data)) {
      point.append("<ExtendedData><Data name=\"Data\"><value>");
      point.append(String.valueOf(data));
      point.append("</value></Data></ExtendedData>");
    }
    point.append("<Point><coordinates>");
    point.append(lon);
    point.append(",");
    point.append(lat);
    point.append("</coordinates></Point></Placemark>");

    return point.toString();
  }

  public boolean addPolygon(String name, String desc, double[] lats, double[] lons, GregorianCalendar dateTime,
          int increment, String style, double data) {
    try {
      String[] tSpan = getTimeSpan(dateTime, increment);
      // System.out.println(time);
      // objectDoc.append(buildPolygon(name, desc, lats, lons, tSpan[0],
      // tSpan[1], style, data));
      writer.write(buildPolygon(name, desc, lats, lons, tSpan[0], tSpan[1], style, data));
      return true;
    } catch (Exception ex) {
      Logger.getLogger(ExportToKml.class.getName()).log(Level.SEVERE, null, ex);
    }
    return false;
  }

  public boolean addPolygon(String name, String desc, float[] lats, float[] lons, GregorianCalendar dateTime,
          int increment, String style, double data) {
    return addPolygon(name, desc, Utils.floatArrayToDoubleArray(lats), Utils.floatArrayToDoubleArray(lons),
            dateTime, increment, style, data);
  }

  private String buildPolygon(String name, String desc, double[] lats, double[] lons, String startTime,
          String endTime, String style, double data) {
    /** Make sure the coordinates are legitamate */
    if (lats == null | lons == null) {
      return null;
    }
    if (lats.length == 0 | lons.length == 0) {
      return null;
    }
    if (lats.length != lons.length) {
      return null;
    }

    /** Build the polygon string */
    StringBuilder poly = new StringBuilder();
    poly.append("<Placemark><name>");
    poly.append(name);
    poly.append("</name><description>");
    poly.append(desc);
    poly.append("</description>");
    if (startTime != null) {// has start time
      if (endTime != null) {// has end time - use TimeSpan
        poly.append("<TimeSpan><begin>");
        poly.append(startTime);
        poly.append("</begin><end>");
        poly.append(endTime);
        poly.append("</end></TimeSpan>");
      } else {// no end time - use TimeStamp
        poly.append("<TimeStamp><when>");
        poly.append(startTime);
        poly.append("</when></TimeStamp>");
      }
    }
    /** Add style. */
    poly.append("<styleUrl>#");
    poly.append(style);
    poly.append("</styleUrl>");
    /** If not NaN, add data information. */
    if (!Double.isNaN(data)) {
      poly.append("<ExtendedData><Data name=\"Data\"><value>");
      poly.append(String.valueOf(data));
      poly.append("</value></Data></ExtendedData>");
    }
    poly.append("<Polygon>");
    // poly.append("<extrude>1</extrude>");
    // poly.append("<altitudeMode>absolute</altitudeMode>");
    poly.append("<altitudeMode>clampToGround</altitudeMode>");
    poly.append("<outerBoundaryIs><LinearRing><coordinates>");
    for (int i = 0; i < lats.length; i++) {
      poly.append(lons[i]);
      poly.append(",");
      poly.append(lats[i]);
      poly.append(",");
      poly.append("100");
      if (i != lats.length) {
        poly.append(" ");
      }
    }
    poly.append("</coordinates></LinearRing></outerBoundaryIs></Polygon></Placemark>");

    return poly.toString();
  }

  public boolean finish(String outPath) {
    outPath = Utils.appendExtension(outPath, "kml");
    // FileWriter fw = null;
    try {
      /** Add the style elements. */
      for (String s : styles.keySet()) {
        // kmlDoc.append(styles.get(s));
        writer.write(styles.get(s));
      }
      // /** Add all of the objects. */
      // kmlDoc.append(objectDoc.toString());
      /** Add the finish tags to the document. */
      // kmlDoc.append("</Document></kml>");
      writer.write("</Document></kml>");
      // fw = new FileWriter(outPath);
      // fw.write(kmlDoc.toString());
      // fw.flush();
      // fw.close();
      // fw = null;
      return true;
    } catch (Exception ex) {
      Logger.getLogger(ExportToKml.class.getName()).log(Level.SEVERE, null, ex);
    } finally {
      // if(fw != null){
      // try{
      // fw.flush();
      // fw.close();
      // }catch(IOException ex){
      // Logger.getLogger(ExportToKml.class.getName()).
      // log(Level.SEVERE, null, ex);
      // }
      // }
      if (writer != null) {
        try {
          writer.flush();
          writer.close();
        } catch (IOException ex) {
          Logger.getLogger(ExportToKml.class.getName()).log(Level.SEVERE, null, ex);
        }
      }
    }

    return false;
  }
}
