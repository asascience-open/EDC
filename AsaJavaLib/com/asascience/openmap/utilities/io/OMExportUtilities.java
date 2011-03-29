/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * OMExportUtilities.java
 *
 * Created on Oct 3, 2008, 8:18:27 AM
 *
 */
package com.asascience.openmap.utilities.io;

import java.awt.Color;
import java.awt.Component;
import java.awt.Point;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;

import com.asascience.openmap.layer.TimeLayer;
import com.asascience.openmap.omgraphic.OMArrow;
import com.asascience.openmap.omgraphic.OMGridCell;
import com.asascience.openmap.omgraphic.OMParticle;
import com.asascience.utilities.Utils;
import com.asascience.utilities.filefilter.CustomFileFilter;
import com.asascience.utilities.filefilter.GeoTiffFileFilter;
import com.asascience.utilities.io.ExportToKml;
import com.bbn.openmap.LatLonPoint;
import com.bbn.openmap.Layer;
import com.bbn.openmap.LayerHandler;
import com.bbn.openmap.dataAccess.shape.DbfTableModel;
import com.bbn.openmap.dataAccess.shape.EsriShapeExport;
import com.bbn.openmap.gui.BasicMapPanel;
import com.bbn.openmap.image.AbstractImageFormatter;
import com.bbn.openmap.image.SunJPEGFormatter;
import com.bbn.openmap.layer.OMGraphicHandlerLayer;
import com.bbn.openmap.omGraphics.OMGraphic;
import com.bbn.openmap.omGraphics.OMGraphicList;

/**
 * 
 * @author cmueller_mac
 */
public class OMExportUtilities {

	public static final int CANCELLED = -101;
	public static final int ERROR = -102;
	public static final int OK = 100;

	// /** Creates a new instance of OMExportUtilities */
	// public OMExportUtilities() {
	// }
	public static int mapToGeotiff(BasicMapPanel omPanel, String startOutDir) {
		try {

			String tifPath = showOutputChooser(omPanel.getTopLevelAncestor(), startOutDir, "mapout.tif",
				new GeoTiffFileFilter());
			if (tifPath == null) {
				return CANCELLED;
			}
			tifPath = (tifPath.endsWith(".tif")) ? tifPath : tifPath + ".tif";
			String tfwPath = tifPath.substring(0, tifPath.length() - 3) + "tfw";
			// File outImg = new File(Utils.appendSeparator(startOutDir) +
			// "outimg.tif");
			// File outTfw = new File(Utils.appendSeparator(startOutDir) +
			// "outimg.tfw");
			File outImg = new File(tifPath);
			File outTfw = new File(tfwPath);

			LatLonPoint llpUL = omPanel.getMapBean().getProjection().getUpperLeft();
			LatLonPoint llpLR = omPanel.getMapBean().getProjection().getLowerRight();
			double llx = llpUL.getLongitude();
			double lly = llpUL.getLatitude();
			double llx2 = llpLR.getLongitude();
			double lly2 = llpLR.getLatitude();
			// Point ulPoint =
			// omPanel.getMapBean().getProjection().forward(llpUL);
			Point lrPoint = omPanel.getMapBean().getProjection().forward(llpLR);
			double dx = llx - llx2;
			double dy = lly - lly2;
			// int w = omPanel.getWidth();
			// int h = omPanel.getHeight();
			double dpx = dx / lrPoint.getX();
			double dpy = dy / lrPoint.getY();
			// System.out.println(llx + " " + lly);
			// System.out.println(dpx + " " + dpy);

			// BufferedImage image =
			// (BufferedImage)omPanel.createImage((int)lrPoint.getX(),
			// (int)lrPoint.getY());

			AbstractImageFormatter aif = new SunJPEGFormatter();
			byte[] imgBytes = aif.getImageFromMapBean(omPanel.getMapBean());

			FileOutputStream binFile;
			FileWriter fw = null;
			BufferedWriter bw = null;
			try {
				/** Write the image file to disk */
				binFile = new FileOutputStream(outImg);
				binFile.write(imgBytes);
				binFile.close();
				/** Write the tfw file to disk */
				fw = new FileWriter(outTfw);
				bw = new BufferedWriter(fw);
				bw.write(String.valueOf(-dpx));
				bw.newLine();
				bw.write(String.valueOf(0.0d));
				bw.newLine();
				bw.write(String.valueOf(0.0d));
				bw.newLine();
				bw.write(String.valueOf(-dpy));
				bw.newLine();
				bw.write(String.valueOf(llx));
				bw.newLine();
				bw.write(String.valueOf(lly));
				bw.close();
				fw.close();
				bw = null;
				fw = null;
			} catch (FileNotFoundException ex) {
				Logger.getLogger(OMExportUtilities.class.getName()).log(Level.SEVERE, null, ex);
			} catch (IOException ex) {
				Logger.getLogger(OMExportUtilities.class.getName()).log(Level.SEVERE, null, ex);
			} finally {
				try {
					if (bw != null) {
						bw.close();
					}
					if (fw != null) {
						fw.close();
					}
				} catch (IOException ex) {
					Logger.getLogger(OMExportUtilities.class.getName()).log(Level.SEVERE, null, ex);
				}
			}
			return OK;
		} catch (Exception ex) {
			Logger.getLogger(OMExportUtilities.class.getName()).log(Level.SEVERE, null, ex);
		}
		return ERROR;
	}

	public static int layerToTemporalShapefile(BasicMapPanel omPanel, String startOutDir, ImageIcon icon) {
		try {
			TimeLayer exportLayer = (TimeLayer) getGraphicLayer(omPanel, icon, true, "Export Layer to Shapefile");
			if (exportLayer == null) {
				return CANCELLED;
			}
			String outPath = showOutputChooser(omPanel.getTopLevelAncestor(), startOutDir, exportLayer.getName(),
				new CustomFileFilter(new String[] { ".shp" }, "ESRI Shapefile"));
			if (outPath == null) {
				return CANCELLED;
			}
			// String outPath = Utils.appendSeparator(startOutDir) +
			// selLayer.getName();
			// System.out.println(outPath);
			SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
			sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
			Long[] times = exportLayer.getTimes();
			GregorianCalendar sc = exportLayer.getStartCal();
			OMGraphicList graphics = null;
			List<String> timeStrings = new ArrayList<String>();
			String tString;
			for (int t = 0; t < times.length; t++) {
				sc.setTimeInMillis(times[t]);
				tString = sdf.format(sc.getTime());
				exportLayer.advanceTime(times[t]);
				if (graphics == null) {
					graphics = (OMGraphicList) exportLayer.getList().clone();
					for (int i = 0; i < graphics.size(); i++) {
						timeStrings.add(tString);
					}
				} else {
					OMGraphicList temp = exportLayer.getList();
					for (int i = 0; i < temp.size(); i++) {
						graphics.add(temp.getOMGraphicAt(i));
						timeStrings.add(tString);
					}
				}
			}
			// OMGraphicList list = exportLayer.getList();
			EsriShapeExport shpExport = new EsriShapeExport(graphics, omPanel.getMapBean().getProjection(), outPath);
			OMGraphic g = graphics.getOMGraphicAt(0);
			ArrayList rec;

			if (g instanceof OMGridCell) {
				DbfTableModel model = new DbfTableModel(6);
				model.setDecimalCount(0, (byte) 0);
				model.setLength(0, (byte) 10);
				model.setColumnName(0, "Record");
				model.setType(0, (byte) DbfTableModel.TYPE_NUMERIC);

				model.setDecimalCount(1, (byte) 0);
				model.setLength(1, (byte) 10);
				model.setColumnName(1, "Desc");
				model.setType(1, (byte) DbfTableModel.TYPE_CHARACTER);

				model.setDecimalCount(2, (byte) 20);
				model.setLength(2, (byte) 30);
				model.setColumnName(2, "Center_Lat");
				model.setType(2, (byte) DbfTableModel.TYPE_NUMERIC);

				model.setDecimalCount(3, (byte) 20);
				model.setLength(3, (byte) 30);
				model.setColumnName(3, "Center_Lon");
				model.setType(3, (byte) DbfTableModel.TYPE_NUMERIC);

				model.setDecimalCount(4, (byte) 20);
				model.setLength(4, (byte) 30);
				model.setColumnName(4, "Data");
				model.setType(4, (byte) DbfTableModel.TYPE_NUMERIC);

				model.setDecimalCount(5, (byte) 0);
				model.setLength(5, (byte) 20);
				model.setColumnName(5, "DateTime");
				model.setType(5, (byte) DbfTableModel.TYPE_CHARACTER);

				OMGridCell gc;
				for (int i = 0; i < graphics.size(); i++) {
					gc = (OMGridCell) graphics.getOMGraphicAt(i);
					rec = new ArrayList();
					rec.add(i);
					rec.add("OMGridCell");
					rec.add(gc.getCenterLat());
					rec.add(gc.getCenterLon());
					rec.add(gc.getData());
					rec.add(timeStrings.get(i));

					model.addRecord(rec);
				}
				shpExport.setMasterDBF(model);
			} else if (g instanceof OMArrow) {
				DbfTableModel model = new DbfTableModel(8);
				model.setDecimalCount(0, (byte) 0);
				model.setLength(0, (byte) 10);
				model.setColumnName(0, "Record");
				model.setType(0, (byte) DbfTableModel.TYPE_NUMERIC);

				model.setDecimalCount(1, (byte) 0);
				model.setLength(1, (byte) 10);
				model.setColumnName(1, "Desc");
				model.setType(1, (byte) DbfTableModel.TYPE_CHARACTER);

				model.setDecimalCount(2, (byte) 20);
				model.setLength(2, (byte) 30);
				model.setColumnName(2, "Center_Lat");
				model.setType(2, (byte) DbfTableModel.TYPE_NUMERIC);

				model.setDecimalCount(3, (byte) 20);
				model.setLength(3, (byte) 30);
				model.setColumnName(3, "Center_Lon");
				model.setType(3, (byte) DbfTableModel.TYPE_NUMERIC);

				model.setDecimalCount(4, (byte) 20);
				model.setLength(4, (byte) 30);
				model.setColumnName(4, "U_Data");
				model.setType(4, (byte) DbfTableModel.TYPE_NUMERIC);

				model.setDecimalCount(5, (byte) 20);
				model.setLength(5, (byte) 30);
				model.setColumnName(5, "V_Data");
				model.setType(5, (byte) DbfTableModel.TYPE_NUMERIC);

				model.setDecimalCount(6, (byte) 20);
				model.setLength(6, (byte) 30);
				model.setColumnName(6, "Speed");
				model.setType(6, (byte) DbfTableModel.TYPE_NUMERIC);

				model.setDecimalCount(7, (byte) 20);
				model.setLength(7, (byte) 30);
				model.setColumnName(7, "Direction");
				model.setType(7, (byte) DbfTableModel.TYPE_NUMERIC);

				OMArrow ga;
				for (int i = 0; i < graphics.size(); i++) {
					ga = (OMArrow) graphics.getOMGraphicAt(i);
					rec = new ArrayList();
					rec.add(i);
					rec.add("OMArrow");
					rec.add(ga.getVectorStartLat());
					rec.add(ga.getVectorStartLon());
					rec.add(ga.getU());
					rec.add(ga.getV());
					rec.add(ga.getSpeed());
					rec.add(ga.getDir());

					model.addRecord(rec);
				}
				shpExport.setMasterDBF(model);
			} else if (g instanceof OMParticle) {
				DbfTableModel model = new DbfTableModel(10);
				model.setDecimalCount(0, (byte) 0);
				model.setLength(0, (byte) 10);
				model.setColumnName(0, "Record");
				model.setType(0, (byte) DbfTableModel.TYPE_NUMERIC);

				model.setDecimalCount(1, (byte) 0);
				model.setLength(1, (byte) 10);
				model.setColumnName(1, "Desc");
				model.setType(1, (byte) DbfTableModel.TYPE_CHARACTER);

				model.setDecimalCount(2, (byte) 8);
				model.setLength(2, (byte) 10);
				model.setColumnName(2, "Particle_ID");
				model.setType(2, (byte) DbfTableModel.TYPE_NUMERIC);

				model.setDecimalCount(3, (byte) 20);
				model.setLength(3, (byte) 30);
				model.setColumnName(3, "Loc_X");
				model.setType(3, (byte) DbfTableModel.TYPE_NUMERIC);

				model.setDecimalCount(4, (byte) 20);
				model.setLength(4, (byte) 30);
				model.setColumnName(4, "Loc_Y");
				model.setType(4, (byte) DbfTableModel.TYPE_NUMERIC);

				model.setDecimalCount(5, (byte) 20);
				model.setLength(5, (byte) 30);
				model.setColumnName(5, "Loc_Z");
				model.setType(5, (byte) DbfTableModel.TYPE_NUMERIC);

				model.setDecimalCount(6, (byte) 20);
				model.setLength(6, (byte) 30);
				model.setColumnName(6, "Radius");
				model.setType(6, (byte) DbfTableModel.TYPE_NUMERIC);

				model.setDecimalCount(7, (byte) 20);
				model.setLength(7, (byte) 30);
				model.setColumnName(7, "pH");
				model.setType(7, (byte) DbfTableModel.TYPE_NUMERIC);

				model.setDecimalCount(8, (byte) 20);
				model.setLength(8, (byte) 30);
				model.setColumnName(8, "Center_Conc");
				model.setType(8, (byte) DbfTableModel.TYPE_NUMERIC);

				model.setDecimalCount(9, (byte) 20);
				model.setLength(9, (byte) 30);
				model.setColumnName(9, "Edge_Conc");
				model.setType(9, (byte) DbfTableModel.TYPE_NUMERIC);

				OMParticle gp;
				for (int i = 0; i < graphics.size(); i++) {
					gp = (OMParticle) graphics.getOMGraphicAt(i);
					rec = new ArrayList();
					rec.add(i);
					rec.add("OMParticle");
					rec.add(gp.getParticleID());
					rec.add(gp.getParticleLocationX());
					rec.add(gp.getParticleLocationY());
					rec.add(gp.getParticleLocationZ());
					rec.add(gp.getRealRadius());
					rec.add(gp.getPH());
					rec.add(gp.getCenterConc());
					rec.add(gp.getEdgeConc());

					model.addRecord(rec);
				}
				shpExport.setMasterDBF(model);
			} else {
				// continue on and export using the "default" provided by
				// openmap
			}

			shpExport.export();
			return OK;
		} catch (Exception ex) {
			Logger.getLogger(OMExportUtilities.class.getName()).log(Level.SEVERE, null, ex);
		}
		return ERROR;
	}

	public static int layerToShapefile(BasicMapPanel omPanel, String startOutDir, ImageIcon icon) {
		try {
			OMGraphicHandlerLayer exportLayer = (OMGraphicHandlerLayer) getGraphicLayer(omPanel, icon, true,
				"Export Layer to Shapefile");
			if (exportLayer == null) {
				return CANCELLED;
			}
			String outPath = showOutputChooser(omPanel.getTopLevelAncestor(), startOutDir, exportLayer.getName(),
				new CustomFileFilter(new String[] { ".shp" }, "ESRI Shapefile"));
			if (outPath == null) {
				return CANCELLED;
			}
			// String outPath = Utils.appendSeparator(startOutDir) +
			// selLayer.getName();
			// System.out.println(outPath);

			OMGraphicList list = exportLayer.getList();
			EsriShapeExport shpExport = new EsriShapeExport(list, omPanel.getMapBean().getProjection(), outPath);
			OMGraphic g = list.getOMGraphicAt(0);
			ArrayList rec;

			if (g instanceof OMGridCell) {
				DbfTableModel model = new DbfTableModel(5);
				model.setDecimalCount(0, (byte) 0);
				model.setLength(0, (byte) 10);
				model.setColumnName(0, "Record");
				model.setType(0, (byte) DbfTableModel.TYPE_NUMERIC);

				model.setDecimalCount(1, (byte) 0);
				model.setLength(1, (byte) 10);
				model.setColumnName(1, "Desc");
				model.setType(1, (byte) DbfTableModel.TYPE_CHARACTER);

				model.setDecimalCount(2, (byte) 20);
				model.setLength(2, (byte) 30);
				model.setColumnName(2, "Center_Lat");
				model.setType(2, (byte) DbfTableModel.TYPE_NUMERIC);

				model.setDecimalCount(3, (byte) 20);
				model.setLength(3, (byte) 30);
				model.setColumnName(3, "Center_Lon");
				model.setType(3, (byte) DbfTableModel.TYPE_NUMERIC);

				model.setDecimalCount(4, (byte) 20);
				model.setLength(4, (byte) 30);
				model.setColumnName(4, "Data");
				model.setType(4, (byte) DbfTableModel.TYPE_NUMERIC);

				OMGridCell gc;
				for (int i = 0; i < list.size(); i++) {
					gc = (OMGridCell) list.getOMGraphicAt(i);
					rec = new ArrayList();
					rec.add(i);
					rec.add("OMGridCell");
					rec.add(gc.getCenterLat());
					rec.add(gc.getCenterLon());
					rec.add(gc.getData());

					model.addRecord(rec);
				}
				shpExport.setMasterDBF(model);
			} else if (g instanceof OMArrow) {
				DbfTableModel model = new DbfTableModel(8);
				model.setDecimalCount(0, (byte) 0);
				model.setLength(0, (byte) 10);
				model.setColumnName(0, "Record");
				model.setType(0, (byte) DbfTableModel.TYPE_NUMERIC);

				model.setDecimalCount(1, (byte) 0);
				model.setLength(1, (byte) 10);
				model.setColumnName(1, "Desc");
				model.setType(1, (byte) DbfTableModel.TYPE_CHARACTER);

				model.setDecimalCount(2, (byte) 20);
				model.setLength(2, (byte) 30);
				model.setColumnName(2, "Center_Lat");
				model.setType(2, (byte) DbfTableModel.TYPE_NUMERIC);

				model.setDecimalCount(3, (byte) 20);
				model.setLength(3, (byte) 30);
				model.setColumnName(3, "Center_Lon");
				model.setType(3, (byte) DbfTableModel.TYPE_NUMERIC);

				model.setDecimalCount(4, (byte) 20);
				model.setLength(4, (byte) 30);
				model.setColumnName(4, "U_Data");
				model.setType(4, (byte) DbfTableModel.TYPE_NUMERIC);

				model.setDecimalCount(5, (byte) 20);
				model.setLength(5, (byte) 30);
				model.setColumnName(5, "V_Data");
				model.setType(5, (byte) DbfTableModel.TYPE_NUMERIC);

				model.setDecimalCount(6, (byte) 20);
				model.setLength(6, (byte) 30);
				model.setColumnName(6, "Speed");
				model.setType(6, (byte) DbfTableModel.TYPE_NUMERIC);

				model.setDecimalCount(7, (byte) 20);
				model.setLength(7, (byte) 30);
				model.setColumnName(7, "Direction");
				model.setType(7, (byte) DbfTableModel.TYPE_NUMERIC);

				OMArrow ga;
				for (int i = 0; i < list.size(); i++) {
					ga = (OMArrow) list.getOMGraphicAt(i);
					rec = new ArrayList();
					rec.add(i);
					rec.add("OMArrow");
					rec.add(ga.getVectorStartLat());
					rec.add(ga.getVectorStartLon());
					rec.add(ga.getU());
					rec.add(ga.getV());
					rec.add(ga.getSpeed());
					rec.add(ga.getDir());

					model.addRecord(rec);
				}
				shpExport.setMasterDBF(model);
			} else if (g instanceof OMParticle) {
				DbfTableModel model = new DbfTableModel(10);
				model.setDecimalCount(0, (byte) 0);
				model.setLength(0, (byte) 10);
				model.setColumnName(0, "Record");
				model.setType(0, (byte) DbfTableModel.TYPE_NUMERIC);

				model.setDecimalCount(1, (byte) 0);
				model.setLength(1, (byte) 10);
				model.setColumnName(1, "Desc");
				model.setType(1, (byte) DbfTableModel.TYPE_CHARACTER);

				model.setDecimalCount(2, (byte) 8);
				model.setLength(2, (byte) 10);
				model.setColumnName(2, "Particle_ID");
				model.setType(2, (byte) DbfTableModel.TYPE_NUMERIC);

				model.setDecimalCount(3, (byte) 20);
				model.setLength(3, (byte) 30);
				model.setColumnName(3, "Loc_X");
				model.setType(3, (byte) DbfTableModel.TYPE_NUMERIC);

				model.setDecimalCount(4, (byte) 20);
				model.setLength(4, (byte) 30);
				model.setColumnName(4, "Loc_Y");
				model.setType(4, (byte) DbfTableModel.TYPE_NUMERIC);

				model.setDecimalCount(5, (byte) 20);
				model.setLength(5, (byte) 30);
				model.setColumnName(5, "Loc_Z");
				model.setType(5, (byte) DbfTableModel.TYPE_NUMERIC);

				model.setDecimalCount(6, (byte) 20);
				model.setLength(6, (byte) 30);
				model.setColumnName(6, "Radius");
				model.setType(6, (byte) DbfTableModel.TYPE_NUMERIC);

				model.setDecimalCount(7, (byte) 20);
				model.setLength(7, (byte) 30);
				model.setColumnName(7, "pH");
				model.setType(7, (byte) DbfTableModel.TYPE_NUMERIC);

				model.setDecimalCount(8, (byte) 20);
				model.setLength(8, (byte) 30);
				model.setColumnName(8, "Center_Conc");
				model.setType(8, (byte) DbfTableModel.TYPE_NUMERIC);

				model.setDecimalCount(9, (byte) 20);
				model.setLength(9, (byte) 30);
				model.setColumnName(9, "Edge_Conc");
				model.setType(9, (byte) DbfTableModel.TYPE_NUMERIC);

				OMParticle gp;
				for (int i = 0; i < list.size(); i++) {
					gp = (OMParticle) list.getOMGraphicAt(i);
					rec = new ArrayList();
					rec.add(i);
					rec.add("OMParticle");
					rec.add(gp.getParticleID());
					rec.add(gp.getParticleLocationX());
					rec.add(gp.getParticleLocationY());
					rec.add(gp.getParticleLocationZ());
					rec.add(gp.getRealRadius());
					rec.add(gp.getPH());
					rec.add(gp.getCenterConc());
					rec.add(gp.getEdgeConc());

					model.addRecord(rec);
				}
				shpExport.setMasterDBF(model);
			} else {
				// continue on and export using the "default" provided by
				// openmap
			}

			shpExport.export();
			return OK;
		} catch (Exception ex) {
			Logger.getLogger(OMExportUtilities.class.getName()).log(Level.SEVERE, null, ex);
		}
		return ERROR;
	}

	public static int layerToKml(BasicMapPanel omPanel, String startOutDir, ImageIcon icon) {
		try {
			TimeLayer exportLayer = (TimeLayer) getGraphicLayer(omPanel, icon, true, "Export Layer to KML");
			if (exportLayer == null) {
				return CANCELLED;
			}
			String outPath = showOutputChooser(omPanel.getTopLevelAncestor(), startOutDir, exportLayer.getName(),
				new CustomFileFilter(new String[] { ".kml" }, "KML File"));
			if (outPath == null) {
				return CANCELLED;
			}

			OMGraphicList graphics;
			OMGraphic g;
			OMGridCell gc;
			OMParticle p;
			double data;
			Color fCol, lCol;
			float[] lats, lons;
			float lat, lon;
			Long[] times = exportLayer.getTimes();
			int increment = new Long(exportLayer.getTimeIncrement()).intValue();
			GregorianCalendar sc = exportLayer.getStartCal();
			ExportToKml etk = new ExportToKml(outPath, exportLayer.getName(), "SHARC Output Layer");
			for (int t = 0; t < times.length; t++) {
				sc.setTimeInMillis(times[t]);
				exportLayer.advanceTime(times[t]);
				graphics = exportLayer.getList();
				for (int i = 0; i < graphics.size(); i++) {
					g = graphics.getOMGraphicAt(i);
					if (g instanceof OMGridCell) {
						gc = (OMGridCell) g;
						data = gc.getData();
						fCol = gc.getFillColor();
						lCol = gc.getLineColor();
						lats = gc.getLats();
						lons = gc.getLons();
						etk.addPolygon(String.valueOf(i), "", lats, lons, (GregorianCalendar) sc.clone(), increment,
							etk.addStyle(fCol, lCol), data);
					} else if (g instanceof OMParticle) {
						p = (OMParticle) g;
						data = p.getCenterConc();
						fCol = p.getFillColor();
						lCol = p.getLineColor();
						lat = p.getLat();
						lon = p.getLon();
						etk.addPoint(String.valueOf(i), lat, lon, (GregorianCalendar) sc.clone(), increment, etk
							.addStyle(fCol, lCol), data);
					}
				}
			}
			etk.finish(outPath);

			return OK;
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return ERROR;
	}

	private static Layer getGraphicLayer(BasicMapPanel omPanel, ImageIcon icon, boolean timeOnly, String title)
		throws Exception {
		Layer[] layers = ((LayerHandler) omPanel.getMapHandler().get(LayerHandler.class)).getLayers();
		List<Layer> graphicLayers = new ArrayList<Layer>();
		List<String> layerNames = new ArrayList<String>();
		for (int i = 0; i < layers.length; i++) {
			if (timeOnly) {
				if (layers[i] instanceof TimeLayer) {
					layerNames.add(layers[i].getName());
					graphicLayers.add((TimeLayer) layers[i]);
				}
			} else {
				if (layers[i] instanceof OMGraphicHandlerLayer) {
					layerNames.add(layers[i].getName());
					graphicLayers.add((OMGraphicHandlerLayer) layers[i]);
				}
			}
		}
		String name = (String) JOptionPane.showInputDialog(omPanel.getTopLevelAncestor(),
			"Please select a layer for export:", title, JOptionPane.PLAIN_MESSAGE, icon, layerNames.toArray(),
			layerNames.get(0));
		if (name == null) {
			return null;
		}
		return (OMGraphicHandlerLayer) graphicLayers.get(layerNames.indexOf(name));
	}

	private static String showOutputChooser(Component comp, String startOutDir, String defaultName, FileFilter filter) {
		JFileChooser c = new JFileChooser();
		c.setAcceptAllFileFilterUsed(false);
		c.setFileFilter(filter);
		c.setMultiSelectionEnabled(false);
		c.setCurrentDirectory(new File(startOutDir));
		c.setSelectedFile(new File(Utils.appendSeparator(startOutDir) + defaultName));
		int ret = c.showSaveDialog(comp);
		if (ret == JFileChooser.APPROVE_OPTION) {
			return c.getSelectedFile().getAbsolutePath();
		}

		return null;
	}
}
