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

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import visad.RealType;

/**
 * BioColorWidget is a widget for controlling mappings
 * from range scalars to color scalars.
 */
public class BioColorWidget extends JPanel {

  // -- CONSTANTS --

  public static final int RGB = 0;
  public static final int HSV = 1;

  public static final RealType SOLID = RealType.getRealType("bio_solid");

  private static final String[][] COLOR_NAMES = {
    {"Red", "Green", "Blue"},
    {"Hue", "Saturation", "Value"}
  };


  // -- GUI COMPONENTS --

  private JLabel color;
  private JComboBox scalars;


  // -- OTHER FIELDS --

  private VisBio bio;
  private int model;
  private int type;


  // -- CONSTRUCTOR --

  /** Constructs a new animation widget. */
  public BioColorWidget(VisBio biovis, int colorType) {
    bio = biovis;
    model = RGB;
    type = colorType;

    // create components
    color = new JLabel(COLOR_NAMES[model][type] + ":");
    color.setForeground(Color.black);
    scalars = new JComboBox();
    scalars.addItem("None");
    color.setLabelFor(scalars);

    // lay out components
    setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
    JPanel p = new JPanel();
    p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
    p.add(Box.createHorizontalGlue());
    p.add(color);
    p.add(Box.createHorizontalGlue());
    add(p);
    add(scalars);
  }


  // -- API METHODS --

  /** Gets the currently selected RealType, or null of None. */
  public RealType getSelectedItem() {
    Object o = scalars.getSelectedItem();
    if (o instanceof RealType) return (RealType) o;
    String s = (String) o;
    return s.equals("Full") ? SOLID : null;
  }

  /** Gets the widget's color model (RGB or HSV). */
  public int getModel() { return model; }

  /** Sets the currently selected RealType. */
  public void setSelectedItem(RealType rt) {
    if (rt == null) scalars.setSelectedIndex(0);
    else scalars.setSelectedItem(rt);
  }

  /** Sets the widget's color model (RGB or HSV). */
  public void setModel(int model) {
    this.model = model;
    color.setText(COLOR_NAMES[model][type] + ":");
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

  /** Sets the mnemonic for this widget. */
  public void setMnemonic(char c) { color.setDisplayedMnemonic(c); }

  /** Sets the tool tip for this widget. */
  public void setToolTipText(String text) {
    color.setToolTipText(text);
    scalars.setToolTipText(text);
  }

  /** Chooses most desirable range type for this widget's color. */
  public void guessType() {
    scalars.removeAllItems();
    scalars.addItem("None");
    scalars.addItem("Full");
    RealType[] rt = bio.sm.rtypes;
    for (int i=0; i<rt.length; i++) scalars.addItem(rt[i]);

    // Autodetect types

    // Case 0: no rtypes
    //   R -> None
    //   G -> None
    //   B -> None
    
    if (rt == null || rt.length == 0) scalars.setSelectedIndex(0); // None

    // Case 1: rtypes.length == 1
    //   R -> rtypes[0]
    //   G -> rtypes[0]
    //   B -> rtypes[0]

    else if (rt.length == 1) scalars.setSelectedItem(rt[0]);

    // Case 2: rtypes.length == 2
    //   R -> rtypes[0]
    //   G -> rtypes[1]
    //   B -> None

    // Case 3: rtypes.length >= 3
    //   R -> rtypes[0]
    //   G -> rtypes[1]
    //   B -> rtypes[2]

    else {
      if (type == 0) scalars.setSelectedItem(rt[0]);
      else if (type == 1) scalars.setSelectedItem(rt[1]);
      else if (type == 2 && rt.length >= 3) scalars.setSelectedItem(rt[2]);
      else scalars.setSelectedIndex(0); // None
    }
  }

}
