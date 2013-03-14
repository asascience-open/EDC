package com.asascience.edc.erddap;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.api.json.JSONConfiguration;
import java.util.ArrayList;
import java.util.Collection;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;

/**
 * ErddapVariable.java
 * 
 * @author Kyle Wilcox <kwilcox@asascience.com>
 */
public class ErddapVariable {
  private String min;
  private String max;
  private String name;
  private String units;
  private String description;
  private String longname;
  private String axis;
  private String type;
  private ArrayList<String> values;
  private ErddapDataset erd;
  
  /*
   * These are constants, defined by ERDDAP.
   * See http://coastwatch.pfeg.noaa.gov/erddap/tabledap/documentation.html
   * Section "Special Variables"
   */
  private static String TAXIS = "time";
  private static String ZAXIS = "altitude";
  private static String XAXIS = "longitude";
  private static String YAXIS = "latitude";
  private static ArrayList<String> CDM_TYPES = new ArrayList<String>();
  
  private ArrayList<String> double_types;

  public ErddapVariable(ErddapDataset erd, String name, String type, boolean values) {
    this.erd = erd;
    this.name = name;
    this.axis = name;
    this.type = type;
    this.min = "";
    this.max = "";
    this.values = new ArrayList<String>();
    CDM_TYPES.add("timeseries_id");
    CDM_TYPES.add("trajectory_id");
    initDoubleTypes();
    if (values) {
      getConstraints();
    }
  }

  public ErddapVariable(ErddapDataset erd, String min, String max, String name, String type) {
    this.erd = erd;
    this.name = name;
    this.axis = name;
    this.type = type;
    this.min = min;
    this.max = max;
    this.values = new ArrayList<String>();
    CDM_TYPES.add("timeseries_id");
    CDM_TYPES.add("trajectory_id");
    initDoubleTypes();
  }
  
  private void initDoubleTypes() {
    this.double_types = new ArrayList<String>(4);
    double_types.add("double");
    double_types.add("float");
    double_types.add("short");
    double_types.add("byte");
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
      String v;
      for (int i = 0 ; i < ar.length() ; i++) {
        v = ar.getJSONArray(i).getString(0);
   
        values.add(v);
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

  public boolean hasMax() {
    return !this.max.isEmpty() && this.max != null && !this.max.equalsIgnoreCase("NaN");
  }
  
  public boolean hasMin() {
    return !this.min.isEmpty() && this.min != null && !this.min.equalsIgnoreCase("NaN");
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

  public void setDescription(String description) {
    this.description = description;
  }

  public String getTitle() {
    if (getLongname() == null || getLongname().isEmpty()) {
      return getName();
    } else {
      return getLongname();
    }
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

  public String getDescription() {
    return description;
  }

  public ArrayList<String> getValues() {
    return values;
  }
  
  public boolean isX() {
    return axis.equals(XAXIS);
  }
  
  public boolean isY() {
    return axis.equals(YAXIS);
  }
  
  public boolean isTime() {
    return axis.equals(TAXIS);
  }
  
  public boolean isZ() {
    return axis.equals(ZAXIS);
  }
  
  public boolean isCdm() {
    return CDM_TYPES.contains(axis);
  }
  
  public boolean isSingleValue() {
    return min.equals(max) && !min.isEmpty();
  }
  
  public boolean hasNoRange() {
    return ((min.isEmpty() && max.isEmpty()) ||
            (min.equalsIgnoreCase("NaN") && max.equalsIgnoreCase("NaN")));
  }
  
  public boolean isString() {
    return this.type.equalsIgnoreCase("String");
  }
  
  public boolean isDouble() {
    if (double_types.contains((String)this.type)) {
      return true;
    } else {
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
  
  
    

