//
// RemoteThingImpl.java
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
import java.rmi.server.UnicastRemoteObject;

/**
   RemoteThingImpl is the VisAD remote adapter for ThingImpl.<P>
*/
// public abstract class RemoteThingImpl extends UnicastRemoteObject
public class RemoteThingImpl extends UnicastRemoteObject
       implements RemoteThing {

  /** 'this' is the Remote adaptor for AdaptedThing (which is local);
      AdaptedThing is transient because UnicastRemoteObject is
      Serializable, but a copy of 'this' on another JVM will not
      be local to AdaptedThing and cannot adapt it;
      the methods of RemoteThingImpl text for null AdaptedThing */
  final transient ThingImpl AdaptedThing;

  /** Tick increments each time data changes;
      used in place of propogating notifyReferences
      to Remote parents */
  private long Tick;

  public RemoteThingImpl(ThingImpl thing) throws RemoteException {
    AdaptedThing = thing;
    Tick = Long.MIN_VALUE + 1;
  }

  /** Tick is incremented in a RemoteThing object, rather than
      propogating Thing changes to RemoteThingReference-s */
  public long incTick() {
    Tick += 1;
    if (Tick == Long.MAX_VALUE) Tick = Long.MIN_VALUE + 1;
    return Tick;
  }

  /** RemoteThingReference-s can (but don't currently) poll getTick() */
  public long getTick() {
    return Tick;
  }

  /** methods adapted from Thing;
      do not adapt equals, toString, hashCode or clone */

  /** add a ThingReference to this RemoteThingImpl;
      must be RemoteThingReference;
      called by ThingReference.setThing */
  public void addReference(ThingReference r) throws VisADException {
    if (!(r instanceof RemoteThingReference)) {
      throw new RemoteVisADException("RemoteThingImpl.addReference: must use " +
                                     "RemoteThingReference");
    }
    if (AdaptedThing == null) {
      throw new RemoteVisADException("RemoteThingImpl.addReference " +
                                     "AdaptedThing is null");
    }
    AdaptedThing.adaptedAddReference((RemoteThingReference )r,
                                     (RemoteThing )this);
  }

  /** remove a ThingReference to this RemoteThingImpl;
      must be RemoteThingReferenceImpl;
      called by ThingReference.setThing */
  public void removeReference(ThingReference r) throws VisADException {
    if (!(r instanceof RemoteThingReference)) {
      throw new RemoteVisADException("RemoteThingImpl.addReference: must use " +
                                     "RemoteThingReference");
    }
    if (AdaptedThing == null) {
      throw new RemoteVisADException("RemoteThingImpl.removeReference " +
                                     "AdaptedThing is null");
    }
    AdaptedThing.adaptedRemoveReference((RemoteThingReference )r,
                                        (RemoteThing )this);
  }

}

