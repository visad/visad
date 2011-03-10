/*

@(#) $Id: BaseRGBMap.java,v 1.22 2002-09-19 21:08:42 curtis Exp $

VisAD Utility Library: Widgets for use in building applications with
the VisAD interactive analysis and visualization library
Copyright (C) 2011 Nick Rasmussen
VisAD is Copyright (C) 1996 - 2011 Bill Hibbard, Curtis Rueden, Tom
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

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Toolkit;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import java.rmi.RemoteException;

import visad.BaseColorControl;
import visad.ControlEvent;
import visad.ControlListener;
import visad.VisADException;

/**
 * An extensible RGB colormap with no interpolation between the
 * internally stored values.  Click and drag with the left mouse
 * button to draw the color curves. Click with the right or middle
 * mouse button to alternate between the color curves.
 *
 * @author Nick Rasmussen nick@cae.wisc.edu
 * @version $Revision: 1.22 $, $Date: 2002-09-19 21:08:42 $
 * @since Visad Utility Library, 0.5
 */

public class BaseRGBMap
  extends ColorMap
  implements ControlListener, MouseListener, MouseMotionListener
{
  /** change this to <TT>true</TT> to use color cursors */
  public static boolean USE_COLOR_CURSORS = false;

  /** default resolution */
  public static final int DEFAULT_RESOLUTION =
    BaseColorControl.DEFAULT_NUMBER_OF_COLORS;

  /** The color control */
  private BaseColorControl ctl;

  /** The left modified value */
  private int valLeft = 0;
  /** The right modified value */
  private int valRight = 0;

  /** A lock to synchronize against when modifying the modified area */
  private Object mutex = new Object();

  /** The index of the color red */
  private static final int RED = BaseColorControl.RED;
  /** The index of the color green */
  private static final int GREEN = BaseColorControl.GREEN;
  /** The index of the color blue */
  private static final int BLUE = BaseColorControl.BLUE;
  /** The index of the alpha channel */
  private static final int ALPHA = BaseColorControl.ALPHA;
  /** The current color for the mouse to draw on */
  private int state = RED;

  /** The resolution of the map */
  private int resolution = 256;
  /** 'true' if this map has an alpha component */
  private boolean hasAlpha;

  private static Cursor[] cursor = null;
  /** a slightly brighter blue */
  private static final Color bluish = new Color(80, 80, 255);

  /** the preferred size of this widget */
  private Dimension preferredSize = null;

  /**
   * Construct a BaseRGBMap with the default resolution
   *
   * @param hasAlpha set to <TT>true</TT> is this map has
   *                 an alpha component
   */
  public BaseRGBMap(boolean hasAlpha)
    throws RemoteException, VisADException
  {
    this(defaultTable(DEFAULT_RESOLUTION, hasAlpha));
  }

  /**
   * Construct a colormap with the specified resolution
   *
   * @param resolution the length of the array
   * @param hasAlpha set to <TT>true</TT> is this map has
   *                 an alpha component
   */
  public BaseRGBMap(int resolution, boolean hasAlpha)
    throws RemoteException, VisADException
  {
    this(defaultTable(resolution, hasAlpha));
  }

  /**
   * Construct a colormap initialized with the supplied tuples
   *
   * @param vals the tuples used to initialize the colormap
   * @param hasAlpha <TT>true</TT> if the colormap should
   *                 have an ALPHA component.
   *
   * @deprecated <TT>hasAlpha</TT> isn't really necessary.
   */
  public BaseRGBMap(float[][] vals, boolean hasAlpha)
    throws RemoteException, VisADException
  {
    this(vals != null ? vals : defaultTable(DEFAULT_RESOLUTION, hasAlpha));
  }

  /**
   * Construct a colormap initialized with the supplied tuples
   *
   * @param vals the tuples used to initialize the colormap
   *             See setValues() for constraints on the <TT>vals</TT> array.
   */
  public BaseRGBMap(float[][] vals)
    throws RemoteException, VisADException
  {
    if (vals == null) {
      vals = defaultTable(DEFAULT_RESOLUTION, true);
    }

    setValues(vals);

    if (USE_COLOR_CURSORS) buildCursors();

    addMouseListener(this);
    addMouseMotionListener(this);
    ctl.addControlListener(this);
  }

  /**
   * Construct a colormap from the specified control.
   *
   * @param ctl control to use as data source
   */
  public BaseRGBMap(BaseColorControl ctl)
  {
    this.ctl = ctl;

    hasAlpha = (ctl.getNumberOfComponents() == 4);
    resolution = ctl.getNumberOfColors();

    if (USE_COLOR_CURSORS) buildCursors();

    addMouseListener(this);
    addMouseMotionListener(this);
    ctl.addControlListener(this);
  }

  /**
   * Build a table with the given number of components and colors
   *
   * @param resolution Number of colors.
   * @param hasAlpha <TT>true</TT> if this colormap has an alpha component.
   *
   * @return The new table.
   */
  static float[][] defaultTable(int resolution, boolean hasAlpha)
  {
    final int components = hasAlpha ? 4 : 3;
    float[][] tbl = new float[components][resolution];
    return BaseColorControl.initTableVis5D(tbl);
  }

  /**
   * Build one of the red, green, blue and alpha cursors
   *
   * @param rgba cursor to build (RED, GREEN, BLUE or ALPHA)
   *
   * @return the new <TT>Cursor</TT>
   */
  static Cursor buildRGBACursor(int rgba)
  {
    if (rgba < 0 || rgba > 3) rgba = 0;

    final int lines = 15;
    final int elements = 15;

    int[] pixel = new int[lines*elements];

    for (int i = 0; i < pixel.length; i++) {
      pixel[i] = 0;
    }

    final int color;
    switch (rgba) {
    case RED: color = Color.red.getRGB(); break;
    case GREEN: color = Color.green.getRGB(); break;
    case BLUE: color = bluish.getRGB(); break;
    default:
    case ALPHA: color = Color.gray.getRGB(); break;
    }

    final int midLine = (lines / 2) * elements;
    for (int i = midLine + elements - 1; i >= midLine; i--) {
      pixel[i] = color;
    }

    final int midElement = (elements / 2);
    for (int i = 0; i < lines; i++) {
      pixel[i*elements + midElement] = color;
    }
    java.awt.image.ImageProducer ip;
    ip = new java.awt.image.MemoryImageSource(elements, lines, pixel,
                                              0, elements);

    java.awt.Image img = Toolkit.getDefaultToolkit().createImage(ip);

    Point pt = new Point(img.getWidth(null) / 2, img.getHeight(null) / 2);
    String name;
    switch (rgba) {
    case RED: name = "crossRed"; break;
    case GREEN: name = "crossGreen"; break;
    case BLUE: name = "crossBlue"; break;
    default:
    case ALPHA: name = "crossAlpha"; break;
    }

    return Toolkit.getDefaultToolkit().createCustomCursor(img, pt, name);
  }

  /**
   * Used internally to initialize the red, green, blue and alpha cursors
   */
  private void buildCursors()
  {
    if (cursor != null) return;

    // only try to change the cursor if we're running under JDK 1.3 or greater
    String jVersion = System.getProperty("java.version");
    if (jVersion == null) return;
    if (jVersion.length() < 3) return;
    if (jVersion.charAt(0) < '1') return;
    if (jVersion.charAt(1) != '.') return;
    if (jVersion.charAt(0) == '1' && jVersion.charAt(2) < '3') return;

    cursor = new Cursor[4];
    cursor[RED] = buildRGBACursor(RED);
    cursor[GREEN] = buildRGBACursor(GREEN);
    cursor[BLUE] = buildRGBACursor(BLUE);
    cursor[ALPHA] = buildRGBACursor(ALPHA);

    setCursor(cursor[state]);
  }

  /**
   * Sets the values of the internal array after the map
   * has been created.
   *
   * The table should be <TT>float[resolution][dimension]</TT>
   * where <TT>dimension</TT> is either 3 (for an RGB table)
   * or 4 (if the table also has an alpha component) and
   * <TT>resolution</TT> is the number of colors in the table,
   * which must be greater than 4.
   *
   * @param newVal the color tuples used to initialize the map.
   */
  public void setValues(float[][] newVal)
    throws RemoteException, VisADException
  {
    if (newVal == null) {
      throw new VisADException("Can't set table to null");
    }

    if (newVal.length >= 3 && newVal.length <= 4 && newVal[0].length > 4) {
      hasAlpha = newVal.length > 3;
      resolution = newVal[0].length;
    } else if (newVal[0].length >= 3 && newVal[0].length <= 4 &&
               newVal.length > 4)
    {
      // table is inverted

      hasAlpha = newVal[0].length > 3;
      resolution = newVal.length;

      float[][] tmpVal = new float[hasAlpha ? 4 : 3][resolution];
      for (int i = 0; i < resolution; i++) {
        tmpVal[RED][i] = newVal[i][RED];
        tmpVal[GREEN][i] = newVal[i][GREEN];
        tmpVal[BLUE][i] = newVal[i][BLUE];
        if (hasAlpha) {
          tmpVal[ALPHA][i] = newVal[i][ALPHA];
        }
      }
      newVal = tmpVal;
    } else {
      throw new VisADException("Cannot set table with dimensions [" +
                               newVal.length + "][" + newVal[0].length + "]");
    }

    if (ctl == null) {
      ctl = new BaseColorControl(null, hasAlpha ? 4 : 3);
    }

    ctl.setTable(newVal);

    sendUpdate(0, resolution-1);
  }

  /**
   * Get the resolution of the map
   *
   * @return the number of colors in the map.
   */
  public int getMapResolution() {
    return resolution;
  }

  /**
   * Get the dimension of the map
   *
   * @return either 3 or 4
   */
  public int getMapDimension() {
    return ctl.getNumberOfComponents();
  }

  /**
   * Get the color map (as an array of <TT>float</TT> tuples.
   *
   * @return a copy of the color map
   */
  public float[][] getColorMap() {
    return ctl.getTable();
  }

  /**
   * Returns the tuple at a floating point value val
   *
   * @param firstVal the location to start.
   * @param lastVal the location to finish.
   * @param num the number of tuples to return.
   *
   * @return The array of 3 or 4 element arrays.
   */
  public float[][] getTuples(float firstVal, float lastVal, int num) {
    if (num <= 0 || firstVal > lastVal ||
        (num == 1 && !Util.isApproximatelyEqual(firstVal, lastVal)))
    {
      return null;
    }

    float floatIdx;

    floatIdx = firstVal * (resolution - 1);
    int startIndex = (int )Math.floor(floatIdx);

    floatIdx = lastVal * (resolution - 1);
    int endIndex = (int )Math.floor(floatIdx);

    float partialEnd = floatIdx - endIndex;
    boolean isPartialEnd = (partialEnd != 0);

    float[][] tuples = new float[num][hasAlpha ? 4 : 3];

    int rStart = startIndex;
    if (rStart < 0) {
      rStart = 0;
    } else if (rStart >= resolution) {
      rStart = resolution - 1;
    }

    int rEnd = (isPartialEnd ? endIndex+1 : endIndex);
    if (rEnd >= resolution) {
      rEnd = resolution - 1;
    } else if (rEnd < 0) {
      rEnd = 0;
    }

    final int rLen = (rEnd - rStart) + 1;

    float[][] colors;
    try {
      colors = ctl.lookupRange(rStart, rEnd);
    } catch (Exception e) {
      System.err.println("Error in " + getClass().getName() + ": " +
                         e.getClass().getName() + ": " + e.getMessage());
      return null;
    }

    float stepVal = (lastVal - firstVal) / (float )num;

    for (int i = 0; i < num; i++) {
      float thisVal = firstVal + (float )i * stepVal;

      floatIdx = thisVal * (resolution - 1);
      int index = (int )Math.floor(floatIdx);

      float partial = floatIdx - index;
      boolean isPartial = (partial != 0);

      index -= rStart;

      if (index < 0 || index >= rLen ||
          (index == (rLen - 1) && isPartial))
      {
        tuples[i][RED] = tuples[i][GREEN] = tuples[i][BLUE] = 0;
        if (hasAlpha) {
          tuples[i][ALPHA] = 0;
        }
        continue;
      }

      if (isPartial) {
        tuples[i][RED] = colors[RED][index] * (1 - partial) +
          colors[RED][index+1] * partial;
        tuples[i][GREEN] = colors[GREEN][index] * (1 - partial) +
          colors[GREEN][index+1] * partial;
        tuples[i][BLUE] = colors[BLUE][index] * (1 - partial) +
          colors[BLUE][index+1] * partial;
        if (hasAlpha) {
          tuples[i][ALPHA] = colors[ALPHA][index] * (1 - partial) +
            colors[ALPHA][index+1] * partial;
        }
      } else {
        tuples[i][RED] = colors[RED][index];
        tuples[i][GREEN] = colors[GREEN][index];
        tuples[i][BLUE] = colors[BLUE][index];
        if (hasAlpha) {
          tuples[i][ALPHA] = colors[ALPHA][index];
        }
      }
    }

    return tuples;
  }

  /**
   * Returns the tuple at a floating point value val
   *
   * <B>WARNING</B>: This is a <I>really</I> slow way to
   * get a color, so don't use it inside a loop.
   *
   * @param value the location to return.
   *
   * @return The 3 or 4 element array.
   */
  public float[] getTuple(float value) {
    float[][] v = getTuples(value, value, 1);
    return (v == null ? null : v[0]);
  }

  /**
   * Implementation of the abstract function in ColorMap
   *
   * <B>WARNING</B>: This is a <I>really</I> slow way to
   * get a color, so don't use it inside a loop.
   */
  public float[][] getRGBTuples(float startVal, float endVal, int num) {
    float[][] t = getTuples(startVal, endVal, num);
    if (t[0].length > 3) {
      float[][] f = new float[t.length][3];
      for (int i = t.length - 1; i >= 0; i--) {
        f[i][RED] = t[i][RED];
        f[i][GREEN] = t[i][GREEN];
        f[i][BLUE] = t[i][BLUE];
      }
      t = f;
    }
    return t;
  }

  /**
   * Implementation of the abstract function in ColorMap
   *
   * <B>WARNING</B>: This is a <I>really</I> slow way to
   * get a color, so don't use it inside a loop.
   *
   * @param value a floating point number between 0 and 1
   * @return an RGB tuple of floating point numbers in the
   * range 0 to 1
   */
  public float[] getRGBTuple(float value) {
    float[] t = getTuple(value);
    if (t.length > 3) {
      float[] f = new float[3];
      f[RED] = t[RED];
      f[GREEN] = t[GREEN];
      f[BLUE] = t[BLUE];
      t = f;
    }
    return t;
  }

  /**
   * Redraw the between the <TT>left</TT> and
   * <TT>right</TT> colors
   *
   * @param left the left edge of the changed area (in the range 0.0-1.0)
   * @param right the right edge of the changed area
   */
  protected void sendUpdate(int left, int right) {

    notifyListeners(new ColorChangeEvent(this, left, right));

    if (left != 0) {
      left--;
    }
    if (right != resolution - 1) {
      right++;
    }

    synchronized (mutex) {
      if (left < valLeft)
        valLeft = left;
      if (right > valRight)
        valRight = right;
    }

    // redraw
    validate();
    repaint();
  }

  /**
   * Present to implement MouseListener, currently ignored
   *
   * @param evt ignored
   */
  public void mouseClicked(MouseEvent evt) {
  }

  /**
   * MouseListener, currently ignored
   *
   * @param evt ignored
   */
  public void mouseEntered(MouseEvent evt) {
  }

  /**
   * MouseListener method, currently ignored
   *
   * @param evt ignored
   */
  public void mouseExited(MouseEvent evt) {
  }

  /** The last mouse event's x value */
  private int oldX;
  /** The last mouse event's y value */
  private int oldY;

  /** A synchronization primitive for the mouse movements */
  private Object mouseMutex = new Object();

  /**
   * Updates the associated Control
   *
   * @param evt the mouse press event
   */
  public void mousePressed(MouseEvent evt) {
    if ((evt.getModifiers() & evt.BUTTON1_MASK) == 0 &&
        evt.getModifiers() != 0)
    {
      return;
    }

    int width = getBounds().width;
    int height = getBounds().height;
    int x = evt.getX();
    int y = evt.getY();

    if (x < 0)
      x = 0;
    else if (x >= width)
      x = width - 1;
    if (y < 0)
      y = 0;
    else if (y >= height)
      y = height - 1;

    float step = (float )(resolution - 1) / (float )width;
    int pos = (int )Math.floor((float )x * step + 0.5);

    float[][] colors;
    try {
      colors = ctl.lookupRange(pos, pos);
    } catch (Exception e) {
      System.err.println("Error in " + getClass().getName() + ": " +
                         e.getClass().getName() + ": " + e.getMessage());
      return;
    }

    colors[state][0] = 1 - (float )y / (float )height;

    try {
      ctl.setRange(pos, pos, colors);
    } catch (Exception e) {
      System.err.println("Error in " + getClass().getName() + ": " +
                         e.getClass().getName() + ": " + e.getMessage());
      return;
    }

    oldX = x;
    oldY = y;

    sendUpdate(pos, pos);
  }

  /**
   * Listens for releases of the right mouse button,
   * and changes the active color
   *
   * @param evt the release event
   */
  public void mouseReleased(MouseEvent evt) {
    if ((evt.getModifiers() & (evt.BUTTON2_MASK|evt.BUTTON3_MASK)) == 0) {
      return;
    }
    state = (state + 1) % (hasAlpha ? 4 : 3);
    if (cursor != null) {
      setCursor(cursor[state]);
    }
  }

  /**
   * Updates the associated Control
   *
   * @param evt the drag event
   */
  public void mouseDragged(MouseEvent evt) {
    if ((evt.getModifiers() & evt.BUTTON1_MASK) == 0 &&
        evt.getModifiers() != 0)
    {
      return;
    }

    drag(evt.getX(), evt.getY(), oldX, oldY);

    oldX = evt.getX();
    oldY = evt.getY();
  }

  /**
   * Internal mouse dragging function
   *
   * @param x the current x coordinate
   * @param y the current y coordinate
   * @param oldx the starting x coordinate
   * @param oldy the starting y coordinate
   */
  private void drag(int x, int y, int oldx, int oldy) {

    int width = getBounds().width;
    int height = getBounds().height;

    // make sure x, y, oldx and oldy are all inside the window
    if (x < 0)
      x = 0;
    else if (x >= width)
      x = width - 1;
    if (y < 0)
      y = 0;
    else if (y >= height)
      y = height - 1;
    if (oldx < 0)
      oldx = 0;
    else if (oldx >= width)
      oldx = width - 1;
    if (oldy < 0)
      oldy = 0;
    else if (oldy >= height)
      oldy = height - 1;

    float step = (float )(resolution - 1) / (float )width;

    int oldPos = (int )Math.floor((float )oldx * step + 0.5);
    int newPos = (int )Math.floor((float )x * step + 0.5);

    float oldVal = 1 - (float )oldy / (float )height;
    float newVal = 1 - (float )y / (float )height;

    final int start, finish;
    final int len = ctl.getNumberOfColors() - 1;
    if (newPos > oldPos) {
      start = oldx < width - 1 ? oldPos : len;
      finish = x < width - 1 ? newPos : len;
    } else {
      start = x < width - 1 ? newPos : len;
      finish = oldx < width - 1 ? oldPos : len;
    }

    float[][] colors;
    try {
      colors = ctl.lookupRange(start, finish);
    } catch (Exception e) {
      System.err.println("Error in " + getClass().getName() + ": " +
                         e.getClass().getName() + ": " + e.getMessage());
      return;
    }

    final float loVal, hiVal;

    if (newPos > oldPos) {
      loVal = newVal;
      hiVal = oldVal;
    } else {
      loVal = oldVal;
      hiVal = newVal;
    }

    final int total = finish - start + 1;
    for (int i = 0; i < total; i++) {
      float v = ((hiVal * (float )(total - i) + loVal * (float )i) /
                 (float )total);
      colors[state][i] = v;
    }

    try {
      ctl.setRange(start, finish, colors);
    } catch (Exception e) {
      System.err.println("Error in " + getClass().getName() + ": " +
                         e.getClass().getName() + ": " + e.getMessage());
      return;
    }

    sendUpdate(start, finish);
  }

  /**
   * MouseMovementListener method, currently ignored
   *
   * @param evt ignored
   */
  public void mouseMoved(MouseEvent evt) {
  }

  /**
   * Repaints the entire JPanel
   *
   * @param g The <TT>Graphics</TT> to update.
   */
  public void paint(Graphics g) {

    synchronized (mutex) {

      valLeft = 0;
      valRight = resolution - 1;
    }

    update(g);
  }

  /** The left bound for updating the JPanel */
  private float updateLeft = 0;

  /** The right bound for updating the JPanel */
  private float updateRight = 1;

  /**
   * Repaints the modified areas of the JPanel
   *
   * @param g The <TT>Graphics</TT> to update.
   */
  public void update(Graphics g) {

    final int maxRight = resolution - 1;

    int left = 0;
    int right = maxRight;

    synchronized (mutex) {
      if (valLeft > valRight) {
        return;
      }

      left = valLeft;
      right = valRight;

      valLeft = maxRight;
      valRight = 0;
    }

    final int numColors = ctl.getNumberOfColors() - 1;

    if (left < 0) {
      left = 0;
    } else if (left > numColors) {
      left = numColors;
    }
    if (right < 0) {
      right = 0;
    } else if (right > numColors) {
      right = numColors;
    }

    if (left > 0) {
      left--;
    }
    if (right < maxRight) {
      right++;
    }

    final int maxWidth = getBounds().width - 1;
    final int maxHeight = getBounds().height - 1;

    int leftPixel = (left * maxWidth) / maxRight;
    int rightPixel = (right * maxWidth) / maxRight;

    g.setColor(Color.black);
    g.fillRect(leftPixel,0,rightPixel - leftPixel + 1, maxHeight + 1);

    if (left > 0) {
      left--;
    }
    if (right < maxRight) {
      right++;
    }

    leftPixel = (left * maxWidth) / maxRight;
    rightPixel = (right * maxWidth) / maxRight;

    float[][] colors;
    try {
      colors = ctl.lookupRange(left, right < maxRight ? right + 1 : maxRight);
    } catch (Exception e) {
      System.err.println("Error in " + getClass().getName() + ": " +
                         e.getClass().getName() + ": " + e.getMessage());
      colors = null;
    }

    if (colors == null) {
      return;
    }

    int prevEnd = leftPixel;

    int prevRed = (int )Math.floor((1 - colors[RED][0]) * maxHeight);
    int prevGreen = (int )Math.floor((1 - colors[GREEN][0]) * maxHeight);
    int prevBlue = (int )Math.floor((1 - colors[BLUE][0]) * maxHeight);
    int prevAlpha;
    if (hasAlpha) {
      prevAlpha = (int )Math.floor((1 - colors[ALPHA][0]) * maxHeight);
    } else {
      prevAlpha = 0;
    }

    int alpha = 0;
    for (int i = 1; i < colors[0].length; i++) {
      int lineEnd = ((left + i) * maxWidth) / maxRight;

      int red = (int )Math.floor((1 - colors[RED][i]) * maxHeight);
      int green = (int )Math.floor((1 - colors[GREEN][i]) * maxHeight);
      int blue = (int )Math.floor((1 - colors[BLUE][i]) * maxHeight);
      if (hasAlpha) {
        alpha = (int )Math.floor((1 - colors[ALPHA][i]) * maxHeight);
      }

      g.setColor(Color.red);
      g.drawLine(prevEnd, prevRed, lineEnd, red);

      g.setColor(Color.green);
      g.drawLine(prevEnd, prevGreen, lineEnd, green);

      g.setColor(bluish);
      g.drawLine(prevEnd, prevBlue, lineEnd, blue);

      if (hasAlpha) {
        g.setColor(Color.gray);
        g.drawLine(prevEnd, prevAlpha, lineEnd, alpha);
      }

      prevEnd = lineEnd;

      prevRed = red;
      prevGreen = green;
      prevBlue = blue;
      if (hasAlpha) {
        prevAlpha = alpha;
      }
    }
  }

  /**
   * Return the preferred size of this map, taking into account
   * the resolution.
   *
   * @return preferred size.
   */
  public Dimension getPreferredSize()
  {
    if (preferredSize == null) {
      final int height = 170;
      preferredSize = new Dimension(resolution, height);
    }
    return preferredSize;
  }

  /**
   * Override the widget's calculation for the preferred size
   * of this map.
   *
   * @param pref preferred size.
   */
  public void setPreferredSize(Dimension pref)
  {
    preferredSize = pref;
  }

  /**
   * If the color data in the <CODE>Control</CODE> associated with this
   * widget's <CODE>Control</CODE> has changed, update the data in
   * the <CODE>ColorMap</CODE>.
   *
   * @param evt Data from the changed <CODE>Control</CODE>.
   */
  public void controlChanged(ControlEvent evt)
    throws RemoteException, VisADException
  {
    hasAlpha = (ctl.getNumberOfComponents() == 4);
    resolution = ctl.getNumberOfColors();

    sendUpdate(0, getMapResolution()-1);
  }
}
