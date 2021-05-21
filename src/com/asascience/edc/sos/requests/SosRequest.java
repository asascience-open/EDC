package com.asascience.edc.sos.requests;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.swing.JFrame;

import org.apache.commons.lang.StringUtils;

import com.asascience.edc.sos.SensorContainer;
import com.asascience.edc.sos.VariableContainer;
import com.asascience.edc.sos.map.SensorPoint;

import gov.nasa.worldwind.render.PointPlacemark;


/**
 * SosRequestInterface.java
 * 
 * @author Kyle Wilcox <kwilcox@asascience.com>
 */
public abstract class SosRequest implements PropertyChangeListener {

	public abstract void getObservations(File savePath);
	protected Date selectedStartTime;
	protected Date selectedEndTime;
	protected List<SensorContainer> selectedSensors;
	protected List<VariableContainer> selectedVariables;
	protected JFrame parentFrame;
	protected PropertyChangeSupport pcs;
	protected String sosURL;
	protected String fileSuffix;
	  protected SimpleDateFormat dateFormatter;
	  protected String responseFormat;

	public SosRequest(){
		pcs = new PropertyChangeSupport(this);
	    dateFormatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");

	}
	public SosRequest(SosRequest sr){
		this();
		selectedSensors = sr.selectedSensors;
		selectedVariables = sr.selectedVariables;
		selectedStartTime = sr.selectedStartTime;
		selectedEndTime = sr.selectedEndTime;
		parentFrame = sr.parentFrame;
		sosURL = sr.sosURL;
	}

	public SosRequest(String url) {
		this();
		sosURL = url;
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
	      params.add("observedProperty=" + StringUtils.join(variableQueryString, ','));
	      // eventTime
	      params.add("eventtime=" + dateFormatter.format(selectedStartTime) + "/" + dateFormatter.format(selectedEndTime));

	      return sosURL + "?" + StringUtils.join(params, '&');
	    } catch (Exception e) {
	      return "";
	    }
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

	public void setFileSuffix(String s) {
		fileSuffix = s;
	}

	public void setWorldwindSelectedSensors(List<PointPlacemark> sensorPoints) {
	    selectedSensors = new ArrayList<SensorContainer>();
	    for (PointPlacemark p : sensorPoints) {
	      selectedSensors.add((SensorContainer)p.getValue("sensor"));
	    }
	  }
	
	  public int getSelectedSensorCount() {
		    return selectedSensors.size();
		  }
	  
	  public void setParentFrame(JFrame frame) {
		    parentFrame = frame;
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
	    selectedSensors = new ArrayList<SensorContainer>();
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

	  
	  public String getResponseFormatValue() {
		    return responseFormat;
		  }

		  public void setResponseFormatValue(String s) {
		    responseFormat = s;
		  }

		 
		 
}
