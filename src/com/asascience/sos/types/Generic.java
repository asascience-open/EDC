/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.asascience.sos.types;

import cern.colt.Timer;
import java.beans.PropertyChangeEvent;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.jdom.Document;
import com.asascience.sos.SensorContainer;
import com.asascience.sos.SensorPoint;
import com.asascience.sos.VariableContainer;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.transform.JDOMResult;
import org.jdom.transform.JDOMSource;
import ucar.unidata.geoloc.LatLonPointImpl;
import ucar.unidata.geoloc.LatLonRect;

/**
 *
 * @author Kyle
 */
public class Generic implements SOSTypeInterface, PropertyChangeListener {

  protected List<SensorContainer> sensorList;
  protected ArrayList<VariableContainer> variableList;
  protected Date selectedStartTime;
  protected Date selectedEndTime;
  protected List<SensorContainer> selectedSensors;
  protected List<VariableContainer> selectedVariables;
  protected double[] NESW;
  protected Date startTime;
  protected Date endTime;
  protected SimpleDateFormat dateFormatter;
  protected String sosURL;
  protected String homeDir;
  protected Document getCapDoc;
  protected String type;
  protected double timeInterval;
  protected int numTimeSteps;
  protected String responseFormat;
  protected String[] varNames;
  protected PropertyChangeSupport pcs;

  public Generic(Document xmlDoc) {
    variableList = new ArrayList();
    selectedSensors = new ArrayList();
    pcs = new PropertyChangeSupport(this);
    getCapDoc = xmlDoc;
  }

  public void process() {
    parseSensors();
  }
  
  public void setUniqueVariables() {
    // Make a unique list of VariableContainers
    pcs.firePropertyChange("message", null, "Parsing out unique variables");
    Timer stopwatch = new Timer();
    stopwatch.start();
    
    boolean found = false;
    for (SensorContainer sensor : sensorList) {
      for (VariableContainer variable : sensor.getVarList()) {
        if (variableList.isEmpty()) {
          variableList.add(variable);
        } else {
          found = false;
          for (VariableContainer varInList : variableList) {
            if (variable.compareTo(varInList) == 0) {
              found = true;
              break;
            }
          }
          if (!found) {
            variableList.add(variable);
          }
        }
      }
    }
    pcs.firePropertyChange("message", null, "Found " + variableList.size() + " unique variables in " + stopwatch.seconds() + " seconds");
  }

  public void setSelectedSensors(List<SensorPoint> sensorPoints) {
    selectedSensors.clear();
    for (SensorPoint p : sensorPoints) {
      selectedSensors.add(p.getSensor());
    }
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
  public void setResponseFormat(String format) {
    responseFormat = format;
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

  protected List<Element> transform(Document doc, Document xsldoc) throws JDOMException {
    try {
      JDOMSource xslSource = new JDOMSource(xsldoc);

      Transformer transformer = TransformerFactory.newInstance().newTransformer(xslSource);

      JDOMSource in = new JDOMSource(doc);
      JDOMResult out = new JDOMResult();

      transformer.transform(in, out);
      
      return out.getResult();
    } catch (TransformerException e) {
      throw new JDOMException("XSLT Transformation failed", e);
    }
  }

  public String getType() {
    return type;
  }

  public boolean parseSensors() {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  public void getObservations() {
    // Implemented in the subclasses
    throw new UnsupportedOperationException("Not supported yet.");
  }

  public void setHomeDir(String homeDir) {
    this.homeDir = homeDir;
  }

  public void setURL(String URL) {
    sosURL = URL;
  }

  public Date getStartTime() {
    return startTime;
  }

  public Date getEndTime() {
    return endTime;
  }

  public double[] getNESW() {
    return NESW;
  }

  public double getTimeInterval() {
    return timeInterval;
  }

  public int getNumTimeSteps() {
    return numTimeSteps;
  }

  public List<VariableContainer> getVariables() {
    return variableList;
  }

  public List<SensorContainer> getSensors() {
    return sensorList;
  }

  public LatLonRect getBBOX() {
    LatLonPointImpl uL = new LatLonPointImpl(NESW[0], NESW[3]);
    LatLonPointImpl lR = new LatLonPointImpl(NESW[2], NESW[1]);
    LatLonRect llr = new LatLonRect(uL, lR);
    return llr;
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
