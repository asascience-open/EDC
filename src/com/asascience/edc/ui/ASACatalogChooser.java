// $Id: CatalogChooser.java 50 2006-07-12 16:30:06Z caron $
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

import com.asascience.edc.erddap.ErddapDataset;
import com.asascience.edc.erddap.gui.ErddapGetDatasetsProgressMonitor;
import com.asascience.edc.erddap.ErddapServer;
import com.asascience.edc.erddap.gui.ErddapDatasetViewer;
import com.asascience.edc.ui.combos.HistoryComboBox;
import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSplitPane;
import javax.swing.KeyStroke;
import javax.swing.RootPaneContainer;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.EventListenerList;

import net.miginfocom.swing.MigLayout;
import thredds.catalog.DatasetFilter;
import thredds.catalog.InvAccess;
import thredds.catalog.InvCatalog;
import thredds.catalog.InvCatalogImpl;
import thredds.catalog.InvCatalogRef;
import thredds.catalog.InvDataset;
import thredds.catalog.InvDatasetImpl;
import thredds.ui.catalog.ThreddsDatasetChooser;
import ucar.nc2.ui.widget.HtmlBrowser;
import ucar.nc2.dataset.NetcdfDataset;
import ucar.util.prefs.ui.ComboBox;

import com.asascience.edc.gui.OpendapInterface;
import com.asascience.edc.ui.combos.DirectComboBox;
import com.asascience.edc.ui.combos.ErddapComboBox;
import com.asascience.edc.ui.combos.SosComboBox;
import com.asascience.ui.CheckBoxList;
import com.asascience.utilities.BusyCursorActions;
import com.asascience.edc.sos.SosServer;
import com.asascience.edc.sos.ui.SosGetCapProgressMonitor;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Rectangle;
import javax.swing.JTabbedPane;
import javax.swing.SwingWorker;
import javax.swing.plaf.basic.BasicTabbedPaneUI;

/**
 * A Swing widget for THREDDS clients to access and choose from Dataset
 * Inventory catalogs. State is maintained in a ucar.util.Preferences store.
 * <p>
 * A list of catalogs is kept in a ComboBox, and the user can choose from them
 * and add new ones. When a catalog is chosen, its contents are displayed in a
 * CatalogTreeView. As the datasets are browsed, the metadata is displayed in an
 * HtmlBrowser widget.
 * <p>
 * When a new dataset is selected, a java.beans.PropertyChangeEvent is thrown,
 * see addPropertyChangeListener.
 * <p>
 * Use Example:
 * 
 * <pre>
 * // create widgets
 * catalogChooser = new thredds.ui.CatalogChooser( prefs);
 * catalogChooserDialog = catalogChooser.makeDialog(rootPaneContainer, &quot;Open THREDDS dataset&quot;, true);
 * // listen for selection
 * catalogChooser.addPropertyChangeListener(  new java.beans.PropertyChangeListener() {
 * public void propertyChange( java.beans.PropertyChangeEvent e) {
 * if (e.getPropertyName().equals(&quot;Dataset&quot;)) {
 * ..
 * }
 * }
 * });
 * // popup dialog
 * catalogChooserDialog.show();
 * </pre>
 * 
 * You can use the CatalogChooser alone, wrap it into a JDialog for popping up,
 * or use a ThreddsDatasetChooser instead, for a more complete interface.
 * 
 * @see ThreddsDatasetChooser
 * @see thredds.catalog.InvDataset
 * 
 * @author John Caron
 * @version $Id: CatalogChooser.java 50 2006-07-12 16:30:06Z caron $
 */
public class ASACatalogChooser extends JPanel {

  private static final String HDIVIDER = "HSplit_Divider";
  private ucar.util.prefs.PreferencesExt prefs;
  private EventListenerList listenerList = new EventListenerList();
  private String eventType = null;
  // ui
  private ComboBox catListBox;
  private ASACatalogTreeView catalogTree;
  private HtmlBrowser catalogHtmlViewer;
  private JSplitPane splitCatalog;
  private ErddapDatasetViewer erddapViewer;
  private JLabel statusLabel;
  private JPanel buttPanel;
  private JLabel sourceText;
  private JFrame parentFrame;
  private OpendapInterface odapInterface;
  private boolean datasetEvents = true;
  private boolean catrefEvents = false;
  private String currentURL = "";
  private boolean debugEvents = false;
  private boolean showHTML = false;
  private JButton acceptButton;
  private NetcdfDataset daDataset;
  private HistoryComboBox daListBox;
  private HistoryComboBox sosListBox;
  private HistoryComboBox erddapListBox;
  private JButton btnCatConnect;
  private JButton btnDirAccess;
  private JButton btnSOS;
  private JButton btnERDDAP;
  private JTabbedPane layers;
  private JPanel topPanel;

  /**
   * Constructor, with control over whether a comboBox of previous catalogs is
   * shown.
   *
   * @param prefs
   *            persistent storage, may be null.
   * @param showComboChooser
   * @param showOpenButton
   *            show the "open" button.
   * @param showFileChooser
   *            show a FileChooser (must have showComboChooser true)
   * @param parent
   */
  public ASACatalogChooser(ucar.util.prefs.PreferencesExt prefs, OpendapInterface parent) {
    this.prefs = prefs;
    this.parentFrame = parent.getMainFrame();

    this.odapInterface = parent;

    topPanel = null;

    // combo box holds the catalogs
    catListBox = new ComboBox(prefs);
    daListBox = new DirectComboBox(prefs);
    sosListBox = new SosComboBox(prefs);
    erddapListBox = new ErddapComboBox(prefs);

    /*
     * Catalog URL "Connect" Button
     */

    catalogHtmlViewer = new HtmlBrowser();
    catalogTree = new ASACatalogTreeView();
    splitCatalog = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, false, catalogTree, catalogHtmlViewer);
    if (prefs != null) {
      splitCatalog.setDividerLocation(prefs.getInt(HDIVIDER, 400));
    }
    acceptButton = new JButton("Subset & Process");

    erddapViewer = new ErddapDatasetViewer();
    erddapViewer.addPropertyChangeListener(new PropertyChangeListener() {

      public void propertyChange(PropertyChangeEvent evt) {
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        ErddapDataset erd;
        // GRIDDAP
        if (evt.getPropertyName().equals("griddap")) {
          erd = (ErddapDataset)evt.getNewValue();
          try {
            daDataset = NetcdfDataset.openDataset(erd.getGriddap());
            daDataset.setTitle(erd.getTitle());

            if (odapInterface.openDataset(daDataset)) {
              daListBox.addItem(erd.getGriddap());
            }
          } catch (IOException ex) {
            JOptionPane.showMessageDialog(odapInterface.getMainFrame(), "Cannot find the file\n\""
                    + erd.getGriddap() + "\"" + "\n\nPlease check the name and try again.");
            System.err.println("OI:directAccess: Invalid filename");
            ex.printStackTrace();
          }
        } else if (evt.getPropertyName().equals("tabledap")) {
          erd = (ErddapDataset)evt.getNewValue();
          odapInterface.openTabledap(erd);
        }
        
        setCursor(Cursor.getDefaultCursor());
      }
    });

    layers = new JTabbedPane();
    layers.setUI(new BasicTabbedPaneUI() {
      // Hides the tabs
      @Override
      protected void paintTab(Graphics g, int tabPlacement, Rectangle[] rects, int tabIndex, Rectangle iconRect, Rectangle textRect) {
        //super.paintTab(g, tabPlacement, rects, tabIndex, iconRect, textRect);
      }
    });
    layers.add(splitCatalog, "grow");
    layers.add(erddapViewer, "grow");
    layers.setEnabledAt(0, false);
    layers.setEnabledAt(1, false);

    btnCatConnect = new JButton("Connect");
    btnCatConnect.setToolTipText("Read the selected catalog");
    btnCatConnect.addActionListener(new ActionListener() {

      public void actionPerformed(ActionEvent evt) {
        String catalogURL = (String) catListBox.getSelectedItem();
        catalogTree.setCatalog(catalogURL.trim()); // will get "Catalog"
      }
    });

    /*
     * Direct Access URL "Connect" Button
     */
    btnDirAccess = new JButton("Connect");
    btnDirAccess.setToolTipText("Enter a dataset url for direct access");
    ActionListener al = new ActionListener() {

      public void actionPerformed(ActionEvent ae) {
        String dataUrl = "";
        try {
          dataUrl = daListBox.getSelectedItem().toString();

          if (dataUrl == null | dataUrl.equals("")) {
            return;
          }// cancel clicked

          System.err.println("Dataset Url: " + dataUrl);
          System.err.println("Retrieving data...");

          daDataset = NetcdfDataset.openDataset(dataUrl);
          daDataset.setTitle(dataUrl.substring(dataUrl.lastIndexOf(File.separator) + 1));

          if (odapInterface.openDataset(daDataset)) {
            daListBox.addItem(dataUrl);
          }

        } catch (IOException ex) {
          JOptionPane.showMessageDialog(odapInterface.getMainFrame(), "Cannot find the file\n\""
                  + dataUrl + "\"" + "\n\nPlease check the name and try again.");
          System.err.println("OI:directAccess: Invalid filename");
          ex.printStackTrace();
        }
      }
    };
    ActionListener cal = com.asascience.utilities.BusyCursorActions.createListener(
            odapInterface.getMainFrame(), al);
    btnDirAccess.addActionListener(cal);

    /*
     * SOS "Connect" Button
     */
    btnSOS = new JButton("Connect");
    btnSOS.setToolTipText("Read the selected SOS");
    btnSOS.addActionListener(new ActionListener() {

      public void actionPerformed(ActionEvent evt) {

        try {
          javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
              final JFrame frame = new JFrame("Get Capabilities");
              frame.setLayout(new MigLayout("fill"));
              frame.setPreferredSize(new Dimension(750, 400));
              frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

              String sosServerURL = sosListBox.getSelectedItem().toString();
              final SosServer myData = new SosServer(sosServerURL);
              JComponent newContentPanel = new SosGetCapProgressMonitor(myData, odapInterface);
              sosListBox.addItem(sosServerURL);
              newContentPanel.addPropertyChangeListener(new PropertyChangeListener() {

                public void propertyChange(PropertyChangeEvent e) {
                  String name = e.getPropertyName();
                  if (name.equals("closed")) {
                    frame.setVisible(false);
                    frame.dispose();
                  }
                  if (name.equals("taskcomplete")) {
                    frame.setVisible(false);
                    frame.dispose();
                    myData.getRequest().setHomeDir(odapInterface.getHomeDir());
                    odapInterface.openSOSDataset(myData,(SwingWorker)e.getOldValue());
                  }
                }
              });

              newContentPanel.setOpaque(true);
              frame.add(newContentPanel, "grow");
              frame.pack();
              frame.setVisible(true);
            }
          });
        } catch(Exception e) {

        }
      }
    });

    /*
     * ERDDAP "Connect" Button
     */
    btnERDDAP = new JButton("Connect");
    btnERDDAP.setToolTipText("Read the selected ERDDAP Server");
    btnERDDAP.addActionListener(new ActionListener() {

      public void actionPerformed(ActionEvent evt) {

        try {
          javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
              final JFrame frame = new JFrame("Get Datasets from ERDDAP");
              frame.setLayout(new MigLayout("fill"));
              frame.setPreferredSize(new Dimension(750, 400));
              frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

              String erddapServerURL = erddapListBox.getSelectedItem().toString();
              ErddapServer erddapServer = new ErddapServer(erddapServerURL);
              erddapViewer.setServer(erddapServer);
              JComponent newContentPanel = new ErddapGetDatasetsProgressMonitor(erddapViewer, odapInterface);
              erddapListBox.addItem(erddapServerURL);
              newContentPanel.addPropertyChangeListener(new PropertyChangeListener() {

                public void propertyChange(PropertyChangeEvent e) {
                  String name = e.getPropertyName();
                  if (name.equals("closed")) {
                    frame.setVisible(false);
                    frame.dispose();
                  }
                }
              });

              newContentPanel.setOpaque(true);
              frame.add(newContentPanel, "grow");
              frame.pack();
              frame.setVisible(true);
            }
          });
        } catch(Exception e) {

        }
      }
    });

    topPanel = new JPanel(new MigLayout("fillx, insets 0"));
    ActionListener radioListener = new ActionListener() {

      public void actionPerformed(ActionEvent ae) {
        boolean enable = true;
        String cmd = ae.getActionCommand();
        if (cmd.equals("cat")) {
          catListBox.setEnabled(enable);
          btnCatConnect.setEnabled(enable);
          daListBox.setEnabled(!enable);
          btnDirAccess.setEnabled(!enable);
          sosListBox.setEnabled(!enable);
          btnSOS.setEnabled(!enable);
          erddapListBox.setEnabled(!enable);
          btnERDDAP.setEnabled(!enable);
          layers.setVisible(enable);
          layers.setSelectedComponent(splitCatalog);
          acceptButton.setVisible(enable);
        } else if (cmd.equals("da")) {
          catListBox.setEnabled(!enable);
          btnCatConnect.setEnabled(!enable);
          daListBox.setEnabled(enable);
          btnDirAccess.setEnabled(enable);
          sosListBox.setEnabled(!enable);
          btnSOS.setEnabled(!enable);
          erddapListBox.setEnabled(!enable);
          btnERDDAP.setEnabled(!enable);
          layers.setVisible(!enable);
          acceptButton.setVisible(!enable);
        } else if (cmd.equals("sos")) {
          catListBox.setEnabled(!enable);
          btnCatConnect.setEnabled(!enable);
          daListBox.setEnabled(!enable);
          btnDirAccess.setEnabled(!enable);
          sosListBox.setEnabled(enable);
          btnSOS.setEnabled(enable);
          erddapListBox.setEnabled(!enable);
          btnERDDAP.setEnabled(!enable);
          layers.setVisible(!enable);
          acceptButton.setVisible(!enable);
        } else if (cmd.equals("erd")) {
          catListBox.setEnabled(!enable);
          btnCatConnect.setEnabled(!enable);
          daListBox.setEnabled(!enable);
          btnDirAccess.setEnabled(!enable);
          sosListBox.setEnabled(!enable);
          btnSOS.setEnabled(!enable);
          erddapListBox.setEnabled(enable);
          btnERDDAP.setEnabled(enable);
          layers.setVisible(enable);
          layers.setSelectedComponent(erddapViewer);
          acceptButton.setVisible(!enable);
        }
      }
    };

    JRadioButton rbCatUrl = new JRadioButton("Catalog URL:");
    rbCatUrl.setActionCommand("cat");
    rbCatUrl.addActionListener(radioListener);
    topPanel.add(rbCatUrl);
    rbCatUrl.addMouseListener(new MouseAdapter() {

      @Override
      public void mouseClicked(MouseEvent e) {
        if (e.getButton() == MouseEvent.BUTTON3) {// right-click
          if (catListBox.isEnabled()) {
            ListEditorDialog dia = new ListEditorDialog(parentFrame, "Remove Catalog Items",
                    "ComboBoxList");
            dia.setVisible(true);

            if (saveDialogChanges) {
              java.util.List<String> remItems = dia.getItemsToRemove();
              for (String s : remItems) {
                for (int i = 0; i < catListBox.getItemCount(); i++) {
                  if (s.equals(catListBox.getItemAt(i).toString())) {
                    catListBox.removeItemAt(i);
                    break;
                  }
                }
              }
            }
            dia.dispose();

            // update the prefs
            catListBox.save();
          }
        }
      }
    });
    topPanel.add(catListBox, "growx");
    topPanel.add(btnCatConnect, "wrap");

    JRadioButton rbDaUrl = new JRadioButton("Direct Access URL:");
    rbDaUrl.setActionCommand("da");
    rbDaUrl.addActionListener(radioListener);
    rbDaUrl.addMouseListener(new MouseAdapter() {

      @Override
      public void mouseClicked(MouseEvent e) {
        if (e.getButton() == MouseEvent.BUTTON3) {// right-click
          if (daListBox.isEnabled()) {
            ListEditorDialog dia = new ListEditorDialog(parentFrame, "Remove Direct Access Items",
                    "DAComboBoxList");
            dia.setVisible(true);

            if (saveDialogChanges) {
              java.util.List<String> remItems = dia.getItemsToRemove();
              for (String s : remItems) {
                for (int i = 0; i < daListBox.getItemCount(); i++) {
                  if (s.equals(daListBox.getItemAt(i).toString())) {
                    daListBox.removeItemAt(i);
                    break;
                  }
                }
              }
            }
            dia.dispose();

            // update the prefs
            daListBox.save();
          }
        }
      }
    });
    topPanel.add(rbDaUrl);
    topPanel.add(daListBox, "growx");
    topPanel.add(btnDirAccess, "wrap"); // David Added "wrap"


    JRadioButton rbSOS = new JRadioButton("Sensor Obs Service:");
    rbSOS.setActionCommand("sos");
    rbSOS.addActionListener(radioListener);
    rbSOS.addMouseListener(new MouseAdapter() {

      @Override
      public void mouseClicked(MouseEvent e) {
        if (e.getButton() == MouseEvent.BUTTON3) {// right-click
          if (sosListBox.isEnabled()) {
            ListEditorDialog dia = new ListEditorDialog(parentFrame, "Remove SOS Access Items",
                    "SOSComboBoxList");
            dia.setVisible(true);

            if (saveDialogChanges) {
              java.util.List<String> remItems = dia.getItemsToRemove();
              for (String s : remItems) {
                for (int i = 0; i < sosListBox.getItemCount(); i++) {
                  if (s.equals(sosListBox.getItemAt(i).toString())) {
                    sosListBox.removeItemAt(i);
                    break;
                  }
                }
              }
            }
            dia.dispose();

            // update the prefs
            sosListBox.save();
          }
        }
      }
    });
    topPanel.add(rbSOS);
    topPanel.add(sosListBox, "growx");
    topPanel.add(btnSOS, "wrap");


    JRadioButton rbErddap = new JRadioButton("ERDDAP Server:");
    rbErddap.setActionCommand("erd");
    rbErddap.addActionListener(radioListener);
    rbErddap.addMouseListener(new MouseAdapter() {

      @Override
      public void mouseClicked(MouseEvent e) {
        if (e.getButton() == MouseEvent.BUTTON3) {// right-click
          if (erddapListBox.isEnabled()) {
            ListEditorDialog dia = new ListEditorDialog(parentFrame, "Remove ERDDAP Access Items",
                    "ERDDAPComboBoxList");
            dia.setVisible(true);

            if (saveDialogChanges) {
              java.util.List<String> remItems = dia.getItemsToRemove();
              for (String s : remItems) {
                for (int i = 0; i < erddapListBox.getItemCount(); i++) {
                  if (s.equals(erddapListBox.getItemAt(i).toString())) {
                    erddapListBox.removeItemAt(i);
                    break;
                  }
                }
              }
            }
            dia.dispose();

            // update the prefs
            erddapListBox.save();
          }
        }
      }
    });
    topPanel.add(rbErddap);
    topPanel.add(erddapListBox, "growx");
    topPanel.add(btnERDDAP);

    ButtonGroup rbGroup = new ButtonGroup();
    rbGroup.add(rbCatUrl);
    rbGroup.add(rbDaUrl);
    rbGroup.add(rbSOS);
    rbGroup.add(rbErddap);

    rbCatUrl.doClick();

    // the catalog tree
    catalogTree.addPropertyChangeListener(new java.beans.PropertyChangeListener() {

      public void propertyChange(java.beans.PropertyChangeEvent e) {
        if (debugEvents) {
          System.err.println("CatalogChooser propertyChange name=" + e.getPropertyName() + "=");
        }

        if (e.getPropertyName().equals("Catalog")) {
          String catalogURL = (String) e.getNewValue();
          setCurrentURL(catalogURL);
          if (catListBox != null) {
            catListBox.addItem(catalogURL);
          }
          firePropertyChangeEvent(e);

        } else if (e.getPropertyName().equals("Selection")) {

          InvDatasetImpl ds = (InvDatasetImpl) catalogTree.getSelectedDataset();
          if (ds == null) {
            return;
          }
          showDatasetInfo(ds);

          if (ds instanceof InvCatalogRef) {
            InvCatalogRef ref = (InvCatalogRef) ds;
            String href = ref.getXlinkHref();
            try {
              java.net.URI uri = ref.getParentCatalog().resolveUri(href);
              setCurrentURL(uri.toString());
            } catch (Exception ee) {
            }
          } else if (ds.getParent() == null) { // top
            setCurrentURL(catalogTree.getCatalogURL());
          }

        } else { // Dataset or File
          firePropertyChangeEvent((InvDataset) e.getNewValue(), e.getPropertyName());
        }

      }
    });

    // htmlViewer Viewer
    // listen for selection
    catalogHtmlViewer.addPropertyChangeListener(new java.beans.PropertyChangeListener() {

      public void propertyChange(java.beans.PropertyChangeEvent e) {
        if (e.getPropertyName().equals("datasetURL")) {
          String datasetURL = (String) e.getNewValue();
          if (debugEvents) {
            System.err.println("***datasetURL= " + datasetURL);
          }
          InvDataset dataset = catalogTree.getSelectedDataset();

          InvAccess access = dataset.findAccess(datasetURL);
          firePropertyChangeEvent(new PropertyChangeEvent(this, "InvAccess", null, access));

        } else if (e.getPropertyName().equals("catrefURL")) {
          String urlString = (String) e.getNewValue();
          if (debugEvents) {
            System.err.println("***catrefURL= " + urlString);
          }
          catalogTree.setCatalog(urlString.trim());
        }
      }
    });

    // status label
    JPanel statusPanel = new JPanel(new BorderLayout());
    statusLabel = new JLabel("not connected");
    sourceText = new JLabel();
    statusPanel.add(statusLabel, BorderLayout.WEST);
    statusPanel.add(sourceText, BorderLayout.EAST);

    ActionListener al2 = new ActionListener() {

      public void actionPerformed(ActionEvent evt) {
        eventType = "Dataset";
        try {
          // System.err.println("ASACatChoose: Before acceptSelected(): "
          // + System.currentTimeMillis());
          catalogTree.acceptSelected();
          // System.err.println("ASACatChoose: After acceptSelected(): "
          // + System.currentTimeMillis());
        } catch (Throwable t) {
          t.printStackTrace();
          // JOptionPane.showMessageDialog(ASACatalogChooser.this,
          // "ERROR "+t.getMessage());
        } finally {
          eventType = null;
        }
      }
    };
    
    ActionListener cursorAl = BusyCursorActions.createListener(odapInterface.getMainFrame(), al2);
    acceptButton.addActionListener(cursorAl);

    // put it all together
    setLayout(new BorderLayout());
    add(topPanel, BorderLayout.NORTH);
    add(layers, BorderLayout.CENTER);

    // Subset Button
    JPanel botPanel = new JPanel(new BorderLayout());
    buttPanel = new JPanel();
    buttPanel.add(acceptButton, null);
    botPanel.add(buttPanel, BorderLayout.NORTH);
    botPanel.add(statusPanel, BorderLayout.SOUTH);
    add(botPanel, BorderLayout.SOUTH);
  }

  /**
   * Set a dataset filter to be used on all catalogs. To turn off, set to
   * null.
   *
   * @param filter
   *            DatasetFilter or null
   */
  public void setDatasetFilter(DatasetFilter filter) {
    catalogTree.setDatasetFilter(filter);
  }

  /**
   * Save persistent state.
   */
  public void save() {
    if (catListBox != null) {
      catListBox.save();
    }
    if (daListBox != null) {
      daListBox.save();
    }
    if (sosListBox != null) {
      sosListBox.save();
    }
    if (erddapListBox != null) {
      erddapListBox.save();
    }
    if (prefs != null) {
      prefs.putInt(HDIVIDER, splitCatalog.getDividerLocation());
    }
  }

  /**
   * Add a PropertyChangeEvent Listener. Throws a PropertyChangeEvent:
   * <ul>
   * <li>propertyName = "Catalog", getNewValue() = catalog URL string
   * <li>propertyName = "Dataset" or "File", getNewValue() = InvDataset
   * chosen.
   * <li>propertyName = "InvAccess" getNewValue() = InvAccess chosen.
   * <li>propertyName = "catrefURL", getNewValue() = catref URL was chosen.
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

  private void firePropertyChangeEvent(InvDataset ds, String oldPropertyName) {
    String propertyName = (eventType != null) ? eventType : oldPropertyName;
    PropertyChangeEvent event = new PropertyChangeEvent(this, propertyName, null, ds);
    firePropertyChangeEvent(event);
  }

  private void firePropertyChangeEvent(PropertyChangeEvent event) {
    // System.err.println("catchoose firePropertyChangeEvent "+event);

    // Process the listeners last to first
    Object[] listeners = listenerList.getListenerList();
    for (int i = listeners.length - 2; i >= 0; i -= 2) {
      if (listeners[i] == PropertyChangeListener.class) {
        ((PropertyChangeListener) listeners[i + 1]).propertyChange(event);
      }
    }
  }

  /**
   * Add this button to the button panel.
   *
   * @param b
   *            button to add
   */
  public void addButton(JButton b) {
    buttPanel.add(b, null);
    buttPanel.revalidate();
  }

  // public void useDQCpopup( boolean use) { tree.useDQCpopup( use); }
  /**
   * Whether to throw events only if dataset has an Access.
   *
   * @param accessOnly
   *            if true, throw events only if dataset has an Access
   */
  public void setAccessOnly(boolean accessOnly) {
    catalogTree.setAccessOnly(accessOnly);
  }

  /**
   * Whether to throw events if catref URL was chosen catref URL was chosen in
   * HtmlViewer (default false).
   */
  public void setCatrefEvents(boolean catrefEvents) {
    this.catrefEvents = catrefEvents;
  }

  /**
   * Whether to throw events if dataset URL was chosen in HtmlViewer (default
   * true).
   */
  public void setDatasetEvents(boolean datasetEvents) {
    this.datasetEvents = datasetEvents;
  }

  /**
   * Set the factory to create catalogs. If you do not set this, it will use
   * the default factory.
   *
   * @param factory
   *            : read XML with this factory
   *
   *            public void setCatalogFactory(InvCatalogFactory factory) {
   *            tree.setCatalogFactory(factory); }
   */
  /**
   * Use this to set the string value in the combo box
   *
   * @param item
   */
  public void setSelectedItem(String item) {
    if (catListBox != null) {
      catListBox.setSelectedItem(item);
    }
  }

  ;

  /**
   * Set the currently selected InvDataset.
   *
   * @param ds
   *            select this InvDataset, must be already in the tree.
   */
  public void setSelectedDataset(InvDatasetImpl ds) {
    catalogTree.setSelectedDataset(ds);
    showDatasetInfo(ds);
  }

  /**
   * Get the current catalog being shown.
   *
   * @return current catalog, or null.
   */
  public InvCatalog getCurrentCatalog() {
    return catalogTree.getCatalog();
  }

  /**
   * Get the TreeView component.
   */
  public ASACatalogTreeView getTreeView() {
    return catalogTree;
  }

  /**
   * Get the current URL string. This may be the top catalog, or a catalogRef,
   * depending on what was last selected. Used to implement the " showSource"
   * debugging tool.
   *
   * @return
   */
  public String getCurrentURL() {
    return currentURL;
  }

  private void setCurrentURL(String currentURL) {
    this.currentURL = currentURL;
    sourceText.setText(currentURL);
    statusLabel.setText("Connected...");
  }

  /**
   * Set the current catalog.
   */
  public void setCatalog(InvCatalogImpl catalog) {
    catalogTree.setCatalog(catalog);
  }

  /**
   * Set the current catalog with a string URL. May be of form
   * catalog#datasetId
   */
  public void setCatalog(String catalogURL) {
    catalogTree.setCatalog(catalogURL.trim());
  }

  private void showDatasetInfo(InvDatasetImpl ds) {
    if (ds == null) {
      return;
    }
    StringBuilder sbuff = new StringBuilder();
    sbuff.setLength(20000);
    InvDatasetImpl.writeHtmlDescription(sbuff, ds, true, false, datasetEvents, catrefEvents);
    if (showHTML) {
      System.err.println("HTML=\n" + sbuff);
    }
    catalogHtmlViewer.setContent(ds.getName(), sbuff.toString());
  }
  private boolean saveDialogChanges;

  private class ListEditorDialog extends JDialog {

    private CheckBoxList cbList = null;

    public ListEditorDialog(JFrame parent, String title, String key) {
      super(parent instanceof JFrame ? (JFrame) parent : null, title, true);
      setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);

      saveDialogChanges = false;

      cbList = new CheckBoxList();
      ArrayList items = null;
      if (prefs != null) {// get the items
        items = (ArrayList) prefs.getList(key, null);
      }
      for (Object o : items) {// add the items to the check list
        cbList.addCheckBox("", String.valueOf(o), false);
      }

      ActionListener cancelListener = new ActionListener() {

        public void actionPerformed(ActionEvent actionEvent) {
          saveDialogChanges = false;

          setVisible(false);
        }
      };
      ActionListener saveListener = new ActionListener() {

        public void actionPerformed(ActionEvent actionEvent) {
          saveDialogChanges = true;

          setVisible(false);
        }
      };

      // add the check list to the dialog
      setLayout(new MigLayout());
      add(new JLabel("Check the items you wish to remove:"), "center, wrap");
      add(cbList, "center");

      JPanel btnPanel = new JPanel(new MigLayout("center"));
      JButton btnOk = new JButton("OK");
      btnOk.addActionListener(saveListener);
      JButton btnCancel = new JButton("Cancel");
      btnCancel.addActionListener(cancelListener);

      btnPanel.add(btnOk);
      btnPanel.add(btnCancel);

      add(btnPanel, "south");
      pack();

      // register "Enter" and "Escape" keys
      KeyStroke cancelStroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
      KeyStroke saveStroke = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0);
      getRootPane().registerKeyboardAction(saveListener, saveStroke, JComponent.WHEN_IN_FOCUSED_WINDOW);
      getRootPane().registerKeyboardAction(cancelListener, cancelStroke, JComponent.WHEN_IN_FOCUSED_WINDOW);

      setLocationRelativeTo(parent);
    }

    public java.util.List<String> getItemsToRemove() {
      return cbList.getSelectedItems();
    }
  }

  /**
   * Wrap this in a JDialog component.
   *
   * @param parent
   *            JFrame (application) or JApplet (applet) or null
   * @param title
   *            dialog window title
   * @param modal
   *            is modal
   */
  public JDialog makeDialog(RootPaneContainer parent, String title, boolean modal) {
    return new Dialog(parent, title, modal);
  }

  private class Dialog extends JDialog {

    private Dialog(RootPaneContainer parent, String title, boolean modal) {
      super(parent instanceof Frame ? (Frame) parent : null, title, modal);

      // L&F may change
      UIManager.addPropertyChangeListener(new PropertyChangeListener() {

        public void propertyChange(PropertyChangeEvent e) {
          if (e.getPropertyName().equals("lookAndFeel")) {
            SwingUtilities.updateComponentTreeUI(ASACatalogChooser.Dialog.this);
          }
        }
      });

      // add a dismiss button
      JButton dismissButton = new JButton("Dismiss");
      buttPanel.add(dismissButton, null);

      dismissButton.addActionListener(new ActionListener() {

        public void actionPerformed(ActionEvent evt) {
          setVisible(false);
        }
      });

      // add it to contentPane
      Container cp = getContentPane();
      cp.setLayout(new BorderLayout());
      cp.add(ASACatalogChooser.this, BorderLayout.CENTER);
      pack();
    }
  }

}