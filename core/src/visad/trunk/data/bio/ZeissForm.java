//
// ZeissForm.java
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

import visad.*;
import visad.data.*;
import visad.data.tiff.*;
import java.io.*;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.*;

/**
 * ZeissForm is the VisAD data adapter used by Zeiss files.
 * @author Eric Kjellman egkjellman@wisc.edu
 */
public class ZeissForm extends Form implements FormBlockReader,
  FormFileInformer, FormProgressInformer, MetadataReader
{

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

  /** Filename of current Zeiss LSM. */
  private String currentId;

  /** Input stream for current Zeiss LSM. */
  private RandomAccessFile r;

  /** XYZ dimensions of current Zeiss LSM. */
  private int[] dimensions;

  /** Domain set of current Zeiss LSM. */
  private Linear2DSet pixelSet;

  /** Offset for each block of current Zeiss LSM. */
  private int[] offsets;

  private int maxChannels;
  private int[] actualImages;

  /** Percent complete with current operation. */
  private double percent;


  // -- Constructor --

  /** Constructs a new Zeiss LSM file form. */
  public ZeissForm() {
    super("ZeissForm" + formCount++);
  }


  // -- FormNode API methods --

  /** Saves a VisAD Data object to Zeiss LSM format at the given location. */
  public void save(String id, Data data, boolean replace)
    throws BadFormException, IOException, RemoteException, VisADException
  {
    throw new UnimplementedException("ZeissForm.save");
  }

  /**
   * Adds data to an existing Zeiss LSM file.
   *
   * @exception BadFormException Always thrown (this method not implemented).
   */
  public void add(String id, Data data, boolean replace)
    throws BadFormException
  {
    throw new BadFormException("ZeissForm.add");
  }

  /**
   * Opens an existing Zeiss LSM file from the given location.
   *
   * @return VisAD Data object containing Zeiss LSM data.
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
    percent = -1;
    return data;
  }

  /**
   * Opens an existing Zeiss LSM file from the given URL.
   *
   * @return VisAD Data object containing Zeiss LSM data.
   * @exception UnimplementedException Always thrown (method not implemented).
   */
  public DataImpl open(URL url)
    throws BadFormException, VisADException, IOException
  {
    throw new BadFormException("ZeissForm.open(URL)");
  }

  /** Returns the data forms that are compatible with a data object. */
  public FormNode getForms(Data data) {
    return null;
  }


  // -- FormBlockReader methods --

  /**
   * Opens the Zeiss LSM file with the file name specified
   * by id, retrieving only the frame number given.
   * @return a DataImpl containing the specified frame
   */
  public DataImpl open(String id, int blockNumber)
    throws BadFormException, IOException, VisADException
  {
    if (id != currentId) initFile(id);

    Hashtable ifdEntries = TiffTools.getIFDHash(r, actualImages[blockNumber]);
    Vector entryData;
    byte[] byteArray;
    int stripOffsets, stripOffsetCount, stripBytes;
    float[] toreturn;

    // This is the directory entry for strip offsets
    entryData = (Vector) ifdEntries.get(new Integer(273));
    stripOffsetCount = ((Integer) entryData.get(1)).intValue();
    stripOffsets = ((Integer) entryData.get(2)).intValue();
    // This is the directory entry for strip bytes
    entryData = (Vector) ifdEntries.get(new Integer(279));
    stripBytes = ((Integer) entryData.get(2)).intValue();
    int[][] stripInfo = new int[stripOffsetCount][2];
    int current;
    int total = 0;

    // If there is only one strip offset in the IFD, it will contain the data
    // itself.
    if (stripOffsetCount == 1) {
      stripInfo[0][0] = stripOffsets;
      stripInfo[0][1] = stripBytes;
      total = stripBytes;
    }
    else {
      // Otherwise, it will contain a pointer, and we need to read the data out
      r.seek(stripOffsets);
      byteArray = new byte[4];
      for(int i = 0; i < stripOffsetCount; i++) {
        r.read(byteArray);
        stripInfo[i][0] = batoi(byteArray);
      }
      r.seek(stripBytes);
      for(int i = 0; i < stripOffsetCount; i++) {
        r.read(byteArray);
        current = batoi(byteArray);
        stripInfo[i][1] = current;
        total += current;
      }
    }
    // Then, create the array to return, and read the data in from the
    // file.
    byte[] imageData = new byte[total];
    current = 0;
    //System.out.println(" Image Number: " + blockNumber);
    for(int i = 0; i < stripOffsetCount; i++) {
      r.seek(stripInfo[i][0]);
      //System.out.println("   Reading from: " + r.getFilePointer());
      byteArray = new byte[stripInfo[i][1]];
      r.read(byteArray);
      // System.out.println("   Read " + byteArray.length + " bytes");
      if (TiffTools.getIFDValue(ifdEntries, 259) == 5) {
        byteArray = TiffTools.lzwUncompress(byteArray);
      }
      System.arraycopy(byteArray, 0, imageData, current, byteArray.length);
      current += byteArray.length;
      // read reads in as bytes (signed), we need them as unsigned floats.
    }

    entryData = (Vector) ifdEntries.get(new Integer(262));
    int photoint = ((Integer) entryData.get(2)).intValue();
    current = 0;
    //System.out.println(" photoint: " + photoint);
    FlatField frameField = null;
    if (photoint == 2) {
      int[] bitsPerSample =
         TiffTools.getIFDArray(r, (Vector) ifdEntries.get(new Integer(258)));
      BitBuffer bb = new BitBuffer(new ByteArrayInputStream(imageData));

      float[][] flatSamples =
        new float[bitsPerSample.length][dimensions[0] * dimensions[1]];
      if (TiffTools.getIFDValue(ifdEntries, 317) == 2) { // use differencing
        int currentVal, currentByte = 0;
        for (int c = 0; c < bitsPerSample.length; c++) {
          for (int y = 0; y < dimensions[1]; y++) {
            currentVal = 0;
            for (int x = 0; x < dimensions[0]; x++) {
              if (bitsPerSample[c] > 0) {
                currentVal += imageData[currentByte];
                currentVal = currentVal % 256;
                if (currentVal < 0) {
                  currentVal += 256;
                }
                flatSamples[c][x + y*dimensions[0]] = currentVal;
                currentByte++;
              }
              else {
                flatSamples[c][x + y*dimensions[0]] = 0;
              }
            }
          }
        }
      }
      else {
        for (int c = 0; c < bitsPerSample.length; c++) {
          for (int y = 0; y < dimensions[1]; y++) {
            for (int x = 0; x < dimensions[0]; x++) {
              flatSamples[c][x + y*dimensions[0]] =
                bb.getBits(bitsPerSample[c]);
            }
          }
        }
      }
      frameField = new FlatField(funcRowColRGB, pixelSet);
      frameField.setSamples(flatSamples);
    }
    else if (photoint == 1) {
      int bitsPerSample = TiffTools.getIFDValue(ifdEntries, 258);
      BitBuffer bb = new BitBuffer(new ByteArrayInputStream(imageData));
      // could be broken, assumes 8 bit depth.
      float[][] flatSamples = new float[1][dimensions[0] * dimensions[1]];
      if (TiffTools.getIFDValue(ifdEntries, 317) == 2) { // use differencing
        int currentVal, currentByte = 0;
        for (int y = 0; y < dimensions[1]; y++) {
          currentVal = 0;
          for (int x = 0; x < dimensions[0]; x++) {
            currentVal += imageData[currentByte];
            currentVal = currentVal % 256;
            if (currentVal < 0) {
              currentVal += 256;
            }
            flatSamples[0][x + y*dimensions[0]] = currentVal;
            currentByte++;
          }
        }
      }
      else {
        for (int y = 0; y < dimensions[1]; y++) {
          for (int x = 0; x < dimensions[0]; x++) {
            flatSamples[0][x + y*dimensions[0]] = bb.getBits(bitsPerSample);
          }
        }
      }
      if (maxChannels == 1) {
        frameField = new FlatField(funcRowColPix, pixelSet);
        frameField.setSamples(flatSamples, false);
      }
      else {
        float[][] flatSamp = new float[3][];
        flatSamp[0] = flatSamp[1] = flatSamp[2] = flatSamples[0];
        frameField = new FlatField(funcRowColRGB, pixelSet);
        frameField.setSamples(flatSamp, false);
      }
    }
    else {
      throw new BadFormException("Invalid Photometric Interpretation");
    }

    return frameField;
  }

  /** Returns the number of frames in the specified Zeiss file. */
  public int getBlockCount(String id)
    throws BadFormException, IOException, VisADException
  {
    if (id != currentId) initFile(id);
    return dimensions[2];
  }

  /** Closes the current form. */
  public void close() {
    try {
      if (r == null) {
        return;
      }
      r.close();
      r = null;
    }
    catch (Exception e) {
      e.printStackTrace();
    }
  }


  // -- FormFileInformer methods --

  /** Checks if the given string is a valid filename for a Zeiss File */
  public boolean isThisType(String name) {
    return (name.toLowerCase().endsWith(".lsm"));
  }

  /** Checks if the given block is a valid header for a Zeiss file. */
  public boolean isThisType(byte[] block) {
    if (block.length < 3) { return false; }
    if (block[0] != 73) { return false; } // denotes little-endian
    if (block[1] != 73) { return false; }
    if (block[2] != 42) { return false; } // denotes tiff
    if (block.length < 8) { return true; } // we have no way of verifying
    int ifdlocation = batoi(new byte[] {
      block[4], block[5], block[6], block[7]
    });
    if (ifdlocation + 1 > block.length) {
      // no way of verifying this is a Zeiss file; it is at least a tiff
      return true;
    }
    else {
      int ifdnumber = batoi(new byte[] {
        block[ifdlocation], block[ifdlocation + 1]
      });
      for (int i = 0; i < ifdnumber; i++) {
        if (ifdlocation + 3 + (i * 12) > block.length) {
          return true;
        }
        else {
          int ifdtag = batoi(new byte[] {
            block[ifdlocation + 2 + (i * 12)],
            block[ifdlocation + 3 + (i * 12)]
          });
          if (ifdtag == 33412) { // 33412 appears to be the Zeiss tag.
            return true; // absolutely a valid file
          }
        }
      }
      return false; // we went through the IFD, the ID wasn't found.
    }
  }

  /** Returns the default file suffixes for the Zeiss file format. */
  public String[] getDefaultSuffixes() {
    return new String[] {"lsm"};
  }


  // -- FormProgressInformer methods --

  /**
   * Gets the percentage complete of the form's current operation.
   * @return the percentage complete (0.0 - 100.0), or Double.NaN
   *         if no operation is currently taking place
   */
  public double getPercentComplete() { return percent; }


  // -- MetadataReader API methods --


  /** Creates a hashtable containing metadata info from the file. */
  public Hashtable getMetadata(String id)
    throws BadFormException, IOException, VisADException
  {
    if (id != currentId) initFile(id);
    // TODO
    return new Hashtable();
  }

  /** Returns a single Metadata value from the file. */
  public Object getMetadataValue(String id, String field)
    throws BadFormException, IOException, VisADException
  {
    Hashtable h = getMetadata(id);
    try {
      return h.get(field);
    }
    catch (NullPointerException e) {
      return null;
    }
  }


  // -- Helper methods --

  private void initFile(String id)
    throws IOException, VisADException, BadFormException
  {
    r = new RandomAccessFile(id, "r");
    currentId = id;
    dimensions = TiffTools.getTIFFDimensions(r);
    // System.out.println(dimensions[0] + " x " +
    //   dimensions[1] + " x " + dimensions[2]);
    pixelSet = new Linear2DSet(domainTuple, 0, dimensions[0] - 1,
      dimensions[0], dimensions[1] - 1, 0, dimensions[1]);

    Hashtable temp = new Hashtable();
    maxChannels = 0;
    int[] tempArray;
    int comp;
    actualImages = new int[dimensions[2]];
    int imageNum = 0;
    for (int i = 0; i < dimensions[2]; i++) {
      temp = TiffTools.getIFDHash(r, i);
      if (TiffTools.getIFDValue(temp, 254) == 0) {
        actualImages[imageNum] = i;
        imageNum++;
      }
      tempArray =
          TiffTools.getIFDArray(r, (Vector) temp.get(new Integer(258)));
      comp = TiffTools.getIFDValue(temp, 259);
      if (comp != 1) {
        // System.out.println(" Compression used: " + comp);
      }
      if (tempArray.length > maxChannels) {
        maxChannels = tempArray.length;
      }
    }
    dimensions[2] = imageNum;
  }

  /** Translates up to the first 4 bytes of a byte array to an integer. */
  private static int batoi(byte[] inp) {
    int len = inp.length>4?4:inp.length;
    int total = 0;
    for (int i = 0; i < len; i++) {
      total = total + ((inp[i]<0?(int)256+inp[i]:(int)inp[i]) << (i * 8));
    }
    return total;
  }


  // -- Main method --

  /**
   * Run 'java visad.data.bio.ZeissForm in_file'
   * to test read a Zeiss LSM data file.
   */
  public static void main(String[] args)
    throws VisADException, IOException
  {
    if (args == null || args.length < 1) {
      System.out.println("To test read a Zeiss LSM file, run:");
      System.out.println("  java visad.data.bio.ZeissForm in_file");
      System.exit(2);
    }

    // Test read Zeiss LSM file
    ZeissForm form = new ZeissForm();
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
