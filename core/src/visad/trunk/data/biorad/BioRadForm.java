//
// BioRadForm.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2001 Bill Hibbard, Curtis Rueden, Tom
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

  /** Debugging level. 1=basic, 2=extended, 3=everything. */
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
  private static int getUnsignedShort(byte b1, byte b2) {
    int i1 = 0x000000ff & b1;
    int i2 = 0x000000ff & b2;
    return (i2 << 8) | i1;
  }

  /** Converts four bytes to a float. */
  private static float getFloat(byte b1, byte b2, byte b3, byte b4) {
    int i1 = 0x000000ff & b1;
    int i2 = 0x000000ff & b2;
    int i3 = 0x000000ff & b3;
    int i4 = 0x000000ff & b4;
    int bits = (i4 << 24) | (i3 << 16) | (i2 << 8) | i1;
    return Float.intBitsToFloat(bits);
  }

  /** Writes the given value as a short, least-significant byte first. */
  static void writeShort(DataOutputStream out, int val) throws IOException {
    int q0 = 0x000000ff & val;
    int q1 = (0x0000ff00 & val) >> 8;
    byte[] b = new byte[2];
    b[0] = (byte) q0;
    b[1] = (byte) q1;
    out.write(b, 0, 2);
  }

  /** Writes the given value as an int, least-significant byte first. */
  static void writeInt(DataOutputStream out, int val) throws IOException {
    int q0 = 0x000000ff & val;
    int q1 = (0x0000ff00 & val) >> 8;
    int q2 = (0x00ff0000 & val) >> 16;
    int q3 = (0xff000000 & val) >> 24;
    byte[] b = new byte[4];
    b[0] = (byte) q0;
    b[1] = (byte) q1;
    b[2] = (byte) q2;
    b[3] = (byte) q3;
    out.write(b, 0, 4);
  }

  /** Writes the given value as a float, in reverse byte order. */
  static void writeFloat(DataOutputStream out, float val) throws IOException {
    int q = Float.floatToIntBits(val);
    int q0 = (0x000000ff & q) << 24;
    int q1 = (0x0000ff00 & q) << 8;
    int q2 = (0x00ff0000 & q) >> 8;
    int q3 = (0xff000000 & q) >> 24;
    byte[] b = new byte[4];
    b[0] = (byte) q0;
    b[1] = (byte) q1;
    b[2] = (byte) q2;
    b[3] = (byte) q3;
    out.write(b, 0, 4);
  }

  /** Writes the given string out using exactly len bytes. */
  static void writeString(DataOutputStream out, String s, int len)
    throws IOException
  {
    byte[] b = s.getBytes();
    byte[] bytes = new byte[len];
    System.arraycopy(b, 0, bytes, 0, b.length < len ? b.length : len);
    out.write(bytes, 0, len);
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

  /** RealType for indexing BioRad notes. */
  private static final RealType noteIndex =
    RealType.getRealType("NoteIndex");

  /** RealType for ramp1_min header variable. */
  private static final RealType rt_ramp1_min =
    RealType.getRealType("ramp1_min");

  /** RealType for ramp1_max header variable. */
  private static final RealType rt_ramp1_max =
    RealType.getRealType("ramp1_max");

  /** RealType for byte_format header variable. */
  private static final RealType rt_byte_format =
    RealType.getRealType("byte_format");

  /** TextType for name header variable. */
  private static final TextType tt_name = TextType.getTextType("name");

  /** RealType for ramp2_min header variable. */
  private static final RealType rt_ramp2_min =
    RealType.getRealType("ramp2_min");

  /** RealType for ramp2_max header variable. */
  private static final RealType rt_ramp2_max =
    RealType.getRealType("ramp2_max");

  /** RealType for lens header variable. */
  private static final RealType rt_lens = RealType.getRealType("lens");

  /** RealType for mag_factor header variable. */
  private static final RealType rt_mag_factor =
    RealType.getRealType("mag_factor");

  /** List of allowed variable names for NOTE_TYPE_VARIABLE notes. */
  private static final String[] noteVarNames = {
    "SCALE_FACTOR", "LENS_MAGNIFICATION", "RAMP_GAMMA1", "RAMP_GAMMA2",
    "RAMP_GAMMA3", "RAMP1_MIN", "RAMP2_MIN", "RAMP3_MIN", "RAMP1_MAX",
    "RAMP2_MAX", "RAMP3_MAX", "PIC_FF_VERSION", "Z_CORRECT_FACTOR"
  };

  /** MathType of a 2-D image with 1-D range. */
  private static MathType image;

  /** MathType of a sequence of images. */
  private static MathType imageSequence;

  /** MathType of a color table mapping 1-D values to (red, green, blue). */
  private static MathType table;

  /** MathType of a sequence of color tables. */
  private static MathType tableSequence;

  /** MathType for indexed list of BioRad notes. */
  private static FunctionType noteFunction;

  static {
    try {
      image = MathType.stringToType("((x, y) -> a)");
      imageSequence = MathType.stringToType("(t -> ((x, y) -> a))");
      table = MathType.stringToType("(value -> (r, g, b))");
      tableSequence = MathType.stringToType("(t -> (value -> (r, g, b)))");
      noteFunction = new FunctionType(noteIndex, BioRadNote.noteTuple);
    }
    catch (VisADException exc) {
      if (DEBUG) exc.printStackTrace();
    }
  }

  /** Saves a VisAD Data object to BioRad .PIC format at the given location. */
  public void save(String id, Data data, boolean replace)
    throws BadFormException, IOException, RemoteException, VisADException
  {
    // make list of data elements
    data = data.local();
    Vector v = new Vector();
    if (data instanceof Tuple) {
      Tuple t = (Tuple) data;
      for (int i=0; i<t.getDimension(); i++) v.add(t.getComponent(i));
    }
    else v.add(data);

    // look for matching data types
    Vector v_images = new Vector();
    Vector v_tables = new Vector();
    Vector v_notes = new Vector();
    Real r_ramp1_min = null;
    Real r_ramp1_max = null;
    Real r_byte_format = null;
    Text t_name = null;
    Real r_ramp2_min = null;
    Real r_ramp2_max = null;
    Real r_lens = null;
    Real r_mag_factor = null;
    boolean hasNoteTuple = false;
    int len = v.size();
    for (int i=0; i<len; i++) {
      DataImpl d = (DataImpl) v.elementAt(i);
      MathType mt = d.getType();
      if (mt.equalsExceptName(image)) {
        // found image
        v_images.add(d);
      }
      else if (mt.equalsExceptName(imageSequence)) {
        // found image sequence
        FieldImpl f = (FieldImpl) d;
        int flen = f.getLength();
        for (int j=0; j<flen; j++) v_images.add(f.getSample(j));
      }
      else if (mt.equalsExceptName(table)) {
        // found color table
        v_tables.add(d);
      }
      else if (mt.equalsExceptName(tableSequence)) {
        // found color table sequence
        FieldImpl f = (FieldImpl) d;
        int flen = f.getLength();
        for (int j=0; j<flen; j++) v_tables.add(f.getSample(j));
      }
      else if (mt.equalsExceptName(noteFunction)) {
        // found BioRad note tuple
        v_notes.removeAllElements();
        FieldImpl f = (FieldImpl) d;
        int flen = f.getLength();
        for (int j=0; j<flen; j++) {
          Tuple t = (Tuple) f.getSample(j);
          Real r_level = (Real) t.getComponent(0);
          Real r_num = (Real) t.getComponent(1);
          Real r_status = (Real) t.getComponent(2);
          Real r_type = (Real) t.getComponent(3);
          Real r_x = (Real) t.getComponent(4);
          Real r_y = (Real) t.getComponent(5);
          Text t_text = (Text) t.getComponent(6);
          int level = (int) r_level.getValue();
          int num = (int) r_num.getValue();
          int status = (int) r_status.getValue();
          int type = (int) r_type.getValue();
          int x = (int) r_x.getValue();
          int y = (int) r_y.getValue();
          String text = t_text.getValue();
          BioRadNote note =
            new BioRadNote(level, num, status, type, x, y, text);
          v_notes.add(note);
        }
        hasNoteTuple = true;
      }
      else if (mt.equals(rt_ramp1_min)) {
        // found ramp1_min variable
        r_ramp1_min = (Real) d;
      }
      else if (mt.equals(rt_ramp1_max)) {
        // found ramp1_max variable
        r_ramp1_max = (Real) d;
      }
      else if (mt.equals(rt_byte_format)) {
        // found byte_format variable
        r_byte_format = (Real) d;
      }
      else if (mt.equals(tt_name)) {
        // found name variable
        t_name = (Text) d;
      }
      else if (mt.equals(rt_ramp2_min)) {
        // found ramp2_min variable
        r_ramp2_min = (Real) d;
      }
      else if (mt.equals(rt_ramp2_max)) {
        // found ramp2_max variable
        r_ramp2_max = (Real) d;
      }
      else if (mt.equals(rt_lens)) {
        // found lens variable
        r_lens = (Real) d;
      }
      else if (mt.equals(rt_mag_factor)) {
        // found mag_factor variable
        r_mag_factor = (Real) d;
      }
      else {
        boolean ok = false;
        if (mt instanceof RealType) {
          if (hasNoteTuple) {
            // ignore extraneous NOTE_TYPE_VARIABLE RealTypes
            ok = true;
          }
          else {
            // check for legal NOTE_TYPE_VARIABLE RealTypes
            RealType rt = (RealType) mt;
            String rtName = rt.getName();
            for (int j=0; j<noteVarNames.length; j++) {
              if (rtName.equals(noteVarNames[j])) {
                BioRadNote note = new BioRadNote(1, 0,
                  BioRadNote.NOTE_STATUS_ALL | BioRadNote.NOTE_STATUS_POSITION,
                  BioRadNote.NOTE_TYPE_VARIABLE, 0, 0,
                  rtName + " " + ((Real) d).getValue());
                v_notes.add(note);
                ok = true;
              }
            }
          }
        }
        if (!ok) {
          // invalid data object; cannot save
          throw new BadFormException("Unsupported data object");
        }
      }
    }

    // validate image data
    int xlen = -1;
    int ylen = -1;
    int numImages = v_images.size();
    for (int i=0; i<numImages; i++) {
      Object o = v_images.elementAt(i);
      if (!(o instanceof FlatField)) {
        throw new BadFormException("Invalid image data");
      }
      FlatField d = (FlatField) o;
      Set set = d.getDomainSet();
      if (!(set instanceof GriddedSet)) {
        throw new BadFormException("Invalid image set");
      }
      GriddedSet gset = (GriddedSet) set;
      if (gset.getDimension() != 2) {
        throw new BadFormException("Invalid domain dimension");
      }
      if (gset.getManifoldDimension() != 2) {
        throw new BadFormException("Invalid manifold dimension");
      }
      int[] l = gset.getLengths();
      if (xlen < 0) {
        xlen = l[0];
        ylen = l[1];
      }
      else {
        if (xlen != l[0] || ylen != l[1]) {
          throw new BadFormException(
            "All images must have same width and height");
        }
      }
      double[][] samples = d.getValues(false);
      if (samples.length != 1 || samples[0].length != xlen * ylen) {
        throw new BadFormException("Invalid image samples");
      }
    }

    // try to extract unit information if necessary
    if (!hasNoteTuple) {
      FlatField d = (FlatField) v_images.elementAt(0);
      Set set = d.getDomainSet();
      if (set instanceof Linear2DSet) {
        Linear2DSet lset = (Linear2DSet) set;
        Linear1DSet xset = lset.getX();
        Linear1DSet yset = lset.getY();
        Unit[] u = set.getSetUnits();
        Unit xu = u[0];
        Unit yu = u[1];
        BioRadNote xNote = BioRadNote.getUnitNote(xu, xset, true);
        BioRadNote yNote = BioRadNote.getUnitNote(yu, yset, false);
        if (xNote != null) v_notes.add(xNote);
        if (yNote != null) v_notes.add(yNote);
      }
    }

    // validate color table data
    int numTables = v_tables.size();
    if (numTables > 3) {
      throw new BadFormException("Too many color tables");
    }
    for (int i=0; i<numTables; i++) {
      Object o = v_tables.elementAt(i);
      if (!(o instanceof FlatField)) {
        throw new BadFormException("Invalid color table data");
      }
      FlatField d = (FlatField) o;
      Set set = d.getDomainSet();
      if (!(set instanceof Gridded1DSet)) {
        throw new BadFormException("Invalid color table set");
      }
      Gridded1DSet gset = (Gridded1DSet) set;
      int[] l = gset.getLengths();
      if (l[0] != 256) {
        throw new BadFormException("Invalid color table length");
      }
    }

    // set up header data
    int nx = xlen;
    int ny = ylen;
    int npic = numImages;
    int ramp1_min = r_ramp1_min == null ? 0 : (int) r_ramp1_min.getValue();
    int ramp1_max = r_ramp1_max == null ? 255 : (int) r_ramp1_max.getValue();
    int notes = v_notes.size();
    int byte_format =
      r_byte_format == null ? 1 : (int) r_byte_format.getValue();
    int image_number = 0;
    String name = t_name == null ? id : t_name.getValue();
    int merged = MERGE_OFF;
    int color1 = 7;
    int file_id = PIC_FILE_ID;
    int ramp2_min = r_ramp2_min == null ? 0 : (int) r_ramp2_min.getValue();
    int ramp2_max = r_ramp2_max == null ? 255 : (int) r_ramp2_max.getValue();
    int color2 = 7;
    int edited = 1;
    int lens = r_lens == null ? 0 : (int) r_lens.getValue();
    float mag_factor =
      r_mag_factor == null ? 0f : (float) r_mag_factor.getValue();

    // extract image data
    byte[] imageBytes;
    if (byte_format == 0) {
      // word format (16 bits per pixel)
      imageBytes = new byte[2 * npic * nx * ny];
      for (int i=0; i<npic; i++) {
        FlatField d = (FlatField) v_images.elementAt(i);
        double[][] samples = d.getValues(false);
        double[] samp = samples[0];
        for (int j=0; j<samp.length; j++) {
          int index = 2 * (samp.length * i + j);
          int q = (int) samp[j];
          int qhi = (0x0000ff00 & q) >> 8;
          int qlo = 0x000000ff & q;
          imageBytes[index] = (byte) qhi;
          imageBytes[index + 1] = (byte) qlo;
        }
      }
    }
    else {
      // byte format (8 bits per pixel)
      imageBytes = new byte[npic * nx * ny];
      for (int i=0; i<npic; i++) {
        FlatField d = (FlatField) v_images.elementAt(i);
        double[][] samples = d.getValues(false);
        double[] samp = samples[0];
        for (int j=0; j<samp.length; j++) {
          int index = samp.length * i + j;
          imageBytes[index] = (byte) samp[j];
        }
      }
    }

    // extract color table data
    byte[] tableBytes = new byte[numTables * 768];
    for (int i=0; i<numTables; i++) {
      FlatField d = (FlatField) v_tables.elementAt(i);
      double[][] samples = d.getValues(false);
      double[] sr = samples[0];
      double[] sg = samples[1];
      double[] sb = samples[2];
      for (int j=0; j<256; j++) {
        int index = 768 * i + j;
        tableBytes[index] = (byte) sr[j];
        tableBytes[index + 256] = (byte) sg[j];
        tableBytes[index + 512] = (byte) sb[j];
      }
    }

    // open file
    DataOutputStream fout = new DataOutputStream(new FileOutputStream(id));

    // write header
    writeShort(fout, nx);
    writeShort(fout, ny);
    writeShort(fout, npic);
    writeShort(fout, ramp1_min);
    writeShort(fout, ramp1_max);
    writeInt(fout, notes);
    writeShort(fout, byte_format);
    writeShort(fout, image_number);
    writeString(fout, name, 32);
    writeShort(fout, merged);
    writeShort(fout, color1);
    writeShort(fout, file_id);
    writeShort(fout, ramp2_min);
    writeShort(fout, ramp2_max);
    writeShort(fout, color2);
    writeShort(fout, edited);
    writeShort(fout, lens);
    writeFloat(fout, mag_factor);
    fout.write(new byte[] {0, 0, 0, 0, 0, 0}, 0, 6);
    if (DEBUG && DEBUG_LEVEL >= 1) {
      System.out.println("Wrote 76 header bytes.");
    }

    // write image data
    fout.write(imageBytes, 0, imageBytes.length);
    if (DEBUG && DEBUG_LEVEL >= 1) {
      System.out.println("Wrote " + npic + " " + nx + " x " + ny + " image" +
        (npic == 1 ? "" : "s") + " (" + imageBytes.length + " bytes).");
    }

    // write notes
    for (int i=0; i<notes; i++) {
      BioRadNote note = (BioRadNote) v_notes.elementAt(i);
      note.write(fout, i != notes - 1);
    }
    if (DEBUG && DEBUG_LEVEL >= 1) {
      System.out.println("Wrote " + notes + " note" +
        (notes == 1 ? "" : "s") + " (" + (96 * notes) + " bytes).");
    }

    // write color tables
    fout.write(tableBytes, 0, tableBytes.length);
    if (DEBUG && DEBUG_LEVEL >= 1) {
      System.out.println("Wrote " + numTables + " table" +
        (numTables == 1 ? "" : "s") + " (" + tableBytes.length + " bytes).");
    }

    // close file
    fout.close();
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
    try {
      return readFile(new DataInputStream(in));
    } finally {
      try { in.close(); } catch (IOException ioe) { }
    }
  }

  DataImpl readFile(DataInput fin)
    throws IOException, RemoteException, VisADException
  {
    // read header
    byte[] header = new byte[76];
    fin.readFully(header);
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

    final int image_len = nx * ny;

    float[][][] samples;

    // read image bytes & convert to floats
    samples = new float[npic][1][image_len];
    if (byte_format) {
      // read in image_len bytes
      byte[] buf = new byte[image_len];
      for (int i=0; i<npic; i++) {
        fin.readFully(buf);

        // each pixel is 8 bits
        for (int l=0; l<image_len; l++) {
          int q = 0x000000ff & buf[l];
          samples[i][0][l] = (float) q;
        }
      }
    }
    else {
      // read in 2 * image_len bytes
      final int data_len = 2 * image_len;
      byte[] buf = new byte[data_len];
      for (int i=0; i<npic; i++) {
        fin.readFully(buf);

        // each pixel is 16 bits
        for (int l=0; l<data_len; l+=2) {
          int q = getUnsignedShort(buf[l], buf[l + 1]);
          samples[i][0][l/2] = (float) q;
        }
      }
    }

    // read notes
    Vector noteList = new Vector();
    while (notes) {
      // read in note
      byte[] note = new byte[96];
      fin.readFully(note);
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
    boolean eof = false;
    while (!eof && numLuts < 3) {
      try {
        fin.readFully(lut[numLuts]);
        numLuts++;
      }
      catch (IOException exc) {
        eof = true;
        if (DEBUG) exc.printStackTrace();
      }
    }
    if (DEBUG && DEBUG_LEVEL >= 2) {
      System.out.println(numLuts + " color table" +
        (numLuts == 1 ? "" : "s") + " present.");
    }

    // get basic note information
    int len = noteList.size();
    DataImpl[] noteData = new DataImpl[len];
    for (int i=0; i<len; i++) {
      BioRadNote note = (BioRadNote) noteList.elementAt(i);
      noteData[i] = note.getNoteData();
    }
    FieldImpl noteField = null;
    if (len > 0) {
      Integer1DSet noteSet = new Integer1DSet(len);
      noteField = new FieldImpl(noteFunction, noteSet);
      noteField.setSamples(noteData, false);
    }

    // extract horizontal and vertical unit information from notes
    double horizOffset = 0, vertOffset = 0;
    double horizStep = 0, vertStep = 0;
    Unit horizUnit = null, vertUnit = null;
    int n = 0;
    while (n < noteList.size()) {
      BioRadNote note = (BioRadNote) noteList.elementAt(n);
      if (note.hasUnitInfo()) {
        int rval = note.analyze();
        if (rval == BioRadNote.HORIZ_UNIT) {
          if (horizStep == 0) {
            horizOffset = note.origin;
            horizStep = note.step;
            horizUnit = BioRadNote.micron;
          }
          else if (DEBUG && DEBUG_LEVEL >= 1) {
            System.err.println("Warning: ignoring extra AXIS_2 note");
          }
          noteList.remove(n);
        }
        else if (rval == BioRadNote.VERT_UNIT) {
          if (vertStep == 0) {
            vertOffset = note.origin;
            vertStep = note.step;
            vertUnit = note.time ? BioRadNote.second : BioRadNote.micron;
          }
          else if (DEBUG && DEBUG_LEVEL >= 1) {
            System.err.println("Warning: ignoring extra AXIS_3 note");
          }
          noteList.remove(n);
        }
        else if (rval == BioRadNote.INVALID_NOTE ||
          rval == BioRadNote.NO_INFORMATION)
        {
          noteList.remove(n);
        }
        else n++;
      }
      else n++;
    }
    if (horizStep == 0) horizStep = 1;
    if (vertStep == 0) vertStep = 1;

    // convert color table bytes to floats
    float[][][] colors = new float[numLuts][3][256];
    for (int i=0; i<numLuts; i++) {
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
    RealType index = RealType.getRealType("index");
    RealType x = RealType.getRealType("ImageElement", horizUnit);
    RealType y = RealType.getRealType("ImageLine", vertUnit);
    RealType value = RealType.getRealType("value");
    RealTupleType xy = new RealTupleType(x, y);
    FunctionType imageFunction = new FunctionType(xy, value);
    FunctionType stackFunction = new FunctionType(index, imageFunction);

    // set up image domain sets
    Linear2DSet imageSet = new Linear2DSet(xy,
      horizOffset, horizOffset + (nx - 1) * horizStep, nx,
      vertOffset, vertOffset + (ny - 1) * vertStep, ny,
      null, new Unit[] {horizUnit, vertUnit}, null);
    Integer1DSet stackSet = new Integer1DSet(index, npic);

    // set up image fields
    FlatField[] imageFields = new FlatField[npic];
    for (int i=0; i<npic; i++) {
      imageFields[i] = new FlatField(imageFunction, imageSet);
      imageFields[i].setSamples(samples[i], false);
    }
    FieldImpl stackField = new FieldImpl(stackFunction, stackSet);
    stackField.setSamples(imageFields, false);

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
      for (int i=0; i<numLuts; i++) {
        rgbFields[i] = new FlatField(rgbFunction, rgbSet);
        rgbFields[i].setSamples(colors[i], false);
      }
      colorField = new FieldImpl(colorFunction, colorSet);
      colorField.setSamples(rgbFields, false);
    }

    // set up header data
    Real r_ramp1_min = new Real(rt_ramp1_min, ramp1_min);
    Real r_ramp1_max = new Real(rt_ramp1_max, ramp1_max);
    Real r_byte_format = new Real(rt_byte_format, byte_format ? 1.0 : 0.0);
    Text t_name = new Text(tt_name, name);
    Real r_ramp2_min = new Real(rt_ramp2_min, ramp2_min);
    Real r_ramp2_max = new Real(rt_ramp2_max, ramp2_max);
    Real r_lens = new Real(rt_lens, lens);
    Real r_mag_factor = new Real(rt_mag_factor, mag_factor);

    // compile data objects into vector
    Vector data = new Vector();
    data.add(stackField);
    if (colorField != null) data.add(colorField);
    if (noteField != null) data.add(noteField);
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
    for (int i=0; i<len; i++) {
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

