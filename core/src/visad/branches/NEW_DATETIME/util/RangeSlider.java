
//
// RangeSlider.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 1998 Bill Hibbard, Curtis Rueden, Tom
Rink and Dave Glowacki.

This program is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 1, or (at your option)
any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License in file NOTICE for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
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

  /** percent through scale of min gripper */
  float minPercent = 0;

  /** percent through scale of max gripper */
  float maxPercent = 100;

  /** minimum slider value */
  float minVal;

  /** maximum slider value */
  float maxVal;

  /** location of min gripper */
  private int minGrip = 9; 

  /** location of max gripper */
  private int maxGrip = 291;

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
    minGrip = 9;
    maxGrip = getSize().width-9;
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

  /** MouseListener method for moving slider */
  public void mousePressed(MouseEvent e) {
    int w = getSize().width;
    int x = e.getX();
    int y = e.getY();
    oldX = x;

    Rectangle min = new Rectangle(minGrip-8, 4, 9, 17);
    Rectangle max = new Rectangle(maxGrip, 4, 9, 17);
    Rectangle between = new Rectangle(minGrip, 1, maxGrip-minGrip, 22);
    Rectangle left = new Rectangle(9, 1, minGrip-18, 22);
    Rectangle right = new Rectangle(maxGrip+8, 1, w-maxGrip-18, 22);

    if (min.contains(x, y)) minSlide = true;
    else if (max.contains(x, y)) maxSlide = true;
    else if (between.contains(x, y)) {
      minSlide = true;
      maxSlide = true;
    }
    else if (left.contains(x, y)) {
      minGrip = x;
      minSlide = true;
      lSlideMoved = true;
      percPaint();
    }
    else if (right.contains(x, y)) {
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
      if (minGrip+change < 9) change = 9-minGrip;
      else if (maxGrip+change > w-9) change = w-9-maxGrip;
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
      if (x < 9) minGrip = 9;
      else if (x >= maxGrip) minGrip = maxGrip-1;
      else minGrip = x;
      lSlideMoved = true;
      percPaint();
    }

    // move max gripper if it is held
    else if (maxSlide) {
      if (x > w-9) maxGrip = w-9;
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
    return new Dimension(0, 42);
  }

  /** return preferred size of widget */
  public Dimension getPreferredSize() {
    return new Dimension(300, 42);
  }

  /** return maximum size of widget */
  public Dimension getMaximumSize() {
    return new Dimension(Integer.MAX_VALUE, 42);
  }

  /** recomputes percent variables then repaints */
  void percPaint() {
    int w = getSize().width;
    minPercent = 100*((float) (minGrip-9))/((float) (w-18));
    maxPercent = 100*((float) (maxGrip-9))/((float) (w-18));
    repaint();
  }

  /** repaints anything that needs it */
  public void repaint() {
    Graphics g = getGraphics();
    if (g == null) return;
    int w = getSize().width;
    if (lSlideMoved) {
      g.setColor(Color.black);
      g.fillRect(2, 4, maxGrip-3, 16);
      g.setColor(Color.white);
      g.drawLine(2, 12, maxGrip-3, 12);
      g.setColor(Color.yellow);
      int[] xpts = {minGrip-7, minGrip+1, minGrip+1};
      int[] ypts = {12, 4, 21};
      g.fillPolygon(xpts, ypts, 3);
    }
    if (rSlideMoved) {
      g.setColor(Color.black);
      g.fillRect(minGrip+1, 4, w-minGrip-3, 16);
      g.setColor(Color.white);
      g.drawLine(minGrip+1, 12, w-3, 12);
      g.setColor(Color.yellow);
      int[] xpts = new int[] {maxGrip+8, maxGrip, maxGrip};
      int[] ypts = {12, 5, 21};
      g.fillPolygon(xpts, ypts, 3);
    }
    if (lSlideMoved || rSlideMoved) {
      g.setColor(Color.pink);
      g.fillRect(minGrip+1, 11, maxGrip-minGrip-1, 3);
    }
    if (textChanged) drawLabels(g, w);
    lSlideMoved = false;
    rSlideMoved = false;
    textChanged = false;
    g.dispose();
  }

  /** use current 'minPercent' and 'maxPercent' values to compute
   *  'minGrip' and 'maxGrip' values
   */
  void updateGripsFromPercents()
  {
    final float gripWidth = 9.0f;
    final float realWidth = (float) (getSize().width - (gripWidth * 2));

    int minNew = (int) ((0.01f * minPercent * realWidth + 0.5f) + gripWidth);
    if (minGrip != minNew) {
      lSlideMoved = true;
      textChanged = true;
      minGrip = minNew;
    }

    int maxNew = (int) ((0.01f * maxPercent * realWidth + 0.5f) + gripWidth);
    if (maxGrip != maxNew) {
      rSlideMoved = true;
      textChanged = true;
      maxGrip = maxNew;
    }
  }

  /** draws the slider from scratch */
  public void paint(Graphics g) {
    int w = getSize().width;

    // draw background
    g.setColor(Color.black);
    g.fillRect(0, 0, w, 42);

    // draw slider lines
    g.setColor(Color.white);
    g.drawLine(0, 12, w-1, 12);
    g.drawLine(0, 0, 0, 23);
    g.drawLine(0, 0, 2, 0);
    g.drawLine(0, 23, 2, 23);
    g.drawLine(w-1, 0, w-1, 23);
    g.drawLine(w-1, 0, w-3, 0);
    g.drawLine(w-1, 23, w-3, 23);

    // draw labels
    drawLabels(g, w);

    // draw grippers
    g.setColor(Color.yellow);
    int[] xpts = {minGrip-8, minGrip+1, minGrip+1};
    int[] ypts = {12, 4, 20};
    g.fillPolygon(xpts, ypts, 3);
    xpts = new int[] {maxGrip+7, maxGrip, maxGrip};
    ypts = new int[] {12, 4, 20};
    g.fillPolygon(xpts, ypts, 3);

    // draw pink rectangle between grippers
    g.setColor(Color.pink);
    g.fillRect(minGrip+1, 11, maxGrip-minGrip-1, 3);
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
      g.fillRect(1, 27, sw, 15);
      lastMin = minVal;
    }
    if (lastMax != maxVal || lastW != w) {
      // maximum bound text string
      g.setColor(Color.black);
      int sw = fm.stringWidth(""+lastMax);
      g.fillRect(lastW - 4 - sw, 27, sw, 15);
      lastMax = maxVal;
    }
    String minS = "" + PlotText.shortString(minPercent
                     * (maxVal - minVal) / 100 + minVal);
    String maxS = "" + PlotText.shortString(maxPercent
                     * (maxVal - minVal) / 100 + minVal);
    String curStr = name + "(" + minS + ", " + maxS + ")";
    if (!curStr.equals(lastCurStr) || lastW != w) {
      g.setColor(Color.black);
      int sw = fm.stringWidth(lastCurStr);
      g.fillRect((lastW - sw)/2, 27, sw, 15);
      lastCurStr = curStr;
    }
    g.setColor(Color.white);
    g.drawString(""+PlotText.shortString(minVal), 1, 40);
    String maxStr = ""+PlotText.shortString(maxVal);
    g.drawString(maxStr, w - 4 - fm.stringWidth(maxStr), 40);
    g.drawString(curStr, (w - fm.stringWidth(curStr))/2, 40);
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

