//
// ScreenAnnotator.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2015 Bill Hibbard, Curtis Rueden, Tom
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

package visad.bom.annotations;

import visad.VisADException;

import java.util.ArrayList;

/**
 * Take descriptions of geometry primatives in Screen Coordinates
 * and draw them onto a VisAD display directly. The objects should then
 * stay on the same screen position.
 *
 * Enables thing like scale bars, tables, logos, etc to be independent
 * of data manipulation tranformations.
 */
public abstract class ScreenAnnotator
{
  // Objects to be diaplayed.
  protected ArrayList things = null;

  /**
   * Default constructor.
   */
  public ScreenAnnotator()
  {
    things = new ArrayList();
  }

  /**
   *  Set the visibility flag.
   *
   *  @param on  <code>true</code> to display, <code>false</code> to undisplay.
   */
  public abstract void  makeVisible(boolean on);

  /**
   *  Traverses all the data objects in the list and transforms
   *  them into viewable objects and then arranges to make them
   *  visible.
   *
   *  @throws VisADException if if any problem creating the picture.
   */
  public abstract void draw()
    throws VisADException;

  /**
   *  Add the object to the list of items to be drawn.
   *
   *  @param object  add this item to the list of things
   *                  to draw.
   */
  public void add(Object object)
  {
    synchronized (things) {
      things.add(object);
    }
  }

  /**
   *  Remove the object from the list of items to be drawn.
   *
   *  @param object  remove this item from the list of things
   *                  to draw.
   */
  public void remove(Object object)
  {
    synchronized (things) {
      things.remove(object);
    }
  }

  /**
   *  Remove all items from the list of things to draw.
   *                  to draw.
   */
  public void clear()
  {
    synchronized (things) {
      things.clear();
    }
  }
} // class ScreenAnnotator
