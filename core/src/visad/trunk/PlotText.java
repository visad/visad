//
// PlotText.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2000 Bill Hibbard, Curtis Rueden, Tom
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

package visad;

import java.awt.*;
import java.awt.font.*;
import java.awt.geom.*;
import java.util.Vector;

import visad.browser.Convert;

/**
   PlotText calculates an array of points to be plotted to
   the screen as vector pairs, given a String and location,
   orientation and size in space.<P>

   The font is a simple one, and includes characters from
   the ASCII collating sequence from 0x20 thru 0x7E.

   Most of this was taken from the original visad.PlotText.
*/
public class PlotText extends Object {

  static final double XMIN = -1.0;
  static final double YMIN = -1.0;
  static final double ZMIN = -1.0;
  static final double WIDTH = .8;

  /* base line and up vectors */
  static double[] bx = { 0.07, 0.0, 0.0 }, ux = { 0.0, 0.07, 0.07 };
  static double[] by = { 0.0, 0.07, 0.0 }, uy = { -0.07, 0.0, -0.07 };
  static double[] bz = { 0.0, 0.0, -0.07 }, uz = { 0.07, 0.07, 0.0 };

  /* vector characters  -- (100 + x) value indicates beginning of segment */
  /* characters are ordered by ASCII collating sequence, starting at 0x20 */
  static float[][] charCodes = {

	{100f,0f}, // sp
	{101f,8f,1f,3f,3f,3f,3f,8f,1f,8f,101f,1f,1f,0f,3f,0f,3f,1f,1f,1f}, // !
	{101f,8f,0f,5f,104f,8f,3f,5f}, // "
	{101.5f,8f,1.5f,0f,103.5f,8f,3.5f,0f,100f,5f,5f,5f,100f,3f,5f,3f}, // #
	{101.5f,8f,1.5f,0f,102.5f,8f,2.5f,0f,104f,5.5f,3f,7f,1f,7f,0f,5.5f,0f,4.5f,4f,3.5f,4f,2.5f,3f,1f,1f,1f,0f,2.5f}, // $
	{100f,8f,0f,7f,1f,7f,1f,8f,0f,8f,105f,8f,0f,0f,104f,1f,4f,0f,5f,0f,5f,1f,4f,1f}, // %
	{105f,0f,0f,5f,0f,7f,1f,8f,3f,8f,4f,7f,4f,5f,0f,3f,0f,1f,1f,0f,3f,0f,5f,3f,5f,4f}, // &
	{101f,8f,0f,5f}, // '
	{104f,8f,2f,6f,2f,2f,4f,0f}, // (
	{101f,8f,3f,6f,3f,2f,1f,0f}, // )
	{100f,7f,5f,1f,102.5f,7f,2.5f,1f,100f,1f,5f,7f,105f,4f,0f,4f}, // *
	{102.5f,7f,2.5f,1f,100f,4f,5f,4f}, // +
	{103f,0f,2f,0f,2f,1f,3f,1f,3f,0f,2.1f,-2f}, // ,
	{100f,4f,5f,4f}, // -
	{102f,0f,3f,0f,3f,1f,2f,1f,2f,0f}, // .
	{100f,0f,5f,8f}, // /
	{102f,8f,0f,6f,0f,2f,2f,0f,3f,0f,5f,2f,5f,6f,3f,8f,2f,8f}, // 0
	{101f,7f,2.5f,8f,2.5f,0f,1f,0f,4f,0f}, // 1
	{100f,7f,1f,8f,4f,8f,5f,7f,5f,5f,0f,0f,5f,0f}, // 2
	{100f,7f,1f,8f,4f,8f,5f,7f,5f,5f,4f,4f,3f,4f,4f,4f,5f,3f,5f,1f,4f,0f,1f,0f,0f,1f}, // 3
	{103f,8f,0f,4f,5f,4f,5f,8f,5f,0f}, // 4
	{100f,1f,1f,0f,4f,0f,5f,1f,5f,4f,4f,5f,0f,5f,0f,8f,5f,8f}, // 5
	{105f,7f,4f,8f,1f,8f,0f,7f,0f,1f,1f,0f,4f,0f,5f,1f,5f,3f,4f,4f,0f,4f}, // 6
	{100f,8f,5f,8f,3f,0f}, // 7
	{101f,8f,0f,7f,0f,5f,1f,4f,4f,4f,5f,5f,5f,7f,4f,8f,1f,8f,101f,4f,0f,3f,0f,1f,1f,0f,4f,0f,5f,1f,5f,3f,4f,4f}, // 8
	{101f,0f,1f,0f,4f,0f,5f,1f,5f,7f,4f,8f,1f,8f,0f,7f,0f,5f,1f,4f,5f,4f}, // 9
	{102f,7f,2f,5f,3f,5f,3f,7f,2f,7f,102f,3f,2f,1f,3f,1f,3f,3f,2f,3f}, // :
	{100f,7f,0f,5f,1f,5f,1f,7f,0f,7f,100f,0f,1f,1f,1f,3f,0f,3f,0f,1f,1f,1f}, // ;
	{105f,7f,0f,4f,5f,1f}, // <
	{100f,5f,5f,5f,100f,3f,5f,3f}, // =
	{100f,7f,5f,4f,0f,1f}, // >
	{100f,7f,1f,8f,4f,8f,5f,7f,5f,5f,4f,4f,2.5f,4f,2.5f,2f,102.5f,1f,2.5f,0f}, // ?
	{104f,0f,1f,0f,0f,1f,0f,7f,1f,8f,4f,8f,5f,7f,5f,3f,4f,1.5f,3f,2f,1.5f,4f,1.5f,5f,2.5f,6f,4f,5f,3f,2f},   // @
	{100f,0f,0f,7f,1f,8f,4f,8f,5f,7f,5f,0f,5f,4f,0f,4f}, // A
	{100f,8f,0f,0f,4f,0f,5f,1f,5f,3f,4f,4f,5f,5f,5f,7f,4f,8f,0f,8f,0f,4f,4f,4f}, // B
	{105f,7f,4f,8f,1f,8f,0f,7f,0f,1f,1f,0f,4f,0f,5f,1f}, // C
	{100f,8f,0f,0f,4f,0f,5f,1f,5f,7f,4f,8f,0f,8f}, // D
	{105f,8f,0f,8f,0f,4f,3f,4f,0f,4f,0f,0f,5f,0f}, // E
	{105f,8f,0f,8f,0f,4f,3f,4f,0f,4f,0f,0f}, // F
	{105f,7f,4f,8f,1f,8f,0f,7f,0f,1f,1f,0f,4f,0f,5f,1f,5f,4f,3f,4f}, // G
	{100f,8f,0f,0f,0f,4f,5f,4f,5f,8f,5f,0f}, // H
	{100f,8f,5f,8f,2.5f,8f,2.5f,0f,0f,0f,5f,0f}, // I
	{105f,8f,5f,1f,4f,0f,1f,0f,0f,1f,0f,3f}, // J
	{100f,8f,0f,0f,0f,4f,5f,8f,0f,4f,5f,0f}, // K
	{100f,8f,0f,0f,5f,0f}, // L
	{100f,0f,0f,8f,2.5f,4f,5f,8f,5f,0f}, // M
	{100f,0f,0f,8f,5f,0f,5f,8f}, // N
	{101f,8f,0f,7f,0f,1f,1f,0f,4f,0f,5f,1f,5f,7f,4f,8f,1f,8f}, // O
	{100f,0f,0f,8f,4f,8f,5f,7f,5f,5f,4f,4f,0f,4f}, // P
	{101f,8f,0f,7f,0f,1f,1f,0f,4f,0f,5f,1f,5f,7f,4f,8f,1f,8f,103f,3f,5f,0f}, // Q
	{100f,0f,0f,8f,4f,8f,5f,7f,5f,5f,4f,4f,0f,4f,3f,4f,5f,0f}, // R
	{105f,7f,4f,8f,1f,8f,0f,7f,0f,5f,1f,4f,4f,4f,5f,3f,5f,1f,4f,0f,1f,0f,0f,1f}, // S
	{100f,8f,5f,8f,2.5f,8f,2.5f,0f}, // T
	{100f,8f,0f,1f,1f,0f,4f,0f,5f,1f,5f,8f}, // U
	{100f,8f,2.5f,0f,5f,8f}, // V
	{100f,8f,0f,0f,2.5f,4f,5f,0f,5f,8f}, // W
	{100f,8f,5f,0f,100f,0f,5f,8f}, // X
	{100f,8f,2.5f,4f,5f,8f,2.5f,4f,2.5f,0f}, // Y
	{100f,8f,5f,8f,0f,0f,5f,0f}, // Z
	{104f,8f,2f,8f,2f,0f,4f,0f}, // [
	{100f,8f,5f,0f}, // \
	{101f,8f,3f,8f,3f,0f,1f,0f}, // ]
	{102f,6f,3f,8f,4f,6f}, // ^
	{100f,-2f,5f,-2f}, // _
	{102f,8f,4f,6f}, // `
	{104f,5f,4f,1f,3f,0f,1f,0f,0f,1f,0f,4f,1f,5f,3f,5f,4f,4f,4f,1f,5f,0f}, // a
	{100f,8f,0f,0f,0f,1f,1f,0f,4f,0f,5f,1f,5f,4f,4f,5f,3f,5f,0f,3f}, // b
	{105f,0f,1f,0f,0f,1f,0f,4f,1f,5f,4f,5f,5f,4f}, // c
	{105f,3f,3f,5f,1f,5f,0f,4f,0f,1f,1f,0f,4f,0f,5f,1f,5f,0f,5f,8f}, // d
	{105f,0f,1f,0f,0f,1f,0f,4f,1f,5f,4f,5f,5f,4f,4f,3f,0f,3f}, // e
	{103f,0f,3f,7f,4f,8f,5f,8f,5f,7f,101f,4f,4f,4f}, // f
	{105f,5f,5f,-3f,4f,-4f,1f,-4f,105f,1f,4f,0f,1f,0f,0f,1f,0f,4f,1f,5f,3f,5f,5f,3f}, // g
	{100f,8f,0f,0f,0f,3f,3f,5f,4f,5f,5f,4f,5f,0f}, // h
	{103f,4f,3f,0f,4f,0f,1f,0f,103f,6.5f,3f,5.5f}, // i
	{104f,4f,4f,-3f,3f,-4f,1f,-4f,0f,-3f,0f,-1f,1f,0f,104f,6.5f,4f,5.5f}, // j
	{101f,8f,1f,0f,101f,3f,5f,5f,101f,3f,5f,0f}, // k
	{102f,8f,3f,8f,3f,0f}, // l
	{100f,0f,0f,5f,0f,4f,1f,5f,4f,5f,5f,4f,5f,0f,102.5f,5f,2.5f,2.0f}, // m
	{100f,0f,0f,5f,0f,4f,1f,5f,4f,5f,5f,3f,5f,0f}, // n
	{101f,0f,0f,1f,0f,4f,1f,5f,4f,5f,5f,4f,5f,1f,4f,0f,1f,0f}, // o
	{100f,-4f,0f,1f,1f,0f,4f,0f,5f,1f,5f,4f,4f,5f,3f,5f,0f,3f,0f,1f,0f,5f}, // p
	{105f,-4f,5f,1f,4f,0f,1f,0f,0f,1f,0f,4f,1f,5f,3f,5f,5f,3f,5f,1f,5f,5f}, // q
	{100f,5f,0f,0f,0f,3f,3f,5f,4f,5f,5f,4f}, // r
	{105f,4f,3f,5f,2f,5f,0f,4f,0f,3f,5f,2f,5f,1f,3f,0f,2f,0f,0f,1f}, // s
	// {105f,4f,4f,5f,3f,5f,1f,3.5f,3f,3f,4f,3f,5f,1f,4f,0f,3f,0f,1f,1f}, // s
	{102.5f,8f,2.5f,0f,100.5f,5f,4.5f,5f}, // t
	{100f,5f,0f,1f,1f,0f,3f,0f,5f,3f,5f,5f,5f,0f}, // u
	{100f,5f,0f,3f,2.5f,0f,5f,3f,5f,5f}, // v
	{100f,5f,0f,0f,2.5f,3f,5f,0f,5f,5f}, // w
	{100f,5f,5f,0f,105f,5f,0f,0f}, // x
	{100f,5f,0f,3f,3f,0f,5f,3f,5f,5f,5f,-3f,3f,-4f}, // y
	{100f,5f,5f,5f,0f,0f,5f,0f}, // z
	{104f,8f,3f,8f,2f,4.5f,1f,4.5f,2f,4.5f,3f,0f,4f,0f}, // {
	{103.5f,8f,3.5f,0f}, // |
	{102f,8f,3f,8f,4f,4.5f,5f,4.5f,4f,4.5f,3f,0f,2f,0f}, // }
	{100f,4f,1f,5f,3f,4f,4f,5f}, // ~
	{100f,0f} // RO
	};

  /**
   * Convert a string of characters (ASCII collating sequence) into a
   *  series of vectors for drawing.
   *
   * @param  axis  [=0 (x), =1 (y), or =2 (z)
   * @param  pos  position along axis to put label in [-1,1]
   * @param  str  the text string to "print"
   * @param  line  line number for multi-line text (0 = first line)
   * @param  c  color (not used yet)
   *
   * @return VisADLineArray of all the vectors needed to draw the
   * characters in this string
  */
  public static VisADLineArray render_label(int axis, double pos, String str,
                                            int line, long c) {
    double XMIN = -1.0;
    double YMIN = -1.0;
    double ZMIN = -1.0;

    /* base line and up vectors */
    double[] bx = { 0.07, 0.0, 0.0 }, ux = { 0.0, 0.07, 0.07 };
    double[] by = { 0.0, 0.07, 0.0 }, uy = { -0.07, 0.0, -0.07 };
    double[] bz = { 0.0, 0.0, -0.07 }, uz = { 0.07, 0.07, 0.0 };

    double[] base = null;
    double[] up = null;
    double[] start = new double[3];

    if (axis==0) { // x
      base = bx;
      up = ux;
      start[0] = pos;
      start[1] = YMIN * (1.1 + 0.07*line);
      start[2] = ZMIN * (1.1 + 0.07*line);
    }
    else if (axis==1) { // y
      base = by;
      up = uy;
      start[0] = XMIN * (1.1 + 0.07*line);
      start[1] = pos;
      start[2] = ZMIN * (1.1 + 0.07*line);
    }
    else if (axis==2) { // z
      base = bz;
      up = uz;
      start[0] = XMIN * (1.1 + 0.07*line);
      start[1] = YMIN * (1.1 + 0.07*line);
      start[2] = pos;
    }
    // abcd 5 February 2001
    return render_label(str, start, base, up, TextControl.Justification.CENTER);
    // return render_label(str, start, base, up, true);
  }

  /**
   * Convert a string of characters (ASCII collating sequence) into a
   *  series of vectors for drawing.
   *
   * @param str  String to use
   * @param  start point (x,y,z)
   * @param  base  (x,y,z) of baseline vector
   * @param  up  (x,y,z) of "up" direction vector
   * @param  center is <CODE>true</CODE> if string is to be centered
   *
   * @return VisADLineArray of all the vectors needed to draw the
   * characters in this string
  */
  public static VisADLineArray render_label(String str, double[] start,
         double[] base, double[] up, boolean center) {
    return render_label(str, start, base, up,
                        (center ? TextControl.Justification.CENTER :
                                  TextControl.Justification.LEFT));
  }

  // abcd 5 February 2001
  // was
  // * @param  center is <CODE>true</CODE> if string is to be centered
  /**
   * Convert a string of characters (ASCII collating sequence) into a
   *  series of vectors for drawing.
   *
   * @param str  String to use
   * @param  start point (x,y,z)
   * @param  base  (x,y,z) of baseline vector
   * @param  up  (x,y,z) of "up" direction vector
   * @param  justification is one of:<ul>
   * <li> TextControl.Justification.LEFT - Left justified text (ie: normal text)
   * <li> TextControl.Justification.CENTER - Centered text
   * <li> TextControl.Justification.RIGHT - Right justified text
   * </ul>
   *
   * @return VisADLineArray of all the vectors needed to draw the
   * characters in this string
   */
  public static VisADLineArray render_label(String str, double[] start,
         double[] base, double[] up, TextControl.Justification justification) {
    double[] temp;
    double cx, cy, cz;
    double startx = 0.0;
    double starty = 0.0;
    double startz = 0.0;
    double sw;
    int i, j, k, v2, len;

    cx = start[0];
    cy = start[1];
    cz = start[2];
    len = str.length();
    // allow 20 2-point 3-component strokes per character
    float[] plot = new float[120 * len];

    // abcd 5 February 2001
    //if (center) {
    //  /* calculate string width for center justify - fixed width font*/
    //  sw = WIDTH * (float) len;
    //  cx -= sw * base[0] / 2.0;
    //  cy -= sw * base[1] / 2.0;
    //  cz -= sw * base[2] / 2.0;
    //}
    if (justification == TextControl.Justification.CENTER) {
      sw = WIDTH * (float) len;
      cx -= sw * base[0] / 2.0;
      cy -= sw * base[1] / 2.0;
      cz -= sw * base[2] / 2.0;
    } else if (justification == TextControl.Justification.RIGHT) {
      sw = WIDTH * (float) len;
      cx -= sw * base[0];
      cy -= sw * base[1];
      cz -= sw * base[2];
    }

    int plot_index = 0;

    /* draw left justified text */

    for (i=0; i<len; i++) {
      k = str.charAt(i) - 32;
      if (k < 0 || k > 127) continue; // invalid - just skip

      int verts = charCodes[k].length/2;

      /* make the vertex array for this character */
      /* points with x>9 are 'start new segment' flag */

      int temp_index = 0;
      for (j=0; j<verts; j++) {

        if (verts == 1) break; // handle space character

        boolean dup_point = true;
        if (j == (verts - 1) ) dup_point = false; // don't dupe last point

        double x, y;
        x = (double) charCodes[k][temp_index]*.1;
        if (x > 9.0) {
          if (j != 0) plot_index -= 3; // reset pointer to remove last point
          x = x - 10.0;
          dup_point = false;
        }

        temp_index++;
        y = (double) charCodes[k][temp_index]*.1;
        temp_index++;

        plot[plot_index] = (float) (cx + x * base[0] + y * up[0]);
        plot[plot_index + 1] = (float) (cy + x * base[1] + y * up[1]);
        plot[plot_index + 2] = (float) (cz + x * base[2] + y * up[2]);

        if (dup_point) { // plot points are in pairs -- set up for next pair
          plot[plot_index + 3] = plot[plot_index];
          plot[plot_index + 4] = plot[plot_index + 1];
          plot[plot_index + 5] = plot[plot_index + 2];
          plot_index += 3;
        }
        plot_index += 3;
      }
      /* calculate position for next char */
      cx += WIDTH * base[0];
      cy += WIDTH * base[1];
      cz += WIDTH * base[2];

    } // end for (i=0; i<len; i++)

    if (plot_index <= 0) return null;

    VisADLineArray array = new VisADLineArray();
    float[] coordinates = new float[plot_index];
    System.arraycopy(plot, 0, coordinates, 0, plot_index);
    array.coordinates = coordinates;
    array.vertexCount = plot_index / 3;

/* WLH 20 Feb 98
    array.vertexFormat = COORDINATES;
*/

    return array;
  }

  /** make a short string for value for use in slider label */
  public static String shortString(double val)
  {
    return Convert.shortString(val);
  }

  /**
   * Convert a string of characters (ASCII collating sequence) into a
   *  series of triangles for drawing.
   *
   * @param str  String to use
   * @param  font  non-null font
   * ?param  start
   * ?param  base
   * ?param  up
   * @param  center is <CODE>true</CODE> if string is to be centered
   *
   * @return VisADTriangleArray of all the triangles needed to draw the
   * characters in this string
  */
  public static VisADTriangleArray render_font(String str, Font font,
            double[] start, double[] base, double[] up, boolean center) {
    return render_font(str, font, start, base, up,
                       (center ? TextControl.Justification.CENTER :
                                 TextControl.Justification.LEFT));
  }

// abcd 5 February 2001
  /**
   * Convert a string of characters (ASCII collating sequence) into a
   *  series of triangles for drawing.
   *
   * @param str  String to use
   * @param  font  non-null font
   * ?param  start
   * ?param  base
   * ?param  up
   * @param  justification is one of:<ul>
   * <li> TextControl.Justification.LEFT - Left justified text (ie: normal text)
   * <li> TextControl.Justification.CENTER - Centered text
   * <li> TextControl.Justification.RIGHT - Right justified text
   * </ul>
   *
   * @return VisADTriangleArray of all the triangles needed to draw the
   * characters in this string
   */
  public static VisADTriangleArray render_font(String str, Font font,
            double[] start, double[] base, double[] up,
            TextControl.Justification justification) {
    VisADTriangleArray array = null;

// System.out.println("x, y, z = " + x + " " + y + " " + z);
// System.out.println("center = " + center);

    float fsize = font.getSize();
    float fsize_inv = 1.0f / fsize;

    // ??
    // Graphics2D g2 = null;
    // FontRenderContext frc = g2.getFontRenderContext();
    AffineTransform at = null;
    boolean isAntiAliased = false;
    boolean usesFractionalMetrics = false;
    FontRenderContext frc =
      new FontRenderContext(at, isAntiAliased, usesFractionalMetrics);

    double flatness = 0.05; // ??

    Vector big_vector = new Vector();
    int big_len = 1000;
    float[][] big_samples = new float[2][big_len];
    float[] seg = new float[6];

    int str_len = str.length();
    float x_offset = 0.0f;
    for (int str_index=0; str_index<str_len; str_index++) {
      char[] chars = {str.charAt(str_index)};
// System.out.println(str_index + " " + chars[0] + " " + x_offset);
      GlyphVector gv = font.createGlyphVector(frc, chars);
      int ng = gv.getNumGlyphs();
      if (ng == 0) continue;
      int path_count = 0;
      Vector samples_vector = new Vector();

      // abcd - 1 February 2001
      // Get x increment from the fonts 'advance' property
      float x_plus = (float) (fsize_inv * gv.getGlyphMetrics(0).getAdvance());

// System.out.println(str_index + " " + chars[0] + " " + x_plus + " " + fsize_inv);
      for (int ig=0; ig<ng; ig++) {
        Shape sh = gv.getGlyphOutline(ig);
        // pi only has SEG_MOVETO, SEG_LINETO, and SEG_CLOSE point types
        PathIterator pi = sh.getPathIterator(at, flatness);
        int k = 0;
        while (!pi.isDone()) {
          int segType = pi.currentSegment(seg);
          switch(segType) {
            case PathIterator.SEG_MOVETO:
              if (k > 0) {
// System.out.println("SEG_MOVETO  k = " + k + "  ig = " + ig);
                float[][] samples = new float[2][k];
                System.arraycopy(big_samples[0], 0, samples[0], 0, k);
                System.arraycopy(big_samples[1], 0, samples[1], 0, k);
                samples_vector.addElement(samples);
                k = 0;
                path_count++;
              }
              // NOTE falls through to SEG_LINETO to add first point
            case PathIterator.SEG_LINETO:
              big_samples[0][k] = x_offset + fsize_inv * seg[0];
              big_samples[1][k] = - fsize_inv * seg[1];
              k++;
              if (k >= big_len) {
                float[][] bs = new float[2][2 * big_len];
                System.arraycopy(big_samples[0], 0, bs[0], 0, big_len);
                System.arraycopy(big_samples[1], 0, bs[1], 0, big_len);
                big_samples = bs;
                big_len = 2 * big_len;
              }
              break;
            case PathIterator.SEG_CLOSE:
              if (k > 0) {
// System.out.println("SEG_CLOSE  k = " + k + "  ig = " + ig);
                float[][] samples = new float[2][k];
                System.arraycopy(big_samples[0], 0, samples[0], 0, k);
                System.arraycopy(big_samples[1], 0, samples[1], 0, k);
                samples_vector.addElement(samples);
                k = 0;
                path_count++;
              }
              break;
          }
          pi.next();
        } // end while (!pi.isDone())
        if (k > 0) {
// System.out.println("  end  k = " + k + "  ig = " + ig);
          float[][] samples = new float[2][k];
          System.arraycopy(big_samples[0], 0, samples[0], 0, k);
          System.arraycopy(big_samples[1], 0, samples[1], 0, k);
          samples_vector.addElement(samples);
          k = 0;
          path_count++;
        }
      } // end for (int ig=0; ig<ng; ig++)
      if (path_count == 1) {
// System.out.println("  char  " + chars[0]);
        big_vector.addElement(samples_vector.elementAt(0));
      }
      else { // (path_count > 1)
        // System.out.println("path_count = " + path_count +
        //                    " for char = " + chars[0]);
        float[][][] ss = new float[path_count][][];
        for (int i=0; i<path_count; i++) {
          ss[i] = (float[][]) samples_vector.elementAt(i);
        }
        try {
// System.out.println("  call link for  " + chars[0]);
          big_vector.addElement(DelaunayCustom.link(ss));
        }
        catch (VisADException ex) {
          System.out.println(ex);
        }
      }
      samples_vector.removeAllElements();
      x_offset += x_plus;
    } // end for (int str_index=0; str_index<str_len; str_index++)

    /*
     * abcd 5 February 2001
     * Figure out how far to the 'left' our text should start
     */
    // x_offset = center ? -0.5f * x_offset : 0.0f;
    if (justification == TextControl.Justification.LEFT) {
      x_offset = 0.0f;
    } else if (justification == TextControl.Justification.CENTER) {
      x_offset = -0.5f * x_offset;
    } else { // justification == TextControl.Justification.RIGHT) {
      x_offset = -1.0f * x_offset;
    }

    int n = big_vector.size();
    VisADTriangleArray[] arrays = new VisADTriangleArray[n];
    for (int i=0; i<n; i++) {
      float[][] samples = (float[][]) big_vector.elementAt(i);
// System.out.println("samples " + i + " " + samples[0][0] + " " + samples[1][0] +
//                    " " + samples[0][1] + " " + samples[1][1]);
      int[][] tris = null;
      try {
        tris = DelaunayCustom.fillCheck(samples, false);
      }
      catch (VisADException ex) {
      }
      if (tris == null || tris.length == 0) continue;
      int m = tris.length;
      float[] coordinates = new float[9 * m];
      for (int j=0; j<m; j++) {
        int j9 = 9 * j;
        for (int tj=0; tj<3; tj++) {
          int j3 = j9 + 3 * tj;
          coordinates[j3 + 0] = (float)
            (start[0] + base[0] * (samples[0][tris[j][tj]] + x_offset) +
                        up[0] * samples[1][tris[j][tj]]);
          coordinates[j3 + 1] = (float)
            (start[1] + base[1] * (samples[0][tris[j][tj]] + x_offset) +
                        up[1] * samples[1][tris[j][tj]]);
          coordinates[j3 + 2] = (float)
            (start[2] + base[2] * (samples[0][tris[j][tj]] + x_offset) +
                        up[2] * samples[1][tris[j][tj]]);
        }
      }
      float[] normals = new float[9 * m];
      for (int j=0; j<3*m; j++) {
        int j3 = 3 * j;
        normals[j3 + 0] = 0.0f;
        normals[j3 + 1] = 0.0f;
        normals[j3 + 2] = 1.0f;
      }
      arrays[i] = new VisADTriangleArray();
      arrays[i].vertexCount = 3 * m;
      arrays[i].coordinates = coordinates;
      arrays[i].normals = normals;
// System.out.println("array[" + i + "] has " + m + " tris");
    } // end for (int i=0; i<n; i++)

    array = new VisADTriangleArray();
    try {
      VisADGeometryArray.merge(arrays, array);
    }
    catch (VisADException ex) {
      array = new VisADTriangleArray();
    }
    if (array.coordinates == null) return null;
    return array;
  }

}
