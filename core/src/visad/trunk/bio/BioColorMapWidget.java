//
// BioColorMapWidget.java
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
 * BioColorMapWidget is a widget for controlling mappings
 * from range scalars to color scalars.
 */
public class BioColorMapWidget extends JPanel implements ItemListener {

  // -- CONSTANTS --

  public static final int RED = 0;
  public static final int GREEN = 1;
  public static final int BLUE = 2;
  public static final int RGB = 3;

  public static final int CYAN = 4;
  public static final int MAGENTA = 5;
  public static final int YELLOW = 6;
  public static final int CMY = 7;

  public static final int HUE = 8;
  public static final int SATURATION = 9;
  public static final int VALUE = 10;
  public static final int HSV = 11;

  private static final DisplayRealType[] COLOR_TYPES = {
    Display.Red, Display.Green, Display.Blue, Display.RGB,
    Display.Cyan, Display.Magenta, Display.Yellow, Display.CMY,
    Display.Hue, Display.Saturation, Display.Value, Display.HSV
  };

  private static final String[] COLOR_NAMES = {
    "Red", "Green", "Blue", "RGB",
    "Cyan", "Magenta", "Yellow", "CMY",
    "Hue", "Saturation", "Value", "HSV"
  };


  // -- GUI COMPONENTS --

  private JLabel color;
  private JComboBox scalars;


  // -- OTHER FIELDS --

  private BioVisAD bio;
  private DisplayRealType type;
  private boolean changed;


  // -- CONSTRUCTOR --

  /** Constructs a new animation widget. */
  public BioColorMapWidget(BioVisAD biovis, int colorType) {
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
    scalars.addItemListener(this);
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

  /** Gets whether the widget has changed since last method call. */
  public boolean hasChanged() {
    boolean b = changed;
    changed = false;
    return b;
  }

  /** Enables or disables this widget. */
  public void setEnabled(boolean enabled) {
    color.setEnabled(enabled);
    scalars.setEnabled(enabled);
  }

  /** Refreshes the combo box to contain the current range types. */
  public void refreshTypes() {
    scalars.removeItemListener(this);
    RealType[] rt = bio.sm.rtypes;
    for (int i=0; i<rt.length; i++) scalars.addItem(rt[i]);

    // Autodetect types

    // Case 1: rtypes.length == 1
    //   RGB, CMY, & HSV -> rtypes[0]
    //   Other           -> None

    if (rt.length == 1) {
      if (type.equals(Display.RGB) || type.equals(Display.CMY) ||
        type.equals(Display.HSV))
      {
        scalars.setSelectedItem(rt[0]);
      }
      else scalars.setSelectedIndex(0); // None
    }

    // Case 2: rtypes.length > 1
    //   R, C & H -> rtypes[0]
    //   G, M & S -> rtypes[1]
    //   B, Y & V -> rtypes[2] (if rtypes.length > 2)
    //   Other    -> None

    else {
      if (type.equals(Display.Red) || type.equals(Display.Cyan) ||
        type.equals(Display.Hue))
      {
        scalars.setSelectedItem(rt[0]);
      }
      else if (type.equals(Display.Green) || type.equals(Display.Magenta) ||
        type.equals(Display.Saturation))
      {
        scalars.setSelectedItem(rt[1]);
      }
      else if (type.equals(Display.Blue) || type.equals(Display.Yellow) ||
        type.equals(Display.Value))
      {
        scalars.setSelectedItem(rt.length > 2 ? rt[2] : null);
      }
      else scalars.setSelectedIndex(0); // None
    }
    scalars.addItemListener(this);
  }


  // -- INTERNAL API METHODS --

  public void itemStateChanged(ItemEvent e) { changed = true; }

}
