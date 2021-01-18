/*

@(#) $Id: ColorChangeEvent.java,v 1.6 2000-08-22 18:17:09 dglo Exp $

VisAD Utility Library: Widgets for use in building applications with
the VisAD interactive analysis and visualization library
Copyright (C) 2021 Nick Rasmussen
VisAD is Copyright (C) 1996 - 2021 Bill Hibbard, Curtis Rueden, Tom
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

import java.util.*;
import java.io.Serializable;

/**
 * An event to be dispatched when the color widget has been changed
 *
 * @author Nick Rasmussen nick@cae.wisc.edu
 * @version $Revision: 1.6 $, $Date: 2000-08-22 18:17:09 $
 * @since Visad Utility Library, 0.5
 */

public class ColorChangeEvent extends EventObject implements Serializable {

  /** The starting location where the ColorMap has changed */
  private float start;
  /** The ending location where the ColorMap has changed */
  private float end;

  /** Construct a colorChangeEvent object to notify any ColorChangeListeners
   * @param source the map that generated the event
   * @param start the start of the region of the map that has been modified
   * @param end the end of the region of the map that has been modified
   */
  public ColorChangeEvent(Object source, float start, float end) {
    super(source);
    this.start = start;
    this.end = end;
  }

  /** Get the start of the modified region of the map */
  public float getStart() {
    return start;
  }

  /** Get the end of the modified region of the map */
  public float getEnd() {
    return end;
  }

  /** Return a string representation of this object */
  public String toString() {
    return new String("Change: from " + start + " to " + end);
  }
}
