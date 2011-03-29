/*
 * Applied Science Associates, Inc.
 * Copyright 2009. All Rights Reserved.
 *
 * AnalysisPrepDialog.java
 *
 * Created on Feb 23, 2009 @ 9:34:24 AM
 */

package com.asascience.edc.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

import net.miginfocom.swing.MigLayout;

import com.asascience.edc.particle.ParticleOutputLayer;
import com.asascience.ui.OptionDialogBase;
import com.asascience.utilities.filefilter.NcFileFilter;

/**
 * 
 * @author CBM <cmueller@asascience.com>
 */

public class AnalysisPrepDialog extends OptionDialogBase {

	private HashMap<String, ParticleOutputLayer> partLayers;
	private HashMap<String, String> dataLayers;
	private JComboBox cbPartLayers;
	private JComboBox cbDataLayers;
	private JTextField tfDataFile;
	private JButton btnDataBrowse;

	private JRadioButton rbDataLayer;
	private JRadioButton rbDataFile;

	public AnalysisPrepDialog() {
		this(new HashMap<String, ParticleOutputLayer>(), new HashMap<String, String>());
	}

	public AnalysisPrepDialog(HashMap<String, ParticleOutputLayer> partLayers, HashMap<String, String> dataLayers) {
		this.partLayers = partLayers;
		this.dataLayers = dataLayers;
		initComponents();
		if (!this.dataLayers.isEmpty()) {
			rbDataLayer.doClick();
		} else {
			rbDataFile.doClick();
		}
	}

	private void initComponents() {
		this.setTitle("Prepare Analysis");
		JPanel pnlPart = new JPanel(new MigLayout("fill"));
		TitledBorder bdrPart = BorderFactory.createTitledBorder("Particle Layer:");
		pnlPart.setBorder(bdrPart);

		cbPartLayers = new JComboBox();
		if (partLayers.isEmpty()) {
			cbPartLayers.setEnabled(false);
		} else {
			for (String s : partLayers.keySet()) {
				cbPartLayers.addItem(s);
			}
		}

		JPanel pnlData = new JPanel(new MigLayout("fill"));
		TitledBorder bdrData = BorderFactory.createTitledBorder("Data Layer:");
		pnlData.setBorder(bdrData);

		pnlPart.add(cbPartLayers, "growx");

		/** Set up data layer combobox. */
		cbDataLayers = new JComboBox();
		if (dataLayers.isEmpty()) {
			cbDataLayers.setEnabled(false);
		} else {
			for (String s : dataLayers.keySet()) {
				cbDataLayers.addItem(s);
			}
		}

		/** Set up data file browser. */
		tfDataFile = new JTextField();
		btnDataBrowse = new JButton("Browse");
		btnDataBrowse.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				JFileChooser jfc = new JFileChooser();
				jfc.setAcceptAllFileFilterUsed(false);
				jfc.setMultiSelectionEnabled(false);
				jfc.setApproveButtonText("Select");
				jfc.setFileFilter(new NcFileFilter());
				int resp = jfc.showOpenDialog(AnalysisPrepDialog.this);
				if (resp == JFileChooser.APPROVE_OPTION) {
					tfDataFile.setText(jfc.getSelectedFile().getAbsolutePath());
				} else {
					tfDataFile.setText("");
				}
				// jfc.setCurrentDirectory(new File(incidentDir));
			}

		});

		/** Set up radio buttons. */
		RBListener rbl = new RBListener();
		rbDataLayer = new JRadioButton("From Layer");
		rbDataLayer.addActionListener(rbl);
		rbDataFile = new JRadioButton("From File");
		rbDataFile.addActionListener(rbl);

		ButtonGroup buttonGroup = new ButtonGroup();
		buttonGroup.add(rbDataLayer);
		buttonGroup.add(rbDataFile);

		/** Add items to the pnlData. */
		pnlData.add(rbDataLayer);
		pnlData.add(cbDataLayers, "wrap, growx");
		pnlData.add(rbDataFile);
		pnlData.add(tfDataFile, "wmin 250, growx");
		pnlData.add(btnDataBrowse);

		/** Add the panels to the dialog. */
		this.add(pnlPart, "growx, wrap");
		this.add(pnlData, "growx, wrap");

		this.add(super.buttonPanel("Accept", "Cancel"), "south");
		if (partLayers == null) {
			super.setAcceptEnabled(false);
		}

		this.pack();
	}

	public ParticleOutputLayer getParticleLayer() {
		return partLayers.get(cbPartLayers.getSelectedItem());
	}

	public String getDataPath() {
		String ret;
		if (rbDataLayer.isSelected()) {
			ret = dataLayers.get(cbDataLayers.getSelectedItem());
		} else {
			ret = tfDataFile.getText();
		}

		return ret;
	}

	class RBListener implements ActionListener {

		public void actionPerformed(ActionEvent e) {
			boolean enableLayers;
			if (e.getSource() == rbDataLayer) {
				enableLayers = true;
			} else {
				enableLayers = false;
			}

			cbDataLayers.setEnabled(enableLayers);
			tfDataFile.setEnabled(!enableLayers);
			btnDataBrowse.setEnabled(!enableLayers);
		}

	}
}
