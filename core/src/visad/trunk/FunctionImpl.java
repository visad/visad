
//
// FunctionImpl.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 1998 Bill Hibbard, Curtis Rueden, Tom
Rink and Dave Glowacki.
 
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
   FunctionImpl is the abstract superclass for approximate
   implmentations of mathematical functions.  It inherits
   the Function interface.<P>
*/
public abstract class FunctionImpl extends DataImpl implements Function {

  public FunctionImpl(FunctionType type) {
    super(type);
  }

  public int getDomainDimension() {
     return ((FunctionType) Type).getDomain().getDimension();
  }

  public Unit[] getDomainUnits() {
     return ((FunctionType) Type).getDomain().getDefaultUnits();
  }

  public CoordinateSystem getDomainCoordinateSystem() {
    return ((FunctionType) Type).getDomain().getCoordinateSystem();
  }

  /** evaluate this Function at domain;
      use default modes for resampling (NEAREST_NEIGHBOR) and errors */
  public Data evaluate(Real domain)
         throws VisADException, RemoteException {
    return evaluate(new RealTuple(new Real[] {domain}),
                    NEAREST_NEIGHBOR, NO_ERRORS);
  }

  /** evaluate this Function with non-default modes for resampling and errors */
  public Data evaluate(Real domain, int sampling_mode, int error_mode)
              throws VisADException, RemoteException {
    return evaluate(new RealTuple(new Real[] {domain}),
                    sampling_mode, error_mode);
  }

  /** evaluate this Function at domain;
      use default modes for resampling (NEAREST_NEIGHBOR) and errors */
  public Data evaluate(RealTuple domain)
         throws VisADException, RemoteException {
    return evaluate(domain, NEAREST_NEIGHBOR, NO_ERRORS);
  }

  /** evaluate this Function with non-default modes for resampling and errors */
  public Data evaluate(RealTuple domain, int sampling_mode, int error_mode)
              throws VisADException, RemoteException {
    Field field = resample(new SingletonSet(domain, domain.getCoordinateSystem(),
                           domain.getTupleUnits(), domain.getErrors()),
                           sampling_mode, error_mode);
    return field.getSample(0);
  }

  /** resample range values of this Function to domain samples in set;
      return a Field (i.e., a finite sampling of a Function) */
  public abstract Field resample(Set set, int sampling_mode, int error_mode)
         throws VisADException, RemoteException;

}

