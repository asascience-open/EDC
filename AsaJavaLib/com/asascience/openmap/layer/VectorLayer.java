package com.asascience.openmap.layer;

/*
 * VectorLayer.java
 *
 * Created on July 30, 2007, 9:55 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import net.miginfocom.swing.MigLayout;

import org.jdom.Element;
import org.softsmithy.lib.swing.JDoubleField;

import ucar.unidata.geoloc.LatLonPointImpl;
import ucar.unidata.geoloc.LatLonRect;

import com.asascience.openmap.mousemode.InformationMouseMode;
import com.asascience.openmap.mousemode.TidalHarmonicsMouseMode;
import com.asascience.openmap.mousemode.TimeseriesMouseMode;
import com.asascience.openmap.mousemode.VectorInterrogationMouseMode;
import com.asascience.openmap.omgraphic.OMArrow;
import com.asascience.openmap.omgraphic.OMGridCell;
import com.asascience.openmap.omgraphic.OMVectorLine;
import com.asascience.openmap.utilities.LayerPropsUtils;
import com.asascience.openmap.utilities.MapUtils;
import com.asascience.ui.JActionLabel;
import com.asascience.ui.OptionDialogBase;
import com.asascience.utilities.NumFieldUtilities;
import com.asascience.utilities.Utils;
import com.bbn.openmap.LatLonPoint;
import com.bbn.openmap.LayerHandler;
import com.bbn.openmap.MapBean;
import com.bbn.openmap.event.MapMouseListener;
import com.bbn.openmap.gui.BasicMapPanel;
import com.bbn.openmap.omGraphics.OMGraphic;
import com.bbn.openmap.omGraphics.OMGraphicList;
import com.bbn.openmap.util.DataBounds;

/**
 * 
 * @author cmueller
 */
public abstract class VectorLayer extends TimeLayer implements MapMouseListener, ActionListener, ChangeListener {

  public static final String COLOR_SINGLE = "color_s";
  public static final String COLOR_BY_SPEED = "color_bs";
  public static final String SELECT_VECT_COLOR = "selcolor";
  public static final String SCALE_BY_SPEED = "scaleBySpeed";
  public static final String USE_BLACK_OUTLINE = "useBlackOutline";
  public static final String MIN_VECT_VAL = "minVectVal";
  public static final String MID_VECT_VAL = "midVectVal";
  public static final String MAX_VECT_VAL = "maxVectVal";
  private static Logger logger = Logger.getLogger("VectorLayer");
  private OMGraphicList omgraphics;
  protected double[] lats;
  protected double[] lons;
  protected double[] us;
  protected double[] vs;
  protected List<OMGridCell> gridCells = null;
  private double uvFillVal;
  private int thinBy = 0;// default to draw everything
  private float vectorSize = 1f;// default to no scaling
  private float maxSpeed;
  private float minSpeed;
  private Color vectorColor = Color.RED;
  private JSpinner jsSize;
  private JSpinner jsThin;
  private JPanel pnlVectorColor;
  private JButton btnColorSel;
  private JPanel pnlMinVectColor;
  private JPanel pnlMidVectColor;
  private JPanel pnlMaxVectColor;
  private JCheckBox cbScaleBySpeed;
  private JCheckBox cbUseBlackOutline;
  private JActionLabel jalVectMinVal;
  private JActionLabel jalVectMidVal;
  private JLabel lblVectMidVal;
  private JActionLabel jalVectMaxVal;
  private JLabel lblVectMaxVal;
  protected boolean scaleBySpeed = false;
  protected boolean useBlackOutline = true;
  private NumFieldUtilities numUtils;
  // actionCommands
  protected boolean drawGridCells = false;
  protected boolean drawVectors = true;
  protected transient JPanel box;
  protected OMGraphic selectedGraphic;
  private boolean showDisplayType = true;
  protected Color[] colorArray;
  protected double[] divisionValues;
  protected String selGridVar = null;
  protected String uvUnits = null;
  protected MapBean parentMap = null;

  /**
   * Creates a new instance of VectorLayer
   */
  public VectorLayer() {
    omgraphics = new OMGraphicList();
    this.setList(omgraphics);
    // this.setName("TestLayer");
    this.consumeEvents = false;
    this.mouseModeIDs = getMouseModeServiceList();
    // createGraphics();
    // this.doPrepare();
    this.numUtils = new NumFieldUtilities();
  }

  /**
   *
   * @return
   */
  @Override
  public boolean receivesMapEvents() {
    return false;
  }

  protected void buildGridCells(double[] data, Color[] colors, double[] divVals) {
    gridCells = new ArrayList<OMGridCell>();
  }

  @Override
  public LatLonRect getLayerExtentRectangle() {
    if (lats != null | lons != null) {
      double[] nLat = new double[lats.length];
      System.arraycopy(lats, 0, nLat, 0, lats.length);
      double[] nLon = new double[lons.length];
      System.arraycopy(lons, 0, nLon, 0, lons.length);

      if (nLat == null || nLat.length == 0 || nLon == null || nLon.length == 0) {
        return null;
      }
      double latMax = nLat[nLat.length - 1];
      double latMin = nLat[0];
      double lonMax = nLon[nLon.length - 1];
      double lonMin = nLon[0];

      // double latMax = lats[0];
      // double latMin = lats[0];
      // double lonMax = lons[0];
      // double lonMin = lons[0];
      //
      // for(double d : lats){
      // if(d > latMax){
      // latMax = d;
      // }
      // if(d < latMin){
      // latMin = d;
      // }
      // }
      // for(double d : lons){
      // if(d > lonMax){
      // lonMax = d;
      // }
      // if(d < lonMin){
      // lonMin = d;
      // }
      // }
      //
      return new LatLonRect(new LatLonPointImpl(latMin, lonMin), new LatLonPointImpl(latMax, lonMax));
    }

    return null;
  }

  public DataBounds getLayerExtent() {
    DataBounds db = null;
    if (lats != null | lons != null) {
      double latMax = lats[0];
      double latMin = lats[0];
      double lonMax = lons[0];
      double lonMin = lons[0];

      for (double d : lats) {
        if (d > latMax) {
          latMax = d;
        }
        if (d < latMin) {
          latMin = d;
        }
      }
      for (double d : lons) {
        if (d > lonMax) {
          lonMax = d;
        }
        if (d < lonMin) {
          lonMin = d;
        }
      }

      db = new DataBounds(lonMin, latMin, lonMax, latMax);
    }

    return db;
  }

  public void clearDisplay() {
    OMGraphicList list = this.getList();
    list.clear();
    this.doPrepare();
  }

  public void display() {
    createGraphics();
    this.doPrepare();
  }

  public void display(double[] uvel, double[] vvel) {
    us = uvel;
    vs = vvel;
    createGraphics();
    this.doPrepare();
  }

  // public void display(double[] latitudes, double[] longitudes, double[]
  // uvel, double[] vvel) {
  // lats = latitudes;
  // lons = longitudes;
  // us = uvel;
  // vs = vvel;
  // createGraphics();
  // this.doPrepare();
  // }
  public void refreshDisplay() {
    if (this.isVisible()) {
      display(us, vs);
      // display(lats, lons, us, vs);
    }
  }

  public void setMaxSpeed(float val) {
    maxSpeed = val;
  }

  public void setMinSpeed(float val) {
    minSpeed = val;
  }

  public float getMaxSpeed() {
    return maxSpeed;
  }

  public float getMinSpeed() {
    return minSpeed;
  }

  public boolean isShowDisplayType() {
    return showDisplayType;
  }

  public void setLats(double[] latitudes) {
    lats = latitudes;
  }

  public void setLons(double[] longitudes) {
    lons = longitudes;
  }

  public void setUs(double[] uVels) {
    us = uVels;
  }

  public void setVs(double[] vVels) {
    vs = vVels;
  }

  public void setUVFillVal(double fillVal) {
    uvFillVal = fillVal;
  }

  public void setVectorThinning(int thinVectorsBy) {
    thinBy = thinVectorsBy;
  }

  public void setVectorColor(Color vColor) {
    vectorColor = vColor;
  }

  public void setShowDisplayType(boolean showDisplayType) {
    this.showDisplayType = showDisplayType;
  }

  /**
   *
   * @return
   */
  @Override
  public Component getGUI() {
    if (box == null) {
      box = new JPanel(new MigLayout("", "[left]", ""));
      // box.setLayout(new BoxLayout(box, BoxLayout.Y_AXIS));
      // box.setAlignmentX(Component.LEFT_ALIGNMENT);

      // Vector thinning section
      JPanel pnlThinning = new JPanel(new MigLayout("insets 0", "[][grow]", ""));
      SpinnerNumberModel snm = new SpinnerNumberModel(thinBy, 0, 50, 1);
      jsThin = new JSpinner(snm);
      jsThin.setName("thin");
      jsThin.addChangeListener(this);
      JLabel lblThin = new JLabel("Vector Thinning:");
      pnlThinning.add(lblThin);
      pnlThinning.add(jsThin, "w 80!");

      JPanel pnlScaling = new JPanel(new MigLayout("insets 0", "[][grow]", ""));
      // SpinnerNumberModel snm2 = new SpinnerNumberModel(vectorSize,
      // 0.00001, 5, 0.005);
      SpinnerNumberModel snm2 = new SpinnerNumberModel(vectorSize, 0.005, 10, 0.005);
      jsSize = new JSpinner(snm2);
      jsSize.setName("scale");
      jsSize.addChangeListener(this);
      JLabel lblScale = new JLabel("Vector Size:");
      pnlScaling.add(lblScale);
      pnlScaling.add(jsSize, "w 80!, wrap");
      cbScaleBySpeed = new JCheckBox("Scale By Speed");
      cbScaleBySpeed.setSelected(scaleBySpeed);
      cbScaleBySpeed.setActionCommand(SCALE_BY_SPEED);
      cbScaleBySpeed.addActionListener(this);
      pnlScaling.add(cbScaleBySpeed);

      // JPanel pnlDisplayType = new JPanel(new MigLayout("insets 0",
      // "[][grow][]", ""));
      // JComboBox cbDisp = new JComboBox(new Object[]{"Vectors", "Grid",
      // "Vectors & Grid"});
      // cbDisp.addItemListener(new ItemListener() {
      //
      // public void itemStateChanged(ItemEvent e) {
      // String item = e.getItem().toString();
      // if(item.toLowerCase().equals("vectors")){
      // drawVectors = true;
      // drawGridCells = false;
      // }else if(item.toLowerCase().equals("grid")){
      // drawVectors = false;
      // drawGridCells = true;
      // }else{
      // drawVectors = true;
      // drawGridCells = true;
      // }
      //
      // refreshDisplay();
      // }
      // });
      // cbDisp.setSelectedIndex(0);
      //
      // if(showDisplayType){
      // pnlDisplayType.add(new JLabel("Display:"));
      // pnlDisplayType.add(cbDisp, "center");
      // }

      // Vector color type
      JPanel pnlColorBy = new JPanel(new MigLayout("insets 0", "[][grow]", ""));
      ButtonGroup bg = new ButtonGroup();
      JRadioButton rbSingle = new JRadioButton("Single Color");
      JRadioButton rbBySpeed = new JRadioButton("Colored By Speed");
      rbSingle.setActionCommand(COLOR_SINGLE);
      rbSingle.addActionListener(this);
      rbSingle.setSelected(true);
      rbBySpeed.setActionCommand(COLOR_BY_SPEED);
      rbBySpeed.addActionListener(this);
      bg.add(rbSingle);
      bg.add(rbBySpeed);
      pnlColorBy.add(rbSingle);
      pnlColorBy.add(rbBySpeed);

      // Vector color selection
      JPanel pnlSelectColor = new JPanel(new MigLayout("insets 0", "[][grow]", ""));
      btnColorSel = new JButton("Select Color");
      btnColorSel.setActionCommand(SELECT_VECT_COLOR);
      btnColorSel.addActionListener(this);
      pnlVectorColor = new JPanel();
      pnlVectorColor.setBackground(vectorColor);
      pnlSelectColor.add(btnColorSel);
      pnlSelectColor.add(pnlVectorColor);

      JPanel pnlVectorScale = new JPanel(new MigLayout("insets 0", "[][grow]", ""));
      BasicMapPanel bmp = new BasicMapPanel();
      LayerHandler bmpLh = new LayerHandler();
      bmp.getMapHandler().add(bmpLh);
      bmp.getMapBean().setBackground(Color.white);
      VectorLayer vl = new VectorLayer() {

        @Override
        public void drawDataForTime(long t) {
          drawVectors = false;
          this.display();
        }
      };
      bmpLh.addLayer(vl);
      pnlVectorScale.add(bmp);
      pnlVectorScale.setPreferredSize(new Dimension(50, 50));

      // Redraw button section
      JPanel pnlRedrawButton = new JPanel(new MigLayout("insets 0", "[][grow]", ""));
      JButton redraw = new JButton("Redraw Layer");
      redraw.setActionCommand(RedrawCmd);
      redraw.addActionListener(this);
      pnlRedrawButton.add(redraw);

      box.add(pnlThinning, "wrap");
      box.add(pnlScaling, "wrap");
      // box.add(pnlVectorScale, "wrap");

      // box.add(pnlDisplayType, "wrap");
      // box.add(pnlColorBy, "wrap");
      box.add(vectorLegendPanel(), "wrap");
      // box.add(pnlSelectColor, "wrap");
      // box.add(pnlRedrawButton);
    }

    return box;
  }

  private JPanel vectorLegendPanel() {
    JPanel pnlVectorColors = new JPanel(new MigLayout("insets 0", "[][grow]", ""));

    ColorPanelMouseListener cpml = new ColorPanelMouseListener();
    pnlMinVectColor = new JPanel();
    pnlMinVectColor.setBorder(BorderFactory.createLineBorder(Color.BLACK));
    pnlMinVectColor.setBackground(Color.BLUE);
    pnlMinVectColor.addMouseListener(cpml);
    pnlMidVectColor = new JPanel();
    pnlMidVectColor.setBorder(BorderFactory.createLineBorder(Color.BLACK));
    pnlMidVectColor.setBackground(Color.YELLOW);
    pnlMidVectColor.addMouseListener(cpml);
    pnlMaxVectColor = new JPanel();
    pnlMaxVectColor.setBorder(BorderFactory.createLineBorder(Color.BLACK));
    pnlMaxVectColor.setBackground(Color.RED);
    pnlMaxVectColor.addMouseListener(cpml);

    pnlVectorColors.setBorder(BorderFactory.createTitledBorder("Vector Legend:"));
    // pnlVectorColors.add(new JLabel("Vector Legend:"), "wrap");
    // pnlVectorColors.add(new JLabel("0 m/s > "));
    jalVectMinVal = new JActionLabel("0", this, MIN_VECT_VAL, Color.BLUE, Color.RED);
    jalVectMinVal.setToolTipText("Click to set the value.");
    pnlVectorColors.add(jalVectMinVal);
    pnlVectorColors.add(new JLabel("knots >"));
    pnlVectorColors.add(pnlMinVectColor);
    // pnlVectorColors.add(new JLabel(" <= 0.544 m/s"), "wrap");
    pnlVectorColors.add(new JLabel("<="));
    lblVectMidVal = new JLabel("1");
    pnlVectorColors.add(lblVectMidVal);
    pnlVectorColors.add(new JLabel("knots"), "wrap");

    // pnlVectorColors.add(new JLabel("0.544 m/s > "));
    jalVectMidVal = new JActionLabel("1", this, MID_VECT_VAL, Color.BLUE, Color.RED);
    jalVectMidVal.setToolTipText("Click to set the value.");
    pnlVectorColors.add(jalVectMidVal);
    pnlVectorColors.add(new JLabel("knots >"));
    pnlVectorColors.add(pnlMidVectColor);
    // pnlVectorColors.add(new JLabel(" <= 1.286 m/s"), "wrap");
    pnlVectorColors.add(new JLabel("<="));
    lblVectMaxVal = new JLabel("2.5");
    pnlVectorColors.add(lblVectMaxVal);
    pnlVectorColors.add(new JLabel("knots"), "wrap");

    // pnlVectorColors.add(new JLabel("1.286 m/s > "));
    jalVectMaxVal = new JActionLabel("2.5", this, MAX_VECT_VAL, Color.BLUE, Color.RED);
    jalVectMaxVal.setToolTipText("Click to set the value.");
    pnlVectorColors.add(jalVectMaxVal);
    pnlVectorColors.add(new JLabel("knots >"));
    pnlVectorColors.add(pnlMaxVectColor, "wrap");

    cbUseBlackOutline = new JCheckBox("Use Black Outline");
    cbUseBlackOutline.setSelected(useBlackOutline);
    cbUseBlackOutline.setActionCommand(USE_BLACK_OUTLINE);
    cbUseBlackOutline.addActionListener(this);
    pnlVectorColors.add(cbUseBlackOutline, "span");

    return pnlVectorColors;
  }

  private Double[] getUniqueLats() {
    List<Double> ret = new ArrayList<Double>();
    for (double d : lats) {
      if (!ret.contains(d)) {
        ret.add(d);
      }
    }

    return (Double[]) ret.toArray(new Double[0]);
  }

  private Double[] getUniqueLons() {
    List<Double> ret = new ArrayList<Double>();
    for (double d : lons) {
      if (!ret.contains(d)) {
        ret.add(d);
      }
    }

    return (Double[]) ret.toArray(new Double[0]);
  }

  private boolean isContained(double lat, double lon, LatLonPoint ul, LatLonPoint lr) {
    /** Fix the lon value if necessary. */
    if (lon > 180) {
      lon = -180 + (lon - 180);
    }

    if (lat > ul.getLatitude()) {
      return false;
    }
    if (lat < lr.getLatitude()) {
      return false;
    }
    if (lon > lr.getLongitude()) {
      return false;
    }
    if (lon < ul.getLongitude()) {
      return false;
    }

    return true;
  }

  private Color getColorBySpeed(double u, double v) {
    double speed = Math.sqrt((Math.pow(u, 2)) + Math.pow(v, 2));
    double minDiv = MapUtils.Conversion.toKNOTS("m/s", Double.valueOf(jalVectMinVal.getText()));
    double midDiv = MapUtils.Conversion.toKNOTS("m/s", Double.valueOf(jalVectMidVal.getText()));
    double maxDiv = MapUtils.Conversion.toKNOTS("m/s", Double.valueOf(jalVectMaxVal.getText()));

    if (speed >= minDiv && speed < midDiv) {
      return (pnlMinVectColor != null) ? pnlMinVectColor.getBackground() : Color.BLUE;
    }
    if (speed >= midDiv && speed < maxDiv) {
      return (pnlMidVectColor != null) ? pnlMidVectColor.getBackground() : Color.YELLOW;
    }
    if (speed >= maxDiv) {
      return (pnlMaxVectColor != null) ? pnlMaxVectColor.getBackground() : Color.RED;
    }
    return null;
  }

  public OMGraphicList createGraphics() {
    OMGraphicList list = this.getList();
    list.clear();
    // GLVector gl;
    // OMArrow ar;
    // OMGridCell gc;
    int thin = 0;

    LatLonPoint llpLR = null, llpUL = null;

    double lat, lon;
    boolean OK = false;
    if (this.getProjection() != null) {
      llpLR = this.getProjection().getLowerRight();
      llpUL = this.getProjection().getUpperLeft();
    }

    if (drawGridCells) {
      for (OMGridCell gc : gridCells) {
        lat = gc.getCenterLat();
        lon = gc.getCenterLon();
        if (llpUL != null & llpLR != null) {
          if (!isContained(lat, lon, llpUL, llpLR)) {
            continue;
          }
        }
        list.addOMGraphic(gc);
      }
    }
    if (drawVectors) {
      /** Convert the u & v values into (for now) m/s */
      double[] convU = MapUtils.Conversion.toMPS(uvUnits, us);
      double[] convV = MapUtils.Conversion.toMPS(uvUnits, vs);
      if (convU == null) {
        convU = us;
      }
      if (convV == null) {
        convV = vs;
      }

      Color arrowColor;
      for (int i = 0; i < lats.length; i++) {
        lat = lats[i];
        lon = lons[i];
        if (llpUL != null & llpLR != null) {
          if (!isContained(lat, lon, llpUL, llpLR)) {
            continue;
          }
        }
        if (us[i] != this.uvFillVal & vs[i] != this.uvFillVal) {
          if (!Double.isNaN(us[i]) & !Double.isNaN(vs[i])) {
            if (us[i] == 0 & vs[i] == 0) {
              continue;
            }
            // if(Math.abs(us[i]) < 1e-8d || Math.abs(vs[i]) <
            // 1e-8d){
            // // System.out.println(i);
            // continue;
            // }
            if (thin == thinBy) {
              thin = 0;
              /** line with arrowhead */
              // list.addOMGraphic(new OMVectorLine(lat, lon,
              // us[i], vs[i], vectorColor, scalingFactor, true,
              // false));
              /** line without arrowhead - for testing */
              // list.addOMGraphic(new OMVectorLine(lat, lon,
              // us[i], vs[i], Color.BLUE, vectorSize, false,
              // false));
              /** polygon arrows */
              // list.addOMGraphic(new OMArrow(lat, lon, us[i],
              // vs[i], vectorColor, scalingFactor, false));
              /** Converted polygon arrows */
              // list.addOMGraphic(new OMArrow(lat, lon, convU[i],
              // convV[i], vectorColor, scalingFactor, false));
              /** Converted polygon arrows colored by speed */
              arrowColor = getColorBySpeed(convU[i], convV[i]);
              if (arrowColor != null) {
                list.addOMGraphic(new OMArrow(lat, lon, convU[i], convV[i], arrowColor, vectorSize,
                        false, scaleBySpeed, useBlackOutline));
              }

              // gl = new GLVector(lat, lon, us[i], vs[i],
              // vectorColor, scalingFactor);
              // list.addOMGraphic((OMLine)gl);
            } else {
              thin++;
            }
          } else {
            // System.err.println("encountered NaN");
            thin++;
            if (thin > thinBy) {
              thin = 0;
            }
          }
        }
      }

      // add a "scale" arrow
      // OMArrow oa = new OMArrow(0, 0, 1, 0, vectorColor, scalingFactor,
      // false);
      // list.addOMGraphic(oa);
      // list.addOMGraphic(oa);
      // System.err.println("thin="+thin+" thinby="+thinBy);
    }

    return list;
  }

  public Element saveProperties() {
    Element ret = null;
    try {
      ret = new Element(LayerPropsUtils.VECTOR_LAYER);
      ret.setAttribute(LayerPropsUtils.LAYER_NAME, this.getName());
      ret.addContent(new Element(LayerPropsUtils.THINNING).setText(String.valueOf(thinBy)));
      ret.addContent(new Element(LayerPropsUtils.SIZE).setText(String.valueOf(vectorSize)));
      ret.addContent(new Element(LayerPropsUtils.SCALE_BY_SPEED).setText(String.valueOf(cbScaleBySpeed.isSelected())));
      ret.addContent(new Element(LayerPropsUtils.USE_BLACK_OUTLINE).setText(String.valueOf(cbUseBlackOutline.isSelected())));
      ret.addContent(new Element(LayerPropsUtils.MIN_VAL).setText(jalVectMinVal.getText()));
      ret.addContent(new Element(LayerPropsUtils.MID_VAL).setText(jalVectMidVal.getText()));
      ret.addContent(new Element(LayerPropsUtils.MAX_VAL).setText(jalVectMaxVal.getText()));
      ret.addContent(new Element(LayerPropsUtils.MIN_COL).setText(Integer.toHexString(pnlMinVectColor.getBackground().getRGB())));
      ret.addContent(new Element(LayerPropsUtils.MID_COL).setText(Integer.toHexString(pnlMidVectColor.getBackground().getRGB())));
      ret.addContent(new Element(LayerPropsUtils.MAX_COL).setText(Integer.toHexString(pnlMaxVectColor.getBackground().getRGB())));

    } catch (Exception ex) {
      Logger.getLogger(VectorLayer.class.getName()).log(Level.SEVERE, null, ex);
      ret = null;
    }

    return ret;
  }

  public boolean applyProperties(Element e) {
    try {
      thinBy = Integer.valueOf(e.getChildText(LayerPropsUtils.THINNING));
      jsThin.setValue(thinBy);
      vectorSize = Float.valueOf(e.getChildText(LayerPropsUtils.SIZE));
      jsSize.setValue(vectorSize);
      cbScaleBySpeed.setSelected(Boolean.valueOf(e.getChildText(LayerPropsUtils.SCALE_BY_SPEED)));
      cbUseBlackOutline.setSelected(Boolean.valueOf(e.getChildText(LayerPropsUtils.USE_BLACK_OUTLINE)));
      String val = e.getChildText(LayerPropsUtils.MIN_VAL);
      jalVectMinVal.setText(val);
      val = e.getChildText(LayerPropsUtils.MID_VAL);
      jalVectMidVal.setText(val);
      lblVectMidVal.setText(val);
      val = e.getChildText(LayerPropsUtils.MAX_VAL);
      jalVectMaxVal.setText(val);
      lblVectMaxVal.setText(val);
      pnlMinVectColor.setBackground(Utils.colorFromHex(e.getChildText(LayerPropsUtils.MIN_COL)));
      pnlMidVectColor.setBackground(Utils.colorFromHex(e.getChildText(LayerPropsUtils.MID_COL)));
      pnlMaxVectColor.setBackground(Utils.colorFromHex(e.getChildText(LayerPropsUtils.MAX_COL)));

      // Color col = new Color(Integer.valueOf("0x" +
      // e.getChildText("minCol"), 16));
      // pnlMinVectColor.setBackground(col);
      // col = new Color(Integer.valueOf("0x" + e.getChildText("midCol"),
      // 16));
      // pnlMidVectColor.setBackground(col);
      // col = new Color(Integer.valueOf("0x" + e.getChildText("maxCol"),
      // 16));
      // pnlMaxVectColor.setBackground(col);

      refreshDisplay();

      return true;
    } catch (Exception ex) {
      Logger.getLogger(VectorLayer.class.getName()).log(Level.SEVERE, null, ex);
    }
    return false;
  }

  // public void tryAnimate() {
  // OMGraphicList list = this.getList();
  // Iterator it = list.iterator();
  // Random r = new Random();
  // Random rI = new Random();
  // while(it.hasNext()){
  // GLVector gl = (GLVector)it.next();
  // double u = gl.getU();
  // double v = gl.getV();
  // if(r.nextInt(2) == 0){
  // gl.setU(u + r.nextFloat());
  // gl.setV(v + r.nextFloat());
  // }else{
  // gl.setU(u - r.nextFloat());
  // gl.setV(v - r.nextFloat());
  // }
  // gl.setLL(gl.calculateLine());
  // }
  // this.doPrepare();
  // }
  /**
   *
   * @return
   */
  @Override
  public MapMouseListener getMapMouseListener() {
    return this;
  }

  public String[] getMouseModeServiceList() {
    String[] ret = new String[4];
    ret[0] = InformationMouseMode.modeID;
    ret[1] = TidalHarmonicsMouseMode.modeID;
    ret[2] = TimeseriesMouseMode.modeID;
    ret[3] = VectorInterrogationMouseMode.modeID;
    return ret;
  }

  public boolean mousePressed(MouseEvent e) {
    return false;
  }

  public boolean mouseReleased(MouseEvent e) {
    return false;
  }

  public boolean mouseClicked(MouseEvent e) {
    if (e.getButton() == MouseEvent.BUTTON1) {
      this.fireHideToolTip();
      selectedGraphic = null;
      this.getList().deselectAll();
      repaint();
      // if(selectedGraphic != null){
      // String s = "";
      // if(selectedGraphic instanceof OMArrow){
      // s = ((OMArrow)selectedGraphic).getPropertyDescription("  ");
      // // this.fireRequestToolTip(g.getPropertyDescription());
      // }else if(selectedGraphic instanceof OMVectorLine){
      // s = ((OMVectorLine)selectedGraphic).getPropertyDescription("  ");
      // }
      // this.fireRequestInfoLine(s);
      // // this.fireRequestToolTip(s);
      // // this.fireRequestMessage(s);
      // }
    }
    return true;
  }

  public void mouseEntered(MouseEvent e) {
  }

  public void mouseExited(MouseEvent e) {
  }

  public boolean mouseDragged(MouseEvent e) {
    return false;
  }

  public boolean mouseMoved(MouseEvent e) {
    this.getList().deselectAll();
    OMGraphic newSelGraphic = this.getList().selectClosest(e.getX(), e.getY(), 5.0f);
    if (newSelGraphic != null) {
      selectedGraphic = newSelGraphic;
      repaint();
    } else {
      this.fireRequestInfoLine("");
    }

    if (selectedGraphic != null) {
      String s = "";
      if (selectedGraphic instanceof OMArrow) {
        s = ((OMArrow) selectedGraphic).getPropertyDescription("  ", "m/s");
      } else if (selectedGraphic instanceof OMVectorLine) {
        s = ((OMVectorLine) selectedGraphic).getPropertyDescription("  ", "m/s");
      } else if (selectedGraphic instanceof OMGridCell) {
        s = String.valueOf(((OMGridCell) selectedGraphic).getData());
      }
      this.fireRequestToolTip(s);
    }

    return true;
  }

  public void mouseMoved() {
    // this.getList().deselectAll();
    // repaint();
  }

  // /**
  // * Called when the Layer is removed from the MapBean, giving an
  // opportunity
  // * to clean up.
  // * @param cont
  // */
  // @Override
  // public void removed(Container cont) {
  // OMGraphicList list = this.getList();
  // if(list != null){
  // list.clear();
  // list = null;
  // }
  // }
  @Override
  public void actionPerformed(ActionEvent e) {
    String cmd = e.getActionCommand();
    boolean refresh = false;
    if (cmd.equals(RedrawCmd)) {
      refresh = true;
    } else if (cmd.equals(COLOR_SINGLE)) {
      btnColorSel.setEnabled(true);
      refresh = true;
    } else if (cmd.equals(COLOR_BY_SPEED)) {
      btnColorSel.setEnabled(false);
      refresh = true;
    } else if (cmd.equals(SELECT_VECT_COLOR)) {
      Color newColor = JColorChooser.showDialog(btnColorSel.getTopLevelAncestor(), "Select New Vector Color",
              vectorColor);
      if (newColor != null) {
        vectorColor = newColor;
        pnlVectorColor.setBackground(vectorColor);
        refresh = true;
      }
    } else if (cmd.equals(SCALE_BY_SPEED)) {
      scaleBySpeed = cbScaleBySpeed.isSelected();
      refresh = true;
    } else if (cmd.equals(USE_BLACK_OUTLINE)) {
      useBlackOutline = cbUseBlackOutline.isSelected();
      refresh = true;
    } else if (cmd.equals(MIN_VECT_VAL)) {
      double val = Double.valueOf(jalVectMinVal.getText());

      ValueChooserDialog vcd = new ValueChooserDialog(val, 0, Double.valueOf(jalVectMidVal.getText()));
      vcd.setVisible(true);
      if (vcd.acceptChanges()) {
        val = vcd.getValue();
        if (val >= 0) {
          if (val < Double.valueOf(jalVectMidVal.getText())) {
            jalVectMinVal.setText(String.valueOf(val));
            refresh = true;
          }
        }
      }

    } else if (cmd.equals(MID_VECT_VAL)) {
      double val = Double.valueOf(jalVectMidVal.getText());

      ValueChooserDialog vcd = new ValueChooserDialog(val, Double.valueOf(jalVectMinVal.getText()), Double.valueOf(jalVectMaxVal.getText()));
      vcd.setVisible(true);
      if (vcd.acceptChanges()) {
        val = vcd.getValue();
        if (val > Double.valueOf(jalVectMinVal.getText())) {
          if (val < Double.valueOf(jalVectMaxVal.getText())) {
            jalVectMidVal.setText(String.valueOf(val));
            lblVectMidVal.setText(String.valueOf(val));
            refresh = true;
          }
        }
      }

    } else if (cmd.equals(MAX_VECT_VAL)) {
      double val = Double.valueOf(jalVectMaxVal.getText());

      ValueChooserDialog vcd = new ValueChooserDialog(val, Double.valueOf(jalVectMidVal.getText()), 10);
      vcd.setVisible(true);
      if (vcd.acceptChanges()) {
        val = vcd.getValue();
        if (val > Double.valueOf(jalVectMidVal.getText())) {
          jalVectMaxVal.setText(String.valueOf(val));
          lblVectMaxVal.setText(String.valueOf(val));
          refresh = true;
        }
      }
    }

    if (refresh) {
      refreshDisplay();
    }
  }

  public void stateChanged(ChangeEvent e) {
    JSpinner spin = (JSpinner) e.getSource();
    SpinnerNumberModel snm = (SpinnerNumberModel) spin.getModel();
    if (spin.getName().equals("thin")) {
      thinBy = snm.getNumber().intValue();
    } else if (spin.getName().equals("scale")) {
      vectorSize = snm.getNumber().floatValue();
    }
    refreshDisplay();
  }

  public float getScalingFactor() {
    return vectorSize;
  }

  public void setScalingFactor(float scalingFactor) {
    this.vectorSize = scalingFactor;
  }

  public List<OMGridCell> getGridCells() {
    return gridCells;
  }

  public void setGridCells(List<OMGridCell> gridCells) {
    this.gridCells = gridCells;
  }

  public boolean isDrawGridCells() {
    return drawGridCells;
  }

  public void setDrawGridCells(boolean drawGridCells) {
    this.drawGridCells = drawGridCells;
  }

  public String getSelGridVar() {
    return selGridVar;
  }

  public String getUvUnits() {
    return uvUnits;
  }

  class ColorPanelMouseListener extends MouseAdapter {

    public void mouseClicked(MouseEvent evt) {
      JPanel pnl = (JPanel) evt.getSource();
      Color sColor = pnl.getBackground();
      Color newColor = JColorChooser.showDialog(pnl.getTopLevelAncestor(), "Select New Vector Color", sColor);
      if (newColor != null) {
        pnl.setBackground(newColor);
        refreshDisplay();
      }
    }
  }

  class ValueChooserDialog extends OptionDialogBase {

    private double lowerLimit;
    private double upperLimit;
    private double value;
    private JDoubleField jdfValue;

    public ValueChooserDialog(double value, double lowerLimit, double upperLimit) {
      super("Division Value");
      this.value = value;
      this.lowerLimit = lowerLimit;
      this.upperLimit = upperLimit;

      initComponents();
    }

    private void initComponents() {
      JPanel pnlMain = new JPanel(new MigLayout("fill"));
      TitledBorder border = BorderFactory.createTitledBorder("Select Value:");
      border.setTitleJustification(TitledBorder.LEFT);
      pnlMain.setBorder(border);

      jdfValue = new JDoubleField(NumFieldUtilities.makeDff2Places());
      jdfValue.setMinimumDoubleValue(lowerLimit);
      jdfValue.setMaximumDoubleValue(upperLimit);
      jdfValue.addFocusListener(numUtils);
      jdfValue.setDoubleValue(value);
      pnlMain.add(jdfValue, "growx");

      this.setMinimumSize(new java.awt.Dimension(250, 200));
      this.add(pnlMain, "wrap, growx");
      this.add(super.buttonPanel("Set"), "center");
      this.pack();
    }

    public double getValue() {
      try {
        jdfValue.commitEdit();
        return jdfValue.getDoubleValue();
      } catch (ParseException ex) {
        Logger.getLogger(VectorLayer.class.getName()).log(Level.SEVERE, null, ex);
      }
      return value;
    }
  }
}
