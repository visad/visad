/*

@(#) $Id: ColorMap.java,v 1.7 2000-02-24 16:00:52 donm Exp $

VisAD Utility Library: Widgets for use in building applications with
the VisAD interactive analysis and visualization library
Copyright (C) 1998 Nick Rasmussen
VisAD is Copyright (C) 1996 - 1998 Bill Hibbard, Curtis Rueden, Tom
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

import java.awt.*;
import java.util.Vector;
import javax.swing.JPanel;

/** 
 * The abstract class that all color-mapping widgets must extend.  This 
 * class manages all of the listener notification for the ColorMaps.
 *
 * @author Nick Rasmussen nick@cae.wisc.edu
 * @version $Revision 1.7 $, $Date: 2000-02-24 16:00:52 $
 * @since Visad Utility Library, 0.5
 */

public abstract class ColorMap extends JPanel {

	/** Maps a floating point value (in the range 0 to 1) onto a Color, 
	 * returns Color.black if the number is out of range
	 */
	public Color getColor(float value) {
		if (value < 0 || value > 1) {
			return Color.black;
		}
		
		float[] rgb = getRGBTuple(value);
		
		if (rgb[0] < 0) rgb[0] = 0;
		if (rgb[1] < 0) rgb[1] = 0;
		if (rgb[2] < 0) rgb[2] = 0;
		if (rgb[0] > 1) rgb[0] = 1;
		if (rgb[1] > 1) rgb[1] = 1;
		if (rgb[2] > 1) rgb[2] = 1;
		
		return new Color(rgb[0], rgb[1], rgb[2]);
	}
	
	/** Maps a floating point value (in the range 0 to 1) onto an RGB
	 * triplet of floating point numbers in the range 0 to 1)
	 */
	public abstract float[] getRGBTuple(float value);
	
	/** Maps a floating point value (in the range 0 to 1) into a tuple
	 * with dimension of the map */
	public abstract float[] getTuple(float value);
	
	/** Returns the current map resolution */
	public abstract int getMapResolution();
	
	/** Returns the dimension of the map */
	public abstract int getMapDimension();
	
	/** Returns a copy of the ColorMap */
	public abstract float[][] getColorMap();
	
	/** The vector containing the ColorChangeListeners */
	private Vector listeners = new Vector();
	
	/** Add a ColorChangeListener to the listeners list */
	public void addColorChangeListener(ColorChangeListener c) {
          synchronized (listeners) {
		if (!listeners.contains(c)) {
			listeners.addElement(c);
		}
          }
	}
	
	/** Remove a ColorChangeListener from the listeners list */
	public void removeColorChangeListener(ColorChangeListener c) {
          synchronized (listeners) {
		if (listeners.contains(c)) {
			listeners.removeElement(c);
		}
          }
	}
	
	/** Notify the ColorChangeListerers that the color widget has changed */
	protected void notifyListeners(ColorChangeEvent e) {
          Vector cl = null;
          synchronized (listeners) {
            cl = (Vector) listeners.clone();
          }
          for (int i = 0; i < cl.size(); i++) {
            ColorChangeListener c = (ColorChangeListener) cl.elementAt(i);
            c.colorChanged(e);
          }
	}
}

