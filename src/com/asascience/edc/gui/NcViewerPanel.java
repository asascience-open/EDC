/*
 * NcViewerPanel.java
 *
 * Created on September 10, 2007, 11:35 AM
 *
 * Applied Science Associates, Inc.
 * Copyright 2007.  All rights reserved.
 */

package com.asascience.edc.gui;

import java.awt.BorderLayout;
import java.io.IOException;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import ucar.nc2.NetcdfFile;
import ucar.nc2.ui.DatasetViewer;
import ucar.util.prefs.PreferencesExt;

/**
 * 
 * @author CBM
 */
public class NcViewerPanel extends JPanel {
	private PreferencesExt mainPrefs;
	private JFrame mainFrame;
	private DatasetViewer dsViewer;
	private JScrollPane sp;
	private NetcdfFile ncfile;

	/**
	 * Creates a new instance of NcViewerPanel
	 * 
	 * @param prefs
	 * @param parentFrame
	 */
	public NcViewerPanel(ucar.util.prefs.PreferencesExt prefs, JFrame parentFrame) {
		this.mainPrefs = prefs;
		this.mainFrame = parentFrame;

		initComponents();
	}

	private void initComponents() {
		setLayout(new BorderLayout());
		dsViewer = new DatasetViewer(mainPrefs,null);

		sp = new JScrollPane(dsViewer);
		add(sp);
	}

	public void setDataset(NetcdfFile ncFile) {
		try {
			if (ncfile != null)
				ncfile.close();
			ncfile = null;
		} catch (IOException ioe) {
		}
		ncfile = ncFile;
		if (ncFile != null) {
			dsViewer.setDataset(ncfile);
			// setSelectedItem(ncFile.getLocation());
		}
	}
}
