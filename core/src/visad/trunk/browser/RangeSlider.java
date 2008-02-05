//
// RangeSlider.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2008 Bill Hibbard, Curtis Rueden, Tom
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

/**
 * A slider widget that allows users to select a lower and upper bound.
 */
public class RangeSlider extends Component
  implements MouseListener, MouseMotionListener
{

  /**
   * Default variable name.
   */
  public static final String DEFAULT_NAME = "value";

  /**
   * Preferred slider height.
   */
  public static final int SLIDER_PREF_HEIGHT = 42;

  /**
   * Preferred slider width.
   */
  public static final int SLIDER_PREF_WIDTH = 300;

  /**
   * Width of grip.
   */
  public static final int GRIP_WIDTH = 9;

  /**
   * Height of grip.
   */
  public static final int GRIP_HEIGHT = 17;

  /**
   * Y-coordinate of top of grip.
   */
  public static final int GRIP_TOP_Y = 4;

  /**
   * Y-coordinate of bottom of grip.
   */
  public static final int GRIP_BOTTOM_Y = GRIP_TOP_Y + GRIP_HEIGHT;

  /**
   * Y-coordinate of middle of grip.
   */
  public static final int GRIP_MIDDLE_Y = GRIP_TOP_Y + (GRIP_HEIGHT / 2);

  /**
   * Height of slider line.
   */
  public static final int SLIDER_LINE_HEIGHT = GRIP_HEIGHT + 2;

  /**
   * Width of slider line.
   */
  public static final int SLIDER_LINE_WIDTH = 2;

  /**
   * Height of font.
   */
  public static final int FONT_HEIGHT = 15;

  /**
   * Y-coordinate of top of font.
   */
  public static final int FONT_TOP_Y = 27;

  /**
   * Y-coordinate of bottom of font.
   */
  public static final int FONT_BOTTOM_Y = FONT_TOP_Y + FONT_HEIGHT - 2;

  /**
   * Percent through scale of min gripper.
   */
  protected float minValue = 0;

  /**
   * Percent through scale of max gripper.
   */
  protected float maxValue = 100;

  /**
   * Minimum slider value.
   */
  protected float minLimit = 0.0f;

  /**
   * Maximum slider value.
   */
  protected float maxLimit = 1.0f;

  /**
   * Location of min gripper.
   */
  protected int minGrip = GRIP_WIDTH;

  /**
   * Location of max gripper.
   */
  protected int maxGrip = SLIDER_PREF_WIDTH - GRIP_WIDTH;

  /**
   * Flag whether mouse is currently affecting min gripper.
   */
  private boolean minSlide = false;

  /**
   * Flag whether mouse is currently affecting max gripper.
   */
  private boolean maxSlide = false;

  /**
   * Flag whether left gripper has moved.
   */
  protected boolean lSlideMoved = false;

  /**
   * Flag whether right gripper has moved.
   */
  protected boolean rSlideMoved = false;

  /**
   * Flag whether current text string value needs updating.
   */
  protected boolean textChanged = false;

  /**
   * Variable name for values.
   */
  private String name;

  /**
   * Label state variable.
   */
  private float lastMinLimit = 0.0f;

  /**
   * Label state variable.
   */
  private float lastMaxLimit = 0.0f;

  /**
   * Label state variable.
   */
  private String lastCurStr = "";

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
   * Constructs a RangeSlider with the specified range of values.
   */
  public RangeSlider(String n, float min, float max) {
    name = n;
    resetValues(min, max);
    addMouseListener(this);
    addMouseMotionListener(this);
  }

  /**
   * Gets minimum and maximum slider values.
   */
  public float[] getMinMaxValues() {
    return new float[] {minValue, maxValue};
  }

  /**
   * Resets the minimum and maximum values.
   */
  protected void resetValues(float min, float max) {
    minLimit = min;
    maxLimit = max;
    minGrip = GRIP_WIDTH;
    maxGrip = getSize().width - GRIP_WIDTH;
    minSlide = false;
    maxSlide = false;
    lSlideMoved = true;
    rSlideMoved = true;
    textChanged = true;

    int w = getSize().width;
    minValue = gripToValue(minGrip, w);
    maxValue = gripToValue(maxGrip, w);
  }

  /**
   * Sets the slider's name.
   */
  public void setName(String name) {
    this.name = name;
    textChanged = true;
    repaint();
  }

  /**
   * Sets the slider's lo and hi bounds.
   */
  public void setBounds(float min, float max) {
    resetValues(min, max);
    valuesUpdated();
    repaint();
  }

  /**
   * Sets the slider's lo and hi values.
   */
  public void setValues(float lo, float hi) {
    int w = getSize().width;
    int g;

    minValue = lo;
    g = minGrip;
    minGrip = valueToGrip(minValue, w);
    if (g != minGrip) lSlideMoved = true;

    maxValue = hi;
    g = maxGrip;
    maxGrip = valueToGrip(maxValue, w);
    if (g != maxGrip) rSlideMoved = true;

    textChanged = true;
    repaint();
  }

  /**
   * Redraws the slider if the widget width changes.
   */
  public void setBounds(int x, int y, int w, int h) {
    int lastW = getSize().width;
    super.setBounds(x, y, w, h);
    if (lastW != w) {
      minGrip = valueToGrip(minValue, w);
      maxGrip = valueToGrip(maxValue, w);
      Graphics g = getGraphics();
      drawLabels(g, lastW);
      if (g != null) g.dispose();
    }
  }

  /**
   * MouseListener method for moving slider.
   */
  public void mousePressed(MouseEvent e) {
    int w = getSize().width;
    int x = e.getX();
    int y = e.getY();
    oldX = x;

    if (Widget.containedIn(x, y, minGrip - (GRIP_WIDTH - 1),
      GRIP_TOP_Y, GRIP_WIDTH, GRIP_HEIGHT))
    {
      // mouse pressed in left grip
      minSlide = true;
    }
    else if (Widget.containedIn(x, y, maxGrip, GRIP_TOP_Y, GRIP_WIDTH, GRIP_HEIGHT)) {
      // mouse pressed in right grip
      maxSlide = true;
    }
    else if (Widget.containedIn(x, y, minGrip, GRIP_TOP_Y - 3, maxGrip-minGrip,
      GRIP_TOP_Y + SLIDER_LINE_HEIGHT - 1))
    {
      // mouse pressed in pink rectangle
      minSlide = true;
      maxSlide = true;
    }
    else if (Widget.containedIn(x, y, 0, GRIP_TOP_Y-3, minGrip-GRIP_WIDTH,
      GRIP_TOP_Y+SLIDER_LINE_HEIGHT-1))
    {
      // mouse pressed to left of grips
      if (x < GRIP_WIDTH) minGrip = GRIP_WIDTH;
      else minGrip = x;
      minValue = gripToValue(minGrip, w);
      minSlide = true;
      lSlideMoved = true;
      valuesUpdated();
      repaint();
    }
    else if (Widget.containedIn(x, y, maxGrip + 1 - GRIP_WIDTH, GRIP_TOP_Y - 3,
      w - maxGrip + GRIP_WIDTH, GRIP_TOP_Y + SLIDER_LINE_HEIGHT - 1))
    {
      // mouse pressed to right of grips
      if (x > w - GRIP_WIDTH) maxGrip = w - GRIP_WIDTH;
      else maxGrip = x;
      maxValue = gripToValue(maxGrip, w);
      maxSlide = true;
      rSlideMoved = true;
      valuesUpdated();
      repaint();
    }
  }

  /**
   * MouseListener method for moving slider.
   */
  public void mouseReleased(MouseEvent e) {
    minSlide = false;
    maxSlide = false;
    textChanged = true;
    repaint();
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
   * Previous mouse X position.
   */
  private int oldX;

  /**
   * MouseMotionListener method for moving slider.
   */
  public void mouseDragged(MouseEvent e) {
    int w = getSize().width;
    int x = e.getX();
    int y = e.getY();

    // move entire range
    if (minSlide && maxSlide) {
      int change = x - oldX;
      if (minGrip+change < GRIP_WIDTH) change = GRIP_WIDTH - minGrip;
      else if (maxGrip + change > w - GRIP_WIDTH) {
        change = w - GRIP_WIDTH - maxGrip;
      }
      if (change != 0) {
        minGrip += change;
        minValue = gripToValue(minGrip, w);
        maxGrip += change;
        maxValue = gripToValue(maxGrip, w);
        lSlideMoved = true;
        rSlideMoved = true;
        valuesUpdated();
        repaint();
      }
    }

    // move min gripper if it is held
    else if (minSlide) {
      if (x < GRIP_WIDTH) minGrip = GRIP_WIDTH;
      else if (x >= maxGrip) minGrip = maxGrip - 1;
      else minGrip = x;
      minValue = gripToValue(minGrip, w);
      lSlideMoved = true;
      valuesUpdated();
      repaint();
    }

    // move max gripper if it is held
    else if (maxSlide) {
      if (x > w - GRIP_WIDTH) maxGrip = w - GRIP_WIDTH;
      else if (x <= minGrip) maxGrip = minGrip + 1;
      else maxGrip = x;
      maxValue = gripToValue(maxGrip, w);
      rSlideMoved = true;
      valuesUpdated();
      repaint();
    }

    oldX = x;
  }

  /**
   * Not used.
   */
  public void mouseMoved(MouseEvent e) { }

  /**
   * Returns minimum size of range slider.
   */
  public Dimension getMinimumSize() {
    if (minSize == null) {
      minSize = new Dimension(0, SLIDER_PREF_HEIGHT);
    }
    return minSize;
  }

  /**
   * Sets minimum size of range slider.
   */
  public void setMinimumSize(Dimension dim) { minSize = dim; }

  /**
   * Returns preferred size of range slider.
   */
  public Dimension getPreferredSize() {
    if (prefSize == null) {
      prefSize = new Dimension(SLIDER_PREF_WIDTH, SLIDER_PREF_HEIGHT);
    }
    return prefSize;
  }

  /**
   * Sets preferred size of range slider.
   */
  public void setPreferredSize(Dimension dim) { prefSize = dim; }

  /**
   * Returns maximum size of range slider.
   */
  public Dimension getMaximumSize() {
    if (maxSize == null) {
      maxSize = new Dimension(Integer.MAX_VALUE, SLIDER_PREF_HEIGHT);
    }
    return maxSize;
  }

  /**
   * Sets preferred size of range slider.
   */
  public void setMaximumSize(Dimension dim) { maxSize = dim; }

  protected float gripToValue(int pos, int width) {
    float q = (float) (pos - GRIP_WIDTH) / (width - 2 * GRIP_WIDTH);
    return (maxLimit - minLimit) * q + minLimit;
  }

  protected int valueToGrip(float value, int width) {
    float rfloat = (((value - (float) minLimit) *
      (float) (width - (GRIP_WIDTH * 2))) / (maxLimit - minLimit));

    // round away from zero
    if (rfloat < 0.0f) rfloat -= 0.5f;
    else rfloat += 0.5f;

    return (int) rfloat + GRIP_WIDTH;
  }

  /**
   * Called whenever the min or max value is updated.
   * This method is meant to be overridden by extension classes.
   */
  public void valuesUpdated() { }

  /**
   * Draws the slider from scratch.
   */
  public void paint(Graphics g) {
    int w = getSize().width;

    // clear old graphics
    g.setColor(Color.black);
    g.fillRect(0, 0, w, SLIDER_PREF_HEIGHT);

    // draw slider lines
    int right = w - 1;
    g.setColor(Color.white);
    g.drawLine(0, GRIP_MIDDLE_Y, right, GRIP_MIDDLE_Y);
    g.drawLine(0, GRIP_TOP_Y - 4, 0, GRIP_TOP_Y + SLIDER_LINE_HEIGHT);
    g.drawLine(0, GRIP_TOP_Y - 4, SLIDER_LINE_WIDTH, GRIP_TOP_Y - 4);
    g.drawLine(0, GRIP_TOP_Y + SLIDER_LINE_HEIGHT, SLIDER_LINE_WIDTH,
      GRIP_TOP_Y + SLIDER_LINE_HEIGHT);
    g.drawLine(right, GRIP_TOP_Y - 4, right, GRIP_TOP_Y + SLIDER_LINE_HEIGHT);
    g.drawLine(right,
      GRIP_TOP_Y - 4, right - SLIDER_LINE_WIDTH, GRIP_TOP_Y - 4);
    g.drawLine(right, GRIP_TOP_Y + SLIDER_LINE_HEIGHT,
      right - SLIDER_LINE_WIDTH, GRIP_TOP_Y + SLIDER_LINE_HEIGHT);

    // refresh everything
    lSlideMoved = true;
    rSlideMoved = true;
    textChanged = true;
    paintMinimum(g);
  }

  /**
   * Repaints anything that needs it.
   */
  public void repaint() {
    Graphics g = getGraphics();
    if (g != null) {
      paintMinimum(g);
      g.dispose();
    }
  }

  /**
   * Paints only components that have changed.
   */
  private void paintMinimum(Graphics g) {
    int w = getSize().width;
    if (lSlideMoved) {
      g.setColor(Color.black);
      g.fillRect(SLIDER_LINE_WIDTH, GRIP_TOP_Y, maxGrip - 3, GRIP_HEIGHT);
      g.setColor(Color.white);
      g.drawLine(SLIDER_LINE_WIDTH, GRIP_MIDDLE_Y, maxGrip - 3, GRIP_MIDDLE_Y);
      g.setColor(Color.yellow);
      int[] xpts = {minGrip - GRIP_WIDTH, minGrip + 1, minGrip + 1};
      int[] ypts = {GRIP_MIDDLE_Y, GRIP_TOP_Y, GRIP_BOTTOM_Y};
      g.fillPolygon(xpts, ypts, 3);
    }
    if (rSlideMoved) {
      g.setColor(Color.black);
      g.fillRect(minGrip + 1, GRIP_TOP_Y, w - minGrip - 3, GRIP_HEIGHT);
      g.setColor(Color.white);
      g.drawLine(minGrip + 1, GRIP_MIDDLE_Y, w - 3, GRIP_MIDDLE_Y);
      g.setColor(Color.yellow);
      int[] xpts = new int[] {maxGrip + GRIP_WIDTH - 1, maxGrip, maxGrip};
      int[] ypts = {GRIP_MIDDLE_Y, GRIP_TOP_Y, GRIP_BOTTOM_Y};
      g.fillPolygon(xpts, ypts, 3);
    }
    if (lSlideMoved || rSlideMoved) {
      g.setColor(Color.pink);
      g.fillRect(minGrip + 1, GRIP_MIDDLE_Y, maxGrip - minGrip - 1, 3);
    }
    if (textChanged) drawLabels(g, w);
    lSlideMoved = false;
    rSlideMoved = false;
    textChanged = false;
  }

  /**
   * Updates the labels at the bottom of the widget.
   */
  private void drawLabels(Graphics g, int lastW) {
    int w = getSize().width;
    FontMetrics fm = g.getFontMetrics();
    if (lastMinLimit != minLimit || lastW != w) {
      // minimum bound text string
      g.setColor(Color.black);
      int sw = fm.stringWidth(""+lastMinLimit);
      g.fillRect(1, FONT_TOP_Y, sw, FONT_HEIGHT);
      lastMinLimit = minLimit;
    }
    if (lastMaxLimit != maxLimit || lastW != w) {
      // maximum bound text string
      g.setColor(Color.black);
      int sw = fm.stringWidth(""+lastMaxLimit);
      g.fillRect(lastW - 4 - sw, FONT_TOP_Y, sw, FONT_HEIGHT);
      lastMaxLimit = maxLimit;
    }

    // err on the side of wider bounds
    String minS, maxS;
    if (minValue < maxValue) { 
      minS = Convert.shortString(minValue, Convert.ROUND_DOWN);
      maxS = Convert.shortString(maxValue, Convert.ROUND_UP);
    }
    else {
      minS = Convert.shortString(minValue, Convert.ROUND_UP);
      maxS = Convert.shortString(maxValue, Convert.ROUND_DOWN);
    }

    String curStr = name + " = (" + minS + ", " + maxS + ")";
    if (!curStr.equals(lastCurStr) || lastW != w) {
      g.setColor(Color.black);
      int sw = fm.stringWidth(lastCurStr);
      g.fillRect((lastW - sw) / 2, FONT_TOP_Y, sw, FONT_HEIGHT);
      lastCurStr = curStr;
    }
    g.setColor(Color.white);

    // err on the side of wider bounds
    String minStr, maxStr;
    if (minLimit < maxLimit) {
      minStr = Convert.shortString(minLimit, Convert.ROUND_DOWN);
      maxStr = Convert.shortString(maxLimit, Convert.ROUND_UP);
    }
    else {
      minStr = Convert.shortString(minLimit, Convert.ROUND_DOWN);
      maxStr = Convert.shortString(maxLimit, Convert.ROUND_UP);
    }

    g.drawString(minStr, 1, FONT_BOTTOM_Y);
    g.drawString(maxStr, w - 4 - fm.stringWidth(maxStr), FONT_BOTTOM_Y);
    g.drawString(curStr, (w - fm.stringWidth(curStr)) / 2, FONT_BOTTOM_Y);
  }

  /**
   * Main method for testing purposes.
   */
  public static void main(String[] argv) {
    RangeSlider rs = new RangeSlider("", 0.0f, 100.0f);
    Frame f = new Frame("RangeSlider test");
    f.addWindowListener(new WindowAdapter() {
        public void windowClosing(WindowEvent e) {
          System.exit(0);
        }
      });
    f.add(rs);
    f.pack();
    f.setVisible(true);

    // dynamically set the values
    rs.setValues(22.2222f, 76.5432f);
  }

}
