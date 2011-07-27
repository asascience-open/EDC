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
 * ErddapDatasetInfo.java
 * 
 * @author Kyle Wilcox <kwilcox@asascience.com>
 */
public class ErddapDatasetInfo extends JPanel {

  private ErddapDataset dataset;
  private JEditorPane datasetText;
  private JPanel buttonPanel;
  // Buttons
  private JButton griddap;
  private JButton tabledap;
  private boolean showButtons = true;
  //private JButton subset;
  //private JButton wms;

  public ErddapDatasetInfo(boolean showButtons) {
    this.showButtons = showButtons;
    initComponents();
  }

  public ErddapDatasetInfo() {
    initComponents();
  }
  
  public ErddapDatasetInfo(ErddapDataset erb, boolean showButtons) {
    this.showButtons = showButtons;
    initComponents();
    setDataset(erb);
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
   
    if (showButtons) {
      // Buttons
      griddap = new JButton("Griddap");
      griddap.setEnabled(false);
      griddap.setText("Griddap");
      griddap.addActionListener(new ActionListener() {

        public void actionPerformed(ActionEvent e) {
          firePropertyChange("griddap", null, dataset);
        }
      });

      tabledap = new JButton("Tabledap");
      tabledap.setEnabled(false);
      tabledap.setText("Tabledap");
      tabledap.addActionListener(new ActionListener() {

        public void actionPerformed(ActionEvent e) {
          firePropertyChange("tabledap", null, dataset);
        }
      });

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
      buttonPanel.add(tabledap);
  //    buttonPanel.add(subset);
  //    buttonPanel.add(wms);
    }
   
    add(scroller, "grow, wrap");
    if (showButtons) {
      add(buttonPanel, "growx, align center");
    }
  }

  private void toggleButtons() {
    griddap.setEnabled(dataset.isGriddap());
    tabledap.setEnabled(dataset.isTabledap());
//    subset.setEnabled(dataset.isSubset());
//    wms.setEnabled(dataset.isWms());
  }
  
  private void setDataset(ErddapDataset dataset) {
    this.dataset = dataset;
    datasetText.setText(this.dataset.toHTMLString());
    if (showButtons) {
      toggleButtons();
    }
  }
  
  public void setDatasetPublic(ErddapDataset dataset) {
    setDataset(dataset);
  }
  
}
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