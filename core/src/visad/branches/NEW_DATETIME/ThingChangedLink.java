
//
// ThingChangedLink.java
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

import java.rmi.RemoteException;

/**
   ThingChangedLink objects are used by ThingReference objects to
   define their connections with Action objects.  That is, a
   ThingReference has a Vector of ThingChangedLinks, one for
   each attached Action.<P>

   Action has a Vector of ReferenceActionLinks, one for
   each attached ThingReference.<P>
*/
class ThingChangedLink extends Object {

  private Action action;  // may be remote or local
  private boolean Ball;
                           // true when Action is ready for a ThingChangedEvent
                           // false when this is waiting for an acknowledgement
  private ThingChangedEvent event; // non-null only when !Ball

  /** this id is from the corresponding ReferenceActionLink */
  private long id;

  ThingChangedLink(Action a, long jd) throws VisADException {
    if (a == null) {
      throw new ReferenceException("ThingChangedLink: Action cannot be null");
    }
    action = a;
    Ball = true;
    id = jd;
  }

  long getId() {
    return id;
  }

  Action getAction() {
    return action;
  }

  /** acknowledge the last event from the ThingReference,
   *  and possibly return a new event to the Action
   */
  public ThingChangedEvent acknowledgeThingChangedEvent()
  {
    // if there is an event queued...
    if (event != null) {

      // pass the queued event back to the Action
      ThingChangedEvent e = event;
      event = null;
      return e;
    }

    // remember that Action is ready for another event
    Ball = true;
    return null;
  }

  /** either deliver the event to the corresponding Action object
   *  or, if the Action isn't ready yet, queue the event for
   *  later delivery
   */
  public void queueThingChangedEvent(ThingChangedEvent e)
        throws RemoteException, VisADException
  {
    if (Ball) {
      // if Action is ready for another event, pass it on
      Ball = action.thingChanged(e);
    }
    else {
      // Action hasn't acknowledged previous event, queue this one
      event = e;
    }
  }

}

