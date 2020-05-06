package com.asascience.edc.utils;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

import com.asascience.edc.map.view.BoundingBoxPanel;
import com.asascience.edc.map.view.WorldwindSelectionMap;

import ucar.util.prefs.PreferencesExt;

public class AoiUtils {
	private PreferencesExt mainPrefs;
	public static final String AOI_LIST = "aoilist";
	private ArrayList<String> aoiList;

	public AoiUtils(ucar.util.prefs.PreferencesExt pref){
		this.mainPrefs = pref;

	    // retrieve the aoiList from the preferences
	    aoiList = (ArrayList) mainPrefs.getList(AOI_LIST, null);
	    if(aoiList == null) {
	    	aoiList = new ArrayList<String>();
	    }
	    
	}
	
	public List<String> getAoiList(){
		return aoiList;
	}
	public PropertyChangeListener getPropertyChangeListener(
			final WorldwindSelectionMap mapPanel,final BoundingBoxPanel bboxGui) {
		PropertyChangeListener  pcl = new PropertyChangeListener() {

	        public void propertyChange(PropertyChangeEvent evt) {
	          if (evt.getPropertyName().equals("bboxchange")) {
	            mapPanel.makeSelectedExtentLayer(bboxGui.getBoundingBox());
	          }
	         // else if (evt.getPropertyName().equals("aoiapply")) {
	          //    applyAOI();
	           // } 
	          else if (evt.getPropertyName().equals("aoisave")) {
	        	  
	              saveAois((String) evt.getOldValue());
	              bboxGui.createAoiSubmenu(aoiList);
	            }
	          else if (evt.getPropertyName().equals("aoiremall")) {
	              removeAllAOI();
	              bboxGui.createAoiSubmenu(aoiList);
	              
	            } 
	         
	        }
	    };
	    return pcl;
	}
	public void removeAllAOI() {
		    if (mainPrefs != null) {
		    	aoiList.clear();
		    	mainPrefs.putList(AOI_LIST,aoiList);
		    }
	  }
	  
	  
	  public void saveAois(String newAoi) {
	    if (mainPrefs != null) {
	      aoiList.add(newAoi);
	      mainPrefs.putList(AOI_LIST,aoiList);
	    }
	  }

}
