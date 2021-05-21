package com.asascience.edc.sos.requests;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import com.asascience.edc.sos.SensorContainer;
import com.asascience.ioos.exception.IoosSosParserException;
import com.asascience.ioos.model.GetObservation;
import com.asascience.ioos.netcdf.CreateNetcdf;
import com.asascience.ioos.parser.GetObservationParser;

import cern.colt.Timer;
import ucar.nc2.NetcdfFile;

public class SweVers1Request extends SosRequest {

	public SweVers1Request(SosRequest sr){
		super(sr);
	}

	/*
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
		      params.add("offering=network-all");
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
		      params.add("procedure=" + URLEncoder.encode(sensor.getGmlName(), "utf-8"));
		      return sosURL + "?" + StringUtils.join(params, '&');
		    } catch (Exception e) {
		      return "";
		    }
		  }

	*/
	@Override
	public void getObservations(File savePath) {

	
		double numSens = getSelectedSensorCount();
	    double countSens = 0;
	    String requestURL;
	    ArrayList<String> filenames = new ArrayList<String>();

	    Timer stopwatch = new Timer();

	    for (SensorContainer sensor : selectedSensors) {
	      stopwatch.reset();
	      stopwatch.start();
	      pcs.firePropertyChange("message", null, "Sensor: " + sensor.getName());
	      pcs.firePropertyChange("message", null, "- Building Request String");

	      requestURL = super.buildRequest(sensor);
	   
	      int written = 0;
	      try {
	    	  URL u = new URL(requestURL);  
	    	  pcs.firePropertyChange("message", null, "- Making Request (" + requestURL + ")");

	    	  GetObservationParser getObsParser = new GetObservationParser();
	    	  GetObservation getObs = getObsParser.parseGO(u);
	    	  if(getObs != null){
	    		  pcs.firePropertyChange("message", null, "- Converting to Netcdf file");
	    		  CreateNetcdf ncCreate  = new CreateNetcdf(getObs);
	    		  List<NetcdfFile> ncFileList = ncCreate.generateNetcdf(savePath.getPath());

	    		  for(NetcdfFile ncFile : ncFileList){
	    			  File fileLoc = new File(ncFile.getLocation());
	    			  filenames.add(ncFile.getLocation());
	    			  if(fileLoc != null){
	    				  written += fileLoc.length();
	    			  }
	    		  }
	    	  }

	      } catch (MalformedURLException e) {
	    	  pcs.firePropertyChange("message", null, "- BAD URL, skipping sensor");
	    	  e.printStackTrace();
	    	  continue;
	      } catch (IOException io) {
	    	  pcs.firePropertyChange("message", null, "- BAD CONNECTION, skipping sensor");
	    	  continue;
	      } catch (IoosSosParserException e) {
	    	  // TODO Auto-generated catch block
	    	  e.printStackTrace();
	      }
	      catch(Exception e1){
	    	  e1.printStackTrace();
	      }

	      countSens++;
	      pcs.firePropertyChange("message", null, "- Completed " + written + " bytes in " + stopwatch.elapsedTime() + " seconds.");
	      int prog = Double.valueOf(countSens / numSens * 100).intValue();
	      pcs.firePropertyChange("progress", null, prog);
	    } // End Sensor List
	    pcs.firePropertyChange("progress", null, 100);
	    if (!filenames.isEmpty()) {
	    	pcs.firePropertyChange("message", null, "Saved Files:");
	    	for (String s : filenames) {
	    		pcs.firePropertyChange("message", null, "- " + s);
	    	}
	    }
	    pcs.firePropertyChange("done", null, filenames.toString());
	}


}
