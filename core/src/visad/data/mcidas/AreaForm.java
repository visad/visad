//
// AreaForm.java
//

/*

The software in this file is Copyright(C) 2021 by Tom Whittaker.
It is designed to be used with the VisAD system for interactive
analysis and visualization of numerical data.

This program is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 1, or (at your option)
any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License in file NOTICE for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
*/

package visad.data.mcidas;

import edu.wisc.ssec.mcidas.*;

import java.io.File;
import java.io.IOException;

import java.net.URL;

import java.rmi.RemoteException;

import visad.Data;
import visad.DataImpl;
import visad.UnimplementedException;
import visad.VisADException;

import visad.data.BadFormException;
import visad.data.Form;
import visad.data.FormNode;
import visad.data.FormFileInformer;

/** to allow determination of whether a file is of type McIDAS
  * 'area'.
  *
  */
public class AreaForm extends Form implements FormFileInformer {

  private AreaAdapter aa;

  public AreaForm() {
    super("AreaForm");

    // force AreaFile class to be loaded so 'adde:' handler is initialized
    AreaFile.isURLHandlerLoaded();
  }

  /** determine the file type by name. McIDAS area files begin
    *  with the letters: AREA (or area).
    *
    * @param name is the filename in question
    */
  public boolean isThisType(String name) {
    File file = new File(name);
    if (file.exists()) name = file.getName();
    return name.startsWith("AREA") || 
           name.endsWith("area")   || 
           ( name.startsWith("adde://") && 
           ( name.indexOf("/image?") > 0 ||
             name.indexOf("/imageda") > 0) );
  }

  /** there is no unique way to identify an AREA file by
    * examining the contents of the first block of data values
    *
    * @param block is an array of ? length from the beginning
    * of the file in question.
    *
    */
  public boolean isThisType(byte[] block) {
    return false;
  }

  /** return a list of suffixes associated with this file type
  *
  */
  public String[] getDefaultSuffixes() {
    String[] suff = { " " };
    return suff;
  }

  /** save the file back to disk
  *
  * This has not been implemented yet
  *
  */
  public synchronized void save(String id, Data data, boolean replace)
	throws  BadFormException, IOException, RemoteException, VisADException
  {
    throw new UnimplementedException("Can't yet save McIDAS AREA objects");
  }

  /** This has not been implemented
  *
  */
  public synchronized void add(String id, Data data, boolean replace)
	throws BadFormException {

    throw new RuntimeException("Can't yet add McIDAS AREA objects");
  }

  /** read the area file from local disk, and return the Area file
    * as a DataImpl object (a FlatField).
    *
    * @param path is the fully-qualified pathname
    *
    */
  public synchronized DataImpl open(String path)
	throws BadFormException, RemoteException, VisADException {

    try {
      aa = new AreaAdapter(path);
      return aa.getData();

    } catch (IOException e) {
      throw new VisADException("IOException: " + e.getMessage());
    }
  }

  /** read the area file from a URL,  and return the Area file
    * as a DataImpl object (a FlatField).
    *
    * @param url is the fully-formed URL
    *
    */
  public synchronized DataImpl open(URL url)
	throws BadFormException, VisADException, IOException {

    // AreaAdapter constructor decides if argument is a file or a URL
    aa = new AreaAdapter(url.toString());
    return aa.getData();
  }

  /** not implemented yet
  *
  */
  public synchronized FormNode getForms(Data data) {

    throw new RuntimeException("Can't yet get McIDAS AREA forms");
  }
}
