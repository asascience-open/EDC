package com.asascience.edc.erddap.gui;

import com.asascience.edc.erddap.ErddapDataset;
import com.asascience.edc.erddap.ErddapServer;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.api.json.JSONConfiguration;
import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import net.miginfocom.swing.MigLayout;
import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;

/**
 * ErddapDatasetSearch.java
 * 
 * @author Kyle Wilcox <kwilcox@asascience.com>
 */
public class ErddapDatasetSearch extends JTextField {

  private ErddapServer erddapServer;
  private List<ErddapDataset> datasets;
  private static Logger guiLogger = Logger.getLogger("com.asascience.log." + ErddapDatasetSearch.class.getName());

  public ErddapDatasetSearch() {
    initComponents();
  }

  private void initComponents() {
    setLayout(new MigLayout("gap 0, fill"));

    this.setText("Connect to 'ERDDAP Server' above to enable search");
    this.setEnabled(false);
    
    getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "enterreleased");
    getActionMap().put("enterreleased", new AbstractAction() {

      public void actionPerformed(ActionEvent e) {
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        try {
          if (getText().isEmpty()) {
            datasets = erddapServer.getDatasets();
          } else {
            guiLogger.info("Searching ERDDAP for: " + getText()); 
            ClientConfig clientConfig = new DefaultClientConfig();
            clientConfig.getFeatures().put(JSONConfiguration.FEATURE_POJO_MAPPING, Boolean.TRUE);
            Client c = Client.create(clientConfig);
            guiLogger.info("Request: " + erddapServer.getSearchURL().concat(getText()));
            WebResource wr = c.resource(erddapServer.getSearchURL().concat(getText()));
            JSONObject listResult = wr.get(JSONObject.class);
            JSONArray ls = listResult.getJSONObject("table").getJSONArray("rows");
            datasets = new ArrayList<ErddapDataset>(ls.length());
            JSONArray ds;
            ErddapDataset erds;
            JSONArray nms = listResult.getJSONObject("table").getJSONArray("columnNames");
            
            ArrayList<String> columnNames = new ArrayList<String>(Arrays.asList(nms.join(",").split(",")));
            guiLogger.info("Found " + ls.length() + " Datasets"); 
            for (int j = 0 ; j < ls.length() ; j++) {
              ds = ls.getJSONArray(j);
              erds = erddapServer.createErrdapDatasetFromRow(ds, columnNames);
              datasets.add(erds);
            }
          }
          fireActionPerformed();
        } catch (Exception ex) {
        }
        
        setCursor(Cursor.getDefaultCursor());
      }
    });
  }

  public void setServer(ErddapServer erddapServer) {
    this.erddapServer = erddapServer;
    this.setText("");
    this.setEnabled(true);
  }
  
  public List<ErddapDataset> getDatasets() {
    return datasets;
  }
}
