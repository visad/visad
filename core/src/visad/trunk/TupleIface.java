//
// TupleIface.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2000 Bill Hibbard, Curtis Rueden, Tom
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

import java.rmi.*;
import java.util.Vector;

/**
   TupleIface is the VisAD data interface for vectors.<P>
*/
public interface TupleIface extends Data {

  public Real[] getRealComponents() throws VisADException, RemoteException;

  /** return number of components */
  public int getDimension();

  /** return component for i between 0 and getDimension() - 1 */
  public Data getComponent(int i) throws VisADException, RemoteException;

  public boolean isMissing();

  public Data binary(Data data, int op, MathType new_type,
                     int sampling_mode, int error_mode)
         throws VisADException, RemoteException;

  public Data unary(int op, MathType new_type,
                    int sampling_mode, int error_mode)
         throws VisADException, RemoteException;

  public DataShadow computeRanges(ShadowType type, DataShadow shadow)
         throws VisADException, RemoteException;

  /** return a Tuple that clones this, except its ErrorEstimate-s
      are adjusted for sampling errors in error */
  public Data adjustSamplingError(Data error, int error_mode)
         throws VisADException, RemoteException;

  public Object clone();

  public String longString(String pre)
         throws VisADException, RemoteException;

  /**
   * Indicates if this Tuple is identical to another object.
   * @param obj		The other object.
   * @return		<code>true</code> if and only if the other object is
   *			a Tuple and both Tuple-s have identical component
   *			sequences.
   */
  public boolean equals(Object obj);

  /**
   * Returns the hash code of this object.
   * @return		The hash code of this object.
   */
  public int hashCode();

}

