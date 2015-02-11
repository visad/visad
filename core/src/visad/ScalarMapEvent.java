//
// ScalarMapEvent.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2015 Bill Hibbard, Curtis Rueden, Tom
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

/**
 * <CODE>ScalarMapEvent</CODE> is the VisAD class for <CODE>Event</CODE>s
 * from <CODE>ScalarMap</CODE> objects.  They are
 * sourced by <CODE>ScalarMap</CODE> objects and received by
 * <CODE>ScalarMapListener</CODE> objects.<P>
 */
public class ScalarMapEvent extends VisADEvent {

  /** values for id */
  public final static int UNKNOWN = 0;
  public final static int AUTO_SCALE = 1;
  public final static int MANUAL = 2;
  public final static int CONTROL_ADDED = 3;
  public final static int CONTROL_REMOVED = 4;
  public final static int CONTROL_REPLACED = 5;

  private int id = UNKNOWN;

  private ScalarMap map; // source of event

  /**
   * Create a <CODE>ScalarMap</CODE> event
   *
   * @param map map to which this event refers
   * @param id the event type.
   */
  public ScalarMapEvent(ScalarMap map, int id)
  {
    this(map, id, LOCAL_SOURCE);
  }

  /**
   * Create a <CODE>ScalarMap</CODE> event
   *
   * @param map map to which this event refers
   * @param id the event type.
   */
  public ScalarMapEvent(ScalarMap map, int id, int remoteId)
  {
    // don't pass map as the source, since source
    // is transient inside Event
    super(null, 0, null, remoteId);
    this.map = map;
    this.id = id;
  }

  /**
   * Create a <CODE>ScalarMap</CODE> event
   *
   * @param map map to which this event refers
   * @param auto <CODE>true</CODE> if this is an AUTO_SCALE event
   *             <CODE>false</CODE> if it's a MANUAL event.
   *
   * @deprecated - Explicitly cite the event ID using the
   *               <CODE>ScalarMapEvent(ScalarMap map, int id)</CODE>
   *               constructor.
   */
  public ScalarMapEvent(ScalarMap map, boolean auto)
  {
    // don't pass map as the source, since source
    // is transient inside Event
    super(null, 0, null, LOCAL_SOURCE);
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
   *           <LI>ScalarMapEvent.CONTROL_ADDED
   *           <LI>ScalarMapEvent.CONTROL_REMOVED
   *           <LI>ScalarMapEvent.CONTROL_REPLACED
   *         </UL>
   */
  public int getId()
  {
    return id;
  }

  /**
   * Get the ID type of this event as a String
   *
   * @return The event type string.
   */
  public String getIdString()
  {
    switch (id) {
    case AUTO_SCALE: return "AUTO_SCALE";
    case MANUAL: return "MANUAL";
    case CONTROL_ADDED: return "CONTROL_ADDED";
    case CONTROL_REMOVED: return "CONTROL_REMOVED";
    case CONTROL_REPLACED: return "CONTROL_REPLACED";
    }

    return "UNKNOWN_ID=" + id;
  }

  /**
   * Get a one-line description of the <CODE>ScalarMap</CODE> which
   * originated this event.
   *
   * @return the one-line map description.
   */
  String getMapString()
  {
    StringBuffer buf = new StringBuffer();
    if (map instanceof ConstantMap) {
      buf.append(((ConstantMap )map).getConstant());
    } else {
      buf.append(map.getScalar());
    }
    buf.append("->");
    buf.append(map.getDisplayScalar());
    return buf.toString();
  }

  public String toString()
  {
    StringBuffer buf = new StringBuffer("ScalarMapEvent[");
    buf.append(getIdString());
    buf.append(", ");
    buf.append(getMapString());
    buf.append(']');
    return buf.toString();
  }
}
