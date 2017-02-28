/*

@(#) $Id: Slider.java,v 1.7 2000-04-19 18:37:20 billh Exp $

VisAD Utility Library: Widgets for use in building applications with
the VisAD interactive analysis and visualization library
Copyright (C) 2017 Nick Rasmussen
VisAD is Copyright (C) 1996 - 2017 Bill Hibbard, Curtis Rueden, Tom
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

import java.util.Vector;

import javax.swing.JPanel;

/**
 * An abstract class which is very similar to java.awt.ScrollBar, except using
 * all floating point values and having an internal name.
 *
 * Although the interface has nothing to do with orientation, a horizontal
 * orientation will be assumed by several other classes
 *
 * @author Nick Rasmussen nick@cae.wisc.edu
 * @version $Revision: 1.7 $, $Date: 2000-04-19 18:37:20 $
 * @since Visad Utility Library v0.7.1
 */

public abstract class Slider extends JPanel {

  /**
   * The internal name of the slider, accessed through getName()
   * @see #getName
   */
  protected String name;

  /** Get the internal name for this slider */
  public String getName() {
    return name;
  }


  /** Return the minimum value of this slider */
  public abstract float getMinimum();

  /** Sets the minimum value for this slider */
  public abstract void setMinimum(float value);

  /** Return the maximum value of this slider */
  public abstract float getMaximum();

  /** Sets the maximum value of this scrolbar */
  public abstract void setMaximum(float value);

  /** Returns the current value of the slider */
  public abstract float getValue();

  /** Sets the current value of the slider */
  public abstract void setValue(float value);


  /** The vector containing the SliderChangeListeners */
  protected Vector listeners = new Vector();
  private Object listeners_lock = new Object();

  /** Add a SliderChangeListener to the listeners list */
  // public synchronized void addSliderChangeListener(SliderChangeListener s) {
  public void addSliderChangeListener(SliderChangeListener s) {
    synchronized (listeners_lock) {
      if (!listeners.contains(s)) {
        listeners.addElement(s);
      }
    }
  }

  /** Remove a SliderChangeListener from the listeners list */
  // public synchronized void removeSliderChangeListener(SliderChangeListener s) {
  public void removeSliderChangeListener(SliderChangeListener s) {
    synchronized (listeners_lock) {
      if (listeners.contains(s)) {
        listeners.removeElement(s);
      }
    }
  }

  /** Notify the ColorChangeListerers that the color widget has changed */
  // protected synchronized void notifyListeners(SliderChangeEvent e) {
  protected void notifyListeners(SliderChangeEvent e) {
    Vector listeners_clone = null;
    synchronized (listeners_lock) {
      listeners_clone = (Vector) listeners.clone();
    }
    for (int i = 0; i < listeners_clone.size(); i++) {
      SliderChangeListener s =
        (SliderChangeListener) listeners_clone.elementAt(i);
      s.sliderChanged(e);
    }
  }
}
