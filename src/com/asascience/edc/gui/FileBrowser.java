/*
 *  Applied Science Associates, Inc.
 *  Copyright 2011.  All rights reserved.
 */
package com.asascience.edc.gui;

import java.awt.FileDialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.Random;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextField;
import net.miginfocom.swing.MigLayout;

/**
 * FileBrowser
 *
 * Created on Aug 1, 2011, 9:26:24 AM
 *
 * @author Kyle Wilcox <kwilcox@asascience.com>
 */
public class FileBrowser extends JPanel {

  private JTextField fileText;
  private File file;
  private boolean isDirectory;

  public FileBrowser() {
    try {
      this.file = File.createTempFile(("tmp_" + new Random(3000)).toString(), ".tmp");
    } catch (IOException ioe) {
    }
    initComponents();
  }

  public FileBrowser(String path) {
    this.file = new File(path);
    initComponents();
  }

  public FileBrowser(File file) {
    this.file = file;
    initComponents();
  }

  private void initComponents() {
    setLayout(new MigLayout("gapx 10, fillx"));
    fileText = new JTextField(file.getAbsolutePath());
    fileText.setEditable(false);
    JButton browse = new JButton("Browse...");

    // Make sure the directories are there
    file.getParentFile().mkdirs();

    browse.addActionListener(new ActionListener() {

      public void actionPerformed(ActionEvent e) {
        FileDialog outputPath = new FileDialog(new JFrame());
        outputPath.setFile(file.getAbsolutePath());
        outputPath.setMode(FileDialog.SAVE);
        outputPath.setVisible(true);
        if (outputPath.getFile() != null) {
          // Remove default folder if it is not being used by anything
          setFile(outputPath.getDirectory() + outputPath.getFile());
        }
      }
    });

    add(fileText, "growx");
    add(browse);
  }

  private void setFile(String path) {
    File nf = new File(path);
    setFile(nf);
  }
  
  public void setFile(File nf) {
    removeOldDirectory(nf);
    this.file = nf;
    if (isDirectory) {
      nf = nf.getParentFile();
    }
    this.fileText.setText(nf.getAbsolutePath());
    firePropertyChange("fileChanged", null, nf);
  }

  private void removeOldDirectory(File newfile) {
    File originalFolder = file.getParentFile();
    if (!newfile.getAbsolutePath().contains(file.getParentFile().getAbsolutePath())) {
      if (originalFolder.length() == 0) {
        originalFolder.delete();
      }
    }
  }
  
  public void setSelectDirectory(boolean directory) {
    isDirectory = directory;
    setFile(file);
  }
  
}
