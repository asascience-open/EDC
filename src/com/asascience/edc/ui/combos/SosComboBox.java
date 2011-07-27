package com.asascience.edc.ui.combos;

import ucar.util.prefs.PersistenceManager;

/**
 * SosComboBox.java
 * 
 * @author Kyle Wilcox <kwilcox@asascience.com>
 */
public class SosComboBox extends HistoryComboBox {

  protected static final String LIST = "SosComboBox";

  public SosComboBox(PersistenceManager prefs) {
    super(prefs, 20);
  }

  @Override
  protected String getList() {
    return LIST;
  }
}
