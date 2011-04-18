/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.asascience.sos;

import com.asascience.edc.gui.OpendapInterface;
import java.awt.BorderLayout;
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
public class SosGetCapProgressMonitor extends JPanel implements ActionListener, PropertyChangeListener {

  private JProgressBar progressBar;
  private JTextArea taskOutput;
  private Task task;
  private SosData sosData;
  private ListenForProgress listener;
  private OpendapInterface odapInterface;
  private JButton closeButton;

  class Task extends SwingWorker<Void, Void> {

    @Override
    public Void doInBackground() {
      setProgress(0);
      try {
        sosData.addPropertyChangeListener(listener);
        boolean testCapParse = sosData.parseSosGetCapabilities();

        if (!testCapParse || isCancelled()) {
          taskOutput.append(String.format("Can't Read SOS service!\n"));
          return null;
        }
        if (!isCancelled()) {
          //sosData.getData().addPropertyChangeListener(listener);
          firePropertyChange("message", null, "Adding sensors to map (this could take awhile)");
          odapInterface.openSOSDataset(sosData, this);
          sosData.getData().setHomeDir(odapInterface.getHomeDir());
          firePropertyChange("progress", null, 100);
        }
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
      taskOutput.append("Done!\n");
      closeButton.setText("Close");
    }
  }

  public SosGetCapProgressMonitor(SosData data, OpendapInterface odap) {
    super(new BorderLayout());
    super.setSize(600, 300);

    listener = new ListenForProgress();

    sosData = data;
    odapInterface = odap;

    progressBar = new JProgressBar(0, 100);
    progressBar.setValue(0);
    progressBar.setStringPainted(true);

    taskOutput = new JTextArea(5, 20);
    taskOutput.setMargin(new Insets(5, 5, 5, 5));
    taskOutput.setEditable(false);

    closeButton = new JButton("Cancel");
    closeButton.addActionListener(this);

    JPanel panel = new JPanel();
    panel.add(closeButton);
    panel.add(progressBar);

    add(panel, BorderLayout.PAGE_START);
    add(new JScrollPane(taskOutput), BorderLayout.CENTER);
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
    firePropertyChange("closed", false, true);
  }

  class ListenForProgress implements PropertyChangeListener {

    public void propertyChange(PropertyChangeEvent evt) {
      if ("progress".equals(evt.getPropertyName())) {
        int progress = (Integer) evt.getNewValue();
        progressBar.setValue(progress);
        task.setTaskProgress(progress);
      } else if ("message".equals(evt.getPropertyName())) {
        taskOutput.append(String.format("%1$s\n", evt.getNewValue()));
      }
    }
  }
}
