
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

/**
   ThingChangedLink objects are used by ThingReference objects to
   define their connections with Action objects.  That is, a
   ThingReference has a Vector of ThingChangedLinks, one for
   each attached Action.<P>

   And Action has a Vector of RefernceActionLinks, one for
   each attached ThingReference.<P>
*/
class ThingChangedLink extends Object {

  Action action;  // may be remote or local
  boolean Ball; // true when Action is waiting for a ThingChangedEvent
                // false when this is waiting for an acknowledgement
  ThingChangedEvent event; // non-null only when Ball = false;

  /** this id is from the corresponding ReferenceActionLink */
  private long id;

  ThingChangedLink(Action a, long jd) throws VisADException {
    if (a == null) {
      throw new ReferenceException("ThingChangedLink: Action cannot be null");
    }
    action = a;
    Ball = false;
    id = jd;
  }

  long getId() {
    return id;
  }

  Action getAction() {
    return action;
  }

  boolean getBall() {
    return Ball;
  }

  void setBall(boolean b) {
    Ball = b;
  }

  ThingChangedEvent getThingChangedEvent() {
    return event;
  }

  void setThingChangedEvent(ThingChangedEvent e) {
    event = e;
  }

}

