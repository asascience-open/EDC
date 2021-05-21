package com.asascience.edc.gui;

import javax.swing.JRadioButton;

import com.asascience.edc.sos.requests.ResponseFormat;

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
