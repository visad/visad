//
// BioColorWidget.java
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

import java.awt.Dimension;
import java.awt.event.*;
import javax.swing.*;
import visad.*;

/**
 * BioColorWidget is a widget for controlling mappings
 * from range scalars to color scalars.
 */
public class BioColorWidget extends JPanel {

  // -- CONSTANTS --

  public static final int RED = 0;
  public static final int GREEN = 1;
  public static final int BLUE = 2;

  private static final DisplayRealType[] COLOR_TYPES = {
    Display.Red, Display.Green, Display.Blue
  };

  private static final String[] COLOR_NAMES = {"Red", "Green", "Blue"};


  // -- GUI COMPONENTS --

  private JLabel color;
  private JComboBox scalars;


  // -- OTHER FIELDS --

  private BioVisAD bio;
  private DisplayRealType type;


  // -- CONSTRUCTOR --

  /** Constructs a new animation widget. */
  public BioColorWidget(BioVisAD biovis, int colorType) {
    bio = biovis;
    type = COLOR_TYPES[colorType];
    color = new JLabel(COLOR_NAMES[colorType] + ":") {
      public Dimension getPreferredSize() {
        Dimension d = super.getPreferredSize();
        return new Dimension(50, d.height);
      }
    };
    scalars = new JComboBox();
    scalars.addItem("None");
    setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
    add(color);
    add(scalars);
  }


  // -- API METHODS --

  /** Gets the currently selected RealType, or null of None. */
  public RealType getSelectedItem() {
    Object o = scalars.getSelectedItem();
    return o instanceof RealType ? (RealType) o : null;
  }

  /** Sets the currently selected RealType. */
  public void setSelectedItem(RealType rt) {
    if (rt == null) scalars.setSelectedIndex(0);
    else scalars.setSelectedItem(rt);
  }

  /** Adds an item listener to this widget. */
  public void addItemListener(ItemListener l) { scalars.addItemListener(l); }

  /** Removes an item listener from this widget. */
  public void removeItemListener(ItemListener l) {
    scalars.removeItemListener(l);
  }

  /** Enables or disables this widget. */
  public void setEnabled(boolean enabled) {
    color.setEnabled(enabled);
    scalars.setEnabled(enabled);
  }

  /** Chooses most desirable range type for this widget's color. */
  public void guessType() {
    RealType[] rt = bio.sm.rtypes;
    for (int i=0; i<rt.length; i++) scalars.addItem(rt[i]);

    // Autodetect types

    // Case 1: rtypes.length == 1
    //   R -> rtypes[0]
    //   G -> rtypes[0]
    //   B -> rtypes[0]

    if (rt.length == 1) scalars.setSelectedItem(rt[0]);

    // Case 2: rtypes.length == 2
    //   R -> rtypes[0]
    //   G -> rtypes[1]
    //   B -> None

    // Case 3: rtypes.length >= 3
    //   R -> rtypes[0]
    //   G -> rtypes[1]
    //   B -> rtypes[2]

    else {
      if (type.equals(Display.Red)) scalars.setSelectedItem(rt[0]);
      else if (type.equals(Display.Green)) scalars.setSelectedItem(rt[1]);
      else if (type.equals(Display.Blue) && rt.length >= 3) {
        scalars.setSelectedItem(rt[2]);
      }
      else scalars.setSelectedIndex(0); // None
    }
  }

}
