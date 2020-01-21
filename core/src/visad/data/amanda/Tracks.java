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

package visad.data.amanda;

import java.rmi.RemoteException;

import visad.Data;
import visad.FieldImpl;
import visad.FlatField;
import visad.FunctionType;
import visad.Integer1DSet;
import visad.VisADException;

public class Tracks
  extends EventList
{
  public static FunctionType functionType;

  static {
    try {
      functionType = new FunctionType(BaseTrack.indexType,
                                      BaseTrack.functionType);
    } catch (VisADException ve) {
      ve.printStackTrace();
      functionType = null;
    }
  }

  public Tracks() { }

  public final void add(BaseTrack track) { super.addUnique(track); }

  public final void computeSamples(float[] timeSteps)
  {
    final int num = size();
    for (int i = 0; i < num; i++) {
      get(i).computeSamples(timeSteps);
    }
  }

  public final BaseTrack get(int i)
  {
    return (BaseTrack )super.internalGet(i);
  }

  public final Data makeData()
    throws RemoteException, VisADException
  {
    final int num = size();

    Integer1DSet set = new Integer1DSet(BaseTrack.indexType,
                                        (num == 0 ? 1 : num));
    FieldImpl fld = new FieldImpl(functionType, set);
    if (num > 0) {
      FlatField[] flds = new FlatField[num];
      for (int i = 0; i < num; i++) {
        flds[i] = get(i).makeData();
      }
      try {
        fld.setSamples(flds, true);
      } catch (RemoteException re) {
        re.printStackTrace();
      }
    }

    return fld;
  }
}
