/*
 * Applied Science Associates, Inc.
 * Copyright 2007. All rights reserved.
 */

/*
 * ImagePanel.java
 *
 * Created on Apr 23, 2008, 9:47:22 AM
 *
 */

package com.asascience.ui;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;

import javax.swing.ImageIcon;
import javax.swing.JPanel;

/**
 * 
 * @author CBM <cmueller@asascience.com>
 */
public class ImagePanel extends JPanel {
	private Image img;

	public ImagePanel(String img) {
		this(new ImageIcon(img).getImage());
	}

	/**
	 * Creates a new instance of ImagePanel
	 * 
	 * @param img
	 */
	public ImagePanel(Image img) {
		this.img = img;
		Dimension size = new Dimension(img.getWidth(null), img.getHeight(null));
		setPreferredSize(size);
		setMinimumSize(size);
		setMaximumSize(size);
		setSize(size);
		setLayout(null);
	}

	@Override
	public void paintComponent(Graphics g) {
		g.drawImage(img, 0, 0, null);
	}
}
