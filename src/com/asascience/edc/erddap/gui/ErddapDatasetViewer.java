/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.asascience.edc.erddap.gui;

import com.asascience.edc.erddap.ErddapDataset;
import com.asascience.edc.erddap.ErddapServer;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import net.miginfocom.swing.MigLayout;

/**
 *
 * @author Kyle
 */
public class ErddapDatasetViewer extends JComponent {
  
  private ErddapDatasetList datasetList;
  private ErddapDatasetInfo datasetInfo;
  private ErddapDatasetSearch datasetSearch;
  private ErddapServer erddapServer;

  public ErddapDatasetViewer() {
    initComponents();
  }

  private void initComponents() {
    setLayout(new MigLayout("gap 0, fill"));

    // Search form
    JLabel searchLabel = new JLabel("Search Datasets: ");
    searchLabel.setLabelFor(datasetSearch);
    
    datasetSearch = new ErddapDatasetSearch();
    datasetSearch.addActionListener(new ActionListener() {

      public void actionPerformed(ActionEvent e) {
        datasetList.setDatasets((List<ErddapDataset>)datasetSearch.getDatasets());
      }
    });
    JPanel searchForm = new JPanel(new MigLayout("fill"));
    searchForm.add(searchLabel);
    searchForm.add(datasetSearch, "growx");
    
    // List
    datasetList = new ErddapDatasetList();
    datasetList.addPropertyChangeListener("datasetSelected", new PropertyChangeListener() {

      public void propertyChange(PropertyChangeEvent evt) {
        datasetInfo.setDatasetPublic((ErddapDataset)evt.getNewValue());
      }
    });

    // Info
    datasetInfo = new ErddapDatasetInfo();
    datasetInfo.addPropertyChangeListener(new PropertyChangeListener() {

      public void propertyChange(PropertyChangeEvent evt) {
        firePropertyChange(evt.getPropertyName(), evt.getOldValue(), evt.getNewValue());    
      }
    });

    // Now add them
    add(searchForm, "width 500, align left, span 2, wrap") ;
    add(datasetList, "growy, width 500");
    add(datasetInfo, "grow, width max(800, 500)");
  }

  public ErddapServer getServer() {
    return erddapServer;
  }

  public void setServer(ErddapServer erddapServer) {
    this.erddapServer = erddapServer;
    datasetSearch.setServer(erddapServer);
  }
  
  public void setDatasets(List<ErddapDataset> datasets) {
    this.datasetList.setDatasets(datasets);
  }
}
