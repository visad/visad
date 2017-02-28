//
// ScalarMapControlEvent.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2017 Bill Hibbard, Curtis Rueden, Tom
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
 * <CODE>ScalarMapControlEvent</CODE> is the VisAD class for control-related
 * <CODE>Event</CODE>s from <CODE>ScalarMap</CODE> objects.  They are
 * sourced by <CODE>ScalarMap</CODE> objects and received by
 * <CODE>ScalarMapListener</CODE> objects.<P>
 */
public class ScalarMapControlEvent
  extends ScalarMapEvent
{
  private Control control;

  /**
   * Create a control-related <CODE>ScalarMap</CODE> event
   *
   * @param map the map to which this event refers
   * @param id the event type.
   * @param ctl the control.
   */
  public ScalarMapControlEvent(ScalarMap map, int id, Control ctl)
  {
    // don't pass map as the source, since source
    // is transient inside Event
    super(map, id);
    this.control = ctl;
  }

  /**
   * Get the <CODE>Control</CODE> referred to by this event.
   *
   * @return the <CODE>Control</CODE>
   */
  public Control getControl()
  {
    return control;
  }
  public String toString()
  {
    StringBuffer buf = new StringBuffer("ScalarMapControlEvent[");
    buf.append(getIdString());
    buf.append(", ");
    buf.append(getMapString());
    buf.append(", ");
    buf.append(control);
    buf.append(']');
    return buf.toString();
  }
}
