//
// GroupDialog.java
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
import visad.util.Util;

/** GroupDialog provides a mechanism for selecting a series of files. */
public class GroupDialog extends JPanel implements ActionListener {

  /** Return value if approve (ok) is chosen. */
  public static final int APPROVE_OPTION = 1;

  /** Return value if cancel is chosen. */
  public static final int CANCEL_OPTION = 2;

  /** Text field containing file prefix. */
  private JTextField name;

  /** Ok button. */
  private JButton ok;

  /** Creates a file series chooser dialog. */
  public GroupDialog() {
    // create panels
    JPanel top = new JPanel();
    JPanel bottom = new JPanel();
    setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
    top.setLayout(new BoxLayout(top, BoxLayout.X_AXIS));
    bottom.setLayout(new BoxLayout(bottom, BoxLayout.X_AXIS));

    // create labels
    JLabel l1 = new JLabel("Group name");
    l1.setForeground(Color.black);

    // create text fields
    name = new JTextField();
    Util.adjustTextField(name);

    // create buttons
    ok = new JButton("Ok");
    JButton cancel = new JButton("Cancel");
    ok.setMnemonic('o');
    cancel.setMnemonic('c');

    // set up component events
    ok.setActionCommand("ok");
    cancel.setActionCommand("cancel");
    ok.addActionListener(this);
    cancel.addActionListener(this);

    // lay out components
    add(top);
    add(bottom);
    top.add(l1);
    top.add(name);
    bottom.add(ok);
    bottom.add(cancel);
  }

  /** Currently visible dialog. */
  private JDialog dialog;

  /** Return value of dialog. */
  private int rval;

  /** Displays a dialog using this series chooser. */
  public int showDialog(Frame parent) {
    dialog = new JDialog(parent, "Create new group", true);
    dialog.getRootPane().setDefaultButton(ok);
    name.setText("");
    dialog.setContentPane(this);
    dialog.pack();
    Util.centerWindow(dialog);
    dialog.setVisible(true);
    return rval;
  }

  /** Returns the group name. */
  public String getGroupName() { return name.getText(); }

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
