//
// ToolPanel.java
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
import javax.swing.border.EmptyBorder;

/** ToolPanel is the superclass of all tool panel types. */
public abstract class ToolPanel extends JScrollPane {

  // -- FIELDS --

  /** BioVisAD frame. */
  protected BioVisAD bio;

  /** Panel for placing controls. */
  protected JPanel controls;


  // -- CONSTRUCTOR --

  /** Constructs a tool panel. */
  public ToolPanel(BioVisAD biovis) {
    super(new JPanel(), VERTICAL_SCROLLBAR_AS_NEEDED,
      HORIZONTAL_SCROLLBAR_NEVER);
    JPanel pane = (JPanel) getViewport().getView();
    bio = biovis;

    // controls panel with vertical scrollbar
    controls = new JPanel() {
      public Dimension getMaximumSize() { return getPreferredSize(); }
    };
    controls.setLayout(new BoxLayout(controls, BoxLayout.Y_AXIS));
    controls.setBorder(new EmptyBorder(10, 10, 10, 10));
    controls.setMaximumSize(controls.getMinimumSize());
    pane.add(controls);
  }


  // -- ABSTRACT METHODS --

  /** Enables or disables this tool panel. */
  public abstract void setEnabled(boolean enabled);


  // -- UTILITY METHODS --

  /** Pads a component or group of components with horizontal space. */
  public static JPanel pad(Component c) {
    JPanel p;
    if (c instanceof JPanel) {
      p = (JPanel) c;
      p.add(Box.createHorizontalGlue(), 0);
      p.add(Box.createHorizontalStrut(5), 0);
      p.add(Box.createHorizontalGlue());
      p.add(Box.createHorizontalStrut(5));
    }
    else {
      p = new JPanel();
      p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
      p.add(Box.createHorizontalStrut(5));
      p.add(Box.createHorizontalGlue());
      p.add(c);
      p.add(Box.createHorizontalGlue());
      p.add(Box.createHorizontalStrut(5));
    }
    return p;
  }

}
