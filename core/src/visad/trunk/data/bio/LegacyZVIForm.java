//
// LegacyZVIForm.java
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
import visad.data.tiff.*;

/**
 * LegacyZVIForm is the VisAD data adapter for reading Zeiss ZVI files.
 * @author Curtis Rueden ctrueden at wisc.edu
 * @author Melissa Linkert linkert at cs.wisc.edu
 */
public class LegacyZVIForm extends Form implements
  FormBlockReader, FormFileInformer, FormProgressInformer, MetadataReader
{

  // -- Constants --

  /** First few bytes of every ZVI file. */
  private static final byte[] ZVI_SIG = {
    -48, -49, 17, -32, -95, -79, 26, -31
  };

  /** Block identifying start of useful header information. */
  private static final byte[] ZVI_MAGIC_BLOCK_1 = { // 41 00 10
    65, 0, 16
  };

  /** Block identifying second part of useful header information. */
  private static final byte[] ZVI_MAGIC_BLOCK_2 = { // 41 00 80
    65, 0, -128
  };

  /** Block identifying third part of useful header information. */
  private static final byte[] ZVI_MAGIC_BLOCK_3 = { // 20 00 10
    32, 0, 16
  };

  /** Memory buffer size in bytes, for reading from disk. */
  private static final int BUFFER_SIZE = 8192;

  /** String apologizing for the fact that this ZVI support still sucks. */
  private static final String WHINING = "Sorry, " +
    "ZVI support is still preliminary. It will be improved as time permits.";

  /** Debugging flag. */
  private static final boolean DEBUG = false;


  // -- Static fields --

  /** Form instantiation counter. */
  private static int formCount = 0;


  // -- Fields --

  /** Filename of current file. */
  private String currentId;

  /** Input stream for current file. */
  private RandomAccessFile in;

  /** List of image blocks. */
  private Vector blockList;

  /** Percent complete with current operation. */
  private double percent;


  // -- Constructor --

  /** Constructs a new ZVI file form. */
  public LegacyZVIForm() {
    super("LegacyZVIForm" + formCount++);
  }


  // -- FormNode API methods --

  /** Saves a VisAD Data object at the given location. */
  public void save(String id, Data data, boolean replace)
    throws UnimplementedException
  {
    throw new UnimplementedException("LegacyZVIForm.save");
  }

  /**
   * Adds data to an existing file.
   *
   * @throws BadFormException Always thrown (not supported).
   */
  public void add(String id, Data data, boolean replace)
    throws BadFormException
  {
    throw new BadFormException("LegacyZVIForm.add");
  }

  /**
   * Opens an existing file from the given location.
   *
   * @return VisAD Data object.
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

    readMetadata(id);
    close();
    percent = -1;
    return data;
  }

  public FormNode getForms(Data data) {
    return null;
  }

  /**
   * Opens an existing Openlab file from the given URL.
   *
   * @return VisAD Data object containing Openlab data.
   * @throws BadFormException Always thrown (not supported).
   */
  public DataImpl open(URL url)
    throws BadFormException, VisADException, IOException
  {
    throw new BadFormException("LegacyZVIForm.open(URL)");
  }


  // -- FormBlockReader API methods --

  /**
   * Obtains the specified block from the given file.
   * @param id The file from which to load data blocks.
   * @param blockNumber The block number of the block to load.
   * @throws VisADException If the block number is invalid.
   */
  public DataImpl open(String id, int blockNumber)
    throws BadFormException, IOException, VisADException
  {
    if (!id.equals(currentId)) initFile(id);

    if (blockNumber < 0 || blockNumber >= blockList.size()) {
      throw new BadFormException("Invalid image number: " + blockNumber);
    }

    if (DEBUG) System.out.println("Reading image #" + blockNumber + "...");

    ZVIBlock zviBlock = (ZVIBlock) blockList.elementAt(blockNumber);
    return zviBlock.readImage(in);
  }

  /**
   * Determines the number of blocks in the given file.
   * @param id The file for which to get a block count.
   */
  public int getBlockCount(String id)
    throws BadFormException, IOException, VisADException
  {
    if (!id.equals(currentId)) initFile(id);
    return blockList.size();
  }

  /** Closes any open files. */
  public void close() throws BadFormException, IOException, VisADException {
    if (currentId == null) return;
    in.close();
    currentId = null;
    in = null;
  }


  // -- FormFileInformer API methods --

  /** Checks if the given string is a valid filename. */
  public boolean isThisType(String name) {
    return name.toLowerCase().endsWith(".zvi");
  }

  /** Checks if the given block is a valid header. */
  public boolean isThisType(byte[] block) {
    if (block == null) return false;
    int len = block.length < ZVI_SIG.length ? block.length : ZVI_SIG.length;
    for (int i=0; i<len; i++) {
      if (block[i] != ZVI_SIG[i]) return false;
    }
    return true;
  }

  /** Returns the default file suffixes. */
  public String[] getDefaultSuffixes() {
    return new String[] {"zvi"};
  }


  // -- FormProgressInformer methods --

  /**
   * Gets the percentage complete of the form's current operation.
   * @return the percentage complete (0.0 - 100.0), or Double.NaN
   *         if no operation is currently taking place
   */
  public double getPercentComplete() { return percent; }


  // -- MetadataReader methods --

  /**
   * Takes a string containing the filename and read relevant metadata
   * into a hashtable
   *
   * @param String the filename
   * @return Hashtable containing metadata from the image header
   */
  public Hashtable getMetadata(String id) throws
    IOException, VisADException, BadFormException
  {
    // this one is handled differently than other getMetadata methods
    // in the visad.data.bio package

    // Since the ZVI specs are pretty much worthless, metadata support is
    // primitive at best.

    RandomAccessFile infile = new RandomAccessFile(id, "r");
    Hashtable metadata = new Hashtable();

    byte[] r = new byte[4];
    byte[] dataType = new byte[4];
    byte[] tagData; // not sure yet how big to make this

    String type = "";
    boolean ok = true;
    long pos = 0;
    Vector blocklist = new Vector();

    // find each of the "magic blocks", after which we can read header info
    while (true) {
      long magic1 = findBlock(infile, ZVI_MAGIC_BLOCK_1, pos);
      if (magic1 < 0) break;
      pos = magic1 + ZVI_MAGIC_BLOCK_1.length;

      infile.skipBytes(19);
      pos += 19;

      infile.read(new byte[ZVI_MAGIC_BLOCK_2.length]);
      pos += ZVI_MAGIC_BLOCK_2.length;
      infile.read(new byte[131]);
      pos += 131;

      long magic3 = findBlock(infile, ZVI_MAGIC_BLOCK_3, pos);
      if (magic3 < 0) {
        throw new BadFormException("Error parsing image header. " + WHINING);
      }
      pos = magic3 + ZVI_MAGIC_BLOCK_3.length;

      int width = readInt(infile);
      int height = readInt(infile);
      int alwaysOne = readInt(infile); //depth--not used
      int pixelType = readInt(infile);
      int bytesPerPixel = readInt(infile);
      int bitDepth = readInt(infile); // doesn't always equal bytesPerPixel * 8
      pos += 24;

      switch (pixelType) {
        case 1: type = "8 bit rgb tuple, 24 bpp"; break;
        case 2: type = "8 bit rgb quad, 32 bpp"; break;
        case 3: type = "8 bit grayscale"; break;
        case 4: type = "16 bit signed int, 8 bpp"; break;
        case 5: type = "32 bit int, 32 bpp"; break;
        case 6: type = "32 bit float, 32 bpp"; break;
        case 7: type = "64 bit float, 64 bpp"; break;
        case 8: type = "16 bit unsigned short triple, 48 bpp"; break;
        case 9: type = "32 bit int triple, 96 bpp"; break;
        default: type = "undefined pixel type"; System.out.println(pixelType);
      }

      metadata.put("Width", new Integer(width));
      metadata.put("Height", new Integer(height));
      metadata.put("PixelType", type);
      metadata.put("BPP", new Integer(bytesPerPixel));
      blocklist.add(new Long(pos));
      pos += width * height * bytesPerPixel;
    }

    // trying to find the tags stream

    return metadata;
  }

  public Object getMetadataValue(String id1, String id2) throws
    IOException, VisADException, BadFormException
  {
    Hashtable h = new Hashtable();
    h = getMetadata(id1);
    try {
      return h.get(id2);
    }
    catch (NullPointerException e) {
      return null;
    }
  }

  public void readMetadata(String id) throws
    VisADException, IOException
  {
    System.out.println("Reading the metadata: ");
    Hashtable metadata = getMetadata(id);
    String[] names = {"Width", "Height", "PixelType", "BPP"};

    for (int j=0; j < names.length; j++) {
      System.out.println(names[j] + " ");
      System.out.println(metadata.get((Object) names[j]));
    }
  }


  // -- Utility methods --

  /**
   * Translates up to the first 4 bytes of a
   * little-endian byte array to an integer.
   */
  private static int batoi(byte[] b) {
    int len = b.length > 4 ? 4 : b.length;
    int total = 0;
    for (int i = 0; i < len; i++) {
      int q = b[i] < 0 ? b[i] + 256 : b[i]; // convert to unsigned
      int shift = 8 * i; // little endian
      total += q << shift;
    }
    return total;
  }

  /** Reads a little-endian integer from the given file. */
  private static int readInt(RandomAccessFile fin) throws IOException {
    byte[] b = new byte[4];
    fin.readFully(b);
    return batoi(b);
  }

  /**
   * Finds the first occurrence of the given byte block within the file,
   * starting from the given file position.
   */
  private static long findBlock(RandomAccessFile in, byte[] block, long start)
    throws IOException
  {
    long filePos = start;
    long fileSize = in.length();
    byte[] buf = new byte[BUFFER_SIZE];
    long spot = -1;
    int step = 0;
    boolean found = false;
    in.seek(start);

    while (true) {
      int len = (int) (fileSize - filePos);
      if (len < 0) break;
      if (len > buf.length) len = buf.length;
      in.readFully(buf, 0, len);

      for (int i=0; i<len; i++) {
        if (buf[i] == block[step]) {
          if (step == 0) {
            // could be a match; flag this spot
            spot = filePos + i;
          }
          step++;
          if (step == block.length) {
            // found complete match; done searching
            found = true;
            break;
          }
        }
        else {
          // no match; reset step indicator
          spot = -1;
          step = 0;
        }
      }
      if (found) break; // found a match; we're done
      if (len < buf.length) break; // EOF reached; we're done

      filePos += len;
    }

    // set file pointer to byte immediately following pattern
    if (spot >= 0) in.seek(spot + block.length);

    return spot;
  }


  // -- Helper methods --

  /** Reads header information from the given file. */
  private void initFile(String id) throws IOException, VisADException {
    // close any currently open files
    close();

    currentId = id;
    in = new RandomAccessFile(id, "r");

    // Highly questionable decoding strategy:
    //
    // Note that all byte ordering is little endian, including 4-byte header
    // fields. Other examples: 16-bit data is LSB MSB, and 3-channel data is
    // BGR instead of RGB.
    //
    // 1) Find image header byte sequence:
    //    A) Find 41 00 10. (ZVI_MAGIC_BLOCK_1)
    //    B) Skip 19 bytes of stuff.
    //    C) Read 41 00 80. (ZVI_MAGIC_BLOCK_2)
    //    D) Read 11 bytes of 00.
    //    E) Read potential header information:
    //       - Z-slice (4 bytes)
    //       - channel (4 bytes)
    //       - timestep (4 bytes)
    //    F) Read 108 bytes of 00.
    //
    // 2) If byte sequence is not as expected at any point (e.g.,
    //    stuff that is supposed to be 00 isn't), start over at 1A.
    //
    // 3) Find 20 00 10. (ZVI_MAGIC_BLOCK_3)
    //
    // 4) Read more header information:
    //    - width (4 bytes)
    //    - height (4 bytes)
    //    - ? (4 bytes; always 1)
    //    - bytesPerPixel (4 bytes)
    //    - pixelType (this is what the AxioVision software calls it)
    //       - 1=24-bit (3 color components, 8-bit each)
    //       - 3=8-bit (1 color component, 8-bit)
    //       - 4=16-bit (1 color component, 16-bit)
    //    - bitDepth (4 bytes--usually, but not always, bytesPerPixel * 8)
    //
    // 5) Read image data (width * height * bytesPerPixel)
    //
    // 6) Repeat the entire process until no more headers are identified.

    long pos = 0;
    blockList = new Vector();
    int numZ = 0, numC = 0, numT = 0;
    while (true) {
      // search for start of next image header
      long header = findBlock(in, ZVI_MAGIC_BLOCK_1, pos);

      if (header < 0) {
        // no more potential headers found; we're done
        break;
      }
      pos = header + ZVI_MAGIC_BLOCK_1.length;

      if (DEBUG) System.err.println("Found potential image block: " + header);

      // these bytes don't matter
      in.skipBytes(19);
      pos += 19;

      // these bytes should match ZVI_MAGIC_BLOCK_2
      byte[] b = new byte[ZVI_MAGIC_BLOCK_2.length];
      in.readFully(b);
      boolean ok = true;
      for (int i=0; i<b.length; i++) {
        if (b[i] != ZVI_MAGIC_BLOCK_2[i]) {
          ok = false;
          break;
        }
        pos++;
      }
      if (!ok) continue;

      // these bytes should be 00
      b = new byte[11];
      in.readFully(b);
      for (int i=0; i<b.length; i++) {
        if (b[i] != 0) {
          ok = false;
          break;
        }
        pos++;
      }
      if (!ok) continue;

      // read potential header information
      int theZ = readInt(in);
      int theC = readInt(in);
      int theT = readInt(in);
      pos += 12;

      // these bytes should be 00
      b = new byte[108];
      in.readFully(b);
      for (int i=0; i<b.length; i++) {
        if (b[i] != 0) {
          ok = false;
          break;
        }
        pos++;
      }
      if (!ok) continue;

      // everything checks out; looks like an image header to me
      long magic3 = findBlock(in, ZVI_MAGIC_BLOCK_3, pos);
      if (magic3 < 0) {
        throw new BadFormException("Error parsing image header. " + WHINING);
      }
      pos = magic3 + ZVI_MAGIC_BLOCK_3.length;

      // read more header information
      int width = readInt(in);
      int height = readInt(in);
      int alwaysOne = readInt(in); // don't know what this is for
      int bytesPerPixel = readInt(in);
      int pixelType = readInt(in); // not clear what this value signifies
      int bitDepth = readInt(in); // doesn't always equal bytesPerPixel * 8
      pos += 24;

      ZVIBlock zviBlock = new ZVIBlock(theZ, theC, theT,
        width, height, alwaysOne, bytesPerPixel, pixelType, bitDepth, pos);
      if (DEBUG) System.out.println(zviBlock);

      // perform some checks on the header info
      if (theZ >= numZ) numZ = theZ + 1;
      if (theC >= numC) numC = theC + 1;
      if (theT >= numT) numT = theT + 1;

      // save this image block's position
      blockList.add(zviBlock);
      pos += width * height * bytesPerPixel;
    }

    if (blockList.isEmpty()) {
      throw new BadFormException("No image data found. " + WHINING);
    }
    if (numZ * numC * numT != blockList.size()) {
      System.err.println("Warning: image counts do not match. " + WHINING);
    }
  }


  // -- Helper classes --

  /** Contains information collected from a ZVI image header. */
  private class ZVIBlock {
    private int theZ, theC, theT;
    private int width, height;
    private int alwaysOne;
    private int bytesPerPixel;
    private int pixelType;
    private int bitDepth;
    private long imagePos;

    private int numPixels;
    private int imageSize;
    private int numChannels;
    private int bytesPerChannel;

    public ZVIBlock(int theZ, int theC, int theT, int width, int height,
      int alwaysOne, int bytesPerPixel, int pixelType, int bitDepth,
      long imagePos)
    {
      this.theZ = theZ;
      this.theC = theC;
      this.theT = theT;
      this.width = width;
      this.height = height;
      this.alwaysOne = alwaysOne;
      this.bytesPerPixel = bytesPerPixel;
      this.pixelType = pixelType;
      this.bitDepth = bitDepth;
      this.imagePos = imagePos;

      numPixels = width * height;
      imageSize = numPixels * bytesPerPixel;
      numChannels = pixelType == 1 ? 3 : 1; // a total shot in the dark
      if (bytesPerPixel % numChannels != 0) {
        System.err.println("Warning: incompatible bytesPerPixel (" +
          bytesPerPixel + ") and numChannels (" + numChannels +
          "). Assuming grayscale data. " + WHINING);
        numChannels = 1;
      }
      bytesPerChannel = bytesPerPixel / numChannels;
    }

    /** Reads in this block's image data from the given file. */
    public FlatField readImage(RandomAccessFile in)
      throws IOException, VisADException
    {
      long fileSize = in.length();
      if (imagePos + imageSize > fileSize) {
        throw new BadFormException("File is not big enough to contain the " +
          "pixels (width=" + width + "; height=" + height +
          "; bytesPerPixel=" + bytesPerPixel + "; imagePos=" + imagePos +
          "; fileSize=" + fileSize + "). " + WHINING);
      }

      // read image (should be buffered, but I'm too lazy for now)
      byte[] imageBytes = new byte[imageSize];
      in.seek(imagePos);
      in.readFully(imageBytes);

      // convert image bytes into VisAD-compatible floating point values
      int index = 0;
      float[][] samples = new float[numChannels][numPixels];
      for (int i=0; i<numPixels; i++) {
        for (int c=numChannels-1; c>=0; c--) {
          byte[] b = new byte[bytesPerChannel];
          System.arraycopy(imageBytes, index, b, 0, bytesPerChannel);
          index += bytesPerChannel;
          samples[c][i] = batoi(b);
        }
      }

      // wrap image in a FlatField object
      RealType xtype = RealType.getRealType("ImageElement");
      RealType ytype = RealType.getRealType("ImageLine");
      RealTupleType xy = new RealTupleType(xtype, ytype);
      RealType[] rtypes;
      if (numChannels == 1) {
        rtypes = new RealType[] {
          RealType.getRealType("value")
        };
      }
      else if (numChannels == 3) {
        rtypes = new RealType[] {
          RealType.getRealType("Red"),
          RealType.getRealType("Green"),
          RealType.getRealType("Blue")
        };
      }
      else {
        rtypes = new RealType[numChannels];
        for (int i=0; i<numChannels; i++) {
          rtypes[i] = RealType.getRealType("value" + (i + 1));
        }
      }
      RealTupleType range = new RealTupleType(rtypes);
      FunctionType ftype = new FunctionType(xy, range);
      Integer2DSet fset = new Integer2DSet(xy, width, height);
      FlatField ff = new FlatField(ftype, fset);
      ff.setSamples(samples, false);

      return ff;
    }

    public String toString() {
      return "Image header block:\n" +
        "  theZ = " + theZ + "\n" +
        "  theC = " + theC + "\n" +
        "  theT = " + theT + "\n" +
        "  width = " + width + "\n" +
        "  height = " + height + "\n" +
        "  alwaysOne = " + alwaysOne + "\n" +
        "  bytesPerPixel = " + bytesPerPixel + "\n" +
        "  pixelType = " + pixelType + "\n" +
        "  bitDepth = " + bitDepth;
    }
  }


  // -- Main method --

  /**
   * Run 'java visad.data.bio.LegacyZVIForm in_file'
   * to test read a Zeiss ZVI data file.
   */
  public static void main(String[] args)
    throws VisADException, IOException
  {
    BaseTiffForm.testRead(new LegacyZVIForm(), "LegacyZVIForm", args);
  }

}
