//
// DisplayMapEvent.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2006 Bill Hibbard, Curtis Rueden, Tom
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
   DisplayMapEvent is the VisAD class for Events from Display
   ScalarMap and ConstantMap objects.  They are sourced by Display
   objects and received by DisplayListener objects.<P>
*/
public class DisplayMapEvent extends DisplayEvent {

  private ScalarMap map;

  /**
   * Constructs a DisplayMapEvent object with the specified
   * source display, type of event, and ScalarMap.
   *
   * @param  d  display that sends the event
   * @param  id  type of DisplayMapEvent that is sent
   * @param m ScalarMap referenced by this event
   */
  public DisplayMapEvent(Display d, int id, ScalarMap m) {
    this(d, id, m, LOCAL_SOURCE);
  }

  /**
   * Constructs a DisplayMapEvent object with the specified
   * source display, type of event, and ScalarMap.
   *
   * @param  d  display that sends the event
   * @param  id  type of DisplayMapEvent that is sent
   * @param  m  ScalarMap referenced by this event
   * @param  remoteId  ID of remote source
   */
  public DisplayMapEvent(Display d, int id, ScalarMap m, int remoteId) {
    super(d, id, remoteId);
    map = m;
  }

  /**
   * Return a new DisplayMapEvent which is a copy of this event,
   * but which uses the specified source display
   *
   * @param dpy Display to use for the new DisplayMapEvent
   * @return a clone of this, except with the given source Display
   */
  public DisplayEvent cloneButDisplay(Display dpy)
  {
    return new DisplayMapEvent(dpy, getId(), map, getRemoteId());
  }

  /** get the ScalarMap referenced by this DisplayMapEvent */
  public ScalarMap getMap() {
    return map;
  }

}
