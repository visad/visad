/*

VisAD Utility Library: Widgets for use in building applications with
the VisAD interactive analysis and visualization library
Copyright (C) 1998 Nick Rasmussen
VisAD is Copyright (C) 1996 - 1998 Bill Hibbard, Curtis Rueden, Tom
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

/* AWT packages */
import java.awt.*;
import java.awt.event.*;

/* RMI classes */
import java.rmi.RemoteException;

/* VisAD packages */
import visad.*;

/** A slider widget that allows users to select a lower and upper bound.<P> */
public class SelectRangeWidget extends Canvas implements MouseListener,
                                                         MouseMotionListener,
                                                         ScalarMapListener {

  /** Percent through scale of min gripper. */
  private float minPercent = 0;

  /** Percent through scale of max gripper. */
  private float maxPercent = 100;

  /** Minimum slider value. */
  private float minVal;

  /** Maximum slider value. */
  private float maxVal;

  /** Location of min gripper. */
  private int minGrip; 

  /** Location of max gripper. */
  private int maxGrip;

  /** Flag whether mouse is currently affecting min gripper. */
  private boolean minSlide = false;

  /** Flag whether mouse is currently affecting max gripper. */
  private boolean maxSlide = false;

  /** This SelectRangeWidget's associated Control. */
  private RangeControl rangeControl;

  /** construct a SelectRangeWidget linked to the Control
      in the map (which must be to Display.SelectRange), with
      auto-scaling range values. */
  public SelectRangeWidget(ScalarMap smap) throws VisADException,
                                                  RemoteException {
    this(smap, smap.getRange());
  }

  /** construct a SelectRangeWidget linked to the Control
      in the map (which must be to Display.SelectRange), with
      range of values (min, max) */
  public SelectRangeWidget(ScalarMap smap, float min, float max)
                           throws VisADException, RemoteException {
    this(smap, new double[] {min, max});
    smap.setRange((double) min, (double) max);
  }

  private SelectRangeWidget(ScalarMap smap, double[] range)
                            throws VisADException, RemoteException {
    minVal = (float) range[0];
    maxVal = (float) range[1];

    if (minVal != minVal || maxVal != maxVal) {
      // fake min and max
      minVal = 0.0f;
      maxVal = 1.0f;
      // listen for real min and max
      smap.addScalarMapListener(this);
    }
    rangeControl = (RangeControl) smap.getControl();
    addMouseListener(this);
    addMouseMotionListener(this);
    setBackground(Color.black);
    minVal = (float) range[0];
    maxVal = (float) range[1];
    try {
      rangeControl.setRange(new float[] {minVal, maxVal});
    }
    catch (VisADException exc) { }
    catch (RemoteException exc) { }
  }

  /** ScalarMapListener method used with delayed auto-scaling. */
  public void mapChanged(ScalarMapEvent e) {
    ScalarMap s = e.getScalarMap();
    double[] range = s.getRange();
    minVal = (float) range[0];
    maxVal = (float) range[1];
    try {
      rangeControl.setRange(new float[] {minVal, maxVal});
    }
    catch (VisADException exc) { }
    catch (RemoteException exc) { }
  }

  /** MouseListener method for moving slider. */
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
      percPaint();
    }
    else if (right.contains(x, y)) {
      maxGrip = x;
      maxSlide = true;
      percPaint();
    }
  }

  /** MouseListener method for moving slider. */
  public void mouseReleased(MouseEvent e) {
    minSlide = false;
    maxSlide = false;
    drawLabels(getGraphics());
  }
  
  // unneeded MouseListener methods
  public void mouseClicked(MouseEvent e) { }
  public void mouseEntered(MouseEvent e) { }
  public void mouseExited(MouseEvent e) { }

  /** Previous mouse X position. */
  private int oldX;

  /** MouseMotionListener method for moving slider. */
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
        percPaint();
      }
    }
    
    // move min gripper if it is held
    else if (minSlide) {
      if (x < 9) minGrip = 9;
      else if (x >= maxGrip) minGrip = maxGrip-1;
      else minGrip = x;
      percPaint();
    }

    // move max gripper if it is held
    else if (maxSlide) {
      if (x > w-9) maxGrip = w-9;
      else if (x <= minGrip) maxGrip = minGrip+1;
      else maxGrip = x;
      percPaint();
    }

    oldX = x;
  }
  
  // unneeded MouseMotionListener methods
  public void mouseMoved(MouseEvent e) { }

  // size methods for widget
  public Dimension getMinimumSize() {
    return new Dimension(0, 42);
  }
  public Dimension getPreferredSize() {
    return new Dimension(200, 42);
  }
  public Dimension getMaximumSize() {
    return new Dimension(Integer.MAX_VALUE, 42);
  }

  /** Recomputes percent variables, updates control, then paints. */
  private void percPaint() {
    int w = getSize().width;
    minPercent = 100*((float) (minGrip-9))/((float) (w-18));
    maxPercent = 100*((float) (maxGrip-9))/((float) (w-18));
    float min = 0.01f*minPercent*(maxVal-minVal)+minVal;
    float max = 0.01f*maxPercent*(maxVal-minVal)+minVal;
    try {
      rangeControl.setRange(new float[] {min, max});
    }
    catch (VisADException exc) { }
    catch (RemoteException exc ) { }
    paint(getGraphics());
  }

  private int lastW = 0;
  private float lastMin = 0.0f;
  private float lastMax = 0.0f;
  private String lastCurStr = "";

  /** Draws the slider. */
  public void paint(Graphics g) {
    int w = getSize().width;

    // compute minGrip and maxGrip
    if (lastW != w) {
      minGrip = (int) (0.01*minPercent*(w-18)+9);
      maxGrip = (int) (0.01*maxPercent*(w-18)+9);
    }

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
    if (!minSlide && !maxSlide) drawLabels(g);

    // erase old slider junk
    g.setColor(Color.black);
    g.fillRect(1, 4, w-2, 8);
    g.fillRect(1, 13, w-2, 8);

    // draw grippers
    g.setColor(Color.yellow);
    int[] xpts = {minGrip-8, minGrip, minGrip};
    int[] ypts = {12, 4, 21};
    g.fillPolygon(xpts, ypts, 3);
    // Note: these coordinates are shifted up and left
    //       by one, to work around a misalignment bug
    xpts = new int[] {maxGrip+7, maxGrip-1, maxGrip-1};
    ypts = new int[] {13, 5, 20};
    g.fillPolygon(xpts, ypts, 3);

    g.setColor(Color.pink);
    g.fillRect(minGrip, 11, maxGrip-minGrip-1, 3);
    lastW = w;
  }

  /** Updates the labels at the bottom of the widget. */
  private void drawLabels(Graphics g) {
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
    String minS = ""+Math.round(minPercent*(maxVal-minVal)+minVal);
    String maxS = ""+Math.round(maxPercent*(maxVal-minVal)+minVal);
    if (minS.charAt(0) != '0') {
      if (minS.endsWith("00")) minS = minS.substring(0, minS.length()-2);
      else {
        minS = minS.substring(0, minS.length()-2)+"."
              +minS.substring(minS.length()-2);
        if (minS.endsWith("0")) minS = minS.substring(0, minS.length()-1);
      }
    }
    if (maxS.charAt(0) != '0') {
      if (maxS.endsWith("00")) maxS = maxS.substring(0, maxS.length()-2);
      else {
        maxS = maxS.substring(0, maxS.length()-2)+"."
              +maxS.substring(maxS.length()-2);
        if (maxS.endsWith("0")) maxS = maxS.substring(0, maxS.length()-1);
      }
    }
    String curStr = "("+minS+", "+maxS+")";
    if (!curStr.equals(lastCurStr) || lastW != w) {
      g.setColor(Color.black);
      int sw = fm.stringWidth(lastCurStr);
      g.fillRect((lastW - sw)/2, 27, sw, 15);
      lastCurStr = curStr;
    }
    g.setColor(Color.white);
    g.drawString(""+minVal, 1, 40);
    String maxStr = ""+maxVal;
    g.drawString(maxStr, w - 4 - fm.stringWidth(maxStr), 40);
    g.drawString(curStr, (w - fm.stringWidth(curStr))/2, 40);
  }

}

