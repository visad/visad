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

import ij.*;
import ij.io.*;
import java.io.*;
import java.net.URL;
import java.rmi.RemoteException;
import visad.*;
import visad.data.*;
import visad.util.DataUtility;

/**
 * ImageJForm is the VisAD data form for the image formats
 * supported by Wayne Rasband's excellent ImageJ program:
 *
 * <dd>DICOM, FITS, PGM, JPEG, GIF, LUT, BMP,
 *     TIFF, ZIP-compressed TIFF and ROI.
 */
public class ImageJForm extends Form implements FormFileInformer {

  private static int num = 0;

  private static final String[] suffixes = {
    "tif", "tiff", "dicom", "fits", "pgm", "jpg",
    "jpeg", "gif", "lut", "bmp", "zip", "roi"
  };

  private Opener opener;

  /** Constructs a new ImageJ file form. */
  public ImageJForm() {
    super("ImageJForm" + num++);
    opener = new Opener();
  }

  /** Checks if the given string is a valid filename for this form. */
  public boolean isThisType(String name) {
    for (int i=0; i<suffixes.length; i++) {
      if (name.toLowerCase().endsWith(suffixes[i])) return true;
    }
    return false;
  }

  /** Checks if the given block is a valid header for this form. */
  public boolean isThisType(byte[] block) { return false; }

  /** Returns the default file suffixes supported by ImageJ. */
  public String[] getDefaultSuffixes() {
    String[] s = new String[suffixes.length];
    System.arraycopy(suffixes, 0, s, 0, suffixes.length);
    return s;
  }

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
    File file = new File(id);
    ImagePlus image = opener.openImage(file.getParent() +
      System.getProperty("file.separator"), file.getName());
    return DataUtility.makeField(image.getImage());
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
    ImagePlus image = opener.openURL(url.toString());
    return DataUtility.makeField(image.getImage());
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

}
