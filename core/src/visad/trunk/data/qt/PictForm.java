//
// PictForm.java
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

package visad.data.qt;

import java.io.*;
import java.net.URL;
import java.rmi.RemoteException;
import visad.*;
import visad.data.*;

/**
 * PictForm is the VisAD data form for Apple PICT images.
 * To use it, QuickTime for Java must be installed.
 */
public class PictForm extends Form
  implements FormBlockReader, FormFileInformer
{

  // -- Constants --

  private static final String[] SUFFIXES = { "pict" };


  // -- Static fields --

  private static int num = 0;

  /** Helper form for reading PICT data with QTJava library. */
  private static QTForm qtForm = new QTForm();


  // -- Constructor --

  /** Constructs a new PICT form. */
  public PictForm() {
    super("PictForm" + num++);
  }


  // -- Form API methods --

  /**
   * Saves data to a PICT file on disk.
   *
   * @exception BadFormException Always thrown (method is not implemented).
   */
  public void save(String id, Data data, boolean replace)
    throws BadFormException, IOException, RemoteException, VisADException
  {
    throw new BadFormException("PictForm.save");
  }

  /**
   * Adds data to an existing PICT file.
   *
   * @exception BadFormException Always thrown (method is not implemented).
   */
  public void add(String id, Data data, boolean replace)
    throws BadFormException
  {
    throw new BadFormException("PictForm.add");
  }

  /**
   * Opens an existing PICT image from the given location.
   *
   * @return VisAD Data object containing PICT data.
   */
  public DataImpl open(String id)
    throws BadFormException, IOException, VisADException
  {
    // read in PICT data
    File file = new File(id);
    int len = (int) file.length() - 512;
    byte[] bytes = new byte[len];
    FileInputStream fin = new FileInputStream(file);
    fin.skip(512); // skip 512 byte PICT header
    int read = 0;
    int left = len;
    while (left > 0) {
      int r = fin.read(bytes, read, left);
      read += r;
      left -= r;
    }
    fin.close();
    return qtForm.pictToField(bytes);
  }

  /**
   * Opens an existing PICT image from the given URL.
   *
   * @exception BadFormException Always thrown (method is not implemented).
   */
  public DataImpl open(URL url)
    throws BadFormException, IOException, VisADException
  {
    throw new BadFormException("PictForm.open(URL)");
  }

  public FormNode getForms(Data data) {
    return null;
  }


  // -- FormBlockReader API methods --

  /**
   * Obtains the specified block from the given file.
   * @param id The file from which to load data blocks.
   * @param block_number The block number of the block to load.
   * @throws VisADException If the block number is invalid.
   */
  public DataImpl open(String id, int block_number)
    throws BadFormException, IOException, VisADException
  {
    if (block_number != 0) {
      throw new BadFormException("Invalid image number: " + block_number);
    }
    return open(id);
  }

  /**
   * Determines the number of blocks in the given file.
   * @param id The file for which to get a block count.
   */
  public int getBlockCount(String id)
    throws BadFormException, IOException, VisADException
  {
    return 1;
  }

  /** Closes any open files. */
  public void close() throws BadFormException, IOException, VisADException { }


  // -- FormFileInformer methods --

  /** Checks if the given string is a valid filename for a PICT image. */
  public boolean isThisType(String name) {
    for (int i=0; i<SUFFIXES.length; i++) {
      if (name.toLowerCase().endsWith(SUFFIXES[i])) return true;
    }
    return false;
  }

  /** Checks if the given block is a valid header for a PICT image. */
  public boolean isThisType(byte[] block) {
    return false;
  }

  /** Returns the default file suffixes for the PICT image formats. */
  public String[] getDefaultSuffixes() {
    String[] s = new String[SUFFIXES.length];
    System.arraycopy(SUFFIXES, 0, s, 0, SUFFIXES.length);
    return s;
  }


  // -- Main method --

  /** Run 'java visad.data.qt.PictForm in_file' to test the PICT reader. */
  public static void main(String[] args)
    throws VisADException, RemoteException, IOException
  {
    if (args == null || args.length != 1) {
      System.out.println("To test read a PICT image, run:");
      System.out.println("  java visad.data.qt.PictForm in_file");
      System.exit(2);
    }

    // Test read PICT image
    PictForm form = new PictForm();
    System.out.print("Reading " + args[0] + " ");
    Data data = form.open(args[0]);
    System.out.println("[done]");
    System.out.println("MathType =\n" + data.getType().prettyString());
  }

}
