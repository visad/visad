//
// Widget.java
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

package visad.browser;

import java.awt.*;
import java.awt.event.*;
import java.util.Vector;

/** Abstract superclass for all browser widgets */
public abstract class Widget extends Panel {

  /** vector of widget listeners */
  private Vector listeners = new Vector();

  /** constructs a new Widget */
  public Widget(WidgetEvent e) { }

  /** adds a component to the applet with the specified constraints */
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

  /** pops up a frame to test this widget */
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

  /** adds a widget listener */
  public void addWidgetListener(WidgetListener l) {
    synchronized (listeners) {
      listeners.addElement(l);
    }
  }

  /** removes a widget listener */
  public void removeWidgetListener(WidgetListener l) {
    synchronized (listeners) {
      listeners.removeElement(l);
    }
  }

  /** notifies all widget listeners of the given widget event */
  public void notifyListeners(WidgetEvent e) {
    synchronized (listeners) {
      for (int i=0; i<listeners.size(); i++) {
        WidgetListener l = (WidgetListener) listeners.elementAt(i);
        l.widgetChanged(e);
      }
    }
  }

  /** update widget based on information from the given WidgetEvent */
  public abstract void updateWidget(WidgetEvent e);

}
