//
// JAIForm.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2019 Bill Hibbard, Curtis Rueden, Tom
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

package visad.data.jai;

import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URL;
import java.rmi.RemoteException;
import visad.*;
import visad.data.*;
import visad.util.*;

/**
 * JAIForm is the VisAD data form for image formats supported by the Java
 * Advanced Imaging API: BMP, GIF, FlashPix, JPEG, PNG, PNM, and TIFF.
 */
public class JAIForm extends Form implements FormFileInformer {

  private static int num = 0;

  private static final String[] suffixes = {
    "bmp", "gif", "flashpix", "jpg", "jpeg", "jpe", "png", "pnm", "tif", "tiff"
  };

  private static boolean noJai = false;

  private static ReflectedUniverse r = createReflectedUniverse();

  private static ReflectedUniverse createReflectedUniverse() {
    ReflectedUniverse r = null;
    try {
      r = new ReflectedUniverse();
      r.exec("import javax.media.jai.JAI");
      r.exec("import javax.media.jai.PlanarImage");
    }
    catch (VisADException exc) { noJai = true; }
    return r;
  }

  private static BufferedImage createImage(String s, Object o) {
    BufferedImage bi = null;
    try {
      r.setVar("s", s);
      r.setVar("o", o);
      r.exec("pi = JAI.create(s, o)");
      bi = (BufferedImage) r.exec("pi.getAsBufferedImage()");
    }
    catch (VisADException exc) { }
    return bi;
  }

  /** Constructs a new JAI file form. */
  public JAIForm() {
    super("JAIForm" + num++);
  }

  /** Checks if the given string is a valid filename for a JAI image file. */
  public boolean isThisType(String name) {
    if (noJai) return false;
    for (int i=0; i<suffixes.length; i++) {
      if (name.toLowerCase().endsWith(suffixes[i])) return true;
    }
    return false;
  }

  /** Checks if the given block is a valid header for a JAI image file. */
  public boolean isThisType(byte[] block) {
    return false;
  }

  /** Returns the default file suffixes for the JAI image file formats. */
  public String[] getDefaultSuffixes() {
    String[] s = new String[suffixes.length];
    System.arraycopy(suffixes, 0, s, 0, suffixes.length);
    return s;
  }

  /**
   * Saves a VisAD Data object to a JAI image format at the given location.
   *
   * @exception BadFormException Always thrown (method is not implemented).
   */
  public void save(String id, Data data, boolean replace)
    throws BadFormException, IOException, RemoteException, VisADException
  {
    throw new BadFormException("JAIForm.save");
  }

  /**
   * Adds data to an existing JAI image file.
   *
   * @exception BadFormException Always thrown (method is not implemented).
   */
  public void add(String id, Data data, boolean replace)
    throws BadFormException
  {
    throw new BadFormException("JAIForm.add");
  }

  /**
   * Opens an existing JAI image file from the given location.
   *
   * @return VisAD Data object containing JAI image data.
   */
  public DataImpl open(String id)
    throws BadFormException, IOException, VisADException
  {
    if (noJai) {
      throw new BadFormException("you need to install JAI from " +
        "http://java.sun.com/products/java-media/jai/index.html");
    }
    BufferedImage bi = createImage("fileload", id);
    return open(bi);
  }

  /**
   * Opens an existing JAI image file from the given URL.
   *
   * @return VisAD Data object containing JAI image data.
   */
  public DataImpl open(URL url)
    throws BadFormException, IOException, VisADException
  {
    if (noJai) {
      throw new BadFormException("you need to install JAI from " +
        "http://java.sun.com/products/java-media/jai/index.html");
    }
    BufferedImage bi = createImage("URL", url);
    return open(bi);
  }

  /** Converts the given image to a VisAD Data object. */
  private DataImpl open(BufferedImage image)
    throws BadFormException, IOException, VisADException
  {
    if (image == null) {
      throw new BadFormException("JAI could not read the file as an image");
    }
    return DataUtility.makeField(image);
  }

  public FormNode getForms(Data data) {
    return null;
  }

  /**
   * Run 'java visad.data.visad.JAIForm in_file' to test read
   * an image file supported by Java Advanced Imaging.
   */
  public static void main(String[] args)
    throws VisADException, RemoteException, IOException
  {
    if (args == null || args.length < 1) {
      System.out.println("To test read an image file, run:");
      System.out.println("  java visad.data.jai.JAIForm in_file");
      System.exit(2);
    }

    // Test read image file
    JAIForm form = new JAIForm();
    System.out.print("Reading " + args[0] + " ");
    Data data = form.open(args[0]);
    System.out.println("[done]");
    System.out.println("MathType =\n" + data.getType().prettyString());
    System.exit(0);
  }

}

