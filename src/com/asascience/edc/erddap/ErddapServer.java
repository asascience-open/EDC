package com.asascience.edc.erddap;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.api.json.JSONConfiguration;

/**
 * ErddapServer.java
 * 
 * @author Kyle Wilcox <kwilcox@asascience.com>
 */
public class ErddapServer implements PropertyChangeListener {

  private String myUrl;
  private PropertyChangeSupport pcs;
  private HashMap serviceLinks;
  private List<ErddapDataset> datasets;
  public final static String ERDDAP_DATASET_ID ="\"Dataset ID\"";
  public final static String ERDDAP_TITLE = "\"Title\"";
  public final static String ERDDAP_SUMMARY = "\"Summary\"";
  public final static String ERDDAP_BACKGROUND_INFO = "\"Background Info\"";
  public final static String ERDDAP_INSTITUTION = "\"Institution\"";
  public final static String ERRDAP_GRIDDAP = "\"griddap\"";
  public final static String ERDDAP_SUBSET = "\"Subset\"";
  public final static String ERDDAP_TABLEDAP = "\"tabledap\"";
  public final static String ERDDAP_WMS = "\"wms\"";
  
  
  public ErddapServer(String url) {
    if (url.contains("?")) {
      url = url.substring(0, url.indexOf('?'));
    }
    if(url.contains("index.html")){
        url = url.substring(0, url.indexOf("index.html"));

    }
    if(!url.endsWith("/"))
    	url = url + "/";
    myUrl = url;
    pcs = new PropertyChangeSupport(this);
    serviceLinks = new HashMap<String,String>(6);
  }

  public boolean processDatasets() {
    // Get Service links
    try {
      // Jersey
      ClientConfig clientConfig = new DefaultClientConfig();
      clientConfig.getFeatures().put(JSONConfiguration.FEATURE_POJO_MAPPING, Boolean.TRUE);
      Client c = Client.create(clientConfig);
      WebResource wr = c.resource(myUrl).path("index.json");
      pcs.firePropertyChange("message", null, "Reading main resources access URLs from: " + wr.getURI().toString());
      JSONObject result = wr.get(JSONObject.class);
      JSONArray ar = result.getJSONObject("table").getJSONArray("rows");
      for (int i = 0 ; i < ar.length() ; i++) {
        serviceLinks.put(ar.getJSONArray(i).getString(0), ar.getJSONArray(i).getString(1));
      }
      
      // Load the JSON listing of ERDDAP datasets
      WebResource list = c.resource(serviceLinks.get("info").toString());
      pcs.firePropertyChange("message", null, "Reading available datasets from: " + list.getURI().toString());
      JSONObject listResult = list.get(JSONObject.class);
      
      JSONObject tbl = listResult.getJSONObject("table");
      JSONArray ls = tbl.getJSONArray("rows");
      JSONArray nms = tbl.getJSONArray("columnNames");
      
      ArrayList<String> columnNames = new ArrayList<String>(Arrays.asList(nms.join(",").split(",")));
      datasets = new ArrayList<ErddapDataset>(ls.length());
      JSONArray ds;
      ErddapDataset erds;
      pcs.firePropertyChange("message", null, "Building datasets...");
      for (int j = 0 ; j < ls.length() ; j++) {
        ds = ls.getJSONArray(j);
        erds = createErrdapDatasetFromRow(ds, columnNames);
        datasets.add(erds);
      }
    } catch (Exception e) {
      pcs.firePropertyChange("message", null, e.getMessage());
      return false;
    }
    return true;
  }

  public ErddapDataset createErrdapDatasetFromRow(JSONArray ds, ArrayList<String> columnNames )
		  throws JSONException{
	  ErddapDataset erds = new ErddapDataset(ds.getString(columnNames.indexOf(ERDDAP_DATASET_ID)));
	  erds.setTitle(ds.getString(columnNames.indexOf(ERDDAP_TITLE)));
	  erds.setSummary(ds.getString(columnNames.indexOf(ERDDAP_SUMMARY)));
	  erds.setBackgroundInfo(ds.getString(columnNames.indexOf(ERDDAP_BACKGROUND_INFO)));
	  erds.setInstitution(ds.getString(columnNames.indexOf(ERDDAP_INSTITUTION)));
	  erds.setErddapServer(this);
	  erds.setGriddap(!ds.getString(columnNames.indexOf(ERRDAP_GRIDDAP)).isEmpty());
	  erds.setSubset(!ds.getString(columnNames.indexOf(ERDDAP_SUBSET)).isEmpty());
	  erds.setTabledap(!ds.getString(columnNames.indexOf(ERDDAP_TABLEDAP)).isEmpty());
	  erds.setWms(!ds.getString(columnNames.indexOf(ERDDAP_WMS)).isEmpty());  

	  return erds;
  }
  
  
  public String getURL() {
    return myUrl;
  }

  public String getSearchURL() {
    return (String)serviceLinks.get("search");
  }

  public List<ErddapDataset> getDatasets() {
    return datasets;
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
