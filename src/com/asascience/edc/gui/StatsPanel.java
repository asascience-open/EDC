/*
 * Applied Science Associates, Inc.
 * Copyright 2009. All Rights Reserved.
 *
 * StatsPanel.java
 *
 * Created on Feb 20, 2009 @ 11:52:13 AM
 */

package com.asascience.edc.gui;

import java.util.HashMap;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;

import net.miginfocom.swing.MigLayout;

import com.asascience.utilities.Utils;

/**
 * 
 * @author CBM <cmueller@asascience.com>
 */

public class StatsPanel extends JPanel {

	public HashMap<String, Double> stats;

	public StatsPanel(HashMap<String, Double> stats) {
		this.setLayout(new MigLayout("fill"));
		this.stats = stats;

		initComponents();
	}

	private void initComponents() {
		this.removeAll();
		int i = 1;
		if (stats != null) {
			for (String s : stats.keySet()) {
				if (i == 3) {
					this.add(buildLabel(s, stats.get(s)), "wrap");
					i = 1;
				} else {
					this.add(buildLabel(s, stats.get(s)), "split 3");
					i++;
				}
			}
		} else {
			this.add(buildLabel("No Statistics Availible", null));
		}
	}

	private JLabel buildLabel(String name, Double value) {
		JLabel lbl = new JLabel();
		lbl.setBorder(BorderFactory.createEtchedBorder());
		StringBuilder sb = new StringBuilder();
		sb.append(name);
		if (value != null) {
			sb.append(" = ");
			sb.append(String.valueOf(Utils.roundDouble(value, 5)));
		}
		lbl.setText(sb.toString());
		return lbl;
	}

	public void setStats(HashMap<String, Double> stats) {
		this.stats = stats;
		initComponents();
		repaint();
	}
}
