//
// IPLabForm.java
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
import java.net.*;
import java.util.Hashtable;
import visad.*;
import visad.data.*;
import visad.data.tiff.BaseTiffForm;
import visad.data.tiff.TiffTools;

/**
 * IPLabForm is the VisAD data adapter for IPLab (.IPL) files.
 *
 * @author Melissa Linkert linkert at cs.wisc.edu
 */
public class IPLabForm extends Form implements FormBlockReader,
  FormFileInformer, FormProgressInformer, MetadataReader, OMEReader
{

  // -- Static fields --

  private static int formCount = 0;


  // -- Fields --

  /** Current filename. */
  protected String currentId;

  /** Current file. */
  protected RandomAccessFile in;

  /** Hashtable containing metadata. */
  protected Hashtable metadata;

  /** Percent complete with current operation. */
  protected double percent;

  /** Flag indicating whether current file is little endian. */
  protected boolean littleEndian;

  /** OME root node for OME-XML metadata. */
  protected Object ome;


  // -- Constructor --

  /** Constructs a new IPLabForm file form. */
  public IPLabForm() {
    super("IPLabForm" + formCount++);
  }


  // -- FormNode API methods --

  /**
   * Opens an existing IPLab file from the given filename.
   *
   * @return VisAD Data object containing IPLab data
   */

  public DataImpl open(String id)
    throws BadFormException, IOException, VisADException
  {
    percent = 0;
    int nImages = getBlockCount(id);
    FieldImpl[] fields = new FieldImpl[nImages];
    for(int i=0; i<nImages; i++) {
      fields[i] = (FieldImpl) open(id, i);
      percent = (double) (i+1) / nImages;
    }

    DataImpl data;
    if (nImages == 1) data = fields[0];
    else {
      // combine data stack into index function
      RealType index = RealType.getRealType("index");
      FunctionType indexFunction =
        new FunctionType(index, fields[0].getType());
      Integer1DSet indexSet = new Integer1DSet(nImages);
      FieldImpl indexField = new FieldImpl(indexFunction,
                indexSet);
      indexField.setSamples(fields, false);
      data = indexField;
    }
    close();
    percent = Double.NaN;
    return data;
  }

  /**
   * Saves a VisAD Data object to IPLab format at the given location.
   *
   * @exception UnimplementedException Always throws (this method not
   * implemented).
   */
  public void save(String id, Data data, boolean replace)
    throws BadFormException, IOException, RemoteException,
           VisADException
  {
    throw new UnimplementedException("IPLabForm.save");
  }

  /**
   * Adds data to an existing IPLab file.
   *
   * @exception BadFormException Always thrown (this method not
   * implemented).
   */
  public void add(String id, Data data, boolean replace)
    throws BadFormException
  {
    throw new BadFormException("IPLabForm.add");
  }

  /**
   * Opens an existing IPLab file from the given URL.
   *
   * @return VisAD data object containing IPLab data
   * @exception UnimplementedException Always thrown (this method not
   * implemented).
   */
  public DataImpl open(URL url)
    throws BadFormException, IOException, VisADException
  {
    throw new UnimplementedException("IPLabForm.open(URL)");
  }

  /** Returns the data forms that are compatible with a data object. */
  public FormNode getForms(Data data) {
    return null;
  }


  // -- FormBlockReader API methods --

  /** Obtains the specified image from the given IPLab file. */
  public DataImpl open(String id, int block_number)
    throws BadFormException, IOException, VisADException
  {
    if (!id.equals(currentId)) initFile(id);

    if (block_number < 0 || block_number >= getBlockCount(id)) {
      throw new BadFormException("Invalid image number: " + block_number);
    }

    // read image bytes and convert to floats

    in.seek(0);
    in.skipBytes(16);

    long dataSize = TiffTools.read4UnsignedBytes(in, littleEndian);
    dataSize -= 28; // size of raw image data, in bytes
    long width = TiffTools.read4UnsignedBytes(in, littleEndian);
    long height = TiffTools.read4UnsignedBytes(in, littleEndian);
    long channels = TiffTools.read4UnsignedBytes(in, littleEndian);
    long zDepth = TiffTools.read4UnsignedBytes(in, littleEndian);
    long tDepth = TiffTools.read4UnsignedBytes(in, littleEndian);
    long pixelType = TiffTools.read4UnsignedBytes(in, littleEndian);
    byte[] rawData = new byte[(int) dataSize];
    in.readFully(rawData);

    int[] bitsPerSample = new int[1];
    // bitsPerSample is dependent on the pixel type

    switch ((int) pixelType) {
      case 0: bitsPerSample[0] = 8; break;
      case 1: bitsPerSample[0] = 16; break;
      case 2: bitsPerSample[0] = 16; break;
      case 3: bitsPerSample[0] = 32; break;
      case 4: bitsPerSample[0] = 32; break;
      case 10: bitsPerSample[0] = 64; break;
    }

    int numSamples = (int) width * (int) height;
    float[][] samples = new float[(int) channels][numSamples];

    if (bitsPerSample[0] == 8) {
      // case for 8 bit data

      for (int i=0; i<3*numSamples; i++) {
        int q = (int) TiffTools.bytesToShort(rawData, i, 1, littleEndian);
        if (i < numSamples) { samples[0][i] = (float) q;}
        else if (channels == 3 && i < 2*numSamples) {
          samples[1][i % numSamples] = (float) q;
        }

        else if (channels == 3) {
          samples[2][i % numSamples] = (float) q;
        }
      }
    }
    else if (bitsPerSample[0] == 16) {
      // case for 16 bit data

      for(int i=0; i<rawData.length; i+=2) {
        int q = ((0x000000ff & rawData[i+1]) << 8) | (0x000000ff & rawData[i]);

        if (i < 2*numSamples) {
          samples[0][i/2] = (float) q;
        }
        else if (channels == 3 && i < 4*numSamples) {
          samples[1][i % numSamples] = (float) q;
        }
        else if (channels == 3) {
          samples[2][i % numSamples] = (float) q;
        }
      }
    }

    // construct the field

    RealType x = RealType.getRealType("ImageElement");
    RealType y = RealType.getRealType("ImageLine");

    RealType[] v = new RealType[(int) channels];
    for(int i=0; i<channels; i++) {
      v[i] = RealType.getRealType("value" + i);
    }

    FlatField field = null;
    try {
      RealTupleType domain = new RealTupleType(x, y);
      RealTupleType range = new RealTupleType(v);
      FunctionType fieldType = new FunctionType(domain, range);
      Linear2DSet fieldSet = new Linear2DSet(domain, 0.0,
        width - 1.0, (int) width, height - 1.0, 0.0, (int) height);

      field = new FlatField(fieldType, fieldSet);
      field.setSamples(samples, false);
    }                    catch(VisADException exc) { exc.printStackTrace(); }

    return field;
  }

  /** Determines the number of images in the given IPLab file. */
  public int getBlockCount(String id)
    throws BadFormException, IOException, VisADException
  {
    if (!id.equals(currentId)) initFile(id);

    in.seek(0);
    in.skipBytes(32);
    long zDepth = TiffTools.read4UnsignedBytes(in, littleEndian);
    long tDepth = TiffTools.read4UnsignedBytes(in, littleEndian);

    int numImages = (int) (zDepth * tDepth);
    return numImages;
  }

  /** Closes any open files. */
  public void close()
    throws BadFormException, IOException, VisADException
  {
    if (in != null) in.close();
    in = null;
    currentId = null;
  }


  // -- FormFileInformer API methods --

  /** Checks if the given string is a valid filename for an IPLab file. */
  public boolean isThisType(String name) {
    String s = name.toLowerCase();
    return s.endsWith(".ipl");
  }

  /** Checks if the given block is a valid header for an IPLab file. */
  public boolean isThisType(byte[] block) {
    if (block.length < 12) return false; // block length too short
    String s = new String(block, 0, 4);
    boolean big = s.equals("iiii");
    boolean little = s.equals("mmmm");
    if (!big && !little) return false;
    int size = TiffTools.bytesToInt(block, 4, 4, little);
    if (size != 4) return false; // first block size should be 4
    int version = TiffTools.bytesToInt(block, 8, 4, little);
    if (version < 0x100e) return false; // invalid version
    return true;
  }

  /** Returns the default file suffixes for IPLab file format. */
  public String[] getDefaultSuffixes() {
    return new String[] {"ipl"};
  }


  // -- FormProgressInformer API methods --

  /** Gets the percentage complete of the form's current operation. */
  public double getPercentComplete() {
    return percent;
  }


  // -- MetadataReader API methods --

  /**
   * Obtains the specified metadata field's value for the given file.
   *
   * @param field the name associated with the metadata field
   * @return the value, or null if the field doesn't exist
   */
  public Object getMetadataValue(String id, String field)
    throws BadFormException, IOException, VisADException
  {
    if (!id.equals(currentId)) initFile(id);
    return metadata.get(field);
  }

  /**
   * Obtains the hashtable containing the metadata field/value pairs from
   * the given file.
   *
   * @param id the filename
   * @return the hashtable containing all metadata from the file
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


  // -- Internal IPLabForm API methods --

  /** Initializes the given IPLab file. */
  protected void initFile(String id)
    throws BadFormException, IOException, VisADException
  {
    close();
    currentId = id;
    in = new RandomAccessFile(id, "r");

    byte[] fourBytes = new byte[4];
    in.read(fourBytes);
    littleEndian = new String(fourBytes).equals("iiii");

    // populate standard metadata hashtable and OME root node
    metadata = new Hashtable();
    in.seek(0);
    in.skipBytes(16);

    long dataSize = TiffTools.read4UnsignedBytes(in, littleEndian);
    dataSize -= 28; // size of raw image data, in bytes
    long width = TiffTools.read4UnsignedBytes(in, littleEndian);
    long height = TiffTools.read4UnsignedBytes(in, littleEndian);
    long channels = TiffTools.read4UnsignedBytes(in, littleEndian);
    long zDepth = TiffTools.read4UnsignedBytes(in, littleEndian);
    long tDepth = TiffTools.read4UnsignedBytes(in, littleEndian);
    long pixelType = TiffTools.read4UnsignedBytes(in, littleEndian);

    metadata.put("Width", new Long(width));
    metadata.put("Height", new Long(height));
    metadata.put("Channels", new Long(channels));
    metadata.put("ZDepth", new Long(zDepth));
    metadata.put("TDepth", new Long(tDepth));

    String ptype;
    switch ((int) pixelType) {
      case 0: ptype = "8 bit unsigned"; break;
      case 1: ptype = "16 bit signed short"; break;
      case 2: ptype = "16 bit unsigned short"; break;
      case 3: ptype = "32 bit signed long"; break;
      case 4: ptype = "32 bit single-precision float"; break;
      case 5: ptype = "Color24"; break;
      case 6: ptype = "Color48"; break;
      case 10: ptype = "64 bit double-precision float"; break;
      default: ptype = "reserved";    // for values 7-9
    }

    metadata.put("PixelType", ptype);
    in.skipBytes((int) dataSize);

    ome = OMETools.createRoot();
    if (ome != null) {
      OMETools.setAttribute(ome, "Pixels", "SizeX", "" + width);
      OMETools.setAttribute(ome, "Pixels", "SizeY", "" + height);
      OMETools.setAttribute(ome, "Pixels", "SizeZ", "" + zDepth);
      OMETools.setAttribute(ome, "Pixels", "SizeC", "" + channels);
      OMETools.setAttribute(ome, "Pixels", "SizeT", "" + tDepth);
      OMETools.setAttribute(ome, "Pixels", "BigEndian",
        littleEndian ? "false" : "true");
      OMETools.setAttribute(ome, "Pixels", "DimensionOrder", "XYZTC");

      // set the pixel type
      String type;
      switch ((int) pixelType) {
        case 0: type = "Uint8"; break;
        case 1: type = "int16"; break;
        case 2: type = "Uint16"; break;
        case 3: type = "Uint32"; break;
        case 4: type = "float"; break;
        case 5: type = "Uint32"; break;
        case 6: type = "Uint32"; break;
        case 10: type = "float"; break;
        default: type = "Uint8";
      }

      OMETools.setAttribute(ome, "Pixels", "PixelType", type);
      OMETools.setAttribute(ome, "Image", "Name", id);
    }

    in.read(fourBytes);
    String tag = new String(fourBytes);
    while (!tag.equals("fini")) {
      if (tag.equals("clut")) {
        // read in Color Lookup Table
        long size = TiffTools.read4UnsignedBytes(in, littleEndian);
        if (size == 8) {
          // indexed lookup table
          in.skipBytes(4);
          long type=TiffTools.read4UnsignedBytes(in, littleEndian);
          String clutType;
          switch ((int) type) {
            case 0: clutType = "monochrome"; break;
            case 1: clutType = "reverse monochrome"; break;
            case 2: clutType = "BGR"; break;
            case 3: clutType = "classify"; break;
            case 4: clutType = "rainbow"; break;
            case 5: clutType = "red"; break;
            case 6: clutType = "green"; break;
            case 7: clutType = "blue"; break;
            case 8: clutType = "cyan"; break;
            case 9: clutType = "magenta"; break;
            case 10: clutType = "yellow"; break;
            case 11: clutType = "saturated pixels"; break;
          }
        }
        else {
          // explicitly defined lookup table
          // length is 772
          in.skipBytes(4);
          byte[] colorTable = new byte[256*3];
          in.read(colorTable);
        }
      }
      else if (tag.equals("norm")) {
        // read in normalization information

        long size = TiffTools.read4UnsignedBytes(in, littleEndian);
        // error checking

        if (size != (44 * channels)) {
          throw new BadFormException("Bad normalization settings");
        }

        for(int i=0; i<channels; i++) {
          long source = TiffTools.read4UnsignedBytes(in, littleEndian);

          String sourceType;
          switch ((int) source) {
            case 0: sourceType = "user"; break;
            case 1: sourceType = "plane"; break;
            case 2: sourceType = "sequence"; break;
            case 3: sourceType = "saturated plane"; break;
            case 4: sourceType = "saturated sequence"; break;
            case 5: sourceType = "ROI"; break;
            default: sourceType = "user";
          }
          metadata.put("NormalizationSource" + i, sourceType);

          double min=TiffTools.read8SignedBytes(in, littleEndian);
          double max=TiffTools.read8SignedBytes(in, littleEndian);
          double gamma=TiffTools.read8SignedBytes(in, littleEndian);
          double black=TiffTools.read8SignedBytes(in, littleEndian);
          double white=TiffTools.read8SignedBytes(in, littleEndian);

          metadata.put("NormalizationMin" + i, new Double(min));
          metadata.put("NormalizationMax" + i, new Double(max));
          metadata.put("NormalizationGamma" + i, new Double(gamma));
          metadata.put("NormalizationBlack" + i, new Double(black));
          metadata.put("NormalizationWhite" + i, new Double(white));
        }
      }
      else if (tag.equals("head")) {
        // read in header labels

        in.skipBytes(4);  // size is defined to 2200

        for(int i=0; i<100; i++) {
          int num = TiffTools.read2UnsignedBytes(in, littleEndian);
          in.read(fourBytes);
          String name = new String(fourBytes);
          metadata.put("Header" + num, name);
        }
      }
      else if (tag.equals("roi ")) {
        // read in ROI information

        long size = TiffTools.read4UnsignedBytes(in, littleEndian);
        long roiType = TiffTools.read4UnsignedBytes(in, littleEndian);
        long roiLeft = TiffTools.read4UnsignedBytes(in, littleEndian);
        long roiTop = TiffTools.read4UnsignedBytes(in, littleEndian);
        long roiRight = TiffTools.read4UnsignedBytes(in, littleEndian);
        long roiBottom = TiffTools.read4UnsignedBytes(in, littleEndian);
        long numRoiPts = TiffTools.read4UnsignedBytes(in, littleEndian);

        if (ome != null) {
          OMETools.setAttribute(ome, "ROI", "X0",
            new Long(roiLeft).toString());
          OMETools.setAttribute(ome, "ROI", "X1",
            new Long(roiRight).toString());
          OMETools.setAttribute(ome, "ROI", "Y0",
            new Long(roiBottom).toString());
          OMETools.setAttribute(ome, "ROI", "Y1",
            new Long(roiTop).toString());
        }

        for(int i=0; i<numRoiPts; i++) {
          long ptX = TiffTools.read4UnsignedBytes(in, littleEndian);
          long ptY = TiffTools.read4UnsignedBytes(in, littleEndian);
        }
      }
      else if (tag.equals("mask")) {
        // read in Segmentation Mask
      }
      else if (tag.equals("unit")) {
        // read in units
        in.skipBytes(4); // size is 48

        for(int i=0; i<4; i++) {
          long xResStyle = TiffTools.read4UnsignedBytes(in, littleEndian);
          long unitsPerPixel = TiffTools.read4UnsignedBytes(in, littleEndian);
          long xUnitName = TiffTools.read4UnsignedBytes(in, littleEndian);

          metadata.put("ResolutionStyle" + i, new Long(xResStyle));
          metadata.put("UnitsPerPixel" + i, new Long(unitsPerPixel));

          if (i == 0 && ome != null) {
            OMETools.setAttribute(ome,
              "Image", "PixelSizeX", "" + unitsPerPixel);
            OMETools.setAttribute(ome,
              "Image", "PixelSizeY", "" + unitsPerPixel);
          }

          metadata.put("UnitName" + i, new Long(xUnitName));
        }
      }
      else if (tag.equals("view")) {
        // read in view
        in.skipBytes(4);
      }
      else if (tag.equals("plot")) {
        // read in plot
        // skipping this field for the moment
        in.skipBytes(4); // size is 2508
        in.skipBytes(2508);
      }
      else if (tag.equals("notes")) {
        // read in notes (image info)
        in.skipBytes(4); // size is 576
        byte[] temp = new byte[64];
        in.read(temp);
        String descriptor = new String(temp);
        temp = new byte[512];
        in.read(temp);
        String notes = new String(temp);
        metadata.put("Descriptor", descriptor);
        metadata.put("Notes", notes);

        if (ome != null) {
          OMETools.setAttribute(ome, "Image", "Description", notes);
        }
      }
      int r = in.read(fourBytes);
      if (r < 0) { // eof
        throw new BadFormException("Unexpected end of file");
      }
      tag = new String(fourBytes);
    }
  }


  // -- Main method --

  /**
   * Run 'java visad.data.bio.IPLabForm in_file' to test read
   * an IPLab file.
   */
  public static void main(String[] args)
    throws VisADException, IOException, RemoteException
  {
    BaseTiffForm.testRead(new IPLabForm(), "IPLab", args);
  }

}
