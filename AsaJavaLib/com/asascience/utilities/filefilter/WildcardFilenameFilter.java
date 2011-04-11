/*
 * Applied Science Associates, Inc.
 * Copyright 2008. All Rights Reserved.
 *
 * WildcardFilenameFilter.java
 *
 * Created on Dec 3, 2008 @ 3:52:08 PM
 */
package com.asascience.utilities.filefilter;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Arrays;
import java.util.List;

/**
 * 
 * @author CBM <cmueller@asascience.com>
 */
public class WildcardFilenameFilter implements FilenameFilter {

  List<String> names;

  public WildcardFilenameFilter(String[] names) {
    this.names = Arrays.asList(names);
  }

  public boolean accept(File dir, String name) {
    for (String s : names) {
      if (s.contains("*")) {// is a wildcard
        if (name.contains(s.replace("*", ""))) {
          return true;
        }
      } else {
        if (s.equals(name)) {
          return true;
        }
      }
    }

    return false;
  }
}
