//
// BioHelpWindow.java
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
import java.io.*;
import java.net.URL;
import javax.swing.*;
import visad.util.Util;

/** BioHelpWindow details basic VisBio program usage. */
public class BioHelpWindow extends JPanel implements ActionListener {

  // -- CONSTANTS --

  /** Default width of help window in pixels. */
  private static final int DEFAULT_WIDTH = 400;


  // -- FIELDS --

  /** Ok button. */
  private JButton ok;

  /** Currently visible window. */
  private JFrame window;


  // -- CONSTRUCTOR --

  /** Creates a file series import dialog. */
  public BioHelpWindow() {
    // create components
    JTabbedPane tabs = new JTabbedPane();

    addTab(tabs, "Overview", "overview.html");
    addTab(tabs, "About", "about.html");

    ok = new JButton("Close");
    ok.setMnemonic('c');
    ok.setActionCommand("ok");
    ok.addActionListener(this);

    // lay out components
    setLayout(new BorderLayout());
    add(tabs, BorderLayout.CENTER);
    JPanel p = new JPanel();
    p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
    p.add(Box.createHorizontalGlue());
    p.add(ok);
    p.add(Box.createHorizontalGlue());
    add(BorderLayout.SOUTH, p);
  }


  // -- API METHODS --

  /** Displays the dialog onscreen. */
  public void showWindow(Frame parent) {
    window = new JFrame("VisBio Help");
    window.getRootPane().setDefaultButton(ok);
    window.setContentPane(this);
    Point loc = parent.getLocation();
    Dimension size = parent.getSize();
    Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
    int x = loc.x + size.width;
    if (x + DEFAULT_WIDTH > screen.width) x = screen.width - DEFAULT_WIDTH;
    window.setLocation(x, loc.y);
    window.setSize(DEFAULT_WIDTH, size.height);
    window.setVisible(true);
  }


  // -- INTERNAL API METHODS --

  /** Handles button press events. */
  public void actionPerformed(ActionEvent e) { window.setVisible(false); }


  // -- HELPER METHODS --

  /**
   * Adds a tab to the given JTabbedPane with the given title,
   * with content from the specified file.
   */
  private static void addTab(JTabbedPane tabs, String title, String file) {
    JEditorPane editor = null;
    try { editor = new JEditorPane(new File(file).toURL()); }
    catch (IOException exc) { exc.printStackTrace(); }
    editor.setEditable(false);
    JScrollPane scroll = new JScrollPane(editor);
    tabs.addTab(title, scroll);
  }

}
