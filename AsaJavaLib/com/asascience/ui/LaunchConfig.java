/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * LaunchConfig.java
 *
 * Created on Jul 17, 2008, 4:28:55 PM
 *
 */
package com.asascience.ui;

import java.io.File;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;

import com.asascience.utilities.Utils;

/**
 * 
 * @author cmueller_mac
 */
public class LaunchConfig {

	public static String SYSTEM_DIR = null;
	public static String USER_DIR = null;

	public static boolean initialize() {
		try {
			String launchDir = Utils.appendSeparator(System.getProperty("user.dir"));
			if (new File(Utils.appendSeparator(launchDir + "System")).exists()) {
				launchDir = Utils.appendSeparator(launchDir + "System");
			}
			File f = new File(launchDir + "launchconfig.xml");
			if (!f.exists()) {
				return false;
			}

			SYSTEM_DIR = f.getParent();// obtain the startup directory

			SAXBuilder in = new SAXBuilder();
			Document xmlDoc = in.build(f);

			Element root = xmlDoc.getRootElement();

			if (root.getChild("USER_DIR") != null) {
				USER_DIR = root.getChildText("USER_DIR");
				File nf = new File(USER_DIR);
				if (nf.exists()) {
					if (nf.isDirectory()) {
						return true;
					}
				}
			}

			return false;
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return false;
	}
}
