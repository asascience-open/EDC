/*
 * Applied Science Associates, Inc.
 * Copyright 2007. All rights reserved.
 */

/*
 * InitializationFailedException.java
 *
 * Created on Mar 18, 2008, 2:54:13 PM
 *
 */
package com.asascience.utilities.exception;

/**
 * 
 * @author CBM <cmueller@asascience.com>
 */
public class InitializationFailedException extends Exception {

  /** Creates a new instance of InitializationFailedException */
  public InitializationFailedException() {
    super();
  }

  public InitializationFailedException(String s) {
    super(s);
  }
}
