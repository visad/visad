 
//
// ThingChangedEvent.java
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
import java.awt.Event;

/**
   ThingChangedEvent is the VisAD class for changes in objects
   (usually Data objects) referred to by ThingReference objects.
   They are sourced by ThingReference objects and received by
   Action objects.<P>
*/
public class ThingChangedEvent extends Event {

  /** this is the id attached to the target ActionReferenceLink
      of the target Action */
  private long id;

  /** this is the Tick value from the ThingReference change
      that generated this ThingChangedEvent */
  private long Tick;

  public ThingChangedEvent(long jd, long tick) {
    super(null, 0, null);
    id = jd;
    Tick = tick;
  }

  public long getId() {
    return id;
  }

  public long getTick() {
    return Tick;
  }

}

