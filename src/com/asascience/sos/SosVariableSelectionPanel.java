/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.asascience.sos;

import com.asascience.edc.gui.SelectionPanelBase;
import com.asascience.edc.nc.NetcdfConstraints;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

/**
 *
 * @author Kyle
 */
public class SosVariableSelectionPanel extends SelectionPanelBase {

  public SosVariableSelectionPanel(NetcdfConstraints cons, SosProcessPanel parent) {
		this("", cons, parent);
	}

  public SosVariableSelectionPanel(String borderTitle, NetcdfConstraints cons, SosProcessPanel parent) {
		super(borderTitle, cons, parent);
		setPanelType(SelectionPanelBase.GENERAL);
		createPanel();

		getCblVars().addPropertyChangeListener(new CheckBoxPropertyListener());
		// getCblVars().addPropertyChangeListener(new PropertyChangeListener(){
		// public void propertyChange(PropertyChangeEvent e){
		// System.err.println("propName="+e.getPropertyName());
		// if(getCblVars().getSelItemsSize() > 0){
		// setProcessEnabled(true);
		// }else{
		// setProcessEnabled(false);
		// }
		// }
		// });

		constraints.setTrimByIndex(-1);
		constraints.setTrimByDim("null");
	}

  class CheckBoxPropertyListener implements PropertyChangeListener {
		public void propertyChange(PropertyChangeEvent e) {
      
    }
	}

}
