//
// LegacyTiffTools.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2006 Bill Hibbard, Curtis Rueden, Tom
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

import java.util.*;
import java.io.*;
import loci.formats.Compression;

/**
 * A utility class for manipulating TIFF files.
 * @author Eric Kjellman egkjellman at wisc.edu
 *
 * @deprecated Use loci.formats.TiffTools
 */
public class LegacyTiffTools {

  private static final int CLEAR_CODE = 256;
  private static final int EOI_CODE = 257;
  private static final int PHOTOMETRIC_INTERPRETATION_FIELD = 262;
  private static final int IMPOSSIBLE_IFD = 424242;

  public static Hashtable getIFDHash(RandomAccessFile readIn)
    throws IOException
  {
    byte[] byteArray = new byte[4];
    int nextOffset;
    readIn.seek(4);
    readIn.read(byteArray); // Gets the offset of the first IFD
    readIn.seek(batoi(byteArray));
    byteArray = new byte[2];
    // Gets the number of directory entries in the IFD
    readIn.read(byteArray);
    Hashtable ifdEntries = new Hashtable();
    Integer numEntries = new Integer(batoi(byteArray));
    Integer entryTag, entryType, entrycount, entryOffset;
    int frames = 1;
    int length, offset;
    Vector entryData;

    // Iterate through the directory entries
    for (int i = 0; i < numEntries.intValue(); i++) {
      byteArray = new byte[2];
      readIn.read(byteArray); // Get the entry tag
      entryTag = new Integer(batoi(byteArray));
      readIn.read(byteArray); // Get the entry type
      entryType = new Integer(batoi(byteArray));
      byteArray = new byte[4];
      // Get the number of entries this offset points to.
      readIn.read(byteArray);
      entrycount = new Integer(batoi(byteArray));
      readIn.read(byteArray); // Gets the offset for the entry
      entryOffset = new Integer(batoi(byteArray));
      // Adds the data to a vector, and then hashs it.
      entryData = new Vector();
      entryData.add(entryType);
      entryData.add(entrycount);
      entryData.add(entryOffset);
      ifdEntries.put(entryTag, entryData);
    }
    readIn.read(byteArray);
    nextOffset = batoi(byteArray);
    ifdEntries.put(new Integer(IMPOSSIBLE_IFD), new Integer(nextOffset));
    // 424242 is not possible as an IFD ID number, which are 16 bit
    return ifdEntries;
  }

  public static Hashtable getIFDHash(RandomAccessFile readIn, int block_id)
    throws IOException
  {
    Hashtable ifdEntries = new Hashtable();
    Integer entryTag, entryType, entrycount, entryOffset;
    int frames = 0;
    int length, offset;
    byte[] byteArray = new byte[4];
    int nextOffset;
    Vector entryData;
    Integer numEntries;


    readIn.seek(4);
    readIn.read(byteArray); // Gets the offset of the first IFD
    readIn.seek(batoi(byteArray));


    // Get to the IFD we want.
    while (frames != block_id) {
      byteArray = new byte[2];
      // Gets the number of directory entries in the IFD
      readIn.read(byteArray);
      numEntries = new Integer(batoi(byteArray));
      // skips the IFD
      readIn.skipBytes(12 * numEntries.intValue());
      // Get the nextOffset
      byteArray = new byte[4];
      readIn.read(byteArray);
      readIn.seek(batoi(byteArray));
      frames++;
    }

    byteArray = new byte[2];
    readIn.read(byteArray);
    numEntries = new Integer(batoi(byteArray));

    // Iterate through the directory entries
    for (int i = 0; i < numEntries.intValue(); i++) {
      byteArray = new byte[2];
      readIn.read(byteArray); // Get the entry tag
      entryTag = new Integer(batoi(byteArray));
      readIn.read(byteArray); // Get the entry type
      entryType = new Integer(batoi(byteArray));
      byteArray = new byte[4];
      // Get the number of entries this offset points to.
      readIn.read(byteArray);
      entrycount = new Integer(batoi(byteArray));
      readIn.read(byteArray); // Gets the offset for the entry
      entryOffset = new Integer(batoi(byteArray));
      // Adds the data to a vector, and then hashs it.
      entryData = new Vector();
      entryData.add(entryType);
      entryData.add(entrycount);
      entryData.add(entryOffset);
      ifdEntries.put(entryTag, entryData);
    }
    readIn.read(byteArray);
    nextOffset = batoi(byteArray);
    ifdEntries.put(new Integer(IMPOSSIBLE_IFD), new Integer(nextOffset));
    // 424242 is not possible as an IFD ID number, which are 16 bit
    return ifdEntries;
  }

  /**
   * Items in an IFD can be pointers to arrays of data, and not just single
   * items. This will return an array of int containing the data pointed to
   * in the IFD. This does not currently handle the type RATIONAL.
   */
  public static int[] getIFDArray(RandomAccessFile readIn, Vector v)
    throws IOException
  {
    int count = ((Integer) v.get(1)).intValue();
    int type = ((Integer) v.get(0)).intValue();
    if (count == 1) {
      // if the count is 1, there is no pointer, it's actual data
      return new int[] {((Integer) v.get(2)).intValue()};
    }
    else {
      readIn.seek(((Integer) v.get(2)).intValue());
      int[] toReturn = new int[count];
      int bytesPerEntry = 1;
      if (type == 1) { // BYTE
        bytesPerEntry = 1;
      }
      if (type == 2) { // ASCII
        bytesPerEntry = 1;
      }
      if (type == 3) { // SHORT
        bytesPerEntry = 2;
      }
      if (type == 4) { // LONG
        bytesPerEntry = 4;
      }
      //if (type == 5) { // RATIONAL, not supported right now.
      //  bytesPerEntry = 4;
      //}
      byte[] data = new byte[count * bytesPerEntry];
      readIn.read(data);
      byte[] translate = new byte[bytesPerEntry];
      for (int i = 0; i < count ; i++) {
        System.arraycopy(data, i * bytesPerEntry, translate, 0, bytesPerEntry);
        toReturn[i] = batoi(translate);
      }
      return toReturn;
    }
  }

  /**
   * Items in an IFD can be pointers to arrays of data, and not just single
   * items. This will return an array of int containing the data pointed to
   * in the IFD.
   */
  public static double[] getIFDRArray(RandomAccessFile readIn, Vector v)
    throws IOException
  {
    int count = ((Integer) v.get(1)).intValue();
    int type = ((Integer) v.get(0)).intValue();
    if (count == 1) {
      // if the count is 1, there is no pointer, it's actual data
      // return new int[] {((Integer) v.get(2)).intValue()};
      // This shouldn't happen: Rationals require 2 floats.
      return new double[] {-1.0D}; // TODO: Change this.
    }
    else {
      readIn.seek(((Integer) v.get(2)).intValue());
      double[] toReturn = new double[count];
      int bytesPerEntry = 8;
      int num, denom;
      if (type != 5) { // Not a rational!
        return new double[] {-1.0D}; // TODO: Change this.
      }
      byte[] data = new byte[count * bytesPerEntry];
      readIn.read(data);
      byte[] translate = new byte[bytesPerEntry];
      for (int i = 0; i < count ; i++) {
        System.arraycopy(data, i * bytesPerEntry, translate, 0, 4);
        num = batoi(translate);
        System.arraycopy(data, i * bytesPerEntry + 4, translate, 0, 4);
        denom = batoi(translate);
        toReturn[i] = num/denom;
      }
      return toReturn;
    }
  }

  public static byte[] lzwUncompress(byte[] input) throws IOException {
    try {
      return Compression.lzwUncompress(input);
    }
    catch (Exception exc) {
      return null;
    }
  }

  public static int getPhotometricInterpretation(RandomAccessFile in)
    throws IOException
  {
    Hashtable ifdHash = getIFDHash(in);
    Vector v = (Vector) ifdHash.get(
      new Integer(PHOTOMETRIC_INTERPRETATION_FIELD));
    return ((Integer) v.get(2)).intValue();
  }

  /** Translates up to the first 4 bytes of a byte array to an integer. */
  public static int batoi(byte[] inp) {
    int len = inp.length>4?4:inp.length;
    int total = 0;
    for (int i = 0; i < len; i++) {
      total += ((inp[i]<0?256+inp[i]:(int)inp[i]) << (i * 8));
    }
    return total;
  }

  public static int[] getTIFFDimensions(RandomAccessFile readIn)
    throws IOException
  {
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
    Hashtable ifdEntries = getIFDHash(readIn);

    nextOffset =
      ((Integer) ifdEntries.get(new Integer(IMPOSSIBLE_IFD))).intValue();

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

  public static int getIFDValue(Hashtable h, int id) {
    Integer k = new Integer(id);
    Vector v = (Vector) h.get(k);
    if (v == null) return -1;
    Integer i = (Integer) v.get(2);
    if (i == null) return -1;
    return i.intValue();
  }

  public static boolean isIFDArray(Hashtable h, int id) {
    return getIFDValue(h, id) == 1;
  }


  // -- Main method --

  public static void main(String args[]) throws IOException {
    Vector v;
    Integer k;
    // File f = new File(args[0]);
    RandomAccessFile f = new RandomAccessFile(args[0], "r");
    Hashtable h = new Hashtable();
    int[] d = getTIFFDimensions(f);

    for (int meh = 0; meh < d[2]; meh++) {

      System.out.println("*** START HASH #" + meh);
      h = getIFDHash(f, meh);


      for(int i = 0; i < 65536; i++) {
        k = new Integer(i);
        if(h.containsKey(k)) {
          v = (Vector) h.get(k);
          System.out.print(k + ":");
          System.out.print((Integer) v.get(0) + " ");
          System.out.print((Integer) v.get(1) + " ");
          System.out.println((Integer) v.get(2));
          if (((Integer) v.get(1)).intValue() != 1) {
            if(((Integer) v.get(1)).intValue() != 5) {
              int[] a= getIFDArray(f, v);
              System.out.print("  [ ");
              for (int j = 0; j < a.length; j++) {
                System.out.print(a[j] + " ");
              }
              System.out.println("]");
            }
            else {
              double[] a= getIFDRArray(f, v);
              System.out.print("  [ ");
              for (int j = 0; j < a.length; j++) {
                System.out.print(a[j] + " ");
              }
              System.out.println("]");
            }
          }
        }
      }
      System.out.println("*** END HASH #" + meh);
      System.out.println(" ");
    }
    int[] a = getTIFFDimensions(f);
    System.out.println(a[0] + "x" + a[1] + "x" + a[2]);
  }

}
