/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * CustomFilenameFilter.java
 *
 * Created on Oct 17, 2008, 9:42:41 AM
 *
 */

package com.asascience.utilities.filefilter;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Arrays;
import java.util.List;

/**
 * 
 * @author cmueller_mac
 */
public class CustomFilenameFilter implements FilenameFilter {

	List<String> names;

	/** Creates a new instance of CustomFilenameFilter */
	public CustomFilenameFilter(String[] names) {
		this.names = Arrays.asList(names);
	}

	public boolean accept(File file, String name) {
		if (names.contains(name)) {
			return true;
		}
		return false;
	}
}
