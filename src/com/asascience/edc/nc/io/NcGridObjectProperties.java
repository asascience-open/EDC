package com.asascience.edc.nc.io;

import java.util.ArrayList;
import java.util.List;

import com.asascience.edc.ArcType;

public class NcGridObjectProperties {
	protected String ncPath;
	protected String startTime;
	protected String timeInterval;
	protected String timeUnits;
	protected String time;
	protected List<String> times;
	protected String xCoord;
	protected String yCoord;
	protected String zCoord;
	protected String trimByValue;
	protected String trimByDim;
	protected String bandDim;
	protected String uVar;
	protected String vVar ;
	protected String tVar;
	protected String projection;
	protected String isZPositive;
	protected ArcType type;
	protected List<String> vars;


	public NcGridObjectProperties(){
		ncPath = "";
		startTime = "";
		timeInterval = "";
		timeUnits = "";
		time = "";
		times = new ArrayList<String>();
		xCoord = "";
		yCoord = "";
		zCoord = "";
		trimByValue = "";
		trimByDim = "";
		bandDim = "";
		uVar = "";
		vVar = "";
		tVar = "";
		projection = "";
		isZPositive = "";
		type = ArcType.NULL;

		vars = new ArrayList<String>();
	}

	public List<String> getVars() {
		return vars;
	}

	public void setVars(List<String> vars) {
		this.vars = vars;
	}
	public String getNcPath() {
		return ncPath;
	}

	public void setNcPath(String path) {
		this.ncPath = path;
	}

	public String getStartTime() {
		return startTime;
	}

	public void setStartTime(String startTime) {
		this.startTime = startTime;
	}

	public String getTimeInterval() {
		return timeInterval;
	}

	public void setTimeInterval(String timeInterval) {
		this.timeInterval = timeInterval;
	}

	public String getTimeUnits() {
		return timeUnits;
	}

	public void setTimeUnits(String timeUnits) {
		this.timeUnits = timeUnits;
	}

	public String getTime() {
		return time;
	}

	public void setTime(String time) {
		this.time = time;
	}

	public String getXCoord() {
		return xCoord;
	}

	public void setXCoord(String xCoord) {
		this.xCoord = xCoord;
	}

	public String getYCoord() {
		return yCoord;
	}

	public void setYCoord(String yCoord) {
		this.yCoord = yCoord;
	}

	public String getBandDim() {
		return bandDim;
	}

	public void setBandDim(String bandDim) {
		this.bandDim = bandDim;
	}

	public ArcType getType() {
		return type;
	}

	public void setType(ArcType type) {
		this.type = type;
	}
	public String getUVar() {
		return uVar;
	}

	public void setUVar(String uVar) {
		this.uVar = uVar;
	}

	public String getVVar() {
		return vVar;
	}

	public void setVVar(String vVar) {
		this.vVar = vVar;
	}
	public String getTVar() {
		return tVar;
	}

	public void setTVar(String tVar) {
		this.tVar = tVar;
	}

	public String getZCoord() {
		return zCoord;
	}

	public void setZCoord(String zCoord) {
		this.zCoord = zCoord;
	}

	public String getProjection() {
		return projection;
	}

	public void setProjection(String projection) {
		this.projection = projection;
	}

	public String getIsZPositive() {
		return isZPositive;
	}

	public void setIsZPositive(String isZPositive) {
		this.isZPositive = isZPositive;
	}

	public String getTrimByValue() {
		return trimByValue;
	}

	public void setTrimByValue(String trimByValue) {
		this.trimByValue = trimByValue;
	}

	public String getTrimByDim() {
		return trimByDim;
	}

	public void setTrimByDim(String trimByDim) {
		this.trimByDim = trimByDim;
	}

	public List<String> getTimes() {
		return times;
	}

	public void setTimes(List<String> times) {
		this.times = times;
	}
}
