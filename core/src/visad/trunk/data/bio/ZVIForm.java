//
// ZVIForm.java
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

import visad.*;
import visad.data.*;

/** ZVIForm is the VisAD data adapter for reading Zeiss ZVI files. */
public class ZVIForm extends Form
  implements FormBlockReader, FormFileInformer
{

  // -- Constants --

  /** First few bytes of every ZVI file. */
  private static final byte[] ZVI_SIG = {
    -48, -49, 17, -32, -95, -79, 26, -31
  };

  /** Block identifying start of useful header information and pixel data. */
  private static final byte[] ZVI_MAGIC_BLOCK = { // S.c.a.l.i.n.g....
    83, 0, 99, 0, 97, 0, 108, 0, 105, 0, 110, 0, 103, 0, 0, 0, 9
  };

  /** Memory buffer size in bytes, for reading from disk. */
  private static final int BUFFER_SIZE = 8192;

  /** String begging user to send unsupported ZVI files in for examination. */
  private static final String WHINING =
    "Perhaps you'd be willing to send your ZVI file to the author " +
    "to help improve this hacked-together ZVI support?";

  /** Debugging flag. */
  private static final boolean DEBUG = false;


  // -- Fields --

  /** Form instantiation counter. */
  private static int num = 0;


  // -- Constructor --

  /** Constructs a new ZVI file form. */
  public ZVIForm() {
    super("ZVIForm" + num++);
  }


  // -- FormNode API methods --

  /** Saves a VisAD Data object at the given location. */
  public void save(String id, Data data, boolean replace)
    throws UnimplementedException
  {
    throw new UnimplementedException("ZVIForm.save");
  }

  /**
   * Adds data to an existing file.
   *
   * @throws BadFormException Always thrown (not supported).
   */
  public void add(String id, Data data, boolean replace)
    throws BadFormException
  {
    throw new BadFormException("ZVIForm.add");
  }

  /**
   * Opens an existing file from the given location.
   *
   * @return VisAD Data object.
   */
  public DataImpl open(String id)
    throws BadFormException, IOException, VisADException
  {
    // Current highly questionable decoding strategy:
    //
    // 1) Find the following 17-byte sequence:
    //   53 00 63 00 61 00 6c 00 69 00 6e 00 67 00 00 00 09 (S.c.a.l.i.n.g....)
    // This sequence is typically ~32000 bytes into the ZVI file.
    //
    // 2) Skip the next 56 bytes (including the 17-byte sequence).
    //
    // 3) Read the following parameters (little endian):
    //   - width (4 bytes)
    //   - height (4 bytes)
    //   - ? (4 bytes; always 1 -- number of images, perhaps?)
    //   - bytes per pixel (4 bytes)
    //   - ? (4 bytes; 1 for 8-bit RGB; 4 for 16-bit grayscale -- no clue)
    //   - bit depth (4 bytes -- same as bytes per pixel * 8)
    //
    // 4) Read (width * height * bytes per pixel) bytes into samples.
    //    Note that all byte ordering is little endian. So 16-bit data is
    //    LSB MSB, and 3-channel data is BGR instead of RGB.

    File fid = new File(id);
    int fileSize = (int) fid.length();
    RandomAccessFile fin = new RandomAccessFile(fid, "r");

    // search for magic ZVI block identifying start of interesting data
    int filePos = 0;
    byte[] buf = new byte[BUFFER_SIZE];
    int header = -1;
    int step = 0;
    boolean found = false;
    while (true) {
      int len = fileSize - filePos;
      if (len > buf.length) len = buf.length;
      fin.readFully(buf, 0, len);

      for (int i=0; i<len; i++) {
        if (buf[i] == ZVI_MAGIC_BLOCK[step]) {
          if (step == 0) {
            // could be a match; flag this spot
            header = filePos + i;
          }
          step++;
          if (step == ZVI_MAGIC_BLOCK.length) {
            // found complete match; done searching
            found = true;
            break;
          }
        }
        else {
          // no match; reset step indicator
          header = -1;
          step = 0;
        }
      }
      if (found) break; // found a match; we're done
      if (len < buf.length) break; // EOF reached; we're done

      filePos += len;
    }

    if (header < 0) {
      throw new BadFormException("Could not locate header information. " +
        WHINING);
    }

    // read in the useful header information
    fin.seek(header + 56);
    int width = readInt(fin);
    int height = readInt(fin);
    int maybeNumImages = readInt(fin); // not really sure...
    int bytesPerPixel = readInt(fin);
    int noClue = readInt(fin); // no idea what this value signifies
    int bitDepth = readInt(fin);

    if (DEBUG) {
      System.out.println("header = " + header);
      System.out.println("width = " + width);
      System.out.println("height = " + height);
      System.out.println("maybeNumImages = " + maybeNumImages);
      System.out.println("bytesPerPixel = " + bytesPerPixel);
      System.out.println("noClue = " + noClue);
      System.out.println("bitDepth = " + bitDepth);
    }

    if (bitDepth != bytesPerPixel * 8) {
      System.err.println("Warning: bitDepth and bytesPerPixel do not match. " +
        WHINING);
    }
    if (maybeNumImages != 1) {
      System.err.println("Warning: maybeNumImages != 1. " + WHINING);
    }
    if (noClue != 1 && noClue != 4) {
      System.err.println("Warning: unknown noClue value (" + noClue + "). " +
        WHINING);
    }

    // guess at number of channel components at each pixel
    int offset = header + 80;
    int numPixels = width * height;
    int imageSize = numPixels * bytesPerPixel;
    int numChannels = noClue == 1 ? 3 : 1; // a total shot in the dark

    if (DEBUG) {
      System.out.println("offset = " + offset);
      System.out.println("numPixels = " + numPixels);
      System.out.println("imageSize = " + imageSize);
      System.out.println("numChannels = " + numChannels);
    }

    if (imageSize + offset > fileSize || numChannels < 1) {
      throw new BadFormException("File is not big enough to contain the " +
        "pixels (width=" + width + "; height=" + height +
        "; bytesPerPixel=" + bytesPerPixel + "; numChannels=" + numChannels +
        "; offset=" + offset + "; fileSize=" + fileSize + "). " + WHINING);
    }

    // compute number of bytes per pixel channel
    int bytesPerChannel = bytesPerPixel / numChannels;
    if (DEBUG) {
      System.out.println("bytesPerChannel = " + bytesPerChannel);
    }

    if (bytesPerPixel % numChannels != 0) {
      System.err.println("Warning: incompatible bytesPerPixel (" +
        bytesPerPixel + ") and numChannels (" + numChannels +
        "). Assuming grayscale data. " + WHINING);
      numChannels = 1;
      bytesPerChannel = bytesPerPixel;
    }

    if (DEBUG) System.out.print("Reading image data...");

    // this should probably be buffered, but I'm too lazy for now
    byte[] imageBytes = new byte[imageSize];
    fin.readFully(imageBytes);
    fin.close();

    if (DEBUG) {
      System.out.println("Done.");
      System.out.print("Converting to VisAD format...");
    }

    // convert image bytes into VisAD-compatible floating point values
    int index = 0;
    float[][] samples = new float[numChannels][numPixels];
    for (int i=0; i<numPixels; i++) {
      for (int c=0; c<numChannels; c++) {
        byte[] b = new byte[bytesPerChannel];
        System.arraycopy(imageBytes, index, b, 0, bytesPerChannel);
        index += bytesPerChannel;
        samples[c][i] = batoi(b);
      }
    }

    if (DEBUG) {
      System.out.println("Done.");
      System.out.print("Constructing FlatField...");
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

    if (DEBUG) System.out.println("Done.");

    return ff;
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
    throw new BadFormException("ZVIForm.open(URL)");
  }


  // -- FormBlockReader API methods --

  /**
   * Obtains the specified block from the given file.
   * @param id The file from which to load data blocks.
   * @param block_number The block number of the block to load.
   * @throws VisADException If the block number is invalid.
   */
  public DataImpl open(String id, int block_number)
    throws BadFormException, IOException, VisADException
  {
    if (block_number != 0) {
      throw new BadFormException("Invalid image number: " + block_number);
    }
    return open(id);
  }

  /**
   * Determines the number of blocks in the given file.
   * @param id The file for which to get a block count.
   */
  public int getBlockCount(String id)
    throws BadFormException, IOException, VisADException
  {
    return 1;
  }

  /** Closes any open files. */
  public void close() throws BadFormException, IOException, VisADException { }


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


  // -- Utility methods --

  /** Translates up to the first 4 bytes of a byte array to an integer. */
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


  // -- Main method --

  public static void main(String[] args) throws VisADException, IOException {
     ZVIForm reader = new ZVIForm();
     System.out.println("Opening " + args[0] + "...");
     Data d = reader.open(args[0]);
     System.out.println(d.getType());
  }

}
