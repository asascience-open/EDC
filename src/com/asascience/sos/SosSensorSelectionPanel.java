/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.asascience.sos;

import com.asascience.ui.CheckBoxList;
import java.awt.Dimension;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import net.miginfocom.swing.MigLayout;

/**
 *
 * @author Kyle
 */
public final class SosSensorSelectionPanel extends JPanel {

  private SensorCheckBoxList cblVars;
  private List<SensorPoint> localSensors;
  private ArrayList sensorNames;
  private ArrayList sensorDescr;
  private ArrayList sensorChecks;
  private boolean getObsEnabled;
  private PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);
  private String panelTitle;

  public SosSensorSelectionPanel(SosProcessPanel parent, String title) {
    this.panelTitle = title;
    initComponents();
    getSensorChecks().addPropertyChangeListener(new CheckBoxPropertyListener());
  }

  public void setSensors(List<SensorPoint> sensors) {
    localSensors = sensors;
    if (localSensors == null) {
      return;
    }

    sensorNames = new ArrayList();
    sensorDescr = new ArrayList();
    sensorChecks = new ArrayList();

    for (SensorPoint s : localSensors) {
      sensorNames.add(s.getSensor().getName());
      sensorDescr.add(s.getSensor().getDescription());
      sensorChecks.add(Boolean.valueOf(s.isPicked()));
    }
    cblVars.clearCBList();
    cblVars.makeCBList(sensorNames, sensorDescr, sensorChecks, sensors, true);

    // remove any existing pcl's
    PropertyChangeListener[] pcls = getSensorChecks().getPropertyChangeListeners();
    for (int i = pcls.length - 1; i >= 0; i--) {
      getSensorChecks().removePropertyChangeListener(pcls[i]);
    }
  }
  
  public SensorCheckBoxList getSensorChecks() {
    return cblVars;
  }

  public void initComponents() {
    setLayout(new MigLayout("gap 0, fill"));

    cblVars = new SensorCheckBoxList(true, true, true);
    cblVars.setLabelLengthLimit(40);

    JScrollPane sp = new JScrollPane();
    sp.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
    sp.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
    sp.setBorder(BorderFactory.createTitledBorder(panelTitle + ": "));
    sp.setMinimumSize(new Dimension(330, 200));
    sp.setViewportView(cblVars);
    add(sp, "grow");
  }

  @Override
  public void addPropertyChangeListener(java.beans.PropertyChangeListener l) {
    propertyChangeSupport.addPropertyChangeListener(l);
  }
  @Override
  public void removePropertyChangeListener(java.beans.PropertyChangeListener l) {
    propertyChangeSupport.removePropertyChangeListener(l);
  }

  class CheckBoxPropertyListener implements PropertyChangeListener {

    public void propertyChange(PropertyChangeEvent e) {
      if (e.getPropertyName().equals("sensorClicked")) {
        propertyChangeSupport.firePropertyChange(e);
      }
    }
  }
}
