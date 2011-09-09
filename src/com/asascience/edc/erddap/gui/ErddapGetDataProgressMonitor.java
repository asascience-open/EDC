package com.asascience.edc.erddap.gui;

import com.asascience.edc.erddap.ErddapDataRequest;
import com.asascience.edc.gui.FileBrowser;
import com.asascience.edc.utils.FileSaveUtils;
import java.awt.Cursor;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingWorker;
import net.miginfocom.swing.MigLayout;
import org.apache.log4j.Logger;

/**
 * ErddapGetDataProgressMonitor.java
 * 
 * @author Kyle Wilcox <kwilcox@asascience.com>
 */
public class ErddapGetDataProgressMonitor extends JPanel implements ActionListener {

  private JButton startButton;
  private JProgressBar progressBar;
  private JTextArea taskOutput;
  private Task task;
  private ListenForProgress listener;
  private PropertyChangeSupport pcs;
  private ErddapDataRequest request;
  private FileBrowser fileBrowser;
  private static Logger guiLogger = Logger.getLogger("com.asascience.log." + ErddapGetDataProgressMonitor.class.getName());

  class Task extends SwingWorker<Void, Void> {
    @Override
    public Void doInBackground() {
      setProgress(0);
      try {
        request.addPropertyChangeListener(listener);
        request.getData();
      } catch (Exception e) {
        taskOutput.append(String.format("%1$s\n", e.toString()));
      }
      request.removePropertyChangeListener(listener);
      return null;
    }

    public void setTaskProgress(int progress) {
      setProgress(progress);
    }
    
    @Override
    public void done() {
      startButton.setEnabled(true);
      request.removePropertyChangeListener(listener);
      setCursor(null);
      taskOutput.append("Done!\n");
    }
  }

  public void update() {
    // Don't fire the events
    fileBrowser.setFile(request.getUpdatedSaveFile(), false);
  }
  
  public ErddapGetDataProgressMonitor(ErddapDataRequest data) {
    super(new MigLayout("fill"));

    pcs = new PropertyChangeSupport(this);
    listener = new ListenForProgress();

    request = data;
   
    startButton = new JButton("Start");
    startButton.setActionCommand("start");
    startButton.addActionListener(this);

    progressBar = new JProgressBar(0, 100);
    progressBar.setValue(0);
    progressBar.setStringPainted(true);
    
    // NEED TO HAVE THE SAVE PATH SET AT THIS POINT
    // Save directory
    File tempFileLocation = request.getComputedSaveFile().getParentFile();
    // Do we need to increment the files?
    File filepath = new File(FileSaveUtils.chooseFilename(tempFileLocation, request.getFilename()));
    fileBrowser = new FileBrowser(filepath);
    fileBrowser.addPropertyChangeListener("fileChanged", new PropertyChangeListener() {

      public void propertyChange(PropertyChangeEvent evt) {
        request.setSaveFile(((File)evt.getNewValue()));
        // Immediatly kick off the processing
        startButton.doClick();
      }
    });

    taskOutput = new JTextArea(5, 20);
    taskOutput.setMargin(new Insets(5,5,5,5));
    taskOutput.setEditable(false);

    JPanel panel = new JPanel(new MigLayout("gapx 10, fillx"));
    panel.add(progressBar, "growx");
    panel.add(startButton, "wrap");
    panel.add(fileBrowser, "growx, spanx");

    add(panel, "wrap, align center");
    add(new JScrollPane(taskOutput), "grow");
    setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
  }

  public void actionPerformed(ActionEvent e) {
    setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
    task = new Task();
    task.addPropertyChangeListener(listener);
    task.execute();
    startButton.setEnabled(false);
  }

  @Override
  public void addPropertyChangeListener(PropertyChangeListener l) {
    pcs.addPropertyChangeListener(l);
  }
  @Override
  public void removePropertyChangeListener(PropertyChangeListener l) {
    pcs.removePropertyChangeListener(l);
  }

  class ListenForProgress implements PropertyChangeListener {
    public void propertyChange(PropertyChangeEvent evt) {
      if ("progress".equals(evt.getPropertyName())) {
        int progress = (Integer)evt.getNewValue();
        progressBar.setValue(progress);
        task.setTaskProgress(progress);
      } else if ("message".equals(evt.getPropertyName())) {
        taskOutput.append(String.format("%1$s\n", evt.getNewValue()));
        guiLogger.info((String)evt.getNewValue());
      } else if ("done".equals(evt.getPropertyName())) {
        guiLogger.info("Processing of ERDDAP Data complete");
        pcs.firePropertyChange(evt);
      }
    }
  }
}
