//
// RangeSlider.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 1999 Bill Hibbard, Curtis Rueden, Tom
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

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import visad.PlotText;
import visad.ScalarMap;

/** A slider widget that allows users to select a lower and upper bound.<P> */
public class RangeSlider extends JComponent implements MouseListener,
                                                       MouseMotionListener {

  /** slider constants */
  public static final int SLIDER_PREF_HEIGHT = 42;
  public static final int SLIDER_PREF_WIDTH = 300;

  /** grip constants */
  public static final int GRIP_WIDTH = 9;
  public static final int GRIP_HEIGHT = 17;
  public static final int GRIP_TOP_Y = 4;
  public static final int GRIP_BOTTOM_Y = GRIP_TOP_Y + GRIP_HEIGHT;
  public static final int GRIP_MIDDLE_Y = GRIP_TOP_Y + (GRIP_HEIGHT / 2);

  /*** track constants */
  public static final int SLIDER_LINE_HEIGHT = GRIP_HEIGHT + 2;
  public static final int SLIDER_LINE_WIDTH = 2;

  /** font constants */
  public static final int FONT_HEIGHT = 15;
  public static final int FONT_TOP_Y = 27;
  public static final int FONT_BOTTOM_Y = FONT_TOP_Y + FONT_HEIGHT - 2;

  /** percent through scale of min gripper */
  float minPercent = 0;

  /** percent through scale of max gripper */
  float maxPercent = 100;

  /** minimum slider value */
  float minVal;

  /** maximum slider value */
  float maxVal;

  /** location of min gripper */
  private int minGrip = GRIP_WIDTH; 

  /** location of max gripper */
  private int maxGrip = SLIDER_PREF_WIDTH - GRIP_WIDTH;

  /** flag whether mouse is currently affecting min gripper */
  private boolean minSlide = false;

  /** flag whether mouse is currently affecting max gripper */
  private boolean maxSlide = false;

  /** flag whether left gripper has moved */
  private boolean lSlideMoved = false;

  /** flag whether right gripper has moved */
  private boolean rSlideMoved = false;

  /** flag whether current text string value needs updating */
  private boolean textChanged = false;

  /** variable name for values */
  private String name;

  /** obtains the name of the specified ScalarMap */
  static String nameOf(ScalarMap smap) {
    String n = "value = ";
    try {
      n = smap.getScalar().getName() + " = ";
    }
    catch (Exception exc) { }
    return n;
  }
  
  /** construct a RangeSlider with range of values (min, max) */
  public RangeSlider(String n, float min, float max) {
    addMouseListener(this);
    addMouseMotionListener(this);
    minVal = min;
    maxVal = max;
    name = n;
  }

  /** sets the slider's bounds to the specified values */
  public void setBounds(float min, float max) {
    minVal = min;
    maxVal = max;
    minGrip = GRIP_WIDTH;
    maxGrip = getSize().width-GRIP_WIDTH;
    minSlide = false;
    maxSlide = false;
    lSlideMoved = true;
    rSlideMoved = true;
    textChanged = true;
    percPaint();
  }

  /** redraw slider if widget width changes */
  public void reshape(int x, int y, int w, int h) {
    int lastW = getSize().width;
    super.reshape(x, y, w, h);
    if (lastW != w) {
      updateGripsFromPercents();
      drawLabels(getGraphics(), lastW);
    }
  }

  /** return true if (px,py) is inside (x,y,w,h) */
  private boolean containedIn(int px, int py, int x, int y, int w, int h)
  {
    return new Rectangle(x, y, w, h).contains(px, py);
  }

  /** MouseListener method for moving slider */
  public void mousePressed(MouseEvent e) {
    int w = getSize().width;
    int x = e.getX();
    int y = e.getY();
    oldX = x;

    if (containedIn(x, y, minGrip-(GRIP_WIDTH-1), GRIP_TOP_Y,
                    GRIP_WIDTH, GRIP_HEIGHT))
    {
      // mouse pressed in left grip
      minSlide = true;
    } else if (containedIn(x, y, maxGrip, GRIP_TOP_Y,
                           GRIP_WIDTH, GRIP_HEIGHT))
    {
      // mouse pressed in right grip
      maxSlide = true;
    } else if (containedIn(x, y, minGrip, GRIP_TOP_Y-3,
                           maxGrip-minGrip, GRIP_TOP_Y+SLIDER_LINE_HEIGHT-1))
    {
      // mouse pressed in pink rectangle
      minSlide = true;
      maxSlide = true;
    } else if (containedIn(x, y, GRIP_WIDTH, GRIP_TOP_Y-3,
                           minGrip-(GRIP_WIDTH*2),
                           GRIP_TOP_Y+SLIDER_LINE_HEIGHT-1))
    {
      // mouse pressed to left of grips
      minGrip = x;
      minSlide = true;
      lSlideMoved = true;
      percPaint();
    } else if (containedIn(x, y, maxGrip+1-GRIP_WIDTH, GRIP_TOP_Y-3,
                           w-maxGrip-(GRIP_WIDTH*2),
                           GRIP_TOP_Y+SLIDER_LINE_HEIGHT-1))
    {
      // mouse pressed to right of grips
      maxGrip = x;
      maxSlide = true;
      rSlideMoved = true;
      percPaint();
    }
  }

  /** MouseListener method for moving slider */
  public void mouseReleased(MouseEvent e) {
    minSlide = false;
    maxSlide = false;
    textChanged = true;
    repaint();
  }
  
  // unneeded MouseListener methods
  public void mouseClicked(MouseEvent e) { }
  public void mouseEntered(MouseEvent e) { }
  public void mouseExited(MouseEvent e) { }

  /** previous mouse X position */
  private int oldX;

  /** MouseMotionListener method for moving slider */
  public void mouseDragged(MouseEvent e) {
    int w = getSize().width;
    int x = e.getX();
    int y = e.getY();

    // move entire range
    if (minSlide && maxSlide) {
      int change = x - oldX;
      if (minGrip+change < GRIP_WIDTH) change = GRIP_WIDTH-minGrip;
      else if (maxGrip+change > w-GRIP_WIDTH) change = w-GRIP_WIDTH-maxGrip;
      if (change != 0) {
        minGrip += change;
        maxGrip += change;
        lSlideMoved = true;
        rSlideMoved = true;
        percPaint();
      }
    }
    
    // move min gripper if it is held
    else if (minSlide) {
      if (x < GRIP_WIDTH) minGrip = GRIP_WIDTH;
      else if (x >= maxGrip) minGrip = maxGrip-1;
      else minGrip = x;
      lSlideMoved = true;
      percPaint();
    }

    // move max gripper if it is held
    else if (maxSlide) {
      if (x > w-GRIP_WIDTH) maxGrip = w-GRIP_WIDTH;
      else if (x <= minGrip) maxGrip = minGrip+1;
      else maxGrip = x;
      rSlideMoved = true;
      percPaint();
    }

    oldX = x;
  }
  
  /** not used */
  public void mouseMoved(MouseEvent e) { }

  /** return minimum size of widget */
  public Dimension getMinimumSize() {
    return new Dimension(0, SLIDER_PREF_HEIGHT);
  }

  /** return preferred size of widget */
  public Dimension getPreferredSize() {
    return new Dimension(SLIDER_PREF_WIDTH, SLIDER_PREF_HEIGHT);
  }

  /** return maximum size of widget */
  public Dimension getMaximumSize() {
    return new Dimension(Integer.MAX_VALUE, SLIDER_PREF_HEIGHT);
  }

  /** recomputes percent variables then repaints */
  void percPaint() {
    int w = getSize().width;
    minPercent = ((100*((float) (minGrip-GRIP_WIDTH)))/
                  ((float) (w-(GRIP_WIDTH*2))));
    maxPercent = ((100*((float) (maxGrip-GRIP_WIDTH)))/
                  ((float) (w-(GRIP_WIDTH*2))));
    repaint();
  }

  /** draws the slider from scratch */
  public void paint(Graphics g) {
    int w = getSize().width;

    // clear old graphics
    g.setColor(Color.black);
    g.fillRect(0, 0, w, SLIDER_PREF_HEIGHT);

    // draw slider lines
    int right = w - 1;
    g.setColor(Color.white);
    g.drawLine(0, GRIP_MIDDLE_Y, right, GRIP_MIDDLE_Y);
    g.drawLine(0, GRIP_TOP_Y-4, 0, GRIP_TOP_Y+SLIDER_LINE_HEIGHT);
    g.drawLine(0, GRIP_TOP_Y-4, SLIDER_LINE_WIDTH, GRIP_TOP_Y-4);
    g.drawLine(0, GRIP_TOP_Y+SLIDER_LINE_HEIGHT,
               SLIDER_LINE_WIDTH, GRIP_TOP_Y+SLIDER_LINE_HEIGHT);
    g.drawLine(right, GRIP_TOP_Y-4, right, GRIP_TOP_Y+SLIDER_LINE_HEIGHT);
    g.drawLine(right, GRIP_TOP_Y-4, right-SLIDER_LINE_WIDTH, GRIP_TOP_Y-4);
    g.drawLine(right, GRIP_TOP_Y+SLIDER_LINE_HEIGHT,
               right-SLIDER_LINE_WIDTH, GRIP_TOP_Y+SLIDER_LINE_HEIGHT);

    // refresh everything
    lSlideMoved = true;
    rSlideMoved = true;
    textChanged = true;
    paintMinimum(g);
  }

  /** repaints anything that needs it */
  public void repaint() {
    Graphics g = getGraphics();
    paintMinimum(getGraphics());
    g.dispose();
  }

  private void paintMinimum(Graphics g)
  {
    if (g == null) return;
    int w = getSize().width;
    if (lSlideMoved) {
      g.setColor(Color.black);
      g.fillRect(SLIDER_LINE_WIDTH, GRIP_TOP_Y, maxGrip-3, GRIP_HEIGHT);
      g.setColor(Color.white);
      g.drawLine(SLIDER_LINE_WIDTH, GRIP_MIDDLE_Y, maxGrip-3, GRIP_MIDDLE_Y);
      g.setColor(Color.yellow);
      int[] xpts = {minGrip-GRIP_WIDTH, minGrip+1, minGrip+1};
      int[] ypts = {GRIP_MIDDLE_Y, GRIP_TOP_Y, GRIP_BOTTOM_Y};
      g.fillPolygon(xpts, ypts, 3);
    }
    if (rSlideMoved) {
      g.setColor(Color.black);
      g.fillRect(minGrip+1, GRIP_TOP_Y, w-minGrip-3, GRIP_HEIGHT);
      g.setColor(Color.white);
      g.drawLine(minGrip+1, GRIP_MIDDLE_Y, w-3, GRIP_MIDDLE_Y);
      g.setColor(Color.yellow);
      int[] xpts = new int[] {maxGrip+GRIP_WIDTH-1, maxGrip, maxGrip};
      int[] ypts = {GRIP_MIDDLE_Y, GRIP_TOP_Y, GRIP_BOTTOM_Y};
      g.fillPolygon(xpts, ypts, 3);
    }
    if (lSlideMoved || rSlideMoved) {
      g.setColor(Color.pink);
      g.fillRect(minGrip+1, GRIP_MIDDLE_Y, maxGrip-minGrip-1, 3);
    }
    if (textChanged) drawLabels(g, w);
    lSlideMoved = false;
    rSlideMoved = false;
    textChanged = false;
  }

  /** use current 'minPercent' and 'maxPercent' values to compute
   *  'minGrip' and 'maxGrip' values
   */
  void updateGripsFromPercents()
  {
    final float realWidth = (float )(getSize().width -
                                     ((float )GRIP_WIDTH * 2));

    int minNew = (int )((0.01f * minPercent * realWidth + 0.5f) +
                        (float )GRIP_WIDTH);
    if (minGrip != minNew) {
      lSlideMoved = true;
      textChanged = true;
      minGrip = minNew;
    }

    int maxNew = (int )((0.01f * maxPercent * realWidth + 0.5f) +
                        (float )GRIP_WIDTH);
    if (maxGrip != maxNew) {
      rSlideMoved = true;
      textChanged = true;
      maxGrip = maxNew;
    }
  }

  private float lastMin = 0.0f;
  private float lastMax = 0.0f;
  private String lastCurStr = "";

  /** updates the labels at the bottom of the widget */
  private void drawLabels(Graphics g, int lastW) {
    int w = getSize().width;
    FontMetrics fm = g.getFontMetrics();
    if (lastMin != minVal || lastW != w) {
      // minimum bound text string
      g.setColor(Color.black);
      int sw = fm.stringWidth(""+lastMin);
      g.fillRect(1, FONT_TOP_Y, sw, FONT_HEIGHT);
      lastMin = minVal;
    }
    if (lastMax != maxVal || lastW != w) {
      // maximum bound text string
      g.setColor(Color.black);
      int sw = fm.stringWidth(""+lastMax);
      g.fillRect(lastW - 4 - sw, FONT_TOP_Y, sw, FONT_HEIGHT);
      lastMax = maxVal;
    }
    String minS = PlotText.shortString(((minPercent * (maxVal - minVal)) /
                                        100) + minVal);
    String maxS = PlotText.shortString(((maxPercent * (maxVal - minVal)) /
                                        100) + minVal);
    String curStr = name + "(" + minS + ", " + maxS + ")";
    if (!curStr.equals(lastCurStr) || lastW != w) {
      g.setColor(Color.black);
      int sw = fm.stringWidth(lastCurStr);
      g.fillRect((lastW - sw)/2, FONT_TOP_Y, sw, FONT_HEIGHT);
      lastCurStr = curStr;
    }
    g.setColor(Color.white);
    g.drawString(PlotText.shortString(minVal), 1, FONT_BOTTOM_Y);
    String maxStr = PlotText.shortString(maxVal);
    g.drawString(maxStr, w - 4 - fm.stringWidth(maxStr), FONT_BOTTOM_Y);
    g.drawString(curStr, (w - fm.stringWidth(curStr))/2, FONT_BOTTOM_Y);
  }

  /** main method for testing purposes */
  public static void main(String[] argv) {
    RangeSlider rs = new RangeSlider("", 0.0f, 100.0f);
    JFrame f = new JFrame("VisAD RangeSlider test");
    f.addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent e) {
        System.exit(0);
      }
    });
    f.getContentPane().add(rs);
    f.pack();
    f.setVisible(true);
  }

}

