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
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import net.miginfocom.swing.MigLayout;
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
            ClientConfig clientConfig = new DefaultClientConfig();
            clientConfig.getFeatures().put(JSONConfiguration.FEATURE_POJO_MAPPING, Boolean.TRUE);
            Client c = Client.create(clientConfig);
            WebResource wr = c.resource(erddapServer.getSearchURL().concat(getText()));
            JSONObject listResult = wr.get(JSONObject.class);
            JSONArray ls = listResult.getJSONObject("table").getJSONArray("rows");
            datasets = new ArrayList<ErddapDataset>(ls.length());
            JSONArray ds;
            ErddapDataset erds;
            for (int j = 0 ; j < ls.length() ; j++) {
              ds = ls.getJSONArray(j);
              erds = new ErddapDataset(ds.getString(12));
              erds.setTitle(ds.getString(5));
              erds.setSummary(ds.getString(6));
              erds.setBackgroundInfo(ds.getString(8));
              erds.setInstitution(ds.getString(11));
              erds.setErddapServer(erddapServer);
              erds.setGriddap(!ds.getString(0).isEmpty());
              erds.setSubset(!ds.getString(1).isEmpty());
              erds.setTabledap(!ds.getString(2).isEmpty());
              erds.setWms(!ds.getString(4).isEmpty());
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
