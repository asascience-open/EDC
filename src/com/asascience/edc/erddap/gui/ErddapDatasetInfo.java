/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.asascience.edc.erddap.gui;

import com.asascience.edc.erddap.ErddapDataset;
import java.awt.Desktop;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URI;
import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import net.miginfocom.swing.MigLayout;

/**
 *
 * @author Kyle
 */
public class ErddapDatasetInfo extends JPanel {

  private ErddapDataset dataset;
  private JEditorPane datasetText;
  private JPanel buttonPanel;
  // Buttons
  private JButton griddap;
  //private JButton tabledap;
  //private JButton subset;
  //private JButton wms;

  public ErddapDatasetInfo() {
    initComponents();
  }

  private void initComponents() {
    setLayout(new MigLayout("gap 0, fill"));
    JLabel lab = new JLabel("Dataset Info");
    add(lab, "growx, align center, wrap");

    datasetText = new JEditorPane("text/html", "");
    datasetText.setEditable(false);
    HyperlinkListener hyperlinkListener = new ActivatedHyperlinkListener();
    datasetText.addHyperlinkListener(hyperlinkListener);
    
    JScrollPane scroller = new JScrollPane(datasetText, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                                           JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
   
    // Buttons
    griddap = new JButton("Griddap");
    griddap.setEnabled(false);
    griddap.setText("Griddap");
    griddap.addActionListener(new ActionListener() {

      public void actionPerformed(ActionEvent e) {
        firePropertyChange("griddap", null, dataset);
      }
    });
    
//    tabledap = new JButton("Tabledap");
//    tabledap.setEnabled(false);
//    tabledap.setText("Tabledap");
//    tabledap.addActionListener(new ActionListener() {
//
//      public void actionPerformed(ActionEvent e) {
//        firePropertyChange("tabledap", null, dataset);
//      }
//    });
      
//    subset = new JButton("Subset");
//    subset.setEnabled(false);
//    subset.setText("Subset");
//    subset.addActionListener(new ActionListener() {
//
//      public void actionPerformed(ActionEvent e) {
//        firePropertyChange("subset", null, dataset);
//      }
//    });
    
    
//    wms = new JButton("WMS");
//    wms.setEnabled(false);
//    wms.setText("WMS");
//    wms.addActionListener(new ActionListener() {
//
//      public void actionPerformed(ActionEvent e) {
//        firePropertyChange("wms", null, dataset);
//      }
//    });
    
    buttonPanel = new JPanel();
    buttonPanel.add(new JLabel("EDC Processing:"));
    buttonPanel.add(griddap);
//    buttonPanel.add(tabledap);
//    buttonPanel.add(subset);
//    buttonPanel.add(wms);
   
    add(scroller, "grow, wrap");
    add(buttonPanel, "growx, align center");
  }

  private void toggleButtons() {
    griddap.setEnabled(dataset.isGriddap());
//    tabledap.setEnabled(dataset.isTabledap());
//    subset.setEnabled(dataset.isSubset());
//    wms.setEnabled(dataset.isWms());
  }
  
  public void setDataset(ErddapDataset dataset) {
    this.dataset = dataset;
    datasetText.setText(this.dataset.toHTMLString());
    toggleButtons();
  }
}


// Open a browser when a user clicks a hyperlink inside of the Info Text area
class ActivatedHyperlinkListener implements HyperlinkListener {
  public void hyperlinkUpdate(HyperlinkEvent hyperlinkEvent) {
    HyperlinkEvent.EventType type = hyperlinkEvent.getEventType();
    try {
      final URI uri = hyperlinkEvent.getURL().toURI();
      if (type == HyperlinkEvent.EventType.ACTIVATED) {
        if (Desktop.isDesktopSupported()) {
            Desktop.getDesktop().browse(uri);
        }
      }
    } catch (Exception e) {
      
    }
  }
}