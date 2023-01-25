//
// DemFamily.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2023 Bill Hibbard, Curtis Rueden, Tom
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

package visad.data.gis;

import java.io.IOException;

import java.rmi.RemoteException;

import visad.Data;

import visad.DataImpl;

import visad.*;

import visad.data.*;

import java.net.URL;

/**
 * A container for all the supported DEM types.  Currently, USGS
 * DEM and Arc ASCIIGRID formats are supported.
 * To read a <tt>Data</tt> object from a file or URL:<br>
 * <pre>
 *    Data data = new DemFamily("dems").open(string);
 * </pre>
 * @see  visad.data.gis.UsgsDemForm
 * @see  visad.data.gis.ArcAsciiGridForm
 * @author Don Murray
 * @version $Revision: 1.6 $ $Date: 2009-03-02 23:35:48 $
 */
public class DemFamily extends FunctionFormFamily implements FormFileInformer{

  /**
   * List of all supported VisAD datatype Forms.
   * @serial
   */
  private static FormNode[] list            = new FormNode[10];
  private static boolean    listInitialized = false;
  private static MathType dataType = null;

  /**
   * Build a list of all known file adapter Forms
   */
  private static void buildList() {

    int i = 0;

    try {
      list[i] = new UsgsDemForm();

      i++;
    } catch (Throwable t) {}
    try {
      list[i] = new ArcAsciiGridForm(dataType);

      i++;
    } catch (Throwable t) {}

    // throw an Exception if too many Forms for list
    FormNode junk = list[i];

    while (i < list.length) {
      list[i++] = null;
    }

    listInitialized = true;    // WLH 24 Jan 2000
  }

  /**
   * Add to the family of the supported map datatype Forms
   * @param  form   FormNode to add to the list
   *
   * @exception ArrayIndexOutOfBoundsException
   *                   If there is no more room in the list.
   */
  public static void addFormToList(FormNode form)
          throws ArrayIndexOutOfBoundsException {

    synchronized (list) {
      if (!listInitialized) {
        buildList();
      }

      int i = 0;

      while (i < list.length) {
        if (list[i] == null) {
          list[i] = form;
  
          return;
        }

        i++;
      }
    }

    throw new ArrayIndexOutOfBoundsException("Only " + list.length
                                                 + " entries allowed");
  }

  /**
   * Determines if this is a DEM file from the name
   * @param  name  name of the file
   * @return  true if it matches the pattern for USGS DEM files
   */
  public boolean isThisType(String name) {
    return false;
  }

  /**
   * Determines if this is a USGS DEM file from the starting block
   * @param  block  block of data to check
   * @return  false  - there is no identifying block in a USGS DEM file
   */
  public boolean isThisType(byte[] block) {
    return false;
  }

  /**
   * Get a list of default suffixes for McIDAS map files
   * @return  valid list of suffixes
   */
  public String[] getDefaultSuffixes() {
    String[] suff = { ".dem", ".asc" };
    return suff;
  }


  /**
   * Construct a family of the supported map datatype Forms
   * @param  name   name of the family
   */
  public DemFamily(String name) {
    this(name, null);
  }

  /**
   * Construct a family of the supported map datatype Forms
   * @param  name   name of the family
   */
  public DemFamily(String name, MathType dataFormat) {

    super(name);
    if (dataFormat != null) dataType = dataFormat;

    synchronized (list) {
      if (!listInitialized) {
        buildList();
      }
    }

    for (int i = 0; (i < list.length) && (list[i] != null); i++) {
      forms.addElement(list[i]);
    }
  }

  /**
   * Open a local data object using the first appropriate map form.
   * @param  id   String representing the path of the map file
   * @throws  BadFormException  - no form is appropriate
   * @throws  VisADException  - VisAD error
   */
  public DataImpl open(String id) throws BadFormException, VisADException {
    return super.open(id);
  }

  /**
   * Open a remote data object using the first appropriate map form.
   * @param  url   URL representing the location of the map file
   * @throws  BadFormException  - no form is appropriate
   * @throws  VisADException  - VisAD error
   * @throws  IOException  - file not found
   */
  public DataImpl open(URL url)
          throws BadFormException, VisADException, IOException {
    return super.open(url);
  }

  /**
   * Test the DemFamily class
   * run java visad.data.gis.DemFamily  dem1 dem2 ... demn
   */
  public static void main(String[] args)
          throws BadFormException, IOException, RemoteException,
                 VisADException {

    if (args.length < 1) {
      System.err.println("Usage: DemFamily infile [infile ...]");
      System.exit(1);

      return;
    }

    DemFamily fr = new DemFamily("DEM data");

    for (int i = 0; i < args.length; i++) {
      Data data;

      System.out.println("Trying file " + args[i]);

      data = fr.open(args[i]);

      System.out.println(args[i] + ": " + data.getType().prettyString());
    }
  }
}
