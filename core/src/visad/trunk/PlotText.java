
//
// PlotText.java
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

package visad;

/**
   PlotText calculates an array of points to be plotted to
   the screen as vector pairs, given a String and location,
   orientation and size in space.<P>
*/
public class PlotText extends Object {



  static final double XMIN = -1.0;
  static final double YMIN = -1.0;
  static final double ZMIN = -1.0;

  /* base line and up vectors */
  static double[] bx = { 0.07, 0.0, 0.0 }, ux = { 0.0, 0.07, 0.07 };
  static double[] by = { 0.0, 0.07, 0.0 }, uy = { -0.07, 0.0, -0.07 };
  static double[] bz = { 0.0, 0.0, -0.07 }, uz = { 0.07, 0.07, 0.0 };

  /* vector characters */
  static double zero[] = { 0,0, 0,.8, .4,.8, .4,0, 0,0 },
    one[] = { 0,0, 0,.8 },
    two[] = { .4,0, 0,0, 0,.4, .4,.4, .4,.8, 0,.8 },
    three[] = { 0,0, .4,0, .4,.4, 0,.4, .4,.4, .4,.8, 0,.8 },
    four[] = { 0,.8, 0,.4, .4,.4, .4,.8, .4,0 },
    five[] = { 0,0, .4,0, .4,.4, 0,.4, 0,.8, .4,.8 },
    six[] = { .4,.8, 0,.8, 0,0, .4,0, .4,.4, 0,.4 },
    seven[] = { 0,.7, 0,.8, .4,.8, .4,0 },
    eight[] = { 0,0, 0,.8, .4,.8, .4,0, 0,0, 0,.4, .4,.4 },
    nine[] = { .4,.4, 0,.4, 0,.8, .4,.8, .4,0 },
    dash[] = { 0,.4, .4,.4 },
    dot[] = { 0,0, 0,.1, .1,.1, .1,0, 0,0 },
 
    equal[] = { 0,.3, .4,.3, 0,.5, .4,.5 },
    curl[] = { .3,0, .2,0, .1,.1, .2,.4, .1,.4, .2,.4, .1,.7,
               .2,.8, .3,.8 },
    uncurl[] = { .1,0, .2,0, .3,.1, .2,.4, .3,.4, .2,.4, .3,.7,
                 .2,.8, .1,.8 },
    space[] = { 0,0 },
    score[] = { 0,0, .4,0 },
    aa[] = { 0,0, 0,.7, .1,.8, .3,.8, .4,.7, .4,.4, 0,.4,
             .4,.4, .4,0 },
    bb[] = { 0,0, 0,.8, .3,.8, .4,.7, .4,.5, .3,.4, 0,.4,
             .3,.4, .4,.3, .4,.1, .3,0, 0,0 },
    cc[] = { .4,.1, .3,0, .1,0, 0,.1, 0,.7, .1,.8, .3,.8,
             .4,.7 },
    dd[] = { 0,0, 0,.8, .3,.8, .4,.7, .4,.1, .3,0, 0,0 },
    ee[] = { .4,0, 0,0, 0,.4, .4,.4, 0,.4, 0,.8, .4,.8 },
    ff[] = { 0,0, 0,.4, .4,.4, 0,.4, 0,.8, .4,.8 },
    gg[] = { .3,.4, .4,.4, .4,.1, .3,0, .1,0, 0,.1, 0,.7,
             .1,.8, .3,.8, .4,.7 },
    hh[] = { 0,0, 0,.8, 0,.4, .4,.4, .4,.8, .4,0 },
    ii[] = { 0,0, .4,0, .2,0, .2,.8, 0,.8, .4,.8 },
    jj[] = { .3,.8, .4,.8, .4,.1, .3,0, .1,0, 0,.1, 0,.2 },
    kk[] = { 0,0, 0,.8, 0,.4, .4,.8, 0,.4, .4,0 },
    ll[] = { 0,.8, 0,0, .4,0 },
    mm[] = { 0,0, 0,.8, .2,.4, .4,.8, .4,0 },
    nn[] = { 0,0, 0,.8, .4,0, .4,.8 },
    oo[] = { .1,0, 0,.1, 0,.7, .1,.8, .3,.8, .4,.7, .4,.1,
             .3,0, .1,0 },
    pp[] = { 0,0, 0,.8, .3,.8, .4,.7, .4,.5, .3,.4, 0,.4 },
    qq[] = { .1,0, 0,.1, 0,.7, .1,.8, .3,.8, .4,.7, .4,.1,
             .35,.05, .3,.1, .4,0, .35,.05, .3,0, .1,0 },
    rr[] = { 0,0, 0,.8, .3,.8, .4,.7, .4,.5, .3,.4, 0,.4,
             .2,.4, .4,0 },
    ss[] = { 0,.1, .1,0, .3,0, .4,.1, .4,.3, .3,.4, .1,.4,
             0,.5, 0,.7, .1,.8, .3,.8, .4,.7 },
    tt[] = { 0,.8, .4,.8, .2,.8, .2,0 },
    uu[] = { 0,.8, 0,.1, .1,0, .3,0, .4,.1, .4,.8 },
    vv[] = { 0,.8, .2,0, .4,.8 },
    ww[] = { 0,.8, 0,0, .2,.4, .4,0, .4,.8 },
    xx[] = { 0,0, .2,.4, 0,.8, .4,0, .2,.4, .4,.8 },
    yy[] = { 0,.8, .2,.4, .2,0, .2,.4, .4,.8 },
    zz[] = { .4,0, 0,0, .4,.8, 0,.8 };

  static double[][] index =
    { zero, one, two, three, four, five, six, seven, eight, nine,
    dash, dot, equal, curl, uncurl, space, score,
    aa, bb, cc, dd, ee, ff, gg, hh, ii, jj,
    kk, ll, mm, nn, oo, pp, qq, rr, ss, tt,
    uu, vv, ww, xx, yy, zz };

  /* width of vector chars */
  static double[] width = { 0.6, 0.2, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6,
    0.6, 0.6, 0.6, 0.3, 0.6,
    0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6,
    0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6,
    0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6 };
  /* vertices in each char */
  static int[] verts = { 5, 2, 6, 7, 5, 6, 6, 4, 7, 5, 2, 5,
    4, 9, 9, 0, 2, 9, 12, 8, 7, 7, 6, 10, 6, 6, 7, 6, 3, 5, 4,
    9, 7, 13, 9, 12, 4, 6, 3, 5, 6, 5, 4 };
  
  /**
     render_label
     Draw a 3-D text label.
     Input:  axis - 0 (x), 1 (y), or 2 (z)
             pos - position along label to put label in [-1,1]
             str - the text string to print.
             line - line number (0 = first line)
             c - color
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
    return render_label(str, start, base, up, true);
  }

  /** plot str in 3-D, at start, x along base and y along up,
      center str at start if center is true */
  public static VisADLineArray render_label(String str, double[] start,
         double[] base, double[] up, boolean center) {
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
    // allow 15 2-point 3-component strokes per character
    float[] plot = new float[90 * len];
  
    if (center) {
      /* calculate string width for center justify */
      sw = 0.0;
      for (i=0; i<len; i++) {
        if (str.charAt(i) == '-')
          k = 10;
        else if (str.charAt(i) == '.')
          k = 11;
        else if (str.charAt(i) == '=')
          k = 12;
        else if (str.charAt(i) == '{')
          k = 13;
        else if (str.charAt(i) == '}')
          k = 14;
        else if (str.charAt(i) == ' ')
          k = 15;
        else if (str.charAt(i) == '_')
          k = 16;
        else if (str.charAt(i) >= '0' && str.charAt(i) <= '9')
          k = str.charAt(i) - '0';
        else if (str.charAt(i) >= 'a' && str.charAt(i) <= 'z')
          k = str.charAt(i) - 'a' + 17;
        else if (str.charAt(i) >= 'A' && str.charAt(i) <= 'Z')
          k = str.charAt(i) - 'A' + 17;
        else continue;
        sw += width[k];
      }
      cx -= sw * base[0] / 2.0;
      cy -= sw * base[1] / 2.0;
      cz -= sw * base[2] / 2.0;
    }
  
    int plot_index = 0;
    /* draw left justified text */
    for (i=0; i<len; i++) {
      if (str.charAt(i) == '-')
        k = 10;
      else if (str.charAt(i) == '.')
        k = 11;
      else if (str.charAt(i) == '=')
        k = 12;
      else if (str.charAt(i) == '{')
        k = 13;
      else if (str.charAt(i) == '}')
        k = 14;
      else if (str.charAt(i) == ' ')
        k = 15;
      else if (str.charAt(i) == '_')
        k = 16;
      else if (str.charAt(i) >= '0' && str.charAt(i) <= '9')
        k = str.charAt(i) - '0';
      else if (str.charAt(i) >= 'a' && str.charAt(i) <= 'z')
        k = str.charAt(i) - 'a' + 17;
      else if (str.charAt(i) >= 'A' && str.charAt(i) <= 'Z')
        k = str.charAt(i) - 'A' + 17;
      else continue;

      /* make the vertex array for this character */
      temp = index[k];
      int temp_index = 0;
      if (k==12) {
        // render '=' as two seperate lines
        // v2 = verts[k]/2;
        // for (j=0; j<v2; j++) {
        for (j=0; j<verts[k]; j++) {
          double x, y;
          x = temp[temp_index];
          temp_index++;
          y = temp[temp_index];
          temp_index++;
          plot[plot_index] = (float) (cx + x * base[0] + y * up[0]);
          plot[plot_index + 1] = (float) (cy + x * base[1] + y * up[1]);
          plot[plot_index + 2] = (float) (cz + x * base[2] + y * up[2]);
/*
          if (plot_index > 0) {
            plot[plot_index + 3] = plot[plot_index];
            plot[plot_index + 4] = plot[plot_index + 1];
            plot[plot_index + 5] = plot[plot_index + 2];
            plot_index += 3;
          }
*/
          plot_index += 3;
        }
/*
        for (j=v2; j<verts[k]; j++) {
          double x, y;
          x = temp[temp_index];
          temp_index++;
          y = temp[temp_index];
          temp_index++;
          plot[plot_index] = (float) (cx + x * base[0] + y * up[0]);
          plot[plot_index + 1] = (float) (cy + x * base[1] + y * up[1]);
          plot[plot_index + 2] = (float) (cz + x * base[2] + y * up[2]);
          if (plot_index > 0) {
            plot[plot_index + 3] = plot[plot_index];
            plot[plot_index + 4] = plot[plot_index + 1];
            plot[plot_index + 5] = plot[plot_index + 2];
            plot_index += 3;
          }
          plot_index += 3;
        }
*/
      }
      else {
        for (j=0; j<verts[k]; j++) {
          double x, y;
          x = temp[temp_index];
          temp_index++;
          y = temp[temp_index];
          temp_index++;
          plot[plot_index] = (float) (cx + x * base[0] + y * up[0]);
          plot[plot_index + 1] = (float) (cy + x * base[1] + y * up[1]);
          plot[plot_index + 2] = (float) (cz + x * base[2] + y * up[2]);
          if (0 < j && j < verts[k] - 1) {
            plot[plot_index + 3] = plot[plot_index];
            plot[plot_index + 4] = plot[plot_index + 1];
            plot[plot_index + 5] = plot[plot_index + 2];
            plot_index += 3;
          }
          plot_index += 3;
        }
      }
      /* calculate position for next char */
      cx += width[k] * base[0];
      cy += width[k] * base[1];
      cz += width[k] * base[2];
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
  public static String shortString(double val) {
    String s = null;
    int is = (val < 0.0) ? -1 : 1;
    val = Math.abs(val);
    int i = (int) (1000 * val);
    int i1000 = i / 1000;
    int i1 = i - 1000 * i1000;
    String s1000 = (is > 0) ? Integer.toString(i1000) :
                              "-" + Integer.toString(i1000);
    if (i1 == 0) {
      s = s1000;
    }
    else {
      String s1 = Integer.toString(i1);
      if (s1.length() == 3) {
        s = s1000 + "." + s1;
      }
      else if (s1.length() == 2) {
        s = s1000 + ".0" + s1;
      }
      else {
        s = s1000 + ".00" + s1;
      }
    }
    return s;
  }

}

