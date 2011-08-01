package com.asascience.edc.sos.requests;

import java.io.File;

/**
 * SosRequestInterface.java
 * 
 * @author Kyle Wilcox <kwilcox@asascience.com>
 */
public interface SosRequestInterface {

  public String getResponseFormatValue();
  public void setResponseFormatValue(String s);
  public void getObservations(File savePath);
  
}
