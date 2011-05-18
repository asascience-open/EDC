/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.asascience.edc.erddap;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.api.json.JSONConfiguration;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;

/**
 *
 * @author Kyle
 */
public class ErddapServer implements PropertyChangeListener {

  private String myUrl;
  private PropertyChangeSupport pcs;
  private HashMap serviceLinks;
  private List<ErddapDataset> datasets;
  
  public ErddapServer(String url) {
    if (url.contains("?")) {
      url = url.substring(0, url.indexOf('?'));
    }
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
      JSONArray ls = listResult.getJSONObject("table").getJSONArray("rows");
      datasets = new ArrayList<ErddapDataset>(ls.length());
      JSONArray ds;
      ErddapDataset erds;
      pcs.firePropertyChange("message", null, "Building datasets...");
      for (int j = 0 ; j < ls.length() ; j++) {
        ds = ls.getJSONArray(j);
        erds = new ErddapDataset(ds.getString(12));
        erds.setTitle(ds.getString(5));
        erds.setSummary(ds.getString(6));
        erds.setBackgroundInfo(ds.getString(8));
        erds.setInstitution(ds.getString(11));
        erds.setErddapServer(this);
        erds.setGriddap(!ds.getString(0).isEmpty());
        erds.setSubset(!ds.getString(1).isEmpty());
        erds.setTabledap(!ds.getString(2).isEmpty());
        erds.setWms(!ds.getString(4).isEmpty());
        datasets.add(erds);
      }
    } catch (Exception e) {
      pcs.firePropertyChange("message", null, e.getMessage());
      return false;
    }
    return true;
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
