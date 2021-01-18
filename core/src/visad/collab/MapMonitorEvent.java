/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2021 Bill Hibbard, Curtis Rueden, Tom
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

package visad.collab;

import visad.ConstantMap;
import visad.ScalarMap;
import visad.VisADException;

/**
 * <CODE>MapMonitorEvent</CODE> is the VisAD class for
 * <CODE>ScalarMap</CODE>-related events from display monitors.
 * They are sourced by <CODE>DisplayMonitor</CODE> objects and received by
 * <CODE>MonitorCallback</CODE> objects.
 */
public class MapMonitorEvent
  extends MonitorEvent
{
  private ScalarMap map;

  /**
   * Creates a <CODE>MapMonitorEvent</CODE> for the specified
   * <CODE>ScalarMap</CODE>.
   *
   * @param type The event type (either <CODE>MonitorEvent.MAP_ADDED</CODE>,
   * 			<CODE>MonitorEvent.MAP_REMOVED</CODE>, or
   * 			<CODE>MonitorEvent.MAP_CHANGED</CODE>, or
   * 			<CODE>MonitorEvent.MAPS_CLEARED</CODE>.)
   * @param map the <CODE>ScalarMap</CODE> (or <CODE>ConstantMap</CODE>).
   *
   * @exception VisADException When a bad <CODE>type</CODE> is specified.
   */
  public MapMonitorEvent(int type, ScalarMap map)
    throws VisADException
  {
    this(type, -1, map);
  }

  /**
   * Creates a <CODE>MapMonitorEvent</CODE> for the specified
   * <CODE>ScalarMap</CODE>.
   *
   * @param type The event type (either <CODE>MonitorEvent.MAP_ADDED</CODE>,
   * 			<CODE>MonitorEvent.MAP_REMOVED</CODE>, or
   * 			<CODE>MonitorEvent.MAP_CHANGED</CODE>, or
   * 			<CODE>MonitorEvent.MAPS_CLEARED</CODE>.)
   * @param originator The ID of the connection from which this event came,
   * 			relative to the receiver of the event.
   * @param map the <CODE>ScalarMap</CODE> (or <CODE>ConstantMap</CODE>).
   *
   * @exception VisADException When a bad <CODE>type</CODE> is specified.
   */
  public MapMonitorEvent(int type, int originator, ScalarMap map)
    throws VisADException
  {
    super(type, originator);
    if (type != MAP_ADDED && type != MAP_CHANGED && type != MAPS_CLEARED &&
        type != MAP_REMOVED) {
      throw new VisADException("Bad type " + type);
    }
    if (map == null && type != MAPS_CLEARED) {
      throw new VisADException("Null map");
    }
    this.map = map;
  }

  /**
   * Get the key used to uniquely identify this event.
   *
   * @return The unique key.
   */
  public String getKey()
  {
    String key;

    if (type == MonitorEvent.MAPS_CLEARED) {
      key = "MAPS_CLEARED";
    } else {
      key = map.toString();
      switch (type) {
      case MonitorEvent.MAP_ADDED:
        key = "ADD " + key;
        break;
      case MonitorEvent.MAP_REMOVED:
        key = "RMV " + key;
        break;
      case MonitorEvent.MAP_CHANGED:
        key = "CHG " + key;
        break;
      default:
        System.err.println("MapMonitorEvent type " + type +
                           " not handled by getKey()");
        break;
      }
    }

    return key;
  }

  /**
   * Gets the <CODE>ScalarMap</CODE> to which this event refers.
   */
  public ScalarMap getMap()
  {
    return map;
  }

  /**
   * Returns <CODE>true</CODE> if the specified object matches this object.
   *
   * @param o The object to compare.
   */
  public boolean equals(Object o)
  {
    if (!(o instanceof MapMonitorEvent)) {
      return false;
    }

    MapMonitorEvent evt = (MapMonitorEvent )o;
    if (getType() != evt.getType()) {
      return false;
    }

    if (map == null) {
      if (evt.map != null) {
        return false;
      }
    } else if (evt.map == null) {
      return false;
    } else if (!map.equals(evt.map)) {
      return false;
    }

    return true;
  }

  /**
   * Returns an exact clone of this object.
   */
  public Object clone()
  {
    MapMonitorEvent evt;
    try {
      evt = new MapMonitorEvent(getType(), getOriginator(),
                                (map == null ? null :
                                 (ScalarMap )map.clone()));
      evt.seqNum = seqNum;
    } catch (VisADException e) {
      evt = null;
    }
    return evt;
  }

  /**
   * Returns a <CODE>String</CODE> representation of this object.
   */
  public String toString()
  {
    StringBuffer buf = new StringBuffer("MapMonitorEvent[");
buf.append('#');buf.append(getSequenceNumber());buf.append(' ');

    buf.append(getTypeName());

    int orig = getOriginator();
    if (orig == -1) {
      buf.append(" Lcl");
    } else {
      buf.append(" Rmt ");
      buf.append(orig);
    }

    if (map == null) {
      buf.append(" <null>");
    } else if (map instanceof ConstantMap) {
      buf.append(' ');
      buf.append(((ConstantMap )map).getConstant());
      buf.append(" -> ");
      buf.append(map.getDisplayScalar());
    } else {
      buf.append(' ');
      buf.append(map.getScalar());
      buf.append(" -> ");
      buf.append(map.getDisplayScalar());
    }

    buf.append(']');
    return buf.toString();
  }
}
