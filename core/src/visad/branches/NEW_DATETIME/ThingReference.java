
//
// ThingReference.java
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
  public abstract void setThing(Thing t) throws VisADException, RemoteException;
 
  public abstract Thing getThing() throws VisADException, RemoteException;
 
  public abstract long getTick() throws VisADException, RemoteException;

  public abstract long incTick() throws VisADException, RemoteException;

  public abstract String getName() throws VisADException, RemoteException;

  public abstract void addThingChangedListener(ThingChangedListener l, long id)
         throws VisADException, RemoteException;

  public abstract void removeThingChangedListener(ThingChangedListener l)
         throws VisADException, RemoteException;

  public ThingChangedEvent acknowledgeThingChanged(Action a)
         throws VisADException, RemoteException;
}

