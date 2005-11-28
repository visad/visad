//
// BioRadForm.java
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
import java.net.URL;
import java.util.Hashtable;
import java.util.Vector;
import visad.*;
import visad.data.*;
import visad.data.tiff.BaseTiffForm;

/**
 * BioRadForm is the VisAD data format adapter for Bio-Rad PIC files.
 * @author Curtis Rueden ctrueden at wisc.edu
 * @author Melissa Linkert linkert at cs.wisc.edu
 */
public class BioRadForm extends Form implements FormBlockReader,
  FormFileInformer, FormProgressInformer, MetadataReader, OMEReader
{

  // -- Constants --

  /** Debugging flag. */
  static final boolean DEBUG = false;

  /** Debugging level. 1=basic, 2=extended, 3=everything. */
  static final int DEBUG_LEVEL = 1;

  /** Numerical ID of a valid Bio-Rad PIC file. */
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
  private static final String[] MERGE_NAMES = {
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

  // RealTypes

  /** RealType for indexing Bio-Rad notes. */
  private static final RealType NOTE_INDEX =
    RealType.getRealType("NoteIndex");

  /** RealType for ramp1_min header variable. */
  private static final RealType RT_RAMP1_MIN =
    RealType.getRealType("ramp1_min");

  /** RealType for ramp1_max header variable. */
  private static final RealType RT_RAMP1_MAX =
    RealType.getRealType("ramp1_max");

  /** RealType for byte_format header variable. */
  private static final RealType RT_BYTE_FORMAT =
    RealType.getRealType("byte_format");

  /** TextType for name header variable. */
  private static final TextType TT_NAME = TextType.getTextType("name");

  /** RealType for ramp2_min header variable. */
  private static final RealType RT_RAMP2_MIN =
    RealType.getRealType("ramp2_min");

  /** RealType for ramp2_max header variable. */
  private static final RealType RT_RAMP2_MAX =
    RealType.getRealType("ramp2_max");

  /** RealType for lens header variable. */
  private static final RealType RT_LENS = RealType.getRealType("lens");

  /** RealType for mag_factor header variable. */
  private static final RealType RT_MAG_FACTOR =
    RealType.getRealType("mag_factor");

  /** List of allowed variable names for NOTE_TYPE_VARIABLE notes. */
  private static final String[] NOTE_VAR_NAMES = {
    "SCALE_FACTOR", "LENS_MAGNIFICATION", "RAMP_GAMMA1", "RAMP_GAMMA2",
    "RAMP_GAMMA3", "RAMP1_MIN", "RAMP2_MIN", "RAMP3_MIN", "RAMP1_MAX",
    "RAMP2_MAX", "RAMP3_MAX", "PIC_FF_VERSION", "Z_CORRECT_FACTOR"
  };


  // -- Static fields --

  /** Form instantiation counter. */
  private static int formCount = 0;

  /** MathType of a 2-D image with 1-D range. */
  private static MathType image;

  /** MathType of a sequence of images. */
  private static MathType imageSequence;

  /** MathType of a color table mapping 1-D values to (red, green, blue). */
  private static MathType table;

  /** MathType of a sequence of color tables. */
  private static MathType tableSequence;

  /** MathType for indexed list of Bio-Rad notes. */
  private static FunctionType noteFunction;

  static {
    try {
      image = MathType.stringToType("((x, y) -> a)");
      imageSequence = MathType.stringToType("(t -> ((x, y) -> a))");
      table = MathType.stringToType("(value -> (r, g, b))");
      tableSequence = MathType.stringToType("(t -> (value -> (r, g, b)))");
      noteFunction = new FunctionType(NOTE_INDEX, BioRadNote.NOTE_TUPLE);
    }
    catch (VisADException exc) {
      exc.printStackTrace();
    }
  }


  // -- Fields --

  /** Filename of current Bio-Rad PIC. */
  private String currentId;

  /** Input stream for current Bio-Rad PIC. */
  private RandomAccessFile in;

  /** Hashtable containing metadata for current Bio-Rad PIC. */
  private Hashtable metadata;

  /** Dimensions of each image in current Bio-Rad PIC. */
  private int nx, ny;

  /** Number of images in current Bio-Rad PIC. */
  private int npic;

  /** Flag indicating current Bio-Rad PIC is packed with bytes. */
  private boolean byteFormat;

  /** MathType for an image of the current Bio-Rad PIC. */
  private FunctionType imageFunction;

  /** Domain set for an image of the current Bio-Rad PIC. */
  private Linear2DSet imageSet;

  /** Percent complete with current operation. */
  private double percent;

  /** OME root node for OME-XML metadata */
  private Object ome;


  // -- Constructor --

  /** Constructs a new Bio-Rad file form. */
  public BioRadForm() {
    super("BioRadForm" + formCount++);
  }


  // -- FormNode API methods --

  /** Saves a VisAD Data object to Bio-Rad PIC format at the given location. */
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
    Vector vImages = new Vector();
    Vector vTables = new Vector();
    Vector vNotes = new Vector();
    Real ramp1minType = null;
    Real ramp1maxType = null;
    Real byteFormatType = null;
    Text nameType = null;
    Real ramp2minType = null;
    Real ramp2maxType = null;
    Real lensType = null;
    Real magFactorType = null;
    boolean hasNoteTuple = false;
    int len = v.size();
    for (int i=0; i<len; i++) {
      DataImpl d = (DataImpl) v.elementAt(i);
      MathType mt = d.getType();
      if (mt.equalsExceptName(image)) {
        // found image
        vImages.add(d);
      }
      else if (mt.equalsExceptName(imageSequence)) {
        // found image sequence
        FieldImpl f = (FieldImpl) d;
        int flen = f.getLength();
        for (int j=0; j<flen; j++) vImages.add(f.getSample(j));
      }
      else if (mt.equalsExceptName(table)) {
        // found color table
        vTables.add(d);
      }
      else if (mt.equalsExceptName(tableSequence)) {
        // found color table sequence
        FieldImpl f = (FieldImpl) d;
        int flen = f.getLength();
        for (int j=0; j<flen; j++) vTables.add(f.getSample(j));
      }
      else if (mt.equalsExceptName(noteFunction)) {
        // found Bio-Rad note tuple
        vNotes.removeAllElements();
        FieldImpl f = (FieldImpl) d;
        int flen = f.getLength();
        for (int j=0; j<flen; j++) {
          Tuple t = (Tuple) f.getSample(j);
          Real rtLevel = (Real) t.getComponent(0);
          Real rtNum = (Real) t.getComponent(1);
          Real rtStatus = (Real) t.getComponent(2);
          Real rtType = (Real) t.getComponent(3);
          Real rtX = (Real) t.getComponent(4);
          Real rtY = (Real) t.getComponent(5);
          Text ttText = (Text) t.getComponent(6);
          int level = (int) rtLevel.getValue();
          int num = (int) rtNum.getValue();
          int status = (int) rtStatus.getValue();
          int type = (int) rtType.getValue();
          int x = (int) rtX.getValue();
          int y = (int) rtY.getValue();
          String text = ttText.getValue();
          BioRadNote note =
            new BioRadNote(level, num, status, type, x, y, text);
          vNotes.add(note);
        }
        hasNoteTuple = true;
      }
      else if (mt.equals(RT_RAMP1_MIN)) {
        // found ramp1_min variable
        ramp1minType = (Real) d;
      }
      else if (mt.equals(RT_RAMP1_MAX)) {
        // found ramp1_max variable
        ramp1maxType = (Real) d;
      }
      else if (mt.equals(RT_BYTE_FORMAT)) {
        // found byteFormat variable
        byteFormatType = (Real) d;
      }
      else if (mt.equals(TT_NAME)) {
        // found name variable
        nameType = (Text) d;
      }
      else if (mt.equals(RT_RAMP2_MIN)) {
        // found ramp2_min variable
        ramp2minType = (Real) d;
      }
      else if (mt.equals(RT_RAMP2_MAX)) {
        // found ramp2_max variable
        ramp2maxType = (Real) d;
      }
      else if (mt.equals(RT_LENS)) {
        // found lens variable
        lensType = (Real) d;
      }
      else if (mt.equals(RT_MAG_FACTOR)) {
        // found mag_factor variable
        magFactorType = (Real) d;
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
            for (int j=0; j<NOTE_VAR_NAMES.length; j++) {
              if (rtName.equals(NOTE_VAR_NAMES[j])) {
                BioRadNote note = new BioRadNote(1, 0,
                  BioRadNote.NOTE_STATUS_ALL | BioRadNote.NOTE_STATUS_POSITION,
                  BioRadNote.NOTE_TYPE_VARIABLE, 0, 0,
                  rtName + " " + ((Real) d).getValue());
                vNotes.add(note);
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
    int numImages = vImages.size();
    for (int i=0; i<numImages; i++) {
      Object o = vImages.elementAt(i);
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
      FlatField d = (FlatField) vImages.elementAt(0);
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
        if (xNote != null) vNotes.add(xNote);
        if (yNote != null) vNotes.add(yNote);
      }
    }

    // validate color table data
    int numTables = vTables.size();
    if (numTables > 3) {
      throw new BadFormException("Too many color tables");
    }
    for (int i=0; i<numTables; i++) {
      Object o = vTables.elementAt(i);
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
    int numX = xlen;
    int numY = ylen;
    int numPic = numImages;
    int ramp1min = ramp1minType == null ? 0 : (int) ramp1minType.getValue();
    int ramp1max = ramp1maxType == null ? 255 : (int) ramp1maxType.getValue();
    int notes = vNotes.size();
    int format =
      byteFormatType == null ? 1 : (int) byteFormatType.getValue();
    int imageNumber = 0;
    String name = nameType == null ? id : nameType.getValue();
    int merged = MERGE_OFF;
    int color1 = 7;
    int fileId = PIC_FILE_ID;
    int ramp2min = ramp2minType == null ? 0 : (int) ramp2minType.getValue();
    int ramp2max = ramp2maxType == null ? 255 : (int) ramp2maxType.getValue();
    int color2 = 7;
    int edited = 1;
    int lens = lensType == null ? 0 : (int) lensType.getValue();
    float magFactor =
      magFactorType == null ? 0f : (float) magFactorType.getValue();

    // extract image data
    byte[] imageBytes;
    if (format == 0) {
      // word format (16 bits per pixel)
      imageBytes = new byte[2 * numPic * numX * numY];
      for (int i=0; i<numPic; i++) {
        FlatField d = (FlatField) vImages.elementAt(i);
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
      imageBytes = new byte[numPic * numX * numY];
      for (int i=0; i<numPic; i++) {
        FlatField d = (FlatField) vImages.elementAt(i);
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
      FlatField d = (FlatField) vTables.elementAt(i);
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
    writeShort(fout, numX);
    writeShort(fout, numY);
    writeShort(fout, numPic);
    writeShort(fout, ramp1min);
    writeShort(fout, ramp1max);
    writeInt(fout, notes);
    writeShort(fout, format);
    writeShort(fout, imageNumber);
    writeString(fout, name, 32);
    writeShort(fout, merged);
    writeShort(fout, color1);
    writeShort(fout, fileId);
    writeShort(fout, ramp2min);
    writeShort(fout, ramp2max);
    writeShort(fout, color2);
    writeShort(fout, edited);
    writeShort(fout, lens);
    writeFloat(fout, magFactor);
    fout.write(new byte[] {0, 0, 0, 0, 0, 0}, 0, 6);
    if (DEBUG && DEBUG_LEVEL >= 1) {
      System.out.println("Wrote 76 header bytes.");
    }

    // write image data
    fout.write(imageBytes, 0, imageBytes.length);
    if (DEBUG && DEBUG_LEVEL >= 1) {
      System.out.println("Wrote " + npic + " " + numX + " x " + numY +
        " image" + (npic == 1 ? "" : "s") +
        " (" + imageBytes.length + " bytes).");
    }

    // write notes
    for (int i=0; i<notes; i++) {
      BioRadNote note = (BioRadNote) vNotes.elementAt(i);
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
   * Adds data to an existing Bio-Rad file.
   *
   * @exception BadFormException Always thrown (this method not implemented).
   */
  public void add(String id, Data data, boolean replace)
    throws BadFormException
  {
    throw new BadFormException("BioRadForm.add");
  }

  /**
   * Opens an existing Bio-Rad PIC file from the given location.
   *
   * @return VisAD Data object containing Bio-Rad data.
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
      // combine data stack into index function
      RealType index = RealType.getRealType("index");
      FunctionType indexFunction =
        new FunctionType(index, fields[0].getType());
      Integer1DSet indexSet = new Integer1DSet(nImages);
      FieldImpl indexField = new FieldImpl(indexFunction, indexSet);
      indexField.setSamples(fields, false);
      data = indexField;
    }
    close();
    percent = Double.NaN;
    return data;
  }

  /**
   * Opens an existing Bio-Rad PIC file from the given URL.
   *
   * @return VisAD Data object containing Bio-Rad data.
   */
  public DataImpl open(URL url)
    throws BadFormException, VisADException, IOException
  {
    throw new BadFormException("BioRadForm.open(URL)");
  }

  /** Returns the data forms that are compatible with a data object. */
  public FormNode getForms(Data data) {
    return null;
  }


  // -- FormBlockReader methods --

  /**
   * Obtains the specified block from the given file.
   * @param id The file from which to load data blocks.
   * @param blockNumber The block number of the block to load.
   * @throws VisADException If the block number is invalid.
   */
  public DataImpl open(String id, int blockNumber)
    throws BadFormException, IOException, VisADException
  {
    if (!id.equals(currentId)) initFile(id);

    if (blockNumber < 0 || blockNumber >= npic) {
      throw new BadFormException("Invalid image number: " + blockNumber);
    }

    // read image bytes & convert to floats
    int imageLen = nx * ny;
    float[][] samples = new float[1][imageLen];
    if (byteFormat) {
      // jump to proper image number
      in.seek(blockNumber * imageLen + 76);

      // read in imageLen bytes
      byte[] buf = new byte[imageLen];
      in.readFully(buf);

      // each pixel is 8 bits
      for (int l=0; l<imageLen; l++) {
        int q = 0x000000ff & buf[l];
        samples[0][l] = (float) q;
      }
    }
    else {
      // jump to proper image number
      in.seek(blockNumber * 2 * imageLen + 76);

      // read in 2 * imageLen bytes
      final int dataLen = 2 * imageLen;
      byte[] buf = new byte[dataLen];
      in.readFully(buf);

      // each pixel is 16 bits
      for (int l=0; l<dataLen; l+=2) {
        int q = getUnsignedShort(buf[l], buf[l + 1]);
        samples[0][l / 2] = (float) q;
      }
    }

    // set up image field
    FlatField field = new FlatField(imageFunction, imageSet);
    field.setSamples(samples, false);

    return field;
  }

  /**
   * Determines the number of blocks in the given file.
   * @param id The file for which to get a block count.
   */
  public int getBlockCount(String id)
    throws BadFormException, IOException, VisADException
  {
    if (!id.equals(currentId)) initFile(id);
    return npic;
  }

  /** Closes any open files. */
  public void close() throws BadFormException, IOException, VisADException {
    if (currentId == null) return;
    in.close();
    currentId = null;
    in = null;
    metadata = null;
  }


  // -- FormFileInformer methods --

  /**
   * Checks if the given string is a valid
   * filename for a Bio-Rad PIC file.
   */
  public boolean isThisType(String name) {
    return name.toLowerCase().endsWith(".pic");
  }

  /** Checks if the given block is a valid header for a Bio-Rad PIC file. */
  public boolean isThisType(byte[] block) {
    if (block.length < 56) return false;
    return getUnsignedShort(block[54], block[55]) == PIC_FILE_ID;
  }

  /** Returns the default file suffixes for the Bio-Rad PIC file format. */
  public String[] getDefaultSuffixes() {
    return new String[] {"pic"};
  }


  // -- FormProgressInformer methods --

  /**
   * Gets the percentage complete of the form's current operation.
   * @return the percentage complete (0.0 - 100.0), or Double.NaN
   *         if no operation is currently taking place
   */
  public double getPercentComplete() { return percent; }


  // -- MetadataReader methods --

  /**
   * Obtains the specified metadata field's value for the given file.
   * @param field the name associated with the metadata field
   * @return the value, or null should the field not exist
   */
  public Object getMetadataValue(String id, String field)
    throws BadFormException, IOException, VisADException
  {
    if (!id.equals(currentId)) initFile(id);
    return metadata.get(field);
  }

  /**
   * Obtains a hashtable containing all metadata field/value pairs from
   * the given file.
   * @param id the filename
   * @return the hashtable containing all metadata associated with the file
   */
  public Hashtable getMetadata(String id)
    throws BadFormException, IOException, VisADException
  {
    if (!id.equals(currentId)) initFile(id);
    return metadata;
  }


  // -- OMEReader API methods --

  /**
   * Obtains a loci.ome.xml.OMENode object representing the
   * file's metadata as an OME-XML DOM structure.
   *
   * @throws BadFormException if the loci.ome.xml package is not present
   */
  public Object getOMENode(String id)
    throws BadFormException, IOException, VisADException
  {
    if (!id.equals(currentId)) initFile(id);
    if (ome == null) {
      throw new BadFormException(
        "This functionality requires the LOCI OME-XML " +
        "package available at http://www.loci.wisc.edu/ome/");
    }
    return ome;
  }


  // -- Utility methods --

  /** Converts two bytes to an unsigned short. */
  public static int getUnsignedShort(byte b1, byte b2) {
    int i1 = 0x000000ff & b1;
    int i2 = 0x000000ff & b2;
    return (i2 << 8) | i1;
  }

  /** Converts four bytes to a float. */
  public static float getFloat(byte b1, byte b2, byte b3, byte b4) {
    int i1 = 0x000000ff & b1;
    int i2 = 0x000000ff & b2;
    int i3 = 0x000000ff & b3;
    int i4 = 0x000000ff & b4;
    int bits = (i4 << 24) | (i3 << 16) | (i2 << 8) | i1;
    return Float.intBitsToFloat(bits);
  }

  /** Writes the given value as a short, least-significant byte first. */
  public static void writeShort(DataOutputStream out, int val)
    throws IOException
  {
    int q0 = 0x000000ff & val;
    int q1 = (0x0000ff00 & val) >> 8;
    byte[] b = new byte[2];
    b[0] = (byte) q0;
    b[1] = (byte) q1;
    out.write(b, 0, 2);
  }

  /** Writes the given value as an int, least-significant byte first. */
  public static void writeInt(DataOutputStream out, int val)
    throws IOException
  {
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
  public static void writeFloat(DataOutputStream out, float val)
    throws IOException
  {
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
  public static void writeString(DataOutputStream out, String s, int len)
    throws IOException
  {
    byte[] b = s.getBytes();
    byte[] bytes = new byte[len];
    System.arraycopy(b, 0, bytes, 0, b.length < len ? b.length : len);
    out.write(bytes, 0, len);
  }


  // -- Helper methods --

  private void initFile(String id)
    throws BadFormException, IOException, VisADException
  {
    // close any currently open files
    close();

    currentId = id;
    in = new RandomAccessFile(id, "r");
    metadata = new Hashtable();

    // read header
    byte[] header = new byte[76];
    in.readFully(header);

    nx = getUnsignedShort(header[0], header[1]);
    ny = getUnsignedShort(header[2], header[3]);
    npic = getUnsignedShort(header[4], header[5]);
    byteFormat = getUnsignedShort(header[14], header[15]) != 0;

    int ramp1min = getUnsignedShort(header[6], header[7]);
    int ramp1max = getUnsignedShort(header[8], header[9]);
    boolean notes = (header[10] | header[11] | header[12] | header[13]) != 0;
    int imageNumber = getUnsignedShort(header[16], header[17]);
    String name = new String(header, 18, 32);
    int merged = getUnsignedShort(header[50], header[51]);
    int color1 = getUnsignedShort(header[52], header[53]);
    int fileId = getUnsignedShort(header[54], header[55]);
    int ramp2min = getUnsignedShort(header[56], header[57]);
    int ramp2max = getUnsignedShort(header[58], header[59]);
    int color2 = getUnsignedShort(header[60], header[61]);
    int edited = getUnsignedShort(header[62], header[63]);
    int lens = getUnsignedShort(header[64], header[65]);
    float magFactor = getFloat(header[66], header[67], header[68], header[69]);

    // check validity of header
    if (fileId != PIC_FILE_ID) {
      throw new BadFormException("Invalid file header: " + fileId);
    }

    // populate metadata fields
    metadata.put("nx", new Integer(nx));
    metadata.put("ny", new Integer(ny));
    metadata.put("npic", new Integer(npic));
    metadata.put("ramp1_min", new Integer(ramp1min));
    metadata.put("ramp1_max", new Integer(ramp1max));
    metadata.put("notes", new Boolean(notes));
    metadata.put("byte_format", new Boolean(byteFormat));
    metadata.put("image_number", new Integer(imageNumber));
    metadata.put("name", name);
    metadata.put("merged", MERGE_NAMES[merged]);
    metadata.put("color1", new Integer(color1));
    metadata.put("file_id", new Integer(fileId));
    metadata.put("ramp2_min", new Integer(ramp2min));
    metadata.put("ramp2_max", new Integer(ramp2max));
    metadata.put("color2", new Integer(color2));
    metadata.put("edited", new Integer(edited));
    metadata.put("lens", new Integer(lens));
    metadata.put("mag_factor", new Float(magFactor));

    // skip image data
    int imageLen = nx * ny;
    int bpp = byteFormat ? 1 : 2;
    in.skipBytes(bpp * npic * imageLen);

    // read notes
    Vector noteList = new Vector();
    int noteCount = 0;
    while (notes) {
      // read in note
      byte[] note = new byte[96];
      in.readFully(note);
      int level = getUnsignedShort(note[0], note[1]);
      notes = (note[2] | note[3] | note[4] | note[5]) != 0;
      int num = getUnsignedShort(note[6], note[7]);
      int status = getUnsignedShort(note[8], note[9]);
      int type = getUnsignedShort(note[10], note[11]);
      int x = getUnsignedShort(note[12], note[13]);
      int y = getUnsignedShort(note[14], note[15]);
      String text = new String(note, 16, 80);

      // add note to list
      BioRadNote bioRadNote =
        new BioRadNote(level, num, status, type, x, y, text);
      noteList.add(bioRadNote);
      noteCount++;
      metadata.put("note" + noteCount, bioRadNote);
    }

    // read color tables
    int numLuts = 0;
    byte[][] lut = new byte[3][768];
    boolean eof = false;
    while (!eof && numLuts < 3) {
      try {
        in.readFully(lut[numLuts]);
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
    metadata.put("luts", colors);

    // extract horizontal and vertical unit information from notes
    double horizOffset = 0, vertOffset = 0;
    double horizStep = 0, vertStep = 0;
    Unit horizUnit = null, vertUnit = null;
    int ndx = 0;
    while (ndx < noteList.size()) {
      BioRadNote note = (BioRadNote) noteList.elementAt(ndx);
      if (note.hasUnitInfo()) {
        int rval = note.analyze();
        if (rval == BioRadNote.HORIZ_UNIT) {
          if (horizStep == 0) {
            horizOffset = note.origin;
            horizStep = note.step;
            horizUnit = BioRadNote.MICRON;
          }
          else if (DEBUG && DEBUG_LEVEL >= 1) {
            System.err.println("Warning: ignoring extra AXIS_2 note");
          }
          noteList.remove(ndx);
        }
        else if (rval == BioRadNote.VERT_UNIT) {
          if (vertStep == 0) {
            vertOffset = note.getOrigin();
            vertStep = note.getStep();
            vertUnit = note.getTime() ? BioRadNote.SECOND : BioRadNote.MICRON;
          }
          else if (DEBUG && DEBUG_LEVEL >= 1) {
            System.err.println("Warning: ignoring extra AXIS_3 note");
          }
          noteList.remove(ndx);
        }
        else if (rval == BioRadNote.INVALID_NOTE ||
          rval == BioRadNote.NO_INFORMATION)
        {
          noteList.remove(ndx);
        }
        else ndx++;
      }
      else ndx++;
    }
    if (horizStep == 0) horizStep = 1;
    if (vertStep == 0) vertStep = 1;

    // create and populate OME-XML DOM tree
    ome = OMETools.createRoot();
    OMETools.setAttribute(ome, "Image", "Name", name);
    OMETools.setAttribute(ome, "Pixels", "SizeX", "" + nx);
    OMETools.setAttribute(ome, "Pixels", "SizeY", "" + ny);
    OMETools.setAttribute(ome, "Pixels", "SizeZ", "" + npic);
    OMETools.setAttribute(ome, "Pixels", "SizeT", "1");
    OMETools.setAttribute(ome, "Pixels", "SizeC", "1");

    int type = getUnsignedShort(header[14], header[15]);
    String format;
    if(type == 1) { format = "Uint8"; }
    else { format = "Uint16"; }

    OMETools.setAttribute(ome, "Image", "PixelType", format);

    // set up image data types
    RealType x = RealType.getRealType("ImageElement", horizUnit);
    RealType y = RealType.getRealType("ImageLine", vertUnit);
    RealType value = RealType.getRealType("intensity");
    RealTupleType xy = new RealTupleType(x, y);
    imageFunction = new FunctionType(xy, value);

    // set up image domain set
    imageSet = new Linear2DSet(xy,
      horizOffset, horizOffset + (nx - 1) * horizStep, nx,
      vertOffset + (ny - 1) * vertStep, vertOffset, ny,
      null, new Unit[] {horizUnit, vertUnit}, null);
  }


  // -- Main method --

  /**
   * Run 'java visad.data.bio.BioRadForm in_file out_file' to convert
   * in_file to out_file in Bio-Rad PIC data format.
   */
  public static void main(String[] args)
    throws VisADException, IOException
  {
    if (args == null || args.length < 1 || args.length > 2) {
      System.out.println("To convert a file to Bio-Rad PIC, run:");
      System.out.println("  java visad.data.bio.BioRadForm in_file out_file");
      System.out.println("To test read a Bio-Rad PIC file, run:");
      System.out.println("  java visad.data.bio.BioRadForm in_file");
      System.exit(2);
    }

    if (args.length == 1) {
      // Test read Bio-Rad PIC file
      BaseTiffForm.testRead(new BioRadForm(), "Bio-Rad PIC", args);
    }
    else if (args.length == 2) {
      // Convert file to Bio-Rad PIC format
      System.out.print(args[0] + " -> " + args[1] + " ");
      DefaultFamily loader = new DefaultFamily("loader");
      DataImpl data = loader.open(args[0]);
      loader = null;
      BioRadForm form = new BioRadForm();
      form.save(args[1], data, true);
      System.out.println("[done]");
    }
  }

}
