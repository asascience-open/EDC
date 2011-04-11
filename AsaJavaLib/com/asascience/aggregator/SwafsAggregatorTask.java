/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * SwafsAggregatorTask.java
 *
 * Created on Jul 28, 2008, 3:30:52 PM
 *
 */
package com.asascience.aggregator;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import ucar.ma2.Array;
import ucar.ma2.DataType;
import ucar.nc2.Attribute;
import ucar.nc2.Dimension;
import ucar.nc2.NetcdfFileWriteable;
import ucar.nc2.Variable;
import ucar.nc2.dataset.NetcdfDataset;

import com.asascience.utilities.BaseTask;

/**
 * 
 * @author cmueller_mac
 */
public class SwafsAggregatorTask extends BaseTask {

  private File[] inFiles;
  private String outFile;

  /**
   * Creates a new instance of SwafsAggregatorTask
   *
   * @param inFiles
   * @param outFile
   */
  public SwafsAggregatorTask(File[] inFiles, String outFile) {
    this.inFiles = inFiles;
    this.outFile = outFile;

    Arrays.sort(this.inFiles);
  }

  @Override
  public Void doInBackground() {
    NetcdfFileWriteable output = null;
    NetcdfDataset ncd;
    StringBuilder ncdLocsAtt = new StringBuilder();
    List<Attribute> attsList = new ArrayList<Attribute>();
    try {
      /**
       * Delete the output file if it already exists - the user has
       * already confirmed replacement
       */
      File del = new File(outFile);
      if (del.exists()) {
        del.delete();
      }
      del = null;

      firePropertyChange("progress", null, 0);// starts the dialog

      // starts the dialog
      Thread.sleep(500); // sleep for a half second to allow the dialog to
      // display

      firePropertyChange("note", null, "Initializing...");

      output = NetcdfFileWriteable.createNew(outFile, true);

      /**
       * Use the first dataset to set up the output file - assumes all are
       * of the same format
       */
      ncd = NetcdfDataset.acquireDataset(inFiles[0].getAbsolutePath(), null);

      /** Add all of the dimensions to the output file */
      Dimension unlimDim = null;
      for (Dimension d : ncd.getDimensions()) {
        if (d.isUnlimited()) {
          unlimDim = output.addUnlimitedDimension(d.getName());
        } else {
          output.addDimension(d.getName(), d.getLength());
        }
      }
      /** Add all of the variables to the output file */
      String name;
      DataType dt;
      List dimList;
      Dimension[] dims;
      List<Variable> vs = ncd.getVariables();
      for (Variable v : ncd.getVariables()) {
        if (v.getName().equals(unlimDim.getName())) {
          output.addVariable(v.getName(), v.getDataType(), new Dimension[]{unlimDim});
        } else {
          /** Replace the unlimited dimension if it exists */
          dimList = v.getDimensions();
          if (v.isUnlimited()) {// if one of it's dimensions is
            // unlimited
            dimList.set(dimList.indexOf(ncd.getUnlimitedDimension()), unlimDim);
          }

          dims = (Dimension[]) dimList.toArray(new Dimension[0]);

          output.addVariable(v.getName(), v.getDataType(), v.getDimensionsString());
        }
        /** Add all of the variable attributes to the output file */
        for (Attribute a : (List<Attribute>) v.getAttributes()) {
          output.addVariableAttribute(v.getName(), a);
        }
      }

      /**
       * Gather all of the global attributes and a count of all of the
       * variables for the progress bar.
       */
      for (File f : inFiles) {
        ncd = NetcdfDataset.acquireDataset(f.getAbsolutePath(), null);

        // TODO: This only transfers the first global attribute of a
        // given name?
        for (Attribute a : ncd.getGlobalAttributes()) {
          if (!attsList.contains(a)) {
            attsList.add(a);
            // }else{
            // Attribute at = attsList.get(attsList.indexOf(a));
            //
          }
        }
        ncdLocsAtt.append(f.getName());
        ncdLocsAtt.append(";");
      }
      attsList.add(new Attribute("Append History",
              "Appended by ASA (SwafsAggregatorTask.java) using the Netcdf-Java CDM; " + "Original Datasets = "
              + ncdLocsAtt + "Translation date = " + new Date() + ";"));

      /** Add the global attributes */
      for (Attribute att : attsList) {
        output.addGlobalAttribute(att);
      }

      /** Write the file */
      output.create();

      /**
       * Write the non-unlimited variables (those that remain the same
       * between files) to the file examples are lat, lon, depth, bottom
       * depth, etc.
       */
      ncd = NetcdfDataset.acquireDataset(inFiles[0].getAbsolutePath(), null);
      for (Variable v : ncd.getVariables()) {
        if (!v.isUnlimited()) {
          output.write(v.getName(), v.read());
        }
      }

      int progMax = 0;
      Variable time;
      double[] times = null;
      List<Double> timeList = new ArrayList<Double>();
      List<Double> tauList = new ArrayList<Double>();
      firePropertyChange("note", null, "Calculate unique times...");
      for (int i = 0; i < inFiles.length; i++) {

        ncd = NetcdfDataset.acquireDataset(inFiles[i].getAbsolutePath(), null);

        /** Get the time variable and capture all unique times */
        time = ncd.findVariable(ncd.getUnlimitedDimension().getName());
        if (time != null) {
          times = (double[]) time.read().copyTo1DJavaArray();
          for (double t : times) {
            if (!timeList.contains(t)) {
              timeList.add(t);
              // NOTE: This only works if the units of time and
              // tau are the same!!
              tauList.add(t - timeList.get(0));
            }
          }
        }

        progMax += ncd.getVariables().size() * times.length;
      }
      firePropertyChange("max", null, progMax);

      int progCount = 0;
      Array timeArray = Array.factory(DataType.DOUBLE, new int[]{1});
      Array tauArray = Array.factory(DataType.DOUBLE, new int[]{1});
      int tIndex;
      for (int i = 0; i < inFiles.length; i++) {
        // System.out.println("GetData: " + inFiles[i].getName());

        firePropertyChange("note", null, "Appending file " + (i + 1) + " of " + inFiles.length);

        ncd = NetcdfDataset.acquireDataset(inFiles[i].getAbsolutePath(), null);
        /** Get the time variable */
        time = ncd.findVariable(ncd.getUnlimitedDimension().getName());
        if (time != null) {
          times = (double[]) time.read().copyTo1DJavaArray();
          for (int ti = 0; ti < times.length; ti++) {
            double t = times[ti];
            // System.out.println("DataTime: " + t);
            tIndex = timeList.indexOf(t);
            timeArray.setDouble(timeArray.getIndex(), t);
            // System.out.println("TIndex: " + tIndex);
            tauArray.setDouble(timeArray.getIndex(), tauList.get(tIndex));

            for (Variable v : ncd.getVariables()) {
              firePropertyChange("progress", null, progCount++);

              if (Thread.currentThread().isInterrupted()) {
                cleanupCancel();
                firePropertyChange("cancel", false, true);
                firePropertyChange("close", false, true);
                return null;
              }
              if (v.isUnlimited()) {
                int unlimIndex = v.findDimensionIndex(ncd.getUnlimitedDimension().getName());
                int[] shape = v.getShape();
                int[] dataorigin = new int[shape.length];
                int[] outorigin = new int[shape.length];
                int[] size = new int[shape.length];
                for (int si = 0; si < shape.length; si++) {
                  size[si] = shape[si];
                  dataorigin[si] = 0;
                  outorigin[si] = 0;
                }
                dataorigin[unlimIndex] = ti;
                outorigin[unlimIndex] = tIndex;
                size[unlimIndex] = 1;

                if (shape.length == 1) {// it's a "tau" type
                  // variable
                  // do nothing - written later (once per
                  // timestep)
                  // output.write(v.getName(), outorigin,
                  // tauArray);
                } else {
                  output.write(v.getName(), outorigin, v.read(dataorigin, size));
                }
              }
            }

            /** If there is a "tau" variable - write to it */
            if (output.findVariable("tau") != null) {
              output.write("tau", new int[]{tIndex}, tauArray);
            }
            output.write(ncd.getUnlimitedDimension().getName(), new int[]{tIndex}, timeArray);
          }
        }

      }

    } catch (IOException ex) {
      Logger.getLogger(SwafsAggregatorTask.class.getName()).log(Level.SEVERE, null, ex);
    } catch (InterruptedException ex) {
      Logger.getLogger(SwafsAggregatorTask.class.getName()).log(Level.SEVERE, null, ex);
    } catch (Exception ex) {
      Logger.getLogger(SwafsAggregatorTask.class.getName()).log(Level.SEVERE, null, ex);
    } finally {
      try {
        if (output != null) {
          output.close();
        }
      } catch (IOException ex) {
        Logger.getLogger(SwafsAggregatorTask.class.getName()).log(Level.SEVERE, null, ex);
      }
      firePropertyChange("done", null, true);
      firePropertyChange("close", false, true);
    }

    return null;
  }

  private void cleanupCancel() {
    File del = new File(outFile);
    if (del.exists()) {
      del.delete();
    }
    del = null;
  }
}
