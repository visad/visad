//
// RealTupleIface.java
//

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

package visad;

import java.rmi.RemoteException;

/**
 * Interface to the VisAD data class for vectors in R^n for n>0.
 */
public interface RealTupleIface
  extends TupleIface
{
  /**
   * Returns the values of the components.
   *
   * @return			The values of the components.
   */
  double[] getValues();

  /**
   * Returns the units of the components.
   *
   * @return			The units of the components.
   */
  Unit[] getTupleUnits();

  /**
   * Returns the uncertainties of the components.
   *
   * @return			The uncertainties of the components.
   * @throws VisADException	VisAD failure.
   * @throws RemoteException	Java RMI failure.
   */
  ErrorEstimate[] getErrors()
    throws VisADException, RemoteException;

  /**
   * Returns the coordinate system transformation.
   *
   * @return			The coordinate system transformation.  May be
   *				<code>null</code>.
   */
  CoordinateSystem getCoordinateSystem();

  /**
   * Clones this instance.
   *
   * @return			A clone of this instance.
   */
  Object clone();

  /**
   * Returns a string representation of this instance.
   *
   * @return			A string representation of this instance.
   */
  String toString();
}
