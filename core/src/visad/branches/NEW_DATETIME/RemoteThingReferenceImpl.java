
//
// RemoteThingReferenceImpl.java
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
   RemoteThingReferenceImpl is VisAD remote adapter for ThingReferenceImpl.<P>
*/
public class RemoteThingReferenceImpl extends UnicastRemoteObject
       implements RemoteThingReference {

  final transient ThingReferenceImpl AdaptedThingReference;

  public RemoteThingReferenceImpl(ThingReferenceImpl ref) throws RemoteException {
    AdaptedThingReference = ref;
  }

  /** set this RemoteThingReferenceImpl to refer to t;
      must be RemoteThingImpl */
  public synchronized void setThing(Thing t)
         throws VisADException, RemoteException {
    if (t == null) {
      throw new ReferenceException("RemoteThingReferenceImpl: thing " +
                                   "cannot be null");
    }
    if (AdaptedThingReference == null) {
      throw new RemoteVisADException("RemoteThingReferenceImpl.setThing: " +
                                     "AdaptedThingReference is null");
    }
    if (t instanceof ThingImpl) {
      // allow Thing object passed by copy from remote JVM
      AdaptedThingReference.setThing(t);
    }
    else {
      AdaptedThingReference.adaptedSetThing((RemoteThing) t,
                                          (RemoteThingReference) this);
    }
  }

  public Thing getThing() throws VisADException, RemoteException {
    if (AdaptedThingReference == null) {
      throw new RemoteVisADException("RemoteThingReferenceImpl.getThing: " +
                                     "AdaptedThingReference is null");
    }
    return AdaptedThingReference.getThing();
  }

  public long getTick() throws VisADException, RemoteException {
    if (AdaptedThingReference == null) {
      throw new RemoteVisADException("RemoteThingReferenceImpl.getTick: " +
                                     "AdaptedThingReference is null");
    }
    return AdaptedThingReference.getTick();
  }

  public long incTick() throws VisADException, RemoteException {
    if (AdaptedThingReference == null) {
      throw new RemoteVisADException("RemoteThingReferenceImpl.incTick: " +
                                     "AdaptedThingReference is null");
    }
    return AdaptedThingReference.incTick();
  }

  public String getName() throws VisADException, RemoteException {
    if (AdaptedThingReference == null) {
      throw new RemoteVisADException("RemoteThingReferenceImpl.getName: " +
                                     "AdaptedThingReference is null");
    }
    return AdaptedThingReference.getName();
  }

  /** addThingChangedListener and removeThingChangedListener
      provide ThingChangedEvent source semantics;
      Action must be RemoteAction */
  public void addThingChangedListener(ThingChangedListener a, long id)
         throws VisADException, RemoteException {
    if (!(a instanceof RemoteAction)) {
      throw new RemoteVisADException("RemoteThingReferenceImpl.addThingChanged" +
                                     "Listener: Action must be Remote");
    }
    if (AdaptedThingReference == null) {
      throw new RemoteVisADException("RemoteThingReferenceImpl." +
                                     "addThingChangedListener: " +
                                     "AdaptedThingReference is null");
    }
    AdaptedThingReference.adaptedAddThingChangedListener(((RemoteAction) a), id);
  }

  /** ThingChangedListener must be RemoteAction */
  public void removeThingChangedListener(ThingChangedListener a)
         throws VisADException, RemoteException {
    if (!(a instanceof RemoteAction)) {
      throw new RemoteVisADException("RemoteThingReferenceImpl.removeThingChanged" +
                                     "Listener: Action must be Remote");
    }
    if (AdaptedThingReference == null) {
      throw new RemoteVisADException("RemoteThingReferenceImpl." +
                                     "removeThingChangedListener: " +
                                     "AdaptedThingReference is null");
    }
    AdaptedThingReference.adaptedRemoveThingChangedListener(((RemoteAction) a));
  }

  /** Action must be RemoteAction */
  public ThingChangedEvent acknowledgeThingChanged(Action a)
         throws VisADException, RemoteException {
    if (!(a instanceof RemoteAction)) {
      throw new RemoteVisADException("RemoteThingReferenceImpl.acknowledge" +
                                     "ThingChanged: Action must be Remote");
    }
    if (AdaptedThingReference == null) {
      throw new RemoteVisADException("RemoteThingReferenceImpl." +
                                     "acknowledgeThingChanged: " +
                                     "AdaptedThingReference is null");
    }
    return AdaptedThingReference.adaptedAcknowledgeThingChanged(((RemoteAction) a));
  }

}

