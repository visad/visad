//
// ExportDialog.java
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

/** ExportDialog provides a set of options for exporting a 4-D data series. */
public class ExportDialog extends JPanel implements ActionListener {

  // -- CONSTANTS --

  /** Return value if approve (ok) is chosen. */
  public static final int APPROVE_OPTION = 1;

  /** Return value if cancel is chosen. */
  public static final int CANCEL_OPTION = 2;


  // -- FIELDS --

  /** Series chooser. */
  private SeriesChooser chooser;

  /** Ok button. */
  private JButton ok;

  /** Currently visible dialog. */
  private JDialog dialog;

  /** Return value of dialog. */
  private int rval;


  // -- CONSTRUCTOR --

  /** Creates a file series chooser dialog. */
  public ExportDialog() {
    // main components
    chooser = new SeriesChooser();
    ok = new JButton("Ok");
    ok.setMnemonic('o');
    ok.setActionCommand("ok");
    ok.addActionListener(this);
    JButton cancel = new JButton("Cancel");
    cancel.setMnemonic('c');
    cancel.setActionCommand("cancel");
    cancel.addActionListener(this);
    JButton more = new JButton("More options");
    more.setMnemonic('m');
    more.setActionCommand("more");
    more.addActionListener(this);

    // options
    JLabel optionLabel = new JLabel("Options");
    JCheckBox doColors = new JCheckBox("Save color adjustments", true);
    JCheckBox doAlign = new JCheckBox("Save alignment", true);
    JCheckBox timeOnly = new JCheckBox("Save current timestep only");
    JCheckBox sliceOnly = new JCheckBox("Save current slice only");

    // output format
    JLabel outputLabel = new JLabel("Output format");
    JRadioButton picFormat = new JRadioButton("Bio-Rad PIC", true);
    JRadioButton tiffFormat = new JRadioButton("Multi-page TIFF");
    JRadioButton qtFormat = new JRadioButton("QuickTime movie");
    JRadioButton bmpFormat = new JRadioButton("BMP image");
    ButtonGroup group = new ButtonGroup();
    group.add(picFormat);
    group.add(tiffFormat);
    group.add(qtFormat);
    group.add(bmpFormat);

    // additional options
    JLabel moreLabel = new JLabel("Additional options");
    DoubleTextCheckBox altRes = new DoubleTextCheckBox(
      "Use alternate resolution", "by", "", "", false);
    DoubleTextCheckBox exportTimesteps = new DoubleTextCheckBox(
      "Only export timesteps", "through", "", "", false);
    DoubleTextCheckBox exportSlices = new DoubleTextCheckBox(
      "Only export slices", "through", "", "", false);

    // lay out output format
    setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
    JPanel top = new JPanel();
    top.setLayout(new BoxLayout(top, BoxLayout.X_AXIS));
    JPanel topLeft = new JPanel();
    topLeft.setLayout(new BoxLayout(topLeft, BoxLayout.Y_AXIS));
    topLeft.add(outputLabel);
    topLeft.add(picFormat);
    topLeft.add(tiffFormat);
    topLeft.add(qtFormat);
    topLeft.add(bmpFormat);
    top.add(topLeft);

    // lay out options
    JPanel topRight = new JPanel();
    topRight.setLayout(new BoxLayout(topRight, BoxLayout.Y_AXIS));
    topRight.add(optionLabel);
    topRight.add(doColors);
    topRight.add(doAlign);
    topRight.add(timeOnly);
    topRight.add(sliceOnly);
    top.add(topRight);
    add(top);

    // lay out series chooser
    add(chooser);

    // lay out buttons
    JPanel mid = new JPanel();
    mid.setLayout(new BoxLayout(mid, BoxLayout.X_AXIS));
    mid.add(ok);
    mid.add(cancel);
    mid.add(more);
    add(ToolPanel.pad(mid));

    // lay out additional options
    JPanel bottom = new JPanel();
    bottom.setLayout(new BoxLayout(bottom, BoxLayout.Y_AXIS));
    bottom.add(altRes);
    bottom.add(exportSlices);
    bottom.add(exportTimesteps);
    add(bottom);
  }


  // -- API METHODS --

  /** Displays a dialog using this series chooser. */
  public int showDialog(Frame parent) {
    dialog = new JDialog(parent, "Save file series", true);
    dialog.getRootPane().setDefaultButton(ok);
    chooser.clearFields();
    dialog.setContentPane(this);
    dialog.pack();
    Util.centerWindow(dialog);
    dialog.setVisible(true);
    return rval;
  }


  // -- INTERNAL API METHODS --

  /** Handles button press events. */
  public void actionPerformed(ActionEvent e) {
    String command = e.getActionCommand();
    if (command.equals("ok")) {
      rval = APPROVE_OPTION;
      dialog.setVisible(false);
    }
    else if (command.equals("cancel")) {
      rval = CANCEL_OPTION;
      dialog.setVisible(false);
    }
  }

}
