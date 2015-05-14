//
// ControlEvent.java
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
   ControlEvent is the VisAD class for changes in Control
   objects.  They are sourced by Control objects and
   received by ControlListener objects.<P>
*/
public class ControlEvent extends VisADEvent {

  private Control control; // source of event

  /**
   * construct a ControlEvent for Control c and LOCAL_SOURCE
   * @param c Control associated with this ControlEvent
   */
  public ControlEvent(Control c) {
    this(c, LOCAL_SOURCE);
  }

  /**
   * construct a ControlEvent for Control c and remoteId
   * @param c Control associated with this ControlEvent
   * @param remoteId ID for this ControlEvent
   */
  public ControlEvent(Control c, int remoteId) {
    // don't pass control as the source, since source
    // is transient inside Event
    super(null, 0, null, remoteId);
    control = c;
  }

  /**
   * @return the Control that sent this ControlEvent (or a copy
   *         if the Control was on a different JVM)
   */
  public Control getControl() {
    return control;
  }

}

