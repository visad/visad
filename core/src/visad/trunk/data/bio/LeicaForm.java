//
// LeicaForm.java
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
import java.util.Vector;
import visad.*;
import visad.data.*;
import visad.data.tiff.BaseTiffForm;
import visad.data.tiff.TiffTools;
import visad.data.tiff.TiffForm;

/**
 * LeicaForm is the VisAD data adapter for Leica files.
 *
 * @author Melissa Linkert linkert at cs.wisc.edu
 */
public class LeicaForm extends Form implements FormBlockReader,
  FormFileInformer, FormProgressInformer, MetadataReader, OMEReader
{

  // -- Static fields --

  private static int formCount = 0;


  // -- Fields --

  /** Flag indicating whether current file is little endian. */
  protected boolean littleEndian;

  /** Array of IFD-like structures containing metadata. */
  protected Hashtable[] headerIFDs;

  /** Array of image file names. */
  protected String[] files;

  /** Helper form. */
  protected TiffForm tiff;

  /** OME-XML root node. */
  protected Object ome;

  /** Hashtable for storing standard metadata. */
  protected Hashtable metadata;

  /** Current file. */
  protected RandomAccessFile in;

  /** Number of images in the dataset. */
  protected int numImages;

  /** Current file name. */
  protected String currentId;

  /** Percent complete. */
  protected double percent;


  // -- Constructor --

  /** Constructs a new LeicaForm file form. */
  public LeicaForm() {
    super("LeicaForm" + formCount++);
    tiff = new TiffForm();
  }


  // -- FormNode API methods --

  /**
   * Opens an existing Leica file from the given filename.
   *
   * @return VisAD Data object containing Leica data
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
   * Saves a VisAD Data object to Leica format at the given location.
   *
   * @exception UnimplementedException Always throws (this method not
   * implemented).
   */
  public void save(String id, Data data, boolean replace)
    throws BadFormException, IOException, RemoteException,
           VisADException
  {
    throw new UnimplementedException("LeicaForm.save");
  }

  /**
   * Adds data to an existing Leica file.
   *
   * @exception BadFormException Always thrown (this method not
   * implemented).
   */
  public void add(String id, Data data, boolean replace)
    throws BadFormException
  {
    throw new BadFormException("LeicaForm.add");
  }

  /**
   * Opens an existing Leica file from the given URL.
   *
   * @return VisAD data object containing Leica data
   * @exception UnimplementedException Always thrown (this method not
   * implemented).
   */
  public DataImpl open(URL url)
    throws BadFormException, IOException, VisADException
  {
    throw new UnimplementedException("LeicaForm.open(URL)");
  }

  /** Returns the data forms that are compatible with a data object. */
  public FormNode getForms(Data data) {
    return null;
  }


  // -- FormBlockReader API methods --

  /** Obtains the specified image from the given Leica file. */
  public DataImpl open(String id, int block_number)
    throws BadFormException, IOException, VisADException
  {
    if (!id.equals(currentId)) initFile(id);

    if (block_number < 0 || block_number >= numImages) {
      throw new BadFormException("Invalid image number: " + block_number);
    }

    return tiff.open(files[block_number], 0);
  }

  /** Determines the number of images in the given Leica file. */
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

  /** Checks if the given string is a valid filename for a Leica file. */
  public boolean isThisType(String name) {
    return name.toLowerCase().endsWith("lei");
  }

  /** Checks if the given block is a valid header for a Leica file. */
  public boolean isThisType(byte[] block) {
    // check that block has value of 0x016033F0
    return (block[0] == 0x01 && block[1] == 0x60 && block[2] == 0x33 &&
      block[3] == 0xF0) || (block[0] == 0xF0 && block[1] == 0x33 &&
      block[2] == 0x60 && block[3] == 0x01);
  }

  /** Returns the default file suffixes for Leica file format. */
  public String[] getDefaultSuffixes() {
    return new String[] {"lei", "LEI"};
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


  // -- Internal BaseTiffForm API methods --

  /** Initializes the given Leica file. */
  protected void initFile(String id)
    throws BadFormException, IOException, VisADException
  {
    close();
    currentId = id;
    in = new RandomAccessFile(id, "r");
    metadata = new Hashtable();

    byte[] fourBytes = new byte[4];
    in.read(fourBytes);
    littleEndian = (fourBytes[0] == TiffTools.LITTLE &&
      fourBytes[1] == TiffTools.LITTLE &&
      fourBytes[2] == TiffTools.LITTLE &&
      fourBytes[3] == TiffTools.LITTLE);

    in.skipBytes(8);
    int addr = (int) TiffTools.read4UnsignedBytes(in, littleEndian);
    Vector v = new Vector();
    while (addr != 0) {
      Hashtable ifd = new Hashtable();
      v.add(ifd);
      in.seek(addr);
      int numEntries = (int) TiffTools.read4UnsignedBytes(in, littleEndian);
      int tag = (int) TiffTools.read4UnsignedBytes(in, littleEndian);

      int numIFDs = 0;
      while (tag != 0) {
        // create the IFD structure
        int offset = (int) TiffTools.read4UnsignedBytes(in,
          littleEndian);
        int fp = (int) in.getFilePointer();
        in.seek(offset + 12);
        int size = (int) TiffTools.read4UnsignedBytes(in, littleEndian);
        byte[] data = new byte[size];
        in.read(data);
        ifd.put(new Integer(tag), (Object) data);
        in.seek(fp);
        tag = (int) TiffTools.read4UnsignedBytes(in, littleEndian);
      }

      addr = (int) TiffTools.read4UnsignedBytes(in, littleEndian);
    }
    headerIFDs = new Hashtable[v.size()];
    v.copyInto(headerIFDs);

    // determine the length of a filename

    int nameLength = 0;
    for (int i=0; i<headerIFDs.length; i++) {
      if (headerIFDs[i].get(new Integer(10)) != null) {
        byte[] temp = (byte[]) headerIFDs[i].get(new Integer(10));
        nameLength = TiffTools.bytesToInt(temp, 8, 4, littleEndian);
      }
    }

    Vector f = new Vector();
    for (int i=0; i<headerIFDs.length; i++) {
      byte[] tempData = (byte[]) headerIFDs[i].get(new Integer(15));
      int tempImages = TiffTools.bytesToInt(tempData, 0, 4, littleEndian);
      for (int j=0; j<tempImages; j++) {
        // read in each filename
        f.add(new String(tempData, 20 + 2*(j*nameLength), 2*nameLength));
      }
    }

    files = new String[f.size()];
    numImages = f.size();
    f.copyInto(files);

    String dirPrefix = new File(id).getParent();
    dirPrefix = dirPrefix == null ? "" : (dirPrefix + File.separator);
    for (int i=0; i<files.length; i++) {
      files[i] = dirPrefix + stripString(files[i]);
    }
    initMetadata();
  }

  /** Populates the metadata hashtable and OME root node. */
  protected void initMetadata() {
    ome = OMETools.createRoot();

    for (int i=0; i<headerIFDs.length; i++) {
      byte[] temp = (byte[]) headerIFDs[i].get(new Integer(10));
      if (temp != null) {
        // the series data
        // ID_SERIES

        metadata.put("Version",
          new Integer(TiffTools.bytesToInt(temp, 0, 4, littleEndian)));
        metadata.put("Number of Series",
          new Integer(TiffTools.bytesToInt(temp, 4, 4, littleEndian)));
        metadata.put("Length of filename",
          new Integer(TiffTools.bytesToInt(temp, 8, 4, littleEndian)));
        metadata.put("Length of file extension",
          new Integer(TiffTools.bytesToInt(temp, 12, 4, littleEndian)));
        metadata.put("Image file extension", stripString(new String(temp, 16,
          ((Integer) metadata.get("Length of file extension")).intValue())));
      }

      temp = (byte[]) headerIFDs[i].get(new Integer(15));
      if (temp != null) {
        // the image data
        // ID_IMAGES

        metadata.put("Number of images", new Integer(
          TiffTools.bytesToInt(temp, 0, 4, littleEndian)));
        metadata.put("Image width", new Integer(
          TiffTools.bytesToInt(temp, 4, 4, littleEndian)));
        metadata.put("Image height", new Integer(
          TiffTools.bytesToInt(temp, 8, 4, littleEndian)));
        metadata.put("Bits per Sample", new Integer(
          TiffTools.bytesToInt(temp, 12, 4, littleEndian)));
        metadata.put("Samples per pixel", new Integer(
          TiffTools.bytesToInt(temp, 16, 4, littleEndian)));
      }

      temp = (byte[]) headerIFDs[i].get(new Integer(20));
      if (temp != null) {
        // dimension description
        // ID_DIMDESCR

        int pt = 0;
        metadata.put("Voxel Version", new Integer(
          TiffTools.bytesToInt(temp, 0, 4, littleEndian)));
        int voxelType = TiffTools.bytesToInt(temp, 4, 4, littleEndian);
        String type = "";
        switch (voxelType) {
          case 0: type = "undefined"; break;
          case 10: type = "gray normal"; break;
          case 20: type = "RGB"; break;
        }

        metadata.put("VoxelType", type);

        metadata.put("Bytes per pixel", new Integer(
          TiffTools.bytesToInt(temp, 8, 4, littleEndian)));
        metadata.put("Real world resolution", new Integer(
          TiffTools.bytesToInt(temp, 12, 4, littleEndian)));
        int length = TiffTools.bytesToInt(temp, 16, 4, littleEndian);
        metadata.put("Maximum voxel intensity",
          stripString(new String(temp, 20, length)));
        pt = 20 + length;
        pt += 4;
        metadata.put("Minimum voxel intensity",
          stripString(new String(temp, pt, length)));
        pt += length;
        length = TiffTools.bytesToInt(temp, pt, 4, littleEndian);
        pt += 4 + length + 4;

        length = TiffTools.bytesToInt(temp, pt, 4, littleEndian);
        for (int j=0; j<length; j++) {
          int dimId = TiffTools.bytesToInt(temp, pt, 4, littleEndian);
          String dimType = "";
          switch (dimId) {
            case 0: dimType = "undefined"; break;
            case 120: dimType = "x"; break;
            case 121: dimType = "y"; break;
            case 122: dimType = "z"; break;
            case 116: dimType = "t"; break;
            case 6815843: dimType = "channel"; break;
            case 6357100: dimType = "wave length"; break;
            case 7602290: dimType = "rotation"; break;
            case 7798904: dimType = "x-wide for the motorized xy-stage"; break;
            case 7798905: dimType = "y-wide for the motorized xy-stage"; break;
            case 7798906: dimType = "z-wide for the z-stage-drive"; break;
            case 4259957: dimType = "user1 - unspecified"; break;
            case 4325493: dimType = "user2 - unspecified"; break;
            case 4391029: dimType = "user3 - unspecified"; break;
            case 6357095: dimType = "graylevel"; break;
            case 6422631: dimType = "graylevel1"; break;
            case 6488167: dimType = "graylevel2"; break;
            case 6553703: dimType = "graylevel3"; break;
            case 7864398: dimType = "logical x"; break;
            case 7929934: dimType = "logical y"; break;
            case 7995470: dimType = "logical z"; break;
            case 7602254: dimType = "logical t"; break;
            case 7077966: dimType = "logical lambda"; break;
            case 7471182: dimType = "logical rotation"; break;
            case 5767246: dimType = "logical x-wide"; break;
            case 5832782: dimType = "logical y-wide"; break;
            case 5898318: dimType = "logical z-wide"; break;
          }

          metadata.put("Dim" + j + " type", dimType);
          pt += 4;
          metadata.put("Dim" + j + " size", new Integer(
            TiffTools.bytesToInt(temp, pt, 4, littleEndian)));
          pt += 4;
          metadata.put("Dim" + j + " distance between sub-dimensions",
            new Integer(TiffTools.bytesToInt(temp, pt, 4, littleEndian)));
          pt += 4;

          int len = TiffTools.bytesToInt(temp, pt, 4, littleEndian);
          pt += 4;
          metadata.put("Dim" + j + " physical length",
            stripString(new String(temp, pt, len)));
          pt += len;

          len = TiffTools.bytesToInt(temp, pt, 4, littleEndian);
          pt += 4;
          metadata.put("Dim" + j + " physical origin",
            stripString(new String(temp, pt, len)));
          pt += len;

          len = TiffTools.bytesToInt(temp, pt, 4, littleEndian);
          pt += 4;
          metadata.put("Dim" + j + " name",
            stripString(new String(temp, pt, len)));
          pt += len;

          len = TiffTools.bytesToInt(temp, pt, 4, littleEndian);
          pt += 4;
          metadata.put("Dim" + j + " description",
            stripString(new String(temp, pt, len)));
        }
      }

      temp = (byte[]) headerIFDs[i].get(new Integer(30));
      if (temp != null) {
        // filter data
        // ID_FILTERSET

        // not currently used
      }

      temp = (byte[]) headerIFDs[i].get(new Integer(40));
      if (temp != null) {
        // time data
        // ID_TIMEINFO

        try {
          metadata.put("Number of time-stamped dimensions",
            new Integer(TiffTools.bytesToInt(temp, 0, 4, littleEndian)));
          int nDims = TiffTools.bytesToInt(temp, 4, 4, littleEndian);
          metadata.put("Time-stamped dimension", new Integer(nDims));

          int pt = 8;

          for (int j=0; j < nDims; j++) {
            metadata.put("Dimension " + j + " ID",
              new Integer(TiffTools.bytesToInt(temp, pt, 4, littleEndian)));
            pt += 4;
            metadata.put("Dimension " + j + " size",
              new Integer(TiffTools.bytesToInt(temp, pt, 4, littleEndian)));
            pt += 4;
            metadata.put("Dimension " + j + " distance between dimensions",
              new Integer(TiffTools.bytesToInt(temp, pt, 4, littleEndian)));
            pt += 4;
          }

          int numStamps = TiffTools.bytesToInt(temp, pt, 4, littleEndian);
          pt += 4;
          metadata.put("Number of time-stamps", new Integer(numStamps));
          for (int j=0; j<numStamps; j++) {
            metadata.put("Timestamp " + j,
              stripString(new String(temp, pt, 64)));
            pt += 64;
          }

          int numTMs = TiffTools.bytesToInt(temp, pt, 4, littleEndian);
          pt += 4;
          metadata.put("Number of time-markers", new Integer(numTMs));
          for (int j=0; j<numTMs; j++) {
            int numDims = TiffTools.bytesToInt(temp, pt, 4, littleEndian);
            pt += 4;

            for (int k=0; k<numDims; k++) {
              metadata.put("Time-marker " + j + " Dimension " + k +
                " coordinate", new Integer(TiffTools.bytesToInt(temp,
                pt, 4, littleEndian)));
              pt += 4;
            }
            metadata.put("Time-marker " + j,
              stripString(new String(temp, pt, 64)));
            pt += 64;
          }
        }
        catch (Throwable t) { }
      }

      temp = (byte[]) headerIFDs[i].get(new Integer(50));
      if (temp != null) {
        // scanner data
        // ID_SCANNERSET

        // not currently used
      }

      temp = (byte[]) headerIFDs[i].get(new Integer(60));
      if (temp != null) {
        // experiment data
        // ID_EXPERIMENT

        int pt = 8;
        int len = TiffTools.bytesToInt(temp, pt, 4, littleEndian);
        pt += 4;

        metadata.put("Image Description",
          stripString(new String(temp, pt, 2*len)));
        pt += 2*len;
        len = TiffTools.bytesToInt(temp, pt, 4, littleEndian);
        pt += 4;

        metadata.put("Main file extension",
          stripString(new String(temp, pt, 2*len)));
        pt += 2*len;

        len = TiffTools.bytesToInt(temp, pt, 4, littleEndian);
        pt += 4;
        metadata.put("Single image format identifier",
          stripString(new String(temp, pt, 2*len)));
        pt += 2*len;

        len = TiffTools.bytesToInt(temp, pt, 4, littleEndian);
        pt += 4;
        metadata.put("Single image extension",
          stripString(new String(temp, pt, 2*len)));
      }

      temp = (byte[]) headerIFDs[i].get(new Integer(70));
      if (temp != null) {
        // LUT data
        // ID_LUTDESC

        int pt = 0;
        int numChannels = TiffTools.bytesToInt(temp, pt, 4, littleEndian);
        pt += 4;
        metadata.put("Number of LUT channels", new Integer(numChannels));
        metadata.put("ID of colored dimension",
          new Integer(TiffTools.bytesToInt(temp, pt, 4, littleEndian)));
        pt += 4;

        for (int j=0; j<numChannels; j++) {
          metadata.put("LUT Channel " + j + " version",
            new Integer(TiffTools.bytesToInt(temp, pt, 4, littleEndian)));
          pt += 4;

          int invert = TiffTools.bytesToInt(temp, pt, 1, littleEndian);
          pt += 1;
          boolean inverted = invert == 1;
          metadata.put("LUT Channel " + j + " inverted?",
            new Boolean(inverted).toString());

          int length = TiffTools.bytesToInt(temp, pt, 4, littleEndian);
          pt += 4;
          metadata.put("LUT Channel " + j + " description",
            stripString(new String(temp, pt, length)));

          pt += length;
          length = TiffTools.bytesToInt(temp, pt, 4, littleEndian);
          pt += 4;
          metadata.put("LUT Channel " + j + " filename",
            stripString(new String(temp, pt, length)));
          pt += length;
          length = TiffTools.bytesToInt(temp, pt, 4, littleEndian);
          pt += 4;
          metadata.put("LUT Channel " + j + " name",
            stripString(new String(temp, pt, length)));
          pt += length;

          pt += 8;
        }
      }
    }

    if (ome != null) {
      OMETools.setAttribute(ome, "Pixels", "SizeX",
        metadata.get("Image width").toString());
      OMETools.setAttribute(ome, "Pixels", "SizeY",
        metadata.get("Image height").toString());
      OMETools.setAttribute(ome, "Pixels", "BigEndian",
        littleEndian ? "false" : "true");
      OMETools.setAttribute(ome, "Group", "Name", "OME");
      OMETools.setAttribute(ome, "Image", "CreationDate",
        metadata.get("Timestamp 1").toString());
      OMETools.setAttribute(ome, "Image", "Description",
        metadata.get("Image Description").toString());

      String voxel = metadata.get("VoxelType").toString();
      String photoInterp;
      if (voxel.equals("gray normal")) photoInterp = "monochrome";
      else if (voxel.equals("RGB")) photoInterp = "RGB";
      else photoInterp = "monochrome";

      OMETools.setAttribute(ome, "ChannelInfo",
        "PhotometricInterpretation", photoInterp);

      OMETools.setAttribute(ome, "ChannelInfo", "SamplesPerPixel",
        metadata.get("Samples per pixel").toString());

      OMETools.setAttribute(ome, "Image", "DimensionOrder", "XYZCT");
    }
  }

  // -- Utility methods --

  /**
   * The Leica format is stupid and stores each String as a character array
   * of twice the required size, with a space at every other position.
   * This method removes the blank spaces from a string.
   */
  protected String stripString(String toStrip) {
    char[] toRtn = new char[toStrip.length()];
    for (int i=0; i<toRtn.length; i+=2) toRtn[i/2] = toStrip.charAt(i);
    toStrip = new String(toRtn);
    toStrip = toStrip.trim();
    return toStrip;
  }

  // -- Main method --

  /**
   * Run 'java visad.data.bio.LeicaForm in_file' to test read
   * a Leica file.
   */
  public static void main(String[] args)
    throws VisADException, IOException, RemoteException
  {
    BaseTiffForm.testRead(new LeicaForm(), "Leica", args);
  }

}
