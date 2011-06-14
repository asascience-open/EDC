/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.asascience.edc.sos.ui;

import com.asascience.edc.sos.map.WorldwindBoundingBoxBuilder;
import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.render.SurfacePolygon;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.JButton;
import javax.swing.JPanel;
import net.miginfocom.swing.MigLayout;
import ucar.unidata.geoloc.LatLonPoint;
import ucar.unidata.geoloc.LatLonPointImpl;
import ucar.unidata.geoloc.LatLonRect;

/**
 *
 * @author Kyle
 */
public class SosWorldwindBoundingBoxTool extends JPanel {

  private final WorldWindow wwd;
  private final WorldwindBoundingBoxBuilder lb;
  private JButton newButton;
  private LatLonRect bbox;

  public SosWorldwindBoundingBoxTool(WorldWindow wwdd) {
    super(new BorderLayout());
    this.wwd = wwdd;
    lb = new WorldwindBoundingBoxBuilder(wwd, null, null);
    lb.addPropertyChangeListener(new PropertyChangeListener() {

      public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName().equals("WorldwindBoundingBoxBuilder.BBOXDrawn")) {
          calculateBBOX(lb.getPolygon());
        }
        if (evt.getPropertyName().equals("WorldwindBoundingBoxBuilder.BBOXComplete")) {
          ((Component) wwd).setCursor(Cursor.getDefaultCursor());
          lb.setArmed(false);
          newButton.setEnabled(true);
        }
      }
    });
    initComponents();
  }

  private void initComponents() {
    JPanel buttonPanel = new JPanel(new MigLayout("gap 0, fill"));
    newButton = new JButton("Select by BBOX");
    newButton.addActionListener(new ActionListener() {

      public void actionPerformed(ActionEvent actionEvent) {
        lb.clear();
        lb.setArmed(true);
        newButton.setEnabled(false);
        ((Component) wwd).setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
      }
    });
    buttonPanel.add(newButton);
    newButton.setEnabled(true);

    add(buttonPanel);
  }
  
  public void calculateBBOX(SurfacePolygon polygon) {
    if (polygon.getReferencePosition() != null) {
      LatLonPointImpl fpoint = new LatLonPointImpl(polygon.getReferencePosition().getLatitude().getDegrees(), polygon.getReferencePosition().getLongitude().getDegrees());
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
    firePropertyChange("boundsStored", false, true);
  }
  
  public LatLonRect getBBOX() {
    return bbox;
  }

  public void setBBOX(LatLonRect llr) {
    lb.clear();
    lb.setUpperLeftPoint(Position.fromDegrees(llr.getUpperLeftPoint().getLatitude(), llr.getUpperLeftPoint().getLongitude()));
    lb.setLowerRightPoint(Position.fromDegrees(llr.getLowerRightPoint().getLatitude(), llr.getLowerRightPoint().getLongitude()));
    lb.redraw();
    calculateBBOX(lb.getPolygon());
  }
}