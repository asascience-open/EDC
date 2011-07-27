package com.asascience.edc.ui.combos;

import ucar.util.prefs.PersistenceManager;

/**
 * ErddapComboBox.java
 * 
 * @author Kyle Wilcox <kwilcox@asascience.com>
 */
public class ErddapComboBox extends HistoryComboBox {

  protected static final String LIST = "ErddapComboBox";

  public ErddapComboBox(PersistenceManager prefs) {
    super(prefs, 20);
  }

  @Override
  protected String getList() {
    return LIST;
  }
}
