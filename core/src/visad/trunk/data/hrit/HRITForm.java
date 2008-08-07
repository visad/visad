//
// HRITForm.java
//

/*
The software in this file is Copyright(C) 2008 by Tommy Jasmin.
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

package visad.data.hrit;

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

/** 
 * to allow determination of whether a file is of type HRIT
 *
 */
public class HRITForm extends Form implements FormFileInformer {

  private HRITAdapter ha;

  public HRITForm() {
    super("HRITForm");
  }

  /** 
   * determine the file type by name. At present we are only
   * checking for MSG2 HRIT files.  This will change.
   *
   * @param name is the filename in question
   */
  public boolean isThisType(String name) {
	  File file = new File(name);
	  if (file.exists()) name = file.getName();
	  return (name.contains("MSG2"));
  }

  /** 
   * This method will be used to identify an HRIT file by
   * examining the contents of the first block of data values.
   * Presently unimplemented.
   *
   * @param block[] is an array of ? length from the beginning
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
    throw new UnimplementedException("Can't yet save HRIT objects");
  }

  /** This has not been implemented
  *
  */
  public synchronized void add(String id, Data data, boolean replace)
	throws BadFormException {

    throw new RuntimeException("Can't yet add HRIT objects");
  }

  /** read the HRIT file from local disk, and return the HRIT data
    * as a DataImpl object (a FlatField).
    *
    * @param path is the fully-qualified pathname
    *
    */
  public synchronized DataImpl open(String path)
	throws BadFormException, RemoteException, VisADException {

    try {
      String [] fileNames = new String[1];
      fileNames[0] = path;
      ha = new HRITAdapter(fileNames);
      return ha.getData();
    } catch (IOException e) {
      throw new VisADException("IOException: " + e.getMessage());
    }
  }

  /** read the HRIT file from a URL, and return the HRIT file
    * as a DataImpl object (a FlatField).
    *
    * @param path is the fully-formed URL
    *
    */
  public synchronized DataImpl open(URL url)
	throws BadFormException, VisADException, IOException {

    // HRITAdapter constructor decides if argument is a file or a URL
	String [] urls = new String[1];
	urls[0] = url.toString();
    ha = new HRITAdapter(urls);
    return ha.getData();
  }

  /** not implemented yet
  *
  */
  public synchronized FormNode getForms(Data data) {

    throw new RuntimeException("Can't yet get HRIT forms");
  }
}
