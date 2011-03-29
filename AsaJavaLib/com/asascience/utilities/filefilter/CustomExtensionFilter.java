/*
 * Applied Science Associates, Inc.
 * Copyright 2008. All Rights Reserved.
 *
 * CustomExtensionFilter.java
 *
 * Created on Nov 18, 2008 @ 10:07:15 AM
 */

package com.asascience.utilities.filefilter;

import java.io.File;
import java.io.FilenameFilter;

/**
 * 
 * @author CBM <cmueller@asascience.com>
 */

public class CustomExtensionFilter implements FilenameFilter {

	private String extension;

	public CustomExtensionFilter(String extension) {
		this.extension = (extension.startsWith(".")) ? extension : "." + extension;
	}

	public boolean accept(File dir, String name) {
		return (name.endsWith(extension));
	}

}
