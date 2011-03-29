/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * LayerPropsUtils.java
 *
 * Created on Oct 14, 2008, 1:33:54 PM
 *
 */
package com.asascience.openmap.utilities;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.output.XMLOutputter;

/**
 * 
 * @author cmueller_mac
 */
public class LayerPropsUtils {

	public static final String LAYERS = "layers";
	public static final String VECTOR_LAYER = "vectorLayer";
	public static final String LAYER_NAME = "name";
	public static final String THINNING = "thinning";
	public static final String SIZE = "size";
	public static final String SCALE_BY_SPEED = "scaleBySpeed";
	public static final String USE_BLACK_OUTLINE = "useBlackOutline";
	public static final String MIN_VAL = "minVal";
	public static final String MID_VAL = "midVal";
	public static final String MAX_VAL = "maxVal";
	public static final String MIN_COL = "minCol";
	public static final String MID_COL = "midCol";
	public static final String MAX_COL = "maxCol";
	private Document layerProps = null;
	private String docPath;

	/** Creates a new instance of LayerPropsUtils */
	public LayerPropsUtils(String propsPath) {
		docPath = propsPath;
		if (new File(propsPath).exists()) {
			SAXBuilder in = new SAXBuilder();
			try {
				layerProps = in.build(propsPath);
			} catch (JDOMException ex) {
				Logger.getLogger(LayerPropsUtils.class.getName()).log(Level.SEVERE, null, ex);
			} catch (IOException ex) {
				Logger.getLogger(LayerPropsUtils.class.getName()).log(Level.SEVERE, null, ex);
			}
		} else {
			layerProps = new Document();
			layerProps.addContent(new Element(LayerPropsUtils.LAYERS));
		}
	}

	public Element getVectorLayerElement(String layerName) {
		try {
			for (Element e : (List<Element>) layerProps.getRootElement().getChildren(LayerPropsUtils.VECTOR_LAYER)) {
				String name = e.getAttribute(LayerPropsUtils.LAYER_NAME).getValue();
				if (name.toLowerCase().equals(layerName.toLowerCase())) {
					return e;
				}
			}
			return null;
		} catch (Exception ex) {
			Logger.getLogger(LayerPropsUtils.class.getName()).log(Level.SEVERE, null, ex);
		}
		return null;
	}

	public boolean addVectorLayerElement(Element e) {
		try {
			XMLOutputter out = new XMLOutputter();
			String inName = e.getAttributeValue(LayerPropsUtils.LAYER_NAME);
			if (inName == null || inName.equals("")) {
				return false;
			}
			Element layersElement = layerProps.getRootElement();
			/** Check to ensure the layer doesn't exist - remove it if it does */
			List els = layersElement.getChildren(LayerPropsUtils.VECTOR_LAYER);
			Iterator it = els.iterator();
			while (it.hasNext()) {
				Element ne = (Element) it.next();
				if (ne.getAttributeValue("name").toLowerCase().equals(inName.toLowerCase())) {
					it.remove();
					continue;
				}
			}
			layersElement.addContent(e);

			out.output(layerProps, new FileOutputStream(docPath));
			return true;
		} catch (IOException ex) {
			Logger.getLogger(LayerPropsUtils.class.getName()).log(Level.SEVERE, null, ex);
		} catch (Exception ex) {
			Logger.getLogger(LayerPropsUtils.class.getName()).log(Level.SEVERE, null, ex);
		}
		return false;
	}
}
