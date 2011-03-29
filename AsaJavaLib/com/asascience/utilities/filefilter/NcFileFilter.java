/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * NcFileFilter.java
 *
 * Created on Jul 25, 2008, 11:31:16 AM
 *
 */
package com.asascience.utilities.filefilter;

import java.io.File;

import javax.swing.filechooser.FileFilter;

import com.asascience.utilities.Utils;

/**
 * 
 * @author cmueller_mac
 */
public class NcFileFilter extends FileFilter {

	/** Creates a new instance of NcFileFilter */
	public NcFileFilter() {
	}

	public boolean accept(File f) {
		if (f.isDirectory()) {
			return true;
		}
		String ext = Utils.getExtension(f);
		if (ext != null) {
			if (ext.equalsIgnoreCase("nc")) {
				return true;
			}
		}
		return false;
	}

	public String getDescription() {
		return "NetCDF Files";
	}
}
