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
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;

/**
 *
 * @author Kyle
 */
public class ErddapVariable {
  private String min;
  private String max;
  private String name;
  private String units;
  private String longname;
  private String axis;
  private ArrayList<String> values;
  private ErddapDataset erd;
  
  private static String TAXIS = "T";
  private static String ZAXIS = "Z";

  public ErddapVariable(ErddapDataset erd, String name, boolean values) {
    this.erd = erd;
    this.name = name;
    this.axis = "";
    this.min = "";
    this.max = "";
    this.values = new ArrayList<String>();
    if (values) {
      getConstraints();
    }
  }

  public ErddapVariable(ErddapDataset erd, String min, String max, String name) {
    this.erd = erd;
    this.name = name;
    this.axis = "";
    this.min = min;
    this.max = max;
    this.values = new ArrayList<String>();
  }
  
  private void getConstraints() {
    String path = erd.getTabledap() + ".json?" + this.name + "&distinct()";
    try {
      // Jersey
      ClientConfig clientConfig = new DefaultClientConfig();
      clientConfig.getFeatures().put(JSONConfiguration.FEATURE_POJO_MAPPING, Boolean.TRUE);
      Client c = Client.create(clientConfig);
      WebResource wr = c.resource(path);
      JSONObject result = wr.get(JSONObject.class);
      JSONArray ar = result.getJSONObject("table").getJSONArray("rows");
      for (int i = 0 ; i < ar.length() ; i++) {
        values.add(ar.getJSONArray(i).getString(0));
      }
      min = values.get(0);
      max = values.get(values.size() - 1);
    } catch (Exception e) {
      //
    }
  }

  public void setMin(String min) {
    this.min = min;
  }

  public void setMax(String max) {
    this.max = max;
  }

  public void setLongname(String longname) {
    this.longname = longname;
  }

  public void setUnits(String units) {
    this.units = units;
  }

  public void setAxis(String axis) {
    this.axis = axis;
  }

  public String getLongname() {
    return longname;
  }

  public String getName() {
    return name;
  }

  public String getUnits() {
    return units;
  }

  public String getMax() {
    return max;
  }

  public String getMin() {
    return min;
  }

  public ArrayList<String> getValues() {
    return values;
  }
  
  public boolean isTime() {
    return axis.equals(TAXIS);
  }
  
  public boolean isZ() {
    return axis.equals(ZAXIS);
  }
  
  public boolean isDouble() {
    try {
      Double.parseDouble(min);
      Double.parseDouble(max);
    } catch (Exception e) {
      return false;
    }
    if (!values.isEmpty()) {
      for (int i = 0 ; i < values.size() ; i++) {
        try {
          Double.parseDouble(values.get(i));
        } catch (Exception e) {
          return false;
        }
      }
    }
    return true;
  }
  
  public double[] getSliderValues() {
    if (isDouble()) {
      double[] ds = new double[values.size()];
      for (int i = 0 ; i < values.size() ; i++) {
        ds[i] = Double.parseDouble(values.get(i));
      }
      return ds;
    }
    return null;
  }
}
  
  
    

