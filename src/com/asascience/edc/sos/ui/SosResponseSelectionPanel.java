package com.asascience.edc.sos.ui;

import com.asascience.edc.gui.ResponseFormatRadioButton;
import com.asascience.edc.sos.requests.ResponseFormat;
import com.asascience.ui.RadioList;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import net.miginfocom.swing.MigLayout;

/**
 * SosResponseSelectionPanel.java
 * 
 * @author Kyle Wilcox <kwilcox@asascience.com>
 */
public final class SosResponseSelectionPanel extends JPanel {

  private RadioList radioList;
  private List<ResponseFormat> localVariables;
  private PropertyChangeSupport pcs;
  private String panelTitle;

  public SosResponseSelectionPanel(String title) {
    this.panelTitle = title;
    pcs = new PropertyChangeSupport(this);
    initComponents();
    getCblVars().addPropertyChangeListener(new RadioListPropertyListener());
  }

  public void setResponseFormats(List<ResponseFormat> formats) {
    localVariables = formats;
    setResponses();
  }

  public void setResponses() {
    if (localVariables == null) {
      return;
    }

    radioList.makeRadioList(localVariables, false);

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

  public void initComponents() {
    setLayout(new MigLayout("gap 0, fill"));

    radioList = new RadioList();

    JScrollPane sp = new JScrollPane();
    sp.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
    sp.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
    sp.setBorder(BorderFactory.createTitledBorder(panelTitle + ": "));
    sp.setViewportView(radioList);
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

  class RadioListPropertyListener implements PropertyChangeListener {

    public void propertyChange(PropertyChangeEvent e) {
      if (e.getPropertyName().equals("selected")) {
        pcs.firePropertyChange("selected", null, ((ResponseFormatRadioButton)e.getOldValue()).getResponseFormat());
      }
    }
  }
}
