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
import java.rmi.RemoteException;
import java.util.Hashtable;
import visad.VisADException;
import visad.data.BadFormException;
import visad.data.tiff.BaseTiffForm;
import visad.data.tiff.TiffTools;

/**
 * FluoviewTiffForm is the VisAD data format adapter for
 * Olympus Fluoview TIFF files.
 *
 * @author Eric Kjellman egkjellman at wisc.edu
 * @author Melissa Linkert linkert at cs.wisc.edu
 * @author Curtis Rueden ctrueden at wisc.edu
 */
public class FluoviewTiffForm extends BaseTiffForm {

  // -- Constants --

  /** Maximum number of bytes to check for Fluoview header information. */
  private static final int BLOCK_CHECK_LEN = 16384;

  /** String identifying a Fluoview file. */
  private static final String FLUOVIEW_MAGIC_STRING = "FLUOVIEW";

  /** Fluoview TIFF private tags */
  private static final int MMHEADER = 34361;
  private static final int MMSTAMP = 34362;
  private static final int MMUSERBLOCK = 34386;


  // -- Static fields --

  /** Form instantiation counter. */
  private static int formCount = 0;


  // -- Constructor --

  /** Constructs a new FluoviewTiffForm file form. */
  public FluoviewTiffForm() {
    super("FluoviewTiffForm" + formCount++);
  }


  // -- Internal BaseTiffForm API methods --

  /** Populates the metadata hashtable. */
  protected void initMetadata() {
    super.initMetadata();

    try {
      Hashtable ifd = ifds[0];

      // determine byte order
      boolean little = TiffTools.isLittleEndian(ifd);

      // set file pointer to start reading MM_HEAD metadata
      short[] mmHead = TiffTools.getIFDShortArray(ifd, MMHEADER, true);
      //byte[] mmHead = new byte[tag.length];
      //for (int i=0; i<tag.length; i++) mmHead[i] = (byte) tag[i];

      int p = 0; // pointer to next byte in mmHead


      // -- Parse standard metadata --

//      System.out.println("HeaderSize: " + headerSize);
      put("HeaderSize", TiffTools.bytesToInt(mmHead, p, 2, little));
      p += 2;
      put("Status", TiffTools.bytesToString(mmHead, p, 1));
      p++;

      // change from the specs: using 257 bytes instead of 256
      put("ImageName", TiffTools.bytesToString(mmHead, p, 257));
      p += 257 + 4; // there are 4 bytes that we don't need

      put("NumberOfColors", TiffTools.bytesToLong(mmHead, p, 4, little));
      p += 4 + 8; // again, 8 bytes we don't need

      // don't add commentSize and commentOffset to hashtable
      // these will be used later to read in the Comment field
      // and add it to the hashtable
      long commentSize = TiffTools.bytesToLong(mmHead, p, 4, little);
      p += 4;
      long commentOffset = TiffTools.bytesToLong(mmHead, p, 4, little);
      p += 4;

      // dimensions info
      // there are 10 blocks of dimension info to be read,
      // each with the same structure
      // in the hashtable, the same tags in different blocks
      // are distinguished by appending the block number to the
      // tag name
      for (int j=0; j<10; j++) {
        put("DimName" + j, TiffTools.bytesToString(mmHead, p, 16));
        p += 16;
        put("Size" + j, TiffTools.bytesToLong(mmHead, p, 4, little));
        p += 4;
        put("Origin" + j, Double.longBitsToDouble(
          TiffTools.bytesToLong(mmHead, p, little)));
        p += 8;
        put("Resolution" + j, Double.longBitsToDouble(
          TiffTools.bytesToLong(mmHead, p, little)));
        p += 8;
      }

      put("MapType", TiffTools.bytesToInt(mmHead, p, 2, little));
      p += 2 + 2; // 2 bytes we don't need
      put("MapMin", Double.longBitsToDouble(
        TiffTools.bytesToLong(mmHead, p, little)));
      p += 8;
      put("MapMax", Double.longBitsToDouble(
        TiffTools.bytesToLong(mmHead, p, little)));
      p += 8;
      put("MinValue", Double.longBitsToDouble(
        TiffTools.bytesToLong(mmHead, p, little)));
      p += 8;
      put("MaxValue", Double.longBitsToDouble(
        TiffTools.bytesToLong(mmHead, p, little)));
      p += 8 + 4; // skipping over 4 bytes
      put("Gamma", Double.longBitsToDouble(
        TiffTools.bytesToLong(mmHead, p, little)));
      p += 8;
      put("Offset", Double.longBitsToDouble(
        TiffTools.bytesToLong(mmHead, p, little)));
      p += 8;

      // get Gray dimension info
      put("DimName11", TiffTools.bytesToString(mmHead, p, 16));
      p += 16;
      put("Size11", TiffTools.bytesToLong(mmHead, p, 4, little));
      p += 4;
      put("Origin11", Double.longBitsToDouble(
        TiffTools.bytesToLong(mmHead, p, little)));
      p += 8;
      put("Resolution11", Double.longBitsToDouble(
        TiffTools.bytesToLong(mmHead, p, little)));

      // read in comments field
      if (commentSize > 0) {
        in.seek(commentOffset);
        byte[] comments = new byte[(int) commentSize];
        in.read(comments);
        put("Comments", new String(comments));
      }


      // -- Parse OME-XML metadata --

      Object off;
      String data;
      long newNum = 0;
      Object obj = new Object();
      double origin = 0;

      // set file to the right place
      off = (Object) ifd.get(new Integer(MMHEADER));
      if (off != null) {
        // read the metadata
        byte[] temp1 = new byte[3];
        in.read(temp1);
        char imageType = in.readChar();
        char name[] = new char[256];
        for (int i=0; i<256; i++) {
          name[i] = in.readChar();
        }
        OMETools.setAttribute(ome, "Image", "ImageName", new String(name));
        byte[] temp2 = new byte[279];
        in.read(temp2);
        char[] dimName;
        for (int j=0; j<10; j++) {
          dimName = new char[16];
          for (int i=0; i<16; i++) {
            dimName[i] = in.readChar();
          }

          String attr = "";
          switch (j) {
            case 1: attr = "X"; break;
            case 2: attr = "Y"; break;
            case 3: attr = "Z"; break;
            case 4: attr = "T"; break;
            case 5: attr = "C"; break;
          }

          newNum = TiffTools.read4SignedBytes(in, little);
          if (j < 6) {
            OMETools.setAttribute(ome, "Pixels", "Size" + attr, "" + newNum);
          }

          origin = TiffTools.readDouble(in, little);
          if (!attr.equals("T") && !attr.equals("C") && !attr.equals("")) {
            OMETools.setAttribute(ome, "StageLabel", attr, "" + origin);
          }

          TiffTools.readDouble(in, little); // skip next double
        }
      }
    }
    catch (IOException e) { e.printStackTrace(); }
    catch (BadFormException e) { e.printStackTrace(); }
  }


  // -- FormFileInformer methods --

  /**
   * Checks if the given string is a valid filename for a Fluoview TIFF file.
   */
  public boolean isThisType(String name) {
    // just checking the filename isn't enough to differentiate between
    // Fluoview and regular TIFF; open the file and check more thoroughly
    long len = new File(name).length();
    int size = len < BLOCK_CHECK_LEN ? (int) len : BLOCK_CHECK_LEN;
    byte[] buf = new byte[size];
    try {
      FileInputStream fin = new FileInputStream(name);
      int r = 0;
      while (r < size) r += fin.read(buf, r, size - r);
      fin.close();
      return isThisType(buf);
    }
    catch (IOException e) {
      return false;
    }
  }

  /** Checks if the given block is a valid header for a Fluoview TIFF file. */
  public boolean isThisType(byte[] block) {
    if (!TiffTools.isValidHeader(block)) return false;

    // if this file is a Fluoview TIFF file, it should have 42
    // for the 3rd byte, and contain the text "FLUOVIEW"
    String test = new String(block);
    return test.indexOf(FLUOVIEW_MAGIC_STRING) != -1;
  }


  // -- Main method --

  /**
   * Run 'java visad.data.bio.FluoviewTiffForm in_file'
   * to test read an Olympus Fluoview TIFF data file.
   */
  public static void main(String[] args)
    throws VisADException, RemoteException, IOException
  {
    testRead(new FluoviewTiffForm(), "Fluoview TIFF", args);
  }

}
