//
// FitsForm.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 1998 Bill Hibbard, Curtis Rueden, Tom
Rink and Dave Glowacki.

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

package visad.data.fits;

import java.io.IOException;

import java.net.URL;

import java.rmi.RemoteException;

import visad.Data;
import visad.DataImpl;
import visad.Tuple;
import visad.UnimplementedException;
import visad.VisADException;

import visad.data.Form;
import visad.data.FormNode;
import visad.data.BadFormException;

public class FitsForm
	extends Form
{
  public FitsForm()
  {
    super("FitsForm");
  }

  public void save(String id, Data data, boolean replace)
	throws  BadFormException, IOException, RemoteException, VisADException
  {
    throw new UnimplementedException("Can't yet save FITS objects");
  }

  public void add(String id, Data data, boolean replace)
	throws BadFormException
  {
    throw new RuntimeException("Can't yet add FITS objects");
  }

  public DataImpl open(String path)
	throws BadFormException, RemoteException, VisADException
  {
    FitsAdapter fits = new FitsAdapter(path);

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

  public DataImpl open(URL url)
	throws BadFormException, VisADException, IOException
  {
    throw new UnimplementedException("Can't yet open FITS URLs");
  }

  public FormNode getForms(Data data)
  {
    throw new RuntimeException("Can't yet get FITS forms");
  }
}
