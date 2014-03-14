package com.asascience.edc.map;

import gov.nasa.worldwind.geom.Position;
/*
 * @author CMorse
 */
public class BoundingBox {
	
	protected Position upperLeft;
	protected Position lowerRight;
	public BoundingBox(Position ul, Position lr){
		upperLeft = ul;
		lowerRight = lr;
		
	}
	public BoundingBox(){
		upperLeft = null;
		lowerRight = null;
	}
	public Position getUpperLeft() {
		return upperLeft;
	}
	public void setUpperLeft(Position upperLeft) {
		this.upperLeft = upperLeft;
	}
	public Position getLowerRight() {
		return lowerRight;
	}
	public void setLowerRight(Position lowerRight) {
		this.lowerRight = lowerRight;
	}

}
