/*
 * Applied Science Associates, Inc.
 * Copyright 2007. All rights reserved.
 */

/*
 * AOIPropertyChangeListener.java
 *
 * Created on Mar 11, 2008, 4:15:39 PM
 *
 */

package com.asascience.openmap.utilities.listener;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

/**
 * 
 * @author CBM <cmueller@asascience.com>
 */
public abstract class AOIPropertyChangeListener implements PropertyChangeListener {
	public static final String AOI_CLEAR = "aoiclear";
	public static final String AOI_SAVE = "aoisave";
	public static final String AOI_APPLY = "aoiapply";
	public static final String AOI_REMALL = "aoiremall";
	public static final String AOI_MANUAL = "aoimanual";

	/** Creates a new instance of AOIPropertyChangeListener */
	public AOIPropertyChangeListener() {
	}

	abstract void saveAOI();

	abstract void applyAOI();

	abstract void clearCurrentAOI();

	abstract void removeAllAOI();

	abstract void enterManualAOI();

	public void propertyChange(PropertyChangeEvent e) {
		String name = e.getPropertyName();
		if (name.equals(AOIPropertyChangeListener.AOI_APPLY)) {
			applyAOI();
		} else if (name.equals(AOIPropertyChangeListener.AOI_SAVE)) {
			saveAOI();
		} else if (name.equals(AOIPropertyChangeListener.AOI_CLEAR)) {
			clearCurrentAOI();
		} else if (name.equals(AOIPropertyChangeListener.AOI_REMALL)) {
			removeAllAOI();
		} else if (name.equals(AOIPropertyChangeListener.AOI_MANUAL)) {
			enterManualAOI();
		}
	}
}
