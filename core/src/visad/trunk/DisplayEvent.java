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

  // <<<< if you add more events, be sure to add them to the getID javadoc >>>

  /** 
   * The "mouse pressed" event.  This event occurs when any
   * of the mouse buttons is pressed inside the display.  Other
   * MOUSE_PRESSED event positions (LEFT, CENTER, RIGHT) are based 
   * on a right-handed mouse configuration.
   */
  public final static int MOUSE_PRESSED = 1;

  /* WLH 28 Oct 98 */
  /** The "transform done" event. */
  public final static int TRANSFORM_DONE = 2;

  /* WLH 15 March 99 */
  /** The "frame done" event. */
  public final static int FRAME_DONE = 3;

  /** 
   * The "center mouse button pressed" event.  This event occurs when 
   * the center mouse button is pressed inside the display.
   */
  public final static int MOUSE_PRESSED_CENTER = 4;

  /* WLH 19 Jul 1999 */
  /** 
   * The "left mouse button pressed" event.  This event occurs when 
   * the left mouse button is pressed inside the display.
   */
  public final static int MOUSE_PRESSED_LEFT = 5;

  /** 
   * The "right mouse button pressed" event.  This event occurs when 
   * the right mouse button is pressed inside the display.
   */
  public final static int MOUSE_PRESSED_RIGHT = 6;

  /* DRM 17 Sep 1999 */
  /** 
   * The "mouse released" event.  This event occurs when any
   * of the mouse buttons is released after one of the MOUSE_PRESSED
   * events. Other MOUSE_RELEASED event positions (LEFT, CENTER, RIGHT) 
   * are based on a right-handed mouse configuration.
   */
  public final static int MOUSE_RELEASED = 7;

  /** 
   * The "center mouse button released" event.  This event occurs when 
   * the center mouse button is released after a MOUSE_PRESSED or 
   * MOUSE_PRESSED_CENTER event.
   */
  public final static int MOUSE_RELEASED_CENTER = 8;

  /** 
   * The "left mouse button released" event.  This event occurs when 
   * the left mouse button is released after a MOUSE_PRESSED or 
   * MOUSE_PRESSED_LEFT event.
   */
  public final static int MOUSE_RELEASED_LEFT = 9;

  /** 
   * The "right mouse button released" event.  This event occurs when 
   * the right mouse button is released after a MOUSE_PRESSED or 
   * MOUSE_PRESSED_RIGHT event.
   */
  public final static int MOUSE_RELEASED_RIGHT = 10;

  /** 
   * The "map added" event.  This event occurs when 
   * a ScalarMap is added to the display.
   */
  public final static int MAP_ADDED = 11;

  /** 
   * The "maps cleared" event.  This event occurs when 
   * all ScalarMaps are removed from the display.
   */
  public final static int MAPS_CLEARED = 12;

  /** 
   * The "reference added" event.  This event occurs when 
   * a DataReference is added to the display.
   */
  public final static int REFERENCE_ADDED = 13;

  /** 
   * The "reference removed" event.  This event occurs when 
   * a DataReference is removed from the display.
   */
  public final static int REFERENCE_REMOVED = 14;

  private int id = 0;

  private int mouse_x = 0, mouse_y = 0; // MouseEvent position

  private Display display; // source of event

  /**
   * Constructs a DisplayEvent object with the specified source display, 
   * and type of event.
   *
   * @param  d  display that sends the event
   * @param  id_d  type of DisplayEvent that is sent
   */
  public DisplayEvent(Display d, int id_d) {
    // don't pass display as the source, since source
    // is transient inside Event
    super(null, 0, null);
    display = d;
    id = id_d;
  }

  /**
   * Constructs a DisplayEvent object with the specified source display, 
   * type of event, and mouse positions where event occurred.
   *
   * @param  d  display that sends the event
   * @param  id_d  type of DisplayEvent that is sent
   * @param  x  the horizontal x coordinate for the mouse location in 
   *            the display component
   * @param  y  the vertical y coordinate for the mouse location in 
   *            the display component
   */
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

  /** 
   * Get the ID type of this event 
   *
   * @return  DisplayEvent type.  Valid types are:
   *          <UL>
   *          <LI>DisplayEvent.FRAME_DONE
   *          <LI>DisplayEvent.TRANSFORM_DONE
   *          <LI>DisplayEvent.MOUSE_PRESSED
   *          <LI>DisplayEvent.MOUSE_PRESSED_LEFT
   *          <LI>DisplayEvent.MOUSE_PRESSED_CENTER
   *          <LI>DisplayEvent.MOUSE_PRESSED_RIGHT
   *          <LI>DisplayEvent.MOUSE_RELEASED_LEFT
   *          <LI>DisplayEvent.MOUSE_RELEASED_CENTER
   *          <LI>DisplayEvent.MOUSE_RELEASED_RIGHT
   *          <LI>DisplayEvent.MAP_ADDED
   *          <LI>DisplayEvent.MAPS_CLEARED
   *          <LI>DisplayEvent.REFERENCE_ADDED
   *          <LI>DisplayEvent.REFERENCE_REMOVED
   *          </UL>
   */
  public int getId() {
    return id;
  }

  /**
   * Get the horizontal x coordinate for the mouse location.  Only valid
   * for MOUSE type events.
   *
   * @return  horizontal x coordinate for the mouse location in
   *          the display component
   */
  public int getX() {
    return mouse_x;
  }

  /**
   * Get the vertical y coordinate for the mouse location.  Only valid
   * for MOUSE type events.
   *
   * @return  vertical y coordinate for the mouse location in 
   *          the display component
   */
  public int getY() {
    return mouse_y;
  }

}

