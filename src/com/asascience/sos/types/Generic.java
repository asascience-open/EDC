/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.asascience.sos.types;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.transform.JDOMResult;
import org.jdom.transform.JDOMSource;
import com.asascience.sos.SensorContainer;
import com.asascience.sos.VariableContainer;
import ucar.unidata.geoloc.LatLonRect;
import ucar.unidata.geoloc.LatLonPoint;
import ucar.unidata.geoloc.LatLonPointImpl;

/**
 *
 * @author Kyle
 */
public class Generic implements SOSTypeInterface {

  protected List<SensorContainer> sensorList;
  protected Date guiStartTime;
  protected Date guiEndTime;
  protected int selectedSensorCnt;
  protected int[] selectedVarCnt;
  protected double[] NESW;
  protected Date startTime;
  protected Date endTime;
  protected String[] varNames;
  protected SimpleDateFormat dateFormatter;
  protected String sosURL;
  protected String homeDir;
  protected Document getCapDoc;
  protected String type;
  protected double timeInterval;
  protected int numTimeSteps;

  public Generic(Document xmlDoc) {
    getCapDoc = xmlDoc;
  }

  public void setSelectedSensors(double[] guiNESW, Date guiStartDate, Date guiEndDate, List<String> guiSelectedVars) {

    // System.out.println("sensorList.size()=" + sensorList.size());

    // Reset Counters
    selectedSensorCnt = 0;
    selectedVarCnt = null;
    selectedVarCnt = new int[guiSelectedVars.size()];

    guiStartTime = guiStartDate;
    guiEndTime = guiEndDate;

    for (SensorContainer sensor : sensorList) {

      // Reset sensor object
      sensor.setSelected(false);

      // Select based on Time
      if (sensor.getStartTime().after(guiEndDate) || sensor.getEndTime().before(guiStartDate)) {
        continue;
      }

      // Select based on position

      // North-South
      if (sensor.getNESW()[0] < guiNESW[2]) {
        continue;
      }

      if (sensor.getNESW()[2] > guiNESW[0]) {
        continue;
      }

      // East-West - I Think I got all the corner cases ?
      if (sensor.getNESW()[1] >= sensor.getNESW()[3] && guiNESW[1] >= guiNESW[3]) {
        // West is less than East (no Date line)
        if (sensor.getNESW()[3] > guiNESW[1]) {
          continue;
        }

        if (sensor.getNESW()[1] < guiNESW[3]) {
          continue;
        }

      } else if (sensor.getNESW()[1] >= sensor.getNESW()[3] && guiNESW[1] < guiNESW[3]) {
        // Gui selection Crosses the dateline
        if (sensor.getNESW()[3] > guiNESW[1] && sensor.getNESW()[1] < guiNESW[3]) {
          continue;
        }

      } else if (sensor.getNESW()[1] < sensor.getNESW()[3] && guiNESW[1] >= guiNESW[3]) {
        // The Sensor Box Crosses the DateLine!
        if (sensor.getNESW()[3] > guiNESW[1] && sensor.getNESW()[1] < guiNESW[3]) {
          continue;
        }

      } // else if (sensor.getNESW()[1] < sensor.getNESW()[3] &&
      // guiNESW[1] < guiNESW[3]) {
      // The Both Boxes Cross the DateLine! - If both cross they must
      // overlap!
      // if (sensor.getNESW()[3] > guiNESW[1] &&
      // sensor.getNESW()[1] < guiNESW[3]) {
      // continue;
      // }
      // } // End If

      for (VariableContainer varObj : sensor.getVarList()) {

        // Reset variable object
        varObj.setSelected(false);

        int ind = 0;
        for (String varName : guiSelectedVars) {

          if (varName.equalsIgnoreCase(varObj.getName())) {
            varObj.setSelected(true);
            selectedVarCnt[ind]++;
            break;
          }

          ind++;

        }

        // Only change Sensor selected if it is not already selected...
        if (!sensor.isSelected()) {
          if (varObj.isSelected()) {
            sensor.setSelected(true);
            selectedSensorCnt++;
          }
        }

      }

    } // End For Sensor list
  }

  protected List<Element> transform(Document doc, Document xsldoc) throws JDOMException {
    try {
      // Transformer transformer = TransformerFactory.newInstance()
      // .newTransformer(new StreamSource(stylesheet));

      JDOMSource xslSource = new JDOMSource(xsldoc);

      // Transformer transformer =
      // TransformerFactory.newInstance().newTransformer(new
      // StreamSource(xsldoc));
      Transformer transformer = TransformerFactory.newInstance().newTransformer(xslSource);

      JDOMSource in = new JDOMSource(doc);
      JDOMResult out = new JDOMResult();

      transformer.transform(in, out);
      // Document outDoc = out.getDocument();
      // if (null != outDoc) {
      // try {
      // XMLOutputter outputter = new
      // XMLOutputter(Format.getPrettyFormat());
      // Writer writer = new FileWriter(new
      // File("/home/dstuebe/test.xml"));
      // outputter.output(outDoc, writer);
      // } catch (IOException e) {
      // System.out.println("couldnt output transformed xml");
      // }
      // }
      // return null;
      return out.getResult();
    } catch (TransformerException e) {
      throw new JDOMException("XSLT Transformation failed", e);
    }
  }

  public String getType() {
    return type;
  }

  public boolean parseSensors() {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  public void getObservations() {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  public void setHomeDir(String homeDir) {
    this.homeDir = homeDir;
  }

  public void setURL(String URL) {
    sosURL = URL;
  }

  public Date getStartTime() {
    return startTime;
  }

  public Date getEndTime() {
    return endTime;
  }

  public double[] getNESW() {
    return NESW;
  }

  public double getTimeInterval() {
    return timeInterval;
  }

  public int getNumTimeSteps() {
    return numTimeSteps;
  }

  public String[] getvarNames() {
    return varNames;
  }

  public int getSelectedSensorCnt() {
    return selectedSensorCnt;
  }

  public LatLonRect getBBOX() {
    LatLonPointImpl uL = new LatLonPointImpl(NESW[0], NESW[3]);
    LatLonPointImpl lR = new LatLonPointImpl(NESW[2], NESW[1]);
    LatLonRect llr = new LatLonRect(uL, lR);
    return llr;
  }
}
