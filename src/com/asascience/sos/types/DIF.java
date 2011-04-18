/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.asascience.sos.types;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import org.jdom.Attribute;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.xpath.XPath;
import cern.colt.Timer;
import com.asascience.sos.SensorContainer;
import com.asascience.sos.SosData;
import com.asascience.sos.VariableContainer;
import com.bbn.openmap.layer.util.http.HttpConnection;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import org.apache.commons.lang.StringUtils;

/**
 *
 * @author Kyle
 */
public class DIF extends Generic implements SOSTypeInterface {

  public DIF(Document xmlDoc) {
    super(xmlDoc);
    type = "DIF";
    parseSensors();
  }

  @Override
  public boolean parseSensors() {

    Timer stopwatch = new Timer();

    // Start Timing
    stopwatch.start();

    sensorList = getCapXPath(getCapDoc);

    if (sensorList != null) {

      // See if this service has a network all sensor
      // (http://sdf.ndbc.noaa.gov/sos/server.php?request=GetCapabilities&service=SOS)

      if (sensorList.get(0).getName().equals("network-all")) {

        // System.out.println("Service has 'network-all' sensor (concatinated sensor list)");

        SensorContainer NetworkAll = sensorList.remove(0);

        // Set min and max location Data
        NESW = NetworkAll.getNESW();

        // Set min and max Time
        startTime = NetworkAll.getStartTime();
        endTime = NetworkAll.getEndTime();

      } else {

        // Set default values for NESW
        NESW = new double[4];
        NESW[0] = -90.;
        NESW[1] = -180.;
        NESW[2] = 90.;
        NESW[3] = 180.;

        // Set default values for Time;
        startTime = new Date();
        endTime = new Date();

        startTime.setYear(300); // set a way future start time...
        endTime.setYear(0); // set an old end time

        for (SensorContainer sensor : sensorList) {

          NESW[0] = Math.max(sensor.getNESW()[0], NESW[0]);
          NESW[1] = Math.max(sensor.getNESW()[1], NESW[1]);
          NESW[2] = Math.min(sensor.getNESW()[2], NESW[2]);
          NESW[3] = Math.min(sensor.getNESW()[3], NESW[3]);

          startTime = startTime.before(sensor.getStartTime()) ? startTime : sensor.getStartTime();
          endTime = endTime.after(sensor.getStartTime()) ? endTime : sensor.getEndTime();
        }

        // Make sure the year is at least 1990
        Date minTime = new Date();
        minTime.setYear(90);
        minTime.setDate(1);
        minTime.setHours(00);
        minTime.setMinutes(00);
        minTime.setMonth(1);
        minTime.setSeconds(00);

        if (startTime.before(minTime)) {
          startTime = minTime;
          System.out.println("================================= Date Error ===================================");
          System.out.println("Observation Data before 1990 is not available through this client at this time.");
          System.out.println("================================================================================");
        }
      }
      float parseTime = stopwatch.seconds();
      System.out.println("Seconds to parse SOS capabilities: " + parseTime);
      System.out.println("Parsed SOS capabilities, found: " + sensorList.size() + " valid sensors!");

      setUniqueVariables();
      return true;
    } else {
      return false;
    }
  }

  @SuppressWarnings("unchecked")
  public List getCapXPath(Document doc) {
    List<SensorContainer> sensorList = null;

    String XPATH_GETEACH_OBS_OFFERING = "//asa:ObservationOffering";
    String XPATH_GETEACH_OBS_ATTRIBUTE = "./@gml:id";
    String XPATH_GETEACH_LOWER_CORNER = ".//gml:lowerCorner";
    String XPATH_GETEACH_UPPER_CORNER = ".//gml:upperCorner";
    String XPATH_GETEACH_DESCRIPTION = ".//gml:description";
    String XPATH_GETEACH_PROCEDURE = ".//asa:procedure";
    String XPATH_GETEACH_XLINK_ATTRIBUTE = "./@xlink:href";
    String XPATH_GETEACH_GML_NAME = ".//gml:name";
    String XPATH_GETEACH_PROPERTY = ".//asa:observedProperty";
    String XPATH_GETEACH_BEGINPOSITION = ".//gml:beginPosition";
    String XPATH_GETEACH_ENDPOSITION = ".//gml:endPosition";
    String XPATH_GETEACH_INDETERMINATEPOSITION = "./@indeterminatePosition";
    String XPATH_GETEACH_RESPONSEFORMAT = ".//asa:responseFormat";

    XPath XPATH_Obs_Offering = null;
    XPath XPATH_Obs_Attribute = null;

    XPath XPATH_Lower_Corner = null;
    XPath XPATH_Upper_Corner = null;

    XPath XPATH_Description = null;
    XPath XPATH_Procedure = null;
    XPath XPATH_Xlink_Attribute = null;
    XPath XPATH_Gml_Name = null;
    XPath XPATH_Property = null;

    XPath XPATH_BeginPosition = null;
    XPath XPATH_EndPosition = null;
    XPath XPATH_IndeterminatePosition = null;

    XPath XPATH_ResponseFormat = null;

    try {

      XPATH_Obs_Offering = XPath.newInstance(XPATH_GETEACH_OBS_OFFERING);
      XPATH_Obs_Offering.addNamespace("asa", "http://www.opengis.net/sos/1.0");
      XPATH_Obs_Attribute = XPath.newInstance(XPATH_GETEACH_OBS_ATTRIBUTE);

      XPATH_Lower_Corner = XPath.newInstance(XPATH_GETEACH_LOWER_CORNER);
      XPATH_Upper_Corner = XPath.newInstance(XPATH_GETEACH_UPPER_CORNER);

      XPATH_Description = XPath.newInstance(XPATH_GETEACH_DESCRIPTION);
      XPATH_Procedure = XPath.newInstance(XPATH_GETEACH_PROCEDURE);
      XPATH_Procedure.addNamespace("asa", "http://www.opengis.net/sos/1.0");
      XPATH_Xlink_Attribute = XPath.newInstance(XPATH_GETEACH_XLINK_ATTRIBUTE);

      XPATH_Gml_Name = XPath.newInstance(XPATH_GETEACH_GML_NAME);

      XPATH_Property = XPath.newInstance(XPATH_GETEACH_PROPERTY);
      XPATH_Property.addNamespace("asa", "http://www.opengis.net/sos/1.0");

      XPATH_BeginPosition = XPath.newInstance(XPATH_GETEACH_BEGINPOSITION);
      XPATH_EndPosition = XPath.newInstance(XPATH_GETEACH_ENDPOSITION);
      XPATH_IndeterminatePosition = XPath.newInstance(XPATH_GETEACH_INDETERMINATEPOSITION);

      XPATH_ResponseFormat = XPath.newInstance(XPATH_GETEACH_RESPONSEFORMAT);
      XPATH_ResponseFormat.addNamespace("asa", "http://www.opengis.net/sos/1.0");

    } catch (JDOMException e1) {
      e1.printStackTrace();
    }

    // Get the list of variables in this service
    List parameterObjects = null;
    try {
      parameterObjects = XPath.selectNodes(doc,
              "//ows:Parameter[@name='observedProperty']/ows:AllowedValues/ows:Value");

      int cnt = parameterObjects.size();
      varNames = new String[cnt];
      int index = 0;
      for (Object o : parameterObjects) {

        if (!(o instanceof Element)) {
          System.out.println("xpathGetCapabilities: Unknown Instance of Parameter (Property Names)!");
          return null;
        }

        Element names = (Element) o;
        varNames[index] = names.getValue();
        index = index + 1;
      }

    } catch (JDOMException e) {
      System.out.println("xpathGetCapabilities: XPATH error reading Parameter names!");
      e.printStackTrace();
      return null;
    } catch (NullPointerException enull) {
      enull.printStackTrace();
      System.out.println("xpathGetCapabilities: error reading Parameter names - returned null!");
      return null;
    }

    // Get the list of observations and parse it:
    List obsList = null;
    try {
      obsList = XPATH_Obs_Offering.selectNodes(doc);

    } catch (JDOMException e) {
      System.out.println("xpathGetCapabilities: error reading ObservationOffering!");
      e.printStackTrace();
    }

    if (obsList == null || obsList.size() <= 0) {
      System.out.println("xpathGetCapabilities: obsList is null or empty!");
      return sensorList;
    }

    sensorList = new ArrayList<SensorContainer>();
    // Instantiate double array here and copy inside the setter...
    double[] NESW = new double[4];

    // Create DateFormatter
    // SimpleDateFormat dateFormatter_seconds = new
    // SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
    // SimpleDateFormat dateFormatter_noseconds = new
    // SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'");
    dateFormatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
    TimeZone tz = TimeZone.getTimeZone("GMT");
    // dateFormatter_seconds.setTimeZone(tz);
    // dateFormatter_noseconds.setTimeZone(tz);
    dateFormatter.setTimeZone(tz);

    for (Object o : obsList) {

      if (!(o instanceof Element)) {
        System.out.println("xpathGetCapabilities: Unknown Instance of ObservationOffering!");
        continue;
      }

      Element obsOffering = (Element) o;

      SensorContainer sensor = new SensorContainer();

      // GET the gmlId of the Observation
      try {
        Attribute gmlId = (Attribute) XPATH_Obs_Attribute.selectSingleNode(obsOffering);
        sensor.setName(gmlId.getValue());
        // gmlId = (Attribute)
        // XPath.selectSingleNode(obsOffering,"./@gml:id");
      } catch (JDOMException e1) {
        System.out.println("Can not find gmlId: JDOM exception");
        e1.printStackTrace();
      } catch (NullPointerException enull) {
        enull.printStackTrace();
        System.out.println("Can not find gmlId Attributre: returned null attribute");
      }

      // Get Upper Corner:
      double[] ll = null;
      try {
        Element lowerCorner = (Element) XPATH_Lower_Corner.selectSingleNode(obsOffering);
        ll = SosData.parseBnds(lowerCorner.getValue());
      } catch (JDOMException e) {
        e.printStackTrace();
        System.out.println("Can not find Lower Corner: JDOM exception");
      } catch (NullPointerException enull) {
        enull.printStackTrace();
        System.out.println("Can not find Lower Corner: returned null lowerCorner");
      }

      // Get Upper Corner:
      double[] ur = null;
      try {
        Element upperCorner = (Element) XPATH_Upper_Corner.selectSingleNode(obsOffering);
        // upperCorner = (Element)
        // XPath.selectSingleNode(obsOffering,XPATH_GETEACH_UPPER_CORNER);
        ur = SosData.parseBnds(upperCorner.getValue());

      } catch (JDOMException e) {
        e.printStackTrace();
        System.out.println("Can not find Upper Corner: JDOM exception");
      } catch (NullPointerException enull) {
        System.out.println("Can not find Upper Corner: returned null upperCorner");
      }

      if (ll != null && ur != null) {
        NESW[0] = ur[0];
        NESW[1] = ur[1];
        NESW[2] = ll[0];
        NESW[3] = ll[1];
        
        sensor.setNESW(NESW);
      }

      // Get Description
      try {
        Element description = (Element) XPATH_Description.selectSingleNode(obsOffering);
        if (description != null) {
          sensor.setDescription(description.getValue());
        }
      } catch (JDOMException e) {
        e.printStackTrace();
        System.out.println("Can not find Description: JDOM exception");
      } catch (NullPointerException enull) {
        // enull.printStackTrace();
        System.out.println("Can not find Description: returned null description");
      }

      // Get gml:Name
      try {
        Element name = (Element) XPATH_Gml_Name.selectSingleNode(obsOffering);
        sensor.setGmlName(name.getValue());
      } catch (JDOMException e) {
        e.printStackTrace();
        System.out.println("Can not find Description: JDOM exception");
        sensor = null;
        continue;
      } catch (NullPointerException enull) {
        // enull.printStackTrace();
        System.out.println("Can not find Description: returned null description");
        sensor = null;
        continue;
      }

      // Get BeginPosition
      String beginString = null;
      try {
        Element begin = (Element) XPATH_BeginPosition.selectSingleNode(obsOffering);
        beginString = begin.getValue().trim();
        
        // Change "yyyy-MM-ddTHH:mmZ" to "yyyy-MM-ddTHH:mm:ssZ"
        if (beginString.length() == 17) {
          beginString = beginString.substring(0, 15).concat(":00Z");
        } else if (beginString.length() > 20) {
          beginString = beginString.substring(0, 18).concat("Z");
        }

        sensor.setStartTime(dateFormatter.parse(beginString));
        
      } catch (JDOMException e) {
        e.printStackTrace();
        System.out.println("Can not find BeginPosition: JDOM exception");
        sensor = null;
        continue;
      } catch (NullPointerException enull) {
        // enull.printStackTrace();
        System.out.println("Can not find BeginPosition: returned null beginPosition");
        sensor = null;
        continue;
      } catch (ParseException e) {
        // e.printStackTrace();
        System.out.println("================= Can not parse beginPosition ==================");
        System.out.println("= Invalid entry returned from SOS server: '" + beginString.trim() + "' =");
        System.out.println("====== Returned null beginPosition and skipping this sensor ====");
        System.out.println("======== Please contact the server admin to fix this issue =====");

        sensor = null;
        continue;
      }

      // Get endPosition
      String endString = null;
      try {
        Element end = (Element) XPATH_EndPosition.selectSingleNode(obsOffering);

        Attribute indPos = (Attribute) XPATH_IndeterminatePosition.selectSingleNode(end);

        endString = end.getValue().trim();

        if (endString != null && endString.length() > 0) {

          // Change "yyyy-MM-ddTHH:mmZ" to "yyyy-MM-ddTHH:mm:ssZ"
          if (endString.trim().length() == 17) {
            endString = endString.substring(0, 15).concat(":00Z");
          }
          sensor.setEndTime(dateFormatter.parse(endString));
        } else if (indPos.getValue() != null
                && (indPos.getValue().equals("now") || indPos.getValue().equals("unknown"))) {
          sensor.setEndTime(new Date()); // Defaults to now
        }

      } catch (JDOMException e) {
        e.printStackTrace();
        System.out.println("Can not find EndPosition: JDOM exception");
        sensor = null;
        continue;
      } catch (NullPointerException enull) {
        // enull.printStackTrace();
        System.out.println("Can not find EndPosition: returned null endPosition");
        sensor = null;
        continue;
      } catch (ParseException e) {
        System.out.println("================== Can not parse EndPosition ===================");
        System.out.println("= Invalid entry returned from SOS server: '" + endString.trim() + "' =");
        System.out.println("======== Returned null endPosition and skipping this sensor ====");
        System.out.println("======== Please contact the server admin to fix this issue =====");

        // e.printStackTrace();
        sensor = null;
        continue;
      }

      // Get Procedure
      List procedureList = null;
      try {
        procedureList = XPATH_Procedure.selectNodes(obsOffering);

      } catch (JDOMException e) {
        System.out.println("xpathGetCapabilities: error reading procedureList!");
        e.printStackTrace();
      }

      // Get ResponseFormats
      ArrayList<String> responseFormats = new ArrayList<String>();
      List responseFormatList = null;
      try {
        responseFormatList = XPATH_ResponseFormat.selectNodes(obsOffering);
      } catch (JDOMException e) {
        System.out.println("xpathGetCapabilities: error reading responseFormat!");
        e.printStackTrace();
        sensor = null;
        continue;
      }
      int cnt, ind;
      cnt = responseFormatList.size();
      for (ind = 0; ind < cnt; ind++) {
        Element format = (Element) responseFormatList.get(ind);
        responseFormats.add(format.getValue().trim());
      }
      sensor.setResponseFormats(responseFormats);

      // Get Properties (variables)
      List propertyList = null;
      try {
        propertyList = XPATH_Property.selectNodes(obsOffering);

      } catch (JDOMException e) {
        System.out.println("xpathGetCapabilities: error reading procedureList!");
        e.printStackTrace();
        sensor = null;
        continue;
      }

      cnt = propertyList.size();
      for (ind = 0; ind < cnt; ind++) {

        VariableContainer var = null;
        Element procedure = null;
        Element property = null;
        try {
          procedure = (Element) procedureList.get(ind);
        } catch (ClassCastException e) {
          System.out.println("xpathGetCapabilities: error casting procedure and properties!");
          continue;
        } catch (IndexOutOfBoundsException ee) {
          // System.out.println("xpathGetCapabilities: More properties than procedures!");
        }

        try {
          property = (Element) propertyList.get(ind);
        } catch (ClassCastException e) {
          System.out.println("xpathGetCapabilities: error casting procedure and properties!");
          continue;
        }

        // Escape if there is no property - that means there is nothing
        // to do...
        if (property != null) {
          var = new VariableContainer();
        } else {
          continue;
        }

        try {
          Attribute xlink = (Attribute) XPATH_Xlink_Attribute.selectSingleNode(property);

          String Name = xlink.getValue();
          var.setProperty(Name);

          Character divider;
          if (Name.contains("http://")) {
            Name = Name.substring(Name.lastIndexOf('/') + 1);
          }

          if (Name.contains(":")) {
            divider = ':';
          } else if (Name.contains("#")) {
            divider = '#';
          } else {
            divider = ' ';
          }

          if (!divider.equals(' ')) {
            var.setName(Name.substring(Name.lastIndexOf(divider) + 1));
          } else {
            var.setName(Name);
          }

        } catch (JDOMException e) {
          e.printStackTrace();
          System.out.println("Can not find Xlink Attribute: JDOM exception");
          var = null;
        } catch (NullPointerException enull) {
          // enull.printStackTrace();
          System.out.println("Can not find Xlink Attribute: returned null Attribute");
          var = null;
        }

        // Only add the procedure if it exists...
        if (procedure != null) {
          try {
            Attribute xlink = (Attribute) XPATH_Xlink_Attribute.selectSingleNode(procedure);
            var.setProcedure(xlink.getValue());

          } catch (JDOMException e) {
            e.printStackTrace();
            System.out.println("Can not find Xlink Attribute: JDOM exception");
            var = null;
            sensor = null;
            continue;
          } catch (NullPointerException enull) {
            // enull.printStackTrace();
            System.out.println("Can not find Xlink Attribute: returned null Attribute");
            var = null;
            sensor = null;
            continue;
          }
        }

        if (var != null) {
          sensor.addVariable(var);
        }
      }
      // Add the Sensor to the list
      sensorList.add(sensor);
    }
    return sensorList;
  }

  public String buildRequest(SensorContainer sensor) {
    try {
      ArrayList<String> params = new ArrayList<String>();
      // request
      params.add("request=GetObservation");
      // service
      params.add("service=SOS");
      // version
      params.add("version=1.0.0");
      // responseFormat
      params.add("responseFormat="+URLEncoder.encode(responseFormat,"utf-8"));
      // offering
      params.add("offering=" + URLEncoder.encode(sensor.getGmlName(), "utf-8"));
      // observedProperty
      ArrayList<String> variableQueryString = new ArrayList<String>();
      for (VariableContainer variable : selectedVariables) {
        for (VariableContainer sv : sensor.getVarList()) {
          if (sv.compareTo(variable) == 0) {
            variableQueryString.add(variable.getName());
            break;
          }
        }
      }
      params.add("observedproperty=" + StringUtils.join(variableQueryString,','));
      // eventTime
      params.add("eventtime=" + dateFormatter.format(selectedStartTime) + "/" + dateFormatter.format(selectedEndTime));
      
      return sosURL + "?" + StringUtils.join(params,'&');
    } catch (Exception e) {
      return "";
    }
  }

  @Override
  public void getObservations() {

    double numSens = getSelectedSensorCount();
    double countSens = 0;
    String requestURL;

    // Are we loading an XSL?
    /*
    SAXBuilder xslBuilder = new SAXBuilder();
    if (responseFormat == "text/xml;schema=\"ioos/0.6.1\"") {
      pcs.firePropertyChange("message", null, "Loading XSL Schema");
      Document xslDoc = null;
      try {
        xslDoc = xslBuilder.build("http://ioos.gov/library/ioos_gmlv061_to_csv.xsl");
      } catch (JDOMException e) {
        System.out.println("XSL at: http://ioos.gov/library/ioos_gmlv061_to_csv.xsl; is not well-formed.");
        e.printStackTrace();
        return;
      } catch (IOException e) {
        System.out.println("SOS at: " + sosURL + "; is inaccessible");
        e.printStackTrace();
        return;
      }
      pcs.firePropertyChange("message", null, "Done (Loading XSL Schema)");
    }
    */
    Timer stopwatch = new Timer();

    for (SensorContainer sensor : selectedSensors) {
      stopwatch.reset();
      stopwatch.start();
      pcs.firePropertyChange("message", null, "Sensor: " + sensor.getName());
      pcs.firePropertyChange("message", null, "- Building Request String...");
      
      requestURL = buildRequest(sensor);

      int written;
      try {
        URL u = new URL(requestURL);
        pcs.firePropertyChange("message", null, "- Making Request (" + requestURL + ")...");
        HttpURLConnection ht = (HttpURLConnection) u.openConnection();
        ht.setDoInput(true);
        ht.setRequestMethod("GET");
        InputStream is = ht.getInputStream();
        pcs.firePropertyChange("message", null, "- Streaming Results to File...");
        File f = new File(homeDir + sensor.getName() + ".txt");
        OutputStream output = new BufferedOutputStream(new FileOutputStream(f));
        byte[] buffer = new byte[2048];
        int len = 0;
        written = 0;
        while (-1 != ( len = is.read(buffer))) {
          output.write(buffer,0,len);
          written += len;
        }
        is.close();
        output.flush();
        output.close();
      } catch (MalformedURLException e) {
        pcs.firePropertyChange("message", null, "- BAD URL, skipping sensor");
        continue;
      } catch (IOException io) {
        pcs.firePropertyChange("message", null, "- BAD CONNECTION, skipping sensor");
        continue;
      }
      /*
      SAXBuilder difBuilder = new SAXBuilder(); // parameters control
      Document difDoc = null;
      try {
        difDoc = difBuilder.build(requestURL);
      } catch (JDOMException e) {
        System.out.println("SOS at: " + requestURL + "; is not well-formed.");
        e.printStackTrace();
        continue;
      } catch (IOException e) {
        System.out.println("SOS at: " + sosURL + "; is inaccessible");
        e.printStackTrace();
        continue;
      }
      */
      

      // Write file

      /*
      try {
        XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());
        Writer writer = new StringWriter(4 * 10 ^ 7); // 40 mb
        outputter.output(difDoc, writer);
        float mysize = writer.toString().length();
        System.out.println("Size of SOS Observations request (mB)= ~" + mysize / 1000000.0);

      } catch (IOException e) {
        System.out.println("couldnt get the size of the xml document");
      }
      
      stopwatch.start();

      List<Element> myList = null;
      try {
        myList = transform(difDoc, xslDoc);
      } catch (JDOMException e) {
        e.printStackTrace();
      }

      try {
        Writer fstream = new FileWriter(new File(homeDir + sensor.getName() + ".txt"));
        BufferedWriter out = new BufferedWriter(fstream);
        // out.write(url + "/n"); // dump url to file header?
        out.write(myList.toString());
        out.close();
      } catch (IOException e) {
        System.out.println("couldnt output transformed xml");
      }
      */
      countSens++;
      pcs.firePropertyChange("message", null, "- Completed " + written + " bytes in " + stopwatch.elapsedTime() + " seconds.");
      int prog = Double.valueOf(countSens / numSens * 100).intValue();
      pcs.firePropertyChange("progress", null, prog);
    } // End Sensor List
    pcs.firePropertyChange("progress", null, 100);
  }
}
