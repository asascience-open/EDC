package com.asascience.edc.map.view;

import com.asascience.edc.map.view.BoundingBoxPanel.BBoxChanged;
import com.asascience.edc.nc.NetcdfConstraints;
import com.asascience.openmap.ui.OMSelectionMapPanel;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.text.DecimalFormat;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;
import javax.swing.border.EtchedBorder;
import net.miginfocom.swing.MigLayout;
import org.softsmithy.lib.swing.JDoubleField;
import ucar.unidata.geoloc.LatLonPointImpl;
import ucar.unidata.geoloc.LatLonRect;

/**
 * BoundingBoxPanel.java
 * 
 * @author Kyle Wilcox <kwilcox@asascience.com>
 */
public class BoundingBoxPanel extends JPanel {
  public static final String AOI_SAVE = "aoisave";
  public static final String AOI_APPLY = "aoiapply";
  public static final String AOI_REMALL = "aoiremall";

  
  private DecimalFormat fmt = new DecimalFormat("###.###");
  private JDoubleField north = new JDoubleField(fmt);
  private JDoubleField south = new JDoubleField(fmt);
  private JDoubleField east = new JDoubleField(fmt);
  private JDoubleField west = new JDoubleField(fmt);
  private JLabel northLabel;
  private JLabel southLabel;
  private JLabel eastLabel;
  private JLabel westLabel;

  private PropertyChangeSupport pcs;
  private BBoxChanged bboxEvent = new BBoxChanged();
  private boolean dataIs360;
  protected JPopupMenu aoiMenu;
  protected JButton aoiButton;
  protected JMenuItem addAoi;
  protected JMenu useAoi;
  protected ActionListener menuActionListener;
  protected JMenuItem remAllAoi;

  public BoundingBoxPanel() {
	  
    pcs = new PropertyChangeSupport(this);
    dataIs360 = false;
    int numCols = 3;
    this.setLayout(new MigLayout("gap 0, fill"));
    this.setBorder(new EtchedBorder());
    north.setColumns(numCols);
    south.setColumns(numCols);
    east.setColumns(numCols);
    west.setColumns(numCols);
    north.setMinimumSize(new Dimension(25, north.getMinimumSize().height));
    south.setMinimumSize(new Dimension(25, north.getMinimumSize().height));
    east.setMinimumSize(new Dimension(25, north.getMinimumSize().height));
    west.setMinimumSize(new Dimension(25, north.getMinimumSize().height));
    northLabel = new JLabel("N");
    southLabel = new JLabel("S");
    westLabel = new JLabel("W");
    eastLabel = new JLabel("E");
    
    this.add(northLabel, "cell 3 1, align center");
    this.add(north, "cell 3 2,  align center");
    this.add(westLabel, "cell 1 3, align center");
    this.add(west, "cell 2 3,  align center");
    this.add(eastLabel, "cell 5 3, align center");
    this.add(east, "cell 4 3,  align center");
    this.add(southLabel, "cell 3 5, align center");
    this.add(south, "cell 3 4,  align center");
    
    menuActionListener = new ActionListener() {

        public void actionPerformed(ActionEvent e) {
          if (e.getActionCommand().equals(AOI_SAVE)) {
        	  if (north.getValue() != null && 
              		west.getValue() != null &&
              		south.getValue() != null &&
              		east.getValue() != null) { 
              String defName = "AOI_[";
              LatLonRect rect = getBoundingBox();
              DecimalFormat df = new DecimalFormat("#,##0.00");
              defName += df.format(rect.getLatMin()) + "," + df.format(rect.getLonMin()) + ","
                      + df.format(rect.getLatMax()) + "," + df.format(rect.getLonMax());
              defName += "]";
 
              pcs.firePropertyChange(AOI_SAVE, defName, rect);
            }
          } else if (e.getActionCommand().equals(AOI_APPLY)) {
        	  JMenuItem source =(JMenuItem) e.getSource();
        	  String aoiText  = source.getText();
        	  String[] aoiS = aoiText.replaceAll("AOI_\\[","").split("(,)|(])");

        	  setBoundingBox(Double.valueOf(aoiS[2]), Double.valueOf(aoiS[3]),
        			  Double.valueOf(aoiS[0]), Double.valueOf(aoiS[1]));
        	  pcs.firePropertyChange("bboxchange", null, null);
            pcs.firePropertyChange(AOI_APPLY, null, source.getText());
          } 
          else if (e.getActionCommand().equals(AOI_REMALL)) {
            pcs.firePropertyChange(AOI_REMALL, false, true);
          } 
        }
      };
    
    
    
    // make the AOI menu items
    aoiMenu = new JPopupMenu("AOIs");
    addAoi = new JMenuItem("Save Current AOI...");
    addAoi.setActionCommand(AOI_SAVE);
    addAoi.addActionListener(menuActionListener);

    useAoi = new JMenu("Apply Existing AOI...");
    useAoi.setActionCommand(AOI_APPLY);
    useAoi.addActionListener(menuActionListener);


    remAllAoi = new JMenuItem("Clear AOI List");
    remAllAoi.setActionCommand(AOI_REMALL);
    remAllAoi.addActionListener(menuActionListener);


    // construct the AOI menu
    aoiMenu.add(addAoi);
    aoiMenu.add(useAoi);

    aoiMenu.add(new JSeparator());
    aoiMenu.add(remAllAoi);

    // attach the AOI menu to a button on the toolbar
    aoiButton = new JButton("AOIs");
    aoiButton.addActionListener(new ActionListener() {

      public void actionPerformed(ActionEvent ae) {
        if (north.getValue() != null && 
        		west.getValue() != null &&
        		south.getValue() != null &&
        		east.getValue() != null) {
          addAoi.setEnabled(true);
      
        } else {
          addAoi.setEnabled(false);        
        }

        aoiMenu.show(aoiButton, 0, aoiButton.getHeight());
      }
    });

    this.add(aoiButton,"cell 3 7, align center,pad 0 0 0 0 ");
  

    addListeners();
  }

  
  public void createAoiSubmenu(List<String> aois) {
	  for(Component mi : this.useAoi.getComponents()) {
		  if (mi instanceof JMenuItem) {
			  ((JMenuItem)mi).removeActionListener(menuActionListener);
		  }
	  }
	  this.useAoi.removeAll();
	  if (aois.size() > 0) {
		  this.useAoi.setEnabled(true);
		  JMenuItem mi;
		  
		  for(String aoi : aois) {
			  mi = new JMenuItem(aoi);
			  mi.setActionCommand(AOI_APPLY);
			  mi.addActionListener(menuActionListener);
			  this.useAoi.add(mi);
			  
		  }
	  }
	  else {
		  this.useAoi.setEnabled(false);
	  }
  }
  
  @Override
  public void setEnabled(boolean enable){
	  super.setEnabled(enable);
	  north.setEnabled(enable);
	  south.setEnabled(enable);
	  east.setEnabled(enable);
	  west.setEnabled(enable);
	  northLabel.setEnabled(enable);
	  southLabel.setEnabled(enable);
	  eastLabel.setEnabled(enable);
	  westLabel.setEnabled(enable);
  }
  public LatLonRect getBoundingBox() {
    LatLonPointImpl uL = new LatLonPointImpl(north.getDoubleValue(), west.getDoubleValue());
    LatLonPointImpl lR = new LatLonPointImpl(south.getDoubleValue(), east.getDoubleValue());
    LatLonRect llr = new LatLonRect(uL, lR);
 
    return llr;
  }

  public void setBoundingBox(LatLonRect llr) {
    setBoundingBox(llr.getUpperRightPoint().getLatitude(), llr.getUpperRightPoint().getLongitude(), 
    		llr.getLowerLeftPoint().getLatitude(), llr.getLowerLeftPoint().getLongitude());
  }

  public void setBoundingBox(double n, double e, double s, double w) {
    removeListeners();
    north.setDoubleValue(n);
    east.setDoubleValue(e);
    south.setDoubleValue(s);
    west.setDoubleValue(w);
    if((e == 360.0 && w == 0.0) ||
       (e ==0.0 && w == 360.0)) {
    	dataIs360 = true;
    }
    addListeners();
  }

  private void removeListeners() {
    north.removePropertyChangeListener("value", bboxEvent);
    east.removePropertyChangeListener("value", bboxEvent);
    south.removePropertyChangeListener("value", bboxEvent);
    west.removePropertyChangeListener("value", bboxEvent);
  }

  private void addListeners() {
    north.addPropertyChangeListener("value", bboxEvent);
    east.addPropertyChangeListener("value", bboxEvent);
    south.addPropertyChangeListener("value", bboxEvent);
    west.addPropertyChangeListener("value", bboxEvent);
  }

  @Override
  public void addPropertyChangeListener(java.beans.PropertyChangeListener l) {
    pcs.addPropertyChangeListener(l);
  }

  class BBoxChanged implements PropertyChangeListener {

    public void propertyChange(PropertyChangeEvent evt) {
      if (evt.getPropertyName().equals("value")) {
        pcs.firePropertyChange("bboxchange", null, null);
      }
    }
  }

public boolean isDataIs360() {
	return dataIs360;
}
}
