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

// Heavily adapted from MetamorphForm.java

package visad.data.bio;

import visad.*;
import visad.data.*;
import visad.data.tiff.*;
import java.io.*;
import java.net.URL;
import java.util.*;

/** ZeissForm is the VisAD data adapter used by Zeiss files. */
public class ZeissForm extends Form
  implements FormFileInformer, FormBlockReader, MetadataReader
{

  /** Form instantiation counter. */
  private static int num = 0;

  // VisAD objects
  private RealType frame, row, column, pixel, red, green, blue;
  private FunctionType funcRowColPix, funcRowColRGB;
  private FunctionType funcTimeRange;
  private RealTupleType domainTuple, rgbTuple;
  private visad.Set pixelSet;
  private visad.Set timeSet;
  private FlatField frameField;
  private FieldImpl timeField;

  // contains currently referenced file information.
  private RandomAccessFile r;
  private String currentId;
  private int dimensions[];
  private int maxchannels;
  private int offsets[];
  private int actualimages[];


  // -- Constructor --

  /** Constructs a new Zeiss file form. */
  public ZeissForm() {
    super("ZeissForm" + num++);
    try {
      frame = RealType.getRealType("frame");
      row = RealType.getRealType("ImageElement");
      column = RealType.getRealType("ImageLine");
      domainTuple = new RealTupleType(row, column);

      // For grayscale images
      pixel = RealType.getRealType("pixel");
      funcRowColPix = new FunctionType(domainTuple, pixel);

      // For color images
      RealTupleType rgbPixelData = new RealTupleType(new RealType[] {
        RealType.getRealType("Red"),
        RealType.getRealType("Green"),
        RealType.getRealType("Blue")
      });
      funcRowColRGB = new FunctionType(domainTuple, rgbPixelData);
      funcTimeRange = new FunctionType(frame, funcRowColPix);
    }
    catch (Exception e) { // Should not happen, but is required.
      e.printStackTrace();
    }
  }

  private void initFile(String id)
    throws IOException, VisADException, BadFormException {
    r = new RandomAccessFile(id, "r");
    currentId = id;
    dimensions = TiffTools.getTIFFDimensions(r);
    // System.out.println(dimensions[0] + " x " +
    //   dimensions[1] + " x " + dimensions[2]);
    pixelSet = new Linear2DSet(domainTuple, 0, dimensions[0] - 1,
      dimensions[0], dimensions[1] - 1, 0, dimensions[1]);
    timeSet = new Integer1DSet(frame,  dimensions[2]);
    frameField = new FlatField(funcRowColPix, pixelSet);
    timeField = new FieldImpl(funcTimeRange, timeSet);
    Hashtable temp = new Hashtable();
    maxchannels = 0;
    int[] tempia;
    int comp;
    actualimages = new int[dimensions[2]];
    int imagenum = 0;
    for (int i = 0; i < dimensions[2]; i++) {
      temp = TiffTools.getIFDHash(r, i);
      if (TiffTools.getIFDValue(temp, 254) == 0) {
        actualimages[imagenum] = i;
        imagenum++;
      }
      tempia =
          TiffTools.getIFDArray(r, (Vector) temp.get(new Integer(258)));
      comp = TiffTools.getIFDValue(temp, 259);
      if (comp != 1) {
        // System.out.println(" Compression used: " + comp);
      }
      if (tempia.length > maxchannels) {
        maxchannels = tempia.length;
      }
    }
    dimensions[2] = imagenum;

  }

// -- FormFileInformer methods --

  /** Checks if the given string is a valid filename for a Zeiss File */
  public boolean isThisType(String name) {
    return (name.toLowerCase().endsWith(".lsm"));
  }

  /** Checks if the given block is a valid header for a Zeiss file.
   */

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
    return new String[] {"lsm", ""};
  }

  // -- API methods --

  /**
   * Saves a VisAD Data object to Zeiss
   * format at the given location.
   */
  public void save(String id, Data data, boolean replace)
    throws UnimplementedException
  {
    throw new UnimplementedException(); // This is not implemented
  }

  /**
   * Adds data to an existing Zeiss file.
   *
   * @exception BadFormException Always thrown (this method not implemented).
   */
  public void add(String id, Data data, boolean replace)
    throws BadFormException
  {
    throw new BadFormException("ZeissForm.add");
  }

  /**
   * Opens an existing Zeiss file from the given location.
   *
   * @return VisAD Data object containing Zeiss data.
   */
  public DataImpl open(String id)
    throws BadFormException, IOException, VisADException
  {

    if (id != currentId) {
      initFile(id);
    }

    // cycle through all frames, get each frame, add it to the timeField.
    for (int z = 0; z < dimensions[2]; z++) {
      frameField = (FlatField) open(id, z);
      timeField.setSample(z, frameField);
    }
    return timeField;
  }

  private static int batoi (byte[] inp) {
    /* Translates up to the first 4 bytes of a byte array to an integer */
    int len = inp.length>4?4:inp.length;
    int total = 0;
    for (int i = 0; i < len; i++) {
      total = total + ((inp[i]<0?(int)256+inp[i]:(int)inp[i]) << (i * 8));
    }
    return total;
  }

  /** Gets Forms(?)
   *  @return Always returns null
   */
  public FormNode getForms(Data data) {
    return null;
  }

  /**
   * Opens an existing Zeiss file from the given URL.
   *
   * @return VisAD Data object containing Zeiss data.
   * @exception UnimplementedException Always thrown (this method not
   * implemented).
   */
  public DataImpl open(URL url)
    throws UnimplementedException
  {
    throw new UnimplementedException(); // This is not implemented
  }


  // -- FormBlockReader methods --

  /** Opens the Zeiss file with the file name specified
   *  by id, retrieving only the frame number given.
   *  @return a DataImpl containing the specified frame
   */

  public DataImpl open(String id, int block_number)
    throws BadFormException, IOException, VisADException
  {

    if (id != currentId) {
      initFile(id);
    }

    Hashtable ifdEntries = TiffTools.getIFDHash(r, actualimages[block_number]);
    Vector entrydata;
    byte[] bytearray;
    int stripoffsets, stripoffsetcount, stripbytes;
    float[] toreturn;

    // This is the directory entry for strip offsets
    entrydata = (Vector) ifdEntries.get(new Integer(273));
    stripoffsetcount = ((Integer) entrydata.get(1)).intValue();
    stripoffsets = ((Integer) entrydata.get(2)).intValue();
    // This is the directory entry for strip bytes
    entrydata = (Vector) ifdEntries.get(new Integer(279));
    stripbytes = ((Integer) entrydata.get(2)).intValue();
    int[][] stripinfo = new int[stripoffsetcount][2];
    int current;
    int total = 0;

    // If there is only one strip offset in the IFD, it will contain the data
    // itself.
    if (stripoffsetcount == 1) {
      stripinfo[0][0] = stripoffsets;
      stripinfo[0][1] = stripbytes;
      total = stripbytes;
    }
    else {
      // Otherwise, it will contain a pointer, and we need to read the data out
      r.seek(stripoffsets);
      bytearray = new byte[4];
      for(int i = 0; i < stripoffsetcount; i++) {
        r.read(bytearray);
        stripinfo[i][0] = batoi(bytearray);
      }
      r.seek(stripbytes);
      for(int i = 0; i < stripoffsetcount; i++) {
        r.read(bytearray);
        current = batoi(bytearray);
        stripinfo[i][1] = current;
        total += current;
      }
    }
    // Then, create the array to return, and read the data in from the
    // file.
    byte[] imagedata = new byte[total];
    current = 0;
    //System.out.println(" Image Number: " + block_number);
    for(int i = 0; i < stripoffsetcount; i++) {
      r.seek(stripinfo[i][0]);
      //System.out.println("   Reading from: " + r.getFilePointer());
      bytearray = new byte[stripinfo[i][1]];
      r.read(bytearray);
      // System.out.println("   Read " + bytearray.length + " bytes");
      if (TiffTools.getIFDValue(ifdEntries, 259) == 5) {
        bytearray = TiffTools.lzwUncompress(bytearray);
      }
      System.arraycopy(bytearray, 0, imagedata, current, bytearray.length);
      current += bytearray.length;
      // read reads in as bytes (signed), we need them as unsigned floats.
    }

    entrydata = (Vector) ifdEntries.get(new Integer(262));
    int photoint = ((Integer) entrydata.get(2)).intValue();
    current = 0;
    //System.out.println(" photoint: " + photoint);
    if (photoint == 2) {
      int[] bitspersample =
         TiffTools.getIFDArray(r, (Vector) ifdEntries.get(new Integer(258)));
      BitBuffer bb = new BitBuffer(new ByteArrayInputStream(imagedata));

      float[][] flatsamples =
        new float[bitspersample.length][dimensions[0] * dimensions[1]];
      if (TiffTools.getIFDValue(ifdEntries, 317) == 2) { // use differencing
        int currentval, currentbyte = 0;
        for (int c = 0; c < bitspersample.length; c++) {
          for (int y = 0; y < dimensions[1]; y++) {
            currentval = 0;
            for (int x = 0; x < dimensions[0]; x++) {
              if (bitspersample[c] > 0) {
                currentval += imagedata[currentbyte];
                currentval = currentval % 256;
                if (currentval < 0) {
                  currentval += 256;
                }
                flatsamples[c][x + y*dimensions[0]] = currentval;
                currentbyte++;
              }
              else {
                flatsamples[c][x + y*dimensions[0]] = 0;
              }
            }
          }
        }
      }
      else {
        for (int c = 0; c < bitspersample.length; c++) {
          for (int y = 0; y < dimensions[1]; y++) {
            for (int x = 0; x < dimensions[0]; x++) {
              flatsamples[c][x + y*dimensions[0]] =
                bb.getBits(bitspersample[c]);
            }
          }
        }
      }
      frameField = new FlatField(funcRowColRGB, pixelSet);
      frameField.setSamples(flatsamples);
    }
    else if (photoint == 1) {
      int bitspersample = TiffTools.getIFDValue(ifdEntries, 258);
      BitBuffer bb = new BitBuffer(new ByteArrayInputStream(imagedata));
      // could be broken, assumes 8 bit depth.
      float[][] flatsamples = new float[1][dimensions[0] * dimensions[1]];
      if (TiffTools.getIFDValue(ifdEntries, 317) == 2) { // use differencing
        int currentval, currentbyte = 0;
        for (int y = 0; y < dimensions[1]; y++) {
          currentval = 0;
          for (int x = 0; x < dimensions[0]; x++) {
            currentval += imagedata[currentbyte];
            currentval = currentval % 256;
            if (currentval < 0) {
              currentval += 256;
            }
            flatsamples[0][x + y*dimensions[0]] = currentval;
            currentbyte++;
          }
        }
      }
      else {
        for (int y = 0; y < dimensions[1]; y++) {
          for (int x = 0; x < dimensions[0]; x++) {
            flatsamples[0][x + y*dimensions[0]] = bb.getBits(bitspersample);
          }
        }
      }
      if (maxchannels == 1) {
        frameField = new FlatField(funcRowColPix, pixelSet);
        frameField.setSamples(flatsamples, false);
      }
      else {
        float[][] flatsamp = new float[3][];
        flatsamp[0] = flatsamp[1] = flatsamp[2] = flatsamples[0];
        frameField = new FlatField(funcRowColRGB, pixelSet);
        frameField.setSamples(flatsamp, false);
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
    if (id != currentId) {
      initFile(id);
    }
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

  /** Creates a hashtable containing metadata info from the file. */
  public Hashtable getMetadata(String id)
    throws BadFormException, IOException, VisADException
  {
    Hashtable metadata = new Hashtable();
    if (id != currentId) {
      initFile(id);
    }

    // TODO

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


  // -- Main method --

  public static void main(String[] args) throws Exception {
    ZeissForm reader = new ZeissForm();
    System.out.println("Opening " + args[0] + "...");
    Data d = reader.open(args[0]);
    System.out.println(d.getType());
    System.out.println();
    System.out.println("Reading metadata pairs...");
    Hashtable metadata = reader.getMetadata(args[0]);
    Enumeration e = metadata.keys();
    while (e.hasMoreElements()) {
      String key = (String) e.nextElement();
      System.out.println(key + " = " + metadata.get(key));
    }
    /*
    RandomAccessFile raf = new RandomAccessFile(args[0], "r");
    int blah[] = TiffTools.getTIFFDimensions(raf);
    for (int i = 0; i < blah.length; i++) {
      //System.out.println(blah[i] + " ");
    }
    //System.out.println("photo interp: " +
    //  TiffTools.getPhotometricInterpretation(raf));
    */
  }

}
