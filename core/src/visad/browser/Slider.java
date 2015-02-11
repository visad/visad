//
// Slider.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2015 Bill Hibbard, Curtis Rueden, Tom
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
 * A simple slider widget.
 */
public class Slider extends Component
  implements MouseListener, MouseMotionListener
{

  /**
   * Preferred slider height.
   */
  public static final int SLIDER_PREF_HEIGHT = 14;

  /**
   * Preferred slider width.
   */
  public static final int SLIDER_PREF_WIDTH = 300;

  /**
   * Width of grip.
   */
  public static final int GRIP_WIDTH = 29;

  /**
   * Height of grip.
   */
  public static final int GRIP_HEIGHT = 14;

  /**
   * Y-coordinate of top of grip.
   */
  public static final int GRIP_TOP_Y = 0;

  /**
   * Y-coordinate of bottom of grip.
   */
  public static final int GRIP_BOTTOM_Y = GRIP_TOP_Y + GRIP_HEIGHT;

  /**
   * Y-coordinate of slider line.
   */
  public static final int LINE_LEVEL = GRIP_BOTTOM_Y - GRIP_HEIGHT / 2;

  /**
   * Current width of slider.
   */
  protected int width = SLIDER_PREF_WIDTH;

  /**
   * Current value of slider.
   */
  protected float value = 0;

  /**
   * Minimum value of slider.
   */
  protected float minimum = 0;

  /**
   * Maximum value of slider.
   */
  protected float maximum = 100;

  /**
   * Pixel location of grip.
   */
  private int grip = 0;

  /**
   * Flag whether mouse is currently affecting grip.
   */
  private boolean slide = false;

  /**
   * Flag whether grip has moved.
   */
  private boolean moved = false;

  /**
   * Minimum widget size.
   */
  protected Dimension minSize = null;

  /**
   * Preferred widget size.
   */
  protected Dimension prefSize = null;

  /**
   * Maximum widget size.
   */
  protected Dimension maxSize = null;

  /**
   * Constructs a slider with default value, minimum and maximum.
   */
  public Slider() { }
  
  /**
   * Constructs a slider with the specified value, minimum and maximum.
   */
  public Slider(float value, float min, float max) {
    setBounds(min, max);
    setValue(value);
    addMouseListener(this);
    addMouseMotionListener(this);
  }

  /**
   * Gets current slider value.
   */
  public float getValue() {
    return value;
  }
  
  /**
   * Gets minimum slider value.
   */
  public float getMinimum() {
    return minimum;
  }

  /**
   * Gets maximum slider value.
   */
  public float getMaximum() {
    return maximum;
  }

  /**
   * Sets current slider value.
   */
  public void setValue(float value) {
    this.value = value;
    grip = valueToGrip(value);
    repaint();
  }

  /**
   * Sets minimum and maximum slider values.
   */
  public void setBounds(float min, float max) {
    minimum = min;
    maximum = max;
    repaint();
  }

  /**
   * Detects changes in slider width.
   */
  public void setBounds(int x, int y, int w, int h) {
    super.setBounds(x, y, w, h);
    width = w;
  }

  /**
   * Horizontal position in grip where mouse was initially pressed.
   */
  private int gripX;

  /**
   * Updates grip location of slider.
   */
  private void updateGrip(int x) {
    grip = x - gripX;
    if (grip < 0) grip = 0;
    if (grip > width - GRIP_WIDTH) grip = width - GRIP_WIDTH;
    value = gripToValue(grip);
    notifyListeners();
    repaint();
  }

  /**
   * Vector of listeners for slider changes.
   */
  private Vector listeners = new Vector();

  /**
   * Command string for slider change notification.
   */
  private String command = null;

  /**
   * Adds a listener to be notified of slider changes.
   */
  public void addActionListener(ActionListener l) {
    synchronized (listeners) {
      listeners.addElement(l);
    }
  }

  /**
   * Removes a listener to be notified of slider changes.
   */
  public void removeActionListener(ActionListener l) {
    synchronized (listeners) {
      listeners.removeElement(l);
    }
  }

  /**
   * Sets command string for slider change notification.
   */
  public void setActionCommand(String cmd) {
    command = cmd;
  }

  /**
   * Notifies listeners of slider change.
   */
  public void notifyListeners() {
    ActionEvent e =
      new ActionEvent(this, ActionEvent.ACTION_PERFORMED, command);
    synchronized (listeners) {
      for (int i=0; i<listeners.size(); i++) {
        ActionListener l = (ActionListener) listeners.elementAt(i);
        l.actionPerformed(e);
      }
    }
  }

  /**
   * MouseListener method for moving grip.
   */
  public void mousePressed(MouseEvent e) {
    int x = e.getX();
    int y = e.getY();

    if (Widget.containedIn(x, y, grip, 0, GRIP_WIDTH, GRIP_HEIGHT))
    {
      // mouse pressed in grip
      gripX = x - grip;
      slide = true;
    }
    else if (Widget.containedIn(x, y, 0, 0, grip - 1, GRIP_HEIGHT) ||
      Widget.containedIn(x, y,
      grip + GRIP_WIDTH, 0, width - grip - GRIP_WIDTH, GRIP_HEIGHT))
    {
      // mouse pressed in slider but outside of grip
      gripX = GRIP_WIDTH / 2;
      slide = true;
      updateGrip(x);
    }
  }

  /**
   * MouseListener method for moving grip.
   */
  public void mouseReleased(MouseEvent e) {
    slide = false;
  }

  /**
   * Not used.
   */
  public void mouseClicked(MouseEvent e) { }

  /**
   * Not used.
   */
  public void mouseEntered(MouseEvent e) { }

  /**
   * Not used.
   */
  public void mouseExited(MouseEvent e) { }

  /**
   * MouseMotionListener method for moving grip.
   */
  public void mouseDragged(MouseEvent e) {
    int x = e.getX();
    int y = e.getY();

    // move grip
    if (slide) updateGrip(x);
  }

  /**
   * Not used.
   */
  public void mouseMoved(MouseEvent e) { }

  /**
   * Returns minimum size of slider.
   */
  public Dimension getMinimumSize() {
    if (minSize == null) minSize = new Dimension(0, SLIDER_PREF_HEIGHT);
    return minSize;
  }

  /**
   * Sets minimum size of slider.
   */
  public void setMinimumSize(Dimension dim) {
    minSize = dim;
  }

  /**
   * Returns preferred size of slider.
   */
  public Dimension getPreferredSize() {
    if (prefSize == null) {
      prefSize = new Dimension(SLIDER_PREF_WIDTH, SLIDER_PREF_HEIGHT);
    }
    return prefSize;
  }

  /**
   * Sets preferred size of slider.
   */
  public void setPreferredSize(Dimension dim) {
    prefSize = dim;
  }

  /**
   * Returns maximum size oa slider.
   */
  public Dimension getMaximumSize() {
    if (maxSize == null) {
      maxSize = new Dimension(Integer.MAX_VALUE, SLIDER_PREF_HEIGHT);
    }
    return maxSize;
  }

  /**
   * Sets preferred size of slider.
   */
  public void setMaximumSize(Dimension dim) {
    maxSize = dim;
  }

  /**
   * Converts grip pixel value to slider value.
   */
  private float gripToValue(int grip) {
    return (float) grip / (width - GRIP_WIDTH) * (maximum - minimum) + minimum;
  }

  /**
   * Converts slider value to grip pixel value.
   */
  private int valueToGrip(float value) {
    return (int)
      ((value - minimum) / (maximum - minimum) * (width - GRIP_WIDTH));
  }

  /**
   * Draws the slider.
   */
  public void paint(Graphics g) {
    // draw slider background
    g.setColor(Widget.PALE_GRAY);
    g.fillRect(0, 0, grip, LINE_LEVEL);
    g.fillRect(grip + GRIP_WIDTH, 0, width - grip - GRIP_WIDTH, LINE_LEVEL);
    g.fillRect(0, LINE_LEVEL + 1, grip, GRIP_HEIGHT - LINE_LEVEL - 1);
    g.fillRect(grip + GRIP_WIDTH, LINE_LEVEL + 1,
      width - grip - GRIP_WIDTH, GRIP_HEIGHT - LINE_LEVEL - 1);

    // draw corner grip dots
    g.drawRect(grip, 0, 0, 0);
    g.drawRect(grip + GRIP_WIDTH - 1, 0, 0, 0);
    g.drawRect(grip, GRIP_HEIGHT - 1, 0, 0);
    g.drawRect(grip + GRIP_WIDTH - 1, GRIP_HEIGHT - 1, 0, 0);

    // draw central grip block
    g.fillRect(grip + 2, 2, GRIP_WIDTH - 3, GRIP_HEIGHT - 3);

    // draw outer grip outline
    g.setColor(Color.black);
    g.drawLine(grip + 1, 0, grip + GRIP_WIDTH - 2, 0);
    g.drawLine(grip + 1, GRIP_HEIGHT - 1,
      grip + GRIP_WIDTH - 2, GRIP_HEIGHT - 1);
    g.drawLine(grip, 1, grip, GRIP_HEIGHT - 2);
    g.drawLine(grip + GRIP_WIDTH - 1, 1,
      grip + GRIP_WIDTH - 1, GRIP_HEIGHT - 2);

    // draw inner grip outline
    g.setColor(Color.white);
    g.drawLine(grip + 1, 1, grip + GRIP_WIDTH - 2, 1);
    g.drawLine(grip + 1, 2, grip + 1, GRIP_HEIGHT - 2);

    // draw slider line
    if (grip > 0) g.drawLine(0, LINE_LEVEL, grip - 1, LINE_LEVEL);
    g.drawLine(grip + GRIP_WIDTH, LINE_LEVEL, width, LINE_LEVEL);
  }

  // CTR: ?
  public void repaint() {
    Graphics g = getGraphics();
    if (g != null) {
      paint(g);
      g.dispose();
    }
  }

  /**
   * Main method for testing purposes.
   */
  public static void main(String[] argv) {
    Slider s = new Slider(26.3f, 0.0f, 100.0f);
    Frame f = new Frame("Slider test");
    f.addWindowListener(new WindowAdapter() {
        public void windowClosing(WindowEvent e) {
          System.exit(0);
        }
      });
    f.add(s);
    f.pack();
    f.setVisible(true);
  }

}
