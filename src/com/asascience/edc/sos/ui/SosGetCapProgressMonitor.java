package com.asascience.edc.sos.ui;

import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingWorker;

import org.apache.log4j.Logger;

import com.asascience.edc.sos.SosServer;

import net.miginfocom.swing.MigLayout;

/**
 * SosGetCapProgressMonitor.java
 * 
 * @author Kyle Wilcox <kwilcox@asascience.com>
 */
public class SosGetCapProgressMonitor extends JPanel implements ActionListener, PropertyChangeListener {

  private JProgressBar progressBar;
  private JTextArea taskOutput;
  private Task task;
  private SosServer sosData;
  private ListenForCapProgress listener;
  private JButton closeButton;
  private static Logger guiLogger = Logger.getLogger("com.asascience.log." + SosGetCapProgressMonitor.class.getName());
  public final static String TASK_COMPLETE = "taskcomplete";
  class Task extends SwingWorker<Void, Void> {

    @Override
    public Void doInBackground() {
      setProgress(0);
      try {
        boolean testCapParse = sosData.parseSosGetCapabilities();
        if (!testCapParse || isCancelled()) {
          taskOutput.append(String.format("Can't Read SOS service!\n"));
          return null;
        }
        
      } catch (Exception e) {
        taskOutput.append(String.format("%1$s\n", e.toString()));
        guiLogger.error("Exception", e);
      }
      return null;
    }

    public void setTaskProgress(int progress) {
      setProgress(progress);
    }

    @Override
    public void done() {

    	if (!isCancelled()) {
            firePropertyChange("message", null, "Adding sensors to map (this could take awhile)");
            firePropertyChange(TASK_COMPLETE, null, false);
            firePropertyChange("progress", null, 100);
          }
    	else
    		firePropertyChange(TASK_COMPLETE, null, true);
      taskOutput.append("Done!\n");
      closeButton.setText("Close");
    }
  }

  public Task getTask() {
	return task;
}

public SosGetCapProgressMonitor(SosServer data) {
    super(new MigLayout("fill"));

    listener = new ListenForCapProgress();

    sosData = data;

    progressBar = new JProgressBar(0, 100);
    progressBar.setValue(0);
    progressBar.setStringPainted(true);

    taskOutput = new JTextArea(5, 20);
    taskOutput.setMargin(new Insets(5, 5, 5, 5));
    taskOutput.setEditable(false);

    closeButton = new JButton("Cancel");
    closeButton.addActionListener(this);

    JPanel panel = new JPanel();
    panel.add(closeButton, "wrap");
    panel.add(progressBar);

    add(panel, "wrap, align center");
    add(new JScrollPane(taskOutput), "grow");
    setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

    // GO
    task = new Task();
    task.addPropertyChangeListener(listener);
    task.execute();
  }

  public void propertyChange(PropertyChangeEvent evt) {
    listener.propertyChange(evt);
  }

  public void actionPerformed(ActionEvent e) {
    task.cancel(true);
    guiLogger.info("Processing of SOS Server CANCELLED");
    firePropertyChange("closed", false, true);
  }


  class ListenForCapProgress implements PropertyChangeListener {

    public void propertyChange(PropertyChangeEvent evt) {
      if ("progress".equals(evt.getPropertyName())) {
        int progress = (Integer) evt.getNewValue();
        progressBar.setValue(progress);
        task.setTaskProgress(progress);
      } else if ("message".equals(evt.getPropertyName())) {
        taskOutput.append(String.format("%1$s\n", evt.getNewValue()));
        guiLogger.info((String)evt.getNewValue());
      } else if (TASK_COMPLETE.equals(evt.getPropertyName())) {
        guiLogger.info("Processing of SOS Server complete");
       firePropertyChange(TASK_COMPLETE, evt.getOldValue(), evt.getNewValue());
      }
    }
  }
}
