/*

@(#) $Id: ColorChangeEvent.java,v 1.1 1998-02-05 21:46:52 billh Exp $

VisAD Utility Library: Widgets for use in building applications with
the VisAD interactive analysis and visualization library
Copyright (C) 1998 Nick Rasmussen
VisAD is Copyright (C) 1996 - 1998 Bill Hibbard, Curtis Rueden and Tom
Rink.
 
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
 * @version $Revision: 1.1 $, $Date: 1998-02-05 21:46:52 $
 * @since Visad Utility Library, 0.5
 */

public class ColorChangeEvent extends EventObject implements Serializable {

	/** The starting location where the ColorMap has changed */
	private float start;
	/** The ending location where the ColorMap has changed */
	private float end;

	/** Construct a colorChangeEvent object to notify any ColorChangeListeners 
	 * @param object the map that generated the event
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