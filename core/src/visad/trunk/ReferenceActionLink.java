//
// ReferenceActionLink.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2001 Bill Hibbard, Curtis Rueden, Tom
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
   ReferenceActionLink objects are used by Action objects to
   define their connections with ThingReference objects.<P>
*/
public class ReferenceActionLink {

  ThingReference ref;
  ActionImpl local_action;
  Action action;  // may be remote or local

  /** this id is unique among ReferenceActionLink attached to action */
  private long id;

  /** name of the associated thing */
  private String name;

  /** set by incTick */
  private long NewTick;
  /** value of NewTick at last setTicks() call */
  private long OldTick;
  /** set by setTicks if OldTick < NewTick; cleared by resetTicks */
  private boolean tickFlag;

  /** Ball describes state of protocol between this ReferenceActionLink
      and ThingReference ref;
      false when this is waiting for a ThingChangedEvent;
      true when ref is waiting for an acknowledgement */
  private boolean Ball;

  public ReferenceActionLink(ThingReference r, ActionImpl local_a, Action a,
                             long jd)
    throws RemoteException, VisADException {
    if (r == null || a == null) {
      throw new ReferenceException("ReferenceActionLink: ThingReference and " +
                                   "Action cannot be null");
    }
    ref = r;
    local_action = local_a;
    action = a;
    Ball = true;
    id = jd;

    name = ref.getName();

    NewTick = ref.getTick();
    OldTick = NewTick - 1;
  }

  long getId() {
    return id;
  }

  public ThingReference getThingReference() {
    return ref;
  }

  public ActionImpl getLocalAction() {
    return local_action;
  }

  public Action getAction() {
    return action;
  }

  public String getName() {
    return name;
  }

  /** set value of NewTick; presumably increases value */
  synchronized void incTick(long t) {
    NewTick = t;
  }

  /** sync consumer's tick count with producer's tick count */
  public synchronized void setTicks() {
    tickFlag = (OldTick < NewTick || (NewTick < 0 && 0 < OldTick));
    OldTick = NewTick;
  }

/*
  public void printTicks(String s) {
System.out.println(s + ":  tickFlag = " + tickFlag + "  OldTick = " + OldTick +
                  "  NewTick = " + NewTick);
  }
*/

  /** returns true if there is an action pending */
  public synchronized boolean peekTicks() {
    return (OldTick < NewTick || (NewTick < 0 && 0 < OldTick));
  }

  /** check whether this link requests Action to be applied */
  public synchronized boolean checkTicks() {
    return tickFlag;
  }

  /** clear internal state */
  synchronized void resetTicks() {
    tickFlag = false;
  }

  /** return any waiting event */
  ThingChangedEvent getThingChangedEvent()
        throws RemoteException, VisADException
  {
    ThingChangedEvent event = null;

    // if the reference has an event waiting...
    if (Ball) {

/* WLH 27 July 99
      // get the event
      event = ref.acknowledgeThingChanged(action);

      // remember that we picked up the event
      Ball = false;
*/
      // remember that we picked up the event
      Ball = false;
      // get the event
      event = ref.acknowledgeThingChanged(action);
/*
System.out.println("ReferenceActionLink.getThingChangedEvent Ball = false " +
                   "event non null = " + (event != null));
*/
    }

    return event;
  }

  void acknowledgeThingChangedEvent(long actionTick) {
    NewTick = actionTick;
    Ball = true;
/*
System.out.println("ReferenceActionLink.acknowledgeThingChangedEvent Ball = true");
*/
  }

}

