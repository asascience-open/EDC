/*
 * Applied Science Associates, Inc.
 * Copyright 2009. All Rights Reserved.
 *
 * SosData.java
 *
 * Created on Aug 25, 2009 @ 11:51:16 AM
 */
package com.asascience.sos;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.jdom.Document;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

import cern.colt.Timer;
import com.asascience.sos.types.DIF;
import com.asascience.sos.types.Generic;
import com.asascience.sos.types.SWE;

//import com.sun.xml.internal.fastinfoset.util.StringArray;
/**
 * 
 * @author DAS <dstuebe@asascience.com>
 */
public class SosData {

  private Generic sosType;
  private String myUrl;
  private float requestTime;
  private float parseTime;
  SimpleDateFormat dateFormatter;

  public SosData(String URL) {
    myUrl = URL;
  }

  public Boolean checkSOS() {

    Boolean ping = false;

    URL server;
    try {
      server = new URL(myUrl);
    } catch (MalformedURLException e1) {
      e1.printStackTrace();
      return ping;
    }

    HttpURLConnection connection = null;
    try {
      connection = (HttpURLConnection) server.openConnection();
    } catch (IOException e) {
      e.printStackTrace();
      return ping;
    }

    try {
      connection.connect();
    } catch (IOException e) {
      e.printStackTrace();
      return ping;
    }

    connection.disconnect();

    return true;

  }

  public boolean parseSosGetCapabilities() {
    Timer stopwatch = new Timer();

    String capRequest = "?request=GetCapabilities&service=SOS";

    System.out.println("Reading SOS URL: " + myUrl + capRequest);

    // Start Timing
    stopwatch.start();

    SAXBuilder getCapBuilder = new SAXBuilder(); // parameters control
    // validation, etc
    Document getCapDoc = null;
    try {
      getCapDoc = getCapBuilder.build(myUrl + capRequest);
    } catch (JDOMException e) {
      System.out.println("SOS at: " + myUrl + "; is not well-formed.");
      return false;
    } catch (IOException e) {
      System.out.println("SOS at: " + myUrl + "; is inaccessible");
      return false;
    }

    stopwatch.stop();
    requestTime = stopwatch.seconds();
    System.out.println("Seconds to get SOS capabilities request: " + requestTime);

    try {
      XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());
      Writer writer = new StringWriter(4 * 10 ^ 7); // 40 mb
      outputter.output(getCapDoc, writer);
      float mysize = writer.toString().length();
      System.out.println("Size of SOS capabilities request (mB)= ~" + mysize / 1000000.0);
    } catch (IOException e) {
      System.out.println("Could not get the size of the xml document");
      return false;
    }

    String type = getSOSType();
    if (type.equalsIgnoreCase("dif")) {
      sosType = new DIF(getCapDoc);
    } else if (type.equalsIgnoreCase("swe")) {
      sosType = new SWE(getCapDoc);
    } else {
      System.out.println("Could not figure out of the output was DIF or SWE");
      return false;
    }
    sosType.setURL(myUrl);
    return true;

  }

  /**
   *
   * @param value
   *            a space separated string of double values of length 2
   * @return double[2] or null
   */
  public static double[] parseBnds(String value) {

    double[] bnds = null;

    String[] values = value.trim().split(" ");

    if (values != null && values.length >= 2) {

      bnds = new double[2];

      try {

        bnds[0] = Double.parseDouble(values[0]);
        bnds[1] = Double.parseDouble(values[1]);

      } catch (NumberFormatException e) {
        bnds = null;
      }

    }

    return bnds;
  }

  // public static void setSelectedSensorCnt(int selectedSensorCnt) {
  // SosData.selectedSensorCnt = selectedSensorCnt;
  // }
  public void setRequestTime(float requestTime) {
    this.requestTime = requestTime;
  }

  public float getRequestTime() {
    return requestTime;
  }

  public void setParseTime(float parseTime) {
    this.parseTime = parseTime;
  }

  public Generic getData() {
    return sosType;
  }

  public float getParseTime() {
    return parseTime;
  }

  private String getSOSType() {
    return "DIF";
  }
}
