
package com.asascience.edc.ui.combos;

import ucar.util.prefs.PersistenceManager;

/**
 * DirectComboBox.java
 * 
 * @author Kyle Wilcox <kwilcox@asascience.com>
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
