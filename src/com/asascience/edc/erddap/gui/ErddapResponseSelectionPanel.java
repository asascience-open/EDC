package com.asascience.edc.erddap.gui;

import com.asascience.edc.sos.requests.ResponseFormat;
import com.asascience.edc.gui.ResponseFormatRadioButton;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import net.miginfocom.swing.MigLayout;

/**
 * ErddapResponseSelectionPanel.java
 * 
 * @author Kyle Wilcox <kwilcox@asascience.com>
 */
public final class ErddapResponseSelectionPanel extends JPanel implements ActionListener {

  private PropertyChangeSupport pcs;
  private String panelTitle;

  public ErddapResponseSelectionPanel(String title) {
    this.panelTitle = title;
    pcs = new PropertyChangeSupport(this);
  }

  public void initComponents() {
    setLayout(new MigLayout("gap 0, fill"));

    ButtonGroup group = new ButtonGroup();
    
    JPanel responsesPanel = new JPanel();
    responsesPanel.setLayout(new GridLayout(0, 1));
    
    ArrayList<ResponseFormat> responses = new ArrayList<ResponseFormat>();
    responses.add(new ResponseFormat("asc", "View an OPeNDAP-style comma-separated ASCII text file.","",".asc",false));
    responses.add(new ResponseFormat("csv", "Download a comma-separated ASCII text table (line 1: names; line 2: units; ISO 8601 times).","",".csv",false));
    responses.add(new ResponseFormat("csvp", "Download a .csv file with line 1: name (units). Times are ISO 8601 strings.","",".csvp",false));
    responses.add(new ResponseFormat("das", "View the data's metadata via an OPeNDAP Dataset Attribute Structure (DAS).","",".das",false));
    responses.add(new ResponseFormat("dds", "View the data's structure via an OPeNDAP Dataset Descriptor Structure (DDS).","",".dds",false));
    //responses.add(new ResponseFormat("dods", "OPeNDAP clients use this to download the data in the DODS binary format.","",".dods",false));
    responses.add(new ResponseFormat("esriCsv", "Download a .csv file for ESRI's ArcGIS (line 1: names; separate date and time columns).","",".esriCsv",false));
    responses.add(new ResponseFormat("geoJson", "Download longitude,latitude,otherColumns data as a GeoJSON .json file.","",".geoJson",false));
    //responses.add(new ResponseFormat("graph", "View a Make A Graph web page.","",".graph",false));
    //responses.add(new ResponseFormat("help", "View a web page with a description of tabledap.","",".help",false));
    //responses.add(new ResponseFormat("html", "View an OPeNDAP-style HTML Data Access Form.","",".html",false));
    //responses.add(new ResponseFormat("htmlTable", "View an .html web page with the data in a table. Times are ISO 8601 strings.","",".htmlTable",false));
    responses.add(new ResponseFormat("json", "Download a table-like JSON file (missing value = 'null'; times are ISO 8601 strings).","",".json",false));
    responses.add(new ResponseFormat("mat", "Download a MATLAB binary file.","",".mat",false));
    responses.add(new ResponseFormat("nc", "Download a flat, table-like, NetCDF-3 binary file with COARDS/CF/THREDDS metadata.","",".nc",false));
    responses.add(new ResponseFormat("ncHeader", "View the header (the metadata) for the NetCDF-3 file.","",".ncHeader",false));
    responses.add(new ResponseFormat("ncCF", "Download a structured, NetCDF-3 binary file using the new CF Discrete Sampling Geometries.","",".ncCF",false));
    responses.add(new ResponseFormat("odvTxt", "Download longitude,latitude,time,otherColumns as an ODV Generic Spreadsheet File (.txt).","",".odvTxt",false));
    //responses.add(new ResponseFormat("subset", "View an HTML form which uses faceted search to simplify picking subsets of the data.","",".subset",false));
    responses.add(new ResponseFormat("tsv", "Download a tab-separated ASCII text table (line 1: names; line 2: units; ISO 8601 times).","",".tsv",false));
    responses.add(new ResponseFormat("tsvp", "Download a .tsv file with line 1: name (units). Times are ISO 8601 strings.","",".tsvp",false));
    responses.add(new ResponseFormat("xhtml", "View an XHTML (XML) file with the data in a table. Times are ISO 8601 strings.","",".xhtml",false));

    boolean select = true;
    for (ResponseFormat rf : responses) {
      ResponseFormatRadioButton r = new ResponseFormatRadioButton();
      r.setName(rf.getName());
      r.setText("." + rf.getValue() + " - " + rf.getName());
      r.setResponseFormat(rf);
      r.addActionListener(this);
      if (select) {
        r.setSelected(true);
        pcs.firePropertyChange("selected", "", rf.getFileSuffix());
        select = false;
      }
      group.add(r);
      responsesPanel.add(r);
    }
    
    JScrollPane sp = new JScrollPane(responsesPanel, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
    sp.setBorder(BorderFactory.createTitledBorder(panelTitle + ": ")); 
    add(sp, "grow");
  }

  @Override
  public void addPropertyChangeListener(PropertyChangeListener l) {
    pcs.addPropertyChangeListener(l);
  }
  @Override
  public void removePropertyChangeListener(PropertyChangeListener l) {
    pcs.removePropertyChangeListener(l);
  }

  public void actionPerformed(ActionEvent e) {
    ResponseFormatRadioButton cb = (ResponseFormatRadioButton) e.getSource();
    if (cb.isSelected()) {
      pcs.firePropertyChange("selected", "", cb.getResponseFormat().getFileSuffix());
    }
  }
}
