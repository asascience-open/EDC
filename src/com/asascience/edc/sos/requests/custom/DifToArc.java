/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.asascience.edc.sos.requests.custom;


import org.jdom.Document;
import cern.colt.Timer;
import com.asascience.edc.sos.SensorContainer;
import com.asascience.edc.sos.requests.GenericRequest;
import com.asascience.edc.utils.FileSaveUtils;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import org.jdom.JDOMException;
import org.jdom.Text;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.jdom.transform.JDOMResult;
import org.jdom.transform.JDOMSource;

/**
 *
 * @author Kyle
 */
public class DifToArc extends GenericRequest {

  public DifToArc(GenericRequest gr) {
    super(gr);
  }

  protected List<Text> transform(Document doc, Document xsldoc) throws JDOMException {
    try {
      JDOMSource xslSource = new JDOMSource(xsldoc);

      Transformer transformer = TransformerFactory.newInstance().newTransformer(xslSource);

      JDOMSource in = new JDOMSource(doc);
      JDOMResult out = new JDOMResult();

      transformer.transform(in, out);

      return out.getResult();
    } catch (TransformerException e) {
      throw new JDOMException("XSLT Transformation failed", e);
    }
  }

  @Override
  public void getObservations() {

    double numSens = getSelectedSensorCount();
    double countSens = 0;
    String requestURL;
    List filenames = new ArrayList<String>();

    File savePath = FileSaveUtils.chooseSavePath(parentFrame, homeDir, sosURL);

    // Are we loading an XSL?
    String schemaLoc = "/resources/schemas/ioos_gmlv061_to_arc.xsl";
    SAXBuilder xslBuilder = new SAXBuilder();
    Document xslDoc = null;
    try {
      pcs.firePropertyChange("message", null, "Loading DIF to CSV XSL Schema from " + schemaLoc);
      xslDoc = xslBuilder.build(this.getClass().getResourceAsStream(schemaLoc));
    } catch (JDOMException e) {
      pcs.firePropertyChange("message", null, "XSL is not well-formed");
      return;
    } catch (IOException e) {
      pcs.firePropertyChange("message", null, "XSL at: " + schemaLoc + "; is inaccessible");
      return;
    }

    Timer stopwatch = new Timer();
    SAXBuilder difBuilder;
    Document difDoc;

    for (SensorContainer sensor : selectedSensors) {
      stopwatch.reset();
      stopwatch.start();
      pcs.firePropertyChange("message", null, "Sensor: " + sensor.getName());
      pcs.firePropertyChange("message", null, "- Building Request String");

      requestURL = buildRequest(sensor);

      difBuilder = new SAXBuilder(); // parameters control
      difDoc = null;
      try {
        pcs.firePropertyChange("message", null, "- Making Request (" + requestURL + ")");
        difDoc = difBuilder.build(requestURL);
      } catch (JDOMException e) {
        pcs.firePropertyChange("message", null, "- SOS at: " + requestURL + "; is not well-formed");
        continue;
      } catch (IOException e) {
        pcs.firePropertyChange("message", null, "- SOS at: " + sosURL + "; is inaccessible");
        continue;
      }
      
      try {
        XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());
        Writer writer = new StringWriter(4 * 10 ^ 7); // 40 mb
        outputter.output(difDoc, writer);
        float mysize = writer.toString().length();
        pcs.firePropertyChange("message", null, "- Size of SOS Observations request (mB)= ~" + mysize / 1000000.0);
      } catch (IOException e) {
        pcs.firePropertyChange("message", null, "- Could not get the size of the xml document");
      }
      
      stopwatch.start();

      List<Text> myList = null;
      try {
        pcs.firePropertyChange("message", null, "- Transforming XML to CSV");
        myList = transform(difDoc, xslDoc);
        if (!myList.get(0).getText().substring(0, 20).contains("No")) {
          String filename = chooseFilename(savePath, sensor.getName());
          Writer fstream = new FileWriter(new File(filename));
          pcs.firePropertyChange("message", null, "- Streaming transformed results to file");
          BufferedWriter out = new BufferedWriter(fstream);
          out.write(myList.get(0).getText());
          out.close();
          filenames.add(filename);
        } else {
          pcs.firePropertyChange("message", null, "- No data returned at this station for selected parameters!");
        }
      } catch (IOException e) {
        pcs.firePropertyChange("message", null, "- Could not output transformed XML");
      } catch (JDOMException e) {
        pcs.firePropertyChange("message", null, "- JDOM tranform failed!");
      }
      countSens++;
      pcs.firePropertyChange("message", null, "- Completed in " + stopwatch.elapsedTime() + " seconds");
      int prog = Double.valueOf(countSens / numSens * 100).intValue();
      pcs.firePropertyChange("progress", null, prog);
    } // End Sensor List
    pcs.firePropertyChange("progress", null, 100);
    pcs.firePropertyChange("done", null, filenames.toString());
  }
}
