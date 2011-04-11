// $Id: XMLStore.java,v 1.9 2005/08/22 17:13:58 caron Exp $
/*
 * Copyright 1997-2004 Unidata Program Center/University Corporation for
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
package com.asascience.utilities;

import java.beans.ExceptionListener;
import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.Stack;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
 * This implements an XML-based backing store for XMLPrefs. XMLStores can be
 * chained together to provide independent collections of "stored defaults", eg
 * store1 -> store2 -> store3. In this case, a call to getXXX( key) first looks
 * in store1, and if not found, then in store2, etc. A call to putXXX(key,
 * value) first checks to see if the (key, value) pair already exists in any of
 * the chained stores; if not, then it is added to store1. Normally, only store1
 * would be changed and saved.
 * 
 * <p>
 * A chain of stored defaults might look like:
 * 
 * <pre>
 * try {
 * 	XMLStore store3 = XMLStore.createFromResource(&quot;/auxdata/system.xml&quot;, null);
 * 	XMLStore store2 = XMLStore.createFromFile(&quot;/usr/local/metapps/GDV/site.xml&quot;, store3);
 * 	XMLStore store1 = XMLStore.createFromFile(&quot;/home/username/.GDV/user.xml&quot;, store2);
 * 	XMLPrefs prefs = store1.getPreferences();
 * } catch (IOException e) {
 * 	System.err.println(&quot;XMLStore Creation failed &quot; + e);
 * }
 * </pre>
 * 
 * If you plan to call Preferences.userRoot(), you must explicilty set it, eg:
 * 
 * <pre>
 * XMLPrefs.setUserRoot(prefs);
 * </pre>
 * 
 * <p>
 * Before exiting the application, in order to save changes, you must call:
 * 
 * <pre>
 * try {
 * 	store1.save();
 * } catch (IOException e) {
 * 	System.err.println(&quot;XMLStore Save failed &quot; + e);
 * }
 * </pre>
 * 
 * 
 * @see ucar.util.prefs.XMLPrefs
 * @see java.util.prefs.Preferences
 * @author John Caron
 * @version $Id: XMLStore.java,v 1.9 2005/08/22 17:13:58 caron Exp $
 */
public class XMLStorage {

  public static final String AOI_LIST = "aoi_rectangles";
  public static final String FRAME_SIZE = "frameSize";

  /**
   * Create an XMLStore reading from the specified filename.
   *
   * @param fileName
   *            The XMLStore is stored in this files.
   * @param storedDefaults
   *            This contains the "stored defaults", or null if none.
   */
  static public XMLStorage createFromFile(String fileName, XMLStorage storedDefaults) throws java.io.IOException {
    File prefsFile = new File(fileName);

    // open file if it exists
    InputStream primIS = null, objIS = null;
    if (prefsFile.exists()) {
      primIS = new BufferedInputStream(new FileInputStream(prefsFile));
      objIS = new BufferedInputStream(new FileInputStream(prefsFile));
    }

    if (debugWhichStore) {
      System.err.println("XMLStore read from file " + fileName);
    }
    XMLStorage store = new XMLStorage(primIS, objIS, storedDefaults);
    store.prefsFile = prefsFile;
    return store;
  }

  /**
   * Create an XMLStore reading from an input stream. Because of some
   * peculiariteis, you must open the input stream wtice, and pass both in.
   *
   * @param is1
   *            the first copy of the input stream.
   * @param is2
   *            the second copy of the input stream.
   * @param storedDefaults
   *            This contains the "stored defaults", or null if none.
   */
  static public XMLStorage createFromInputStream(InputStream is1, InputStream is2, XMLStorage storedDefaults)
          throws java.io.IOException {
    if (debugWhichStore) {
      System.err.println("XMLStore read from input stream " + is1);
    }
    XMLStorage store = new XMLStorage(is1, is2, storedDefaults);
    return store;
  }

  /**
   * Create a read-only XMLStore reading from the specified resource, opened
   * as a Resource stream using the XMLStore ClassLoader. This allows you to
   * find files that are in jar files on the application CLASSPATH.
   *
   * @param resourceName
   *            The XMLStore is stored in this resource. By convention it has
   *            .xml suffix.
   * @param storedDefaults
   *            This contains the "stored defaults", or null if none.
   * @throws java.io.IOException
   *             is Resource not found or error reading it
   */
  static public XMLStorage createFromResource(String resourceName, XMLStorage storedDefaults)
          throws java.io.IOException {

    // open files if exist
    Class c = XMLStorage.class;
    InputStream primIS = c.getResourceAsStream(resourceName);
    InputStream objIS = c.getResourceAsStream(resourceName);

    // debug
    // InputStream debugIS = c.getResourceAsStream(fileName);
    // System.err.println("Resource stream= "+fileName);
    // thredds.util.IO.copy(debugIS, System.err);

    if (primIS == null) {
      // System.err.println("classLoader="+new
      // XMLStore().getClass().getClassLoader());
      throw new java.io.IOException("XMLStore.createFromResource cant find <" + resourceName + ">");
    }

    if (debugWhichStore) {
      System.err.println("XMLStore read from resource " + resourceName);
    }
    return new XMLStorage(primIS, objIS, storedDefaults);
  }
  private static boolean debugRead = false, debugReadValues = false, debugWhichStore = false;
  private static boolean debugWriteNested = false, debugWriteBean = false;
  private File prefsFile = null;
  private XMLPrefs rootPrefs = new XMLPrefs(null, ""); // root node
  private boolean showDecoderExceptions = false; // debugging

  public XMLStorage() {
  }

  /**
   * Constructor. Needs two copies of the same input stream, one for our
   * parser and one for XMLDecoder.
   *
   * @param primIS
   *            : store input stream. may be null.
   * @param objIS
   *            : store input stream. may be null only if primIS is null.
   * @param storedDefaults
   *            : chain to this one.
   * @throws IOException
   */
  private XMLStorage(InputStream primIS, InputStream objIS, XMLStorage storedDefaults) throws java.io.IOException {

    // read file if it exists
    if (null != primIS) {

      // get a SAX parser from JAXP layer
      SAXParserFactory factory = SAXParserFactory.newInstance();
      try {
        SAXParser saxParser = factory.newSAXParser();
        MySaxHandler handler = new MySaxHandler(objIS);

        // the work is done here
        saxParser.parse(primIS, handler);

      } catch (ParserConfigurationException e) {
        e.printStackTrace();

      } catch (SAXException se) {
        System.err.println("SAXException = " + se.getMessage());
        se.printStackTrace();

        Exception see = se.getException();
        if (see != null) {
          System.err.println("from = " + see.getMessage());
          see.printStackTrace();
        }

      } catch (IOException ioe) {
        ioe.printStackTrace();
      }

      primIS.close();
      objIS.close();
    }

    // chain
    if (storedDefaults != null) {
      rootPrefs.setStoredDefaults(storedDefaults.getPreferences());
    }
  }

  private XMLDecoder openBeanDecoder(InputStream objIS) {
    // filter stream for XMLDecoder
    try {
      InputMunger im = new InputMunger(objIS);
      XMLDecoder beanDecoder = new XMLDecoder(im, null, new ExceptionListener() {

        public void exceptionThrown(Exception e) {
          if (showDecoderExceptions) {
            System.err.println("***XMLStore.read() got Exception= " + e.getClass().getName() + " "
                    + e.getMessage());
          }
          // e.printStackTrace();
        }
      });

      // System.err.println("openBeanDecoder at "+objIS);
      return beanDecoder;

    } catch (IOException ioe) {
      ioe.printStackTrace();
      return null;
    }

  }

  static public String makeStandardFilename(String storeName) {
    File f = new File(System.getProperty("user.dir") + File.separator + storeName);

    return f.getAbsolutePath();
  }

  /**
   * Convenience routine for creating an XMLStore file in a standard place.
   *
   * <p>
   * Initialize:
   * <ol>
   * <li>$user.home = System.getProperty("user.home"), if not exist, use "."
   * <li>create directory "$(user_home)/appName/" , if not exist, create it
   * <li>return "$(user_home)/appName/storeName" for use in createFromFile()
   * </ol>
   */
  static public String makeStandardFilename(String appName, String storeName) {
    // the directory
    String userHome = null;
    try {
      userHome = System.getProperty("user.dir");
    } catch (Exception e) {
      System.err.println("XMLStore.makeStandardFilename: error System.getProperty(user.home) " + e);
    }
    if (null == userHome) {
      userHome = ".";
    }

    String dirFilename = userHome + "/" + appName;
    File f = new File(dirFilename);
    if (!f.exists()) {
      f.mkdirs(); // now ready for file creation in writeXML
    }
    return dirFilename + "/" + storeName;
  }

  /**
   * Open/Create a read-only XMLStore from the specified InputStream.
   *
   * @param InputStream
   *            inStream: read the XML from this InputStream.
   * @param XMLStore
   *            chain: This contains the "stored defaults", or null if none.
   *
   *            public XMLStore(InputStream prefsIS, XMLStore storedDefaults)
   *            {
   *
   *            // read in values readXmlInput( new
   *            BufferedInputStream(inStream), rootPrefs);
   *
   *            // chain if (storedDefaults != null)
   *            rootPrefs.setStoredDefaults( storedDefaults.getPreferences());
   *            }
   */
  /**
   * Get the root Preferences node. All manipulation is done through it.
   */
  public XMLPrefs getPreferences() {
    return rootPrefs;
  }

  // ///////////////////////////////////////////////
  // reading
  // SAX callback handler
  private class MySaxHandler extends org.xml.sax.helpers.DefaultHandler {

    private boolean debug = false, debugDetail = false;
    private InputMunger im;
    private InputStream objIS;
    private XMLDecoder beanDecoder = null; // defer reading beans in case
    // there arent any

    MySaxHandler(InputStream objIS) throws IOException {
      super();
      this.objIS = objIS;
    }

    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
      if (qName.equalsIgnoreCase("root")) {
        startRoot(attributes);
      } else if (qName.equalsIgnoreCase("map")) {
        startMap(attributes);
      } else if (qName.equalsIgnoreCase("node")) {
        startNode(attributes);
      } else if (qName.equalsIgnoreCase("entry")) {
        startEntry(attributes);
      } else if (qName.equalsIgnoreCase("bean")) {
        startBean(attributes);
      } else if (qName.equalsIgnoreCase("beanCollection")) {
        startBeanCollection(attributes);
      } else if (qName.equalsIgnoreCase("beanObject")) {
        startBeanObject(attributes);
      } else if (debugDetail) {
        System.err.println(" unprocessed startElement = " + qName);
      }
    }

    public void endElement(String uri, String localName, String qName) throws SAXException {
      if (qName.equalsIgnoreCase("node")) {
        endNode();
      }
      if (qName.equalsIgnoreCase("beanCollection")) {
        endBeanCollection();
      } else if (debugDetail) {
        System.err.println(" unprocessed endElement = " + qName);
      }
    }
    private Bean.Collection currentBeanCollection = null;
    private Stack stack;
    private XMLPrefs current;

    private void startRoot(Attributes attributes) {
      if (debugDetail) {
        System.err.println(" root ");
      }
      stack = new Stack();
      current = rootPrefs;
    }

    private void startMap(Attributes attributes) {
      if (debugDetail) {
        System.err.println(" map ");
      }
    }

    private void startNode(Attributes attributes) {
      String name = attributes.getValue("name");
      if (debug) {
        System.err.println(" node = " + name);
      }
      if (name.equals("dumpPane") && debug) {
        System.err.println("");
      }
      stack.push(current);
      current = (XMLPrefs) current.node(name); // create new node
    }

    private void startEntry(Attributes attributes) {
      String key = attributes.getValue("key");
      String values = attributes.getValue("value"); // values or value ??
      if (debug) {
        System.err.println(" entry = " + key + " " + values);
      }
      current.put(key, values);
    }

    private void startBean(Attributes attributes) {
      String key = attributes.getValue("key");
      try {
        if (currentBeanCollection != null) {
          Object value = currentBeanCollection.readProperties(attributes);
          if (debug) {
            System.err.println(" bean(collection) = " + key + " value= " + value);
          }
        } else {
          Object value = new Bean(attributes);
          if (debug) {
            System.err.println(" bean = " + key + " value= " + value);
          }
          current.putObject(key, value);
        }
      } catch (Exception e) {
        e.printStackTrace();
      } // ??
    }

    private void startBeanCollection(Attributes attributes) {
      String key = attributes.getValue("key");
      try {
        currentBeanCollection = new Bean.Collection(attributes);
        if (debug) {
          System.err.println(" beanCollection = " + key);
        }
        current.putObject(key, currentBeanCollection);
      } catch (Exception e) {
      } // ??
    }

    private void startBeanObject(Attributes attributes) {
      String key = attributes.getValue("key");
      if (beanDecoder == null) {
        beanDecoder = openBeanDecoder(objIS);
      }
      try {
        if (debug) {
          System.err.print(" beanObject = " + key + " ");
        }
        Object value = beanDecoder.readObject(); // read from filtered
        // stream
        if (debug) {
          System.err.println(" value= " + value);
        }
        current.putObject(key, value);
      } catch (Exception e) {
        System.err.println("#ERROR beanDecoder; beanObject key = " + key);
        e.printStackTrace();
      }
    }

    private void endBeanCollection() {
      currentBeanCollection = null;
    }

    private void endNode() {
      current = (XMLPrefs) stack.pop();
    }
  }

  /*
   * Filter out the prefs stuff, add the header and trailer. this is needed to
   * present to XMLDecoder a clean IOstream. rather lame, XMLDecoder should be
   * modified to take a Filter or something.
   */
  private static final int BUFF_SIZE = 1024;
  private static final String prologue = "<?xml version='1.0' encoding='UTF-8'?>\n";
  private static final String header = "<?xml version='1.0' encoding='UTF-8'?>\n<java version='1.4.1_01' class='java.beans.XMLDecoder'>\n";
  private static final String trailer = "</java>\n";

  class InputMunger extends java.io.BufferedInputStream { // java.io.FilterInputStream
    // {
    // buffer

    private byte buf[];
    private int count = 0;
    private int pos = 0;
    // insert header
    boolean isHeader = true;
    int countHeader = 0;
    int sizeHeader = header.length();
    // insert trailer
    boolean isTrailer = false;
    int countTrailer = 0;
    int sizeTrailer = trailer.length();

    InputMunger(InputStream in) throws IOException {
      this(in, BUFF_SIZE);
    }

    InputMunger(InputStream in, int size) throws IOException {
      super(in);
      buf = new byte[size];
      fill(0);
      pos = prologue.length(); // skip the prologue, its in the header
    }

    public int read(byte[] b, int off, int len) throws IOException {
      for (int i = 0; i < len; i++) {
        int bval = read();
        if (bval < 0) {
          return i > 0 ? i : -1;
        }
        b[off + i] = (byte) bval;
      }
      return len;
    }

    public int read() throws IOException {
      if (isHeader) {
        isHeader = countHeader + 1 < sizeHeader;
        return (int) header.charAt(countHeader++);
      } else if (isTrailer) {
        return (countTrailer < sizeTrailer) ? (int) trailer.charAt(countTrailer++) : -1;
      } else {
        return read1();
      }
    }

    // read 1 byte from buffer
    private int read1() throws IOException {
      if (pos >= count) { // need a new buffer
        fill(0);
        if (pos >= count) {
          isTrailer = true; // switch to trailer
          return read();
        }
      }
      return buf[pos++] & 0xff;
    }

    // fill buffer from underlying stream, saving the last "save" chars
    // pos always set to 0
    private int fill(int save) throws IOException {
      int start = count - save;
      if (save > 0) {
        System.arraycopy(buf, start, buf, 0, save);
      }

      pos = 0;
      count = save;
      int n = in.read(buf, save, buf.length - save);
      if (n > 0) {
        count += n;
      }
      return n;
    }
  } // InputMunger

  // ///////////////////////////////////////////////
  // writing
  /**
   * Save the current state of the Preferences tree to disk, using the
   * original filename. The XMLStore must have been constructed from a
   * writeable XML file.
   *
   * @throws UnsupportedOperationException
   *             : if XMLStore was created from createFromResource.
   */
  public void save() throws java.io.IOException {
    if (prefsFile == null) {
      throw new UnsupportedOperationException("XMLStore is read-only");
    }

    // get temporary file to write to
    File prefTemp;
    String parentFilename = prefsFile.getParent();
    if (parentFilename == null) {
      prefTemp = File.createTempFile("pref", ".xml");
    } else {
      File parentFile = new File(parentFilename);
      prefTemp = File.createTempFile("pref", ".xml", parentFile);
    }
    prefTemp.deleteOnExit();

    // save to the temp file
    FileOutputStream fos = new FileOutputStream(prefTemp, false);
    save(fos);
    fos.close();

    // success - rename files
    File xmlBackup = new File(prefsFile.getAbsolutePath() + ".bak");
    if (xmlBackup.exists()) {
      xmlBackup.delete();
    }
    prefsFile.renameTo(xmlBackup);
    prefTemp.renameTo(prefsFile);
    prefTemp.delete();
  }

  /**
   * Save the current state of the Preferences tree to the given OutputStream.
   */
  public void save(OutputStream out) throws java.io.IOException {

    // the OutputMunger strips off the XMLEncoder header
    OutputMunger bos = new OutputMunger(out);
    PrintWriter pw = new PrintWriter(bos);

    XMLEncoder beanEncoder = new XMLEncoder(bos);
    beanEncoder.setExceptionListener(new ExceptionListener() {

      public void exceptionThrown(Exception exception) {
        System.err.println("XMLStore.save() got Exception");
        exception.printStackTrace();
      }
    });

    pw.print(prologue);
    // pw.println("<!DOCTYPE preferences SYSTEM 'http://java.sun.com/dtd/preferences.dtd'>");
    pw.print("<preferences EXTERNAL_XML_VERSION='1.0'>\n");
    // pw.println("<java version='1.4.1_01' class='java.beans.XMLDecoder'>");
    if (!rootPrefs.isUserNode()) {
      pw.print("  <root type='system'>\n");
    } else {
      pw.print("  <root type='user'>\n");
    }

    writeXmlNode(bos, pw, rootPrefs, beanEncoder, "  ");
    pw.print("  </root>\n");
    // pw.println("</java>");
    pw.print("</preferences>\n");
    pw.flush();
  }

  private void writeXmlNode(OutputMunger bos, PrintWriter out, XMLPrefs prefs, XMLEncoder beanEncoder, String m)
          throws IOException {

    m = m + "  ";

    if (debugWriteNested) {
      System.err.println(" writeXmlNode " + prefs);
    }
    if (debugWriteBean) {
      ClassLoader l = Thread.currentThread().getContextClassLoader();
      System.err.println("  ClassLoader " + l.getClass().getName());
    }

    try {
      String[] keys = prefs.keysNoDefaults();
      if (keys.length == 0) {
        out.println(m + "<map/>");
      } else {
        out.println(m + "<map>");
        for (int i = 0; i < keys.length; i++) {
          Object value = prefs.getObjectNoDefaults(keys[i]);
          // LOOK! test if in stored defaults ??

          if (value instanceof String) {
            if (debugWriteNested) {
              System.err.println("  write entry " + keys[i] + " " + value);
            }
            out.println(m + "  <entry key='" + keys[i] + "' value='" + quote((String) value) + "' />");

          } else if (value instanceof Bean.Collection) {
            Bean.Collection bean = (Bean.Collection) value;
            if (debugWriteNested) {
              System.err.println("  write bean collection " + keys[i]);
            }

            if (bean.getCollection().isEmpty()) // skip empty ??
            {
              continue;
            }

            out.println(m + "  <beanCollection key='" + keys[i] + "' class='"
                    + bean.getBeanClass().getName() + "'>");

            Iterator iter = bean.getCollection().iterator();
            while (iter.hasNext()) {
              Object o = iter.next();
              out.print(m + "    <bean ");
              bean.writeProperties(out, o);
              out.println("/>");
            }
            out.println(m + "  </beanCollection>");

          } else if (value instanceof Bean) {
            Bean bean = (Bean) value;
            if (debugWriteNested) {
              System.err.println("  write bean " + keys[i] + " " + value);
            }
            out.print(m + "  <bean key='" + keys[i] + "' class='" + bean.getBeanClass().getName() + "' ");
            bean.writeProperties(out);
            out.println("/>");

          } else { // not a String or Bean
            out.println(m + "  <beanObject key='" + keys[i] + "' >");
            out.flush();
            bos.enterBeanStream();
            try {
              if (debugWriteNested || debugWriteBean) {
                System.err.println("  write beanObject " + keys[i] + " " + value + " "
                        + value.getClass().getName());
              }
              beanEncoder.writeObject(value);
              if (debugWriteBean) {
                System.err.println("  write bean done ");
              }
            } catch (Exception e) {
              System.err.println("Exception beanEncoder: " + e);
              e.printStackTrace();
              continue;
            }
            beanEncoder.flush();
            bos.exitBeanStream();
            out.println(m + "  </beanObject>");
          }
        }
        out.println(m + "</map>");
      }

      String[] kidName = prefs.childrenNames();
      for (int i = 0; i < kidName.length; i++) {
        XMLPrefs pkid = (XMLPrefs) prefs.node(kidName[i]);
        out.println(m + "<node name='" + pkid.name() + "' >");
        writeXmlNode(bos, out, pkid, beanEncoder, m);
        out.println(m + "</node>");
      }

    } catch (java.util.prefs.BackingStoreException e) {
      e.printStackTrace();
    }
  }
  static private char[] replaceChar = {'&', '<', '>', '\'', '"', '\r', '\n'};
  static private String[] replaceWith = {"&amp;", "&lt;", "&gt;", "&apos;", "&quot;", "&#13;", "&#10;"};

  static String quote(String x) {
    // common case no replacement
    boolean ok = true;
    for (int i = 0; i < replaceChar.length; i++) {
      int pos = x.indexOf(replaceChar[i]);
      ok &= (pos < 0);
    }
    if (ok) {
      return x;
    }

    // gotta do it
    StringBuffer result = new StringBuffer(x);
    for (int i = 0; i < replaceChar.length; i++) {
      int pos = x.indexOf(replaceChar[i]);
      if (pos >= 0) {
        replace(result, replaceChar[i], replaceWith[i]);
      }
    }

    return result.toString();
  }

  static private void replace(StringBuffer sb, char out, String in) {
    for (int i = 0; i < sb.length(); i++) {
      if (sb.charAt(i) == out) {
        sb.replace(i, i + 1, in);
        i += in.length();
      }
    }
  }

  // private final int DIE = 0; // 97;
  private class OutputMunger extends java.io.BufferedOutputStream {

    boolean done = false;
    boolean bean = false;
    int countNL = 0;

    OutputMunger(OutputStream out) {
      super(out, 1024);
    }

    void enterBeanStream() {
      bean = true;
    }

    void exitBeanStream() {
      bean = false;
    }

    public void write(int b) throws IOException {
      if (done || !bean) {
        super.write(b);
      } else {
        if (b == '\n') {
          countNL++;
        }
        if (countNL == 2) {
          done = true; // skip 2 lines
        }
      }
    }

    public void write(byte b[], int off, int len) throws IOException {
      if (done || !bean) {
        super.write(b, off, len);
      } else {
        for (int i = 0; i < len; i++) {
          write(b[off + i]);
        }
      }
    }
  }
  /**
   * testing public static void main(String args[]) throws IOException {
   * InputStream is = new FileInputStream( TestAll.dir + "dataViewer.xml");
   * byte[] b = new byte[50]; int n; while (0 <= (n = is.read(b)))
   * System.err.write(b, 0, n); is.close();
   * System.err.println("************");
   *
   * XMLStore store = new XMLStore(); InputMunger im = store.new
   * InputMunger(new FileInputStream( TestAll.dir + "dataViewer.xml")); while
   * (0 <= (n = im.read(b))) { System.err.write(b, 0, n); } }
   */
}

/*
 * old way - doesnt handle nested objects correclty
 * 
 * private static final int BUFF_SIZE = 1024; private static final String header
 * =
 * "<?xml version='1.0' encoding='UTF-8'?>\n<java version='1.4.1_01' class='java.beans.XMLDecoder'>\n"
 * ; private static final String trailer = "</java>\n"; private static final
 * String startMarkerString = "<object "; private static final String
 * endMarkerString = "</object>"; class InputMunger extends
 * java.io.FilterInputStream { // buffer private byte buf[]; private int count =
 * 0; private int pos = 0;
 * 
 * // insert header boolean isHeader = true; int countHeader = 0; int sizeHeader
 * = header.length(); // insert trailer boolean isTrailer = false; int
 * countTrailer = 0; int sizeTrailer = trailer.length();
 * 
 * // look for object markers byte[] startMarker, endMarker; boolean bean =
 * false; boolean foundAngle = false; int endMarkerPos = -1;
 * 
 * InputMunger(InputStream in) throws IOException { this( in, BUFF_SIZE); }
 * 
 * InputMunger(InputStream in, int size) throws IOException { super(in); buf =
 * new byte[size]; fill(0);
 * 
 * // make a byte array for markers startMarker = new
 * byte[startMarkerString.length()]; for (int i=0; i<startMarkerString.length();
 * i++) startMarker[i] = (byte) startMarkerString.charAt(i); endMarker = new
 * byte[endMarkerString.length()]; for (int i=0; i<endMarkerString.length();
 * i++) endMarker[i] = (byte) endMarkerString.charAt(i); }
 * 
 * public int read(byte[] b, int off, int len) throws IOException { for (int i =
 * 0; i < len; i++) { int bval = read(); if (bval < 0) return i > 0 ? i : -1;
 * b[off+i] = (byte) bval; } return len; }
 * 
 * public int read() throws IOException { if (isHeader) { isHeader =
 * countHeader+1 < sizeHeader; return (int) header.charAt ( countHeader++); }
 * else if (isTrailer) { return (countTrailer < sizeTrailer) ? (int)
 * trailer.charAt ( countTrailer++) : -1; } else if (bean) { if (pos ==
 * endMarkerPos) bean = false; return read1(); } else { if (findNextObject()) {
 * foundAngle = false; findEndOfObject(); bean = true; return read1(); } else {
 * isTrailer = true; // switch to trailer return read(); } } }
 * 
 * private boolean findNextObject() throws IOException { int objectPos = -1; //
 * find pos of object marker while (0 > (objectPos =
 * indexOfMarker(startMarker))) { // didnt find, get next buffer, saving last
 * startMarker.length if (0 > fill(startMarker.length)) { count = 0; return
 * false; } }
 * 
 * // copy the <object... to start of buf; makes easier to find endMarker //
 * LOOK this means that the buffer must always be big enough to hold the <object
 * ..> element fill(count - objectPos); return true; }
 * 
 * private void findEndOfObject() { // look for /> ending if (!foundAngle) { //
 * find first angle char '>' byte b = (byte) '>'; int i = pos; while (i < count
 * && buf[i] != b) i++; if ((i == count) || (i == 0)) { // this cant happen
 * throw new RuntimeException("didnt find ending angle on object tag"); }
 * foundAngle = true;
 * 
 * if ((byte)'/' == buf[i-1]) { // is a /> endMarkerPos = i; return; } }
 * 
 * // look for the </object> tag int endPos = indexOfMarker(endMarker);
 * endMarkerPos = (endPos == -1) ? -1 : endPos + endMarker.length; }
 * 
 * // optimized for byte arrays // look for the marker in the buffer private int
 * indexOfMarker(byte marker[]) {
 * 
 * int markerLen = marker.length; byte first = marker[0]; int i = pos; // start
 * int max = count - markerLen; // end
 * 
 * startSearchForFirstChar: while (true) { // Look for first character. while (i
 * <= max && buf[i] != first) { i++; } if (i > max) { return -1; // not found }
 * 
 * // Found first character, now look for the rest of the marker int j = i + 1;
 * int end = j + markerLen - 1; int k = 1; while (j < end) { if (buf[j++] !=
 * marker[k++]) { i++; // Look for str's first char again. continue
 * startSearchForFirstChar; } } return i; // found } } // indexOfObjectMarker
 * 
 * // fill buffer from underlying stream, saving the last "save" chars // pos
 * always set to 0 private int fill(int save) throws IOException { int start =
 * count - save; if (save > 0) System.arraycopy(buf, start, buf, 0, save);
 * 
 * pos = 0; count = save; int n = in.read(buf, save, buf.length - save); if (n >
 * 0) count += n; return n; }
 * 
 * // read 1 byte from buffer private int read1() throws IOException { if (pos
 * >= count) { // need a new buffer if (bean) { fill(endMarker.length);
 * findEndOfObject(); pos = endMarker.length; } else { fill(0); } if (pos >=
 * count) { isTrailer = true; // switch to trailer return read(); } } return
 * buf[pos++] & 0xff; }
 * 
 * } // InputMunger
 * 
 * end old way
 */

/*
 * Change History: $Log: XMLStore.java,v $ Revision 1.9 2005/08/22 17:13:58
 * caron minor fixes from intelliJ analysis
 * 
 * Revision 1.8 2004/08/26 17:55:18 caron no message
 * 
 * Revision 1.7 2004/02/20 02:03:03 caron add createFromInputStream
 * 
 * Revision 1.6 2003/05/29 23:33:27 john latest release
 * 
 * Revision 1.5 2003/01/14 19:34:33 john add save(OutputStream out)
 * 
 * Revision 1.4 2003/01/06 23:21:17 john system root
 * 
 * Revision 1.3 2003/01/06 19:37:04 john new tests
 * 
 * Revision 1.2 2002/12/24 22:04:48 john add bean, beanObject methods
 * 
 * Revision 1.1.1.1 2002/12/20 16:40:25 john start new cvs root: prefs
 */
