 
//
// ScalarMapEvent.java
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
   ScalarMapEvent is the VisAD class for auto scaling
   calls to setRange in ScalarMap objects.  They are
   sourced by ScalarMap objects and received by
   ScalarMapListener objects.<P>
*/
public class ScalarMapEvent extends Event {

  /** values for id */
  public final static int AUTO_SCALE = 1;
  public final static int MANUAL = 2;

  private int id = 0; // WLH 25 March 99

  private ScalarMap map; // source of event

  public ScalarMapEvent(ScalarMap m, boolean auto) {
    // don't pass map as the source, since source
    // is transient inside Event
    super(null, 0, null);
    map = m;
    id = auto ? AUTO_SCALE : MANUAL;
  }

  /** get the ScalarMap that sent this ScalarMapEvent (or
      a copy if the ScalarMap was on a different JVM) */
  public ScalarMap getScalarMap() {
    return map;
  }

  /** get the ID type of this event; legal ID's are
      ScalarMapEvent.AUTO_SCALE, ScalarMapEvent.MANUAL */
  public int getId() {
    return id;
  }

}

