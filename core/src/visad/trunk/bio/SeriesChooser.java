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

/** SeriesChooser provides a mechanism for selecting a series of files. */
public class SeriesChooser extends JPanel implements ActionListener {

  // -- CONSTANTS --

  /** Return value if approve (ok) is chosen. */
  public static final int APPROVE_OPTION = 1;

  /** Return value if cancel is chosen. */
  public static final int CANCEL_OPTION = 2;

  /** List of standard file series types. */
  private static final String[] types = {"PIC", "tiff", "gif", "jpg"};


  // -- FIELDS --

  /** Text field containing file prefix. */
  private JTextField prefix;

  /** Text field containing series count. */
  private JTextField count;

  /** Text field containing file extension. */
  private JComboBox type;

  /** Toggle for creation of low-resolution thumbnails. */
  private JCheckBox thumbs;

  /** Text field for number of megabytes to use for thumbnails. */
  private JTextField thumbSize;

  /** Label for thumbnail megabyte size. */
  private JLabel thumbLabel;

  /** Ok button. */
  private JButton ok;

  /** Currently visible dialog. */
  private JDialog dialog;

  /** Return value of dialog. */
  private int rval;


  // -- CONSTRUCTOR --

  /** Creates a file series chooser dialog. */
  public SeriesChooser() {
    // create panels
    JPanel top = new JPanel();
    JPanel mid1 = new JPanel();
    JPanel mid2 = new JPanel();
    JPanel bottom = new JPanel();
    JPanel mid1Left = new JPanel();
    JPanel mid1Right = new JPanel();
    setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
    top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));
    mid1.setLayout(new BoxLayout(mid1, BoxLayout.X_AXIS));
    mid2.setLayout(new BoxLayout(mid2, BoxLayout.X_AXIS));
    bottom.setLayout(new BoxLayout(bottom, BoxLayout.X_AXIS));
    mid1Left.setLayout(new BoxLayout(mid1Left, BoxLayout.Y_AXIS));
    mid1Right.setLayout(new BoxLayout(mid1Right, BoxLayout.Y_AXIS));
    top.setAlignmentX(JPanel.LEFT_ALIGNMENT);
    mid1.setAlignmentX(JPanel.LEFT_ALIGNMENT);
    mid2.setAlignmentX(JPanel.LEFT_ALIGNMENT);
    bottom.setAlignmentX(JPanel.LEFT_ALIGNMENT);

    // create labels
    JLabel l1 = new JLabel("File prefix");
    JLabel l2 = new JLabel("Count");
    JLabel l3 = new JLabel("Type");
    thumbLabel = new JLabel(" MB");
    l1.setForeground(Color.black);
    l2.setForeground(Color.black);
    l3.setForeground(Color.black);
    thumbLabel.setForeground(Color.black);

    // create text fields
    prefix = new JTextField();
    count = new JTextField();
    thumbSize = new JTextField("  32");
    Vector items = new Vector(types.length);
    for (int i=0; i<types.length; i++) items.add(types[i]);
    type = new JComboBox(items);
    prefix.setAlignmentX(JLabel.LEFT_ALIGNMENT);
    count.setAlignmentX(JLabel.LEFT_ALIGNMENT);
    type.setAlignmentX(JLabel.LEFT_ALIGNMENT);
    Util.adjustTextField(prefix);
    Util.adjustTextField(thumbSize);
    type.setEditable(true);

    // create check box
    thumbs = new JCheckBox("Create low-resolution thumbnails", true);
    thumbs.addItemListener(new ItemListener() {
      public void itemStateChanged(ItemEvent e) {
        boolean enabled = thumbs.isSelected();
        thumbSize.setEnabled(enabled);
        thumbLabel.setEnabled(enabled);
      }
    });

    // create buttons
    JButton select = new JButton("Choose file");
    ok = new JButton("Ok");
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
    add(mid1);
    add(mid2);
    add(bottom);
    top.add(l1);
    top.add(prefix);
    mid1.add(mid1Left);
    mid1.add(mid1Right);
    mid2.add(thumbs);
    mid2.add(thumbSize);
    mid2.add(thumbLabel);
    bottom.add(Box.createHorizontalGlue());
    bottom.add(select);
    bottom.add(ok);
    bottom.add(cancel);
    bottom.add(Box.createHorizontalGlue());
    mid1Left.add(l2);
    mid1Left.add(count);
    mid1Right.add(l3);
    mid1Right.add(type);
  }


  // -- API METHODS --

  /** Displays a dialog using this series chooser. */
  public int showDialog(Frame parent) {
    dialog = new JDialog(parent, "Open file series", true);
    dialog.getRootPane().setDefaultButton(ok);
    prefix.setText("");
    count.setText("");
    type.setSelectedItem(types[0]);
    dialog.setContentPane(this);
    dialog.pack();
    Util.centerWindow(dialog);
    dialog.setVisible(true);
    return rval;
  }

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

  /** Gets whether to make low-resolution thumbnails. */
  public boolean getThumbs() { return thumbs.isSelected(); }

  /** Gets number of megabytes to use for low-resolution thumbnails. */
  public int getThumbSize() {
    int size = -1;
    try { size = Integer.parseInt(thumbSize.getText().trim()); }
    catch (NumberFormatException exc) { }
    return size;
  }


  // -- INTERNAL API METHODS --

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

      // determine file extension
      String name = file.getPath();
      int dot = name.lastIndexOf(".");
      String ext;
      if (dot >= 0) {
        ext = name.substring(dot + 1);
        name = name.substring(0, dot);
      }
      else ext = "";

      // determine file prefix
      boolean series = false;
      int i = name.length();
      while (i > 0) {
        char last = name.charAt(--i);
        if (last < '0' || last > '9') break;
        series = true;
      }
      String prefix = name.substring(0, i + 1);

      // determine series count
      String count;
      if (series) {
        File dir = file.getParentFile();
        int maxFiles = dir.list(filter).length;
        int min = 1;
        int guess = maxFiles / 2;
        int max = maxFiles;
        String end = (ext.equals("") ? "" : ".") + ext;
        while (min != max) {
          File top = new File(prefix + guess + end);
          File top1 = new File(prefix + (guess + 1) + end);
          boolean exists = top.exists();
          boolean exists1 = top1.exists();
          if (!exists) {
            // guess is too high
            max = guess;
          }
          else if (exists1) {
            // guess is too low
            min = guess;
          }
          else break;
          guess = (min + max) / 2;
        }
        count = "" + guess;
      }
      else count = "";

      // fill in text fields appropriately
      this.prefix.setText(prefix);
      this.count.setText(count);
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
