/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.asascience.sos;

import com.asascience.sos.requests.ResponseFormat;
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
