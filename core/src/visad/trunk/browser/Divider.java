//
// Divider.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2002 Bill Hibbard, Curtis Rueden, Tom
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
   * Paints the divider.
   */
  public void paint(Graphics g) {
    int w = getSize().width;
    g.setColor(Color.white);
    g.drawRect(0, 0, w-2, 6);
    g.drawRect(2, 2, w-4, 2);
    g.setColor(Color.black);
    g.drawRect(1, 1, w-3, 3);
  }

  /**
   * Gets the divider's minimum size.
   */
  public Dimension getMinimumSize() {
    return new Dimension(0, 6);
  }

  /**
   * Gets the divider's preferred size.
   */
  public Dimension getPreferredSize() {
    return new Dimension(0, 6);
  }

  /**
   * Gets the divider's maximum size.
   */
  public Dimension getMaximumSize() {
    return new Dimension(Integer.MAX_VALUE, 6);
  }

}
