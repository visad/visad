
//
// Action.java
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

/**
   Action is the VisAD interface for runnable threads that need to be
   notified when ThingReference objects change.  For example, this may
   be used for a Data display or for a spreadsheet cell.<P>
*/
public interface Action extends ThingChangedListener {

  /** create link to a ThingReference;
      invokes ref.addThingChangedListener(ThingChangedListener l, long id) */
  public abstract void addReference(ThingReference ref)
         throws VisADException, RemoteException;

  /** delete link to a ThingReference */
  public abstract void removeReference(ThingReference ref)
         throws VisADException, RemoteException;

  /** return name of this Action */
  public abstract String getName()
         throws VisADException, RemoteException;

}

