/*
 * Applied Science Associates, Inc.
 * Copyright 2008. All Rights Reserved.
 *
 * CollapsiblePanel.java
 *
 * Created on Nov 14, 2008 @ 8:48:31 AM
 */

package com.asascience.ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.event.MouseListener;

import javax.swing.Action;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.UIManager;
import javax.swing.border.Border;

import org.jdesktop.swingx.JXCollapsiblePane;
import org.jdesktop.swingx.JXHyperlink;
import org.jdesktop.swingx.VerticalLayout;

/**
 * 
 * @author CBM <cmueller@asascience.com>
 */

public class CollapsiblePanel extends JPanel {
	public static final int TITLE_TOP = 0;
	public static final int TITLE_BOTTOM = 0;

	private Color titleBackgroundColor;
	private Color titleForegroundColor;
	private Color separatorColor;
	private Border separatorBorder;
	private JXCollapsiblePane collapsible;
	protected JToggleButton toggle;
	protected JXHyperlink link;

	public CollapsiblePanel(String title, boolean useButton) {
		this(title, useButton, TITLE_TOP);
	}

	public CollapsiblePanel(String title, boolean useButton, int titlePosition) {
		this(title, useButton, titlePosition, null);
	}

	/**
	 * 
	 * @param title
	 */
	public CollapsiblePanel(String title, boolean useButton, int titlePosition, Font titleFont) {
		setLayout(new VerticalLayout());
		setOpaque(true);
		// setBackground(Color.WHITE);

		separatorBorder = new SeparatorBorder();
		setTitleForegroundColor(Color.BLACK);
		// setTitleBackgroundColor(new Color(248, 248, 248));
		setTitleBackgroundColor(this.getBackground());
		// setSeparatorColor(new Color(214, 223, 247));
		setSeparatorColor(Color.BLACK);

		collapsible = new JXCollapsiblePane();
		// collapsible.getContentPane().setBackground(
		// AdempierePLAF.getFormBackground());
		// collapsible.setBorder(new CompoundBorder(separatorBorder, collapsible
		// .getBorder()));

		Action toggleAction = collapsible.getActionMap().get(JXCollapsiblePane.TOGGLE_ACTION);
		// use the collapse/expand icons from the JTree UI
		toggleAction.putValue(JXCollapsiblePane.COLLAPSE_ICON, UIManager.getIcon("Tree.expandedIcon"));
		toggleAction.putValue(JXCollapsiblePane.EXPAND_ICON, UIManager.getIcon("Tree.collapsedIcon"));

		if (titlePosition == 1) {
			super.add(collapsible);
		}

		if (useButton) {
			toggle = new JToggleButton(toggleAction);
			toggle.setText(title);
			toggle.setHorizontalAlignment(JToggleButton.LEFT);
			if (titleFont != null) {
				toggle.setFont(titleFont);
			}
			super.add(toggle);
		} else {
			link = new JXHyperlink(toggleAction);
			link.setText(title);
			link.setOpaque(true);
			link.setBackground(getTitleBackgroundColor());
			link.setFocusPainted(false);

			link.setUnclickedColor(getTitleForegroundColor());
			link.setClickedColor(getTitleForegroundColor());

			// link.setBorder(new CompoundBorder(separatorBorder, BorderFactory
			// .createEmptyBorder(2, 4, 2, 4)));
			// link.setBorderPainted(true);
			super.add(link);
		}
		if (titleFont != null) {
			setTitleFont(titleFont);
		}

		if (titlePosition == 0) {
			super.add(collapsible);
		}
	}

	public synchronized void addMouseListener(MouseListener l) {
		collapsible.addMouseListener(l);
		if (toggle != null) {
			toggle.addMouseListener(l);
		}
		if (link != null) {
			link.addMouseListener(l);
		}
	}

	public void setCollapsed(boolean isCollapsed) {
		boolean doIt = false;
		if (isCollapsed) {
			if (!collapsible.isCollapsed()) {
				doIt = true;
			}
		} else {
			if (collapsible.isCollapsed()) {
				doIt = true;
			}
		}

		if (doIt) {
			if (toggle != null) {
				toggle.doClick();
			}
			if (link != null) {
				link.doClick();
			}
		}
	}

	/**
	 * 
	 * @return color
	 */
	public Color getSeparatorColor() {
		return separatorColor;
	}

	/**
	 * 
	 * @param separatorColor
	 */
	public void setSeparatorColor(Color separatorColor) {
		this.separatorColor = separatorColor;
	}

	public Font getTitleFont() {
		if (toggle != null) {
			return toggle.getFont();
		} else {
			return link.getFont();
		}
	}

	public void setTitleFont(Font font) {
		if (toggle != null) {
			toggle.setFont(font);
		} else {
			link.setFont(font);
		}
	}

	/**
	 * get title foreground color
	 * 
	 * @return color
	 */
	public Color getTitleForegroundColor() {
		return titleForegroundColor;
	}

	/**
	 * Set title foreground color
	 * 
	 * @param titleForegroundColor
	 */
	public void setTitleForegroundColor(Color titleForegroundColor) {
		this.titleForegroundColor = titleForegroundColor;
	}

	/**
	 * 
	 * @return title background color
	 */
	public Color getTitleBackgroundColor() {
		return titleBackgroundColor;
	}

	/**
	 * Set background color of title
	 * 
	 * @param titleBackgroundColor
	 */
	public void setTitleBackgroundColor(Color titleBackgroundColor) {
		this.titleBackgroundColor = titleBackgroundColor;
	}

	/**
	 * Set title of the section
	 * 
	 * @param title
	 */
	public void setTitle(String title) {
		if (link != null)
			link.setText(title);
	}

	/**
	 * 
	 * @return collapsible pane
	 */
	public JXCollapsiblePane getCollapsiblePane() {
		return collapsible;
	}

	public JXHyperlink getToggleLink() {
		return link;
	}

	public JToggleButton getToggleButton() {
		return toggle;
	}

	/**
	 * The border between the stack components. It separates each component with
	 * a fine line border.
	 */
	class SeparatorBorder implements Border {

		boolean isFirst(Component c) {
			return c.getParent() == null || c.getParent().getComponent(0) == c;
		}

		public Insets getBorderInsets(Component c) {
			// if the collapsible is collapsed, we do not want its border to be
			// painted.
			if (c instanceof JXCollapsiblePane) {
				if (((JXCollapsiblePane) c).isCollapsed()) {
					return new Insets(0, 0, 0, 0);
				}
			}
			return new Insets(4, 0, 1, 0);
		}

		public boolean isBorderOpaque() {
			return true;
		}

		public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
			// if the collapsible is collapsed, we do not want its border to be
			// painted.
			if (c instanceof JXCollapsiblePane) {
				if (((JXCollapsiblePane) c).isCollapsed()) {
					return;
				}
			}
			g.setColor(getSeparatorColor());
			if (isFirst(c)) {
				g.drawLine(x, y + 2, x + width, y + 2);
			}
			g.drawLine(x, y + height - 1, x + width, y + height - 1);
		}
	}

	@Override
	public Component add(Component comp) {
		if (collapsible != null)
			return collapsible.add(comp);

		return null;
	}
}
