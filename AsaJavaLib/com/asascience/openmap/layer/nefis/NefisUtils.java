/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * NefisUtils.java
 *
 * Created on Jun 11, 2008, 12:17:16 PM
 *
 */
package com.asascience.openmap.layer.nefis;

import java.util.List;

import com.asascience.openmap.layer.nefis.NefisFile.CellDef;
import com.asascience.openmap.layer.nefis.NefisFile.ElementDef;
import com.asascience.openmap.layer.nefis.NefisFile.GroupDef;

/**
 * 
 * @author cmueller_mac
 */
public class NefisUtils {

  public static final String D3D_COM = "Delft3D-com";
  public static final String D3D_TRIM = "Delft3D-trim";
  public static final String D3D_TRID = "Delft3D-trid";
  public static final String D3D_TRACK = "Delft3D-track";
  public static final String D3D_HWGXY = "Delft3D-hwgxy";
  public static final String D3D_TRAM = "Delft3D-tram";
  public static final String D3D_TRAH = "Delft3D-trah";
  public static final String D3D_TRIH = "Delft3D-trih";
  public static final String D3D_BOTM = "Delft3D-botm";
  public static final String D3D_BOTH = "Delft3D-both";
  public static final String D3D_BAGR = "Delft3D-bagr";
  public static final String SOBEK_M = "sobek-m";
  public static final String SOBEK_O = "sobek-o";
  public static final String SOBEK_R = "sobek-r";
  public static final String SKYLLA = "Skylla";
  public static final String PHAROS = "Pharos";
  public static final String UNKNOWN = "unknown";

  /**
   * Creates a new instance of NefisUtils
   *
   * @param nf
   * @return
   */
  // public NefisUtils(){
  //
  // }
  public static String determineSubType(NefisFile nf) {
    String ret = "";
    try {
      if (checkContent(nf, "com-version", "SIMDAT")) {
        ret = D3D_COM;
      } else if (checkContent(nf, "map-version", "SIMDAT") | checkContent(nf, "map-version", "FLOW-SIMDAT")) {
        ret = D3D_TRIM;
      } else if (checkContent(nf, "dro-version", null)) {
        ret = D3D_TRID;
      } else if (checkContent(nf, "trk-series", null)) {
        ret = D3D_TRACK;
      } else if (checkContent(nf, "map-series", "HSIGN")) {
        ret = D3D_HWGXY;
      } else if (checkContent(nf, "MAPATRANNTR", "NTR")) {
        ret = D3D_TRAM;
      } else if (checkContent(nf, "HISTRANNTRM", "NTRM")) {
        ret = D3D_TRAH;
      } else if (checkContent(nf, "his-version", "SIMDAT")) {
        ret = D3D_TRIH;
      } else if (checkContent(nf, "MAPBOT", "NTMBOT")) {
        ret = D3D_BOTM;
      } else if (checkContent(nf, "HISBOT", "NTHBOT")) {
        ret = D3D_BOTH;
      } else if (checkContent(nf, "MAPBGREF", null)) {
        ret = D3D_BAGR;// TODO: add checks at lines 38 - 51 of
        // vs_type...
      } else if (checkContent(nf, "PARSE-REL-GRP", "PARSE-REL")) {
        ret = SOBEK_M;
      } else if (checkContent(nf, "FLOW-DES-GROUP", "NO_TIMES_MAP")) {
        ret = SOBEK_O;
      } else if (checkContent(nf, "FLOW-RES-GROUP", "ISTEP")) {
        ret = SOBEK_R;
      } else if (checkContent(nf, "GEOMETRY", "ICOD")) {
        ret = SKYLLA;
      } else if (checkContent(nf, "GRID-coor", "X_coor")) {
        ret = PHAROS;
      } else {
        ret = UNKNOWN;
      }

    } catch (Exception ex) {
      ret = null;
    }

    return ret;
  }

  public static boolean checkContent(NefisFile nf, String groupName, String elementName) {
    GroupDef gdef = null;
    CellDef cdef = null;
    ElementDef edef = null;
    try {
      for (GroupDef g : nf.getGrpDef()) {
        if (g.getName().equals(groupName)) {
          gdef = g;
          break;
        }
      }
      if (gdef != null) {
        if (elementName == null) {
          return true;
        }
        int ci = gdef.getCelIndex();
        cdef = nf.getCelDef().get(ci);
        if (cdef != null) {
          List<Integer> elms = cdef.getElm();
          for (int ei : elms) {
            edef = nf.getElmDef().get(ei);
            if (edef.getName().equals(elementName)) {
              return true;
            }
          }
        }
      }

    } catch (Exception ex) {
    }

    return false;
  }
}
