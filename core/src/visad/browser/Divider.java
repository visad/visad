//
// Divider.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2014 Bill Hibbard, Curtis Rueden, Tom
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

import java.awt.*;

/**
 * A small divider for visually separating components.
 */
public class Divider extends Component {

  /**
   * Constant for horizontal divider alignment.
   */
  public static final int HORIZONTAL = 1;

  /**
   * Constant for vertical divider alignment.
   */
  public static final int VERTICAL = 2;

  /**
   * Orientation for this divider.
   */
  private int orientation;

  /**
   * Constructor for horizontal divider.
   */
  public Divider() {
    this(HORIZONTAL);
  }

  /**
   * Constructor for divider in the given orientation.
   *
   * @param orientation Divider.HORIZONTAL or Divider.VERTICAL
   */
  public Divider(int orientation) {
    this.orientation = orientation;
  }

  /**
   * Paints the divider.
   */
  public void paint(Graphics g) {
    if (orientation == HORIZONTAL) {
      int w = getSize().width;
      g.setColor(Color.white);
      g.drawRect(0, 0, w-2, 6);
      g.drawRect(2, 2, w-4, 2);
      g.setColor(Color.black);
      g.drawRect(1, 1, w-3, 3);
    }
    else if (orientation == VERTICAL) {
      int h = getSize().height;
      g.setColor(Color.white);
      g.drawRect(0, 0, 6, h-2);
      g.drawRect(2, 2, 2, h-4);
      g.setColor(Color.black);
      g.drawRect(1, 1, 3, h-3);
    }
  }

  /**
   * Gets the divider's minimum size.
   */
  public Dimension getMinimumSize() {
    if (orientation == HORIZONTAL) {
      return new Dimension(0, 6);
    }
    else if (orientation == VERTICAL) {
      return new Dimension(6, 0);
    }
    else {
      return new Dimension(0, 0);
    }
  }

  /**
   * Gets the divider's preferred size.
   */
  public Dimension getPreferredSize() {
    if (orientation == HORIZONTAL) {
      return new Dimension(0, 6);
    }
    else if (orientation == VERTICAL) {
      return new Dimension(6, 0);
    }
    else {
      return new Dimension(0, 0);
    }
  }

  /**
   * Gets the divider's maximum size.
   */
  public Dimension getMaximumSize() {
    if (orientation == HORIZONTAL) {
      return new Dimension(Integer.MAX_VALUE, 6);
    }
    else if (orientation == VERTICAL) {
      return new Dimension(6, Integer.MAX_VALUE);
    }
    else {
      return new Dimension(0, 0);
    }
  }

}
