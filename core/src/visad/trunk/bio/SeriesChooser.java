//
// SeriesChooser.java
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
import java.io.File;
import java.util.Vector;
import javax.swing.*;
import visad.util.Util;

/** SeriesChooser provides a mechanism for specifying a 4-D data series. */
public class SeriesChooser extends JPanel {

  // -- CONSTANTS --

  /** List of standard file series types. */
  private static final String[] types = {"PIC", "TIFF", "MOV", "BMP"};


  // -- FIELDS --

  /** Text field containing file prefix. */
  JTextField prefix;

  /** Text field containing series count. */
  JTextField count;

  /** Text field containing file extension. */
  JComboBox type;

  /** Label for how files should be interpreted. */
  private JLabel treatLabel;

  /** Radio button for treating each file as a timestep. */
  private JRadioButton treatTimestep;

  /** Radio button for treating each file as a slice. */
  private JRadioButton treatSlice;


  // -- CONSTRUCTOR --

  /** Creates a file series chooser component. */
  public SeriesChooser() {
    // create labels
    JLabel l1 = new JLabel("File prefix");
    JLabel l2 = new JLabel("Count");
    JLabel l3 = new JLabel("Extension");
    treatLabel = new JLabel("Treat each file as a:");
    l1.setForeground(Color.black);
    l2.setForeground(Color.black);
    l3.setForeground(Color.black);
    treatLabel.setForeground(Color.black);

    // create text fields
    prefix = new JTextField();
    count = new JTextField();
    Vector items = new Vector(types.length);
    for (int i=0; i<types.length; i++) items.add(types[i]);
    type = new JComboBox(items);
    Util.adjustTextField(prefix);
    type.setEditable(true);

    // create radio buttons
    treatTimestep = new JRadioButton("timestep");
    treatSlice = new JRadioButton("slice");
    treatTimestep.setForeground(Color.black);
    treatSlice.setForeground(Color.black);
    treatTimestep.setSelected(true);
    ButtonGroup group = new ButtonGroup();
    group.add(treatTimestep);
    group.add(treatSlice);

    // lay out top components
    setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
    add(ToolPanel.pad(l1, false, true));
    add(prefix);

    // lay out middle components
    JPanel mid = new JPanel();
    mid.setLayout(new BoxLayout(mid, BoxLayout.X_AXIS));
    JPanel midLeft = new JPanel();
    midLeft.setLayout(new BoxLayout(midLeft, BoxLayout.Y_AXIS));
    midLeft.add(ToolPanel.pad(l2, false, true));
    midLeft.add(count);
    mid.add(midLeft);
    JPanel midRight = new JPanel();
    midRight.setLayout(new BoxLayout(midRight, BoxLayout.Y_AXIS));
    midRight.add(ToolPanel.pad(l3, false, true));
    midRight.add(type);
    mid.add(midRight);
    add(mid);

    // lay out bottom components
    JPanel bottom = new JPanel();
    bottom.setLayout(new BoxLayout(bottom, BoxLayout.X_AXIS));
    bottom.add(treatLabel);
    bottom.add(Box.createHorizontalGlue());
    bottom.add(treatTimestep);
    bottom.add(treatSlice);
    add(bottom);
  }


  // -- API METHODS --

  /** Returns the selected series of files in array form. */
  public File[] getSeries() {
    String p = prefix.getText();
    String c = count.getText();
    String t = (String) type.getSelectedItem();
    int num = 0;
    try { num = Integer.parseInt(c); }
    catch (NumberFormatException exc) { }
    boolean dot = (t != null && !t.equals(""));
    if (dot) t = "." + t;

    File[] series;
    if (num < 1) {
      // single file
      series = new File[1];
      String name = p + (dot ? t : "");
      series[0] = new File(name);
    }
    else {
      // series of files
      series = new File[num];
      for (int i=0; i<num; i++) {
        String name = p + (i + 1) + (dot ? t : "");
        series[i] = new File(name);
      }
    }
    return series;
  }

  /** Gets the prefix of the selected file series. */
  public String getPrefix() { return prefix.getText(); }

  /** Gets whether to treat each file as a slice instead of as a timestep. */
  public boolean getFilesAsSlices() { return treatSlice.isSelected(); }

  /** Blanks out the text fields. */
  public void clearFields() {
    prefix.setText("");
    count.setText("");
    type.setSelectedItem(types[0]);
  }

}
