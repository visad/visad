/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2023 Bill Hibbard, Curtis Rueden, Tom
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

import java.rmi.RemoteException;

import visad.Data;
import visad.Function;
import visad.Scalar;
import visad.Set;
import visad.Tuple;
import visad.VisADException;

public abstract class TourGuide
{
  public boolean show(Data data, Tourist tourist, int depth)
	throws RemoteException, VisADException
  {
    if (data instanceof Function) {
      return show((Function )data, tourist,  depth);
    }

    if (data instanceof Scalar) {
      return show((Scalar )data, tourist,  depth);
    }

    if (data instanceof Set) {
      return show((Set )data, tourist,  depth);
    }

    if (data instanceof Tuple) {
      return show((Tuple )data, tourist,  depth);
    }

    throw new VisADException("Unknown datatype " + data.getClass().getName());
  }

  public abstract boolean show(Function func, Tourist tourist, int depth)
	throws RemoteException, VisADException;
  public abstract boolean show(Scalar scalar, Tourist tourist, int depth)
	throws VisADException;
  public abstract boolean show(Set set, Tourist tourist, int depth)
	throws VisADException;
  public abstract boolean show(Tuple tuple, Tourist tourist, int depth)
	throws RemoteException, VisADException;
}
