//
// DeltavisionForm.java
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
 * DeltavisionForm is the VisAD data adapter for Deltavision files.
 *
 * @author Melissa Linkert linkert at cs.wisc.edu
 */
public class DeltavisionForm extends Form implements FormBlockReader,
  FormFileInformer, FormProgressInformer, MetadataReader, OMEReader
{

  // -- Constants --

  private static final short LITTLE_ENDIAN = -16224;


  // -- Static fields --

  private static int formCount = 0;


  // -- Fields --

  /** Current filename. */
  protected String currentId;

  /** Current file. */
  protected RandomAccessFile in;

  /** Number of images in the current file. */
  protected int numImages;

  /** Hashtable containing metadata. */
  protected Hashtable metadata;

  /** Percent complete with current operation. */
  protected double percent;

  /** Flag indicating whether current file is little endian. */
  protected boolean little;

  /** OME root node for OME-XML metadata. */
  protected Object ome;

  /** Byte array containing basic image header data. */
  protected byte[] header;

  /** Byte array containing extended header data. */
  protected byte[] extHeader;

  // -- Constructor --

  /** Constructs a new DeltavisionForm file form. */
  public DeltavisionForm() {
    super("DeltavisionForm" + formCount++);
  }


  // -- FormNode API methods --

  /**
   * Opens an existing Deltavision file from the given filename.
   *
   * @return VisAD Data object containing Deltavision data
   */
  public DataImpl open(String id)
    throws BadFormException, IOException, VisADException
  {
    percent = 0;
    int nImages = getBlockCount(id);
    FieldImpl[] fields = new FieldImpl[nImages];
    for (int i=0; i<nImages; i++) {
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
   * Saves a VisAD Data object to Deltavision format at the given location.
   *
   * @exception UnimplementedException Always thrown (this method not
   * implemented).
   */
  public void save(String id, Data data, boolean replace)
    throws BadFormException, IOException, RemoteException,
           VisADException
  {
    throw new UnimplementedException("DeltavisionForm.save");
  }

  /**
   * Adds data to an existing Deltavision file.
   *
   * @exception BadFormException Always thrown (this method not
   * implemented).
   */
  public void add(String id, Data data, boolean replace)
    throws BadFormException
  {
    throw new BadFormException("DeltavisionForm.add");
  }

  /**
   * Opens an existing Deltavision file from the given URL.
   *
   * @return VisAD data object containing Deltavision data
   * @exception UnimplementedException Always thrown (this method not
   * implemented).
   */
  public DataImpl open(URL url)
    throws BadFormException, IOException, VisADException
  {
    throw new UnimplementedException("DeltavisionForm.open(URL)");
  }

  /** Returns the data forms that are compatible with a data object. */
  public FormNode getForms(Data data) {
    return null;
  }


  // -- FormBlockReader API methods --

  /** Obtains the specified image from the given Deltavision file. */
  public DataImpl open(String id, int block_number)
    throws BadFormException, IOException, VisADException
  {
    if (!id.equals(currentId)) initFile(id);

    int width = TiffTools.bytesToInt(header, 0, 4, little);
    int height = TiffTools.bytesToInt(header, 4, 4, little);
    int numTimes = TiffTools.bytesToInt(header, 180, 2, little);
    int numWaves = TiffTools.bytesToInt(header, 196, 2, little);
    int numZs = numImages / (numWaves * numTimes);
    int pixelType = TiffTools.bytesToInt(header, 12, 4, little);

    int dimOrder = TiffTools.bytesToInt(header, 182, 2, little);

    int imageSequence = TiffTools.bytesToInt(header, 182, 2, little);

    int bytesPerPixel = 0;
    switch (pixelType) {
      case 0: bytesPerPixel = 1; break;
      case 1: bytesPerPixel = 2; break;
      case 2: bytesPerPixel = 4; break;
      case 3: bytesPerPixel = 4; break; // not well supported
      case 4: bytesPerPixel = 8; break; // not supported
      case 6: bytesPerPixel = 2; break;
    }

    // read the image plane's pixel data

    int offset = header.length + extHeader.length;
    offset += width * height * bytesPerPixel * block_number;

    int channels = 1;
    int numSamples = (int) width * (int) height;
    float[][] samples = new float[(int) channels][numSamples];

    byte[] rawData = new byte[width*height*bytesPerPixel];
    in.seek(offset);
    in.read(rawData);

    if (bytesPerPixel == 1) {
      // case for 8 bit data

      for (int i=0; i<3*numSamples; i++) {
        int q = (int) TiffTools.bytesToShort(rawData, i, 1, little);
        if (i < numSamples) { samples[0][i] = (float) q;}
        else if (channels == 3 && i < 2*numSamples) {
          samples[1][i % numSamples] = (float) q;
        }
        else if (channels == 3) {
          samples[2][i % numSamples] = (float) q;
        }
      }
    }
    else if (bytesPerPixel == 2) {
      // case for 16 bit data

      for (int i=0; i<rawData.length; i+=2) {
        int q = TiffTools.bytesToInt(rawData, i, 2, little);
        samples[0][i/2] = (float) q;
      }
    }
    else if (bytesPerPixel == 4) {
      // case for 32 bit data
      // could be broken, since we don't have any data to test

      for (int i=0; i<rawData.length; i+=4) {
        int q = TiffTools.bytesToInt(rawData, i, little);
        samples[0][i/4] = (float) q;
      }
    }
    else if (bytesPerPixel == 8) {
      // Applied Precision doesn't provide much support for 64 bit data,
      // so we won't either
      throw new BadFormException("Sorry, 64 bit pixel data not supported");
    }

    // construct the field

    RealType x = RealType.getRealType("ImageElement");
    RealType y = RealType.getRealType("ImageLine");

    RealType[] v = new RealType[(int) channels];
    for (int i=0; i<channels; i++) {
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
    }
    catch (VisADException exc) { exc.printStackTrace(); }

    return field;
  }

  /** Determines the number of images in the given Deltavision file. */
  public int getBlockCount(String id)
    throws BadFormException, IOException, VisADException
  {
    if (!id.equals(currentId)) initFile(id);

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

  /** Checks if the given string is a valid filename for a Deltavision file. */
  public boolean isThisType(String name) {
    return (name.endsWith(".dv"));
  }

  /** Checks if the given block is a valid header for a Deltavision file. */
  public boolean isThisType(byte[] block) {
    return (TiffTools.bytesToShort(block, 0, 2, little) == LITTLE_ENDIAN);
  }

  /** Returns the default file suffixes for Deltavision file format. */
  public String[] getDefaultSuffixes() {
    return new String[] {"dv", "DV"};
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


  // -- Internal DeltavisionForm API methods --

  /** Initializes the given Deltavision file. */
  protected void initFile(String id)
    throws BadFormException, IOException, VisADException
  {
    close();
    currentId = id;
    in = new RandomAccessFile(id, "r");

    // read in the image header data
    header = new byte[1024];
    in.read(header);

    int endian = TiffTools.bytesToShort(header, 96, 2, true);
    little = endian == LITTLE_ENDIAN;
    numImages = TiffTools.bytesToInt(header, 8, 4, little);

    int extSize = TiffTools.bytesToInt(header, 92, 4, little);
    extHeader = new byte[extSize];
    in.read(extHeader);
    initMetadata();
  }

  /** Populates the metadata hashtable. */
  protected void initMetadata() {
    metadata = new Hashtable();
    ome = OMETools.createRoot();

    metadata.put("ImageWidth", new Integer(TiffTools.bytesToInt(header,
      0, 4, little)));
    metadata.put("ImageHeight", new Integer(TiffTools.bytesToInt(header,
      4, 4, little)));
    metadata.put("NumberOfImages", new Integer(TiffTools.bytesToInt(header,
      8, 4, little)));
    int pixelType = TiffTools.bytesToInt(header, 12, 4, little);
    String pixel;
    String omePixel;
    switch (pixelType) {
      case 0: pixel = "8 bit unsigned integer"; omePixel = "Uint8"; break;
      case 1: pixel = "16 bit signed integer"; omePixel = "int16"; break;
      case 2: pixel = "32 bit floating point"; omePixel = "float"; break;
      case 3: pixel = "32 bit complex"; omePixel = "Uint32"; break;
      case 4: pixel = "64 bit complex"; omePixel = "float"; break;
      case 6: pixel = "16 bit unsigned integer"; omePixel = "Uint16"; break;
      default: pixel = "unknown"; omePixel = "Uint8";
    }

    if (ome != null) {
      OMETools.setAttribute(ome, "Pixels", "SizeX",
        "" + metadata.get("ImageWidth"));
      OMETools.setAttribute(ome, "Pixels", "SizeY",
        "" + metadata.get("ImageHeight"));
      OMETools.setAttribute(ome, "Pixels", "PixelType", "" + omePixel);
      OMETools.setAttribute(ome, "Pixels", "BigEndian", "" + !little);
    }

    metadata.put("PixelType", pixel);
    metadata.put("Sub-image starting point (X)", new Integer(
      TiffTools.bytesToInt(header, 16, 4, little)));
    metadata.put("Sub-image starting point (Y)", new Integer(
      TiffTools.bytesToInt(header, 20, 4, little)));
    metadata.put("Sub-image starting point (Z)", new Integer(
      TiffTools.bytesToInt(header, 24, 4, little)));
    metadata.put("Pixel sampling size (X)", new Integer(
      TiffTools.bytesToInt(header, 28, 4, little)));
    metadata.put("Pixel sampling size (Y)", new Integer(
      TiffTools.bytesToInt(header, 32, 4, little)));
    metadata.put("Pixel sampling size (Z)", new Integer(
      TiffTools.bytesToInt(header, 36, 4, little)));
    metadata.put("X element length (in um)", new Float(Float.intBitsToFloat(
      TiffTools.bytesToInt(header, 40, 4, little))));
    metadata.put("Y element length (in um)", new Float(Float.intBitsToFloat(
      TiffTools.bytesToInt(header, 44, 4, little))));
    metadata.put("Z element length (in um)", new Float(Float.intBitsToFloat(
      TiffTools.bytesToInt(header, 48, 4, little))));
    metadata.put("X axis angle", new Float(Float.intBitsToFloat(
      TiffTools.bytesToInt(header, 52, 4, little))));
    metadata.put("Y axis angle", new Float(Float.intBitsToFloat(
      TiffTools.bytesToInt(header, 56, 4, little))));
    metadata.put("Z axis angle", new Float(Float.intBitsToFloat(
      TiffTools.bytesToInt(header, 60, 4, little))));
    metadata.put("Column axis sequence", new Integer(TiffTools.bytesToInt(
      header, 64, 4, little)));
    metadata.put("Row axis sequence", new Integer(TiffTools.bytesToInt(
      header, 68, 4, little)));
    metadata.put("Section axis sequence", new Integer(TiffTools.bytesToInt(
      header, 72, 4, little)));
    metadata.put("Wavelength 1 min. intensity", new Float(Float.intBitsToFloat(
      TiffTools.bytesToInt(header, 76, 4, little))));
    metadata.put("Wavelength 1 max. intensity", new Float(Float.intBitsToFloat(
      TiffTools.bytesToInt(header, 80, 4, little))));
    metadata.put("Wavelength 1 mean intensity", new Float(Float.intBitsToFloat(
      TiffTools.bytesToInt(header, 84, 4, little))));
    metadata.put("Space group number", new Integer(TiffTools.bytesToInt(
      header, 88, 4, little)));
    metadata.put("Number of Sub-resolution sets", new Integer(
      TiffTools.bytesToShort(header, 132, 2, little)));
    metadata.put("Z axis reduction quotient", new Integer(
      TiffTools.bytesToShort(header, 134, 2, little)));
    metadata.put("Wavelength 2 min. intensity", new Float(
      Float.intBitsToFloat(TiffTools.bytesToInt(header, 136, 4, little))));
    metadata.put("Wavelength 2 max. intensity", new Float(
      Float.intBitsToFloat(TiffTools.bytesToInt(header, 140, 4, little))));
    metadata.put("Wavelength 3 min. intensity", new Float(
      Float.intBitsToFloat(TiffTools.bytesToInt(header, 144, 4, little))));
    metadata.put("Wavelength 3 max. intensity", new Float(
      Float.intBitsToFloat(TiffTools.bytesToInt(header, 148, 4, little))));
    metadata.put("Wavelength 4 min. intensity", new Float(
      Float.intBitsToFloat(TiffTools.bytesToInt(header, 152, 4, little))));
    metadata.put("Wavelength 4 max. intensity", new Float(
      Float.intBitsToFloat(TiffTools.bytesToInt(header, 156, 4, little))));
    int type = TiffTools.bytesToShort(header, 160, 2, little);
    String imageType;
    switch (type) {
      case 0: imageType = "normal"; break;
      case 1: imageType = "Tilt-series"; break;
      case 2: imageType = "Stereo tilt-series"; break;
      case 3: imageType = "Averaged images"; break;
      case 4: imageType = "Averaged stereo pairs"; break;
      default: imageType = "unknown";
    }

    metadata.put("Image Type", imageType);
    metadata.put("Lens ID Number", new Integer(TiffTools.bytesToShort(
      header, 162, 2, little)));
    metadata.put("Wavelength 5 min. intensity", new Float(
      Float.intBitsToFloat(TiffTools.bytesToInt(header, 172, 4, little))));
    metadata.put("Wavelength 5 max. intensity", new Float(
      Float.intBitsToFloat(TiffTools.bytesToInt(header, 176, 4, little))));
    int numT = TiffTools.bytesToShort(header, 180, 2, little);
    metadata.put("Number of timepoints", new Integer(numT));
    if (ome != null) OMETools.setAttribute(ome, "Pixels", "SizeT", "" + numT);

    int sequence = TiffTools.bytesToInt(header, 182, 4, little);
    String imageSequence;
    String dimOrder;
    switch (sequence) {
      case 0: imageSequence = "ZTW"; dimOrder = "XYZTC"; break;
      case 1: imageSequence = "WZT"; dimOrder = "XYCZT"; break;
      case 2: imageSequence = "ZWT"; dimOrder = "XYZCT"; break;
      default: imageSequence = "unknown"; dimOrder = "XYZTC";
    }
    metadata.put("Image sequence", imageSequence);
    if (ome != null) {
      OMETools.setAttribute(ome, "Pixels", "DimensionOrder", dimOrder);
    }

    metadata.put("X axis tilt angle", new Float(Float.intBitsToFloat(
      TiffTools.bytesToInt(header, 184, 4, little))));
    metadata.put("Y axis tilt angle", new Float(Float.intBitsToFloat(
      TiffTools.bytesToInt(header, 188, 4, little))));
    metadata.put("Z axis tilt angle", new Float(Float.intBitsToFloat(
      TiffTools.bytesToInt(header, 192, 4, little))));
    int numW = TiffTools.bytesToShort(header, 196, 2, little);
    metadata.put("Number of wavelengths", new Integer(numW));
    if (ome != null) OMETools.setAttribute(ome, "Pixels", "SizeC", "" + numW);
    int numZ = numImages / (numW * numT);
    metadata.put("Number of focal planes", new Integer(numZ));
    if (ome != null) OMETools.setAttribute(ome, "Pixels", "SizeZ", "" + numZ);

    metadata.put("Wavelength 1 (in nm)", new Integer(TiffTools.bytesToShort(
      header, 198, 2, little)));
    metadata.put("Wavelength 2 (in nm)", new Integer(TiffTools.bytesToShort(
      header, 200, 2, little)));
    metadata.put("Wavelength 3 (in nm)", new Integer(TiffTools.bytesToShort(
      header, 202, 2, little)));
    metadata.put("Wavelength 4 (in nm)", new Integer(TiffTools.bytesToShort(
      header, 204, 2, little)));
    metadata.put("Wavelength 5 (in nm)", new Integer(TiffTools.bytesToShort(
      header, 206, 2, little)));
    metadata.put("X origin (in um)", new Float(Float.intBitsToFloat(
      TiffTools.bytesToInt(header, 208, 4, little))));
    metadata.put("Y origin (in um)", new Float(Float.intBitsToFloat(
      TiffTools.bytesToInt(header, 212, 4, little))));
    metadata.put("Z origin (in um)", new Float(Float.intBitsToFloat(
      TiffTools.bytesToInt(header, 216, 4, little))));
    int numTitles = TiffTools.bytesToInt(header, 220, 4, little);

    if (ome != null) {
      OMETools.setAttribute(ome, "StageLabel", "X",
        "" + metadata.get("X origin (in um)"));
      OMETools.setAttribute(ome, "StageLabel", "Y",
        "" + metadata.get("Y origin (in um)"));
      OMETools.setAttribute(ome, "StageLabel", "Z",
        "" + metadata.get("Z origin (in um)"));
    }

    for (int i=1; i<=10; i++) {
      metadata.put("Title " + i, new String(header, 224 + 80*(i-1), 80));
      if (i == 1 && ome != null) {
        OMETools.setAttribute(ome, "Image", "Description",
          "" + metadata.get("Title 1"));
      }
    }
  }


  // -- Main method --

  /**
   * Run 'java visad.data.bio.DeltavisionForm in_file' to test read
   * a Deltavision file.
   */
  public static void main(String[] args)
    throws VisADException, IOException, RemoteException
  {
    BaseTiffForm.testRead(new DeltavisionForm(), "Deltavision", args);
  }

}
