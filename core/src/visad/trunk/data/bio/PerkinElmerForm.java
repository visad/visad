//
// PerkinElmerForm.java
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
import java.lang.reflect.Method;
import java.rmi.RemoteException;
import java.net.*;
import java.util.Hashtable;
import java.util.StringTokenizer;
import visad.*;
import visad.data.*;
import visad.data.tiff.BaseTiffForm;
import visad.data.tiff.TiffForm;

/**
 * PerkinElmerForm is the VisAD data adapter for PerkinElmer files.
 *
 * @author Melissa Linkert linkert at cs.wisc.edu
 */
public class PerkinElmerForm extends Form implements FormBlockReader,
  FormFileInformer, FormProgressInformer, MetadataReader, OMEReader
{

  // -- Static fields --

  private static int formCount = 0;


  // -- Fields --

  /** Current filename. */
  protected String currentId;

  /** Hashtable containing metadata. */
  protected Hashtable metadata;

  /** Percent complete with current operation. */
  protected double percent;

  /** OME root node for OME-XML metadata. */
  protected Object ome;

  /** Number of images. */
  protected int numImages;

  /** Helper form. */
  protected TiffForm tiff;

  /** Tiff files to open. */
  protected String[] files;

  // -- Constructor --

  /** Constructs a new PerkinElmerForm file form. */
  public PerkinElmerForm() {
    super("PerkinElmerForm" + formCount++);
    tiff = new TiffForm();
  }


  // -- FormNode API methods --

  /**
   * Opens an existing PerkinElmer file from the given filename.
   *
   * @return VisAD Data object containing PerkinElmer data
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
   * Saves a VisAD Data object to PerkinElmer format at the given location.
   *
   * @exception UnimplementedException Always throws (this method not
   * implemented).
   */
  public void save(String id, Data data, boolean replace)
    throws BadFormException, IOException, RemoteException,
      VisADException
  {
    throw new UnimplementedException("PerkinElmerForm.save");
  }

  /**
   * Adds data to an existing PerkinElmer file.
   *
   * @exception BadFormException Always thrown (this method not
   * implemented).
   */
  public void add(String id, Data data, boolean replace)
    throws BadFormException
  {
    throw new BadFormException("PerkinElmerForm.add");
  }

  /**
   * Opens an existing PerkinElmer file from the given URL.
   *
   * @return VisAD data object containing PerkinElmer data
   * @exception UnimplementedException Always thrown (this method not
   * implemented).
   */
  public DataImpl open(URL url)
    throws BadFormException, IOException, VisADException
  {
    throw new UnimplementedException("PerkinElmerForm.open(URL)");
  }

  /** Returns the data forms that are compatible with a data object. */
  public FormNode getForms(Data data) {
    return null;
  }

  // -- FormBlockReader API methods --

  /** Obtains the specified image from the given PerkinElmer file. */
  public DataImpl open(String id, int block_number)
    throws BadFormException, IOException, VisADException
  {
    if (!id.equals(currentId)) initFile(id);
    if (block_number < 0 || block_number >= numImages) {
      throw new BadFormException("Invalid image number: " + block_number);
    }
    return tiff.open(files[block_number], 0);
  }

  /** Determines the number of images in the given PerkinElmer file. */
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
    currentId = null;
  }


  // -- FormFileInformer API methods --

  /** Checks if the given string is a valid filename for a PerkinElmer file. */
  public boolean isThisType(String name) {
    String lname = name.toLowerCase();
    return lname.endsWith(".tim") || lname.endsWith(".zpo") ||
      lname.endsWith(".csv") || lname.endsWith(".htm");
  }

  /** Checks if the given block is a valid header for a PerkinElmer file. */
  public boolean isThisType(byte[] block) {
    return false;
  }

  /** Returns the default file suffixes for PerkinElmer file format. */
  public String[] getDefaultSuffixes() {
    return new String[] {"tim", "zpo", "csv", "htm"};
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


  // -- Internal PerkinElmerForm API methods --

  /** Initializes the given PerkinElmer file. */
  protected void initFile(String id)
    throws BadFormException, IOException, VisADException
  {
    close();
    currentId = id;
    metadata = new Hashtable();

    // get the working directory
    File tempFile = new File(id);
    File workingDir = tempFile.getParentFile();
    String workingDirPath = workingDir.getPath() + File.separator;
    String[] ls = workingDir.list();

    // check if we have any of the required header file types

    int timPos = -1;
    int csvPos = -1;
    int zpoPos = -1;
    int htmPos = -1;
    int filesPt = 0;
    files = new String[ls.length];

    String tempFileName = tempFile.getName();
    int dot = tempFileName.lastIndexOf(".");
    String check = dot < 0 ? tempFileName : tempFileName.substring(0, dot);

    // locate appropriate .tim, .csv, .zpo, .htm and .tif files

    for (int i=0; i<ls.length; i++) {
      // make sure that the file has a name similar to the name of the
      // specified file
      if (ls[i].startsWith(check)) {
        if (timPos == -1) {
          if (ls[i].toLowerCase().endsWith(".tim")) timPos = i;
        }
        if (csvPos == -1) {
          if (ls[i].toLowerCase().endsWith(".csv")) csvPos = i;
        }
        if (zpoPos == -1) {
          if (ls[i].toLowerCase().endsWith(".zpo")) zpoPos = i;
        }
        if (htmPos == -1) {
          if (ls[i].toLowerCase().endsWith(".htm")) htmPos = i;
        }

        if (ls[i].toLowerCase().endsWith(".tif") ||
          ls[i].toLowerCase().endsWith(".tiff"))
        {
          files[filesPt] = workingDirPath + ls[i];
          filesPt++;
        }
      }
    }

    String[] tempFiles = files;
    files = new String[filesPt];
    System.arraycopy(tempFiles, 0, files, 0, filesPt);

    numImages = files.length;
    FileReader read;
    char[] data;
    StringTokenizer t;

    // highly questionable metadata parsing

    // at most two files will get parsed; the .tim file (if it exists) and
    // one of .csv, .zpo and .htm (in that order)

    if (timPos != -1) {
      tempFile = new File(workingDir, ls[timPos]);
      read = new FileReader(tempFile);
      data = new char[(int) tempFile.length()];
      read.read(data);
      t = new StringTokenizer(new String(data));
      int tNum = 0;
      // can ignore "Zero x" and "Extra int"
      String[] hashKeys = {"Number of Wavelengths/Timepoints", "Zero 1",
        "Zero 2", "Number of slices", "Extra int", "Calibration Unit",
        "Pixel Size Y", "Pixel Size X", "Image Width", "Image Length",
        "Origin X", "SubfileType X", "Dimension Label X", "Origin Y",
        "SubfileType Y", "Dimension Label Y", "Origin Z",
        "SubfileType Z", "Dimension Label Z"};

      // there are 9 additional tokens, but I don't know what they're for

      while (t.hasMoreTokens() && tNum<hashKeys.length) {
        metadata.put(hashKeys[tNum], t.nextToken());
        tNum++;
      }
    }

    if (csvPos != -1) {
      tempFile = new File(workingDir, ls[csvPos]);
      read = new FileReader(tempFile);
      data = new char[(int) tempFile.length()];
      read.read(data);
      t = new StringTokenizer(new String(data));
      int tNum = 0;
      String[] hashKeys = {"Calibration Unit", "Pixel Size X", "Pixel Size Y",
        "Z slice space"};
      int pt = 0;
      while (t.hasMoreTokens()) {
        if (tNum < 7) { String temp = t.nextToken(); }
        else if ((tNum > 7 && tNum < 12) || (tNum > 12 && tNum < 18) ||
          (tNum > 18 && tNum < 22)) {
          String temp = t.nextToken();
        }
        else if (pt < hashKeys.length) {
          metadata.put(hashKeys[pt], t.nextToken());
          pt++;
        }
        else {
          metadata.put(t.nextToken() + t.nextToken(),
                    t.nextToken());
        }
        tNum++;
      }
    }
    else if (zpoPos != -1) {
      tempFile = new File(workingDir, ls[zpoPos]);
      read = new FileReader(tempFile);
      data = new char[(int) tempFile.length()];
      read.read(data);
      t = new StringTokenizer(new String(data));
      int tNum = 0;
      while (t.hasMoreTokens()) {
        metadata.put("Z slice #" + tNum + " position", t.nextToken());
        tNum++;
      }
    }
    else if (htmPos != -1) {
      // ooh, pretty HTML

      tempFile = new File(workingDir, ls[htmPos]);
      read = new FileReader(tempFile);
      data = new char[(int) tempFile.length()];
      read.read(data);

      // parsing is fairly primitive, since we only use this file if nothing
      // else is available

      String regex = "<p>|</p>|<br>|<hr>|<b>|</b>|<HTML>|<HEAD>|</HTML>|" +
        "</HEAD>|<h1>|</h1>|<HR>|</body>";
      //String[] tokens = (new String(data)).split(regex);

      // use reflection to avoid dependency on Java 1.4-specific split method
      Class c = String.class;
      String[] tokens = new String[0];
      try {
        Method split = c.getMethod("split", new Class[] {c});
        tokens = (String[]) split.invoke(new String(data),
          new Object[] {regex});
      }
      catch (Throwable e) { }

      for (int j=0; j<tokens.length; j++) {
        if (tokens[j].indexOf("<") != -1) tokens[j] = "";
      }

      int slice = 0;
      for (int j=0; j<tokens.length-1; j+=2) {
        if (tokens[j].indexOf("Wavelength") != -1) {
          metadata.put("Camera Data " + tokens[j].charAt(13), tokens[j]);
          j--;
        }
        else if (!tokens[j].trim().equals("")) {
          metadata.put(tokens[j], tokens[j+1]);
        }
      }
    }
    else {
      throw new BadFormException("Valid header files not found.");
    }

    // initialize OME-XML

    ome = OMETools.createRoot();

    if (ome != null) {
      OMETools.setAttribute(ome, "Image", "PixelSizeX", "" +
        metadata.get("Pixel Size X"));
      OMETools.setAttribute(ome, "Image", "PixelSizeY", "" +
        metadata.get("Pixel Size Y"));
      OMETools.setAttribute(ome, "Image", "CreationDate", "" +
        metadata.get("Finish Time:"));
      OMETools.setAttribute(ome, "Pixels", "SizeX", "" +
        metadata.get("Image Width"));
      OMETools.setAttribute(ome, "Pixels", "SizeY", "" +
        metadata.get("Image Length"));
      OMETools.setAttribute(ome, "StageLabel", "X", "" +
        metadata.get("Origin X"));
      OMETools.setAttribute(ome, "StageLabel", "Y", "" +
        metadata.get("Origin Y"));
      OMETools.setAttribute(ome, "StageLabel", "Z", "" +
        metadata.get("Origin Z"));
    }
  }


  // -- Main method --

  /**
   * Run 'java visad.data.bio.PerkinElmerForm in_file' to test read
   * a PerkinElmer file.
   */
  public static void main(String[] args)
    throws VisADException, IOException, RemoteException
  {
    BaseTiffForm.testRead(new PerkinElmerForm(), "PerkinElmer", args);
  }

}
