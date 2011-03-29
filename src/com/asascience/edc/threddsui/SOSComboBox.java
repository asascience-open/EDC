// $Id: ComboBox.java,v 1.6 2005/08/22 01:12:28 caron Exp $
/*
 * Copyright 1997-2004 Unidata Program Center/University Corporation for
 * Atmospheric Research, P.O. Box 3000, Boulder, CO 80307,
 * support@unidata.ucar.edu.
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or (at
 * your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser
 * General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, write to the Free Software Foundation,
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */

package com.asascience.edc.threddsui;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;

import ucar.util.prefs.PreferencesExt;
import ucar.util.prefs.ui.ComboBox;
import ucar.util.prefs.ui.Field;
import ucar.util.prefs.PersistenceManager;
import ucar.util.prefs.ui.PrefPanel;

/**
 * A simple extension to JComboBox, which persists the n latest values. The
 * JComboBox is editable; user can add a new String, then if acceptable, the
 * calling routine should transform it to correct object type and call
 * addItem(). When item is added, it is placed on the top of the list, so it is
 * more likely to be saved.
 * <p>
 * The items in the list can be any Object type with these caveats:
 * <ul>
 * <li>item.toString() used as display name
 * <li>item.equals() used for object equality
 * <li>prefs.putBeanObject() used for storage, so XMLEncoder used, so object
 * must have no-arg Constructor.
 * </ul>
 * 
 * When listening for change events, generally key on type comboBoxChanged, and
 * you must explicitly decide to save it in the list:
 * 
 * <pre>
 *  cb.addActionListener(new ActionListener() {
 *       public void actionPerformed(ActionEvent e) {
 *         if (e.getActionCommand().equals(&quot;comboBoxChanged&quot;)) {
 *           Object select = cb.getSelectedItem());
 *           if (isOK( select))
 *             cb.addItem( select);
 *         }
 *       }
 *     });
 * </pre>
 * 
 * @see Field.TextCombo
 * @see PrefPanel#addTextComboField
 * 
 * @author John Caron
 * @version $Id: ComboBox.java,v 1.6 2005/08/22 01:12:28 caron Exp $
 */

public class SOSComboBox extends JComboBox {
	private static final String LIST = "SOSComboBoxList";

	private boolean deleting = false;

	private PersistenceManager prefs;
	private int nkeep = 20;

	/**
	 * Constructor.
	 * 
	 * @param prefs
	 *            get/put list here; may be null.
	 */
	public SOSComboBox(PersistenceManager prefs) {
		this(prefs, 20);
	}

	/**
	 * Constructor.
	 * 
	 * @param prefs
	 *            get/put list here; may be null.
	 * @param nkeep
	 *            keep this many when you save.
	 */
	public SOSComboBox(PersistenceManager prefs, int nkeep) {
		super();
		this.prefs = prefs;
		this.nkeep = nkeep;
		setEditable(true);

		if (prefs != null) {
			ArrayList list = (ArrayList) prefs.getList(LIST, null);
			setItemList(list);
		}
	}

	public JComponent getDeepEditComponent() {
		return (JComponent) getEditor().getEditorComponent();
	}

	private JPopupMenu popupMenu;

	public void addContextMenu() {
		Component editComp = getEditor().getEditorComponent();
		popupMenu = new JPopupMenu();
		editComp.addMouseListener(new PopupTriggerListener() {
			public void showPopup(java.awt.event.MouseEvent e) {
				popupMenu.show(SOSComboBox.this, e.getX(), e.getY());
			}
		});

		AbstractAction deleteAction = new AbstractAction() {
			public void actionPerformed(java.awt.event.ActionEvent e) {
				int index = getSelectedIndex();
				deleting = true;
				if (index >= 0)
					removeItemAt(index);
				deleting = false;
			}
		};
		deleteAction.putValue(Action.NAME, "Delete");
		popupMenu.add(deleteAction);

		AbstractAction deleteAllAction = new AbstractAction() {
			public void actionPerformed(java.awt.event.ActionEvent e) {
				setItemList(new ArrayList());
			}
		};
		deleteAllAction.putValue(Action.NAME, "Delete All");
		popupMenu.add(deleteAllAction);

	}

	private static abstract class PopupTriggerListener extends MouseAdapter {
		public void mouseReleased(MouseEvent e) {
			if (e.isPopupTrigger())
				showPopup(e);
		}

		public abstract void showPopup(MouseEvent e);
	}

	protected void fireActionEvent() {
		if (deleting)
			return; // no events while deleting
		super.fireActionEvent();
	}

	/**
	 * Add the item to the top of the list. If it already exists, move it to the
	 * top.
	 * 
	 * @param item
	 *            to be added.
	 */
	public void addItem(Object item) {
		if (item == null)
			return;
		for (int i = 0; i < getItemCount(); i++) {
			if (item.equals(getItemAt(i))) {
				if (i == 0)
					return; // already there
				removeItemAt(i);
			}
		}

		// add as first in the list
		insertItemAt(item, 0);
		setSelectedIndex(0);
	}

	/** Save the last n items to PreferencesExt. */
	public void save() {
		// System.err.println("Saving DAComboBox");
		if (prefs != null)
			prefs.putList(LIST, getItemList());
	}

	/**
	 * Use this to obtain the list of items.
	 * 
	 * @return ArrayList of items, may be any Object type.
	 */
	public ArrayList getItemList() {
		ArrayList list = new ArrayList();
		for (int i = 0; i < getItemCount() && i < nkeep; i++)
			list.add(getItemAt(i));
		return list;
	}

	/**
	 * Use this to set the list of items.
	 * 
	 * @param list
	 *            of items, may be any Object type.
	 */
	public void setItemList(Collection list) {
		if (list == null)
			return;
		setModel(new DefaultComboBoxModel(list.toArray()));

		if (list.size() > 0)
			setSelectedIndex(0);
	}

	/** Set the number of items to keep */
	public void setNkeep(int nkeep) {
		this.nkeep = nkeep;
	}

	/** Get the number of items to keep */
	public int getNkeep() {
		return nkeep;
	}

	/** Get value from Store, will be an ArrayList or null */
	protected Object getStoreValue(Object defValue) {
		if (prefs == null)
			return defValue;
		return ((PreferencesExt) prefs).getBean(LIST, defValue);
	}

	/** Put new value into Store, must be a List of Strings */
	protected void setStoreValue(List newValue) {
		if (prefs != null)
			prefs.putList(LIST, newValue);
	}

	// debug
	private static long lastEvent;

	public static void main(String args[]) throws IOException {

		JFrame frame = new JFrame("Test");
		frame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				System.exit(0);
			}
		});

		final ComboBox cb = new ComboBox(null);
		cb.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				System.err.println("**** cb event=" + e);
				if (e.getActionCommand().equals("comboBoxChanged")) {
					System.err.println("cb.getSelectedItem=" + cb.getSelectedItem());
					cb.addItem(cb.getSelectedItem());
				}
			}
		});
		cb.getEditor().getEditorComponent().setForeground(Color.red);

		/*
		 * JButton butt = new JButton("accept"); butt.addActionListener( new
		 * AbstractAction() { public void actionPerformed(ActionEvent e) {
		 * System.err.println("butt accept"); cb.accept(); } });
		 */

		JPanel main = new JPanel();
		main.add(cb);
		// main.add(butt);

		frame.getContentPane().add(main);
		// cb.setPreferredSize(new java.awt.Dimension(500, 200));

		frame.pack();
		frame.setLocation(300, 300);
		frame.setVisible(true);
	}

}