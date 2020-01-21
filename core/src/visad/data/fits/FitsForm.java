//
// FitsForm.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2020 Bill Hibbard, Curtis Rueden, Tom
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

package visad.data.fits;

import java.io.IOException;

import java.net.URL;

import java.rmi.RemoteException;

import visad.Data;
import visad.DataImpl;
import visad.Tuple;
import visad.VisADException;

import visad.data.BadFormException;
import visad.data.Form;
import visad.data.FormNode;
import visad.data.FormFileInformer;

public class FitsForm
	extends Form
	implements FormFileInformer
{
  public FitsForm()
  {
    super("FitsForm");
  }

  public boolean isThisType(String name)
  {
    return name.endsWith(".fits");
  }

  public boolean isThisType(byte[] block)
  {
    String front = new String(block, 0, 9);
    if (!front.startsWith("SIMPLE  =")) {
      return false;
    }

    String back = new String(block, 9, 71);
    back = back.trim();
    if (back.length() != 1 || back.charAt(0) != 'T') {
      return false;
    }

    return true;
  }

  public String[] getDefaultSuffixes()
  {
    String[] suff = { "fits" };
    return suff;
  }

  public synchronized void save(String id, Data data, boolean replace)
	throws  BadFormException, IOException, RemoteException, VisADException
  {
    new FitsAdapter().save(id, data, replace);
  }

  public synchronized void add(String id, Data data, boolean replace)
	throws BadFormException
  {
    throw new RuntimeException("Can't yet add FITS objects");
  }

  private DataImpl extractData(FitsAdapter fits)
	throws RemoteException, VisADException
  {
    // save any exceptions
    ExceptionStack eStack = null;

    // convert the FITS object to a VisAD data object
    Data[] data;
    try {
      data = fits.getData();
    } catch (ExceptionStack e) {
      eStack = e;
      fits.clearExceptionStack();
      data = fits.getData();
    }

    // throw away FitsAdapter object so we can reuse that memory
    fits = null;

    // if there's no data, we're done
    if (data == null || data.length == 0) {
      if (eStack != null) {
	throw eStack;
      }
      return null;
    }

    // either grab solo Data object or wrap a Tuple around all the Data objects
    DataImpl di;
    if (data.length == 1) {
      di = (DataImpl )data[0];
    } else {
      di = new Tuple(data);
    }

    // throw away Data array so we can reuse (a small bit of) that memory
    data = null;

    return di;
  }

  public synchronized DataImpl open(String path)
	throws BadFormException, RemoteException, VisADException
  {
    return extractData(new FitsAdapter(path));
  }

  public synchronized DataImpl open(URL url)
	throws BadFormException, VisADException, IOException
  {
    return extractData(new FitsAdapter(url));
  }

  public synchronized FormNode getForms(Data data)
  {
    throw new RuntimeException("Can't yet get FITS forms");
  }
}
