//
// ImportDialog.java
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
import java.math.BigInteger;
import java.util.Vector;
import javax.swing.*;
import visad.util.Util;

/** ImportDialog provides a set of options for importing a 4-D data series. */
public class ImportDialog extends JPanel implements ActionListener {

  // -- CONSTANTS --

  /** Return value if approve (ok) is chosen. */
  public static final int APPROVE_OPTION = 1;

  /** Return value if cancel is chosen. */
  public static final int CANCEL_OPTION = 2;


  // -- FIELDS --

  /** Series chooser. */
  private SeriesChooser chooser;

  /** Choose file dialog box. */
  private JFileChooser fileBox;

  /** Thumbnail widget. */
  private DoubleTextCheckBox thumbs;

  /** Ok button. */
  private JButton ok;

  /** Currently visible dialog. */
  private JDialog dialog;

  /** Return value of dialog. */
  private int rval;


  // -- CONSTRUCTOR --

  /** Creates a file series import dialog. */
  public ImportDialog() {
    // create components
    chooser = new SeriesChooser();
    thumbs = new DoubleTextCheckBox(
      "Create low-resolution thumbnails", "by", "64", "64", true);
    thumbs.setMnemonic('t');
    ok = new JButton("Ok");
    ok.setMnemonic('o');
    ok.setActionCommand("ok");
    ok.addActionListener(this);
    JButton cancel = new JButton("Cancel");
    cancel.setMnemonic('c');
    cancel.setActionCommand("cancel");
    cancel.addActionListener(this);
    JButton select = new JButton("Choose file");
    select.setMnemonic('f');
    select.setActionCommand("select");
    select.addActionListener(this);

    // lay out components
    setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
    add(chooser);
    add(thumbs);
    JPanel buttons = new JPanel();
    buttons.setLayout(new BoxLayout(buttons, BoxLayout.X_AXIS));
    buttons.add(select);
    buttons.add(ok);
    buttons.add(cancel);
    add(buttons);
  }


  // -- API METHODS --

  /** Displays the dialog onscreen. */
  public int showDialog(Frame parent) {
    dialog = new JDialog(parent, "Open file series", true);
    dialog.getRootPane().setDefaultButton(ok);
    chooser.clearFields();
    dialog.setContentPane(this);
    dialog.pack();
    Util.centerWindow(dialog);
    dialog.setVisible(true);
    return rval;
  }

  /** Gets the selected series of files in array form. */
  public File[] getSeries() { return chooser.getSeries(); }

  /** Gets the prefix of the selected file series. */
  public String getPrefix() { return chooser.getPrefix(); }

  /** Gets whether to treat each file as a slice instead of as a timestep. */
  public boolean getFilesAsSlices() { return chooser.getFilesAsSlices(); }

  /** Gets whether to make low-resolution thumbnails. */
  public boolean getThumbs() { return thumbs.isSelected(); }

  /** Gets X resolution for thumbnails. */
  public int getThumbResX() {
    int size = -1;
    try { size = Integer.parseInt(thumbs.getFirstValue().trim()); }
    catch (NumberFormatException exc) { }
    return size;
  }

  /** Gets Y resolution for thumbnails. */
  public int getThumbResY() {
    int size = -1;
    try { size = Integer.parseInt(thumbs.getSecondValue().trim()); }
    catch (NumberFormatException exc) { }
    return size;
  }


  // -- INTERNAL API METHODS --

  /** Handles button press events. */
  public void actionPerformed(ActionEvent e) {
    String command = e.getActionCommand();
    if (command.equals("select")) {
      if (fileBox == null) {
        fileBox = Util.getVisADFileChooser();
        SeriesFileFilter filter = new SeriesFileFilter();
        fileBox.setFileFilter(filter);
      }
      int returnVal = fileBox.showOpenDialog(this);
      if (returnVal != JFileChooser.APPROVE_OPTION) return;
      File file = fileBox.getSelectedFile();

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
      String first = "";
      String last = "";
      if (series) {
        String end = (ext.equals("") ? "" : ".") + ext;
        String snum = dot >= 0 ?
          name.substring(i + 1, dot) : name.substring(i + 1);
        BigInteger num = new BigInteger(snum);

        // determine whether series numbering width is fixed or dynamic
        boolean fixed;
        int width = snum.length();
        if (snum.startsWith("0")) fixed = true;
        else {
          char[] c = snum.toCharArray();
          c[0] = '0';
          for (i=1; i<c.length; i++) c[i] = '9';
          fixed = new File(prefix + new String(c) + end).exists();
        }

        // find lower series bound
        BigInteger min = num;
        while (min.compareTo(BigInteger.ZERO) > 0) {
          BigInteger min1 = min.subtract(BigInteger.ONE);
          String s = BioUtil.getString(min1, fixed, width);
          if (!new File(prefix + s + end).exists()) break;
          min = min1;
        }
        first = BioUtil.getString(min, fixed, width);

        // find upper series bound
        BigInteger max = num;
        while (true) {
          BigInteger max1 = max.add(BigInteger.ONE);
          String s = BioUtil.getString(max1, fixed, width);
          if (!new File(prefix + s + end).exists()) break;
          max = max1;
        }
        last = BioUtil.getString(max, fixed, width);
      }

      // fill in text fields appropriately
      chooser.prefix.setText(prefix);
      chooser.start.setText(first);
      chooser.end.setText(last);
      chooser.type.setSelectedItem(ext);
      if (chooser.type.getSelectedItem() == null) chooser.type.addItem(ext);
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
