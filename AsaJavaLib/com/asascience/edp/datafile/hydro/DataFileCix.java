/*
 * DataFileCix.java
 *
 * Created on August 2, 2007, 10:03 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package com.asascience.edp.datafile.hydro;

import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.TimeZone;

import com.asascience.edp.Cell;
import com.asascience.openmap.layer.asa.CixReader;
import com.asascience.utilities.Vector3D;

/**
 * 
 * @author cmueller
 */
public class DataFileCix extends DataFileBase {

	private CixReader cixReader;
	private int lSeq;
	private int numTimes;
	// private long[] timeSteps;
	private List<Cell> dataCells;
	// private List<ArrayList> dataCells;
	private List<ArrayList> data;

	/**
	 * Creates a new instance of DataFileCix
	 */
	public DataFileCix() {
	}

	private int getTimeIndex() {
		return cixReader.getTimeIndex(this.getQueryTime());

		// <editor-fold defaultstate="collapsed" desc=" pre CixReader ">
		// int ret = -1;
		// long qt = this.getQueryTime();
		//
		// for(int i = 0; i < timeSteps.length; i++){
		// if(qt <= timeSteps[timeSteps.length - 1]){
		// if(qt >= timeSteps[i]){
		// ret = i;
		// }
		// }
		// }
		//
		// return ret;
		// </editor-fold>
	}

	private int getCellIndex(Vector3D position) {
		return cixReader.getCellIndex(position);

		// <editor-fold defaultstate="collapsed" desc=" pre CixReader ">
		// int ret = -1;
		//
		// Cell c;
		// for(int i = 0; i < dataCells.size(); i++){
		// c = dataCells.get(i);
		// if(c.contains(position))//checks the horizontal position
		// {
		// return i;
		// }
		// }
		//
		// return ret;
		// </editor-fold>
	}

	// <editor-fold defaultstate="collapsed" desc=" pre CixReader ">
	// private void loadCIXGrid() {
	// int nBytes;
	// int ndMax;
	// int nRecL;
	// int nTime;
	// float dLon;
	// float dLat;
	// float oLon;
	// float oLat;
	// int nDummy;
	// float depth;
	// short i;
	// short j;
	// short level;
	// short subI;
	// short subJ;
	//
	// LERandomAccessFile raf = null;
	// try{
	// File f = new File(this.getDataFile());
	// if(f.exists()){
	// // System.out.println(f.getAbsolutePath());
	// raf = new LERandomAccessFile(f, "r");
	//
	// if(raf != null){
	//
	// raf.seek(0);
	// nRecL = raf.readInt();
	// ndMax = raf.readInt();
	// lSeq = raf.readInt();
	// raf.seek(42);
	// dLon = raf.readFloat();
	// dLat = raf.readFloat();
	// nDummy = raf.readInt();
	// raf.seek(56);
	// oLon = raf.readFloat();
	// oLat = raf.readFloat();
	// nTime = raf.readInt();
	// nBytes = nRecL * (6 + nDummy + nTime - 1);
	//
	// // System.out.println("nRecL="+nRecL);
	// // System.out.println("ndMax="+ndMax);
	// // System.out.println("lSeq="+lSeq);
	// // System.out.println("dLon="+dLon);
	// // System.out.println("dLat="+dLat);
	// // System.out.println("oLon="+oLon);
	// // System.out.println("oLat="+oLat);
	// // System.out.println("nTime="+nTime);
	// // System.out.println("nBytes="+nBytes);
	//
	// dataCells = new ArrayList<Cell>();
	// // dataCells = new ArrayList<ArrayList>();
	// // dataCells.add(0, new ArrayList<Cell>());
	//
	// data = new ArrayList<ArrayList>();
	// data.add(0, new ArrayList<Integer>());//type 1 or 9
	// data.add(1, new ArrayList<Float>());//depth
	// data.add(2, new ArrayList<Float>());//u
	// data.add(3, new ArrayList<Float>());//v
	//
	// data.get(0).clear();
	//
	// for(int n = 1; n <= lSeq; n++){
	// nBytes += nRecL;
	// raf.seek(nBytes);
	//
	// depth = raf.readFloat();
	// i = raf.readShort();
	// j = raf.readShort();
	// level = raf.readShort();
	// subI = raf.readShort();
	// subJ = raf.readShort();
	//
	// // System.out.println("depth="+depth);
	// // System.out.println("i="+i);
	// // System.out.println("j="+j);
	// // System.out.println("level="+level);
	// // System.out.println("subI="+subI);
	// // System.out.println("subJ="+subJ);
	//
	// if((level != 0) || (subI != 9)){
	// Cell myCell = new Cell();
	// if(level == 0){
	//
	// myCell.p1.setLocation(
	// (oLon + ((i - 1) * dLon)),
	// (oLat + ((j - 1) * dLat)));
	// myCell.p3.setLocation(
	// (myCell.p1.getX() + dLon),
	// (myCell.p1.getY() + dLat));
	//
	// }else{
	// double dlon_s = dLon / Math.pow(2, level);
	// double dlat_s = dLat / Math.pow(2, level);
	//
	// myCell.p1.setLocation(
	// (oLon + (dLon * (i - 1)) + (dlon_s * (subI - 1))),
	// (oLat + (dLat * (j - 1)) + (dlat_s * (subJ - 1))));
	// myCell.p3.setLocation(
	// (myCell.p1.getX() + dlon_s),
	// (myCell.p1.getY() + dlat_s));
	// }
	// myCell.p2.setLocation(myCell.p3.getX(), myCell.p1.getY());
	// myCell.p4.setLocation(myCell.p1.getX(), myCell.p3.getY());
	// // dataCells.get(0).add(myCell);
	// dataCells.add(myCell);
	// // data.get(0).add(1);
	// // data.get(1).add(depth);
	//
	// // System.out.println("p1: x="+myCell.p1.getX()+" y="+myCell.p1.getY());
	// // System.out.println("p2: x="+myCell.p2.getX()+" y="+myCell.p2.getY());
	// // System.out.println("p3: x="+myCell.p3.getX()+" y="+myCell.p3.getY());
	// // System.out.println("p4: x="+myCell.p4.getX()+" y="+myCell.p4.getY());
	// }else{
	// // dataCells.get(0).add(null);
	// dataCells.add(null);
	// // data.get(0).add(9);
	// // data.get(1).add((float)-999);
	// }
	// }
	// }
	// }
	// }catch(EOFException ex){
	// ex.printStackTrace();
	// }catch(IOException ex){
	// ex.printStackTrace();
	// }catch(Exception ex){
	// ex.printStackTrace();
	// }finally{
	// if(raf != null){
	// try{
	// raf.close();
	// }catch(IOException ex){
	// ex.printStackTrace();
	// }
	// }
	// }
	// }
	//
	// private void loadCIXTimes() {
	// LERandomAccessFile raf = null;
	// try{
	// File f = new File(this.getDataFile());
	// if(f.exists()){
	// // System.out.println(f.getAbsolutePath());
	// raf = new LERandomAccessFile(f, "r");
	//
	// if(raf != null){
	// raf.seek(0);
	//
	// int nRecL = raf.readInt();
	// raf.seek(64);
	// numTimes = raf.readInt();
	//
	// // System.out.println("numTimes="+numTimes);
	//
	// short sy, sm, sd, sh, smin;
	// raf.seek(70);
	// sy = raf.readShort();
	// sm = raf.readShort();
	// sd = raf.readShort();
	// sh = raf.readShort();
	// smin = raf.readShort();
	//
	// //
	// System.out.println("sy="+sy+" sm="+sm+" sd="+sd+" sh="+sh+" smin="+smin);
	//
	// //set the start calendar in the base class
	// this.startCal = Calendar.getInstance();
	// // TO DO: Fix this calendar issue properly
	// // subtracting 1 from month solves the problem. Calendar MONTH is 0
	// based.
	// // i.e. Calendar.DECEMBER = 11;
	// this.startCal.set(Calendar.MONTH, sm - 1);
	// this.startCal.set(Calendar.DAY_OF_MONTH, sd);
	// this.startCal.set(Calendar.YEAR, sy);
	// this.startCal.set(Calendar.HOUR_OF_DAY, sh);
	// this.startCal.set(Calendar.MINUTE, smin);
	// this.startCal.set(Calendar.SECOND, 0);
	// this.startCal.set(Calendar.MILLISECOND, 0);
	//
	// // System.out.println(startCal.getTime());
	//
	// // Calendar currCal = Calendar.getInstance();
	// long startTime = startCal.getTimeInMillis();
	// timeSteps = new long[numTimes];
	// float currVal;
	// for(int n = 1; n <= numTimes; n++){
	// int nBytes = (nRecL * (6 + n - 1)) + 4;
	// raf.seek(nBytes);
	// currVal = raf.readFloat();//in hours
	//
	// // System.out.println("currVal="+currVal);
	//
	// // currCal = startCal;
	// // currCal.add(Calendar.MINUTE, (int)(currVal * 60));
	// // timeSteps[n-1] = currCal.getTimeInMillis();
	// timeSteps[n - 1] = startTime + (long)(currVal * 60 * 60 * 1000);//convert
	// hours to ms
	// // System.out.println(timeSteps[n-1]);
	// }
	// }
	// }
	// }catch(EOFException ex){
	// ex.printStackTrace();
	// }catch(IOException ex){
	// ex.printStackTrace();
	// }catch(Exception ex){
	// ex.printStackTrace();
	// }finally{
	// if(raf != null){
	// try{
	// raf.close();
	// }catch(IOException ex){
	// ex.printStackTrace();
	// }
	// }
	// }
	// }
	// </editor-fold>

	@Override
	protected void loadDataFile() {
		cixReader = new CixReader(this.getDataFile());

		GregorianCalendar c = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
		c.setTimeInMillis(cixReader.getStartTime());
		setStartTime((GregorianCalendar) c.clone());
		c.setTimeInMillis(cixReader.getEndTime());
		setEndTime((GregorianCalendar) c.clone());
		this.timeIncrement = cixReader.getTimeIncrement();

		// <editor-fold defaultstate="collapsed" desc=" pre CixReader ">
		// loadCIXGrid();
		// loadCIXTimes();
		// </editor-fold>
	}

	@Override
	public Vector3D getCurrentAt(Vector3D position) {
		return cixReader.getCurrentAt(position, getTimeIndex(), getCellIndex(position));

		// <editor-fold defaultstate="collapsed" desc=" pre CixReader ">
		// Vector3D ret = null;
		// int tIndex = getTimeIndex();
		// // System.out.println("tIndex="+tIndex);
		// int cIndex = getCellIndex(position);
		// // System.out.println("cIndex="+cIndex);
		//
		// LERandomAccessFile raf = null;
		// File f = null;
		//
		// if(tIndex == -1 | cIndex == -1){
		// return ret;
		// }

		// try{
		// f = new File(this.getDataFile());
		// if(f.exists()){
		// // System.out.println(f.getAbsolutePath());
		// raf = new LERandomAccessFile(f, "r");
		//
		// if(raf != null){
		// int nRecL, nBases, nPotr, nBytes;
		// float elev;
		// float u;
		// float v;
		// float[] us;
		// float[] vs;
		//
		// raf.seek(0);
		// nRecL = raf.readInt();
		// if(nRecL <= 14){
		// nBases = 1;
		// }else{
		// nBases = (nRecL - 4) / 8;
		// }
		//
		// nBytes = nRecL * (6 + tIndex - 1);
		// raf.seek(nBytes);
		// nPotr = raf.readInt();
		//
		// us = new float[nBases];
		// vs = new float[nBases];
		//
		// nBytes = nRecL * (nPotr - 2);//byte pointer to beginning of timestep
		//
		// nBytes += nRecL * cIndex;
		// raf.seek(nBytes);//the cell of interest
		// elev = raf.readFloat();
		// //cycle through levels - max of 3, 1=surface
		// for(int i = 0; i < nBases; i++){
		// us[i] = raf.readFloat();
		// vs[i] = raf.readFloat();
		// }
		// u = us[0];
		// v = vs[0];
		//
		// ret = new Vector3D(u, v, 0);
		// }
		// }
		// }catch(Exception ex){
		// ex.printStackTrace();
		// }finally{
		// try{
		// f = null;
		// raf.close();
		// }catch(Exception ex){
		// ex.printStackTrace();
		// }
		// }
		// // System.out.println("curr: u="+ret.getU()+" v="+ret.getV());
		//
		// // ret=null;
		// return ret;
		// </editor-fold>
	}

	@Override
	public Double[] getLats() {
		List<Double> dbls = new ArrayList<Double>();
		for (double d : cixReader.getLats()) {
			if (!dbls.contains(d)) {
				dbls.add(d);
			}
		}
		return (Double[]) dbls.toArray(new Double[0]);
	}

	@Override
	public Double[] getLons() {
		List<Double> dbls = new ArrayList<Double>();
		for (double d : cixReader.getLons()) {
			if (!dbls.contains(d)) {
				dbls.add(d);
			}
		}
		return (Double[]) dbls.toArray(new Double[0]);
	}

	/**
	 * 
	 * @param position
	 * @return
	 */
	@Override
	public double[][] getUVTimeSeries(Vector3D position) {
		int ci = cixReader.getCellIndex(position);

		if (ci == -1) {
			return null;
		}
		return cixReader.extractTimeSeries(ci);

		// <editor-fold defaultstate="collapsed" desc=" Old Bad Method ">

		// double[][] retVals = null;
		// try{
		// long[] times = cixReader.getTimeSteps();
		// retVals = new double[2][times.length];
		// int timeI, cellI, counter = 0;
		// double u, v;
		//            
		// long go = System.currentTimeMillis();
		// long avgLoad = 0;
		//            
		// for(int i = 0; i < times.length; i++){
		// timeI = cixReader.getTimeIndex(times[i]);
		// cellI = cixReader.getCellIndex(position);
		// if(timeI > -1 & cellI > -1){
		//                    
		// long go2 = System.currentTimeMillis();
		//                    
		// cixReader.loadCIXStep(cixReader.getTimeSteps()[timeI]);
		//                    
		// avgLoad = avgLoad + (System.currentTimeMillis() - go2);
		//                    
		// u = cixReader.getU(timeI, cellI);
		// v = cixReader.getV(timeI, cellI);
		// retVals[0][i] = u;
		// retVals[1][i] = v;
		// // retVals[0][i] = (u != 0) ? u : Double.NaN;
		// // retVals[1][i] = (v != 0) ? v : Double.NaN;
		//                    
		// if(!Double.isNaN(retVals[0][i]) || !Double.isNaN(retVals[1][i])){
		// counter++;
		// }
		// }else{
		// retVals[0][i] = Double.NaN;
		// retVals[1][i] = Double.NaN;
		// }
		// }
		//            
		// System.out.println("Total LoadTime: " + avgLoad + " Avg LoadTime: " +
		// (avgLoad / times.length));
		// System.out.println("FOR LOOP Time: " + (System.currentTimeMillis() -
		// go));
		// if(counter < 2){
		// return null;
		// }
		//            
		//            
		// }catch(Exception ex){
		// ex.printStackTrace();
		// }
		// return retVals;
		// </editor-fold>
	}

	@Override
	public void disposeAll() {
	}
}
