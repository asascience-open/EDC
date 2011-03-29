/*
 * Applied Science Associates, Inc.
 * Copyright 2009. All Rights Reserved.
 *
 * JCloseableTabbedPaneListener.java
 *
 * Created on Feb 11, 2009 @ 1:22:26 PM
 */

package com.asascience.ui;

/**
 *
 * @author CBM <cmueller@asascience.com>
 */

import java.util.EventListener;

/**
 * The listener that's notified when an tab should be closed in the
 * <code>CloseableTabbedPane</code>.
 */
public interface JCloseableTabbedPaneListener extends EventListener {
	/**
	 * Informs all <code>CloseableTabbedPaneListener</code>s when a tab should
	 * be closed
	 * 
	 * @param tabIndexToClose
	 *            the index of the tab which should be closed
	 * @return true if the tab can be closed, false otherwise
	 */
	boolean closeTab(int tabIndexToClose);
}
