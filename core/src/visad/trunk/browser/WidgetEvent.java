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
 * WidgetEvent represents an Event regarding any of the various browser
 * Widgets. They are sourced by Widget objects and received by
 * WidgetListener objects.
 */
public class WidgetEvent extends Event {

  /**
   * The "widget created" event occurs when a new widget has been created.
   */
  public static final int CREATED = 1;

  /**
   * The "widget destroyed" event occurs when a widget is no longer needed
   * and gets destroyed.
   */
  public static final int DESTROYED = 2;

  /**
   * The "widget updated" event is a general-purpose event indicating that
   * some kind of change has occurred to one of the widget's values.
   * The exact nature of the change is dependent on the widget type,
   * and can be determined from the getField() and getValue() methods.
   */
  public static final int UPDATED = 3;

  /**
   * Source of event.
   */
  private Widget widget;

  /**
   * Type of event.
   */
  private int id = 0;

  /**
   * Field name used by UPDATED event.
   */
  private String field;

  /**
   * Value used by CREATED and UPDATED events.
   */
  private String value;

  /**
   * Whether WidgetEvent came from remote source.
   */
  private boolean remote_source; 

  /**
   * Constructs a WidgetEvent object of the given type with the
   * specified new value v for field f.
   *
   * @param w  widget that sends the event
   * @param id_d  type of WidgetEvent that is sent
   * @param f  the name of the field that has changed
   * @param v  the new value of the field that has changed
   */
  public WidgetEvent(Widget w, int id_d, String f, String v) {
    this(w, id_d, f, v, false);
  }

  /**
   * Constructs a WidgetEvent object of the given type with the
   * specified value v for field f, and remote flag indicating
   * whether the event came from a remote source.
   *
   * @param w  widget that sends the event
   * @param id_d  type of WidgetEvent that is sent
   * @param f  the name of the field that has changed
   * @param v  the new value of the field that has changed
   * @param remote  true if this WidgetEvent came from a remote source
   */
  public WidgetEvent(Widget w, int id_d, String f, String v, boolean remote) {
    // don't pass widget as the source, since source is transient inside Event
    super(null, 0, null);
    widget = w;
    id = id_d;
    field = f;
    value = v;
    remote_source = remote;
  }

  /**
   * Get the ID type of this event.
   *
   * @return WidgetEvent type. Valid types are:
   *         <UL>
   *         <LI>WidgetEvent.CREATED
   *         <LI>WidgetEvent.DESTROYED
   *         <LI>WidgetEvent.UPDATED
   *         </UL>
   */
  public int getId() {
    return id;
  }

  /**
   * Get the widget field name. Only valid for UPDATED events.
   *
   * @return name of the widget field that has changed.
   */
  public String getField() {
    return field;
  }

  /**
   * Get the widget field's new value. For CREATED events, returns a string
   * with information needed to contruct the widget's initial state.
   * Not valid for DESTROYED events.
   *
   * @return new value of the widget field that has changed.
   */
  public String getValue() {
    return value;
  }

  /**
   * Get whether the event came from a remote source.
   *
   * @return true if remote, false if local.
   */
  public boolean isRemote() {
    return remote_source;
  }

}
