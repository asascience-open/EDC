package com.asascience.openmap.layer;

/*
 * IncidentLocLayer.java
 *
 * Created on July 24, 2007, 9:07 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

import java.awt.Component;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeListener;
import java.util.Properties;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.asascience.openmap.mousemode.InformationMouseMode;
import com.asascience.openmap.mousemode.LocationSelectMouseMode;
import com.bbn.openmap.LatLonPoint;
import com.bbn.openmap.event.MapMouseListener;
import com.bbn.openmap.layer.OMGraphicHandlerLayer;
import com.bbn.openmap.omGraphics.DrawingAttributes;
import com.bbn.openmap.omGraphics.OMGraphic;
import com.bbn.openmap.omGraphics.OMGraphicList;
import com.bbn.openmap.omGraphics.OMPoint;

/**
 *An implementation of OMGraphicHandlerLayer that allows the user to "click-in"
 * an incident location and have it show up as a point on the map. The layer can
 * be generated either from a click event or simply by adding a location using
 * AddIncidentLocation();
 * 
 * @author cmueller
 */
public class IncidentLocLayer extends OMGraphicHandlerLayer implements MapMouseListener, ActionListener {

	private OMGraphicList omgraphics;
	private OMGraphic selectedGraphic;
	// private boolean canAddLoc;
	private JTextField tfLat = new JTextField();
	private JTextField tfLon = new JTextField();
	private LatLonPoint incidentLoc;

	protected DrawingAttributes drawingAttributes;

	/** Creates a new instance of IncidentLocLayer */
	public IncidentLocLayer() {
		omgraphics = new OMGraphicList();
		this.setList(omgraphics);
		this.consumeEvents = false;
		// canAddLoc = true;
		this.mouseModeIDs = getMouseModeServiceList();
	}

	/**
	 * 
	 * @return
	 */
	@Override
	public boolean receivesMapEvents() {
		return false;
	}

	// public void locAdditionEnabled(boolean bool){
	// canAddLoc = bool;
	// }

	public void setLatLonTextFields(JTextField lat, JTextField lon) {
		this.tfLat = lat;
		this.tfLon = lon;
	}

	/**
	 * 
	 * @param prefix
	 * @param props
	 */
	@Override
	public void setProperties(String prefix, Properties props) {
		super.setProperties(prefix, props);

		drawingAttributes = new DrawingAttributes(prefix, props);
	}

	@Override
	public void addPropertyChangeListener(String name, PropertyChangeListener pcl) {
	}

	public void setDrawingAttributes(DrawingAttributes da) {
		drawingAttributes = da;
	}

	public DrawingAttributes getDrawingAttributes() {
		return drawingAttributes;
	}

	public boolean addIncidentLocation(float lat, float lon) {
		try {
			// if(this.canAddLoc){
			OMGraphicList omgl = this.getList();
			omgl.clear();
			omgl.addOMGraphic(createPoint(lat, lon, 4));

			this.doPrepare();
			return true;
			// }
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return false;
	}

	public OMPoint createPoint(float lat, float lon, int radius) {
		OMPoint pt = new OMPoint(lat, lon);
		pt.setRadius(radius);
		pt.setOval(true);

		return pt;
	}

	/**
	 * 
	 * @return
	 */
	@Override
	public OMGraphicList prepare() {
		OMGraphicList list = this.getList();
		OMGraphic g;
		for (int i = 0; i < list.size(); i++) {
			g = list.getOMGraphicAt(i);
			g.setLinePaint(drawingAttributes.getLinePaint());
			g.setFillPaint(drawingAttributes.getFillPaint());
			g.setSelectPaint(drawingAttributes.getSelectPaint());
		}
		list.generate(this.getProjection());
		return list;
	}

	/**
	 * 
	 * @return
	 */
	@Override
	public MapMouseListener getMapMouseListener() {
		return this;
	}

	// <editor-fold defaultstate="collapsed" desc=" GUI for changing colors ">
	protected transient JPanel box;

	/**
	 * 
	 * @return
	 */
	@Override
	public Component getGUI() {
		if (box == null) {
			box = new JPanel();
			box.setLayout(new BoxLayout(box, BoxLayout.Y_AXIS));
			box.setAlignmentX(Component.LEFT_ALIGNMENT);

			JPanel stuff = new JPanel();
			DrawingAttributes da = drawingAttributes;

			stuff.add(da.getGUI());

			JPanel pnl2 = new JPanel();
			JButton redraw = new JButton();
			redraw.setText("Redraw Layer");
			redraw.setActionCommand(RedrawCmd);
			redraw.addActionListener(this);
			pnl2.add(redraw);

			box.add(stuff);
			box.add(pnl2);
		}

		return box;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		super.actionPerformed(e);
		String cmd = e.getActionCommand();
		if (cmd.equals(RedrawCmd)) {
			if (this.isVisible()) {
				this.doPrepare();
			}
		}
	}

	// </editor-fold>

	// <editor-fold defaultstate="collapsed"
	// desc="MapMouseListener Implementation">

	/**
	 * Indicates which mouse modes should send events to this <code>Layer</code>
	 * .
	 * 
	 * @return An array mouse mode names
	 * 
	 * @see com.bbn.openmap.event.MapMouseListener
	 * @see com.bbn.openmap.MouseDelegator
	 */
	public String[] getMouseModeServiceList() {
		String[] ret = new String[2];
		ret[0] = LocationSelectMouseMode.modeID;
		ret[1] = InformationMouseMode.modeID;
		return ret;
	}

	/**
	 * Called whenever the mouse is pressed by the user and one of the requested
	 * mouse modes is active.
	 * 
	 * @param e
	 *            the press event
	 * @return true if event was consumed(handled), false otherwise
	 * @see #getMouseModeServiceList
	 */
	public boolean mousePressed(MouseEvent e) {
		return false;
	}

	/**
	 * Called whenever the mouse is released by the user and one of the
	 * requested mouse modes is active.
	 * 
	 * @param e
	 *            the release event
	 * @return true if event was consumed (handled), false otherwise
	 * @see #getMouseModeServiceList
	 */
	public boolean mouseReleased(MouseEvent e) {
		return false;
	}

	/**
	 * Called whenever the mouse is clicked by the user and one of the requested
	 * mouse modes is active.
	 * 
	 * @param e
	 *            the click event
	 * @return true if event was consumed (handled), false otherwise
	 * @see #getMouseModeServiceList
	 */
	public boolean mouseClicked(MouseEvent e) {
		// <editor-fold defaultstate="collapsed" desc=" Old LocSelectStuff ">

		// try {
		// // if(canAddLoc){
		// incidentLoc = ((MapMouseEvent)e).getLatLon();
		// addIncidentLocation(incidentLoc.getLatitude(),
		// incidentLoc.getLongitude());
		// // tfLat.setText(Float.toString(incidentLoc.getLatitude()));
		// // tfLon.setText(Float.toString(incidentLoc.getLongitude()));
		// // tfLat.setEditable(false);
		// // tfLon.setEditable(false);
		// // canAddLoc = false;
		// // this.setSelectingLocation(false);
		// return true;
		// // }
		//            
		// // if(((MapMouseEvent)e).getButton() == MouseEvent.BUTTON3){
		// // if(selectedGraphic != null){
		// // this.fireRequestInfoLine("Incident Location");
		// //// JOptionPane.showMessageDialog(null, "Selected Graphic ID:" +
		// selectedGraphic.getDescription());
		// //// }else{
		// //// JOptionPane.showMessageDialog(null,
		// "Howdy Right-Clicker!!\n\nYou've clicked in empty space!");
		// // }
		// // }
		//            
		// } catch(Exception ex) {
		// ex.printStackTrace();
		// }

		// </editor-fold>
		return false;
	}

	/**
	 * Called whenever the mouse enters this layer and one of the requested
	 * mouse modes is active.
	 * 
	 * @param e
	 *            the enter event
	 * @see #getMouseModeServiceList
	 */
	public void mouseEntered(MouseEvent e) {
	}

	/**
	 * Called whenever the mouse exits this layer and one of the requested mouse
	 * modes is active.
	 * 
	 * @param e
	 *            the exit event
	 * @see #getMouseModeServiceList
	 */
	public void mouseExited(MouseEvent e) {
	}

	/**
	 * Called whenever the mouse is dragged on this layer and one of the
	 * requested mouse modes is active.
	 * 
	 * @param e
	 *            the drag event
	 * @return true if event was consumed (handled), false otherwise
	 * @see #getMouseModeServiceList
	 */
	public boolean mouseDragged(MouseEvent e) {
		return false;
	}

	/**
	 * Called whenever the mouse is moved on this layer and one of the requested
	 * mouse modes is active.
	 * <p>
	 * Tries to locate a graphic near the mouse, and if it is found, it is
	 * highlighted and the Layer is repainted to show the highlighting.
	 * 
	 * @param e
	 *            the move event
	 * @return true if event was consumed (handled), false otherwise
	 * @see #getMouseModeServiceList
	 */
	public boolean mouseMoved(MouseEvent e) {
		OMGraphic newSelGraphic = this.getList().selectClosest(e.getX(), e.getY(), 5.0f);
		if (newSelGraphic == null) {
			this.fireRequestInfoLine("");
			repaint();
			return true;
		} else {// if(newSelGraphic != selectedGraphic){
			selectedGraphic = newSelGraphic;
			this.fireRequestInfoLine("Incident Location [" + incidentLoc.getLatitude() + ", "
				+ incidentLoc.getLongitude() + "]");
			repaint();
		}
		return true;
	}

	/**
	 * Called whenever the mouse is moved on this layer and one of the requested
	 * mouse modes is active, and the gesture is consumed by another active
	 * layer. We need to deselect anything that may be selected.
	 * 
	 * @see #getMouseModeServiceList //
	 */
	public void mouseMoved() {
		// this.getList().deselectAll();
		// // this.fireRequestInfoLine("");
		// repaint();
	}

	// /**
	// * Holds value of property selectingLocation.
	// */
	// private boolean selectingLocation;
	//
	// /**
	// * Utility field used by bound properties.
	// */
	// private java.beans.PropertyChangeSupport propertyChangeSupport = new
	// java.beans.PropertyChangeSupport(this);
	//
	// /**
	// * Adds a PropertyChangeListener to the listener list.
	// * @param l The listener to add.
	// */
	// public void addPropertyChangeListener(java.beans.PropertyChangeListener
	// l) {
	// propertyChangeSupport.addPropertyChangeListener(l);
	// }
	//
	// /**
	// * Removes a PropertyChangeListener from the listener list.
	// * @param l The listener to remove.
	// */
	// public void
	// removePropertyChangeListener(java.beans.PropertyChangeListener l) {
	// propertyChangeSupport.removePropertyChangeListener(l);
	// }
	//
	// /**
	// * Getter for property selectingLocation.
	// *
	// * @return Value of property selectingLocation.
	// */
	// public boolean isSelectingLocation() {
	// return this.selectingLocation;
	// }
	//
	// /**
	// * Setter for property selectingLocation.
	// *
	// * @param selectingLocation New value of property selectingLocation.
	// */
	// public void setSelectingLocation(boolean selectingLocation) {
	// boolean oldSelectingLocation = this.selectingLocation;
	// this.selectingLocation = selectingLocation;
	// // PropertyChangeListener[] listen =
	// propertyChangeSupport.getPropertyChangeListeners();
	// // for(PropertyChangeListener p : listen){
	// // System.err.println(p.toString());
	// // }
	// System.err.println("about to fire pc");
	// propertyChangeSupport.firePropertyChange ("layerSelLoc", new Boolean
	// (oldSelectingLocation), new Boolean (selectingLocation));
	// }

	/**
	 * Called when the Layer is removed from the MapBean, giving an opportunity
	 * to clean up.
	 * 
	 * @param cont
	 */
	@Override
	public void removed(Container cont) {
		OMGraphicList list = this.getList();
		if (list != null) {
			list.clear();
			list = null;
		}
	}

	public LatLonPoint getIncidentLoc() {
		return incidentLoc;
	}

	// <editor-fold defaultstate="collapsed" desc=" Old LocSelectStuff ">

	// public boolean isCanAddLoc() {
	// return canAddLoc;
	// }

	// public void setCanAddLoc(boolean canAddLoc) {
	// this.canAddLoc = canAddLoc;
	// }

	// </editor-fold>

	public void setIncidentLoc(LatLonPoint incidentLoc) {
		this.incidentLoc = incidentLoc;
		// canAddLoc = true;
		addIncidentLocation(incidentLoc.getLatitude(), incidentLoc.getLongitude());
		// canAddLoc = false;
	}

	public void setIncidentLoc(double lat, double lon) {
		this.incidentLoc = new LatLonPoint(lat, lon);
		// canAddLoc = true;
		addIncidentLocation((float) lat, (float) lon);
		// canAddLoc = false;
	}
}
