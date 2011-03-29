/*
 * Applied Science Associates, Inc.
 * Copyright 2009. All Rights Reserved.
 *
 * NetcdfMap.java
 *
 * Created on Feb 10, 2009 @ 1:50:07 PM
 */

package com.asascience.openmap.layer.nc;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

import com.asascience.utilities.Utils;

/**
 * 
 * @author CBM <cmueller@asascience.com>
 */

public class NetcdfSupport {

	private static final String NCSUPPORT = "ncSupport";
	private static final String MAP = "map";
	private static final String VAR = "var";
	private static final String NAME = "name";
	private static final String ISNCELL = "isncell";

	private Document doc;
	private Element map;

	public NetcdfSupport(String fileLoc) throws JDOMException, IOException {
		if (NetcdfSupport.isNetcdfSupportFile(fileLoc)) {
			SAXBuilder in = new SAXBuilder();
			doc = in.build(fileLoc);
			Element root = doc.getRootElement();
			map = root.getChild(MAP);
		} else {
			createXml(fileLoc);
			writeXml();
		}
	}

	public static boolean isNetcdfSupportFile(String fileLoc) {
		boolean ret = false;
		try {
			File f = new File(fileLoc);
			if (f.exists()) {
				if (Utils.getExtension(f).equals("xml")) {
					SAXBuilder in = new SAXBuilder();
					Document d = in.build(f);
					if (d.getRootElement().getName().equals(NCSUPPORT)) {
						ret = true;
					}
				}
			}
		} catch (JDOMException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			ret = false;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			ret = false;
		}
		return ret;
	}

	public void setMapping(HashMap<String, String> mapping) {
		map.removeContent();
		for (String s : mapping.keySet()) {
			map.addContent(new Element(VAR).setAttribute("name", s).setText(mapping.get(s)));
		}
	}

	public void setIsNcell(boolean value) {
		map.addContent(new Element(ISNCELL).setText(String.valueOf(value)));
	}

	public HashMap<String, String> getMapping() {
		HashMap<String, String> ret = new HashMap<String, String>();
		Element e;
		for (Object o : map.getChildren(VAR)) {
			if (o instanceof Element) {
				e = (Element) o;
				ret.put(e.getAttributeValue(NAME), e.getText());
			}
		}
		return ret;
	}

	public void createXml(String fileLoc) {
		doc = new Document();
		doc.setBaseURI(fileLoc);
		Element root = new Element(NCSUPPORT);
		map = new Element(MAP);
		root.addContent(map);
		doc.setRootElement(root);
	}

	public void writeXml() throws FileNotFoundException, IOException {
		XMLOutputter out = new XMLOutputter();
		out.setFormat(Format.getPrettyFormat());
		out.output(doc, new FileOutputStream(doc.getBaseURI()));
	}
}
