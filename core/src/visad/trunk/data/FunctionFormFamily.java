/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2001 Bill Hibbard, Curtis Rueden, Tom
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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import java.net.URL;
import java.net.MalformedURLException;

import java.rmi.RemoteException;

import java.util.Enumeration;

import visad.Data;
import visad.DataImpl;
import visad.VisADException;

public class FunctionFormFamily
	extends FormFamily
{
  public FunctionFormFamily(String name)
  {
    super(name);
  }

  /**
    * Base class which tries to perform an operation on an object
    * using the first valid Form.
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
      * using the first valid Form.
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
          // WLH 19 Feb 2000 - switch order of try and check
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
      if (is != null) {
        is.read(block);
        is.close();

        // see if we can guess the file type based on first block of data
        for (Enumeration enum = forms.elements(); enum.hasMoreElements(); ) {
          FormNode node = (FormNode)enum.nextElement();

          if (node instanceof FormFileInformer) {
            // WLH 19 Feb 2000 - switch order of try and check
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
          }
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
    * using the first valid Form.
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
    * Save a Data object to a local file
    * using the first valid Form.
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

    InputStream getStream()
	throws IOException
    {
      FileInputStream stream;
      try {
        stream = new FileInputStream(name);
      } catch (FileNotFoundException fnfe) {
        stream = null;
      }

      return stream;
    }
  }

  /**
    * Add a Data object to an existing local file
    * using the first valid Form.
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
    * Read a Data object from a local file
    * using the first valid Form.
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
    * using the first valid Form.
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
    * Read a Data object from a remote file
    * using the first valid Form.
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
    * Save a Data object using the first appropriate Form.
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
   * Convert a 'file:' URL into a file path
   */
  private static String trimFileURL(String str)
  {
    // reject strings which couldn't possibly be file URLs
    if (str == null || str.length() < 6 ||
        !str.substring(0, 6).equalsIgnoreCase("file:/"))
    {
      return str;
    }

    // if it's the short form, just strip off the 'file:' part
    if (str.length() < 7 || str.charAt(6) != '/') {
      str = str.substring(5);
    } else {
      // strip off the 'file://' bit
      str = str.substring(7);

      // if the host field isn't empty (i.e. 'file:///')
      if (str.length() > 0 && str.charAt(0) != '/') {
        int slash = str.indexOf('/');

        // if there's no end-of-host marker...
        if (slash < 0) {
          // bad URL; restore 'file://'
          str = "file://" + str;
        } else {
          // break into host part and path part
          String host = str.substring(0, slash);
          String path = str.substring(slash);

          // if its empty or 'localhost', just use the path
          if (host.length() == 0 || host.equalsIgnoreCase("localhost")) {
            str = path;
          } else {
            // remote host; give up
            str = "file://" + host + path;
          }
        }
      }
    }

    // hack for NT/Windows
    if (str.length() >= 2 && str.charAt(2) == ':') {
      str = str.substring(1);
    }

    return str;
  }

  /**
    * Open a local data object using the first appropriate Form.
    */
  public synchronized DataImpl open(String id)
	throws BadFormException, VisADException
  {
    // Garbage in, garbage out
    if (id == null) {
      return null;
    }

    // treat 'file:/' URLs as plain files
    if (id.length() >= 6 && id.substring(0, 6).equalsIgnoreCase("file:/")) {
      id = trimFileURL(id);
    }

    OpenStringForm o = new OpenStringForm(id);

    DataImpl data;
    IOException strEx;

    try {
      if (!o.run()) {
        data = null;
      } else {
        data = o.getData();
      }
      strEx = null;
    } catch (IOException ioe) {
      data = null;
      strEx = ioe;
    }

    if (data == null) {
      URL url;
      try {
        url = new URL(id);
      } catch (MalformedURLException mue) {
        url = null;
      }

      if (url != null) {
        OpenURLForm u = new OpenURLForm(url);

        try {
          if (!u.run()) {
            data = null;
          } else {
            data = u.getData();
          }
        } catch (Exception e) {
          data = null;
        }
      }
    }

    if (data == null) {
      if (!new java.io.File(id).exists()) {
        throw new BadFormException("No such data object \"" + id + "\"");
      }
      throw new BadFormException("Data object \"" + id +
                                 "\" not compatible with \"" + getName() +
                                 "\" data family");
    }

    return data;
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
}
