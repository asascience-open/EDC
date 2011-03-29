/*
 * Applied Science Associates, Inc.
 * Copyright 2009. All Rights Reserved.
 *
 * StatsChartFrame.java
 *
 * Created on Feb 20, 2009 @ 11:45:43 AM
 */

package com.asascience.edc.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.HashMap;
import java.util.List;

import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.WindowConstants;

import net.miginfocom.swing.MigLayout;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;

import com.asascience.utilities.Utils;

/**
 * 
 * @author CBM <cmueller@asascience.com>
 */

public class CustomChartFrame extends JFrame {

	private final PropertyChangeSupport propertyChangeSupport;

	/** The chart panel. */
	private ChartPanel chartPanel;
	private StatsPanel statsPanel;
	private JComboBox cbChartType;
	private JComboBox cbDataGrids;

	/**
	 * Creates a custom chart frame with statistics.
	 * 
	 * @param title
	 *            - the title of the frame
	 * @param chart
	 *            - the chart to place in the chart panel
	 * @param stats
	 *            - the statistics to display below the chart
	 * @param grids
	 *            - the available analysis grid names
	 */
	public CustomChartFrame(String title, JFreeChart chart, HashMap<String, Double> stats, List<String> grids) {
		this(title, chart, stats, grids, false);
	}

	/**
	 * Creates a custom chart frame with statistics.
	 * 
	 * @param title
	 *            - the title of the frame
	 * @param chart
	 *            - the chart to place in the chart panel
	 * @param stats
	 *            - the statistics to display below the chart
	 * @param grids
	 *            - the available analysis grid names
	 * @param scrollPane
	 *            - determines if the chart should be placed in a JScrollPane
	 */
	public CustomChartFrame(String title, JFreeChart chart, HashMap<String, Double> stats, List<String> grids,
		boolean scrollPane) {
		super(title);
		propertyChangeSupport = new PropertyChangeSupport(this);

		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		JPanel pnl = new JPanel(new MigLayout("fill"));

		pnl.add(new JLabel("Chart Type:"), "center, split 4");
		cbChartType = new JComboBox();
		cbChartType.addItem("Timeseries");
		cbChartType.addItem("Histogram");
		cbChartType.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				propertyChangeSupport.firePropertyChange("chartType", cbChartType.getSelectedIndex(), null);
			}

		});
		pnl.add(cbChartType);

		pnl.add(new JLabel("Data Grid:"));
		cbDataGrids = new JComboBox();
		for (String s : grids) {
			cbDataGrids.addItem(s);
		}
		cbDataGrids.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				propertyChangeSupport.firePropertyChange("gridChange", cbDataGrids.getSelectedItem().toString(), null);
			}

		});
		pnl.add(cbDataGrids, "wrap");

		this.chartPanel = new ChartPanel(chart);
		this.statsPanel = new StatsPanel(stats);
		pnl.add(this.chartPanel, "grow, wrap");
		pnl.add(this.statsPanel, "center");

		if (scrollPane) {
			setContentPane(new JScrollPane(pnl));
		} else {
			setContentPane(pnl);
		}
	}

	/**
	 * Returns the chart panel for the frame.
	 * 
	 * @return The chart panel.
	 */
	public ChartPanel getChartPanel() {
		return this.chartPanel;
	}

	/**
	 * Returns the stats panel for the frame.
	 * 
	 * @return The stats panel.
	 */
	public StatsPanel getStatsPanel() {
		return this.statsPanel;
	}

	/**
	 * Sets the selected analysis grid.
	 * 
	 */
	public void setSelectedGrid(String gridName) {
		if (Utils.comboBoxContains(cbDataGrids.getModel(), gridName)) {
			cbDataGrids.setSelectedItem(gridName);
		}
	}

	public void addPropertyChangeListener(PropertyChangeListener listener) {
		propertyChangeSupport.addPropertyChangeListener(listener);
	}

	public void removePropertyChangeListener(PropertyChangeListener listener) {
		propertyChangeSupport.removePropertyChangeListener(listener);
	}
}
