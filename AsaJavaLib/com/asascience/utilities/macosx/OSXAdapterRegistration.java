/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * OSXAdapterRegistration.java
 *
 * Created on Jun 2, 2008, 9:33:38 AM
 *
 */

package com.asascience.utilities.macosx;

/**
 * 
 * @author cmueller_mac
 */
public interface OSXAdapterRegistration {

	public boolean registerForMacOSXEvents();

	// {
	// try {
	// // Generate and register the OSXAdapter, passing it a hash of all the
	// methods we wish to
	// // use as delegates for various com.apple.eawt.ApplicationListener
	// methods
	// OSXAdapter.setQuitHandler(this, getClass().getDeclaredMethod("osxQuit",
	// (Class[])null));
	// OSXAdapter.setAboutHandler(this, getClass().getDeclaredMethod("about",
	// (Class[])null));
	// OSXAdapter.setPreferencesHandler(this,
	// getClass().getDeclaredMethod("preferences", (Class[])null));
	// OSXAdapter.setFileHandler(this,
	// getClass().getDeclaredMethod("loadImageFile", new Class[] { String.class
	// }));
	//                
	// return true;
	// } catch (Exception e) {
	// System.err.println("Error while loading the OSXAdapter:");
	// e.printStackTrace();
	// }
	// return false;
	// }

	// General info dialog; fed to the OSXAdapter as the method to call when
	// "About OSXAdapter" is selected from the application menu
	// public void about();//
	// {
	// aboutBox.setLocation((int)this.getLocation().getX() + 22,
	// (int)this.getLocation().getY() + 22);
	// aboutBox.setVisible(true);
	// }

	// General preferences dialog; fed to the OSXAdapter as the method to call
	// when
	// "Preferences..." is selected from the application menu
	// public void preferences();//
	// {
	// prefs.setLocation((int)this.getLocation().getX() + 22,
	// (int)this.getLocation().getY() + 22);
	// prefs.setVisible(true);
	// }

	// General osxQuit handler; fed to the OSXAdapter as the method to call when
	// a system osxQuit event occurs
	// A osxQuit event is triggered by Cmd-Q, selecting Quit from the
	// application or Dock menu, or logging out
	public boolean osxQuit();//
	// {
	// int option = JOptionPane.showConfirmDialog(this,
	// "Are you sure you want to osxQuit?", "Quit?", JOptionPane.YES_NO_OPTION);
	// return (option == JOptionPane.YES_OPTION);
	// }
}
