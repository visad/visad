 
//
// DisplayEvent.java
//
 
 /*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 1998 Bill Hibbard, Curtis Rueden, Tom
Rink and Dave Glowacki.
 
This program is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 1, or (at your option)
any later version.
 
This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License in file NOTICE for more details.
 
You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
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
  /* TDR 1 Nov 98 */
  //- Center mouse button pressed
  public final static int MOUSE_PRESSED_CENTER = 3;

  private int id = 0;

  private Display display; // source of event

  public DisplayEvent(Display d, int id_d) {
    // don't pass display as the source, since source
    // is transient inside Event
    super(null, 0, null);
    display = d;
    id = id_d;
  }

  public Display getDisplay() {
    return display;
  }

  public int getId() {
    return id;
  }

}

