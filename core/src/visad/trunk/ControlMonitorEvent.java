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

/**
 * <CODE>ControlMonitorEvent</CODE> is the VisAD class for
 * <CODE>Control</CODE>-related <CODE>Event</CODE>s
 * from display monitors.  They are sourced by <CODE>DisplayMonitor</CODE>
 * objects and received by <CODE>DisplayMonitorListener</CODE> objects.
 */
public class ControlMonitorEvent
  extends MonitorEvent
{
  Control ctl;

  /**
   * Creates a <CODE>ControlMonitorEvent</CODE> for the specified
   * <CODE>Control</CODE>.
   *
   * @param type The event type (either
   *			<CODE>MonitorEvent.CONTROL_INIT_REQUESTED</CODE>
   *			or <CODE>MonitorEvent.CONTROL_CHANGED</CODE>.)
   * @param ctl The <CODE>Control</CODE>.
   */
  public ControlMonitorEvent(int type, Control ctl)
    throws VisADException
  {
    this(type, -1, ctl);
  }

  /**
   * Creates a <CODE>ControlMonitorEvent</CODE> for the specified
   * <CODE>Control</CODE>.
   *
   * @param type The event type (either
   *			<CODE>MonitorEvent.CONTROL_INIT_REQUESTED</CODE>
   *			or <CODE>MonitorEvent.CONTROL_CHANGED</CODE>.)
   * @param originator The ID of the connection from which this event came,
   *                    relative to the receiver of the event.
   * @param ctl The <CODE>Control</CODE>.
   *
   * @exception VisADException When a bad <CODE>type</CODE> is specified.
   */
  public ControlMonitorEvent(int type, int originator, Control ctl)
    throws VisADException
  {
    super(type, originator);
    if (type != CONTROL_CHANGED && type != CONTROL_INIT_REQUESTED) {
      throw new VisADException("Bad type for ControlMonitorEvent");
    }
    this.ctl = ctl;
  }

  /**
   * Gets the <CODE>Control</CODE> to which this event refers.
   */
  public Control getControl()
  {
    return ctl;
  }

  /**
   * Returns <CODE>true</CODE> if the specified object matches this object.
   *
   * @param o The object to compare.
   */
  public boolean equals(Object o)
  {
    if (!(o instanceof ControlMonitorEvent)) {
      return false;
    }

    ControlMonitorEvent evt = (ControlMonitorEvent )o;
    if (getType() != evt.getType()) {
      return false;
    }

    return ctl.equals(evt.ctl);
  }

  /**
   * Returns an exact clone of this object.
   */
  public Object clone()
  {
    ControlMonitorEvent evt;
    try {
      evt = new ControlMonitorEvent(getType(), getOriginator(),
                                    (Control )ctl.clone());
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
    return "ControlMonitorEvent[" + getTypeName() + "," + getOriginator() +
      "," + ctl + "]";
  }
}
