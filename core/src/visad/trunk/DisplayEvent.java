//
// DisplayEvent.java
//
 
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
 
import java.rmi.*;
import java.awt.Event;

/**
   DisplayEvent is the VisAD class for Events from Display
   objects.  They are sourced by Display objects and
   received by DisplayListener objects.<P>
*/
public class DisplayEvent extends Event {

  /** values for id */
  public final static int MOUSE_PRESSED = 1;
  /* WLH 28 Oct 98 */
  public final static int TRANSFORM_DONE = 2;
  /* WLH 15 March 99 */
  public final static int FRAME_DONE = 3;
  /* TDR 1 Nov 98 */
  //- Center mouse button pressed
  public final static int MOUSE_PRESSED_CENTER = 4;
  //- Left mouse button pressed
  public final static int MOUSE_PRESSED_LEFT = 5;
  //- Right mouse button pressed
  public final static int MOUSE_PRESSED_RIGHT = 6;

  private int id = 0;

  private int mouse_x = 0, mouse_y = 0; // MouseEvent position

  private Display display; // source of event

  public DisplayEvent(Display d, int id_d) {
    // don't pass display as the source, since source
    // is transient inside Event
    super(null, 0, null);
    display = d;
    id = id_d;
  }

  public DisplayEvent(Display d, int id_d, int x, int y) {
    // don't pass display as the source, since source
    // is transient inside Event
    super(null, 0, null);
    display = d;
    id = id_d;
    mouse_x = x;
    mouse_y = y;
  }

  /** get the DisplayImpl that sent this DisplayEvent (or
      a RemoteDisplay reference to it if the Display was on
      a different JVM) */
  public Display getDisplay() {
    return display;
  }

  /** get the ID type of this event; legal ID's are
      DisplayEvent.MOUSE_PRESSED, DisplayEvent.TRANSFORM_DONE
      DisplayEvent.MOUSE_PRESSED_CENTER, DisplayEvent.FRAME_DONE */
  public int getId() {
    return id;
  }

  public int getX() {
    return mouse_x;
  }

  public int getY() {
    return mouse_y;
  }

}

