//
// F2000Form.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2018 Bill Hibbard, Curtis Rueden, Tom
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

package visad.data.amanda;

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

/**
   F2000Form is the VisAD data format adapter for
   F2000 files for Amanda events.<P>
*/
public class F2000Form
  extends Form
  implements FormFileInformer
{
  private static int num = 0;

  private AmandaFile file = null;

  public F2000Form()
  {
    super("F2000Form#" + num++);
  }

  public synchronized void add(String id, Data data, boolean replace)
    throws BadFormException
  {
    throw new BadFormException("F2000Form.add");
  }

  public String[] getDefaultSuffixes()
  {
    String[] suff = { "r" };
    return suff;
  }

  public synchronized FormNode getForms(Data data)
  {
    return null;
  }

  public final double getXMax() { return file.getXMax(); }
  public final double getXMin() { return file.getXMin(); }

  public final double getYMax() { return file.getYMax(); }
  public final double getYMin() { return file.getYMin(); }

  public final double getZMax() { return file.getZMax(); }
  public final double getZMin() { return file.getZMin(); }

  public boolean isThisType(String name)
  {
    return name.endsWith(".r");
  }

  public boolean isThisType(byte[] block)
  {
    return false;
  }

  private Tuple makeTuple(AmandaFile file)
    throws VisADException
  {
    Tuple t;
    try {
      t = new Tuple(new Data[] {file.makeEventData(), file.makeModuleData()});
    } catch (RemoteException re) {
      re.printStackTrace();
      t = null;
    }

    return t;
  }

  public synchronized DataImpl open(String id)
    throws BadFormException, IOException, VisADException
  {
    file = new AmandaFile(id);
    return makeTuple(file);
  }

  public synchronized DataImpl open(URL url)
    throws BadFormException, VisADException, IOException
  {
    file = new AmandaFile(url);
    return makeTuple(file);
  }

  public synchronized void save(String id, Data data, boolean replace)
    throws BadFormException, IOException, RemoteException, VisADException
  {
    throw new BadFormException("F2000Form.save");
  }
}
