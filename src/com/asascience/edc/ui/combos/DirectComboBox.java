/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.asascience.edc.ui.combos;

import ucar.util.prefs.PersistenceManager;

/**
 *
 * @author Kyle
 */
public class DirectComboBox extends HistoryComboBox {

  protected static final String LIST = "DirectComboBox";

  public DirectComboBox(PersistenceManager prefs) {
    super(prefs, 20);
  }

  @Override
  protected String getList() {
    return LIST;
  }
  
}
