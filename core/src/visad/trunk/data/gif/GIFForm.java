//
// GIFForm.java
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

package visad.data.gif;

import java.io.IOException;

import java.net.URL;

import java.rmi.RemoteException;

import visad.Data;
import visad.DataImpl;
import visad.FlatField;
import visad.UnimplementedException;
import visad.VisADException;

import visad.data.Form;
import visad.data.FormNode;
import visad.data.BadFormException;

public class GIFForm
	extends Form
{
  public GIFForm()
  {
    super("GIFForm");
  }

  public void save(String id, Data data, boolean replace)
	throws  BadFormException, IOException, RemoteException, VisADException
  {
    throw new UnimplementedException("Can't yet save GIF objects");
  }

  public void add(String id, Data data, boolean replace)
	throws BadFormException
  {
    throw new RuntimeException("Can't yet add GIF objects");
  }

  public DataImpl open(String path)
	throws BadFormException, RemoteException, VisADException
  {
    try {
      return new GIFAdapter(path).getData();
    } catch (IOException e) {
      throw new VisADException("IOException: " + e.getMessage());
    }
  }

  public DataImpl open(URL url)
	throws BadFormException, VisADException, IOException
  {
    try {
      return new GIFAdapter(url).getData();
    } catch (IOException e) {
      throw new VisADException("IOException: " + e.getMessage());
    }
  }

  public FormNode getForms(Data data)
  {
    throw new RuntimeException("Can't yet get GIF forms");
  }
}
