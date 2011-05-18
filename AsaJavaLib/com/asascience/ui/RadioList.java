/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.asascience.ui;

import com.asascience.edc.sos.ResponseFormatRadioButton;
import com.asascience.edc.sos.requests.ResponseFormat;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeSupport;
import java.util.Enumeration;
import java.util.List;
import javax.swing.ButtonGroup;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

/**
 *
 * @author Kyle
 */
public class RadioList extends JPanel implements ActionListener {

  private PropertyChangeSupport pcs;
  private int labelLengthLimit = 20;
  private ButtonGroup group;
  
  public RadioList() {
    group = new ButtonGroup();
    pcs = new PropertyChangeSupport(this);
    setLayout(new GridLayout(0, 1));
  }
  /*
  public void makeRadioList(List<String> items, boolean trimName) {
    clearRadioList();
    boolean c = true;
    for (String s : items) {
      group.add(addRadio(s, trimName, c));
      c = false;
    }
  }
  */

  public void makeRadioList(List<ResponseFormat> items, boolean trimName) {
    clearRadioList();
    boolean c = true;
    for (ResponseFormat rf : items) {
      group.add(addRadio(rf, trimName, c));
      c = false;
    }
  }

  public void clearRadioList() {
    for (Enumeration e=group.getElements(); e.hasMoreElements(); ) {
      JRadioButton r = (JRadioButton)e.nextElement();
      group.remove(r);
      remove(r);
    }
  }

  public JRadioButton addRadio(ResponseFormat rf, boolean trimName, boolean select) {
    ResponseFormatRadioButton r = new ResponseFormatRadioButton();
    String text;
    String name = rf.getName();
    if (trimName) {
      text = (name.length() > labelLengthLimit) ? name.substring(0, labelLengthLimit - 1) + "..." : name;
    } else {
      text = name;
    }
    r.setName(rf.getValue());
    r.setText(text);
    r.setResponseFormat(rf);
    r.addActionListener(this);
    r.setActionCommand(name);
    add(r);
    if (select) {
      r.setSelected(select);
      pcs.firePropertyChange("selected", r, r.getText());
    }
    return r;
  }

  public void actionPerformed(ActionEvent e) {
    JRadioButton cb = (JRadioButton) e.getSource();
    if (cb.isSelected()) {
      pcs.firePropertyChange("selected", cb, cb.getText());
    }
  }

  public String getSelected() {
    return group.getSelection().toString();
  }
  
  public void setLabelLengthLimit(int labelLengthLimit) {
    this.labelLengthLimit = labelLengthLimit;
  }

  @Override
  public void addPropertyChangeListener(java.beans.PropertyChangeListener l) {
    pcs.addPropertyChangeListener(l);
  }

  @Override
  public void removePropertyChangeListener(java.beans.PropertyChangeListener l) {
    pcs.removePropertyChangeListener(l);
  }
}
