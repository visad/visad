//
// RemoteFunctionImpl.java
//

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

package visad;

import java.rmi.*;

/**
   RemoteFunctionImpl is the VisAD remote adapter for FieldImpl.<P>
*/
public abstract class RemoteFunctionImpl extends RemoteDataImpl
       implements RemoteFunction {

  public RemoteFunctionImpl(FunctionImpl function) throws RemoteException {
    super(function);
  }

  /** methods adapted from Function */
  public int getDomainDimension() throws VisADException, RemoteException {
    if (AdaptedData == null) {
      throw new RemoteVisADException("RemoteFunctionImpl.getDomainDimension: " +
                                     "AdaptedData is null");
    }
    return ((FunctionImpl) AdaptedData).getDomainDimension();
  }

  public Unit[] getDomainUnits() throws VisADException, RemoteException {
    if (AdaptedData == null) {
      throw new RemoteVisADException("RemoteFunctionImpl.getDomainUnits: " +
                                     "AdaptedData is null");
    }
    return ((FunctionImpl) AdaptedData).getDomainUnits();
  }

  public CoordinateSystem getDomainCoordinateSystem()
         throws VisADException, RemoteException {
    if (AdaptedData == null) {
      throw new RemoteVisADException(
                 "RemoteFunctionImpl.getDomainCoordinateSystem: " +
                 "AdaptedData is null");
    }
    return ((FunctionImpl) AdaptedData).getDomainCoordinateSystem();
  }

  public Data evaluate(Real domain)
         throws VisADException, RemoteException {
    if (AdaptedData == null) {
      throw new RemoteVisADException("RemoteFunctionImpl.evaluate: " +
                                     "AdaptedData is null");
    }
    return ((FunctionImpl) AdaptedData).evaluate(domain);
  }

  public Data evaluate(Real domain, int sampling_mode, int error_mode)
              throws VisADException, RemoteException {
    if (AdaptedData == null) {
      throw new RemoteVisADException("RemoteFunctionImpl.evaluate: " +
                                     "AdaptedData is null");
    }
    return ((FunctionImpl) AdaptedData).evaluate(domain,
                                                 sampling_mode, error_mode);
  }

  public Data evaluate(RealTuple domain)
         throws VisADException, RemoteException {
    if (AdaptedData == null) {
      throw new RemoteVisADException("RemoteFunctionImpl.evaluate: " +
                                     "AdaptedData is null");
    }
    return ((FunctionImpl) AdaptedData).evaluate(domain);
  }

  public Data evaluate(RealTuple domain, int sampling_mode, int error_mode)
              throws VisADException, RemoteException {
    if (AdaptedData == null) {
      throw new RemoteVisADException("RemoteFunctionImpl.evaluate: " +
                                     "AdaptedData is null");
    }
    return ((FunctionImpl) AdaptedData).evaluate(domain,
                                                 sampling_mode, error_mode);
  }

  public Field resample(Set set) throws VisADException, RemoteException {
    return resample(set, Data.WEIGHTED_AVERAGE, Data.NO_ERRORS);
  }

  /** can decide whether to return the local FieldImpl returned by
      ((FunctionImpl) AdaptedData).resample, or whether to return a
      RemoteFunctionImpl adapted for that FieldImpl;
      the same is true for the methods: extract, binary, unary, evaluate
      and getSample (as long as their return value is an instanceof Field) */
  public Field resample(Set set, int sampling_mode, int error_mode)
         throws VisADException, RemoteException {
    if (AdaptedData == null) {
      throw new RemoteVisADException("RemoteFunctionImpl.resample: " +
                                     "AdaptedData is null");
    }
    return ((FunctionImpl) AdaptedData).resample(set, sampling_mode, error_mode);
  }

  public Data derivative( RealTuple location, RealType[] d_partial_s,
                          MathType[] derivType_s, int error_mode )
         throws VisADException, RemoteException {
    if (AdaptedData == null) {
      throw new RemoteVisADException("RemoteFunctionImpl.derivative: " +
                                     "AdaptedData is null");
    }
    return ((FunctionImpl) AdaptedData).derivative( location, d_partial_s, derivType_s, error_mode);
  }

  public Data derivative( int error_mode )
         throws VisADException, RemoteException {
    if (AdaptedData == null) {
      throw new RemoteVisADException("RemoteFunctionImpl.derivative: " +
                                     "AdaptedData is null");
    }
    return ((FunctionImpl) AdaptedData).derivative(error_mode);
  }

  public Data derivative( MathType[] derivType_s, int error_mode )
         throws VisADException, RemoteException {
    if (AdaptedData == null) {
      throw new RemoteVisADException("RemoteFunctionImpl.derivative: " +
                                     "AdaptedData is null");
    }
    return ((FunctionImpl) AdaptedData).derivative(derivType_s, error_mode);
  }

  public Function derivative( RealType d_partial, int error_mode )
         throws VisADException, RemoteException {
    if (AdaptedData == null) {
      throw new RemoteVisADException("RemoteFunctionImpl.derivative: " +
                                     "AdaptedData is null");
    }
    return ((FunctionImpl) AdaptedData).derivative(d_partial, error_mode);
  }

  public Function derivative( RealType d_partial, MathType derivType, int error_mode )
         throws VisADException, RemoteException {
    if (AdaptedData == null) {
      throw new RemoteVisADException("RemoteFunctionImpl.derivative: " +
                                     "AdaptedData is null");
    }
    return ((FunctionImpl) AdaptedData).derivative( d_partial, derivType, error_mode);
  }
}

