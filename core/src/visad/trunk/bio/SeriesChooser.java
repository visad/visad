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

/** SeriesChooser provides a mechanism for specifying a 4-D data series. */
public class SeriesChooser extends JPanel {

  // -- CONSTANTS --

  /** List of standard file series types. */
  private static final String[] types = {"PIC", "TIFF", "MOV", "BMP"};


  // -- FIELDS --

  /** Text field containing file prefix. */
  JTextField prefix;

  /** Text field containing series count start. */
  JTextField start;

  /** Text field containing series count end. */
  JTextField end;

  /** Text field containing file extension. */
  JComboBox type;

  /** Label for how files should be interpreted. */
  private JLabel treatLabel;

  /** Radio button for treating each file as a timestep. */
  JRadioButton treatTimestep;

  /** Radio button for treating each file as a slice. */
  JRadioButton treatSlice;


  // -- CONSTRUCTOR --

  /** Creates a file series chooser component. */
  public SeriesChooser() {
    // create labels
    JLabel l1 = new JLabel("File prefix");
    JLabel l2 = new JLabel("Start");
    JLabel l3 = new JLabel("End");
    JLabel l4 = new JLabel("Extension");
    treatLabel = new JLabel("Treat each file as a:");
    l1.setForeground(Color.black);
    l2.setForeground(Color.black);
    l3.setForeground(Color.black);
    l4.setForeground(Color.black);
    treatLabel.setForeground(Color.black);

    // create text fields
    prefix = new JTextField() {
      public Dimension getPreferredSize() {
        Dimension d = super.getPreferredSize();
        d.width = 150;
                
        return d;
      }
    };
    start = new JTextField();
    end = new JTextField();
    Vector items = new Vector(types.length);
    for (int i=0; i<types.length; i++) items.add(types[i]);
    type = new JComboBox(items) {
      public Dimension getPreferredSize() {
        Dimension d = super.getPreferredSize();
        d.width = 60;
        return d;
      }
    };
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
    JPanel top = new JPanel();
    top.setLayout(new BoxLayout(top, BoxLayout.X_AXIS));
    JPanel topLeft = new JPanel();
    topLeft.setLayout(new BoxLayout(topLeft, BoxLayout.Y_AXIS));
    topLeft.add(ToolPanel.pad(l1, false, true));
    topLeft.add(prefix);
    top.add(topLeft);
    JPanel topMid1 = new JPanel();
    topMid1.setLayout(new BoxLayout(topMid1, BoxLayout.Y_AXIS));
    topMid1.add(ToolPanel.pad(l2, false, true));
    topMid1.add(start);
    top.add(topMid1);
    JPanel topMid2 = new JPanel();
    topMid2.setLayout(new BoxLayout(topMid2, BoxLayout.Y_AXIS));
    topMid2.add(ToolPanel.pad(l3, false, true));
    topMid2.add(end);
    top.add(topMid2);
    JPanel topRight = new JPanel();
    topRight.setLayout(new BoxLayout(topRight, BoxLayout.Y_AXIS));
    topRight.add(ToolPanel.pad(l4, false, true));
    topRight.add(type);
    top.add(topRight);
    add(top);

    // lay out bottom components
    JPanel bottom = new JPanel();
    bottom.setLayout(new BoxLayout(bottom, BoxLayout.X_AXIS));
    bottom.add(ToolPanel.pad(treatLabel, false, true));
    bottom.add(treatTimestep);
    bottom.add(treatSlice);
    add(bottom);
  }


  // -- API METHODS --

  /** Returns the selected series of files in array form. */
  public File[] getSeries() {
    String p = prefix.getText();
    String s = start.getText();
    String e = end.getText();
    String t = (String) type.getSelectedItem();
    int first = -1, last = -1;
    try {
      first = Integer.parseInt(s);
      last = Integer.parseInt(e);
    }
    catch (NumberFormatException exc) { }
    boolean dot = (t != null && !t.equals(""));
    if (dot) t = "." + t;

    File[] series;
    if (first < 0 || last < 0) {
      // single file
      series = new File[1];
      String name = p + (dot ? t : "");
      series[0] = new File(name);
    }
    else {
      // series of files
      int count = last - first + 1;
      series = new File[count];
      int c = first;
      for (int i=0; i<count; i++, c++) {
        String name = p + c + (dot ? t : "");
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
    start.setText("");
    end.setText("");
    type.setSelectedItem(types[0]);
  }

}
