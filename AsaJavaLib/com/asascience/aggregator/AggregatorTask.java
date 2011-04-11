/*
 * Applied Science Associates, Inc.
 * Copyright 2008. All Rights Reserved.
 *
 * AggregatorTask.java
 *
 * Created on Nov 17, 2008 @ 8:46:26 AM
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
import ucar.ma2.ArrayFloat;
import ucar.ma2.DataType;
import ucar.nc2.Attribute;
import ucar.nc2.Dimension;
import ucar.nc2.NetcdfFile;
import ucar.nc2.NetcdfFileWriteable;
import ucar.nc2.Variable;
import ucar.nc2.dataset.NetcdfDataset;

import com.asascience.utilities.BaseTask;
import com.asascience.utilities.Utils;

/**
 * 
 * @author CBM <cmueller@asascience.com>
 */
public class AggregatorTask extends BaseTask {

  private File[] inFiles;
  private String outFile;

  public AggregatorTask(File[] inFiles, String outFile) {
    this.inFiles = inFiles;
    this.outFile = outFile;

    /**
     * Sort the file array by name. NOTE: This assumes that the names
     * contain some indicator of chronology.
     */
    Arrays.sort(this.inFiles);
  }

  public Void doInBackground() {
    NetcdfFileWriteable output = null;
    NetcdfDataset ncd;
    NetcdfFile ncf;
    StringBuilder aggHistAttribute = new StringBuilder();
    List<Attribute> attsList = new ArrayList<Attribute>();
    boolean errorEncountered = false;
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

      /** Create the output file writer. */
      output = NetcdfFileWriteable.createNew(outFile, true);

      /**
       * Use the first dataset to set up the output file - assumes all are
       * of the same format.
       */
      ncf = NetcdfFile.open(inFiles[0].getAbsolutePath());

      /**
       * Add all of the dimensions to the output file and identify the
       * unlimited dimension.
       */
      Dimension unlimDim = null;
      for (Dimension d : ncf.getDimensions()) {
        if (d.isUnlimited()) {
          unlimDim = output.addUnlimitedDimension(d.getName());
        } else {
          output.addDimension(d.getName(), d.getLength());
        }
      }

      /**
       * If the "time" dimesion is not unlimited, attempt to assign the
       * 'correct' dimension as time.
       */
      if (unlimDim == null) {
        List<String> timeNames = new ArrayList<String>();
        timeNames.add("time");
        timeNames.add("record");
        for (Dimension d : ncf.getDimensions()) {
          if (timeNames.contains(d.getName().toLowerCase())) {
            output.removeDimension(output.getRootGroup(), d.getName());
            unlimDim = output.addUnlimitedDimension(d.getName());
            break;
          }
        }
      }

      if (unlimDim == null) {
        throw new Exception("File {" + inFiles[0].getName() + "} does not contain a time dimension.");
      }

      /** Add all of the variables to the output file. */
      List<Dimension> dimList;
      for (Variable v : ncf.getVariables()) {
        if (v.getName().equals(unlimDim.getName())) {
          output.addVariable(v.getName(), v.getDataType(), new Dimension[]{unlimDim});
        } else {
          /** Replace the unlimited dimension if it exists. */
          dimList = v.getDimensions();
          if (v.isUnlimited()) {
            dimList.set(dimList.indexOf(ncf.getUnlimitedDimension()), unlimDim);
          }

          output.addVariable(v.getName(), v.getDataType(), v.getDimensionsString());
        }

        /** Add all of the variable attributes to the output file. */
        for (Attribute a : (List<Attribute>) v.getAttributes()) {
          output.addVariableAttribute(v.getName(), a);
        }
      }

      /**
       * Gather all of the global attributes. Also determine the unique
       * times.
       */
      int progMax = 0;
      Variable time;
      Variable tau;
      double[] times = null;
      double[] taus = null;
      List<Double> timeList = new ArrayList<Double>();
      List<Double> tauList = new ArrayList<Double>();
      for (File f : inFiles) {
        ncf = NetcdfFile.open(f.getAbsolutePath());

        /** Capture any new global attributes. */
        for (Attribute a : ncf.getGlobalAttributes()) {
          if (!attsList.contains(a)) {
            attsList.add(a);
          }
        }
        aggHistAttribute.append(f.getName());
        aggHistAttribute.append(";");

        /** Capture any new unique times. */
        time = ncf.findVariable(unlimDim.getName());
        tau = ncf.findVariable("tau");
        if (time != null) {
          times = (double[]) time.read().get1DJavaArray(double.class);
          /** If there is a tau variable. */
          if (tau != null) {
            taus = (double[]) tau.read().get1DJavaArray(double.class);
            for (double t : taus) {
              // if (!tauList.contains(t)) {
              tauList.add(t);
              // }
            }
          }

          for (double t : times) {
            if (!timeList.contains(t)) {
              timeList.add(t);
              /** If there is no tau variable. */
              if (tau == null) {
                tauList.add(t - timeList.get(0));
              }
            }
          }
        }
        progMax += ncf.getVariables().size() * times.length;
      }
      attsList.add(new Attribute("Aggregate History", "Aggregated by ASA using the Java NetCDF CDM: "
              + "Translation Date:: " + new Date() + "  Original Datasets:: " + aggHistAttribute.toString()));

      /** Add the global attributes. */
      for (Attribute att : attsList) {
        output.addGlobalAttribute(att);
      }

      /** Write the file to disk. */
      output.create();

      /**
       * Write the non-unlimited variables (those that remain the same
       * between files) to the file examples are lat, lon, depth, bottom
       * depth, etc.
       */
      ncf = NetcdfFile.open(inFiles[0].getAbsolutePath());
      Attribute val_rangeAtt = null;
      for (Variable v : ncf.getVariables()) {
        // if(!v.isUnlimited()) {
        // if (!v.getDimensions().contains(unlimDim)) {
        int unlimIndex = v.findDimensionIndex(unlimDim.getName());
        if (unlimIndex == -1) {
          /**
           * Check to ensure longitude is -180 to 180 rather than 0 to
           * 360
           */
          if (v.findDimensionIndex("lon") != -1) {
            val_rangeAtt = null;
            // FIXME find a way to determine this without
            // hardcoding!
            if (v.getName().toLowerCase().equals("lon")) {
              Array larr = Array.factory(double.class, v.getShape());
              double[] lons = (double[]) v.read().get1DJavaArray(double.class);
              double[] olons = new double[lons.length];
              for (int l = 0; l < lons.length; l++) {
                if (lons[l] > 180) {
                  olons[l] = -180 + (lons[l] - 180);
                } else {
                  olons[l] = lons[l];
                }
                larr.setDouble(larr.getIndex().set(l), olons[l]);
              }
              v.setCachedData(larr, false);
              /** Update the valid range attribute. */
              double[] mm = Utils.minMaxDouble(olons);
              // val_rangeAtt = v.findAttribute("valid_range");
              ArrayFloat.D1 valid = new ArrayFloat.D1(2);
              valid.set(0, (float) mm[0]);
              valid.set(1, (float) mm[1]);
              val_rangeAtt = new Attribute("valid_range", valid);
              v.remove(v.findAttribute("valid_range"));
              // v.addAttribute(val_rangeAtt);
              // val_rangeAtt.setValues(valid);
            }
          }
          output.write(v.getName(), v.read());
          // if(val_rangeAtt != null) {
          // output.updateAttribute(v, val_rangeAtt);
          // }
        }
      }

      /**  */
      firePropertyChange("max", null, progMax);

      int progCount = 0;
      int tIndex;
      Array timeArray = Array.factory(DataType.DOUBLE, new int[]{1});
      Array tauArray = Array.factory(DataType.DOUBLE, new int[]{1});
      for (int i = 0; i < inFiles.length; i++) {
        firePropertyChange("note", null, "Appending file " + (i + 1) + " of " + inFiles.length);

        ncf = NetcdfFile.open(inFiles[i].getAbsolutePath());
        /** Get the time variable. */
        time = ncf.findVariable(unlimDim.getName());
        if (time != null) {
          times = (double[]) time.read().get1DJavaArray(double.class);
          for (int ti = 0; ti < times.length; ti++) {
            if (Thread.currentThread().isInterrupted()) {
              cleanupCancel();
              firePropertyChange("cancel", false, true);
              firePropertyChange("close", false, true);
              return null;
            }
            double t = times[ti];
            tIndex = timeList.indexOf(t);
            timeArray.setDouble(timeArray.getIndex(), t);
            tauArray.setDouble(tauArray.getIndex(), tauList.get(tIndex));

            for (Variable v : ncf.getVariables()) {
              firePropertyChange("progress", null, progCount++);
              if (Thread.currentThread().isInterrupted()) {
                cleanupCancel();
                firePropertyChange("cancel", false, true);
                firePropertyChange("close", false, true);
                return null;
              }
              // if(v.isUnlimited()) {
              // if (v.getDimensions().contains(unlimDim)) {
              int unlimIndex = v.findDimensionIndex(unlimDim.getName());
              if (unlimIndex != -1) {
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

                if (shape.length == 1) {
                } else {
                  output.write(v.getName(), outorigin, v.read(dataorigin, size));
                }
              }
            }

            /** If there is a "tau" variable - write to it. */
            if (output.findVariable("tau") != null) {
              output.write("tau", new int[]{tIndex}, tauArray);
            }
            /** Write the times to the file. */
            output.write(unlimDim.getName(), new int[]{tIndex}, timeArray);
          }
        }
      }
    } catch (IOException ex) {
      errorEncountered = true;
      Logger.getLogger(AggregatorTask.class.getName()).log(Level.SEVERE, null, ex);
      firePropertyChange("error", null, ex);
    } catch (InterruptedException ex) {
      errorEncountered = true;
      Logger.getLogger(AggregatorTask.class.getName()).log(Level.SEVERE, null, ex);
      firePropertyChange("error", null, ex);
    } catch (IllegalArgumentException ex) {
      errorEncountered = true;
      Logger.getLogger(AggregatorTask.class.getName()).log(Level.SEVERE, null, ex);
      firePropertyChange("error", "Input files may not be of the same type.", ex);
    } catch (Exception ex) {
      errorEncountered = true;
      Logger.getLogger(AggregatorTask.class.getName()).log(Level.SEVERE, null, ex);
      firePropertyChange("error", null, ex);
    } finally {
      try {
        if (output != null) {
          output.close();
        }
      } catch (IOException ex) {
        Logger.getLogger(AggregatorTask.class.getName()).log(Level.SEVERE, null, ex);
        firePropertyChange("error", null, ex);
      }
      if (!errorEncountered) {
        firePropertyChange("done", null, true);
      } else {
        File f = new File(outFile);
        if (f.exists()) {
          f.delete();
        }
      }
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
