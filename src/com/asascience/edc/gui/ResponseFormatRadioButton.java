package com.asascience.edc.gui;

import com.asascience.edc.sos.requests.ResponseFormat;
import javax.swing.JRadioButton;

/**
 * ResponseFormatRadioButton.java
 * 
 * @author Kyle Wilcox <kwilcox@asascience.com>
 */
public class ResponseFormatRadioButton extends JRadioButton {

  private ResponseFormat responseFormat;

  public void setResponseFormat(ResponseFormat r) {
    responseFormat = r;
  }

  public ResponseFormat getResponseFormat() {
    return responseFormat;
  }

}
