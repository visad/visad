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

/** ToolPanel is the superclass of all tool panel types. */
public abstract class ToolPanel extends JPanel implements SwingConstants {

  // -- FIELDS --

  /** BioVisAD frame. */
  protected BioVisAD bio;

  /** Panel for placing controls. */
  protected JPanel controls;


  // -- CONSTRUCTOR --

  /** Constructs a tool panel. */
  public ToolPanel(BioVisAD biovis) {
    bio = biovis;

    // CTR: TODO: fix scroll bar problem

    // outer pane with vertical scroll bar
    JPanel outerPane = new JPanel();
    outerPane.setLayout(new BoxLayout(outerPane, BoxLayout.X_AXIS));
    JScrollPane scroll = new JScrollPane(outerPane);
    scroll.setHorizontalScrollBarPolicy(
      JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
    add(scroll, BorderLayout.CENTER);

    // inner pane
    JPanel innerPane = new JPanel();
    innerPane.setLayout(new BoxLayout(innerPane, BoxLayout.Y_AXIS));
    outerPane.add(Box.createHorizontalStrut(10));
    outerPane.add(innerPane);
    outerPane.add(Box.createHorizontalStrut(10));

    // controls panel
    controls = new JPanel() {
      public Dimension getMaximumSize() { return getPreferredSize(); }
    };
    controls.setLayout(new BoxLayout(controls, BoxLayout.Y_AXIS));
    innerPane.add(Box.createVerticalStrut(10));
    innerPane.add(controls);
    innerPane.add(Box.createVerticalStrut(30));
    innerPane.add(Box.createVerticalGlue());
  }


  // -- ABSTRACT METHODS --

  /** Enables or disables this tool panel. */
  public abstract void setEnabled(boolean enabled);

  /** Updates the tool panel's contents. */
  public abstract void update();


  // -- UTILITY METHODS --

  /** Pads a component or group of components with horizontal space. */
  protected static JPanel pad(Component c) {
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
