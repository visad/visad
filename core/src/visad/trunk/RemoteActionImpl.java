
//
// RemoteActionImpl.java
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
   RemoteActionImpl is the VisAD remote adapter for ActionImpl.<P>
*/
public abstract class RemoteActionImpl extends UnicastRemoteObject
       implements RemoteAction {

  final transient ActionImpl AdaptedAction;

  RemoteActionImpl(ActionImpl a) throws RemoteException {
    AdaptedAction = a;
  }

  public void dataChanged(DataChangedOccurrence e)
         throws VisADException, RemoteException {
    if (AdaptedAction == null) {
      throw new RemoteVisADException("RemoteActionImpl.dataChanged: " +
                                     "AdaptedAction is null");
    }
    AdaptedAction.dataChanged(e);
  }

  /** create link to DataReference;
      must be RemoteDataReferenceImpl */
  public void addReference(DataReference ref)
         throws VisADException, RemoteException {
    if (ref instanceof DataReferenceImpl) {
      throw new RemoteVisADException("RemoteActionImpl.addReference: requires " +
                                     "RemoteDataReferenceImpl");
    }
    if (AdaptedAction == null) {
      throw new RemoteVisADException("RemoteActionImpl.addReference " +
                                     "AdaptedAction is null");
    }
    // WLH - will 'this' be passed to RemoteDataReference ref as a RemoteAction?
    AdaptedAction.adaptedAddReference((RemoteDataReference) ref,
                                      (RemoteAction) this);
  }

  /** delete link to a DataReference
      must be RemoteDataReferenceImpl */
  public void removeReference(DataReference ref)
         throws VisADException, RemoteException {
    if (ref instanceof DataReferenceImpl) {
      throw new RemoteVisADException("RemoteActionImpl.removeReference: requires " +
                                     "RemoteDataReferenceImpl");
    }
    if (AdaptedAction == null) {
      throw new RemoteVisADException("RemoteActionImpl.removeReference: " +
                                     "AdaptedAction is null");
    }
    AdaptedAction.adaptedRemoveReference((RemoteDataReference) ref);
  }

  /** return name of this Action */
  public String getName() throws VisADException {
    if (AdaptedAction == null) {
      throw new RemoteVisADException("RemoteActionImpl.getName: " +
                                     "AdaptedAction is null");
    }
    return AdaptedAction.getName();
  }

}

