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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;

/**
 *
 * @author Kyle
 */
public class ErddapDataset {

  private ErddapServer erddapServer;
  private final String id;
  private String title;
  private String summary;
  private String background_info;
  private String institution;
  private boolean griddap;
  private boolean tabledap;
  private boolean subset;
  private boolean wms;
  private ArrayList<ErddapVariable> variables;

  public ErddapDataset(String id) {
    this.id = id;
  }

  public ErddapDataset(String id, String title, String summary, String background_info) {
    this.id = id;
    this.title = title;
    this.summary = summary;
    this.background_info = background_info;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public void setBackgroundInfo(String background_info) {
    this.background_info = background_info;
  }

  public void setSummary(String summary) {
    this.summary = summary;
  }

  public void setInstitution(String institution) {
    this.institution = institution;
  }

  public void setErddapServer(ErddapServer erddapServer) {
    this.erddapServer = erddapServer;
  }

  public void setGriddap(boolean griddap) {
    this.griddap = griddap;
  }

  public void setSubset(boolean subset) {
    this.subset = subset;
  }

  public void setTabledap(boolean tabledap) {
    this.tabledap = tabledap;
  }

  public void setWms(boolean wms) {
    this.wms = wms;
  }

  public boolean isGriddap() {
    return griddap;
  }

  public boolean isSubset() {
    return subset;
  }

  public boolean isTabledap() {
    return tabledap;
  }

  public boolean isWms() {
    return wms;
  }
  
  public boolean isGraph() {
    return (isTabledap() || isGriddap());
  }

  public String getId() {
    return id;
  }

  public String getInstitution() {
    return institution;
  }

  public String getTitle() {
    return title;
  }

  public ErddapServer getErddapServer() {
    return erddapServer;
  }

  public String getBackgroundInfo() {
    return background_info;
  }

  public String getSummary() {
    return summary;
  }

  public String getInfo() {
    return erddapServer.getURL() + "info/" + this.id + "/index.json";
  }

  public String getRSS() {
    return erddapServer.getURL() + "rss/" + this.id + ".rss";
  }

  public String getEmail() {
    return erddapServer.getURL() + "subscriptions/add.html?datasetID=" + this.id + "&showErrors=false&email=";
  }

  public String getGriddap() {
    if (isGriddap()) {
      return erddapServer.getURL() + "griddap/" + this.id;
    }
    return null;
  }

  public String getSubset() {
    if (isSubset()) {
      return getTabledap() + ".subset";
    }
    return null;
  }

  public String getTabledap() {
    if (isTabledap()) {
      return erddapServer.getURL() + "tabledap/" + this.id;
    }
    return null;
  }
  
  public String getTabledapHtml() {
    if (isTabledap()) {
      return getTabledap() + ".html";
    }
    return null;
  }

  public String getWms() {
    if (isWms()) {
      return erddapServer.getURL() + "wms/" + this.id + "/request";
    }
    return null;
  }

  public String getGraph() {
    if (isTabledap()) {
      return getTabledap() + ".graph";
    } else {
      if (isGriddap()) {
        return getGriddap() + ".graph";
      }
    }
    return null;
  }
  
  public List<ErddapVariable> getVariables() {
    return variables;
  }
  
  public void buildVariables() {
    try {
      ClientConfig clientConfig = new DefaultClientConfig();
      clientConfig.getFeatures().put(JSONConfiguration.FEATURE_POJO_MAPPING, Boolean.TRUE);
      Client c = Client.create(clientConfig);
      WebResource wr = c.resource(this.getInfo());
      JSONObject result = wr.get(JSONObject.class);
      JSONArray ar = result.getJSONObject("table").getJSONArray("rows");
      variables = new ArrayList<ErddapVariable>();
      ErddapVariable edv = null;
      String[] subsetVars = null;
      for (int i = 0 ; i < ar.length() ; i++) {
        if (ar.getJSONArray(i).getString(2).equals("subsetVariables")) {
          subsetVars = ar.getJSONArray(i).getString(4).split(",");
          for (int j = 0 ; j < subsetVars.length ; j++) {
            subsetVars[j] = subsetVars[j].trim();
          }
          continue;
        }
        if (ar.getJSONArray(i).getString(0).equals("variable")) {
          if (edv != null) {
            variables.add(edv);
          }
          edv = new ErddapVariable(this,ar.getJSONArray(i).getString(1),Arrays.asList(subsetVars).contains(ar.getJSONArray(i).getString(1).trim()));
          continue;
        }
          
        if (ar.getJSONArray(i).getString(0).equals("attribute")) {
          if (ar.getJSONArray(i).getString(2).equals("actual_range")) {
            edv.setMin(ar.getJSONArray(i).getString(4).split(",")[0].trim());
            edv.setMax(ar.getJSONArray(i).getString(4).split(",")[1].trim());
          } else if (ar.getJSONArray(i).getString(2).equals("long_name")) {
            edv.setLongname(ar.getJSONArray(i).getString(4).trim());
          } else if (ar.getJSONArray(i).getString(2).equals("units")) {
            edv.setUnits(ar.getJSONArray(i).getString(4).trim());
          } else if (ar.getJSONArray(i).getString(2).equals("axis")) {
            edv.setAxis(ar.getJSONArray(i).getString(4).trim());
          }
        }
      }
      // Get the last iteration's edv variable
      if (edv != null) {
        variables.add(edv);
      }
    } catch (Exception e) {
      //
    }
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("Title: ").append(getTitle()).append("\n\n");
    sb.append("Summary: ").append(getSummary()).append("\n\n");
    return sb.toString();
  }

  private String linkify(String label, String link) {
    return "<a href='" + link +"'>" + label + "</a>";
  }
  
  private String buildWmsRequest(String baseURL) {
    StringBuilder sb = new StringBuilder(baseURL);
    sb.append("?SERVICE=WMS");
    sb.append("&VERSION=1.3.0");
    sb.append("&REQUEST=GetCapabilities");
    return sb.toString();
  }

  public String toHTMLString() {
    StringBuilder sb = new StringBuilder();
    sb.append("<b>Title:</b> ").append(getTitle()).append("<br /><br />");
    sb.append("<b>Dataset ID:</b> ").append(getId()).append("<br /><br />");
    sb.append("<b>Institution:</b> ").append(getInstitution()).append("<br /><br />");
    sb.append("<b>Background Info:</b> ").append(linkify(getBackgroundInfo(),getBackgroundInfo())).append("<br /><br />");
    sb.append("<b>Summary:</b> ").append(getSummary()).append("<br /><br />");
    
    sb.append("<b>ERDDAP Provided Web Services:</b><br />");
    if (isWms()) {
      sb.append("<i>WMS GetCapabilities:</i> ").append(linkify(getWms(),buildWmsRequest(getWms()))).append("<br />");
    }
    if (isTabledap()) {
      sb.append("<i>DataAccess Web Form:</i> ").append(linkify(getTabledap() + ".html",getTabledap() + ".html")).append("<br />");
    }
    if (isGriddap()) {
      sb.append("<i>DataAccess Web Form:</i> ").append(linkify(getGriddap() + ".html",getGriddap() + ".html")).append("<br />");
    }
    if (isSubset()) {
      sb.append("<i>Subset Web Form:</i> ").append(linkify(getSubset(),getSubset())).append("<br />");
    }
    if (isGraph()) {
      sb.append("<i>Graphing Web Form:</i> ").append(linkify(getGraph(),getGraph())).append("<br />");
    }
    
    return sb.toString();
  }
  
}
