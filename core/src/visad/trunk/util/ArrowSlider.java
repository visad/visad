/*

@(#) $Id: ArrowSlider.java,v 1.6 1998-07-30 20:30:02 curtis Exp $

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
import java.awt.event.*;

/**
 * A pointer slider for visad .
 * 
 * @author Nick Rasmussen nick@cae.wisc.edu
 * @version $Revision: 1.6 $, $Date: 1998-07-30 20:30:02 $
 * @since Visad Utility Library v0.7.1
 */

public class ArrowSlider extends Slider implements MouseListener, MouseMotionListener {

	/** The upper bound */
	private float upper;
	
	/** The lower bound */
	private float lower;
	
	/** The current value */
	private float val;
		
	/** Construct a new arrow slider with the default values */
	public ArrowSlider() {
		this(-1, 1, 0);
	}
	
	/** 
	 * Construct a new arrow slider with the givden lower, upper and initial values
	 * @throws IllegalArgumenentException if lower is not less than initial or initial
	 * is not less than upper 
	 */
	public ArrowSlider(float lower, float upper, float initial) {
		this(lower, upper, initial, "value");
	}
	
	/** 
	 * Construct a new arrow slider with the given lower, upper and initial values
	 * @throws IllegalArgumenentException if lower is not less than initial or initial
	 * is not less than upper 
	 */
	public ArrowSlider(float lower, float upper, float initial, String name) {
		
		if (lower > initial) {
			throw new IllegalArgumentException("ArrowSlider: lower bound is greater than initial value");
		}
		
		if (initial > upper) {
			throw new IllegalArgumentException("ArrowSlider: initial value is greater than the upper bound");
		}
		
		this.upper = upper;
		this.lower = lower;
		this.val = initial;
		
		this.name = name;
		
		this.addMouseListener(this);
		this.addMouseMotionListener(this);
		
	}
		
	/** For testing purposes */
	public static void main(String[] argv) {
	
		Frame frame = new Frame("Visad Arrow Slider");
		frame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {System.exit(0);}
		});
		
		ArrowSlider a = new ArrowSlider();
		
		frame.add(a);
		
		frame.setSize(a.getPreferredSize());
		frame.setVisible(true);
	}

    /* CTR: 29 Jul 1998: added setBounds method */
    /** Sets new minimum, maximum, and initial values for this slider */
    public synchronized void setBounds(float min, float max, float init) {
      if (min > max) {
        throw new IllegalArgumentException("ArrowSlider: min cannot be "
                                          +"greater than max");
      }
      if (init < min || init > max) {
        throw new IllegalArgumentException("ArrowSlider: initial value "
                                          +"must be between min and max");
      }
      lower = min;
      upper = max;
      val = init;
      notifyListeners(new SliderChangeEvent(SliderChangeEvent.LOWER_CHANGE, min));
      notifyListeners(new SliderChangeEvent(SliderChangeEvent.UPPER_CHANGE, max));
      notifyListeners(new SliderChangeEvent(SliderChangeEvent.VALUE_CHANGE, init));

      // won't update on repaint, so hit it with a big hammer
      update(getGraphics());
      repaint();
    }

	/** Return the minimum value of this slider */
	public float getMinimum() {
		return lower;
	}
	
	/** Sets the minimum value for this slider */
	public synchronized void setMinimum(float value) {
		
		if (value > val || (value == val && value == upper)) {
			throw new IllegalArgumentException("ArrowSlider: Attemped to set new minimum value greater than the current value");
		}
		
		lower = value;
		
		notifyListeners(new SliderChangeEvent(SliderChangeEvent.LOWER_CHANGE, value));
		
// won't update on repaint, so hit it with a big hammer
update(getGraphics());
		repaint();
	}
		
	/** Return the maximum value of this slider */
	public float getMaximum() {
		return upper;
	}
	
	/** Sets the maximum value of this scrolbar */
	public synchronized void setMaximum(float value){
		
		if (value < val || (value == val && value == lower)) {
			throw new IllegalArgumentException("ArrowSlider: Attemped to set new maximum value less than the current value");
		}
		
		upper = value;
		
		notifyListeners(new SliderChangeEvent(SliderChangeEvent.UPPER_CHANGE, value));

// won't update on repaint, so hit it with a big hammer
update(getGraphics());
		repaint();		
	}
	
	/** Returns the current value of the slider */
	public float getValue() {
		return val;
	}
	
	/** 
	 * Sets the current value of the slider
	 * @throws IllegalArgumentException if the new value is out of bounds for the slider
	 */
	public synchronized void setValue(float value) {
		
		if (value > upper || value < lower) {
			throw new IllegalArgumentException("ArrowSlider: Attemped to set new value out of slider range");
		}
		
		val = value;
		
		notifyListeners(new SliderChangeEvent(SliderChangeEvent.VALUE_CHANGE, value));

// won't update on repaint, so hit it with a big hammer
update(getGraphics());
		repaint();		
	}
	
	/** Return the preferred sise of the arrow slider */
	public Dimension getPreferredSize() {
		return new Dimension(256, 16);
	}
	
	/** Return the maximum size of the arrow slider */
	public Dimension getMaximumSize() {
		return new Dimension(Integer.MAX_VALUE, 16);
	}
	
	/** Return the minimum size of the arrow slider */
	public Dimension getMinimumSize() {
		return new Dimension(40, 16);
	}
		
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
	
	/** Moves the slider to the clicked position */
	public void mousePressed(MouseEvent e) {
		//System.out.println(e.paramString());
		updatePosition(e);
	}
	
	/** Present to implement MouseListener, currently ignored */
	public void mouseReleased(MouseEvent e) {
		//System.out.println(e.paramString());
	}

	/** Updates the slider position */
	public void mouseDragged(MouseEvent e) {
		//System.out.println(e.paramString());
		updatePosition(e);
	}
	
	/** Present to implement MouseMovementListener, currently ignored */
	public void mouseMoved(MouseEvent e) {
		//System.out.println(e.paramString());
	}

	/** Recalculate the position and value of the slider given the new mouse position */
	private void updatePosition(MouseEvent e) {
		int x = e.getX();
		
		if (x < 0) x = 0;
		if (x >= getBounds().width) x = getBounds().width - 1;

		float dist = (float) x / (float) (getBounds().width - 1);
		
		setValue(lower + dist*(upper - lower));
	}

	/** the last position where the arrow was drawn */
	private int oldxval;

	/** update the slider */	
	public void update(Graphics g) {
		g.setColor(Color.black);
		g.drawLine(oldxval, 0, oldxval, getBounds().height - 1);
		g.drawLine(oldxval, 0, oldxval - 4, 4);
		g.drawLine(oldxval, 0, oldxval + 4, 4);

		g.setColor(Color.white);
		
		int xval = (int) Math.floor((val - lower) * (getBounds().width - 1) / (upper - lower));
		g.drawLine(xval, 0, xval, getBounds().height - 1);
		g.drawLine(xval, 0, xval - 4, 4);
		g.drawLine(xval, 0, xval + 4, 4);
		
		oldxval = xval;
	}	

	/** Redraw the slider */
	public void paint(Graphics g) {
		g.setColor(Color.black);
		g.fillRect(0, 0, getBounds().width, getBounds().height);
		
		g.setColor(Color.white);
		
		int xval = (int) Math.floor((val - lower) * (getBounds().width - 1) / (upper - lower));
		g.drawLine(xval, 0, xval, getBounds().height - 1);
		g.drawLine(xval, 0, xval - 4, 4);
		g.drawLine(xval, 0, xval + 4, 4);
		
		oldxval = xval;
	}
		 
}

