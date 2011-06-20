/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.asascience.edc.erddap.gui;

import cern.colt.Timer;
import com.asascience.edc.Configuration;
import com.asascience.edc.erddap.ErddapDataset;
import com.asascience.edc.erddap.ErddapVariable;
import com.asascience.edc.gui.OpendapInterface;
import com.asascience.edc.utils.FileSaveUtils;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import net.miginfocom.swing.MigLayout;

/**
 *
 * @author Kyle
 */
public class ErddapTabledapGui extends JPanel {

  private ErddapDataset erd;
  private JPanel sliderPanel;
  private ArrayList<ErddapVariableSubset> variables;
  private JTextField url;
  private OpendapInterface parent;
  private ErddapDataRequest request;
  
  public ErddapTabledapGui(ErddapDataset erd, OpendapInterface parent, String homeDir) {
    this.erd = erd;
    this.parent = parent;
    this.request = new ErddapDataRequest(homeDir);
    initComponents();
  }
  
  private void initComponents() {
    setLayout(new MigLayout("gap 0, fill"));

    // Panel with subsetting sliders and such
    sliderPanel = new JPanel(new MigLayout("gap 0, fill")) ;
    
    // Info
    add(new ErddapDatasetInfo(erd, false), "grow, wmax 500");
    
    // Subsetting Sliders in a scroll pane
    JScrollPane scroller = new JScrollPane(sliderPanel, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
    add(scroller, "grow, wrap");
    createSliders();
    
    // Button and URL
    JPanel bottom = new JPanel(new MigLayout("gap 0, fill"));
    url = new JTextField();
    bottom.add(url, "growx");
    
    JButton sub = new JButton("Submit");
    sub.addActionListener(new ActionListener() {

        public void actionPerformed(ActionEvent e) {
          updateURL();
        }
      });
    bottom.add(sub);
    
    add(bottom,"grow, spanx 2");
  }
  
  private void createSliders() {
    variables = new ArrayList<ErddapVariableSubset>(erd.getVariables().size());
    for (ErddapVariable erv : erd.getVariables()) {
      ErddapVariableSubset evs = new ErddapVariableSubset(erv);
      variables.add(evs);
      sliderPanel.add(evs, "growx, wrap");
    }
  }
  
  private void updateURL() {
    
    ArrayList<String> selections = new ArrayList<String>();
    ArrayList<String> constraints = new ArrayList<String>();
    
    for (int i = 0 ; i < variables.size() ; i++) {
      if (variables.get(i).isSelected()) {
        // Add to the selection variables
        selections.add(erd.getVariables().get(i).getName());
        
        // Did the user specify a range?
        if ((!variables.get(i).getMin().equals(erd.getVariables().get(i).getMin())) ||
            (!variables.get(i).getMax().equals(erd.getVariables().get(i).getMax()))) {
          
          // Add constraints to URL
          constraints.add(erd.getVariables().get(i).getName() + ">=" + variables.get(i).getMin());
          constraints.add(erd.getVariables().get(i).getName() + "<=" + variables.get(i).getMax());
        }
      }
    }

    String params = selections.toString().replace(" ","").replace("[","").replace("]","");
    params += "&";
    params += constraints.toString().replace(", ", "&").replace("[","").replace("]","");

    request.setBaseUrl(erd.getTabledap());
    request.setParameters(params);
   
    url.setText(request.getRequestUrl());
    
    javax.swing.SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        JFrame frame = new JFrame("Get Data");
        frame.setLayout(new MigLayout("fill"));
        frame.setPreferredSize(new Dimension(980, 400));
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        
        ErddapResponseSelectionPanel responsePanel = new ErddapResponseSelectionPanel("Available Response Formats");
        responsePanel.addPropertyChangeListener(new PropertyChangeListener() {

          public void propertyChange(PropertyChangeEvent evt) {
            request.setResponseFormat((String)evt.getNewValue());
          }
        });
        responsePanel.initComponents();
        
        
        JComponent newContentPane = new ErddapGetDataProgressMonitor(request);
        newContentPane.addPropertyChangeListener(new PropertyChangeListener() {

          public void propertyChange(PropertyChangeEvent evt) {
            if (evt.getPropertyName().equals("done")) {
              if (Configuration.CLOSE_AFTER_PROCESSING) {
                parent.formWindowClose(evt.getNewValue().toString());
              }
            }
          }
        });
        newContentPane.setOpaque(true);
        request.setParent(frame);
        frame.add(responsePanel, "grow");
        frame.add(newContentPane, "grow");
        frame.pack();
        frame.setVisible(true);
      }
    });
    
  }
  
  public class ErddapDataRequest implements PropertyChangeListener {

    private String baseUrl;
    private String responseFormat;
    private String parameters;
    private PropertyChangeSupport pcs;
    private String homeDir;
    private JFrame parent;
    
    public ErddapDataRequest(String homeDir) {
      this.homeDir = homeDir;
      pcs = new PropertyChangeSupport(this);
    }

    public void setParent(JFrame parent) {
      this.parent = parent;
    }
    
    public void setResponseFormat(String responseFormat) {
      this.responseFormat = responseFormat;
    }

    public void setParameters(String parameters) {
      this.parameters = parameters;
    }

    public void setBaseUrl(String baseUrl) {
      this.baseUrl = baseUrl;
    }

    public String getRequestUrl() {
      return baseUrl + responseFormat + "?" + parameters;
    }

    public void propertyChange(PropertyChangeEvent evt) {
      pcs.firePropertyChange(evt);
    }
    
    public void addPropertyChangeListener(PropertyChangeListener l) {
      pcs.addPropertyChangeListener(l);
    }

    public void removePropertyChangeListener(PropertyChangeListener l) {
      pcs.removePropertyChangeListener(l);
    }
    
    public void getData() {
      File savePath = FileSaveUtils.chooseSavePath(parent, homeDir, baseUrl);
      String filename = FileSaveUtils.chooseFilename(savePath, "erddap_response" + responseFormat);

      Timer stopwatch = new Timer();

      stopwatch.reset();

      int written = 0;
      try {
        URL u = new URL(getRequestUrl());
        pcs.firePropertyChange("message", null, "- Making Request (" + getRequestUrl() + ")");
        HttpURLConnection ht = (HttpURLConnection) u.openConnection();
        ht.setDoInput(true);
        ht.setRequestMethod("GET");
        InputStream is = ht.getInputStream();
        pcs.firePropertyChange("message", null, "- Streaming Results to File: " + filename);
        File f = new File(filename);
        OutputStream output = new BufferedOutputStream(new FileOutputStream(f));
        byte[] buffer = new byte[2048];
        int len = 0;
        written = 0;
        while (-1 != (len = is.read(buffer))) {
          output.write(buffer, 0, len);
          written += len;
        }
        is.close();
        output.flush();
        output.close();
      } catch (MalformedURLException e) {
        pcs.firePropertyChange("message", null, "- BAD URL, skipping sensor");
      } catch (IOException io) {
        pcs.firePropertyChange("message", null, "- BAD CONNECTION, skipping sensor");
      }

      pcs.firePropertyChange("message", null, "- Completed " + written + " bytes in " + stopwatch.elapsedTime() + " seconds.");
      pcs.firePropertyChange("progress", null, 100);
    }
  }
  
}
