//
// GatanForm.java
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
 * GatanForm is the VisAD data adapter for Gatan files.
 *
 * @author Melissa Linkert linkert at cs.wisc.edu
 */
public class GatanForm extends Form implements FormBlockReader,
  FormFileInformer, FormProgressInformer, MetadataReader, OMEReader
{

  // -- Static fields --

  private static int formCount = 0;
  private static final byte[] GATAN_MAGIC_BLOCK_1 = {0, 0, 0, 3};


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

  /** Array of pixel bytes. */
  protected byte[] pixelData;

  /** Dimensions -- width, height, bytes per pixel */
  protected int[] dims = new int[3];

  protected int pixelDataNum = 0;


  // -- Constructor --

  /** Constructs a new GatanForm file form. */
  public GatanForm() {
    super("GatanForm" + formCount++);
  }


  // -- FormNode API methods --

  /**
   * Opens an existing Gatan file from the given filename.
   *
   * @return VisAD Data object containing Gatan data
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
      FieldImpl indexField = new FieldImpl(indexFunction, indexSet);
      indexField.setSamples(fields, false);
      data = indexField;
    }
    close();
    percent = Double.NaN;
    return data;
  }

  /**
   * Saves a VisAD Data object to Gatan format at the given location.
   *
   * @exception UnimplementedException Always thrown (this method not
   * implemented).
   */
  public void save(String id, Data data, boolean replace)
    throws BadFormException, IOException, RemoteException,
           VisADException
  {
    throw new UnimplementedException("GatanForm.save");
  }

  /**
   * Adds data to an existing Gatan file.
   *
   * @exception BadFormException Always thrown (this method not
   * implemented).
   */
  public void add(String id, Data data, boolean replace)
    throws BadFormException
  {
    throw new BadFormException("GatanForm.add");
  }

  /**
   * Opens an existing Gatan file from the given URL.
   *
   * @return VisAD data object containing Gatan data
   * @exception UnimplementedException Always thrown (this method not
   * implemented).
   */
  public DataImpl open(URL url)
    throws BadFormException, IOException, VisADException
  {
    throw new UnimplementedException("GatanForm.open(URL)");
  }

  /** Returns the data forms that are compatible with a data object. */
  public FormNode getForms(Data data) {
    return null;
  }


  // -- FormBlockReader API methods --

  /** Obtains the specified image from the given Gatan file. */
  public DataImpl open(String id, int block_number)
    throws BadFormException, IOException, VisADException
  {
    if (!id.equals(currentId)) initFile(id);

    int width = dims[0];
    int height = dims[1];
    int channels = 1;

    int numSamples = width*height;
    float[][] samples = new float[channels][numSamples];

    // only supporting 8 and 16 bit data for now

    if (dims[2] == 1) {
      for (int i=0; i<pixelData.length; i++) {
        int q = (int) TiffTools.bytesToShort(pixelData, i, 1, littleEndian);
        samples[0][i] = (float) q;
      }
    }
    else if (dims[2] == 2) {
      for (int i=0; i<pixelData.length; i+=2) {
        int q = TiffTools.bytesToShort(pixelData, i, littleEndian);
        samples[0][i/2] = (float) q;
      }
    }
    else {
      throw new BadFormException("Sorry, " + dims[2] +
        " bytes per pixel is unsupported");
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

  /** Determines the number of images in the given Gatan file. */
  public int getBlockCount(String id)
    throws BadFormException, IOException, VisADException
  {
    if (!id.equals(currentId)) initFile(id);
    // every Gatan file has only one regular image
    return 1;
  }

  /** Closes any open files. */
  public void close() throws BadFormException, IOException, VisADException {
    if (in != null) in.close();
    in = null;
    currentId = null;
  }


  // -- FormFileInformer API methods --

  /** Checks if the given string is a valid filename for a Gatan file. */
  public boolean isThisType(String name) {
    return name.toLowerCase().endsWith(".dm3");
  }

  /** Checks if the given block is a valid header for a Gatan file. */
  public boolean isThisType(byte[] block) {
    if (block == null) return false;
    if (block.length != GATAN_MAGIC_BLOCK_1.length) return false;
    for (int i=0; i<block.length; i++) {
      if (block[i] != GATAN_MAGIC_BLOCK_1[i]) return false;
    }
    return true;
  }

  /** Returns the default file suffixes for Gatan file format. */
  public String[] getDefaultSuffixes() {
    return new String[] {"dm3"};
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

  /** Initializes the given Gatan file. */
  protected void initFile(String id)
    throws BadFormException, IOException, VisADException
  {
    close();
    currentId = id;
    in = new RandomAccessFile(id, "r");
    metadata = new Hashtable();
    littleEndian = false;

    byte[] temp = new byte[4];
    in.read(temp);
    // only support version 3
    if (temp[0] != GATAN_MAGIC_BLOCK_1[0] &&
      temp[1] != GATAN_MAGIC_BLOCK_1[1] &&
      temp[2] != GATAN_MAGIC_BLOCK_1[2] &&
      temp[3] != GATAN_MAGIC_BLOCK_1[3])
    {
      throw new BadFormException("invalid header");
    }

    in.read(temp);
    int numBytes = TiffTools.bytesToInt(temp, littleEndian);
    in.read(temp);
    if (TiffTools.bytesToInt(temp, littleEndian) == 1) littleEndian = true;

    // TagGroup instance

    in.skipBytes(2);
    in.read(temp);
    parseTags(TiffTools.bytesToInt(temp, !littleEndian), "initFile");
    initOMEMetadata();
  }

  // Information on the DM3 structure found at:
  // http://rsb.info.nih.gov/ij/plugins/DM3Format.gj.html and
  // http://www-hrem.msm.cam.ac.uk/~cbb/info/dmformat/

  public void parseTags(int numTags, String parent) throws IOException {
    byte[] temp = new byte[4];
    for (int i=0; i<numTags; i++) {
      byte type = in.readByte(); // can be 21 (data) or 20 (tag group)
      byte[] twobytes = new byte[2];
      in.read(twobytes);
      int length = TiffTools.bytesToInt(twobytes, !littleEndian);
      byte[] label = new byte[length];
      in.read(label);
      String labelString = new String(label);

      // image data is in tag with type 21 and label 'Data'
      // image dimensions are in type 20 tag with 2 type 15 tags
      // bytes/pixel is in type 21 tag with label 'PixelDepth'

      if (type == 21) {
        in.skipBytes(4); // equal to '%%%%'
        in.read(temp);
        int n = TiffTools.bytesToInt(temp, !littleEndian);
        int dataType = 0;
        if (n == 1) {
          in.read(temp);
          dataType = TiffTools.bytesToInt(temp, !littleEndian);
          int data;
          switch (dataType) {
            case 2: data = TiffTools.read2SignedBytes(in, littleEndian); break;
            case 3: data = TiffTools.read4SignedBytes(in, littleEndian); break;
            case 4:
              data = TiffTools.read2UnsignedBytes(in, littleEndian);
              break;
            case 5:
              data = (int) TiffTools.read4UnsignedBytes(in, littleEndian);
              break;
            case 6: data = (int) TiffTools.readFloat(in, littleEndian); break;
            case 7:
              data = (int) TiffTools.readFloat(in, littleEndian);
              in.skipBytes(4);
              break;
            case 8: data = TiffTools.readSignedByte(in); break;
            case 9: data = TiffTools.readSignedByte(in); break;
            case 10: data = TiffTools.readSignedByte(in); break;
            default: data = 0;
          }
          if (parent.equals("Dimensions")) {
            if (i == 0) dims[0] = data;
            else if (i == 1) dims[1] = data;
          }
          if ("PixelDepth".equals(labelString)) dims[2] = data;

          metadata.put(labelString, new Integer(data));
        }
        else if (n == 2) {
          in.read(temp);
          dataType = TiffTools.bytesToInt(temp, littleEndian);
          if (dataType == 18) { // this should always be true
            in.read(temp);
            length = TiffTools.bytesToInt(temp, littleEndian);
          }
          byte[] data = new byte[length];
          in.read(data);
          metadata.put(labelString, new String(data));
        }
        else if (n == 3) {
          in.read(temp);
          dataType = TiffTools.bytesToInt(temp, !littleEndian);
          if (dataType == 20) { // this should always be true
            in.read(temp);
            dataType = TiffTools.bytesToInt(temp, !littleEndian);
            in.read(temp);
            length = TiffTools.bytesToInt(temp, !littleEndian);

            if ("Data".equals(labelString)) pixelDataNum++;

            if ("Data".equals(labelString) && pixelDataNum == 2) {
              // we're given the number of pixels,
              // but the tag containing bytes per
              // pixel doesn't occur until after the
              // image data
              //
              // this is a messy way to read pixel
              // data, which uses the fact that the
              // first byte after the pixel data is
              // either 20 or 21

              byte check = 0;
              double bpp = 0.5;
              int fp = (int) in.getFilePointer();
              while (check != 20 && check != 21) {
                bpp *= 2;
                in.seek(fp);
                pixelData = new byte[(int) bpp * length];
                in.read(pixelData);
                check = in.readByte();
              }
              in.seek((long) (fp + bpp * length));
            }
            else {
              int[] data = new int[length];

              for (int j=0; j<length; j++) {
                if (dataType == 2 || dataType == 4) {
                  byte[] two = new byte[2];
                  in.read(two);
                  data[j] = (int) TiffTools.bytesToShort(two, !littleEndian);
                }
                else if (dataType == 7) in.skipBytes(8);
                else if (dataType == 8 || dataType == 9) in.skipBytes(1);
                else {
                  in.read(temp);
                  data[j] = TiffTools.bytesToInt(temp, !littleEndian);
                }
              }
            }
          }
        }
        else {
          in.read(temp);
          dataType = TiffTools.bytesToInt(temp, !littleEndian);
          // this is a normal struct of simple types
          if (dataType == 15) {
            int skip = 0;
            in.read(temp);
            skip += TiffTools.bytesToInt(temp, !littleEndian);
            in.read(temp);
            int numFields = TiffTools.bytesToInt(temp, !littleEndian);
            for (int j=0; j<numFields; j++) {
              in.read(temp);
              skip += TiffTools.bytesToInt(temp, !littleEndian);
              in.read(temp);
              dataType = TiffTools.bytesToInt(temp, !littleEndian);
              switch (dataType) {
                case 2: skip += 2; break;
                case 3: skip += 4; break;
                case 4: skip += 2; break;
                case 5: skip += 4; break;
                case 6: skip += 4; break;
                case 7: skip += 8; break;
                case 8: skip += 1; break;
                case 9: skip += 1; break;
              }
            }
            in.skipBytes(skip);
          }
          else if (dataType == 20) {
            // this is an array of structs
            int skip = 0;
            in.read(temp);
            dataType = TiffTools.bytesToInt(temp, !littleEndian);
            if (dataType == 15) { // should always be true
              in.read(temp);
              skip += TiffTools.bytesToInt(temp, !littleEndian);
              in.read(temp);
              int numFields = TiffTools.bytesToInt(temp, !littleEndian);
              for (int j=0; j<numFields; j++) {
                in.read(temp);
                skip += TiffTools.bytesToInt(temp, !littleEndian);
                in.read(temp);
                dataType = TiffTools.bytesToInt(temp, !littleEndian);
                switch (dataType) {
                  case 2: skip += 2; break;
                  case 3: skip += 4; break;
                  case 4: skip += 2; break;
                  case 5: skip += 4; break;
                  case 6: skip += 4; break;
                  case 7: skip += 8; break;
                  case 8: skip += 1; break;
                  case 9: skip += 1; break;
                }
              }
            }
            in.read(temp);
            skip *= TiffTools.bytesToInt(temp, !littleEndian);
            in.skipBytes(skip);
          }
        }
      }
      else if (type == 20) {
        in.skipBytes(2);
        in.read(temp);
        parseTags(TiffTools.bytesToInt(temp, !littleEndian), labelString);
      }
    }
  }

  public void initOMEMetadata() {
    ome = OMETools.createRoot();

    if (ome != null) {
      int datatype = ((Integer) metadata.get("DataType")).intValue();

      String type = "int8";
      switch (datatype) {
        case 1: type = "int16"; break;
        case 2: type = "float"; break;
        case 3: type = "float"; break;
        case 5: type = "float"; break;
        case 6: type = "Uint8"; break;
        case 7: type = "int32"; break;
        case 8: type = "Uint32"; break;
        case 9: type = "int8"; break;
        case 10: type = "Uint16"; break;
        case 11: type = "Uint32"; break;
        case 12: type = "float"; break;
        case 13: type = "float"; break;
        case 14: type = "Uint8"; break;
        case 23: type = "int32"; break;
      }

      OMETools.setAttribute(ome, "Pixels", "PixelType", type);
      OMETools.setAttribute(ome, "Pixels", "BigEndian",
        littleEndian ? "false" : "true");
      OMETools.setAttribute(ome, "Pixels", "SizeX", "" + dims[0]);
      OMETools.setAttribute(ome, "Pixels", "SizeY", "" + dims[1]);
      OMETools.setAttribute(ome, "Pixels", "SizeC", "" + 1);
      OMETools.setAttribute(ome, "Pixels", "SizeZ", "" + 1);
      OMETools.setAttribute(ome, "Pixels", "SizeT", "" + 1);
      OMETools.setAttribute(ome, "Pixels", "DimensionOrder", "XYZTC");
    }
  }


  // -- Main method --

  /**
   * Run 'java visad.data.bio.GatanForm in_file' to test read
   * a Gatan file.
   */
  public static void main(String[] args)
    throws VisADException, IOException, RemoteException
  {
    BaseTiffForm.testRead(new GatanForm(), "Gatan", args);
  }

}
