/*

@(#) $Id: GrayscaleMap.java,v 1.1 1998-02-05 21:46:54 billh Exp $

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

import java.awt.event.*;
import java.awt.*;

/** 
 * A simple grayscale colormap with no interpolation between
 * the internally stored values.  Click and drag with the left
 * mouse button to draw the color curve.
 *
 * @author Nick Rasmussen nick@cae.wisc.edu
 * @version $Revision: 1.1 $, $Date: 1998-02-05 21:46:54 $
 * @since Visad Utility Library, 0.5
 */

public class GrayscaleMap extends ColorMap 
	implements MouseListener, MouseMotionListener, ColorChangeListener {

	private float[] val;
	private int resolution;
	
	/** Construct a GrayscaleMap with the default resolution of 256 */
	public GrayscaleMap() {
		this(256);
	}
	
	/** The grayscale map is represented internally by an array of
	 * floats
	 * @param resolution the length of the array
	 */
	public GrayscaleMap(int resolution) {
		this.resolution = resolution;
		val = new float[resolution];
		addMouseListener(this);
		addMouseMotionListener(this);
		this.addColorChangeListener(this);
	}
	
	/** Used internally to post areas to update to the objects listening
	 * to the map 
	 */
	protected void notifyListeners(int left, int right) {		
		float start = (float) left / (float) val.length;
		float end = (float) (right + 1) / (float) val.length;
		super.notifyListeners(new ColorChangeEvent(this, start, end));
	}
	
	/** Implementation of the abstract function in ColorMap
	 * @param value a floating point number between 0 and 1
	 * @return an RGB tuple of floating point numbers in the
	 * range 0 to 1
	 */
	public float[] getRGBTuple(float value) {
		int index = (int) Math.floor(value * val.length);
		if (index >= val.length) index = val.length - 1;
		if (index < 0) index = 0;
		float[] f = {val[index], val[index], val[index]};
		return f;
	}
	
	/** The last index that was modified */
	private int oldPos;
	/** The value of the last index modified */
	private float oldVal;
	
	/** Present to implement MouseListener, currently ignored */
	public void mouseClicked(MouseEvent e) {
		//System.out.println(e.paramString());
	}

	/** Present to implement MouseListener, currently ignored */
	public void mouseEntered(MouseEvent e) {
		//System.out.println(e.paramString());
	}

	/** Present to implement MouseListener, currently ignored */
	public void mouseExited(MouseEvent e) {
		//System.out.println(e.paramString());
	}
	
	/** Updates the internal array and sends notification to the
	 * ColorChangeListeners that are listening to this map
	 */
	public void mousePressed(MouseEvent e) {
		//System.out.println(e.paramString());
		
		if (e.getX() < 0) return;
		if (e.getX() >= getBounds().width) return;
		if (e.getY() < 0) return;
		if (e.getY() >= getBounds().height) return;
		
		float dist = (float) e.getX() / (float) getBounds().width;
		int index = (int) Math.floor(dist * val.length);
		val[index] = 1 - (float) e.getY() / (float) getBounds().height;
		
		oldVal = val[index];
		oldPos = index;
		
		notifyListeners(index, index);
	}
	
	/** Present to implement MouseListener, currently ignored */
	public void mouseReleased(MouseEvent e) {
		//System.out.println(e.paramString());
	}

	/** Updates the internal array and sends notification to the
	 * ColorChangeListeners that are listening to this map
	 */
	public void mouseDragged(MouseEvent e) {
		//System.out.println(e.paramString());

		if (e.getX() < 0) return;
		if (e.getX() >= getBounds().width) return;
		if (e.getY() < 0) return;
		if (e.getY() >= getBounds().height) return;
		
		float dist = (float) e.getX() / (float) getBounds().width;
		int index = (int) Math.floor(dist * val.length);
		float target = 1 - (float) e.getY() / (float) getBounds().height;
		
		if (index > oldPos) {
			for (int i = oldPos + 1; i <= index; i++) {
				val[i] = oldVal * ((float) (index - i)) / ((float) (index - oldPos))
						+ target * ((float) (i - oldPos)) / ((float) (index - oldPos));
			}
			notifyListeners(oldPos + 1, index);
			oldPos = index;
			oldVal = target;
			return;
		}
		if (index < oldPos) {
			for (int i = oldPos - 1; i >= index; i--) {
				val[i] = oldVal * ((float) (i - index)) / ((float) (oldPos - index))
						+ target * ((float) (oldPos - i)) / ((float) (oldPos - index));
			}
			notifyListeners(index, oldPos - 1);
			oldPos = index;
			oldVal = target;
			return;
		}
		if (index == oldPos) {
			val[index] = target;
			notifyListeners(index, index);
			oldPos = index;
			oldVal = target;
		}
	}
	
	/** Present to implement MouseMovementListener, currently ignored */
	public void mouseMoved(MouseEvent e) {
		//System.out.println(e.paramString());
	}

	/** Repaints the entire Panel */
	public void paint(Graphics g) {
		updateLeft = 0;
		updateRight = 1;
		update(g);
	}
	
	/** The left bound for updating the Panel */
	private float updateLeft = 0;
	
	/** The right bound for updating the Panel */
	private float updateRight = 1;
	
	/** Repaints the modified areas of the Panel */
	public void update(Graphics g) {
	
		int leftIndex;
		int rightIndex;
	
		synchronized(this) {
			leftIndex = (int) Math.floor(updateLeft * (float) getBounds().width);
			rightIndex = (int) Math.floor(updateRight * (float) getBounds().width);
		}
		
		if (leftIndex > rightIndex) {
			int tmp = leftIndex;
			leftIndex = rightIndex;
			rightIndex = tmp;
		}

		if (leftIndex < 0) {
			leftIndex = 0;
		}
		if (leftIndex >= getBounds().width) {
			leftIndex = getBounds().width - 1;
		}
		if (rightIndex < 0) {
			rightIndex = 0;
		}
		if (rightIndex >= getBounds().width) {
			rightIndex = getBounds().width - 1;
		}

		for (int i = leftIndex; i <= rightIndex; i++) {
			int index = (i * val.length) / getBounds().width;
			if (index >= val.length) {
				index = val.length - 1;
			}
			if (index < 0) {
				index = 0;
			}
			
			int whiteHeight = (int) Math.floor(val[index] * (float) getBounds().height);
			g.setColor(Color.black);
			g.drawLine(i,0,i,getBounds().height - 1);
			if (whiteHeight > 0) {
				g.setColor(Color.white);
				g.drawLine(i,getBounds().height - whiteHeight,i,getBounds().height - whiteHeight);
			}
			
			/* if you want solid white under the value
			if (whiteHeight > 0) {
				g.setColor(Color.white);
				g.drawLine(i,getBounds().height - whiteHeight,i,getBounds().height - 1);
			}
			*/
		}
	}
	
	/** Listens for changes in the map to properly redraw the Panel */
	public void colorChanged(ColorChangeEvent e) {
		synchronized(this) {
			updateLeft = e.getStart();
			updateRight = e.getEnd();
		}
		repaint();	
	}
	
	/** Returns the preferred size of the map taking into account the resolution */
	public Dimension getPreferredSize() {
		return new Dimension(resolution, resolution / 2);
	}
	
}