//
// ICSForm.java
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
import java.util.*;
import visad.*;
import visad.data.*;
import visad.data.tiff.BaseTiffForm;
import visad.data.tiff.TiffTools;

/**
 * ICSForm is the VisAD data adapter for ICS (Image Cytometry Standard) files.
 *
 * @author Melissa Linkert linkert at cs.wisc.edu
 */
public class ICSForm extends Form implements FormBlockReader,
  FormFileInformer, FormProgressInformer, MetadataReader, OMEReader
{

  // -- Static fields --

  private static int formCount = 0;


  // -- Fields --

  /** Current filename. */
  protected String currentIcsId;
  protected String currentIdsId;

  /** Current file. */
  protected RandomAccessFile idsIn; // IDS file
  protected File icsIn; // ICS file

  /** Hashtable containing metadata. */
  protected Hashtable metadata;

  /** Percent complete with current operation. */
  protected double percent;

  /** Flag indicating whether current file is little endian. */
  protected boolean littleEndian;

  /** OME root node for OME-XML metadata. */
  protected Object ome;

  /** Number of images. */
  protected int numImages;

  /**
   * Dimensions in the following order:
   * 1) bits per pixel
   * 2) width
   * 3) height
   * 4) z
   * 5) channels
   * 6) timepoints
   */
  protected int[] dimensions = new int[6];


  // -- Constructor --

  /** Constructs a new ICSForm file form. */
  public ICSForm() {
    super("ICSForm" + formCount++);
  }


  // -- FormNode API methods --

  /**
   * Opens an existing ICS file from the given filename.
   *
   * @return VisAD Data object containing ICS data
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
   * Saves a VisAD Data object to ICS format at the given location.
   *
   * @exception UnimplementedException Always thrown (this method not
   * implemented).
   */
  public void save(String id, Data data, boolean replace)
    throws BadFormException, IOException, RemoteException,
           VisADException
  {
    throw new UnimplementedException("ICSForm.save");
  }

  /**
   * Adds data to an existing ICS file.
   *
   * @exception BadFormException Always thrown (this method not
   * implemented).
   */
  public void add(String id, Data data, boolean replace)
    throws BadFormException
  {
    throw new BadFormException("ICSForm.add");
  }

  /**
   * Opens an existing ICS file from the given URL.
   *
   * @return VisAD data object containing ICS data
   * @exception UnimplementedException Always thrown (this method not
   * implemented).
   */
  public DataImpl open(URL url)
    throws BadFormException, IOException, VisADException
  {
    throw new UnimplementedException("ICSForm.open(URL)");
  }

  /** Returns the data forms that are compatible with a data object. */
  public FormNode getForms(Data data) {
    return null;
  }


  // -- FormBlockReader API methods --

  /** Obtains the specified image from the given ICS file. */
  public DataImpl open(String id, int block_number)
    throws BadFormException, IOException, VisADException
  {
    if (!id.equals(currentIdsId) && !id.equals(currentIcsId)) initFile(id);

    int width = dimensions[1];
    int height = dimensions[2];

    idsIn.seek((dimensions[0]/8) * width * height * block_number);
    byte[] data = new byte[(dimensions[0]/8) * width * height];
    idsIn.readFully(data);
    int numSamples = width * height;
    int channels = 1;

    float[][] samples = new float[channels][numSamples];
    if (dimensions[0] == 8) {
      for (int i=0; i<numSamples; i++) {
        int q = (int) TiffTools.bytesToShort(data, i, 1, littleEndian);
        samples[0][i] = (float) q;
      }
    }
    else if (dimensions[0] == 16) {
      int pt = 0;
      for (int i=0; i<data.length; i+=2) {
        int q = ((0x000000ff & data[i+1]) << 8) | (0x000000ff & data[i]);
        samples[0][pt] = (float) q;
        pt++;
      }
    }
    else if (dimensions[0] == 32) {
      int pt = 0;
      for (int i=0; i<data.length; i+=4) {
        int q = ((0x000000ff & data[i+3]) << 24) |
          ((0x000000ff & data[i+2]) << 16) |
          ((0x000000ff & data[i+1]) << 8) | (0x000000ff & data[i]);
        samples[0][pt] = (float) q;
        pt++;
      }
    }

    // construct the field

    RealType x = RealType.getRealType("ImageElement");
    RealType y = RealType.getRealType("ImageLine");

    RealType[] v = new RealType[channels];
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

  /** Determines the number of images in the given ICS file. */
  public int getBlockCount(String id)
    throws BadFormException, IOException, VisADException
  {
    if (!id.equals(currentIdsId) && !id.equals(currentIcsId)) initFile(id);
    return numImages;
  }

  /** Closes any open files. */
  public void close() throws BadFormException, IOException, VisADException {
    if (idsIn != null) idsIn.close();
    idsIn = null;
    icsIn = null;
    currentIcsId = null;
    currentIdsId = null;
  }


  // -- FormFileInformer API methods --

  /** Checks if the given string is a valid filename for an ICS file. */
  public boolean isThisType(String name) {
    name = name.toLowerCase();
    return name.endsWith(".ics") || name.endsWith(".ids");
  }

  /** Checks if the given block is a valid header for an ICS file. */
  public boolean isThisType(byte[] block) {
    return false;
  }

  /** Returns the default file suffixes for ICS file format. */
  public String[] getDefaultSuffixes() {
    return new String[] {"ics", "ids"};
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
    if (!id.equals(currentIdsId) && !id.equals(currentIcsId)) initFile(id);
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
    if (!id.equals(currentIdsId) && !id.equals(currentIcsId)) initFile(id);
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
    if (!id.equals(currentIdsId) && !id.equals(currentIcsId)) initFile(id);
    if (ome == null) {
      throw new BadFormException(
        "This functionality requires the LOCI OME-XML " +
        "package available at http://www.loci.wisc.edu/ome/");
    }
    return ome;
  }


  // -- Internal BaseTiffForm API methods --

  /** Initializes the given ICS file. */
  protected void initFile(String id)
    throws BadFormException, IOException, VisADException
  {
    close();

    File tempFile = new File(id);
    String absPath = tempFile.getAbsolutePath();
    File workingDir = new File(absPath.substring(0, absPath.lastIndexOf("/")));
    String workingDirPath = workingDir.getPath() + File.separator;
    String[] ls = workingDir.list();

    String tempFileName = tempFile.getName();
    int dot = tempFileName.lastIndexOf(".");
    String check = dot < 0 ? tempFileName : tempFileName.substring(0, dot);

    if (id.toLowerCase().endsWith("ics")) {
      currentIcsId = id;
      icsIn = new File(id);
      for (int i=0; i<ls.length; i++) {
        if (ls[i].startsWith(check) && ls[i].toLowerCase().endsWith("ids")) {
          currentIdsId = ls[i];
          idsIn = new RandomAccessFile(currentIdsId, "r");
        }
      }
      if (idsIn.length() == 0) {
        throw new BadFormException("Sorry, .ids file not found.");
      }
    }
    else {
      currentIdsId = id;
      idsIn = new RandomAccessFile(id, "r");
      for (int i=0; i<ls.length; i++) {
        if (ls[i].startsWith(check) && ls[i].toLowerCase().endsWith("ics")) {
          currentIcsId = ls[i];
          icsIn = new File(currentIcsId);
        }
      }
      if (currentIcsId.length() == 0) {
        throw new BadFormException("Sorry, .ics file not found.");
      }
    }

    metadata = new Hashtable();

    BufferedReader reader = new BufferedReader(new FileReader(icsIn));
    String line = reader.readLine();
    line = reader.readLine();
    StringTokenizer t;
    while (line != null) {
      t = new StringTokenizer(line);
      while (t.hasMoreTokens()) {
        String token = t.nextToken();
        if (!token.equals("layout") && !token.equals("representation") &&
          !token.equals("parameter") && !token.equals("history") &&
          !token.equals("sensor"))
        {
          if (t.countTokens() < 3) {
            try {
              metadata.put(token, t.nextToken());
            }
            catch (NoSuchElementException e) { }
          }
          else {
            String meta = t.nextToken();
            while (t.hasMoreTokens()) {
              meta = meta + " " + t.nextToken();
            }
            metadata.put(token, meta);
          }
        }
      }
      line = reader.readLine();
    }

    String images = (String) metadata.get("sizes");
    String order = (String) metadata.get("order");
    // bpp, width, height, z, channels
    StringTokenizer t1 = new StringTokenizer(images);
    StringTokenizer t2 = new StringTokenizer(order);

    for (int i=0; i<dimensions.length; i++) {
      dimensions[i] = 1;
    }

    while (t1.hasMoreTokens() && t2.hasMoreTokens()) {
      String imageToken = t1.nextToken();
      String orderToken = t2.nextToken();
      if (orderToken.equals("bits")) {
        dimensions[0] = Integer.parseInt(imageToken);
      }
      else if (orderToken.equals("x")) {
        dimensions[1] = Integer.parseInt(imageToken);
      }
      else if (orderToken.equals("y")) {
        dimensions[2] = Integer.parseInt(imageToken);
      }
      else if (orderToken.equals("z")) {
        dimensions[3] = Integer.parseInt(imageToken);
      }
      else if (orderToken.equals("ch")) {
        dimensions[4] = Integer.parseInt(imageToken);
      }
      else {
        dimensions[5] = Integer.parseInt(imageToken);
      }
    }
    numImages = dimensions[3] * dimensions[4] * dimensions[5];

    String endian = (String) metadata.get("byte_order");
    littleEndian = true;

    if (endian != null) {
      StringTokenizer endianness = new StringTokenizer(endian);
      int firstByte = 0;
      int lastByte = 0;

      for (int i=0; i<endianness.countTokens(); i++) {
        if (i == 0) { firstByte = Integer.parseInt(endianness.nextToken()); }
        else { lastByte = Integer.parseInt(endianness.nextToken()); }
      }
      if (lastByte < firstByte) { littleEndian = false; }
    }

    initOMEMetadata();
  }

  public void initOMEMetadata() {
    ome = OMETools.createRoot();

    if (ome != null) {
      OMETools.setAttribute(ome, "Pixels", "SizeX", "" + dimensions[1]);
      OMETools.setAttribute(ome, "Pixels", "SizeY", "" + dimensions[2]);
      OMETools.setAttribute(ome, "Pixels", "SizeZ", "" + dimensions[3]);
      OMETools.setAttribute(ome, "Pixels", "SizeC", "" + dimensions[4]);
      OMETools.setAttribute(ome, "Pixels", "SizeT", "" + dimensions[5]);
      OMETools.setAttribute(ome, "Pixels", "BigEndian", "" + !littleEndian);
      OMETools.setAttribute(ome, "Image",
        "Name", "" + metadata.get("filename"));

      String order = (String) metadata.get("order");
      order = order.substring(order.indexOf("x"));
      char[] tempOrder = new char[(order.length() / 2) + 1];
      int pt = 0;
      for (int i=0; i<order.length(); i+=2) {
        tempOrder[pt] = order.charAt(i);
        pt++;
      }
      order = new String(tempOrder);
      order = order.toUpperCase();

      if (order.indexOf("Z") == -1) { order = order + "Z"; }
      if (order.indexOf("T") == -1) { order = order + "T"; }
      if (order.indexOf("C") == -1) { order = order + "C"; }
      OMETools.setAttribute(ome, "Pixels", "DimensionOrder", order);

      String bits = (String) metadata.get("significant_bits");
      String format = (String) metadata.get("format");
      String sign = (String) metadata.get("sign");

      String type;
      if (sign.equals("unsigned")) { type = "U"; }
      else { type = ""; }

      if (format.equals("real")) { type = "float"; }
      else if (format.equals("integer")) {
        type = type + "int" + bits;
      }
      OMETools.setAttribute(ome, "Pixels", "PixelType", type);
    }
  }


  // -- Main method --

  /**
   * Run 'java visad.data.bio.ICSForm in_file' to test read
   * an ICS file.
   */
  public static void main(String[] args)
    throws VisADException, IOException, RemoteException
  {
    BaseTiffForm.testRead(new ICSForm(), "ICS", args);
  }

}
