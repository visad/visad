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
    JPanel mid = new JPanel();
    JPanel bottom = new JPanel();
    JPanel midLeft = new JPanel();
    JPanel midRight = new JPanel();
    setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
    top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));
    mid.setLayout(new BoxLayout(mid, BoxLayout.X_AXIS));
    bottom.setLayout(new BoxLayout(bottom, BoxLayout.X_AXIS));
    midLeft.setLayout(new BoxLayout(midLeft, BoxLayout.Y_AXIS));
    midRight.setLayout(new BoxLayout(midRight, BoxLayout.Y_AXIS));
    top.setAlignmentX(JPanel.LEFT_ALIGNMENT);
    mid.setAlignmentX(JPanel.LEFT_ALIGNMENT);
    bottom.setAlignmentX(JPanel.LEFT_ALIGNMENT);

    // create labels
    JLabel l1 = new JLabel("File prefix");
    JLabel l2 = new JLabel("Count");
    JLabel l3 = new JLabel("Type");
    l1.setForeground(Color.black);
    l2.setForeground(Color.black);
    l3.setForeground(Color.black);

    // create text fields
    prefix = new JTextField();
    count = new JTextField();
    Vector items = new Vector(types.length);
    for (int i=0; i<types.length; i++) items.add(types[i]);
    type = new JComboBox(items);
    prefix.setAlignmentX(JLabel.LEFT_ALIGNMENT);
    count.setAlignmentX(JLabel.LEFT_ALIGNMENT);
    type.setAlignmentX(JLabel.LEFT_ALIGNMENT);
    Util.adjustTextField(prefix);
    type.setEditable(true);

    // create buttons
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
    add(top);
    add(mid);
    add(bottom);
    top.add(l1);
    top.add(prefix);
    mid.add(midLeft);
    mid.add(midRight);
    bottom.add(select);
    bottom.add(ok);
    bottom.add(cancel);
    midLeft.add(l2);
    midLeft.add(count);
    midRight.add(l3);
    midRight.add(type);
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

  /** Returns the selected series of files in array form. */
  public File[] getSeries() {
    String p = prefix.getText();
    String c = count.getText();
    String t = (String) type.getSelectedItem();
    int num = 1;
    try {
      num = Integer.parseInt(c);
      if (num < 1) num = 1;
    }
    catch (NumberFormatException exc) { }
    boolean dot = (t != null && !t.equals(""));
    if (dot) t = "." + t;
    File[] series = new File[num];
    for (int i=0; i<num; i++) {
      String name = p + (i + 1) + (dot ? t : "");
      series[i] = new File(name);
    }
    return series;
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
        prefix = name.substring(0, i + 1);
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
      while (min != max) {
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
