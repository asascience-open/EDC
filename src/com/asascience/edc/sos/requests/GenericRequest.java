package com.asascience.edc.sos.requests;

import cern.colt.Timer;
import com.asascience.edc.sos.SensorContainer;
import com.asascience.edc.sos.map.SensorPoint;
import com.asascience.edc.sos.VariableContainer;
import com.asascience.edc.utils.FileSaveUtils;
import gov.nasa.worldwind.render.PointPlacemark;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.swing.JFrame;
import org.apache.commons.lang.StringUtils;

/**
 * GenericRequest.java
 * 
 * @author Kyle Wilcox <kwilcox@asascience.com>
 */
public class GenericRequest extends SosRequest  {


  public GenericRequest(String url) {
	  super(url);
    fileSuffix = "txt";
    
  }

  public GenericRequest(SosRequest gr) {
    super(gr);
    fileSuffix = "txt";

  }
  



  
  public void getObservations(File savePath) {

    double numSens = getSelectedSensorCount();
    double countSens = 0;
    String requestURL;
    ArrayList<String> filenames = new ArrayList<String>();

    Timer stopwatch = new Timer();

    for (SensorContainer sensor : selectedSensors) {
      stopwatch.reset();
      stopwatch.start();
      pcs.firePropertyChange("message", null, "Sensor: " + sensor.getName());
      pcs.firePropertyChange("message", null, "- Building Request String");

      requestURL = buildRequest(sensor);

      int written;
      try {
        URL u = new URL(requestURL);
        pcs.firePropertyChange("message", null, "- Making Request (" + requestURL + ")");
        HttpURLConnection ht = (HttpURLConnection) u.openConnection();
        ht.setDoInput(true);
        ht.setRequestMethod("GET");
        InputStream is = ht.getInputStream();
        pcs.firePropertyChange("message", null, "- Streaming Results to File");
        String filename = FileSaveUtils.chooseFilename(savePath, sensor.getName(), fileSuffix);
        File f = new File(filename);
        OutputStream output = new BufferedOutputStream(new FileOutputStream(f));
        byte[] buffer = new byte[2048];
        int len = 0;
        written = 0;
        while (-1 != (len = is.read(buffer))) {
          output.write(buffer, 0, len);
          written += len;
        }
        is.close();
        output.flush();
        output.close();
        filenames.add(filename);
      } catch (MalformedURLException e) {
        pcs.firePropertyChange("message", null, "- BAD URL, skipping sensor");
        continue;
      } catch (IOException io) {
        pcs.firePropertyChange("message", null, "- BAD CONNECTION, skipping sensor");
        continue;
      }

      countSens++;
      pcs.firePropertyChange("message", null, "- Completed " + written + " bytes in " + stopwatch.elapsedTime() + " seconds.");
      int prog = Double.valueOf(countSens / numSens * 100).intValue();
      pcs.firePropertyChange("progress", null, prog);
    } // End Sensor List
    pcs.firePropertyChange("progress", null, 100);
    if (!filenames.isEmpty()) {
      pcs.firePropertyChange("message", null, "Saved Files:");
      for (String s : filenames) {
        pcs.firePropertyChange("message", null, "- " + s);
      }
    }
    pcs.firePropertyChange("done", null, filenames.toString());
  }


 



}
