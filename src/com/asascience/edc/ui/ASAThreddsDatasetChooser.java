// $Id: ThreddsDatasetChooser.java 50 2006-07-12 16:30:06Z caron $
/*
 * Copyright 1997-2006 Unidata Program Center/University Corporation for
 * Atmospheric Research, P.O. Box 3000, Boulder, CO 80307,
 * support@unidata.ucar.edu.
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or (at
 * your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser
 * General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, write to the Free Software Foundation,
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */
package com.asascience.edc.ui;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Iterator;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import com.asascience.edc.gui.OpendapInterface;
import com.asascience.ui.JCloseableTabbedPane;

import thredds.catalog.DatasetFilter;
import thredds.catalog.InvAccess;
import thredds.catalog.InvCatalog;
import thredds.catalog.InvCatalogFactory;
import thredds.catalog.InvDataset;
import thredds.catalog.ServiceType;
import ucar.util.prefs.PreferencesExt;

/**
 * A Swing widget for THREDDS clients that combines a CatalogChooser, a
 * QueryChooser, and a SearchChooser widget. PropertyChangeEvent events are
 * thrown to notify you of various user actions; see addPropertyChangeListener.
 * <p>
 * You can use the ThreddsDatasetChooser:
 * <ol>
 * <li>add the components into your own JTabbedPanel.
 * <li>wrapped in a JDialog for popping up
 * <li>as a standalone application through its main() method
 * </ol>
 * Example:
 * 
 * <pre>
 * datasetChooser = new ThreddsDatasetChooser( prefs, tabbedPane);
 *     datasetChooser.addPropertyChangeListener(  new java.beans.PropertyChangeListener() {
 *       public void propertyChange( java.beans.PropertyChangeEvent e) {
 *         if (e.getPropertyName().equals(&quot;Dataset&quot;)) {
 *           thredds.catalog.InvDataset ds = (thredds.catalog.InvDataset) e.getNewValue();
 *           setDataset( ds);
 *          }
 *         }
 *       }
 *     });
 * </pre>
 * 
 * To use as popup dialog box:
 * 
 * <pre>
 * ThreddsDatasetChooser datasetChooser = new ThreddsDatasetChooser(prefs, null);
 * JDialog datasetChooserDialog = datasetChooser.makeDialog(&quot;Open THREDDS dataset&quot;, true);
 * datasetChooserDialog.show();
 * </pre>
 * 
 * When using as a standalone application, the default behavior is to write the
 * dataURLs of the selections to standard out. Copy main() and make changes as
 * needed.
 * 
 * <pre>
 * java -classpath clientUI.jar;... thredds.catalog.ui.ThreddsDatasetChooser
 * </pre>
 * 
 * 
 * @author John Caron
 * @version $Id: ThreddsDatasetChooser.java 50 2006-07-12 16:30:06Z caron $
 */
public class ASAThreddsDatasetChooser extends JPanel {

  private ASACatalogChooser catalogChooser;
  private JCloseableTabbedPane tabbedPane;
  private boolean pipeOut = true; // send results to standard out
  private boolean messageOut = false; // send results to popup message
  private JFrame parentFrame; // need for popup messages
  private boolean debugResolve = false;

  /**
   * Usual Constructor. Create a CatalogChooser and a QueryChooser widget, add
   * them to a JTabbedPane.
   *
   * @param prefs
   *            persistent storage, may be null
   * @param tabs
   *            add panels to this JTabbedPane, may be null if you are using
   *            as Dialog.
   * @param parent
   */
  public ASAThreddsDatasetChooser(PreferencesExt prefs, JCloseableTabbedPane tabs, OpendapInterface parent) {
    this(prefs, tabs, parent, false, false);
  }

  /**
   * General Constructor. Create a CatalogChooser and a QueryChooser widget,
   * add them to a JTabbedPane. Optionally write to stdout and/or pop up event
   * messsages.
   *
   * @param prefs
   *            persistent storage
   * @param tabs
   *            add to this JTabbedPane
   * @param parent
   * @param messageOutput
   *            send selection to popup message
   * @param pipeOutput
   */
  public ASAThreddsDatasetChooser(ucar.util.prefs.PreferencesExt prefs, JCloseableTabbedPane tabs,
          OpendapInterface parent, boolean pipeOutput, boolean messageOutput) {

    this.parentFrame = parent.getMainFrame();
    this.pipeOut = pipeOutput;
    this.messageOut = messageOutput;

    // create the catalog chooser
    PreferencesExt node = (prefs == null) ? null : (PreferencesExt) prefs.node("catChooser");
    catalogChooser = new ASACatalogChooser(node, parent);
    catalogChooser.addPropertyChangeListener(new java.beans.PropertyChangeListener() {

      public void propertyChange(java.beans.PropertyChangeEvent e) {
        if (e.getPropertyName().equals("InvAccess")) {
          firePropertyChangeEvent(e);
        }
        if (e.getPropertyName().equals("Dataset") || e.getPropertyName().equals("File")) {
          firePropertyChangeEvent(e);
        }
      }
    });

    // the overall UI
    tabbedPane = (tabs == null) ? new JCloseableTabbedPane() : tabs;
    tabbedPane.addTabNoClose("Browse", catalogChooser);
    tabbedPane.setSelectedComponent(catalogChooser);

    setLayout(new BorderLayout());
    add(tabbedPane, BorderLayout.CENTER);
  }

  /**
   * Set a dataset filter to be used on all catalogs. To turn off, set to
   * null.
   *
   * @param filter
   *            DatasetFilter or null
   */
  public void setDatasetFilter(DatasetFilter filter) {
    catalogChooser.setDatasetFilter(filter);
  }

  /** Get the component CatalogChooser */
  public ASACatalogChooser getCatalogChooser() {
    return catalogChooser;
  }

  /** save the state */
  public void save() {
    catalogChooser.save();
  }

  private void firePropertyChangeEvent(PropertyChangeEvent event) {
    // System.err.println("firePropertyChangeEvent "+((InvDatasetImpl)ds).dump());
    if (pipeOut) {
      pipeEvent(event);
    }
    if (messageOut) {
      messageEvent(event);
    }

    // Process the listeners last to first
    Object[] listeners = listenerList.getListenerList();
    for (int i = listeners.length - 2; i >= 0; i -= 2) {
      if (listeners[i] == PropertyChangeListener.class) {
        ((PropertyChangeListener) listeners[i + 1]).propertyChange(event);
      }
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
   * <li>propertyName = "InvAccess" getNewValue() = InvAccess chosen.
   * </ul>
   */
  @Override
  public void addPropertyChangeListener(PropertyChangeListener l) {
    listenerList.add(PropertyChangeListener.class, l);
  }

  /**
   * Remove a PropertyChangeEvent Listener.
   */
  @Override
  public void removePropertyChangeListener(PropertyChangeListener l) {
    listenerList.remove(PropertyChangeListener.class, l);
  }

  private void messageEvent(java.beans.PropertyChangeEvent e) {
    StringBuffer buff = new StringBuffer();
    buff.append("Event propertyName = ");
    buff.append(e.getPropertyName());
    Object newValue = e.getNewValue();
    if (newValue != null) {
      buff.append(", class = ");
      buff.append(newValue.getClass().getName());
    }
    buff.append("\n");

    if (e.getPropertyName().equals("Dataset")) {
      showDatasetInfo(buff, (thredds.catalog.InvDataset) e.getNewValue());

    } else if (e.getPropertyName().equals("Datasets")) {
      Object[] ds = (Object[]) e.getNewValue();
      buff.append(" element class = ");
      buff.append(ds[0].getClass().getName());
      buff.append("\n");

      for (int i = 0; i < ds.length; i++) {
        if (ds[i] instanceof InvDataset) {
          showDatasetInfo(buff, (InvDataset) ds[i]);
        }
      }
    }

    try {
      JOptionPane.showMessageDialog(parentFrame, buff);
    } catch (HeadlessException he) {
    }
  }

  private void pipeEvent(java.beans.PropertyChangeEvent e) {
    StringBuffer buff = new StringBuffer();

    if (e.getPropertyName().equals("Dataset")) {
      getAccessURLs(buff, (thredds.catalog.InvDataset) e.getNewValue());

    } else if (e.getPropertyName().equals("Datasets")) {
      Object[] ds = (Object[]) e.getNewValue();
      for (int i = 0; i < ds.length; i++) {
        if (ds[i] instanceof InvDataset) {
          getAccessURLs(buff, (InvDataset) ds[i]);
        }
      }
    }

    System.err.println(buff);
  }

  private void getAccessURLs(StringBuffer buff, thredds.catalog.InvDataset ds) {
    Iterator iter = ds.getAccess().iterator();
    while (iter.hasNext()) {
      thredds.catalog.InvAccess ac = (thredds.catalog.InvAccess) iter.next();
      buff.append(ac.getStandardUrlName());
      buff.append(" ");
      buff.append(ac.getService().getServiceType());
      buff.append("\n");
    }
  }

  private void showDatasetInfo(StringBuffer buff, thredds.catalog.InvDataset ds) {
    buff.append(" Dataset = ");
    buff.append(ds.getName());
    buff.append(", dataType = ");
    buff.append(ds.getDataType());
    buff.append("\n");
    Iterator iter = ds.getAccess().iterator();
    while (iter.hasNext()) {
      thredds.catalog.InvAccess ac = (thredds.catalog.InvAccess) iter.next();
      buff.append("  service = ");
      buff.append(ac.getService().getServiceType());
      buff.append(", url = ");
      buff.append(ac.getStandardUrlName());
      buff.append("\n");
      System.err.println("  url = ");
      buff.append(ac.getStandardUrlName());
    }
  }

  private void resolve(thredds.catalog.InvDataset ds) {
    InvAccess resolverAccess;
    if (null != (resolverAccess = ds.getAccess(ServiceType.RESOLVER))) {
      String urlName = resolverAccess.getStandardUrlName();
      if (debugResolve) {
        System.err.println(" resolve=" + urlName);
      }
      try {
        InvCatalogFactory factory = InvCatalogFactory.getDefaultFactory(true);
        InvCatalog catalog = factory.readXML(urlName); // should be
        // asynch ?
        if (catalog == null) {
          return;
        }
        StringBuilder buff = new StringBuilder();
        if (!catalog.check(buff)) {
          javax.swing.JOptionPane.showMessageDialog(this, "Invalid catalog <" + urlName + ">\n"
                  + buff.toString());
          if (debugResolve) {
            System.err.println("Invalid catalog <" + urlName + ">\n" + buff.toString());
          }
          return;
        }
        InvDataset top = catalog.getDataset();
        if (top.hasAccess()) {
          firePropertyChangeEvent(new PropertyChangeEvent(this, "Dataset", null, top));
        } else {
          Object[] dsa = top.getDatasets().toArray();
          firePropertyChangeEvent(new PropertyChangeEvent(this, "Datasets", null, dsa));
        }
        return;

      } catch (Exception e) {
        e.printStackTrace();
        return;
      }
    }
  }

  /**
   * Wrap this in a JDialog component.
   *
   * @param parent
   *            put dialog on top of this, may be null
   * @param title
   *            dialog window title
   * @param modal
   *            is modal
   */
  public JDialog makeDialog(JFrame parent, String title, boolean modal) {
    return new Dialog(parentFrame, title, modal);
  }

  private class Dialog extends JDialog {

    private Dialog(JFrame frame, String title, boolean modal) {
      super(frame, title, modal);

      // L&F may change
      UIManager.addPropertyChangeListener(new PropertyChangeListener() {

        public void propertyChange(PropertyChangeEvent e) {
          if (e.getPropertyName().equals("lookAndFeel")) {
            SwingUtilities.updateComponentTreeUI(ASAThreddsDatasetChooser.Dialog.this);
          }
        }
      });

      // add a dismiss button
      JButton dismissButton = new JButton("Dismiss");
      // buttPanel.add(dismissButton, null);

      dismissButton.addActionListener(new ActionListener() {

        public void actionPerformed(ActionEvent evt) {
          setVisible(false);
        }
      });

      // add it to contentPane
      Container cp = getContentPane();
      cp.setLayout(new BorderLayout());
      cp.add(ASAThreddsDatasetChooser.this, BorderLayout.CENTER);
      pack();
    }
  }
}
