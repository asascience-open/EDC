// $Id: ASACatalogFactoryCancellable.java 50 2006-07-12 16:30:06Z caron $
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

package com.asascience.edc.ui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;

import thredds.catalog.CatalogSetCallback;
import thredds.catalog.InvCatalogFactory;
import thredds.catalog.InvCatalogImpl;
import ucar.nc2.ui.widget.ProgressMonitorTask;
import ucar.nc2.ui.widget.ProgressMonitor;

/**
 * A subclass of InvCatalogFactory that allows the reading of a catalog to be
 * cancelled by the user. Pops up a ProgressMonitor widget.
 * 
 * @author John Caron
 * @version $Id: CASACatalogFactoryCancellablejava 50 2006-07-12 16:30:06Z caron
 *          $
 */

public class ASACatalogFactoryCancellable extends InvCatalogFactory {
	private java.awt.Component parent;
	private boolean callbackDone = true, taskDone = true;
	private boolean debug = false;

	/**
	 * Constructor.
	 * 
	 * @param parent
	 *            : put ProgressMonotpr on top of his component; may be null.
	 * @param name
	 *            : name of the InvCatalogFactory
	 * @param validate
	 *            : should CML validation be done?
	 * 
	 * @see thredds.catalog.InvCatalogFactory
	 * @see thredds.ui.ProgressMonitor
	 */
	public ASACatalogFactoryCancellable(java.awt.Component parent, String name, boolean validate) {
		super(name, validate);
		this.parent = parent;
	}

	/**
	 * Pops up a ProgressMonitor to allow user cancellation while reading the
	 * named catalog. This method immediately returns, and the reading is done
	 * on a background thread. If successfully read, callback.setCatalog() is
	 * called on the awt event thread. If failure, the user will be given a
	 * popup error message, and callback.failure() is called..
	 * 
	 * @param catalogName
	 *            : the URI name that the XML doc is at.
	 * @param callbacker
	 *            : this will be called (from AWT thread) if catalog was
	 *            successfully called.
	 */
	public void readXMLasynch(String catalogName, CatalogSetCallback callbacker) {
		this.callback = callbacker;
		callbackDone = false;
		taskDone = false;

		openTask = new OpenCatalogTask(catalogName);

		ProgressMonitor pm = new ProgressMonitor(openTask, 10, 10);
		pm.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (debug)
					System.err.println("ProgressMonitor event  " + e.getActionCommand());
				if (e.getActionCommand().equals("success")) {
					checkFailure();
				} else
					callback.failed();
				callbackDone = true;
			}
		});
		pm.start(parent, "Open catalog " + catalogName, 20);
	}

	private void checkFailure() {
		StringBuilder buff = new StringBuilder();
		openTask.catalog.check(buff);

		if (openTask.catalog.hasFatalError()) {
			String catalogName = openTask.catalog.getName();
			javax.swing.JOptionPane.showMessageDialog(null, "Catalog Read Failed on " + catalogName + "\n"
				+ buff.toString());
			callback.failed();
			return;
		}

		callback.setCatalog(openTask.catalog);
	}

	/**
	 * See if this object can be reused.
	 * 
	 * @return true if not compled last task.
	 */
	public boolean isBusy() {
		return !taskDone || !callbackDone;
	}

	private OpenCatalogTask openTask;
	private CatalogSetCallback callback;

	private class OpenCatalogTask extends ProgressMonitorTask {
		String catalogName;
		URI catalogURI;
		InvCatalogImpl catalog;

		OpenCatalogTask(String catalogName) {
			this.catalogName = catalogName;
		}

		public void run() {

			try {
				catalogURI = new URI(catalogName);
			} catch (URISyntaxException e) {
				catalog = new InvCatalogImpl(catalogName, null, null);
				catalog.appendErrorMessage("**Fatal:  InvCatalogFactory.readXML URISyntaxException on URL ("
					+ catalogName + ") " + e.getMessage() + "\n", true);
				success = false;
				done = true;
				taskDone = true;
				return;
			}
			if (debug)
				System.err.println("CatalogFactoryCancellable run task on " + catalogName);

			if (catalogURI.getScheme().equals("file")) {
				catalog = ASACatalogFactoryCancellable.super.readXML(catalogURI);
				success = !cancel;
				done = true;
				taskDone = true;
				return;
			}

			GetMethod m = null;
			try {
				m = new GetMethod(catalogName);
				m.setFollowRedirects(true);

				HttpClient client = new HttpClient();
				client.executeMethod(m);
				InputStream stream = m.getResponseBodyAsStream();
				catalog = ASACatalogFactoryCancellable.super.readXML(stream, catalogURI);

			} catch (IOException e) {
				catalog = new InvCatalogImpl(catalogName, null, null);
				catalog.appendErrorMessage("**Fatal:  InvCatalogFactory.readXML IOException on URL (" + catalogName
					+ ") " + e.getMessage() + "\n", true);
				success = false;
				done = true;
				taskDone = true;
				return;

			} finally {
				if (null != m)
					m.releaseConnection();
			}

			success = !cancel;
			done = true;
			taskDone = true;
		}

		/*
		 * old way public void run() { if (debug)
		 * System.err.println("ASACatalogFactoryCancellable run task on "
		 * +catalogName); catalog = ASACatalogFactoryCancellable.super.readXML(
		 * catalogName); success = !cancel; done = true; taskDone = true; }
		 */

	}

}