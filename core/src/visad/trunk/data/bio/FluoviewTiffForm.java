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

// Heavily adapted from BioRadForm.java

package visad.data.bio;

import visad.*;
import visad.data.*;
import java.io.*;
import java.rmi.RemoteException;
import java.net.URL;
import java.util.*;

/**
 * FluoviewTiffForm is the VisAD data format adapter for
 * Olympus Fluoview TIFF files.
 *
 * @author Eric Kjellman egkjellman@wisc.edu
 */
public class FluoviewTiffForm extends Form
  implements FormFileInformer, FormProgressInformer, FormBlockReader
{

  /** Form instantiation counter. */
  private static int num = 0;


  // Private variables

  // The quantities to be displayed in x- and y-axes
  private RealType frame, row, column, pixel;
  // The function pixel = f(r,c)
  // as ( row,column -> pixel )
  private FunctionType func_rc_p;
  // The ( time -> range )
  private FunctionType func_t_range;

  private RealTupleType domain_tuple;
  private visad.Set pixelSet;
  private visad.Set timeSet;
  // The Data class FlatField, which will hold data.
  private FlatField frame_ff;
  // A FieldImpl, which will hold all data.
  private FieldImpl timeField;
  // The DataReference from the data to display
  private DataReferenceImpl data_ref;
  // The 2D display, and its the maps
  private DisplayImpl display;
  private ScalarMap timeAnimMap, timeZMap, lenXMap, ampYMap, ampRGBMap;
  private RandomAccessFile readin;
  private String current_id;
  private double percent;


  // -- Constructor --

  /** Constructs a new FluoviewTiffForm file form. */
  public FluoviewTiffForm() {
    super("FluoviewTiffForm" + num++);
  }


  // -- FormFileInformer methods --

  /** Checks if the given string is a valid filename for a Fluoview file. */
  public boolean isThisType(String name) {
    // 1 or 2 fs
    if (!(name.toLowerCase().endsWith(".tif")
       || name.toLowerCase().endsWith(".tiff"))) { return false; }
    long len = new File(name).length();
    int num = len < 16384 ? (int) len : 16384;
    byte[] buf = new byte[num];
    try {
      FileInputStream fin = new FileInputStream(name);
      int r = 0;
      while(r < num) {
        r += fin.read(buf, r, num-r);
      }
      fin.close();
      return isThisType(buf);
    } catch (IOException e) {
      return false;
    }
  }

  /**
   * Checks if the given block is a valid header for a Fluoview .tif file.
   * If it is, it should have 42 for the 3rd byte, and contain the text
   * "fluoview"
   */
  public boolean isThisType(byte[] block) {
    if (block[2] != 42) { return false; }
    String test = new String(block);
    test = test.toLowerCase();
    return (test.indexOf("fluoview") != -1);
  }

  /** Returns the default file suffixes for the Fluoview TIFF file format. */
  public String[] getDefaultSuffixes() {
    return new String[] {"tif", "tiff"};
  }


  // -- API methods --

  /**
   * Saves a VisAD Data object to Fluoview TIFF
   * format at the given location.
   *
   * @exception UnimplementedException Always thrown (this method not
   * implemented).
   */
  public void save(String id, Data data, boolean replace)
    throws BadFormException, IOException, RemoteException, VisADException
  {
    throw new UnimplementedException("FluoviewTiffForm.save");
  }

  /**
   * Adds data to an existing Fluoview TIFF file.
   *
   * @exception BadFormException Always thrown (this method not implemented).
   */
  public void add(String id, Data data, boolean replace)
    throws BadFormException
  {
    throw new BadFormException("FluoviewTiffForm.add");
  }

  /**
   * Opens an existing Fluoview TIFF file from the given location.
   *
   * @return VisAD Data object containing Fluoview TIFF data.
   * @exception BadFormException
   * @exception IOException Thrown when the file does not exist or there is a
   *            read error
   * @exception VisADException Thrown when an error occurs in the VisAD library
   */
  public DataImpl open(String id)
    throws BadFormException, IOException, VisADException
  {
    if (!id.equals(current_id)) {
      readin = new RandomAccessFile(id, "r");
      current_id = id;
    }
    percent = 0;
    frame = RealType.getRealType("frame");
    row = RealType.getRealType("row");
    column = RealType.getRealType("column");
    domain_tuple = new RealTupleType(row, column);
    pixel = RealType.getRealType("pixel");
    func_rc_p = new FunctionType(domain_tuple, pixel);
    func_t_range = new FunctionType(frame, func_rc_p);

    int[] dimensions = getFTIFFDimensions();
    pixelSet = new Integer2DSet(domain_tuple, dimensions[0], dimensions[1]);
    timeSet = new Integer1DSet(frame,  dimensions[2]);
    // Create a FlatField
    frame_ff = new FlatField(func_rc_p, pixelSet);
    // ...and a FieldImpl
    timeField = new FieldImpl(func_t_range, timeSet);

    // Populates the FieldImpl with data.
    for (int framecount = 0; framecount < dimensions[2]; framecount++) {
      percent = framecount / dimensions[2];
      float[][] flat_samples = new float[1][];
      flat_samples[0] = getFrame(framecount+1);
      frame_ff.setSamples(flat_samples);
      timeField.setSample(framecount, frame_ff);
    }

    percent = -1;
    return timeField;
  }

  /**
   * Opens an existing Fluoview TIFF file from the given URL.
   *
   * @return VisAD Data object containing Fluoview TIFF data.
   * @exception UnimplementedException Always thrown (this method not
   * implemented).
   */
  public DataImpl open(URL url)
    throws BadFormException, IOException, VisADException
  {
    throw new UnimplementedException("FluoviewTiffForm.open(URL)");
  }

  public FormNode getForms(Data data) {
    return null;
  }


  // -- FormBlockReader methods --

  /**
   * Opens the FluoviewTiff file with the file name specified
   * by id, retrieving only the frame number given.
   * @return a DataImpl containing the specified frame
   */
  public DataImpl open(String id, int block_number)
    throws BadFormException, IOException, VisADException
  {
    if (!id.equals(current_id)) {
      readin = new RandomAccessFile(id, "r");
      current_id = id;
    }
    frame = RealType.getRealType("frame");
    row = RealType.getRealType("row");
    column = RealType.getRealType("column");
    domain_tuple = new RealTupleType(row, column);
    pixel = RealType.getRealType("intensity");
    func_rc_p = new FunctionType(domain_tuple, pixel);
    func_t_range = new FunctionType(frame, func_rc_p);

    int[] dimensions = getFTIFFDimensions();
    pixelSet = new Integer2DSet(domain_tuple, dimensions[0], dimensions[1]);
    timeSet = new Integer1DSet(frame,  dimensions[2]);
    frame_ff = new FlatField(func_rc_p, pixelSet);

    float[][] flat_samples = new float[1][];
    flat_samples[0] = getFrame(block_number);
    frame_ff.setSamples(flat_samples);
    return frame_ff;
  }

  /** Returns the number of frames in the specified FluoviewTiff file. */
  public int getBlockCount(String id)
    throws BadFormException, IOException, VisADException
  {
    if (!id.equals(current_id)) {
      readin = new RandomAccessFile(id, "r");
      current_id = id;
    }
    int[] dimensions = getFTIFFDimensions();
    return dimensions[2];
  }

  /** Closes the current form. */
  public void close() {
    try {
      readin.close();
    } catch (Exception e) {}
  }


  // -- FormProgressInformer methods --

  /** Returns the percentage complete in the current operation */
  public double getPercentComplete() { return percent; }


  // -- Private Methods --

  /**
   * Returns a Hashtable containing all of the info from the first IFD of
   * the file.
   */
  private Hashtable getIFDHash() throws IOException {
    byte[] bytearray = new byte[4];
    int nextoffset;
    readin.seek(4);
    readin.read(bytearray); // Gets the offset of the first IFD
    readin.seek(batoi(bytearray));
    bytearray = new byte[2];
    // Gets the number of directory entries in the IFD
    readin.read(bytearray);
    Hashtable IFDentries = new Hashtable();
    Integer numentries = new Integer(batoi(bytearray));
    Integer entrytag, entrytype, entrycount, entryoffset;
    int frames = 1;
    int length, offset;
    Vector entrydata;

    // Iterate through the directory entries
    for (int i = 0; i < numentries.intValue(); i++) {
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
      IFDentries.put(entrytag, entrydata);
    }
    readin.read(bytearray);
    nextoffset = batoi(bytearray);
    IFDentries.put(new Integer(424242), new Integer(nextoffset));
    // 424242 is not possible as an IFD ID number, which are 16 bit
    return IFDentries;
  }

  /**
   * Returns the x, y, and z dimensions of the passed filename,
   * which is assumed to be a Fluoview TIFF. This method currently does
   * not check whether this is a Fluoview TIFF file.
   * This method does not handle URLs
   * @return an int[3] containing dimensions for the array, x, y, z
   * @throws IOException  if a file input or output exception occured
   */
  private int[] getFTIFFDimensions() throws IOException {
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
    Hashtable IFDentries = getIFDHash();

    nextoffset = ((Integer) IFDentries.get(new Integer(424242))).intValue();

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
    entrydata = (Vector) IFDentries.get(new Integer(256));
    width = (Integer) entrydata.get(2);
    // This is the directory entry for height.
    entrydata = (Vector) IFDentries.get(new Integer(257));
    length = (Integer) entrydata.get(2);
    return new int[] {width.intValue(), length.intValue(), frames};
  }

  /**
   * Returns a float[] containing image data from a file,
   * which is assumed to be a Fluoview TIFF. This method currently does
   * not check whether this is a Fluoview TIFF file.
   * The calling method should know the dimensions of this file.
   * This method does not handle URLs
   * @param framecount   the number of the frame to retrieve.
   * @return a float[][] containing the file data.
   * @throws IOException  if a file input or output exception occured
   */
  public float[] getFrame(int framecount) throws IOException {
    Hashtable IFDentries = getIFDHash();
    Vector entrydata;
    byte[] bytearray;
    int stripoffsets, stripoffsetcount, stripbytes;
    float[] toreturn;

    // This is the directory entry for strip offsets
    entrydata = (Vector) IFDentries.get(new Integer(273));
    stripoffsetcount = ((Integer) entrydata.get(1)).intValue();
    stripoffsets = ((Integer) entrydata.get(2)).intValue();
    // This is the directory entry for strip bytes
    entrydata = (Vector) IFDentries.get(new Integer(279));
    stripbytes = ((Integer) entrydata.get(2)).intValue();
    int[][] stripinfo = new int[stripoffsetcount][2];
    int current;
    int total = 0; //  /rude Java

    // If there is only one strip offset in the IFD, it will contain the data
    // itself.
    if (stripoffsetcount == 1) {
      stripinfo[0][0] = stripoffsets;
      stripinfo[0][1] = stripbytes;
      total = stripbytes;
    } else {
    // Otherwise, it will contain a pointer, and we need to read the data out
      readin.seek(stripoffsets);
      bytearray = new byte[4];
      for(int i = 0; i < stripoffsetcount; i++) {
        readin.read(bytearray);
        stripinfo[i][0] = batoi(bytearray);
      }
      readin.seek(stripbytes);
      for(int i = 0; i < stripoffsetcount; i++) {
        readin.read(bytearray);
        current = batoi(bytearray);
        stripinfo[i][1] = current;
        total += current;
      }
    }
    // Then, create the array to return, and read the data in from the
    // file.
    toreturn = new float[total/2];
    current = 0;
    for(int i = 0; i < stripoffsetcount; i++) {
      readin.seek(stripinfo[i][0]);
      bytearray = new byte[stripinfo[i][1]];
      readin.read(bytearray);
      // read reads in as bytes (signed), we need them as unsigned floats.
      for(int j = 0; j < bytearray.length; j += 2) {
        toreturn[current] =
          (float) (bytearray[j]<0?(int)256+bytearray[j]:(int)bytearray[j]) +
          ((bytearray[j+1]<0?(int)256+bytearray[j+1]:(int)bytearray[j+1])<<8);
        current++;
      }
    }
    return toreturn;
  }

  /** Translates up to the first 4 bytes of a byte array to an integer. */
  private int batoi(byte[] inp) {
    int len = inp.length>4?4:inp.length;
    int total = 0;
    for (int i = 0; i < len; i++) {
      total = total + ((inp[i]<0?(int)256+inp[i]:(int)inp[i]) << (i * 8));
    }
    return total;
  }


  // -- Main method --

  /**
   * Run 'java visad.data.bio.FluoviewTiffForm in_file out_file' to convert
   * in_file to out_file in Olympus Fluoview TIFF data format.
   */
  public static void main(String[] args)
    throws VisADException, RemoteException, IOException
  {
    if (args == null || args.length < 1 || args.length > 2) {
      System.out.println("To convert a file to Fluoview TIFF, run:");
      System.out.println(
        "  java visad.data.bio.FluoviewTiffForm in_file out_file");
      System.out.println("To test read a Fluoview TIFF file, run:");
      System.out.println("  java visad.data.bio.FluoviewTiffForm in_file");
      System.exit(2);
    }

    if (args.length == 1) {
      // Test read Fluoview TIFF file
      FluoviewTiffForm form = new FluoviewTiffForm();
      System.out.print("Reading " + args[0] + " ");
      Data data = form.open(args[0]);
      System.out.println("[done]");
      System.out.println("MathType =\n" + data.getType().prettyString());
    }
    else if (args.length == 2) {
      // Convert file to Fluoview TIFF format
      System.out.print(args[0] + " -> " + args[1] + " ");
      DefaultFamily loader = new DefaultFamily("loader");
      DataImpl data = loader.open(args[0]);
      loader = null;
      FluoviewTiffForm form = new FluoviewTiffForm();
      form.save(args[1], data, true);
      System.out.println("[done]");
    }
    System.exit(0);
  }

}

// Add an initialize method which creates a list of all the IFD offsets
// Eventually should also return metadata with the FieldImpl
