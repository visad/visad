/*

@(#) $Id: BarSlider.java,v 1.8 2000-03-14 17:18:38 dglo Exp $

VisAD Utility Library: Widgets for use in building applications with
the VisAD interactive analysis and visualization library
Copyright (C) 2011 Nick Rasmussen
VisAD is Copyright (C) 1996 - 2011 Bill Hibbard, Curtis Rueden, Tom
Rink, and Dave Glowacki.

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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * The visad utillity sliding bar
 *
 * @author Nick Rasmussen nick@cae.wisc.edu
 * @version $Revision: 1.8 $, $Date: 2000-03-14 17:18:38 $
 * @since Visad Utility Library v0.7.1
 */

public class BarSlider extends Slider implements MouseListener, MouseMotionListener {

  /** The upper bound */
  private float upper;

  /** The lower bound */
  private float lower;

  /** The current value */
  private float val;

  /** widget sizes */
  Dimension minSize = null;
  Dimension prefSize = null;
  Dimension maxSize = null;

  /** Construct a new bar slider with the default values */
  public BarSlider() {
    this(-1, 1, 0);
  }

  /**
   * Construct a new bar slider with the givden lower, upper and initial values
   * @throws IllegalArgumenentException if lower is not less than initial or initial
   * is not less than upper
   */
  public BarSlider(float lower, float upper, float initial) {

    if (lower > initial) {
      throw new IllegalArgumentException("BarSlider: lower bound is greater than initial value");
    }

    if (initial > upper) {
      throw new IllegalArgumentException("BarSlider: initial value is greater than the upper bound");
    }

    this.upper = upper;
    this.lower = lower;
    this.val = initial;

    this.addMouseListener(this);
    this.addMouseMotionListener(this);

  }

  /** For testing puropses */
  public static void main(String[] argv) {

    javax.swing.JFrame frame;
    frame = new javax.swing.JFrame("Visad Bar Slider");
    frame.addWindowListener(new WindowAdapter() {
        public void windowClosing(WindowEvent e) {System.exit(0);}
      });

    BarSlider b = new BarSlider();

    frame.add(b);

    frame.setSize(b.getPreferredSize());
    frame.setVisible(true);
  }

  /** Return the minimum value of this slider */
  public float getMinimum() {
    return lower;
  }

  /** Sets the minimum value for this slider */
  public synchronized void setMinimum(float value) {

    if (value > val || (value == val && value == upper)) {
      throw new IllegalArgumentException("BarSlider: Attemped to set new minimum value greater than the current value");
    }

    lower = value;

    notifyListeners(new SliderChangeEvent(SliderChangeEvent.LOWER_CHANGE, value));

    repaint();
  }

  /** Return the maximum value of this slider */
  public float getMaximum() {
    return upper;
  }

  /** Sets the maximum value of this scrolbar */
  public synchronized void setMaximum(float value){

    if (value < val || (value == val && value == lower)) {
      throw new IllegalArgumentException("BarSlider: Attemped to set new maximum value less than the current value");
    }

    upper = value;

    notifyListeners(new SliderChangeEvent(SliderChangeEvent.UPPER_CHANGE, value));

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
  public synchronized void setValue(float value){

    if (value > upper || value < lower) {
      throw new IllegalArgumentException("BarSlider: Attemped to set new value out of slider range");
    }

    val = value;

    notifyListeners(new SliderChangeEvent(SliderChangeEvent.VALUE_CHANGE, value));

    repaint();
  }

  /** Return the preferred sise of the bar slider */
  public Dimension getPreferredSize() {
    if (prefSize == null) {
      prefSize = new Dimension(256, 16);
    }
    return prefSize;
  }

  /** Set the preferred size of the bar slider */
  public void setPreferredSize(Dimension dim) { prefSize = dim; }

  /** Return the maximum size of the bar slider */
  public Dimension getMaximumSize() {
    if (maxSize == null) {
      maxSize = new Dimension(Integer.MAX_VALUE, 16);
    }
    return maxSize;
  }

  /** Set the preferred size of the bar slider */
  public void setMaximumSize(Dimension dim) { maxSize = dim; }

  /** Return the minimum size of the bar slider */
  public Dimension getMinimumSize() {
    if (minSize == null) {
      minSize = new Dimension(40, 16);
    }
    return minSize;
  }

  /** Set the preferred size of the bar slider */
  public void setMinimumSize(Dimension dim) { minSize = dim; }

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

  /** the last position where the bar was drawn */
  private int oldxval;

  /** update the slider */
  public void update(Graphics g) {
    g.setColor(Color.black);
    g.drawRect(oldxval - 2, 0, 5, getBounds().height - 1);
    g.setColor(Color.gray);
    g.fillRect(oldxval - 2, getBounds().height / 2 - 1, 6, 3);
    g.setColor(Color.white);

    int xval = (int) Math.floor((val - lower) * (getBounds().width - 1) / (upper - lower));
    g.drawRect(xval - 2, 0, 5, getBounds().height - 1);

    oldxval = xval;
  }

  /** Redraw the slider */
  public void paint(Graphics g) {
    g.setColor(Color.black);
    g.fillRect(0, 0, getBounds().width, getBounds().height);
    g.setColor(Color.gray);
    g.fillRect(0, getBounds().height / 2 - 1, getBounds().width, 3);

    g.setColor(Color.white);

    int xval = (int) Math.floor((val - lower) * (getBounds().width - 1) / (upper - lower));
    g.drawRect(xval - 2, 0, 5, getBounds().height - 1);
    oldxval = xval;
  }
}
