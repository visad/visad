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
 * <CODE>ScalarMapEvent</CODE> is the VisAD class for <CODE>Event</CODE>s
 * from <CODE>ScalarMap</CODE> objects.  They are
 * sourced by <CODE>ScalarMap</CODE> objects and received by
 * <CODE>ScalarMapListener</CODE> objects.<P>
 */
public class ScalarMapEvent extends Event {

  /** values for id */
  public final static int UNKNOWN = 0;
  public final static int AUTO_SCALE = 1;
  public final static int MANUAL = 2;

  private int id = UNKNOWN;

  private ScalarMap map; // source of event

  /**
   * Create a <CODE>ScalarMap</CODE> event
   *
   * @param map map to which this event refers
   * @param auto <CODE>true</CODE> if this is an AUTO_SCALE event
   *             <CODE>false</CODE> if it's a MANUAL event.
   */
  public ScalarMapEvent(ScalarMap map, boolean auto)
  {
    // don't pass map as the source, since source
    // is transient inside Event
    super(null, 0, null);
    this.map = map;
    this.id = auto ? AUTO_SCALE : MANUAL;
  }

  /**
   * Get the ScalarMap that sent this ScalarMapEvent (or
   * a copy if the ScalarMap was on a different JVM)
   *
   * @return the <CODE>ScalarMap</CODE>
   */
  public ScalarMap getScalarMap()
  {
    return map;
  }

  /**
   * Get the ID type of this event.
   *
   * @return <CODE>ScalarMapEvent</CODE> type.  Valid types are:
   *         <UL>
   *           <LI>ScalarMapEvent.AUTO_SCALE
   *           <LI>ScalarMapEvent.MANUAL
   *         </UL>
   */
  public int getId()
  {
    return id;
  }

}

