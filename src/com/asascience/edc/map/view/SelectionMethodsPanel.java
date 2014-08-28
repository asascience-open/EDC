package com.asascience.edc.map.view;

import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.render.SurfacePolygon;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.net.URL;
import java.util.List;

import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JToggleButton;

import com.asascience.edc.dap.ui.DapWorldwindProcessPanel;
import com.asascience.edc.map.WorldWindMapControl;
import com.asascience.edc.map.WwPolygonSelection;
import com.asascience.edc.map.WorldwindBoundingBoxBuilder;
import com.asascience.edc.map.WwTrackLineSelection;
import com.asascience.edc.utils.WorldwindUtils;

import net.miginfocom.swing.MigLayout;
import ucar.unidata.geoloc.LatLonPoint;
import ucar.unidata.geoloc.LatLonPointImpl;
import ucar.unidata.geoloc.LatLonRect;

/**
 * WorldwindBoundingBoxTool.java
 * 
 * @author Kyle Wilcox <kwilcox@asascience.com>
 */
public class SelectionMethodsPanel extends JPanel {

  private final WorldWindow wwd;
  private final WorldwindBoundingBoxBuilder lb;
  private final WwPolygonSelection polygonSel;
  private final WwTrackLineSelection trackSel;
  private JToggleButton bboxButton;
  private JToggleButton trackLineButton;
  private JToggleButton polygonButton;
  private JToggleButton hiddenButton;
  private JButton importTrackButton;
  private JButton exportTrackButton;
  private LatLonRect bbox;
  private JTextField trackLineWidth;
  private final String TRACK_ICON = "TrackLine.png";
  private final String SELECTED_TRACK_ICON = "SelectedTrackLine.png";
  private final String TRACK_LINE_TXT = "Select Track Line";
  private final String BBOX_ICON = "SelectBBOX.gif";
  private final String BBOX_LINE_TXT = "Select Bounding Box";
  private final String POLYGON_ICON = "SelectPolygon.gif";
  private final String POLYGON_TXT = "Select Polygon";
  private ActiveSelectionSource activeSelectionSource;
  public final static String BOUNDS_STORED = "boundsStored";
  private JLabel trackRadiusLabel;
  public final static String TRACK_WIDTH_UPDATED = "trackWidthUpdated";
  public enum ActiveSelectionSource {
	  BBOX,
	  TRACK_LINE,
	  POLYGON
  }
  public SelectionMethodsPanel(WorldWindow wwdd, boolean showTrackLineOptions) {
    super(new BorderLayout());
    this.wwd = wwdd;
    activeSelectionSource = ActiveSelectionSource.BBOX;
    lb = new WorldwindBoundingBoxBuilder(wwd, null, null);
    polygonSel = new WwPolygonSelection(wwd, null);
   
    trackSel = new WwTrackLineSelection(wwd, null);
    initComponents(showTrackLineOptions);

    initListeners();
  }

  
  

  public List<Position> getTrackLinePositions(){
	  return polygonSel.getPolygonPositions();
  }
  
  private void initListeners(){

	  polygonSel.addPropertyChangeListener(new PropertyChangeListener(){

			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				String propName = evt.getPropertyName();
				if(propName.equals(WwPolygonSelection.SELECTION_FINISHED)){
					hiddenButton.setSelected(true);
					processSelectionEvent(polygonButton, polygonSel);
				   bbox = polygonSel.getPolygonBBox();
		            firePropertyChange(BOUNDS_STORED, false, true);

				}
				else if(propName.equals(WwPolygonSelection.SELECTION_STARTED)){
					lb.clear();
					trackSel.clearTrackPoints();

					activeSelectionSource = ActiveSelectionSource.POLYGON;
		            firePropertyChange(DapWorldwindProcessPanel.DISABLE_BBOX, true, false);
		    		firePropertyChange(DapWorldwindProcessPanel.DISABLE_SLIDER, false, true);


				}
				else if(propName.equals(WwPolygonSelection.SELECTION_ADDED)){
					firePropertyChange(WwPolygonSelection.SELECTION_ADDED, false, true);
				}
				
			}
	    	
	    });
	
	  trackSel.addPropertyChangeListener(new PropertyChangeListener(){

		@Override
		public void propertyChange(PropertyChangeEvent evt) {
			String propName = evt.getPropertyName();
			
			 if(propName.equals(WwTrackLineSelection.TRACK_PT_STARTED)){
					lb.clear();
		        	polygonSel.clear();
		        	trackSel.clearTrackPoints();
		        	activeSelectionSource = ActiveSelectionSource.TRACK_LINE;
		            firePropertyChange(DapWorldwindProcessPanel.DISABLE_BBOX, true, false);
		    		firePropertyChange(DapWorldwindProcessPanel.DISABLE_SLIDER, false, true);

			 }
			 else if(propName.equals(WwTrackLineSelection.TRACK_PT_ADDED) ||
					 propName.equals(WwTrackLineSelection.TRACK_RADIUS_UPDATED)){
				 firePropertyChange(WwTrackLineSelection.TRACK_PT_ADDED, false, true);

			 }
		
			 else if(propName.equals(WwTrackLineSelection.TRACK_PT_FINISHED)){
					hiddenButton.setSelected(true);
					processSelectionEvent(trackLineButton, trackSel);
			 }

		}
		  
	  });
	  lb.addPropertyChangeListener(new PropertyChangeListener() {

	      public void propertyChange(PropertyChangeEvent evt) {
				String propName = evt.getPropertyName();

	        if (propName.equals(WorldwindBoundingBoxBuilder.BBOX_DRAWN)) {
	          calculateBBOX(lb.getPolygon());
	        }
	        else if (propName.equals(WorldwindBoundingBoxBuilder.BBOX_COMPLETE)) {
	          ((Component) wwd).setCursor(Cursor.getDefaultCursor());
	          lb.setArmed(false);
	          hiddenButton.setSelected(true);
	        }
	        else if(propName.equals(WorldwindBoundingBoxBuilder.BBOX_STARTED)){
				activeSelectionSource = ActiveSelectionSource.BBOX;
	            firePropertyChange(DapWorldwindProcessPanel.DISABLE_BBOX, false, true);
	    		firePropertyChange(DapWorldwindProcessPanel.DISABLE_SLIDER, false, true);
	            
	        	polygonSel.clear();
	        	trackSel.clearTrackPoints();
	        }
	        
	      }
	    });
	  trackLineWidth.addActionListener(new ActionListener(){

		@Override
		public void actionPerformed(ActionEvent event) {
			processTrackLineWidthEvent();
		}
		  
	  });
	  trackLineWidth.addFocusListener(new FocusListener(){

		@Override
		public void focusGained(FocusEvent arg0) {}

		@Override
		public void focusLost(FocusEvent arg0) {
			processTrackLineWidthEvent();
		}
		  
	  });
	  importTrackButton.addActionListener(new ActionListener(){

		@Override
		public void actionPerformed(ActionEvent event) {
			activeSelectionSource = ActiveSelectionSource.TRACK_LINE;

			boolean disableSlider = trackSel.importTrackLineFromFile(importTrackButton);
			
			if(trackSel.getTrackPoints().size() > 0) {
				lb.clear();
	        	polygonSel.clear();
				firePropertyChange(DapWorldwindProcessPanel.DISABLE_BBOX, true, false);

				firePropertyChange(DapWorldwindProcessPanel.DISABLE_SLIDER, 
										disableSlider, !disableSlider);
			}
		}
		  
	  });
	  exportTrackButton.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent event) {
				trackSel.exportTrackLineToFile(exportTrackButton);
			}
			  
		  });
	  
  }
  

	  
  private void processTrackLineWidthEvent(){
	  Double newVal;
		try{
			newVal = Double.valueOf(trackLineWidth.getText());
			trackSel.setTrackLineWidth(newVal);
			if(activeSelectionSource == ActiveSelectionSource.TRACK_LINE)
				firePropertyChange(TRACK_WIDTH_UPDATED, false, true);
		}
		catch(NumberFormatException e){
			trackLineWidth.setText(String.valueOf(trackSel.getTrackLineWidth()));
		}
  }
  private void initComponents(boolean showTrackLineOptions) {
    JPanel buttonPanel = new JPanel(new MigLayout("hidemode 3"));
    bboxButton = new JToggleButton();
    URL imageBboxURL = SelectionMethodsPanel.class.getResource(BBOX_ICON);
    if(imageBboxURL != null) {
    	ImageIcon  bboxImage = new ImageIcon(imageBboxURL);
    	bboxButton.setIcon(bboxImage);
    }
    else {
    	bboxButton.setText(BBOX_LINE_TXT);
    }
    bboxButton.setToolTipText(BBOX_LINE_TXT);
    bboxButton.addActionListener(new ActionListener() {

    	public void actionPerformed(ActionEvent actionEvent) {
    		if(lb.isArmed())
    			hiddenButton.setSelected(true);
    		if(bboxButton.isSelected()){
				polygonSel.setArmed(false);
				trackSel.setArmed(false);
			}
			
    		
			processSelectionEvent(bboxButton, lb);

    	}
    });
    //  bboxButton.setEnabled(true);

    trackLineButton = new JToggleButton();
    URL imageTrackLineURL = SelectionMethodsPanel.class.getResource(TRACK_ICON);
    URL imageTrackLineSelURL = SelectionMethodsPanel.class.getResource(SELECTED_TRACK_ICON);

    if(imageTrackLineURL != null){
        ImageIcon trackLineImage = new ImageIcon(imageTrackLineURL);

    	trackLineButton.setIcon(trackLineImage);
    }
    else
        trackLineButton.setText(TRACK_LINE_TXT);

    trackLineButton.setToolTipText(TRACK_LINE_TXT);
    
    if(imageTrackLineSelURL != null) {
        ImageIcon trackLineSelectedImage = new ImageIcon(imageTrackLineSelURL);
    	trackLineButton.setSelectedIcon(trackLineSelectedImage);
   }
    trackLineButton.addActionListener(new ActionListener(){

		@Override
		public void actionPerformed(ActionEvent event) {
			if(trackSel.isArmed())
				hiddenButton.setSelected(true);
			if(trackLineButton.isSelected()){
				polygonSel.setArmed(false);
				lb.setArmed(false);
			}
			
			processSelectionEvent(trackLineButton, trackSel);
		}	
    });
 
    

    
    polygonButton = new JToggleButton();
    URL imagePolygonURL = SelectionMethodsPanel.class.getResource(POLYGON_ICON);

    if(imagePolygonURL != null){
        ImageIcon polygonImage = new ImageIcon(imagePolygonURL);
    	polygonButton.setIcon(polygonImage);
    }
    else
        polygonButton.setText(POLYGON_TXT);

    polygonButton.setToolTipText(POLYGON_TXT);
    polygonButton.addActionListener(new ActionListener(){

		@Override
		public void actionPerformed(ActionEvent event) {
			if(polygonSel.isArmed())
				hiddenButton.setSelected(true);
			if(polygonButton.isSelected()){
				trackSel.setArmed(false);
				lb.setArmed(false);
			}
			processSelectionEvent(polygonButton, polygonSel);

		}	
    });
    
    importTrackButton = new JButton("Import Track");
    importTrackButton.setToolTipText("Import track line from csv file");
    exportTrackButton = new JButton("Export Track");
    exportTrackButton.setToolTipText("Export track line to csv file");
    trackLineWidth = new JTextField();
    trackLineWidth.setColumns(4);
    
    trackLineWidth.setText(String.valueOf(trackSel.getTrackLineWidth()));
    ButtonGroup buttonGroup = new ButtonGroup();
    buttonGroup.add(bboxButton);
    buttonGroup.add(trackLineButton);
    buttonGroup.add(polygonButton);
    hiddenButton = new JToggleButton();
    //add an invisible button so that 1 button can always be selected
    buttonGroup.add(hiddenButton);
    buttonPanel.add(bboxButton,"sgy");
    buttonPanel.add(polygonButton,"sgy");
    if(showTrackLineOptions){
    	buttonPanel.add(trackLineButton,"sgy");
    	buttonPanel.add(importTrackButton,"sgy");// "split 2, flowy");
    	//  buttonPanel.add(exportTrackButton);
    	trackRadiusLabel = new JLabel("Track Radius (km)");
    	buttonPanel.add(trackRadiusLabel);
    	buttonPanel.add(trackLineWidth);
    }
    add(buttonPanel);
  }
  
  
  private void processSelectionEvent(JToggleButton button, 
		  							WorldWindMapControl control){

	  if(button.isSelected()) 
	        ((Component) wwd).setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
		
		else 
	        ((Component) wwd).setCursor(Cursor.getDefaultCursor());
		control.setArmed(button.isSelected());
	  
  }
  
  public void setSelectedLocsFromTrackLine(List<LatLon> selectedLoc){
	  polygonSel.setSelectedLocs(selectedLoc);
  }
  public void calculateBBOX(SurfacePolygon polygon) {
	
    if (polygon.getReferencePosition() != null) {
    	
      LatLon point = WorldwindUtils.normalizeLatLon(LatLon.fromDegrees(polygon.getReferencePosition().getLatitude().degrees, 
    		  polygon.getReferencePosition().getLongitude().degrees));
      LatLonPointImpl fpoint = new LatLonPointImpl(point.getLatitude().getDegrees(), point.getLongitude().getDegrees());

      bbox = new LatLonRect(fpoint,fpoint);
    } else {
      // Reset point
      LatLonPointImpl fpoint = new LatLonPointImpl(0,0);
      bbox = new LatLonRect(fpoint,fpoint);
    }
    for (LatLon pt : polygon.getOuterBoundary()) {
    

      LatLonPoint pti = (LatLonPoint)new LatLonPointImpl(pt.getLatitude().getDegrees(), pt.getLongitude().getDegrees());
      if (!bbox.contains(pti)) { 
        bbox.extend(pti);
      }
    }
 
    firePropertyChange(BOUNDS_STORED, false, true);
  }
  
  public LatLonRect getBBOX() {
    return bbox;
  }

  public void setBBOX(LatLonRect llr) {
    lb.clear();
    LatLon ul = WorldwindUtils.normalizeLatLon(
    		LatLon.fromDegrees(llr.getUpperLeftPoint().getLatitude(), 
    				llr.getUpperLeftPoint().getLongitude()));
    LatLon lr = WorldwindUtils.normalizeLatLon(
    		LatLon.fromDegrees(llr.getLowerRightPoint().getLatitude(), 
    				llr.getLowerRightPoint().getLongitude()));

    lb.setUpperLeftPoint(Position.fromDegrees(ul.getLatitude().degrees, 
    		ul.getLongitude().degrees));
    lb.setLowerRightPoint(Position.fromDegrees(lr.getLatitude().degrees, 
    		lr.getLongitude().degrees));
    lb.redraw();
    calculateBBOX(lb.getPolygon());
  }

public ActiveSelectionSource getActiveSelectionSource() {
	return activeSelectionSource;
}

public WwTrackLineSelection getTrackSel() {
	return trackSel;
}

public JToggleButton getTrackLineButton() {
	return trackLineButton;
}



public WwPolygonSelection getPolygonSel() {
	return polygonSel;
}
}
