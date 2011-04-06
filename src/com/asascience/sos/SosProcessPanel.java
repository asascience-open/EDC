/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.asascience.sos;

import com.asascience.edc.gui.OpendapInterface;
import com.asascience.edc.gui.SelectionPanelBase;
import gov.noaa.pmel.swing.JSlider2Date;

import java.awt.BorderLayout;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EtchedBorder;

import net.miginfocom.swing.MigLayout;
import ucar.nc2.ui.widget.FileManager;

import com.asascience.edc.nc.NetcdfConstraints;
import com.asascience.openmap.ui.OMSelectionMapPanel;
import com.asascience.utilities.Utils;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

/**
 *
 * @author Kyle
 */
public class SosProcessPanel extends JPanel {

  private SosData sosData;
  private String sysDir;
  private OMSelectionMapPanel mapPanel;
  private SelectionPanelBase selPanel;
  private NetcdfConstraints constraints;
  private JSlider2Date dateSlider;
  private JButton btnProcess;
  private JLabel lblDateIncrement;
	private JLabel lblNumDatesSelected;

  public SosProcessPanel(ucar.util.prefs.PreferencesExt prefs,
                          FileManager fileChooser, OpendapInterface caller,
                          NetcdfConstraints cons, SosData sosd, String homeDir, String sysDir) {
    this.sosData = sosd;
    this.sysDir = Utils.appendSeparator(sysDir);
    this.constraints = cons;
    initComponents();
  }
  
  public boolean initData() {
    // Opens the old panel (popup)
    /*
    SosGui mygiu = new SosGui(SosProcessPanel.this);
    mygiu.sosAction(sosData);
    */

    return true;
  }

  public void setData(SosData data) {
    this.sosData = data;
  }

  private boolean initComponents() {
		try {
			setLayout(new BorderLayout());
			setBorder(BorderFactory.createRaisedBevelBorder());

			// create the map panel
			String gisDataDir = sysDir + "data";
			mapPanel = new OMSelectionMapPanel(constraints, gisDataDir, false);
      mapPanel.makeDataExtentLayer(sosData.getData().getBBOX());
			mapPanel.setBorder(new EtchedBorder());
			add(mapPanel, BorderLayout.CENTER);

      // Variable selection panel
      selPanel = new SosVariableSelectionPanel(constraints, this);

			selPanel.addPropertyChangeListener(new PropertyChangeListener() {
				public void propertyChange(PropertyChangeEvent e) {
					if (btnProcess != null) {
						String name = e.getPropertyName();
						if (name.equals("processEnabled")) {
							btnProcess.setEnabled(Boolean.valueOf(e.getNewValue().toString()));
						}
					}
				}
			});
			add(selPanel, BorderLayout.LINE_END);

			// create a panel to hold the time and processing panels
			JPanel pageEndPanel = new JPanel(new MigLayout("insets 0, fill"));
			pageEndPanel.setBorder(new EtchedBorder());

			// create a panel to hold all the time related components
			JPanel timePanel = new JPanel(new MigLayout("insets 0, fill"));
			timePanel.setBorder(new EtchedBorder());

			dateSlider = new JSlider2Date();
			dateSlider.setAlwaysPost(true);
			dateSlider.setHandleSize(7);

      lblDateIncrement = new JLabel("Time Interval (sec): ");
			lblNumDatesSelected = new JLabel("# Timesteps Selected: ");

			// TODO: add a means for remembering time-ranges?
			timePanel.add(lblDateIncrement, "gap 0, gapright 20, center, split 2");
			timePanel.add(lblNumDatesSelected, "gap 0, gapleft 20, center, wrap");
			timePanel.add(dateSlider, "gap 0, grow, center");

			// add the time panel to the SosProcessPanel
			pageEndPanel.add(timePanel, "grow, wrap");

			JPanel processPanel = new JPanel();
			processPanel.setBorder(new EtchedBorder());
			btnProcess = new JButton("Get Observations");
			btnProcess.setToolTipText("Apply the specified spatial & temporal constraints\n"
				+ "and export the selected variables to the desired output format.");
			btnProcess.setEnabled(false);
			//btnProcess.addActionListener(new ProcessDataListener());
			processPanel.add(btnProcess);

			pageEndPanel.add(processPanel, "grow");

			add(pageEndPanel, BorderLayout.PAGE_END);

		} catch (Exception ex) {
			ex.printStackTrace();
		}
    return true;
  }
/*
  class ProcessDataListener implements ActionListener {

		public void actionPerformed(ActionEvent e) {
			if (selPanel != null) {
				// get the extent set by the user
				LatLonRect bounds = mapPanel.getSelectedExtent();
				// if no user extent, use entire extent
				if (bounds == null) {
					bounds = ncReader.getBounds();
				}// bounds = parent.getNcExtent();
				constraints.setBoundingBox(bounds);

				// set the start & end date/time
				constraints.setStartTime(null);
				constraints.setEndTime(null);
				if (ncReader.isHasTime()) {
					constraints.setStartTime(dateSlider.getMinValue().getCalendar().getTime());
					constraints.setEndTime(dateSlider.getMaxValue().getCalendar().getTime());
				}

				// set the panel type
				constraints.setPanelType(selPanel.getPanelType());

				double lonSpan = Math.abs(constraints.getWesternExtent()) - Math.abs(constraints.getEasternExtent());
				double latSpan = constraints.getNorthernExtent() - constraints.getSouthernExtent();

				// if(!selPanel.isHasGeoSub() | (Math.abs(lonSpan) > 90 |
				// Math.abs(latSpan) > 90)){
				if ((Math.abs(lonSpan) > 90 | Math.abs(latSpan) > 90)) {
					if (JOptionPane.showConfirmDialog(mainFrame, "The geospatial subset is larger than 90 degrees.\n"
						+ "This may result in a very large dataset.\n\nDo you wish to continue?", "Geospatial Subset",
						JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) == JOptionPane.NO_OPTION) {
						return;
					}
				}
				// int rng = endSlider.getValue() - startSlider.getValue();
				if (ncReader.isHasTime()) {
					double rng = calcNumTimesteps();
					// System.err.println("timesteps="+rng);
					if (rng >= 100) {
						if (JOptionPane.showConfirmDialog(mainFrame, "The temporal subset has not been indicated"
							+ " or is larger than 100 timesteps.\n"
							+ "This may result in a very large dataset.\n\nDo you wish to continue?",
							"Temporal Subset", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) == JOptionPane.NO_OPTION) {
							return;
						}
					}
				}
			} else {
				return;
			}

			// String homeDir = Utils.retrieveHomeDirectory("ODC");

			boolean cont = false;
			boolean skip = false;
			boolean rept = false;
			String outname = "outname";
			String inname = "outname";
			if (selPanel.getCblVars().getSelItemsSize() > 0) {
				inname = selPanel.getCblVars().getSelectedItems().get(0);
			}
			if (inname != null) {
				// alter default name
				if (Configuration.USE_VARIABLE_NAME_FOR_OUTPUT) {
					// just the variable name
					outname = createSuitableLayerName(inname.substring(1, inname.indexOf("]")));
				} else {
					// the description with underscores
          outname = createSuitableLayerName(selPanel.getFullDescriptionFromShortDescription(inname));
				}
			}
			if (Configuration.DISPLAY_TYPE == Configuration.DisplayType.ESRI) {
				if (selPanel.isMakeRaster()) {
					// If longer, trim the name down to the first 8 characters
					if (outname.length() > 8) {
						outname = outname.substring(0, 7);
					}
				}
			}

			File f;
			if (Configuration.USE_SUBDIRECTORIES) {
				f = Utils.createIncrementalName(homeDir, outname, null);
				outname = createSuitableLayerName(f.getName());
			} else {
				f = Utils.createIncrementalName(homeDir, outname, ".nc");
				outname = createSuitableLayerName(f.getName().replace(".nc", ""));
			}

			do {
				skip = false;

				outname = (String) JOptionPane.showInputDialog(mainFrame, "Enter a name for the output file:",
					"Output Name", JOptionPane.OK_CANCEL_OPTION, null, null, outname);

				// If Cancal was clicked, outname would be null
				if (outname == null) {// cancel
					return;
				}

        // Cancel was not clicked, set the outname
        outname = createSuitableLayerName(outname);

        if (Configuration.DISPLAY_TYPE == Configuration.DisplayType.ESRI) {// ESRI
          if (selPanel.isMakeRaster()) {
            if (!rasterNameOk(outname)) {
              JOptionPane.showMessageDialog(null, "The name contains illegal characters "
                + "or is too long.\nThe maximum allowable size is 10 characters.\n"
                + "Special characters (i.e. @, #, $, \"space\", etc.) are not allowed.",
                "Invalid Name", JOptionPane.WARNING_MESSAGE);
              skip = true;
            }
          }
        }

        if (!skip) {
          if (Configuration.USE_SUBDIRECTORIES) {
            f = new File(homeDir + File.separator + outname);
          } else {
            f = new File(homeDir + File.separator + outname + ".nc");
          }

          if (f.exists()) {
            if (Configuration.ALLOW_FILE_REPLACEMENT) {
              int i = JOptionPane.showConfirmDialog(mainFrame,
                "An output file with this name already exists." + "\nDo you wish to replace it?",
                "Duplicate Name", JOptionPane.YES_NO_OPTION);
              if (i == JOptionPane.YES_OPTION) {
                // System.err.println(f.getParentFile().getAbsolutePath());
                if (Configuration.USE_SUBDIRECTORIES) {
                  Utils.deleteDirectory(f);
                } else {
                  f.delete();
                }

                // f.delete();
                cont = true;
              } else {
                cont = false;
              }
            } else {
              JOptionPane.showMessageDialog(mainFrame,
                "An output file with this name already exists."
                  + "\nPlease select a different name to continue.", "Duplicate Name",
                JOptionPane.OK_OPTION);
              cont = false;
            }
          } else {
            cont = true;
          }
        }
			} while (!cont);

			if (f != null) {
				if (Configuration.USE_SUBDIRECTORIES) {
					if (!f.mkdirs()) {
						System.err.println("SubsetProcessPanel.ProcesDataListener: " + "Could not make directory \""
							+ f.getAbsolutePath() + "\"");
					}
				} else {
					try {
						f.createNewFile();
					} catch (Exception ex) {
						ex.printStackTrace();
					}
				}
			}

			ncOutPath = "";
			if (Configuration.USE_SUBDIRECTORIES) {
				ncOutPath = f.getAbsolutePath() + File.separator + outname + ".nc";
			} else {
				ncOutPath = f.getAbsolutePath();
			}

			IndeterminateProgressDialog pd = new IndeterminateProgressDialog(mainFrame, "Progress", new ImageIcon(Utils
				.getImageResource("ASA.png", SubsetProcessPanel.class)));
			ProcessDataTask pdt = new ProcessDataTask("Processing Data...", ncOutPath, outname);
			pdt.addPropertyChangeListener(new ProcessPropertyListener());
			pd.setRunTask(pdt);
			pd.runTask();
		}
	}
*/
}
