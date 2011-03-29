// $Id: CatalogChooser.java 50 2006-07-12 16:30:06Z caron $
/*
 * Copyright 1997-2006 Unidata Program Center/University Corporation for
 * Atmospheric Research, P.O. Box 3000, Boulder, CO 80307,
 * support@unidata.ucar.edu.
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or (at
 * your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser
 * General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, write to the Free Software Foundation,
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */
package com.asascience.edc.threddsui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;

import javax.swing.AbstractAction;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSplitPane;
import javax.swing.KeyStroke;
import javax.swing.RootPaneContainer;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.EventListenerList;
import javax.swing.filechooser.FileFilter;

import net.miginfocom.swing.MigLayout;
import thredds.catalog.DatasetFilter;
import thredds.catalog.InvAccess;
import thredds.catalog.InvCatalog;
import thredds.catalog.InvCatalogFactory;
import thredds.catalog.InvCatalogImpl;
import thredds.catalog.InvCatalogRef;
import thredds.catalog.InvDataset;
import thredds.catalog.InvDatasetImpl;
import thredds.catalog.InvService;
import thredds.catalog.ServiceType;
import thredds.catalog.XMLEntityResolver;
import thredds.ui.catalog.ThreddsDatasetChooser;
import thredds.cataloggen.DirectoryScanner;
import ucar.nc2.ui.widget.BAMutil;
import ucar.nc2.ui.widget.FileManager;
import ucar.nc2.ui.widget.HtmlBrowser;
import ucar.nc2.ui.widget.IndependentWindow;
import ucar.nc2.ui.widget.TextGetPutPane;
import ucar.nc2.dataset.NetcdfDataset;
import ucar.util.prefs.PreferencesExt;
import ucar.util.prefs.ui.ComboBox;

import com.asascience.edc.gui.OpendapInterface;
import com.asascience.ui.CheckBoxList;
import com.asascience.utilities.BusyCursorActions;
import com.asascience.utilities.Utils;
import com.asascience.sos.SosData;
import com.asascience.sos.SosGui;

/**
 * A Swing widget for THREDDS clients to access and choose from Dataset
 * Inventory catalogs. State is maintained in a ucar.util.Preferences store.
 * <p>
 * A list of catalogs is kept in a ComboBox, and the user can choose from them
 * and add new ones. When a catalog is chosen, its contents are displayed in a
 * CatalogTreeView. As the datasets are browsed, the metadata is displayed in an
 * HtmlBrowser widget.
 * <p>
 * When a new dataset is selected, a java.beans.PropertyChangeEvent is thrown,
 * see addPropertyChangeListener.
 * <p>
 * Use Example:
 * 
 * <pre>
 * // create widgets
 * catalogChooser = new thredds.ui.CatalogChooser( prefs);
 * catalogChooserDialog = catalogChooser.makeDialog(rootPaneContainer, &quot;Open THREDDS dataset&quot;, true);
 * // listen for selection
 * catalogChooser.addPropertyChangeListener(  new java.beans.PropertyChangeListener() {
 * public void propertyChange( java.beans.PropertyChangeEvent e) {
 * if (e.getPropertyName().equals(&quot;Dataset&quot;)) {
 * ..
 * }
 * }
 * });
 * // popup dialog
 * catalogChooserDialog.show();
 * </pre>
 * 
 * You can use the CatalogChooser alone, wrap it into a JDialog for popping up,
 * or use a ThreddsDatasetChooser instead, for a more complete interface.
 * 
 * @see ThreddsDatasetChooser
 * @see thredds.catalog.InvDataset
 * 
 * @author John Caron
 * @version $Id: CatalogChooser.java 50 2006-07-12 16:30:06Z caron $
 */
public class ASACatalogChooser extends JPanel {

	private static final String HDIVIDER = "HSplit_Divider";
	private static final String FILECHOOSER_DEFAULTDIR = "FileChooserDefaultDir";
	private ucar.util.prefs.PreferencesExt prefs;
	private EventListenerList listenerList = new EventListenerList();
	private String eventType = null;
	// ui
	private ComboBox catListBox;
	// private DAComboBox catListBox;
	private ASACatalogTreeView tree;
	private HtmlBrowser htmlViewer;
	private FileManager fileChooser;
	private JSplitPane split;
	private JLabel statusLabel;
	private JPanel buttPanel;
	private JLabel sourceText;
	private RootPaneContainer parent = null;
	private JFrame parentFrame;
	private OpendapInterface odapInterface;
	private boolean datasetEvents = true;
	private boolean catrefEvents = false;
	private String currentURL = "";
	// private boolean catgenShow = true;
	private FileManager catgenFileChooser;
	private boolean debugEvents = false;
	// private boolean debugTree = false;
	private boolean showHTML = false;
	private JButton acceptButton;
	private NetcdfDataset daDataset;
	private DAComboBox daListBox;
	private SOSComboBox sosListBox;

	private JButton btnCatConnect;
	private JButton btnDirAccess;
	private JButton btnSOS;
	private SosGui SosGui;
	JPanel topPanel;

	/**
	 * Constructor, with control over whether a comboBox of previous catalogs is
	 * shown.
	 * 
	 * @param prefs
	 *            persistent storage, may be null.
	 * @param showComboChooser
	 * @param showOpenButton
	 *            show the "open" button.
	 * @param showFileChooser
	 *            show a FileChooser (must have showComboChooser true)
	 * @param parent
	 */
	public ASACatalogChooser(ucar.util.prefs.PreferencesExt prefs, boolean showComboChooser, boolean showOpenButton,
		boolean showFileChooser, OpendapInterface parent) {
		this.prefs = prefs;
		this.parentFrame = parent.getMainFrame();

		this.odapInterface = parent;

		topPanel = null;

		if (showComboChooser) {

			// combo box holds the catalogs
			catListBox = new ComboBox(prefs);
			daListBox = new DAComboBox(prefs);
			sosListBox = new SOSComboBox(prefs);

			// top panel buttons
			btnCatConnect = new JButton("Connect");
			btnCatConnect.setToolTipText("Read the selected catalog");
			btnCatConnect.addActionListener(new ActionListener() {

				public void actionPerformed(ActionEvent evt) {
					String catalogURL = (String) catListBox.getSelectedItem();
					tree.setCatalog(catalogURL.trim()); // will get "Catalog"
					// property change event
					// if ok
				}
			});

			// David Added sos panel button
			btnSOS = new JButton("Connect");
			btnSOS.setToolTipText("Read the selected SOS");
			btnSOS.addActionListener(new ActionListener() {

				public void actionPerformed(ActionEvent evt) {

					SosData myData = null;
					try {
						
						Utils.showBusyCursor(ASACatalogChooser.this);
						
						myData = new SosData();
						String sosURL = (String) sosListBox.getSelectedItem();


						myData.setmyUrl(sosURL);

						myData.setHomeDir(odapInterface.getHomeDir());
						
						// statusLabel.setText("Testing SOS URL...");
						//
						// if (!SosData.checkSOS()) {
						// System.out.println("Can't Ping URL!");
						// JOptionPane.showConfirmDialog((Component) topPanel,
						// "Bad SOS URL", "ASA",
						// JOptionPane.OK_CANCEL_OPTION,
						// JOptionPane.ERROR_MESSAGE);
						//
						// statusLabel.setText("Not Connected");
						//
						// return;
						// }
						//
						// statusLabel.setText("URL is valid, Connecting to Service");

						// // Can't fix status label - does not change on click?
						// statusLabel.setText("Connecting to Service and Reading data");
						// ASACatalogChooser.this.repaint();

						boolean test = myData.sosGetCapabilities();
						if (!test) {
							System.out.println("Can't Read SOS service!");
							JOptionPane.showConfirmDialog((Component) topPanel, "Bad SOS Connection", "ASA",
								JOptionPane.OK_CANCEL_OPTION, JOptionPane.ERROR_MESSAGE);

							statusLabel.setText("Not Connected");

							return;
						}

						statusLabel.setText("Connected to SOS!");

					} finally {
						
						Utils.hideBusyCursor(ASACatalogChooser.this);
						
					}
						SosGui mygiu = new SosGui(ASACatalogChooser.this);
						mygiu.sosAction(myData); // will get "Catalog"
						// property change event if
						// ok

				}
			});

			// NEW
			btnDirAccess = new JButton("Direct Access...");
			btnDirAccess.setToolTipText("Enter a dataset url for direct access");
			ActionListener al = new ActionListener() {

				public void actionPerformed(ActionEvent ae) {
					String dataUrl = "";
					try {
						// dataUrl =
						// JOptionPane.showInputDialog(odapInterface.getMainFrame(),
						// "Enter a data url:", "Direct Data Access...");
						dataUrl = daListBox.getSelectedItem().toString();

						if (dataUrl == null | dataUrl.equals("")) {
							return;
						}// cancel clicked

						// setThreddsDatatype(dataUrl);

						System.err.println("Dataset Url: " + dataUrl);
						System.err.println("Retrieving data...");
						// odapInterface.getMainFrame().setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

						daDataset = NetcdfDataset.openDataset(dataUrl);

						daDataset.setTitle(dataUrl.substring(dataUrl.lastIndexOf(File.separator) + 1));

						if (odapInterface.openDataset(daDataset)) {
							daListBox.addItem(dataUrl);
						}
						// OpenDirectAccessDataset odad = new
						// OpenDirectAccessDataset(ncd);
						// oi.openDataset(ncd);

					} catch (IOException ex) {
						JOptionPane.showMessageDialog(odapInterface.getMainFrame(), "Cannot find the file\n\""
							+ dataUrl + "\"" + "\n\nPlease check the name and try again.");
						System.err.println("OI:directAccess: Invalid filename");
						ex.printStackTrace();
					} finally {
						// odapInterface.getMainFrame().setCursor(Cursor.getDefaultCursor());
					}
				}
			};
			ActionListener cal = com.asascience.utilities.BusyCursorActions.createListener(
				odapInterface.getMainFrame(), al);
			btnDirAccess.addActionListener(cal);

			// btnDirAccess.addActionListener(new OpenDADatasetListener());

			JPanel topButtons = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
			topButtons.add(btnCatConnect);
			topButtons.add(btnDirAccess);
			topButtons.add(btnSOS); // David Added

			// topPanel = new JPanel(new BorderLayout());
			// topPanel.add(new JLabel("Catalog URL"), BorderLayout.WEST);
			// topPanel.add(catListBox, BorderLayout.CENTER);
			// topPanel.add(topButtons, BorderLayout.EAST);

			topPanel = new JPanel(new MigLayout("fillx, insets 0"));

			ActionListener radioListener = new ActionListener() {

				public void actionPerformed(ActionEvent ae) {
					// "enable" boolean: if true - enable cat & disable da
					// if false - disable cat & enable da
					boolean enable = true;
					String cmd = ae.getActionCommand();
					if (cmd.equals("cat")) {
						catListBox.setEnabled(enable);
						btnCatConnect.setEnabled(enable);
						daListBox.setEnabled(!enable);
						btnDirAccess.setEnabled(!enable);
						sosListBox.setEnabled(!enable);
						btnSOS.setEnabled(!enable);
						// enable = true;
					} else if (cmd.equals("da")) {
						catListBox.setEnabled(!enable);
						btnCatConnect.setEnabled(!enable);
						daListBox.setEnabled(enable);
						btnDirAccess.setEnabled(enable);
						sosListBox.setEnabled(!enable);
						btnSOS.setEnabled(!enable);
						// enable = false;
					} else if (cmd.equals("sos")) {
						catListBox.setEnabled(!enable);
						btnCatConnect.setEnabled(!enable);
						daListBox.setEnabled(!enable);
						btnDirAccess.setEnabled(!enable);
						sosListBox.setEnabled(enable);
						btnSOS.setEnabled(enable);
					}

					// catListBox.setEnabled(enable);
					// btnCatConnect.setEnabled(enable);
					// daListBox.setEnabled(!enable);
					// btnDirAccess.setEnabled(!enable);
					// sosListBox.setEnabled(!enable);
					// btnSOS.setEnabled(!enable);
				}
			};

			JRadioButton rbCatUrl = new JRadioButton("Catalog URL:");
			rbCatUrl.setActionCommand("cat");
			rbCatUrl.addActionListener(radioListener);
			topPanel.add(rbCatUrl);
			rbCatUrl.addMouseListener(new MouseAdapter() {

				@Override
				public void mouseClicked(MouseEvent e) {
					if (e.getButton() == MouseEvent.BUTTON3) {// right-click
						if (catListBox.isEnabled()) {
							ListEditorDialog dia = new ListEditorDialog(parentFrame, "Remove Catalog Items",
								"ComboBoxList");
							dia.setVisible(true);

							if (saveDialogChanges) {
								java.util.List<String> remItems = dia.getItemsToRemove();
								for (String s : remItems) {
									for (int i = 0; i < catListBox.getItemCount(); i++) {
										if (s.equals(catListBox.getItemAt(i).toString())) {
											catListBox.removeItemAt(i);
											break;
										}
									}
								}
								// for(int i = 0; i < catListBox.getItemCount();
								// i++){
								// String item =
								// catListBox.getItemAt(i).toString();
								// for(String rem : remItems){
								// if(rem.equals(item)){
								// catListBox.removeItemAt(i);
								// }
								// }
								// }
							}
							dia.dispose();

							// update the prefs
							catListBox.save();
						}
					}
				}
			});
			topPanel.add(catListBox, "growx");
			topPanel.add(btnCatConnect, "wrap");
			// topPanel.add(edit, "wrap");

			JRadioButton rbDaUrl = new JRadioButton("Direct Access URL:");
			rbDaUrl.setActionCommand("da");
			rbDaUrl.addActionListener(radioListener);
			rbDaUrl.addMouseListener(new MouseAdapter() {

				@Override
				public void mouseClicked(MouseEvent e) {
					if (e.getButton() == MouseEvent.BUTTON3) {// right-click
						if (daListBox.isEnabled()) {
							ListEditorDialog dia = new ListEditorDialog(parentFrame, "Remove Direct Access Items",
								"DAComboBoxList");
							dia.setVisible(true);

							if (saveDialogChanges) {
								java.util.List<String> remItems = dia.getItemsToRemove();
								for (String s : remItems) {
									for (int i = 0; i < daListBox.getItemCount(); i++) {
										if (s.equals(daListBox.getItemAt(i).toString())) {
											daListBox.removeItemAt(i);
											break;
										}
									}
								}
								// java.util.List<String> remItems =
								// dia.getItemsToRemove();
								// for(int i = 0; i < daListBox.getItemCount();
								// i++){
								// String item =
								// daListBox.getItemAt(i).toString();
								// for(String rem : remItems){
								// if(rem.equals(item)){
								// daListBox.removeItemAt(i);
								// }
								// }
								// }
							}
							dia.dispose();

							// update the prefs
							daListBox.save();
						}
					}
				}
			});
			topPanel.add(rbDaUrl);
			topPanel.add(daListBox, "growx");
			topPanel.add(btnDirAccess, "wrap"); // David Added "wrap"

			// David added:

			// List of previous servers in the SOS LIST
			// http://sdf.ndbc.noaa.gov/sos/server.php
			// http://opendap.co-ops.nos.noaa.gov/ioos-dif-sos/SOS
			// http://neptune.baruch.sc.edu/cgi-bin/oostethys_sos.cgi
			// http://www.gomoos.org/cgi-bin/sos/V1.0/oostethys_sos.cgi

			JRadioButton rbDaSOS = new JRadioButton("Sensor Obs Service:");
			rbDaSOS.setActionCommand("sos");
			rbDaSOS.addActionListener(radioListener);
			rbDaSOS.addMouseListener(new MouseAdapter() {

				@Override
				public void mouseClicked(MouseEvent e) {
					if (e.getButton() == MouseEvent.BUTTON3) {// right-click
						if (sosListBox.isEnabled()) {
							ListEditorDialog dia = new ListEditorDialog(parentFrame, "Remove SOS Access Items",
								"SOSComboBoxList");
							dia.setVisible(true);

							if (saveDialogChanges) {
								java.util.List<String> remItems = dia.getItemsToRemove();
								for (String s : remItems) {
									for (int i = 0; i < sosListBox.getItemCount(); i++) {
										if (s.equals(sosListBox.getItemAt(i).toString())) {
											sosListBox.removeItemAt(i);
											break;
										}
									}
								}
								// java.util.List<String> remItems =
								// dia.getItemsToRemove();
								// for(int i = 0; i < daListBox.getItemCount();
								// i++){
								// String item =
								// daListBox.getItemAt(i).toString();
								// for(String rem : remItems){
								// if(rem.equals(item)){
								// daListBox.removeItemAt(i);
								// }
								// }
								// }
							}
							dia.dispose();

							// update the prefs
							sosListBox.save();
						}
					}
				}
			});
			topPanel.add(rbDaSOS);
			topPanel.add(sosListBox, "growx");
			topPanel.add(btnSOS);

			// end David Added

			ButtonGroup rbGroup = new ButtonGroup();
			rbGroup.add(rbCatUrl);
			rbGroup.add(rbDaUrl);
			rbGroup.add(rbDaSOS);

			rbCatUrl.doClick();

			if (showFileChooser) {
				// add a file chooser
				PreferencesExt fcPrefs = (PreferencesExt) prefs.node("FileManager");
				FileFilter[] filters = new FileFilter[] { new FileManager.XMLExtFilter() };
				fileChooser = new FileManager(null, null, filters, fcPrefs);

				AbstractAction fileAction = new AbstractAction() {

					public void actionPerformed(ActionEvent e) {
						String filename = fileChooser.chooseFilename();
						if (filename == null) {
							return;
						}
						tree.setCatalog("file:" + filename);
					}
				};
				BAMutil.setActionProperties(fileAction, "FileChooser", "open Local catalog...", false, 'L', -1);
				BAMutil.addActionToContainer(topButtons, fileAction);

				// a file chooser used for catgen on a directory
				PreferencesExt catgenPrefs = (PreferencesExt) prefs.node("CatgenFileManager");
				catgenFileChooser = new FileManager(null, null, null, catgenPrefs);
				catgenFileChooser.getFileChooser().setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
				catgenFileChooser.getFileChooser().setDialogTitle("Run CatGen on Directory");

				AbstractAction catgenAction = new AbstractAction() {

					public void actionPerformed(ActionEvent e) {
						String filename = catgenFileChooser.chooseFilename();
						if (filename == null) {
							return;
						}
						File d = new File(filename);
						if (d.isDirectory()) {
							System.err.println("Run catgen on filename");

							InvService service = new InvService("local", ServiceType.FILE.toString(), d.toURI()
								.toString(), null, null);
							DirectoryScanner catgen = new DirectoryScanner(service, "local access to files", d, null,
								false);

							InvCatalogImpl cat = (InvCatalogImpl) catgen.getDirCatalog(d, null, false, false);

							InvCatalogFactory catFactory = InvCatalogFactory.getDefaultFactory(true);

							// internalize to dirscan?
							try {
								cat.setBaseURI(new URI(d.toURI().toString() + "catalog.xml"));
							} catch (URISyntaxException e1) {
								e1.printStackTrace();
							}
							catFactory.setCatalogConverter(cat, XMLEntityResolver.CATALOG_NAMESPACE_10);

							setCatalog(cat);

							try {
								// System.err.println( catFactory.writeXML(
								// cat));
								catFactory.writeXML(cat, filename + "/catalog.xml");
							} catch (IOException e1) {
								e1.printStackTrace(); // To change body of catch
								// statement use File |
								// Settings | File
								// Templates.
							}
						}
					}
				};
				BAMutil.setActionProperties(catgenAction, "catalog", "run catgen on directory...", false, 'L', -1);
				BAMutil.addActionToContainer(topButtons, catgenAction);

				AbstractAction srcEditAction = new AbstractAction() {

					public void actionPerformed(ActionEvent e) {
						TextGetPutPane sourceEditor = new TextGetPutPane(null);
						IndependentWindow sourceEditorWindow = new IndependentWindow("Source", BAMutil
							.getImage("thredds"), sourceEditor);
						sourceEditorWindow.setBounds(new Rectangle(50, 50, 725, 450));
						sourceEditorWindow.show();
					}
				};
				BAMutil.setActionProperties(srcEditAction, "Edit", "Source Editor", false, 'E', -1);
				BAMutil.addActionToContainer(topButtons, srcEditAction);
			}
		}

		// the catalog tree
		tree = new ASACatalogTreeView();
		tree.addPropertyChangeListener(new java.beans.PropertyChangeListener() {

			public void propertyChange(java.beans.PropertyChangeEvent e) {
				if (debugEvents) {
					System.err.println("CatalogChooser propertyChange name=" + e.getPropertyName() + "=");
				}

				if (e.getPropertyName().equals("Catalog")) {
					String catalogURL = (String) e.getNewValue();
					setCurrentURL(catalogURL);
					if (catListBox != null) {
						catListBox.addItem(catalogURL);
					}
					firePropertyChangeEvent(e);

				} else if (e.getPropertyName().equals("Selection")) {

					InvDatasetImpl ds = (InvDatasetImpl) tree.getSelectedDataset();
					if (ds == null) {
						return;
					}
					showDatasetInfo(ds);

					if (ds instanceof InvCatalogRef) {
						InvCatalogRef ref = (InvCatalogRef) ds;
						String href = ref.getXlinkHref();
						try {
							java.net.URI uri = ref.getParentCatalog().resolveUri(href);
							setCurrentURL(uri.toString());
						} catch (Exception ee) {
						}
					} else if (ds.getParent() == null) { // top
						setCurrentURL(tree.getCatalogURL());
					}

				} else { // Dataset or File
					firePropertyChangeEvent((InvDataset) e.getNewValue(), e.getPropertyName());
				}

			}
		});

		// htmlViewer Viewer
		htmlViewer = new HtmlBrowser();

		// listen for selection
		htmlViewer.addPropertyChangeListener(new java.beans.PropertyChangeListener() {

			public void propertyChange(java.beans.PropertyChangeEvent e) {
				if (e.getPropertyName().equals("datasetURL")) {
					String datasetURL = (String) e.getNewValue();
					if (debugEvents) {
						System.err.println("***datasetURL= " + datasetURL);
					}
					InvDataset dataset = tree.getSelectedDataset();

					InvAccess access = dataset.findAccess(datasetURL);
					firePropertyChangeEvent(new PropertyChangeEvent(this, "InvAccess", null, access));

				} else if (e.getPropertyName().equals("catrefURL")) {
					String urlString = (String) e.getNewValue();
					if (debugEvents) {
						System.err.println("***catrefURL= " + urlString);
					}
					tree.setCatalog(urlString.trim());
				}
			}
		});

		// splitter
		split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, false, tree, htmlViewer);
		if (prefs != null) {
			split.setDividerLocation(prefs.getInt(HDIVIDER, 400));
		}

		// status label
		JPanel statusPanel = new JPanel(new BorderLayout());
		statusLabel = new JLabel("not connected");
		sourceText = new JLabel();
		statusPanel.add(statusLabel, BorderLayout.WEST);
		statusPanel.add(sourceText, BorderLayout.EAST);

		// button panel
		buttPanel = new JPanel();
		JButton openfileButton = new JButton("Open file");
		// buttPanel.add(openfileButton, null);
		openfileButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent evt) {
				eventType = "File";
				try {
					tree.acceptSelected();
				} catch (Throwable t) {
					t.printStackTrace();
					JOptionPane.showMessageDialog(ASACatalogChooser.this, "ERROR " + t.getMessage());
				} finally {
					eventType = null;
				}
			}
		});

		// JButton acceptButton = new JButton("Open dataset");
		acceptButton = new JButton("Subset & Process");
		// acceptButton.setEnabled(false);
		buttPanel.add(acceptButton, null);
		// ActionListener al = new OpenDatasetListener();
		// acceptButton.addActionListener(al);
		ActionListener al = new ActionListener() {

			public void actionPerformed(ActionEvent evt) {
				eventType = "Dataset";
				try {
					// System.err.println("ASACatChoose: Before acceptSelected(): "
					// + System.currentTimeMillis());
					tree.acceptSelected();
					// System.err.println("ASACatChoose: After acceptSelected(): "
					// + System.currentTimeMillis());
				} catch (Throwable t) {
					t.printStackTrace();
					// JOptionPane.showMessageDialog(ASACatalogChooser.this,
					// "ERROR "+t.getMessage());
				} finally {
					eventType = null;
				}
			}
		};
		ActionListener cursorAl = BusyCursorActions.createListener(odapInterface.getMainFrame(), al);
		acceptButton.addActionListener(cursorAl);

		// put it all together
		setLayout(new BorderLayout());
		if (showComboChooser) {
			add(topPanel, BorderLayout.NORTH);
		}
		add(split, BorderLayout.CENTER);

		if (showOpenButton) {
			JPanel botPanel = new JPanel(new BorderLayout());
			botPanel.add(buttPanel, BorderLayout.NORTH);
			botPanel.add(statusPanel, BorderLayout.SOUTH);
			add(botPanel, BorderLayout.SOUTH);
		}
	}

	private void makeSourceEditWindow() {
		TextGetPutPane sourceEditor = new TextGetPutPane(null);
		IndependentWindow sourceEditorWindow = new IndependentWindow("Source", BAMutil.getImage("thredds"),
			sourceEditor);
		sourceEditorWindow.setBounds(new Rectangle(50, 50, 725, 450));
		sourceEditorWindow.show();
	}

	/**
	 * Set a dataset filter to be used on all catalogs. To turn off, set to
	 * null.
	 * 
	 * @param filter
	 *            DatasetFilter or null
	 */
	public void setDatasetFilter(DatasetFilter filter) {
		tree.setDatasetFilter(filter);
	}

	/**
	 * Save persistent state.
	 */
	public void save() {
		if (catListBox != null) {
			catListBox.save();
		}
		if (daListBox != null) {
			daListBox.save();
		}
		if (sosListBox != null) {
			sosListBox.save();
		}

		if (prefs != null) {
			if (fileChooser != null) {
				fileChooser.save();
			}
			if (catgenFileChooser != null) {
				catgenFileChooser.save();
			}
			// if (sosFileChooser != null) {
			// sosFileChooser.save();
			// }

			prefs.putInt(HDIVIDER, split.getDividerLocation());
		}
	}

	/**
	 * Add a PropertyChangeEvent Listener. Throws a PropertyChangeEvent:
	 * <ul>
	 * <li>propertyName = "Catalog", getNewValue() = catalog URL string
	 * <li>propertyName = "Dataset" or "File", getNewValue() = InvDataset
	 * chosen.
	 * <li>propertyName = "InvAccess" getNewValue() = InvAccess chosen.
	 * <li>propertyName = "catrefURL", getNewValue() = catref URL was chosen.
	 * </ul>
	 */
	public void addPropertyChangeListener(PropertyChangeListener l) {
		listenerList.add(PropertyChangeListener.class, l);
	}

	/**
	 * Remove a PropertyChangeEvent Listener.
	 */
	public void removePropertyChangeListener(PropertyChangeListener l) {
		listenerList.remove(PropertyChangeListener.class, l);
	}

	private void firePropertyChangeEvent(InvDataset ds, String oldPropertyName) {
		String propertyName = (eventType != null) ? eventType : oldPropertyName;
		PropertyChangeEvent event = new PropertyChangeEvent(this, propertyName, null, ds);
		firePropertyChangeEvent(event);
	}

	private void firePropertyChangeEvent(PropertyChangeEvent event) {
		// System.err.println("catchoose firePropertyChangeEvent "+event);

		// Process the listeners last to first
		Object[] listeners = listenerList.getListenerList();
		for (int i = listeners.length - 2; i >= 0; i -= 2) {
			if (listeners[i] == PropertyChangeListener.class) {
				((PropertyChangeListener) listeners[i + 1]).propertyChange(event);
			}
		}
	}

	/**
	 * Add this button to the button panel.
	 * 
	 * @param b
	 *            button to add
	 */
	public void addButton(JButton b) {
		buttPanel.add(b, null);
		buttPanel.revalidate();
	}

	// public void useDQCpopup( boolean use) { tree.useDQCpopup( use); }
	/**
	 * Whether to throw events only if dataset has an Access.
	 * 
	 * @param accessOnly
	 *            if true, throw events only if dataset has an Access
	 */
	public void setAccessOnly(boolean accessOnly) {
		tree.setAccessOnly(accessOnly);
	}

	/**
	 * Whether to throw events if catref URL was chosen catref URL was chosen in
	 * HtmlViewer (default false).
	 */
	public void setCatrefEvents(boolean catrefEvents) {
		this.catrefEvents = catrefEvents;
	}

	/**
	 * Whether to throw events if dataset URL was chosen in HtmlViewer (default
	 * true).
	 */
	public void setDatasetEvents(boolean datasetEvents) {
		this.datasetEvents = datasetEvents;
	}

	/**
	 * Set the factory to create catalogs. If you do not set this, it will use
	 * the default factory.
	 * 
	 * @param factory
	 *            : read XML with this factory
	 * 
	 *            public void setCatalogFactory(InvCatalogFactory factory) {
	 *            tree.setCatalogFactory(factory); }
	 */
	/**
	 * Use this to set the string value in the combo box
	 * 
	 * @param item
	 */
	public void setSelectedItem(String item) {
		if (catListBox != null) {
			catListBox.setSelectedItem(item);
		}
	}

	;

	/**
	 * Set the currently selected InvDataset.
	 * 
	 * @param ds
	 *            select this InvDataset, must be already in the tree.
	 */

	public void setSelectedDataset(InvDatasetImpl ds) {
		tree.setSelectedDataset(ds);
		showDatasetInfo(ds);
	}

	/**
	 * Get the current catalog being shown.
	 * 
	 * @return current catalog, or null.
	 */
	public InvCatalog getCurrentCatalog() {
		return tree.getCatalog();
	}

	/**
	 * Get the TreeView component.
	 */
	public ASACatalogTreeView getTreeView() {
		return tree;
	}

	/**
	 * Get the current URL string. This may be the top catalog, or a catalogRef,
	 * depending on what was last selected. Used to implement the " showSource"
	 * debugging tool.
	 * 
	 * @return
	 */
	public String getCurrentURL() {
		return currentURL;
	}

	private void setCurrentURL(String currentURL) {
		this.currentURL = currentURL;
		sourceText.setText(currentURL);
		statusLabel.setText("Connected...");
	}

	/**
	 * Set the current catalog.
	 */
	public void setCatalog(InvCatalogImpl catalog) {
		tree.setCatalog(catalog);
	}

	/**
	 * Set the current catalog with a string URL. May be of form
	 * catalog#datasetId
	 */
	public void setCatalog(String catalogURL) {
		tree.setCatalog(catalogURL.trim());
	}

	private void showDatasetInfo(InvDatasetImpl ds) {
		if (ds == null) {
			return;
		}
		StringBuilder sbuff = new StringBuilder();
    sbuff.setLength(20000);
		InvDatasetImpl.writeHtmlDescription(sbuff, ds, true, false, datasetEvents, catrefEvents);
		if (showHTML) {
			System.err.println("HTML=\n" + sbuff);
		}
		htmlViewer.setContent(ds.getName(), sbuff.toString());
	}

	private boolean saveDialogChanges;

	private class ListEditorDialog extends JDialog {

		private CheckBoxList cbList = null;

		public ListEditorDialog(JFrame parent, String title, String key) {
			super(parent instanceof JFrame ? (JFrame) parent : null, title, true);
			setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);

			saveDialogChanges = false;

			cbList = new CheckBoxList();
			ArrayList items = null;
			if (prefs != null) {// get the items
				items = (ArrayList) prefs.getList(key, null);
			}
			for (Object o : items) {// add the items to the check list
				cbList.addCheckBox("", String.valueOf(o), false);
			}

			ActionListener cancelListener = new ActionListener() {

				public void actionPerformed(ActionEvent actionEvent) {
					saveDialogChanges = false;

					setVisible(false);
				}
			};
			ActionListener saveListener = new ActionListener() {

				public void actionPerformed(ActionEvent actionEvent) {
					saveDialogChanges = true;

					setVisible(false);
				}
			};

			// add the check list to the dialog
			setLayout(new MigLayout());
			add(new JLabel("Check the items you wish to remove:"), "center, wrap");
			add(cbList, "center");

			JPanel btnPanel = new JPanel(new MigLayout("center"));
			JButton btnOk = new JButton("OK");
			btnOk.addActionListener(saveListener);
			JButton btnCancel = new JButton("Cancel");
			btnCancel.addActionListener(cancelListener);

			btnPanel.add(btnOk);
			btnPanel.add(btnCancel);

			add(btnPanel, "south");
			pack();

			// register "Enter" and "Escape" keys
			KeyStroke cancelStroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
			KeyStroke saveStroke = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0);
			getRootPane().registerKeyboardAction(saveListener, saveStroke, JComponent.WHEN_IN_FOCUSED_WINDOW);
			getRootPane().registerKeyboardAction(cancelListener, cancelStroke, JComponent.WHEN_IN_FOCUSED_WINDOW);

			setLocationRelativeTo(parent);
		}

		public java.util.List<String> getItemsToRemove() {
			return cbList.getSelectedItems();
		}
	}

	/**
	 * Wrap this in a JDialog component.
	 * 
	 * @param parent
	 *            JFrame (application) or JApplet (applet) or null
	 * @param title
	 *            dialog window title
	 * @param modal
	 *            is modal
	 */
	public JDialog makeDialog(RootPaneContainer parent, String title, boolean modal) {
		this.parent = parent;
		return new Dialog(parent, title, modal);
	}

	private class Dialog extends JDialog {

		private Dialog(RootPaneContainer parent, String title, boolean modal) {
			super(parent instanceof Frame ? (Frame) parent : null, title, modal);

			// L&F may change
			UIManager.addPropertyChangeListener(new PropertyChangeListener() {

				public void propertyChange(PropertyChangeEvent e) {
					if (e.getPropertyName().equals("lookAndFeel")) {
						SwingUtilities.updateComponentTreeUI(ASACatalogChooser.Dialog.this);
					}
				}
			});

			// add a dismiss button
			JButton dismissButton = new JButton("Dismiss");
			buttPanel.add(dismissButton, null);

			dismissButton.addActionListener(new ActionListener() {

				public void actionPerformed(ActionEvent evt) {
					setVisible(false);
				}
			});

			// add it to contentPane
			Container cp = getContentPane();
			cp.setLayout(new BorderLayout());
			cp.add(ASACatalogChooser.this, BorderLayout.CENTER);
			pack();
		}
	}

	// class OpenDatasetListener implements ActionListener {
	//
	// public void actionPerformed(ActionEvent e) {
	// ProgressDialog pd = new ProgressDialog(odapInterface.getMainFrame(),
	// "Progress", "Opening Dataset...");
	// pd.addTask(new OpenDataset());
	// pd.run();
	// pd.finished();
	// }
	// }
	//
	// class OpenDADatasetListener implements ActionListener {
	//
	// public void actionPerformed(ActionEvent e) {
	//
	// String dataUrl = "";
	// try{
	// dataUrl = JOptionPane.showInputDialog(odapInterface.getMainFrame(),
	// "Enter a data url:", "Direct Data Access...");
	//
	// if(dataUrl == null){
	// return;
	// }//cancel clicked
	//
	// // setThreddsDatatype(dataUrl);
	//
	// System.err.println("Dataset Url: " + dataUrl);
	// System.err.println("Retrieving data...");
	// //
	// odapInterface.getMainFrame().setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
	//
	// daDataset = NetcdfDataset.openDataset(dataUrl);
	//
	// ProgressDialog pd = new ProgressDialog(odapInterface.getMainFrame(),
	// "Progress", "Opening Dataset...");
	// pd.addTask(new OpenDirectAccessDataset());
	// pd.run();
	// pd.finished();
	//                
	// }catch(IOException ex){
	// JOptionPane.showMessageDialog(odapInterface.getMainFrame(),
	// "Cannot find the file\n\"" + dataUrl + "\"" +
	// "\n\nPlease check the name and try again.");
	// System.err.println("OI:directAccess: Invalid filename");
	// ex.printStackTrace();
	// }finally{
	// // odapInterface.getMainFrame().setCursor(Cursor.getDefaultCursor());
	// }
	// }
	// }
	//
	// class OpenDataset implements Runnable {
	//
	// public void run() {
	// eventType = "Dataset";
	// try{
	// System.err.println("od runnable");
	// tree.acceptSelected();
	// }catch(Throwable t){
	// t.printStackTrace();
	// JOptionPane.showMessageDialog(ASACatalogChooser.this, "ERROR " +
	// t.getMessage());
	// }finally{
	// eventType = null;
	// }
	// }
	// }
	//
	// class OpenDirectAccessDataset implements Runnable {
	// public void run() {
	// System.err.println("odad runnable");
	// odapInterface.openDataset(daDataset);
	// }
	// }
}

/*
 * Change History: $Log: CatalogChooser.java,v $ Revision 1.35 2005/11/18
 * 17:47:41 caron** empty log message *** Revision 1.34 2005/08/08 19:38:59
 * caron minor Revision 1.33 2005/08/05 18:40:22 caron no message Revision 1.32
 * 2005/07/27 23:29:13 caron minor Revision 1.31 2005/07/22 16:19:49 edavis
 * Allow DatasetSource and InvDatasetScan to add dataset size metadata. Revision
 * 1.30 2005/07/11 20:06:17 caron** empty log message *** Revision 1.29
 * 2005/07/08 18:34:59 edavis Fix problem dealing with service URLs that are
 * relative to the catalog (base="") and those that are relative to the
 * collection (base URL is not empty). Revision 1.28 2005/07/01 02:50:12 caron
 * no message Revision 1.27 2005/06/23 20:02:55 caron add "View File" button to
 * thredds dataset chooser Revision 1.26 2005/06/23 19:18:50 caron no message
 * Revision 1.25 2005/05/25 21:09:36 caron no message Revision 1.24 2005/05/04
 * 03:37:05 edavis Remove several unnecessary methods in DirectoryScanner.
 * Revision 1.23 2005/04/29 14:55:56 edavis Fixes for change in
 * InvCatalogFactory.writeXML( cat, filename) method signature. And start on
 * allowing wildcard characters in pathname given to DirectoryScanner. Revision
 * 1.22 2005/04/28 23:15:11 caron catChooser writes catalog to directory
 * Revision 1.21 2005/04/27 22:08:03 caron no message Revision 1.20 2005/01/14
 * 22:44:03 caron** empty log message *** Revision 1.19 2004/12/16 00:32:13
 * caron** empty log message *** Revision 1.18 2004/12/15 00:11:45 caron 2.2.05
 * Revision 1.17 2004/12/14 15:41:01 caron** empty log message *** Revision 1.16
 * 2004/12/07 02:43:19 caron** empty log message *** Revision 1.15 2004/12/01
 * 05:54:23 caron improve FileChooser Revision 1.14 2004/09/30 00:33:36 caron**
 * empty log message *** Revision 1.13 2004/09/28 21:39:09 caron** empty log
 * message *** Revision 1.12 2004/09/24 03:26:30 caron merge nj22 Revision 1.11
 * 2004/06/12 02:01:11 caron dqc 0.3 Revision 1.10 2004/06/09 00:27:28 caron
 * version 2.0a release; cleanup javadoc Revision 1.9 2004/05/11 23:30:32 caron
 * release 2.0a Revision 1.8 2004/03/11 23:35:20 caron minor bugs Revision 1.7
 * 2004/03/05 23:35:48 caron rel 1.3.1 javadoc Revision 1.6 2004/03/05 17:21:50
 * caron 1.3.1 release Revision 1.5 2004/02/20 00:49:53 caron 1.3 changes
 * Revision 1.4 2003/12/04 22:27:45 caron** empty log message *** Revision 1.3
 * 2003/05/29 22:59:48 john refactor choosers into toolkit framework Revision
 * 1.2 2003/03/17 20:09:33 john improve catalog chooser, use ucar.unidata.geoloc
 * Revision 1.1 2003/01/31 22:06:14 john ThreddsDatasetChooser standalone
 */
