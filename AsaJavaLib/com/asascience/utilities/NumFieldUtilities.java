/*
 * NumFieldUtilities.java
 *
 * Created on December 11, 2007, 10:01 AM
 *
 * Applied Science Associates, Inc.
 * Copyright 2007.  All rights reserved.
 */
package com.asascience.utilities;

import java.awt.Component;
import java.awt.Container;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.SwingUtilities;

import org.softsmithy.lib.swing.AbstractNumberField;
import org.softsmithy.lib.swing.JDoubleField;
import org.softsmithy.lib.swing.text.AbstractXNumberFormatterFactory;
import org.softsmithy.lib.swing.text.DoubleFormatter;
import org.softsmithy.lib.swing.text.DoubleFormatterFactory;

/**
 * 
 * @author CBM
 */
public class NumFieldUtilities implements FocusListener {

	public static DecimalFormat getZeroDecimalFormat() {
		return new DecimalFormat("0.0000");
	}

	public static DecimalFormat getEditingDecimalFormat() {
		return new DecimalFormat("#,###,##0.000000");
	}

	public static DecimalFormat getBigEditingDecimalFormat() {
		return new DecimalFormat("#,###,##0.000000000000000");
	}

	public static DecimalFormat getFloatDecimalFormat() {
		return new DecimalFormat("#,###,##0.000000000");
	}

	public static DecimalFormat get4PlacesDecimalFormat() {
		return new DecimalFormat("#,###,##0.0000");
	}

	public static DecimalFormat get2PlacesDecimalFormat() {
		return new DecimalFormat("#,###,##0.00");
	}

	public static DecimalFormat getSciNoteDecimalFormat() {
		return new DecimalFormat("0.###E0");
	}

	public static DoubleFormatterFactory makeDffZero() {
		return new DoubleFormatterFactory(new DoubleFormatter(getZeroDecimalFormat()));
	}

	public static DoubleFormatterFactory makeDffEditing() {
		return new DoubleFormatterFactory(new DoubleFormatter(getBigEditingDecimalFormat()));
	}

	public static DoubleFormatterFactory makeDffFloatPlaces() {
		return new DoubleFormatterFactory(new DoubleFormatter(getFloatDecimalFormat()));
	}

	public static DoubleFormatterFactory makeDff4Places() {
		return new DoubleFormatterFactory(new DoubleFormatter(get4PlacesDecimalFormat()));
	}

	public static DoubleFormatterFactory makeDff2Places() {
		return new DoubleFormatterFactory(new DoubleFormatter(get2PlacesDecimalFormat()));
	}

	public static DoubleFormatterFactory makeDffSciNote() {
		return new DoubleFormatterFactory(new DoubleFormatter(getSciNoteDecimalFormat()));
	}

	/**
	 * Creates a new instance of NumFieldUtilities
	 */
	public NumFieldUtilities() {
	}

	public static void changeFormatterFactory(AbstractNumberField anf, AbstractXNumberFormatterFactory axnff) {
		Number min = anf.getAbstractXNumberFormatter().getMinimumNumberValue();
		Number max = anf.getAbstractXNumberFormatter().getMaximumNumberValue();

		anf.setAbstractXNumberFormatterFactory(axnff);

		anf.getAbstractXNumberFormatter().setMinimumNumberValue(min);
		anf.getAbstractXNumberFormatter().setMaximumNumberValue(max);
	}

	public static void formatDoubleField(JDoubleField jdf) {
		if (Math.abs(jdf.getDoubleValue()) == 0.0) {
			jdf.setDoubleValue(0.0);
			// jdf.setDoubleFormatterFactory(makeDffZero());
			changeFormatterFactory(jdf, makeDffZero());
		} else if (Math.abs(jdf.getDoubleValue()) <= 0.005) {
			// jdf.setDoubleFormatterFactory(makeDffSciNote());
			changeFormatterFactory(jdf, makeDffSciNote());
		} else {
			// jdf.setDoubleFormatterFactory(makeDff4Places());
			changeFormatterFactory(jdf, makeDff4Places());
		}
	}

	public static void setDoubleField(double val, JDoubleField jdf) {
		jdf.setDoubleValue(val);
		formatDoubleField(jdf);
	}

	public static void updateNumFieldValues(Component comp) {
		try {
			// System.err.println(comp.toString());
			if (comp instanceof AbstractNumberField) {
				AbstractNumberField anf = (AbstractNumberField) comp;
				anf.commitEdit();
			}
			if (comp instanceof Container) {
				Component[] comps = ((Container) comp).getComponents();
				for (int i = 0; i < comps.length; i++) {
					updateNumFieldValues(comps[i]);
				}
			}
		} catch (ParseException ex) {
			ex.printStackTrace();
		}
	}

	public void focusGained(FocusEvent e) {
		Object o = e.getSource();
		if (o instanceof AbstractNumberField) {
			final AbstractNumberField af = (AbstractNumberField) o;
			if (af instanceof JDoubleField) {
				// ((JDoubleField)af).setDoubleFormatterFactory(makeDffEditing());
				changeFormatterFactory(af, makeDffEditing());
			}
			SwingUtilities.invokeLater(new Runnable() {

				public void run() {
					af.selectAll();
				}
			});
		}
		// if(o instanceof JDoubleField){
		// final JDoubleField f = (JDoubleField) o;
		// f.setDoubleFormatterFactory(makeDffEditing());
		// SwingUtilities.invokeLater(new Runnable(){
		// public void run(){
		// f.selectAll();
		// }
		// });
		// // f.selectAll();//TODO: Make this work!!
		// }
	}

	public void focusLost(FocusEvent e) {
		Object o = e.getSource();
		if (o instanceof AbstractNumberField) {
			try {
				AbstractNumberField af = (AbstractNumberField) o;
				af.commitEdit();
				if (o instanceof JDoubleField) {
					formatDoubleField((JDoubleField) o);
				}
			} catch (ParseException ex) {
				Logger.getLogger(NumFieldUtilities.class.getName()).log(Level.SEVERE, null, ex);
			}
		}
		// if(o instanceof JDoubleField){
		// JDoubleField f = (JDoubleField) o;
		// try {
		// f.commitEdit();//commit the user-entered value
		// formatDoubleField(f);
		// } catch (ParseException ex) {
		// ex.printStackTrace();
		// }formatDoubleField(f);
		// }
	}

	public SimpleFocusListener getSimpleFocusListener() {
		return new SimpleFocusListener();
	}

	public class SimpleFocusListener implements FocusListener {

		public SimpleFocusListener() {
		}

		public void focusGained(FocusEvent e) {
			Object o = e.getSource();
			if (o instanceof AbstractNumberField) {
				final AbstractNumberField af = (AbstractNumberField) o;
				SwingUtilities.invokeLater(new Runnable() {

					public void run() {
						af.selectAll();
					}
				});
			}
		}

		public void focusLost(FocusEvent e) {
			Object o = e.getSource();
			if (o instanceof AbstractNumberField) {
				try {
					AbstractNumberField af = (AbstractNumberField) o;
					af.commitEdit();
				} catch (ParseException ex) {
					// Logger.getLogger(NumFieldUtilities.class.getName()).
					// log(Level.SEVERE, null, ex);
				}
			}
		}
	}
}
