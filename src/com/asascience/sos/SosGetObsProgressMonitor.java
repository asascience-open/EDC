/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.asascience.sos;

import com.asascience.sos.types.Generic;
import java.awt.BorderLayout;
import java.awt.Cursor;
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

/**
 *
 * @author Kyle
 */
public class SosGetObsProgressMonitor extends JPanel implements ActionListener, PropertyChangeListener {

  private JButton startButton;
  private JProgressBar progressBar;
  private JTextArea taskOutput;
  private Task task;
  private Generic sosData;
  private ListenForProgress listener;

  class Task extends SwingWorker<Void, Void> {
    @Override
    public Void doInBackground() {
      setProgress(0);
      try {
        sosData.addPropertyChangeListener(listener);
        sosData.getObservations();
      } catch (Exception e) {
        taskOutput.append(String.format("%1$s\n", e.toString()));
      }
      return null;
    }

    public void setTaskProgress(int progress) {
      setProgress(progress);
    }
    
    @Override
    public void done() {
      startButton.setEnabled(true);
      sosData.removePropertyChangeListener(listener);
      setCursor(null);
      taskOutput.append("Done!\n");
    }
  }

  public SosGetObsProgressMonitor(Generic data) {
    super(new BorderLayout());

    listener = new ListenForProgress();
    
    sosData = data;
   
    startButton = new JButton("Start");
    startButton.setActionCommand("start");
    startButton.addActionListener(this);

    progressBar = new JProgressBar(0, 100);
    progressBar.setValue(0);
    progressBar.setStringPainted(true);

    taskOutput = new JTextArea(5, 20);
    taskOutput.setMargin(new Insets(5,5,5,5));
    taskOutput.setEditable(false);

    JPanel panel = new JPanel();
    panel.add(startButton);
    panel.add(progressBar);

    add(panel, BorderLayout.PAGE_START);
    add(new JScrollPane(taskOutput), BorderLayout.CENTER);
    setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
  }


  public void actionPerformed(ActionEvent e) {
    setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
    task = new Task();
    task.addPropertyChangeListener(listener);
    task.execute();
    startButton.setEnabled(false);
  }

  public void propertyChange(PropertyChangeEvent evt) {
    listener.propertyChange(evt);
  }

  class ListenForProgress implements PropertyChangeListener {
    public void propertyChange(PropertyChangeEvent evt) {
      if ("progress".equals(evt.getPropertyName())) {
        int progress = (Integer)evt.getNewValue();
        progressBar.setValue(progress);
        task.setTaskProgress(progress);
      } else if ("message".equals(evt.getPropertyName())) {
        taskOutput.append(String.format("%1$s\n", evt.getNewValue()));
      }
    }
  }
}
