
//
// ReferenceActionLink.java
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

/**
   ReferenceActionLink objects are used by Action objects to
   define their connections with DataReference objects.<P>
*/
public class ReferenceActionLink extends Object {

  DataReference ref;
  ActionImpl local_action;
  Action action;  // may be remote or local

  /** this id is unique among ReferenceActionLink attached to action */
  private long id;

  /** set by incTick */
  private long NewTick;
  /** value of NewTick at last setTicks() call */
  private long OldTick;
  /** set by setTicks if OldTick < NewTick; cleared by resetTicks */
  private boolean tickFlag;

  /** Ball describes state of protocol between this ReferenceActionLink
      and DataReference ref;
      false when this is waiting for a DataChangedEvent;
      true when ref is waiting for an acknowledgement */
  boolean Ball;

  public ReferenceActionLink(DataReference r, ActionImpl local_a, Action a,
                             long jd) throws VisADException {
    if (r == null || a == null) {
      throw new ReferenceException("ReferenceActionLink: DataReference and " +
                                   "Action cannot be null");
    }
    ref = r;
    local_action = local_a;
    action = a;
    Ball = true;
    id = jd;
  }

  long getId() {
    return id;
  }

  public DataReference getDataReference() {
    return ref;
  }

  public ActionImpl getLocalAction() {
    return local_action;
  }

  public Action getAction() {
    return action;
  }

  /** initialize Ticks requesting Action to be applied */
  synchronized void initTicks(long tick) {
    OldTick = tick - 1;
    NewTick = tick;
  }

  /** set value of NewTick; presumably ncreases value */
  synchronized void incTick(long t) {
    NewTick = t;
  }

  /** set tickFlag according to OldTick and NewTick */
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

  public synchronized boolean peekTicks() {
    return (OldTick < NewTick || (NewTick < 0 && 0 < OldTick));
  }

  /** check whether this link requests Action to be applied */
  public synchronized boolean checkTicks() {
    return tickFlag;
  }

  /** reset tickFlag */
  synchronized void resetTicks() {
    tickFlag = false;
  }

  boolean getBall() {
    return Ball;
  }
 
  void setBall(boolean b) {
    Ball = b;
  }

}

