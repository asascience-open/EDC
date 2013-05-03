package com.asascience.edc.map;

import com.asascience.edc.map.BoundingBoxPanel.BBoxChanged;

import java.awt.Dimension;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.text.DecimalFormat;
import javax.swing.JLabel;
import javax.swing.JPanel;
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

  
  
  private DecimalFormat fmt = new DecimalFormat("###.###");
  private JDoubleField north = new JDoubleField(fmt);
  private JDoubleField south = new JDoubleField(fmt);
  private JDoubleField east = new JDoubleField(fmt);
  private JDoubleField west = new JDoubleField(fmt);
  private PropertyChangeSupport pcs;
  private BBoxChanged bboxEvent = new BBoxChanged();
  private boolean dataIs360;
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

    this.add(new JLabel("N"), "cell 3 1, align center");
    this.add(north, "cell 3 2,  align center");
    this.add(new JLabel("W"), "cell 1 3, align center");
    this.add(west, "cell 2 3,  align center");
    this.add(new JLabel("E"), "cell 5 3, align center");
    this.add(east, "cell 4 3,  align center");
    this.add(new JLabel("S"), "cell 3 5, align center");
    this.add(south, "cell 3 4,  align center");
    addListeners();
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
