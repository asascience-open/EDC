/*
 * Applied Science Associates, Inc.
 * Copyright 2008. All Rights Reserved.
 *
 * ErrorDisplayDialog.java
 *
 * Created on Nov 20, 2008 @ 10:16:17 AM
 */

package com.asascience.ui;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JTextArea;

/**
 * 
 * @author CBM <cmueller@asascience.com>
 */

public class ErrorDisplayDialog {

	private static JDialog pane;

	public static void showErrorDialog(String message, Exception ex) {
		pane = new JDialog();
		if (message == null) {
			pane.setTitle("Error Dialog");
		} else {
			pane.setTitle(message);
		}
		pane.setModal(true);
		pane.setResizable(false);
		pane.setLocationByPlatform(true);
		pane.setAlwaysOnTop(true);
		pane.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

		JTextArea taError = new JTextArea();
		taError.setSize(300, 300);
		StringBuilder sb = new StringBuilder();
		sb.append(ex.toString());
		for (StackTraceElement se : ex.getStackTrace()) {
			sb.append("\n\tat ");
			sb.append(se.getClassName());
			sb.append(".");
			sb.append(se.getMethodName());
			sb.append("(");
			sb.append(se.getFileName());
			sb.append(":");
			sb.append((se.getLineNumber() == -1) ? "Unknown Source" : se.getLineNumber());
			sb.append(")");
		}
		taError.setText(sb.toString());
		pane.add(taError, BorderLayout.CENTER);

		JButton btnOK = new JButton("Close");
		btnOK.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				pane.dispose();
			}

		});
		pane.add(btnOK, BorderLayout.SOUTH);
		pane.pack();
		pane.setVisible(true);
	}

	public static void showErrorDialog(Exception ex) {
		showErrorDialog(null, ex);
	}
}
