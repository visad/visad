//
// TiffForm.java
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

import java.io.*;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.Hashtable;
import visad.*;
import visad.data.*;

/**
 * TiffForm is the VisAD data form for the TIFF file format.
 *
 * This form has been rewritten from scratch to stand alone without any
 * third party libraries. There are undoubtably some flavors of TIFF not
 * supported, but vanilla TIFFs should be readable.
 */
public class TiffForm extends Form implements FormFileInformer,
  FormBlockReader, FormProgressInformer
{

  // -- Static fields --

  /** Counter for TIFF form instantiation. */
  private static int formCount = 0;

  /** Legal TIFF SUFFIXES. */
  private static final String[] SUFFIXES = { "tif", "tiff" };


  // -- Fields --

  /** Filename of the current TIFF. */
  private String currentId;

  /** Random access file for the current TIFF. */
  private RandomAccessFile in;

  /** List of IFDs for the current TIFF. */
  private Hashtable[] ifds;

  /** Number of images in the current TIFF stack. */
  private int numImages;

  /** Percent complete with current operation. */
  private double percent;

  /** An instance of the old TIFF form, for use if this one fails. */
  private LegacyTiffForm legacy;

  /** Flag indicating the current file requires the legacy TIFF form. */
  private boolean needLegacy;


  // -- Constructor --

  /** Constructs a new TIFF file form. */
  public TiffForm() {
    super("TiffForm" + formCount++);
    legacy = new LegacyTiffForm();
  }


  // -- TiffForm API methods --

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
    // save is not yet implemented; delegate to legacy form
    legacy.save(id, data, replace);
  }

  /**
   * Adds data to an existing TIFF file.
   *
   * @exception BadFormException Always thrown (method is not implemented).
   */
  public void add(String id, Data data, boolean replace)
    throws BadFormException
  {
    throw new BadFormException("TiffForm.add");
  }

  /**
   * Opens an existing TIFF file from the given filename.
   *
   * @return VisAD Data object containing TIFF data.
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
      FunctionType timeFunction = new FunctionType(time, fields[0].getType());
      Integer1DSet timeSet = new Integer1DSet(nImages);
      FieldImpl timeField = new FieldImpl(timeFunction, timeSet);
      timeField.setSamples(fields, false);
      data = timeField;
    }
    close();
    percent = -1;
    return data;
  }

  /**
   * Opens an existing TIFF file from the given URL.
   *
   * @return VisAD Data object containing TIFF data.
   *
   * @exception BadFormException Always thrown (method is not implemented).
   */
  public DataImpl open(URL url)
    throws BadFormException, IOException, VisADException
  {
    throw new BadFormException("TiffForm.open(URL)");
  }

  /** Returns the data forms that are compatible with the given data object. */
  public FormNode getForms(Data data) {
    return null;
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
      if (!id.equals(currentId)) initFile(id);

      if (block_number < 0 || block_number >= numImages) {
        throw new BadFormException("Invalid image number: " + block_number);
      }

      return TiffTools.getImage(ifds[block_number], in);
    }
    catch (BadFormException exc) {
      if (exc.getMessage().startsWith("Sorry")) {
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
      if (!id.equals(currentId)) initFile(id);
      return numImages;
    }
    catch (BadFormException exc) {
      if (exc.getMessage().startsWith("Sorry")) {
        needLegacy = true;
        return getBlockCount(id);
      }
      else throw exc;
    }
  }

  /** Closes any open files. */
  public void close() throws BadFormException, IOException, VisADException {
    if (needLegacy) legacy.close();
    if (in != null) in.close();
    in = null;
    ifds = null;
    currentId = null;
    needLegacy = false;
  }


  // -- FormFileInformer API methods --

  /** Checks if the given string is a valid filename for a TIFF file. */
  public boolean isThisType(String name) {
    for (int i=0; i<SUFFIXES.length; i++) {
      if (name.toLowerCase().endsWith(SUFFIXES[i])) return true;
    }
    return false;
  }

  /** Checks if the given block is a valid header for a TIFF file. */
  public boolean isThisType(byte[] block) {
    return TiffTools.isValidHeader(block);
  }

  /** Returns the default file SUFFIXES for the TIFF file format. */
  public String[] getDefaultSuffixes() {
    String[] s = new String[SUFFIXES.length];
    System.arraycopy(SUFFIXES, 0, s, 0, SUFFIXES.length);
    return s;
  }


  // -- FormProgressInformer API methods --

  /** Gets the percentage complete of the form's current operation. */
  public double getPercentComplete() {
    if (needLegacy) return legacy.getPercentComplete();
    return percent;
  }


  // -- Helper methods --

  /** Initializes the given TIFF file. */
  private void initFile(String id)
    throws BadFormException, IOException, VisADException
  {
    close();
    currentId = id;
    in = new RandomAccessFile(id, "r");
    ifds = TiffTools.getIFDs(in);
    numImages = ifds.length;
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
      TiffForm form = new TiffForm();
      System.out.print("Reading " + args[0] + " ");
      Data data = form.open(args[0]);
      System.out.println("[done]");
      System.out.println("MathType =\n" + data.getType().prettyString());
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
    System.exit(0);
  }

}
