/*

@(#) $Id: ColorMap.java,v 1.11 2000-08-22 18:17:09 dglo Exp $

VisAD Utility Library: Widgets for use in building applications with
the VisAD interactive analysis and visualization library
Copyright (C) 2023 Nick Rasmussen
VisAD is Copyright (C) 1996 - 2023 Bill Hibbard, Curtis Rueden, Tom
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

import java.util.Vector;

import javax.swing.JPanel;

/**
 * The abstract class that all color-mapping widgets must extend.  This
 * class manages all of the listener notification for the ColorMaps.
 *
 * @author Nick Rasmussen nick@cae.wisc.edu
 * @version $Revision: 1.11 $, $Date: 2000-08-22 18:17:09 $
 * @since Visad Utility Library, 0.5
 */

public abstract class ColorMap extends JPanel {

  /** Maps a floating point value (in the range 0 to 1) onto a Color,
   * returns Color.black if the number is out of range
   *
   * If you're getting more than 1 or 2 colors,
   * use getColors() instead.
   */
  public Color getColor(float value) {
    if (value < 0 || value > 1) {
      return Color.black;
    }

    float[] rgb = getRGBTuple(value);

    if (rgb[0] < 0) rgb[0] = 0;
    if (rgb[1] < 0) rgb[1] = 0;
    if (rgb[2] < 0) rgb[2] = 0;
    if (rgb[0] > 1) rgb[0] = 1;
    if (rgb[1] > 1) rgb[1] = 1;
    if (rgb[2] > 1) rgb[2] = 1;

    return new Color(rgb[0], rgb[1], rgb[2]);
  }

  /**
   * Map a range of floating point values (in the range 0 to 1) onto
   * a range of Colors.
   */
  public Color[] getColors(float start, float end, int num) {
    float[][] rgb;
    if (getMapDimension() < 3) {
      rgb = getRGBTuples(start, end, num);
    } else {
      rgb = getTuples(start, end, num);
    }

    if (rgb == null) {
      return null;
    }

    Color[] colors = new Color[rgb.length];

    for (int i = rgb.length - 1; i >= 0; i--) {
      if (rgb[i][0] < 0.0f)
        rgb[i][0] = 0.0f;
      else if (rgb[i][0] > 1.0f)
        rgb[i][0] = 1.0f;
      if (rgb[i][1] < 0.0f)
        rgb[i][1] = 0.0f;
      else if (rgb[i][1] > 1.0f)
        rgb[i][1] = 1.0f;
      if (rgb[i][2] < 0.0f)
        rgb[i][2] = 0.0f;
      else if (rgb[i][2] > 1.0f)
        rgb[i][2] = 1.0f;

      colors[i] = new Color(rgb[i][0], rgb[i][1], rgb[i][2]);
    }

    return colors;
  }

  /**
   * Maps a floating point value (in the range 0 to 1) onto an RGB
   * triplet of floating point numbers in the range 0 to 1)
   *
   * If you're getting more than 1 or 2 tuples,
   * use getRGBTuples() instead.
   */
  public abstract float[] getRGBTuple(float value);

  /**
   * Maps the specified floating point values (in the range 0 to 1)
   * onto a group of RGB triplets of floating point numbers in the
   * range 0 to 1.  The endpoints are included in <TT>count</TT>
   * so if <TT>count=1</TT>, only the RGBTuple for <TT>start</TT>
   * is returned; if <TT>count=3</TT>, then RGBTuples are returned
   * for <TT>start</TT>, the midpoint between <TT>start</TT> and
   * <TT>end, and for <TT>end</TT>.
   *
   * @param start the first value to translate
   * @param end the last value to translate
   * @param count the number of values (including the two endpoints)
   *              to be returned.
   */
  public abstract float[][] getRGBTuples(float start, float end, int count);

  /**
   * Maps a floating point value (in the range 0 to 1) into a tuple
   * with dimension of the map
   *
   * If you're getting more than 1 or 2 tuples,
   * use getTuples() instead.
   */
  public abstract float[] getTuple(float value);

  /**
   * Maps the specified floating point values (in the range 0 to 1)
   * onto a group of floating point tuples with the dimension of
   * the map.  The endpoints are included in <TT>count</TT>
   * so if <TT>count=1</TT>, only the tuple for <TT>start</TT>
   * is returned; if <TT>count=3</TT>, then tuples are returned
   * for <TT>start</TT>, the midpoint between <TT>start</TT> and
   * <TT>end, and for <TT>end</TT>.
   *
   * @param start The first value to translate
   * @param end The last value to translate
   * @param count The number of values (including the two endpoints)
   *              to be returned.
   */
  public abstract float[][] getTuples(float start, float end, int count);

  /** Returns the current map resolution */
  public abstract int getMapResolution();

  /** Returns the dimension of the map */
  public abstract int getMapDimension();

  /** Returns a copy of the ColorMap */
  public abstract float[][] getColorMap();

  /** The vector containing the ColorChangeListeners */
  private Vector listeners = new Vector();

  /** Add a ColorChangeListener to the listeners list */
  public void addColorChangeListener(ColorChangeListener c) {
    synchronized (listeners) {
      if (!listeners.contains(c)) {
        listeners.addElement(c);
      }
    }
  }

  /** Remove a ColorChangeListener from the listeners list */
  public void removeColorChangeListener(ColorChangeListener c) {
    synchronized (listeners) {
      if (listeners.contains(c)) {
        listeners.removeElement(c);
      }
    }
  }

  /** Notify the ColorChangeListerers that the color widget has changed */
  protected void notifyListeners(ColorChangeEvent e) {
    Vector cl = null;
    synchronized (listeners) {
      cl = (Vector) listeners.clone();
    }
    for (int i = 0; i < cl.size(); i++) {
      ColorChangeListener c = (ColorChangeListener) cl.elementAt(i);
      c.colorChanged(e);
    }
  }
}
