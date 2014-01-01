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

import visad.RemoteReferenceLink;
import visad.VisADException;

/**
 * <CODE>ReferenceMonitorEvent</CODE> is the VisAD class for
 * <CODE>RemoteReferenceLink</CODE>-related events from display monitors.
 * They are sourced by <CODE>DisplayMonitor</CODE> objects and received by
 * <CODE>MonitorCallback</CODE> objects.
 */
public class ReferenceMonitorEvent
  extends MonitorEvent
{
  private RemoteReferenceLink link;

  /**
   * Creates a <CODE>ReferenceMonitorEvent</CODE> for the specified
   * <CODE>RemoteReferenceLink</CODE>.
   *
   * @param type The event type (either
   * 			<CODE>MonitorEvent.REFERENCE_ADDED</CODE> or
   * 			<CODE>MonitorEvent.REFERENCE_REMOVED</CODE>.)
   * @param link The <CODE>RemoteReferenceLink</CODE>.
   *
   * @exception VisADException When a bad <CODE>type</CODE> is specified.
   */
  public ReferenceMonitorEvent(int type, RemoteReferenceLink link)
    throws VisADException
  {
    this(type, -1, link);
  }

  /**
   * Creates a <CODE>ReferenceMonitorEvent</CODE> for the specified
   * <CODE>RemoteReferenceLink</CODE>.
   *
   * @param type The event type (either
   * 			<CODE>MonitorEvent.REFERENCE_ADDED</CODE> or
   * 			<CODE>MonitorEvent.REFERENCE_REMOVED</CODE>.)
   * @param originator The ID of the connection from which this event came,
   * 			relative to the receiver of the event.
   * @param link The <CODE>RemoteReferenceLink</CODE>.
   *
   * @exception VisADException When a bad <CODE>type</CODE> is specified.
   */
  public ReferenceMonitorEvent(int type, int originator,
                               RemoteReferenceLink link)
    throws VisADException
  {
    super(type, originator);
    if (type != REFERENCE_ADDED && type != REFERENCE_REMOVED) {
      throw new VisADException("Bad type for ReferenceMonitorEvent");
    }
    if (link == null) {
      throw new VisADException("Null link for ReferenceMonitorEvent");
    }
    this.link = link;
  }

  /**
   * Get the key used to uniquely identify this event.
   *
   * @return The unique key.
   */
  public String getKey()
  {
    return link.toString();
  }

  /**
   * Gets the <CODE>RemoteReferenceLink</CODE> to which this event refers.
   */
  public RemoteReferenceLink getLink()
  {
    return link;
  }

  /**
   * Returns <CODE>true</CODE> if the specified object matches this object.
   *
   * @param o The object to compare.
   */
  public boolean equals(Object o)
  {
    if (!(o instanceof ReferenceMonitorEvent)) {
      return false;
    }

    ReferenceMonitorEvent evt = (ReferenceMonitorEvent )o;
    if (getType() != evt.getType()) {
      return false;
    }

    return link.equals(evt.link);
  }

  /**
   * Returns an exact clone of this object.
   */
  public Object clone()
  {
    ReferenceMonitorEvent evt;
    try {
      evt = new ReferenceMonitorEvent(getType(), getOriginator(), link);
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
    StringBuffer buf = new StringBuffer("ReferenceMonitorEvent[");
buf.append('#');buf.append(getSequenceNumber());buf.append(' ');

    buf.append(getTypeName());

    int orig = getOriginator();
    if (orig == -1) {
      buf.append(" Lcl ");
    } else {
      buf.append(" Rmt ");
      buf.append(orig);
    }

    buf.append(' ');
    buf.append(link);

    buf.append(']');
    return buf.toString();
  }
}
