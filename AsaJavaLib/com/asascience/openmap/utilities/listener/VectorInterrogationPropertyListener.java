/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * VectorInterrogationPropertyListener.java
 *
 * Created on Jul 22, 2008, 9:57:19 AM
 *
 */
package com.asascience.openmap.utilities.listener;

import java.awt.Point;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Hashtable;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import com.asascience.edp.EdpFileLoader;
import com.asascience.edp.datafile.hydro.DataFileBase;
import com.asascience.openmap.layer.VectorLayer;
import com.asascience.openmap.mousemode.VectorInterrogationMouseMode;
import com.asascience.openmap.omgraphic.OMArrow;
import com.asascience.openmap.omgraphic.OMGridCell;
import com.asascience.openmap.ui.OMGraphicTimeseriesDialog;
import com.asascience.openmap.utilities.MapUtils;
import com.asascience.utilities.Utils;
import com.asascience.utilities.Vector3D;
import com.bbn.openmap.Layer;
import com.bbn.openmap.LayerHandler;

/**
 * 
 * @author cmueller_mac
 */
public class VectorInterrogationPropertyListener implements PropertyChangeListener {

  protected transient JFrame parent;
  protected transient LayerHandler lh;
  protected OMGraphicTimeseriesDialog ogtd;

  /** Creates a new instance of VectorInterrogationPropertyListener */
  public VectorInterrogationPropertyListener(JFrame parent, LayerHandler lh) {
    this.parent = parent;
    this.lh = lh;
    // this.ogtd = ogtd;
  }

  public void propertyChange(PropertyChangeEvent evt) {
    String propName = evt.getPropertyName();
    if (propName.equals(VectorInterrogationMouseMode.VECTOR_INTERROGATION)) {
      String name = String.valueOf(evt.getOldValue());
      // get top-most vector layer that is visible
      VectorLayer vl = null;
      for (Layer l : lh.getMapLayers()) {
        if (l instanceof VectorLayer) {
          if (l.isVisible()) {
            vl = (VectorLayer) l;
            break;
          }
        }
      }
      if (vl == null) {
        return;
      }

      Point cp = (Point) evt.getNewValue();
      if (cp != null) {
        // get the closest graphic
        com.bbn.openmap.omGraphics.OMGraphic newSelGraphic = vl.getList().selectClosest((int) cp.getX(),
                (int) cp.getY(), 5.0f);
        if (newSelGraphic != null) {// & newSelGraphic instanceof
          // OMArrow){
          double lat, lon;
          StringBuilder desc = new StringBuilder();
          if (newSelGraphic instanceof OMArrow) {
            OMArrow arrow = (OMArrow) newSelGraphic;
            lat = arrow.getVectorStartLat();
            lon = arrow.getVectorStartLon();
            desc.append("Lat = ");
            desc.append(Utils.roundDouble(lat, 4));
            desc.append(" Lon = ");
            desc.append(Utils.roundDouble(lon, 4));
            desc.append("\n");
            desc.append(arrow.getUDescription());
            desc.append(" ");
            desc.append(arrow.getVDescription());
            desc.append("\n");
            desc.append(arrow.getSpeedDescription());
            desc.append(" ");
            desc.append(arrow.getDirectionDescription());
            // desc = arrow.getUDescription() +
            // " " + arrow.getVDescription() +
            // "\n" + arrow.getSpeedDescription() +
            // " " + arrow.getDirectionDescription();
          } else if (newSelGraphic instanceof OMGridCell) {
            OMGridCell gc = (OMGridCell) newSelGraphic;
            lat = gc.getCenterLat();
            lon = gc.getCenterLon();
            desc.append("Lat = ");
            desc.append(Utils.roundDouble(lat, 4));
            desc.append(" Lon = ");
            desc.append(Utils.roundDouble(lon, 4));
            desc.append("\n");
            desc.append(vl.getSelGridVar());
            desc.append(" = ");
            desc.append(gc.getData());
            desc.append(" (units)");
            // desc = gc.getData() + " (units)";
          } else {
            return;
          }
          // if(arrow != null){
          if (name.equals(VectorInterrogationMouseMode.VIEW_DATA)) {

            // JOptionPane.showMessageDialog(parent, arrow.
            // getUDescription() +
            // " " + arrow.getVDescription() + "\n" + arrow.
            // getSpeedDescription() +
            // " " + arrow.getDirectionDescription());
            JOptionPane.showMessageDialog(parent, desc.toString());
          } else if (name.equals(VectorInterrogationMouseMode.TIMESERIES)) {
            // double lat = arrow.getVectorStartLat();
            // double lon = arrow.getVectorStartLon();
            // System.out.println(lat + " " + lon);
            DataFileBase dfb = EdpFileLoader.loadVectorDataFile(vl.getSourceFilePath());

            try {
              Vector3D pos = new Vector3D(lon, lat, 0);
              Utils.showBusyCursor(parent.getRootPane());
              /** Get the UV Timeseries from the dataset */
              // this call can take FOREVER...
              double[][] uvData = dfb.getUVTimeSeries(pos);
              /**
               * Build a hashtable to hold all the timeseries data
               */
              Hashtable<String, double[]> ht = null;
              /** Put the UV data in the hashtable */
              if (uvData != null) {
                ht = new Hashtable<String, double[]>();
                ht.put(OMGraphicTimeseriesDialog.U_COMP, MapUtils.Conversion.toMPS(vl.getUvUnits(),
                        uvData[0]));
                ht.put(OMGraphicTimeseriesDialog.V_COMP, MapUtils.Conversion.toMPS(vl.getUvUnits(),
                        uvData[1]));
              }
              /**
               * If the dataset has the "surf_el" variable, get
               * the data and add it to the hashtable
               */
              double[] surfel = dfb.getDataTimeseries("surf_el", pos);
              if (surfel != null) {
                if (ht == null) {
                  ht = new Hashtable<String, double[]>();
                }
                ht.put(OMGraphicTimeseriesDialog.W_LEVEL, surfel);
              }
              /**
               * If an OMGridCell was clicked, get the data and
               * add it to the hashtable
               */
              if (newSelGraphic instanceof OMGridCell) {
                String grdName = vl.getSelGridVar();
                double[] grdVar = dfb.getDataTimeseries(grdName, pos);
                if (grdVar != null) {
                  if (ht == null) {
                    ht = new Hashtable<String, double[]>();
                  }
                  ht.put(grdName, grdVar);
                }
              }
              if (ht != null) {
                if (ogtd == null) {
                  ogtd = new OMGraphicTimeseriesDialog(parent, dfb.getStartTime(), dfb.getTimeIncrement(), ht);
                  ogtd.setVisible(true);
                } else {
                  if (!ogtd.isVisible()) {
                    ogtd.setVisible(true);
                  }
                  ogtd.redisplay(dfb.getStartTime(), dfb.getTimeIncrement(), ht);
                  ogtd.toFront();
                }
              }
            } catch (Exception ex) {
              Logger.getLogger(VectorInterrogationPropertyListener.class.getName()).log(Level.SEVERE,
                      null, ex);
            } finally {
              Utils.hideBusyCursor(parent.getRootPane());
            }
          }
          // }
        }
      }
    }
  }
}
