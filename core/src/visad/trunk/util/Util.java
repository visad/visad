/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2000 Bill Hibbard, Curtis Rueden, Tom
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

import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.Window;
import javax.swing.JFileChooser;
import javax.swing.JTextField;
import javax.swing.filechooser.FileFilter;
import ncsa.hdf.hdf5lib.H5;

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
   * Return a JFileChooser that recognizes accepted VisAD file types.
   */
  public static JFileChooser getVisADFileChooser() {
    // construct file filters
    FileFilter biorad = new ExtensionFileFilter(
      "pic", "BioRad PIC images");
    FileFilter bmp = new ExtensionFileFilter(
      "bmp", "BMP images");
    FileFilter fits = new ExtensionFileFilter(
      "fits", "FITS datasets");
    FileFilter flashpix = new ExtensionFileFilter(
      "flashpix", "FlashPix images");
    FileFilter gif = new ExtensionFileFilter(
      "gif", "GIF images");
    FileFilter hdf5 = new ExtensionFileFilter(
      new String[] {"hdf", "hdf5"}, "HDF-5 datasets");
    FileFilter hdfeos = new ExtensionFileFilter(
      new String[] {"hdf", "hdfeos"}, "HDF-EOS datasets");
    FileFilter jpeg = new ExtensionFileFilter(
      new String[] {"jpg", "jpeg", "jpe"}, "JPEG images");
    FileFilter mcidas = new McIDASFileFilter();
    FileFilter netcdf = new ExtensionFileFilter(
      new String[] {"nc", "ncf", "netcdf", "cdf"}, "netCDF datasets");
    FileFilter png = new ExtensionFileFilter(
      "png", "PNG images");
    FileFilter pnm = new ExtensionFileFilter(
      "pnm", "PNM images");
    FileFilter serial = new ExtensionFileFilter(
      new String[] {"svd", "vad", "visad"}, "Serialized VisAD data");
    FileFilter tiff = new ExtensionFileFilter(
      new String[] {"tiff", "tif"}, "TIFF images");
    FileFilter text = new ExtensionFileFilter(
      new String[] {"csv", "tsv","bsv","txt"}, "ASCII text data");
    FileFilter vis5d = new ExtensionFileFilter(
      "v5d", "Vis5D datasets");
    FileFilter combo = new ComboFileFilter(new FileFilter[] {
      biorad, bmp, fits, flashpix, gif, hdf5, hdfeos, jpeg, mcidas, netcdf,
      png, pnm, serial, tiff, text, vis5d}, "All VisAD file types");

    // construct and configure file chooser
    JFileChooser dialog = new JFileChooser(System.getProperty("user.dir"));
    dialog.addChoosableFileFilter(biorad);
    dialog.addChoosableFileFilter(bmp);
    dialog.addChoosableFileFilter(fits);
    dialog.addChoosableFileFilter(flashpix);
    dialog.addChoosableFileFilter(gif);
    dialog.addChoosableFileFilter(hdf5);
    dialog.addChoosableFileFilter(hdfeos);
    dialog.addChoosableFileFilter(jpeg);
    dialog.addChoosableFileFilter(mcidas);
    dialog.addChoosableFileFilter(netcdf);
    dialog.addChoosableFileFilter(png);
    dialog.addChoosableFileFilter(pnm);
    dialog.addChoosableFileFilter(serial);
    dialog.addChoosableFileFilter(tiff);
    dialog.addChoosableFileFilter(text);
    dialog.addChoosableFileFilter(vis5d);
    dialog.addChoosableFileFilter(combo);
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
   * Center the given window on the screen.
   */
  public static void centerWindow(Window window) {
    Dimension s = Toolkit.getDefaultToolkit().getScreenSize();
    Dimension w = window.getSize();
    window.setLocation(s.width / 2 - w.width / 2, s.height / 2 - w.height / 2);
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
   * Test whether JPEG codec (com.sun.image.codec.jpeg) is present in this JVM.
   * @return true if found, otherwise false
   */
  public static boolean canDoJPEG() {
    /* DRM 04-APR-2001
    boolean success = false;
    try {
      Class c = Class.forName("com.sun.image.codec.jpeg.JPEGCodec");
      success = true;
    }
    catch (ClassNotFoundException exc) { }
    return success;
    */
    return (canDoClass("com.sun.image.codec.jpeg.JPEGCodec") != null);
  }

  /**
   * Test whether JPython is present in this JVM.
   * @return true if found, otherwise false
   */
  public static boolean canDoPython() {
    /* DRM 04-APR-2001
    boolean success = false;
    try {
      Class c = Class.forName("org.python.util.PythonInterpreter");
      success = true;
    }
    catch (ClassNotFoundException exc) { }
    return success;
    */
    return (canDoClass("org.python.util.PythonInterpreter") != null);
  }

  /**
   * Test whether Java 3D is present
   * @return true if found, otherwise false
   */
  public static boolean canDoJava3D() {
    return canDoJava3D("1.0");
  }

  /**
   * Checks to see if the version of Java 3D being used is compatible
   * with the desired specification version.
   * @param version   version to check.  Needs to conform to the dotted format
   *                  of specification version numbers (e.g., 1.2)
   * @return true if the Java 3D version being used is greater than or 
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
}
