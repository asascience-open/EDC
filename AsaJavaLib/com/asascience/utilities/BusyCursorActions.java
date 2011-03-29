/*
 * BusyCursorActions.java
 *
 * Created on October 19, 2007, 8:23 AM
 *
 * Applied Science Associates, Inc.
 * Copyright 2007.  All rights reserved.
 */

package com.asascience.utilities;

import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.AbstractAction;
import javax.swing.JFrame;

/**
 * 
 * @author CBM
 */
public class BusyCursorActions {
	private final static Cursor busyCursor = Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR);
	private final static Cursor defaultCursor = Cursor.getDefaultCursor();

	/**
	 * Creates a new instance of BusyCursorActions
	 */
	public BusyCursorActions() {
	}

	public static ActionListener createListener(final JFrame frame, final ActionListener mainActionListener) {
		ActionListener actionListener = new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				try {
					frame.setCursor(busyCursor);
					mainActionListener.actionPerformed(ae);
				} finally {
					frame.setCursor(defaultCursor);
				}
			}
		};
		return actionListener;
	}

	public static AbstractAction createAbstractAction(final JFrame frame, final AbstractAction mainAbstractAction) {
		AbstractAction abstractAction = new AbstractAction() {
			public void actionPerformed(ActionEvent ae) {
				try {
					frame.setCursor(busyCursor);
					mainAbstractAction.actionPerformed(ae);
				} finally {
					frame.setCursor(defaultCursor);
				}
			}
		};
		return abstractAction;
	}

}
