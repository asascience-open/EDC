/*
 * Applied Science Associates, Inc.
 * Copyright 2009. All Rights Reserved.
 *
 * GenericNetcdfLayer.java
 *
 * Created on Feb 10, 2009 @ 1:47:29 PM
 */

package com.asascience.openmap.layer.nc;

import com.asascience.openmap.layer.VectorLayer;

/**
 * 
 * @author CBM <cmueller@asascience.com>
 */

public class GenericNetcdfLayer extends VectorLayer {

	private GenericNetcdfReader reader;

	public GenericNetcdfLayer(String fileLoc) {
		try {
			reader = new GenericNetcdfReader(fileLoc);

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void drawDataForTime(long t) {
		// TODO Auto-generated method stub

	}

}
