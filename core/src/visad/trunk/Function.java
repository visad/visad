
//
// Function.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 1998 Bill Hibbard, Curtis Rueden and Tom
Rink.
 
This program is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 1, or (at your option)
any later version.
 
This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License in file NOTICE for more details.
 
You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
*/

package visad;

import java.util.*;
import java.rmi.*;

/**
   Function is the interface for approximate implmentations
   of mathematical function.<P>
*/
public interface Function extends Data {

  public abstract int getDomainDimension()
         throws VisADException, RemoteException;

  /** evaluate this Function at domain; first check that types match;
      use default modes for resampling (NEAREST_NEIGHBOR) and errors */
  public abstract Data evaluate(RealTuple domain)
         throws VisADException, RemoteException;

  /** evaluate this Function with non-default modes for resampling and errors */
  public abstract Data evaluate(RealTuple domain, int sampling_mode,
         int error_mode) throws VisADException, RemoteException;

  /** resample range values of this Function to domain samples in set;
      return a Field (i.e., a finite sampling of a Function) */
  public abstract Field resample(Set set, int sampling_mode, int error_mode)
         throws VisADException, RemoteException;

}

