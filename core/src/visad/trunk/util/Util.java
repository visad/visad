//
// Util.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2006 Bill Hibbard, Curtis Rueden, Tom
Rink, Dave Glowacki, Steve Emmerson, Tom Whittaker, Don Murray, and
Tommy Jasmin.

This library is free software; you can redistribute it and/or
modify it under the terms of the GNU Library General Public
License as published by the Free Software Foundation; either
version 2 of the License, or (at your option) any later version.

This library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
Library General Public License for more details.

You should have received a copy of the GNU Library General Public
License along with this library; if not, write to the Free
Software Foundation, Inc., 59 Temple Place - Suite 330, Boston,
MA 02111-1307, USA
*/

package visad.util;

import com.sun.image.codec.jpeg.*;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import loci.formats.ComboFileFilter;
import loci.formats.ExtensionFileFilter;
import ncsa.hdf.hdf5lib.H5;
import visad.ConstantMap;
import visad.Display;
import visad.DisplayImpl;
import visad.VisADException;
import visad.data.bio.LociForm;
import visad.data.mcidas.AreaForm;
import visad.data.mcidas.MapForm;

/**
 * A hodge-podge of general utility methods.
 */
public class Util
{
  /**
   * Determine whether two numbers are roughly the same.
   *
   * @param a First number
   * @param b Second number
   * @param epsilon Absolute amount by which they can differ.
   *
   * @return <TT>true</TT> if they're approximately equal.
   */
  public static boolean isApproximatelyEqual(float a, float b, float epsilon)
  {
    // deal with NaNs first
    if (Float.isNaN(a)) {
      return Float.isNaN(b);
    } else if (Float.isNaN(b)) {
      return false;
    }

    if (Float.isInfinite(a)) {
      if (Float.isInfinite(b)) {
        return ((a < 0.0 && b < 0.0) || (a > 0.0 && b > 0.0));
      }

      return false;
    } else if (Float.isInfinite(b)) {
      return false;
    }

    if (Math.abs(a - b) < epsilon) {
      return true;
    }

    if (a == 0.0 || b == 0.0) {
      return false;
    }

    final float FLT_EPSILON = 1.192092896E-07F;

    return Math.abs(1 - a / b) < FLT_EPSILON;
  }

  /**
   * Determine whether two numbers are roughly the same.
   *
   * @param a First number
   * @param b Second number
   *
   * @return <TT>true</TT> if they're approximately equal.
   */
  public static boolean isApproximatelyEqual(float a, float b)
  {
    return isApproximatelyEqual(a, b, 0.00001);
  }

  /**
   * Determine whether two numbers are roughly the same.
   *
   * @param a First number
   * @param b Second number
   * @param epsilon Absolute amount by which they can differ.
   *
   * @return <TT>true</TT> if they're approximately equal.
   */
  public static boolean isApproximatelyEqual(double a, double b,
                                             double epsilon)
  {
    // deal with NaNs first
    if (Double.isNaN(a)) {
      return Double.isNaN(b);
    } else if (Double.isNaN(b)) {
      return false;
    }

    if (Double.isInfinite(a)) {
      if (Double.isInfinite(b)) {
        return ((a < 0.0 && b < 0.0) || (a > 0.0 && b > 0.0));
      }

      return false;
    } else if (Double.isInfinite(b)) {
      return false;
    }

    if (Math.abs(a - b) < epsilon) {
      return true;
    }

    if (a == 0.0 || b == 0.0) {
      return false;
    }

    final double DBL_EPSILON = 2.2204460492503131E-16;

    return Math.abs(1 - a / b) < DBL_EPSILON;
  }

  /**
   * Determine whether two numbers are roughly the same.
   *
   * @param a First number
   * @param b Second number
   *
   * @return <TT>true</TT> if they're approximately equal.
   */
  public static boolean isApproximatelyEqual(double a, double b)
  {
    return isApproximatelyEqual(a, b, 0.000000001);
  }

  /**
   * Return a string representation of the current date and time.
   */
  public static String getTimestamp() {
    StringBuffer sb = new StringBuffer();
    Calendar cal = Calendar.getInstance();
    int year = cal.get(Calendar.YEAR);
    int month = cal.get(Calendar.MONTH);
    int day = cal.get(Calendar.DAY_OF_MONTH);
    int hour = cal.get(Calendar.HOUR_OF_DAY);
    int min = cal.get(Calendar.MINUTE);
    int sec = cal.get(Calendar.SECOND);
    int milli = cal.get(Calendar.MILLISECOND);
    sb.append(year);
    sb.append("/");
    if (month < 9) sb.append("0");
    sb.append(month + 1);
    sb.append("/");
    if (day < 10) sb.append("0");
    sb.append(day);
    sb.append(", ");
    if (hour < 10) sb.append("0");
    sb.append(hour);
    sb.append(":");
    if (min < 10) sb.append("0");
    sb.append(min);
    sb.append(":");
    if (sec < 10) sb.append("0");
    sb.append(sec);
    sb.append(".");
    if (milli < 100) sb.append("0");
    if (milli < 10) sb.append("0");
    sb.append(milli);
    return sb.toString();
  }

  /**
   * Return a JFileChooser that recognizes supported VisAD file types.
   */
  public static JFileChooser getVisADFileChooser() {
    JFileChooser dialog = new JFileChooser(System.getProperty("user.dir"));
    Vector filters = new Vector();
    boolean jai = canDoJAI();

    // Amanda F2000 - amanda/F2000Form
    FileFilter f2000 = new ExtensionFileFilter("r", "Amanda F2000");
    filters.add(f2000);

    // ASCII text - text/TextForm
    FileFilter text = new ExtensionFileFilter(
      new String[] {"csv", "tsv","bsv","txt"}, "ASCII text");
    filters.add(text);

    // DEM - gis/DemFamily
    FileFilter dem = new ExtensionFileFilter("dem",
      "Digital Elevation Model");
    filters.add(dem);

    // FITS - fits/FitsForm
    FileFilter fits = new ExtensionFileFilter("fits",
      "Flexible Image Transport System");
    filters.add(fits);

    // FlashPix - jai/JAIForm
    if (jai) {
      FileFilter flashpix = new ExtensionFileFilter("flashpix", "FlashPix");
      filters.add(flashpix);
    }

    // HDF-5 - hdf5/HDF5Form
    if (canDoHDF5()) {
      FileFilter hdf5 = new ExtensionFileFilter(
        new String[] {"hdf", "hdf5"}, "HDF-5");
      filters.add(hdf5);
    }

    // HDF-EOS - hdfeos/HdfeosForm
    FileFilter hdfeos = new ExtensionFileFilter(
      new String[] {"hdf", "hdfeos"}, "HDF-EOS");
    filters.add(hdfeos);

    // McIDAS area - mcidas/AreaForm
    FormFileFilter mcidasArea =
      new FormFileFilter(new AreaForm(), "McIDAS area (AREA*, *area)");
    filters.add(mcidasArea);

    // McIDAS map - mcidas/MapForm
    FormFileFilter mcidasMap =
      new FormFileFilter(new MapForm(), "McIDAS map (OUTL*)");
    filters.add(mcidasMap);

    // netCDF - netcdf/Plain
    FileFilter netcdf = new ExtensionFileFilter("nc", "NetCDF");
    filters.add(netcdf);

    // PNM - jai/JAIForm
    if (jai) {
      FileFilter pnm = new ExtensionFileFilter("pnm", "PNM");
      filters.add(pnm);
    }

    // VisAD binary/serialized - visad/VisADForm
    FileFilter visad = new ExtensionFileFilter(
      "vad", "Binary or serialized VisAD");
    filters.add(visad);

    // Vis5D - vis5d/Vis5DForm
    FileFilter vis5d = new ExtensionFileFilter("v5d", "Vis5D");
    filters.add(vis5d);

    // biology-related formats - LociForm
    FileFilter[] lociFilters = new LociForm().getReader().getFileFilters();
    for (int i=0; i<lociFilters.length; i++) filters.add(lociFilters[i]);

    // sort and combine filters alphanumerically
    FileFilter[] ff = ComboFileFilter.sortFilters(filters);

    // combination filter
    FileFilter combo = new ComboFileFilter(ff, "All VisAD file types");

    // add filters to chooser
    dialog.addChoosableFileFilter(combo);
    for (int i=0; i<ff.length; i++) dialog.addChoosableFileFilter(ff[i]);
    dialog.setFileFilter(combo);

    return dialog;
  }

  /**
   * Limit the given text field to one line in height.
   */
  public static void adjustTextField(JTextField field) {
    Dimension msize = field.getMaximumSize();
    Dimension psize = field.getPreferredSize();
    msize.height = psize.height;
    field.setMaximumSize(msize);
  }

  /**
   * Limit the given combo box to one line in height.
   */
  public static void adjustComboBox(JComboBox combo) {
    Dimension msize = combo.getMaximumSize();
    Dimension psize = combo.getPreferredSize();
    msize.height = psize.height;
    combo.setMaximumSize(msize);
  }

  /**
   * Center the given window on the screen.
   */
  public static void centerWindow(Window window) {
    Dimension s = Toolkit.getDefaultToolkit().getScreenSize();
    Dimension w = window.getSize();
    window.setLocation((s.width - w.width) / 2, (s.height - w.height) / 2);
    int x = (s.width - w.width) / 2;
    int y = (s.height - w.height) / 2;
    if (x < 0) x = 0;
    if (y < 0) y = 0;
    window.setLocation(x, y);
  }

  /**
   * Center the given window within the specified parent window.
   */
  public static void centerWindow(Window parent, Window window) {
    Point loc = parent.getLocation();
    Dimension p = parent.getSize();
    Dimension w = window.getSize();
    int x = loc.x + (p.width - w.width) / 2;
    int y = loc.y + (p.height - w.height) / 2;
    if (x < 0) x = 0;
    if (y < 0) y = 0;
    window.setLocation(x, y);
  }

  /**
   * Test whether HDF-5 native code is present in this JVM.
   * @return true if found, otherwise false
   */
  public static boolean canDoHDF5() {
    boolean success = false;
    try {
      H5.J2C(0); // HDF-5 call initializes HDF-5 native library
      success = true;
    }
    catch (NoClassDefFoundError err) { }
    catch (UnsatisfiedLinkError err) { }
    catch (Exception exc) { }
    return success;
  }

  /**
   * Test whether ImageJ is present in this JVM.
   * @return true if found, otherwise false
   */
  public static boolean canDoImageJ() {
    return canDoClass("ij.IJ") != null;
  }

  /**
   * Test whether JPEG codec (com.sun.image.codec.jpeg) is present in this JVM.
   * @return true if found, otherwise false
   */
  public static boolean canDoJPEG() {
    return canDoClass("com.sun.image.codec.jpeg.JPEGCodec") != null;
  }

  /**
   * Test whether Java Advanced Imaging is present in this JVM.
   * @return true if found, otherwise false
   */
  public static boolean canDoJAI() {
    return canDoClass("javax.media.jai.JAI") != null;
  }

  /**
   * Test whether Jython is present in this JVM.
   * @return true if found, otherwise false
   */
  public static boolean canDoPython() {
    return canDoClass("org.python.util.PythonInterpreter") != null;
  }

  /**
   * Test whether QuickTime for Java is present in this JVM.
   * @return true if found, otherwise false
   */
  public static boolean canDoQuickTime() {
    return canDoClass("quicktime.QTSession") != null;
  }

  /**
   * Test whether Java3D is present in this JVM.
   * @return true if found, otherwise false
   */
  public static boolean canDoJava3D() {
    return canDoJava3D("1.0");
  }

  /**
   * Check to see if the version of Java3D being used is compatible
   * with the desired specification version.
   * @param version   version to check.  Needs to conform to the dotted format
   *                  of specification version numbers (e.g., 1.2)
   * @return true if the Java3D version being used is greater than or
   *         equal to the desired version number
   */
  public static boolean canDoJava3D(String version) {
    Class testClass = canDoClass("javax.vecmath.Point3d");
    boolean b = (testClass != null)
                   ? (canDoClass("javax.media.j3d.SceneGraphObject") != null)
                   : false;
    if (b) {
      Package p = testClass.getPackage();
      if (p != null) {
        try {
          b = p.isCompatibleWith(version);
        } catch (NumberFormatException nfe) { b = false; }
      }
    }
    return b;
  }

  /**
   * General classloader tester.
   * @param classname  name of class to test
   * @return  the class or null if class can't be loaded.
   */
  private static Class canDoClass(String classname)
  {
    Class c = null;
    try {
      c = Class.forName(classname);
    }
    catch (ClassNotFoundException exc) { }
    return c;
  }

  /**
   * Capture a DisplayImpl into a JPEG file
   *
   * @param display the DisplayImpl to capture
   * @param filename the name of the file to write into
   *
   */
  public static void captureDisplay(DisplayImpl display, String filename)
  {
      captureDisplay(display, filename, false);
  }

  /**
   * Capture a DisplayImpl into a JPEG file
   *
   * @param display the DisplayImpl to capture
   * @param filename the name of the file to write into
   * @param sync ensure the display is "done" if true
   *
   */
  public static void captureDisplay(DisplayImpl display,
                                    String filename, boolean sync)
  {
    final DisplayImpl disp = display;
    final File fn = new File(filename);
    final boolean wait = sync;

    Runnable savedisp = new Runnable() {
      public void run() {
        BufferedImage image = disp.getImage(wait);
        try {
          JPEGEncodeParam param = JPEGCodec.getDefaultJPEGEncodeParam(image);
          param.setQuality(1.0f, true);
          FileOutputStream fout = new FileOutputStream(fn);
          JPEGImageEncoder encoder = JPEGCodec.createJPEGEncoder(fout);
          encoder.encode(image, param);
          fout.close();
        }
        catch (Exception err) {
          System.err.println("Error whilst saving JPEG: "+err);
        }
      }
    };
    Thread t = new Thread(savedisp);
    t.start();
  }

  /**
   * Tests whether two arrays are component-wise equal.
   */
  public static boolean arraysEqual(Object[] o1, Object[] o2) {
    // test for null
    if (o1 == null && o2 == null) return true;
    if (o1 == null || o2 == null) return false;

    // test for differing lengths
    if (o1.length != o2.length) return false;

    // test each component
    for (int i=0; i<o1.length; i++) {
      if (!o1[i].equals(o2[i])) return false;
    }
    return true;
  }

  /**
   * Executes the given Runnable object with the Swing event handling thread.
   *
   * @param wait <tt>true</tt> if method should block until Runnable code
   *             finishes execution.
   * @param r Runnable object to execute using the event handling thread.
   */
  public static void invoke(boolean wait, Runnable r) {
    invoke(wait, false, r);
  }

  /**
   * Executes the given Runnable object with the Swing event handling thread.
   *
   * @param wait <tt>true</tt> if method should block until Runnable code
   *             finishes execution.
   * @param printStackTraces <tt>true</tt> if the stack trace for
   *                         any exception should be printed.
   * @param r Runnable object to execute using the event handling thread.
   */
  public static void invoke(boolean wait,
    boolean printStackTraces, Runnable r)
  {
    if (wait) {
      // use invokeAndWait
      if (Thread.currentThread().getName().startsWith("AWT-EventQueue")) {
        // current thread is the AWT event queue thread; just execute the code
        r.run();
      }
      else {
        // execute the code with the AWT event thread
        try {
          SwingUtilities.invokeAndWait(r);
        }
        catch (InterruptedException exc) {
          if (printStackTraces) exc.printStackTrace();
        }
        catch (InvocationTargetException exc) {
          if (printStackTraces) exc.getTargetException().printStackTrace();
        }
      }
    }
    else {
      // use invokeLater
      SwingUtilities.invokeLater(r);
    }
  }

  /**
   * Create a ConstantMap array of colors for use with
   * @{link Display.addReference(DataReference, ConstantMap[])
   *   Display.addReference()}
   *
   * @param color color to encode
   *
   * @return an array containing either 3 colors or, if the <tt>color</tt>
   *         parameter included an alpha component, a 4 element array
   *         with 3 colors and an @{link Display.Alpha alpha} component.
   */
  public static ConstantMap[] getColorMaps(Color color)
    throws VisADException
  {
    final int alpha = color.getAlpha();

    ConstantMap[] maps = new ConstantMap[alpha == 255 ? 3 : 4];

    maps[0] = new ConstantMap((float )color.getRed() / 255.0f,
                              Display.Red);
    maps[1] = new ConstantMap((float )color.getGreen() / 255.0f,
                              Display.Green);
    maps[2] = new ConstantMap((float )color.getBlue() / 255.0f,
                              Display.Blue);
    if (alpha != 255) {
      maps[3] = new ConstantMap((float )color.getAlpha() / 255f,
                                Display.Alpha);
    }

    return maps;
  }
}
