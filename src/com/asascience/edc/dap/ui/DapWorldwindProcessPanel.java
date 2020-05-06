/*
 * SubsetProcessPanel.java
 *
 * Created on September 6, 2007, 12:57 PM
 *
 * Applied Science Associates, Inc.
 * Copyright 2007.  All rights reserved.
 */
package com.asascience.edc.dap.ui;

import com.asascience.edc.dap.ui.variables.GeneralVariableSelectionPanel;
import com.asascience.edc.dap.ui.variables.EsriVariableSelectionPanel;
import com.asascience.edc.dap.ui.variables.OilmapVariableSelectionPanel;
import com.asascience.edc.dap.ui.variables.VariableSelectionPanel;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Path2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.border.EtchedBorder;

import net.miginfocom.swing.MigLayout;
import ucar.ma2.Array;
import ucar.ma2.IndexIterator;
import ucar.nc2.Attribute;
import ucar.nc2.FileWriter;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;
import ucar.nc2.dataset.CoordinateAxis;
import ucar.nc2.dataset.CoordinateAxis1D;
import ucar.nc2.dataset.NetcdfDataset;
import ucar.nc2.dt.GridCoordSystem;
import ucar.nc2.dt.GridDatatype;
import ucar.nc2.dt.grid.GeoGrid;
import ucar.nc2.dt.grid.GridDataset;
import ucar.nc2.units.DateUnit;
import ucar.unidata.geoloc.LatLonPointImpl;
import ucar.unidata.geoloc.LatLonRect;
import ucar.util.prefs.PreferencesExt;

import com.asascience.edc.ArcType;
import com.asascience.edc.Configuration;
import com.asascience.edc.History;
import com.asascience.edc.gui.OpendapInterface;
import com.asascience.edc.gui.jslider.JSlider2Date;
import com.asascience.edc.map.TrackLineVertex;
import com.asascience.edc.map.view.BoundingBoxPanel;
import com.asascience.edc.map.view.SelectionMethodsPanel;
import com.asascience.edc.map.view.WorldwindSelectionMap;
import com.asascience.edc.map.view.SelectionMethodsPanel.ActiveSelectionSource;
import com.asascience.edc.nc.GridReader;
import com.asascience.edc.nc.NcReaderBase;
import com.asascience.edc.nc.NetcdfConstraints;
import com.asascience.edc.nc.io.NcGridObjectProperties;
import com.asascience.edc.nc.io.NcProperties;
import com.asascience.edc.nc.io.NetcdfGridWriter;
import com.asascience.edc.utils.AoiUtils;
import com.asascience.edc.utils.FileSaveUtils;
import com.asascience.openmap.utilities.listener.AOIPropertyChangeListener;
import com.asascience.ui.IndeterminateProgressDialog;
import com.asascience.utilities.FileMonitor;
import com.asascience.utilities.Utils;
import java.text.CharacterIterator;
import java.text.StringCharacterIterator;
import java.util.Random;
import org.apache.log4j.Logger;

/**
 * 
 * @author CBM
 */
public class DapWorldwindProcessPanel extends JPanel {


  private JFrame mainFrame;
  private NetcdfConstraints constraints;
  private WorldwindSelectionMap mapPanel;
  private VariableSelectionPanel selPanel;
  private NetcdfDataset ncd;
  private List<GridDataset> gdsList;
  protected NcReaderBase ncReader;
  private OpendapInterface parent;

  private String homeDir;
  private String sysDir;
  private String ncOutPath;
  private BoundingBoxPanel bboxGui;
  private JButton btnProcess;
  private JButton btnAddDataset;
  private JSlider2Date dateSlider;
  private JLabel lblDateIncrement;
  private JLabel lblNumDatesSelected;
  private JLabel lblNcName;
  private JPanel pageTopPanel;
  public static final String DISABLE_BBOX = "disableBBOX";
  public static final String DISABLE_SLIDER = "disableSlider";
  private static Logger logger = Logger.getLogger(Configuration.class);
  private static Logger guiLogger = Logger.getLogger("com.asascience.log." + DapWorldwindProcessPanel.class.getName());
  private AoiUtils aoiUtils;
  
	public enum ErrdapRequestType{
		BOUNDING_BOX,
		TRACK_LINE,
		POLYGON
	}
	
  /**
   * Creates a new instance of SubsetProcessPanel
   *
   * @param prefs
   * @param fileChooser
   * @param caller
   * @param cons
   * @param ncd
   * @param homeDir
   * @param sysDir
   */
  public DapWorldwindProcessPanel(ucar.util.prefs.PreferencesExt prefs, OpendapInterface caller,
          NetcdfConstraints cons, NetcdfDataset ncd, String homeDir, String sysDir) {
    // try {

    aoiUtils = new AoiUtils(prefs);
    this.mainFrame = caller.getMainFrame();
    this.parent = caller;
    this.constraints = cons;
    this.ncd = ncd;
    this.homeDir = Utils.appendSeparator(homeDir);
    this.sysDir = Utils.appendSeparator(sysDir);


    initComponents();
  }

  
  public WorldwindSelectionMap getMapPanel(){
	  return mapPanel;
  }
  
  
  
  public boolean initData() {
    try {
      gdsList = new ArrayList<GridDataset>();
      // try it as a grid first...
      ncReader = new GridReader(ncd, constraints);
      // if(!gridReader.initialize()){
      // throw new NcGridReadException("Error Reading Grid Dataset");
      // }
      int ret = ncReader.initialize();
      switch (ret) {
        case NcReaderBase.INIT_OK:
          gdsList.add(((GridReader) ncReader).getGridDataset());

          setNcName(ncd.getTitle(), ncd.getLocation());
          // setGridDataset(((GridReader)ncReader).getGridDataset());
          setNcExtent(ncReader.getBounds());
          // setTimeDates(gridReader.getTimes());
          if (ncReader.isHasTime()) {
            // timeDates = ncReader.getTimes();
            dateSlider.setRange(ncReader.getStartTime(), ncReader.getEndTime());
            Date tempDate = new Date();
            tempDate.setTime(ncReader.getEndTime().getTime() - 1000*60*60*24*10);
            dateSlider.setStartDate(tempDate);
            lblDateIncrement.setText(lblDateIncrement.getText() + constraints.getTimeInterval());
            lblNumDatesSelected.setText("# Timesteps Selected: " + Math.round(calcNumTimesteps()));
          } else {
            dateSlider.reset();
            dateSlider.setVisible(false);
            lblDateIncrement.setText(lblDateIncrement.getText() + "NA");
            lblNumDatesSelected.setText("# Timesteps Selected: " + "NA");
          }

          setVariables(((GridReader) ncReader).getGeoGrids());
          // setBandDims();


          if (!((GridReader) ncReader).isRegularSpatial()) {
            if (selPanel.getPanelType() == VariableSelectionPanel.ESRI) {
            	
              selPanel.gridRegularity(((GridReader) ncReader).isRegularSpatial());
            }
          }

          validate();

          return true;
        case NcReaderBase.NO_GRIDDATASET:
          JOptionPane.showMessageDialog(mainFrame,
                  "A GridDataset could not be built from the selected dataset.", "Could not open dataset",
                  JOptionPane.WARNING_MESSAGE);
          break;
        case NcReaderBase.NO_GEOGRIDS:
          JOptionPane.showMessageDialog(mainFrame,
                  "The selected dataset does not contain any gridded fields.", "Could not open dataset",
                  JOptionPane.WARNING_MESSAGE);
          break;
        case NcReaderBase.INVALID_BOUNDS:
          JOptionPane.showMessageDialog(mainFrame, "The selected dataset has invalid bounds.",
                  "Could not open dataset", JOptionPane.WARNING_MESSAGE);
          break;
        case NcReaderBase.UNDEFINED_ERROR:
          JOptionPane.showMessageDialog(mainFrame,
                  "An undefined error was encountered when reading the selected dataset.\n\n"
                  + "See the \"edcsysout.log\" file for further details", "Could not open dataset",
                  JOptionPane.WARNING_MESSAGE);
          break;
      }
      this.pageTopPanel.revalidate();
      this.pageTopPanel.repaint();
    } catch (IOException ex) {
      logger.error("SPP:initData:", ex);
    } catch (Exception ex) {
      logger.error("SPP:initData:", ex);
    }
    return false;
  }

  // These were all long....
  private double calcNumTimesteps() {
    // selected Range in seconds
    if (dateSlider.getEndDate() != null && dateSlider.getStartDate() != null) {
      double rng = (dateSlider.getEndDate().getTime() - dateSlider.getStartDate().getTime()) / 1000;

      // long inter = Long.parseLong(constraints.getTimeInterval());
      double inter = Double.parseDouble(constraints.getTimeInterval());
      if (inter > 0) {
        return (rng / inter) + 1;// add 1 to account for the start/end time
      }
    }
    return 0;
  }

  private void initComponents() {
    try {
      setLayout(new MigLayout("gap 0, fill"));
      setBorder(new EtchedBorder());
      
      pageTopPanel = new JPanel(new MigLayout("gap 0, fill"));

      lblNcName = new JLabel("NetCDF Filename:");
      btnAddDataset = new JButton("Add Dataset");
      btnAddDataset.addActionListener(new ActionListener() {

        public void actionPerformed(ActionEvent e) {
          // use a method in the parent OpendapInterface to add the
          // variables to the
          // current S&P tab
          parent.addDataset(DapWorldwindProcessPanel.this);
        }
      });

      // make a panel to hold the name of the Netcdf file
      JPanel namePanel = new JPanel();
      namePanel.add(lblNcName, "center");
      namePanel.add(btnAddDataset);

      // create the map panel
      String gisDataDir = sysDir + "data";
      mapPanel = new WorldwindSelectionMap(gisDataDir, true);
      mapPanel.addPropertyChangeListener(new PropertyChangeListener() {

        public void propertyChange(PropertyChangeEvent e) {
          String name = e.getPropertyName();
          // Bounding box was drawn
          if (name.equals(SelectionMethodsPanel.BOUNDS_STORED)) {
            if (selPanel != null) {
            	selPanel.setHasGeoSub(true);
            	List<LatLonRect> bbox = mapPanel.getSelectedExtent();
            	if(bbox != null && bbox.size() > 0)
            		bboxGui.setBoundingBox(bbox.get(0));
            }
          }
          else if(name.equals(DISABLE_BBOX)){
        	  bboxGui.setEnabled((Boolean)e.getNewValue());
        	  
          }
          else if(name.equals(DISABLE_SLIDER)){
        	  Boolean enabled = (Boolean) e.getNewValue();
        	  dateSlider.setEnabled(enabled);
        	  dateSlider.getSlider_().setVisible(enabled);
        	  if(enabled == false){
        		  dateSlider.getDisableReasonLabel().setText("Temporal Contraints Set Via Track Import");
        	  }
        	  dateSlider.getDisableReasonLabel().setVisible(!enabled);
        	  lblDateIncrement.setEnabled(enabled);
        	  lblNumDatesSelected.setEnabled(enabled);
        	  lblNcName.setEnabled(enabled);
          }
        }
      });

      pageTopPanel.add(mapPanel, "gap 0, grow");

      selPanel = new GeneralVariableSelectionPanel(constraints, this);
      switch (Configuration.DISPLAY_TYPE) {
        case Configuration.DisplayType.GENERAL:
          break;
        case Configuration.DisplayType.ESRI:
          selPanel = new EsriVariableSelectionPanel(constraints, this);
          break;
        case Configuration.DisplayType.OILMAP:
          selPanel = new OilmapVariableSelectionPanel(constraints, this);
          break;
      }

      selPanel.addPropertyChangeListener(new PropertyChangeListener() {

        public void propertyChange(PropertyChangeEvent e) {
          if (btnProcess != null) {
            String name = e.getPropertyName();
            if (name.equals("processEnabled")) {
              btnProcess.setEnabled(Boolean.valueOf(e.getNewValue().toString()));
            }
        
          }
        }
      });
      pageTopPanel.add(selPanel, "gap 0, growy, wrap");

      // Bottom panel
      JPanel pageEndPanel = new JPanel(new MigLayout("gap 2, fill"));

      // BBOX panel
      bboxGui = new BoundingBoxPanel();
      bboxGui.addPropertyChangeListener(aoiUtils.getPropertyChangeListener(mapPanel, bboxGui));
      bboxGui.createAoiSubmenu(aoiUtils.getAoiList());
      pageEndPanel.add(bboxGui, "gap 0, growy");

      // TIME panel
      JPanel timePanel = new JPanel(new MigLayout("gap 0, fill"));
      timePanel.setBorder(new EtchedBorder());
      dateSlider = new JSlider2Date();
      dateSlider.setAlwaysPost(true);
      dateSlider.setHandleSize(6);// default is 6
      dateSlider.addPropertyChangeListener(new PropertyChangeListener() {

        public void propertyChange(PropertyChangeEvent evt) {
          if (ncReader.isHasTime()) {
            lblNumDatesSelected.setText("# Timesteps Selected: " + Math.round(calcNumTimesteps()));
          }
        }
      });
      lblDateIncrement = new JLabel("Time Interval (sec): ");
      lblNumDatesSelected = new JLabel("# Timesteps Selected: ");

      timePanel.add(lblDateIncrement, "gap 0, gapright 20, center, split 2");
      timePanel.add(lblNumDatesSelected, "gap 0, gapleft 20, center, wrap");
      timePanel.add(dateSlider, "gap 0, grow, center");

      pageEndPanel.add(timePanel, "gap 0, grow");

      JPanel processPanel = new JPanel();
      btnProcess = new JButton("Process");
      btnProcess.setToolTipText("Apply the specified spatial & temporal constraints\n"
              + "and export the selected variables to the desired output format.");
      btnProcess.setEnabled(false);
      btnProcess.addActionListener(new ProcessDataListener());
      processPanel.add(btnProcess);

      add(namePanel, "spanx 3, growx, wrap");
      add(pageTopPanel, "spanx 3, grow, wrap");
      add(pageEndPanel, "spanx 3, growx, wrap, hmax 200");
      add(processPanel, "spanx 3, growx");

    } catch (Exception ex) {
      logger.error("Exception", ex);
    }
  }

  /*
   *  Removes all special characters and spaces from a layer name
   */
  private String createSuitableLayerName(String name) {

    // Generate a random name if it is null or blank
    if ((name == null) || (name.isEmpty())) {
      Random generator = new Random();
      Integer rand = (Integer) generator.nextInt(10);
      return "Output_" + rand.toString();
    }

    final StringBuilder result = new StringBuilder();
    final StringCharacterIterator iterator = new StringCharacterIterator(name);
    char character = iterator.current();
    while (character != CharacterIterator.DONE) {
      if (character == '.') {
        result.append("_");
      } else if (character == ',') {
        result.append("_");
      } else if (character == '\\') {
        result.append("");
      } else if (character == '?') {
        result.append("");
      } else if (character == '*') {
        result.append("");
      } else if (character == '+') {
        result.append("");
      } else if (character == '&') {
        result.append("");
      } else if (character == ':') {
        result.append("");
      } else if (character == '{') {
        result.append("");
      } else if (character == '}') {
        result.append("");
      } else if (character == '[') {
        result.append("");
      } else if (character == ']') {
        result.append("");
      } else if (character == '(') {
        result.append("");
      } else if (character == ')') {
        result.append("");
      } else if (character == '^') {
        result.append("");
      } else if (character == '$') {
        result.append("");
      } else if (character == ' ') {
        result.append("");
      } else {
        //the char is not a special one
        //add it to the result as is
        result.append(character);
      }
      character = iterator.next();
    }
    return result.toString();
  }

  private boolean isArcNameGood(String name) {
    if (name.contains(" ")) {
      return false;
    }
    if (name.contains("@")) {
      return false;
    }
    if (name.contains("#")) {
      return false;
    }
    if (name.contains("$")) {
      return false;
    }
    if (name.contains("^")) {
      return false;
    }
    if (name.contains("&")) {
      return false;
    }
    if (name.contains("*")) {
      return false;
    }
    if (name.contains("!")) {
      return false;
    }
    if (name.length() > 10) {
      return false;
    }

    return true;
  }

  public boolean addDataset(NetcdfDataset ncds) {
    // boolean ret = false;
    try {
      // ret = canMerge(ncds, ncd);
      String s = canMerge2(ncds, ncd);
      if (!s.equals("equal")) {
        // if(!ret){
        JOptionPane.showMessageDialog(mainFrame, "The selected dataset is not"
                + " compatible with the first dataset.\n\n" + s + "\n\nPlease try another dataset.",
                "Incompatible Datasets", JOptionPane.WARNING_MESSAGE);
        // btnAddDataset.setEnabled(true);
        return false;
      } else {
        GridReader gr = new GridReader(ncds, constraints);
        int ret = gr.initialize();
        switch (ret) {
          case NcReaderBase.INIT_OK:
            gdsList.add(gr.getGridDataset());
            addVariables(gr.getGeoGrids());
            return true;
          default:
            return false;
        }
      }

    } catch (Exception ex) {
      logger.error("Exception", ex);
    }

    return false;
  }

  public String canMerge2(NetcdfDataset ncds1, NetcdfDataset ncds2) {
    StringBuilder ret = new StringBuilder();
    try {
      List coords1 = ncds1.getCoordinateAxes();
      List coords2 = ncds2.getCoordinateAxes();
      CoordinateAxis c1;
      CoordinateAxis c2;
      if (coords1.size() == coords2.size()) {
        for (int i = 0; i < coords1.size(); i++) {
          c1 = (CoordinateAxis) coords1.get(i);
          c2 = (CoordinateAxis) coords2.get(i);
          if (c1.getAxisType() == c2.getAxisType()) {
            if (c1.getMinValue() == c2.getMinValue()) {
              if (c1.getMaxValue() == c2.getMaxValue()) {
                if (c1.getDataType() == c2.getDataType()) {
                  continue;
                } else {
                  ret.append("Unequal coordinate axis data type: name=").append(c1.getName()).append(": c1=")
                     .append(c1.getDataType()).append(" c2=").append(c2.getDataType());
                  break;
                }
              } else {
                ret.append("Unequal coordinate axis maximum value: name=").append(c1.getName()).append(": c1=")
                   .append(c1.getMaxValue()).append(" c2=").append(c2.getMaxValue());
                break;
              }
            } else {
              ret.append("Unequal coordinate axis minimum value: name=").append(c1.getName()).append(": c1=")
                 .append(c1.getMinValue()).append(" c2=").append(c2.getMinValue());
              break;
            }
          } else {
            ret.append("Unequal coordinate axis type: name=").append(c1.getName()).append(": c1=")
               .append(c1.getAxisType()).append(" c2=").append(c2.getAxisType());
            break;
          }
        }
        // if the StringBuilder has no characters, all checks were
        // successfull
        if (ret.length() == 0) {
          ret.append("equal");
        }
      } else {
        ret.append("Unequal number of coordinate axes: c1=").append(coords1.size()).append(" c2=").append(coords2.size());
      }
    } catch (Exception ex) {
      ret.delete(0, ret.length() - 1);
      ret.append("Error: There was an error checking the dataset for compatibility.\n\n"
              + "See \"edcsysout.log\" for more details.");
      logger.warn("Datasets not compatible", ex);
    }
    return ret.toString();
  }

  public boolean canMerge(NetcdfDataset ncds1, NetcdfDataset ncds2) {
    // StringBuilder ret = new StringBuilder();
    try {
      List coords1 = ncds1.getCoordinateAxes();
      List coords2 = ncds2.getCoordinateAxes();
      if (coords1.size() == coords2.size()) {
        CoordinateAxis c1;
        CoordinateAxis c2;
        for (int i = 0; i < coords1.size(); i++) {
          c1 = (CoordinateAxis) coords1.get(i);
          c2 = (CoordinateAxis) coords2.get(i);
          if (c1.getAxisType() != c2.getAxisType()) {
            logger.warn("Unequal coordinate axis type: name=" + c1.getName() + ": c1="
                    + c1.getAxisType() + " c2=" + c2.getAxisType());
            guiLogger.warn("Unequal coordinate axis type: name=" + c1.getName() + ": c1="
                    + c1.getAxisType() + " c2=" + c2.getAxisType());
            return false;
          }
          if (c1.getMinValue() != c2.getMinValue()) {
            logger.warn("Unequal coordinate axis minimum value: name=" + c1.getName() + ": c1="
                    + c1.getMinValue() + " c2=" + c2.getMinValue());
            guiLogger.warn("Unequal coordinate axis minimum value: name=" + c1.getName() + ": c1="
                    + c1.getMinValue() + " c2=" + c2.getMinValue());
            return false;
          }
          if (c1.getMaxValue() != c2.getMaxValue()) {
            logger.warn("Unequal coordinate axis maximum value: name=" + c1.getName() + ": c1="
                    + c1.getMaxValue() + " c2=" + c2.getMaxValue());
            guiLogger.warn("Unequal coordinate axis maximum value: name=" + c1.getName() + ": c1="
                    + c1.getMaxValue() + " c2=" + c2.getMaxValue());
            return false;
          }
          if (c1.getDataType() != c2.getDataType()) {
            logger.warn("Unequal coordinate axis dataType: name=" + c1.getName() + ": c1="
                    + c1.getDataType() + " c2=" + c2.getDataType());
            guiLogger.warn("Unequal coordinate axis dataType: name=" + c1.getName() + ": c1="
                    + c1.getDataType() + " c2=" + c2.getDataType());
            return false;
          }
        }
        // ret.append("equal");
        return true;// only triggered if all of the above are equal for
        // all axes
      } else {
        logger.warn("Unequal number of coordinate axes: c1=" + coords1.size() + " c2=" + coords2.size());
        guiLogger.warn("Unequal number of coordinate axes: c1=" + coords1.size() + " c2=" + coords2.size());
      }
    } catch (Exception ex) {
      logger.error("Exception", ex);
    }

    return false;
  }


  public void setNcName(String name, String location) {
    lblNcName.setText("NetCDF Filename:  " + name);
    lblNcName.setToolTipText(location);
  }

  public void setBandDims() {
    // cbBandDim.removeAllItems();
    // if(!constraints.getTimeDim().equals("null"))
    // cbBandDim.addItem(constraints.getTimeDim());
    // if(!constraints.getZDim().equals("null"))
    // cbBandDim.addItem(constraints.getZDim());
  }

  public Variable getVarByName(String name) {
    if (selPanel != null) {
      for (Variable v : selPanel.getLocalVariables()) {
        if (v.getName().equals(name)) {
          return v;
        }
      }
    }

    return null;
  }

  public GeoGrid getGridByName(String name, boolean isDescription) {
    if (selPanel != null) {
      for (GeoGrid g : selPanel.getLocalGeoGrids()) {
        if (!isDescription) {
          if (g.getName().equals(name)) {// orig
            return g;
          }
        } else {
          if (g.getName().equals(selPanel.getVarNameFromDescription(name))) {
            return g;
          }
        }
      }
    }
    return null;
  }

  public void setVariables(List vars) {
    if (selPanel != null) {
      if (vars.get(0) instanceof GeoGrid) {
        selPanel.setGeoGridVars(vars);
      } else if (vars.get(0) instanceof Variable) {
        selPanel.setVariables(vars);
      }
    }
  }

  public void addVariables(List vars) {
    if (selPanel != null) {
      if (vars.get(0) instanceof GeoGrid) {
        selPanel.addGeoGridVars(vars);
      } else if (vars.get(0) instanceof Variable) {
        selPanel.addVariables(vars);
      }
    }
  }

  public void setVariables() {
    if (selPanel != null) {
      selPanel.setGeoGridVars();
    }
  }

  public void setNcExtent(LatLonRect llRect) {
    mapPanel.makeDataExtentLayer(llRect);
  }

  public void setConstraints(NetcdfConstraints cons) {
    constraints = cons;
  }
  int runOK = -1;

  class ProcessPropertyListener implements PropertyChangeListener {

    private String ncPath = "null";

    public void propertyChange(PropertyChangeEvent evt) {
      if (evt.getPropertyName().equals("close")) {
        if (runOK == NetcdfGridWriter.SUCCESSFUL_PROCESS) {
          logger.info("Processing completed successfully: \"" + ncPath + "\"");
          guiLogger.info("Processing completed successfully: \"" + ncPath + "\"");
          if (Configuration.CLOSE_AFTER_PROCESSING) {
            parent.formWindowClose(ncPath.replace(".nc", ".xml"));
          } else {
            parent.loadDatasetInViewer(ncPath);
          }

        } else if (runOK == NetcdfGridWriter.UNDEFINED_ERROR) {
          logger.warn("Undefined error when processing.");
          guiLogger.warn("Undefined error when processing.");
          JOptionPane.showMessageDialog(mainFrame, "There was a problem extracting the data.\n"
                  + "Please try again or refer to the \"edcsysout.log\" for more details.", "Processing Error",
                  JOptionPane.ERROR_MESSAGE);
        } else if (runOK == NetcdfGridWriter.CANCELLED_PROCESS) {
          // TODO: processing cancelled - perform actions to "reset"
          logger.warn("Processing cancelled by user.");
          guiLogger.warn("Processing cancelled by user.");
          File f = new File(ncOutPath);
          if (f.exists()) {
            f.delete();
            f = new File(f.getName().replace(".nc", ".xml"));
            if (f.exists()) {
              f.delete();
            }
          }
        }

        // "suggest" that the garbage collector clean-up
        // System.gc();//BAD: can result in performance "black-hole"
      } else if (evt.getPropertyName().equals("done")) {
        ncPath = (String) evt.getNewValue();
      }
    }
  }

  class ProcessDataListener implements ActionListener {
	  private String userOutname;

	 private File getOutputFile(){
		  File f = null;
		  String outname = "outname";
		  String inname = "outname";
		  boolean cont = false;
		  boolean skip = false;
		
		  if (selPanel.getCblVars().getSelItemsSize() > 0) {
			  inname = selPanel.getCblVars().getSelectedItems().get(0);
		  }
		  if (inname != null) {
			  // alter default name
			  if (Configuration.USE_VARIABLE_NAME_FOR_OUTPUT) {
				  // just the variable name
				  outname = createSuitableLayerName(inname.substring(1, inname.indexOf("]")));
			  } else {
				  // the description with underscores
				  outname = createSuitableLayerName(selPanel.getFullDescriptionFromShortDescription(inname));
			  }
		  }
		  if (Configuration.DISPLAY_TYPE == Configuration.DisplayType.ESRI) {
			  // If longer, trim the name down to the first 8 characters
			  if (outname.length() > 8) {
				  outname = outname.substring(0, 7);
			  }
		  }

		  do {
			  skip = false;

			  if (Configuration.DISPLAY_TYPE == Configuration.DisplayType.ESRI) {// ESRI
				  if (!isArcNameGood(outname)) {
					  JOptionPane.showMessageDialog(null, "The name contains illegal characters "
							  + "or is too long.\nThe maximum allowable size is 10 characters.\n"
							  + "Special characters (i.e. @, #, $, \"space\", etc.) are not allowed.",
							  "Invalid Name", JOptionPane.WARNING_MESSAGE);
					  skip = true;
				  }
			  }

			  File find_file = FileSaveUtils.chooseDirectSavePath(mainFrame, homeDir, createSuitableLayerName(outname.replace(".nc", "")));

			  if (!skip && find_file != null) {
				  userOutname = createSuitableLayerName(find_file.getName());

				  f = new File(find_file.getParentFile().getAbsolutePath() + File.separator + userOutname + ".nc");

				  if (f.exists()) {
					  if (Configuration.ALLOW_FILE_REPLACEMENT) {
						  int i = JOptionPane.showConfirmDialog(mainFrame,
								  "An output file with this name already exists." + "\nDo you wish to replace it?",
								  "Duplicate Name", JOptionPane.YES_NO_OPTION);
						  if (i == JOptionPane.YES_OPTION) {
							  f.delete();
							  cont = true;
						  } else {
							  cont = false;
						  }
					  } else {
						  JOptionPane.showMessageDialog(mainFrame,
								  "An output file with this name already exists."
										  + "\nPlease select a different name to continue.", "Duplicate Name",
										  JOptionPane.OK_OPTION);
						  cont = false;
					  }
				  } else {
					  cont = true;
				  }
			  }
			  else if(find_file == null)
				  return null;
		  } while (!cont);
		  return f;
	 }
	  
	  public void actionPerformed(ActionEvent e) {
		  // Process button is clicked
		  if (selPanel == null) 
			  return;
		  List<LatLonRect> boundsList;
		  List<LatLonPointImpl> polygonVertices = null;
		  List<NetcdfConstraints> constraintsList = new ArrayList<NetcdfConstraints>();
		

	
			  // get the extent set by the user
			  boundsList = mapPanel.getSelectedExtent();
		  
		  ErrdapRequestType requestType = null;
		  ActiveSelectionSource activeSelectionSource = mapPanel.getLocationSelectionTool().getActiveSelectionSource();
		  switch(activeSelectionSource){
		  case BBOX:
			  requestType = ErrdapRequestType.BOUNDING_BOX;
			  break;
		  case POLYGON:
			  requestType = ErrdapRequestType.POLYGON;
			  polygonVertices = mapPanel.getSelectedVertices();
			  
			  break;
		  case TRACK_LINE:
			  requestType = ErrdapRequestType.TRACK_LINE;		 
			  break;
		  }
		  
		  if(requestType == ErrdapRequestType.TRACK_LINE){
			  processTrackLineRequest(constraintsList,
					  					selPanel.getPanelType(), dateSlider.getStartDate(), dateSlider.getEndDate());
		  }
		  else {
			  // set the start & end date/time
			  constraints.setStartTime(null);
			  constraints.setEndTime(null);
			  if (ncReader.isHasTime()) {
				  constraints.setStartTime(dateSlider.getStartDate());
				  constraints.setEndTime(dateSlider.getEndDate());
			  }

			  // set the panel type
			  constraints.setPanelType(selPanel.getPanelType());

			  // if no user extent, use entire extent
			  if (boundsList == null || boundsList.size() == 0) {
				  boundsList.add(ncReader.getBounds());
			  }// bounds = parent.getNcExtent();
			  constraints.setBoundingBox(boundsList.get(0));
		  
			  if((!checkLocationExtents(constraints)) || (!checkTimeExtents(constraints)))
				  return;
			  constraintsList.add(constraints);

		  }
		  File outFile = getOutputFile();
//		  if (outFile != null) {
//			  try {
//				  System.out.println("CREATE " +outFile.getName());
//				  outFile.createNewFile();
//			  } catch (Exception ex) {
//				  ex.printStackTrace();
//			  }
//		  }

		  ncOutPath = outFile.getAbsolutePath();

		  IndeterminateProgressDialog pd = new IndeterminateProgressDialog(mainFrame, 
				  "Progress", new ImageIcon(new ImageIcon
						  (this.getClass().getResource("/resources/images/edc.png")).getImage()));
		  ProcessDataTask pdt = new ProcessDataTask("Processing Data...", ncOutPath, 
				  userOutname, constraintsList, polygonVertices, requestType);
		  pdt.addPropertyChangeListener(new ProcessPropertyListener());
		  pd.setRunTask(pdt);
		  pd.runTask();
	  }
	  
	  
	  
	  public void  processTrackLineRequest(List<NetcdfConstraints> constraintsList,
			  							  int panelType, Date sliderStart, Date sliderEnd){
			 List<TrackLineVertex> trackPts = mapPanel.getLocationSelectionTool().getTrackSel().getTrackConnectorModel();
			 boolean  passedLocationCheck = false;
			 boolean   passedTimeCheck = false;
			 int vertexId = 1;
			 boolean multTrackPts = trackPts.size() > 1;
			 boolean isEsri = selPanel.getPanelType() == VariableSelectionPanel.ESRI ? true : false;
			 for(TrackLineVertex pt : trackPts){
				 NetcdfConstraints cons = new NetcdfConstraints(constraints);
				 cons.setBoundingBox(pt.getBoundingBox());
				 Date startDate = pt.getStartTime();
				 if(startDate == null)
					 startDate = sliderStart;
				 Date endDate = pt.getEndTime();
				 if(endDate == null)
					 endDate = sliderEnd;
				 cons.setStartTime(startDate);
				 cons.setEndTime(endDate);
				 cons.setPanelType(panelType);
				 String bandDim = cons.getBandDim();
				 String trimDim = cons.getTrimByDim();
				 if(bandDim != null && !bandDim.equals("") && !bandDim.equals("null")){
					 if(multTrackPts && !isEsri)
						 cons.setBandDim(bandDim + vertexId);
					 else
						 cons.setBandDim(bandDim);
				 }
				 if(trimDim != null && !trimDim.equals("") && !trimDim.equals("null")) {
					 if(multTrackPts && !isEsri)
						 cons.setTrimByDim(trimDim + vertexId);
					 else
						 cons.setTrimByDim(trimDim);
				 }
				 if(passedLocationCheck || checkLocationExtents(cons)){
					 passedLocationCheck = true;
					 if(passedTimeCheck || checkTimeExtents(cons))
						 constraintsList.add(cons);
					 else 
						 return;
				 }
				 else {
					 constraintsList = null;
					 return;
				 }
				 vertexId++;
			 }
	  }
	  
	  
	  public boolean checkLocationExtents(NetcdfConstraints constraints){
		  double lonSpan = Math.abs(constraints.getWesternExtent()) - Math.abs(constraints.getEasternExtent());
		  double latSpan = constraints.getNorthernExtent() - constraints.getSouthernExtent();
		  // if(!selPanel.isHasGeoSub() | (Math.abs(lonSpan) > 90 |
		  // Math.abs(latSpan) > 90)){
		  if ((Math.abs(lonSpan) > 90 | Math.abs(latSpan) > 90)) {
			  if (JOptionPane.showConfirmDialog(mainFrame, "The geospatial subset is larger than 90 degrees.\n"
					  + "This may result in a very large dataset.\n\nDo you wish to continue?", "Geospatial Subset",
					  JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) == JOptionPane.NO_OPTION) {
				  return false;
			  }
		  }
	
		  return true;
	  }
	  public boolean checkTimeExtents(NetcdfConstraints constraints){
		  // int rng = endSlider.getValue() - startSlider.getValue();
		  if (ncReader.isHasTime()) {
			  double rng = calcNumTimesteps();
			  if (rng >= 100) {
				  if (JOptionPane.showConfirmDialog(mainFrame, "The temporal subset has not been indicated"
						  + " or is larger than 100 timesteps.\n"
						  + "This may result in a very large dataset.\n\nDo you wish to continue?",
						  "Temporal Subset", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) == JOptionPane.NO_OPTION) {
					  return false;
				  }
			  }
		  }
		  return true;
	  }

  }
  public class ProcessDataTask extends com.asascience.utilities.BaseTask {

    private List<String> ncPaths;
    private String ncPath;
    private String outname;
	protected List<NetcdfConstraints> constraintsList;
	protected List<LatLonPointImpl> polygonVertices;
	private ErrdapRequestType requestType;
	

	
	  
	
	public ProcessDataTask(String name, String ncfile, 
			String outname, List<NetcdfConstraints> constraintsList, 
			List<LatLonPointImpl> polygonVertices, ErrdapRequestType type) {
		super();
		this.setName(name);
		this.ncPath = ncfile;
		this.outname = outname;
		this.requestType = type;

		this.constraintsList = constraintsList;
		this.polygonVertices = polygonVertices;

	}

    @Override
    public Void doInBackground() {
    	try {
    		firePropertyChange("progress", null, 0);// starts the dialog
    		Thread.sleep(500);// sleep for a half second to allow the dialog
    		// to display
    		firePropertyChange("note", null, "Initializing...");
    		Thread.sleep(500);
    		NcProperties props = new NcProperties(ncPath, constraintsList.size());
    		ArcType type = ArcType.NULL;
    		String sTime = null;
    		String ncExt = ".nc";
    		boolean isEsri = selPanel.getPanelType() == VariableSelectionPanel.ESRI ? true : false;
    		List<String> vars = null;
    		FileMonitor fm = new FileMonitor(ncPath);
    		Map<Integer, List<Variable>> varVertexMap = new HashMap<Integer, List<Variable>>();
    		Map<Integer, Boolean[][]> includeIndicesMap = new HashMap<Integer, Boolean[][]>();
    		 Map<Integer, String> xDimNameMap = new HashMap<Integer, String>();
    		 Map<Integer, String> yDimNameMap = new HashMap<Integer, String>();
    		 Map<Integer, String> zDimNameMap = new HashMap<Integer, String>();
    		 File ncFilePath = new File(ncPath);
    		 
    		 String baseFilePath = ncFilePath.getParent() + File.separator;
    		 String baseFileName = ncFilePath.getName();
    		 baseFileName = baseFileName.substring(0, baseFileName.length() - ncExt.length());
    				 
    		NetcdfGridWriter writer = new NetcdfGridWriter(this.getPropertyChangeSupport());

    		fm.addPropertyChangeListener(new PropertyChangeListener() {

    			public void propertyChange(PropertyChangeEvent evt) {
    				double mbytes = Utils.roundDouble(Utils.Memory.Convert.bytesToMegabytes(((Long) evt.getNewValue()).longValue()), 2);
    				firePropertyChange("note", null, "Writing File... : " + String.valueOf(mbytes) + " mb");
    			}
    		});
    		fm.startMonitor();
    		Integer vertexIndex = 1;
    		boolean isMultGrids = constraintsList.size() > 1;
    		for(NetcdfConstraints constraints : constraintsList){
    			// constraints.getBoundingBox().toString2());
    			NcGridObjectProperties gridProps = props.getGridProperties(vertexIndex-1);
    			constraints.resetVariables();
    			for (String s : selPanel.getCblVars().getSelectedItems()) {
    				// constraints.addVariable(s);//orig
    				constraints.addVariable(selPanel.getVarNameFromDescription(s));
    			}

    			vars = constraints.getSelVars();


    			// ensure that the zVar tag and tVar tag are null unless one of
    			// the variables has a z dimension
    			boolean hasZ = false;
    			boolean hasT = false;
    			for (String s : vars) {
    				GeoGrid g = getGridByName(s, false);
    				if (g != null) {
    					GridCoordSystem gcs = g.getCoordinateSystem();
    					CoordinateAxis1D vAxis = gcs.getVerticalAxis();
    					if (vAxis != null) {
    						hasZ = true;
    					}
    					if (gcs.hasTimeAxis()) {
    						hasT = true;
    					}
    				}
    			}
    			if (!hasZ) {
    				constraints.setZDim("null");
    			}
    			if (!hasT) {
    				constraints.setTimeDim("null");
    				constraints.setTVar("null");
    				sTime = "null";
    			} else {
    				sTime = constraints.getStartTime().toString();
    			}



    			guiLogger.info("Processing data...");



    			// If selected area is a track line then call writeFile once for each track vertex

    			// Set up array lists to hold the variables, variable names, and
    			// coordinate axis


    			List<Variable> varList = new ArrayList<Variable>();
    			List<GridDatatype> trimmedGrids = new ArrayList<GridDatatype>();
    			if(isEsri && isMultGrids){
    				ncPath = baseFilePath + baseFileName + vertexIndex + ncExt;
    				gridProps.setNcPath(ncPath);
    			}
    			runOK = writer.prepareToWriteFile(ncPath, gdsList, constraints, type,
    					varList, polygonVertices, trimmedGrids);
    		
    			
    			if(polygonVertices != null && trimmedGrids.size() > 0){
    				GridDatatype grid = trimmedGrids.get(0);
    				if(grid != null){
    					Boolean[][] allowedIndices = writer.getIndiciestoInclude(polygonVertices, trimmedGrids.get(0));
    					GridCoordSystem coordSys = grid.getCoordinateSystem();
    					if(coordSys != null){
    						if(coordSys.getXHorizAxis() != null)
    							xDimNameMap.put(vertexIndex, coordSys.getXHorizAxis().getFullName());
    						if(coordSys.getYHorizAxis() != null)
    							yDimNameMap.put(vertexIndex, coordSys.getYHorizAxis().getFullName());
    						if(coordSys.getVerticalAxis() != null)
    							zDimNameMap.put(vertexIndex, coordSys.getVerticalAxis().getFullName());
    					}
    					includeIndicesMap.put(vertexIndex, allowedIndices);
    				}
    			}
    			
    			String timeDim = constraints.getTimeDim();
    			String xDim = constraints.getXDim();
    			String yDim = constraints.getYDim();
    			String zDim = constraints.getZDim();
    			String varName = constraints.getTVar();
    			
    			List<String> varForVertex = new ArrayList<String>();
    			Integer vertForSetProp = null;
    			 if(isMultGrids && !isEsri){
    				 if(timeDim != null && !timeDim.equals("") && !timeDim.equals("null"))
    					 timeDim += vertexIndex;
    				 if(xDim != null && !xDim.equals("") && !xDim.equals("null"))
    					 xDim += vertexIndex;
    				 if(yDim != null && !yDim.equals("") && !yDim.equals("null"))
    					 yDim += vertexIndex;
    				 if(zDim != null && !zDim.equals("")  && !zDim.equals("null"))
    					 zDim += vertexIndex;
    				 if(varName != null && !varName.equals("")  && !varName.equals("null"))
    					 varName += vertexIndex;
					 vertForSetProp = vertexIndex;
					 for(String v : vars){
						 varForVertex.add(v+vertexIndex);
					 }
    			 }
    			 else{
    				 varForVertex.addAll(vars);
    			 }
    			 
     			setProperties(constraints, type, gridProps, varForVertex, vertForSetProp);

    			gridProps.setStartTime(sTime);
    			gridProps.setTimeInterval(constraints.getTimeInterval());
    			gridProps.setTimeUnits(constraints.getTimeUnits());
    			gridProps.setTime(timeDim);
    			gridProps.setTVar(varName);
    			gridProps.setXCoord(xDim);
    			gridProps.setYCoord(yDim);
    			gridProps.setZCoord(zDim);
    			gridProps.setProjection(constraints.getProjection());
    			gridProps.setVars(varForVertex);
    			varVertexMap.put(vertexIndex, varList);

    			if(isEsri){
    				// write a separate file for each track point
    				writer.writerFile(varVertexMap, ncPath, polygonVertices!=null,
    	    				includeIndicesMap, xDimNameMap, yDimNameMap, zDimNameMap); //grid,
    				varVertexMap.clear();
    				xDimNameMap.clear();
    				yDimNameMap.clear();
    				zDimNameMap.clear();
    				
        			
        			calculateTimes(gridProps);
        				
        			
    			}
    			
    			vertexIndex++;
    		}

    		if(!isEsri)
    			writer.writerFile(varVertexMap, ncPath, polygonVertices!=null,
    				includeIndicesMap, xDimNameMap, yDimNameMap, zDimNameMap); //grid,

    		else{
    			ncPath = baseFilePath + baseFileName + ncExt;
	
    		}
    		fm.stopMonitor();



    		//   writer.writerFile(varVertexMap, ncPath, this.polygonVertices);


    		// only write the props file if the nc file was generated properly
    		if (runOK == NetcdfGridWriter.SUCCESSFUL_PROCESS) {
    			firePropertyChange("note", null, "Writing properties file...");
    			Thread.sleep(250);
    			
    			if(!isEsri){
    				for(int consI = 0; consI < 	constraintsList.size(); consI++) {
    					NcGridObjectProperties gridProps = props.getGridProperties(consI);
    					calculateTimes(gridProps);
    				}
    			}
    			props.setOutPath(outname.replace("-", "_"));
    			
    			if(selPanel.getPanelType() == 2){
    				props.setSurfLayer(selPanel.getSurfaceLevel());
    				props.setVectorType(selPanel.isVectorType());
    			}
    			props.writeFile();
    			firePropertyChange("done", null, ncPath);

    			// Write text file to track locations
    			History.addEntry(outname, ncPath.replace(".nc", ".xml"));

    		} else if (runOK == NetcdfGridWriter.CANCELLED_PROCESS) {
    			firePropertyChange("cancel", false, true);
    		} else if (runOK == NetcdfGridWriter.UNDEFINED_ERROR) {
    			firePropertyChange("error", false, true);
    		}


    	} catch (Exception ex) {
        logger.error("PD:run:", ex);
      }

      firePropertyChange("close", false, true);
      return null;
    }

    
    
    private void setProperties(NetcdfConstraints constraints, ArcType type, 
    						  NcGridObjectProperties props, 	List<String> vars, Integer vertexId){
    	// if(makeRaster){
		switch (selPanel.getPanelType()) {
		case 0:// general
			constraints.setTrimByIndex(selPanel.getTrimByIndex());
			constraints.setUseAllValues(selPanel.isUseAllLevels());
			break;
		case 1:// ESRI
			type = ArcType.FEATURE;
			constraints.setTrimByIndex(selPanel.getTrimByIndex());
			constraints.setUseAllValues(selPanel.isUseAllLevels());
			props.setBandDim(constraints.getBandDim());
			props.setTrimByDim(constraints.getTrimByDim());
			props.setTrimByValue(selPanel.getTrimByValue());

			if (selPanel.isMakeRaster()) {// RASTER
				type = ArcType.RASTER;
				// constraints.setBandDim((String)cbBandDim.getSelectedItem());
				// constraints.setTrimByIndex(cbTrimBy.getSelectedIndex());
				// constraints.setTrimByIndex(selPanel.getTrimByIndex());
				// props.setBandDim(constraints.getBandDim());
				// props.setTrimByDim(constraints.getTrimByDim());
				// props.setTrimByValue(selPanel.getTrimByValue());

				// ensure that the correct variable dimensions are
				// used
				GeoGrid grid = getGridByName(vars.get(0), false);
				if (grid != null) {
					constraints.setXDim(grid.getXDimension().getShortName());
					constraints.setYDim(grid.getYDimension().getShortName());
				}
				// }else if(constraints.getSelVars().size() > 1){
			} else if (selPanel.isMakeVector()) {// VECTOR
				type = ArcType.VECTOR;
				vars.clear();
				String uVar = selPanel.getVarNameFromDescription(selPanel.getUVar());
				String vVar = selPanel.getVarNameFromDescription(selPanel.getVVar());
				if(vertexId != null){
					uVar += vertexId;
					vVar += vertexId;
				}
				props.setUVar(uVar);
				props.setVVar(vVar);
				vars.add(props.getUVar());
				vars.add(props.getVVar());
			}
			break;
		case 2:// OILMAP
			props.setUVar(selPanel.getUVar());
			props.setVVar(selPanel.getVVar());
			
			break;
		}
		props.setType(type);

    }
    
    
    private void calculateTimes(NcGridObjectProperties gridProps) {
      List<String> tStrings = new ArrayList<String>();
      Date[] times;
      Variable tVar = null;
      try {
        NetcdfFile ncfile = NetcdfFile.open(ncPath);
        tVar = ncfile.findVariable(gridProps.getTVar());
      } catch (IOException ex) {
        ex.printStackTrace();
      }
      if (tVar == null) {
        return;
      }
      try {
        Array arr = tVar.read();
        DateUnit du;
        String uString = tVar.getUnitsString();
        IndexIterator iter = arr.getIndexIterator();
        times = new Date[(int) tVar.getSize()];
        int i = 0;
        double t;
        while (iter.hasNext()) {
          t = iter.getDoubleNext();
          du = new DateUnit(t + " " + uString);
          times[i] = du.getDate();
          i++;
        }

        // removed ':ss' for timeslider compatibility
        SimpleDateFormat df = new SimpleDateFormat("MM/dd/yyyy HH:mm");
        df.setTimeZone(TimeZone.getTimeZone("GMT"));
        for (Date d : times) {
          tStrings.add(df.format(d));
        }

      } catch (Exception ex) {
        tStrings = new ArrayList<String>();

      ex.printStackTrace();
      }
      gridProps.setTimes(tStrings);
    }
  }
}
