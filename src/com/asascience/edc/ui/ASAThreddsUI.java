/*
 * ASAThreddsUI.java
 *
 * Created on October 22, 2007, 9:20 AM
 *
 * Applied Science Associates, Inc.
 * Copyright 2007.  All rights reserved.
 */
package com.asascience.edc.ui;

import com.asascience.edc.ui.ASAThreddsDatasetChooser;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import ucar.util.prefs.PreferencesExt;

/**
 * 
 * @author CBM
 */
public class ASAThreddsUI extends JPanel {

  private PreferencesExt prefs;
  private ASAThreddsDatasetChooser datasetChooser;

  /**
   * Creates a new instance of ASAThreddsUI
   */
  public ASAThreddsUI(PreferencesExt prefs, JTabbedPane tabPane, JFrame frame) {
    this.prefs = prefs;
    makeDatasetChooser(tabPane, frame);
  }

  private ASAThreddsDatasetChooser makeDatasetChooser(JTabbedPane tabbedPane, JFrame frame) {
    System.err.println("making datasetChooser");
    // datasetChooser = new ASAThreddsDatasetChooser( (PreferencesExt)
    // prefs.node("ThreddsDatasetChooser"),
    // tabbedPane, frame);
    System.err.println("datasetChooser made");
    // datasetChooser.addPropertyChangeListener(new
    // java.beans.PropertyChangeListener() {
    // public void propertyChange(java.beans.PropertyChangeEvent e) {
    // System.err.println(e.getPropertyName());
    // if (e.getPropertyName().equals("InvAccess")) {
    // thredds.catalog.InvAccess access = (thredds.catalog.InvAccess)
    // e.getNewValue();
    // setThreddsDatatype(access);
    // }
    //
    // if (e.getPropertyName().equals("Dataset") ||
    // e.getPropertyName().equals("File")) {
    // thredds.catalog.InvDataset ds = (thredds.catalog.InvDataset)
    // e.getNewValue();
    // setThreddsDatatype(ds, e.getPropertyName().equals("File"));
    // }
    // }
    // });
    // if (Debug.isSet("System/filterDataset"))
    // datasetChooser.setDatasetFilter( new DatasetFilter.ByServiceType(
    // ServiceType.DODS));

    datasetChooser.addPropertyChangeListener(new PropertyChangeListener() {

      public void propertyChange(PropertyChangeEvent e) {
        if (e.getPropertyName().equals("InvAccess")) {
          firePropertyChangeEvent(e);
          return;
        }
        //
        // if (e.getPropertyName().equals("Dataset") ||
        // e.getPropertyName().equals("File")) {
        // // // intercept XML, ASCII return types
        // // InvDataset ds = (InvDataset) e.getNewValue();
        // // InvAccess access = ds.getAccess( ServiceType.HTTPServer);
        // // if (access != null) {
        // // DataFormatType format = access.getDataFormatType();
        // // if (format == DataFormatType.PLAIN || format ==
        // DataFormatType.XML) {
        // // String urlString = access.getStandardUrlName();
        // //
        // //System.err.println("got station XML data access = "+urlString);
        // // thredds.util.IO.readURLcontents(urlString);
        // // xmlPane.setText(
        // thredds.util.IO.readURLcontents(urlString));
        // // xmlPane.gotoTop();
        // //// xmlWindow.show();
        // // xmlWindow.setVisible(true);
        // // return;
        // // }
        // // }
        //
        // // firePropertyChangeEvent( e);
        // }
      }
    });
    return datasetChooser;

  }

  /** save all data in the PersistentStore */
  public void storePersistentData() {
    // store.putBeanObject(VIEWER_SIZE, getSize());
    // store.putBeanObject(SOURCE_WINDOW_SIZE, (Rectangle)
    // sourceWindow.getBounds());

    if (datasetChooser != null) {
      datasetChooser.save();
    }
  }

  /**
   * Add a PropertyChangeEvent Listener. Throws a PropertyChangeEvent:
   * <ul>
   * <li>propertyName = "Dataset" or "File", getNewValue() = InvDataset
   * chosen.
   * <li>propertyName = "Datasets", getNewValue() = InvDataset[] chosen. This
   * can only happen if you have set doResolve = true, and the resolved
   * dataset is a list of datasets.
   * </ul>
   */
  public void addPropertyChangeListener(PropertyChangeListener l) {
    listenerList.add(PropertyChangeListener.class, l);
  }

  /**
   * Remove a PropertyChangeEvent Listener.
   */
  public void removePropertyChangeListener(PropertyChangeListener l) {
    listenerList.remove(PropertyChangeListener.class, l);
  }

  private void firePropertyChangeEvent(PropertyChangeEvent event) {

    // Process the listeners last to first
    Object[] listeners = listenerList.getListenerList();
    for (int i = listeners.length - 2; i >= 0; i -= 2) {
      if (listeners[i] == PropertyChangeListener.class) {
        ((PropertyChangeListener) listeners[i + 1]).propertyChange(event);
      }
    }
  }
}
