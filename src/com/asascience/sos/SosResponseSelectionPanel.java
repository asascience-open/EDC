/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.asascience.sos;

import com.asascience.ui.RadioList;
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
public final class SosResponseSelectionPanel extends JPanel {

  private RadioList radioList;
  private List<String> localVariables;
  private PropertyChangeSupport pcs;
  private String panelTitle;

  public SosResponseSelectionPanel(String title) {
    this.panelTitle = title;
    pcs = new PropertyChangeSupport(this);
    initComponents();
    getCblVars().addPropertyChangeListener(new RadioListPropertyListener());
  }

  public void addResponseFormats(List<String> formats) {
    localVariables = formats;
    setResponses();
  }

  public void setResponses() {
    if (localVariables == null) {
      return;
    }

    radioList.makeRadioList(localVariables, true);

    // remove any existing pcl's
    PropertyChangeListener[] pcls = getCblVars().getPropertyChangeListeners();
    for (int i = pcls.length - 1; i >= 0; i--) {
      getCblVars().removePropertyChangeListener(pcls[i]);
    }
  }

  public RadioList getCblVars() {
    return radioList;
  }

  public String getSelectedResponse() {
    return radioList.getSelected();
  }

  public void setAvailableResponseFormats(List<SensorPoint> selectedPoints) {
    List<String> formats = new ArrayList<String>();
    for (SensorPoint sp : selectedPoints) {
      for (String f : sp.getSensor().getResponseFormats()) {
        if (!formats.contains(f)) {
          formats.add(f);
        }
      }
    }
    addResponseFormats(formats);
  }

  public void initComponents() {
    setLayout(new MigLayout("gap 0, fill"));

    radioList = new RadioList();
    radioList.setLabelLengthLimit(40);

    JScrollPane sp = new JScrollPane();
    sp.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
    sp.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
    sp.setBorder(BorderFactory.createTitledBorder(panelTitle + ": "));
    sp.setViewportView(radioList);
    add(sp, "grow");
  }

  @Override
  public void addPropertyChangeListener(java.beans.PropertyChangeListener l) {
    pcs.addPropertyChangeListener(l);
  }
  @Override
  public void removePropertyChangeListener(java.beans.PropertyChangeListener l) {
    pcs.removePropertyChangeListener(l);
  }

  class RadioListPropertyListener implements PropertyChangeListener {

    public void propertyChange(PropertyChangeEvent e) {
      if (e.getPropertyName().equals("selected")) {
        pcs.firePropertyChange(e);
      }
    }
  }
}
