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

// Heavily adapted from MetamorphForm.java
package visad.data.bio;

import java.awt.Dimension;
import java.io.*;
import java.net.URL;
import java.util.*;

import visad.*;
import visad.data.*;
import visad.data.qt.*;

/** OpenlabForm is the VisAD data adapter used by Openlab files. */
public class OpenlabForm extends Form
  implements FormFileInformer, FormBlockReader
{

  // -- Fields --

  /** Form instantiation counter. */
  private static int num = 0;

  // VisAD objects
  private RealType frame, row, column, pixel, red, green, blue;
  private FunctionType funcRcP, funcRcRgb;
  private FunctionType funcTRange;
  private RealTupleType domainTuple, rgbTuple;
  private visad.Set pixelSet;
  private visad.Set timeSet;
  private FlatField frameField;
  private FieldImpl timeField;

  // contains currently referenced file information.
  private RandomAccessFile r;
  private String currentId;

  // dimensions[0] and dimensions[1] may not be used.
  private int dimensions[] = new int[3];
  private int[] offsets;
  private int[] imagetype;

  /** Is there color data in this file? */
  private boolean isColor;


  // -- Constructor --

  /** Constructs a new Openlab file form. */
  public OpenlabForm() {
    super("OpenlabForm" + num++);
    try {
      frame = RealType.getRealType("frame");
      row = RealType.getRealType("ImageElement");
      column = RealType.getRealType("ImageLine");
      domainTuple = new RealTupleType(row, column);

      // For grayscale images
      pixel = RealType.getRealType("pixel");
      funcRcP = new FunctionType(domainTuple, pixel);

      // For color images
      RealTupleType rgbPixelData = new RealTupleType(new RealType[] {
        RealType.getRealType("Red"),
        RealType.getRealType("Green"),
        RealType.getRealType("Blue")
      });
      funcRcRgb = new FunctionType(domainTuple, rgbPixelData);
      funcTRange = new FunctionType(frame, funcRcP);
    }
    catch (Exception e) { // Should not happen, but is required.
      e.printStackTrace();
    }
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
    if (block[0] == 0 && block[1] == 0 &&
      block[2] == -1 && block[3] == -1 &&
      block[4] == 105 && block[5] == 109 &&
      block[6] == 112 && block[7] == 114)
    {
      return true;
    }
    return false;
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
    throws UnimplementedException
  {
    throw new UnimplementedException(); // This is not implemented
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

  /** Opens the Openlab file with the file name specified
   *  by id, retrieving only the frame number given.
   *  There is a known bug involving the QuickTime library, where
   *  it can crash when converting two byte blocks to Pict at the
   *  same time.
   *  @return a DataImpl containing the specified frame
   */
  public synchronized DataImpl open(String id, int block_number)
    throws BadFormException, IOException, VisADException
  {
    if (id != currentId) {
      initFile(id);
    }

    // First, initialize:
    r.seek(offsets[block_number] + 12);
    byte[] toread = new byte[4];
    r.read(toread);
    int blockSize = batoi(toread);

    toread = new byte[1];
    r.read(toread);
    // right now I'm gonna skip all the header info
    // check to see whether or not this is v2 data
    if (toread[0] == 1) {
      r.skipBytes(128);
    }
    r.skipBytes(169);
    // read in the block of data
    toread = new byte[blockSize];
    int read = 0;
    int left = blockSize;
    while (left > 0) {
      int i = r.read(toread, read, left);
      read += i;
      left -= i;
    }
    byte[] pixeldata = new byte[blockSize];
    int pixpos = 0;
    Dimension dim = QTForm.getPictDimensions(toread);
    dimensions[0] = dim.width;
    dimensions[1] = dim.height;

    int length = toread.length;
    int blocknumber, blocksize, blockend;
    int totalblocks = -1; // set to allow loop to start.
    int expectedblock = 0;
    int pos = 0;
    int imagepos = 0;
    int imagesize = dimensions[0] * dimensions[1];
    float[][] flatsamples = new float[1][imagesize];
    byte[] temp;

    // Set up visad objects.
    pixelSet = new Linear2DSet(domainTuple,
     0, dimensions[0] - 1, dimensions[0], dimensions[1] - 1, 0, dimensions[1]);
    timeSet = new Integer1DSet(frame,  dimensions[2]);
    frameField = new FlatField(funcRcP, pixelSet);
    boolean skipflag;

    // read in deep grey pixel data into an array, and create a
    // visad object out of it

    // First, checks the existence of a deep gray block. If it doesn't exist,
    // assume it is PICT data, and attempt to read it. This is unpleasantly
    // dangerous, because QuickTime has this unpleasant habit of crashing
    // when it doesn't work.

    // check whether or not there is deep gray data
    while(expectedblock != totalblocks) {
      skipflag = false;
      while(pos + 7 < length &&
        (toread[pos] != 73 || toread[pos + 1] != 86 ||
        toread[pos + 2] != 69 || toread[pos + 3] != 65 ||
        toread[pos + 4] != 100 || toread[pos + 5] != 98 ||
        toread[pos + 6] != 112 || toread[pos + 7] != 113))
      {
        pos++;
      }
      if (pos + 32 > length) { // The header is 32 bytes long.
        if (expectedblock == 0 && imagetype[block_number] < 9) {
          // there has been no deep gray data, and it is supposed
          // to be a pict... *crosses fingers*
          try { // This never actually does an exception, to my knowledge,
                // but we can always hope.
            return QTForm.pictToField(toread);
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
        toread[pos], toread[pos+1], toread[pos+2], toread[pos+3]
      };
      blocknumber = batoi(temp);
      if (blocknumber != expectedblock) {
        throw new BadFormException("Expected iPic block not found");
      }
      expectedblock++;
      temp = new byte[] {
        toread[pos+4], toread[pos+5], toread[pos+6], toread[pos+7]
      };
      if (totalblocks == -1) {
        totalblocks = batoi(temp);
      }
      else {
        if (batoi(temp) != totalblocks) {
          throw new BadFormException("Unexpected totalBlocks number read");
        }
      }

      // skip to blocksize
      pos += 16;
      temp = new byte[] {
        toread[pos], toread[pos+1], toread[pos+2], toread[pos+3]
      };
      blocksize = batoi(temp);
      pos += 8;
      blockend = pos + blocksize;

      // copy into our data array.
      System.arraycopy(toread, pos, pixeldata, pixpos, blocksize);
      pixpos += blocksize;
    }
    int pixelvalue = 0;
    pos = 0;

    // Now read the data and put it into the visad objects
    while(true) {
      if (pos + 1 < pixeldata.length) {
        pixelvalue = pixeldata[pos]<0?256+pixeldata[pos]:
                     (int)pixeldata[pos]<<8;
        pixelvalue += pixeldata[pos+1]<0?256+pixeldata[pos+1]:
                      (int)pixeldata[pos+1];
      }
      else {
        throw new BadFormException("Malformed LIFF data");
      }
      flatsamples[0][imagepos] = pixelvalue;
      imagepos++;
      if (imagepos == imagesize) { // done, return it.
        if (isColor) {
          float[][] flatsamp = new float[3][];
          flatsamp[0] = flatsamp[1] = flatsamp[2] = flatsamples[0];
          frameField = new FlatField(funcRcRgb, pixelSet);
          frameField.setSamples(flatsamp, false);
          return frameField;
        }
        else { // it's all grayscale.
          frameField = new FlatField(funcRcP, pixelSet);
          frameField.setSamples(flatsamples, false);
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
    if (id != currentId) {
      initFile(id);
    }
    return dimensions[2];
  }

  /** Closes any currently open files. */
  public void close() throws BadFormException, IOException, VisADException {
    if (r != null) {
      r.close();
      r = null;
    }
  }


  // -- Utility methods --

  /** Translates up to the first 4 bytes of a byte array to an integer. */
  private static int batoi(byte[] inp) {
    // This is different than the one in MetamorphForm, since the byte order
    // is reversed.
    int len = inp.length>4?4:inp.length;
    int total = 0;
    for (int i = 0; i < len; i++) {
      total = total +
        ((inp[i]<0?(int)256+inp[i]:(int)inp[i]) << (((len - 1) - i) * 8));
    }
    return total;
  }


  // -- Helper methods --

  private void initFile(String id)
    throws IOException, VisADException, BadFormException
  {
    r = new RandomAccessFile(id, "r");
    currentId = id;

    // initialize an array containing tag offsets, so we can
    // use an O(1) search instead of O(n) later.
    // Also determine whether we will be reading color or grayscale
    // images

    isColor = false;

    byte[] toread = new byte[4];
    Vector v = new Vector(); // a temp vector containing offsets.

    // Get first offset.
    r.seek(16);
    r.read(toread);
    int nextoffset = batoi(toread);
    int nextoffsettemp;

    while(nextoffset != 0) {
      r.seek(nextoffset + 4);
      r.read(toread);
      nextoffsettemp = batoi(toread); // get next tag, but we still need
                                      // this one.
      r.read(toread);
      if ((new String(toread)).equals("PICT")) {
        v.add(new Integer(nextoffset)); // add THIS tag offset
      }
      if (nextoffset == nextoffsettemp) {
        break;
      }
      nextoffset = nextoffsettemp;
    }
    // create and populate the array of offsets from the vector.
    offsets = new int[v.size()];
    for (int i = 0; i < v.size(); i++) {
      offsets[i] = ((Integer) v.get(i)).intValue();
    }
    dimensions[2] = v.size();

    // check to see whether there is any color data. This also populates
    // the imagetypes that the file uses.
    toread = new byte[2];
    imagetype = new int[v.size()];
    for (int i = 0; i < v.size(); i++) {
      r.seek(offsets[i]);
      r.skipBytes(40);
      r.read(toread);
      imagetype[i] = batoi(toread);
      if (imagetype[i] < 9) {
        isColor = true;
      }
    }
  }


  // -- Main method --

  public static void main(String[] args) throws VisADException, IOException {
     OpenlabForm reader = new OpenlabForm();
     System.out.println("Opening " + args[0] + "...");
     Data d = reader.open(args[0]);
     System.out.println(d.getType());
  }

}
