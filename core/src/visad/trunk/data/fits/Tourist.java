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

import visad.Function;
import visad.Scalar;
import visad.Set;
import visad.VisADException;

public class Tourist
{
  boolean replace;

  public Tourist(boolean replace)
  {
    this.replace = replace;
  }

  public boolean visit(Function func, int depth)
	throws RemoteException, VisADException
  {
//    System.err.println("Tourist.visit: function " + func.getType() + ", depth=" + depth);
    return false;
  }

  public boolean visit(Scalar scalar, int depth)
	throws VisADException
  {
//    System.err.println("Tourist.visit: scalar " + scalar.getType() + ", depth=" + depth);
    return false;
  }

  public boolean visit(Set set, int depth)
	throws VisADException
  {
//    System.err.println("Tourist.visit: set " + set.getType() + ", depth=" + depth);
    return false;
  }
}
