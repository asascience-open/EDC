/*
 * Applied Science Associates, Inc.
 * Copyright 2008. All Rights Reserved.
 *
 * Configuration.java
 *
 * Created on Nov 18, 2008 @ 9:42:14 AM
 */

package com.asascience.aggregator;

import java.io.File;
import java.io.FileOutputStream;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import org.jdom.output.XMLOutputter;

/**
 * 
 * @author CBM <cmueller@asascience.com>
 */

public class Configuration {
	public static String HOME_DIR = "";

	// public static String DESTINATION_DIR = "";
	// public static String LOGFILE_LOCATION = "";

	public static boolean initialize(String xmlFile) {
		try {
			File f = new File(xmlFile);
			if (!f.exists()) {
				return false;
			}
			SAXBuilder in = new SAXBuilder();
			Document xmlDoc = in.build(f);

			Element root = xmlDoc.getRootElement();

			if (root.getChild("HOME_DIR") != null) {
				HOME_DIR = root.getChildText("HOME_DIR");
			}
			// if(root.getChild("DESTINATION_DIR") != null){
			// DESTINATION_DIR = root.getChildText("DESTINATION_DIR");
			// }
			// if(root.getChild("LOGFILE_LOCATION") != null){
			// LOGFILE_LOCATION = root.getChildText("LOGFILE_LOCATION");
			// if(LOGFILE_LOCATION.equals("")){
			// LOGFILE_LOCATION = "null";
			// }
			// }

			return true;
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return false;
	}

	public static void writeProperties() {
		System.out.println("Configuration initialization:");
		System.out.println("  HOME_DIR: " + HOME_DIR);
		// System.out.println("  DESTINATION_DIR: " + DESTINATION_DIR);
		// System.out.println("  LOGFILE_LOCATION: " + LOGFILE_LOCATION);
		System.out.println();
	}

	public static boolean writeXml(String xmlLoc, String sourceLoc, String destLoc, String logLoc) {
		try {
			Element root = new Element("configurataion");

			Element source = new Element("HOME_DIR");
			source.setText(sourceLoc);
			// Element dest = new Element("DESTINATION_DIR");
			// dest.setText(destLoc);
			// Element log = new Element("LOGFILE_LOCATION");
			// log.setText(logLoc);

			root.addContent(source);
			// root.addContent(dest);
			// root.addContent(log);

			Document doc = new Document();
			doc.setRootElement(root);

			XMLOutputter out = new XMLOutputter();
			out.output(doc, new FileOutputStream(xmlLoc));

			return true;
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return false;
	}
}
