
//
// Divider.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 1998 Bill Hibbard, Curtis Rueden, Tom
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

package visad.ss;

/* AWT packages */
import java.awt.*;

/* JFC classes */
import com.sun.java.swing.JComponent;

/** A thin, horizontal divider. */
public class Divider extends JComponent {

  /** Constructor. */
  public Divider() {
  }

  public void paint(Graphics g) {
    int w = getSize().width;
    g.setColor(Color.white);
    g.drawRect(0, 0, w-2, 6);
    g.drawRect(2, 2, w-4, 2);
    g.setColor(Color.black);
    g.drawRect(1, 1, w-3, 3);
  }

  public Dimension getMinimumSize() {
    return new Dimension(0, 6);
  }

  public Dimension getPreferredSize() {
    return new Dimension(0, 6);
  }

  public Dimension getMaximumSize() {
    return new Dimension(Integer.MAX_VALUE, 6);
  }

}

