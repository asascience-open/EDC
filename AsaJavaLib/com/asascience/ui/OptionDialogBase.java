/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * OptionDialogBase.java
 *
 * Created on May 19, 2008, 9:01:26 AM
 *
 */

package com.asascience.ui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.KeyStroke;

import net.miginfocom.swing.MigLayout;

/**
 * 
 * @author cmueller_mac
 */
public class OptionDialogBase extends JDialog implements ActionListener {
	private boolean acceptChanges = false;
	private JButton btnAccept;

	public OptionDialogBase() {
		this(null);
	}

	/**
	 * Creates a new instance of OptionDialogBase
	 * 
	 * @param title
	 */
	public OptionDialogBase(String title) {
		if (title != null) {
			this.setTitle(title);
		}
		this.setLayout(new MigLayout("fill"));
		this.setLocationByPlatform(true);
		this.setResizable(false);
		this.setModal(true);

		this.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
	}

	public JPanel buttonPanel(String acceptText) {
		return buttonPanel(acceptText, "Cancel");
	}

	public JPanel buttonPanel(String acceptText, String cancelText) {
		JPanel finishPanel = new JPanel(new MigLayout("center"));
		btnAccept = new JButton(acceptText);
		btnAccept.setActionCommand("save");
		btnAccept.addActionListener(this);

		this.getRootPane().setDefaultButton(btnAccept);

		JButton btnCancel = new JButton(cancelText);
		btnCancel.setActionCommand("cancel");
		btnCancel.addActionListener(this);

		finishPanel.add(btnAccept, "split 2");
		finishPanel.add(btnCancel);

		return finishPanel;
	}

	public void setAcceptEnabled(boolean isEnabled) {
		if (btnAccept != null) {
			btnAccept.setEnabled(isEnabled);
		}
	}

	protected JRootPane createRootPane() {
		ActionListener actionListener = new ActionListener() {

			public void actionPerformed(ActionEvent actionEvent) {
				acceptChanges = false;
				setVisible(false);
			}
		};
		JRootPane rPane = new JRootPane();
		KeyStroke stroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
		rPane.registerKeyboardAction(actionListener, stroke, JComponent.WHEN_IN_FOCUSED_WINDOW);
		return rPane;
	}

	public void actionPerformed(ActionEvent e) {
		String cmd = e.getActionCommand();
		if (cmd.equals("save")) {
			acceptChanges = true;
			this.setVisible(false);
		} else if (cmd.equals("cancel")) {
			acceptChanges = false;
			this.setVisible(false);
		}
	}

	public boolean acceptChanges() {
		return acceptChanges;
	}

	public void closeDialog() {
		this.dispose();
	}
}
