//
// ColorRenderer.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2001 Bill Hibbard, Curtis Rueden, Tom
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

/** ColorRenderer is a JComboBox renderer that uses ColorBars. */
public class ColorRenderer implements ListCellRenderer {

  /** Preferred size of the renderer's components. */
  private Dimension size = new Dimension(200, 15);

  /** Constructs a color JComboBox renderer. */
  public ColorRenderer() { }

  /** Sets the preferred size of the renderer's components. */
  public void setSize(Dimension size) { this.size = size; }

  /** Gets the specified JComboBox list component. */
  public Component getListCellRendererComponent(JList list, Object value,
    int index, boolean isSelected, boolean cellHasFocus)
  {
    ColorBar bar = new ColorBar((Color) value);
    bar.setPreferredSize(size);
    if (isSelected) {
      bar.setBackground(list.getSelectionBackground());
      bar.setForeground(list.getSelectionForeground());
    }
    else {
      bar.setBackground(list.getBackground());
      bar.setForeground(list.getForeground());
    }
    return bar;
  }

}
