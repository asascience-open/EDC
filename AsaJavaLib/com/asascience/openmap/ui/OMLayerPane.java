/*
 * Applied Science Associates, Inc.
 * Copyright 2007. All rights reserved.
 */

/*
 * DcpsysLayerPane.java
 *
 * Created on Apr 28, 2008, 2:12:22 PM
 *
 */
package com.asascience.openmap.ui;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JToggleButton;

import net.miginfocom.swing.MigLayout;
import ucar.unidata.geoloc.LatLonRect;

import com.asascience.openmap.layer.ASALayer;
import com.asascience.openmap.layer.IncidentLocLayer;
import com.asascience.openmap.layer.TimeLayer;
import com.asascience.openmap.utilities.MapUtils;
import com.asascience.ui.CollapsiblePanel;
import com.asascience.utilities.Utils;
import com.bbn.openmap.LatLonPoint;
import com.bbn.openmap.Layer;
import com.bbn.openmap.LayerHandler;
import com.bbn.openmap.MapBean;
import com.bbn.openmap.MapHandler;
import com.bbn.openmap.layer.DateLayer;
import com.bbn.openmap.layer.GraticuleLayer;
import com.bbn.openmap.layer.daynight.DayNightLayer;
import com.bbn.openmap.layer.etopo.ETOPOLayer;
import com.bbn.openmap.layer.shape.ShapeLayer;
import com.bbn.openmap.omGraphics.DrawingAttributes;
import com.bbn.openmap.proj.Proj;

/**
 * 
 * @author CBM <cmueller@asascience.com>
 */
public class OMLayerPane extends JPanel implements ActionListener {

	protected transient JToggleButton toggle;
	protected JCheckBox cbVis;
	protected transient Layer layer;
	protected transient LayerHandler layerHandler;
	protected OMTimeSlider timeLayerHandler;
	protected JPopupMenu rightClick;
	protected PropertyChangeSupport pcs;
	// protected JXCollapsiblePane cp;
	protected CollapsiblePanel colp;
	protected JPanel pnlColp;

	/**
	 * Creates a new instance of DcpsysLayerPane
	 * 
	 * @param inLayer
	 * @param lh
	 * @param tlh
	 */
	public OMLayerPane(Layer inLayer, LayerHandler lh, OMTimeSlider tlh) {
		super(new MigLayout("insets 0, fill"));
		this.setBorder(BorderFactory.createEtchedBorder());
		layer = inLayer;
		layerHandler = lh;
		timeLayerHandler = tlh;
		pcs = new PropertyChangeSupport(this);

		colp = new CollapsiblePanel(inLayer.getName(), false);
		colp.getCollapsiblePane().setCollapsed(true);
		pnlColp = new JPanel(new MigLayout("insets 5, fill"));
		pnlColp.setBorder(BorderFactory.createEtchedBorder());
		colp.add(pnlColp);

		// cp = new JXCollapsiblePane();
		// cp.setAnimated(false);
		// cp.setCrollapsed(true);
		// cp.setLayout(new MigLayout("insets 0, fill"));

		cbVis = new JCheckBox("Layer Visible");
		cbVis.setSelected(layer.isVisible());
		cbVis.setActionCommand("visible");
		cbVis.addActionListener(this);

		pnlColp.add(cbVis, "grow, wrap");
		// cp.add(cbVis, "grow, wrap");

		// construct the settings panel for the layer
		JPanel pnlSettings = new JPanel(new MigLayout("insets 0, fill"));
		// add the layers gui (if available) to the settings panel
		Component gui = layer.getGUI();
		if (gui != null) {
			pnlSettings.add(gui);
		}

		// add the settings panel to the collapsible pane
		pnlColp.add(pnlSettings, "center");
		// cp.add(pnlSettings, "center");

		// create a popup menu for the toggle button
		rightClick = new JPopupMenu();
		JMenuItem zoom = new JMenuItem("Zoom to Layer");
		zoom.setActionCommand("zoom");
		zoom.addActionListener(this);
		JMenuItem remove = new JMenuItem("Remove Layer");
		remove.setActionCommand("remove");
		remove.addActionListener(this);
		JMenuItem moveUp = new JMenuItem("Up");
		moveUp.setActionCommand("moveUp");
		moveUp.addActionListener(this);
		JMenuItem moveDown = new JMenuItem("Down");
		moveDown.setActionCommand("moveDown");
		moveDown.addActionListener(this);
		JMenuItem moveToTop = new JMenuItem("To Top");
		moveToTop.setActionCommand("moveToTop");
		moveToTop.addActionListener(this);
		JMenuItem moveToBottom = new JMenuItem("To Bottom");
		moveToBottom.setActionCommand("moveToBottom");
		moveToBottom.addActionListener(this);
		JMenuItem changeBasemap = new JMenuItem("Change Basemap");
		changeBasemap.setActionCommand("changeBasemap");
		changeBasemap.addActionListener(this);

		String name = (layer.getName().lastIndexOf(File.separator) == -1) ? layer.getName() : layer.getName()
			.substring(layer.getName().lastIndexOf(File.separator) + 1);

		if (layer instanceof ASALayer) {
			rightClick.add(zoom);
			rightClick.add(new javax.swing.JSeparator());
		}
		if (name.equals("Basemap")) {
			if (layer instanceof ShapeLayer) {
				rightClick.add(changeBasemap);
				rightClick.add(new javax.swing.JSeparator());
			}
		} else if (layer instanceof IncidentLocLayer || layer instanceof ETOPOLayer || layer instanceof DayNightLayer
			|| layer instanceof GraticuleLayer || layer instanceof DateLayer) {
			// don't add the remove menu item
		} else {
			rightClick.add(remove);
			rightClick.add(new javax.swing.JSeparator());
		}
		JMenu moveMenu = new JMenu("Move Layer");
		moveMenu.add(moveUp);
		moveMenu.add(moveDown);
		moveMenu.add(new javax.swing.JSeparator());
		moveMenu.add(moveToTop);
		moveMenu.add(moveToBottom);
		rightClick.add(moveMenu);

		// rightClick.add(moveUp);
		// rightClick.add(moveDown);
		// rightClick.add(moveToTop);
		// rightClick.add(moveToBottom);

		// Action toggleAction = cp.getActionMap().
		// get(JXCollapsiblePane.TOGGLE_ACTION);
		// toggleAction.putValue(JXCollapsiblePane.COLLAPSE_ICON,
		// UIManager.getIcon("Tree.expandedIcon"));
		// toggleAction.putValue(JXCollapsiblePane.EXPAND_ICON,
		// UIManager.getIcon("Tree.collapsedIcon"));

		// toggle = new JToggleButton(toggleAction);
		// toggle.setText(name);
		//
		// toggle.setHorizontalAlignment(JToggleButton.LEFT);
		// toggle.addMouseListener(new PopupListener());
		//
		// this.add(toggle, "grow, wrap");
		// this.add(cp);

		colp.addMouseListener(new PopupListener());

		this.add(colp);

		// cp.setCollapsed(true);
	}

	public void setCollapsed(boolean collapse) {
		// if(collapse){
		// if(cp.isCollapsed()){
		// //do nothing
		// }else{
		// toggle.doClick();
		// }
		// }else{
		// if(cp.isCollapsed()){
		// toggle.doClick();
		// }else{
		// //do nothing
		// }
		// }
	}

	protected void toggleVisibility(boolean isUser) {
		if (layerHandler != null) {

			if (layer instanceof TimeLayer) {
				TimeLayer tl = (TimeLayer) layer;
				if (tl.timeIsValid(timeLayerHandler.getGblCurrTime())) {
					layerHandler.turnLayerOn(cbVis.isSelected(), layer);
				}
				if (isUser) {
					((TimeLayer) layer).setLayerManuallyOn(cbVis.isSelected());
				}
			} else {
				layerHandler.turnLayerOn(cbVis.isSelected(), layer);
			}
		}
	}

	public void actionPerformed(ActionEvent e) {
		String cmd = e.getActionCommand();
		if (cmd.equals("visible")) {
			// firePropertyChange(cmd, cbVis.isSelected(), layer);
			toggleVisibility(true);
			// if(layerHandler != null){
			// layerHandler.turnLayerOn(cbVis.isSelected(), layer);
			// if(layer instanceof TimeLayer){
			// ((TimeLayer)layer).setLayerManuallyOn(cbVis.isSelected());
			// }
			// }
		} else if (cmd.equals("remove")) {
			// System.out.println(pcs.getPropertyChangeListeners().length);
			// firePropertyChange("removeLayer", this, layer);
			if (layerHandler != null) {
				if (layer instanceof TimeLayer) {// this removes the layer from
													// the layerHandler as well
					timeLayerHandler.removeLayer((TimeLayer) layer);
				}
				layerHandler.removeLayer(layer);
				// paneList.remove((SharcLayerPane)evt.getOldValue());
			}
		} else if (cmd.equals("zoom")) {
			// can't really do this generically....depends on the data behind
			// the layer....
			// if(layerHandler != null){
			MapHandler bc = (MapHandler) layer.getBeanContext();
			MapBean mb = null;
			if (bc != null) {
				mb = (MapBean) bc.get(com.bbn.openmap.MapBean.class);
			}
			if (mb != null) {
				Proj proj = (Proj) mb.getProjection();
				LatLonRect rect = ((ASALayer) layer).getLayerExtentRectangle();
				if (rect != null) {
					double cLat = ((rect.getLatMax() - rect.getLatMin()) * 0.5) + rect.getLatMin();
					LatLonPoint center = new LatLonPoint(cLat, rect.getCenterLon());

					LatLonPoint ll = MapUtils.ucarPointToBbnPoint(rect.getLowerLeftPoint());
					LatLonPoint ur = MapUtils.ucarPointToBbnPoint(rect.getUpperRightPoint());

					float scale = com.bbn.openmap.proj.ProjMath.getScale(MapUtils.ucarPointToBbnPoint(rect
						.getLowerLeftPoint()), MapUtils.ucarPointToBbnPoint(rect.getUpperRightPoint()), proj);

					scale = scale * 1.5f;

					proj.setCenter(center);
					proj.setScale(scale);
					mb.setProjection(proj);
				}
			}
		} else if (cmd.equals("moveUp")) {
			int li = getLayerIndex();
			if (layer.getAddAsBackground()) {// background layer
				if (li > getForegroundCount()) {
					layerHandler.moveLayer(layer, li - 1);
				}
			} else {// other layers
				if (li > 0) {
					layerHandler.moveLayer(layer, li - 1);
				}
			}
		} else if (cmd.equals("moveDown")) {
			int li = getLayerIndex();
			if (layer.getAddAsBackground()) {// background layer
				if (li < (getBackgroundCount() + getForegroundCount())) {
					layerHandler.moveLayer(layer, li + 1);
				}
			} else {// other layers
				if (li > -1 & li < getForegroundCount()) {
					layerHandler.moveLayer(layer, li + 1);
				}
			}
		} else if (cmd.equals("moveToTop")) {
			int li = getLayerIndex();
			if (layer.getAddAsBackground()) {// background layer
				if (li > getForegroundCount()) {
					layerHandler.moveLayer(layer, getForegroundCount());
				}
			} else {// other layers
				if (li > 0) {
					layerHandler.moveLayer(layer, 0);
				}
			}
		} else if (cmd.equals("moveToBottom")) {
			int li = getLayerIndex();
			if (layer.getAddAsBackground()) {// background layer
				if (li < (getBackgroundCount() + getForegroundCount())) {
					layerHandler.moveLayer(layer, getForegroundCount() + getBackgroundCount() - 1);
				}
			} else {// other layers
				if (li > -1 & li < getForegroundCount()) {
					layerHandler.moveLayer(layer, getForegroundCount() - 1);
				}
			}
		} else if (cmd.equals("changeBasemap")) {
			if (layer instanceof ShapeLayer) {
				ShapeLayer s = (ShapeLayer) layer;
				Properties props = new Properties();
				props = s.getProperties(props);
				String dir = props.getProperty("shapeFile");
				String nowFile = dir.substring(dir.lastIndexOf(File.separator) + 1);
				dir = dir.replace(nowFile, "");

				File f = new File(dir);
				String[] ss = f.list();
				List<String> names = new ArrayList<String>();
				for (String st : ss) {
					if (st.endsWith(".shp")) {
						names.add(st);
					}
				}

				String outS = (String) JOptionPane.showInputDialog(this.getTopLevelAncestor(),
					"Choose a file for the Basemap layer:", "Select Basemap", JOptionPane.PLAIN_MESSAGE, null, names
						.toArray(), (names.contains(nowFile) ? nowFile : names.get(0)));

				if (outS != null && !outS.equals("")) {
					DrawingAttributes da = s.getDrawingAttributes();
					props.setProperty("shapeFile", Utils.appendSeparator(dir) + outS);
					s.setProperties(props);
					s.setDrawingAttributes(da);

					s.doPrepare();
				}
			}
		}
	}

	protected int getForegroundCount() {
		int count = 0;
		for (Layer l : layerHandler.getLayers()) {
			if (!l.getAddAsBackground()) {
				count++;
			}
		}
		return count;
	}

	protected int getBackgroundCount() {
		int count = 0;
		for (Layer l : layerHandler.getLayers()) {
			if (l.getAddAsBackground()) {
				count++;
			}
		}
		return count;
	}

	protected int getLayerIndex() {
		Layer[] lyrs = layerHandler.getLayers();
		for (int i = 0; i < lyrs.length; i++) {
			if (layer == lyrs[i]) {
				return i;
			}
		}
		return -1;
	}

	public Layer getLayer() {
		return layer;
	}

	public void layerVisibility(boolean turnOn) {
		if (turnOn) {
			if (cbVis.isSelected()) {
				// do nothing
			} else {
				// cbVis.doClick();
				cbVis.setSelected(true);
				toggleVisibility(false);
			}
		} else {
			if (cbVis.isSelected()) {
				// cbVis.doClick();
				cbVis.setSelected(false);
				toggleVisibility(false);
			} else {
				// do nothing
			}
		}
	}

	@Override
	public void addPropertyChangeListener(PropertyChangeListener pcl) {

		if (pcl instanceof OMLayerPanel) {
			// System.out.println("Add: " + pcl.toString());
			pcs.addPropertyChangeListener(pcl);
		}
	}

	@Override
	public void removePropertyChangeListener(PropertyChangeListener pcl) {
		pcs.removePropertyChangeListener(pcl);
	}

	class PopupListener extends MouseAdapter {

		@Override
		public void mousePressed(MouseEvent e) {
			maybeShowPopup(e);
		}

		@Override
		public void mouseReleased(MouseEvent e) {
			maybeShowPopup(e);
		}

		private void maybeShowPopup(MouseEvent e) {
			if (e.getButton() == MouseEvent.BUTTON3) {
				rightClick.show(e.getComponent(), e.getX(), e.getY());
			}
		}
	}
}
