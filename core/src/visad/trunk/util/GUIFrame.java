//
// GUIFrame.java
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

package visad.util;

import java.awt.event.*;
import java.lang.reflect.*;
import java.util.Hashtable;
import javax.swing.*;

/** A general-purpose frame for simplifing GUI construction and management. */
public class GUIFrame extends JFrame implements ActionListener {

  /** menu bar */
  JMenuBar menubar;

  /** hashtable */
  Hashtable hash;

  /** constructs a GUIFrame */
  public GUIFrame() {
    menubar = new JMenuBar();
    setJMenuBar(menubar);
    hash = new Hashtable();
  }

  /** gets the JMenu corresponding to the given menu name */
  protected JMenu getMenu(String menu) {
    // get menu from hashtable
    JMenu m = (JMenu) hash.get(menu);
    if (m == null) {
      m = new JMenu(menu);
      m.setMnemonic(menu.charAt(0));
      menubar.add(m);
      hash.put(menu, m);
    }
    return m;
  }

  /** gets the JMenuItem corresponding to the given menu and item name */
  public JMenuItem getMenuItem(String menu, String item) {
    // get menu item from hashtable
    JMenuItem x = (JMenuItem) hash.get(menu + "\n" + item);
    return x;
  }

  /** adds the given menu item to the specified menu */
  public void addMenuItem(String menu, String item, String command,
    char mnemonic)
  {
    // add menu item to menu
    JMenu m = getMenu(menu);
    JMenuItem x = new JMenuItem(item);
    x.setMnemonic(mnemonic);
    x.setActionCommand(command);
    x.addActionListener(this);
    m.add(x);
    hash.put(menu + "\n" + item, x);
  }

  /** adds a separator to the specified menu */
  public void addMenuSeparator(String menu) {
    JMenu m = getMenu(menu);
    m.addSeparator();
  }

  /** handle menu item actions */
  public void actionPerformed(ActionEvent e) {
    // convert command name to method
    String command = e.getActionCommand();
    Method method = null;
    try {
      method = getClass().getMethod(command, null);
    }
    catch (NoSuchMethodException exc) { }
    catch (SecurityException exc) { }

    // execute the method
    if (method != null) {
      try {
        method.invoke(this, null);
      }
      catch (IllegalAccessException exc) { }
      catch (IllegalArgumentException exc) { }
      catch (InvocationTargetException exc) { }
    }
  }

}
