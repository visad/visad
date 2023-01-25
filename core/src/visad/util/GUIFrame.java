//
// GUIFrame.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2023 Bill Hibbard, Curtis Rueden, Tom
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

import java.awt.Toolkit;
import java.awt.event.*;
import java.lang.reflect.*;
import java.util.Hashtable;
import javax.swing.*;

/** A general-purpose frame for simplifing GUI construction and management. */
public class GUIFrame extends JFrame implements ActionListener {

  /** key mask for use with keyboard shortcuts on this operating system */
  public static final int MENU_MASK =
    Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();

  /** menu bar */
  protected JMenuBar menubar;

  /** hashtable */
  protected Hashtable hash;

  /** heavyweight flag */
  protected boolean heavy;

  /** constructs a GUIFrame */
  public GUIFrame() {
    this(false);
  }

  /** constructs a GUIFrame with light- or heavy-weight menus as specified */
  public GUIFrame(boolean heavyweight) {
    menubar = new JMenuBar();
    setJMenuBar(menubar);
    hash = new Hashtable();
    heavy = heavyweight;
  }

  /** gets the JMenu corresponding to the given menu name */
  public JMenu getMenu(String menu) {
    // get menu from hashtable
    JMenu m = (JMenu) hash.get(menu);
    if (m == null) {
      m = new JMenu(menu);
      m.setMnemonic(menu.charAt(0));
      m.getPopupMenu().setLightWeightPopupEnabled(!heavy);
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
  public JMenuItem addMenuItem(String menu, String item, String command,
    char mnemonic)
  {
    JMenuItem x = new JMenuItem(item);
    addMenuItem(menu, x, command, mnemonic, true);
    return x;
  }

  /** adds the given menu item to the specified menu */
  public JMenuItem addMenuItem(String menu, String item, String command,
    char mnemonic, boolean enabled)
  {
    JMenuItem x = new JMenuItem(item);
    addMenuItem(menu, x, command, mnemonic, enabled);
    return x;
  }

  /** adds the given menu item to the specified menu */
  public void addMenuItem(String menu, JMenuItem item, String command,
    char mnemonic, boolean enabled)
  {
    // add menu item to menu
    JMenu m = getMenu(menu);
    item.setMnemonic(mnemonic);
    item.setActionCommand(command);
    item.addActionListener(this);
    item.setEnabled(enabled);
    m.add(item);
    hash.put(menu + "\n" + item.getText(), item);
  }

  /** adds the given sub-menu to the specified menu */
  public JMenu addSubMenu(String menu, String sub, char mnemonic) {
    JMenu x = new JMenu(sub);
    addSubMenu(menu, x, mnemonic, true);
    return x;
  }

  /** adds the given sub-menu to the specified menu */
  public JMenu addSubMenu(String menu, String sub, char mnemonic,
    boolean enabled)
  {
    JMenu x = new JMenu(sub);
    addSubMenu(menu, x, mnemonic, enabled);
    return x;
  }

  /** adds the given sub-menu to the specified menu */
  public void addSubMenu(String menu, JMenu sub, char mnemonic,
    boolean enabled)
  {
    // add sub-menu to menu
    JMenu m = getMenu(menu);
    sub.setMnemonic(mnemonic);
    sub.getPopupMenu().setLightWeightPopupEnabled(!heavy);
    sub.setEnabled(enabled);
    m.add(sub);
    hash.put(sub.getText(), sub);
  }

  /** adds a separator to the specified menu */
  public void addMenuSeparator(String menu) {
    JMenu m = getMenu(menu);
    m.addSeparator();
  }

  /** sets the keyboard shortcut for the given menu item */
  public void setMenuShortcut(String menu, String item, int keycode) {
    JMenuItem jmi = getMenuItem(menu, item);
    if (jmi == null) return;
    jmi.setAccelerator(KeyStroke.getKeyStroke(keycode, MENU_MASK));
  }

  /** handles menu item actions */
  public void actionPerformed(ActionEvent e) {
    // convert command name to method
    String command = e.getActionCommand();
    Method method = null;
    try {
      method = getClass().getMethod(command, (Class[]) null);
    }
    catch (NoSuchMethodException exc) { exc.printStackTrace(); }
    catch (SecurityException exc) { exc.printStackTrace(); }

    // execute the method
    if (method != null) {
      try {
        method.invoke(this, (Object[]) null);
      }
      catch (IllegalAccessException exc) { exc.printStackTrace(); }
      catch (IllegalArgumentException exc) { exc.printStackTrace(); }
      catch (InvocationTargetException exc) { exc.printStackTrace(); }
    }
  }

}
