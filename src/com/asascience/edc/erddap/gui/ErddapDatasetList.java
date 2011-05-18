/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.asascience.edc.erddap.gui;

import com.asascience.edc.erddap.ErddapDataset;
import java.util.List;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import net.miginfocom.swing.MigLayout;

/**
 *
 * @author Kyle
 */
public class ErddapDatasetList extends JPanel {

  private ErddapDataset[] datasets = {};
  private JList datasetList;

  public ErddapDatasetList() {
    initComponents();
  }

  private void initComponents() {
    setLayout(new MigLayout("gap 0, fill"));

    JLabel lab = new JLabel("Dataset Listing");

    datasetList = new JList();
    datasetList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    datasetList.setCellRenderer(new DatasetCellRenderer());
    datasetList.addListSelectionListener(new ListSelectionListener() {

      public void valueChanged(ListSelectionEvent e) {
        firePropertyChange("datasetSelected", null,(ErddapDataset)datasetList.getSelectedValue());
      }
    });
    JScrollPane scroller = new JScrollPane(datasetList);
    
    add(lab, "align center, wrap");
    add(scroller, "grow");
  }

  public void setDatasets(List<ErddapDataset> datasets) {
    ErddapDataset[] temp = new ErddapDataset[datasets.size()];
    this.datasets = datasets.toArray(temp);
    datasetList.setListData(this.datasets);
    temp = null;
  }

}
