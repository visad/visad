//
// LegacyTiffForm.java
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

package visad.data.tiff;

import java.awt.Image;
import java.awt.image.*;
import java.lang.reflect.*;
import java.io.*;
import java.net.URL;
import java.rmi.RemoteException;
import visad.*;
import visad.data.*;
import visad.util.*;

/**
 * LegacyTiffForm is the old VisAD data form for the TIFF file format.
 * It relies on either ImageJ or JAI being available in the class path,
 * and is very inefficient when dealing with large multi-page TIFF files.
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
 * This form requires ImageJ, available from the
 * <a href="http://rsb.info.nih.gov/ij/download.html">ImageJ</a> web site.
 *
 * Note that features marked with &quot;(with JAI)&quot; also require
 * the Java Advanced Imaging (JAI) package, available at Sun's
 * <a href="http://java.sun.com/products/java-media/jai/index.html">
 * Java Advanced Imaging</a> web site.
 *
 * Also, no support for reading TIFF data from URLs is provided.
 * However, the visad.data.jai package provides limited support for
 * importing single-image TIFF data from a URL.
 *
 * @deprecated Use TiffForm, or visad.data.bio.LociForm
 *   with loci.formats.TiffReader and loci.formats.TiffWriter
 */
public class LegacyTiffForm extends Form
  implements FormFileInformer, FormBlockReader, FormProgressInformer
{

  // -- Static fields --

  /** Counter for TIFF form instantiation. */
  private static int formCount = 0;

  /** Legal TIFF SUFFIXES. */
  private static final String[] SUFFIXES = { "tif", "tiff" };

  /** Message produced when attempting to use ImageJ without it installed. */
  private static final String NO_IJ = "This feature requires ImageJ, " +
    "available online at http://rsb.info.nih.gov/ij/download.html";

  /** Message produced when attempting to use JAI without it installed. */
  private static final String NO_JAI = "This feature requires JAI, " +
    "available from Sun at http://java.sun.com/products/java-media/jai/";


  // -- Fields --

  /** Reflection tool for ImageJ and JAI calls. */
  private ReflectedUniverse r;

  /** Flag indicating ImageJ is not installed. */
  private boolean noImageJ = false;

  /** Flag indicating JAI is not installed. */
  private boolean noJai = false;

  /** Filename of current TIFF stack. */
  private String currentId;

  /** Number of images in current TIFF stack. */
  private int numImages;

  /** Flag indicating whether ImageJ supports the current TIFF stack. */
  private boolean canUseImageJ;

  /** Percent complete with current operation. */
  private double percent;


  // -- Constructor --

  /** Constructs a new TIFF file form. */
  public LegacyTiffForm() {
    super("LegacyTiffForm" + formCount++);
    r = new ReflectedUniverse();

    // ImageJ imports
    try {
      r.exec("import ij.ImagePlus");
      r.exec("import ij.ImageStack");
      r.exec("import ij.io.FileInfo");
      r.exec("import ij.io.FileSaver");
      r.exec("import ij.io.Opener");
      r.exec("import ij.io.TiffDecoder");
      r.exec("import ij.process.ByteProcessor");
      r.exec("import ij.process.ColorProcessor");
      r.exec("import ij.process.FloatProcessor");
      r.exec("import ij.process.ImageProcessor");
      r.exec("import ij.process.ShortProcessor");
    }
    catch (VisADException exc) { noImageJ = true; }

    // JAI imports
    try {
      r.exec("import com.sun.media.jai.codec.ImageDecodeParam");
      r.exec("import com.sun.media.jai.codec.ImageDecoder");
      r.exec("import com.sun.media.jai.codec.ImageCodec");
    }
    catch (VisADException exc) { noJai = true; }
  }


  // -- FormFileInformer methods --

  /** Checks if the given string is a valid filename for a TIFF file. */
  public boolean isThisType(String name) {
    for (int i=0; i<SUFFIXES.length; i++) {
      if (name.toLowerCase().endsWith(SUFFIXES[i])) return true;
    }
    return false;
  }

  /** Checks if the given block is a valid header for a TIFF file. */
  public boolean isThisType(byte[] block) {
    return false;
  }

  /** Returns the default file SUFFIXES for the TIFF file format. */
  public String[] getDefaultSuffixes() {
    String[] s = new String[SUFFIXES.length];
    System.arraycopy(SUFFIXES, 0, s, 0, SUFFIXES.length);
    return s;
  }

  // -- API methods --

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
    if (noImageJ) throw new BadFormException(NO_IJ);

    percent = 0;
    FlatField[] fields = DataUtility.getImageFields(data);
    if (fields == null) {
      throw new BadFormException(
        "Data type must be image or time sequence of images");
    }
    r.setVar("id", id);
    if (fields.length > 1) {
      // save as multi-page TIFF
      Object is = null;
      for (int i=0; i<fields.length; i++) {
        r.setVar("ips", extractImage(fields[i]));
        if (is == null) {
          r.exec("w = ips.getWidth()");
          r.exec("h = ips.getHeight()");
          r.exec("cm = ips.getColorModel()");
          r.exec("is = new ImageStack(w, h, cm)");
          is = r.getVar("is");
        }
        r.setVar("si", "" + i);

        // UGLY HACK
        //
        // There are two methods:
        //  - ImageStack.addSlice(String, Object)
        //  - ImageStack.addSlice(String, ImageProcessor)
        //
        // But since addSlice(String, Object) is declared first,
        // ReflectedUniverse always matches it first, and thus it is
        // impossible to call addSlice(String, ImageProcessor).
        //
        // We must fall back to basic Java reflection to accomplish this...

        //r.exec("is.addSlice(si, ips)");
        try {
          Class imageStack = Class.forName("ij.ImageStack");
          Class imageProcessor = Class.forName("ij.process.ImageProcessor");
          Method addSlice = imageStack.getMethod("addSlice",
            new Class[] {String.class, imageProcessor});
          addSlice.invoke(is, new Object[] {"" + i, r.getVar("ips")});
        }
        catch (ClassNotFoundException exc) {
          throw new BadFormException(
            "Reflection exception: class not found", exc);
        }
        catch (NoSuchMethodException exc) {
          throw new BadFormException(
            "Reflection exception: no such method", exc);
        }
        catch (IllegalAccessException exc) {
          throw new BadFormException(
            "Reflection exception: illegal access", exc);
        }
        catch (InvocationTargetException exc) {
          throw new BadFormException(
            "Reflection exception", exc.getTargetException());
        }

        percent = (double) (i + 1) / fields.length;
      }
      r.exec("image = new ImagePlus(id, is)");
      r.exec("sav = new FileSaver(image)");
      r.exec("sav.saveAsTiffStack(id)");
    }
    else {
      // save as single image TIFF
      r.setVar("ip", extractImage(fields[0]));
      r.exec("image = new ImagePlus(id, ip)");
      r.exec("sav = new FileSaver(image)");
      r.exec("sav.saveAsTiff(id)");
    }

    percent = -1;
  }

  /**
   * Adds data to an existing TIFF file.
   *
   * @exception BadFormException Always thrown (method is not implemented).
   */
  public void add(String id, Data data, boolean replace)
    throws BadFormException
  {
    throw new BadFormException("LegacyTiffForm.add");
  }

  /**
   * Opens an existing TIFF file from the given filename.
   *
   * @return VisAD Data object containing TIFF data.
   */
  public DataImpl open(String id)
    throws BadFormException, IOException, VisADException
  {
    percent = 0;
    int nImages = getBlockCount(id);
    FieldImpl[] fields = new FieldImpl[nImages];
    for (int i=0; i<nImages; i++) {
      fields[i] = (FieldImpl) open(id, i);
      percent = (double) (i + 1) / nImages;
    }

    DataImpl data;
    if (nImages == 1) data = fields[0];
    else {
      // combine data stack into time function
      RealType time = RealType.getRealType("time");
      FunctionType timeFunction = new FunctionType(time, fields[0].getType());
      Integer1DSet timeSet = new Integer1DSet(nImages);
      FieldImpl timeField = new FieldImpl(timeFunction, timeSet);
      timeField.setSamples(fields, false);
      data = timeField;
    }
    close();
    percent = -1;
    return data;
  }

  /**
   * Opens an existing TIFF file from the given URL.
   *
   * @return VisAD Data object containing TIFF data.
   *
   * @exception BadFormException Always thrown (method is not implemented).
   */
  public DataImpl open(URL url)
    throws BadFormException, IOException, VisADException
  {
    throw new BadFormException("LegacyTiffForm.open(URL)");
  }

  public FormNode getForms(Data data) {
    return null;
  }


  // -- FormBlockReader methods --

  public DataImpl open(String id, int block_number)
    throws BadFormException, IOException, VisADException
  {
    if (!id.equals(currentId)) initFile(id);

    if (block_number < 0 || block_number >= numImages) {
      throw new BadFormException("Invalid image number: " + block_number);
    }

    Image img = null;

    if (canUseImageJ) {
      if (noImageJ) throw new BadFormException(NO_IJ);
      r.exec("stack = image.getStack()");
      r.setVar("bn1", block_number + 1);
      r.exec("ip = stack.getProcessor(bn1)");
      r.exec("img = ip.createImage()");
      img = (Image) r.getVar("img");
    }
    else {
      if (noJai) throw new BadFormException(NO_JAI);
      try {
        r.setVar("i", block_number);
        RenderedImage ri =
          (RenderedImage) r.exec("id.decodeAsRenderedImage(i)");
        WritableRaster wr = ri.copyData(null);
        ColorModel cm = ri.getColorModel();
        img = new BufferedImage(cm, wr, false, null);
      }
      catch (VisADException exc) { throw new BadFormException(exc); }
    }
    return DataUtility.makeField(img);
  }

  public int getBlockCount(String id)
    throws BadFormException, IOException, VisADException
  {
    if (!id.equals(currentId)) initFile(id);
    return numImages;
  }

  public void close() throws BadFormException, IOException, VisADException { }


  // -- FormProgressInformer methods --

  public double getPercentComplete() { return percent; }


  // -- Helper methods --

  /**
   * Converts a FlatField of the form <tt>((x, y) -&gt; value)</tt> or
   * <tt>((x, y) -&gt; (r, g, b))</tt> to an ImageJ ImageProcessor object.
   */
  private Object extractImage(FlatField field) throws VisADException {
    GriddedSet set = (GriddedSet) field.getDomainSet();
    int[] wh = set.getLengths();
    int w = wh[0];
    int h = wh[1];
    float[][] samples = field.getFloats(false);
    r.setVar("w", w);
    r.setVar("h", h);

    // HACK - detect "fake" 3-color images
    boolean fake3 = samples.length == 3 &&
      samples[0] == samples[1] && samples[0] == samples[2];

    if (samples.length == 3 && !fake3) {
      // 24-bit color is the best we can do
      int[] pixels = new int[samples[0].length];
      for (int i=0; i<pixels.length; i++) {
        int red = (int) samples[0][i] & 0x000000ff;
        int green = (int) samples[1][i] & 0x000000ff;
        int blue = (int) samples[2][i] & 0x000000ff;
        pixels[i] = red << 16 | green << 8 | blue;
      }
      r.setVar("pixels", pixels);
      r.exec("proc = new ColorProcessor(w, h, pixels)");
    }
    else if (samples.length == 1 || fake3) {
      // check for 8-bit, 16-bit or 32-bit grayscale
      float lo = Float.POSITIVE_INFINITY, hi = Float.NEGATIVE_INFINITY;
      for (int i=0; i<samples[0].length; i++) {
        float value = samples[0][i];
        if (value != (int) value) {
          // force 32-bit floats
          hi = Float.POSITIVE_INFINITY;
          break;
        }
        if (value < lo) {
          lo = value;
          if (lo < 0) break; // need 32-bit floats
        }
        if (value > hi) {
          hi = value;
          if (hi >= 65536) break; // need 32-bit floats
        }
      }
      if (lo >= 0 && hi < 256) {
        // 8-bit grayscale
        byte[] pixels = new byte[samples[0].length];
        for (int i=0; i<pixels.length; i++) {
          int val = (int) samples[0][i] & 0x000000ff;
          pixels[i] = (byte) val;
        }
        r.setVar("pixels", pixels);
        r.setVar("cm", null);
        r.exec("proc = new ByteProcessor(w, h, pixels, cm)");
      }
      else if (lo >= 0 && hi < 65536) {
        // 16-bit grayscale
        short[] pixels = new short[samples[0].length];
        for (int i=0; i<pixels.length; i++) {
          int val = (int) samples[0][i];
          pixels[i] = (short) val;
        }
        r.setVar("pixels", pixels);
        r.setVar("cm", null);
        r.exec("proc = new ShortProcessor(w, h, pixels, cm)");
      }
      else {
        // 32-bit floating point grayscale
        r.setVar("pixels", samples[0]);
        r.setVar("cm", null);
        r.exec("proc = new FloatProcessor(w, h, pixels, cm)");
      }
    }
    return r.getVar("proc");
  }

  private void initFile(String id)
    throws BadFormException, IOException, VisADException
  {
    if (noImageJ) throw new BadFormException(NO_IJ);

    // close any currently open files
    close();

    // determine whether ImageJ can handle the file
    r.setVar("id", id);
    r.setVar("empty", "");
    r.exec("tdec = new TiffDecoder(empty, id)");
    canUseImageJ = true;
    try {
      r.exec("info = tdec.getTiffInfo()");
    }
    catch (VisADException exc) {
      canUseImageJ = false;
    }

    // determine number of images in TIFF file
    if (canUseImageJ) {
      r.exec("opener = new Opener()");
      r.exec("image = opener.openImage(id)");
      r.exec("numImages = image.getStackSize()");
      numImages = ((Integer) r.getVar("numImages")).intValue();
    }
    else {
      if (noJai) throw new BadFormException(NO_JAI);
      try {
        r.setVar("tiff", "tiff");
        r.setVar("file", new File(id));
        r.exec("id = ImageCodec.createImageDecoder(tiff, file, null)");
        numImages = ((Integer) r.exec("id.getNumPages()")).intValue();
      }
      catch (VisADException exc) { throw new BadFormException(exc); }
    }

    currentId = id;
  }


  // -- Main method --

  /**
   * Run 'java visad.data.visad.LegacyTiffForm in_file out_file' to convert
   * in_file to out_file in TIFF data format.
   */
  public static void main(String[] args)
    throws VisADException, RemoteException, IOException
  {
    if (args == null || args.length < 1 || args.length > 2) {
      System.out.println("To convert a file to TIFF, run:");
      System.out.println("  java " +
        "visad.data.tiff.LegacyTiffForm in_file out_file");
      System.out.println("To test read a TIFF file, run:");
      System.out.println("  java visad.data.tiff.LegacyTiffForm in_file");
      System.exit(2);
    }

    if (args.length == 1) {
      // Test read TIFF file
      LegacyTiffForm form = new LegacyTiffForm();
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
      LegacyTiffForm form = new LegacyTiffForm();
      form.save(args[1], data, true);
      System.out.println("[done]");
    }
    System.exit(0);
  }

}
