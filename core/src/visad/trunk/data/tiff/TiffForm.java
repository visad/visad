//
// TiffForm.java
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

package visad.data.tiff;

import ij.*;
import ij.io.*;
import ij.process.*;
import java.awt.Image;
import java.awt.image.*;
import java.io.*;
import java.net.URL;
import java.rmi.RemoteException;
import visad.*;
import visad.data.*;
import visad.util.*;

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
public class TiffForm extends Form
  implements FormFileInformer, FormBlockReader, FormProgressInformer
{

  // -- Static fields --

  /** Counter for TIFF form instantiation. */
  private static int num = 0;

  /** Legal TIFF suffixes. */
  private static final String[] suffixes = { "tif", "tiff" };

  /** Message produced when attempting to use JAI without it installed. */
  private static final String NO_JAI = "This feature requires JAI, " +
    "available from Sun at http://java.sun.com/products/java-media/jai/";


  // -- Fields --

  /** Reflection tool for JAI calls. */
  private ReflectedUniverse r;

  /** Flag indicating JAI is not installed. */
  private boolean noJai = false;

  /** Filename of current TIFF stack. */
  private String current_id;

  /** Number of images in current TIFF stack. */
  private int numImages;

  /** Flag indicating whether ImageJ supports the current TIFF stack. */
  private boolean canUseImageJ;

  /** Percent complete with current operation. */
  private double percent;


  // -- Constructor --

  /** Constructs a new TIFF file form. */
  public TiffForm() {
    super("TiffForm" + num++);

    try {
      r = new ReflectedUniverse();
      r.exec("import com.sun.media.jai.codec.ImageDecodeParam");
      r.exec("import com.sun.media.jai.codec.ImageDecoder");
      r.exec("import com.sun.media.jai.codec.ImageCodec");
    }
    catch (VisADException exc) { noJai = true; }
  }


  // -- FormFileInformer methods --

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
    percent = 0;
    FlatField[] fields = DataUtility.getImageFields(data);
    if (fields == null) {
      throw new BadFormException(
        "Data type must be image or time sequence of images");
    }
    if (fields.length > 1) {
      // save as multi-page TIFF
      int len = fields.length;
      ImageProcessor[] ips = new ImageProcessor[len];
      ImageStack is = null;
      for (int i=0; i<len; i++) {
        ips[i] = extractImage(fields[i]);
        if (is == null) {
          is = new ImageStack(ips[0].getWidth(),
            ips[0].getHeight(), ips[0].getColorModel());
        }
        is.addSlice("" + i, ips[i]);
        percent = (double) (i + 1) / len;
      }
      ImagePlus image = new ImagePlus(id, is);
      FileSaver sav = new FileSaver(image);
      sav.saveAsTiffStack(id);
    }
    else {
      // save as single image TIFF
      ImageProcessor ip = extractImage(fields[0]);
      ImagePlus image = new ImagePlus(id, ip);
      FileSaver sav = new FileSaver(image);
      sav.saveAsTiff(id);
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
      FunctionType time_function = new FunctionType(time, fields[0].getType());
      Integer1DSet time_set = new Integer1DSet(nImages);
      FieldImpl time_field = new FieldImpl(time_function, time_set);
      time_field.setSamples(fields, false);
      data = time_field;
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
    throw new BadFormException("TiffForm.open(URL)");
  }

  public FormNode getForms(Data data) {
    return null;
  }


  // -- FormBlockReader methods --

  public DataImpl open(String id, int block_number)
    throws BadFormException, IOException, VisADException
  {
    if (!id.equals(current_id)) initFile(id);

    if (block_number < 0 || block_number >= numImages) {
      throw new BadFormException("Invalid image number: " + block_number);
    }

    Image img = null;

    if (canUseImageJ) {
      ImagePlus image = new Opener().openImage(id);
      ImageStack stack = image.getStack();
      ImageProcessor ip = stack.getProcessor(block_number + 1);
      img = ip.createImage();
    }
    else {
      if (noJai) throw new BadFormException(NO_JAI);
      try {
        r.setVar("i", new Integer(block_number));
        RenderedImage ri =
          (RenderedImage) r.exec("id.decodeAsRenderedImage(i)");
        WritableRaster wr = ri.copyData(null);
        ColorModel cm = ri.getColorModel();
        img = new BufferedImage(cm, wr, false, null);
      }
      catch (VisADException exc) {
        throw new BadFormException(exc.getMessage());
      }
    }
    return DataUtility.makeField(img);
  }

  public int getBlockCount(String id)
    throws BadFormException, IOException, VisADException
  {
    if (!id.equals(current_id)) initFile(id);
    return numImages;
  }

  public void close() throws BadFormException, IOException, VisADException { }


  // -- FormProgressInformer methods --

  public double getPercentComplete() { return percent; }


  // -- Helper methods --

  /**
   * Converts a FlatField of the form <tt>((x, y) -&gt; (r, g, b))</tt>
   * to an ImageJ ImageProcessor.
   */
  public static ImageProcessor extractImage(FlatField field)
    throws VisADException
  {
    Gridded2DSet set = (Gridded2DSet) field.getDomainSet();
    int[] wh = set.getLengths();
    int w = wh[0];
    int h = wh[1];
    double[][] samples = field.getValues();
    int[] pixels = new int[samples[0].length];
    if (samples.length == 3) {
      for (int i=0; i<samples[0].length; i++) {
        int r = (int) samples[0][i] & 0x000000ff;
        int g = (int) samples[1][i] & 0x000000ff;
        int b = (int) samples[2][i] & 0x000000ff;
        pixels[i] = r << 16 | g << 8 | b;
      }
    }
    else if (samples.length == 1) {
      for (int i=0; i<samples[0].length; i++) {
        int v = (int) samples[0][i] & 0x000000ff;
        pixels[i] = v << 16 | v << 8 | v;
      }
    }
    ColorProcessor cp = new ColorProcessor(w, h, pixels);
    return cp;
  }

  private void initFile(String id)
    throws BadFormException, IOException, VisADException
  {
    // close any currently open files
    close();

    // determine whether ImageJ can handle the file
    TiffDecoder tdec = new TiffDecoder("", id);
    canUseImageJ = true;
    try {
      FileInfo[] info = tdec.getTiffInfo();
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

    // determine number of images in TIFF file
    if (canUseImageJ) {
      ImagePlus image = new Opener().openImage(id);
      numImages = image.getStackSize();
    }
    else {
      if (noJai) throw new BadFormException(NO_JAI);
      try {
        r.setVar("tiff", "tiff");
        r.setVar("file", new File(id));
        r.exec("id = ImageCodec.createImageDecoder(tiff, file, null)");
        Object ni = r.exec("id.getNumPages()");
        numImages = ((Integer) ni).intValue();
      }
      catch (VisADException exc) {
        throw new BadFormException(exc.getMessage());
      }
    }

    current_id = id;
  }


  // -- Main method --

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
