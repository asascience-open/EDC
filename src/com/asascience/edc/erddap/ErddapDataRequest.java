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
import java.util.List;

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
    
    public ErddapDataRequest(String homeDir, ErddapDataset erd) {
      this.homeDir = homeDir;
      this.erd = erd;
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

        Timer stopwatch = new Timer();
        File f = null;
                
        stopwatch.reset();

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
    
    
    public void getCsvForPolygon(){
        Timer stopwatch = new Timer();
        File f = null;
                
        stopwatch.reset();

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
  	        	System.out.println("filter by polygon " +filterByPolygon );
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
  	        				System.out.println("xindex " +xIndex+ " yindex " +yIndex);
  	        			}
  	        		}
  	        		else if(polygon != null){
  	        			System.out.println(currLine);
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
  	        					System.out.println("x " + x + " y" + y);
  	        					if(x != null && y != null && polygon.contains(x, y) ){
  	        						byte [] outB = currLine.getBytes();
  	        					    output.write(outB);

  	        						System.out.println("wrote line");
  	        						written += outB.length;
  	        					}
  	        					else{
  	        						System.out.println("not including");
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
    

      Timer stopwatch = new Timer();
      File f = null;
              
      stopwatch.reset();

      int written = 0;
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
      pcs.firePropertyChange("message", null, "- Completed " + written + " bytes in " + stopwatch.elapsedTime() + " seconds.");
      pcs.firePropertyChange("progress", null, 100);
      if (f != null) {
        pcs.firePropertyChange("done", null, f.getAbsolutePath());
      }
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