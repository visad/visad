
//
// ReferenceActionLink.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 1998 Bill Hibbard, Curtis Rueden and Tom
Rink.
 
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

/**
   ReferenceActionLink objects are used by Action objects to
   define their connections with DataReference objects.<P>
*/
class ReferenceActionLink extends Object {

  DataReference ref;
  ActionImpl local_action;
  Action action;  // may be remote or local
  // value of ref.getTick() last time Action was applied to ref
  private long OldTick;
  // value of ref.getTick() from latest DataChangedOccurrence
  private long NewTick;

  /** Ball describes state of protocol between this ReferenceActionLink
      and DataReference ref;
      false when this is waiting for a DataChangedOccurrence;
      true when ref is waiting for an acknowledgement */
  boolean Ball;

  ReferenceActionLink(DataReference r, ActionImpl local_a, Action a)
                      throws VisADException {
    if (r == null || a == null) {
      throw new ReferenceException("ReferenceActionLink: DataReference and " +
                                   "Action cannot be null");
    }
    ref = r;
    local_action = local_a;
    action = a;
    Ball = true;
  }

  DataReference getDataReference() {
    return ref;
  }

  ActionImpl getLocalAction() {
    return local_action;
  }

  Action getAction() {
    return action;
  }

  /** initialize Ticks requesting Action to be applied */
  synchronized void initTicks(long tick) {
    OldTick = tick - 1;
    NewTick = tick;
  }

  /** check whether this link requests Action to be applied */
  synchronized boolean checkTicks() {
    return (OldTick < NewTick || (NewTick < 0 && 0 < OldTick));
  }

  /** clear request for Action to be applied */
  synchronized void syncTicks() {
    OldTick = NewTick;
  }

  /** set value of NewTick; presumably requests Action */
  synchronized void setTicks(long t) {
    NewTick = t;
  }

  boolean getBall() {
    return Ball;
  }
 
  void setBall(boolean b) {
    Ball = b;
  }

}

