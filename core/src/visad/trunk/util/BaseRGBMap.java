/*

@(#) $Id: BaseRGBMap.java,v 1.10 2000-02-04 20:34:57 dglo Exp $

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
 * @version $Revision: 1.10 $, $Date: 2000-02-04 20:34:57 $
 * @since Visad Utility Library, 0.5
 */

public class BaseRGBMap
  extends ColorMap
  implements MouseListener, MouseMotionListener
{
  /** change this to <TT>true</TT> to use color cursors */
  public static boolean USE_COLOR_CURSORS = false;

  /** default resolution */
  public static final int DEFAULT_RESOLUTION = 256;

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

  /**
   * Construct a BaseRGBMap with the default resolution
   *
   * @param hasAlpha set to <TT>true</TT> is this map has
   *                 an alpha component
   */
  public BaseRGBMap(boolean hasAlpha) {
    this(DEFAULT_RESOLUTION, hasAlpha);
  }

  /**
   * Construct a colormap with the specified resolution
   *
   * @param resolution the length of the array
   * @param hasAlpha set to <TT>true</TT> is this map has
   *                 an alpha component
   */
  public BaseRGBMap(int resolution, boolean hasAlpha) {
    this.resolution = resolution;
    this.hasAlpha = hasAlpha;

    if (USE_COLOR_CURSORS) buildCursors();

    val = new float[resolution][hasAlpha ? 4 : 3];
    initColormap();

    addMouseListener(this);
    addMouseMotionListener(this);
  }

  /**
   * Construct a colormap initialized with the supplied tuples
   *
   * @param vals the tuples used to initialize the colormap
   * @param hasAlpha <TT>true</TT> if the colormap should
   *                 have an ALPHA component.
   */
  public BaseRGBMap(float[][] vals, boolean hasAlpha) {
    this.hasAlpha = hasAlpha;

    if (USE_COLOR_CURSORS) buildCursors();

    setValues(vals, false);

    addMouseListener(this);
    addMouseMotionListener(this);
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
   * @param newVal the color tuples used to initialize the map.
   */
  public void setValues(float[][] newVal) {
    setValues(newVal, true);
  }

  /**
   * Sets the values of the internal array after the map
   * has been created.
   *
   * @param newVal the color tuples used to initialize the map.
   * @param notify <TT>true</TT> if listeners are notified of
   *               the change.  This should only be <TT>false<TT>
   *               when called from the constructor.
   */
  private void setValues(float[][] newVal, boolean notify) {
    int newResolution;
    synchronized (mutex_val) {
      if (newVal == null) {
        resolution = DEFAULT_RESOLUTION;
        val = new float[resolution][hasAlpha ? 4 : 3];
        initColormap();
        newResolution = 0;
      } else if (newVal.length > 4 && newVal[0].length >= 3 &&
                 newVal[0].length <= 4)
      {
        final boolean newHasAlpha = newVal[0].length > 3;
        resolution = newVal.length;
        val = new float[resolution][hasAlpha ? 4 : 3];
        for (int i = 0; i < resolution; i++) {
          val[i][0] = newVal[i][0];
          val[i][1] = newVal[i][1];
          val[i][2] = newVal[i][2];
          if (hasAlpha) {
            if (newHasAlpha) {
              val[i][3] = newVal[i][3];
            } else {
              val[i][3] = 0.0f;
            }
          }
        }
        newResolution = resolution-1;
      } else {
        // table is inverted
        final boolean newHasAlpha = newVal.length > 3;
        resolution = newVal[0].length;
        val = new float[resolution][hasAlpha ? 4 : 3];
        for (int i = 0; i < resolution; i++) {
          val[i][0] = newVal[0][i];
          val[i][1] = newVal[1][i];
          val[i][2] = newVal[2][i];
          if (hasAlpha) {
            if (newHasAlpha) {
              val[i][3] = newVal[3][i];
            } else {
              val[i][3] = 0.0f;
            }
          }
        }
        newResolution = resolution-1;
      }
    }

    if (notify) notifyListeners(0, newResolution);
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
    return hasAlpha ? 4: 3;
  }

  /**
   * Get the color map (as an array of <TT>float</TT> tuples.
   *
   * @return a copy of the color map
   */
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

  /**
   * Returns the tuple at a floating point value val
   *
   * @param value the location to return.
   *
   * @return The 3 or 4 element array.
   */
  public float[] getTuple(float value) {
    synchronized (mutex_val) {
      float arrayIndex = value * (resolution - 1);
      int index = (int )Math.floor(arrayIndex);
      float partial = arrayIndex - index;
      boolean isPartial = (partial != 0);

      if (index >= resolution || index < 0 ||
          (index == (resolution - 1) && isPartial))
      {
        if (hasAlpha) {
          return new float[] {0,0,0,0};
        } else {
          return new float[] {0,0,0};
        }
      }

      float red, green, blue, alpha = 0.0F;
      if (isPartial) {
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

      if (hasAlpha) {
        return new float[] {red, green, blue, alpha};
      } else {
        return new float[] {red, green, blue};
      }
    }
  }

  /**
   * Redraw the between the <TT>left</TT> and
   * <TT>right</TT> colors
   *
   * @param left the left edge of the changed area (in the range 0.0-1.0)
   * @param right the right edge of the changed area
   */
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

  /**
   * Used internally to post areas to update to the objects listening
   * to the map
   *
   * @param left the left edge of the changed area
   * @param right the right edge of the changed area
   */
  protected void notifyListeners(int left, int right) {

    // !!!fix this to reflect a more accurate region of affectation
    if (left != 0) {
      left--;
    }
    if (right != resolution - 1) {
      right++;
    }

    float start = (float )left / (float )(resolution - 1);
    float end = (float )right + 1 / (float )(resolution - 1);
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
   * Updates the internal array and sends notification to the
   * ColorChangeListeners that are listening to this map
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

    int pos;
    synchronized (mutex_val) {
      float step = (float )(resolution - 1) / (float )width;
      pos = (int )Math.floor(x * step + 0.5);
      val[pos][state] = 1 - (float )y / (float )height;
    }

    oldX = x;
    oldY = y;

    notifyListeners(pos, pos);
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
   * Updates the internal array and sends notification to the
   * ColorChangeListeners that are listening to this map
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


    int notelow = -1;
    int notehi = -1;
    synchronized (mutex_val) {
    float step = (float )(resolution - 1) / (float )width;

    int oldPos = (int )Math.floor((float )oldx * step + 0.5);
    int newPos = (int )Math.floor((float )x * step + 0.5);

    float oldVal = 1 - (float )oldy / (float )(height - 1);
    float newVal = 1 - (float )y / (float )(height - 1);

    if (x == oldx) {
      val[newPos][state] = newVal;
      notelow = notehi = newPos;
    } else {

      final int start, finish;
      final float loVal, hiVal;
      final int adj;

      if (newPos > oldPos) {
        start = oldPos;
        finish = newPos;
        loVal = newVal;
        hiVal = oldVal;
        adj = 1;
      } else {
        start = newPos;
        finish = oldPos;
        loVal = oldVal;
        hiVal = newVal;
        adj = 0;
      }

      final int total = finish - start;
      for (int i = adj; i < total + adj; i++) {
        float v = ((hiVal * (float )(total - i) + loVal * (float )i) /
                   (float )total);
        val[i + start][state] = v;
      }

      notelow = start + adj;
      notehi = finish + (1 - adj);
    }
    }

    if (notelow > -1 && notehi > -1)
      notifyListeners(notelow, notehi);
  }

  /**
   * MouseMovementListener method, currently ignored
   *
   * @param evt ignored
   */
  public void mouseMoved(MouseEvent evt) {
  }

  /**
   * Repaints the entire Panel
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

  /** The left bound for updating the Panel */
  private float updateLeft = 0;

  /** The right bound for updating the Panel */
  private float updateRight = 1;

  /**
   * Repaints the modified areas of the Panel
   *
   * @param g The <TT>Graphics</TT> to update.
   */
  public void update(Graphics g) {

    synchronized (mutex_val) {

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

      final int numColors = val.length - 1;

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

      int prevEnd = leftPixel;

      int prevRed = (int )Math.floor((1 - val[left][RED]) * maxHeight);
      int prevGreen = (int )Math.floor((1 - val[left][GREEN]) * maxHeight);
      int prevBlue = (int )Math.floor((1 - val[left][BLUE]) * maxHeight);
      int prevAlpha;
      if (hasAlpha) {
        prevAlpha = (int )Math.floor((1 - val[left][ALPHA]) * maxHeight);
      } else {
        prevAlpha = 0;
      }

      int alpha = 0;
      for (int i = left + 1; i <= right; i++) {
        int lineEnd = (i * maxWidth) / maxRight;

        int red = (int )Math.floor((1 - val[i][RED]) * maxHeight);
        int green = (int )Math.floor((1 - val[i][GREEN]) * maxHeight);
        int blue = (int )Math.floor((1 - val[i][BLUE]) * maxHeight);
        if (hasAlpha) {
          alpha = (int )Math.floor((1 - val[i][ALPHA]) * maxHeight);
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

  /**
   * Return the preferred size of this map, taking into account
   * the resolution.
   *
   * @return preferred size.
   */
  public Dimension getPreferredSize() {
    return new Dimension(resolution, resolution / 2);
  }

  /**
   * Initializes the colormap to default values
   */
  private void initColormap() {
    initColormapVis5D();
  }

  /**
   * Initializes the colormap to the Vis5D sine waves.
   */
  private void initColormapVis5D() {
    synchronized (mutex_val) {

      float curve = 1.4f;
      float bias = 1.0f;
      float rfact = 0.5f * bias;

      for (int i = 0; i < resolution; i++) {

        /* compute s in [0,1] */
        float s = (float )i / (float )(resolution-1);

        float t = curve * (s - rfact);   /* t in [curve*-0.5,curve*0.5) */
        val[i][RED] = (float )(0.5 + 0.5 * Math.atan( 7.0*t ) / 1.57);
        val[i][GREEN] = (float )(0.5 + 0.5 * (2 * Math.exp(-7*t*t) - 1));
        val[i][BLUE] = (float )(0.5 + 0.5 * Math.atan( -7.0*t ) / 1.57);
        if (hasAlpha) {
          val[i][ALPHA] = 1.0f;
        }
      }
    }
  }


  /**
   * Initializes the colormap to be linear in hue
   */
  private synchronized void initColormapHSV() {
    float s = 1;
    float v = 1;

    synchronized (mutex_val) {

      for (int i = 0; i < resolution; i++) {

        float h = i * 6 / (float )(resolution - 1);

        int hFloor = (int )Math.floor(h);
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
