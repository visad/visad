//
// WidgetEvent.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2000 Bill Hibbard, Curtis Rueden, Tom
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

package visad.browser;

import java.awt.Event;

/**
   WidgetEvent represents an Event regarding any of the various browser
   Widgets. They are sourced by Widget objects and received by WidgetListener
   objects.<P>
*/
public class WidgetEvent extends Event {

  // <<<< if you add more events, be sure to add them to the getID javadoc >>>

  /**
   * The "mouse pressed" event.  This event occurs when any
   * of the mouse buttons is pressed inside the display.  Other
   * MOUSE_PRESSED event positions (LEFT, CENTER, RIGHT) are based
   * on a right-handed mouse configuration.
   */
  public final static int MOUSE_PRESSED = 1;

  /** The "transform done" event. */
  public final static int TRANSFORM_DONE = 2;

  /** The "frame done" event. */
  public final static int FRAME_DONE = 3;

  /**
   * The "center mouse button pressed" event.  This event occurs when
   * the center mouse button is pressed inside the display.
   */
  public final static int MOUSE_PRESSED_CENTER = 4;

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

  /**
   * The "display destroyed" event.  This event occurs when
   * a display's destroy() method is called.
   */
  public final static int DESTROYED = 15;

  /** id number */
  private int id = 0;

  /** MouseEvent x position */
  private int mouse_x = 0;
  
  /** MouseEvent y position */
  private int mouse_y = 0;

  /** source of event */
  private Widget widget;

  /** whether WidgetEvent came from remote source */
  private boolean remote_source; 

  /**
   * Constructs a WidgetEvent object with the specified source widget,
   * and type of event.
   *
   * @param  w  widget that sends the event
   * @param  id_d  type of WidgetEvent that is sent
   */
  public WidgetEvent(Widget w, int id_d) {
    // don't pass widget as the source, since source is transient inside Event
    super(null, 0, null);
    widget = w;
    id = id_d;
  }

  /**
   * Constructs a WidgetEvent object with the specified source widget,
   * type of event, and mouse positions where event occurred.
   *
   * @param  d  display that sends the event
   * @param  id_d  type of WidgetEvent that is sent
   * @param  x  the horizontal x coordinate for the mouse location in
   *            the display component
   * @param  y  the vertical y coordinate for the mouse location in
   *            the display component
   */
  public WidgetEvent(Widget w, int id_d, int x, int y) {
    this(w, id_d, x, y, false);
  }

  /**
   * Constructs a WidgetEvent object with the specified source display,
   * type of event, mouse positions where event occurred, and
   * remote flag indicating whether event came from a remote source.
   *
   * @param  d  display that sends the event
   * @param  id_d  type of WidgetEvent that is sent
   * @param  x  the horizontal x coordinate for the mouse location in
   *            the display component
   * @param  y  the vertical y coordinate for the mouse location in
   *            the display component
   * @param  remote  true if this WidgetEvent came from a remote source
   */
  public WidgetEvent(Widget w, int id_d, int x, int y, boolean remote) {
    // don't pass widget as the source, since source is transient inside Event
    super(null, 0, null);
    widget = w;
    id = id_d;
    mouse_x = x;
    mouse_y = y;
    remote_source = remote;
  }

  /**
   * Get the ID type of this event
   *
   * @return  WidgetEvent type.  Valid types are:
   *          <UL>
   *          <LI>WidgetEvent.FRAME_DONE
   *          <LI>WidgetEvent.TRANSFORM_DONE
   *          <LI>WidgetEvent.MOUSE_PRESSED
   *          <LI>WidgetEvent.MOUSE_PRESSED_LEFT
   *          <LI>WidgetEvent.MOUSE_PRESSED_CENTER
   *          <LI>WidgetEvent.MOUSE_PRESSED_RIGHT
   *          <LI>WidgetEvent.MOUSE_RELEASED_LEFT
   *          <LI>WidgetEvent.MOUSE_RELEASED_CENTER
   *          <LI>WidgetEvent.MOUSE_RELEASED_RIGHT
   *          <LI>WidgetEvent.MAP_ADDED
   *          <LI>WidgetEvent.MAPS_CLEARED
   *          <LI>WidgetEvent.REFERENCE_ADDED
   *          <LI>WidgetEvent.REFERENCE_REMOVED
   *          <LI>WidgetEvent.DESTROYED
   *          </UL>
   */
  public int getId() {
    return id;
  }

  /**
   * Get the horizontal x coordinate for the mouse location.  Only valid
   * for MOUSE type events.
   *
   * @return  horizontal x coordinate for the mouse location in the widget
   */
  public int getX() {
    return mouse_x;
  }

  /**
   * Get the vertical y coordinate for the mouse location.  Only valid
   * for MOUSE type events.
   *
   * @return  vertical y coordinate for the mouse location in the widget
   */
  public int getY() {
    return mouse_y;
  }

  /**
   * Get whether the event came from a remote source.
   *
   * @return true if remote, false if local
   */
  public boolean isRemote() {
    return remote_source;
  }

}

