//
// ImageJForm.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2002 Bill Hibbard, Curtis Rueden, Tom
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

package visad.data.ij;

import java.awt.Image;
import java.io.*;
import java.net.URL;
import java.rmi.RemoteException;
import visad.*;
import visad.data.*;
import visad.util.*;

/**
 * ImageJForm is the VisAD data form for the image formats
 * supported by Wayne Rasband's excellent ImageJ program:
 *
 * <dd>DICOM, FITS, PGM, JPEG, GIF, LUT, BMP,
 *     TIFF, ZIP-compressed TIFF and ROI.
 */
public class ImageJForm extends Form
  implements FormFileInformer, FormBlockReader, FormProgressInformer
{

  // -- Static fields --

  /** Counter for ImageJ form instantiation. */
  private static int num = 0;

  /** Legal ImageJ SUFFIXES. */
  private static final String[] SUFFIXES = {
    "tif", "tiff", "dicom", "fits", "pgm", "jpg",
    "jpeg", "gif", "png", "lut", "bmp", "zip", "roi"
  };

  /** Message produced when attempting to use ImageJ without it installed. */
  private static final String NO_IJ = "This feature requires ImageJ, " +
    "avialable online at http://rsb.info.nih.gov/ij/download.html";


  // -- Fields --

  /** Reflection tool for ImageJ and JAI calls. */
  private ReflectedUniverse r;

  /** Flag indicating ImageJ is not installed. */
  private boolean noImageJ = false;

  /** Percent complete with current operation. */
  private double percent = -1;


  // -- Constructor --

  /** Constructs a new ImageJ file form. */
  public ImageJForm() {
    super("ImageJForm" + num++);
    r = new ReflectedUniverse();

    // ImageJ imports
    try {
      r.exec("import ij.ImagePlus");
      r.exec("import ij.ImageStack");
      r.exec("import ij.io.Opener");
      r.exec("import ij.process.ImageProcessor");
      r.exec("opener = new Opener()");
    }
    catch (VisADException exc) { noImageJ = true; }
  }


  // -- FormFileInformer methods --

  /** Checks if the given string is a valid filename for this form. */
  public boolean isThisType(String name) {
    for (int i=0; i<SUFFIXES.length; i++) {
      if (name.toLowerCase().endsWith(SUFFIXES[i])) return true;
    }
    return false;
  }

  /** Checks if the given block is a valid header for this form. */
  public boolean isThisType(byte[] block) { return false; }

  /** Returns the default file SUFFIXES supported by ImageJ. */
  public String[] getDefaultSuffixes() {
    String[] s = new String[SUFFIXES.length];
    System.arraycopy(SUFFIXES, 0, s, 0, SUFFIXES.length);
    return s;
  }


  // -- API methods --

  /**
   * Saves a VisAD Data object to an ImageJ format.
   *
   * @param id        Filename of image file to save.
   * @param data      VisAD Data to convert to image format.
   * @param replace   Whether to overwrite an existing file.
   *
   * @exception BadFormException Always thrown (method is not implemented).
   */
  public void save(String id, Data data, boolean replace)
    throws BadFormException, IOException, RemoteException, VisADException
  {
    throw new BadFormException("ImageJForm.save");
  }

  /**
   * Adds data to an existing image file.
   *
   * @exception BadFormException Always thrown (method is not implemented).
   */
  public void add(String id, Data data, boolean replace)
    throws BadFormException
  {
    throw new BadFormException("ImageJForm.add");
  }

  /**
   * Opens an existing image file from the given filename.
   *
   * @return VisAD Data object containing image data.
   */
  public DataImpl open(String id)
    throws BadFormException, IOException, VisADException
  {
    if (noImageJ) throw new BadFormException(NO_IJ);

    percent = 0;
    File file = new File(id);
    r.setVar("dir", file.getParent() + System.getProperty("file.separator"));
    r.setVar("name", file.getName());
    r.exec("image = opener.openImage(dir, name)");
    r.exec("size = image.getStackSize()");
    int size = ((Integer) r.getVar("size")).intValue();
    if (size == 1) {
      return DataUtility.makeField((Image) r.exec("image.getImage()"));
    }

    // combine data stack into time function
    r.exec("stack = image.getStack()");
    FieldImpl[] fields = new FieldImpl[size];
    for (int i=0; i<size; i++) {
      r.setVar("i1", i + 1);
      r.exec("ip = stack.getProcessor(i1)");
      fields[i] = DataUtility.makeField((Image) r.exec("ip.createImage()"));
    }
    RealType time = RealType.getRealType("time");
    FunctionType timeFunction = new FunctionType(time, fields[0].getType());
    Integer1DSet timeSet = new Integer1DSet(size);
    FieldImpl timeField = new FieldImpl(timeFunction, timeSet);
    timeField.setSamples(fields, false);
    percent = -1;
    return timeField;
  }

  /**
   * Opens an existing image file from the given URL.
   * The data must be one of the following types:
   *
   * <dd>TIFF, ZIP-compressed TIFF, GIF or JPEG.
   *
   * @return VisAD Data object containing image data.
   */
  public DataImpl open(URL url)
    throws BadFormException, IOException, VisADException
  {
    if (noImageJ) throw new BadFormException(NO_IJ);

    r.setVar("url", url.toString());
    r.exec("image = opener.openURL(url)");
    return DataUtility.makeField((Image) r.exec("image.getImage()"));
  }

  public FormNode getForms(Data data) {
    return null;
  }

  /** Run 'java visad.data.ij.ImageJForm in_file' to read in_file. */
  public static void main(String[] args)
    throws VisADException, RemoteException, IOException
  {
    if (args == null || args.length < 1) {
      System.out.println("To test read an image file, run:");
      System.out.println("  java visad.data.ij.ImageJForm in_file");
      System.exit(2);
    }

    // Test read image file
    ImageJForm form = new ImageJForm();
    System.out.print("Reading " + args[0] + " ");
    Data data = form.open(args[0]);
    System.out.println("[done]");
    System.out.println("MathType =\n" + data.getType().prettyString());
    System.exit(0);
  }


  // -- FormBlockReader methods --

  public DataImpl open(String id, int block_number)
    throws BadFormException, IOException, VisADException
  {
    if (block_number != 0) {
      throw new BadFormException("Invalid image number: " + block_number);
    }
    return open(id);
  }

  public int getBlockCount(String id)
    throws BadFormException, IOException, VisADException
  {
    return 1;
  }

  public void close() throws BadFormException, IOException, VisADException { }


  // -- FormProgressInformer methods --

  public double getPercentComplete() { return percent; }

}
