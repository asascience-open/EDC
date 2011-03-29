/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * AboutDialog.java
 *
 * Created on Sep 11, 2008, 10:11:37 AM
 *
 */
package com.asascience.ui;

import java.awt.Color;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.Method;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import net.miginfocom.swing.MigLayout;

//import org.jdesktop.jdic.desktop.Desktop;
//import org.jdesktop.jdic.desktop.DesktopException;
//import org.jdesktop.jdic.desktop.Message;
/**
 * 
 * @author cmueller_mac
 */
public class AboutDialog extends JDialog implements ActionListener {

	private static Image image;
	private static String appName;
	private static String version;
	private static String email;
	private static String webAddress;

	public AboutDialog(String appName, String version, String imageLoc, String email, String webAddress) {
		this(appName, version, new ImageIcon(imageLoc).getImage(), email, webAddress);
	}

	public AboutDialog(String appName, String version, Image image, String email, String webAddress) {
		if (appName != null) {
			this.setTitle("About " + appName);
		}
		this.setLayout(new MigLayout("fill"));
		this.setLocationByPlatform(true);
		this.setResizable(false);
		this.setModal(true);
		this.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

		AboutDialog.appName = appName;
		AboutDialog.version = version;
		AboutDialog.image = image;
		AboutDialog.email = email;
		AboutDialog.webAddress = webAddress;

		initComponents();
		this.setVisible(true);
	}

	private void initComponents() {
		/** Add the image */
		if (image != null) {
			ImagePanel pnlImage = new ImagePanel(image);
			this.add(pnlImage, "wrap");
		}

		/** Add the version label */
		this.add(new JLabel("Version: " + version), "center, wrap");

		/** Add the website and email labels */
		this.add(new JLabel("More Information: "), "center, split 2");
		this.add(new JActionLabel(webAddress, this, "website", Color.BLUE, Color.RED), "wrap");
		this.add(new JLabel("Support: "), "center, split 2");
		this.add(new JActionLabel(email, this, "email", Color.BLUE, Color.RED), "wrap");

		this.add(buttonPanel(), "center");

		this.pack();
	}

	private JPanel buttonPanel() {
		JPanel finishPanel = new JPanel(new MigLayout("center"));
		JButton btnClose = new JButton("Close");
		btnClose.setActionCommand("close");
		btnClose.addActionListener(this);

		finishPanel.add(btnClose);

		this.getRootPane().setDefaultButton(btnClose);

		return finishPanel;
	}

	public void actionPerformed(ActionEvent e) {
		String cmd = e.getActionCommand();
		if (cmd.equals("close")) {
			this.setVisible(false);
			this.dispose();
		} else if (cmd.equals("website")) {
			openURL();
			/** When JDK 1.6 is available... */
			// try{
			// Desktop.browse(new URL("http://" + webAddress));
			// }catch(MalformedURLException ex){
			// Logger.getLogger(AboutDialog.class.getName()).
			// log(Level.SEVERE, null, ex);
			// }catch(DesktopException ex){
			// Logger.getLogger(AboutDialog.class.getName()).
			// log(Level.SEVERE, null, ex);
			// }
		} else if (cmd.equals("email")) {
			openEmail();
			/** When JDK 1.6 is available... */
			// try{
			// Message m = new Message();
			// m.setToAddrs(Arrays.asList(new String[]{email}));
			// m.setSubject("Support for " + appName);
			// Desktop.mail(m);
			// }catch(DesktopException ex){
			// Logger.getLogger(AboutDialog.class.getName()).
			// log(Level.SEVERE, null, ex);
			// }
		}

	}

	public static void openEmail() {
		try {
			String mailTo = "mailto:" + email + "?subject=" + appName + "%20Support";
			String osName = System.getProperty("os.name");
			if (osName.startsWith("Mac OS")) {
				Runtime.getRuntime().exec("open " + mailTo);
			} else if (osName.startsWith("Windows")) {
				Runtime.getRuntime().exec("rundll32 url.dll,FileProtocolHandler " + mailTo);
				// }else{ //assume Unix or Linux
				//                
				// String[] browsers = {"firefox", "opera", "konqueror",
				// "epiphany", "mozilla", "netscape"};
				// String browser = null;
				// for(int count = 0; count < browsers.length && browser ==
				// null; count++){
				// if(Runtime.getRuntime().exec(new String[]{"which",
				// browsers[count]}).
				// waitFor() == 0){
				// browser = browsers[count];
				// }
				// }
				// if(browser == null){
				// throw new Exception("Could not find web browser");
				// }else{
				// Runtime.getRuntime().exec(new String[]{browser, url});
				// }
			}
		} catch (Exception e) {
			JOptionPane.showMessageDialog(null, "Error attempting to launch web browser" + ":\n"
				+ e.getLocalizedMessage());
		}
	}

	public static void openURL() {
		try {
			String url = (webAddress.startsWith("http://")) ? webAddress : "http://" + webAddress;
			String osName = System.getProperty("os.name");
			if (osName.startsWith("Mac OS")) {
				Class fileMgr = Class.forName("com.apple.eio.FileManager");
				Method openURL = fileMgr.getDeclaredMethod("openURL", new Class[] { String.class });
				openURL.invoke(null, new Object[] { url });
			} else if (osName.startsWith("Windows")) {
				Runtime.getRuntime().exec("rundll32 url.dll,FileProtocolHandler " + url);
			} else { // assume Unix or Linux
				String[] browsers = { "firefox", "opera", "konqueror", "epiphany", "mozilla", "netscape" };
				String browser = null;
				for (int count = 0; count < browsers.length && browser == null; count++) {
					if (Runtime.getRuntime().exec(new String[] { "which", browsers[count] }).waitFor() == 0) {
						browser = browsers[count];
					}
				}
				if (browser == null) {
					throw new Exception("Could not find web browser");
				} else {
					Runtime.getRuntime().exec(new String[] { browser, url });
				}
			}
		} catch (Exception e) {
			JOptionPane.showMessageDialog(null, "Error attempting to launch web browser" + ":\n"
				+ e.getLocalizedMessage());
		}
	}

	public static void main(String[] args) {
		// TODO code application logic here
		javax.swing.SwingUtilities.invokeLater(new Runnable() {

			public void run() {
				new AboutDialog("TEST", "1.0.0", "/Users/cmueller_mac/Desktop/ASA_Graphics/SHARC_splash.png",
					"cmueller@asascience.com", "www.asascience.com");
			}
		});
	}
}
