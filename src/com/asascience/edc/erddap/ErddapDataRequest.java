/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.asascience.edc.erddap;

import cern.colt.Timer;
import com.asascience.edc.Configuration;
import com.asascience.edc.CsvProperties;
import com.asascience.edc.utils.CsvFileUtils;
import com.asascience.edc.utils.FileSaveUtils;
import com.jmatio.io.MatFileReader;
import com.jmatio.io.MatFileWriter;
import com.jmatio.types.MLArray;
import com.jmatio.types.MLCell;
import com.jmatio.types.MLChar;
import com.jmatio.types.MLDouble;
import com.jmatio.types.MLInt16;
import com.jmatio.types.MLInt32;
import com.jmatio.types.MLInt64;
import com.jmatio.types.MLInt8;
import com.jmatio.types.MLNumericArray;
import com.jmatio.types.MLSingle;
import com.jmatio.types.MLSparse;
import com.jmatio.types.MLStructure;

import gov.nasa.worldwind.render.PointPlacemark;

import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JFrame;

import org.jfree.util.Log;

/**
 *
 * @author kwilcox
 */
public class ErddapDataRequest implements PropertyChangeListener {

    private String baseUrl;
    private String responseFormat = ".htmlTable";
    private String parameters;
  
    private boolean filterByPolygon;
    private List<String> selectedStationLocations;
    private Path2D.Double polygon;
    private PropertyChangeSupport pcs;
    private String homeDir;
    private JFrame parent;
    private File saveFile;
    private ErddapDataset erd;
    private int written;
    private    Timer stopwatch;
    public ErddapDataRequest(String homeDir, ErddapDataset erd) {
      this.homeDir = homeDir;
      this.erd = erd;
      stopwatch  = new Timer();
      selectedStationLocations = new ArrayList<String>();
      
      pcs = new PropertyChangeSupport(this);
    }

    public void setParent(JFrame parent) {
      this.parent = parent;
    }

    public JFrame getParent() {
      return parent;
    }

    public String getHomeDir() {
      return homeDir;
    }

    public String getBaseUrl() {
      return baseUrl;
    }
    
    public void setResponseFormat(String responseFormat) {
      this.responseFormat = responseFormat;
    }

    public void setParameters(String parameters) {
      this.parameters = parameters;
    }

    public File getComputedSaveFile() {
      saveFile = new File(FileSaveUtils.chooseFilename(new File(homeDir + File.separator + FileSaveUtils.getNameAndDateFromUrl(baseUrl) + File.separator), erd.getId() + responseFormat));
      return saveFile;
    }
    
    public File getUpdatedSaveFile() {
      return new File (FileSaveUtils.getFilePathNoSuffix(saveFile.getAbsolutePath()) + responseFormat);
    }
    
    public File getSaveFile() {
      try {
        return getUpdatedSaveFile();
      } catch(NullPointerException npe) {
        return null;
      }
    }

    public void setSaveFile(File saveFile) {
      this.saveFile = saveFile;
    }
    
    public String getFilename() {
      return saveFile.getName();
    }
    
    public void setBaseUrl(String baseUrl) {
      this.baseUrl = baseUrl;
    }

    public String getResponseFormat() {
      return responseFormat;
    }
    
    public String getRequestUrl() {
      return baseUrl + responseFormat + "?" + parameters;
    }

    public String getRequestUrl(Double stationLat, Double stationLon) {
        return baseUrl + responseFormat + "?" + parameters;
      }
    public void propertyChange(PropertyChangeEvent evt) {
      pcs.firePropertyChange(evt);
    }
    
    public void addPropertyChangeListener(PropertyChangeListener l) {
      pcs.addPropertyChangeListener(l);
    }

    public void removePropertyChangeListener(PropertyChangeListener l) {
      pcs.removePropertyChangeListener(l);
    }
    
    
    public void getDataForPolygon(){

        File f = null;
                
        stopwatch.reset();
        stopwatch.start();
        int written = 0;
        try {
        	// Increment the file
        	File incrementFile = new File(FileSaveUtils.chooseFilename(getSaveFile().getParentFile(), getSaveFile().getName()));
        	f = incrementFile;
        	OutputStream output = new BufferedOutputStream(new FileOutputStream(f));
        	for(String currStationQuery : selectedStationLocations){
        		URL u = new URL(getRequestUrl() + currStationQuery );

        		// Add the 
        		pcs.firePropertyChange("message", null, "- Making Request (" + getRequestUrl() + currStationQuery +")");


        		pcs.firePropertyChange("message", null, "- Streaming Results to File: " + incrementFile.getAbsolutePath());
        		int currWrote;
        		currWrote = writeData(output, u);
        		pcs.firePropertyChange("message", null, "- Wrote " + currWrote +" bytes");
        		written += currWrote;
        	}
        	output.close();
        	// Now write the properties file if we need to (ARC)
        	writeArcFile( f);
        } catch (MalformedURLException e) {
      	  e.printStackTrace();

          pcs.firePropertyChange("message", null, "- BAD URL, skipping sensor");
        } catch (IOException io) {
      	  io.printStackTrace();
      	  if(written == 0)
      		  pcs.firePropertyChange("message", null, "- NO DATA available for selected range: " + io.getMessage());
      	  else
      	       writeArcFile( f);
        }
        catch(Exception e){
      	  pcs.firePropertyChange("message", null, "Exception " + e.getMessage());
      	  e.printStackTrace();
        }
        pcs.firePropertyChange("message", null, "- Completed " + written + " bytes in " + stopwatch.elapsedTime() + " seconds.");
        pcs.firePropertyChange("progress", null, 100);
        if (f != null) {
          pcs.firePropertyChange("done", null, f.getAbsolutePath());
        }
    }
    
    
    public int writeData(OutputStream output, URL u){
        int written = 0;

    	try {  
    	HttpURLConnection ht = (HttpURLConnection) u.openConnection();
          ht.setDoInput(true);
          ht.setRequestMethod("GET");
	        InputStream is = ht.getInputStream();
	        byte[] buffer = new byte[2048];
	        int len = 0;
	        written = 0;
	        try{
	        while (-1 != (len = is.read(buffer))) {
	          output.write(buffer, 0, len);
	          written += len;
	        }
	        }
	        catch(Exception e){
	        	e.printStackTrace();
	        }
	        is.close();
	        output.flush();
	        
		} catch (ProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	return written;
    }
    
    
    public void filterMatlabForPolygon(){
    	
    	
    	
    }
    
    public void getCsvForPolygon(){
        File f = null;
                
        stopwatch.reset();
        stopwatch.start();
        int written = 0;
        try {
          URL u = new URL(getRequestUrl());
          pcs.firePropertyChange("message", null, "- Making Request (" + getRequestUrl() + ")");
   
          // Increment the file
          File incrementFile = new File(FileSaveUtils.chooseFilename(getSaveFile().getParentFile(), getSaveFile().getName()));
          pcs.firePropertyChange("message", null, "- Streaming Results to File: " + incrementFile.getAbsolutePath());
          f = incrementFile;
          OutputStream output = new BufferedOutputStream(new FileOutputStream(f));
      	HttpURLConnection ht = (HttpURLConnection) u.openConnection();
          ht.setDoInput(true);
          ht.setRequestMethod("GET");
  	        InputStream is = ht.getInputStream();
  	        InputStreamReader isReader = new InputStreamReader(is);
  	       BufferedReader bufReader = new BufferedReader(isReader);
  	        int len = 0;
  	        written = 0;
  	        try{
  	        	int currLineNum = 0;
  	        	int xIndex = -1;
  	        	int yIndex = -1;
  	        	String currLine;
  	        	while ((currLine = bufReader.readLine()) !=  null){
  	        		currLine = currLine + "\n";
  	        		if(currLineNum < 2 || !filterByPolygon){
  	        			byte [] outB = currLine.getBytes();
  					    output.write(outB);
  						written += outB.length;

  	        			if(currLineNum == 0 && filterByPolygon){
  	        				int currIndex = 0;
  	        				for(String tok : currLine.split(",") ){
  	        					if(tok.equals(this.erd.getX().getName())){
  	        						xIndex = currIndex;
  	        					}
  	        					else if(tok.equals(this.erd.getY().getName())){
  	        						yIndex = currIndex;
  	        					}
  	        					currIndex++;
  	        				}
  	        			}
  	        		}
  	        		else if(polygon != null){
  	        			// make sure the current data is within the polygon
  	        			if(xIndex >= 0 && yIndex >= 0){
  	        					String[] decoded = currLine.split(",");
  	        					Double x = null;
  	        					Double y = null;
  	        					if(xIndex < decoded.length){
  	        						try{
  	        							x = Double.valueOf(decoded[xIndex]);
  	        						}
  	        						catch(NumberFormatException e){
  	        							Log.error(e.getMessage());
  	        							e.printStackTrace();
  	        						}
  	        					}
  	        					if(yIndex < decoded.length){
  	        						try{
  	        							y = Double.valueOf(decoded[yIndex]);
  	        						}
  	        						catch(NumberFormatException e){
  	        							Log.error(e.getMessage());
  	        							e.printStackTrace();
  	        						}
  	        					}
  	        					if(x != null && y != null && polygon.contains(x, y) ){
  	        						byte [] outB = currLine.getBytes();
  	        					    output.write(outB);

  	        						written += outB.length;
  	        					}
  	        					
  	        				}
  	        			
  	        		}
  	        		output.flush();
  	        		currLineNum++;
  	        	}
  	        }
  	        catch(Exception e){
  	        	e.printStackTrace();
  	        }
  	        is.close();
  	        output.flush();
          output.close();
          // Now write the properties file if we need to (ARC)
          if(written > 0)
          	writeArcFile( f);
        } catch (MalformedURLException e) {
      	  e.printStackTrace();

          pcs.firePropertyChange("message", null, "- BAD URL, skipping sensor");
        } catch (IOException io) {
      	  io.printStackTrace();
      	  if(written == 0)
      		  pcs.firePropertyChange("message", null, "- NO DATA available for selected range: " + io.getMessage());
      	  else
      	       writeArcFile( f);
        }
        catch(Exception e){
      	  pcs.firePropertyChange("message", null, "Exception " + e.getMessage());
      	  e.printStackTrace();
        }
        pcs.firePropertyChange("message", null, "- Completed " + written + " bytes in " + stopwatch.elapsedTime() + " seconds.");
        pcs.firePropertyChange("progress", null, 100);
        if (f != null) {
          pcs.firePropertyChange("done", null, f.getAbsolutePath());
        }
    }
    
    
    public void getData() {
    
    	Integer written = new Integer(0);
    	File f = getDataNow();
      pcs.firePropertyChange("message", null, "- Completed " + written + " bytes in " + stopwatch.elapsedTime() + " seconds.");
      pcs.firePropertyChange("progress", null, 100);
      if (f != null) {
        pcs.firePropertyChange("done", null, f.getAbsolutePath());
      }
    }
    
    
    private File getDataNow(){
        File f = null;
                
        stopwatch.reset();
        stopwatch.start();
    
        try {
          URL u = new URL(getRequestUrl());
          pcs.firePropertyChange("message", null, "- Making Request (" + getRequestUrl() + ")");
   
          // Increment the file
          File incrementFile = new File(FileSaveUtils.chooseFilename(getSaveFile().getParentFile(), getSaveFile().getName()));
          pcs.firePropertyChange("message", null, "- Streaming Results to File: " + incrementFile.getAbsolutePath());
          f = incrementFile;
          OutputStream output = new BufferedOutputStream(new FileOutputStream(f));
          written = writeData(output, u);
    
          output.close();
          // Now write the properties file if we need to (ARC)
          if(written > 0)
          	writeArcFile( f);
        } catch (MalformedURLException e) {
      	  e.printStackTrace();

          pcs.firePropertyChange("message", null, "- BAD URL, skipping sensor");
        } catch (IOException io) {
      	  io.printStackTrace();
      	  if(written == 0)
      		  pcs.firePropertyChange("message", null, "- NO DATA available for selected range: " + io.getMessage());
      	  else
      	       writeArcFile( f);
        }
        catch(Exception e){
      	  pcs.firePropertyChange("message", null, "Exception " + e.getMessage());
      	  e.printStackTrace();
        }
        return f;
    }
    
	public void getMatlabDataForPolygon(){
    	File f = getDataNow();
    	if(f != null && f.exists()){// && written > 0){
    		try {
				MatFileReader matReader = new MatFileReader(f.getAbsolutePath());
				Map<String, MLArray> content = matReader.getContent();
				if(content != null){
					
					MLArray dataset = content.get(this.erd.getId());
				
				if(dataset != null && dataset.isStruct()){
			
						MLStructure struct = (MLStructure) dataset;
						MLArray xVar = struct.getField((this.erd.getX().getName()));
						MLArray yVar = struct.getField((this.erd.getY().getName()));
						
						Map<String, ArrayList<Object>>  varInPolygonDataMap = new HashMap<String, ArrayList<Object>>();
						Map<String, MLArray> varArrayMap = new HashMap<String, MLArray>();
						for(ErddapVariable var : this.erd.getVariables()){
							String varName = var.getName();
							MLArray varArray = struct.getField(varName);
							if(varArray != null){
								varInPolygonDataMap.put(varName, new ArrayList<Object>());
								varArrayMap.put(varName, varArray);
							}
						}
						
						if(xVar != null && yVar != null){
							
							 MLNumericArray<?> xVarArray = (MLNumericArray<?>)xVar;
							 MLNumericArray<?> yVarArray = (MLNumericArray<?>)yVar;

							 for(int currIndex = 0; currIndex < xVarArray.getSize(); currIndex++){
							 
								 
								 	Double x, y;
								 	x = Double.valueOf(String.valueOf(xVarArray.get(currIndex)));
								 	y = Double.valueOf(String.valueOf(yVarArray.get(currIndex)));
								 	
								

									if(x != null && y != null && polygon.contains(x, y) ){
										
										for(ErddapVariable var : this.erd.getVariables()){
											String varName = var.getName();

											MLArray varArray = varArrayMap.get(varName);
											if(varArray != null) {
												if(varArray instanceof MLNumericArray){
													varInPolygonDataMap.get(var.getName()).add(
															((MLNumericArray<?>)varArray).get(currIndex));
											
												}
												else if(varArray instanceof MLChar){
													varInPolygonDataMap.get(var.getName()).add(
															((MLChar)varArray).getString(currIndex));
												}
												else if(varArray instanceof MLCell){
													varInPolygonDataMap.get(var.getName()).add(
															((MLCell)varArray).get(currIndex));
												}
											}
										}
										
									}
						}
						
							
					}
						MLStructure trimmedStruct = new MLStructure(dataset.name, dataset.getDimensions());
					
						for(String varName : 	varInPolygonDataMap.keySet()){
							MLArray varArray = getNewArrayForVar(varArrayMap.get(varName), 	varInPolygonDataMap.get(varName));

							if(varArray !=  null)
								trimmedStruct.setField(varName, varArray);
						}
						ArrayList<MLArray> structList = new ArrayList<MLArray>();
						structList.add(trimmedStruct);
						new MatFileWriter(f.getAbsolutePath(), structList);
				}
				
				
				
				
				
				}
			} catch (IOException e) {
				Log.error("Error writing .mat file");
				Log.warn("Exception writing .mat file ", e);
			
    		}

    	}
    	   pcs.firePropertyChange("message", null, "- Completed " + written + " bytes in " + stopwatch.elapsedTime() + " seconds.");
    	      pcs.firePropertyChange("progress", null, 100);
    	      if (f != null) {
    	        pcs.firePropertyChange("done", null, f.getAbsolutePath());
    	      }
    }
    
    
    private MLArray getNewArrayForVar(MLArray oldArray, ArrayList<Object> varArrayList){
    	MLArray varArray = null;
    	int sizeArray = varArrayList.size();
    		int type = oldArray.getType();
    		switch(type){
    		case MLArray.mxDOUBLE_CLASS:
    			double[][] doubleArray = new double[sizeArray][1];
    			for(int i = 0; i < sizeArray; i++){
    				doubleArray[i][0] = (double)(varArrayList.get(i));
    			}
    			varArray = new MLDouble(oldArray.name, doubleArray);
    			break;
    		case MLArray.mxINT16_CLASS:
    		case MLArray.mxUINT16_CLASS:

    			short[][] shortArray = new short[sizeArray][1];
    			for(int i = 0; i < sizeArray; i++){
    				
    				shortArray[i][0] = (short)varArrayList.get(i);
    			}
    			varArray = new MLInt16(oldArray.name, shortArray);
    			break;
    		case MLArray.mxINT8_CLASS:
    		case MLArray.mxUINT8_CLASS:

    			byte[][] byteArray = new byte[sizeArray][1];
    			for(int i = 0; i < sizeArray; i++){
    				
    				byteArray[i][0] = (byte)varArrayList.get(i);
    			}
    			varArray = new MLInt8(oldArray.name, byteArray);
    			break;
    		
    		case MLArray.mxINT32_CLASS:
    		case MLArray.mxUINT32_CLASS:


    			int[][] intArray = new int[sizeArray][1];
    			for(int i = 0; i < sizeArray; i++){
    				
    				intArray[i][0] = (int)varArrayList.get(i);
    			}
    			varArray = new MLInt32(oldArray.name, intArray);
    			
    			break;
    		case MLArray.mxINT64_CLASS:
    		case MLArray.mxUINT64_CLASS:

    			long[][] longArray = new long[sizeArray][1];
    			for(int i = 0; i < sizeArray; i++){
    				
    				longArray[i][0] = (long)varArrayList.get(i);
    			}
    			varArray = new MLInt64(oldArray.name, longArray);
    			break;
 
    		case MLArray.mxSINGLE_CLASS:
    			Float[] floatArray = new Float[sizeArray];
    			for(int i = 0; i < sizeArray; i++){
    				floatArray[i] = (Float)(varArrayList.get(i));
    			}
    			varArray = new MLSingle(oldArray.name, floatArray, sizeArray);
    			break;
    
    		case MLArray.mxSPARSE_CLASS:
    			int dim[] = new int[1];
				dim[0] = sizeArray;
    			varArray = new MLSparse(oldArray.name, dim, type, 0);
    			for(int i = 0; i < sizeArray; i++){
    				((MLSparse)varArray).setReal((double)varArrayList.get(i), i);
    			}
    			break;
    		case MLArray.mxCHAR_CLASS:
    			String[] strArray = new String[sizeArray];
    			for(int i = 0; i < sizeArray; i++){
    				
    				strArray[i] = (String)varArrayList.get(i);
    			}
    			varArray = new MLChar(oldArray.name, strArray);
    			break;
    		
    		case MLArray.mxCELL_CLASS:
    			dim = new int[1];
				dim[0] = sizeArray;
    			varArray = new MLCell(oldArray.name, dim);
    			for(int i = 0; i < sizeArray; i++){
    				((MLCell) varArray).set((MLArray)varArrayList.get(i), i);
    			}
    			break;
    		}    		
    		
    	
   
    	 return varArray;
    	
    }
    private void writeArcFile( File f ){
    	// Now write the properties file if we need to (ARC)
    	if (Configuration.DISPLAY_TYPE == Configuration.DisplayType.ESRI) {
    	      CsvProperties properties;

    		// We can assume a CSV file here, because we are in ESRI mode
    		// We need the date and times to be NOT zulu (go figure).
    		String timeHeader = null;
    		if(erd.getTime() != null)
    			timeHeader =	erd.getTime().getName();
    		try {
    			CsvFileUtils.convertToEsri(f, timeHeader);

    			properties = new CsvProperties();
    			properties.setPath(f.getAbsolutePath());
    			properties.setSuffix("csv");
    			if (erd.hasZ()) {
    				properties.setZHeader(erd.getZ().getName());
    			}
    			if (erd.hasCdmAxis()) {
    				properties.setIdHeader(erd.getCdmAxis().getName());
    			}
    			if (erd.hasX()) {
    				properties.setLonHeader(erd.getX().getName());
    			}
    			if (erd.hasY()) {
    				properties.setLatHeader(erd.getY().getName());
    			}
    			if (erd.hasTime()) {
    				properties.setTimeHeader(erd.getTime().getName());
    			}
    			if (erd.hasCdmDataType()) {
    				properties.setCdmFeatureType(erd.getCdmDataType());
    			}
    			if(erd.getTime() != null)
    				properties.setTimesteps(CsvFileUtils.getTimesteps(f, erd.getTime().getName()));
    			properties.setVariableHeaders(CsvFileUtils.getVariables(f, properties.getHeaders()));
    			properties.writeFile();
    		} catch (IOException e) {
    			// TODO Auto-generated catch block
    			e.printStackTrace();
    		} catch(ArrayIndexOutOfBoundsException e2){
    			e2.printStackTrace();
    		}
    	}
    }

	public List<String> getSelectedStationLocations() {
		return selectedStationLocations;
	}

	public void setSelectedStationLocations(List<String> selectedStationLocations) {
		this.selectedStationLocations = selectedStationLocations;
	}

	public Path2D.Double getPolygon() {
		return polygon;
	}

	public void setPolygon(Path2D.Double polygon) {
		this.polygon = polygon;
	}


	public boolean isFilterByPolygon() {
		return filterByPolygon;
	}

	public void setFilterByPolygon(boolean filterByPolygon) {
		this.filterByPolygon = filterByPolygon;
	}
  }