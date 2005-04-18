//
// TiffTools.java
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

import java.lang.reflect.Field;
import java.util.*;
import java.io.*;
import visad.*;
import visad.data.BadFormException;

/**
 * A utility class for manipulating TIFF files.
 * @author Curtis Rueden ctrueden at wisc.edu
 * @author Eric Kjellman egkjellman at wisc.edu
 */
public class TiffTools {

  // -- Constants --

  public static final boolean DEBUG = false;

  // non-IFD tags (for internal use)
  public static final int LITTLE_ENDIAN = 0;

  // IFD tags
  public static final int NEW_SUBFILE_TYPE = 254;
  public static final int SUBFILE_TYPE = 255;
  public static final int IMAGE_WIDTH = 256;
  public static final int IMAGE_LENGTH = 257;
  public static final int BITS_PER_SAMPLE = 258;
  public static final int COMPRESSION = 259;
  public static final int PHOTOMETRIC_INTERPRETATION = 262;
  public static final int THRESHHOLDING = 263;
  public static final int CELL_WIDTH = 264;
  public static final int CELL_LENGTH = 265;
  public static final int FILL_ORDER = 266;
  public static final int DOCUMENT_NAME = 269;
  public static final int IMAGE_DESCRIPTION = 270;
  public static final int MAKE = 271;
  public static final int MODEL = 272;
  public static final int STRIP_OFFSETS = 273;
  public static final int ORIENTATION = 274;
  public static final int SAMPLES_PER_PIXEL = 277;
  public static final int ROWS_PER_STRIP = 278;
  public static final int STRIP_BYTE_COUNTS = 279;
  public static final int MIN_SAMPLE_VALUE = 280;
  public static final int MAX_SAMPLE_VALUE = 281;
  public static final int X_RESOLUTION = 282;
  public static final int Y_RESOLUTION = 283;
  public static final int PLANAR_CONFIGURATION = 284;
  public static final int PAGE_NAME = 285;
  public static final int X_POSITION = 286;
  public static final int Y_POSITION = 287;
  public static final int FREE_OFFSETS = 288;
  public static final int FREE_BYTE_COUNTS = 289;
  public static final int GRAY_RESPONSE_UNIT = 290;
  public static final int GRAY_RESPONSE_CURVE = 291;
  public static final int T4_OPTIONS = 292;
  public static final int T6_OPTIONS = 293;
  public static final int RESOLUTION_UNIT = 296;
  public static final int PAGE_NUMBER = 297;
  public static final int TRANSFER_FUNCTION = 301;
  public static final int SOFTWARE = 305;
  public static final int DATE_TIME = 306;
  public static final int ARTIST = 315;
  public static final int HOST_COMPUTER = 316;
  public static final int PREDICTOR = 317;
  public static final int WHITE_POINT = 318;
  public static final int PRIMARY_CHROMATICITIES = 319;
  public static final int COLOR_MAP = 320;
  public static final int HALFTONE_HINTS = 321;
  public static final int TILE_WIDTH = 322;
  public static final int TILE_LENGTH = 323;
  public static final int TILE_OFFSETS = 324;
  public static final int TILE_BYTE_COUNTS = 325;
  public static final int INK_SET = 332;
  public static final int INK_NAMES = 333;
  public static final int NUMBER_OF_INKS = 334;
  public static final int DOT_RANGE = 336;
  public static final int TARGET_PRINTER = 337;
  public static final int EXTRA_SAMPLES = 338;
  public static final int SAMPLE_FORMAT = 339;
  public static final int S_MIN_SAMPLE_VALUE = 340;
  public static final int S_MAX_SAMPLE_VALUE = 341;
  public static final int TRANSFER_RANGE = 342;
  public static final int JPEG_PROC = 512;
  public static final int JPEG_INTERCHANGE_FORMAT = 513;
  public static final int JPEG_INTERCHANGE_FORMAT_LENGTH = 514;
  public static final int JPEG_RESTART_INTERVAL = 515;
  public static final int JPEG_LOSSLESS_PREDICTORS = 517;
  public static final int JPEG_POINT_TRANSFORMS = 518;
  public static final int JPEG_Q_TABLES = 519;
  public static final int JPEG_DC_TABLES = 520;
  public static final int JPEG_AC_TABLES = 521;
  public static final int Y_CB_CR_COEFFICIENTS = 529;
  public static final int Y_CB_CR_SUB_SAMPLING = 530;
  public static final int Y_CB_CR_POSITIONING = 531;
  public static final int REFERENCE_BLACK_WHITE = 532;
  public static final int COPYRIGHT = 33432;

  // compression types
  public static final int UNCOMPRESSED = 1;
  public static final int CCITT_1D = 2;
  public static final int GROUP_3_FAX = 3;
  public static final int GROUP_4_FAX = 4;
  public static final int LZW = 5;
  public static final int JPEG = 6;
  public static final int PACK_BITS = 32773;

  // photometric interpretation types
  public static final int WHITE_IS_ZERO = 0;
  public static final int BLACK_IS_ZERO = 1;
  public static final int RGB = 2;
  public static final int RGB_PALETTE = 3;
  public static final int TRANSPARENCY_MASK = 4;
  public static final int CMYK = 5;
  public static final int Y_CB_CR = 6;
  public static final int CIE_LAB = 8;

  // LZW compression codes
  protected static final int CLEAR_CODE = 256;
  protected static final int EOI_CODE = 257;


  // -- TiffTools API methods --

  /**
   * Tests the given data block to see if it represents
   * the first few bytes of a TIFF file.
   */
  public static boolean isValidHeader(byte[] block) {
    if (block.length < 4) return false;

    // byte order should be II or MM
    if ((block[0] != 0x49 || block[1] != 0x49) &&
      (block[0] != 0x4d || block[1] != 0x4d))
    {
      return false;
    }

    // check magic number (42)
    return block[2] == 0x00 && block[3] == 0x2a;
  }


  // -- IFD parsing methods --

  /**
   * Gets all IFDs within the given TIFF file, or null
   * if the given file is not a valid TIFF file.
   */
  public static Hashtable[] getIFDs(RandomAccessFile in) throws IOException {
    in.seek(0); // start at the beginning of the file

    // determine byte order (II = little-endian, MM = big-endian)
    byte[] order = new byte[2];
    in.read(order);
    boolean littleEndian = (order[0] == 0x49 && order[1] == 0x49);
    if (!littleEndian && (order[0] != 0x4d || order[1] != 0x4d)) return null;

    // check magic number (42)
    int magic = read2UnsignedBytes(in, littleEndian);
    if (magic != 42) return null;

    // get offset to first IFD
    long offset = read4UnsignedBytes(in, littleEndian);

    // compute maximum possible number of IFDs, for loop safety
    // each IFD must have at least one directory entry, which means that
    // each IFD must be at least 2 + 12 + 4 = 18 bytes in length
    long ifdMax = (in.length() - 8) / 18;

    // read in IFDs
    Vector v = new Vector();
    for (long ifdNum=0; ifdNum<ifdMax; ifdNum++) {
      Hashtable ifd = new Hashtable();
      v.add(ifd);

      // save little endian flag to internal LITTLE_ENDIAN tag
      ifd.put(new Integer(LITTLE_ENDIAN), new Boolean(littleEndian));

      // read in directory entries for this IFD
      in.seek(offset);
      int numEntries = read2UnsignedBytes(in, littleEndian);
      for (int i=0; i<numEntries; i++) {
        int tag = read2UnsignedBytes(in, littleEndian);
        int type = read2UnsignedBytes(in, littleEndian);
        int count = (int) read4UnsignedBytes(in, littleEndian);
        Object value = null;
        long pos = in.getFilePointer() + 4;
        if (type == 1) { // BYTE
          // 8-bit unsigned integer
          short[] bytes = new short[count];
          if (count > 4) in.seek(read4UnsignedBytes(in, littleEndian));
          for (int j=0; j<count; j++) bytes[j] = readUnsignedByte(in);
          if (bytes.length == 1) value = new Short(bytes[0]);
          else value = bytes;
        }
        else if (type == 2) { // ASCII
          // 8-bit byte that contain a 7-bit ASCII code;
          // the last byte must be NUL (binary zero)
          byte[] ascii = new byte[count];
          if (count > 4) in.seek(read4UnsignedBytes(in, littleEndian));
          in.readFully(ascii);

          // count number of null terminators
          int nullCount = 0;
          for (int j=0; j<count; j++) if (ascii[j] == 0) nullCount++;

          // convert character array to array of strings
          String[] strings = new String[nullCount];
          int c = 0, ndx = -1;
          for (int j=0; j<count; j++) {
            if (ascii[j] == 0) {
              strings[c++] = new String(ascii, ndx + 1, j - ndx - 1);
              ndx = j;
            }
          }
          if (strings.length == 1) value = strings[0];
          else value = strings;
        }
        else if (type == 3) { // SHORT
          // 16-bit (2-byte) unsigned integer
          int[] shorts = new int[count];
          if (count > 2) in.seek(read4UnsignedBytes(in, littleEndian));
          for (int j=0; j<count; j++) {
            shorts[j] = read2UnsignedBytes(in, littleEndian);
          }
          if (shorts.length == 1) value = new Integer(shorts[0]);
          else value = shorts;
        }
        else if (type == 4) { // LONG
          // 32-bit (4-byte) unsigned integer
          long[] longs = new long[count];
          if (count > 1) in.seek(read4UnsignedBytes(in, littleEndian));
          for (int j=0; j<count; j++) {
            longs[j] = read4UnsignedBytes(in, littleEndian);
          }
          if (longs.length == 1) value = new Long(longs[0]);
          else value = longs;
        }
        else if (type == 5) { // RATIONAL
          // Two LONGs: the first represents the numerator of a fraction;
          // the second, the denominator
          TiffRational[] rationals = new TiffRational[count];
          in.seek(read4UnsignedBytes(in, littleEndian));
          for (int j=0; j<count; j++) {
            long numer = read4UnsignedBytes(in, littleEndian);
            long denom = read4UnsignedBytes(in, littleEndian);
            rationals[j] = new TiffRational(numer, denom);
          }
          if (rationals.length == 1) value = rationals[0];
          else value = rationals;
        }
        else if (type == 6 || type == 7) { // SBYTE or UNDEFINED
          // SBYTE: An 8-bit signed (twos-complement) integer
          // UNDEFINED: An 8-bit byte that may contain anything,
          // depending on the definition of the field
          byte[] sbytes = new byte[count];
          if (count > 4) in.seek(read4UnsignedBytes(in, littleEndian));
          in.readFully(sbytes);
          if (sbytes.length == 1) value = new Byte(sbytes[0]);
          else value = sbytes;
        }
        else if (type == 8) { // SSHORT
          // A 16-bit (2-byte) signed (twos-complement) integer
          short[] sshorts = new short[count];
          if (count > 2) in.seek(read4UnsignedBytes(in, littleEndian));
          for (int j=0; j<count; j++) {
            sshorts[j] = read2SignedBytes(in, littleEndian);
          }
          if (sshorts.length == 1) value = new Short(sshorts[0]);
          else value = sshorts;
        }
        else if (type == 9) { // SLONG
          // A 32-bit (4-byte) signed (twos-complement) integer
          int[] slongs = new int[count];
          if (count > 1) in.seek(read4UnsignedBytes(in, littleEndian));
          for (int j=0; j<count; j++) {
            slongs[j] = read4SignedBytes(in, littleEndian);
          }
          if (slongs.length == 1) value = new Integer(slongs[0]);
          else value = slongs;
        }
        else if (type == 10) { // SRATIONAL
          // Two SLONG's: the first represents the numerator of a fraction,
          // the second the denominator
          TiffRational[] srationals = new TiffRational[count];
          in.seek(read4UnsignedBytes(in, littleEndian));
          for (int j=0; j<count; j++) {
            int numer = read4SignedBytes(in, littleEndian);
            int denom = read4SignedBytes(in, littleEndian);
            srationals[j] = new TiffRational(numer, denom);
          }
          if (srationals.length == 1) value = srationals[0];
          else value = srationals;
        }
        else if (type == 11) { // FLOAT
          // Single precision (4-byte) IEEE format
          float[] floats = new float[count];
          if (count > 1) in.seek(read4UnsignedBytes(in, littleEndian));
          for (int j=0; j<count; j++) floats[j] = readFloat(in, littleEndian);
          if (floats.length == 1) value = new Float(floats[0]);
          else value = floats;
        }
        else if (type == 12) { // DOUBLE
          // Double precision (8-byte) IEEE format
          double[] doubles = new double[count];
          in.seek(read4UnsignedBytes(in, littleEndian));
          for (int j=0; j<count; j++) {
            doubles[j] = readDouble(in, littleEndian);
          }
          if (doubles.length == 1) value = new Double(doubles[0]);
          else value = doubles;
        }
        in.seek(pos);
        if (value != null) ifd.put(new Integer(tag), value);
      }
      offset = read4UnsignedBytes(in, littleEndian);
      if (offset == 0) break;
    }

    Hashtable[] ifds = new Hashtable[v.size()];
    v.copyInto(ifds);
    return ifds;
  }

  /** Gets the name of the IFD tag encoded by the given number. */
  public static String getIFDTagName(int tag) {
    // this method uses reflection to scan the values of this class's
    // static fields, returning the first matching field's name; it is
    // probably not very efficient, and is mainly intended for debugging
    Field[] fields = TiffTools.class.getFields();
    for (int i=0; i<fields.length; i++) {
      try {
        if (fields[i].getInt(null) == tag) return fields[i].getName();
      }
      catch (Exception exc) { }
    }
    return "" + tag;
  }


  // -- Image reading methods --

  /** Reads the image defined in the given IFD from the specified file. */
  public static FlatField getImage(Hashtable ifd, RandomAccessFile in)
    throws BadFormException, IOException
  {
    if (DEBUG) debug("parsing IFD entries");

    // get internal non-IFD entries
    boolean littleEndian = ((Boolean) getIFDValue(ifd,
      LITTLE_ENDIAN, true, Boolean.class)).booleanValue();

    // get relevant IFD entries
    long imageWidth = getIFDLongValue(ifd, IMAGE_WIDTH, true, 0);
    long imageLength = getIFDLongValue(ifd, IMAGE_LENGTH, true, 0);
    int[] bitsPerSample = getIFDIntArray(ifd, BITS_PER_SAMPLE, false);
    if (bitsPerSample == null) bitsPerSample = new int[] {1};
    int samplesPerPixel = getIFDIntValue(ifd, SAMPLES_PER_PIXEL, false, 1);
    int compression = getIFDIntValue(ifd, COMPRESSION, false, UNCOMPRESSED);
    int photoInterp = getIFDIntValue(ifd, PHOTOMETRIC_INTERPRETATION, true, 0);
    long[] stripOffsets = getIFDLongArray(ifd, STRIP_OFFSETS, true);
    long rowsPerStrip = getIFDLongValue(ifd, ROWS_PER_STRIP, true, 0);
    long[] stripByteCounts = getIFDLongArray(ifd, STRIP_BYTE_COUNTS, true);
    TiffRational xResolution = getIFDRationalValue(ifd, X_RESOLUTION, false);
    TiffRational yResolution = getIFDRationalValue(ifd, Y_RESOLUTION, false);
    int planarConfig = getIFDIntValue(ifd, PLANAR_CONFIGURATION, false, 1);
    int resolutionUnit = getIFDIntValue(ifd, RESOLUTION_UNIT, false, 2);
    if (xResolution == null || yResolution == null) resolutionUnit = 0;
    int[] colorMap = getIFDIntArray(ifd, COLOR_MAP, false);
    int predictor = getIFDIntValue(ifd, PREDICTOR, false, 1);
    if (DEBUG) {
      StringBuffer sb = new StringBuffer();
      sb.append("IFD directory entry values:");
      sb.append("\n\tLittleEndian=");
      sb.append(littleEndian);
      sb.append("\n\tImageWidth=");
      sb.append(imageWidth);
      sb.append("\n\tImageLength=");
      sb.append(imageLength);
      sb.append("\n\tBitsPerSample=");
      sb.append(bitsPerSample[0]);
      for (int i=1; i<bitsPerSample.length; i++) {
        sb.append(",");
        sb.append(bitsPerSample[i]);
      }
      sb.append("\n\tSamplesPerPixel=");
      sb.append(samplesPerPixel);
      sb.append("\n\tCompression=");
      sb.append(compression);
      sb.append("\n\tPhotometricInterpretation=");
      sb.append(photoInterp);
      sb.append("\n\tStripOffsets=");
      sb.append(stripOffsets[0]);
      for (int i=1; i<stripOffsets.length; i++) {
        sb.append(",");
        sb.append(stripOffsets[i]);
      }
      sb.append("\n\tRowsPerStrip=");
      sb.append(rowsPerStrip);
      sb.append("\n\tStripByteCounts=");
      sb.append(stripByteCounts[0]);
      for (int i=1; i<stripByteCounts.length; i++) {
        sb.append(",");
        sb.append(stripByteCounts[i]);
      }
      sb.append("\n\tXResolution=");
      sb.append(xResolution);
      sb.append("\n\tYResolution=");
      sb.append(yResolution);
      sb.append("\n\tPlanarConfiguration=");
      sb.append(planarConfig);
      sb.append("\n\tResolutionUnit=");
      sb.append(resolutionUnit);
      sb.append("\n\tColorMap=");
      if (colorMap == null) sb.append("null");
      else {
        sb.append(colorMap[0]);
        for (int i=1; i<colorMap.length; i++) {
          sb.append(",");
          sb.append(colorMap[i]);
        }
      }
      sb.append("\n\tPredictor=");
      sb.append(predictor);
      debug(sb.toString());
    }

    // do some error checking
    for (int i=0; i<bitsPerSample.length; i++) {
      if (bitsPerSample[i] < 1) {
        throw new BadFormException("Illegal BitsPerSample (" +
          bitsPerSample[i] + ")");
      }
      else if (bitsPerSample[i] > 8 && bitsPerSample[i] % 8 != 0) {
        throw new BadFormException("Sorry, unsupported BitsPerSample (" +
          bitsPerSample[i] + ")");
      }
    }
    if (bitsPerSample.length != samplesPerPixel) {
      throw new BadFormException("BitsPerSample length (" +
        bitsPerSample.length + ") does not match SamplesPerPixel (" +
        samplesPerPixel + ")");
    }
    if (photoInterp == RGB_PALETTE) {
      throw new BadFormException(
        "Sorry, Palette color PhotometricInterpretation is not supported");
    }
    else if (photoInterp == TRANSPARENCY_MASK) {
      throw new BadFormException(
        "Sorry, Transparency Mask PhotometricInterpretation is not supported");
    }
    else if (photoInterp == CMYK) {
      throw new BadFormException(
        "Sorry, CMYK PhotometricInterpretation is not supported");
    }
    else if (photoInterp == Y_CB_CR) {
      throw new BadFormException(
        "Sorry, YCbCr PhotometricInterpretation is not supported");
    }
    else if (photoInterp == CIE_LAB) {
      throw new BadFormException(
        "Sorry, CIELAB PhotometricInterpretation is not supported");
    }
    else if (photoInterp != WHITE_IS_ZERO &&
      photoInterp != BLACK_IS_ZERO && photoInterp != RGB)
    {
      throw new BadFormException("Unknown PhotometricInterpretation (" +
        photoInterp + ")");
    }
    long numStrips = (imageLength + rowsPerStrip - 1) / rowsPerStrip;
    if (planarConfig == 2) numStrips *= samplesPerPixel;
    if (stripOffsets.length != numStrips) {
      throw new BadFormException("StripOffsets length (" +
        stripOffsets.length + ") does not match expected " +
        "number of strips (" + numStrips + ")");
    }
    if (stripByteCounts.length != numStrips) {
      throw new BadFormException("StripByteCounts length (" +
        stripByteCounts.length + ") does not match expected " +
        "number of strips (" + numStrips + ")");
    }
    if (imageWidth > Integer.MAX_VALUE || imageLength > Integer.MAX_VALUE ||
      imageWidth * imageLength > Integer.MAX_VALUE)
    {
      throw new BadFormException("Sorry, ImageWidth x ImageLength > " +
        Integer.MAX_VALUE + " is not supported (" +
        imageWidth + " x " + imageLength + ")");
    }
    int numSamples = (int) (imageWidth * imageLength);
    if (planarConfig == 2) {
      throw new BadFormException(
        "Sorry, \"Planar\" PlanarConfiguration is not supported");
    }
    else if (planarConfig != 1) {
      throw new BadFormException(
        "Unknown PlanarConfiguration (" + planarConfig + ")");
    }

    // read in image strips
    if (DEBUG) {
      debug("reading image data (samplesPerPixel=" +
        samplesPerPixel + "; numSamples=" + numSamples + ")");
    }
    float[][] samples = new float[samplesPerPixel][numSamples];
    for (int strip=0, row=0; strip<numStrips; strip++, row+=rowsPerStrip) {
      if (DEBUG) debug("reading image strip #" + strip);
      long actualRows = (row + rowsPerStrip > imageLength) ?
        imageLength - row : rowsPerStrip;
      in.seek(stripOffsets[strip]);
      if (stripByteCounts[strip] > Integer.MAX_VALUE) {
        throw new BadFormException("Sorry, StripByteCounts > " +
          Integer.MAX_VALUE + " is not supported");
      }
      byte[] bytes = new byte[(int) stripByteCounts[strip]];
      in.readFully(bytes);
      bytes = uncompress(bytes, compression);
      difference(bytes, bitsPerSample, imageWidth, planarConfig, predictor);
      unpackBytes(samples, (int) (imageWidth * row), bytes,
        bitsPerSample, photoInterp, colorMap, littleEndian);
    }

    // construct field
    if (DEBUG) debug("constructing field");
    RealType x = RealType.getRealType("ImageElement");
    RealType y = RealType.getRealType("ImageLine");
    RealType[] v = new RealType[samplesPerPixel];
    for (int i=0; i<samplesPerPixel; i++) {
      v[i] = RealType.getRealType("value" + i);
    }
    FlatField field = null;
    try {
      RealTupleType domain = new RealTupleType(x, y);
      RealTupleType range = new RealTupleType(v);
      FunctionType fieldType = new FunctionType(domain, range);
      Linear2DSet fieldSet = new Linear2DSet(domain, 0.0, imageWidth - 1.0,
        (int) imageWidth, imageLength - 1.0, 0.0, (int) imageLength);
      field = new FlatField(fieldType, fieldSet);
      field.setSamples(samples, false);
    }
    catch (VisADException exc) { exc.printStackTrace(); }
    return field;
  }

  /**
   * Extracts pixel information from the given byte array according to the
   * bits per sample, photometric interpretation and color map IFD directory
   * entry values, and the specified byte ordering.
   * No error checking is performed.
   */
  public static void unpackBytes(float[][] samples, int startIndex,
    byte[] bytes, int[] bitsPerSample, int photoInterp, int[] colorMap,
    boolean littleEndian) throws BadFormException
  {
    int totalBits = 0;
    for (int i=0; i<bitsPerSample.length; i++) totalBits += bitsPerSample[i];
    int sampleCount = 8 * bytes.length / totalBits;

    if (DEBUG) {
      debug("unpacking " + sampleCount + " samples (startIndex=" + startIndex +
        "; totalBits=" + totalBits + "; numBytes=" + bytes.length + ")");
    }
    if (startIndex + sampleCount > samples[0].length) {
      int trunc = startIndex + sampleCount - samples[0].length;
      if (DEBUG) debug("WARNING: truncated " + trunc + " extra samples");
      sampleCount -= trunc;
    }
    int index = 0;
    for (int j=0; j<sampleCount; j++) {
      for (int i=0; i<bitsPerSample.length; i++) {
        int numBytes = bitsPerSample[i] / 8;
        if (numBytes == 1) {
          // special case handles 8-bit data more quickly
          byte b = bytes[index++];
          samples[i][startIndex + j] = b < 0 ? 256 + b : b;
        }
        else {
          byte[] b = new byte[numBytes];
          System.arraycopy(bytes, index, b, 0, numBytes);
          index += numBytes;
          samples[i][startIndex + j] = bytesToLong(b, littleEndian);
        }
        if (photoInterp == WHITE_IS_ZERO) {
          // invert color values
          float maxValue = (float) Math.pow(8, numBytes);
          samples[i][startIndex + j] = maxValue - samples[i][startIndex + j];
        }
      }
    }
  }


  // -- Compression methods --

  /** Decodes a strip of data compressed with the given compression scheme. */
  public static byte[] uncompress(byte[] input, int compression)
    throws BadFormException, IOException
  {
    if (compression == UNCOMPRESSED) return input;
    else if (compression == CCITT_1D) {
      throw new BadFormException(
        "Sorry, CCITT Group 3 1-Dimensional Modified Huffman " +
        "run length encoding compression mode is not supported");
    }
    else if (compression == GROUP_3_FAX) {
      throw new BadFormException("Sorry, CCITT T.4 bi-level encoding " +
        "(Group 3 Fax) compression mode is not supported");
    }
    else if (compression == GROUP_4_FAX) {
      throw new BadFormException("Sorry, CCITT T.6 bi-level encoding " +
        "(Group 4 Fax) compression mode is not supported");
    }
    else if (compression == LZW) return lzwUncompress(input);
    else if (compression == JPEG) {
      throw new BadFormException(
        "Sorry, JPEG compression mode is not supported");
    }
    else if (compression == PACK_BITS) {
      throw new BadFormException("Sorry, PackBits " +
        "compression mode is not supported");
    }
    else {
      throw new BadFormException(
        "Unknown Compression type (" + compression + ")");
    }
  }

  /** Performs in-place differencing according to the given predictor value. */
  public static void difference(byte[] input, int[] bitsPerSample,
    long width, int planarConfig, int predictor) throws BadFormException
  {
    if (predictor == 2) {
      if (DEBUG) debug("performing horizontal differencing");
      for (int b=0; b<input.length; b++) {
        if (b / bitsPerSample.length % width == 0) continue;
        input[b] += input[b - bitsPerSample.length];
      }
    }
    else if (predictor != 1) {
      throw new BadFormException("Unknown Predictor (" + predictor + ")");
    }
  }

  /**
   * Decodes an LZW-compressed image strip.
   * Adapted from the TIFF 6.0 Specification:
   * http://partners.adobe.com/asn/developer/pdfs/tn/TIFF6.pdf (page 61)
   * @author Eric Kjellman egkjellman at wisc.edu
   * @author Wayne Rasband wsr at nih.gov
   */
  public static byte[] lzwUncompress(byte[] input) {
    if (input == null || input.length == 0) return input;
    if (DEBUG) debug("decompressing " + input.length + " bytes of LZW data");
    byte[][] symbolTable = new byte[4096][1];
    int bitsToRead = 9;
    int nextSymbol = 258;
    int code;
    int oldCode = -1;
    ByteVector out = new ByteVector(8192);
    BitBuffer bb = new BitBuffer(input);
    byte[] byteBuffer1 = new byte[16];
    byte[] byteBuffer2 = new byte[16];

    while (true) {
      code = bb.getBits(bitsToRead);
      if (code == EOI_CODE || code == -1) break;
      if (code == CLEAR_CODE) {
        // initialize symbol table
        for (int i = 0; i < 256; i++) symbolTable[i][0] = (byte) i;
        nextSymbol = 258;
        bitsToRead = 9;
        code = bb.getBits(bitsToRead);
        if (code == EOI_CODE || code == -1) break;
        out.add(symbolTable[code]);
        oldCode = code;
      }
      else {
        if (code < nextSymbol) {
          // code is in table
          out.add(symbolTable[code]);
          // add string to table
          ByteVector symbol = new ByteVector(byteBuffer1);
          symbol.add(symbolTable[oldCode]);
          symbol.add(symbolTable[code][0]);
          symbolTable[nextSymbol] = symbol.toByteArray(); //**
          oldCode = code;
          nextSymbol++;
        }
        else {
          // out of table
          ByteVector symbol = new ByteVector(byteBuffer2);
          symbol.add(symbolTable[oldCode]);
          symbol.add(symbolTable[oldCode][0]);
          byte[] outString = symbol.toByteArray();
          out.add(outString);
          symbolTable[nextSymbol] = outString; //**
          oldCode = code;
          nextSymbol++;
        }
        if (nextSymbol == 511) bitsToRead = 10;
        if (nextSymbol == 1023) bitsToRead = 11;
        if (nextSymbol == 2047) bitsToRead = 12;
      }
    }
    return out.toByteArray();
  }


  // -- Word-decoding convenience methods --

  /** Reads 1 signed byte [-128, 127]. */
  public static byte readSignedByte(RandomAccessFile in) throws IOException {
    byte[] b = new byte[1];
    in.readFully(b);
    return b[0];
  }

  /** Reads 1 unsigned byte [0, 255]. */
  public static short readUnsignedByte(RandomAccessFile in)
    throws IOException
  {
    short q = readSignedByte(in);
    if (q < 0) q += 256;
    return q;
  }

  /** Reads 2 signed bytes [-32768, 32767]. */
  public static short read2SignedBytes(RandomAccessFile in, boolean little)
    throws IOException
  {
    byte[] bytes = new byte[2];
    in.readFully(bytes);
    return bytesToShort(bytes, little);
  }

  /** Reads 2 unsigned bytes [0, 65535]. */
  public static int read2UnsignedBytes(RandomAccessFile in, boolean little)
    throws IOException
  {
    int q = read2SignedBytes(in, little);
    if (q < 0) q += 65536;
    return q;
  }

  /** Reads 4 signed bytes [-2147483648, 2147483647]. */
  public static int read4SignedBytes(RandomAccessFile in, boolean little)
    throws IOException
  {
    byte[] bytes = new byte[4];
    in.readFully(bytes);
    return bytesToInt(bytes, little);
  }

  /** Reads 4 unsigned bytes [0, 4294967296]. */
  public static long read4UnsignedBytes(RandomAccessFile in, boolean little)
    throws IOException
  {
    long q = read4SignedBytes(in, little);
    if (q < 0) q += 4294967296L;
    return q;
  }

  /** Reads 8 signed bytes [-9223372036854775808, 9223372036854775807]. */
  public static long read8SignedBytes(RandomAccessFile in, boolean little)
    throws IOException
  {
    byte[] bytes = new byte[8];
    in.readFully(bytes);
    return bytesToLong(bytes, little);
  }

  /** Reads 4 bytes in single precision IEEE format. */
  public static float readFloat(RandomAccessFile in, boolean little)
    throws IOException
  {
    return Float.intBitsToFloat(read4SignedBytes(in, little));
  }

  /** Reads 8 bytes in double precision IEEE format. */
  public static double readDouble(RandomAccessFile in, boolean little)
    throws IOException
  {
    return Double.longBitsToDouble(read8SignedBytes(in, little));
  }

  /**
   * Translates up to the first 2 bytes of a byte array to a short.
   * If there are fewer than 2 bytes in the array, the MSBs are all
   * assumed to be zero (regardless of endianness).
   */
  public static short bytesToShort(byte[] bytes, boolean little) {
    int len = bytes.length > 2 ? 2 : bytes.length;
    short total = 0;
    for (int i=0; i<len; i++) {
      total |= (bytes[i] < 0 ? 256 + bytes[i] : (int) bytes[i]) <<
        ((little ? i : len - i - 1) * 8);
    }
    return total;
  }

  /**
   * Translates up to the first 4 bytes of a byte array to an int.
   * If there are fewer than 4 bytes in the array, the MSBs are all
   * assumed to be zero (regardless of endianness).
   */
  public static int bytesToInt(byte[] bytes, boolean little) {
    int len = bytes.length > 4 ? 4 : bytes.length;
    int total = 0;
    for (int i=0; i<len; i++) {
      total |= (bytes[i] < 0 ? 256 + bytes[i] : (int) bytes[i]) <<
        ((little ? i : len - i - 1) * 8);
    }
    return total;
  }

  /**
   * Translates up to the first 8 bytes of a byte array to a long.
   * If there are fewer than 8 bytes in the array, the MSBs are all
   * assumed to be zero (regardless of endianness).
   */
  public static long bytesToLong(byte[] bytes, boolean little) {
    int len = bytes.length > 8 ? 8 : bytes.length;
    long total = 0;
    for (int i=0; i<len; i++) {
      total |= (bytes[i] < 0 ? 256L + bytes[i] : (long) bytes[i]) <<
        ((little ? i : len - i - 1) * 8);
    }
    return total;
  }


  // -- Methods for IFD parsing --

  /**
   * Gets the given directory entry value from the specified IFD,
   * performing some error checking.
   */
  public static Object getIFDValue(Hashtable ifd,
    int tag, boolean checkNull, Class checkClass) throws BadFormException
  {
    Object value = ifd.get(new Integer(tag));
    if (checkNull && value == null) {
      throw new BadFormException(
        getIFDTagName(tag) + " directory entry not found");
    }
    if (checkClass != null && value != null &&
      !checkClass.isInstance(value))
    {
      throw new BadFormException(getIFDTagName(tag) +
        " directory entry is the wrong type (got " +
        value.getClass().getName() + ", expected " + checkClass.getName());
    }
    return value;
  }

  /**
   * Gets the given directory entry value in long format from the
   * specified IFD, performing some error checking.
   */
  public static long getIFDLongValue(Hashtable ifd, int tag,
    boolean checkNull, long defaultValue) throws BadFormException
  {
    long value = defaultValue;
    Number number = (Number) getIFDValue(ifd, tag, checkNull, Number.class);
    if (number != null) value = number.longValue();
    return value;
  }

  /**
   * Gets the given directory entry value in int format from the
   * specified IFD, performing some error checking.
   */
  public static int getIFDIntValue(Hashtable ifd, int tag,
    boolean checkNull, int defaultValue) throws BadFormException
  {
    int value = defaultValue;
    Number number = (Number) getIFDValue(ifd, tag, checkNull, Number.class);
    if (number != null) value = number.intValue();
    return value;
  }

  /**
   * Gets the given directory entry value in rational format from the
   * specified IFD, performing some error checking.
   */
  public static TiffRational getIFDRationalValue(Hashtable ifd, int tag,
    boolean checkNull) throws BadFormException
  {
    return (TiffRational) getIFDValue(ifd, tag, checkNull, TiffRational.class);
  }

  /**
   * Gets the given directory entry values in long format
   * from the specified IFD, performing some error checking.
   */
  public static long[] getIFDLongArray(Hashtable ifd,
    int tag, boolean checkNull) throws BadFormException
  {
    Object value = getIFDValue(ifd, tag, checkNull, null);
    long[] results = null;
    if (value instanceof long[]) results = (long[]) value;
    else if (value instanceof Number) {
      results = new long[] {((Number) value).longValue()};
    }
    else if (value instanceof Number[]) {
      Number[] numbers = (Number[]) value;
      results = new long[numbers.length];
      for (int i=0; i<results.length; i++) results[i] = numbers[i].longValue();
    }
    else if (value != null) {
      throw new BadFormException(getIFDTagName(tag) +
        " directory entry is the wrong type (got " +
        value.getClass().getName() + ", expected Number, long[] or Number[])");
    }
    return results;
  }

  /**
   * Gets the given directory entry values in int format
   * from the specified IFD, performing some error checking.
   */
  public static int[] getIFDIntArray(Hashtable ifd,
    int tag, boolean checkNull) throws BadFormException
  {
    Object value = getIFDValue(ifd, tag, checkNull, null);
    int[] results = null;
    if (value instanceof int[]) results = (int[]) value;
    else if (value instanceof Number) {
      results = new int[] {((Number) value).intValue()};
    }
    else if (value instanceof Number[]) {
      Number[] numbers = (Number[]) value;
      results = new int[numbers.length];
      for (int i=0; i<results.length; i++) results[i] = numbers[i].intValue();
    }
    else if (value != null) {
      throw new BadFormException(getIFDTagName(tag) +
        " directory entry is the wrong type (got " +
        value.getClass().getName() + ", expected Number, int[] or Number[])");
    }
    return results;
  }


  // -- Debugging --

  /** Prints a debugging message with current time. */
  public static void debug(String message) {
    System.out.println(System.currentTimeMillis() + ": " + message);
  }

}
