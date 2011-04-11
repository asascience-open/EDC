/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * SwafsAggregatorDialog.java
 *
 * Created on Jul 25, 2008, 11:20:46 AM
 *
 */
package com.asascience.aggregator;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

import net.miginfocom.swing.MigLayout;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.output.XMLOutputter;

import com.asascience.ui.CheckBoxList;
import com.asascience.ui.DeterminateProgressDialog;
import com.asascience.ui.ErrorDisplayDialog;
import com.asascience.ui.OptionDialogBase;
import com.asascience.utilities.Utils;
import com.asascience.utilities.filefilter.CustomExtensionFilter;
import com.asascience.utilities.filefilter.NcFileFilter;

/**
 * 
 * @author cmueller_mac
 */
public class AggregatorDialog extends OptionDialogBase {

  protected List<File> inputFiles;
  private CheckBoxList cblFiles;
  private JTextField tfSaveLoc;
  private static String homeDir = null;
  private static String sysDir = null;
  // private static String outputDir;
  private File outFile;

  /** Creates a new instance of AggregatorDialog */
  public AggregatorDialog(List<File> files) {
    if (files == null || files.isEmpty()) {
      return;
    }
    inputFiles = files;

    if (homeDir == null) {
      homeDir = inputFiles.get(0).getAbsolutePath().replace(inputFiles.get(0).getName(), "");
    }

    initComponents();

    // for(File f : swafsFiles){
    // System.out.println(f.getAbsolutePath());
    // }
  }

  private void initComponents() {
    this.setTitle("Files to Aggregate");
    this.setPreferredSize(new Dimension(550, 550));
    this.setResizable(true);

    JPanel pnlMain = new JPanel(new MigLayout("insets 0, fill"));

    cblFiles = new CheckBoxList(true, true, true);
    for (File f : inputFiles) {
      // pnlMain.add(new JLabel(f.getName()), "center, wrap");
      cblFiles.addCheckBox(f.getName());
      // cblFiles.selectSingleItem(f.getName());
    }

    JScrollPane jsp = new JScrollPane();
    jsp.setBorder(BorderFactory.createTitledBorder("Available Files:"));
    jsp.getVerticalScrollBar().setBlockIncrement(100);
    jsp.getVerticalScrollBar().setUnitIncrement(5);
    jsp.setViewportView(cblFiles);

    JButton btnAddFiles = new JButton("Add File(s)");
    btnAddFiles.addActionListener(new ActionListener() {

      public void actionPerformed(ActionEvent e) {
        List<File> files = AggregatorDialog.selectFiles(rootPane, homeDir);
        for (File f : files) {
          if (!cblFiles.getAllItems().contains(f.getName())) {
            inputFiles.add(f);
            cblFiles.addCheckBox(f.getName());
            cblFiles.selectSingleItem(f.getName());
          }
        }
        // pack();
      }
    });

    tfSaveLoc = new JTextField();
    tfSaveLoc.setEditable(false);

    JButton btnSaveBrowse = new JButton("Browse");
    btnSaveBrowse.addActionListener(new ActionListener() {

      public void actionPerformed(ActionEvent e) {
        // String defName = aggFiles[0].getName().substring(0,
        // aggFiles[0].getName().
        // length() - 13) + "Agg.nc";
        String path;
        if (cblFiles.getSelItemsSize() > 0) {
          path = cblFiles.getSelectedItems().get(0);
        } else {
          path = inputFiles.get(0).getAbsolutePath();
        }
        // String defName = path.toString().substring(0, path.length() -
        // 13)
        // + "Agg.nc";
        String defName = Utils.replaceExtension(new File(path), "_Agg.nc");
        File outFile = new File(Utils.appendSeparator(homeDir) + defName);

        /** Have the user select an output file */
        JFileChooser fc = new JFileChooser();
        fc.setDialogType(JFileChooser.SAVE_DIALOG);
        fc.setAcceptAllFileFilterUsed(false);
        fc.setFileFilter(new NcFileFilter());
        fc.setDialogTitle("Save Aggregated File As...");
        // fc.setCurrentDirectory(new File(startLoc));
        fc.setCurrentDirectory(new File(homeDir));
        fc.setMultiSelectionEnabled(false);
        fc.setSelectedFile(outFile);
        if (fc.showSaveDialog(rootPane) == JFileChooser.APPROVE_OPTION) {
          outFile = new File(Utils.appendExtension(fc.getSelectedFile(), ".nc"));
        } else {
          // return false;
          return;
        }

        /** If the file exists, ask if it should be overwritten */
        if (outFile.exists()) {
          int resp = JOptionPane.showConfirmDialog(rootPane,
                  "The file already exists.\nDo you wish to replace it?", "Replace File",
                  JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
          if (resp == JOptionPane.NO_OPTION) {
            // return false;
            return;
          }
        }

        tfSaveLoc.setText(outFile.getAbsolutePath());
      }
    });
    String path = inputFiles.get(0).getAbsolutePath();
    tfSaveLoc.setText(Utils.replaceExtension(new File(path), "_Agg.nc"));

    JPanel pnlSave = new JPanel(new MigLayout("insets 5, fill"));
    pnlSave.setBorder(BorderFactory.createTitledBorder("Output Location:"));
    // pnlSave.add(new JLabel("Output Path:"), "wrap");
    pnlSave.add(tfSaveLoc, "growx");
    pnlSave.add(btnSaveBrowse);

    // pnlMain.add(btnAddFiles, "wrap");
    pnlMain.add(jsp, "center, grow, wrap");
    pnlMain.add(pnlSave, "center, growx");

    add(pnlMain, "center, grow");
    add(super.buttonPanel("Process"), "south");

    pack();
    setLocationRelativeTo(null);
  }

  public boolean hasFiles() {
    if (inputFiles != null && inputFiles.size() > 0) {
      return true;
    }
    return false;
  }

  public static List<File> selectFiles(Component parent, String chooserDirectory) {
    JFileChooser fc = new JFileChooser();
    fc.setDialogType(JFileChooser.OPEN_DIALOG);
    fc.setAcceptAllFileFilterUsed(false);
    fc.setDialogTitle("Select Files to Aggregate...");
    fc.setFileFilter(new NcFileFilter());
    fc.setCurrentDirectory(new File(chooserDirectory));
    fc.setMultiSelectionEnabled(true);
    if (fc.showDialog(parent, "Select") == JFileChooser.APPROVE_OPTION) {
      File[] files = fc.getSelectedFiles();

      return Arrays.asList(files);

      /**
       * This code is unecessary unless/until filtering of nc types is
       * needed.
       */
      // //thin the selected files down to only the Netcdf files
      // List<File> flist = new ArrayList<File>();
      // for(File f : files){
      // /** Add to the filelist if it is an nc file. */
      // if(f.getAbsolutePath().toLowerCase().endsWith(".nc")) {
      // // if(DetermineNcType.isSWAFS(f.getAbsolutePath())){
      // flist.add(f);
      // }
      // }
      // return flist;
      // return flist.toArray(new File[0]);
    }
    return null;
  }

  public boolean runAggregationProcess() {
    /** Figure out what files are selected */
    List<String> selFiles = cblFiles.getSelectedItems();
    File[] aggFiles = new File[selFiles.size()];
    for (int i = 0; i < aggFiles.length; i++) {
      for (File f : inputFiles) {
        if (f.getName().equals(selFiles.get(i))) {
          aggFiles[i] = f;
          break;
        }
      }
    }

    // outputDir = homeDir;

    // /**Set some defaults*/
    // String startLoc;
    // if(outputDir == null) {
    // startLoc = aggFiles[0].getAbsolutePath().replace(aggFiles[0].
    // getName(), "");
    // }else {
    // startLoc = outputDir;
    // }
    // String defName = aggFiles[0].getName().substring(0,
    // aggFiles[0].getName().
    // length() - 13) + "Agg.nc";
    // outFile = new File(Utils.appendSeparator(homeDir) + defName);
    //
    // /**Have the user select an output file*/
    // JFileChooser fc = new JFileChooser();
    // fc.setDialogType(JFileChooser.SAVE_DIALOG);
    // fc.setAcceptAllFileFilterUsed(false);
    // fc.setFileFilter(new NcFileFilter());
    // fc.setDialogTitle("Save Aggregated File As...");
    // // fc.setCurrentDirectory(new File(startLoc));
    // fc.setCurrentDirectory(new File(homeDir));
    // fc.setMultiSelectionEnabled(false);
    // fc.setSelectedFile(outFile);
    // if(fc.showSaveDialog(rootPane) == JFileChooser.APPROVE_OPTION){
    // outFile = new File(Utils.appendExtension(fc.getSelectedFile(),
    // ".nc"));
    // }else{
    // return false;
    // }
    //
    // /**If the file exists, ask if it should be overwritten*/
    // if(outFile.exists()){
    // int resp = JOptionPane.showConfirmDialog(rootPane,
    // "The file already exists.\nDo you wish to replace it?",
    // "Replace File", JOptionPane.YES_NO_OPTION,
    // JOptionPane.QUESTION_MESSAGE);
    // if(resp == JOptionPane.NO_OPTION){
    // return false;
    // }
    // }

    /** Run the process */
    DeterminateProgressDialog pd = new DeterminateProgressDialog(null, "Progress", null);
    // SwafsAggregatorTask sat = new SwafsAggregatorTask(aggFiles, outFile.
    // getAbsolutePath());
    // AggregatorTask sat = new AggregatorTask(aggFiles, outFile.
    // getAbsolutePath());
    AggregatorTask sat = new AggregatorTask(aggFiles, tfSaveLoc.getText());
    sat.addPropertyChangeListener(new ProcessPropertyListener());
    pd.setRunTask(sat);
    pd.runTask();

    return true;
  }

  public List<File> getInputFiles() {
    return inputFiles;
  }

  public void deleteOutputXml() {
    File f = new File(sysDir, "aggout.xml");
    if (f.exists()) {
      f.delete();
    }
  }

  public void writeOutputXml() {
    try {
      deleteOutputXml();

      XMLOutputter out = new XMLOutputter();
      Document doc = new Document();
      Element root = new Element("aggRoot");
      Element outLoc = new Element("outputLocation");
      outLoc.setText(tfSaveLoc.getText());

      root.addContent(outLoc);
      doc.setRootElement(root);
      // out.output(doc, new
      // FileOutputStream(Utils.appendSeparator(sysDir) + "aggout.xml"));
      out.output(doc, new FileOutputStream(Utils.appendSeparator(homeDir) + "aggout.xml"));
    } catch (FileNotFoundException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  public static void initialize(String configLoc) {
    try {
      if (configLoc == null) {
        throw new Exception("Configuration file does not exist.  Cannot continue.");
      }

      if (Configuration.initialize(configLoc)) {
        homeDir = Configuration.HOME_DIR;
        String logLoc = Utils.appendSeparator(homeDir) + "agglog.log";
        if (logLoc != null && !logLoc.equals("null")) {
          File f = new File(logLoc);
          if (f.exists()) {
            if (f.length() >= 10485760l)// delete the log file if it
            // exceeds 10mb
            {
              f.delete();
            }
          }
          PrintStream ps = new PrintStream(new FileOutputStream(logLoc, true), true);
          System.setOut(ps);
          System.setErr(ps);
          String le = System.getProperty("line.separator");
          System.out.println(le + le + "-----Start New Run: " + new Date().toString() + "-----");
        }
      }
      Configuration.writeProperties();

      File inDir = new File(homeDir);
      File[] list = inDir.listFiles(new CustomExtensionFilter("nc"));
      if (list != null) {
        AggregatorDialog ad = new AggregatorDialog(Arrays.asList(list));
        if (ad.hasFiles()) {
          ad.setVisible(true);
          if (ad.acceptChanges()) {
            if (!ad.runAggregationProcess()) {
              ad.dispose();
              exitApp();
            }
          } else {
            ad.dispose();
            exitApp();
          }
        }
      }
    } catch (Exception ex) {
      Logger.getLogger(AggregatorDialog.class.getName()).log(Level.SEVERE, null, ex);
    }
  }

  private static void exitApp() {
    System.exit(0);
  }

  public static void main(final String[] args) {
    // if(args != null && args.length != 0) {
    // switch(args.length) {
    // case 1:
    // inputDir = args[0];
    // outputDir = args[0];
    // break;
    // case 2:
    // inputDir = args[0];
    // outputDir = args[1];
    // break;
    // }
    // }else {
    // outputDir = null;
    // inputDir = null;
    // }
    // TODO code application logic here
    javax.swing.SwingUtilities.invokeLater(new Runnable() {

      public void run() {
        String configLoc = null;
        if (args.length > 0) {
          configLoc = args[0];
        }
        AggregatorDialog.initialize(configLoc);

        // try {
        // sysDir = System.getProperty("user.dir");
        // String logLoc = Utils.appendSeparator(sysDir)
        // + "agglog.log";
        // if (logLoc != null && !logLoc.equals("null")) {
        // File f = new File(logLoc);
        // if (f.exists()) {
        // // delete the log file if it exceeds 10mb
        // if (f.length() >= 10485760l) {
        // f.delete();
        // }
        // }
        // PrintStream ps;
        // ps = new PrintStream(
        // new FileOutputStream(logLoc, true), true);
        //
        // System.setOut(ps);
        // System.setErr(ps);
        // String le = System.getProperty("line.separator");
        // System.out.println(le + le + "-----Start New Run: "
        // + new Date().toString() + "-----");
        // }
        // } catch (FileNotFoundException e) {
        // // TODO Auto-generated catch block
        // e.printStackTrace();
        // }

        // AggregatorDialog ad = new AggregatorDialog(AggregatorDialog
        // .selectFiles(null, ((inputDir != null) ? inputDir :
        // sysDir)));
        // if (ad.hasFiles()){
        // ad.setVisible(true);
        // if(ad.acceptChanges()){
        // ad.runAggregationProcess();
        // }
        // }
      }
    });
  }

  class ProcessPropertyListener implements PropertyChangeListener {

    public void propertyChange(PropertyChangeEvent evt) {
      if (evt.getPropertyName().equals("done")) {
        /** Save the location of the output file to an xml. */
        writeOutputXml();
        /** Close the dialog. */
        AggregatorDialog.this.dispose();
        exitApp();
      } else if (evt.getPropertyName().equals("cancel")) {
        deleteOutputXml();
        // should leave the dialog up - but simply dispose for now
        AggregatorDialog.this.dispose();
        exitApp();
      } else if (evt.getPropertyName().equals("error")) {
        deleteOutputXml();
        if (evt.getOldValue() != null) {
          ErrorDisplayDialog.showErrorDialog((String) evt.getOldValue(), (Exception) evt.getNewValue());
        } else {
          ErrorDisplayDialog.showErrorDialog((Exception) evt.getNewValue());
        }
        // JOptionPane.showMessageDialog(AggregatorDialog.this,
        // "Error aggregating file.");
        AggregatorDialog.this.dispose();
        exitApp();
      }

      // System.exit(0);
    }
  }
}
