//
// OpenlabForm.java
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

import java.awt.Dimension;
import java.io.*;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.*;

import visad.*;
import visad.data.*;
import visad.data.qt.*;
import visad.data.tiff.*;

/**
 * OpenlabForm is the VisAD data adapter used for Openlab LIFF files.
 * @author Eric Kjellman egkjellman at wisc.edu
 * @author Curtis Rueden ctrueden at wisc.edu
 */
public class OpenlabForm extends Form implements FormBlockReader,
  FormFileInformer, FormProgressInformer, MetadataReader
{

  // -- Static fields --

  /** Form instantiation counter. */
  private static int formCount = 0;

  /** Helper form foin.reading PICT data with QTJava library. */
  private static QTForm qtForm = new QTForm();

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

  /** Filename of current Openlab LIFF. */
  private String currentId;

  /** Input stream for current Openlab LIFF. */
  private RandomAccessFile in;

  /** Number of blocks for current Openlab LIFF. */
  private int numBlocks;

  /** Hashtable containing metadata for the current Openlab LIFF. */
  private Hashtable metadata;

  /** Offset for each block of current Openlab LIFF. */
  private int[] offsets;

  /** Image type for each block of current Openlab LIFF. */
  private int[] imageType;

  /** Indicates whether there is any color data in current Openlab LIFF. */
  private boolean isColor;

  /** Percent complete with current operation. */
  private double percent;


  // -- Constructor --

  /** Constructs a new Openlab file form. */
  public OpenlabForm() {
    super("OpenlabForm" + formCount++);
  }


  // -- FormFileInformer API methods --

  /** Checks if the given string is a valid filename for a Openlab File. */
  public boolean isThisType(String name) {
    // Since we can't always determine it from the name alone (blank
    // extensions) we open the file and call the block verifier.
    long len = new File(name).length();
    int count = len < 16384 ? (int) len : 16384;
    byte[] buf = new byte[count];
    try {
      FileInputStream fin = new FileInputStream(name);
      int read = 0;
      while(read < count) {
        read += fin.read(buf, read, count-read);
      }
      fin.close();
      return isThisType(buf);
    }
    catch (IOException e) {
      return false;
    }
  }

  /** Checks if the given block is a valid header for a Openlab file. */
  public boolean isThisType(byte[] block) {
    return block[0] == 0 && block[1] == 0 &&
      block[2] == -1 && block[3] == -1 &&
      block[4] == 105 && block[5] == 109 &&
      block[6] == 112 && block[7] == 114;
  }

  /** Returns the default file suffixes for the Openlab file format. */
  public String[] getDefaultSuffixes() {
    return new String[] {"liff", "lif"};
  }


  // -- FormNode API methods --

  /**
   * Saves a VisAD Data object to Openlab
   * format at the given location.
   */
  public void save(String id, Data data, boolean replace)
    throws BadFormException, IOException, RemoteException, VisADException
  {
    throw new UnimplementedException("OpenlabForm.save");
  }

  /**
   * Adds data to an existing Openlab file.
   *
   * @throws BadFormException Always thrown (not supported).
   */
  public void add(String id, Data data, boolean replace)
    throws BadFormException
  {
    throw new BadFormException("OpenlabForm.add");
  }

  /**
   * Opens an existing Openlab file from the given location.
   *
   * @return VisAD Data object containing Openlab data.
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

  /** Gets Forms(?)
   *  @return Always returns null
   */
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
    throw new BadFormException("OpenlabForm.open(URL)");
  }


  // -- FormBlockReader API methods --

  /**
   * Opens the Openlab file with the file name specified
   * by id, retrieving only the frame number given.
   *
   * Warning: This method is not thread-safe when accessing multiple blocks
   * of a color Openlab file. There is a known bug in the QuickTime library,
   * where it crashes when converting two byte blocks to PICT at the same time.
   *
   * @return a DataImpl containing the specified frame
   */
  public synchronized DataImpl open(String id, int blockNumber)
    throws BadFormException, IOException, VisADException
  {
    if (!id.equals(currentId)) initFile(id);

    // First, initialize:
    in.seek(offsets[blockNumber] + 12);
    byte[] toRead = new byte[4];
    in.read(toRead);
    int blockSize = batoi(toRead);

    toRead = new byte[1];
    in.read(toRead);
    // right now I'm gonna skip all the header info
    // check to see whether or not this is v2 data
    if (toRead[0] == 1) {
      in.skipBytes(128);
    }
    in.skipBytes(169);
    // read in the block of data
    toRead = new byte[blockSize];
    int read = 0;
    int left = blockSize;
    while (left > 0) {
      int i = in.read(toRead, read, left);
      read += i;
      left -= i;
    }
    byte[] pixelData = new byte[blockSize];
    int pixPos = 0;
    Dimension dim = qtForm.getPictDimensions(toRead);

    int length = toRead.length;
    int num, size, blockEnd;
    int totalBlocks = -1; // set to allow loop to start.
    int expectedBlock = 0;
    int pos = 0;
    int imagePos = 0;
    int imageSize = dim.width * dim.height;
    float[][] flatSamples = new float[1][imageSize];
    byte[] temp;

    // set up VisAD objects
    Linear2DSet pixelSet = new Linear2DSet(domainTuple,
     0, dim.width - 1, dim.width, dim.height - 1, 0, dim.height);
    FlatField frameField = new FlatField(funcRowColPix, pixelSet);
    boolean skipflag;

    // read in deep grey pixel data into an array, and create a
    // VisAD object out of it

    // First, checks the existence of a deep gray block. If it doesn't exist,
    // assume it is PICT data, and attempt to read it. This is unpleasantly
    // dangerous, because QuickTime has this unfortunate habit of crashing
    // when it doesn't work.

    // check whether or not there is deep gray data
    while (expectedBlock != totalBlocks) {
      skipflag = false;
      while (pos + 7 < length &&
        (toRead[pos] != 73 || toRead[pos + 1] != 86 ||
        toRead[pos + 2] != 69 || toRead[pos + 3] != 65 ||
        toRead[pos + 4] != 100 || toRead[pos + 5] != 98 ||
        toRead[pos + 6] != 112 || toRead[pos + 7] != 113))
      {
        pos++;
      }
      if (pos + 32 > length) { // The header is 32 bytes long.
        if (expectedBlock == 0 && imageType[blockNumber] < 9) {
          // there has been no deep gray data, and it is supposed
          // to be a pict... *crosses fingers*
          try { // This never actually does an exception, to my knowledge,
                // but we can always hope.
            return qtForm.pictToField(toRead);
          }
          catch (Exception e) {
            throw new BadFormException("No iPic comment block found");
          }
        }
        else {
          throw new BadFormException("Expected iPic comment block not found");
        }
      }

      pos += 8; // skip the block type we just found.

      // Read info from the iPic comment. This serves as a
      // starting point to read the rest.
      temp = new byte[] {
        toRead[pos], toRead[pos+1], toRead[pos+2], toRead[pos+3]
      };
      num = batoi(temp);
      if (num != expectedBlock) {
        throw new BadFormException("Expected iPic block not found");
      }
      expectedBlock++;
      temp = new byte[] {
        toRead[pos+4], toRead[pos+5], toRead[pos+6], toRead[pos+7]
      };
      if (totalBlocks == -1) {
        totalBlocks = batoi(temp);
      }
      else {
        if (batoi(temp) != totalBlocks) {
          throw new BadFormException("Unexpected totalBlocks numbein.read");
        }
      }

      // skip to size
      pos += 16;
      temp = new byte[] {
        toRead[pos], toRead[pos+1], toRead[pos+2], toRead[pos+3]
      };
      size = batoi(temp);
      pos += 8;
      blockEnd = pos + size;

      // copy into our data array.
      System.arraycopy(toRead, pos, pixelData, pixPos, size);
      pixPos += size;
    }
    int pixelValue = 0;
    pos = 0;

    // Now read the data and put it into the VisAD objects
    while(true) {
      if (pos + 1 < pixelData.length) {
        pixelValue = pixelData[pos]<0?256+pixelData[pos]:
                     (int)pixelData[pos]<<8;
        pixelValue += pixelData[pos+1]<0?256+pixelData[pos+1]:
                      (int)pixelData[pos+1];
      }
      else {
        throw new BadFormException("Malformed LIFF data");
      }
      flatSamples[0][imagePos] = pixelValue;
      imagePos++;
      if (imagePos == imageSize) { // done, return it.
        if (isColor) {
          float[][] flatSamp = new float[3][];
          flatSamp[0] = flatSamp[1] = flatSamp[2] = flatSamples[0];
          frameField = new FlatField(funcRowColRGB, pixelSet);
          frameField.setSamples(flatSamp, false);
          return frameField;
        }
        else { // it's all grayscale.
          frameField = new FlatField(funcRowColPix, pixelSet);
          frameField.setSamples(flatSamples, false);
          return frameField;
        }
      }
      pos += 2;
    }
  }

  /** Returns the number of frames in the specified Openlab file. */
  public int getBlockCount(String id)
    throws BadFormException, IOException, VisADException
  {
    if (!id.equals(currentId)) initFile(id);
    return numBlocks;
  }

  /** Closes any currently open files. */
  public void close() throws BadFormException, IOException, VisADException {
    if (in != null) {
      in.close();
      in = null;
    }
  }


  // -- FormProgressInformer methods --

  /**
   * Gets the percentage complete of the form's current operation.
   * @return the percentage complete (0.0 - 100.0), or Double.NaN
   *         if no operation is currently taking place
   */
  public double getPercentComplete() { return percent; }


  // -- MetadataReader API methods --

  /**
   * Obtains the specified metadata field's value for the given file.
   * @param field the name associated with the metadata field
   * @return the value, or null should the field not exist
   */
  public Object getMetadataValue(String id, String field)
    throws BadFormException, IOException, VisADException
  {
    if (!id.equals(currentId)) initFile(id);
    return metadata.get(field);
  }

  /**
   * Obtains a hashtable containing all metadata field/value pairs from
   * the given file.
   * @param id the filename
   * @return the hashtable containing all metadata associated with the file
   */
  public Hashtable getMetadata(String id)
    throws BadFormException, IOException, VisADException
  {
    if (!id.equals(currentId)) initFile(id);
    return metadata;
  }


  // -- Utility methods --

  /** Translates up to the first 4 bytes of a byte array to an integer. */
  private static int batoi(byte[] inp) {
    int len = inp.length>4?4:inp.length;
    int total = 0;
    for (int i = 0; i < len; i++) {
      total += (inp[i]<0?256+inp[i]:(int)inp[i]) << (((len - 1) - i) * 8);
    }
    return total;
  }


  // -- Helper methods --

  private void initFile(String id)
    throws IOException, VisADException, BadFormException
  {
    in = new RandomAccessFile(id, "r");
    currentId = id;

    // initialize an array containing tag offsets, so we can
    // use an O(1) search instead of O(n) later.
    // Also determine whether we will be reading color or grayscale
    // images

    isColor = false;

    byte[] toRead = new byte[4];
    Vector v = new Vector(); // a temp vector containing offsets.

    // Get first offset.
    in.seek(16);
    in.read(toRead);
    int nextOffset = batoi(toRead);
    int nextOffsetTemp;

    boolean first = true;
    while(nextOffset != 0) {
      in.seek(nextOffset + 4);
      in.read(toRead);
      nextOffsetTemp = batoi(toRead); // get next tag, but still need this one
      in.read(toRead);
      if ((new String(toRead)).equals("PICT")) {
        boolean ok = true;
        if (first) {
          // ignore first image if it is called "Original Image" (pure white)
          first = false;
          in.skipBytes(47);
          byte[] layerNameBytes = new byte[127];
          in.read(layerNameBytes);
          String layerName = new String(layerNameBytes);
          if (layerName.startsWith("Original Image")) ok = false;
        }
        if (ok) v.add(new Integer(nextOffset)); // add THIS tag offset
      }
      if (nextOffset == nextOffsetTemp) break;
      nextOffset = nextOffsetTemp;
    }

    in.seek(((Integer) v.firstElement()).intValue());

    // create and populate the array of offsets from the vector.
    numBlocks = v.size();
    offsets = new int[numBlocks];
    for (int i = 0; i < numBlocks; i++) {
      offsets[i] = ((Integer) v.get(i)).intValue();
    }

    // check to see whether there is any color data. This also populates
    // the imageTypes that the file uses.
    toRead = new byte[2];
    imageType = new int[numBlocks];
    for (int i = 0; i < numBlocks; i++) {
      in.seek(offsets[i]);
      in.skipBytes(40);
      in.read(toRead);
      imageType[i] = batoi(toRead);
      if (imageType[i] < 9) isColor = true;
    }

    initMetadata();
  }

  /** Populates the metadata hashtable. */
  private void initMetadata() throws IOException {
    metadata = new Hashtable();

    //start by reading the file header
    in.seek(0);
    byte[] toRead = new byte[4];
    in.read(toRead);
    long order = batoi(toRead);  //byte ordering
    boolean little = toRead[2] != 0xff || toRead[3] != 0xff;
    metadata.put("Byte Order", new Boolean(little));

    in.skipBytes(4);
    in.read(toRead);
    long version = TiffTools.bytesToLong(toRead, little);
    metadata.put("Version", new Long(version));

    byte[] er = new byte[2];
    in.read(er);
    short count = TiffTools.bytesToShort(er, little);
    metadata.put("Count", new Short(count));

    in.skipBytes(2);

    in.read(toRead);
    long offset = TiffTools.bytesToLong(toRead, little);

    //skip to first tag
    in.seek(offset);

    //read in each tag and its data

    for (int i=0; i<count; i++) {
      in.read(er);
      short tag = TiffTools.bytesToShort(er, little);
      in.skipBytes(2);

      in.read(toRead);
      offset = TiffTools.bytesToLong(toRead, little);

      in.read(toRead);
      long format = TiffTools.bytesToLong(toRead, little);
      metadata.put("Format", new Long(format));

      in.read(toRead);
      long numBytes = TiffTools.bytesToLong(toRead, little);
      metadata.put("NumBytes", new Long(numBytes));

      if (tag == 67 || tag == 68) {
        byte[] b = new byte[1];
        in.read(b);
        boolean isOpenlab2;
        if (b[0] == '0') isOpenlab2 = false;
        else isOpenlab2 = true;
        metadata.put("isOpenlab2", new Boolean(isOpenlab2));

        in.skipBytes(2);
        in.read(er);
        short layerId = TiffTools.bytesToShort(er, little);
        metadata.put("LayerID", new Short(layerId));

        in.read(er);
        short layerType = TiffTools.bytesToShort(er, little);
        metadata.put("LayerType", new Short(layerType));

        in.read(er);
        short bitDepth = TiffTools.bytesToShort(er, little);
        metadata.put("BitDepth", new Short(bitDepth));

        in.read(er);
        short opacity = TiffTools.bytesToShort(er, little);
        metadata.put("Opacity", new Short(opacity));

        //not sure how many bytes to skip here
        in.skipBytes(10);

        in.read(toRead);
        long type = TiffTools.bytesToLong(toRead, little);
        metadata.put("ImageType", new Long(type));

        //not sure how many bytes to skip
        in.skipBytes(10);

        in.read(toRead);
        long timestamp = TiffTools.bytesToLong(toRead, little);
        metadata.put("Timestamp", new Long(timestamp));

        in.skipBytes(2);

        if (isOpenlab2 == true) {
          byte[] layerName = new byte[127];
          in.read(layerName);
          metadata.put("LayerName", new String(layerName));

          in.read(toRead);
          long timestampMS = TiffTools.bytesToLong(toRead, little);
          metadata.put("Timestamp-MS", new Long(timestampMS));

          in.skipBytes(1);
          byte[] notes = new byte[118];
          in.read(notes);
          metadata.put("Notes", new String(notes));
        }
        else in.skipBytes(123);


      }
      else if (tag == 69) {
        in.read(toRead);
        long platform = TiffTools.bytesToLong(toRead, little);
        metadata.put("Platform", new Long(platform));

        in.read(er);
        short units = TiffTools.bytesToShort(er, little);
        metadata.put("Units", new Short(units));

        in.read(er);
        short imageId = TiffTools.bytesToShort(er, little);
        metadata.put("ID", new Short(imageId));
        in.skipBytes(1);

        byte[] toRead2 = new byte[8];
        double xOrigin = TiffTools.readDouble(in, little);
        metadata.put("XOrigin", new Double(xOrigin));
        double yOrigin = TiffTools.readDouble(in, little);
        metadata.put("YOrigin", new Double(yOrigin));
        double xScale = TiffTools.readDouble(in, little);
        metadata.put("XScale", new Double(xScale));
        double yScale = TiffTools.readDouble(in, little);
        metadata.put("YScale", new Double(yScale));
        in.skipBytes(1);

        byte[] other = new byte[31];
        in.read(other);
        metadata.put("Other", new String(other));
      }

      in.seek(offset);
    }
  }


  // -- Main method --

  /**
   * Run 'java visad.data.bio.OpenlabForm in_file'
   * to test read an Openlab LIFF data file.
   */
  public static void main(String[] args)
    throws VisADException, IOException
  {
    BaseTiffForm.testRead(new OpenlabForm(), "Openlab LIFF", args);
  }

}
