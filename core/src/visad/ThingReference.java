//
// ThingReference.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2017 Bill Hibbard, Curtis Rueden, Tom
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
   ThingReference is the VisAD interface for named holders for
   objects.  It can provide a symbol table binding between a name and
   a variable for a user interface that includes a formula interpreter,
   or a full language interpreter (e.g., a Java interpreter).<P>

   During computations the object referenced by a ThingReference
   may change.<p>

   ThingReference is a source of ThingChangedEvent-s, and thus defines
   addThingChangedListener and removeThingChangedListener.<P>

   ThingReference objects may be local (ThingReferenceImpl) or
   remote (RemoteThingReferenceImpl).<P>
*/
public interface ThingReference {

  /** invokes t.addReference((ThingReference r) */
  void setThing(Thing t) throws VisADException, RemoteException;

  Thing getThing() throws VisADException, RemoteException;

  long getTick() throws VisADException, RemoteException;

  long incTick() throws VisADException, RemoteException;

  String getName() throws VisADException, RemoteException;

  void addThingChangedListener(ThingChangedListener l, long id)
         throws VisADException, RemoteException;

  void removeThingChangedListener(ThingChangedListener l)
         throws VisADException, RemoteException;

  ThingChangedEvent acknowledgeThingChanged(Action a)
         throws VisADException, RemoteException;

  ThingChangedEvent peekThingChanged(Action a)
         throws VisADException, RemoteException;
}

