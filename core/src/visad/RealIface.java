//
// RealIface.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2015 Bill Hibbard, Curtis Rueden, Tom
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
 *  Interface to scalar data for real numbers represented
 *  as double precision floating point values.  Double.NaN is used to
 *  indicate missing values, because it has the appropriate arithmetic
 *  semantics.
 */
public interface RealIface
  extends ScalarIface
{
  /**
   * Returns the numeric value in the unit of {@link #getUnit()}.
   *
   * @return		The numeric value in the unit of {@link #getUnit()}.
   */
  double getValue();

  /**
   * Returns the numeric value in a particular unit.
   *
   * @param unit_out	The desired unit for the numeric value.  Must be
   *				convertible with {@link #getUnit()}.
   * @return		The numeric value in the given unit.
   */
  double getValue(Unit unit_out)
    throws VisADException;

  /**
   * Returns the unit of this instance.
   *
   * @return			The unit of this instance.
   */
  Unit getUnit();

  /**
   * Returns the uncertainty in the numeric value of this instance.
   *
   * @return			The uncertainty in the numeric value of this
   *				instance.
   */
  ErrorEstimate getError();

  /**
   * Returns a clone, except that the ErrorEstimate of the clone
   * is adjusted for a given error mode and uncertainty.
   *
   * @param error		The uncertainty by which to adjust the clone.
   * @param error_mode		The mode for propagating errors.  See {@link
   *				Data}.
   * @return			A clone of this instance with a modified
   *				uncertainty.
   * @throws VisADException	VisAD failure.
   * @throws RemoteException	Java RMI failure.
   */
  Data adjustSamplingError(Data error, int error_mode)
    throws VisADException, RemoteException;

  /**
   * Returns a clone of this instance with a different numeric value.  The unit
   * is unchanged.
   *
   * @param value		The numeric value for the clone.
   * @return			A clone of this nstance with the given numeric
   *				value.
   * @throws VisADException	VisAD failure.
   */
  Real cloneButValue(double value)
    throws VisADException;

  /**
   * Returns a clone of this instance but with a new Unit.  The numeric value is
   * unchanged.
   *
   * @param u			The unit for the clone.
   * @return			A clone of this instance but with the given
   *				unit.
   */
  Real cloneButUnit(Unit u)
    throws VisADException;

  /*
   * Returns a string representation of this instance.
   *
   * @return			A string representation of this instance.
   */
  String toString();

  /**
   * Returns a string that represents just the value portion of this Real -- but
   * with full semantics (e.g. numeric value and unit).
   *
   * @return			A string representation of just the value
   *				portion of this Real.
   */
  String toValueString();

  /**
   * Compares this Real to another.
   *
   * @param object		The other Real to compare against.  It shall be
   *				a Real with a compatible (i.e. convertible)
   *				unit.
   * @return                    A negative integer, zero, or a positive integer
   *                            depending on whether this Real is considered
   *                            less than, equal to, or greater than the other
   *                            Real, respectively.  If the values of the Real-s
   *                            in the default unit are equal, then the <code>
   *                            ErrorEstimate.compareTo()</code> method is used
   *                            to break the tie.
   */
  int compareTo(Object object);

  /**
   * Returns the hash code of this Real.
   *
   * @return			The hash code of this Real.  If two Real-s are
   *				semantically identical, then their hash codes
   *				are equal.
   */
  int hashCode();
}
