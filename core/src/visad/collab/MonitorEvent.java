/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2014 Bill Hibbard, Curtis Rueden, Tom
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

/**
 * <CODE>MonitorEvent</CODE> is the VisAD superclass for events from
 * display monitors.<P>
 * They are sourced by <CODE>DisplayMonitor</CODE> objects and received by
 * <CODE>MonitorCallback</CODE> objects.
 */
public abstract class MonitorEvent
  implements java.io.Serializable
{
  /**
   * This event occurs whenever a <CODE>ConstantMap</CODE> or
   * <CODE>ScalarMap</CODE> is added to a <CODE>Display</CODE>.
   */
  public static final int MAP_ADDED = 1;
  /**
   * This event occurs whenever a <CODE>ConstantMap</CODE> or
   * <CODE>ScalarMap</CODE> is added to a <CODE>Display</CODE>.
   */
  public static final int MAP_CHANGED = 2;
  /**
   * This event occurs whenever the <CODE>ConstantMap</CODE>s and
   * <CODE>ScalarMap</CODE>s are cleared from a <CODE>Display</CODE>.
   */
  public static final int MAPS_CLEARED = 3;
  /**
   * This event occurs whenever a <CODE>ConstantMap</CODE> or
   * <CODE>ScalarMap</CODE> is removed from a <CODE>Display</CODE>.
   */
  public static final int MAP_REMOVED = 4;

  /**
   * This event occurs whenever a <CODE>DataReference</CODE> is added to
   * a <CODE>Display</CODE>.
   */
  public static final int REFERENCE_ADDED = 10;
  /**
   * This event occurs whenever a <CODE>DataReference</CODE> is removed
   * from a <CODE>Display</CODE>.
   */
  public static final int REFERENCE_REMOVED = 11;

  /**
   * This event occurs whenever a <CODE>Control</CODE> attached to a
   * <CODE>Display</CODE> requests that it be initialized to the state of
   * a remote <CODE>Control</CODE>.
   */
  public static final int CONTROL_INIT_REQUESTED = 20;
  /**
   * This event occurs whenever the state of a <CODE>Control</CODE> attached
   * to a <CODE>Display</CODE> is changed.
   */
  public static final int CONTROL_CHANGED = 21;

  /**
   * This event occurs whenever a message is sent.
   */
  public static final int MESSAGE_SENT = 22;

  // these two variables provide a unique number for each event
  private static int nextSeqNum = 0;
  protected int seqNum = nextSeqNum++;

  // the MonitorEvent type
  protected int type;

  // the originator of this MonitorEvent
  // (relative to the receiving DisplayMonitor)
  private int originator;

  /**
   * Creates a <CODE>MonitorEvent</CODE>
   *
   * @param type The event type.
   * @param originator The ID of the connection from which this event came,
   *                   relative to the receiver of the event.
   */
  public MonitorEvent(int type, int originator) {
    this.type = type;
    this.originator = originator;
  }

  /**
   * Gets the type of this <CODE>MonitorEvent</CODE>.
   */
  public int getType()
  {
    return type;
  }

  /**
   * Get the key used to uniquely identify this event.
   *
   * @return The unique key.
   */
  public abstract String getKey();

  /**
   * Gets the originator of this <CODE>MonitorEvent</CODE>.
   */
  public int getOriginator()
  {
    return originator;
  }

  /**
   * Gets the sequence number of this <CODE>MonitorEvent</CODE>.
   */
  public int getSequenceNumber() {
    return seqNum;
  }

  /**
   * Returns a <CODE>String</CODE> description of the
   * specified <CODE>MonitorEvent</CODE> type.
   *
   * @param type the <CODE>MonitorEvent</CODE> type.
   *
   * @return name of the specified type.
   */
  public static String getTypeName(int type)
  {
    switch (type) {
    case MAP_ADDED: return "MAP_ADDED";
    case MAP_REMOVED: return "MAP_REMOVED";
    case MAP_CHANGED: return "MAP_CHANGED";
    case MAPS_CLEARED: return "MAPS_CLEARED";
    case REFERENCE_ADDED: return "REFERENCE_ADDED";
    case REFERENCE_REMOVED: return "REFERENCE_REMOVED";
    case CONTROL_INIT_REQUESTED: return "CONTROL_INIT_REQUESTED";
    case CONTROL_CHANGED: return "CONTROL_CHANGED";
    }
    return "Unknown MonitorEvent Type #" + type;
  }

  /**
   * Returns a <CODE>String</CODE> description of this
   * <CODE>MonitorEvent</CODE>'s type.
   *
   * @return name of this event's type.
   */
  public String getTypeName()
  {
    return getTypeName(type);
  }

  /**
   * Sets the originator of this <CODE>MonitorEvent</CODE>.
   *
   *  @param id The ID of the connection from which this event came,
   *            relative to the receiver of the event.
   */
  public void setOriginator(int id)
  {
    originator = id;
  }

  /**
   * Returns an exact copy of this <CODE>MonitorEvent</CODE>.
   */
  public abstract Object clone();

  /**
   * Returns a <CODE>String</CODE> representation of this object.
   */
  public String toString()
  {
    StringBuffer buf = new StringBuffer("MonitorEvent[");
buf.append('#');buf.append(getSequenceNumber());buf.append(' ');

    buf.append(getTypeName());

    if (originator == -1) {
      buf.append(" Lcl");
    } else {
      buf.append(" Rmt ");
      buf.append(originator);
    }

    buf.append(']');
    return buf.toString();
  }
}
