//
// RenderToolPanel.java
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

import java.awt.event.*;
import javax.swing.*;

/**
 * RenderToolPanel is the tool panel for
 * performing rendering operations on the data.
 */
public class RenderToolPanel extends ToolPanel {

  // -- GUI COMPONENTS --

  /** Toggle for 3-D volume rendering. */
  JCheckBox volume;


  // -- CONSTRUCTOR --

  /** Constructs a rendering tool panel. */
  public RenderToolPanel(BioVisAD biovis) {
    super(biovis);

    // 3-D volume rendering checkbox
    boolean okay3d = bio.display3 != null;
    volume = new JCheckBox("Render 3-D image stack as a volume", false);
    volume.addItemListener(new ItemListener() {
      public void itemStateChanged(ItemEvent e) {
        boolean b = volume.isSelected();
        bio.setVolume(b);
      }
    });
    volume.setEnabled(okay3d);
    controls.add(pad(volume));
  }


  // -- API METHODS --

  /** Enables or disables this tool panel. */
  public void setEnabled(boolean enabled) { }

  /** Adds a widget to the tool panel. */
  public void addWidget(JComponent c) { controls.add(c); }

  /** Removes all widgets from the tool panel. */
  public void removeAllWidgets() {
    int size = controls.getComponentCount();
    for (int i=controls.getComponentCount(); i>1; i--) controls.remove(1);
  }

}
