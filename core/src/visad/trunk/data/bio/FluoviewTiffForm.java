//
// FluoviewTiffForm.java
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
import java.rmi.RemoteException;
import java.util.*;

import visad.*;
import visad.data.*;

/**
 * FluoviewTiffForm is the VisAD data format adapter for
 * Olympus Fluoview TIFF files.
 *
 * @author Eric Kjellman egkjellman@wisc.edu
 */
public class FluoviewTiffForm extends Form
  implements FormBlockReader, FormFileInformer, FormProgressInformer
{

  // -- Constants --

  /** Number of bytes to check for Fluoview header information. */
  private static final int BLOCK_CHECK_LEN = 16384;

  /** Number identifying a TIFF file. */
  private static final int TIFF_MAGIC_NUMBER = 42;

  /** String identifying a Fluoview file. */
  private static final String FLUOVIEW_MAGIC_STRING = "fluoview";

  /** A value that is impossible as an IFD number. */
  private static final int IMPOSSIBLE_IFD = 424242;


  // -- Static fields --

  /** Form instantiation counter. */
  private static int formCount = 0;

  /** Domain of 2-D image. */
  private static RealTupleType domainTuple;

  /** MathType of a 2-D image with a 1-D range. */
  private static FunctionType funcRowColPix;

  static {
    try {
      RealType column = RealType.getRealType("ImageElement");
      RealType row = RealType.getRealType("ImageLine");
      domainTuple = new RealTupleType(column, row);

      // for grayscale images
      RealType pixel = RealType.getRealType("intensity");
      funcRowColPix = new FunctionType(domainTuple, pixel);
    }
    catch (VisADException exc) {
      exc.printStackTrace();
    }
  }


  // -- Fields --

  /** Filename of current Fluoview TIFF. */
  private String currentId;

  /** Input stream for current Fluoview TIFF. */
  private RandomAccessFile readIn;

  /** Number of blocks for current Fluoview TIFF. */
  private int numBlocks;

  /** Domain set of current Fluoview TIFF. */
  private Linear2DSet pixelSet;

  /** Percent complete with current operation. */
  private double percent;


  // -- Constructor --

  /** Constructs a new FluoviewTiffForm file form. */
  public FluoviewTiffForm() {
    super("FluoviewTiffForm" + formCount++);
  }


  // -- FormNode API methods --

  /**
   * Saves a VisAD Data object to Fluoview TIFF
   * format at the given location.
   *
   * @exception UnimplementedException Always thrown (this method not
   * implemented).
   */
  public void save(String id, Data data, boolean replace)
    throws BadFormException, IOException, RemoteException, VisADException
  {
    throw new UnimplementedException("FluoviewTiffForm.save");
  }

  /**
   * Adds data to an existing Fluoview TIFF file.
   *
   * @exception BadFormException Always thrown (this method not implemented).
   */
  public void add(String id, Data data, boolean replace)
    throws BadFormException
  {
    throw new BadFormException("FluoviewTiffForm.add");
  }

  /**
   * Opens an existing Fluoview TIFF file from the given location.
   *
   * @return VisAD Data object containing Fluoview TIFF data.
   * @exception BadFormException
   * @exception IOException Thrown when the file does not exist or there is a
   *            read error
   * @exception VisADException Thrown when an error occurs in the VisAD library
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

  /**
   * Opens an existing Fluoview TIFF file from the given URL.
   *
   * @return VisAD Data object containing Fluoview TIFF data.
   * @exception UnimplementedException Always thrown (this method not
   * implemented).
   */
  public DataImpl open(URL url)
    throws BadFormException, IOException, VisADException
  {
    throw new UnimplementedException("FluoviewTiffForm.open(URL)");
  }

  /** Returns the data forms that are compatible with a data object. */
  public FormNode getForms(Data data) {
    return null;
  }


  // -- FormBlockReader methods --

  /**
   * Opens the Fluoview TIFF file with the file name specified
   * by id, retrieving only the frame number given.
   * @return a DataImpl containing the specified frame
   */
  public DataImpl open(String id, int blockNumber)
    throws BadFormException, IOException, VisADException
  {
    if (!id.equals(currentId)) initFile(id);
    FlatField frameField = new FlatField(funcRowColPix, pixelSet);
    float[][] samples = new float[1][];
    samples[0] = getFrame(blockNumber + 1);
    frameField.setSamples(samples);
    return frameField;
  }

  /** Returns the number of frames in the specified Fluoview TIFF file. */
  public int getBlockCount(String id)
    throws BadFormException, IOException, VisADException
  {
    if (!id.equals(currentId)) initFile(id);
    return numBlocks;
  }

  /** Closes the current form. */
  public void close() {
    try {
      readIn.close();
    }
    catch (Exception e) { }
  }


  // -- FormFileInformer methods --

  /** Checks if the given string is a valid filename for a Fluoview file. */
  public boolean isThisType(String name) {
    // 1 or 2 fs
    if (!(name.toLowerCase().endsWith(".tif")
       || name.toLowerCase().endsWith(".tiff"))) { return false; }
    long len = new File(name).length();
    int size = len < BLOCK_CHECK_LEN ? (int) len : BLOCK_CHECK_LEN;
    byte[] buf = new byte[size];
    try {
      FileInputStream fin = new FileInputStream(name);
      int r = 0;
      while(r < size) {
        r += fin.read(buf, r, size - r);
      }
      fin.close();
      return isThisType(buf);
    }
    catch (IOException e) {
      return false;
    }
  }

  /**
   * Checks if the given block is a valid header for a Fluoview .tif file.
   * If it is, it should have 42 for the 3rd byte, and contain the text
   * "fluoview"
   */
  public boolean isThisType(byte[] block) {
    if (block.length < 3 || block[2] != TIFF_MAGIC_NUMBER) { return false; }
    String test = new String(block);
    test = test.toLowerCase();
    return (test.indexOf(FLUOVIEW_MAGIC_STRING) != -1);
  }

  /** Returns the default file suffixes for the Fluoview TIFF file format. */
  public String[] getDefaultSuffixes() {
    return new String[] {"tif", "tiff"};
  }


  // -- FormProgressInformer methods --

  /** Returns the percentage complete in the current operation */
  public double getPercentComplete() { return percent; }


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
    readIn = new RandomAccessFile(id, "r");
    currentId = id;
    int[] dimensions = getFTIFFDimensions();
    numBlocks = dimensions[2];
    pixelSet = new Linear2DSet(domainTuple, 0, dimensions[0] - 1,
      dimensions[0], dimensions[1] - 1, 0, dimensions[1]);
  }

  /**
   * Returns a Hashtable containing all of the info from the first IFD of
   * the file.
   */
  private Hashtable getIFDHash(int framecount) throws IOException {
    byte[] byteArray = new byte[4];
    int nextOffset;
    readIn.seek(4);
    readIn.read(byteArray); // Gets the offset of the first IFD
//    readIn.seek(batoi(byteArray));
    nextOffset = batoi(byteArray);
//    byteArray = new byte[2];
    // Gets the number of directory entries in the IFD
//    readIn.read(byteArray);
    Hashtable ifdEntries = new Hashtable();
//    Integer numEntries = new Integer(batoi(byteArray));
    Integer entrytag, entrytype, entrycount, entryOffset;
    int frames = 1;
    int length, offset, numEntries;
    Vector entryData;

    while (nextOffset != 0 && frames != framecount) {
      frames++;
      readIn.seek(nextOffset);
      byteArray = new byte[2];
      readIn.read(byteArray); // Gets number of directory entries in the IFD
      numEntries = batoi(byteArray);
      readIn.skipBytes(12 * numEntries);
      byteArray = new byte[4];
      readIn.read(byteArray);
      nextOffset = batoi(byteArray);
    }

    readIn.seek(nextOffset);
    byteArray = new byte[2];
    readIn.read(byteArray); // Gets the number of directory entries in the IFD
    numEntries = batoi(byteArray);


    // Iterate through the directory entries
    for (int i = 0; i < numEntries; i++) {
      byteArray = new byte[2];
      readIn.read(byteArray); // Get the entry tag
      entrytag = new Integer(batoi(byteArray));
      readIn.read(byteArray); // Get the entry type
      entrytype = new Integer(batoi(byteArray));
      byteArray = new byte[4];
      // Get the number of entries this offset points to.
      readIn.read(byteArray);
      entrycount = new Integer(batoi(byteArray));
      readIn.read(byteArray); // Gets the offset for the entry
      entryOffset = new Integer(batoi(byteArray));
      // Adds the data to a vector, and then hashs it.
      entryData = new Vector();
      entryData.add(entrytype);
      entryData.add(entrycount);
      entryData.add(entryOffset);
      ifdEntries.put(entrytag, entryData);
    }
    readIn.read(byteArray);
    nextOffset = batoi(byteArray);
    ifdEntries.put(new Integer(IMPOSSIBLE_IFD), new Integer(nextOffset));
    // 424242 is not possible as an IFD ID number, which are 16 bit
    return ifdEntries;
  }

  /**
   * Returns the x, y, and z dimensions of the passed filename,
   * which is assumed to be a Fluoview TIFF. This method currently does
   * not check whether this is a Fluoview TIFF file.
   * This method does not handle URLs
   * @return an int[3] containing dimensions for the array, x, y, z
   * @throws IOException  if a file input or output exception occured
   */
  private int[] getFTIFFDimensions() throws IOException {
    // For this one, we're going to read the entire IFD, get the x and y
    // coordinates out of it, and then just pass through the other IFDs to get
    // z. It is conceivable that the various images are of different sizes,
    // but for now I'm going to assume that they are not.
    byte[] byteArray;
    int nextOffset;
    int numEntries;
    int frames = 1;
    Integer width, length;
    Vector entryData;
    Hashtable ifdEntries = getIFDHash(1);

    nextOffset = ((Integer)
      ifdEntries.get(new Integer(IMPOSSIBLE_IFD))).intValue();

    while (nextOffset != 0) {
      frames++;
      try {
        readIn.seek(nextOffset);
      }
      catch (Exception e) {
        e.printStackTrace();
      }
      byteArray = new byte[2];
      readIn.read(byteArray); // Get the number of directory entries in the IFD
      numEntries = batoi(byteArray);
      readIn.skipBytes(12 * numEntries);
      byteArray = new byte[4];
      readIn.read(byteArray);
      nextOffset = batoi(byteArray);
    }

    // This is the directory entry for width.
    entryData = (Vector) ifdEntries.get(new Integer(256));
    width = (Integer) entryData.get(2);
    // This is the directory entry for height.
    entryData = (Vector) ifdEntries.get(new Integer(257));
    length = (Integer) entryData.get(2);
    return new int[] {width.intValue(), length.intValue(), frames};
  }

  /**
   * Returns a float[] containing image data from a file,
   * which is assumed to be a Fluoview TIFF. This method currently does
   * not check whether this is a Fluoview TIFF file.
   * The calling method should know the dimensions of this file.
   * This method does not handle URLs
   * @param framecount   the number of the frame to retrieve.
   * @return a float[][] containing the file data.
   * @throws IOException  if a file input or output exception occured
   */
  private float[] getFrame(int framecount) throws IOException {
    Hashtable ifdEntries = getIFDHash(framecount);
    Vector entryData;
    byte[] byteArray;
    int stripOffsets, stripOffsetCount, stripBytes;
    float[] toReturn;

    // This is the directory entry for strip offsets
    entryData = (Vector) ifdEntries.get(new Integer(273));
    stripOffsetCount = ((Integer) entryData.get(1)).intValue();
    stripOffsets = ((Integer) entryData.get(2)).intValue();
    // This is the directory entry for strip bytes
    entryData = (Vector) ifdEntries.get(new Integer(279));
    stripBytes = ((Integer) entryData.get(2)).intValue();
    int[][] stripInfo = new int[stripOffsetCount][2];
    int current;
    int total = 0; //  /rude Java

    // If there is only one strip offset in the IFD, it will contain the data
    // itself.
    if (stripOffsetCount == 1) {
      stripInfo[0][0] = stripOffsets;
      stripInfo[0][1] = stripBytes;
      total = stripBytes;
    }
    else {
      // Otherwise, it will contain a pointer, and we need to read the data out
      readIn.seek(stripOffsets);
      byteArray = new byte[4];
      for(int i = 0; i < stripOffsetCount; i++) {
        readIn.read(byteArray);
        stripInfo[i][0] = batoi(byteArray);
      }
      readIn.seek(stripBytes);
      for(int i = 0; i < stripOffsetCount; i++) {
        readIn.read(byteArray);
        current = batoi(byteArray);
        stripInfo[i][1] = current;
        total += current;
      }
    }
    // Then, create the array to return, and read the data in from the file.
    toReturn = new float[total/2];
    current = 0;
    for(int i = 0; i < stripOffsetCount; i++) {
      readIn.seek(stripInfo[i][0]);
      byteArray = new byte[stripInfo[i][1]];
      readIn.read(byteArray);
      // read reads in as bytes (signed), we need them as unsigned floats.
      for(int j = 0; j < byteArray.length; j += 2) {
        toReturn[current] =
          (float) (byteArray[j]<0?(int)256+byteArray[j]:(int)byteArray[j]) +
          ((byteArray[j+1]<0?(int)256+byteArray[j+1]:(int)byteArray[j+1])<<8);
        current++;
      }
    }
    return toReturn;
  }


  // -- Main method --

  /**
   * Run 'java visad.data.bio.FluoviewTiffForm in_file'
   * to test read an Olympus Fluoview TIFF data file.
   */
  public static void main(String[] args)
    throws VisADException, RemoteException, IOException
  {
    if (args == null || args.length < 1) {
      System.out.println("To test read a Fluoview TIFF file, run:");
      System.out.println("  java visad.data.bio.FluoviewTiffForm in_file");
      System.exit(2);
    }

    // Test read Fluoview TIFF file
    FluoviewTiffForm form = new FluoviewTiffForm();
    System.out.print("Reading " + args[0] + " ");
    Data data = form.open(args[0]);
    System.out.println("[done]");
    System.out.println("MathType =\n" + data.getType());
    System.exit(0);
  }

}

// Add an initialize method which creates a list of all the IFD offsets
// Eventually should also return metadata with the FieldImpl
