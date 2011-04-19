/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.asascience.sos.requests.custom;

import com.asascience.sos.requests.GenericRequest;
import java.util.List;

/**
 *
 * @author Kyle
 */
public class DifToNetCDF extends GenericRequest {

  public DifToNetCDF(GenericRequest gr, List<String> formats) {
    super(gr);
    for (String s : formats) {
      if (s.contains("0.6.1") && !s.contains("post-process")) {
        type = s;
        break;
      }
    }
  }
}
