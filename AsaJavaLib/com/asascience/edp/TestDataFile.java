/*
 * TestDataFile.java
 *
 * Created on December 5, 2007, 10:24 AM
 *
 * Applied Science Associates, Inc.
 * Copyright 2007.  All rights reserved.
 */

package com.asascience.edp;

import java.io.File;

import com.asascience.edp.datafile.hydro.DataFileBase;
import com.asascience.edp.datafile.hydro.DataFileClassLoader;

/**
 * 
 * @author CBM
 */
public class TestDataFile {
	private String dataFileType = "DataFileCix";
	private String dataFileLoc = "data" + File.separator + "cixtest.cix";

	/** Creates a new instance of TestDataFile */
	public TestDataFile() {
		DataFileClassLoader dfcl = new DataFileClassLoader();
		try {
			DataFileBase dataFile = dfcl.getDataFileInstance(dataFileType);
			dataFile.setDataFile(dataFileLoc);
		} catch (IllegalAccessException ex) {
			ex.printStackTrace();
		} catch (InstantiationException ex) {
			ex.printStackTrace();
		}
	}

	public static void main(String[] args) {
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				new TestDataFile();
			}
		});
	}
}
