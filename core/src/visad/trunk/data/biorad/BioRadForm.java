//
// BioRadForm.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2000 Bill Hibbard, Curtis Rueden, Tom
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

package visad.data.biorad;

import visad.*;
import visad.data.*;
import java.io.*;
import java.rmi.RemoteException;
import java.net.URL;
import java.util.Vector;
import visad.data.DefaultFamily;

/** BioRadForm is the VisAD data format adapter for Bio-Rad .PIC files. */
public class BioRadForm extends Form implements FormFileInformer {

  /** Debugging flag. */
  static final boolean DEBUG = false;

  static final int DEBUG_LEVEL = 1;

  /** Numerical ID of a valid BioRad .PIC file. */
  private static final int PIC_FILE_ID = 12345;


  // Merge types

  /** Image is not merged. */
  private static final int MERGE_OFF = 0;

  /** All pixels merged, 16 color (4-bit). */
  private static final int MERGE_16 = 1;

  /** Alternate pixels merged, 128 color (7-bit). */
  private static final int MERGE_ALTERNATE = 2;

  /** Alternate columns merged. */
  private static final int MERGE_COLUMN = 3;

  /** Alternate rows merged. */
  private static final int MERGE_ROW = 4;

  /** Maximum pixels merged. */
  private static final int MERGE_MAXIMUM = 5;

  /** 64-color (12-bit) optimized 2-image merge. */
  private static final int MERGE_OPT12 = 6;

  /**
   * As above except convert look up table saved after the notes in file,
   * as opposed to at the end of each image data.
   */
  private static final int MERGE_OPT12_V2 = 7;

  /** List of merge types. */
  private static final String[] mergeNames = {
    "MERGE_OFF", "MERGE_16", "MERGE_ALTERNATE", "MERGE_COLUMN",
    "MERGE_ROW", "MERGE_MAXIMUM", "MERGE_OPT12", "MERGE_OPT12_V2"
  };


  // Look-up table constants

  /** A red pane appears on the screen. */
  private static final int RED_LUT = 0x01;

  /** A green pane appears on the screen. */
  private static final int GREEN_LUT = 0x02;

  /** A blue pane appears on the screen. */
  private static final int BLUE_LUT = 0x04;


  /** Form instantiation counter. */
  private static int num = 0;

  /** Constructs a new BioRad file form. */
  public BioRadForm() {
    super("BioRadForm" + num++);
  }

  /** Converts two bytes to an unsigned short. */
  private int getUnsignedShort(byte b1, byte b2) {
    int i1 = 0x000000ff & b1;
    int i2 = 0x000000ff & b2;
    return (i2 << 8) | i1;
  }

  /** Converts four bytes to a float. */
  private float getFloat(byte b1, byte b2, byte b3, byte b4) {
    int i1 = 0x000000ff & b1;
    int i2 = 0x000000ff & b2;
    int i3 = 0x000000ff & b3;
    int i4 = 0x000000ff & b4;
    int bits = (i4 << 24) | (i3 << 16) | (i2 << 8) | i1;
    return Float.intBitsToFloat(bits);
  }

  /** Checks if the given string is a valid filename for a BioRad .PIC file. */
  public boolean isThisType(String name) {
    return name.toLowerCase().endsWith(".pic");
  }

  /** Checks if the given block is a valid header for a BioRad .PIC file. */
  public boolean isThisType(byte[] block) {
    if (block.length < 56) return false;
    return getUnsignedShort(block[54], block[55]) == PIC_FILE_ID;
  }

  /** Returns the default file suffixes for the BioRad .PIC file format. */
  public String[] getDefaultSuffixes() {
    return new String[] {"pic"};
  }

  /** Saves a VisAD Data object to BioRad .PIC format at the given location. */
  public void save(String id, Data data, boolean replace)
    throws BadFormException, IOException, RemoteException, VisADException
  {
    // CTR: TODO
    throw new UnimplementedException("BioRadForm.save");
  }

  /**
   * Adds data to an existing BioRad file.
   *
   * @exception BadFormException Always thrown (this method not implemented).
   */
  public void add(String id, Data data, boolean replace)
    throws BadFormException
  {
    throw new BadFormException("BioRadForm.add");
  }

  /**
   * Opens an existing BioRad .PIC file from the given location.
   *
   * @return VisAD Data object containing BioRad data.
   */
  public DataImpl open(String id)
    throws BadFormException, IOException, VisADException
  {
    return open(new FileInputStream(id));
  }

  /**
   * Opens an existing BioRad .PIC file from the given URL.
   *
   * @return VisAD Data object containing BioRad data.
   */
  public DataImpl open(URL url)
    throws BadFormException, VisADException, IOException
  {
    return open(url.openStream());
  }

  /** Reads in BioRad .PIC file data from the given input stream. */
  private DataImpl open(InputStream in)
    throws BadFormException, IOException, VisADException
  {
    DataInputStream fin = new DataInputStream(in);

    // read header
    byte[] header = new byte[76];
    fin.read(header, 0, 76);
    int nx = getUnsignedShort(header[0], header[1]);
    int ny = getUnsignedShort(header[2], header[3]);
    int npic = getUnsignedShort(header[4], header[5]);
    int ramp1_min = getUnsignedShort(header[6], header[7]);
    int ramp1_max = getUnsignedShort(header[8], header[9]);
    boolean notes = (header[10] | header[11] | header[12] | header[13]) != 0;
    boolean byte_format = getUnsignedShort(header[14], header[15]) != 0;
    int image_number = getUnsignedShort(header[16], header[17]);
    String name = new String(header, 18, 32);
    int merged = getUnsignedShort(header[50], header[51]);
    int color1 = getUnsignedShort(header[52], header[53]);
    int file_id = getUnsignedShort(header[54], header[55]);
    int ramp2_min = getUnsignedShort(header[56], header[57]);
    int ramp2_max = getUnsignedShort(header[58], header[59]);
    int color2 = getUnsignedShort(header[60], header[61]);
    int edited = getUnsignedShort(header[62], header[63]);
    int lens = getUnsignedShort(header[64], header[65]);
    float mag_factor =
      getFloat(header[66], header[67], header[68], header[69]);
    if (DEBUG && DEBUG_LEVEL >= 2) {
      System.out.println("\nBioRad header:\n" +
        "nx = " + nx + "\n" +
        "ny = " + ny + "\n" +
        "npic = " + npic + "\n" +
        "ramp1_min = " + ramp1_min + "\n" +
        "ramp1_max = " + ramp1_max + "\n" +
        "notes = " + notes + "\n" +
        "byte_format = " + byte_format + "\n" +
        "image_number = " + image_number + "\n" +
        "name = " + name + "\n" +
        "merged = " + mergeNames[merged] + "\n" +
        "color1 = " + color1 + "\n" +
        "file_id = " + file_id + "\n" +
        "ramp2_min = " + ramp2_min + "\n" +
        "ramp2_max = " + ramp2_max + "\n" +
        "color2 = " + color2 + "\n" +
        "edited = " + edited + "\n" +
        "lens = " + lens + "\n" +
        "mag_factor = " + mag_factor);
    }

    // check validity of header
    if (file_id != PIC_FILE_ID) {
      throw new BadFormException("Invalid file header: " + file_id);
    }

    // read image data
    int image_len = nx * ny;
    byte[][] image_data = new byte[npic][image_len];
    if (byte_format) {
      // read in image_len bytes
      image_data = new byte[npic][image_len];
      for (int i=0; i<npic; i++) fin.read(image_data[i], 0, image_len);
    }
    else {
      // read in 2 * image_len bytes
      image_data = new byte[npic][2 * image_len];
      for (int i=0; i<npic; i++) fin.read(image_data[i], 0, 2 * image_len);
    }

    // read notes
    Vector noteList = new Vector();
    while (notes) {
      // read in note
      byte[] note = new byte[96];
      fin.read(note, 0, 96);
      int level = getUnsignedShort(note[0], note[1]);
      notes = (note[2] | note[3] | note[4] | note[5]) != 0;
      int num = getUnsignedShort(note[6], note[7]);
      int status = getUnsignedShort(note[8], note[9]);
      int type = getUnsignedShort(note[10], note[11]);
      int x = getUnsignedShort(note[12], note[13]);
      int y = getUnsignedShort(note[14], note[15]);
      String text = new String(note, 16, 80);
      noteList.add(new BioRadNote(level, num, status, type, x, y, text));
    }

    // read color table
    int numLuts = 0;
    byte[][] lut = new byte[3][768];
    try {
      fin.read(lut[numLuts]);
      numLuts++;
    }
    catch (IOException exc) { } // eof
    if (DEBUG && DEBUG_LEVEL >= 2) {
      System.out.println(numLuts + " color table" +
        (numLuts == 1 ? "" : "s") + " present.");
    }

    // close file
    fin.close();

    // get basic note information
    int len = noteList.size();
    DataImpl[] noteData = new DataImpl[len];
    for (int i=0; i<len; i++) {
      BioRadNote note = (BioRadNote) noteList.elementAt(i);
      noteData[i] = note.getNoteData();
    }
    RealType noteIndex = RealType.getRealType("NoteIndex");
    FunctionType noteFunction =
      new FunctionType(noteIndex, BioRadNote.noteTuple);
    Integer1DSet noteSet = new Integer1DSet(len);
    FieldImpl noteField = new FieldImpl(noteFunction, noteSet);
    noteField.setSamples(noteData, false);

    // extract horizontal and vertical unit information from notes
    Unit horizUnit = null, vertUnit = null;
    int i = 0;
    while (i < noteList.size()) {
      BioRadNote note = (BioRadNote) noteList.elementAt(i);
      if (note.hasUnitInfo()) {
        int rval = note.analyze();
        if (rval == BioRadNote.HORIZ_UNIT) {
          if (horizUnit == null) horizUnit = note.unit;
          else if (DEBUG && DEBUG_LEVEL >= 1) {
            System.err.println("Warning: ignoring extra AXIS_2 note");
          }
          noteList.remove(i);
        }
        else if (rval == BioRadNote.VERT_UNIT) {
          if (vertUnit == null) vertUnit = note.unit;
          else if (DEBUG && DEBUG_LEVEL >= 1) {
            System.err.println("Warning: ignoring extra AXIS_3 note");
          }
          noteList.remove(i);
        }
        else if (rval == BioRadNote.INVALID_NOTE ||
          rval == BioRadNote.NO_INFORMATION)
        {
          noteList.remove(i);
        }
        else i++;
      }
      else i++;
    }

    // convert image bytes to floats
    float[][][] samples = new float[npic][1][image_len];
    if (byte_format) {
      // each pixel is 8 bits
      for (i=0; i<npic; i++) {
        for (int l=0; l<image_len; l++) {
          int q = 0x000000ff & image_data[i][l];
          samples[i][0][l] = (float) q;
        }
      }
    }
    else {
      // each pixel is 16 bits
      for (i=0; i<npic; i++) {
        for (int l=0; l<image_len; l++) {
          int q = getUnsignedShort(
            image_data[i][2 * l], image_data[i][2 * l + 1]);
          samples[i][0][l] = (float) q;
        }
      }
    }

    // convert color table bytes to floats
    float[][][] colors = new float[numLuts][3][256];
    for (i=0; i<numLuts; i++) {
      for (int l=0; l<256; l++) {
        int qr = 0x000000ff & lut[i][l];
        int qg = 0x000000ff & lut[i][l + 256];
        int qb = 0x000000ff & lut[i][l + 512];
        colors[i][0][l] = (float) qr;
        colors[i][1][l] = (float) qg;
        colors[i][2][l] = (float) qb;
      }
    }

    // set up image data types
    RealType time = RealType.getRealType("time");
    RealType x = RealType.getRealType("ImageElement");
    RealType y = RealType.getRealType("ImageLine");
    RealType value = RealType.getRealType("value");
    RealTupleType xy = new RealTupleType(x, y);
    FunctionType imageFunction = new FunctionType(xy, value);
    FunctionType timeFunction = new FunctionType(time, imageFunction);

    // set up image domain sets
    Integer2DSet imageSet = new Integer2DSet(RealTupleType.Generic2D,
      nx, ny, null, new Unit[] {horizUnit, vertUnit}, null);
    Integer1DSet timeSet = new Integer1DSet(npic);

    // set up image fields
    FlatField[] imageFields = new FlatField[npic];
    for (i=0; i<npic; i++) {
      imageFields[i] = new FlatField(imageFunction, imageSet);
      imageFields[i].setSamples(samples[i], false);
    }
    FieldImpl timeField = new FieldImpl(timeFunction, timeSet);
    timeField.setSamples(imageFields, false);

    FieldImpl colorField = null;
    if (numLuts > 0) {
      // set up color table data types
      RealType tableNum = RealType.getRealType("TableNumber");
      RealType red = RealType.getRealType("Red");
      RealType green = RealType.getRealType("Green");
      RealType blue = RealType.getRealType("Blue");
      RealTupleType rgb = new RealTupleType(red, green, blue);
      FunctionType rgbFunction = new FunctionType(value, rgb);
      FunctionType colorFunction = new FunctionType(tableNum, rgbFunction);

      // set up color table domain sets
      Integer1DSet rgbSet = new Integer1DSet(256);
      Integer1DSet colorSet = new Integer1DSet(numLuts);

      // set up color table fields
      FlatField[] rgbFields = new FlatField[numLuts];
      for (i=0; i<numLuts; i++) {
        rgbFields[i] = new FlatField(rgbFunction, rgbSet);
        rgbFields[i].setSamples(colors[i], false);
      }
      colorField = new FieldImpl(colorFunction, colorSet);
      colorField.setSamples(rgbFields, false);
    }

    // set up header data
    Real r_ramp1_min = new Real(RealType.getRealType("ramp1_min"), ramp1_min);
    Real r_ramp1_max = new Real(RealType.getRealType("ramp1_max"), ramp1_max);
    Real r_byte_format = new Real(
      RealType.getRealType("byte_format"), byte_format ? 1.0 : 0.0);
    Text t_name = new Text(TextType.getTextType("name"), name);
    Real r_ramp2_min = new Real(RealType.getRealType("ramp2_min"), ramp2_min);
    Real r_ramp2_max = new Real(RealType.getRealType("ramp2_max"), ramp2_max);
    Real r_lens = new Real(RealType.getRealType("lens"), lens);
    Real r_mag_factor = new Real(
      RealType.getRealType("mag_factor"), mag_factor);

    // compile data objects into vector
    Vector data = new Vector();
    data.add(timeField);
    data.add(colorField);
    data.add(noteField);
    data.add(r_ramp1_min);
    data.add(r_ramp1_max);
    data.add(r_byte_format);
    data.add(t_name);
    data.add(r_ramp2_min);
    data.add(r_ramp2_max);
    data.add(r_lens);
    data.add(r_mag_factor);

    // parse remaining notes
    len = noteList.size();
    for (i=0; i<len; i++) {
      BioRadNote note = (BioRadNote) noteList.elementAt(i);
      int rval = note.analyze();
      if (rval == BioRadNote.METADATA) data.add(note.metadata);
    }

    // convert vector into VisAD tuple
    Data[] dataArray = new Data[data.size()];
    data.copyInto(dataArray);
    return new Tuple(dataArray, false);
  }

  public FormNode getForms(Data data) {
    return null;
  }

  /**
   * Run 'java visad.data.biorad.BioRadForm in_file out_file' to convert
   * in_file to out_file in BioRad .PIC data format.
   */
  public static void main(String[] args)
    throws VisADException, RemoteException, IOException
  {
    if (args == null || args.length < 1 || args.length > 2) {
      System.out.println("To convert a file to BioRad .PIC, run:");
      System.out.println(
        "  java visad.data.biorad.BioRadForm in_file out_file");
      System.out.println("To test read a BioRad .PIC file, run:");
      System.out.println("  java visad.data.biorad.BioRadForm in_file");
      System.exit(2);
    }

    if (args.length == 1) {
      // Test read BioRad .PIC file
      BioRadForm form = new BioRadForm();
      System.out.print("Reading " + args[0] + " ");
      Data data = form.open(args[0]);
      System.out.println("[done]");
      System.out.println("MathType =\n" + data.getType().prettyString());
    }
    else if (args.length == 2) {
      // Convert file to BioRad .PIC format
      System.out.print(args[0] + " -> " + args[1] + " ");
      DefaultFamily loader = new DefaultFamily("loader");
      DataImpl data = loader.open(args[0]);
      loader = null;
      BioRadForm form = new BioRadForm();
      form.save(args[1], data, true);
      System.out.println("[done]");
    }
    System.exit(0);
  }

}

