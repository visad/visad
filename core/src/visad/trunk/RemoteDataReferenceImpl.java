
//
// RemoteDataReferenceImpl.java
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
   RemoteDataReferenceImpl is VisAD remote adapter for DataReferenceImpl.<P>
*/
public class RemoteDataReferenceImpl extends RemoteThingReferenceImpl
       implements RemoteDataReference {

  public RemoteDataReferenceImpl(DataReferenceImpl ref) throws RemoteException {
    super(ref);
  }

  /** set this RemoteDataReferenceImpl to refer to d;
      must be RemoteDataImpl */
  public synchronized void setData(Data d)
         throws VisADException, RemoteException {
    if (d == null) {
      throw new ReferenceException("RemoteDataReferenceImpl: data " +
                                   "cannot be null");
    }
    if (AdaptedThingReference == null) {
      throw new RemoteVisADException("RemoteDataReferenceImpl.setData: " +
                                     "AdaptedThingReference is null");
    }
    if (d instanceof DataImpl) {
      // allow Data object passed by copy from remote JVM
      ((DataReferenceImpl) AdaptedThingReference).setData(d);
    }
    else {
      ((DataReferenceImpl) AdaptedThingReference).adaptedSetData((RemoteData) d,
                                          (RemoteDataReference) this);
    }
  }

  public Data getData() throws VisADException, RemoteException {
    if (AdaptedThingReference == null) {
      throw new RemoteVisADException("RemoteDataReferenceImpl.getData: " +
                                     "AdaptedThingReference is null");
    }
    Data data = ((DataReferenceImpl) AdaptedThingReference).getData();
    if (data instanceof FieldImpl) {
      // decide here whether to return copy of remote reference
      boolean return_copy = false;
      if (return_copy) {
        return data;
      }
      else {
        return new RemoteFieldImpl((FieldImpl) data);
      }
    }
    else {
      return data;
    }
  }

  public MathType getType() throws VisADException, RemoteException {
    if (AdaptedThingReference == null) {
      throw new RemoteVisADException("RemoteDataReferenceImpl.getType: " +
                                     "AdaptedThingReference is null");
    }
    return ((DataReferenceImpl) AdaptedThingReference).getData().getType();
  }

}

