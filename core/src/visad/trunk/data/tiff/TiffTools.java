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

import java.util.*;
import java.io.*;

// TODO: Actually document this.
// This is still being modified and added to.

public class TiffTools {

  private static final int CLEAR_CODE = 256;
  private static final int EOI_CODE = 257;
  private static final int PHOTOMETRIC_INTERPRETATION_FIELD = 262;

  public static Hashtable getIFDHash(RandomAccessFile readin)
    throws IOException
  {
    byte[] bytearray = new byte[4];
    int nextoffset;
    readin.seek(4);
    readin.read(bytearray); // Gets the offset of the first IFD
    readin.seek(batoi(bytearray));
    bytearray = new byte[2];
    // Gets the number of directory entries in the IFD
    readin.read(bytearray);
    Hashtable ifdEntries = new Hashtable();
    Integer numentries = new Integer(batoi(bytearray));
    Integer entrytag, entrytype, entrycount, entryoffset;
    int frames = 1;
    int length, offset;
    Vector entrydata;

    // Iterate through the directory entries
    for (int i=0; i<numentries.intValue(); i++) {
      bytearray = new byte[2];
      readin.read(bytearray); // Get the entry tag
      entrytag = new Integer(batoi(bytearray));
      readin.read(bytearray); // Get the entry type
      entrytype = new Integer(batoi(bytearray));
      bytearray = new byte[4];
      // Get the number of entries this offset points to.
      readin.read(bytearray);
      entrycount = new Integer(batoi(bytearray));
      readin.read(bytearray); // Gets the offset for the entry
      entryoffset = new Integer(batoi(bytearray));
      // Adds the data to a vector, and then hashs it.
      entrydata = new Vector();
      entrydata.add(entrytype);
      entrydata.add(entrycount);
      entrydata.add(entryoffset);
      ifdEntries.put(entrytag, entrydata);
    }
    readin.read(bytearray);
    nextoffset = batoi(bytearray);
    ifdEntries.put(new Integer(424242), new Integer(nextoffset));
    // 424242 is not possible as an IFD ID number, which are 16 bit
    return ifdEntries;
  }

  public static int[] getIFDArray(RandomAccessFile readin, Vector v)
    throws IOException
  {
    // Items in an IFD can be pointers to arrays of data, and not just single
    // items. This will return an array of int containing the data pointed to
    // in the IFD
    // This does not currently handle the type RATIONAL.

    int count = ((Integer) v.get(1)).intValue();
    int type = ((Integer) v.get(0)).intValue();
    if (count == 1) { // there is no pointer, it's actual data
      return new int[] {((Integer) v.get(2)).intValue()};
    } else {
      readin.seek(((Integer) v.get(2)).intValue());
      int[] toreturn = new int[count];
      int bytesperentry = 1;
      if (type == 1) { // BYTE
        bytesperentry = 1;
      }
      if (type == 2) { // ASCII
        bytesperentry = 1;
      }
      if (type == 3) { // SHORT
        bytesperentry = 2;
      }
      if (type == 4) { // LONG
        bytesperentry = 4;
      }
      //if (type == 5) { // RATIONAL, not supported right now.
      //  bytesperentry = 4;
      //}
      byte[] data = new byte[count * bytesperentry];
      readin.read(data);
      byte[] translate = new byte[bytesperentry];
      for (int i = 0; i < count ; i++) {
        System.arraycopy(data, i * bytesperentry, translate, 0, bytesperentry);
        toreturn[i] = batoi(translate);
      }
      return toreturn;
    }
  }

  public static double[] getIFDRArray(RandomAccessFile readin, Vector v)
    throws IOException
  {
    // Items in an IFD can be pointers to arrays of data, and not just single
    // items. This will return an array of int containing the data pointed to
    // in the IFD

    int count = ((Integer) v.get(1)).intValue();
    int type = ((Integer) v.get(0)).intValue();
    if (count == 1) { // there is no pointer, it's actual data
      // return new int[] {((Integer) v.get(2)).intValue()};
      // This shouldn't happen: Rationals require 2 floats.
      return new double[] {-1.0D}; // TODO: Change this.
    } else {
      readin.seek(((Integer) v.get(2)).intValue());
      double[] toreturn = new double[count];
      int bytesperentry = 8;
      int num, denom;
      if (type != 5) { // Not a rational!
        return new double[] {-1.0D}; // TODO: Change this.
      }
      byte[] data = new byte[count * bytesperentry];
      readin.read(data);
      byte[] translate = new byte[bytesperentry];
      for (int i = 0; i < count ; i++) {
        System.arraycopy(data, i * bytesperentry, translate, 0, 4);
        num = batoi(translate);
        System.arraycopy(data, i * bytesperentry + 4, translate, 0, 4);
        denom = batoi(translate);
        toreturn[i] = num/denom;
      }
      return toreturn;
    }
  }

  public static byte[] lzwUncompress(byte[] input) throws IOException {
    // Adapted from the TIFF 6.0 Specification
    // http://partners.adobe.com/asn/developer/pdfs/tn/TIFF6.pdf page 61
    byte[][] symboltable = new byte[4096][];
    int bitstoread = 9;
    int nextsymbol = 258;
    int currentcode;
    int oldcode = -1;
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    ByteArrayOutputStream symbol = new ByteArrayOutputStream();
    BitBuffer bb = new BitBuffer(new ByteArrayInputStream(input));
    int readcode;
    // initialize the symbol table
    for (int i = 0; i < 256; i++) {
      symboltable[i] = new byte[] { (byte) i };
    }
    // Handle the first character, since this causes problems somehow.
    currentcode = bb.getBits(bitstoread);
    if (currentcode == EOI_CODE) {
      return out.toByteArray();
    } else if (currentcode == CLEAR_CODE) { // ignore, already done
    } else { // the first character will be in the table, so:
          // System.out.println("In table (first!)");
          out.write(symboltable[currentcode], 0,
            symboltable[currentcode].length);
          //symbol.reset();
          //symbol.write(symboltable[oldcode], 0, symboltable[oldcode].length);
          //symbol.write(symboltable[currentcode], 0, 1);
          //symboltable[nextsymbol] = symbol.toByteArray();
          //nextsymbol++;
          oldcode = currentcode;
    }
    while((currentcode = bb.getBits(bitstoread)) != EOI_CODE) {
      if (currentcode == CLEAR_CODE) {
        symboltable = new byte[4096][];
        for (int i = 0; i < 256; i++) {
          symboltable[i] = new byte[] { (byte) i };
        }
        nextsymbol = 258;
        bitstoread = 9;
        currentcode = bb.getBits(bitstoread);
        if (currentcode == EOI_CODE) {
          break;
        }
        out.write(symboltable[currentcode], 0,
          symboltable[currentcode].length);
        oldcode = currentcode;
      } else {
        // System.out.println("Handling: " + currentcode + " " + nextsymbol);
        // System.out.print("Current: " + currentcode + " Old: " + oldcode);
        if (currentcode < nextsymbol) {
          // System.out.println("In table");
          out.write(symboltable[currentcode], 0,
            symboltable[currentcode].length);
          // System.out.print(" Out: ");
          for (int j = 0; j < symboltable[currentcode].length ; j++) {
            // System.out.print(symboltable[currentcode][j]);
          }

          symbol.reset();
          symbol.write(symboltable[oldcode], 0, symboltable[oldcode].length);
          symbol.write(symboltable[currentcode], 0, 1);
          symboltable[nextsymbol] = symbol.toByteArray();
          for (int d = 0; d < symboltable[nextsymbol].length ; d++) {
            // System.out.print(symboltable[nextsymbol][d]);
          }
          // System.out.println(" ");
          nextsymbol++;
          oldcode = currentcode;
        } else {
          // System.out.println("Out of table");
          // System.out.println("Code: " + oldcode);
          out.write(symboltable[oldcode], 0, symboltable[oldcode].length);
          out.write(symboltable[oldcode], 0, 1); // don't think this is right
          symbol.reset();
          symbol.write(symboltable[oldcode], 0, symboltable[oldcode].length);
          symbol.write(symboltable[oldcode], 0, 1); // may not be correct
          symboltable[nextsymbol] = symbol.toByteArray();
          oldcode=currentcode;
          nextsymbol++;
        }
        if (nextsymbol == 510) { bitstoread = 10; }
        if (nextsymbol == 1022) { bitstoread = 11; }
        if (nextsymbol == 2046) { bitstoread = 12; }
      }
    }
    for (int i = 258 ; i < nextsymbol ; i++) {
      // System.out.print(i + ": ");
      for (int j = 0; j < symboltable[i].length ; j++) {
        // System.out.print(symboltable[i][j]);
      }
      // System.out.println();
    }
    return out.toByteArray();
  }

  public static int getPhotometricInterpretation(RandomAccessFile in)
    throws IOException
  {
    Hashtable ifdHash = getIFDHash(in);
    Vector v = (Vector) ifdHash.get(
      new Integer(PHOTOMETRIC_INTERPRETATION_FIELD));
    return ((Integer) v.get(2)).intValue();
  }

  public static int batoi(byte[] inp) {
    // Translates up to the first 4 bytes of a byte array to an integer
    int len = inp.length>4?4:inp.length;
    int total = 0;
    for (int i = 0; i < len; i++) {
      total = total + ((inp[i]<0?(int)256+inp[i]:(int)inp[i]) << (i * 8));
    }
    return total;
  }

  public static int[] getTIFFDimensions(RandomAccessFile readin)
    throws IOException
  {
    // For this one, we're going to read the entire IFD, get the x and y
    // coordinates out of it, and then just pass through the other IFDs to get
    // z. It is conceivable that the various images are of different sizes,
    // but for now I'm going to assume that they are not.
    byte[] bytearray;
    int nextoffset;
    int numentries;
    int frames = 1;
    Integer width, length;
    Vector entrydata;
    Hashtable ifdEntries = getIFDHash(readin);

    nextoffset = ((Integer) ifdEntries.get(new Integer(424242))).intValue();

    while (nextoffset != 0) {
      frames++;
      try {
        readin.seek(nextoffset);
      } catch (Exception e) {
        e.printStackTrace();
      }
      bytearray = new byte[2];
      readin.read(bytearray); // Get the number of directory entries in the IFD
      numentries = batoi(bytearray);
      readin.skipBytes(12 * numentries);
      bytearray = new byte[4];
      readin.read(bytearray);
      nextoffset = batoi(bytearray);
    }

    // This is the directory entry for width.
    entrydata = (Vector) ifdEntries.get(new Integer(256));
    if (entrydata == null) {
      return null;
    }
    width = (Integer) entrydata.get(2);
    // This is the directory entry for height.
    entrydata = (Vector) ifdEntries.get(new Integer(257));
    if (entrydata == null) {
      return null;
    }
    length = (Integer) entrydata.get(2);
    return new int[] {width.intValue(), length.intValue(), frames};
  }

  public static void main(String args[]) throws IOException {
    Vector v;
    Integer k;
    // File f = new File(args[0]);
    RandomAccessFile f = new RandomAccessFile(args[0], "r");
    Hashtable h = new Hashtable();
    h = getIFDHash(f);


    for(int i=0; i<65536; i++) {
      k = new Integer(i);
      if (h.containsKey(k)) {
        v = (Vector) h.get(k);
        System.out.print(k + ":");
        System.out.print((Integer) v.get(0) + " ");
        System.out.print((Integer) v.get(1) + " ");
        System.out.println((Integer) v.get(2));
        if (((Integer) v.get(1)).intValue() != 1) {
          if (((Integer) v.get(1)).intValue() != 5) {
            int[] a= getIFDArray(f, v);
            System.out.print("  [ ");
            for (int j = 0; j < a.length; j++) {
              System.out.print(a[j] + " ");
            }
            System.out.println("]");
          } else {
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

    int[] a = getTIFFDimensions(f);
    System.out.println(a[0] + "x" + a[1] + "x" + a[2]);
  }

  public static boolean isIFDArray(Hashtable h, int id) {
    Integer k = new Integer(id);
    Vector v = (Vector) h.get(k);
    if (v == null) {
      return false;
    }
    return (((Integer) v.get(1)).intValue() == 1);
  }

  public static int getIFDValue(Hashtable h, int id) {
    Integer k = new Integer(id);
    Vector v = (Vector) h.get(k);
    if (v == null) {
      return -1;
    }
    return ((Integer) v.get(2)).intValue();
  }

}
