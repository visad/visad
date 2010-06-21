/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2009 Bill Hibbard, Curtis Rueden, Tom
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

public class FitsTourGuide
	extends TourGuide
{
  private boolean replace;

  public FitsTourGuide(Data data, Tourist tourist)
	throws RemoteException, VisADException
  {
    this.replace = replace;

    show(data, tourist, 0);
  }

  public boolean show(Function func, Tourist tourist, int depth)
	throws RemoteException, VisADException
  {
    return tourist.visit(func, depth);
  }

  public boolean show(Scalar scalar, Tourist tourist, int depth)
	throws VisADException
  {
    return tourist.visit(scalar, depth);
  }

  public boolean show(Set set, Tourist tourist, int depth)
	throws VisADException
  {
    return tourist.visit(set, depth);
  }

  public boolean show(Tuple tuple, Tourist tourist, int depth)
	throws RemoteException, VisADException
  {
    boolean rtnval = true;

    int dim = tuple.getDimension();
    for (int i = 0; i < dim; i++) {
      rtnval |= show(tuple.getComponent(i), tourist, depth+1);
    }

    return rtnval;
  }
}
