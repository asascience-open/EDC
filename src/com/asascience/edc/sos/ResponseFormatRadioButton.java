/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.asascience.edc.sos;

import com.asascience.edc.sos.requests.ResponseFormat;
import javax.swing.JRadioButton;

/**
 *
 * @author Kyle
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
