//
// IPWForm.java
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
import java.util.Iterator;
import java.util.StringTokenizer;
import visad.*;
import visad.data.*;
import visad.data.tiff.*;
import visad.util.ReflectedUniverse;

/**
 * IPWForm is the VisAD data adapter for Image-Pro workspace (IPW) files.
 *
 * @author Melissa Linkert linkert at cs.wisc.edu
 */
public class IPWForm extends BaseTiffForm implements FormBlockReader,
  FormFileInformer, FormProgressInformer, MetadataReader, OMEReader
{

  // -- Static fields --

  private static int formCount = 0;
  private static final boolean DEBUG = false;
  private static boolean noPOI = false;
  private static final String NO_POI_MSG =
    "You need to install Jakarta POI from http://jakarta.apache.org/poi/";

  private static ReflectedUniverse r = createReflectedUniverse();

  private static ReflectedUniverse createReflectedUniverse() {
    r = null;
    try {
      r = new ReflectedUniverse();
      r.exec("import org.apache.poi.poifs.filesystem.POIFSFileSystem");
      r.exec("import org.apache.poi.poifs.filesystem.DirectoryEntry");
      r.exec("import org.apache.poi.poifs.filesystem.DocumentEntry");
      r.exec("import org.apache.poi.poifs.filesystem.DocumentInputStream");
      r.exec("import java.util.Iterator");
    }
    catch (Throwable exc) { noPOI = true; }
    return r;
  }


  // -- Fields --

  private Hashtable pixelData = new Hashtable();
  private byte[] header; // general image header data
  private byte[] tags; // tags data
  private Hashtable allIFDs;
  private RandomAccessArray ra;

  private int totalBytes = 0;


  // -- Constructor --

  /** Constructs a new IPWForm file form. */
  public IPWForm() {
    super("IPWForm" + formCount++);
  }


  // -- FormNode API methods --

  /**
   * Opens an existing Image-Pro IPW file from the given filename.
   *
   * @return VisAD Data object containing Image-Pro IPW data
   */
  public DataImpl open(String id)
    throws BadFormException, IOException, VisADException
  {
    if (!id.equals(currentId)) initFile(id);
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
   * Saves a VisAD Data object to Image-Pro IPW format at the given location.
   *
   * @exception UnimplementedException Always throws (this method not
   * implemented).
   */
  public void save(String id, Data data, boolean replace)
    throws BadFormException, IOException, RemoteException,
           VisADException
  {
    throw new UnimplementedException("IPWForm.save");
  }

  /**
   * Adds data to an existing Image-Pro IPW file.
   *
   * @exception BadFormException Always thrown (this method not
   * implemented).
   */
  public void add(String id, Data data, boolean replace)
    throws BadFormException
  {
    throw new BadFormException("IPWForm.add");
  }

  /**
   * Opens an existing Image-Pro IPW file from the given URL.
   *
   * @return VisAD data object containing Image-Pro IPW data
   * @exception UnimplementedException Always thrown (this method not
   * implemented).
   */
  public DataImpl open(URL url)
    throws BadFormException, IOException, VisADException
  {
    throw new UnimplementedException("IPWForm.open(URL)");
  }

  /** Returns the data forms that are compatible with a data object. */
  public FormNode getForms(Data data) {
    return null;
  }


  // -- FormBlockReader API methods --

  /** Obtains the specified image from the given Image-Pro IPW file. */
  public DataImpl open(String id, int block_number)
    throws BadFormException, IOException, VisADException
  {
    if (!id.equals(currentId)) initFile(id);
    byte[] pixels = (byte[]) pixelData.get(new Integer(block_number));
    ifds = (Hashtable[]) allIFDs.get(new Integer(block_number));
    ra.setStream(pixels);

    return TiffTools.getImage(ifds[0], ra);
  }

  /** Determines the number of images in the given Image-Pro IPW file. */
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

  /**
   * Checks if the given string is a valid filename
   * for an Image-Pro IPW file.
   */
  public boolean isThisType(String name) {
    return name.toLowerCase().endsWith(".ipw");
  }

  /** Checks if the given block is a valid header for a Image-Pro IPW file. */
  public boolean isThisType(byte[] block) {
    // all of our samples begin with d0cf11e0
    return (block[0] == 0xd0 && block[1] == 0xcf &&
      block[2] == 0x11 && block[3] == 0xe0);
  }

  /** Returns the default file suffixes for Image-Pro IPW file format. */
  public String[] getDefaultSuffixes() {
    return new String[] {"ipw"};
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
    if (id != currentId) initFile(id);
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
    if (id != currentId) initFile(id);
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
    if (id != currentId) initFile(id);
    if (ome == null) {
      throw new BadFormException(
        "This functionality requires the LOCI OME-XML " +
        "package available at http://www.loci.wisc.edu/ome/");
    }
    return ome;
  }


  // -- Internal BaseTiffForm API methods --

  /** Initializes the given Image-Pro IPW file. */
  protected void initFile(String id)
    throws BadFormException, IOException, VisADException
  {
    if (noPOI) throw new BadFormException(NO_POI_MSG);
    close();
    currentId = id;
    in = new RandomAccessFile(id, "r");
    metadata = new Hashtable();
    allIFDs = new Hashtable();
    numImages = 0;
    ra = new RandomAccessArray(id, "r");

    try {
      r.setVar("fis", new FileInputStream(id));
      r.exec("fs = new POIFSFileSystem(fis)");
      r.exec("dir = fs.getRoot()");
      parseDir(0, r.getVar("dir"));
      for(int i=0; i<pixelData.size(); i++) {
        Integer key = new Integer(i);
        ra.setStream((byte[]) pixelData.get(key));
        allIFDs.put(key, TiffTools.getIFDs(ra));
      }
      initMetadata(id);
    }
    catch (Throwable t) {
      noPOI = true;
      if (DEBUG) t.printStackTrace();
    }
  }

  /** Initialize metadata hashtable and OME-XML structure. */
  public void initMetadata(String id)
    throws BadFormException, IOException, VisADException
  {
    // parse the image description
    String description = new String(tags, 22, tags.length-22);
    metadata.put("Image Description", description);

    // default values
    metadata.put("slices", "1");
    metadata.put("channels", "1");
    metadata.put("frames", new Integer(getBlockCount(id)));

    // parse the description to get channels/slices/times where applicable
    // basically the same as in ImageProSeqForm
    if (description != null) {
      StringTokenizer tokenizer = new StringTokenizer(description, "\n");
      while (tokenizer.hasMoreTokens()) {
        String token = tokenizer.nextToken();
        String label = "Timestamp";
        String data;
        if (token.indexOf("=") != -1) {
          label = token.substring(0, token.indexOf("="));
          data = token.substring(token.indexOf("=")+1);
        }
        else {
          data = token.trim();
        }
        metadata.put(label, data);
      }
    }

    metadata.put("Version", new String(header).trim());

    ifds = (Hashtable[]) allIFDs.get(new Integer(0));
    super.initStandardMetadata();

    ome = OMETools.createRoot();

    if (ome != null) {
      super.initOMEMetadata();
      OMETools.setAttribute(ome, "Pixels", "SizeZ", "" +
        metadata.get("slices"));
      OMETools.setAttribute(ome, "Pixels", "SizeC", "" +
        metadata.get("channels"));
      OMETools.setAttribute(ome, "Pixels", "SizeT", "" +
        metadata.get("frames"));
      OMETools.setAttribute(ome, "Image", "Description", "" +
        metadata.get("Version"));
    }
  }


  // -- Helper methods --

  protected void parseDir(int depth, Object dir)
    throws IOException, BadFormException, VisADException
  {
    r.setVar("dir", dir);
    r.exec("dirName = dir.getName()");
    if (DEBUG) print(depth, r.getVar("dirName") + " {");
    r.setVar("depth", depth);
    r.exec("iter = dir.getEntries()");
    Iterator iter = (Iterator) r.getVar("iter");
    while (iter.hasNext()) {
      r.setVar("entry", iter.next());
      r.exec("isInstance = entry.isDirectoryEntry()");
      r.exec("isDocument = entry.isDocumentEntry()");
      boolean isInstance = ((Boolean) r.getVar("isInstance")).booleanValue();
      boolean isDocument = ((Boolean) r.getVar("isDocument")).booleanValue();
      r.setVar("dir", dir);
      r.exec("dirName = dir.getName()");
      if (isInstance)  {
        parseDir(depth + 1, r.getVar("entry"));
      }
      else if (isDocument) {
        r.exec("entryName = entry.getName()");
        if (DEBUG) {
          print(depth + 1, "Found document: " + r.getVar("entryName"));
        }
        r.setVar("doc", r.getVar("entry"));
        r.exec("dis = new DocumentInputStream(doc)");
        r.exec("numBytes = dis.available()");
        int numbytes = ((Integer) r.getVar("numBytes")).intValue();
        byte[] data = new byte[numbytes];
        r.setVar("data", data);
        r.exec("dis.read(data)");

        String entryName = (String) r.getVar("entryName");
        String dirName = (String) r.getVar("dirName");

        boolean isContents = entryName.equals("CONTENTS");
        totalBytes += data.length + entryName.length();

        if (isContents) {
          // software version
          header = data;
        }
        else if (entryName.equals("FrameRate")) {
          // should always be exactly 4 bytes
          // only exists if the file has more than one image
          metadata.put("Frame Rate",
            new Long(TiffTools.bytesToInt(data, true)));
        }
        else if (entryName.equals("FrameInfo")) {
          // should always be 16 bytes (if present)
          for(int i=0; i<data.length/2; i++) {
            metadata.put("FrameInfo "+i, new Short(
            TiffTools.bytesToShort(data, i*2, true)));
          }
        }
        else if (entryName.equals("ImageInfo")) {
          // acquisition data
          tags = data;
        }
        else if (entryName.equals("ImageResponse")) {
          // skip this entry
        }
        else if (entryName.equals("ImageTIFF")) {
          // pixel data
          String name;
          if (!dirName.equals("Root Entry")) {
            name = dirName.substring(11, dirName.length());
          }
          else name = "0";

          Integer imageNum = Integer.valueOf(name);
          pixelData.put(imageNum, (Object) data);
          numImages++;
        }
        r.exec("dis.close()");
        if (DEBUG) {
          print(depth + 1, ((byte[])
            r.getVar("data")).length + " bytes read.");
        }
      }
    }
  }

  /** Debugging utility method */
  public static final void print(int depth, String s) {
    StringBuffer sb = new StringBuffer();
    for (int i=0; i<depth; i++) sb.append("  ");
    sb.append(s);
    System.out.println(sb.toString());
  }


  // -- Main method --

  /**
   * Run 'java visad.data.bio.ImageProForm in_file' to test read
   * a ImagePro IPW file.
   */
  public static void main(String[] args)
    throws VisADException, IOException, RemoteException
  {
    BaseTiffForm.testRead(new IPWForm(), "IPW", args);
  }

}
