
//
// RemoteActionImpl.java
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
   RemoteActionImpl is the VisAD remote adapter for ActionImpl.<P>
*/
public abstract class RemoteActionImpl extends UnicastRemoteObject
       implements RemoteAction {

  final transient ActionImpl AdaptedAction;

  RemoteActionImpl(ActionImpl a) throws RemoteException {
    AdaptedAction = a;
  }

  // WLH 4 Dec 98
  // public void thingChanged(ThingChangedEvent e)
  public boolean thingChanged(ThingChangedEvent e)
         throws VisADException, RemoteException {
    if (AdaptedAction == null) {
      throw new RemoteVisADException("RemoteActionImpl.thingChanged: " +
                                     "AdaptedAction is null");
    }
    // WLH 4 Dec 98
    // AdaptedAction.thingChanged(e);
    return AdaptedAction.thingChanged(e);
  }

  /** create link to ThingReference;
      must be RemoteThingReferenceImpl */
  public void addReference(ThingReference ref)
         throws VisADException, RemoteException {
    if (!(ref instanceof RemoteThingReference)) {
      throw new RemoteVisADException("RemoteActionImpl.addReference: requires " +
                                     "RemoteThingReferenceImpl");
    }
    if (AdaptedAction == null) {
      throw new RemoteVisADException("RemoteActionImpl.addReference " +
                                     "AdaptedAction is null");
    }
    // WLH - will 'this' be passed to RemoteThingReference ref as a RemoteAction?
    AdaptedAction.adaptedAddReference((RemoteThingReference) ref,
                                      (RemoteAction) this);
  }

  /** delete link to a ThingReference
      must be RemoteThingReferenceImpl */
  public void removeReference(ThingReference ref)
         throws VisADException, RemoteException {
    if (!(ref instanceof RemoteThingReference)) {
      throw new RemoteVisADException("RemoteActionImpl.removeReference: requires " +
                                     "RemoteThingReferenceImpl");
    }
    if (AdaptedAction == null) {
      throw new RemoteVisADException("RemoteActionImpl.removeReference: " +
                                     "AdaptedAction is null");
    }
    AdaptedAction.adaptedRemoveReference((RemoteThingReference) ref);
  }

  /** delete all links to ThingReferences */
  public void removeAllReferences()
         throws VisADException, RemoteException {
    if (AdaptedAction == null) {
      throw new RemoteVisADException("RemoteActionImpl.removeAllReferences: " +
                                     "AdaptedAction is null");
    }
    AdaptedAction.removeAllReferences();
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

