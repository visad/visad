//
// MetamorphForm.java
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

package visad.data.bio;

import java.io.*;
import java.net.URL;
import java.util.*;

import visad.*;
import visad.data.*;
import visad.data.tiff.BitBuffer;
import visad.data.tiff.TiffTools;

/**
 * MetamorphForm is the VisAD data format adapter for Metamorph STK files.
 * @author Eric Kjellman egkjellman@wisc.edu
 */
public class MetamorphForm extends Form implements FormBlockReader,
  FormFileInformer, FormProgressInformer, MetadataReader
{

  // -- Constants --

  /** Number identifying a TIFF file. */
  private static final int TIFF_MAGIC_NUMBER = 42;

  /** Denotes little-endian. */
  private static final int LITTLE_ENDIAN = 73;

  // IFD Tag numbers of important fields
  private static final int BITS_PER_SAMPLE_FIELD = 258;
  private static final int STRIP_OFFSET_FIELD = 273;
  private static final int METAMORPH_ID = 33629;
  private static final int UIC1TAG = 33628;
  private static final int UIC2TAG = 33629;
  private static final int UIC3TAG = 33630;
  private static final int UIC4TAG = 33631;


  // -- Static fields --

  /** Form instantiation counter. */
  private static int formCount = 0;

  /** Domain of 2-D image. */
  private static RealTupleType domainTuple;

  /** MathType of a 2-D image with a 1-D range. */
  private static FunctionType funcRowColPix;

  /** MathType of a 2-D image with a 3-D range. */
  private static FunctionType funcRowColRGB;

  static {
    try {
      RealType column = RealType.getRealType("ImageElement");
      RealType row = RealType.getRealType("ImageLine");
      domainTuple = new RealTupleType(column, row);

      // for grayscale images
      RealType pixel = RealType.getRealType("intensity");
      funcRowColPix = new FunctionType(domainTuple, pixel);

      // for color images
      RealType red = RealType.getRealType("Red");
      RealType green = RealType.getRealType("Green");
      RealType blue = RealType.getRealType("Blue");
      RealType[] rgb = new RealType[] {red, green, blue};
      RealTupleType rgbPixelData = new RealTupleType(rgb);
      funcRowColRGB = new FunctionType(domainTuple, rgbPixelData);
    }
    catch (VisADException exc) {
      exc.printStackTrace();
    }
  }


  // -- Fields --

  /** Filename of current Metamorph STK. */
  private String currentId;

  /** Input stream for current Metamorph STK. */
  private RandomAccessFile r;

  /** IFD hash for current Metamorph STK. */
  private Hashtable ifdHash;

  /** XYZ dimensions of current Metamorph STK. */
  private int[] dimensions;

  /** Domain set of current Metamorph STK. */
  private Linear2DSet pixelSet;

  /** Percent complete with current operation. */
  private double percent;


  // -- Constructor --

  /** Constructs a new Metamorph file form. */
  public MetamorphForm() {
    super("MetamorphForm" + formCount++);
  }


  // -- FormFileInformer methods --

  /**
   * Checks if the given string is a valid filename
   * for a Metamorph .stk file.
   */
  public boolean isThisType(String name) {
    return name.toLowerCase().endsWith(".stk");
  }

  /**
   * Checks if the given block is a valid header for a Metamorph .stk file.
   * If it is, it should have a specific IFD tag.
   * Most metamorph files seem to have the IFD information at the end, so it is
   * difficult to determine whether or not the block is a metamorph block
   * without being passed the entire file. Therefore, we will check the only
   * things we can reasonably check at the beginning of the file, and if we
   * happen to be passed the entire file, well, great, we'll check that too.
   */
  public boolean isThisType(byte[] block) {

    // Must be little-endian tiff.
    if (block.length < 3) { return false; }
    if (block[0] != LITTLE_ENDIAN) { return false; } // denotes little-endian
    if (block[1] != LITTLE_ENDIAN) { return false; }
    if (block[2] != TIFF_MAGIC_NUMBER) { return false; } // denotes tiff
    if (block.length < 8) { return true; } // we have no way of verifying
    int ifdlocation = batoi(new byte[] {
      block[4], block[5], block[6], block[7]
    });
    if (ifdlocation + 1 > block.length) {
      // we have no way of verifying this is a Metamorph file.
      // It is at least a tiff.
      return true;
    }
    else {
      int ifdnumber = batoi(new byte[]
        {block[ifdlocation], block[ifdlocation + 1]});
      for (int i = 0; i < ifdnumber; i++) {
        if (ifdlocation + 3 + (i * 12) > block.length) {
          return true;
        }
        else {
          int ifdtag = batoi(new byte[] {
            block[ifdlocation + 2 + (i * 12)],
            block[ifdlocation + 3 + (i * 12)]
          });
          if (ifdtag == METAMORPH_ID) {
            return true; // absolutely a valid file
          }
        }
      }
      return false; // we went through the IFD, the ID wasn't found.
    }
  }

  /** Returns the default file suffixes for the Metamorph .stk file format. */
  public String[] getDefaultSuffixes() {
    return new String[] {"stk"};
  }


  // -- FormNode API methods --

  /**
   * Saves a VisAD Data object to Metamorph .stk
   * format at the given location.
   */
  public void save(String id, Data data, boolean replace)
    throws UnimplementedException
  {
    throw new UnimplementedException("MetamorphForm.save");
  }

  /**
   * Adds data to an existing Metamorph .stk file.
   *
   * @exception BadFormException Always thrown (this method not implemented).
   */
  public void add(String id, Data data, boolean replace)
    throws BadFormException
  {
    throw new BadFormException("MetamorphForm.add");
  }

  /**
   * Opens an existing Metamorph .stk file from the given location.
   *
   * @return VisAD Data object containing Metamorph data.
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
      // combine data stack into index function
      RealType index = RealType.getRealType("index");
      FunctionType indexFunction =
        new FunctionType(index, fields[0].getType());
      Integer1DSet indexSet = new Integer1DSet(nImages);
      FieldImpl indexField = new FieldImpl(indexFunction, indexSet);
      indexField.setSamples(fields, false);
      data = indexField;
    }
    close();
    percent = Double.NaN;
    return data;
  }

  /** Returns the data forms that are compatible with a data object. */
  public FormNode getForms(Data data) {
    return null;
  }

  /**
   * Opens an existing Metamorph file from the given URL.
   *
   * @return VisAD Data object containing Metamorph data.
   * @exception UnimplementedException Always thrown (method not implemented).
   */
  public DataImpl open(URL url)
    throws BadFormException, VisADException, IOException
  {
    throw new BadFormException("MetamorphForm.open(URL)");
  }


  // -- FormBlockReader API methods --

  /**
   * Opens the Metamorph file with the file name specified
   * by id, retrieving only the frame number given.
   * @return a DataImpl containing the specified frame
   */
  public DataImpl open(String id, int blockNumber)
    throws BadFormException, IOException, VisADException
  {
    if (id != currentId) initFile(id);
    int[] bitsPerPixel = null;
    int photoInterp = TiffTools.getPhotometricInterpretation(r);
    Vector v = (Vector) ifdHash.get(new Integer(BITS_PER_SAMPLE_FIELD)); // bpp
    if (v == null) {
      throw new BadFormException("Bits per sample field not found");
    }
    if (photoInterp == 2) { // RGB color
      bitsPerPixel = TiffTools.getIFDArray(r, v);
    }
    else { // assume it's grayscale
      bitsPerPixel = new int[] {((Integer) v.get(2)).intValue()};
    }
    v = (Vector) ifdHash.get(new Integer(STRIP_OFFSET_FIELD));
    if (v == null) {
      throw new BadFormException("Strip offset field not found");
    }
    // Skip ahead to the correct point in the file.
    r.seek(TiffTools.getIFDArray(r, v)[0]);
    BitBuffer bb = new BitBuffer(new FileInputStream(currentId));
    bb.skipBits(8 * r.getFilePointer());

    // Check whether it's RGB or grayscale and read file in appropriately.
    FlatField frameField = null;
    if (photoInterp == 2) { // RGB file
      long toSkip = dimensions[0] * dimensions[1];
      toSkip *= (bitsPerPixel[0] + bitsPerPixel[1] + bitsPerPixel[2]);
      toSkip *= blockNumber;
      bb.skipBits(toSkip);
      float[][] flatSamples = new float[3][dimensions[0] * dimensions[1]];
      for (int y = 0; y < dimensions[1]; y++) {
        for (int x = 0; x < dimensions[0]; x++) {
          for (int c = 0; c < 3; c ++) {
            flatSamples[c][x + y*dimensions[0]] = bb.getBits(bitsPerPixel[c]);
          }
        }
      }
      frameField = new FlatField(funcRowColRGB, pixelSet);
      frameField.setSamples(flatSamples);
    }
    else { // grayscale
      long toSkip = dimensions[0] * dimensions[1] *
        bitsPerPixel[0] * blockNumber;
      bb.skipBits(toSkip);
      float[][] flatSamples = new float[1][dimensions[0] * dimensions[1]];
      if (bitsPerPixel[0] == 8) { // 8 bit
        for (int y = 0; y < dimensions[1]; y++) {
          for (int x = 0; x < dimensions[0]; x++) {
            flatSamples[0][x + y*dimensions[0]] = bb.getBits(bitsPerPixel[0]);
            //datargb[x][y][z][c] = bb.getBits(bitsPerPixel[c]);
          }
        }
      }
      else if (bitsPerPixel[0] % 8 == 0) { // 16, 24, etc. bit
        int bytesPerPixel = bitsPerPixel[0] / 8;
        for (int y = 0; y < dimensions[1]; y++) {
          for (int x = 0; x < dimensions[0]; x++) {
            int[] thisPixel = new int[bytesPerPixel];
            for (int b = 0; b < bytesPerPixel; b++) {
              thisPixel[b] = bb.getBits(8);
            }
            for (int b = bytesPerPixel - 1; b >= 0; b--) {
              flatSamples[0][x + y*dimensions[0]] *= 256;
              flatSamples[0][x + y*dimensions[0]] += thisPixel[b];
            }
            //datargb[x][y][z][c] = bb.getBits(bitsPerPixel[c]);
          }
        }
      }
      else { // arbitrary bit case, I don't know that this works.
        for (int y = 0; y < dimensions[1]; y++) {
          for (int x = 0; x < dimensions[0]; x++) {
            flatSamples[0][x + y*dimensions[0]] = bb.getBits(bitsPerPixel[0]);
          }
        }
      }
      frameField = new FlatField(funcRowColPix, pixelSet);
      frameField.setSamples(flatSamples);
    }
    return frameField;
  }

  /** Returns the number of frames in the specified Metamorph file. */
  public int getBlockCount(String id)
    throws BadFormException, IOException, VisADException
  {
    if (id != currentId) initFile(id);
    return dimensions[2];
  }

  /** Closes the current form. */
  public void close() throws BadFormException, IOException, VisADException {
    if (r != null) {
      r.close();
      r = null;
    }
  }


  // -- FormProgressInformer API methods --

  /**
   * Gets the percentage complete of the form's current operation.
   * @return the percentage complete (0.0 - 100.0), or Double.NaN
   *         if no operation is currently taking place
   */
  public double getPercentComplete() { return percent; }



  // -- MetadataReader API methods --

  /** Creates a Hashtable containing metadata info from the file */
  public Hashtable getMetadata(String id)
    throws BadFormException, IOException, VisADException
  {
    // Where I pull all this from:
    // http://www.universal-imaging.com/ftp/support/stack/STK.doc

    Hashtable metadata = new Hashtable();
    if (id != currentId) initFile(id);
    int offset = TiffTools.getIFDValue(ifdHash, UIC4TAG);
    if (offset < 0) {
      throw new BadFormException("UIC4TAG not found");
    }
    r.seek(offset);
    int currentcode = -1;
    byte[] toread;
    Vector v = (Vector) ifdHash.get(new Integer(METAMORPH_ID));
    if (v == null) {
      throw new BadFormException("Metamorph ID not found");
    }

    int planes = ((Integer) v.get(1)).intValue();

    while (currentcode != 0) {
       toread = new byte[2];
       r.read(toread);
       currentcode = TiffTools.batoi(toread);
       toread = new byte[4]; // this is default.

       // variable declarations, because switch is dumb.
       int num, denom, julian, ms, millis, minutes, seconds, hours;
       int xnum, xdenom, ynum, ydenom;
              long a, b, c, d, e, alpha, z;
       short day, month, year;
       double xpos, ypos;
       String thedate, thetime;
       // System.out.println("Handling code: " + currentcode);
       switch(currentcode) {
         case 0:
           r.read(toread);
           metadata.put("AutoScale", new Integer(batoi(toread)));
           break;
         case 1:
           r.read(toread);
           metadata.put("MinScale", new Integer(batoi(toread)));
           break;
         case 2:
           r.read(toread);
           metadata.put("MaxScale", new Integer(batoi(toread)));
           break;
         case 3:
           r.read(toread);
           metadata.put("Spatial Calibration", new Integer(batoi(toread)));
           break;
         case 4:
           r.read(toread);
           num = batoi(toread);
           r.read(toread);
           denom = batoi(toread);
           metadata.put("XCalibration",
             new Double((double) num/(double) denom));
           break;
         case 5:
           r.read(toread);
           num = batoi(toread);
           r.read(toread);
           denom = batoi(toread);
           metadata.put("YCalibration",
             new Double((double) num/(double) denom));
           break;
         case 6:
           r.read(toread);
           num = batoi(toread);
           toread = new byte[num];
           r.read(toread);
           metadata.put("CalibrationUnits", new String(toread));
           break;
         case 7:
           r.read(toread);
           num = batoi(toread);
           toread = new byte[num];
           r.read(toread);
           metadata.put("Name", new String(toread));
           break;
         case 8:
           r.read(toread);
           metadata.put("ThreshState", new Integer(batoi(toread)));
           break;
         case 9:
           r.read(toread);
           metadata.put("ThreshStateRed", new Integer(batoi(toread)));
           break;
           // There is no 10.
         case 11:
           r.read(toread);
           metadata.put("ThreshStateGreen", new Integer(batoi(toread)));
           break;
         case 12:
           r.read(toread);
           metadata.put("ThreshStateBlue", new Integer(batoi(toread)));
           break;
         case 13:
           r.read(toread);
           metadata.put("ThreshStateLo", new Integer(batoi(toread)));
           break;
         case 14:
           r.read(toread);
           metadata.put("ThreshStateHi", new Integer(batoi(toread)));
           break;
         case 15:
           r.read(toread);
           metadata.put("Zoom", new Integer(batoi(toread)));
           break;
         case 16: // oh how we hate you Julian format...
           r.read(toread);
           julian = batoi(toread);

           // code reused from the Metamorph data specification
           z = julian + 1;

           if (z < 2299161L) {
             a = z;
           }
           else {
             alpha = (long) ((z - 1867216.25) / 36524.25);
             a = z + 1 + alpha - alpha / 4;
           }

           b = (a > 1721423L ? a + 1524 : a + 1158);
           c = (long) ((b - 122.1) / 365.25);
           d = (long) (365.25 * c);
           e = (long) ((b - d) / 30.6001);

           day = (short)(b - d - (long)(30.6001 * e));
           month = (short)((e < 13.5) ? e - 1 : e - 13);
           year = (short)((month > 2.5) ? (c - 4716) : c - 4715);

           thedate = new String(day + "/" + month + "/" + year);

           r.read(toread);
           millis = batoi(toread);

           ms = millis % 1000;
           millis -= ms;
           millis /= 1000;
           seconds = millis % 60;
           millis -= seconds;
           millis /= 60;
           minutes = millis % 60;
           millis -= minutes;
           millis /= 60;
           hours = millis;

           thetime = new String(hours + ":" +
             minutes + ":" + seconds + "." + ms);

           metadata.put("CreateTime", thedate + " " + thetime);
           break;
         case 17:
           r.read(toread);
           julian = batoi(toread);

           // code reused from the Metamorph data specification
           z = julian + 1;

           if (z < 2299161L) {
             a = z;
           }
           else {
             alpha = (long) ((z - 1867216.25) / 36524.25);
             a = z + 1 + alpha - alpha / 4;
           }

           b = (a > 1721423L ? a + 1524 : a + 1158);
           c = (long) ((b - 122.1) / 365.25);
           d = (long) (365.25 * c);
           e = (long) ((b - d) / 30.6001);

           day = (short)(b - d - (long)(30.6001 * e));
           month = (short)((e < 13.5) ? e - 1 : e - 13);
           year = (short)((month > 2.5) ? (c - 4716) : c - 4715);

           thedate = new String(day + "/" + month + "/" + year);

           r.read(toread);
           millis = batoi(toread);

           ms = millis % 1000;
           millis -= ms;
           millis /= 1000;
           seconds = millis % 60;
           millis -= seconds;
           millis /= 60;
           minutes = millis % 60;
           millis -= minutes;
           millis /= 60;
           hours = millis;

           thetime = new String(hours + ":" +
             minutes + ":" + seconds + "." + ms);

           metadata.put("LastSavedTime", thedate + " " + thetime);
           break;
         case 18:
           r.read(toread);
           metadata.put("currentBuffer", new Integer(batoi(toread)));
           break;
         case 19:
           r.read(toread);
           metadata.put("grayFit", new Integer(batoi(toread)));
           break;
         case 20:
           r.read(toread);
           metadata.put("grayPointCount", new Integer(batoi(toread)));
           break;
         case 21:
           r.read(toread);
           num = batoi(toread);
           r.read(toread);
           denom = batoi(toread);
           metadata.put("grayX", new Double((double) num/(double) denom));
           break;
         case 22:
           r.read(toread);
           num = batoi(toread);
           r.read(toread);
           denom = batoi(toread);
           metadata.put("gray", new Double((double) num/(double) denom));
           break;
         case 23:
           r.read(toread);
           num = batoi(toread);
           r.read(toread);
           denom = batoi(toread);
           metadata.put("grayMin", new Double((double) num/(double) denom));
           break;
         case 24:
           r.read(toread);
           num = batoi(toread);
           r.read(toread);
           denom = batoi(toread);
           metadata.put("grayMax", new Double((double) num/(double) denom));
           break;
         case 25:
           r.read(toread);
           num = batoi(toread);
           toread = new byte[num];
           r.read(toread);
           metadata.put("grayUnitName", new String(toread));
           break;
         case 26:
           r.read(toread);
           metadata.put("StandardLUT", new Integer(batoi(toread)));
           break;
         case 27:
           r.read(toread);
           metadata.put("Wavelength", new Integer(batoi(toread)));
           break;
         case 28:
           for (int i = 0; i < planes; i++) {
             r.read(toread);
             xnum = batoi(toread);
             r.read(toread);
             xdenom = batoi(toread);
             r.read(toread);
             ynum = batoi(toread);
             r.read(toread);
             ydenom = batoi(toread);
             xpos = xnum / xdenom;
             ypos = ynum / ydenom;
             metadata.put("Stage Position Plane " + i,
               "(" + xpos + ", " + ypos + ")");
           }
           break;
         case 29:
           for (int i = 0; i < planes; i++) {
             r.read(toread);
             xnum = batoi(toread);
             r.read(toread);
             xdenom = batoi(toread);
             r.read(toread);
             ynum = batoi(toread);
             r.read(toread);
             ydenom = batoi(toread);
             xpos = xnum / xdenom;
             ypos = ynum / ydenom;
             metadata.put("Camera Offset Plane " + i,
               "(" + xpos + ", " + ypos + ")");
           }
           break;
         case 30:
           r.read(toread);
           metadata.put("OverlayMask", new Integer(batoi(toread)));
           break;
         case 31:
           r.read(toread);
           metadata.put("OverlayCompress", new Integer(batoi(toread)));
           break;
         case 32:
           r.read(toread);
           metadata.put("Overlay", new Integer(batoi(toread)));
           break;
         case 33:
           r.read(toread);
           metadata.put("SpecialOverlayMask", new Integer(batoi(toread)));
           break;
         case 34:
           r.read(toread);
           metadata.put("SpecialOverlayCompress", new Integer(batoi(toread)));
           break;
         case 35:
           r.read(toread);
           metadata.put("SpecialOverlay", new Integer(batoi(toread)));
           break;
         case 36:
           r.read(toread);
           metadata.put("ImageProperty", new Integer(batoi(toread)));
           break;
         case 37:
           for (int i = 0; i<planes; i++) {
             toread = new byte[4];
             r.read(toread);
             num = batoi(toread);
             // System.out.println("Reading " + num + " bytes");
             toread = new byte[num];
             r.read(toread);
             metadata.put("StageLabel Plane " + i, new String(toread));
           }
           break;
         case 38:
           r.read(toread);
           num = batoi(toread);
           r.read(toread);
           denom = batoi(toread);
           metadata.put("AutoScaleLoInfo",
             new Double((double) num/(double) denom));
           break;
         case 39:
           r.read(toread);
           num = batoi(toread);
           r.read(toread);
           denom = batoi(toread);
           metadata.put("AutoScaleHiInfo",
             new Double((double) num/(double) denom));
           break;
         case 40:
           for (int i=0;i<planes;i++) {
             r.read(toread);
             num = batoi(toread);
             r.read(toread);
             denom = batoi(toread);
             metadata.put("AbsoluteZ Plane " + i,
               new Double((double) num/(double) denom));
           }
           break;
         case 41:
           for (int i=0; i<planes; i++) {
             r.read(toread);
             metadata.put("AbsoluteZValid Plane " + i,
               new Integer(batoi(toread)));
           }
           break;
         case 42:
           r.read(toread);
           metadata.put("Gamma", new Integer(batoi(toread)));
           break;
         case 43:
           r.read(toread);
           metadata.put("GammaRed", new Integer(batoi(toread)));
           break;
         case 44:
           r.read(toread);
           metadata.put("GammaGreen", new Integer(batoi(toread)));
           break;
         case 45:
           r.read(toread);
           metadata.put("GammaBlue", new Integer(batoi(toread)));
           break;
       } // end switch

    }
    return metadata;
  }

  /** Returns a single Metadata value from the file. */
  public Object getMetadataValue(String id, String field)
    throws BadFormException, IOException, VisADException {

    Hashtable h = getMetadata(id);
    try {
      return h.get(field);
    }
    catch (NullPointerException e) {
      return null;
    }

  }


  // -- Utility methods --

  /** Translates up to the first 4 bytes of a byte array to an integer. */
  private static int batoi(byte[] inp) {
    int len = inp.length>4?4:inp.length;
    int total = 0;
    for (int i = 0; i < len; i++) {
      total += ((inp[i]<0?256+inp[i]:(int)inp[i]) << (i * 8));
    }
    return total;
  }


  // -- Helper methods --

  private void initFile(String id)
    throws IOException, VisADException, BadFormException
  {
    r = new RandomAccessFile(id, "r");
    currentId = id;
    ifdHash = TiffTools.getIFDHash(r);
    // Gets dimensions from TiffTools, but here it's not quite right.
    dimensions = TiffTools.getTIFFDimensions(r);
    if (dimensions == null) {
      throw new BadFormException("Metamorph dimensions not found");
    }
    // metamorph stores its data a bit differently, so we need to
    // get the z dimension seperately:
    Vector v = (Vector) ifdHash.get(new Integer(METAMORPH_ID));
    if (v == null) {
      throw new BadFormException("Metamorph ID not found");
    }
    // In the ID tags, the count field indicates how many frames there are,
    // not the way it is handled in standard TIFF files.
    dimensions[2] = ((Integer) v.get(1)).intValue();
    pixelSet = new Linear2DSet(domainTuple, 0,
      dimensions[0] - 1, dimensions[0], dimensions[1] - 1, 0, dimensions[1]);
  }


  // -- Main method --

  /**
   * Run 'java visad.data.bio.MetamorphForm in_file'
   * to test read a Metamorph STK data file.
   */
  public static void main(String[] args)
    throws VisADException, IOException
  {
    if (args == null || args.length < 1) {
      System.out.println("To test read a Metamorph STK file, run:");
      System.out.println("  java visad.data.bio.MetamorphForm in_file");
      System.exit(2);
    }

    // Test read Metamorph STK file
    MetamorphForm form = new MetamorphForm();
    System.out.print("Reading " + args[0] + " metadata ");
    Hashtable meta = form.getMetadata(args[0]);
    System.out.println("[done]");
    System.out.println();

    Enumeration e = meta.keys();
    Vector v = new Vector();
    while (e.hasMoreElements()) v.add(e.nextElement());
    String[] keys = new String[v.size()];
    v.copyInto(keys);
    Arrays.sort(keys);

    for (int i=0; i<keys.length; i++) {
      System.out.println(keys[i] + ": " + meta.get(keys[i]));
    }
    System.out.println();

    System.out.print("Reading " + args[0] + " pixel data ");
    Data data = form.open(args[0]);
    System.out.println("[done]");

    System.out.println("MathType =\n" + data.getType());
    System.exit(0);
  }

}
