package com.asascience.edc.erddap.gui;

import com.asascience.edc.Configuration;
import com.asascience.edc.erddap.ErddapDataRequest;
import com.asascience.edc.erddap.ErddapDataset;
import com.asascience.edc.erddap.ErddapVariable;
import com.asascience.edc.gui.OpendapInterface;
import com.asascience.edc.gui.jslider.JSlider2Date;
import com.asascience.edc.map.WwPolygonSelection;
import com.asascience.edc.map.view.BoundingBoxPanel;
import com.asascience.edc.map.view.SelectionMethodsPanel;
import com.asascience.edc.map.view.SelectionMethodsPanel.ActiveSelectionSource;
import com.asascience.edc.map.view.WorldwindSelectionMap;
import com.asascience.edc.utils.AoiUtils;
import com.asascience.edc.utils.PolygonUtils;
import com.asascience.edc.utils.WorldwindUtils;
import com.asascience.edc.map.WwTrackLineSelection;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.render.PointPlacemark;
import gov.nasa.worldwind.render.SurfaceSquare;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.border.EtchedBorder;
import net.miginfocom.swing.MigLayout;
import org.apache.log4j.Logger;
import ucar.nc2.units.DateUnit;
import ucar.unidata.geoloc.LatLonRect;

/**
 * ErddapTabledapGui.java
 * 
 * @author Kyle Wilcox <kwilcox@asascience.com>
 */
public class ErddapTabledapGui extends JPanel {

  private ErddapDataset erd;
  private JPanel sliderPanel;
  private ArrayList<ErddapVariableSubset> variables;
  private JTextField url;
  private OpendapInterface parent;
  private ErddapDataRequest request;
  private WorldwindSelectionMap mapPanel;
  private BoundingBoxPanel bboxGui;
  private JSlider2Date dateSlider;
  private String homeDir;
  private AoiUtils aoiUtils;
  private static Logger guiLogger = Logger.getLogger("com.asascience.log." + ErddapTabledapGui.class.getName());
  private boolean dataExtentFound;
  public ErddapTabledapGui(ErddapDataset erd, OpendapInterface parent, String homeDir,
		  ucar.util.prefs.PreferencesExt prefs) {
    this.erd = erd;
    aoiUtils = new AoiUtils(prefs);
    this.parent = parent;
    this.request = new ErddapDataRequest(homeDir, erd);
    this.homeDir = homeDir;
	variables = new ArrayList<ErddapVariableSubset>();
	this.dataExtentFound = true;
    initComponents();
  }
  public WorldwindSelectionMap getMapPanel(){
	  return mapPanel;
  }
  
  // Workaround for mac os - jogl issue where the
  // worldwind always appears on top of everything.
  public void reInitComponents(){
	  if (erd.hasX() && erd.hasY()) {
	  // Add either the sensor layer, or the data extent layer
      if (erd.hasLocations()) {
        mapPanel.addSensors(erd.getLocations());
      }
     else{
        mapPanel.makeDataExtentLayer(bboxGui.getBoundingBox(), dataExtentFound,  bboxGui.isDataIs360());
        mapPanel.getLocationSelectionTool().getTrackLineButton().setEnabled(false);

      }
	  }
	  
  }
  private void initComponents() {
    setLayout(new MigLayout("gap 0, fill"));
    
    // Panel for map, bbox, and timeslider
    JPanel mapStuff = new JPanel(new MigLayout("gap 0, fill"));
    // Panel for bbox and timeslider
    JPanel mapControls = new JPanel(new MigLayout("gap 0, fillx"));
    
    // Button and URL
    JPanel bottom = new JPanel(new MigLayout("gap 0, fill"));
    url = new JTextField();
    bottom.add(url, "growx");
    
    if (erd.hasX() && erd.hasY()) {
      // Map
      mapPanel = new WorldwindSelectionMap(homeDir);
      
      // BBOX panel
      bboxGui = new BoundingBoxPanel();
      if(erd.getX() != null && erd.getY() != null) {
    	  try{
    	
    	  bboxGui.setBoundingBox(Double.parseDouble(erd.getY().getMax()),
    			  Double.parseDouble(erd.getX().getMax()),
    			  Double.parseDouble(erd.getY().getMin()), 
    			  Double.parseDouble(erd.getX().getMin()));
    	  }
    	  catch(NumberFormatException e){
    		  guiLogger.warn("Error: Latitude/ Longitude bounds not defined");
    		
        		dataExtentFound = false;
    	  }
      }
      bboxGui.addPropertyChangeListener(aoiUtils.getPropertyChangeListener(mapPanel, bboxGui));
      bboxGui.createAoiSubmenu(aoiUtils.getAoiList());
     
      mapPanel.addPropertyChangeListener(new PropertyChangeListener() {

        public void propertyChange(PropertyChangeEvent e) {
          String name = e.getPropertyName();
          // Bounding box was drawn
          if (name.equals(SelectionMethodsPanel.BOUNDS_STORED)) {
        	  List<LatLonRect> bbox = mapPanel.getSelectedExtent();
          	if(bbox != null && bbox.size() > 0)
          		bboxGui.setBoundingBox(bbox.get(0));
            updateURL();
          }
   
          
        }
      });
      mapStuff.add(mapPanel, "gap 0, grow, wrap");
    
    bboxGui.addPropertyChangeListener(new PropertyChangeListener() {

        public void propertyChange(PropertyChangeEvent evt) {
          if (evt.getPropertyName().equals("bboxchange")) {
            mapPanel.makeSelectedExtentLayer(bboxGui.getBoundingBox());
            updateURL();
          }
        }
      });
      mapControls.add(bboxGui, "gap 0, growx, bottom");
      
      // Add either the sensor layer, or the data extent layer
      if (erd.hasLocations()) {
        mapPanel.addSensors(erd.getLocations());
       
      } else  {
        mapPanel.makeDataExtentLayer(bboxGui.getBoundingBox(), dataExtentFound, bboxGui.isDataIs360());
        
      }
    }
    if (erd.hasTime()) {
      // TIME panel
      JPanel timePanel = new JPanel(new MigLayout("gap 0, fill"));
      timePanel.setBorder(new EtchedBorder());
      dateSlider = new JSlider2Date();
      dateSlider.setAlwaysPost(true);
      dateSlider.setShowBorder(false);
      dateSlider.setHandleSize(6);
    	  // Get min and max time for dataset
    	  Date st = DateUnit.getStandardDate(erd.getTime().getMin() + " " + erd.getTime().getUnits());
    	  Date et = DateUnit.getStandardDate(erd.getTime().getMax() + " " + erd.getTime().getUnits());
    	  // getMax() was probably NaN returned from ERDDAP 
    	  if (!erd.getTime().hasMax()) {
    		  et = new Date();
    	  }
    	  dateSlider.setRange(st,et);
    	  // 10 days before end date
    	  //Date tempDate = new Date();
    	  //tempDate.setTime(et.getTime() - 1000*60*60*24*10);
    	  //dateSlider.setStartDate(tempDate);
    	  // Beginning of dataset
    	  dateSlider.setStartDate(st);
      
    	  
      dateSlider.addPropertyChangeListener(new PropertyChangeListener() {
      
        public void propertyChange(PropertyChangeEvent evt) {
          updateURL();
        }
      });
      timePanel.add(dateSlider, "gap 0, grow, center");
      mapControls.add(timePanel, "gap 0, growx, bottom");
    }
    mapStuff.add(mapControls, "gap 0, growx, bottom");
    add(mapStuff, "gap 0, grow");
    
    // Panel with subsetting sliders and such
    sliderPanel = new JPanel(new MigLayout("gap 0, fillx"));
    
    // Subsetting Sliders in a scroll pane
    JScrollPane scroller = new JScrollPane(sliderPanel, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
    add(scroller, "gap 0, grow, wrap");
    createSliders();

    
    JButton sub = new JButton("Submit");
    sub.addActionListener(new ActionListener() {

        public void actionPerformed(ActionEvent e) {
          updateURL();
          makeRequest();
        }
      });
    bottom.add(sub);
    
    add(bottom,"gap 0, growx, spanx");
    
    // Update URL from the start
    updateURL();
  }
  
  private void createSliders() {

    for (ErddapVariable erv : erd.getVariables()) {
      ErddapVariableSubset evs = new ErddapVariableSubset(erv);
      evs.addPropertyChangeListener(new PropertyChangeListener() {

        public void propertyChange(PropertyChangeEvent evt) {
          updateURL();
        }
      });
      variables.add(evs);
      sliderPanel.add(evs, "gap 0, growx, wrap");
    }
  }
  
  private String encodeConstraintRequest(String constraintsString){
	  String updatedString = "";
	    boolean inQuotes = false;
	    for(int i=0; i < constraintsString.length()-1; i++){
	    	if(!inQuotes && constraintsString.charAt(i) == ',' && 
	    				    constraintsString.charAt(i+1) == ' '){
	    		updatedString  += '&';
	    		i = i+1;
	    	}
	    	else if(constraintsString.charAt(i) == ',' && inQuotes){
	    		updatedString += "%2C";
	    	}
	    	else if(constraintsString.charAt(i) == ' ' && inQuotes){
	    		updatedString += "%20";
	    	}
	    	else {
	    		if(constraintsString.charAt(i) == '\"'){
	    			updatedString += "%22";
	    			if(inQuotes)
	    				inQuotes = false;
	    			else
	    				inQuotes = true;
	    		}
	    		else
	    			updatedString += constraintsString.charAt(i);

	    	}
	    }
	    return updatedString;
  }
  
  
//  public String getTrackConstraint(boolean isX){
//  		List<BoundingBox> selectedLocations = mapPanel.getLocationSelectionTool().getTrackSel().getSelectedTrackPoints();
//  		String constraint;
//  		if(isX)
//  			constraint = erd.getX().getName()+ "={";
//  		else 
//  			constraint = erd.getY().getName()+ "={";
//
//  		boolean first = true;
//  		for(LatLon pos : selectedLocations){
//  			if(!first)
//  				constraint += ",";
//  			Double val;
//  			if(isX)
//  				val = pos.getLongitude().degrees;
//  			else
//  				val = pos.getLatitude().degrees;
//  			constraint += val;
//  			first = false;
//  	
//  		}
//  		constraint += "}";
//  		
//  		return constraint;
//  }
  private void updateURL() {
    
    ArrayList<String> selections = new ArrayList<String>();
    ArrayList<String> constraints = new ArrayList<String>();
    
 
    for (int i = 0 ; i < variables.size() ; i++) {
      if (variables.get(i).isSelected()) {
    	
        selections.add(erd.getVariables().get(i).getName());
        constraints.addAll(variables.get(i).toConstraints());

      }
    }
    
    // Add the Time values
    if (erd.hasTime() && dateSlider != null) {
      SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd");
      constraints.add(erd.getTime().getName() + ">=" + fmt.format(dateSlider.getStartDate()));
      constraints.add(erd.getTime().getName() + "<=" + fmt.format(dateSlider.getEndDate()));
    }

    
    if(erd.hasX() && erd.hasY() && 
    		mapPanel.getLocationSelectionTool().getActiveSelectionSource() == ActiveSelectionSource.POLYGON &&
    		mapPanel.getSensorLayer() != null &&
    		mapPanel.getSensorLayer().getPickedSensors() != null){
    	List<String> stationQueries = new ArrayList<String>();
    	for(PointPlacemark pointLoc : mapPanel.getSensorLayer().getPickedSensors()){
    		String currStation = erd.getX().getName() + "=" + pointLoc.getPosition().getLongitude().degrees+"&";
    		currStation += erd.getY().getName() + "=" + pointLoc.getPosition().getLatitude().degrees;
    		stationQueries.add(currStation);
    	}
    	request.setFilterByPolygon(true);
    	request.setSelectedStationLocations(stationQueries);

    }
    else {
    	request.setSelectedStationLocations(null);
    	request.setFilterByPolygon(false);
    	// Add the X values
    	if (erd.hasX()) {

    		double minLon = bboxGui.getBoundingBox().getLonMin();
    		double maxLon =  bboxGui.getBoundingBox().getLonMax();
    		try {
    			if(Double.valueOf(erd.getX().getMax()) > 180.0){ 
    				minLon =   WorldwindUtils.normLon360(minLon);
    				maxLon =   WorldwindUtils.normLon360(maxLon);
    				if(minLon > maxLon) {
    					maxLon+=360.0;
    				}
    				if(bboxGui.isDataIs360())
    					maxLon = 360.0;
    			}
    			constraints.add(erd.getX().getName() + ">=" + minLon);
    			constraints.add(erd.getX().getName() + "<=" + maxLon);
    		}

    		catch(NumberFormatException e){
    			guiLogger.warn("No Longitude values defined");
    		}


    	}
    	// Add the Y values
    	if (erd.hasY()) {

    		try{
    			constraints.add(erd.getY().getName() + ">=" + bboxGui.getBoundingBox().getLatMin());
    			constraints.add(erd.getY().getName() + "<=" + bboxGui.getBoundingBox().getLatMax());
    		}
    		catch(Exception e){
    			guiLogger.warn("No Latitude values defined");
    		}

    	}
    }
    String params = selections.toString().replace(" ","").replace("[","").replace("]","");
    if (params.endsWith("&")) {
      params = params.substring(0,params.length() - 1);
    }
    params += "&";
    String updatedString =  encodeConstraintRequest(constraints.toString());
    params += updatedString.replace("[","").replace("]","");
    // Strip off final '&'
    if (params.endsWith("&")) {
      params = params.substring(0,params.length() - 1);
    }

    request.setBaseUrl(erd.getTabledap());
    request.setParameters(params);

    url.setText(request.getRequestUrl());
    
  }
  
  private void makeRequest() {
    javax.swing.SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        JFrame frame = new JFrame("Get Data");
        frame.setLayout(new MigLayout("fill"));
        frame.setPreferredSize(new Dimension(980, 400));
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        
        final ErddapGetDataProgressMonitor newContentPane = new ErddapGetDataProgressMonitor(request);
        newContentPane.addPropertyChangeListener(new PropertyChangeListener() {

          public void propertyChange(PropertyChangeEvent evt) {
            if (evt.getPropertyName().equals("done")) {
              if (Configuration.CLOSE_AFTER_PROCESSING) {
                parent.formWindowClose(evt.getNewValue().toString());
              }
            }
          }
        });

        final ErddapResponseSelectionPanel responsePanel = new ErddapResponseSelectionPanel("Available Response Formats");
        responsePanel.addPropertyChangeListener(new PropertyChangeListener() {

          public void propertyChange(PropertyChangeEvent evt) {
            request.setResponseFormat((String)evt.getNewValue());
            newContentPane.update();
            updateURL();
          }
        });
        responsePanel.initComponents(request.isFilterByPolygon());
        
        newContentPane.setOpaque(true);
        request.setParent(frame);
        frame.add(responsePanel, "grow");
        frame.add(newContentPane, "grow");
        frame.pack();
        frame.setVisible(true);
      }
    });
  }
}
