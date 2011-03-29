/*
 * Applied Science Associates, Inc.
 * Copyright 2009. All Rights Reserved.
 *
 * GenericGridLayer.java
 *
 * Created on Feb 12, 2009 @ 9:08:04 AM
 */

package com.asascience.openmap.layer.nc.grid;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.ArrayList;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ToolTipManager;
import javax.swing.plaf.basic.BasicComboBoxRenderer;

import net.miginfocom.swing.MigLayout;

import com.asascience.openmap.layer.VectorLayer;
import com.asascience.openmap.omgraphic.OMGridCell;
import com.asascience.openmap.utilities.MapUtils;
import com.asascience.utilities.Utils;

/**
 * 
 * @author CBM <cmueller@asascience.com>
 */

public class GenericGridLayer extends VectorLayer {

	private GenericGridReader gReader;
	private JComboBox jcbVars;

	public GenericGridLayer(String ncFile) {
		if (new File(ncFile).exists()) {
			gReader = new GenericGridReader(ncFile);
			this.setSourceFilePath(ncFile);

			// if(gReader.getGridDataset().getStartDate() != null) {
			this.setTimeRange(gReader.getStartTime(), gReader.getEndTime());
			this.setTimeIncrement(gReader.getTimeIncrement());
			this.setTimes(gReader.getTimes());
			// }
			this.setName(ncFile.substring(ncFile.lastIndexOf(File.separator) + 1));

			if (gReader.isHasUV()) {
				this.setVectorColor(Color.BLUE);
				uvUnits = gReader.getUvUnits();
			}
			this.lats = gReader.getFullLats();
			this.lons = gReader.getFullLons();

		}
	}

	public void drawDataForTime() {
		drawDataForTime(this.getCurrentTime());
	}

	@Override
	public void drawDataForTime(long t) {
		boolean display = false;
		if (drawGridCells) {
			double[] varDat = gReader.getScalarDataByName(t, 0, selGridVar);
			double[] minMax = Utils.minMaxDouble(varDat, gReader.getFillValue());
			Color[] colors = MapUtils.buildColorRamp(Color.RED, Color.GREEN, 10, 240);
			double[] divs = MapUtils.buildEqualDivisionVals(10, minMax[1], minMax[0]);
			//
			buildGridCells(varDat, colors, divs);
			// buildGridCells(null, null, null);
			display = true;
		}
		if (drawVectors) {
			us = gReader.getUVals(t, 0d);
			vs = gReader.getVVals(t, 0d);

			// us = nReader.getUVals(step, 0);
			// vs = nReader.getVVals(step, 0);
			if (us.length == 1 && Double.isNaN(us[0])) {
				// The layer has no data for the specified time
				// this.setVisible(false);
			} else {
				// draw the layer
				// this.setVisible(true);

				// this.display(us, vs);
				// this.display(fullLats, fullLons, us, vs);
				display = true;
			}
		}

		if (display) {
			this.display();
		} else {
			this.clearDisplay();
		}
	}

	@Override
	protected void buildGridCells(double[] dataList, Color[] colors, double[] divVals) {
		gridCells = new ArrayList<OMGridCell>();
		double[][] grdData;
		double[] grdx;
		double[] grdy;
		double data;
		boolean add;
		double[] uLats = gReader.getYValues();
		double[] uLons = gReader.getXValues();

		int d = 0;
		for (int i = 0; i < uLats.length; i++) {
			for (int j = 0; j < uLons.length; j++) {
				// make sure the data is valid
				try {
					if (dataList != null) {
						data = dataList[d];
					} else {
						data = Double.NaN;
					}
				} catch (Exception ex) {
					data = Double.NaN;
				}

				add = true;

				grdData = gReader.calculateGridCellAxes(uLats[i], uLons[j]);
				if (grdData == null) {
					add = false;
				}

				if (add) {
					grdy = grdData[0];
					grdx = grdData[1];
					Color cellColor = null;
					if (!Double.isNaN(data)) {
						for (int h = 0; h < divVals.length; h++) {
							if (data > divVals[h]) {
								cellColor = colors[h];
								break;
							}
						}
					}
					if (!Double.isNaN(data)) {
						gridCells.add(new OMGridCell(null, MapUtils.buildFloatPolygonArray(grdy, grdx), data,
							cellColor, false));
					}

				}

				d++;
			}
		}
	}

	/**
	 * 
	 * @return
	 */
	@Override
	public Component getGUI() {
		JPanel pnl = new JPanel(new MigLayout("fill"));
		if (gReader.isHasUV()) {
			pnl = (JPanel) super.getGUI();
		}
		if (gReader.isHasScalars()) {
			if (jcbVars == null) {
				// add the appropriate components for visualization of grid
				JPanel pnlGrid = new JPanel(new MigLayout("fill"));
				jcbVars = new JComboBox();
				jcbVars.setRenderer(new ComboToolTipRenderer(jcbVars));
				jcbVars.setActionCommand("jcbVars");
				if (gReader.isHasUV()) {
					jcbVars.addItem("Current Vectors");
				}
				for (String s : gReader.getScalarNames()) {
					jcbVars.addItem(s);
				}
				jcbVars.addActionListener(new GridActionListener());
				jcbVars.setSelectedIndex(0);
				pnlGrid.add(new JLabel("Variables:"), "wrap");
				pnlGrid.add(jcbVars);

				pnl.add(pnlGrid);
			}
		}
		return pnl;
	}

	class GridActionListener implements ActionListener {

		public void actionPerformed(ActionEvent e) {
			String name = e.getActionCommand();
			if (name.equals("jcbVars")) {
				String cmd = jcbVars.getSelectedItem().toString();
				/** get the appropriate data and draw the grid using that data */
				if (cmd.equals("Current Vectors")) {
					selGridVar = null;
					drawVectors = true;
					drawGridCells = false;
					/** Set the Tool Tip */
					jcbVars.setToolTipText("Vectors representing the U & V current components");
				} else {
					// //just draw the grids first...
					// buildGridCells(null);//works
					selGridVar = cmd;
					// buildGridCells(ncellReader.getScalarDataByName(cmd));
					drawGridCells = true;
					drawVectors = false;
					/** Set the Tool Tip */
					jcbVars.setToolTipText("Vectors representing the U & V current components");
				}
				/** Set the Tool Tip */
				String s = (cmd.equals("Current Vectors")) ? "Vectors representing the U & V current components"
					: gReader.getScalarDescriptionByName(cmd.toString());
				jcbVars.setToolTipText(s);

				drawDataForTime();

				// refreshDisplay();
			}
		}
	}

	class ComboToolTipRenderer extends BasicComboBoxRenderer {

		JComboBox combo;
		JList comboList;

		public ComboToolTipRenderer(JComboBox combo) {
			this.combo = combo;
		}

		public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected,
			boolean cellHasFocus) {
			super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
			if (comboList == null) {
				comboList = list;
				combo.addKeyListener(new KeyAdapter() {

					public void keyReleased(KeyEvent e) {
						if (e.getKeyCode() == KeyEvent.VK_DOWN || e.getKeyCode() == KeyEvent.VK_UP) {
							int x = 5;
							int y = comboList.indexToLocation(comboList.getSelectedIndex()).y;
							ToolTipManager.sharedInstance().mouseEntered(
								new MouseEvent(comboList, MouseEvent.MOUSE_ENTERED, 0, 0, x, y, 0, false));
							ToolTipManager.sharedInstance().mouseMoved(
								new MouseEvent(comboList, MouseEvent.MOUSE_MOVED, 0, 0, x, y, 0, false));
						}
					}
				});
			}
			if (isSelected) {
				String s = (value.toString().equals("Current Vectors")) ? "Vectors representing the U & V current components"
					: gReader.getScalarDescriptionByName(value.toString());
				list.setToolTipText(s);
			}
			return this;
		}
	}
}
