//
// ColorBar.java
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

package visad.bio;

import java.awt.*;
import javax.swing.*;

/** ColorBar is a bar of color designed to represent color graphically. */
public class ColorBar extends JComponent {

  /** Associated color components. */
  private Color color;

  /** Constructs a color bar from the given color components. */
  public ColorBar(float r, float g, float b) {
    this(new Color(r, g, b));
  }

  /** Constructs a color bar of the given color. */
  public ColorBar(Color color) { this.color = color; }

  /** Paints the color bar. */
  public void paint(Graphics g) {
    g.setColor(isEnabled() ? color : Color.gray);
    Dimension size = getSize();
    g.fillRect(0, 0, size.width, size.height);
  }

}
