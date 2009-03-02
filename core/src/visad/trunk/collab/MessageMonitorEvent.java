/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2009 Bill Hibbard, Curtis Rueden, Tom
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

import java.rmi.RemoteException;

import visad.Data;
import visad.FieldImpl;
import visad.MessageEvent;
import visad.RemoteData;
import visad.RemoteFieldImpl;
import visad.VisADException;

/**
 * <CODE>MessageMonitorEvent</CODE> is the VisAD class for
 * <CODE>MessageEvent</CODE>-related events from display monitors.
 * They are sourced by <CODE>DisplayMonitor</CODE> objects and received by
 * <CODE>MonitorCallback</CODE> objects.
 */
public class MessageMonitorEvent
  extends MonitorEvent
{
  private int id;
  private String str;
  private RemoteData data;

  /**
   * Creates a <CODE>MessageMonitorEvent</CODE> for the specified
   * <CODE>MessageEvent</CODE>.
   *
   * @param msg the <CODE>MessageEvent</CODE>.
   *
   * @exception VisADException When a bad <CODE>type</CODE> is specified.
   */
  public MessageMonitorEvent(MessageEvent msg)
    throws RemoteException, VisADException
  {
    this(MESSAGE_SENT, -1, msg);
  }

  /**
   * Creates a <CODE>MessageMonitorEvent</CODE> for the specified
   * <CODE>MessageEvent</CODE>.
   *
   * @param type The event type (currently only
   *                             <CODE>MonitorEvent.MESSAGE_SENT</CODE>.)
   * @param msg the <CODE>MessageEvent</CODE>.
   *
   * @exception VisADException When a bad <CODE>type</CODE> is specified.
   */
  public MessageMonitorEvent(int type, MessageEvent msg)
    throws RemoteException, VisADException
  {
    this(type, -1, msg);
  }

  /**
   * Creates a <CODE>MessageMonitorEvent</CODE> for the specified
   * <CODE>MessageEvent</CODE>.
   *
   * @param type The event type (currently only
   *                             <CODE>MonitorEvent.MESSAGE_SENT</CODE>.)
   * @param originator The ID of the connection from which this event came,
   * 			relative to the receiver of the event.
   * @param msg the <CODE>MessageEvent</CODE>.
   *
   * @exception VisADException When a bad <CODE>type</CODE> is specified.
   */
  public MessageMonitorEvent(int type, int originator, MessageEvent msg)
    throws RemoteException, VisADException
  {
    super(type, originator);
    if (type != MESSAGE_SENT) {
      throw new VisADException("Bad type for MessageMonitorEvent");
    }

    this.id = msg.getId();
    this.str = msg.getString();

    Data data = msg.getData();
    if (data == null) {
      this.data = null;
    } else if (data instanceof RemoteData) {
      this.data = (RemoteData )data;
    } else if (data instanceof FieldImpl) {
      this.data = new RemoteFieldImpl((FieldImpl )data);
/*
    } else if (data instanceof FunctionImpl) {
      this.data = new RemoteFunctionImpl((FunctionImpl )data);
    } else if (data instanceof DataImpl) {
      this.data = new RemoteDataImpl((DataImpl )data);
*/
    } else {
      throw new VisADException("Don't know how to make " +
                               data.getClass().getName() + " remote!");
    }
  }

  /**
   * Get the key used to uniquely identify this event.
   *
   * @return The unique key.
   */
  public String getKey()
  {
    return Integer.toString(id) + str +
      (data == null ? "null" : data.toString());
  }

  /**
   * Gets the <CODE>ScalarMap</CODE> to which this event refers.
   */
  public MessageEvent getMessage()
  {
    return new MessageEvent(id, getOriginator(), str, data);
  }

  /**
   * Returns <CODE>true</CODE> if the specified object matches this object.
   *
   * @param o The object to compare.
   */
  public boolean equals(Object o)
  {
    if (!(o instanceof MessageMonitorEvent)) {
      return false;
    }

    MessageMonitorEvent evt = (MessageMonitorEvent )o;
    if (getType() != evt.getType()) {
      return false;
    }

    if (id != evt.id) {
      return false;
    }

    if (str == null) {
      if (evt.str != null) {
        return false;
      }
    } else if (evt.str == null) {
      return false;
    } else if (!str.equals(evt.str)) {
      return false;
    }

    if (data == null) {
      if (evt.data != null) {
        return false;
      }
    } else if (evt.data == null) {
      return false;
    } else if (!data.equals(evt.data)) {
      return false;
    }

    return true;
  }

  /**
   * Returns an exact clone of this object.
   */
  public Object clone()
  {
    MessageMonitorEvent evt;
/*
    try {
      evt = new MessageMonitorEvent(getType(), getOriginator(),
                                    id, str, data);
      evt.seqNum = seqNum;
    } catch (VisADException e) {
      evt = null;
    }
*/
    evt = null;
    return evt;
  }

  /**
   * Returns a <CODE>String</CODE> representation of this object.
   */
  public String toString()
  {
    StringBuffer buf = new StringBuffer("MessageMonitorEvent[");
buf.append('#');buf.append(getSequenceNumber());buf.append(' ');

    buf.append(getTypeName());

    int orig = getOriginator();
    if (orig == -1) {
      buf.append(" Lcl");
    } else {
      buf.append(" Rmt ");
      buf.append(orig);
    }

    buf.append(' ');
    buf.append(id);

    buf.append(' ');
    buf.append(str);

    if (data == null) {
      buf.append(" <null>");
    } else {
      buf.append(' ');
      buf.append(data.toString());
    }

    buf.append(']');
    return buf.toString();
  }
}
