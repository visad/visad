//
// SeriesChooser.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2000 Bill Hibbard, Curtis Rueden, Tom
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

/** SeriesChooser provides a mechanism for selecting a series of files. */
public class SeriesChooser extends JPanel implements ActionListener {

  /** List of standard file series types. */
  private static String[] types = {"PIC", "tiff", "gif", "jpg"};

  /** Return value if approve (ok) is chosen. */
  public static final int APPROVE_OPTION = 1;

  /** Return value if cancel is chosen. */
  public static final int CANCEL_OPTION = 2;

  /** Text field containing file prefix. */
  private JTextField prefix;

  /** Text field containing series count. */
  private JTextField count;

  /** Text field containing file extension. */
  private JComboBox type;

  /** Creates a file series chooser dialog. */
  public SeriesChooser() {
    // create panels
    JPanel top = new JPanel();
    JPanel bottom = new JPanel();
    JPanel topLeft = new JPanel();
    JPanel topMid = new JPanel();
    JPanel topRight = new JPanel();
    setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
    top.setLayout(new BoxLayout(top, BoxLayout.X_AXIS));
    bottom.setLayout(new BoxLayout(bottom, BoxLayout.X_AXIS));
    topLeft.setLayout(new BoxLayout(topLeft, BoxLayout.Y_AXIS));
    topMid.setLayout(new BoxLayout(topMid, BoxLayout.Y_AXIS));
    topRight.setLayout(new BoxLayout(topRight, BoxLayout.Y_AXIS));
    add(top);
    add(bottom);
    top.add(topLeft);
    top.add(topMid);
    top.add(topRight);

    // create first row of components
    prefix = new JTextField();
    count = new JTextField();
    Vector items = new Vector(types.length);
    for (int i=0; i<types.length; i++) items.add(types[i]);
    type = new JComboBox(items);
    Util.adjustTextField(prefix);
    Util.adjustTextField(count);
    type.setEditable(true);

    // create second row of components
    JButton select = new JButton("Choose file");
    JButton ok = new JButton("Ok");
    JButton cancel = new JButton("Cancel");
    select.setMnemonic('f');
    ok.setMnemonic('o');
    cancel.setMnemonic('c');

    // set up component events
    select.setActionCommand("select");
    ok.setActionCommand("ok");
    cancel.setActionCommand("cancel");
    select.addActionListener(this);
    ok.addActionListener(this);
    cancel.addActionListener(this);

    // lay out components
    topLeft.add(new JLabel("File prefix"));
    topLeft.add(prefix);
    topMid.add(new JLabel("Count"));
    topMid.add(count);
    topRight.add(new JLabel("Type"));
    topRight.add(type);
    bottom.add(select);
    bottom.add(ok);
    bottom.add(cancel);
  }

  /** Currently visible dialog. */
  private JDialog dialog;

  /** Return value of dialog. */
  private int rval;

  /** Displays a dialog using this series chooser. */
  public int showDialog(Frame parent) {
    dialog = new JDialog(parent, "Open file series", true);
    dialog.setContentPane(this);
    dialog.pack();
    dialog.setVisible(true);
    return rval;
  }

  public File[] getFileSeries() {
    // CTR: TODO
    return null;
  }

  /** Handles button press events. */
  public void actionPerformed(ActionEvent e) {
    String command = e.getActionCommand();
    if (command.equals("select")) {
      JFileChooser chooser = new JFileChooser();
      SeriesFileFilter filter = new SeriesFileFilter();
      chooser.setFileFilter(filter);
      int returnVal = chooser.showOpenDialog(dialog);
      if (returnVal != JFileChooser.APPROVE_OPTION) return;
      File file = chooser.getSelectedFile();

      // determine file prefix and extension
      String name = file.getPath();
      int dot = name.lastIndexOf(".");
      String prefix, ext;
      if (dot >= 0) {
        int i = dot;
        while (true) {
          if (i == 0) break;
          char last = name.charAt(--i);
          if (last < '0' || last > '9') break;
        }
        prefix = name.substring(0, i);
        ext = name.substring(dot + 1);
      }
      else {
        prefix = name;
        ext = "";
      }

      // determine series count
      File dir = file.getParentFile();
      int maxFiles = dir.list(filter).length;
      int min = 1;
      int count = maxFiles / 2;
      int max = maxFiles;
      String end = (ext.equals("") ? "" : ".") + ext;
      while (true) {
        File top = new File(prefix + count + end);
        File top1 = new File(prefix + (count + 1) + end);
        boolean exists = top.exists();
        boolean exists1 = top1.exists();
        if (!exists) {
          // guess is too high
          max = count;
        }
        else if (exists1) {
          // guess is too low
          min = count;
        }
        else break;
        /* CTR: START HERE: debug this loop. Print out each file checked. */
        count = (min + max) / 2;
      }

      // fill in text fields appropriately
      this.prefix.setText(prefix);
      this.count.setText("" + count);
      this.type.setSelectedItem(ext);
      if (this.type.getSelectedItem() == null) this.type.addItem(ext);
    }
    else if (command.equals("ok")) {
      rval = APPROVE_OPTION;
      dialog.setVisible(false);
    }
    else if (command.equals("cancel")) {
      rval = CANCEL_OPTION;
      dialog.setVisible(false);
    }
  }

}
