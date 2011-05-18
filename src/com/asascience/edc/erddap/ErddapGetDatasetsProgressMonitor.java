/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.asascience.edc.erddap;

import com.asascience.edc.erddap.gui.ErddapDatasetViewer;
import com.asascience.edc.gui.OpendapInterface;
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
import net.miginfocom.swing.MigLayout;

/**
 *
 * @author Kyle
 */
public class ErddapGetDatasetsProgressMonitor extends JPanel implements ActionListener, PropertyChangeListener {
  private JProgressBar progressBar;
  private JTextArea taskOutput;
  private Task task;
  private ListenForProgress listener;
  private OpendapInterface odapInterface;
  private ErddapDatasetViewer erddapViewer;
  private JButton closeButton;

  class Task extends SwingWorker<Void, Void> {

    @Override
    public Void doInBackground() {
      setProgress(0);
      try {
        erddapViewer.getServer().addPropertyChangeListener(listener);
        boolean testParse = erddapViewer.getServer().processDatasets();

        if (!testParse || isCancelled()) {
          taskOutput.append(String.format("Can't read from ERDDAP server!\n"));
          return null;
        }
        if (!isCancelled()) {
          //sosData.getData().addPropertyChangeListener(listener);
          firePropertyChange("message", null, "Displaying Datasetes");
          odapInterface.openErddapDataset(erddapViewer, this);
          //erddapServer.getRequest().setHomeDir(odapInterface.getHomeDir());
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

  public ErddapGetDatasetsProgressMonitor(ErddapDatasetViewer viewer, OpendapInterface odap) {
    super(new MigLayout("fill"));

    listener = new ListenForProgress();

    odapInterface = odap;
    erddapViewer = viewer;

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
