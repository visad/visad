//
// TiffForm.java
//

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

package visad.data.tiff;

import ij.*;
import ij.io.*;
import ij.process.ImageProcessor;
import java.awt.Image;
import java.awt.image.*;
import java.lang.reflect.*;
import java.io.*;
import java.net.URL;
import java.rmi.RemoteException;
import visad.*;
import visad.data.*;
import visad.util.DataUtility;

/**
 * TiffForm is the VisAD data form for the TIFF file format.
 * The following table indicates features that the form supports:<p>
 *
 * <table border=1><tr>
 * <td>&nbsp;</td>
 * <td><b>uncompressed</b></td>
 * <td><b>compressed (LZW)</b></td>
 * </tr><tr>
 * <td><b>single image</b></td>
 * <td>read and write</td>
 * <td>read only (with JAI)</td>
 * </tr><tr>
 * <td><b>multi-page</b></td>
 * <td>read and write</td>
 * <td>read only (with JAI)</td>
 * </tr></table><p>
 *
 * Note that features marked with &quot;(with JAI)&quot; require the
 * Java Advanced Imaging (JAI) package, available at Sun's
 * <a href="http://java.sun.com/products/java-media/jai/index.html">
 * Java Advanced Imaging</a> web site.
 *
 * Also, no support for reading TIFF data from URLs is provided.
 * However, the visad.data.jai package provides limited support for
 * importing single-image TIFF data from a URL.
 */
public class TiffForm extends Form implements FormFileInformer {

  private static int num = 0;

  private static final String[] suffixes = { "tif", "tiff" };

  private static final String NO_JAI = "This feature requires JAI, " +
    "available from Sun at http://java.sun.com/products/java-media/jai/";

  private static boolean noJai = false;

  private static final Method[] methods = createMethods();

  private static final Method[] createMethods() {
    Method[] m = new Method[3];
    for (int i=0; i<3; i++) m[i] = null;
    try {
      Class idp = Class.forName("com.sun.media.jai.codec.ImageDecodeParam");
      Class id = Class.forName("com.sun.media.jai.codec.ImageDecoder");
      Class ic = Class.forName("com.sun.media.jai.codec.ImageCodec");

      m = new Method[3];
      m[0] = ic.getMethod("createImageDecoder",
        new Class[] {String.class, File.class, idp});
      m[1] = id.getMethod("getNumPages", new Class[0]);
      m[2] = id.getMethod("decodeAsRenderedImage", new Class[] {int.class});
    }
    catch (ClassNotFoundException exc) { noJai = true; }
    catch (NoSuchMethodException exc) { noJai = true; }
    return m;
  }

  private static final Method icCreateDec = methods[0];
  private static final Method idNumPages = methods[1];
  private static final Method idDecode = methods[2];

  private static BufferedImage[] jaiGetImages(String filename)
    throws BadFormException, IOException
  {
    if (noJai) throw new BadFormException(NO_JAI);
    BufferedImage[] bi = null;
    try {
      File file = new File(filename);
      Object id = icCreateDec.invoke(null, new Object[] {"tiff", file, null});
      Object ni = idNumPages.invoke(id, new Object[0]);
      int numImages = ((Integer) ni).intValue();
      bi = new BufferedImage[numImages];
      for (int i=0; i<numImages; i++) {
        Object o = idDecode.invoke(id, new Object[] {new Integer(i)});
        RenderedImage ri = (RenderedImage) o;
        WritableRaster wr = ri.copyData(null);
        ColorModel cm = ri.getColorModel();
        bi[i] = new BufferedImage(cm, wr, false, null);
      }
    }
    catch (IllegalAccessException exc) {
      throw new BadFormException(exc.getMessage());
    }
    catch (IllegalArgumentException exc) {
      throw new BadFormException(exc.getMessage());
    }
    catch (InvocationTargetException exc) {
      Throwable t = exc.getTargetException();
      if (t instanceof IOException) throw (IOException) t;
      else throw new BadFormException(t.getMessage());
    }
    return bi;
  }

  /** Constructs a new TIFF file form. */
  public TiffForm() {
    super("TiffForm" + num++);
  }

  /** Checks if the given string is a valid filename for a TIFF file. */
  public boolean isThisType(String name) {
    for (int i=0; i<suffixes.length; i++) {
      if (name.toLowerCase().endsWith(suffixes[i])) return true;
    }
    return false;
  }

  /** Checks if the given block is a valid header for a TIFF file. */
  public boolean isThisType(byte[] block) {
    return false;
  }

  /** Returns the default file suffixes for the TIFF file format. */
  public String[] getDefaultSuffixes() {
    String[] s = new String[suffixes.length];
    System.arraycopy(suffixes, 0, s, 0, suffixes.length);
    return s;
  }

  /**
   * Saves a VisAD Data object to an uncompressed TIFF file.
   *
   * @param id        Filename of TIFF file to save.
   * @param data      VisAD Data to convert to TIFF format.
   * @param replace   Whether to overwrite an existing file.
   */
  public void save(String id, Data data, boolean replace)
    throws BadFormException, IOException, RemoteException, VisADException
  {
    // determine data type
    String imageType = "((e, l) -> (r, g, b))";
    String timeType = "(t -> " + imageType + ")";
    MathType type = data.getType();
    RealType time = null;
    FlatField[] field;
    boolean multiPage;
    if (type.equalsExceptName(MathType.stringToType(timeType))) {
      multiPage = true;
      FieldImpl fi = (FieldImpl) data;
      int len = fi.getLength();
      field = new FlatField[len];
      for (int i=0; i<len; i++) field[i] = (FlatField) fi.getSample(i);
      FunctionType ft = (FunctionType) data.getType();
      time = (RealType) ft.getDomain().getComponent(0);
    }
    else if (type.equalsExceptName(MathType.stringToType(imageType))) {
      multiPage = false;
      field = new FlatField[] {(FlatField) data};
    }
    else {
      throw new BadFormException(
        "Data type must be image or time sequence of images");
    }
    Gridded2DSet set = (Gridded2DSet) field[0].getDomainSet();
    int[] wh = set.getLengths();
    int w = wh[0];
    int h = wh[1];

    throw new UnimplementedException("Saving data to TIFF coming soon");
  }

  /**
   * Adds data to an existing TIFF file.
   *
   * @exception BadFormException Always thrown (method is not implemented).
   */
  public void add(String id, Data data, boolean replace)
    throws BadFormException
  {
    throw new BadFormException("TiffForm.add");
  }

  /**
   * Opens an existing TIFF file from the given filename.
   *
   * @return VisAD Data object containing TIFF data.
   */
  public DataImpl open(String id)
    throws BadFormException, IOException, VisADException
  {
    // determine whether ImageJ can handle the file
    TiffDecoder tdec = new TiffDecoder("", id);
    FileInfo info = null;
    boolean canUseImageJ = true;
    try {
      info = tdec.getTiffInfo()[0];
    }
    catch (IOException exc) {
      String msg = exc.getMessage();
      if (msg.startsWith("Unsupported BitsPerSample")
        || msg.startsWith("Unsupported SamplesPerPixel")
        || msg.startsWith("ImageJ cannot open compressed TIFF files"))
      {
        canUseImageJ = false;
      }
      else throw exc;
    }

    int nImages;
    FieldImpl[] fields;

    if (canUseImageJ) {
      // use ImageJ
      FileOpener fo = new FileOpener(info);
      ImageStack stack = fo.open(false).getStack();
      nImages = stack.getSize();
      fields = new FieldImpl[nImages];
      for (int i=0; i<nImages; i++) {
        ImageProcessor ip = stack.getProcessor(i + 1);
        fields[i] = DataUtility.makeField(ip.createImage());
      }
    }
    else {
      // ImageJ could not read the TIFF; try JAI
      BufferedImage[] bi = jaiGetImages(id);
      nImages = bi.length;
      fields = new FieldImpl[nImages];
      for (int i=0; i<nImages; i++) fields[i] = DataUtility.makeField(bi[i]);
    }
    if (nImages < 1) throw new BadFormException("No images in file");

    if (nImages == 1) return fields[0];
    else {
      // combine data stack into time function
      RealType time = RealType.getRealType("time");
      FunctionType time_function = new FunctionType(time, fields[0].getType());
      Integer1DSet time_set = new Integer1DSet(nImages);
      FieldImpl time_field = new FieldImpl(time_function, time_set);
      time_field.setSamples(fields, false);
      return time_field;
    }
  }

  /**
   * Opens an existing TIFF file from the given URL.
   *
   * @return VisAD Data object containing TIFF data.
   */
  public DataImpl open(URL url)
    throws BadFormException, IOException, VisADException
  {
    throw new BadFormException("TiffForm.open(URL)");
  }

  public FormNode getForms(Data data) {
    return null;
  }

  /**
   * Run 'java visad.data.visad.TiffForm in_file out_file' to convert
   * in_file to out_file in TIFF data format.
   */
  public static void main(String[] args)
    throws VisADException, RemoteException, IOException
  {
    if (args == null || args.length < 1 || args.length > 2) {
      System.out.println("To convert a file to TIFF, run:");
      System.out.println("  java visad.data.tiff.TiffForm in_file out_file");
      System.out.println("To test read a TIFF file, run:");
      System.out.println("  java visad.data.tiff.TiffForm in_file");
      System.exit(2);
    }

    if (args.length == 1) {
      // Test read TIFF file
      TiffForm form = new TiffForm();
      System.out.print("Reading " + args[0] + " ");
      Data data = form.open(args[0]);
      System.out.println("[done]");
      System.out.println("MathType =\n" + data.getType().prettyString());
    }
    else if (args.length == 2) {
      // Convert file to TIFF format
      System.out.print(args[0] + " -> " + args[1] + " ");
      DefaultFamily loader = new DefaultFamily("loader");
      DataImpl data = loader.open(args[0]);
      loader = null;
      TiffForm form = new TiffForm();
      form.save(args[1], data, true);
      System.out.println("[done]");
    }
    System.exit(0);
  }

}
