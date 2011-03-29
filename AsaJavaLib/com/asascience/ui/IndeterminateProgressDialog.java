/*
 * Applied Science Associates, Inc.
 * Copyright 2007. All rights reserved.
 */

/*
 * IndeterminateProgressDialog.java
 *
 * Created on Jan 1, 2008, 12:00:00 AM
 *
 */
package com.asascience.ui;

import java.awt.Frame;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.ImageIcon;
import javax.swing.JDialog;

import com.asascience.utilities.BaseTask;

/**
 * 
 * @author CBM <cmueller@asascience.com>
 */
public class IndeterminateProgressDialog extends JDialog implements PropertyChangeListener {
	public static final String PROGRESS = "progress";
	public static final String NOTE = "note";
	public static final String STATE = "state";
	public static final String CANCEL = "cancel";
	public static final String PROG_ERROR = "error";
	public static final String DONE = "done";
	public static final String CLOSE = "close";

	private IndeterminateProgressMonitor progressMonitor;
	private BaseTask runTask;
	private Frame parent;
	private ImageIcon monitorIcon;

	public IndeterminateProgressDialog(Frame parent, String title, ImageIcon monitorIcon) {
		super(parent, title, true);
		this.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
		this.parent = parent;
		this.monitorIcon = monitorIcon;
	}

	public void runTask() {
		progressMonitor = new IndeterminateProgressMonitor(parent, runTask.getName(), "", true, false, monitorIcon);

		runTask.addPropertyChangeListener(this);
		runTask.execute();
	}

	/**
	 * Invoked when task's progress property changes.
	 */
	public void propertyChange(PropertyChangeEvent evt) {
		// System.err.println("prop: "+evt.getPropertyName());
		if (progressMonitor.isUserCanceled()) {
			runTask.cancel(true);
		}
		if (evt.getPropertyName().equals("progress")) {
			progressMonitor.setProgress((Integer) evt.getNewValue());
		} else if (evt.getPropertyName().equals("note")) {
			if (!progressMonitor.isUserCanceled()) {
				progressMonitor.setNote((String) evt.getNewValue());
				progressMonitor.rePack();
			}
		} else if (evt.getPropertyName().equals("close")) {
			progressMonitor.close();
		}
	}

	public IndeterminateProgressMonitor getProgressMonitor() {
		return progressMonitor;
	}

	public void setRunTask(BaseTask runTask) {
		this.runTask = runTask;
	}
}
