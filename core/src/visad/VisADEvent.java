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

import java.awt.Event;

/**
 * <CODE>VisADEvent</CODE> is the VisAD class for <CODE>Event</CODE>s
 * passed between <CODE>Display</CODE>s.
 */
public class VisADEvent
  extends Event
{
  public static final int LOCAL_SOURCE = 0;
  public static final int UNKNOWN_REMOTE_SOURCE = -1;

  /** non-zero if this event came from a remote source */
  private int remoteId; 

  public VisADEvent(Object target, int id, Object arg, int remoteId)
  {
    super(target, id, arg);
    this.remoteId = remoteId;
  }

  /**
   * Get the remote event source id.
   *
   * @return the remote id (or 0 if this was a local event)
   */
  public int getRemoteId()
  {
    return remoteId;
  }

  /**
   * Get whether the event came from a remote source.
   *
   * @return true if remote, false if local
   */
  public boolean isRemote()
  {
    return remoteId != LOCAL_SOURCE;
  }
}
