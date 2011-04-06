/*
 * EsriSelectionPanel.java
 *
 * Created on November 26, 2007, 11:39 AM
 *
 * Applied Science Associates, Inc.
 * Copyright 2007.  All rights reserved.
 */
package com.asascience.edc.gui;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Date;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.border.EtchedBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import net.miginfocom.swing.MigLayout;
import ucar.nc2.dataset.CoordinateAxis1D;
import ucar.nc2.dt.GridCoordSystem;

import com.asascience.edc.nc.NetcdfConstraints;
import com.asascience.ui.CheckBoxList;

/**
 * Extends <CODE>SelectionPanelBase</CODE> to add specific functionality for use
 * as part of an ESRI ArcGIS tool.
 * 
 * Specifically, this class overrides the setVariables() method to allow use of
 * a specialized property handler for the checkbox list.
 * 
 * @author CBM
 */
public class EsriSelectionPanel extends SelectionPanelBase {

	// private JRadioButton rbRaster;
	// private JRadioButton rbVector;
	// private JRadioButton rbFeature;
	private JTabbedPane tpOutputAs;
	private JComboBox cbUComp;
	private JComboBox cbVComp;
	private JComboBox cbBandDim;
	private JComboBox cbTrimBy;
	private JComboBox cbTrimByFeat;
	private JCheckBox ckVector;
	private JCheckBox ckRasNoTrim;
	private JCheckBox ckFeatNoTrim;
	private JLabel lblTrimSel;
	private JLabel lblBandDim;

	/**
	 * Creates a new instance of EsriSelectionPanel. Calls the other constructor
	 * using a blank panel title
	 * 
	 * @param cons
	 *            The <CODE>NetcdfConstraints</CODE> object for this instance.
	 *            Some properties of this object are dynamically updated by the
	 *            controls in this panel.
	 * @param parent
	 *            The <CODE>SubsetProcessPanel</CODE> that contains this
	 *            control.
	 */
	public EsriSelectionPanel(NetcdfConstraints cons, SubsetProcessPanel parent) {
		this("", cons, parent);
	}

	/**
	 * Creates a new instance of EsriSelectionPanel
	 * 
	 * @param borderTitle
	 *            The title to be used for this panel.
	 * @param cons
	 *            The <CODE>NetcdfConstraints</CODE> object for this instance.
	 *            Some properties of this object are dynamically updated by the
	 *            controls in this panel.
	 * @param parent
	 *            The <CODE>SubsetProcessPanel</CODE> that contains this
	 *            control.
	 */
	public EsriSelectionPanel(String borderTitle, NetcdfConstraints cons, SubsetProcessPanel parent) {

		super(borderTitle, cons, parent);
		setPanelType(SelectionPanelBase.ESRI);

		createPanel();
	}

	/**
	 * Builds the panel and initializes the various components.
	 */
	@Override
	public void createPanel() {
		super.createPanel();
		this.add(outputPanel(), BorderLayout.SOUTH);
	}

	private JPanel outputPanel() {
		tpOutputAs = new JTabbedPane();

		JPanel outputPanel = new JPanel(new MigLayout("fillx"));
		outputPanel.setBorder(BorderFactory.createTitledBorder("Output Options:"));

		tpOutputAs.add("Raster", rasterPanel());
		tpOutputAs.setToolTipTextAt(0, "Select this tab to output data as an ArcGIS Raster dataset.");
		tpOutputAs.add("Feature", featurePanel());
		tpOutputAs.setToolTipTextAt(1, "Select this tab to output data as an ArcGIS Feature dataset.");

		tpOutputAs.addChangeListener(new TabPaneChangeListener());
		tpOutputAs.setSelectedIndex(0);

		outputPanel.add(tpOutputAs);

		return outputPanel;
	}

	private JPanel rasterPanel() {
		cbBandDim = new JComboBox();
		cbBandDim.setToolTipText("Select the dimension which will be made into bands.");
		cbBandDim.setEnabled(false);
		cbTrimBy = new JComboBox();
		cbTrimBy.setToolTipText("Select where to \"slice\" the 4D data to create a 3D dataset.");
		cbTrimBy.setEnabled(false);
		cbTrimBy.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				int index = cbTrimBy.getSelectedIndex();
				if (index > -1) {
					setTrimByIndex(index);
					setTrimByValue(cbTrimBy.getSelectedItem().toString());
				}
			}
		});
		lblTrimSel = new JLabel("Select Level:");

		ckRasNoTrim = new JCheckBox("All");
		ckRasNoTrim.setEnabled(false);
		ckRasNoTrim.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				if (ckRasNoTrim.isSelected()) {
					ckFeatNoTrim.setSelected(true);
					setUseAllLevels(true);
					cbTrimBy.setEnabled(false);
					cbTrimByFeat.setEnabled(false);
				} else {
					ckFeatNoTrim.setSelected(false);
					setUseAllLevels(false);
					if (getCblVars().getSelItemsSize() > 0) {
						cbTrimBy.setEnabled(true);
						cbTrimByFeat.setEnabled(true);
					}
				}
			}
		});

		JPanel rasterPanel = new JPanel(new MigLayout("fillx"));
		rasterPanel.setBorder(new EtchedBorder());

		lblBandDim = new JLabel("Band Dimension:");
		rasterPanel.add(lblBandDim);
		rasterPanel.add(cbBandDim, "wrap");
		rasterPanel.add(lblTrimSel);
		rasterPanel.add(cbTrimBy, "split 2");
		rasterPanel.add(ckRasNoTrim);

		return rasterPanel;
	}

	private JPanel featurePanel() {
		ckVector = new JCheckBox("As Vectors");
		ckVector.setToolTipText("Check this box to output data as a vector dataset.");

		JPanel featurePanel = new JPanel(new MigLayout("fillx"));
		featurePanel.setBorder(new EtchedBorder());

		cbTrimByFeat = new JComboBox();
		cbTrimByFeat.setEnabled(false);
		cbTrimByFeat.setToolTipText("Select where to \"slice\" the 4D data to create a 3D dataset.");
		cbTrimByFeat.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				int index = cbTrimByFeat.getSelectedIndex();
				if (index > -1) {
					setTrimByIndex(index);
					setTrimByValue(cbTrimByFeat.getSelectedItem().toString());
				}
			}
		});
		lblTrimSel = new JLabel("Select Level:");

		ckFeatNoTrim = new JCheckBox("All");
		ckFeatNoTrim.setEnabled(false);
		ckFeatNoTrim.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				if (ckFeatNoTrim.isSelected()) {
					ckRasNoTrim.setSelected(true);
					setUseAllLevels(true);
					cbTrimBy.setEnabled(false);
					cbTrimByFeat.setEnabled(false);
				} else {
					ckRasNoTrim.setSelected(false);
					setUseAllLevels(false);
					cbTrimBy.setEnabled(true);
					cbTrimByFeat.setEnabled(true);
				}
			}
		});

		ckVector.addChangeListener(new ChangeListener() {

			public void stateChanged(ChangeEvent e) {
				boolean enable = false;
				if (ckVector.isSelected()) {
					if (getCblVars().getSelItemsSize() > 1) {
						enable = true;
						cbUComp.setSelectedIndex(0);
						cbVComp.setSelectedIndex(1);
					}
				}

				setMakeVector(enable);
				cbUComp.setEnabled(enable);
				cbVComp.setEnabled(enable);
			}
		});

		featurePanel.add(lblTrimSel, "split 3");
		featurePanel.add(cbTrimByFeat);
		featurePanel.add(ckFeatNoTrim, "wrap");

		featurePanel.add(ckVector, "wrap");
		featurePanel.add(vectorPanel());

		ckVector.setSelected(true);
		ckVector.setSelected(false);
		ckVector.setEnabled(false);

		return featurePanel;
	}

	private JPanel vectorPanel() {
		cbUComp = new JComboBox();
		cbUComp.setToolTipText("The U-Component (x) of the vector.");
		cbUComp.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				if (cbUComp.getSelectedIndex() > -1) {
					setUVar(cbUComp.getSelectedItem().toString());
				} else {
					setUVar("");
				}
			}
		});
		cbVComp = new JComboBox();
		cbVComp.setToolTipText("The V-Component (y) of the vector.");
		cbVComp.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				if (cbVComp.getSelectedIndex() > -1) {
					setVVar(cbVComp.getSelectedItem().toString());
				} else {
					setVVar("");
				}
			}
		});

		JPanel vectorPanel = new JPanel(new MigLayout("fillx"));
		vectorPanel.setBorder(new EtchedBorder());

		vectorPanel.add(new JLabel("U Component:"));
		vectorPanel.add(cbUComp, "wrap");
		vectorPanel.add(new JLabel("V Component:"));
		vectorPanel.add(cbVComp);

		return vectorPanel;
	}

	/**
	 * 
	 * @param isRegular
	 */
	@Override
	public void gridRegularity(boolean isRegular) {
		tpOutputAs.setEnabledAt(0, isRegular);
		if (!isRegular) {
			// if the grid is irregular, set the tab pane to feature
			tpOutputAs.setSelectedIndex(1);
			// TODO: can notify the user if desired...??
		}
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

	class CheckBoxPropertyListener implements PropertyChangeListener {
		// TODO: FIXED: Checkbox(es) from first dataset do not respond to the
		// "remove" calls....
		// the checkboxes are not in the selItems property of the CheckBoxList
		// class
		// when the call to remove them comes - thus, no event is fired...
		public void propertyChange(PropertyChangeEvent e) {

			// System.err.println("PropChange " + e.getPropertyName());

			String propName = e.getPropertyName();
			String vName = (String) e.getOldValue();

			// get the grid for the selected variable
			// GeoGrid grid = parentSpp.getGridByName(vName);
			GridCoordSystem coordSys = ((SubsetProcessPanel)parentSpp).getGridByName(vName, true).getCoordinateSystem();
			CoordinateAxis1D vert;// = coordSys.getVerticalAxis();

			vert = coordSys.getVerticalAxis();

			if (propName.equals(CheckBoxList.ADDED)) {
				// if the raster tab is selected
				// System.err.println("adding a var");

				if (isMakeRaster()) {
					// //take care of the feature cbboxes - now taken care of
					// with tab listener
					// cbUComp.removeAllItems();
					// cbVComp.removeAllItems();
					// cbUComp.addItem(vName);
					// cbVComp.addItem(vName);

					// reset all of the comboboxes
					cbBandDim.removeAllItems();
					cbBandDim.setEnabled(false);
					ActionListener[] als = cbBandDim.getActionListeners();
					for (int i = als.length - 1; i >= 0; i--) {
						cbBandDim.removeActionListener(als[i]);
						// System.err.println("AL removed");
					}

					// System.err.println("cbALs="+cbBandDim.getActionListeners().length);

					cbTrimBy.removeAllItems();
					cbTrimBy.setEnabled(false);

					int sz = getCblVars().getSelItemsSize();

					getCblVars().deselectAllButOne(((JCheckBox) e.getNewValue()));

					sz = getCblVars().getSelItemsSize();
					// System.err.println("cblVars size:" +
					// cblVars.getSelectedItems().size());

					// get the grid for the selected variable
					// GeoGrid grid = parentSpp.getGridByName(vName);
					// GridCoordSystem coordSys = grid.getCoordinateSystem();
					// CoordinateAxis1D vert = coordSys.getVerticalAxis();

					double[] zVals = null;
					Date[] tVals = null;
					String zDesc = null;
					String tDesc = null;

					// add items to the band dimension combobox
					if (coordSys.hasTimeAxis1D()) {
						String s = coordSys.getTimeAxis1D().getDimensionsString();
						cbBandDim.addItem(s);
						constraints.setTimeDim(s);

						tVals = coordSys.getTimeAxis1D().getTimeDates();
						tDesc = coordSys.getTimeAxis1D().getDescription();
						if (tDesc == null || tDesc.equals("")) {
							tDesc = "No Description Available";
						}
					}
					if (vert != null) {
						cbBandDim.addItem((vert.getDimensionsString()));
						constraints.setZDim(vert.getDimensionsString());

						zVals = vert.getCoordValues();
						zDesc = vert.getDescription();
						if (zDesc == null || zDesc.equals("")) {
							zDesc = "No Description Available";
						}
					}

					final double[] inZVals = zVals;
					final Date[] inTVals = tVals;
					final String inZDesc = zDesc;
					final String inTDesc = tDesc;
					// register a new ActionListener with cbBandDim
					cbBandDim.addActionListener(new ActionListener() {

						public void actionPerformed(ActionEvent e) {
							cbTrimBy.setEnabled(false);
							cbTrimBy.removeAllItems();
							if (cbBandDim.getItemCount() == 1) {
								constraints.setBandDim(cbBandDim.getSelectedItem().toString());
							} else if (cbBandDim.getItemCount() == 2) {
								cbTrimBy.setEnabled(true);
								// cbTrimBy.removeAllItems();

								int i = cbBandDim.getSelectedIndex();
								String bDim = cbBandDim.getSelectedItem().toString();
								String trimByDim;
								if (i == 0) {
									trimByDim = cbBandDim.getItemAt(1).toString();
								} else {
									trimByDim = cbBandDim.getItemAt(0).toString();
								}

								if (trimByDim.equals(constraints.getZDim())) {// trim
									// by
									// depth
									for (int j = 0; j < inZVals.length; j++) {
										cbTrimBy.addItem(inZVals[j]);
									}
									constraints.setTrimByZ(true);
									lblTrimSel.setText("Select Level:");
									lblTrimSel.setToolTipText(inZDesc);
									lblBandDim.setToolTipText(inTDesc);
								} else {
									for (int j = 0; j < inTVals.length; j++) {
										cbTrimBy.addItem(inTVals[j]);
									}
									constraints.setTrimByZ(false);
									lblTrimSel.setText("Select Time:");
									lblTrimSel.setToolTipText(inTDesc);
									lblBandDim.setToolTipText(inZDesc);
								}
								// System.err.println("banddimset");
								constraints.setBandDim(bDim);
								constraints.setTrimByDim(trimByDim);

								// System.err.println(trimByDim);
								// System.err.println(bandDim);
							}
							// }else{
							// cbTrimBy.setEnabled(false);
							// }
						}
					});
					// make it notify the listener once if there are items in
					// the list
					if (cbBandDim.getItemCount() > 0) {
						cbBandDim.setSelectedIndex(0);
					}

					// if(vert != null){
					// double[] vertVals = vert.getCoordValues();
					// cbTrimBy.setEnabled(true);
					// for(int i = 0; i < vertVals.length; i++){
					// cbTrimBy.addItem(vertVals[i]);
					// }
					// cbBandDim.addItem(vert.getDimensionsString());
					// constraints.setZDim(vert.getDimensionsString());
					// } else {
					// cbTrimBy.setEnabled(false);
					// }

					if (cbBandDim.getItemCount() != 0) {
						cbBandDim.setEnabled(true);
					}

				} else {// if the feature tab is selected
					// cbUComp.setEnabled(true);
					// cbVComp.setEnabled(true);
					// //get the grid for the selected variable
					// GeoGrid grid = parentSpp.getGridByName(vName);
					// GridCoordSystem coordSys = grid.getCoordinateSystem();
					// CoordinateAxis1D vert = coordSys.getVerticalAxis();

					// make sure the value isn't already added...
					// does this EVER get used??
					boolean added = false;
					for (int i = 0; i < cbUComp.getItemCount(); i++) {
						if (cbUComp.getItemAt(i).toString().equals(vName)) {
							added = true;
						}
					}

					if (!added) {
						double[] zVals = null;
						String zDesc = null;

						// add items to the band dimension combobox
						if (vert != null) {
							zVals = vert.getCoordValues();
							zDesc = vert.getDescription();
							if (zDesc == null) {
								zDesc = "No Description Available";
							}
						}

						if (vert != null) {// if the new variable's level is NOT
							// null
							cbTrimByFeat.removeAllItems();

							for (int j = 0; j < zVals.length; j++) {
								cbTrimByFeat.addItem(zVals[j]);
							}
							constraints.setZDim(vert.getDimensionsString());
						}

						if (cbTrimByFeat.getItemCount() != 0) {
							cbTrimByFeat.setEnabled(true);
							constraints.setTrimByZ(true);
							constraints.setTrimByDim(vert.getName());
						} else {
							cbTrimByFeat.setEnabled(false);
							constraints.setTrimByZ(false);
							constraints.setTrimByDim("null");
						}

						cbUComp.addItem(vName);
						cbVComp.addItem(vName);
						if (cbUComp.getItemCount() >= 2) {
							ckVector.setEnabled(true);
						}

						if (ckVector.isSelected()) {
							if (cbUComp.getItemCount() >= 2) {
								cbUComp.setEnabled(true);
							}
							if (cbVComp.getItemCount() >= 2) {
								cbVComp.setEnabled(true);
							}
						}
						// cbUComp.validate();
						// cbVComp.validate();
					}
				}
			} else if (propName.equals(CheckBoxList.REMOVED)) {
				// System.err.println("removing a var");
				cbUComp.removeItem(vName);
				cbVComp.removeItem(vName);
				if (isMakeRaster()) {
					cbTrimBy.removeAllItems();
					cbTrimBy.setEnabled(false);
					cbBandDim.removeAllItems();
					cbBandDim.setEnabled(false);
				} else {// if feature tab
					// cbUComp.removeItem(vName);
					// cbVComp.removeItem(vName);

					boolean keepVerts = false;
					for (String s : getCblVars().getSelectedItems()) {
						vert = ((SubsetProcessPanel)parentSpp).getGridByName(s, true).getCoordinateSystem().getVerticalAxis();
						if (vert != null) {
							keepVerts = true;
							constraints.setTrimByDim(vert.getName());
						}
					}

					if (!keepVerts) {
						cbTrimByFeat.removeAllItems();
						constraints.setTrimByDim("null");
					}

					if (cbTrimByFeat.getItemCount() == 0) {
						cbTrimByFeat.setEnabled(false);
					}

					if (cbUComp.getItemCount() < 2) {
						cbUComp.setEnabled(false);
						cbVComp.setEnabled(false);
						ckVector.setEnabled(false);
						ckVector.setSelected(false);
					}

					// if(cbVComp.getItemCount() < 2) cbVComp.setEnabled(false);
				}
			}

			// ensure that the "noTrim" checkboxes are available if there are
			// variable(s) selected
			if (getCblVars().getSelItemsSize() > 0) {
				ckRasNoTrim.setEnabled(true);
				ckFeatNoTrim.setEnabled(true);
			} else {
				setUseAllLevels(false);
				ckRasNoTrim.setSelected(false);
				ckFeatNoTrim.setSelected(false);
				ckRasNoTrim.setEnabled(false);
				ckFeatNoTrim.setEnabled(false);
			}

			// TODO: temp while 4D rasters not supported
			if (isMakeRaster()) {
				ckRasNoTrim.setSelected(false);
				ckRasNoTrim.setEnabled(false);
				setUseAllLevels(false);
			}

			// ensure the btnProcess button can't be clicked if there aren't
			// sufficient variables
			// selected for the output type
			boolean isEnabled = true;
			// System.err.println("enabling");
			if (isMakeRaster()) {
				if (getCblVars().getSelItemsSize() != 1) {
					isEnabled = false;
				}
			} else if (isMakeVector()) {
				if (getCblVars().getSelItemsSize() < 2) {
					isEnabled = false;
				}
			} else if (getCblVars().getSelItemsSize() == 0) {
				isEnabled = false;
			}

			setProcessEnabled(isEnabled);
		}
	}

	class TabPaneChangeListener implements ChangeListener {

		public void stateChanged(ChangeEvent e) {
			int i = tpOutputAs.getSelectedIndex();

			if (i == 0) {// raster
				setMakeRaster(true);
			} else if (i == 1) {// feature
				setMakeRaster(false);
			}

			if (getCblVars().getSelItemsSize() == 1) {
				String s = getCblVars().getSelectedItems(0);
				getCblVars().deselectAll();
				getCblVars().selectSingleItem(s);// this fires the
				// "CheckBoxPropertyListener"
			} else {
				// deselect all variables
				getCblVars().deselectAll();
				// remove everything from the raster panel
				cbBandDim.removeAllItems();
				cbBandDim.setEnabled(false);
				cbTrimBy.removeAllItems();
				cbTrimBy.setEnabled(false);
				ckRasNoTrim.setSelected(false);
				ckRasNoTrim.setEnabled(false);
				// remove everything from the feature panel
				cbUComp.removeAllItems();
				cbVComp.removeAllItems();
				ckVector.setSelected(false);
				ckVector.setEnabled(false);
				cbTrimByFeat.removeAllItems();
				cbTrimByFeat.setEnabled(false);
				ckFeatNoTrim.setSelected(false);
				ckFeatNoTrim.setEnabled(false);
			}

			if (i == 0) {// raster
				if (ckRasNoTrim.isSelected()) {
					cbTrimBy.setEnabled(false);
				}
			} else if (i == 1) {// feature
				if (ckFeatNoTrim.isSelected()) {
					cbTrimByFeat.setEnabled(false);
				}
				// disable vectors since there will never be more than one
				// variable selected at this point...
				ckVector.setSelected(false);
			}

			if (getCblVars().getSelItemsSize() == 0) {
				setProcessEnabled(false);
			}

			// if(getCblVars() != null){// && btnProcess != null){
			// if(getCblVars().getSelItemsSize() < 1){
			// setProcessEnabled(false);
			// // btnProcess.setEnabled(false);
			// }else{
			// setProcessEnabled(true);
			// // btnProcess.setEnabled(true);
			// }
			// }
		}
	}
}
