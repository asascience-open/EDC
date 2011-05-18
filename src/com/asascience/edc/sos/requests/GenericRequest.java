/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.asascience.edc.sos.requests;

import cern.colt.Timer;
import com.asascience.edc.sos.SensorContainer;
import com.asascience.edc.sos.SensorPoint;
import com.asascience.edc.sos.VariableContainer;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.awt.FileDialog;
import javax.swing.JFrame;
import org.apache.commons.lang.StringUtils;

/**
 *
 * @author Kyle
 */
public class GenericRequest implements PropertyChangeListener, SosRequestInterface {

  protected String responseFormat;
  protected Date selectedStartTime;
  protected Date selectedEndTime;
  protected List<SensorContainer> selectedSensors;
  protected List<VariableContainer> selectedVariables;
  protected PropertyChangeSupport pcs;
  protected SimpleDateFormat dateFormatter;
  protected String sosURL;
  protected String homeDir;
  protected String fileSuffix;
  protected JFrame parentFrame;

  public GenericRequest(String url) {
    fileSuffix = "txt";
    sosURL = url;
    pcs = new PropertyChangeSupport(this);
    dateFormatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
  }

  public GenericRequest(GenericRequest gr) {
    this(gr.sosURL);
    selectedSensors = gr.selectedSensors;
    selectedVariables = gr.selectedVariables;
    selectedStartTime = gr.selectedStartTime;
    selectedEndTime = gr.selectedEndTime;
    parentFrame = gr.parentFrame;
    homeDir = gr.homeDir;
  }

  public void setParentFrame(JFrame frame) {
    parentFrame = frame;
  }

  public void getObservations() {

    double numSens = getSelectedSensorCount();
    double countSens = 0;
    String requestURL;

    File savePath = chooseSavePath();

    Timer stopwatch = new Timer();

    for (SensorContainer sensor : selectedSensors) {
      stopwatch.reset();
      stopwatch.start();
      pcs.firePropertyChange("message", null, "Sensor: " + sensor.getName());
      pcs.firePropertyChange("message", null, "- Building Request String");

      requestURL = buildRequest(sensor);

      int written;
      try {
        URL u = new URL(requestURL);
        pcs.firePropertyChange("message", null, "- Making Request (" + requestURL + ")");
        HttpURLConnection ht = (HttpURLConnection) u.openConnection();
        ht.setDoInput(true);
        ht.setRequestMethod("GET");
        InputStream is = ht.getInputStream();
        pcs.firePropertyChange("message", null, "- Streaming Results to File");
        String filename = chooseFilename(savePath, sensor.getName());
        File f = new File(filename);
        OutputStream output = new BufferedOutputStream(new FileOutputStream(f));
        byte[] buffer = new byte[2048];
        int len = 0;
        written = 0;
        while (-1 != (len = is.read(buffer))) {
          output.write(buffer, 0, len);
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

      countSens++;
      pcs.firePropertyChange("message", null, "- Completed " + written + " bytes in " + stopwatch.elapsedTime() + " seconds.");
      int prog = Double.valueOf(countSens / numSens * 100).intValue();
      pcs.firePropertyChange("progress", null, prog);
    } // End Sensor List
    pcs.firePropertyChange("progress", null, 100);
  }

  public String getResponseFormatValue() {
    return responseFormat;
  }

  public void setResponseFormatValue(String s) {
    responseFormat = s;
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
      params.add("responseFormat=" + URLEncoder.encode(getResponseFormatValue(), "utf-8"));
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
      params.add("observedproperty=" + StringUtils.join(variableQueryString, ','));
      // eventTime
      params.add("eventtime=" + dateFormatter.format(selectedStartTime) + "/" + dateFormatter.format(selectedEndTime));

      return sosURL + "?" + StringUtils.join(params, '&');
    } catch (Exception e) {
      return "";
    }
  }

  protected String getNameFromURL(String url) {
    try {
      URL target = new URL(url);
      return target.getHost().replace('.','_') + File.separator + getNameFromDate();
    } catch (MalformedURLException e) {
      return "";
    }
  }
  
  protected String getNameFromDate() {
    Date dateNow = new Date ();
    SimpleDateFormat formatted = new SimpleDateFormat("yyyy-MM-dd_HHmma");
    return new StringBuilder( formatted.format( dateNow ) ).toString();
  }

  public File chooseSavePath() {
    FileDialog outputPath = new FileDialog(parentFrame, "Create directory and save output files here...", FileDialog.SAVE);
    File containingFolder = new File(homeDir + File.separator + getNameFromURL(sosURL) + File.separator);
    if (!containingFolder.exists()) {
      containingFolder.mkdirs();
    }
    outputPath.setDirectory(containingFolder.getAbsolutePath());
    outputPath.setFile("Choose Output Directory (ignore this filename)");
    outputPath.setVisible(true);
    
    
    
    File newHomeDir = new File(outputPath.getDirectory());
    // Did the user use the new directory we created for them?
    if (!newHomeDir.getAbsolutePath().contains(containingFolder.getAbsolutePath())) {
      if (containingFolder.length() == 0) {
        containingFolder.delete();
        // Create a timestamped directory where the user has chosen
        newHomeDir = new File(outputPath.getDirectory() + File.separator + getNameFromDate());
        newHomeDir.mkdirs();
      }
    }
    return newHomeDir;
  }

  public String chooseFilename(File path, String sensorName) {
    return path.getAbsolutePath() + File.separator + sensorName + "." + fileSuffix;
  }

  public void validate() throws Exception {
    // Check to make sure the selected sensors contain at least one of
    // the selected variables.
    boolean found;
    List<SensorContainer> sensorsWithVariables = new ArrayList<SensorContainer>();
    for (SensorContainer s : selectedSensors) {
      for (VariableContainer v : selectedVariables) {
        found = false;
        for (VariableContainer vs : s.varList) {
          if (vs.compareTo(v) == 0) {
            if (!sensorsWithVariables.contains(s)) {
              sensorsWithVariables.add(s);
            }
            found = true;
            break;
          }
          if (found) {
            break;
          }
        }
      }
    }
    selectedSensors = sensorsWithVariables;
    if (selectedSensors.isEmpty()) {
      throw new Exception("None of the variables selected are available at the sensors selected");
    }
  }

  public void setSelectedSensors(List<SensorPoint> sensorPoints) {
    selectedSensors = new ArrayList();
    for (SensorPoint p : sensorPoints) {
      selectedSensors.add(p.getSensor());
    }
  }
  public List<SensorContainer> getSelectedSensors() {
    return selectedSensors;
  }
  public List<VariableContainer> getSelectedVariables() {
    return selectedVariables;
  }
  public int getSelectedSensorCount() {
    return selectedSensors.size();
  }
  public void setSelectedVariables(List<VariableContainer> variableContainer) {
    selectedVariables = variableContainer;
  }
  public int getSelectedVariableCount() {
    return selectedVariables.size();
  }
  public void setStartTime(Date startTime) {
    selectedStartTime = startTime;
  }
  public void setEndTime(Date endTime) {
    selectedEndTime = endTime;
  }
  public void setHomeDir(String homeDir) {
    this.homeDir = homeDir;
  }

  public void setFileSuffix(String s) {
    fileSuffix = s;
  }

  public void addPropertyChangeListener(java.beans.PropertyChangeListener l) {
    pcs.addPropertyChangeListener(l);
  }

  public void removePropertyChangeListener(java.beans.PropertyChangeListener l) {
    pcs.removePropertyChangeListener(l);
  }

  public void propertyChange(PropertyChangeEvent evt) {
    pcs.firePropertyChange(evt);
  }

}