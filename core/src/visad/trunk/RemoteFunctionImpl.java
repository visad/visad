
//
// RemoteFunctionImpl.java
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
import java.rmi.server.UnicastRemoteObject;

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

}

