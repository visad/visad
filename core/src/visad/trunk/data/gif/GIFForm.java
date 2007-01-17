//
// GIFForm.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2007 Bill Hibbard, Curtis Rueden, Tom
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

package visad.data.gif;

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

public class GIFForm
	extends Form
	implements FormFileInformer
{
  public GIFForm()
  {
    super("GIFForm");
  }

  public boolean isThisType(String name)
  {
    return (name.endsWith(".gif") || name.endsWith(".GIF") ||
            name.endsWith(".jpg") || name.endsWith(".JPG") ||
            name.endsWith(".jpeg") || name.endsWith(".JPEG") ||
            name.endsWith(".png") || name.endsWith(".PNG"));
  }

  public boolean isThisType(byte[] block)
  {
    return false;
  }

  public String[] getDefaultSuffixes()
  {
    String[] suff = { "gif" };
    return suff;
  }

  public synchronized void save(String id, Data data, boolean replace)
	throws  BadFormException, IOException, RemoteException, VisADException
  {
    throw new UnimplementedException("Can't yet save GIF objects");
  }

  public synchronized void add(String id, Data data, boolean replace)
	throws BadFormException
  {
    throw new RuntimeException("Can't yet add GIF objects");
  }

  public synchronized DataImpl open(String path)
	throws BadFormException, RemoteException, VisADException
  {
    try {
      return new GIFAdapter(path).getData();
    } catch (IOException e) {
      throw new VisADException("IOException: " + e.getMessage());
    }
  }

  public synchronized DataImpl open(URL url)
	throws BadFormException, VisADException, IOException
  {
    GIFAdapter ga = new GIFAdapter(url);
    return ga.getData();
  }

  public synchronized FormNode getForms(Data data)
  {
    throw new RuntimeException("Can't yet get GIF forms");
  }
}
