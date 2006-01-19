//
// TupleIface.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2006 Bill Hibbard, Curtis Rueden, Tom
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

/**
   TupleIface is the VisAD data interface for vectors.<P>
*/
public interface TupleIface extends Data {

  Real[] getRealComponents() throws VisADException, RemoteException;

  /** return number of components */
  int getDimension() throws RemoteException;

  /** return component for i between 0 and getDimension() - 1 */
  Data getComponent(int i) throws VisADException, RemoteException;

  boolean isMissing() throws RemoteException;

  Data binary(Data data, int op, MathType new_type,
                     int sampling_mode, int error_mode)
         throws VisADException, RemoteException;

  Data unary(int op, MathType new_type,
                    int sampling_mode, int error_mode)
         throws VisADException, RemoteException;

  DataShadow computeRanges(ShadowType type, DataShadow shadow)
         throws VisADException, RemoteException;

  /** return a Tuple that clones this, except its ErrorEstimate-s
      are adjusted for sampling errors in error */
  Data adjustSamplingError(Data error, int error_mode)
         throws VisADException, RemoteException;

  String longString(String pre)
         throws VisADException, RemoteException;

}

