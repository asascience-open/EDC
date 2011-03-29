/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * JActionLabel.java
 *
 * Created on Sep 4, 2008, 11:33:53 AM
 *
 */
package com.asascience.ui;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JLabel;

/**
 * 
 * @author cmueller_mac
 */
public class JActionLabel extends JLabel {

	private ActionListener al;
	private ActionEvent ae;
	private Color lc;
	private Color sc;

	/** Creates a new instance of JActionLabel */
	public JActionLabel(String text, ActionListener actionListener, String actionName, Color labelColor,
		Color selectColor) {
		super(text);
		this.lc = labelColor;
		this.sc = selectColor;
		this.setForeground(lc);
		ae = new ActionEvent(this.getText(), 0, (actionName == null || actionName.equals("")) ? "jactionlabel"
			: actionName);
		this.al = actionListener;
		addMouseListener();
	}

	/**
	 *Adds a feature to the MouseListener attribute of the JLinkLabel object
	 */
	public void addMouseListener() {
		// Add listener for the label clicks
		addMouseListener(new MouseListener() {

			public void mouseClicked(MouseEvent e) {
				if (al != null) {
					al.actionPerformed(ae);
				}
			}

			public void mousePressed(MouseEvent e) {
			}

			public void mouseReleased(MouseEvent e) {
			}

			public void mouseEntered(MouseEvent e) {
				JActionLabel.this.setForeground(sc);
				setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
			}

			public void mouseExited(MouseEvent e) {
				JActionLabel.this.setForeground(Color.BLUE);
				setCursor(Cursor.getDefaultCursor());
			}
		});

	}
}
