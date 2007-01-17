//
// Widget.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2007 Bill Hibbard, Curtis Rueden, Tom
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

package visad.browser;

import java.awt.*;
import java.awt.event.*;
import java.util.Vector;

/**
 * Abstract superclass for all browser widgets.
 */
public abstract class Widget extends Panel {

  /**
   * Debugging flag.
   */
  protected static final boolean DEBUG = false;

  /**
   * Coded string value for true.
   */
  protected static final String TRUE = "T";

  /**
   * Coded string value for false.
   */
  protected static final String FALSE = "F";

  protected static final Color PALE_GRAY = new Color(0.8f, 0.8f, 0.8f);

  /**
   * Vector of widget listeners.
   */
  private Vector listeners = new Vector();

  /**
   * Returns true if (px, py) is inside (x, y, w, h)
   */
  public static boolean containedIn(int px, int py,
    int x, int y, int w, int h)
  {
    return new Rectangle(x, y, w, h).contains(px, py);
  }

  /**
   * Performs GUI setup common to all widgets.
   */
  public Widget() {
    setBackground(PALE_GRAY);
  }

  /**
   * Adds a component to the applet with the specified constraints.
   */
  protected void addComponent(Component c, GridBagLayout layout,
    int x, int y, int w, int h, double wx, double wy)
  {
    GridBagConstraints gbc = new GridBagConstraints();
    gbc.gridx = x;
    gbc.gridy = y;
    gbc.gridwidth = w;
    gbc.gridheight = h;
    gbc.fill = GridBagConstraints.BOTH;
    gbc.weightx = wx;
    gbc.weighty = wy;
    layout.setConstraints(c, gbc);
    add(c);
  }

  /**
   * Pops up a frame to test this widget.
   */
  protected void testWidget() {
    String title = getClass().getName();
    title = title.substring(title.lastIndexOf('.') + 1);
    Frame f = new Frame(title);
    f.addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent e) {
        System.exit(0);
      }
    });
    f.add(this);
    f.pack();
    f.show();
  }

  /**
   * Adds a widget listener.
   */
  public void addWidgetListener(WidgetListener l) {
    synchronized (listeners) {
      listeners.addElement(l);
    }
  }

  /**
   * Removes a widget listener.
   */
  public void removeWidgetListener(WidgetListener l) {
    synchronized (listeners) {
      listeners.removeElement(l);
    }
  }

  /**
   * Notifies all widget listeners of the given widget event.
   */
  public void notifyListeners(WidgetEvent e) {
    synchronized (listeners) {
      for (int i=0; i<listeners.size(); i++) {
        WidgetListener l = (WidgetListener) listeners.elementAt(i);
        l.widgetChanged(e);
      }
    }
  }

  /**
   * Gets a string representing this widget's current state.
   */
  public abstract String getSaveString();

  /**
   * Reconstructs this widget's state using the specified save string.
   */
  public abstract void setSaveString(String save);

}
