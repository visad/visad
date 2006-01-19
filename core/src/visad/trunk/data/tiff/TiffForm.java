//
// TiffForm.java
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

import java.io.*;
import java.rmi.RemoteException;
import java.util.Hashtable;
import visad.*;
import visad.data.*;
import visad.data.bio.OMETools;
import visad.util.DataUtility;

/**
 * TiffForm is the VisAD data form for the TIFF file format.
 *
 * This form has been rewritten from scratch to stand alone without any
 * third party libraries. There are undoubtably some flavors of TIFF not
 * supported, but vanilla TIFFs should be readable.
 *
 * For now, if the form fails to read the TIFF for some reason, it
 * automatically delegates to the old logic, encapsulated in LegacyTiffForm,
 * which uses ImageJ and/or JAI to read the TIFF.
 *
 * @author Curtis Rueden ctrueden at wisc.edu
 * @author Melissa Linkert linkert at cs.wisc.edu
 */
public class TiffForm extends BaseTiffForm {

  // -- Static fields --

  /** Counter for TIFF form instantiation. */
  private static int formCount = 0;


  // -- Fields --

  /** An instance of the old TIFF form, for use if this one fails. */
  private LegacyTiffForm legacy;

  /** Flag indicating the current file requires the legacy TIFF form. */
  private boolean needLegacy;

  /** Filename of TIFF file currently being saved. */
  private String savingId;

  /** Output stream of TIFF file currently being saved. */
  private FileOutputStream out;

  /** Offset into TIFF file currently being saved. */
  private int offset;


  // -- Constructor --

  /** Constructs a new TIFF file form. */
  public TiffForm() {
    super("TiffForm" + formCount++);
    legacy = new LegacyTiffForm();
  }


  // -- TiffForm API methods --

  /**
   * Saves the given image to the specified (possibly already open) file.
   * If this image is the last one in the file, the last flag must be set.
   */
  public void saveImage(String id, FlatField image, boolean last)
    throws IOException, VisADException
  {
    saveImage(id, image, null, last);
  }

  /**
   * Saves the given image to the specified (possibly already open) file.
   * The IFD hashtable allows specification of TIFF parameters such as bit
   * depth, compression and units. If this image is the last one in the file,
   * the last flag must be set.
   */
  public void saveImage(String id, FlatField image, Hashtable ifd,
    boolean last) throws IOException, VisADException
  {
    if (!id.equals(savingId)) {
      if (out != null) {
        System.err.println("Warning: abandoning previous TIFF file (" +
          savingId + ")");
        out.close();
      }
      savingId = id;
      out = new FileOutputStream(savingId);
      DataOutputStream dataOut = new DataOutputStream(out);
      dataOut.writeByte(TiffTools.BIG);
      dataOut.writeByte(TiffTools.BIG);
      dataOut.writeShort(TiffTools.MAGIC_NUMBER);
      dataOut.writeInt(8); // offset to first IFD
      offset = 8;
    }
    offset += TiffTools.writeImage(image, ifd, out, offset, last);
    if (last) {
      out.close();
      out = null;
      savingId = null;
    }
  }


  // -- Internal BaseTiffForm API methods --

  /** Parses standard metadata. */
  protected void initStandardMetadata() {
    super.initStandardMetadata();
    metadata.put("Legacy", needLegacy ? "yes" : "no");
  }

  /** Parses OME-XML metadata. */
  protected void initOMEMetadata() {
    // check for OME-XML in TIFF comment (OME-TIFF format)
    Object o = TiffTools.getIFDValue(ifds[0], TiffTools.IMAGE_DESCRIPTION);
    Object root = o instanceof String ? OMETools.createRoot((String) o) : null;
    metadata.put("OME-TIFF", root == null ? "no" : "yes");
    if (root == null) super.initOMEMetadata();
    else ome = root;
  }


  // -- FormNode API methods --

  /**
   * Saves a VisAD Data object to a TIFF file.
   *
   * @param id        Filename of TIFF file to save.
   * @param data      VisAD Data to convert to TIFF format.
   * @param replace   Whether to overwrite an existing file.
   */
  public void save(String id, Data data, boolean replace)
    throws BadFormException, IOException, RemoteException, VisADException
  {
    FlatField[] ff = DataUtility.getImageFields(data);
    if (ff == null || ff.length == 0) {
      throw new BadFormException("Incompatible data object");
    }
    if (!replace && new File(id).exists()) {
      throw new BadFormException("File already exists");
    }
    for (int i=0; i<ff.length; i++) saveImage(id, ff[i], i == ff.length - 1);
  }


  // -- FormBlockReader API methods --

  /** Obtains the specified image from the given TIFF file. */
  public DataImpl open(String id, int block_number)
    throws BadFormException, IOException, VisADException
  {
    if (id.equals(currentId) && needLegacy) {
      return legacy.open(id, block_number);
    }
    try {
      return super.open(id, block_number);
    }
    catch (BadFormException exc) {
      if (exc.getMessage().startsWith("Sorry")) {
        if (TiffTools.DEBUG) exc.printStackTrace();
        needLegacy = true;
        return open(id, block_number);
      }
      else throw exc;
    }
  }

  /** Determines the number of images in the given TIFF file. */
  public int getBlockCount(String id)
    throws BadFormException, IOException, VisADException
  {
    if (id.equals(currentId) && needLegacy) return legacy.getBlockCount(id);
    try {
      return super.getBlockCount(id);
    }
    catch (BadFormException exc) {
      if (exc.getMessage().startsWith("Sorry")) {
        if (TiffTools.DEBUG) exc.printStackTrace();
        needLegacy = true;
        return getBlockCount(id);
      }
      else throw exc;
    }
  }

  /** Closes any open files. */
  public void close() throws BadFormException, IOException, VisADException {
    if (needLegacy) legacy.close();
    super.close();
    needLegacy = false;
  }


  // -- FormProgressInformer API methods --

  /** Gets the percentage complete of the form's current operation. */
  public double getPercentComplete() {
    if (needLegacy) return legacy.getPercentComplete();
    return super.getPercentComplete();
  }


  // -- Main method --

  /**
   * Run 'java visad.data.visad.TiffForm in_file out_file' to convert
   * in_file to out_file in TIFF data format.
   */
  public static void main(String[] args)
    throws VisADException, RemoteException, IOException
  {
    if (args == null || args.length < 1 || args.length > 2) {
      System.out.println("To convert a file to TIFF, run:");
      System.out.println("  java visad.data.tiff.TiffForm in_file out_file");
      System.out.println("To test read a TIFF file, run:");
      System.out.println("  java visad.data.tiff.TiffForm in_file");
      System.exit(2);
    }

    if (args.length == 1) {
      // Test read TIFF file
      testRead(new TiffForm(), "TIFF", args);
    }
    else if (args.length == 2) {
      // Convert file to TIFF format
      System.out.print(args[0] + " -> " + args[1] + " ");
      DefaultFamily loader = new DefaultFamily("loader");
      DataImpl data = loader.open(args[0]);
      loader = null;
      TiffForm form = new TiffForm();
      form.save(args[1], data, true);
      System.out.println("[done]");
    }
  }

}
