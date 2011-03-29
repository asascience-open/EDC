package com.asascience.openmap.layer;

/*
 * TimeLayer.java
 *
 * Created on October 31, 2007, 9:35 AM
 *
 * Applied Science Associates, Inc.
 * Copyright 2007.  All rights reserved.
 */

import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.TimeZone;

import org.apache.commons.lang.ArrayUtils;

import com.asascience.utilities.Utils;

/**
 * 
 * @author CBM
 */
public abstract class TimeLayer extends ASALayer {

	private long startTime = Long.MAX_VALUE;
	private long endTime = Long.MIN_VALUE;
	private long currentTime;
	private long timeIncrement;
	private boolean layerManuallyOn = true;
	private boolean hasTimes = true;
	private Long[] times = null;

	/** Creates a new instance of TimeLayer */
	public TimeLayer() {
	}

	/**
	 * 
	 * @return
	 */
	@Override
	public boolean receivesMapEvents() {
		return false;
	}

	// TODO: Figure out feedback for animation loop
	// this could be the ticket to effective animation!!
	// need to use property change support (yuck) to fire this up to the
	// timeLayerHandler
	// which needs to wait unti all the layers have repainted and then trigger
	// the next
	// timestep to go
	// NO GOOD - this still cannot keep tabs on how long the graphics take to
	// draw on the screen
	// only when the call is made. Problematic for large layers that take a
	// while to render...
	@Override
	public void repaint() {
		super.repaint();
		// System.err.println("repainted");
	}

	public abstract void drawDataForTime(long t);

	public void advanceTime(long newTime) {
		newTime = correctTime(newTime);
		if (timeIsValid(newTime)) {
			if (isLayerManuallyOn()) {
				this.setVisible(true);
				currentTime = newTime;
				drawDataForTime(currentTime);
			} else {
				this.setVisible(false);
			}
		} else {
			this.setVisible(false);
		}

	}

	private long correctTime(long time) {
		/** Always draw layers with only 1 timestep. */
		if (times.length == 1) {
			return times[0];
		}
		int index = Utils.closestPrevious(times, time, timeIncrement);
		if (index != -1) {
			return times[index];
		} else {
			return time;
		}
	}

	public void setTimes(long[] times) {
		this.times = ArrayUtils.toObject(times);
	}

	public void setTimes(Long[] times) {
		this.times = times;
	}

	public Long[] getTimes() {
		return this.times;
	}

	public List<Long> getUniqueTimes() {
		List<Long> ret = new ArrayList<Long>();
		long currT = getStartTime();
		long endT = getEndTime();
		while (currT <= endT) {
			ret.add(currT);
			currT += timeIncrement;
		}

		return ret;
	}

	public boolean timeIsValid(long t) {
		// System.err.println(startTime + "<=" + t + "=>" + endTime);
		if ((t >= startTime) & (t <= endTime)) {
			return true;
		}
		return false;
	}

	public void setTimeRange(long st, long et) {
		setStartTime(st);
		setEndTime(et);
	}

	public void setStartTime(long t) {
		startTime = t;
	}

	public long getStartTime() {
		return startTime;
	}

	public GregorianCalendar getStartCal() {
		GregorianCalendar c = new GregorianCalendar(TimeZone.getTimeZone("UTC"));// Calendar.getInstance();

		c.setTimeInMillis(startTime);
		return c;
	}

	public void setEndTime(long t) {
		this.endTime = t;
	}

	public long getEndTime() {
		return endTime;
	}

	public GregorianCalendar getEndCal() {
		GregorianCalendar c = new GregorianCalendar(TimeZone.getTimeZone("UTC"));//

		c.setTimeInMillis(endTime);
		return c;
	}

	public void setTimeIncrement(long ti) {
		this.timeIncrement = ti;
		hasTimes = true;
		// if(this.timeIncrement == 0){
		// hasTimes = false;
		// }
	}

	public long getTimeIncrement() {
		return timeIncrement;
	}

	public boolean isLayerManuallyOn() {
		return layerManuallyOn;
	}

	public void setLayerManuallyOn(boolean layerManuallyOn) {
		this.layerManuallyOn = layerManuallyOn;
	}

	public long getCurrentTime() {
		/**
		 * If the current time hasn't been initialized (i.e. is 0), set it equal
		 * to the startTime.
		 */
		if (currentTime == 0) {
			currentTime = startTime;
		}
		return currentTime;
	}

	public boolean isHasTimes() {
		return hasTimes;
	}
}
