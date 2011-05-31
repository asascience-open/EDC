/*
 * OpendapInterface.java
 *
 * Created on September 2, 2007, 8:02 PM
 *
 * Applied Science Associates, Inc.
 * Copyright 2007.  All rights reserved.
 */
/**
 * http://motherlode.ucar.edu:8080/thredds/idv/rt-models.1.0.xml
 * http://www.unidata.ucar.edu/georesources/idvcatalog.xml
 *
 */
package com.asascience.edc.gui;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.ScrollPaneConstants;

import net.miginfocom.swing.MigLayout;

import org.apache.commons.httpclient.auth.CredentialsProvider;

import ucar.nc2.ui.widget.FileManager;
import ucar.nc2.ui.widget.UrlAuthenticatorDialog;
import ucar.nc2.util.net.URLStreamHandlerFactory;
import ucar.nc2.util.net.HttpClientManager;
import ucar.nc2.constants.FeatureType;
import ucar.nc2.dataset.NetcdfDataset;
import ucar.nc2.thredds.ThreddsDataFactory;
import ucar.util.prefs.PreferencesExt;
import ucar.util.prefs.XMLStore;

import com.asascience.edc.Configuration;
import com.asascience.edc.History;
import com.asascience.edc.erddap.gui.ErddapDatasetViewer;
import com.asascience.edc.nc.NetcdfConstraints;
import com.asascience.edc.nc.io.NcProperties;
import com.asascience.edc.particle.ParticleOutputLayer;
import com.asascience.edc.particle.ParticleOutputReader;
import com.asascience.edc.ui.ASAThreddsDatasetChooser;
import com.asascience.openmap.layer.TimeLayer;
import com.asascience.openmap.layer.VectorLayer;
import com.asascience.openmap.layer.nc.grid.GenericGridLayer;
import com.asascience.openmap.mousemode.VectorInterrogationMouseMode;
import com.asascience.openmap.ui.OMLayerPanel;
import com.asascience.openmap.ui.OMTimeSlider;
import com.asascience.openmap.utilities.MapUtils;
import com.asascience.openmap.utilities.listener.VectorInterrogationPropertyListener;
import com.asascience.edc.sos.SosServer;
import com.asascience.edc.sos.ui.SosProcessPanel;
import com.asascience.edc.sos.ui.SosWorldwindProcessPanel;
import com.asascience.ui.ErrorDisplayDialog;
import com.asascience.ui.ImagePanel;
import com.asascience.ui.JCloseableTabbedPane;
import com.asascience.utilities.Utils;
import com.asascience.utilities.exception.InitializationFailedException;
import com.bbn.openmap.Layer;
import javax.swing.SwingWorker;
import ucar.nc2.ft.FeatureDatasetFactoryManager;

/**
 * 
 * @author CBM
 */
public class OpendapInterface {

  private static String FRAME_SIZE = "frameSize";
  JFrame mainFrame = null;
  private ASAThreddsDatasetChooser datasetChooser = null;
  private SubsetProcessPanel spPanel = null;
  private SosWorldwindProcessPanel sosPanel = null;
  private ThreddsDataFactory threddsDataFactory = new ThreddsDataFactory();
  private PreferencesExt prefs = null;
  private XMLStore store;
  private JCloseableTabbedPane tabbedPane = null;
  private NcViewerPanel viewerPanel;
  private FileManager fileChooser;
  private NetcdfConstraints constraints;
  boolean tuiInit = false;
  private String xmlStoreLoc = "";
  private String sysDir;
  private String homeDir;
  private OmPanel omPanel;
  private OMTimeSlider timeHandler;
  private OMLayerPanel omLayerPanel;
  private JSplitPane horizSplit;
  private Color defaultBackground;

  /**
   * Creates a new instance of OpendapInterface
   *
   * @param args
   */
  public OpendapInterface(String[] args) {
    // System.err.println("At Startup:");
    // System.err.println("  Total Memory:" +
    // Utils.Memory.totalMemoryAs(Utils.Memory.MEGABYTE) + " mb");
    // System.err.println("  Free Memory:" +
    // Utils.Memory.freeMemoryAs(Utils.Memory.MEGABYTE) + " mb");

    store = null;
    try {
      sysDir = System.getProperty("user.dir");
      sysDir = Utils.appendSeparator(sysDir);
      File sysFile = new File(sysDir);
      homeDir = sysFile.getParent();
      String loc = "";
      if (args != null & args.length > 0) {
        if (Utils.isNumeric(args[0])) {
          Configuration.setDisplayType(Integer.valueOf(args[0]));
        } else {
          loc = args[0];
        }
      } else {
        loc = Configuration.OUTPUT_LOCATION;
      }
      if (!loc.equals("")) {
        loc = Utils.appendSeparator(loc);
        File f = new File(loc);

        if (f != null && f.exists()) {
          homeDir = f.getAbsolutePath();
        } else {
          if (JOptionPane.showConfirmDialog(null, "The indicated output directory:\n" + "\""
                  + f.getAbsolutePath() + "\ndoes not exist.\n\nDo you wish to create it?\n\n"
                  + "By selecting \"No\" data will be output to the default directory:\n\"" + sysFile.getParent()
                  + "\"", "Create Directory?", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            if (!f.mkdirs()) {
              System.err.println("Directory \"" + f.getAbsolutePath() + "\" could not be created.");
              homeDir = sysFile.getParent();
            } else {
              homeDir = f.getAbsolutePath();
            }
          }
        }
      } else {
        homeDir = sysFile.getParent();
      }
      homeDir = Utils.appendSeparator(homeDir);

      xmlStoreLoc = sysDir + "edcstore.xml";
      store = XMLStore.createFromFile(xmlStoreLoc, null);
      if (store == null) {
        System.err.println("AtCreation: xmlstore is null");
      }
    } catch (IOException ex) {
      ex.printStackTrace();
    }

    prefs = store.getPreferences();
    // FileChooser is shared
    javax.swing.filechooser.FileFilter[] filters = new javax.swing.filechooser.FileFilter[2];
    filters[0] = new FileManager.HDF5ExtFilter();
    filters[1] = new FileManager.NetcdfExtFilter();
    fileChooser = new FileManager(mainFrame, null, filters, (PreferencesExt) prefs.node("FileManager"));

    constraints = new NetcdfConstraints();
    // if(store == null)System.err.println("store null before C&SGui");
    createAndShowGUI();
  }

  public void formWindowClose(String ncPath) {

    if (Configuration.MAKE_POINTER) {
      NcProperties ncprops = new NcProperties();
      ncprops.createPointerXml(ncPath, sysDir);
    }

    closeInterface(ncPath);
  }

  private void createAndShowGUI() {
    if (store == null) {
      System.err.println("store null in C&SGui");
    }

    mainFrame = new JFrame("Environmental Data Connector");
    mainFrame.setLayout(new MigLayout("fill"));
    mainFrame.setIconImage(Utils.getImageResource("ASA.png", OpendapInterface.class));
    Rectangle bounds = (Rectangle) prefs.getBean(FRAME_SIZE, new Rectangle(100, 50, 800, 600));
    mainFrame.setBounds(bounds);
    // mainFrame.setSize(800, 600);
    mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    mainFrame.addWindowListener(new WindowAdapter() {

      @Override
      public void windowActivated(WindowEvent e) {
        // splash.setVisible(false);
        // splash.dispose();
      }

      @Override
      public void windowClosing(WindowEvent e) {
        // this event only triggered if the "close" button is clicked...
        // NOT if cmd+Q is clicked...

        // formWindowClose(xmlStoreLoc);
        formWindowClose("null");
      }
    });

    mainFrame.addComponentListener(new ComponentAdapter() {

      @Override
      public void componentResized(ComponentEvent e) {
        mainFrame.setSize(Math.max(600, mainFrame.getWidth()), Math.max(600, mainFrame.getHeight()));
      }
    });

    tabbedPane = new JCloseableTabbedPane();
    tabbedPane.addMouseListener(new MouseAdapter() {

      @Override
      public void mouseClicked(MouseEvent e) {
        // System.err.println(e.getButton());
        addDatasetToExisting = false;// if the user clicks the tabs...
      }
    });

    // The "Browse" Tab
    datasetChooser = makeDatasetChooser(tabbedPane);

    // The "Data Viewer" Tab
    tabbedPane.addTabNoClose("Data Viewer", makeViewerPanel());

    mainFrame.add(tabbedPane, "span, grow, wrap");
    tabbedPane.setSelectedIndex(0);

    // The Bottom-Right images
    ImagePanel noaaPanel = new ImagePanel(new ImageIcon(Utils.getImageResource("NOAA.png", OpendapInterface.class)).getImage());
    ImagePanel asaPanel = new ImagePanel(
            new ImageIcon(Utils.getImageResource("ASAIMG.png", OpendapInterface.class)).getImage());
    noaaPanel.setLayout(new MigLayout("insets 0"));
    asaPanel.setLayout(new MigLayout("insets 0"));

    mainFrame.add(new JPanel(), "growx");
    mainFrame.add(noaaPanel, "split 2");
    mainFrame.add(asaPanel);

    mainFrame.setVisible(true);

    // set Authentication for accessing passsword protected services like TDS
    UrlAuthenticatorDialog authenticator = new UrlAuthenticatorDialog(mainFrame);
    java.net.Authenticator.setDefault(authenticator);

    // use HTTPClient
    CredentialsProvider provider = authenticator;
    HttpClientManager.init(provider, "OpendapConnector");
    ucar.nc2.dods.DODSNetcdfFile.setAllowSessions(false);

    // load protocol for ADDE URLs
    URLStreamHandlerFactory.install();
    URLStreamHandlerFactory.register("adde", new edu.wisc.ssec.mcidas.adde.AddeURLStreamHandler());

  }

  public HashMap<String, ParticleOutputLayer> getParticleLayers() {
    HashMap<String, ParticleOutputLayer> ret = new HashMap<String, ParticleOutputLayer>();
    Layer[] layers = omPanel.getLayerHandler().getLayers();
    for (Layer l : layers) {
      if (l instanceof ParticleOutputLayer) {
        ret.put(l.getName(), (ParticleOutputLayer) l);
      }
    }
    return ret;
  }

  public HashMap<String, String> getDataLayers() {
    HashMap<String, String> ret = new HashMap<String, String>();
    Layer[] layers = omPanel.getLayerHandler().getLayers();
    for (Layer l : layers) {
      if (l instanceof GenericGridLayer) {
        ret.put(l.getName(), ((GenericGridLayer) l).getSourceFilePath());
      }
    }
    return ret;
  }

  public void loadDatasetInViewer(File[] infiles) {
    for (File f : infiles) {
      loadDatasetInViewer(f);
    }
  }

  public void loadDatasetInViewer(File infile) {
    loadDatasetInViewer(infile.getAbsolutePath());
  }

  public void loadDatasetInViewer(String ncPath) {
    /**
     * FIXME This is where you would load the dataset using the
     * GenericNetcdfLayer
     */
    /** Check to see if it's a particle layer. */
    if (ncPath.contains(ParticleOutputReader.OUTPUT_NAME.replace(".nc", ""))) {
      try {
        ParticleOutputLayer pol = new ParticleOutputLayer(new File(ncPath), this.mainFrame);
        omPanel.getLayerHandler().addLayer(pol);
        if (pol.getTimes().length > 1) {
          timeHandler.addLayer(pol);
        }
      } catch (FileNotFoundException e) {
        // TODO Auto-generated catch block
        ErrorDisplayDialog.showErrorDialog("Error Loading Dataset", e);
      } catch (InitializationFailedException e) {
        // TODO Auto-generated catch block
        ErrorDisplayDialog.showErrorDialog("Error Loading Dataset", e);
      }
    } else {
      VectorLayer l = MapUtils.obtainVectorLayer(ncPath);
      if (l != null) {
        omPanel.getLayerHandler().addLayer(l);
        if (l instanceof TimeLayer) {
          if (((TimeLayer) l).getTimes().length > 1) {
            timeHandler.addLayer(l);
          }
        }
      }
    }
    /** Select the Data Viewer Tab. */
    tabbedPane.setSelectedIndex(1);
  }

  private JPanel makeViewerPanel() {
    try {

      JPanel pnl = new JPanel(new MigLayout("insets 5, fill"));
      horizSplit = new JSplitPane();
      horizSplit.setOrientation(JSplitPane.HORIZONTAL_SPLIT);
      horizSplit.setOneTouchExpandable(true);
      omPanel = new OmPanel(Utils.appendSeparator(sysDir) + "data");
      omPanel.getVimm().addPropertyChangeListener(VectorInterrogationMouseMode.VECTOR_INTERROGATION,
              new VectorInterrogationPropertyListener(mainFrame, omPanel.getLayerHandler()));
      timeHandler = new OMTimeSlider();

      omLayerPanel = new OMLayerPanel(omPanel, timeHandler);
      defaultBackground = omLayerPanel.getBackground();
      omLayerPanel.addComponentListener(new ComponentAdapter() {

        @Override
        public void componentResized(ComponentEvent e) {
          Dimension d = e.getComponent().getSize();
          int ns = (int) d.getWidth() + 28;
          if (horizSplit.getDividerLocation() > 0 && horizSplit.getDividerLocation() < ns) {
            horizSplit.setDividerLocation(ns);
          }
        }
      });

      DropTarget dt = new DropTarget();
      dt.setDefaultActions(DnDConstants.ACTION_LINK);
      dt.setComponent(omLayerPanel);
      dt.setFlavorMap(null);
      dt.addDropTargetListener(new DropTargetListener() {

        public void dragEnter(DropTargetDragEvent e) {
          if (e.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
            omLayerPanel.setBorder(BorderFactory.createLineBorder(Color.GREEN, 3));
            Utils.setComponetCursor(mainFrame.getRootPane(), Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
          } else {
            System.err.println("Not fileListFlavor");
            for (DataFlavor d : e.getCurrentDataFlavors()) {
              // System.out.println(d.toString());
              System.err.println(d.toString());
            }
          }
        }

        public void dragOver(DropTargetDragEvent e) {
          // System.err.println("dragOver");
          // System.out.println("dragOver");
        }

        public void dropActionChanged(DropTargetDragEvent e) {
          // System.err.println("dropActionChanged");
          // System.out.println("dropActionChanged");
        }

        public void dragExit(DropTargetEvent e) {
          // System.err.println("dragExit");
          // System.out.println("dragExit");
          omLayerPanel.setBorder(BorderFactory.createLineBorder(defaultBackground, 3));
          Utils.setComponetCursor(mainFrame.getRootPane(), Cursor.getDefaultCursor());
        }

        public void drop(DropTargetDropEvent e) {
          try {
            // System.err.println("drop");
            // System.out.println("drop");
            if (e.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
              e.acceptDrop(DnDConstants.ACTION_LINK);
            } else {
              e.rejectDrop();
              return;
            }

            Transferable transObj = e.getTransferable();
            List transData = null;
            try {
              transData = (List) transObj.getTransferData(DataFlavor.javaFileListFlavor);
            } catch (UnsupportedFlavorException ex) {
              ex.printStackTrace();
              // Logger.getLogger(OmTesterGui.class.getName()).
              // log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
              ex.printStackTrace();
              // Logger.getLogger(OmTesterGui.class.getName()).
              // log(Level.SEVERE, null, ex);
            }

            List<File> files = new ArrayList<File>();
            for (Iterator i = transData.iterator(); i.hasNext();) {

              File file = (File) i.next();
              System.err.println("File Path = " + file.getAbsolutePath());
              // System.out.println(files.getAbsolutePath());
              if (file.isDirectory()) {
                for (File f : file.listFiles()) {// new
                  // CurrentsFileFilter())){
                  files.add(f);
                  // loadLayer(f.getAbsolutePath());
                }
              } else {
                files.add(file);
                // loadLayer(files.getAbsolutePath());
              }
            }

            loadDatasetInViewer(files.toArray(new File[0]));

          } finally {
            omLayerPanel.setBorder(BorderFactory.createLineBorder(defaultBackground, 10));
          }
        }
      });

      JScrollPane jsp = new JScrollPane();
      jsp.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
      jsp.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
      jsp.setBorder(BorderFactory.createTitledBorder("Layers"));
      jsp.getVerticalScrollBar().setUnitIncrement(5);
      jsp.setViewportView(omLayerPanel);

      horizSplit.setTopComponent(jsp);
      horizSplit.setBottomComponent(omPanel);

      // pnl.add(makeAnalysisButton(), "wrap");

      pnl.add(horizSplit, "grow, wrap");
      pnl.add(timeHandler, "span, grow");
      return pnl;
    } catch (Exception ex) {
      ex.printStackTrace();
    }
    return new JPanel();
  }

  private ASAThreddsDatasetChooser makeDatasetChooser(JCloseableTabbedPane tabbedPane) {
    datasetChooser = new ASAThreddsDatasetChooser((PreferencesExt) prefs.node("ThreddsDatasetChooser"), tabbedPane,
            this);
    datasetChooser.addPropertyChangeListener(new java.beans.PropertyChangeListener() {

      public void propertyChange(java.beans.PropertyChangeEvent e) {
        // System.err.println(e.getPropertyName());
        if (e.getPropertyName().equals("InvAccess")) {
          thredds.catalog.InvAccess access = (thredds.catalog.InvAccess) e.getNewValue();
          setThreddsDatatype(access);
        }

        if (e.getPropertyName().equals("Dataset") || e.getPropertyName().equals("File")) {
          thredds.catalog.InvDataset ds = (thredds.catalog.InvDataset) e.getNewValue();
          setThreddsDatatype(ds, e.getPropertyName().equals("File"));
        }
      }
    });
    return datasetChooser;
  }
  private boolean addDatasetToExisting = false;
  private SubsetProcessPanel addToSpp = null;

  public void addDataset(SubsetProcessPanel spp) {
    if (spp != null) {
      addToSpp = spp;
      addDatasetToExisting = true;
      tabbedPane.setSelectedIndex(0);
    }
  }

  public boolean openSOSDataset(SosServer sosData, SwingWorker task) {
    try {
      if (!task.isCancelled()) {
        sosPanel = new SosWorldwindProcessPanel((PreferencesExt) prefs, fileChooser,
              this, sosData, homeDir, sysDir);
      }
      if (task.isCancelled() || !sosPanel.initData()) {
        return false;
      }

      if (!task.isCancelled()) {
        int i = tabbedPane.indexOfTab("SOS - Subset & Process");
        if (i != -1) {
          tabbedPane.removeTabAt(i);
        }
        tabbedPane.addTabClose("SOS - Subset & Process", sosPanel);
        tabbedPane.setSelectedComponent(sosPanel);
        return true;
      }
    } catch (Exception ex) {
      ex.printStackTrace();
    }
    return false;
  }

  public void openErddapDataset(ErddapDatasetViewer erddapViewer, SwingWorker task) {
    if (!task.isCancelled()) {
      erddapViewer.setDatasets(erddapViewer.getServer().getDatasets());
    }
  }

  public boolean openDataset(NetcdfDataset ncd) {
    try {
      if (ncd != null) {
        if (!addDatasetToExisting) {
          // System.err.println("openDataset");
          // try {
          // ensures a new set of constraints each time a dataset is
          // loaded...
          constraints = new NetcdfConstraints();

          // gridReader = new GridReader(ncd, constraints);
          // if(!gridReader.initData()) return;

          spPanel = new SubsetProcessPanel((PreferencesExt) prefs, fileChooser, this, constraints, ncd,
                  homeDir, sysDir);
          if (!spPanel.initData()) {
            return false;
          }
          tabbedPane.addTabClose("Grid - Subset & Process", spPanel);
          tabbedPane.setSelectedComponent(spPanel);
          return true;
        } else {
          if (addToSpp != null) {
            // method(s) in SPP to "add" the dataset - including
            // checking
            if (addToSpp.addDataset(ncd)) {
              tabbedPane.setSelectedComponent(addToSpp);
              addDatasetToExisting = false;
            }
          }

          return true;
        }
      }
    } catch (Exception ex) {
      ex.printStackTrace();
    }

    return false;
  }

  private void showInViewer(NetcdfDataset ds) {
    viewerPanel.setDataset(ds);
    tabbedPane.setSelectedComponent(viewerPanel);
  }

  /** save all data in the PersistentStore */
  public void storePersistentData() {
    // prefs.putBeanObject(VIEWER_SIZE, getSize());
    // prefs.putBeanObject(SOURCE_WINDOW_SIZE, (Rectangle) getBounds());
    prefs.putBeanObject(FRAME_SIZE, mainFrame.getBounds());

    if (fileChooser != null) {
      fileChooser.save();
    }

    if (datasetChooser != null) {
      datasetChooser.save();
    }

    if (store != null) {
      try {
        store.save();
        // System.err.println("prefs stored");
      } catch (IOException ex) {
        System.err.println("OI:storePersistentData:");
        ex.printStackTrace();
      }
    }
  }

  public void closeInterface(String exitMessage) {
    // System.err.println("closeInterface");
    try {
      prefs.putBeanObject(FRAME_SIZE, mainFrame.getBounds());
      storePersistentData();

      if (store != null) {
        store.save();
      } else {
        System.err.println("AtWindowClose: xmlstore is null - preferences not saved");
      }
    } catch (IOException ex) {
      System.err.println("OI:closeIterface:");
      ex.printStackTrace();
    } finally {
      this.mainFrame.setVisible(false);
      this.mainFrame.dispose();

      if (exitMessage.endsWith("odcstore.xml")) {
        exitMessage = "null";
      }
      System.out.println("exitpath:" + exitMessage);

      System.exit(0);// non-zero status indicates abnormal termination...
    }
  }

  // public boolean extractData(NetcdfConstraints cons, String outpath){
  // // System.err.println("OI: " + cons.getBoundingBox().toString2());
  // return gridReader.extractData2(cons, outpath);
  // }
  // public LatLonRect getNcExtent(){
  // return gridReader.getBounds();
  // }
  // public Date getStartTime(){
  // return gridReader.getStartTime();
  // }
  public JFrame getMainFrame() {
    return this.mainFrame;
  }

  // <editor-fold desc=" Datatype Sets ">
  // jump to the appropriate tab based on datatype of InvDataset
  private void setThreddsDatatype(thredds.catalog.InvDataset invDataset, boolean wantsViewer) {
    if (invDataset == null) {
      return;
    }

    try {
      // just open as a NetcdfDataset
      if (wantsViewer) {
        System.err.println("Wants Viewer");
        showInViewer(threddsDataFactory.openDataset(invDataset, true, null, null));
        return;
      }

      // TODO: takes forever for very large datasets....
      // otherwise do the datatype thing -
      //ThreddsDataFactory.Result threddsData = threddsDataFactory.openFeatureDataset(invDataset, null);
      NetcdfDataset ncdata = threddsDataFactory.openDataset(invDataset, true, null, null);

      if (ncdata == null) {
        JOptionPane.showMessageDialog(null, "Unknown datatype");
        return;
      }
      //setThreddsDatatype(threddsData);
      setThreddsDatatype(ncdata);

    } catch (IOException ioe) {
      JOptionPane.showMessageDialog(null, "Error on setThreddsDataset = " + ioe.getMessage());
    }

  }

  // jump to the appropriate tab based on datatype of InvDataset
  private void setThreddsDatatype(thredds.catalog.InvAccess invAccess) {
    if (invAccess == null) {
      return;
    }

    thredds.catalog.InvService s = invAccess.getService();
    if (s.getServiceType() == thredds.catalog.ServiceType.HTTPServer) {
      System.err.println("Is an HTTPServer");
      return;
    }

    try {
      NetcdfDataset ncdata = threddsDataFactory.openDataset(invAccess, true, null, null);
      setThreddsDatatype(ncdata);
    } catch (IOException ioe) {
      JOptionPane.showMessageDialog(null, "Error on setThreddsDataset = " + ioe.getMessage());
    }

  }

  // What type of dataset are we dealing with?
  private void setThreddsDatatype(String dataset) {

    try {
      NetcdfDataset ncdata = threddsDataFactory.openDataset(dataset, true, null, null);
      setThreddsDatatype(ncdata);
    } catch (IOException ioe) {
      JOptionPane.showMessageDialog(null, "Error on setThreddsDataset = " + ioe.getMessage());
    }

  }

  // What type of dataset are we dealing with?
  private void setThreddsDatatype(NetcdfDataset ncdataset) {

    if (ncdataset == null) {
      System.err.println("Is a Fatal Error");
      JOptionPane.showMessageDialog(null, "Can't open dataset:" + ncdataset.getLocation());
      return;
    }

    FeatureType featureType = FeatureDatasetFactoryManager.findFeatureType(ncdataset);

    if (featureType == FeatureType.GRID) {
      System.err.println("Is a Grid");
      System.err.println("Dataset Name: " + ncdataset.getTitle());
      System.err.println("Dataset Location: " + ncdataset.getLocation());
      openDataset(ncdataset);
    } else if (featureType == FeatureType.IMAGE) {
      System.err.println("Is an Image");
    } else if (featureType == FeatureType.RADIAL) {
      System.err.println("Is a Radial");
    } else if (featureType == FeatureType.POINT) {
      System.err.println("Is a Point");
    } else if (featureType == FeatureType.STATION) {
      System.err.println("Is a Station");
    }
  }

  /**
   * @param args
   *            the command line arguments
   */
  public static void main(String[] args) {
    final String[] pass = args;
    javax.swing.SwingUtilities.invokeLater(new Runnable() {

      public void run() {
        // read and apply the parameters from the configuration file
        Configuration.initialize(System.getProperty("user.dir") + File.separator + "edcconfig.xml");
        History.initialize(System.getProperty("user.dir") + File.separator + "history.txt");
        if (Configuration.OUTPUT_TO_FILE) {
          try {
            // String outloc = "systemoutput.log";
            // if(System.getProperty("os.name").toLowerCase().contains("windows"))
            // outloc = "C:" + File.separator + "systemoutput.log";
            // String sysOutLoc =
            // XMLStore.makeStandardFilename("asascience" +
            // File.separator +
            // "ODC" + File.separator + "System", "odcsysout.log");
            String sysOutLoc = System.getProperty("user.dir") + File.separator + "edcsysout.log";
            // File f = new File(System.getProperty("user.dir") +
            // File.separator + "odcsysout.log");
            File f = new File(sysOutLoc);
            if (f.exists()) {
              if (f.length() >= 10485760l)// delete the log file
              // if it exceeds 10mb
              {
                if (!f.delete()) {
                  System.err.println("OpendapInterface.main: Could not delete file \""
                          + f.getAbsolutePath() + "\"");
                }
              }
            }
            PrintStream ps = new PrintStream(new FileOutputStream(f, true), true);
            // System.setOut(ps);//System.out now provides response
            // to calling C# app
            System.setErr(ps);
            System.err.println("\n\n\n-----Start New Run: " + new Date().toString() + "-----");
          } catch (FileNotFoundException ex) {
            ex.printStackTrace();
          }
        }
        new OpendapInterface(pass);
      }
    });
  }

  public String getHomeDir() {
    return homeDir;
  }
}
