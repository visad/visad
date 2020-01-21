//
// DisplayReferenceEvent.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2020 Bill Hibbard, Curtis Rueden, Tom
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
   DisplayReferenceEvent is the VisAD class for Events from Display
   DataReference obkects.  They are sourced by Display objects and
   received by DisplayListener objects.<P>
*/
public class DisplayReferenceEvent extends DisplayEvent {

  private DataDisplayLink link;

  /**
   * Constructs a DisplayReferenceEvent object with the specified
   * source display, type of event, and DataReference connection.
   *
   * @param  d  display that sends the event
   * @param  id  type of DisplayReferenceEvent that is sent
   * @param link DataReference link
   */
  public DisplayReferenceEvent(Display d, int id, DataDisplayLink link) {
    super(d, id);
    this.link = link;
  }

  /**
   * Return a new DisplayReferenceEvent which is a copy of this event,
   * but which uses the specified source display
   *
   * @param dpy Display to use for the new DisplayReferenceEvent
   */
  public DisplayEvent cloneButDisplay(Display dpy)
  {
    return new DisplayReferenceEvent(dpy, getId(), link);
  }

  /**
   * @return the DataDisplayLink referenced by this
   */
  public DataDisplayLink getDataDisplayLink() {
    return link;
  }

}
