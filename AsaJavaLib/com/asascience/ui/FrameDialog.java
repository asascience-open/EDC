/*
 * Applied Science Associates, Inc.
 * Copyright 2007. All rights reserved.
 */

/*
 * FrameDialog.java
 *
 * Created on Mar 25, 2008, 12:59:19 PM
 *
 */
package com.asascience.ui;

import javax.swing.JDialog;
import javax.swing.JFrame;

/**
 * 
 * @author CBM <cmueller@asascience.com>
 */
public class FrameDialog extends JDialog {

	public FrameDialog() {
	}

	public JDialog getDialog(JFrame frame, boolean isResizable, boolean isModal) {
		this.setLocationByPlatform(true);
		this.setResizable(isResizable);
		this.setModal(isModal);
		this.setRootPane(frame.getRootPane());
		this.setBounds(frame.getBounds());

		return this;
	}
}
