/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * EdpFileLoader.java
 *
 * Created on May 6, 2008, 12:31:10 PM
 *
 */

package com.asascience.edp;

import com.asascience.edp.datafile.hydro.DataFileBase;
import com.asascience.edp.datafile.hydro.DataFileClassLoader;
import com.asascience.utilities.io.DetermineNcType;
import com.asascience.utilities.io.NcFileType;

/**
 * 
 * @author cmueller_mac
 */
public class EdpFileLoader {

	public static DataFileBase loadVectorDataFile(String fileLoc) {
		DataFileBase dfb = null;
		DataFileClassLoader dfcl = new DataFileClassLoader();
		// System.out.println("currFile = " + currFile);
		try {
			// Logic to check the datafile and determine
			// what kind of file we're trying to open (wind/current,
			// ncom/swafs/wix/etc).
			if (fileLoc.toLowerCase().endsWith(".nc")) {
				NcFileType ncType = DetermineNcType.determineFileType(fileLoc);
				if (ncType == NcFileType.SWAFS) {
					// System.out.println("SWAFS File");
					dfb = dfcl.getDataFileInstance("DataFileSwafs");
				} else if (ncType == NcFileType.NCOM) {
					// System.out.println("NCOM File");
					dfb = dfcl.getDataFileInstance("DataFileNcom");
				} else if (ncType == NcFileType.NCELL) {
					// System.out.println("NCELL File");
					dfb = dfcl.getDataFileInstance("DataFileNcell");
				}
			} else if (fileLoc.toLowerCase().endsWith(".cix")) {
				// System.out.println("CIX File");
				dfb = dfcl.getDataFileInstance("DataFileCix");
			}

			if (dfb != null) {
				dfb.setDataFile(fileLoc);
			}
		} catch (IllegalAccessException ex) {
			ex.printStackTrace();
		} catch (InstantiationException ex) {
			ex.printStackTrace();
		}

		return dfb;
	}
}
