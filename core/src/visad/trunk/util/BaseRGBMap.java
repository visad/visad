/*

@(#) $Id: BaseRGBMap.java,v 1.6 1999-11-17 21:44:53 dglo Exp $

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

import java.awt.event.*;
import java.awt.*;

/**
 * An extensible RGB colormap with no interpolation between the
 * internally stored values.  Click and drag with the left mouse
 * button to draw the color curves. Click with the right or middle
 * mouse button to alternate between the color curves.
 *
 * @author Nick Rasmussen nick@cae.wisc.edu
 * @version $Revision: 1.6 $, $Date: 1999-11-17 21:44:53 $
 * @since Visad Utility Library, 0.5
 */

public class BaseRGBMap
  extends ColorMap
  implements MouseListener, MouseMotionListener
{
  /** The array of color tuples */
  private float[][] val;

  /** The left modified value */
  private int valLeft;
  /** The right modified value */
  private int valRight;

  /** A lock to synchronize against when modifying the modified area */
  private Object mutex = new Object();

  /** A lock to synchronize against when modifying val or resolution */
  private Object mutex_val = new Object();

  /** The index of the color red */
  private static final int RED = 0;
  /** The index of the color green */
  private static final int GREEN = 1;
  /** The index of the color blue */
  private static final int BLUE = 2;
  /** The index of the alpha channel */
  private static final int ALPHA = 3;
  /** The current color for the mouse to draw on */
  private int state = RED;

  /** The resolution of the map */
  private int resolution;
  /** 'true' if this map has an alpha component */
  private boolean hasAlpha;

  private static Cursor[] cursor = null;
  /** a slightly brighter blue */
  private static final Color bluish = new Color(80, 80, 255);

  /** Construct a BaseRGBMap with the default resolution of 256
   * @param hasAlpha set to <TT>true</TT> is this map has
   *                 an alpha component
   */
  public BaseRGBMap(boolean hasAlpha) {
    this(256, hasAlpha);
  }

  /** The BaseRGBMap map is represented internally by an array of
   * floats
   * @param resolution the length of the array
   * @param hasAlpha set to <TT>true</TT> is this map has
   *                 an alpha component
   */
  public BaseRGBMap(int resolution, boolean hasAlpha) {
    this.resolution = resolution;
    this.hasAlpha = hasAlpha;

    // buildCursors();

    val = new float[resolution][hasAlpha ? 4 : 3];
    initColormap();

    addMouseListener(this);
    addMouseMotionListener(this);
  }

  public BaseRGBMap(float[][] vals, boolean hasAlpha) {
    this.hasAlpha = hasAlpha;

    // buildCursors();

    setValues(vals, false);

    addMouseListener(this);
    addMouseMotionListener(this);
  }

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
    case RED: color = (255<<16) + (255<<24); break;
    case GREEN: color = (255<<8) + (255<<24); break;
    case BLUE: color = 255 + (255<<24); break;
    default:
    case ALPHA: color = 127 + (127<<8) + (127<<16) + (255<<24); break;
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

  private void buildCursors()
  {
    if (cursor != null) return;

    cursor = new Cursor[4];
    cursor[RED] = buildRGBACursor(RED);
    cursor[GREEN] = buildRGBACursor(GREEN);
    cursor[BLUE] = buildRGBACursor(BLUE);
    cursor[ALPHA] = buildRGBACursor(ALPHA);

    setCursor(cursor[state]);
  }

  /** Sets the values of the internal array after the map
      has been created. */
  public void setValues(float[][] vals) {
    setValues(vals, true);
  }

  private void setValues(float[][] vals, boolean notify) {
    int note = 0;
    synchronized (mutex_val) {
      if (vals == null) {
        this.resolution = 256;
        val = new float[this.resolution][hasAlpha ? 4 : 3];
        this.initColormap();
      }
      else {
        this.resolution = vals.length;
        val = new float[this.resolution][hasAlpha ? 4 : 3];
        for (int i = 0; i < this.resolution; i++) {
          val[i][0] = vals[i][0];
          val[i][1] = vals[i][1];
          val[i][2] = vals[i][2];
          if (hasAlpha) {
            val[i][3] = vals[i][3];
          }
        }
      }
      note = this.resolution-1;
    }

    // if (notify) notifyListeners(0, this.resolution-1);
    if (notify) notifyListeners(0, note);
  }

  /** Returns the resolution of the map */
  public int getMapResolution() {
    return resolution;
  }

  /** Returns the dimension of the map */
  public int getMapDimension() {
    return hasAlpha ? 4: 3;
  }

  /** Returns a copy of the color map */
  public float[][] getColorMap() {

    synchronized (mutex_val) {
      float[][] ret = new float[resolution][hasAlpha ? 4 : 3];

      for (int i = 0; i < resolution; i++) {
        ret[i][0] = val[i][0];
        ret[i][1] = val[i][1];
        ret[i][2] = val[i][2];
        if (hasAlpha) {
          ret[i][3] = val[i][3];
        }
      }

      return ret;
    }
  }

  /** Returns the tuple at a floating point value val */
  public float[] getTuple(float value) {
    synchronized (mutex_val) {
      float arrayIndex = value * (resolution - 1);
      int index = (int) Math.floor(arrayIndex);
      float partial = arrayIndex - index;

      if (index >= resolution || index < 0 ||
          (index == (resolution - 1) && partial != 0))
      {
        float[] f;
        if (hasAlpha) {
          float[] fx = {0,0,0,0};
          f = fx;
        } else {
          float[] fx = {0,0,0};
          f = fx;
        }
        return f;
      }

      float red, green, blue, alpha = 0.0F;
      if (partial != 0) {
        red = val[index][RED] * (1 - partial) +
          val[index+1][RED] * partial;
        green = val[index][GREEN] * (1 - partial) +
          val[index+1][GREEN] * partial;
        blue = val[index][BLUE] * (1 - partial) +
          val[index+1][BLUE] * partial;
        if (hasAlpha) {
          alpha = val[index][ALPHA] * (1 - partial) +
            val[index+1][ALPHA] * partial;
        }
      } else {
        red = val[index][RED];
        green = val[index][GREEN];
        blue = val[index][BLUE];
        if (hasAlpha) {
          alpha = val[index][ALPHA];
        }
      }
      float[] f;
      if (hasAlpha) {
        float[] fx = {red, green, blue, alpha};
        f = fx;
      } else {
        float[] fx = {red, green, blue};
        f = fx;
      }
      return f;
    }
  }

  protected void sendUpdate(int left, int right) {

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

  /** Used internally to post areas to update to the objects listening
   * to the map
   */
  protected void notifyListeners(int left, int right) {

    // !!!fix this to reflect a more accurate region of affectation
    if (left != 0) {
      left--;
    }
    if (right != resolution - 1) {
      right++;
    }

    float start = (float) left / (float) (resolution - 1);
    float end = (float) right + 1 / (float) (resolution - 1);
    sendUpdate(left, right);
    super.notifyListeners(new ColorChangeEvent(this, start, end));

  }

  /** Implementation of the abstract function in ColorMap
   * @param value a floating point number between 0 and 1
   * @return an RGB tuple of floating point numbers in the
   * range 0 to 1
   */
  public float[] getRGBTuple(float value) {
    float[] t = getTuple(value);
    if (t.length > 3) {
      float[] f = new float[3];
      f[0] = t[0];
      f[1] = t[1];
      f[2] = t[2];
      t = f;
    }
    return t;
  }

  /** Present to implement MouseListener, currently ignored */
  public void mouseClicked(MouseEvent e) {
    //System.out.println(e.paramString());
  }

  /** Present to implement MouseListener, currently ignored */
  public void mouseEntered(MouseEvent e) {
    //System.out.println(e.paramString());
  }

  /** Present to implement MouseListener, currently ignored */
  public void mouseExited(MouseEvent e) {
    //System.out.println(e.paramString());
  }

  /** The last mouse event's x value */
  private int oldX;
  /** The last mouse event's y value */
  private int oldY;

  /** A synchronization primitive for the mouse movements */
  private Object mouseMutex = new Object();

  /** Updates the internal array and sends notification to the
   * ColorChangeListeners that are listening to this map
   */
  public void mousePressed(MouseEvent e) {
    //System.out.println(e.paramString());
    if ((e.getModifiers() & e.BUTTON1_MASK) == 0 &&
        e.getModifiers() != 0)
    {
      return;
    }

    int index = 0;
    int width = getBounds().width;
    int height = getBounds().height;
    int x = e.getX();
    int y = e.getY();

    if (x < 0)
      x = 0;

    if (x >= width)
      x = width - 1;

    if (y < 0)
      y = 0;

    if (y >= height)
      y = height - 1;

    float dist = (float) x / (float) width;
    synchronized (mutex_val) {
      index = (int) Math.floor(dist * (resolution - 1) + 0.5);
      val[index][state] = 1 - (float) y / (float) height;
    }

    oldX = x;
    oldY = y;

    notifyListeners(index, index);


  }

  /** Listens for releases of the right mouse button, and changes the active color */
  public void mouseReleased(MouseEvent e) {
    //System.out.println(e.paramString());
    if ((e.getModifiers() & (e.BUTTON2_MASK|e.BUTTON3_MASK)) == 0) {
      return;
    }
    state = (state + 1) % (hasAlpha ? 4 : 3);
    if (cursor != null) {
      setCursor(cursor[state]);
    }
  }

  /** Updates the internal array and sends notification to the
   * ColorChangeListeners that are listening to this map
   */
  public void mouseDragged(MouseEvent e) {
    //System.out.println(e.paramString());
    if ((e.getModifiers() & e.BUTTON1_MASK) == 0 && e.getModifiers() != 0) {
      return;
    }

    drag(e.getX(), e.getY(), oldX, oldY);

    oldX = e.getX();
    oldY = e.getY();
  }

  /** Internal mouse dragging function */
  private void drag(int x, int y, int oldx, int oldy) {

    if (x < 0)
      x = 0;

    if (x >= getBounds().width)
      x = getBounds().width - 1;

    if (y < 0)
      y = 0;

    if (y >= getBounds().height)
      y = getBounds().height - 1;

    if (oldx < 0)
      oldx = 0;

    if (oldx >= getBounds().width)
      oldx = getBounds().width - 1;

    if (oldy < 0)
      oldy = 0;

    if (oldy >= getBounds().height)
      oldy = getBounds().height - 1;


    int notelow = -1;
    int notehi = -1;
    synchronized (mutex_val) {
      float dist = (float) x / (float) (getBounds().width - 1);
      int index = (int) Math.floor(dist * (resolution - 1) + 0.5);

      float oldDist = (float) oldx / (float) (getBounds().width - 1);
      int oldPos = (int) Math.floor(oldDist * (resolution - 1) + 0.5);

      float oldVal = val[oldPos][state];
      float target = 1 - (float) y / (float) (getBounds().height - 1);

      if (index > oldPos) {
        for (int i = oldPos + 1; i <= index; i++) {
          val[i][state] = oldVal * ((float) (index - i)) / ((float) (index - oldPos))
            + target * ((float) (i - oldPos)) / ((float) (index - oldPos));
        }
        // notifyListeners(oldPos + 1, index);
        // return;
        notelow = oldPos + 1;
        notehi = index;
      }
      if (index < oldPos) {
        for (int i = oldPos - 1; i >= index; i--) {
          val[i][state] = oldVal * ((float) (i - index)) / ((float) (oldPos - index))
            + target * ((float) (oldPos - i)) / ((float) (oldPos - index));
        }
        // notifyListeners(index, oldPos - 1);
        // return;
        notelow = index;
        notehi = oldPos - 1;
      }
      if (index == oldPos) {
        val[index][state] = target;
        // notifyListeners(index, index);
        // return;
        notelow = index;
        notehi = index;
      }
    }
    if (notelow > -1 && notehi > -1) notifyListeners(notelow, notehi);
    return;
  }

  /** Present to implement MouseMovementListener, currently ignored */
  public void mouseMoved(MouseEvent e) {
    //System.out.println(e.paramString());
  }

  /** Repaints the entire Panel */
  public void paint(Graphics g) {

    synchronized (mutex) {

      valLeft = 0;
      valRight = resolution - 1;
    }

    update(g);
  }

  /** The left bound for updating the Panel */
  private float updateLeft = 0;

  /** The right bound for updating the Panel */
  private float updateRight = 1;

  /** Repaints the modified areas of the Panel */
  public void update(Graphics g) {

    // System.out.println("update");
    synchronized (mutex_val) {

      int left = 0;
      int right = resolution - 1;

      synchronized (mutex) {
        if (valLeft > valRight) return;

        left = valLeft;
        right = valRight;

        valLeft = resolution - 1;
        valRight = 0;
      }

      Rectangle bounds = getBounds();

      if (left != 0) {
        left--;
      }

      if (right != resolution - 1) {
        right++;
      }

      int leftPixel = (left * (bounds.width - 1)) / (resolution - 1);
      int rightPixel = (right * (bounds.width - 1)) / (resolution - 1);

      g.setColor(Color.black);
      g.fillRect(leftPixel,0,rightPixel - leftPixel + 1, bounds.height);


      if (left != 0) {
        left--;
      }

      if (right != resolution - 1) {
        right++;
      }

      leftPixel = (left * (bounds.width - 1)) / (resolution - 1);
      rightPixel = (right * (bounds.width - 1)) / (resolution - 1);

      int len = val.length - 1;
      if (left < 0) left = 0;
      if (left > len) left = len;
      if (right < 0) right = 0;
      if (right > len) right = len;

      int prevEnd = leftPixel;

      int prevRed = (int) Math.floor((1 - val[left][RED]) * (bounds.height - 1));
      int prevGreen = (int) Math.floor((1 - val[left][GREEN]) * (bounds.height - 1));
      int prevBlue = (int) Math.floor((1 - val[left][BLUE]) * (bounds.height - 1));
      int prevAlpha;
      if (hasAlpha) {
        prevAlpha = (int) Math.floor((1 - val[left][ALPHA]) * (bounds.height - 1));
      } else {
        prevAlpha = 0;
      }

      int alpha = 0;
      for (int i = left + 1; i <= right; i++) {
        int lineEnd = (i * (getBounds().width - 1)) / (resolution - 1);

        int red = (int) Math.floor((1 - val[i][RED]) * (bounds.height - 1));
        int green = (int) Math.floor((1 - val[i][GREEN]) * (bounds.height - 1));
        int blue = (int) Math.floor((1 - val[i][BLUE]) * (bounds.height - 1));
        if (hasAlpha) {
          alpha = (int) Math.floor((1 - val[i][ALPHA]) * (bounds.height - 1));
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
  }

  /** Return the preferred size of this map, taking into account
   * the resolution */
  public Dimension getPreferredSize() {
    return new Dimension(resolution, resolution / 2);
  }

  /** Initializes the colormap to default values */
  private void initColormap() {
    initColormapVis5D();
  }

  private void initColormapVis5D() {
    synchronized (mutex_val) {

      float curve = 1.4f;
      float bias = 1.0f;
      float rfact = 0.5f * bias;

      for (int i = 0; i < resolution; i++) {

        /* compute s in [0,1] */
        float s = (float) i / (float) (resolution-1);

        float t = curve * (s - rfact);   /* t in [curve*-0.5,curve*0.5) */
        val[i][RED] = (float) (0.5 + 0.5 * Math.atan( 7.0*t ) / 1.57);
        val[i][GREEN] = (float) (0.5 + 0.5 * (2 * Math.exp(-7*t*t) - 1));
        val[i][BLUE] = (float) (0.5 + 0.5 * Math.atan( -7.0*t ) / 1.57);
        if (hasAlpha) {
          val[i][ALPHA] = 1.0f;
        }
      }
    }
  }


  /** Initializes the colormap to be linear in hue */
  private synchronized void initColormapHSV() {
    float s = 1;
    float v = 1;

    synchronized (mutex_val) {

      for (int i = 0; i < resolution; i++) {

        float h = i * 6 / (float) (resolution - 1);

        int hFloor = (int) Math.floor(h);
        float hPart = h - hFloor;

        // if hFloor is even
        if ((hFloor & 1) == 0) {
          hPart = 1 - hPart;
        }

        float m = v * (1 - s);
        float n = v * (1 - s*hPart);

        float r = 0;
        float g = 0;
        float b = 0;
        switch (hFloor) {
        case 6:
        case 0:
          r = v;
          g = n;
          b = m;
          break;
        case 1:
          r = n;
          g = v;
          b = m;
          break;
        case 2:
          r = m;
          g = v;
          b = n;
          break;
        case 3:
          r = m;
          g = n;
          b = v;
          break;
        case 4:
          r = n;
          g = m;
          b = v;
          break;
        case 5:
          r = v;
          g = m;
          b = n;
          break;
        }

        val[i][RED] = r;
        val[i][GREEN] = g;
        val[i][BLUE] = b;
        if (hasAlpha) {
          val[i][ALPHA] = 1.0f;
        }
      }
    }
  }
}
