/*
 * Applied Science Associates, Inc.
 * Copyright 2007. All rights reserved.
 */

/*
 * OilmapSelectionPanel.java
 *
 * Created on Jan 1, 2008, 12:00:00 AM
 *
 */

package com.asascience.edc.gui;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import net.miginfocom.swing.MigLayout;
import ucar.nc2.dataset.CoordinateAxis1D;
import ucar.nc2.dt.GridCoordSystem;
import ucar.nc2.dt.grid.GeoGrid;

import com.asascience.edc.nc.NetcdfConstraints;
import com.asascience.ui.CheckBoxList;
import com.asascience.utilities.Utils;

/**
 * 
 * @author CBM <cmueller@asascience.com>
 */
public class OilmapSelectionPanel extends SelectionPanelBase {

	private int minLevel = 0;
	private int maxLevel = 0;
	private int surfLevel = 0;
	private JRadioButton rbSurfAt0;
	private JRadioButton rbSurfAtN;
	private JCheckBox cbIsSpdDir;
	private JCheckBox cbIsScalar;
	private JComboBox cbUVar;
	private JComboBox cbVVar;
	private JLabel lblUVar;
	private JLabel lblVVar;
	private JLabel btnWarn;
	private boolean wantsScalars;
	private JPanel optionsPanel;
	private boolean gridsCompatible;

	private StringBuilder timeComp;
	private StringBuilder vertComp;

	private boolean rbEnableState;
	private boolean ckEnableState;
	private boolean cbEnableState;

	public OilmapSelectionPanel(NetcdfConstraints cons, SubsetProcessPanel parent) {
		this("", cons, parent);
	}

	public OilmapSelectionPanel(String borderTitle, NetcdfConstraints cons, SubsetProcessPanel parent) {
		super(borderTitle, cons, parent);
		super.setPanelType(SelectionPanelBase.OILMAP);

		setUseAllLevels(true);
		createPanel();
	}

	/**
	 * Builds the panel and initializes the various components.
	 */
	@Override
	public void createPanel() {
		super.createPanel();
		optionsPanel = optionsPanel();
		this.add(optionsPanel, BorderLayout.SOUTH);
	}

	private JPanel scalarsPanel() {
		JPanel sPanel = new JPanel(new MigLayout("fill"));
		return sPanel;
	}

	private JPanel optionsPanel() {
		JPanel optPanel = new JPanel(new MigLayout("fill"));
		optPanel.setBorder(BorderFactory.createTitledBorder("Options:"));

		cbIsScalar = new JCheckBox("Get Scalars");
		cbIsScalar.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				if (cbIsScalar.isSelected()) {
					rbEnableState = rbSurfAt0.isEnabled();
					ckEnableState = cbIsSpdDir.isEnabled();
					cbEnableState = cbUVar.isEnabled();

					wantsScalars = true;

					cbIsSpdDir.setEnabled(false);
					cbUVar.setEnabled(false);
					cbVVar.setEnabled(false);
					rbSurfAt0.setEnabled(false);
					rbSurfAtN.setEnabled(false);
				} else {
					wantsScalars = false;

					cbIsSpdDir.setEnabled(ckEnableState);
					rbSurfAt0.setEnabled(rbEnableState);
					rbSurfAtN.setEnabled(rbEnableState);
					if (getCblVars().getSelItemsSize() > 1) {
						cbEnableState = true;
						if (cbUVar.getSelectedIndex() == cbVVar.getSelectedIndex()) {
							cbUVar.setSelectedIndex(0);
							cbVVar.setSelectedIndex(1);
						}
					}
					cbUVar.setEnabled(cbEnableState);
					cbVVar.setEnabled(cbEnableState);
				}

				if (wantsScalars) {
					if (getCblVars().getSelItemsSize() > 0) {
						setProcessEnabled(true);
					} else {
						setProcessEnabled(false);
					}
				} else if (getCblVars().getSelItemsSize() > 1) {
					setProcessEnabled(true);
				} else {
					setProcessEnabled(false);
				}
			}
		});
		optPanel.add(cbIsScalar, "wrap");

		ActionListener cbAction = new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				if (wantsScalars) {
					if (getCblVars().getSelItemsSize() >= 1) {
						setProcessEnabled(true);
					}
				} else {
					if (e.getActionCommand().equals("u")) {
						if (cbUVar.getItemCount() > 1 & cbUVar.getSelectedIndex() > -1) {
							setUVar(cbUVar.getSelectedItem().toString());
						} else {
							setUVar("");
						}
					} else if (e.getActionCommand().equals("v")) {
						if (cbVVar.getItemCount() > 1 & cbVVar.getSelectedIndex() > -1) {
							setVVar(cbVVar.getSelectedItem().toString());
						} else {
							setVVar("");
						}
					}

					if (cbUVar.getSelectedIndex() != -1 & cbVVar.getSelectedIndex() != -1) {
						analyzeSelGrids();
					}

					if (!getUVar().equals("") & !getVVar().equals("")) {// compare
						// the
						// var
						// Level
						// Lengths
						boolean showWarn = compareGrids(getUVar(), getVVar());
						// if(!compareGrids(getUVar(), getVVar())){
						// showWarn = true;
						// }
						btnWarn.setVisible(!showWarn);
						setProcessEnabled(showWarn);
						rbSurfAt0.setEnabled(showWarn);
						rbSurfAtN.setEnabled(showWarn);
					}
				}
			}
		};

		cbUVar = new JComboBox();
		cbVVar = new JComboBox();
		cbUVar.setActionCommand("u");
		cbVVar.setActionCommand("v");
		cbUVar.addActionListener(cbAction);
		cbVVar.addActionListener(cbAction);
		lblUVar = new JLabel("U Variable:");
		lblVVar = new JLabel("V Variable:");
		cbUVar.setEnabled(false);
		cbVVar.setEnabled(false);

		ImageIcon warn = new ImageIcon(Utils.getImageResource("warn.png", OilmapSelectionPanel.class));
		btnWarn = new JLabel(warn);
		btnWarn.addMouseListener(new MouseAdapter() {

			@Override
			public void mouseClicked(MouseEvent e) {
				StringBuilder message = new StringBuilder();
				if (!timeComp.toString().equals("equal")) {
					message.append("Time Axis: " + timeComp.toString());
					message.append("\n");
				}
				if (!vertComp.toString().equals("equal")) {
					message.append("Vertical Axis: " + vertComp.toString());
					message.append("\n");
				}
				javax.swing.JOptionPane.showMessageDialog(parentSpp, "The coordinate axes of the selected"
					+ " variables are not compatible.\n\n" + message.toString()
					+ "\nPlease select different variable(s)", "Incompatible Axes",
					javax.swing.JOptionPane.WARNING_MESSAGE);
			}
		});

		optPanel.add(lblUVar, "gap 0, split 3");
		optPanel.add(cbUVar, "gap 0");
		optPanel.add(btnWarn, "wrap");
		optPanel.add(lblVVar, "gap 0, split 3");
		optPanel.add(cbVVar, "gap 0, wrap");

		cbIsSpdDir = new JCheckBox("Use Speed & Direction");
		cbIsSpdDir.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				if (cbIsSpdDir.isSelected()) {
					lblUVar.setText("Speed Variable:");
					lblVVar.setText("Direction Variable:");
					setVectorType(true);
				} else {
					lblUVar.setText("U Variable:");
					lblVVar.setText("V Variable:");
					setVectorType(false);
				}
			}
		});
		optPanel.add(cbIsSpdDir, "wrap");

		btnWarn.setVisible(false);

		ActionListener rbAction = new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				if (getCblVars().getSelItemsSize() > 0) {
					surfLevel = (rbSurfAt0.isSelected()) ? minLevel : maxLevel;
					// if(rbSurfAt0.isSelected()){
					// surfLevel = minLevel;
					// }else{
					// surfLevel = maxLevel;
					// }
					setSurfaceLevel(surfLevel);
				}
			}
		};

		rbSurfAt0 = new JRadioButton("Level 0 is Surface");
		rbSurfAtN = new JRadioButton("Level 0 is Surface");
		rbSurfAt0.addActionListener(rbAction);
		rbSurfAtN.addActionListener(rbAction);

		ButtonGroup bg = new ButtonGroup();
		bg.add(rbSurfAt0);
		bg.add(rbSurfAtN);

		rbSurfAt0.setEnabled(false);
		rbSurfAtN.setEnabled(false);

		optPanel.add(rbSurfAt0, "gap 0, wrap");
		optPanel.add(rbSurfAtN, "gap 0, wrap");

		JButton btnCheckVals = new JButton("check");
		btnCheckVals.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				System.err.println("Surf=" + getSurfaceLevel() + "\nU=" + getUVar() + "\nV=" + getVVar());
			}
		});
		// optPanel.add(btnCheckVals);

		return optPanel;
	}

	private boolean compareGrids(String g1, String g2) {

		boolean vertOK = false;
		boolean timeOK = false;
		String sVert = "equal";
		String sTime = "equal";
		timeComp = new StringBuilder();
		vertComp = new StringBuilder();

		try {
			GridCoordSystem coordSys1;
			GridCoordSystem coordSys2;
			CoordinateAxis1D vert1;
			CoordinateAxis1D vert2;
			CoordinateAxis1D time1;
			CoordinateAxis1D time2;

			coordSys1 = ((SubsetProcessPanel)parentSpp).getGridByName(g1, true).getCoordinateSystem();
			vert1 = coordSys1.getVerticalAxis();
			time1 = coordSys1.getTimeAxis1D();

			coordSys2 = ((SubsetProcessPanel)parentSpp).getGridByName(g2, true).getCoordinateSystem();
			vert2 = coordSys2.getVerticalAxis();
			time2 = coordSys2.getTimeAxis1D();

			// check the vertical axes
			if (vert1 == null & vert2 == null) {// return true if both are null
				// (no vert axis)...
				vertOK = true;
			}
			if (vert1 != null & vert2 != null) {// check null
				if (vert1.getSize() == vert2.getSize()) {// check length
					if (vert1.getMinValue() == vert2.getMinValue()) {// check
						// min
						if (vert1.getMaxValue() == vert2.getMaxValue()) {// check
							// max
							if (vert1.getIncrement() == vert2.getIncrement()) {// check
								// increment
								constraints.setZDim(vert1.getDimensionsString());
								vertOK = true;
								vertComp.append("equal");
							} else {
								vertComp.append("Vertical axis increments are not equal: v1=" + vert1.getIncrement()
									+ " v2=" + vert2.getIncrement());
							}
						} else {
							vertComp.append("Vertical axis maximum values are not equal: v1=" + vert1.getMaxValue()
								+ " v2=" + vert2.getMaxValue());
						}
					} else {
						vertComp.append("Vertical axis minimum values are not equal: v1=" + vert1.getMinValue()
							+ " v2=" + vert2.getMinValue());
					}
				} else {
					vertComp.append("Vertical axis sizes are not equal: v1=" + vert1.getSize() + " v2="
						+ vert2.getSize());
				}
			} else {
				vertComp.append("One vertical axis is null");
			}

			// check the time axes
			if (time1 == null & time2 == null) {
				timeOK = true;
			}
			if (time1 != null & time2 != null) {
				if (time1.getSize() == time2.getSize()) {
					if (time1.getMinValue() == time2.getMinValue()) {
						if (time1.getMaxValue() == time2.getMaxValue()) {
							if (time1.getIncrement() == time2.getIncrement()) {
								constraints.setTimeDim(time1.getDimensionsString());
								timeOK = true;
								timeComp.append("equal");
							} else {
								timeComp.append("Temporal axis increments are not equal: t1=" + time1.getIncrement()
									+ " t2=" + time2.getIncrement());
							}
						} else {
							timeComp.append("Temporal axis maximum values are not equal: t1=" + time1.getMaxValue()
								+ " t2=" + time2.getMaxValue());
						}
					} else {
						timeComp.append("Temporal axis minimum values are not equal: t1=" + time1.getMinValue()
							+ " t2=" + time2.getMinValue());
					}
				} else {
					timeComp.append("Temporal axis sizes are not equal: t1=" + time1.getSize() + " t2="
						+ time2.getSize());
				}
			} else {
				timeComp.append("One temporal axis is null");
			}
		} catch (Exception ex) {
			timeOK = false;
			vertOK = false;
			ex.printStackTrace();
		}

		gridsCompatible = false;
		if (vertOK & timeOK) {
			gridsCompatible = true;
			return true;
		} else if (!vertOK) {
			System.err.println("Vertical axes not equal: " + vertComp.toString());
		} else if (!timeOK) {
			System.err.println("Temporal axes not equal: " + timeComp.toString());
		} else {
			System.err.println("Both axes not equal: " + vertComp.toString() + " : " + timeComp.toString());
		}

		constraints.setZDim("null");
		return false;
	}

	private void analyzeSelGrids() {
		// bail if there are no items to avoid null pointer exception when
		// gathering var names below
		if (cbUVar.getItemCount() < 1)
			return;

		// get the grid for the selected variable
		GeoGrid grid;
		GridCoordSystem coordSys;
		CoordinateAxis1D vert;

		boolean oneHasLevels = false;
		maxLevel = 0;
		String[] vars = new String[] { cbUVar.getSelectedItem().toString(), cbVVar.getSelectedItem().toString() };
		if (!vars[0].equals(vars[1])) {
			for (String vName : vars) {
				grid = ((SubsetProcessPanel)parentSpp).getGridByName(vName, true);
				coordSys = grid.getCoordinateSystem();
				vert = coordSys.getVerticalAxis();

				if (vert != null) {
					zVals = vert.getCoordValues();
					maxLevel = (zVals.length > maxLevel) ? zVals.length : maxLevel;

					oneHasLevels = true;
				}
			}
		}

		rbSurfAt0.setEnabled(oneHasLevels);
		rbSurfAtN.setEnabled(oneHasLevels);
		if (oneHasLevels) {
			minLevel = 1;
			rbSurfAt0.doClick();
		} else {
			minLevel = 0;
			maxLevel = 0;
			surfLevel = 0;
			rbSurfAt0.setSelected(false);
			rbSurfAtN.setSelected(false);
			setSurfaceLevel(surfLevel);
		}
		rbSurfAt0.setText("Level " + minLevel + " is Surface");
		rbSurfAtN.setText("Level " + maxLevel + " is Surface");
	}

	/**
	 * Constructs the <CODE>CheckBoxList</CODE> from the variables in the
	 * dataset.
	 */
	@Override
	public void setGeoGridVars() {
		super.setGeoGridVars();

		getCblVars().addPropertyChangeListener(new CheckBoxPropertyListener());
	}

	private List<String> selVars = new ArrayList<String>();
	private double[] zVals = null;

	class CheckBoxPropertyListener implements PropertyChangeListener {

		public void propertyChange(PropertyChangeEvent e) {

			// System.err.println("PropChange " + e.getPropertyName());

			String propName = e.getPropertyName();
			String vName = (String) e.getOldValue();

			if (propName.equals(CheckBoxList.ADDED)) {
				selVars.add(vName);

				// make sure the value isn't already added...
				boolean added = false;
				for (int i = 0; i < cbUVar.getItemCount(); i++) {
					if (cbUVar.getItemAt(i).toString().equals(vName)) {
						added = true;
					}
				}
				if (!added) {
					cbUVar.addItem(vName);
					cbVVar.addItem(vName);
				}
			} else if (propName.equals(CheckBoxList.REMOVED)) {
				selVars.remove(vName);

				cbUVar.removeItem(vName);
				cbVVar.removeItem(vName);
			} else {
			}

			boolean isEnabled = false;
			if (wantsScalars) {
				if (getCblVars().getSelItemsSize() > 0) {
					isEnabled = true;
				}
			} else if (!gridsCompatible) {
				if (getCblVars().getSelItemsSize() >= 2) {
					isEnabled = true;
					if (cbUVar.getSelectedIndex() == cbVVar.getSelectedIndex()) {
						cbUVar.setSelectedIndex(0);
						cbVVar.setSelectedIndex(1);
					}
				}
				// determine the interface options for the selected grid(s)
				analyzeSelGrids();

				cbUVar.setEnabled(isEnabled);
				cbVVar.setEnabled(isEnabled);
				setProcessEnabled(false);
			} else {
				if (getCblVars().getSelItemsSize() >= 2) {
					isEnabled = true;
					if (cbUVar.getSelectedIndex() == cbVVar.getSelectedIndex()) {
						cbUVar.setSelectedIndex(0);
						cbVVar.setSelectedIndex(1);
					}
				}

				// determine the interface options for the selected grid(s)
				analyzeSelGrids();

				cbUVar.setEnabled(isEnabled);
				cbVVar.setEnabled(isEnabled);
				setProcessEnabled(isEnabled);
			}
		}
	}
}
