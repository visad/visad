//
// BioSpinWidget.java
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
import java.net.URL;
import java.util.Vector;
import javax.swing.*;
import javax.swing.event.*;

/** BioSpinWidget is a simple widget for controlling animation. */
public class BioSpinWidget extends JPanel implements MouseListener {

  // -- GUI COMPONENTS --

  private JTextField val;
  private JButton up;
  private JButton down;


  // -- OTHER FIELDS --

  private Vector listeners;
  private int min, max, value;
  private boolean upPressed, downPressed;
  private int pressTime;


  // -- CONSTRUCTOR --

  /** Constructs a new animation widget. */
  public BioSpinWidget(int minimum, int maximum, int starting) {
    listeners = new Vector();
    min = minimum;
    max = maximum;
    value = starting;
    val = new JTextField("" + value);
    val.setEditable(false);
    FontMetrics fm = getFontMetrics(val.getFont());
    Dimension size = new Dimension(fm.stringWidth("x" + max), fm.getHeight());
    val.setPreferredSize(size);
    val.setMaximumSize(size);
    URL upImg = getClass().getResource("up.gif");
    URL downImg = getClass().getResource("down.gif");
    ImageIcon upIcon = null, downIcon = null;
    if (upImg != null && downImg != null) {
      upIcon = new ImageIcon(upImg);
      downIcon = new ImageIcon(downImg);
    }
    if (upIcon == null || downIcon == null) {
      up = new JButton();
      down = new JButton();
    }
    else {
      up = new JButton(upIcon);
      down = new JButton(downIcon);
    }
    up.setPreferredSize(new Dimension(
      upIcon.getIconWidth() + 2, upIcon.getIconHeight() + 2));
    down.setPreferredSize(new Dimension(
      downIcon.getIconWidth() + 2, downIcon.getIconHeight() + 2));
    up.addMouseListener(this);
    down.addMouseListener(this);
    setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
    add(val);
    add(Box.createHorizontalStrut(3));
    JPanel p = new JPanel();
    p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
    p.add(up);
    p.add(down);
    add(p);
  }


  // -- API METHODS --

  /** Gets the current value of the spinner. */
  public int getValue() { return value; }

  /** Sets the current value of the spinner. */
  public void setValue(int value) {
    if (this.value == value) return;
    this.value = value;
    updateWidget();
  }

  /** Adds the given change listener to this widget. */
  public void addChangeListener(ChangeListener l) { listeners.add(l); }

  /** Removes the given change listener from this widget. */
  public void removeChangeListener(ChangeListener l) { listeners.remove(l); }

  /** Enables or disables this widget. */
  public void setEnabled(boolean enabled) {
    val.setEnabled(enabled);
    up.setEnabled(enabled);
    down.setEnabled(enabled);
  }


  // -- INTERNAL API METHODS --

  /** Called when mouse button pressed. */
  public void mousePressed(MouseEvent e) {
    Object source = e.getSource();
    if (source == up && up.isEnabled()) {
      upPressed = true;
      value++;
      if (value > max) value = max;
    }
    else if (source == down && down.isEnabled()) {
      downPressed = true;
      value--;
      if (value < min) value = min;
    }
    else return;
    startSpinThread();
  }

  /** Called when mouse button released. */
  public void mouseReleased(MouseEvent e) {
    Object source = e.getSource();
    if (source == up) upPressed = false;
    else if (source == down) downPressed = false;
  }

  public void mouseClicked(MouseEvent e) { }
  public void mouseEntered(MouseEvent e) { }
  public void mouseExited(MouseEvent e) { }


  // -- HELPER METHODS --

  /** Refreshes the GUI to match the linked animation control. */
  private void updateWidget() {
    val.setText("" + value);
    notifyListeners();
  }

  /** Notifies all listeners of a change to this widget. */
  private void notifyListeners() {
    int size = listeners.size();
    for (int i=0; i<size; i++) {
      ChangeListener l = (ChangeListener) listeners.elementAt(i);
      l.stateChanged(new ChangeEvent(this));
    }
  }

  /** Launches a separate thread to control spinning. */
  private void startSpinThread() {
    Thread t = new Thread(new Runnable() {
      public void run() {
        updateWidget();
        try { Thread.sleep(500); }
        catch (InterruptedException exc) { }
        while (true) {
          if (upPressed) {
            value++;
            if (value > max) value = max;
          }
          else if (downPressed) {
            value--;
            if (value < min) value = min;
          }
          else break;
          pressTime++;
          updateWidget();
          int waitTime = 75 - pressTime;
          if (waitTime < 1) waitTime = 1;
          try { Thread.sleep(waitTime); }
          catch (InterruptedException exc) { }
        }
        pressTime = 0;
      }
    });
    t.start();
  }

}
