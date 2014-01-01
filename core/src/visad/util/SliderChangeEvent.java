/*

@(#) $Id: SliderChangeEvent.java,v 1.4 2000-03-14 16:56:48 dglo Exp $

VisAD Utility Library: Widgets for use in building applications with
the VisAD interactive analysis and visualization library
Copyright (C) 2014 Nick Rasmussen
VisAD is Copyright (C) 1996 - 2014 Bill Hibbard, Curtis Rueden, Tom
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

package visad.util;

/**
 * The event that occurs when a slider is changed
 *
 * @author Nick Rasmussen
 * @version $Revision: 1.4 $, $Date: 2000-03-14 16:56:48 $
 * @since Visad Utility Library v0.7.1
 */

public class SliderChangeEvent {

  /** The string representation of this event */
  private String string;

  /** The type of this event */
  public int type;

  /** The value of this event.  Typically the new value of the field
   * by mods */
  private float val;

  /** The constant indicating a value change */
  public static final int VALUE_CHANGE = 1;

  /** The constant indicating a lower bound change */
  public static final int LOWER_CHANGE = 2;

  /** The constant indicating an upper bound change */
  public static final int UPPER_CHANGE = 3;

  /** Construct a new event with the given type and value */
  SliderChangeEvent(int type, float val) {
    this(getTypeString(type), type, val);
    this.type = type;
    string = new String("SliderChangeEvent: " + string + " value=" + val);
  }

  /** Construct a new event with the given type, value, and description string */
  SliderChangeEvent(String string, int type, float val) {
    this.string = string;
    this.type = type;
    this.val = val;
  }

  /** Return the string representation of a specific type */
  public static String getTypeString(int type) {

    String result = null;

    switch (type) {
    case VALUE_CHANGE:
      result = "VALUE_CHANGE";
      break;
    case LOWER_CHANGE:
      result = "LOWER_CHANGE";
      break;
    case UPPER_CHANGE:
      result = "UPPER_CHANGE";
      break;
    default:
      result = "(unknown type)";
      break;
    }

    return result;
  }

  /** Return a string description of this object */
  public String toString() {
    return string;
  }
}
