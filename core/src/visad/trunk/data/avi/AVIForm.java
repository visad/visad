//
// AVIForm.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data. Copyright (C) 1996 - 2006 Bill Hibbard, Curtis Rueden, Tom
Rink, Dave Glowacki, Steve Emmerson, Tom Whittaker, Don Murray, and
Tommy Jasmin.

This library is free software; you can redistribute it and/or
modify it under the terms of the GNU Library General Public
License as published by the Free Software Foundation; either
version 2 of the License, or (at your option) any later version.

This library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
Library General Public License for more details.

You should have received a copy of the GNU Library General Public
License along with this library; if not, write to the Free
Software Foundation, Inc., 59 Temple Place - Suite 330, Boston,
MA 02111-1307, USA
*/

package visad.data.avi;

import java.io.*;
import java.net.URL;
import java.rmi.RemoteException;

import visad.*;
import visad.data.*;
import visad.util.DataUtility;

/**
 * AVIForm is the VisAD data form for handling uncompressed AVI movies.
 *
 * Much of this form's code was adapted from Wayne Rasband's
 * AVI Movie Reader and AVI Movie Writer plugins for ImageJ
 * (available at http://rsb.info.nih.gov/ij/).
 */
public class AVIForm extends Form
  implements FormFileInformer, FormBlockReader, FormProgressInformer
{

  // -- Static fields --

  /** Counter for AVI form instantiation. */
  private static int num = 0;

  /** Legal AVI suffixes. */
  private static final String[] suffixes = { "avi" };


  // -- Fields --

  /** Filename of current AVI movie. */
  private String current_id;

  /** Number of images in current AVI movie. */
  private int numImages;

  /** Percent complete with current operation. */
  private double percent;

  /** Frames per second of output movies. */
  private int frameRate;


  // -- Save fields --

  private RandomAccessFile raFile;


  // -- Constructor --

  /** Constructs a new AVI file form. */
  public AVIForm() {
    super("AVIForm" + num++);
    setFrameRate(10); // default to 10 fps
  }


  // -- FormFileInformer methods --

  /** Checks if the given string is a valid filename for an AVI movie. */
  public boolean isThisType(String name) {
    for (int i=0; i<suffixes.length; i++) {
      if (name.toLowerCase().endsWith(suffixes[i])) return true;
    }
    return false;
  }

  /** Checks if the given block is a valid header for an AVI movie. */
  public boolean isThisType(byte[] block) {
    return false;
  }

  /** Returns the default file suffixes for the AVI movie format. */
  public String[] getDefaultSuffixes() {
    String[] s = new String[suffixes.length];
    System.arraycopy(suffixes, 0, s, 0, suffixes.length);
    return s;
  }


  // -- New API methods --

  /** Sets the frame rate of output movies in frames per second. */
  public void setFrameRate(int fps) { frameRate = fps; }

  /** Gets teh frame rate of output movies in frames per second. */
  public int getFrameRate() { return frameRate; }


  // -- Form API methods --

  /**
   * Saves a VisAD Data object to an uncompressed AVI movie.
   *
   * @param id        Filename of AVI movie to save.
   * @param data      VisAD Data to convert to AVI format.
   * @param replace   Whether to overwrite an existing file.
   */
  public void save(String id, Data data, boolean replace)
    throws BadFormException, IOException, RemoteException, VisADException
  {
    percent = 0;
    FlatField[] fields = DataUtility.getImageFields(data);
    if (fields == null || fields.length < 1) {
      throw new BadFormException(
        "Data type must be image or time sequence of images");
    }

    int bytesPerPixel;
    File file;
    int xDim, yDim, zDim, tDim, xPad;
    int microSecPerFrame;
    int[] dcLength;

    // location of file size in bytes not counting first 8 bytes
    long saveFileSize;

    // location of length of CHUNK with first LIST - not including first 8
    // bytes with LIST and size. JUNK follows the end of this CHUNK
    long saveLIST1Size;

    int[] extents;

    // location of length of CHUNK with second LIST - not including first 8
    // bytes with LIST and size. Note that saveLIST1subSize = saveLIST1Size +
    // 76, and that the length size written to saveLIST2Size is 76 less than
    // that written to saveLIST1Size. JUNK follows the end of this CHUNK.
    long saveLIST1subSize;

    // location of length of strf CHUNK - not including the first 8 bytes with
    // strf and size. strn follows the end of this CHUNK.
    long savestrfSize;

    int resXUnit = 0;
    int resYUnit = 0;
    float xResol = 0.0f; // in distance per pixel
    float yResol = 0.0f; // in distance per pixel
    long biXPelsPerMeter = 0L;
    long biYPelsPerMeter = 0L;
    byte[] strnSignature;
    byte[] text;
    long savestrnPos;
    byte[] JUNKsignature;
    long saveJUNKsignature;
    int paddingBytes;
    long saveLIST2Size;
    byte[] dataSignature;
    byte[] idx1Signature;
    long savedbLength[];
    long savedcLength[];
    long idx1Pos;
    long endPos;
    long saveidx1Length;
    int t,z;
    long savemovi;
    int xMod;

    FunctionType ftype = (FunctionType) fields[0].getType();
    MathType range = ftype.getRange();
    if (range instanceof RealTupleType) {
      RealTupleType ttype = (RealTupleType) range;
      bytesPerPixel = ttype.getDimension();
    }
    else bytesPerPixel = 1;

    file = new File(id);
    raFile = new RandomAccessFile(file, "rw");

    writeString("RIFF"); // signature
    saveFileSize = raFile.getFilePointer();
    // Bytes 4 thru 7 contain the length of the file. This length does
    // not include bytes 0 thru 7.
    writeInt(0); // for now write 0 in the file size location
    writeString("AVI "); // RIFF type
    // Write the first LIST chunk, which contains information on data decoding
    writeString("LIST"); // CHUNK signature
    // Write the length of the LIST CHUNK not including the first 8 bytes with
    // LIST and size. Note that the end of the LIST CHUNK is followed by JUNK.
    saveLIST1Size = raFile.getFilePointer();
    writeInt(0); // for now write 0 in avih sub-CHUNK size location
    writeString("hdrl"); // CHUNK type
    writeString("avih"); // Write the avih sub-CHUNK

    // Write the length of the avih sub-CHUNK (38H) not including the
    // the first 8 bytes for avihSignature and the length
    writeInt(0x38);

    // dwMicroSecPerFrame - Write the microseconds per frame
    microSecPerFrame = (int) (1.0 / frameRate * 1.0e6);
    writeInt(microSecPerFrame);

    // Write the maximum data rate of the file in bytes per second
    writeInt(0); // dwMaxBytesPerSec

    writeInt(0); // dwReserved1 - Reserved1 field set to zero
    writeInt(0x10); // dwFlags - just set the bit for AVIF_HASINDEX

    // 10H AVIF_HASINDEX: The AVI file has an idx1 chunk containing
    //   an index at the end of the file. For good performance, all
    //   AVI files should contain an index.
    // 20H AVIF_MUSTUSEINDEX: Index CHUNK, rather than the physical
    // ordering of the chunks in the file, must be used to determine the
    // order of the frames.
    // 100H AVIF_ISINTERLEAVED: Indicates that the AVI file is interleaved.
    //   This is used to read data from a CD-ROM more efficiently.
    // 800H AVIF_TRUSTCKTYPE: USE CKType to find key frames
    // 10000H AVIF_WASCAPTUREFILE: The AVI file is used for capturing
    //   real-time video. Applications should warn the user before
    //   writing over a file with this fla set because the user
    //   probably defragmented this file.
    // 20000H AVIF_COPYRIGHTED: The AVI file contains copyrighted data
    //   and software. When, this flag is used, software should not
    //   permit the data to be duplicated.

    tDim = 1;
    zDim = fields.length;
    Gridded2DSet set = (Gridded2DSet) fields[0].getDomainSet();
    int[] len = set.getLengths();
    yDim = len[1];
    xDim = len[0];
    xPad = 0;
    xMod = xDim % 4;
    if (xMod != 0) {
      xPad = 4 - xMod;
      xDim += xPad;
    }

    // dwTotalFrames - total frame number
    writeInt(zDim * tDim);

    // dwInitialFrames -Initial frame for interleaved files.
    // Noninterleaved files should specify 0.
    writeInt(0);

    // dwStreams - number of streams in the file - here 1 video and zero audio.
    writeInt(1);

    // dwSuggestedBufferSize - Suggested buffer size for reading the file.
    // Generally, this size should be large enough to contain the largest
    // chunk in the file.
    writeInt(0);

    writeInt(xDim - xPad); // dwWidth - image width in pixels
    writeInt(yDim); // dwHeight - image height in pixels

    // dwReserved[4] - Microsoft says to set the following 4 values to 0.
    writeInt(0);
    writeInt(0);
    writeInt(0);
    writeInt(0);

    // Write the Stream line header CHUNK
    writeString("LIST");

    // Write the size of the first LIST subCHUNK not including the first 8
    // bytes with LIST and size. Note that saveLIST1subSize = saveLIST1Size +
    // 76, and that the length written to saveLIST1subSize is 76 less than the
    // length written to saveLIST1Size. The end of the first LIST subCHUNK is
    // followed by JUNK.
    saveLIST1subSize = raFile.getFilePointer();

    writeInt(0); // for now write 0 in CHUNK size location
    writeString("strl");   // Write the chunk type
    writeString("strh"); // Write the strh sub-CHUNK
    writeInt(56); // Write the length of the strh sub-CHUNK

    // fccType - Write the type of data stream - here vids for video stream
    writeString("vids");

    // Write DIB for Microsoft Device Independent Bitmap. Note: Unfortunately,
    // at least 3 other four character codes are sometimes used for
    // uncompressed AVI videos: 'RGB ', 'RAW ', 0x00000000
    writeString("DIB ");

    writeInt(0); // dwFlags

    // 0x00000001 AVISF_DISABLED The stram data should be rendered only when
    // explicitly enabled.
    // 0x00010000 AVISF_VIDEO_PALCHANGES Indicates that a palette change is
    // included in the AVI file. This flag warns the playback software that it
    // will need to animate the palette.

    // dwPriority - priority of a stream type. For example, in a file with
    // multiple audio streams, the one with the highest priority might be the
    // default one.
    writeInt(0);

    // dwInitialFrames - Specifies how far audio data is skewed ahead of video
    // frames in interleaved files. Typically, this is about 0.75 seconds. In
    // interleaved files specify the number of frames in the file prior
    // to the initial frame of the AVI sequence.
    // Noninterleaved files should use zero.
    writeInt(0);

    // rate/scale = samples/second
    writeInt(1); // dwScale

    //  dwRate - frame rate for video streams
    writeInt(frameRate);

    writeInt(0); // dwStart - this field is usually set to zero

    // dwLength - playing time of AVI file as defined by scale and rate
    // Set equal to the number of frames
    writeInt(tDim * zDim);

    // dwSuggestedBufferSize - Suggested buffer size for reading the stream.
    // Typically, this contains a value corresponding to the largest chunk
    // in a stream.
    writeInt(0);

    // dwQuality - encoding quality given by an integer between 0 and 10,000.
    // If set to -1, drivers use the default quality value.
    writeInt(-1);

    // dwSampleSize #
    // 0 if the video frames may or may not vary in size
    // If 0, each sample of data(such as a video frame) must be in a separate
    // chunk. If nonzero, then multiple samples of data can be grouped into
    // a single chunk within the file.
    writeInt(0);

    // rcFrame - Specifies the destination rectangle for a text or video stream
    // within the movie rectangle specified by the dwWidth and dwHeight members
    // of the AVI main header structure. The rcFrame member is typically used
    // in support of multiple video streams. Set this rectangle to the
    // coordinates corresponding to the movie rectangle to update the whole
    // movie rectangle. Units for this member are pixels. The upper-left corner
    // of the destination rectangle is relative to the upper-left corner of the
    // movie rectangle.
    writeShort((short) 0); // left
    writeShort((short) 0); // top
    writeShort((short) 0); // right
    writeShort((short) 0); // bottom

    // Write the size of the stream format CHUNK not including the first 8
    // bytes for strf and the size. Note that the end of the stream format
    // CHUNK is followed by strn.
    writeString("strf"); // Write the stream format chunk

    savestrfSize = raFile.getFilePointer();
    writeInt(0); // for now write 0 in the strf CHUNK size location

    // Applications should use this size to determine which BITMAPINFO header
    // structure is being used. This size includes this biSize field.
    writeInt(40); // biSize - Write header size of BITMAPINFO header structure

    writeInt(xDim - xPad);  // biWidth - image width in pixels

    // biHeight - image height in pixels. If height is positive, the bitmap is
    // a bottom up DIB and its origin is in the lower left corner. If height is
    // negative, the bitmap is a top-down DIB and its origin is the upper left
    // corner. This negative sign feature is supported by the Windows Media
    // Player, but it is not supported by PowerPoint.
    writeInt(yDim);

    // biPlanes - number of color planes in which the data is stored
    // This must be set to 1.
    writeShort(1);

    int bitsPerPixel = bytesPerPixel * 8;

    // biBitCount - number of bits per pixel #
    // 0L for BI_RGB, uncompressed data as bitmap
    writeShort((short) bitsPerPixel);

    //writeInt(bytesPerPixel * xDim * yDim * zDim * tDim); // biSizeImage #
    writeInt(0); // biSizeImage #
    writeInt(0); // biCompression - type of compression used
    writeInt(0); // biXPelsPerMeter - horizontal resolution in pixels
    writeInt(0); // biYPelsPerMeter - vertical resolution in pixels per meter
    if (bitsPerPixel == 8) writeInt(256); // biClrUsed
    else writeInt(0); // biClrUsed

    // biClrImportant - specifies that the first x colors of the color table
    // are important to the DIB. If the rest of the colors are not available,
    // the image still retains its meaning in an acceptable manner. When this
    // field is set to zero, all the colors are important, or, rather, their
    // relative importance has not been computed.
    writeInt(0);

    // Write the LUTa.getExtents()[1] color table entries here. They are
    // written: blue byte, green byte, red byte, 0 byte
    if (bytesPerPixel == 1) {
      byte[] lutWrite = new byte[4 * 256];
      for (int i=0; i<256; i++) {
        lutWrite[4*i] = (byte) i; // blue
        lutWrite[4*i+1] = (byte) i; // green
        lutWrite[4*i+2] = (byte) i; // red
        lutWrite[4*i+3] = 0;
      }
      raFile.write(lutWrite);
    }

    // Use strn to provide a zero terminated text string describing the stream
    savestrnPos = raFile.getFilePointer();
    raFile.seek(savestrfSize);
    writeInt((int)(savestrnPos - (savestrfSize+4)));
    raFile.seek(savestrnPos);
    writeString("strn");
    writeInt(16); // Write the length of the strn sub-CHUNK
    text = new byte[16];
    text[0] = 70; // F
    text[1] = 105; // i
    text[2] = 108; // l
    text[3] = 101; // e
    text[4] = 65; // A
    text[5] = 118; // v
    text[6] = 105; // i
    text[7] = 32; // space
    text[8] = 119; // w
    text[9] = 114; // r
    text[10] = 105; // i
    text[11] = 116; // t
    text[12] = 101; // e
    text[13] = 32; // space
    text[14] = 32; // space
    text[15] = 0; // termination byte
    raFile.write(text);

    // write a JUNK CHUNK for padding
    saveJUNKsignature = raFile.getFilePointer();
    raFile.seek(saveLIST1Size);
    writeInt((int)(saveJUNKsignature - (saveLIST1Size+4)));
    raFile.seek(saveLIST1subSize);
    writeInt((int)(saveJUNKsignature - (saveLIST1subSize+4)));
    raFile.seek(saveJUNKsignature);
    writeString("JUNK");
    paddingBytes = (int)(4084 - (saveJUNKsignature + 8));
    writeInt(paddingBytes);
    for (int i=0; i<(paddingBytes/2); i++) writeShort(0);

    // Write the second LIST chunk, which contains the actual data
    writeString("LIST");

    // Write the length of the LIST CHUNK not including the first 8 bytes with
    // LIST and size. The end of the second LIST CHUNK is followed by idx1.
    saveLIST2Size = raFile.getFilePointer();

    writeInt(0);  // For now write 0
    savemovi = raFile.getFilePointer();
    writeString("movi"); // Write CHUNK type 'movi'
    savedbLength = new long[tDim * zDim];
    savedcLength = new long[tDim * zDim];
    dcLength = new int[tDim * zDim];

    dataSignature = new byte[4];
    dataSignature[0] = 48; // 0
    dataSignature[1] = 48; // 0
    dataSignature[2] = 100; // d
    dataSignature[3] = 98; // b

    // Write the data. Each 3-byte triplet in the bitmap array represents the
    // relative intensities of blue, green, and red, respectively, for a pixel.
    // The color bytes are in reverse order from the Windows convention.
    byte[] buf = new byte[bytesPerPixel * xDim * yDim];

    int width = xDim - xPad;
    for (z=0; z<zDim; z++) {
      percent = (double) z / zDim;
      raFile.write(dataSignature);
      savedbLength[z] = raFile.getFilePointer();
      writeInt(bytesPerPixel * xDim * yDim); // Write the data length

      double[][] values = fields[z].getValues(false);
      int index = 0;
      for (int y=yDim-1; y>=0; y--) {
        for (int x=0; x<width; x++) {
          int ndx = width * y + x;
          for (int q=bytesPerPixel-1; q>=0; q--) {
            buf[index++] = (byte) values[q][ndx];
          }
        }
        int pad = (bytesPerPixel * xPad) % 4;
        for (int i=0; i<pad; i++) buf[index++] = 0;
      }
      raFile.write(buf);
    }

    // Write the idx1 CHUNK
    // Write the 'idx1' signature
    idx1Pos = raFile.getFilePointer();
    raFile.seek(saveLIST2Size);
    writeInt((int)(idx1Pos - (saveLIST2Size + 4)));
    raFile.seek(idx1Pos);
    writeString("idx1");

    // Write the length of the idx1 CHUNK not including the idx1 signature and
    // the 4 length bytes. Write 0 for now.
    saveidx1Length = raFile.getFilePointer();
    writeInt(0);

    for (z=0; z<zDim; z++) {
      // In the ckid field write the 4 character code to identify the chunk
      // 00db or 00dc
      raFile.write(dataSignature);
      if (z == 0) writeInt(0x10); // Write the flags - select AVIIF_KEYFRAME
      else writeInt(0x00);

      // AVIIF_KEYFRAME 0x00000010L
      // The flag indicates key frames in the video sequence.
      // Key frames do not need previous video information to be decompressed.
      // AVIIF_NOTIME 0x00000100L The CHUNK does not influence video timing
      // (for example a palette change CHUNK).
      // AVIIF_LIST 0x00000001L Marks a LIST CHUNK.
      // AVIIF_TWOCC 2L
      // AVIIF_COMPUSE 0x0FFF0000L These bits are for compressor use.
      writeInt((int)(savedbLength[z]- 4 - savemovi));

      // Write the offset (relative to the 'movi' field) to the relevant CHUNK
      // Write the length of the relevant CHUNK. Note that this length is also
      // written at savedbLength
      writeInt(bytesPerPixel*xDim*yDim);
    }
    endPos = raFile.getFilePointer();
    raFile.seek(saveFileSize);
    writeInt((int)(endPos - (saveFileSize+4)));
    raFile.seek(saveidx1Length);
    writeInt((int)(endPos - (saveidx1Length+4)));
    raFile.close();

    percent = -1;
  }

  /**
   * Adds data to an existing AVI movie.
   *
   * @exception BadFormException Always thrown (method is not implemented).
   */
  public void add(String id, Data data, boolean replace)
    throws BadFormException
  {
    throw new BadFormException("AVIForm.add");
  }

  /**
   * Opens an existing AVI movie from the given filename.
   *
   * @return VisAD Data object containing AVI data.
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
   * Opens an existing AVI movie from the given URL.
   *
   * @return VisAD Data object containing AVI data.
   *
   * @exception BadFormException Always thrown (method is not implemented).
   */
  public DataImpl open(URL url)
    throws BadFormException, IOException, VisADException
  {
    throw new BadFormException("AVIForm.open(URL)");
  }

  public FormNode getForms(Data data) {
    return null;
  }


  // -- FormBlockReader methods --

  public DataImpl open(String id, int block_number)
    throws BadFormException, IOException, VisADException
  {
    if (true) {
      throw new UnimplementedException("AVIForm.open(String, int)");
    }

    if (!id.equals(current_id)) initFile(id);

    if (block_number < 0 || block_number >= numImages) {
      throw new BadFormException("Invalid image number: " + block_number);
    }

    return null;
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

  private void initFile(String id)
    throws BadFormException, IOException, VisADException
  {
    if (true) throw new UnimplementedException("AVIForm.initFile(String)");

    // close any currently open files
    close();

    // determine number of images in AVI movie
    numImages = 0;

    current_id = id;
  }

  private void writeString(String s) throws IOException {
    byte[] bytes =  s.getBytes("UTF-8");
    raFile.write(bytes);
  }

  private void writeInt(int v) throws IOException {
    raFile.write(v & 0xFF);
    raFile.write((v >>>  8) & 0xFF);
    raFile.write((v >>> 16) & 0xFF);
    raFile.write((v >>> 24) & 0xFF);
  }

  private void writeShort(int v) throws IOException {
    raFile.write(v& 0xFF);
    raFile.write((v >>> 8) & 0xFF);
  }


  // -- Main method --

  /**
   * Run 'java visad.data.visad.AVIForm in_file out_file' to convert
   * in_file to out_file in AVI data format.
   */
  public static void main(String[] args)
    throws VisADException, RemoteException, IOException
  {
    if (args == null || args.length < 1 || args.length > 2) {
      System.out.println("To convert a file to AVI, run:");
      System.out.println("  java visad.data.avi.AVIForm in_file out_file");
      System.out.println("To test read an AVI movie, run:");
      System.out.println("  java visad.data.avi.AVIForm in_file");
      System.exit(2);
    }

    if (args.length == 1) {
      // Test read AVI movie
      AVIForm form = new AVIForm();
      System.out.print("Reading " + args[0] + " ");
      Data data = form.open(args[0]);
      System.out.println("[done]");
      System.out.println("MathType =\n" + data.getType().prettyString());
    }
    else if (args.length == 2) {
      // Convert file to AVI format
      System.out.print(args[0] + " -> " + args[1] + " ");
      DefaultFamily loader = new DefaultFamily("loader");
      DataImpl data = loader.open(args[0]);
      loader = null;
      AVIForm form = new AVIForm();
      form.save(args[1], data, true);
      System.out.println("[done]");
    }
    System.exit(0);
  }

}
