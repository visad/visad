//
// ScalarMapEvent.java
//
 
 /*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 1999 Bill Hibbard, Curtis Rueden, Tom
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

