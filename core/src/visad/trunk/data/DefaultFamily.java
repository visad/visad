/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2000 Bill Hibbard, Curtis Rueden, Tom
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

package visad.data;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import java.net.URL;
import java.net.MalformedURLException;

import java.rmi.RemoteException;

import java.util.Enumeration;

import visad.Data;
import visad.DataImpl;
import visad.VisADException;

import visad.data.fits.FitsForm;

import visad.data.gif.GIFForm;

import visad.data.hdfeos.HdfeosAdaptedForm;

import visad.data.netcdf.Plain;

import visad.data.vis5d.Vis5DForm;

import visad.data.visad.VisADForm;

import visad.data.mcidas.AreaForm;

/**
  * A container for all the officially supported VisAD datatypes.
  */
public class DefaultFamily
	extends FormFamily
{
  /**
    * List of all supported VisAD datatype Forms.
    */
  /*
   *  note that I hardcoded the number of FormNodes (100)
   *  increase this if you add a new FormNode
   */
  private static FormNode[] list = new FormNode[100];
  private static boolean listInitialized = false;

  /**
    * Base class which tries to perform an operation on an object
    * using the first valid VisAD datatype Form.
    */
  abstract class FormFunction
  {
    /**
      * Return 'true' if this object's name applies to the given node.
      */
    abstract boolean check(FormFileInformer node);

    /**
      * Return an InputStream for the object.
      *
      * Used to read in the first block of the object.
      */
    abstract InputStream getStream() throws IOException;

    /**
      * The operation to be performed on the object.
      */
    abstract boolean function(FormNode node);

    /**
      * Perform an operation on an object
      * using the first valid VisAD datatype Form.
      *
      * If a Form successfully performs the operation, return 'true'.
      */
    public boolean run()
	throws IOException
    {
      // see if we can guess the file type based on the name
      for (Enumeration enum = forms.elements(); enum.hasMoreElements(); ) {
	FormNode node = (FormNode)enum.nextElement();

	if (node instanceof FormFileInformer) {
          // WLH 19 Feb 2000 - switch order ot try and check
          // needed for HDF5
	  try {
	    if (check((FormFileInformer) node)) {
	      if (function(node)) {
		return true;
	      }
	    }
	  } catch (Exception e) {
	  } catch (Error e) {
            // WLH 19 Feb 2000 - needed for HDF5
	  }
	}
      }

      // get the first block of data from the file
      byte[] block = new byte[2048];
      InputStream is = getStream();
      is.read(block);
      is.close();

      // see if we can guess the file type based on first block of data
      for (Enumeration enum = forms.elements(); enum.hasMoreElements(); ) {
	FormNode node = (FormNode)enum.nextElement();

	if (node instanceof FormFileInformer) {
          // WLH 19 Feb 2000 - switch order ot try and check
          // needed for HDF5
	  try {
	    if (((FormFileInformer )node).isThisType(block)) {
	      if (function(node)) {
		return true;
	      }
	    }
	  } catch (Exception e) {
	  } catch (Error e) {
            // WLH 19 Feb 2000 - needed for HDF5
	  }
	} else {
	}
      }

      // use the brute-force method of checking all the forms
      for (Enumeration enum = forms.elements(); enum.hasMoreElements(); ) {
	FormNode node = (FormNode)enum.nextElement();

	try {
	  if (function(node)) {
	    return true;
	  }
	} catch (Exception e) {
	} catch (UnsatisfiedLinkError ule) {
	}
      }

      return false;
    }
  }

  /**
    * Perform an operation on a local file object
    * using the first valid VisAD datatype Form.
    */
  abstract class FileFunction
	extends FormFunction
  {
    String name;

    public FileFunction()
    {
      name = null;
    }

    boolean check(FormFileInformer node)
    {
      return node.isThisType(name);
    }

    InputStream getStream()
	throws IOException
    {
      return new FileInputStream(name);
    }
  }

  /**
    * Save a VisAD Data object to a local file
    * using the first valid VisAD datatype Form.
    */
  class SaveForm
	extends FileFunction
  {
    private Data data;
    private boolean replace;

    public SaveForm(String name, Data data, boolean replace)
    {
      this.name = name;
      this.data = data;
      this.replace = replace;
    }

    boolean function(FormNode node)
    {
      try {
	node.save(name, data, replace);
      } catch (Exception e) {
	return false;
      }

      return true;
    }
  }

  /**
    * Add a VisAD Data object to an existing local file
    * using the first valid VisAD datatype Form.
    */
  class AddForm
	extends FileFunction
  {
    private Data data;
    private boolean replace;

    public AddForm(String name, Data data, boolean replace)
    {
      this.name = name;
      this.data = data;
      this.replace = replace;
    }

    boolean function(FormNode node)
    {
      try {
	node.add(name, data, replace);
      } catch (Exception e) {
	return false;
      }

      return true;
    }
  }

  /**
    * Read a VisAD Data object from a local file
    * using the first valid VisAD datatype Form.
    */
  class OpenStringForm
	extends FileFunction
  {
    private DataImpl data;

    public OpenStringForm(String name)
    {
      this.name = name;
      data = null;
    }

    boolean function(FormNode node)
    {
      try {
	data = node.open(name);
      } catch (OutOfMemoryError t) { // WLH 5 Feb 99
	throw t;
      } catch (Throwable t) {
	return false;
      }

      return true;
    }

    public DataImpl getData()
    {
      return data;
    }
  }

  /**
    * Perform an operation on a remote file object
    * using the first valid VisAD datatype Form.
    */
  abstract class URLFunction
	extends FormFunction
  {
    URL url;

    public URLFunction()
    {
      url = null;
    }

    boolean check(FormFileInformer node)
    {
      return node.isThisType(url.getFile());
    }

    InputStream getStream()
	throws IOException
    {
      return url.openStream();
    }
  }

  /**
    * Read a VisAD Data object from a remote file
    * using the first valid VisAD datatype Form.
    */
  class OpenURLForm
	extends URLFunction
  {
    /* CTR: 13 Oct 1998
    private URL url;
    */
    private DataImpl data;

    public OpenURLForm(URL url)
    {
      this.url = url;
      data = null;
    }

    boolean function(FormNode node)
    {
      try {
	data = node.open(url);
      } catch (Throwable t) {
	return false;
      }

      return true;
    }

    public DataImpl getData()
    {
      return data;
    }
  }

  /**
   * Build a list of all known file adapter Forms
   */
  private static void buildList()
  {
    int i = 0;

    try {
      list[i] = new FitsForm();
      i++;
    } catch (Throwable t) {
    }
    try {
      list[i] = new GIFForm();
      i++;
    } catch (Throwable t) {
    }
    try {
      list[i] = new HdfeosAdaptedForm();
      i++;
    } catch (Throwable t) {
    }
    try {
      list[i] = new Plain();
      i++;
    } catch (Throwable t) {
    }
    try {
      list[i] = new Vis5DForm();
      i++;
    } catch (Throwable t) {
    }
    try {
      list[i] = new VisADForm();
      i++;
    } catch (Throwable t) {
    }
    try {
      list[i] = new AreaForm();
      i++;
    } catch (Throwable t) {
    }
    // added to support HDF5 adapter (visad.data.hdf5.HDF5Form)
    try {
      Object hdf5form = null;
      ClassLoader cl = ClassLoader.getSystemClassLoader();
      Class hdf5form_class = cl.loadClass("visad.data.hdf5.HDF5Form");
      hdf5form = hdf5form_class.newInstance();
      if (hdf5form != null) list[i++] = (Form)hdf5form;
    } catch (Throwable t) {
    }

    // throw an Exception if too many Forms for list
    FormNode junk = list[i];

    while (i < list.length) {
      list[i++] = null;
    }
    listInitialized = true; // WLH 24 Jan 2000
  }

  /**
    * Add to the family of the supported VisAD datatype Forms
    * @exception ArrayIndexOutOfBoundsException
    *			If there is no more room in the list.
    */
  public static void addFormToList(FormNode form)
    throws ArrayIndexOutOfBoundsException
  {
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

    throw new ArrayIndexOutOfBoundsException("Only " + list.length +
                                             " entries allowed");
  }

  /**
    * Construct a family of the supported VisAD datatype Forms
    */
  public DefaultFamily(String name)
  {
    super(name);

    synchronized (list) {
      if (!listInitialized) {
	buildList();
      }
    }

    for (int i = 0; i < list.length && list[i] != null; i++) {
      forms.addElement(list[i]);
    }
  }

  /**
    * Save a VisAD Data object using the first appropriate Form.
    */
  public synchronized void save(String id, Data data, boolean replace)
	throws BadFormException, RemoteException, IOException, VisADException
  {
    SaveForm s = new SaveForm(id, data, replace);
    if (!s.run()) {
      throw new BadFormException("Data object not compatible with \"" +
				 getName() + "\" data family");
    }
  }

  /**
    * Add data to an existing data object using the first appropriate Form.
    */
  public synchronized void add(String id, Data data, boolean replace)
	throws BadFormException
  {
    AddForm a = new AddForm(id, data, replace);
    try {
      if (a.run()) {
	return;
      }
    } catch (IOException e) {
    }

    throw new BadFormException("Data object not compatible with \"" +
			       getName() + "\" data family");
  }

  /**
    * Open a local data object using the first appropriate Form.
    */
  public synchronized DataImpl open(String id)
	throws BadFormException, IOException, VisADException
  {
    OpenStringForm o = new OpenStringForm(id);
    if (!o.run()) {
      throw new BadFormException("Data object \"" + id +
				 "\" not compatible with \"" + getName() +
				 "\" data family");
    }

    return o.getData();
  }

  /**
    * Open a remote data object using the first appropriate Form.
    */
  public synchronized DataImpl open(URL url)
	throws BadFormException, IOException, VisADException
  {
    OpenURLForm o = new OpenURLForm(url);
    if (!o.run()) {
      throw new BadFormException("Data object \"" + url +
				 "\" not compatible with \"" + getName() +
				 "\" data family");
    }

    return o.getData();
  }

  /**
    * Test the DefaultFamily class
    */
  public static void main(String[] args)
	throws BadFormException, IOException, RemoteException, VisADException
  {
    if (args.length < 1) {
      System.err.println("Usage: DefaultFamily infile [infile ...]");
      System.exit(1);
      return;
    }

    DefaultFamily fr = new DefaultFamily("sample");

    for (int i = 0; i < args.length; i++) {
      URL url = null;
      try {
        url = new URL(args[i]);
      }
      catch (MalformedURLException exc) { }
      Data data;
      if (url != null) System.out.println("Trying URL " + url.toString());
      else System.out.println("Trying file " + args[i]);
      if (url == null) data = fr.open(args[i]);
      else data = fr.open(url);
      System.out.println(args[i] + ": " + data.getType().prettyString());
    }
  }
}
