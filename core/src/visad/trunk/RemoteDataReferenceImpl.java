
//
// RemoteDataReferenceImpl.java
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
import java.rmi.server.UnicastRemoteObject;

/**
   RemoteDataReferenceImpl is VisAD remote adapter for DataReferenceImpl.<P>
*/
public class RemoteDataReferenceImpl extends UnicastRemoteObject
       implements RemoteDataReference {

  final transient DataReferenceImpl AdaptedDataReference;

  public RemoteDataReferenceImpl(DataReferenceImpl ref) throws RemoteException {
    AdaptedDataReference = ref;
  }

  /** set this RemoteDataReferenceImpl to refer to d;
      must be RemoteDataImpl */
  public synchronized void setData(Data d)
         throws VisADException, RemoteException {
    if (d == null) {
      throw new ReferenceException("RemoteDataReferenceImpl: data " +
                                   "cannot be null");
    }
    if (AdaptedDataReference == null) {
      throw new RemoteVisADException("RemoteDataReferenceImpl.setData: " +
                                     "AdaptedDataReference is null");
    }
    if (d instanceof DataImpl) {
/* WLH 12 Dec 97 - allow Data object passed by copy from remote JVM
      throw new RemoteVisADException("RemoteDataReferenceImpl.setData: must use " +
                                     "DataReferenceImpl for DataImpl");
*/
      AdaptedDataReference.setData(d);
    }
    else {
      AdaptedDataReference.adaptedSetData((RemoteData) d,
                                          (RemoteDataReference) this);
    }
  }

  public Data getData() throws VisADException, RemoteException {
    if (AdaptedDataReference == null) {
      throw new RemoteVisADException("RemoteDataReferenceImpl.getData: " +
                                     "AdaptedDataReference is null");
    }
    Data data = AdaptedDataReference.getData();
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

  public long getTick() throws VisADException, RemoteException {
    if (AdaptedDataReference == null) {
      throw new RemoteVisADException("RemoteDataReferenceImpl.getTick: " +
                                     "AdaptedDataReference is null");
    }
    return AdaptedDataReference.getTick();
  }

  public long incTick() throws VisADException, RemoteException {
    if (AdaptedDataReference == null) {
      throw new RemoteVisADException("RemoteDataReferenceImpl.incTick: " +
                                     "AdaptedDataReference is null");
    }
    return AdaptedDataReference.incTick();
  }

  public String getName() throws VisADException, RemoteException {
    if (AdaptedDataReference == null) {
      throw new RemoteVisADException("RemoteDataReferenceImpl.getName: " +
                                     "AdaptedDataReference is null");
    }
    return AdaptedDataReference.getName();
  }

  /** addDataChangedListener and removeDataChangedListener
      provide DataChangedOccurrence source semantics;
      Action must be RemoteAction */
  public void addDataChangedListener(Action a)
         throws VisADException, RemoteException {
    if (a instanceof ActionImpl) {
      throw new RemoteVisADException("RemoteDataReferenceImpl.addDataChanged" +
                                     "Listener: Action must be Remote");
    }
    if (AdaptedDataReference == null) {
      throw new RemoteVisADException("RemoteDataReferenceImpl." +
                                     "addDataChangedListener: " +
                                     "AdaptedDataReference is null");
    }
    AdaptedDataReference.adaptedAddDataChangedListener(((RemoteAction) a));
  }

  /** DataChangedListener must be RemoteActionImpl */
  public void removeDataChangedListener(Action a)
         throws VisADException, RemoteException {
    if (a instanceof ActionImpl) {
      throw new RemoteVisADException("RemoteDataReferenceImpl.removeDataChanged" +
                                     "Listener: Action must be Remote");
    }
    if (AdaptedDataReference == null) {
      throw new RemoteVisADException("RemoteDataReferenceImpl." +
                                     "removeDataChangedListener: " +
                                     "AdaptedDataReference is null");
    }
    AdaptedDataReference.adaptedRemoveDataChangedListener(((RemoteAction) a));
  }

  /** DataChangedListener must be RemoteActionImpl */
  public DataChangedOccurrence acknowledgeDataChanged(Action a)
         throws VisADException, RemoteException {
    if (a instanceof ActionImpl) {
      throw new RemoteVisADException("RemoteDataReferenceImpl.acknowledge" +
                                     "DataChanged: Action must be Remote");
    }
    if (AdaptedDataReference == null) {
      throw new RemoteVisADException("RemoteDataReferenceImpl." +
                                     "acknowledgeDataChanged: " +
                                     "AdaptedDataReference is null");
    }
    return AdaptedDataReference.adaptedAcknowledgeDataChanged(((RemoteAction) a));
  }

}

