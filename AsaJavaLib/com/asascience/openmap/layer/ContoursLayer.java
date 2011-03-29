package com.asascience.openmap.layer;

/*
 * ContoursLayer.java
 *
 * Created on July 20, 2007, 9:25 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

import java.awt.Color;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;
import java.util.logging.Logger;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

import com.bbn.openmap.dataAccess.shape.DbfTableModel;
import com.bbn.openmap.dataAccess.shape.EsriGraphicList;
import com.bbn.openmap.dataAccess.shape.input.ShpInputStream;
import com.bbn.openmap.event.MapMouseListener;
import com.bbn.openmap.event.SelectMouseMode;
import com.bbn.openmap.layer.OMGraphicHandlerLayer;
import com.bbn.openmap.omGraphics.OMGraphic;
import com.bbn.openmap.omGraphics.OMGraphicList;
import com.bbn.openmap.omGraphics.OMLine;
import com.bbn.openmap.omGraphics.OMPoly;
import com.bbn.openmap.proj.Projection;

/**
 * 
 * @author cmueller
 */
public class ContoursLayer extends OMGraphicHandlerLayer implements MapMouseListener {

	private static Logger logger = Logger.getLogger("ContoursLayer");

	// <editor-fold defaultstate="collapsed" desc="Enums">
	public enum ScenarioTags {
		id, info, timestep, polygon, vertex, fillcolor, selcolor
	}

	// </editor-fold>

	private OMGraphicList omgraphics;
	private Projection projection;
	private OMGraphic selectedGraphic;
	private DbfTableModel dbfTable;

	/**
	 * Creates a new instance of ContoursLayer
	 */
	public ContoursLayer() {
		omgraphics = new OMGraphicList();
		this.setList(omgraphics);
	}

	public ArrayList loadScenarioFromXML(File xmlScenario) {
		ArrayList al = null;
		try {
			al = new ArrayList();

			Document doc = new Document();
			SAXBuilder in = new SAXBuilder();
			doc = in.build(xmlScenario);
			Element root = doc.getRootElement();
			Element t1 = root.getChild(ScenarioTags.timestep.toString());
			Iterator iter = t1.getChildren().iterator();

			ArrayList poly;
			ArrayList points;
			Element ele;
			Color fillColor = Color.BLACK;
			Color selColor = Color.BLACK;
			float[] llPoints;
			String shapeInfo;
			Iterator polyIter;

			while (iter.hasNext()) {
				poly = new ArrayList();
				points = new ArrayList();
				ele = (Element) iter.next();// a polygon element
				shapeInfo = ele.getAttributeValue(ScenarioTags.info.toString());
				polyIter = ele.getChildren().iterator();
				while (polyIter.hasNext()) {
					ele = (Element) polyIter.next();
					if (ele.getName().equalsIgnoreCase(ScenarioTags.fillcolor.toString())) {
						fillColor = Color.decode(ele.getText());
					} else if (ele.getName().equalsIgnoreCase(ScenarioTags.selcolor.toString())) {
						selColor = Color.decode(ele.getText());
					} else if (ele.getName().equalsIgnoreCase(ScenarioTags.vertex.toString())) {
						points.add(ele.getAttribute("x").getValue());
						points.add(ele.getAttribute("y").getValue());
					}
				}

				llPoints = new float[points.size() + 2];
				Object[] o = points.toArray();
				for (int i = 0; i < o.length; i++) {
					llPoints[i] = (Float.valueOf((String) o[i])).floatValue();
				}
				// add the first lat/lon pair to the end of the set
				llPoints[llPoints.length - 2] = llPoints[0];
				llPoints[llPoints.length - 1] = llPoints[1];

				// String s = new String();
				// for(int h = 0;h<llPoints.length;h++){
				// s += String.valueOf(llPoints[h]) + "\t";
				// }
				// logger.info(s);

				poly.add(llPoints);
				poly.add(fillColor);
				poly.add(selColor);
				poly.add(shapeInfo);
				al.add(poly);
			}

		} catch (JDOMException ex) {
			logger.warning(ex.getMessage());
		} catch (IOException ex) {
			logger.warning(ex.getMessage());
		}
		return al;
	}

	public Boolean setShapefile(File shapefilePath) {
		try {
			omgraphics.clear();
			FileInputStream fis = new FileInputStream(shapefilePath);
			ShpInputStream sis = new ShpInputStream(fis);
			EsriGraphicList egl = sis.getGeometry();
			Iterator eglIter = egl.iterator();
			OMGraphic gr;
			Random r = new Random();
			while (eglIter.hasNext()) {
				gr = (OMGraphic) eglIter.next();
				gr.setLinePaint(Color.BLACK);
				gr.setFillPaint(Color.getHSBColor(r.nextFloat(), 1.0F, 1.0F));
				gr.setSelectPaint(Color.YELLOW);
			}

			omgraphics = (OMGraphicList) egl;
			return true;
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return false;
	}

	public boolean generatePolygons(ArrayList arrayList) {
		try {
			// Iterator alIter = arrayList.iterator();
			OMPoly poly;
			float[] llPoints;
			Color fillColor;
			Color selColor;
			String shapeInfo;
			ArrayList polyAl;

			this.getList().clear();

			for (int i = 0; i < arrayList.size(); i++) {
				polyAl = (ArrayList) arrayList.get(i);
				llPoints = (float[]) polyAl.get(0);
				fillColor = (Color) polyAl.get(1);
				selColor = (Color) polyAl.get(2);
				shapeInfo = (String) polyAl.get(3);
				poly = this.createPoly(llPoints, fillColor, selColor);
				if (poly != null) {
					poly.setAppObject(shapeInfo);
					this.getList().addOMGraphic(poly);
				}
			}
			this.doPrepare();
			return true;
		} catch (Exception ex) {
			logger.warning("Error generating polygons.\n" + ex.getMessage());
		}
		return false;
	}

	public OMPoly createPoly(float[] llPoints, Color fillCol, Color selCol) {
		OMPoly poly = new OMPoly(llPoints, OMGraphic.DECIMAL_DEGREES, OMGraphic.LINETYPE_RHUMB);
		poly.setFillPaint(fillCol);
		poly.setIsPolygon(true);
		poly.setSelectPaint(selCol);
		// poly.generate(this.getProjection());

		return poly;
	}

	public OMLine createLine(float lat1, float lng1, float lat2, float lng2, int lineType, Color color, Color selColor) {
		OMLine line = new OMLine(lat1, lng1, lat2, lng2, lineType);
		line.setLinePaint(color);
		line.setSelectPaint(selColor);
		return line;
	}

	public OMGraphicList createGraphics(OMGraphicList graphics) {
		graphics.clear();

		graphics.addOMGraphic(createLine(42.0f, -71.0f, 35.5f, -120.5f, OMGraphic.LINETYPE_GREATCIRCLE, Color.red,
			Color.yellow));

		return graphics;
	}

	public MapMouseListener getMapMouseListener() {
		return this;
	}

	// <editor-fold defaultstate="collapsed"
	// desc="MapMouseListener Implementation">

	/**
	 * Indicates which mouse modes should send events to this <code>Layer</code>
	 * .
	 * 
	 * @return An array mouse mode names
	 * 
	 * @see com.bbn.openmap.event.MapMouseListener
	 * @see com.bbn.openmap.MouseDelegator
	 */
	public String[] getMouseModeServiceList() {
		String[] ret = new String[1];
		ret[0] = SelectMouseMode.modeID;
		return ret;
	}

	/**
	 * Called whenever the mouse is pressed by the user and one of the requested
	 * mouse modes is active.
	 * 
	 * @param e
	 *            the press event
	 * @return true if event was consumed(handled), false otherwise
	 * @see #getMouseModeServiceList
	 */
	public boolean mousePressed(MouseEvent e) {
		return false;
	}

	/**
	 * Called whenever the mouse is released by the user and one of the
	 * requested mouse modes is active.
	 * 
	 * @param e
	 *            the release event
	 * @return true if event was consumed (handled), false otherwise
	 * @see #getMouseModeServiceList
	 */
	public boolean mouseReleased(MouseEvent e) {
		return false;
	}

	/**
	 * Called whenever the mouse is clicked by the user and one of the requested
	 * mouse modes is active.
	 * 
	 * @param e
	 *            the click event
	 * @return true if event was consumed (handled), false otherwise
	 * @see #getMouseModeServiceList
	 */
	public boolean mouseClicked(MouseEvent e) {
		if (selectedGraphic != null) {
			String shapeInfo = (String) selectedGraphic.getAppObject();
			if (shapeInfo != null) {
				this.fireRequestInfoLine(shapeInfo);
			}
			return true;
		}

		return false;
	}

	/**
	 * Called whenever the mouse enters this layer and one of the requested
	 * mouse modes is active.
	 * 
	 * @param e
	 *            the enter event
	 * @see #getMouseModeServiceList
	 */
	public void mouseEntered(MouseEvent e) {
	}

	/**
	 * Called whenever the mouse exits this layer and one of the requested mouse
	 * modes is active.
	 * 
	 * @param e
	 *            the exit event
	 * @see #getMouseModeServiceList
	 */
	public void mouseExited(MouseEvent e) {
	}

	/**
	 * Called whenever the mouse is dragged on this layer and one of the
	 * requested mouse modes is active.
	 * 
	 * @param e
	 *            the drag event
	 * @return true if event was consumed (handled), false otherwise
	 * @see #getMouseModeServiceList
	 */
	public boolean mouseDragged(MouseEvent e) {
		return false;
	}

	/**
	 * Called whenever the mouse is moved on this layer and one of the requested
	 * mouse modes is active.
	 * <p>
	 * Tries to locate a graphic near the mouse, and if it is found, it is
	 * highlighted and the Layer is repainted to show the highlighting.
	 * 
	 * @param e
	 *            the move event
	 * @return true if event was consumed (handled), false otherwise
	 * @see #getMouseModeServiceList
	 */
	public boolean mouseMoved(MouseEvent e) {
		OMGraphic newSelGraphic = this.getList().selectClosest(e.getX(), e.getY(), 5.0f);
		if (newSelGraphic != selectedGraphic) {
			selectedGraphic = newSelGraphic;
			repaint();
		} else {
			this.fireRequestInfoLine("");
		}
		return true;
	}

	/**
	 * Called whenever the mouse is moved on this layer and one of the requested
	 * mouse modes is active, and the gesture is consumed by another active
	 * layer. We need to deselect anything that may be selected.
	 * 
	 * @see #getMouseModeServiceList
	 */
	public void mouseMoved() {
		this.getList().deselectAll();
		repaint();
	}
	// </editor-fold>
}
