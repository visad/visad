//
// Action.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2002 Bill Hibbard, Curtis Rueden, Tom
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
  void addReference(ThingReference ref)
         throws VisADException, RemoteException;

  /** delete link to a ThingReference */
  void removeReference(ThingReference ref)
         throws VisADException, RemoteException;

  /** delete all links to ThingReferences */
  void removeAllReferences()
         throws VisADException, RemoteException;

  /** return name of this Action */
  String getName()
         throws VisADException, RemoteException;

}

