//
// ThingChangedLink.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2018 Bill Hibbard, Curtis Rueden, Tom
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

  /** possibly return a new event to the Action */
  public synchronized ThingChangedEvent peekThingChangedEvent()
  {
    return event;
  }

  /** acknowledge the last event from the ThingReference,
   *  and possibly return a new event to the Action
   */
/* WLH 27 July 99 synchronized helps but does not fix */
  // public ThingChangedEvent acknowledgeThingChangedEvent()
  public synchronized ThingChangedEvent acknowledgeThingChangedEvent()
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
/*
if (action instanceof ActionImpl) {
  System.out.println("ThingChangedLink.acknowledgeThingChangedEvent Ball = true");
}
else {
  System.out.println("ThingChangedLink.acknowledgeThingChangedEvent Ball = true" +
                   " REMOTE");
}
*/
    return null;
  }

  /** either deliver the event to the corresponding Action object
   *  or, if the Action isn't ready yet, queue the event for
   *  later delivery
   */
/* WLH 27 July 99 synchronized helps but does not fix */
  // public void queueThingChangedEvent(ThingChangedEvent e)
  public synchronized void queueThingChangedEvent(ThingChangedEvent e)
        throws RemoteException, VisADException
  {
    if (Ball) {
      // if Action is ready for another event, pass it on
/* WLH 27 July 99
      Ball = action.thingChanged(e);
*/
      Ball = false;
      boolean temp_ball = action.thingChanged(e);
      if (temp_ball) Ball = true;
/*
if (action instanceof ActionImpl) {
  System.out.println("ThingChangedLink.queueThingChangedEvent Ball = " + Ball);
}
else {
  System.out.println("ThingChangedLink.queueThingChangedEvent Ball = " + Ball +
                   " REMOTE");
}
*/
    }
    else {
      // Action hasn't acknowledged previous event, queue this one
      event = e;
    }
  }

}

